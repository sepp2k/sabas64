package sabas64.rules

import sabas64.*
import sabas64.BasicParser.ArrayExpressionContext
import sabas64.BasicParser.ArrayLValueContext
import sabas64.BasicParser.DefStatementContext
import sabas64.BasicParser.DimStatementContext
import sabas64.BasicParser.ForStatementContext
import sabas64.BasicParser.IdentifierContext
import sabas64.BasicParser.ProgramContext
import sabas64.BasicParser.UserDefinedFunctionContext
import sabas64.BasicParser.VariableExpressionContext
import sabas64.BasicParser.VariableLValueContext

class VariableNameChecker(private val issueReporter: IssueReporter) : BasicBaseListener() {
    class IdentifierInfo(val identifier: IdentifierContext) {
        var everAssigned = false
    }

    private val env = Environment<IdentifierInfo>()

    private fun checkId(map: MutableMap<String, IdentifierInfo>, id: IdentifierContext): IdentifierInfo {
        if (id.baseName.length > 2) {
            val message = "Shorten variable name '${id.fullName}' to two characters - additional characters are ignored."
            issueReporter.reportIssue(id, message)
        }
        val idInfo = map.getOrPut(id.effectiveName) { IdentifierInfo(id) }
        if (idInfo.identifier.fullName != id.fullName) {
            val message = "Variable name '${id.fullName}' conflicts with '${idInfo.identifier.fullName}'."
            issueReporter.reportIssue(id, message)
        }
        return idInfo
    }

    override fun enterVariableExpression(variable: VariableExpressionContext) {
        checkId(env.variables, variable.identifier())
    }

    override fun enterVariableLValue(lValue: VariableLValueContext) {
        val idInfo = checkId(env.variables, lValue.identifier())
        idInfo.everAssigned = true
    }

    override fun enterArrayExpression(arrayAccess: ArrayExpressionContext) {
        checkId(env.arrays, arrayAccess.identifier())
    }

    override fun enterArrayLValue(lValue: ArrayLValueContext) {
        val idInfo = checkId(env.arrays, lValue.identifier())
        idInfo.everAssigned = true
    }

    override fun enterDimStatement(dim: DimStatementContext) {
        checkId(env.arrays, dim.identifier())
    }

    override fun enterDefStatement(funDef: DefStatementContext) {
        val idInfo = checkId(env.functions, funDef.name)
        idInfo.everAssigned = true
    }

    override fun enterUserDefinedFunction(function: UserDefinedFunctionContext) {
        checkId(env.functions, function.identifier())
    }

    override fun enterForStatement(forStatement: ForStatementContext) {
        val idInfo = checkId(env.variables, forStatement.identifier())
        idInfo.everAssigned = true
    }

    override fun exitProgram(program: ProgramContext) {
        for (map in listOf(env.variables, env.arrays, env.functions)) {
            for (idInfo in map.values) {
                if (!idInfo.everAssigned) {
                    val message = "Variable '${idInfo.identifier.fullName}' is never assigned a value."
                    issueReporter.reportIssue(idInfo.identifier, message)
                }
            }
        }
    }
}
