package com.vendkit.shelfcount

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vendkit.shelfcount.data.AppDatabase
import com.vendkit.shelfcount.data.ItemDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Runs on a device or emulator against a real (in-memory) SQLite database. */
@RunWith(AndroidJUnit4::class)
class ItemDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: ItemDao

    @Before fun setUp() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, AppDatabase::class.java).build()
        dao = db.items()
    }

    @After fun tearDown() = db.close()

    @Test fun firstScanCreatesItemWithOne() = runTest {
        val item = dao.countScan("4006381333931", "EAN-13", "Item 3931", 1L)
        assertEquals(1, item.quantity)
        assertEquals("Item 3931", item.name)
    }

    @Test fun scanningAgainAddsToSameRow() = runTest {
        dao.countScan("4006381333931", "EAN-13", "Item 3931", 1L)
        dao.countScan("4006381333931", "EAN-13", "Item 3931", 2L)
        val again = dao.countScan("4006381333931", "EAN-13", "Item 3931", 3L)
        assertEquals(3, again.quantity)
        assertEquals(1, dao.observeAll().first().size)
    }

    @Test fun renameIsKeptOnNextScan() = runTest {
        val first = dao.countScan("5012345678900", "EAN-13", "Item 8900", 1L)
        dao.upsert(first.copy(name = "Wool Beanie"))
        assertEquals("Wool Beanie", dao.countScan("5012345678900", "EAN-13", "Item 8900", 2L).name)
    }

    @Test fun listIsAlphabeticalAndStaysPutWhenCounting() = runTest {
        dao.countScan("1", "Code 128", "wool beanie", 1L)
        dao.countScan("2", "Code 128", "Canvas Tote", 2L)
        dao.countScan("1", "Code 128", "wool beanie", 3L) // counting again must not move the row
        assertEquals(listOf("Canvas Tote", "wool beanie"), dao.observeAll().first().map { it.name })
    }

    @Test fun deleteRemovesItem() = runTest {
        val item = dao.countScan("1", "Code 128", "Item 1", 1L)
        dao.delete(item)
        assertNull(dao.find("1"))
    }
}
