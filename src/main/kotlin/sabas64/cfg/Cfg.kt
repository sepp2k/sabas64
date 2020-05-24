package sabas64.cfg

import sabas64.BasicParser

class Cfg(
    val programEntry: BasicBlock,
    val subroutines: Map<Int, BasicBlock>,
    val allBlocks: List<BasicBlock>,
    val dataItems: List<BasicParser.DataItemContext>
)
