package sabas64

import kotlinx.serialization.Serializable
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token

@Serializable
data class Issue(
    val location: Location,
    val message: String,
    val priority: Priority
) {
    constructor(tree: ParserRuleContext, message: String, priority: Priority)
        : this(Location.fromParseTree(tree), message, priority)

    constructor(token: Token, message: String, priority: Priority)
        : this(Location.fromToken(token), message, priority)

    @Serializable
    data class Location(
        val fileName: String,
        val basicLine: Int?,
        val actualLine: Int,
        val startColumn: Int,
        val endColumn: Int
    ) {
        companion object {
            fun fromParseTree(tree: ParserRuleContext): Location {
                val fileName = tree.start.inputStream.sourceName
                val basicLineNumber = tree.lineContext?.lineNumber?.value
                val actualLineNumber = tree.start.line
                val startColumn = tree.start.charPositionInLine
                val endColumn = tree.stop.charPositionInLine + tree.stop.text.length
                return Location(fileName, basicLineNumber, actualLineNumber, startColumn, endColumn)
            }

            fun fromToken(token: Token): Location {
                val fileName = token.inputStream.sourceName
                val actualLineNumber = token.line
                val startColumn = token.charPositionInLine
                val endColumn = token.charPositionInLine + token.text.length
                return Location(fileName, null, actualLineNumber, startColumn, endColumn)
            }
        }
    }

    enum class Priority {
        WARNING, ERROR
    }
}
