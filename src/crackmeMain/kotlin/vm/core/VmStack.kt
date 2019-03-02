package crackme.vm.core

class VmStack(
  private val size: Int = 1024
) {
  var current: Int = 0
  val array = arrayOfNulls<Long>(size)

  fun push(value: Long) {
    if (current + 1 > size) {
      throw OverflowException()
    }

    array[current++] = value
  }

  fun pop(): Long {
    if (current - 1 < 0) {
      throw UnderflowException()
    }

    return array[current--]!!
  }

  fun peek(index: Int): Long {
    if (index < 0 || index > size) {
      throw OutOfBoundsException(index, size)
    }

    return array[current]!!
  }

  class OverflowException : Exception("Stack overflow")
  class UnderflowException : Exception("Stack is empty")
  class OutOfBoundsException(val index: Int, val upper: Int) : Exception("index is out of bounds (index = ${index}, upperBound = ${upper})")
}