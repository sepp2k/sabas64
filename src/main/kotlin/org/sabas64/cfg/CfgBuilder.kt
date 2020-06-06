package org.sabas64.cfg

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.apache.logging.log4j.LogManager
import org.sabas64.*
import org.sabas64.BasicParser.*
import java.util.ArrayDeque

class CfgBuilder private constructor(private val issueReporter: IssueReporter) : BasicBaseVisitor<Unit>() {
    private class BasicBlockImpl(override val isProgramEntry: Boolean) : BasicBlock {
        private val logger = LogManager.getLogger()
        private lateinit var term: Terminator
        override val simpleStatements: MutableList<SimpleStatement> = mutableListOf()
        override var terminator: Terminator
            get() = term
            set(value) {
                if (::term.isInitialized) {
                    logger.error("Overridden terminator from $term to $value")
                }
                term = value
                for (successor in value.successors) {
                    (successor as BasicBlockImpl).predecessors.add(this)
                }
            }
        override val predecessors: MutableList<BasicBlock> = mutableListOf()
        override var isSubroutineEntry: Boolean = false
    }

    companion object {
        fun build(program: ProgramContext, issueReporter: IssueReporter): Cfg? {
            val builder = CfgBuilder(issueReporter)
            builder.visit(program)
            if (builder.isValid) {
                return Cfg(
                    programEntry = builder.programEntry,
                    allBlocks = builder.allBasicBlocks,
                    dataItems = builder.dataItems,
                    subroutines = builder.subroutines
                )
            }
            return null
        }
    }

    private val allBasicBlocks = mutableListOf<BasicBlock>()
    private val allLineNumbers = mutableSetOf<Int>()
    private val jumpTargetContexts = mutableSetOf<JumpTargetContext>()
    private val jumpTargets = mutableMapOf<Int, BasicBlockImpl>()
    private val subroutines = mutableMapOf<Int, BasicBlockImpl>()
    private val programEntry = newBasicBlock(isProgramEntry = true)
    private var currentBasicBlock = programEntry
    private val dataItems = mutableListOf<DataItemContext>()
    private var isValid = true

    private class LoopInfo(
        val forLoop: ForStatementContext,
        val conditionBlock: BasicBlockImpl,
        val afterLoop: BasicBlockImpl
    )
    private var loopStack = ArrayDeque<LoopInfo>()

    // This will be set to the current line number at the beginning of processing each line.
    // If a line is missing a line number, we use the same numbering scheme as petcat: number of previous line + 2
    // or 10 for the first line. So we initialize to 8 because 8 + 2 = 10
    private var currentLine: Int = 8

    override fun visitProgram(program: ProgramContext) {
        ParseTreeWalker.DEFAULT.walk(jumpCollector, program)

        visitChildren(program)

        currentBasicBlock.terminator = Terminator.End(null)
        for (loopInfo in loopStack) {
            error(loopInfo.forLoop, "Add a NEXT statement for this for loop.")
        }
        for (target in jumpTargetContexts) {
            if (!allLineNumbers.contains(target.value)) {
                error(target, "Fix this jump to a non-existent line number.")
            }
        }
        // Don't clean up invalid code because it may contain unterminated blocks, that'd cause exceptions in the
        // cleanup code
        if (isValid) {
            cleanUpUnreachableGeneratedBlocks()
        }
    }

    override fun visitLine(line: LineContext) {
        currentLine = line.lineNumber?.value ?: currentLine + 2
        allLineNumbers.add(currentLine)
        jumpTargets[currentLine]?.let { block ->
            currentBasicBlock.terminator = Terminator.UnconditionalBranch(block, null)
            currentBasicBlock = block
        }

        visitChildren(line)
    }

    override fun visitSimpleStatementWrapper(statement: SimpleStatementWrapperContext) {
        currentBasicBlock.simpleStatements.add(SimpleStatement.SourceStatement(statement.simpleStatement()))
    }
    override fun visitDataStatement(data: DataStatementContext) {
        dataItems.addAll(data.items)
    }

    override fun visitComment(comment: CommentContext?) {
        // Comments don't need to appear in the CFG
    }

    override fun visitGotoStatement(goto: GotoStatementContext) {
        val target = jumpTargets[goto.jumpTarget().value]!!
        currentBasicBlock.terminator = Terminator.UnconditionalBranch(target, goto)
        currentBasicBlock = newBasicBlock()
    }

    override fun visitEndStatement(end: EndStatementContext) {
        currentBasicBlock.terminator = Terminator.End(end)
        currentBasicBlock = newBasicBlock()
    }

    override fun visitReturnStatement(ret: ReturnStatementContext) {
        currentBasicBlock.terminator = Terminator.Return(ret)
        currentBasicBlock = newBasicBlock()
    }

