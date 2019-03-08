package sample.helloworld

import kotlin.test.*

class UtilsTest {

  @Test
  fun testShouldNotExceedWhenStringIsLongMinValue() {
    val str = Long.MIN_VALUE.toString()
    assertNotEquals(null, str.toLongOrNull())
  }

  @Test
  fun testShouldNotExceedWhenStringIsLongMaxValue() {
    val str = Long.MAX_VALUE.toString()
    assertNotEquals(null, str.toLongOrNull())
  }

  @Test
  fun testShouldExceedWhenStringIsLessThanLongMinValue() {
    val str = "-9223372036854775809"
    assertEquals(null, str.toLongOrNull())
  }

  @Test
  fun testShouldExceedWhenStringIsGreaterThanLongMaxValue() {
    val str = "9223372036854775808"
    assertEquals(null, str.toLongOrNull())
  }
}