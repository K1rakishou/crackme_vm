package crackme.vm.parser

import crackme.vm.core.ParsingException
import crackme.vm.operands.C32
import crackme.vm.operands.C64
import crackme.vm.operands.Constant

class ConstactExtractor {

  fun extractConstant(
    functionName: String,
    functionLine: Int,
    constantString: String
  ): Constant {
    if (constantString.isEmpty()) {
      throw ParsingException(functionName, functionLine, "Constant is empty")
    }

    val isNegative = constantString.startsWith('-')
    val isHex = constantString.contains("0x")

    if (isNegative && isHex) {
      throw ParsingException(
        functionName,
        functionLine,
        "Numeric constant cannot be hexadecimal and negative and the same time!"
      )
    }

    //remove the '0x' at the beginning of the string if the string is hexadecimal
    val string = if (isHex) {
      constantString.substring(2)
    } else {
      constantString
    }

    val radix = if (isHex) {
      16
    } else {
      10
    }

    val extractedValue32 = string.toIntOrNull(radix)
    if (extractedValue32 != null) {
      return C32(extractedValue32)
    }

    val extractedValue64 = string.toLongOrNull(radix)
    if (extractedValue64 == null) {
      throw ParsingException(functionName, functionLine, "Cannot parse constant ($extractedValue64), unknown error")
    }

    return C64(extractedValue64)
  }

}