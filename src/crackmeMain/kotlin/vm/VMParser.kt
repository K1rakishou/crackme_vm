package crackme.vm

import crackme.vm.instructions.Add
import crackme.vm.instructions.Cmp
import crackme.vm.instructions.Instruction
import crackme.vm.instructions.Mov
import crackme.vm.operands.*

class VMParser {

  fun parse(program: String): VM {
    val lines = program.split("\n")
      .map { it.trim() }
      .filterNot { it.isEmpty() }

    val instructionList = lines.mapIndexed { index, str ->
      val instruction = tryParseInstruction(index + 1, str)
      if (instruction == null) {
        //TODO: parse labels etc
      }

      instruction
    }.filterNotNull()

    return VM(
      emptyMap(),
      instructionList,
      listOf(0UL, 0UL, 0UL, 0UL, 0UL, 0UL, 0UL, 0UL)
    )
  }

  private fun tryParseInstruction(lineNum: Int, line: String): Instruction? {
    val indexOfFirstSpace = line.indexOfFirst { it == ' ' }
    if (indexOfFirstSpace == -1) {
      return null
    }

    val instructionName = line.substring(0, indexOfFirstSpace).toLowerCase()
    val body = line.substring(indexOfFirstSpace)

    return when (instructionName) {
      "mov" -> parseMov(lineNum, body)
      "add" -> parseAdd(lineNum, body)
      "cmp" -> parseCmp(lineNum, body)
      "je",
      "jne",
      "jmp" -> parseJxx(lineNum, instructionName, body)
      "call" -> parseCall(lineNum, body)
      else -> null
    }
  }

  private fun parseCall(lineNum: Int, body: String): Instruction? {
    return null
  }

  private fun parseJxx(lineNum: Int, instructionName: String, body: String): Instruction? {
    return null
  }

  private fun parseCmp(lineNum: Int, body: String): Instruction? {
    if (body.isEmpty()) {
      throw ParsingException(lineNum, "Instruction has name but does not have a body")
    }

    if (body.indexOf(',') == -1) {
      throw ParsingException(lineNum, "Cannot parse operands because there is no \',\' symbol")
    }

    val (destOperand, srcOperand) = body.split(',')
      .map { it.trim() }

    return Cmp(
      parseOperand(lineNum, destOperand),
      parseOperand(lineNum, srcOperand)
    )
  }

  private fun parseAdd(lineNum: Int, body: String): Instruction? {
    if (body.isEmpty()) {
      throw ParsingException(lineNum, "Instruction has name but does not have a body")
    }

    if (body.indexOf(',') == -1) {
      throw ParsingException(lineNum, "Cannot parse operands because there is no \',\' symbol")
    }

    val (destOperand, srcOperand) = body.split(',')
      .map { it.trim() }

    return Add(
      parseOperand(lineNum, destOperand),
      parseOperand(lineNum, srcOperand)
    )
  }

  private fun parseMov(lineNum: Int, body: String): Instruction? {
    if (body.isEmpty()) {
      throw ParsingException(lineNum, "Instruction has name but does not have a body")
    }

    if (body.indexOf(',') == -1) {
      throw ParsingException(lineNum, "Cannot parse operands because there is no \',\' symbol")
    }

    val (destOperand, srcOperand) = body.split(',')
      .map { it.trim() }

    return Mov(
      parseOperand(lineNum, destOperand),
      parseOperand(lineNum, srcOperand)
    )
  }

  private fun parseOperand(lineNum: Int, operandString: String): Operand {
    val ch = operandString[0].toLowerCase()

    when {
      ch == 'r' -> {
        val registerIndex = numberRegex.find(operandString)?.value?.toInt()
        if (registerIndex == null) {
          throw ParsingException(lineNum, "Cannot parse register index for operand ${operandString}")
        }

        return Registers(registerIndex)
      }
      ch == '[' -> {
        TODO("memory type")
      }
      ch.isDigit() -> {
        val constantString = numberRegex.find(operandString)?.value
        if (constantString == null) {
          throw ParsingException(lineNum, "Cannot parse constant operand ${operandString}")
        }

        return extractConstant(lineNum, constantString)
      }
      else -> throw ParsingException(lineNum, "Cannot parse operand for \'${operandString}\'")
    }
  }

  private fun extractConstant(lineNum: Int, constantString: String): Constant {
    if (constantString.isEmpty()) {
      throw ParsingException(lineNum, "Constant is empty")
    }

    val extractedValue = constantString.toULongOrNull()
    if (extractedValue == null) {
      throw ParsingException(lineNum, "Cannot parse constant ${constantString}, unknown error")
    }

    //TODO: add C16 and C8 here when needed

    if (extractedValue >= UInt.MIN_VALUE && extractedValue <= UInt.MAX_VALUE) {
      return C32(extractedValue.toUInt())
    }

    return C64(extractedValue)
  }

  companion object {
    val numberRegex = Regex("[0-9]+")
  }
}