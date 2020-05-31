package org.sabas64

import kotlinx.serialization.Serializable

@Serializable
data class TestCase(
    val name: String,
    val basicFileName: String? = null,
    val basicSource: String? = null,
    val parseTree: String? = null,
    val issues: List<MiniIssue>? = null
) {
    class ValidationError(name: String, message: String) : Exception("In test case $name: $message")

    @Serializable
    data class MiniIssue(
        val message: String,
        val actualLine: Int
    ) {
        override fun toString(): String = "$actualLine: $message"
    }

    fun validate() {
        if (basicFileName == null && basicSource == null) {
            throw ValidationError(name, "Must specify either a filename or the source code")
        }
        if (basicFileName != null && basicSource != null) {
            throw ValidationError(name, "Can't specify both a filename and the source code")
        }
        if (parseTree == null && issues == null) {
            throw ValidationError(name, "Must specify at least one requirement for success")
        }
    }
}
