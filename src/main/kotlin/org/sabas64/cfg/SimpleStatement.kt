package org.sabas64.cfg

import org.sabas64.BasicParser.*

sealed class SimpleStatement {
    open val tree: SimpleStatementContext? = null

    class SourceStatement(override val tree: SimpleStatementContext) : SimpleStatement()
    class GeneratedAssignment(val variable: IdentifierContext, val value: ExpressionContext) : SimpleStatement()
    class GeneratedIncrement(val variable: IdentifierContext, val value: ExpressionContext?) : SimpleStatement()
}
