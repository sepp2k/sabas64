package sabas64

import org.antlr.v4.runtime.ParserRuleContext

interface IssueReporter {
    val issueCount: Int

    fun reportIssue(issue: Issue)

    fun reportIssue(tree: ParserRuleContext, message: String) {
        reportIssue(Issue(tree, message))
    }

    class StdOut : IssueReporter {
        override var issueCount: Int = 0

        override fun reportIssue(issue: Issue) {
            val location = issue.location
            val basicLineNumberMessage = location.basicLine?.let { " (BASIC line $it)" } ?: ""
            println("${location.fileName}:${location.actualLine}$basicLineNumberMessage: ${issue.message}")
            issueCount++
        }
    }

    class ToList : IssueReporter {
        val issues = mutableListOf<Issue>()

        override val issueCount: Int
            get() = issues.size

        override fun reportIssue(issue: Issue) {
            issues.add(issue)
        }
    }
}
