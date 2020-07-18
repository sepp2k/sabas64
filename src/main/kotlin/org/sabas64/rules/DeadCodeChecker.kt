package org.sabas64.rules

import org.antlr.v4.runtime.ParserRuleContext
import org.sabas64.Analyzer
import org.sabas64.Issue
import org.sabas64.IssueReporter
import org.sabas64.cfg.BasicBlock
import org.sabas64.cfg.Cfg
import org.sabas64.cfg.DepthFistVisitor

object DeadCodeChecker : Rule {
    override fun apply(analyzer: Analyzer) {
        val visitor = object : DepthFistVisitor() {
            override fun visit(block: BasicBlock) {
                // Do nothing because all we need is for the visited block to be added to the visited set, which happens
                // automatically
            }

            val reachable = visited
        }
        visitor.traverse(analyzer.cfg.programEntry)

        for (subroutineEntry in analyzer.cfg.subroutines.values) {
            visitor.traverse(subroutineEntry)
        }

        for (block in analyzer.cfg.allBlocks) {
            if (!visitor.reachable.contains(block)) {
                for (statement in block.simpleStatements) {
                    report(statement.tree)
                }
                report(block.terminator.tree)
            }
        }
    }

    private fun report(analyzer: Analyzer, tree: ParserRuleContext?) {
        if (tree != null) {
            analyzer.issueReporter.reportIssue(Issue.Priority.WARNING, tree, "Remove this unreachable code or make it reachable.")
        }
    }
}
