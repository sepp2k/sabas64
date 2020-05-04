package sabas64

import org.antlr.v4.runtime.ParserRuleContext

class IssueReporter {
    var issueCount: Int = 0

    fun reportIssue(tree: ParserRuleContext, message: String) {
        val basicLineNumberMessage = tree.lineContext?.lineNumber?.let { " (BASIC line ${it.value})" } ?: ""
        val fileName = tree.start.inputStream.sourceName
        val actualLineNumber = tree.start.line
        println("$fileName:$actualLineNumber$basicLineNumberMessage: $message")
        issueCount++
    }
}