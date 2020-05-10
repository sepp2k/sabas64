package sabas64.rules

import org.antlr.v4.runtime.tree.RuleNode
import sabas64.*
import sabas64.BasicParser.*

class TypeChecker(private val issueReporter: IssueReporter) : BasicBaseListener() {
    /**
     * @param argumentTypes List containing the required type of each argument. For arguments that can take either type,
     *                      the corresponding entry in the list should be null
     */
    private data class FunctionSignature(val returnType: Type, val argumentTypes: List<Type?>) {
        constructor(returnType: Type, vararg argumentTypes: Type?) : this(returnType, argumentTypes.toList())
    }

    companion object {
        private val functionSignatures = mapOf(
            "abs" to FunctionSignature(Type.NUMBER, Type.NUMBER),
            "asc" to FunctionSignature(Type.NUMBER, Type.STRING),
            "atn" to FunctionSignature(Type.NUMBER, Type.NUMBER),
            "chr$" to FunctionSignature(Type.STRING, Type.NUMBER),
            "cos" to FunctionSignature(Type.NUMBER, Type.NUMBER),
            "exp" to FunctionSignature(Type.NUMBER, Type.NUMBER),
            "fre" to FunctionSignature(Type.NUMBER, null),
            "int" to FunctionSignature(Type.NUMBER, Type.NUMBER),
            "left$" to FunctionSignature(Type.STRING, Type.STRING, Type.NUMBER),
            "len" to FunctionSignature(Type.NUMBER, Type.STRING),
            "log" to FunctionSignature(Type.NUMBER, Type.NUMBER),
            "mid$" to FunctionSignature(Type.STRING, Type.STRING, Type.NUMBER, Type.NUMBER),
            "peek" to FunctionSignature(Type.NUMBER, Type.NUMBER),
            "pos" to FunctionSignature(Type.NUMBER, null),
            "right$" to FunctionSignature(Type.STRING, Type.STRING, Type.NUMBER),
            "rnd" to FunctionSignature(Type.NUMBER, Type.NUMBER),
            "sgn" to FunctionSignature(Type.NUMBER, Type.NUMBER),
            "sin" to FunctionSignature(Type.NUMBER, Type.NUMBER),
            "spc" to FunctionSignature(Type.IO, Type.NUMBER),
            "sqr" to FunctionSignature(Type.NUMBER, Type.NUMBER),
            "str$" to FunctionSignature(Type.STRING, Type.NUMBER),
            "tab" to FunctionSignature(Type.IO, Type.NUMBER),
            "tan" to FunctionSignature(Type.NUMBER, Type.NUMBER),
            "usr" to FunctionSignature(Type.NUMBER, null),
            "val" to FunctionSignature(Type.NUMBER, Type.STRING)
        )
    }

