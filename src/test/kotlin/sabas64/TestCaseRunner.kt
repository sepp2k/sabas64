package sabas64

import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.nio.channels.Channels
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets

class TestCaseRunner {
    private fun charStreamFromResource(fileName: String): CharStream {
        val stream = javaClass.getResourceAsStream(fileName)
        val channel = Channels.newChannel(stream)
        return CharStreams.fromChannel(channel, StandardCharsets.UTF_8, 4096, CodingErrorAction.REPLACE, fileName, -1)
    }

    @TestFactory
    fun testCases(): List<DynamicTest> {
        val json = Json(JsonConfiguration.Stable)
        val testCasesString = javaClass.getResource("test-cases.json").readText()
        val testCases = json.parse(TestCase.serializer().list, testCasesString)
        return testCases.map { testCase ->
            testCase.validate()
            val input =
                testCase.basicSource?.let {src -> CharStreams.fromString(src, testCase.name)} ?:
                charStreamFromResource(testCase.basicFileName!!)
            val name = testCase.basicFileName ?: testCase.name
            DynamicTest.dynamicTest(testCase.name) {
                val reporter = IssueReporter.ToList()
                val analyzer = Analyzer(reporter)
                val tree = analyzer.parse(input)
                testCase.parseTree?.let { expectedTree ->
                    val actual = tree.toStringTree(BasicParser.ruleNames.toList())
                    assertEquals(expectedTree, actual, "Should produce the correct parse tree")
                }
                testCase.issues?.let { testCaseIssues ->
                    analyzer.applyAllRules(tree)
                    val expectedIssues = testCaseIssues.sortedWith(compareBy({ it.actualLine }, { it.message }))
                    val actualIssues = reporter.issues
                        .sortedWith(compareBy({ it.location.actualLine }, { it.message }))
                        .map { TestCase.MiniIssue(it.message, it.location.actualLine) }
                    assertEquals(expectedIssues, actualIssues, "Should report the right issues")
                    for (issue in reporter.issues) {
                        assertEquals(name, issue.location.fileName, "Should report issues with the correct file name")
                    }
                }
            }
        }
    }
}
