package crackme.vm.core.os

import kotlinx.cinterop.*
import platform.posix.*

class WinFile(
  private val path: String
) : File {
  private var file: CPointer<FILE>? = null
  private var openType: OpenType = OpenType.Read
  private var offset: Long = 0L

  override fun open(openType: OpenType): Boolean {
    val mode = when (openType) {
      OpenType.Read -> "r"
      OpenType.Write -> "w"
      OpenType.ReadWrite -> "rw"
    }

    file = fopen(path, mode) ?: return false
    return true
  }

  override fun read(offset: Long, count: Long): ByteArray {
    if (file == null) {
      throw NullPointerException("file is not opened")
    }

    if (!OpenType.canRead(openType)) {
      throw NullPointerException("file is not opened as readable")
    }

    if (fseek(file, offset.toInt(), SEEK_SET) != 0) {
      throw RuntimeException("Could not do fseek at ${offset}")
    }

    return memScoped {
      val nativeArray = allocArray<ByteVar>(count)
      val readCount = fread(nativeArray, sizeOf<ByteVar>().toULong(), count.toULong(), file)

      if (readCount != count.toULong()) {
        throw RuntimeException("Could not read ${count} bytes from the file")
      }

      return@memScoped nativeArray.readBytes(count.toInt())
    }
  }

  override fun write(offset: Long, buffer: ByteArray) {
    if (file == null) {
      throw NullPointerException("file is not opened")
    }

    if (!OpenType.canWrite(openType)) {
      throw NullPointerException("file is not opened as writable")
    }

    if (fseek(file, offset.toInt(), SEEK_SET) != 0) {
      throw RuntimeException("Could not do fseek at ${offset}")
    }

    writeInternal(offset, buffer)
  }

  override fun write(value: Int) {
    val array = ByteArray(4)

    array[0] = ((value shr 24) and 0x000000FF).toByte()
    array[1] = ((value shr 16) and 0x000000FF).toByte()
    array[2] = ((value shr 8) and 0x000000FF).toByte()
    array[3] = (value and 0x000000FF).toByte()

    writeInternal(offset, array)
    offset += 4
  }

  override fun write(value: Long) {
    val array = ByteArray(8)

    array[0] = ((value shr 56) and 0x000000FF).toByte()
    array[1] = ((value shr 48) and 0x000000FF).toByte()
    array[2] = ((value shr 40) and 0x000000FF).toByte()
    array[3] = ((value shr 32) and 0x000000FF).toByte()
    array[4] = ((value shr 24) and 0x000000FF).toByte()
    array[5] = ((value shr 16) and 0x000000FF).toByte()
    array[6] = ((value shr 8) and 0x000000FF).toByte()
    array[7] = ((value) and 0x000000FF).toByte()

    writeInternal(offset, array)
    offset += 8
  }

  override fun write(value: String) {
    write(value.length)
    writeInternal(offset, encodeToUtf8(value))
    offset += value.length
  }

  private fun writeInternal(offset: Long, buffer: ByteArray) {
    memScoped {
      val nativeArray = allocArrayOf(buffer)
      val writeCount = fwrite(nativeArray, sizeOf<ByteVar>().toULong(), buffer.size.toULong(), file)

      if (writeCount != buffer.size.toULong()) {
        throw RuntimeException("Could not write ${buffer.size} bytes to the file")
      }
    }
  }

  override fun close() {
    if (file == null) {
      return
    }

    fclose(file)
    file = null
  }

  enum class OpenType {
    Read,
    Write,
    ReadWrite;

    companion object {
      fun canRead(openType: OpenType): Boolean {
        return openType == Read || openType == ReadWrite
      }

      fun canWrite(openType: OpenType): Boolean {
        return openType == Write || openType == ReadWrite
      }
    }
  }

  companion object {
    fun withFileDo(path: String, openType: OpenType, func: (file: File) -> Unit) {
      val file = WinFile(path)
      if (!file.open(openType)) {
        throw RuntimeException("Could not open file ${path}")
      }

      try {
        func(file)
      } finally {
        file.close()
      }
    }
  }
}