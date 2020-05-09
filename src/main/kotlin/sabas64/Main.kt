package sabas64

import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import sabas64.rules.VariableNameChecker
import kotlin.system.exitProcess

object Main {
    fun applyAllRules(input: CharStream, issueReporter: IssueReporter) {
        val lexer = BasicLexer(input)
        val parser = BasicParser(CommonTokenStream(lexer))
        val program = parser.program()
        val listeners = listOf<BasicListener>(
            VariableNameChecker(issueReporter)
        )
        ParseTreeWalker.DEFAULT.walk(ProxyListener(listeners), program)
    }
}

fun main(args: Array<String>) {
    val issueReporter = IssueReporter.StdOut()
    val input = if (args.isEmpty()) {
        CharStreams.fromStream(System.`in`)
    } else {
        CharStreams.fromFileName(args[0])
    }

    Main.applyAllRules(input, issueReporter)

    if (issueReporter.issueCount > 0) {
        println("${issueReporter.issueCount} issues found")
    } else {
        println("No issues found")
    }
    exitProcess(issueReporter.issueCount)
}
