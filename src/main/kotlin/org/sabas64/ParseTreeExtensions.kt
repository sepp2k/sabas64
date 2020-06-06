package org.sabas64

import org.antlr.v4.runtime.RuleContext
import org.apache.logging.log4j.LogManager
import org.sabas64.BasicParser.*

val RuleContext.lineContext: LineContext?
private val logger = LogManager.getLogger()
    get() = when (this) {
        is LineContext -> this
        else -> parent?.lineContext
    }

val IntLiteralContext.value: Int
    get() = text.toInt()

val NumberExpressionContext.value: Float
    get() = text.toFloat()

val JumpTargetContext.value: Int
    get() = intLiteral()?.value ?: 0

val LValueContext.type: Type
    get() = when (this) {
        is VariableLValueContext -> identifier().type
        is ArrayLValueContext -> identifier().type
        else -> {
            logger.error("Unknown type of l-value: ${javaClass.simpleName}")
            Type.NUMBER
        }
    }

val IdentifierContext.baseName
    get() = letters.joinToString("") { it.text }

val IdentifierContext.suffix
    get() = sigil?.text ?: ""

val IdentifierContext.fullName
    get() = "$baseName$suffix"

val IdentifierContext.effectiveName
    get() = "${baseName.take(2)}$suffix"

val IdentifierContext.type
    get() = when (sigil?.type) {
        null -> Type.NUMBER
        BasicLexer.PERCENT -> Type.NUMBER
        BasicLexer.DOLLAR -> Type.STRING
        else -> {
            logger.error("Unknown sigil: '${sigil.text}'")
            Type.NUMBER
        }
    }
