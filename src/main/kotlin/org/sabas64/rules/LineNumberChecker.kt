package org.sabas64.rules

import org.sabas64.BasicBaseListener
import org.sabas64.BasicParser.LineContext
import org.sabas64.Issue
import org.sabas64.IssueReporter
import org.sabas64.value

class LineNumberChecker(private val issueReporter: IssueReporter) : BasicBaseListener() {
    private var previousLineNumber: Int? = null

    override fun enterLine(line: LineContext) {
        previousLineNumber?.let { prev ->
            if (line.lineNumber == null) {
                issueReporter.reportIssue(Issue.Priority.WARNING, line, "Add a line number to this line.")
                return
            }
            val current = line.lineNumber.value
            if (prev >= current) {
                issueReporter.reportIssue(Issue.Priority.WARNING, line.lineNumber, "Non-increasing line number from $prev to $current.")
            }
            previousLineNumber = current
        }
        if (previousLineNumber == null) {
            previousLineNumber = line.lineNumber?.value
        }
    }
}