    private val expressionTypeChecker = object : BasicBaseVisitor<Type>() {
        override fun visitVariableExpression(variable: VariableExpressionContext): Type {
            return variable.identifier().type
        }

        override fun visitPiExpression(pi: PiExpressionContext): Type {
            return Type.NUMBER
        }

        override fun visitNumberExpression(number: NumberExpressionContext): Type {
            return Type.NUMBER
        }

        override fun visitStringExpression(string: StringExpressionContext): Type {
            return Type.STRING
        }

        override fun visitArrayExpression(arrayAccess: ArrayExpressionContext): Type {
            for (indexExpression in arrayAccess.indices) {
                expectType(indexExpression, Type.NUMBER)
            }
            return arrayAccess.identifier().type
        }

        override fun visitFunctionCallExpression(call: FunctionCallExpressionContext): Type {
            return when (val func = call.function()) {
                is BuiltInFunctionContext ->
                    typeCheckBuiltInFunctionCall(func, call)
                is UserDefinedFunctionContext -> {
                    for (argument in call.arguments) {
                        expectType(argument, Type.NUMBER)
                    }
                    Type.NUMBER
                }
                else ->
                    throw IllegalStateException("Unknown type of function ${func.javaClass.simpleName}")
            }
        }

        private fun typeCheckBuiltInFunctionCall(func: BuiltInFunctionContext, call: FunctionCallExpressionContext): Type {
            val sig = functionSignatures.getValue(func.text)
            if (call.arguments.size == sig.argumentTypes.size) {
                for ((argument, expected) in call.arguments.zip(sig.argumentTypes)) {
                    if (expected == null) {
                        notIo(argument)
                    } else {
                        expectType(argument, expected)
                    }
                }
            } else {
                issueReporter.reportIssue(call, "Arity mismatch: Given ${call.arguments.size}, expected ${sig.argumentTypes.size}.")
                for (argument in call.arguments) {
                    visit(argument)
                }
            }
            return sig.returnType
        }

        override fun visitInfixExpression(expression: InfixExpressionContext): Type = when (expression.operator.text) {
            "<", ">", "<=", ">=", "=", "<>" -> {
                val operandType = notIo(expression.lhs)
                expectType(expression.rhs, operandType)
                Type.NUMBER
            }
            "-", "*", "/", "^", "and", "or" -> {
                expectType(expression.lhs, Type.NUMBER)
                expectType(expression.rhs, Type.NUMBER)
                Type.NUMBER
            }
            "+" -> {
                val operandType = notIo(expression.lhs)
                expectType(expression.rhs, operandType)
                operandType
            }
            else ->
                throw IllegalStateException("Unknown operator ${expression.operator.text}")
        }

        override fun visitPrefixExpression(expression: PrefixExpressionContext): Type {
            expectType(expression.operand, Type.NUMBER)
            return Type.NUMBER
        }

        override fun visitChildren(node: RuleNode): Type {
            throw IllegalStateException("Unknown type of expression ${node.javaClass.simpleName}")
        }
    }

    override fun enterArrayLValue(arrayAccess: ArrayLValueContext) {
        for (index in arrayAccess.indices) {
            expectType(index, Type.NUMBER)
        }
    }

    override fun enterAssignmentStatement(assignment: AssignmentStatementContext) {
        expectType(assignment.expression(), assignment.lValue().type)
    }

    override fun enterPrintArgument(arg: PrintArgumentContext) {
        expressionTypeChecker.visit(arg.expression())
    }

    override fun enterPokeStatement(poke: PokeStatementContext) {
        expectType(poke.target, Type.NUMBER)
        expectType(poke.value, Type.NUMBER)
    }

    override fun enterDefStatement(def: DefStatementContext) {
        expectType(def.body, Type.NUMBER)
    }

    override fun enterIfStatement(ifStatement: IfStatementContext) {
        notIo(ifStatement.condition)
    }

    override fun enterForStatement(forStatement: ForStatementContext) {
        if (forStatement.identifier().type != Type.NUMBER) {
            issueReporter.reportIssue(forStatement.identifier(), "Iteration variables must be numeric.")
        }
        expectType(forStatement.from, Type.NUMBER)
        expectType(forStatement.to, Type.NUMBER)
        forStatement.step?.let { step ->
            expectType(step, Type.NUMBER)
        }
    }

    override fun enterDimStatement(dim: DimStatementContext) {
        for (dimension in dim.dimensions) {
            expectType(dimension, Type.NUMBER)
        }
    }

    private fun notIo(expression: ExpressionContext): Type {
        val type = expressionTypeChecker.visit(expression)
        if (type == Type.IO) {
            issueReporter.reportIssue(expression, "SPC and TAB are only allowed as arguments to IO functions like PRINT.")
        }
        return type
    }

    private fun expectType(expression: ExpressionContext, expected: Type) {
        val actual = notIo(expression)
        if (actual != expected && actual != Type.IO) {
            issueReporter.reportIssue(expression, "Expected expression of type $expected, but got $actual.")
        }
    }
}