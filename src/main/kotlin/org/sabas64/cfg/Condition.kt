package org.sabas64.cfg

import org.sabas64.BasicParser.*

sealed class Condition {
    class Expression(val tree: ExpressionContext) : Condition()
    class ForCondition(val indexVariable: IdentifierContext, val bound: ExpressionContext) : Condition()
}
