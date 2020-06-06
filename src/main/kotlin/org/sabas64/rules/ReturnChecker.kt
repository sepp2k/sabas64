package org.sabas64.rules

import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.sabas64.*
import org.sabas64.BasicParser.*
import org.sabas64.cfg.Cfg
import org.sabas64.cfg.DepthFistVisitor
import org.sabas64.cfg.Terminator

class ReturnChecker(private val issueReporter: IssueReporter, private val program: ProgramContext) : CfgRule, BasicBaseListener() {
    private val subroutinesWithoutReturn = mutableSetOf<Int>()

    override fun processCfg(cfg: Cfg) {
        DepthFistVisitor.dfs(cfg.programEntry) { block ->
            (block.terminator as? Terminator.Return)?.let { returnStatement ->
                issueReporter.reportIssue(
                    Issue.Priority.ERROR, returnStatement.tree,
                    "Remove this RETURN statement or prevent it from being reachable outside of a subroutine."
                )
            }
        }
        for ((subroutineLineNumber, subroutineEntryPoint) in cfg.subroutines) {
            var hasReachableReturn = false
            DepthFistVisitor.dfs(subroutineEntryPoint) { block ->
                if (block.terminator is Terminator.Return) {
                    hasReachableReturn = true
                }
            }
            if (!hasReachableReturn) {
                subroutinesWithoutReturn.add(subroutineLineNumber)
            }
        }
        ParseTreeWalker.DEFAULT.walk(this, program)
    }

    override fun enterGosubStatement(gosub: GosubStatementContext) {
        checkGosub(gosub.jumpTarget())
    }

    override fun enterOnStatement(on: OnStatementContext) {
        if (on.isGosub) {
            for (target in on.targets) {
                checkGosub(target)
            }
        }
    }

    private fun checkGosub(target: JumpTargetContext) {
        if (subroutinesWithoutReturn.contains(target.value)) {
            issueReporter.reportIssue(Issue.Priority.ERROR, target, "Change this GOSUB to GOTO or add a RETURN to the called subroutine.")
        }
    }
}
