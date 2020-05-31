package org.sabas64.cfg

import org.sabas64.BasicParser.*

sealed class Terminator {
    abstract val successors: List<BasicBlock>
    abstract val tree: StatementContext?

    class UnconditionalBranch(
        val target: BasicBlock,
        override val tree: StatementContext?
    ) : Terminator() {
        override val successors = listOf(target)
    }

    class ConditionalBranch(
        val condition: Condition,
        val ifTrue: BasicBlock,
        val ifFalse: BasicBlock,
        override val tree: StatementContext
    ) : Terminator() {
        override val successors = listOf(ifTrue, ifFalse)
    }

    class SwitchBranch(
        val value: ExpressionContext,
        val targets: List<BasicBlock>,
        val nextLine: BasicBlock,
        override val tree: StatementContext
    ) : Terminator() {
        override val successors = targets + nextLine
    }

    class Return(override val tree: StatementContext) : Terminator() {
        override val successors = listOf<BasicBlock>()
    }

    class End(override val tree: StatementContext?) : Terminator() {
        override val successors = listOf<BasicBlock>()
    }
}
