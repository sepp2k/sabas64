package org.sabas64

import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.sabas64.cfg.CfgBuilder
import org.sabas64.rules.*

class Analyzer(private val issueReporter: IssueReporter) {
    fun parse(input: CharStream): BasicParser.ProgramContext? {
        val lexer = BasicLexer(input)
        val parser = BasicParser(CommonTokenStream(lexer))
        parser.removeErrorListeners()
        val syntaxChecker = SyntaxChecker(issueReporter)
        parser.addErrorListener(syntaxChecker)
        val program = parser.program()
        return if (syntaxChecker.validSyntax) {
            program
        } else {
            null
        }
    }

    fun applyAllRules(program: BasicParser.ProgramContext) {
        val listeners = listOf<BasicListener>(
            VariableNameChecker(issueReporter),
            LineNumberChecker(issueReporter),
            TypeChecker(issueReporter)
        )
        ParseTreeWalker.DEFAULT.walk(ProxyListener(listeners), program)

        val cfgRules = listOf<CfgRule>(
            DeadCodeChecker(issueReporter)
        )
        CfgBuilder.build(program, issueReporter)?.let { cfg ->
            for (rule in cfgRules) {
                rule.processCfg(cfg)
            }
        }
    }

    fun applyAllRules(input: CharStream) {
        parse(input)?.let(::applyAllRules)
    }
}
