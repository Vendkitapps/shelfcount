package com.vendkit.shelfcount

import com.vendkit.shelfcount.data.Item
import com.vendkit.shelfcount.logic.Csv
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

class CsvTest {
    private val time = LocalDateTime.of(2026, 11, 16, 9, 30).toInstant(ZoneOffset.UTC).toEpochMilli()
    private fun item(name: String, qty: Int = 3, code: String = "4006381333931") = Item(code, name, qty, "EAN-13", time)

    @Test fun writesHeaderAndRows() {
        val csv = Csv.build(listOf(item("Wool Beanie", 12)), ZoneOffset.UTC)
        val lines = csv.trimEnd().split("\r\n")
        assertEquals(Csv.HEADER, lines[0])
        assertEquals("4006381333931,Wool Beanie,12,EAN-13,2026-11-16 09:30", lines[1])
    }

    @Test fun quotesCommasAndQuotes() {
        assertEquals("\"Socks, wool\"", Csv.cell("Socks, wool"))
        assertEquals("\"The \"\"Big\"\" Tote\"", Csv.cell("The \"Big\" Tote"))
    }

    @Test fun blocksSpreadsheetFormulas() {
        assertEquals("'=HYPERLINK(1)", Csv.cell("=HYPERLINK(1)"))
        assertEquals("'+1", Csv.cell("+1"))
        assertEquals("'@cmd", Csv.cell("@cmd"))
    }

    @Test fun keepsOrderOfItems() {
        val csv = Csv.build(listOf(item("A", code = "1"), item("B", code = "2")), ZoneOffset.UTC)
        val rows = csv.trimEnd().split("\r\n").drop(1).map { it.substringBefore(',') }
        assertEquals(listOf("1", "2"), rows)
    }

    @Test fun emptyListIsJustHeader() {
        assertEquals(Csv.HEADER + "\r\n", Csv.build(emptyList()))
    }
}
