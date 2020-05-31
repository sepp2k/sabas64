package org.sabas64.rules

import org.sabas64.cfg.Cfg

interface CfgRule {
    fun processCfg(cfg: Cfg)
}
