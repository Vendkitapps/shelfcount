package com.vendkit.shelfcount.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One counted product. The barcode is the key, so scanning it again adds to the same row. */
@Entity(tableName = "items")
data class Item(
    @PrimaryKey val barcode: String,
    val name: String,
    val quantity: Int,
    val format: String,
    val updatedAt: Long,
)
