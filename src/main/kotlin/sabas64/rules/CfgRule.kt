package sabas64.rules

import sabas64.cfg.Cfg

interface CfgRule {
    fun processCfg(cfg: Cfg)
}