    override fun visitOnStatement(onStatement: OnStatementContext) {
        if (onStatement.isGoto) {
            val nextBlock = newBasicBlock()
            currentBasicBlock.terminator = Terminator.SwitchBranch(
                onStatement.value,
                onStatement.targets.map { jumpTargets.getValue(it.value) },
                nextBlock, onStatement
            )
            currentBasicBlock = nextBlock
        }
    }

    override fun visitIfStatement(ifStatement: IfStatementContext) {
        val ifBody = newBasicBlock()
        val afterIf = newBasicBlock()
        currentBasicBlock.terminator = Terminator.ConditionalBranch(
            Condition.Expression(ifStatement.condition), ifBody, afterIf, ifStatement
        )
        currentBasicBlock = ifBody

        visitChildren(ifStatement)

        currentBasicBlock.terminator = Terminator.UnconditionalBranch(afterIf, null)
        currentBasicBlock = afterIf
    }

    override fun visitForStatement(forLoop: ForStatementContext) {
        val id = forLoop.identifier()
        currentBasicBlock.simpleStatements.add(SimpleStatement.GeneratedAssignment(id, forLoop.from))
        val conditionBlock = newBasicBlock()
        val bodyBlock = newBasicBlock()
        val afterLoop = newBasicBlock()
        currentBasicBlock.terminator = Terminator.UnconditionalBranch(conditionBlock, null)
        currentBasicBlock = bodyBlock
        val condition = Condition.ForCondition(id, forLoop.to)
        conditionBlock.terminator = Terminator.ConditionalBranch(condition, bodyBlock, afterLoop, forLoop)
        loopStack.push(LoopInfo(forLoop, conditionBlock, afterLoop))
    }

    override fun visitNextStatement(next: NextStatementContext) {
        if (loopStack.isEmpty()) {
            error(next, "Remove this unmatched NEXT statement.")
            return
        }
        val loopInfo = loopStack.pop()
        val loopId = loopInfo.forLoop.identifier()
        currentBasicBlock.simpleStatements.add(SimpleStatement.GeneratedIncrement(loopId, loopInfo.forLoop.step))
        currentBasicBlock.terminator = Terminator.UnconditionalBranch(loopInfo.conditionBlock, next)
        currentBasicBlock = loopInfo.afterLoop
        next.identifier()?.let { id ->
            if (id.effectiveName != loopId.effectiveName) {
                error(id, "Change this NEXT statement to close the '${loopId.fullName}' loop.")
            }
        }
    }

    /**
     * Remove unreachable blocks created due to the logic of the builder, but not ones corresponding to actual dead code
     * written by the user
     */
    private fun cleanUpUnreachableGeneratedBlocks() {
        allBasicBlocks.removeIf { block ->
            if (block.simpleStatements.isEmpty() && block.terminator.tree == null && !block.isEntry && block.predecessors.isEmpty()) {
                for (successor in block.successors) {
                    (successor as BasicBlockImpl).predecessors.remove(block)
                }
                true
            } else {
                false
            }
        }
    }

    private fun error(tree: ParserRuleContext, message: String) {
        issueReporter.reportIssue(Issue.Priority.ERROR, tree, message)
        isValid = false
    }

    private fun newBasicBlock(isProgramEntry: Boolean = false): BasicBlockImpl {
        val block = BasicBlockImpl(isProgramEntry)
        allBasicBlocks.add(block)
        return block
    }

    private val jumpCollector = object : BasicBaseListener() {
        override fun enterGotoStatement(goto: GotoStatementContext) {
            processGoto(goto.jumpTarget())
        }

        override fun enterIfStatement(ifStatement: IfStatementContext) {
            ifStatement.jumpTarget()?.let(::processGoto)
        }

        override fun enterGosubStatement(gosub: GosubStatementContext) {
            processGosub(gosub.jumpTarget())
        }

        override fun enterOnStatement(onStatement: OnStatementContext) {
            for (target in onStatement.targets) {
                if (onStatement.isGoto) {
                    processGoto(target)
                } else {
                    processGosub(target)
                }
            }
        }

        private fun processGoto(target: JumpTargetContext) {
            jumpTargetContexts.add(target)
            val targetLine = target.value
            if (!jumpTargets.containsKey(targetLine)) {
                jumpTargets[targetLine] = newBasicBlock()
            }
        }

        private fun processGosub(target: JumpTargetContext) {
            jumpTargetContexts.add(target)
            val targetLine = target.value
            val block = jumpTargets[targetLine] ?: newBasicBlock()
            block.isSubroutineEntry = true
            jumpTargets.putIfAbsent(targetLine, block)
            subroutines.putIfAbsent(targetLine, block)
        }
    }
}
