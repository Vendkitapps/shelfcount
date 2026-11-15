package com.vendkit.shelfcount

import com.vendkit.shelfcount.data.Item
import com.vendkit.shelfcount.logic.Barcodes
import com.vendkit.shelfcount.ui.summary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BarcodesTest {
    @Test fun acceptsValidEan13() = assertTrue(Barcodes.isValidEan13("4006381333931"))
    @Test fun rejectsWrongCheckDigit() = assertFalse(Barcodes.isValidEan13("4006381333932"))
    @Test fun rejectsLettersAndLength() {
        assertFalse(Barcodes.isValidEan13("40063813339A1"))
        assertFalse(Barcodes.isValidEan13("400638133393"))
    }
    @Test fun defaultNameUsesLastFourDigits() = assertEquals("Item 3931", Barcodes.defaultName("4006381333931"))

    @Test fun summaryCountsProductsAndUnits() {
        val items = listOf(Item("1", "A", 12, "EAN-13", 0), Item("2", "B", 1, "EAN-13", 0))
        assertEquals("2 products · 13 units", summary(items))
        assertEquals("1 product · 1 unit", summary(items.drop(1)))
    }
}
