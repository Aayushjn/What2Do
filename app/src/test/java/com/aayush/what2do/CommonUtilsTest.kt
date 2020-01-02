package com.aayush.what2do

import com.aayush.what2do.util.common.capitalize
import com.aayush.what2do.util.common.formattedDate
import com.aayush.what2do.util.common.formattedString
import com.aayush.what2do.util.common.formattedTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*


class CommonUtilsTest {
    @Test
    fun capitalization_isCorrect() {
        val empty = ""
        val nonAlphabetic = "!432test"
        val alphabetic = "testString"
        val capitalized = "TestString"

        assertEquals(empty, empty.capitalize())
        assertEquals(nonAlphabetic, nonAlphabetic.capitalize())
        assertEquals(capitalized, alphabetic.capitalize())
        assertEquals(capitalized, capitalized.capitalize())
    }

    @Test
    fun dateFormatting_isCorrect() {
        val timeFormat1 = "k:mm"
        val timeFormat2 = "h:mm a"

        val calendar: Calendar = Calendar.getInstance()
        calendar.set(2020, 0, 2, 23, 15, 0)
        val date: Date = calendar.time

        assertEquals("2 Jan, 2020", date.formattedDate())
        assertEquals("23:15", date.formattedTime(timeFormat1))
        assertEquals("11:15 PM", date.formattedTime(timeFormat2))
        assertEquals("2 Jan, 2020 11:15:00 PM", date.formattedString())
    }
}
