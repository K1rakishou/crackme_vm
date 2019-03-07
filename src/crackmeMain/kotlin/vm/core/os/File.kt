package crackme.vm.core.os

interface File {
  fun open(openType: WinFile.OpenType): Boolean
  fun read(offset: Long, count: Long): ByteArray
  fun write(offset: Long, buffer: ByteArray)
  fun write(value: Int)
  fun write(value: Long)
  fun write(value: String)
  fun close()
}