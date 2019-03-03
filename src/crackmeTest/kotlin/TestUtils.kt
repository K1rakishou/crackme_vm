package sample.helloworld

import kotlin.test.assertEquals
import kotlin.test.fail

inline fun <reified T : Exception> expectException(func: () -> Unit) {
  try {
    func()
  } catch (error: Throwable) {
    assertEquals(error::class, T::class)
    return
  }

  fail("Exception ${T::class} did not occur")
}