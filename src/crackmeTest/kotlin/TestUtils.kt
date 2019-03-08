package sample.helloworld

import kotlin.test.assertEquals
import kotlin.test.fail

inline fun <reified T : Exception> expectException(func: () -> Unit): T {
  try {
    func()
  } catch (error: Throwable) {
    assertEquals(error::class, T::class)
    return error as T
  }

  fail("Exception ${T::class} did not occur")
}