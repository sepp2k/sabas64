package sabas64.rules

import sabas64.BasicBaseListener
import sabas64.BasicParser.LineContext
import sabas64.IssueReporter
import sabas64.value

class LineNumberChecker(private val issueReporter: IssueReporter) : BasicBaseListener() {
    var previousLineNumber: Int? = null

    override fun enterLine(line: LineContext) {
        previousLineNumber?.let { prev ->
            if (line.lineNumber == null) {
                issueReporter.reportIssue(line, "Add a line number to this line.")
                return
            }
            val current = line.lineNumber.value
            if (prev >= current) {
                issueReporter.reportIssue(line.lineNumber, "Non-increasing line number from $prev to $current.")
            }
            previousLineNumber = current
        }
        if (previousLineNumber == null) {
            previousLineNumber = line.lineNumber?.value
        }
    }
}
