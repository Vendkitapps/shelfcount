package com.vendkit.shelfcount.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ItemDao {

    // Alphabetical, so rows stay in place while you tap + and - during a count.
    @Query("SELECT * FROM items ORDER BY name COLLATE NOCASE, barcode")
    abstract fun observeAll(): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE barcode = :barcode")
    abstract suspend fun find(barcode: String): Item?

    @Upsert
    abstract suspend fun upsert(item: Item)

    @Delete
    abstract suspend fun delete(item: Item)

    @Query("DELETE FROM items")
    abstract suspend fun clear()

    /** Adds one to an existing barcode, or creates the item with a count of 1. Runs as one transaction. */
    @Transaction
    open suspend fun countScan(barcode: String, format: String, defaultName: String, now: Long): Item {
        val existing = find(barcode)
        val next = existing?.copy(quantity = existing.quantity + 1, updatedAt = now)
            ?: Item(barcode = barcode, name = defaultName, quantity = 1, format = format, updatedAt = now)
        upsert(next)
        return next
    }
}
