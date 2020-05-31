package org.sabas64.rules

import org.antlr.v4.runtime.ParserRuleContext
import org.sabas64.Issue
import org.sabas64.IssueReporter
import org.sabas64.cfg.Cfg

class DeadCodeChecker(private val issueReporter: IssueReporter) : CfgRule {
    override fun processCfg(cfg: Cfg) {
        for (block in cfg.allBlocks) {
            if (!block.isReachable) {
                for (statement in block.simpleStatements) {
                    report(statement.tree)
                }
                report(block.terminator.tree)
            }
        }
    }

    private fun report(tree: ParserRuleContext?) {
        if (tree != null) {
            issueReporter.reportIssue(Issue.Priority.WARNING, tree, "Remove this unreachable code or make it reachable.")
        }
    }
}
