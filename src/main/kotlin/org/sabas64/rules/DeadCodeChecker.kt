package org.sabas64.rules

import org.antlr.v4.runtime.ParserRuleContext
import org.sabas64.Issue
import org.sabas64.IssueReporter
import org.sabas64.cfg.BasicBlock
import org.sabas64.cfg.Cfg
import org.sabas64.cfg.DepthFistVisitor

class DeadCodeChecker(private val issueReporter: IssueReporter) : CfgRule, DepthFistVisitor() {
    override fun processCfg(cfg: Cfg) {
        traverse(cfg.programEntry)
        for (subroutineEntry in cfg.subroutines.values) {
            traverse(subroutineEntry)
        }

        for (block in cfg.allBlocks) {
            if (!visited.contains(block)) {
                for (statement in block.simpleStatements) {
                    report(statement.tree)
                }
                report(block.terminator.tree)
            }
        }
    }

    override fun visit(block: BasicBlock) {
        // Do nothing because all we need is for the visited block to be added to the visited set, which happens
        // automatically
    }

    private fun report(tree: ParserRuleContext?) {
        if (tree != null) {
            issueReporter.reportIssue(Issue.Priority.WARNING, tree, "Remove this unreachable code or make it reachable.")
        }
    }
}
