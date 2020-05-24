package sabas64.cfg

import sabas64.BasicParser.*

sealed class Condition {
    class Expression(tree: ExpressionContext) : Condition()
    class ForCondition(indexVariable: IdentifierContext, bound: ExpressionContext) : Condition()
}