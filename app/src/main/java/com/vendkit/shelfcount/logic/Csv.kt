package com.vendkit.shelfcount.logic

import com.vendkit.shelfcount.data.Item
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Builds the CSV export. Pure Kotlin, so it is covered by plain unit tests. */
object Csv {
    const val HEADER = "barcode,name,quantity,format,last_counted"
    private val stamp = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    fun build(items: List<Item>, zone: ZoneId = ZoneId.systemDefault()): String = buildString {
        append(HEADER).append("\r\n")
        for (item in items) {
            val time = stamp.format(Instant.ofEpochMilli(item.updatedAt).atZone(zone))
            append(listOf(item.barcode, item.name, item.quantity.toString(), item.format, time).joinToString(",") { cell(it) })
            append("\r\n")
        }
    }

    /**
     * Quotes a cell when needed and stops spreadsheet formula injection:
     * a name such as "=HYPERLINK(...)" is written as text, not run as a formula.
     */
    fun cell(raw: String): String {
        val safe = if (raw.isNotEmpty() && raw[0] in "=+-@\t\r") "'$raw" else raw
        return if (safe.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"" + safe.replace("\"", "\"\"") + "\""
        } else safe
    }
}
