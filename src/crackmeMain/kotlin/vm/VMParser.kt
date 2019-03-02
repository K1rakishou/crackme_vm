package crackme.vm

import crackme.vm.core.*
import crackme.vm.instructions.*
import crackme.vm.meta.NativeFunction
import crackme.vm.operands.*
import platform.windows.GetTickCount
import kotlin.random.Random

class VMParser {

  fun parse(program: String): VM {
    val lines = program.split("\n")
      .map { it.trim() }
      .filterNot { it.isEmpty() }

    var instructionIndex = 0
    val instructions = mutableListOf<Instruction>()
    val nativeFunctions = mutableMapOf<NativeFunctionType, NativeFunction>()
    val labels = mutableMapOf<String, Int>()

    for ((programLine, line) in lines.withIndex()) {
      when {
        line.startsWith("use") -> { }
        line.startsWith("@") -> {
          val label = parseLabel(programLine, line)
          labels.put(label, instructionIndex)
        }
        else -> {
          ++instructionIndex
        }
      }
    }

    instructionIndex = 0

    for ((programLine, line) in lines.withIndex()) {
      when {
        line.startsWith("use") -> {
          val nativeFunction = parseNativeFunction(programLine, line)
          nativeFunctions[nativeFunction.type] = nativeFunction
        }
        line.startsWith("@") -> { }
        else -> {
          val instruction = parseInstruction(programLine, line, nativeFunctions, labels)
          instructions.add(instruction)

          ++instructionIndex
        }
      }
    }

    return VM(
      nativeFunctions,
      instructions,
      listOf(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L),
      VmStack(1024),
      VmMemory(16384, Random(GetTickCount().toInt()))
    )
  }

  private fun parseLabel(programLine: Int, line: String): String {
    val labelName = line.substring(1)
    if (labelName.isEmpty()) {
      throw ParsingException(programLine, "Cannot parse label (${line})")
    }

    return labelName.dropLast(1)
  }

  private fun parseNativeFunction(programLine: Int, line: String): NativeFunction {
    val functionBody = line.substring(4)
    if (functionBody.isEmpty()) {
      throw ParsingException(programLine, "Cannot parse native function body ($line)")
    }

    val parametersStart = functionBody.indexOf('(')
    if (parametersStart == -1) {
      throw ParsingException(programLine, "Cannot parse function's parameters start")
    }

    val parametersEnd = functionBody.indexOf(')', parametersStart)
    if (parametersEnd == -1) {
      throw ParsingException(programLine, "Cannot parse function's parameters end")
    }

    val functionName = functionBody.substring(0, parametersStart)
    val functionType = NativeFunctionType.fromString(functionName)
    if (functionType == null) {
      throw ParsingException(programLine, "Cannot parse function type ($functionName)")
    }

    //"parametersStart + 1" to skip the opening bracket
    val parametersList = functionBody.substring(parametersStart + 1, parametersEnd).split(',')
    if (parametersList.isEmpty()) {
      return NativeFunction(functionType, emptyList())
    }

    val functionParameters = parametersList
      .map { parameter ->
        val parameterType = ParameterType.fromString(parameter)
        if (parameterType == null) {
          throw ParsingException(programLine, "Unknown parameter type ($parameter)")
        }

        parameterType!!
      }

    return NativeFunction(functionType, functionParameters)
  }

  private fun parseInstruction(
    programLine: Int,
    line: String,
    nativeFunctions: MutableMap<NativeFunctionType, NativeFunction>,
    labels: MutableMap<String, Int>
  ): Instruction {
    val indexOfFirstSpace = line.indexOfFirst { it == ' ' }
    if (indexOfFirstSpace == -1) {
      throw ParsingException(programLine, "Cannot parse instruction name ($line)")
    }

    val instructionName = line.substring(0, indexOfFirstSpace).toLowerCase()
    val body = line.substring(indexOfFirstSpace)

    return when (instructionName) {
      "mov" -> parseMov(programLine, body)
      "add" -> parseAdd(programLine, body)
      "cmp" -> parseCmp(programLine, body)
      "je",
      "jne",
      "jmp" -> parseJxx(programLine, instructionName, body, labels)
      "call" -> parseCall(programLine, body, nativeFunctions)
      "ret" -> parseRet(programLine, body)
      else -> throw ParsingException(programLine, "Unknown instruction name")
    }
  }

