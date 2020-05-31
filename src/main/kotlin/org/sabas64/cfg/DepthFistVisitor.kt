package org.sabas64.cfg

abstract class DepthFistVisitor {
    protected val visited: MutableSet<BasicBlock> = mutableSetOf()

    fun traverse(block: BasicBlock) {
        if (!visited.contains(block)) {
            visited.add(block)
            visit(block)
            for (successor in block.successors) {
                traverse(successor)
            }
        }
    }

    abstract fun visit(block: BasicBlock)

    companion object {
        fun dfs(startBlock: BasicBlock, f: (BasicBlock) -> Unit) {
            FromFunction(f).traverse(startBlock)
        }
    }

    private class FromFunction(private val f: (BasicBlock) -> Unit) : DepthFistVisitor() {
        override fun visit(block: BasicBlock) {
            f(block)
        }
    }
}
