package org.sabas64.cfg

interface BasicBlock {
    val simpleStatements: List<SimpleStatement>
    val terminator: Terminator
    val predecessors: List<BasicBlock>
    val isProgramEntry: Boolean
    val isSubroutineEntry: Boolean

    val successors: List<BasicBlock>
        get() = terminator.successors

    val isEntry: Boolean
        get() = isProgramEntry || isSubroutineEntry
}
