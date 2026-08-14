package com.pronoagg.aggregator.data.local

enum class PronoOutcome {
    UNKNOWN, WON, LOST, VOID;

    companion object {
        fun fromStorage(value: String): PronoOutcome =
            entries.firstOrNull { it.name == value } ?: UNKNOWN
    }
}
