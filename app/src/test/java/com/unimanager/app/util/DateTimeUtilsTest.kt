package com.unimanager.app.util

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Unit Tests for date calculations
 */
class DateTimeUtilsTest {

    @Test
    fun `ChronoUnit DAYS between gives correct total days`() {
        val today = LocalDate.now()
        val futureDate = today.plusDays(30)
        val daysBetween = ChronoUnit.DAYS.between(today, futureDate).toInt()
        assertEquals(30, daysBetween)
    }

    @Test
    fun `ChronoUnit DAYS between for past date returns negative`() {
        val today = LocalDate.now()
        val pastDate = today.minusDays(5)
        val daysBetween = ChronoUnit.DAYS.between(today, pastDate).toInt()
        assertEquals(-5, daysBetween)
    }

    @Test
    fun `ChronoUnit DAYS between same date returns 0`() {
        val today = LocalDate.now()
        val daysBetween = ChronoUnit.DAYS.between(today, today).toInt()
        assertEquals(0, daysBetween)
    }

    @Test
    fun `Period vs ChronoUnit difference`() {
        // Period.between فقط يأخذ جزء الأيام في الشهر
        // بينما ChronoUnit.DAYS.between يأخذ المجموع الكلي
        val today = LocalDate.of(2026, 1, 1)
        val futureDate = LocalDate.of(2026, 3, 1) // شهرين لاحقاً = 59 يوم

        // ChronoUnit يعطي القيمة الصحيحة
        val chronoResult = ChronoUnit.DAYS.between(today, futureDate).toInt()
        assertEquals(59, chronoResult)

        // Period.days يعطي فقط الجزء اليومي (0 في هذه الحالة)
        val periodResult = java.time.Period.between(today, futureDate).days
        assertEquals(0, periodResult) // خطأ! هذا ما كان يحدث قبل الإصلاح
    }

    @Test
    fun `large date range with ChronoUnit`() {
        val today = LocalDate.of(2026, 1, 1)
        val futureDate = LocalDate.of(2027, 6, 1) // 516 يوم تقريباً

        val daysBetween = ChronoUnit.DAYS.between(today, futureDate).toInt()
        assertTrue(daysBetween > 500)
        assertTrue(daysBetween < 520)
    }
}
