package org.sabas64

import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.antlr.v4.runtime.Token

class SyntaxErrorListener() : BaseErrorListener() {
    private val syntaxErrorListeners = mutableListOf<(Token, String) -> Unit>()

    private val missingNewlineAtEofListeners = mutableListOf<(Token) -> Unit>()

    fun registerSyntaxErrorListener(listener: (Token, String) -> Unit) {
        syntaxErrorListeners.add(listener)
    }

    fun registerMissingNewlineAtEofListener(listener: (Token) -> Unit) {
        missingNewlineAtEofListeners.add(listener)
    }

    var validSyntax = true

    override fun syntaxError(parser: Recognizer<*, *>, offendingSymbol: Any, line: Int, column: Int, msg: String, e: RecognitionException?) {
        val token = offendingSymbol as Token
        if (msg.matches("""^\w+ input '<EOF>' expecting \{.*'\n'.*}$""".toRegex())) {
            for (listener in missingNewlineAtEofListeners) {
                listener(token)
            }
        } else {
            for (listener in syntaxErrorListeners) {
                listener(token, msg)
            }
            validSyntax = false
        }
    }
}
