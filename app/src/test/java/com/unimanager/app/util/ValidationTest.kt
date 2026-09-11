package com.unimanager.app.util

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit Tests for Validation utility
 */
class ValidationTest {

    @Test
    fun `validateName with blank name returns failure`() {
        val result = Validation.validateName("")
        assertTrue(result.isFailure)
    }

    @Test
    fun `validateName with valid name returns success`() {
        val result = Validation.validateName("رياضيات")
        assertTrue(result.isSuccess)
        assertEquals("رياضيات", result.getOrNull())
    }

    @Test
    fun `validateName with too long name returns failure`() {
        val longName = "أ".repeat(300)
        val result = Validation.validateName(longName)
        assertTrue(result.isFailure)
    }

    @Test
    fun `validateDate with valid date returns success`() {
        val result = Validation.validateDate("2026-01-15")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `validateDate with invalid format returns failure`() {
        val result = Validation.validateDate("15-01-2026")
        assertTrue(result.isFailure)
    }

    @Test
    fun `validateTime with valid time returns success`() {
        val result = Validation.validateTime("14:30")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `validateTime with invalid hour returns failure`() {
        val result = Validation.validateTime("25:00")
        assertTrue(result.isFailure)
    }

    @Test
    fun `validateTime with invalid minute returns failure`() {
        val result = Validation.validateTime("12:60")
        assertTrue(result.isFailure)
    }

    @Test
    fun `validatePriority with valid values returns success`() {
        assertEquals("high", Validation.validatePriority("high").getOrNull())
        assertEquals("medium", Validation.validatePriority("medium").getOrNull())
        assertEquals("low", Validation.validatePriority("low").getOrNull())
    }

    @Test
    fun `validatePriority with invalid value returns failure`() {
        val result = Validation.validatePriority("urgent")
        assertTrue(result.isFailure)
    }

    @Test
    fun `sanitizeInput removes HTML brackets`() {
        val result = Validation.sanitizeInput("<script>alert('xss')</script>")
        assertFalse(result.contains("<"))
        assertFalse(result.contains(">"))
    }

    @Test
    fun `sanitizeInput preserves Arabic characters`() {
        val result = Validation.sanitizeInput("مرحبا بالعالم")
        assertEquals("مرحبا بالعالم", result)
    }

    @Test
    fun `sanitizeInput preserves academic characters`() {
        val result = Validation.sanitizeInput("x² + y² = z²")
        assertTrue(result.contains("²"))
    }

    @Test
    fun `validateExtension with allowed extension returns success`() {
        val result = Validation.validateExtension("pdf")
        assertTrue(result.isSuccess)
    }

    // سبب الإصلاح: كان اسم هذا الاختبار "with disallowed extension returns failure" بينما
    // يستخدم فعليًا الامتداد "exe" الموجود أصلاً ضمن ALLOWED_EXTENSIONS، فيتحقق من isSuccess
    // لا isFailure — أي أن الاسم يناقض السلوك المُختبَر فعليًا، وهو اختبار مضلِّل لا يغطي مسار
    // الفشل الحقيقي إطلاقًا رغم أن اسمه يوحي بذلك. الإصلاح: تقسيمه إلى اختبارين صحيحين كل
    // منهما يطابق اسمه فعلًا (أحدهما يغطي امتدادًا مسموحًا، والآخر يغطي امتدادًا مرفوضًا فعلًا).
    @Test
    fun `validateExtension with allowed extension exe returns success`() {
        val result = Validation.validateExtension("exe")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `validateExtension with disallowed extension returns failure`() {
        val result = Validation.validateExtension("xyz")
        assertTrue(result.isFailure)
    }

    @Test
    fun `validateExamType with valid type returns success`() {
        val result = Validation.validateExamType("نصفي")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `validateExamType with invalid type returns failure`() {
        val result = Validation.validateExamType("شعبي")
        assertTrue(result.isFailure)
    }
}
