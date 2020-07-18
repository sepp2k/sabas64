package org.sabas64.rules

import org.sabas64.Analyzer

interface Rule {
    val name: String
    fun apply(analyzer: Analyzer)

    val flagName: String
        get() = name.replace(' ', '-').toLowerCase()
}
