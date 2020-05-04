package sabas64

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import sabas64.rules.VariableNameChecker
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val lexer = BasicLexer(CharStreams.fromStream(System.`in`))
    val parser = BasicParser(CommonTokenStream(lexer))
    val program = parser.program()
    val issueReporter = IssueReporter()
    val listeners = listOf<BasicListener>(
        VariableNameChecker(issueReporter)
    )
    ParseTreeWalker.DEFAULT.walk(ProxyListener(listeners), program)
    if (issueReporter.issueCount > 0) {
        println("${issueReporter.issueCount} issues found")
    } else {
        println("No issues found")
    }
    exitProcess(issueReporter.issueCount)
}