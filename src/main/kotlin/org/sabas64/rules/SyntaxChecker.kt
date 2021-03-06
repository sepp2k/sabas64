package org.sabas64.rules

import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.antlr.v4.runtime.Token
import org.sabas64.Issue
import org.sabas64.IssueReporter

class SyntaxChecker(private val issueReporter: IssueReporter) : BaseErrorListener() {
    var validSyntax = true

    override fun syntaxError(parser: Recognizer<*, *>, offendingSymbol: Any, line: Int, column: Int, msg: String, e: RecognitionException?) {
        val token = offendingSymbol as Token
        if (msg.matches("""^\w+ input '<EOF>' expecting \{.*'\n'.*}$""".toRegex())) {
            issueReporter.reportIssue(Issue.Priority.WARNING, token, "Add a line break at the end of the file.")
        } else {
            issueReporter.reportIssue(Issue.Priority.ERROR, token, "Syntax Error: $msg")
            validSyntax = false
        }
    }
}
