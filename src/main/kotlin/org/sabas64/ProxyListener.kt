package org.sabas64

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.tree.TerminalNode

class ProxyListener() : BasicBaseListener() {
    private val listeners: MutableList<BasicListener> = mutableListOf()

    fun registerListener(listener: BasicListener) {
        listeners.add(listener)
    }

    override fun enterEveryRule(ctx: ParserRuleContext) {
        for (listener in listeners) {
            listener.enterEveryRule(ctx)
            ctx.enterRule(listener)
        }
    }

    override fun exitEveryRule(ctx: ParserRuleContext) {
        for (listener in listeners) {
            ctx.exitRule(listener)
            listener.exitEveryRule(ctx)
        }
    }

    override fun visitErrorNode(node: ErrorNode) {
        for (listener in listeners) {
            listener.visitErrorNode(node)
        }
    }

    override fun visitTerminal(node: TerminalNode) {
        for (listener in listeners) {
            listener.visitTerminal(node)
        }
    }
}
