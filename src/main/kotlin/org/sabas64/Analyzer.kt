package org.sabas64

import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.sabas64.cfg.Cfg
import org.sabas64.cfg.CfgBuilder
import org.sabas64.rules.*
import org.sabas64.BasicParser.ProgramContext

class Analyzer(private val issueReporter: IssueReporter, input: CharStream) {
    class ListenerContext(
        val issueReporter: IssueReporter,
        val ast: ProgramContext,
        val astListeners: ProxyListener,
        val cfg: Cfg
    )

    class SyntaxErrorListenerContext

    private val listeners = mutableListOf<(ListenerContext) -> Unit>()

    private val lexer = BasicLexer(input)
    private val parser = BasicParser(CommonTokenStream(lexer))
    private val syntaxErrorListener = SyntaxErrorListener()

    init {
        parser.removeErrorListeners()
    }

    fun registerListener(listener: (ListenerContext) -> Unit) {
        listeners.add(listener)
    }

    fun registerSyntaxErrorListener(listener: (IssueReporter, Token, String) -> Unit) {
        syntaxErrorListener.registerSyntaxErrorListener { token, message ->  }
    }

    fun registerControlFlowErrorListener(listener: (IssueReporter) -> Unit) {
        parser.addErrorListener(listener(issueReporter))
    }

    fun parse(): BasicParser.ProgramContext? {
        val syntaxChecker = SyntaxErrorListener(issueReporter)
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
        val astProcessor = ProxyListener(listeners)
        ParseTreeWalker.DEFAULT.walk(astProcessor, program)

        val cfgRules = listOf<CfgRule>(
            DeadCodeChecker(issueReporter),
            ReturnChecker(issueReporter, program)
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
