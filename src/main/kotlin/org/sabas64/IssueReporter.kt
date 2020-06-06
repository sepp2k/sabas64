package org.sabas64

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.tree.TerminalNode

interface IssueReporter {
    val issueCount: Int

    fun reportIssue(issue: Issue)

    fun reportIssue(priority: Issue.Priority, tree: ParserRuleContext, message: String) {
        reportIssue(Issue(tree, message, priority))
    }

    fun reportIssue(priority: Issue.Priority, token: Token, message: String) {
        reportIssue(Issue(token, message, priority))
    }

    fun reportIssue(priority: Issue.Priority, node: TerminalNode, message: String) {
        reportIssue(Issue(node, message, priority))
    }

    class StdOut : IssueReporter {
        override var issueCount: Int = 0

        override fun reportIssue(issue: Issue) {
            val location = issue.location
            val basicLineNumberMessage = location.basicLine?.let { " (BASIC line $it)" } ?: ""
            val prio = issue.priority.toString().toLowerCase().capitalize()
            println("$prio: ${location.fileName}:${location.actualLine}$basicLineNumberMessage: ${issue.message}")
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
