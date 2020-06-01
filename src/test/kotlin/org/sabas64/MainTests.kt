package org.sabas64

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class MainTests {
    private val program = "10 print x\nprint 42\n"

    private val expectedOutputNormal = "Warning: <unknown>:2: Add a line number to this line.\n" +
            "Warning: <unknown>:1 (BASIC line 10): Variable 'x' is never assigned a value.\n" +
            "2 issues found\n"

    private val expectedOutputJson = "[{\"location\":{\"fileName\":\"<unknown>\",\"basicLine\":null,\"actualLine\":2," +
            "\"startColumn\":0,\"endColumn\":9},\"message\":\"Add a line number to this line.\",\"priority\":\"WARNING\"}," +
            "{\"location\":{\"fileName\":\"<unknown>\",\"basicLine\":10,\"actualLine\":1,\"startColumn\":9,\"endColumn\":10}," +
            "\"message\":\"Variable 'x' is never assigned a value.\",\"priority\":\"WARNING\"}]\n"

    @Test
    fun testMainNormal() {
        System.setIn(program.byteInputStream())
        val out = ByteArrayOutputStream()
        System.setOut(PrintStream(out))

        assertEquals(2, intMain(arrayOf()), "Main should return the correct number of issues")
        assertEquals(expectedOutputNormal, out.toString())
    }

    @Test
    fun testMainNormalNoIssues() {
        System.setIn("10 print 42\n".byteInputStream())
        val out = ByteArrayOutputStream()
        System.setOut(PrintStream(out))

        assertEquals(0, intMain(arrayOf()), "Main should return the correct number of issues")
        assertEquals("No issues found\n", out.toString())
    }

    @Test
    fun testMainJson() {
        System.setIn(program.byteInputStream())
        val out = ByteArrayOutputStream()
        System.setOut(PrintStream(out))

        assertEquals(2, intMain(arrayOf("--json")), "Main should return the correct number of issues")
        assertEquals(expectedOutputJson, out.toString())
    }
}
