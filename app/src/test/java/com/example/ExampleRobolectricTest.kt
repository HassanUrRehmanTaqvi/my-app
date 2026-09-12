package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("College Attendance", appName)
  }

  @Test
  fun `test csv parser line splitting`() {
    val line = "101,\"Khan, Muhammad\",Ahmad,03001234567,First Year,A,Pre-Medical,\"Biology, Chemistry\""
    val tokens = com.example.util.CsvParserHelper.parseCsvLine(line)
    assertEquals(8, tokens.size)
    assertEquals("101", tokens[0])
    assertEquals("Khan, Muhammad", tokens[1])
    assertEquals("Biology, Chemistry", tokens[7])
  }
}
