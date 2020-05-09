package sabas64

import org.antlr.v4.runtime.ParserRuleContext

data class Issue(
    val location: Location,
    val message: String
) {
    constructor(tree: ParserRuleContext, message: String) : this(Location.fromParseTree(tree), message)

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
        }
    }
}
