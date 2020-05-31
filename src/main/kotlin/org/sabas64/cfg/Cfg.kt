package org.sabas64.cfg

import org.sabas64.BasicParser.DataItemContext

class Cfg(
    val programEntry: BasicBlock,
    val subroutines: Map<Int, BasicBlock>,
    val allBlocks: List<BasicBlock>,
    val dataItems: List<DataItemContext>
)
