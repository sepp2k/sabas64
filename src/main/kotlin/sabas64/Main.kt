package sabas64

import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.antlr.v4.runtime.CharStreams
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    var fileNameIndex = 0
    var outputJson = false
    if (args.size > 0 && args[0] == "--json") {
        fileNameIndex++
        outputJson = true
    }
    val input = if (args.size > fileNameIndex) {
        CharStreams.fromFileName(args[fileNameIndex])
    } else {
        CharStreams.fromStream(System.`in`)
    }

    val issueReporter = if (outputJson) IssueReporter.ToList() else IssueReporter.StdOut()
    val analyzer = Analyzer(issueReporter)

    analyzer.applyAllRules(input)

    if (outputJson) {
        val json = Json(JsonConfiguration.Stable)
        println(json.toJson(Issue.serializer().list, (issueReporter as IssueReporter.ToList).issues))
    } else {
        if (issueReporter.issueCount > 0) {
            println("${issueReporter.issueCount} issues found")
        } else {
            println("No issues found")
        }
    }
    exitProcess(issueReporter.issueCount)
}
