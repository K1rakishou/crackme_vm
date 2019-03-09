package crackme.vm.core.os

import kotlinx.cinterop.*
import platform.windows.GetSystemTime
import platform.windows._SYSTEMTIME

class WinTime : Time {

  override fun getCurrentTime(): Long {
    return memScoped {
      val systemTime = allocPointerTo<_SYSTEMTIME>()
      GetSystemTime(systemTime.value)

      return@memScoped systemTime.pointed!!
        .let { time -> (time.wSecond * 1000UL) + time.wMilliseconds }
        .let { it.toLong() }
    }
  }

}