  private fun parseRet(programLine: Int, body: String): Instruction {
    return Ret(parseOperand(programLine, body.trim()))
  }

  private fun parseCall(programLine: Int, body: String, nativeFunctions: MutableMap<NativeFunctionType, NativeFunction>): Instruction {
    TODO("parseCall not implemented")
  }

  private fun parseJxx(programLine: Int, instructionName: String, body: String, labels: MutableMap<String, Int>): Instruction {
    val jumpType = JumpType.fromString(instructionName)
    if (jumpType == null) {
      throw ParsingException(programLine, "Cannot parse jump type from instruction name ($instructionName)")
    }

    val labelName = body.trim()
    if (labels[labelName] == null) {
      throw ParsingException(programLine, "Label with name ($labelName) does not exist in the labels map")
    }

    return Jxx(jumpType, labels.getValue(labelName))
  }

  private fun parseCmp(programLine: Int, body: String): Instruction {
    if (body.isEmpty()) {
      throw ParsingException(programLine, "Instruction has name but does not have a body ($body)")
    }

    if (body.indexOf(',') == -1) {
      throw ParsingException(programLine, "Cannot parse operands because there is no \',\' symbol")
    }

    val (destOperand, srcOperand) = body.split(',')
      .map { it.trim() }

    return Cmp(
      parseOperand(programLine, destOperand),
      parseOperand(programLine, srcOperand)
    )
  }

  private fun parseAdd(programLine: Int, body: String): Instruction {
    if (body.isEmpty()) {
      throw ParsingException(programLine, "Instruction has name but does not have a body ($body)")
    }

    if (body.indexOf(',') == -1) {
      throw ParsingException(programLine, "Cannot parse operands because there is no \',\' symbol")
    }

    val (destOperand, srcOperand) = body.split(',')
      .map { it.trim() }

    return Add(
      parseOperand(programLine, destOperand),
      parseOperand(programLine, srcOperand)
    )
  }

  private fun parseMov(programLine: Int, body: String): Instruction {
    if (body.isEmpty()) {
      throw ParsingException(programLine, "Instruction has name but does not have a body ($body)")
    }

    if (body.indexOf(',') == -1) {
      throw ParsingException(programLine, "Cannot parse operands because there is no \',\' symbol")
    }

    val (destOperand, srcOperand) = body.split(',')
      .map { it.trim() }

    return Mov(
      parseOperand(programLine, destOperand),
      parseOperand(programLine, srcOperand)
    )
  }

  private fun parseOperand(programLine: Int, operandString: String): Operand {
    val ch = operandString[0].toLowerCase()

    when {
      ch == 'r' -> {
        val registerIndex = numberRegex.find(operandString)?.value?.toInt()
        if (registerIndex == null) {
          throw ParsingException(programLine, "Cannot parse register index for operand ($operandString)")
        }

        return Registers(registerIndex)
      }
      ch == '[' -> {
        TODO("memory type")
      }
      ch == '-' || ch.isDigit() -> {
        val constantString = numberRegex.find(operandString)?.value
        if (constantString == null) {
          throw ParsingException(programLine, "Cannot parse constant operand ($operandString)")
        }

        return extractConstant(programLine, constantString)
      }
      else -> throw ParsingException(programLine, "Cannot parse operand for ($operandString)")
    }
  }

  private fun extractConstant(programLine: Int, constantString: String): Constant {
    if (constantString.isEmpty()) {
      throw ParsingException(programLine, "Constant is empty")
    }

    val extractedValue = constantString.toLongOrNull()
    if (extractedValue == null) {
      throw ParsingException(programLine, "Cannot parse constant ($constantString), unknown error")
    }

    //TODO: add C16 and C8 here when needed
    if (extractedValue >= Int.MIN_VALUE && extractedValue <= Int.MAX_VALUE) {
      return C32(extractedValue.toInt())
    }

    return C64(extractedValue)
  }

  companion object {
    val numberRegex = Regex("-?\\d+")
  }
}