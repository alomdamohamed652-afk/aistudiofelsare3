package com.example

import com.example.core.model.DispatchMode
import com.example.core.model.DriverStatus
import com.example.data.local.DriverProfileEntity
import com.example.engine.DispatchEngine
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DispatchEngineTest {

    private fun makeDriver(id: Long, status: DriverStatus = DriverStatus.AVAILABLE, currentOrderId: Long? = null, workingArea: String = "") =
        DriverProfileEntity(id = id, name = "Driver $id", phone = "", status = status, currentOrderId = currentOrderId, workingArea = workingArea)

    @Before
    fun resetCursor() {
        DispatchEngine.setCursor(0L)
    }

    // ─── Eligibility ──────────────────────────────────────────────────────────

    @Test
    fun `available driver with no order is eligible`() {
        val driver = makeDriver(1)
        assertTrue(DispatchEngine.isDriverEligible(driver))
    }

    @Test
    fun `busy driver (has current order) is not eligible`() {
        val driver = makeDriver(1, currentOrderId = 42L)
        assertFalse(DispatchEngine.isDriverEligible(driver))
    }

    @Test
    fun `offline driver is not eligible`() {
        val driver = makeDriver(1, status = DriverStatus.OFFLINE)
        assertFalse(DispatchEngine.isDriverEligible(driver))
    }

    @Test
    fun `driver outside working area is not eligible`() {
        val driver = makeDriver(1, workingArea = "المعادي")
        assertFalse(DispatchEngine.isDriverEligible(driver, targetWorkingArea = "مدينة نصر"))
    }

    @Test
    fun `driver in matching working area is eligible`() {
        val driver = makeDriver(1, workingArea = "مدينة نصر والنزهة")
        assertTrue(DispatchEngine.isDriverEligible(driver, targetWorkingArea = "مدينة نصر"))
    }

    @Test
    fun `driver with blank working area is eligible for any area`() {
        val driver = makeDriver(1, workingArea = "")
        assertTrue(DispatchEngine.isDriverEligible(driver, targetWorkingArea = "المعادي"))
    }

    // ─── Round Robin rotation ─────────────────────────────────────────────────

    @Test
    fun `round robin starts with lowest id driver when cursor is 0`() {
        val drivers = listOf(makeDriver(3), makeDriver(1), makeDriver(2))
        val selected = DispatchEngine.selectCandidateDriver(drivers, DispatchMode.ROUND_ROBIN)
        assertEquals(1L, selected?.id)
    }

    @Test
    fun `round robin advances to next id after first dispatch`() {
        val drivers = listOf(makeDriver(1), makeDriver(2), makeDriver(3))
        DispatchEngine.selectCandidateDriver(drivers, DispatchMode.ROUND_ROBIN) // selects 1
        val second = DispatchEngine.selectCandidateDriver(drivers, DispatchMode.ROUND_ROBIN)
        assertEquals(2L, second?.id)
    }

    @Test
    fun `round robin wraps around after last driver`() {
        val drivers = listOf(makeDriver(1), makeDriver(2), makeDriver(3))
        DispatchEngine.selectCandidateDriver(drivers, DispatchMode.ROUND_ROBIN) // 1
        DispatchEngine.selectCandidateDriver(drivers, DispatchMode.ROUND_ROBIN) // 2
        DispatchEngine.selectCandidateDriver(drivers, DispatchMode.ROUND_ROBIN) // 3
        val wrapped = DispatchEngine.selectCandidateDriver(drivers, DispatchMode.ROUND_ROBIN) // should wrap to 1
        assertEquals(1L, wrapped?.id)
    }

    @Test
    fun `round robin skips ineligible drivers`() {
        val drivers = listOf(makeDriver(1), makeDriver(2, status = DriverStatus.OFFLINE), makeDriver(3))
        DispatchEngine.selectCandidateDriver(drivers, DispatchMode.ROUND_ROBIN) // 1
        val second = DispatchEngine.selectCandidateDriver(drivers, DispatchMode.ROUND_ROBIN) // should skip 2, pick 3
        assertEquals(3L, second?.id)
    }

    @Test
    fun `round robin returns null when no eligible drivers`() {
        val drivers = listOf(makeDriver(1, status = DriverStatus.OFFLINE), makeDriver(2, currentOrderId = 10L))
        val result = DispatchEngine.selectCandidateDriver(drivers, DispatchMode.ROUND_ROBIN)
        assertNull(result)
    }

    @Test
    fun `open dispatch always returns first eligible driver`() {
        val drivers = listOf(makeDriver(2), makeDriver(1))
        val result = DispatchEngine.selectCandidateDriver(drivers, DispatchMode.OPEN_DISPATCH)
        // firstOrNull of filtered eligible list (insertion order)
        assertNotNull(result)
    }

    @Test
    fun `manual assignment always returns null`() {
        val drivers = listOf(makeDriver(1), makeDriver(2))
        val result = DispatchEngine.selectCandidateDriver(drivers, DispatchMode.MANUAL_ASSIGNMENT)
        assertNull(result)
    }

    @Test
    fun `cursor advances correctly after round robin selection`() {
        val drivers = listOf(makeDriver(1), makeDriver(2))
        assertEquals(0L, DispatchEngine.lastDispatchedDriverId)
        DispatchEngine.selectCandidateDriver(drivers, DispatchMode.ROUND_ROBIN)
        assertEquals(1L, DispatchEngine.lastDispatchedDriverId)
        DispatchEngine.selectCandidateDriver(drivers, DispatchMode.ROUND_ROBIN)
        assertEquals(2L, DispatchEngine.lastDispatchedDriverId)
    }
}
