package crackme.vm

import crackme.vm.core.*
import crackme.vm.instructions.*
import crackme.vm.meta.NativeFunction
import crackme.vm.operands.*
import platform.windows.GetTickCount
import kotlin.random.Random

class VMParser {
  private lateinit var instructions: MutableList<Instruction>
  private lateinit var nativeFunctions: MutableMap<NativeFunctionType, NativeFunction>
  private lateinit var labels: MutableMap<String, Int>
  //this list is to keep track of all added string to the memory (their addresses in the memory)
  private lateinit var strings: MutableList<Int>
  private lateinit var vmMemory: VmMemory

  fun parse(program: String): VM {
    instructions = mutableListOf()
    nativeFunctions = mutableMapOf()
    labels = mutableMapOf()
    strings = mutableListOf()
    vmMemory = VmMemory(16384, Random(GetTickCount().toInt()))

    val lines = program.split("\n")
      .map { it.trim() }
      .filterNot { it.isEmpty() }

    var instructionIndex = 0

    for ((programLine, line) in lines.withIndex()) {
      when {
        line.startsWith("use") -> {
        }
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
        line.startsWith("@") -> {
        }
        else -> {
          val instruction = parseInstruction(programLine, line)
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
      vmMemory
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
      throw ParsingException(programLine, "Cannot parse function's parameterTypeList start")
    }

    val parametersEnd = functionBody.indexOf(')', parametersStart)
    if (parametersEnd == -1) {
      throw ParsingException(programLine, "Cannot parse function's parameterTypeList end")
    }

    val functionName = functionBody.substring(0, parametersStart)
    val functionType = NativeFunctionType.fromString(functionName)
    if (functionType == null) {
      throw ParsingException(programLine, "Cannot parse function type ($functionName)")
    }

    //"parametersStart + 1" to skip the opening bracket
    val parametersList = functionBody.substring(parametersStart + 1, parametersEnd).split(',')
    val functionParameters = parametersList
      .map { parameter ->
        val parameterType = ParameterType.fromString(parameter)
        if (parameterType == null) {
          throw ParsingException(programLine, "Unknown parameter type ($parameter)")
        }

        parameterType!!
      }

    return NativeFunction(
      functionType,
      functionParameters,
      NativeFunctionCallbacks.getCallbackByFunctionType(functionType)
    )
  }

  private fun parseInstruction(
    programLine: Int,
    line: String
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
      "jmp" -> parseJxx(programLine, instructionName, body)
      "call" -> parseCall(programLine, body)
      "ret" -> parseRet(programLine, body)
      "let" -> parseLet(programLine, body)
      else -> throw ParsingException(programLine, "Unknown instruction name ($instructionName)")
    }
  }

  private fun parseLet(programLine: Int, body: String): Instruction {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  private fun parseRet(programLine: Int, body: String): Instruction {
    return Ret(parseOperand(programLine, body.trim()))
  }

  private fun parseCall(programLine: Int, body: String): Instruction {
    val functionBody = body.trim()
    if (functionBody.isEmpty()) {
      throw ParsingException(programLine, "Function body is empty")
    }

    val parametersStart = functionBody.indexOf('(')
    if (parametersStart == -1) {
      throw ParsingException(programLine, "Cannot parse function's parameterTypeList start")
    }

    val parametersEnd = functionBody.indexOf(')', parametersStart)
    if (parametersEnd == -1) {
      throw ParsingException(programLine, "Cannot parse function's parameterTypeList end")
    }

    val functionName = functionBody.substring(0, parametersStart)
    println("functionBody = $functionBody")

    val functionType = NativeFunctionType.fromString(functionName)
    if (functionType == null) {
      throw ParsingException(programLine, "Cannot parse function type ($functionName)")
    }

    //"parametersStart + 1" to skip the opening bracket
    val operandsList = functionBody.substring(parametersStart + 1, parametersEnd).split(',')

    val operands = operandsList.map {
      operandString -> parseOperand(programLine, operandString)
    }

    return Call(
      functionType,
      operands
    )
  }

  private fun parseJxx(programLine: Int, instructionName: String, body: String): Instruction {
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

        return Register(registerIndex)
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
      ch == '\"' -> {
        val stringEndIndex = operandString.indexOf('\"', 1)
        if (stringEndIndex == -1) {
          throw ParsingException(programLine, "Cannot find end of the string operand (${operandString})")
        }

        val string = operandString.substring(1, stringEndIndex - 1)

        val lastStringEndIndex = strings.lastOrNull() ?: 0
        val currentStringEndIndex = lastStringEndIndex + string.length

        strings.add(currentStringEndIndex)
        vmMemory.putString(lastStringEndIndex, string)

        return VmString(lastStringEndIndex)
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