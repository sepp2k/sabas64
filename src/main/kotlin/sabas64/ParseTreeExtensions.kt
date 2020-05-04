package sabas64

import org.antlr.v4.runtime.RuleContext
import sabas64.BasicParser.IdentifierContext
import sabas64.BasicParser.IntLiteralContext
import sabas64.BasicParser.LineContext
import sabas64.BasicParser.NumberExpressionContext

val RuleContext.lineContext: LineContext?
    get() = when (this) {
        is LineContext -> this
        else -> parent?.lineContext
    }

val IntLiteralContext.value: Int
    get() = text.toInt()

val NumberExpressionContext.value: Float
    get() = text.toFloat()

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
        null -> Type.FLOAT
        BasicLexer.PERCENT -> Type.INT
        BasicLexer.DOLLAR -> Type.STRING
        else -> error("")
    }