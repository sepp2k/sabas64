package sabas64

import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import sabas64.BasicParser.ProgramContext
import sabas64.rules.LineNumberChecker
import sabas64.rules.TypeChecker
import sabas64.rules.VariableNameChecker
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val issueReporter = IssueReporter.StdOut()
    val analyzer = Analyzer(issueReporter)
    val input = if (args.isEmpty()) {
        CharStreams.fromStream(System.`in`)
    } else {
        CharStreams.fromFileName(args[0])
    }

    analyzer.applyAllRules(input)

    if (issueReporter.issueCount > 0) {
        println("${issueReporter.issueCount} issues found")
    } else {
        println("No issues found")
    }
    exitProcess(issueReporter.issueCount)
}
