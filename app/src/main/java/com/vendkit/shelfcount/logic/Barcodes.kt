package com.vendkit.shelfcount.logic

object Barcodes {

    /** True when [code] is a 13-digit EAN with a correct check digit. */
    fun isValidEan13(code: String): Boolean {
        if (code.length != 13 || !code.all(Char::isDigit)) return false
        val digits = code.map { it - '0' }
        val sum = (0 until 12).sumOf { i -> digits[i] * if (i % 2 == 0) 1 else 3 }
        return (10 - sum % 10) % 10 == digits[12]
    }

    /** Name used until the person renames a newly scanned product. */
    fun defaultName(code: String): String = "Item " + code.takeLast(4)
}
