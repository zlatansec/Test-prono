package com.pronoagg.aggregator.data.local

enum class ChannelSource {
    TELEGRAM, PRONOSOFT;

    companion object {
        fun fromStorage(value: String): ChannelSource =
            entries.firstOrNull { it.name == value } ?: TELEGRAM
    }
}
