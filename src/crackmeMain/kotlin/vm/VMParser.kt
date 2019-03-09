package crackme.vm

import crackme.vm.core.*
import crackme.vm.core.function.NativeFunctionCallbacks
import crackme.vm.core.function.NativeFunctionType
import crackme.vm.core.VariableType
import crackme.vm.instructions.*
import crackme.vm.core.function.NativeFunction
import crackme.vm.obfuscator.NoOpInstructionObfuscator
import crackme.vm.obfuscator.VMInstructionObfuscator
import crackme.vm.operands.*
import platform.windows.GetTickCount
import kotlin.random.Random

class VMParser(
  private val vmInstructionObfuscator: VMInstructionObfuscator = NoOpInstructionObfuscator()
) {
  private lateinit var instructions: MutableList<Instruction>
  private lateinit var nativeFunctions: MutableMap<NativeFunctionType, NativeFunction>
  private lateinit var labels: MutableMap<String, Int>
  //this list is to keep track of all added string to the memory (their addresses in the memory)
  private lateinit var vmMemory: VmMemory
  private lateinit var vmFlags: VmFlags

  fun parse(program: String): VM {
    instructions = mutableListOf()
    nativeFunctions = mutableMapOf()
    labels = mutableMapOf()
    vmMemory = VmMemory(1024, Random(GetTickCount().toInt()))
    vmFlags = VmFlags()

    val lines = program.split("\n")
      .map { it.trim() }
      .filterNot { it.isEmpty() }


    for ((programLine, line) in lines.withIndex()) {
      if (line.startsWith("@")) {
        val label = parseLabel(programLine, line)
        labels.put(label, -1)
      }
    }

    var instructionIndex = 0

    for ((programLine, line) in lines.withIndex()) {
      if (line.startsWith("use")) {
        val nativeFunction = parseNativeFunction(programLine, line)
        nativeFunctions[nativeFunction.type] = nativeFunction
      } else {
        val trimmed = line.trim()

        if (trimmed.contains('@') && trimmed.endsWith(':')) {
          val labelName = parseLabel(instructionIndex, trimmed)
          if (!labels.containsKey(labelName)) {
            throw ParsingException(instructionIndex, "Label ($labelName) was not defined")
          }

          labels[labelName] = instructionIndex
          continue
        }

        val newInstructions = parseInstruction(programLine, trimmed)

        instructions.addAll(newInstructions)
        instructionIndex += newInstructions.size
      }
    }

    val uninitializedLabels = labels.filter { it.value == -1 }
    if (uninitializedLabels.isNotEmpty()) {
      throw RuntimeException("There are uninitialized labels after parsing: (${uninitializedLabels.map { it.key }})")
    }

    return VM(
      nativeFunctions,
      instructions,
      mutableListOf(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L),
      labels,
      VmStack(1024),
      vmMemory,
      vmFlags
    )
  }

  private fun parseLabel(programLine: Int, line: String): String {
    val labelName = labelRegex.find(line)?.value?.substring(1)
    if (labelName == null) {
      throw ParsingException(programLine, "Line contains label symbol but no label name")
    }

    if (labelName.endsWith(':')) {
      return labelName.dropLast(1)
    }

    return labelName
  }

  private fun parseNativeFunction(programLine: Int, line: String): NativeFunction {
    val functionBody = line.substring(4)
    if (functionBody.isEmpty()) {
      throw ParsingException(programLine, "Cannot parse native function body ($line)")
    }

    val parametersStart = functionBody.indexOf('(')
    if (parametersStart == -1) {
      throw ParsingException(programLine, "Cannot parse function's variableTypeList start")
    }

    val parametersEnd = functionBody.indexOf(')', parametersStart)
    if (parametersEnd == -1) {
      throw ParsingException(programLine, "Cannot parse function's variableTypeList end")
    }

    val functionName = functionBody.substring(0, parametersStart)
    val functionType = NativeFunctionType.fromString(functionName)
    if (functionType == null) {
      throw ParsingException(programLine, "Cannot parse function operandType ($functionName)")
    }

    //"parametersStart + 1" to skip the opening bracket
    val parametersList = functionBody.substring(parametersStart + 1, parametersEnd).split(',')
    val functionParameters = parametersList
      .map { parameter -> parameter.trim() }
      .map { parameter ->
        val parameterType = VariableType.fromString(parameter)
        if (parameterType == null) {
          throw ParsingException(programLine, "Unknown parameter operandType ($parameter)")
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
  ): List<Instruction> {
    if (line.startsWith("ret")) {
      return listOf(Ret())
    }

    val indexOfFirstSpace = line.indexOfFirst { it == ' ' }
    if (indexOfFirstSpace == -1) {
      throw ParsingException(programLine, "Cannot parse instruction name ($line)")
    }

    val instructionName = line.substring(0, indexOfFirstSpace).toLowerCase()
    val body = line.substring(indexOfFirstSpace)

    val instruction = when (instructionName) {
      "mov" -> parseGenericTwoOperandsInstruction(programLine, body, InstructionType.Mov)
      "add" -> parseGenericTwoOperandsInstruction(programLine, body, InstructionType.Add)
      "cmp" -> parseGenericTwoOperandsInstruction(programLine, body, InstructionType.Cmp)
      "xor" -> parseGenericTwoOperandsInstruction(programLine, body, InstructionType.Xor)
      "sub" -> parseGenericTwoOperandsInstruction(programLine, body, InstructionType.Sub)
      "inc" -> parseGenericOneOperandInstruction(programLine, body, InstructionType.Inc)
      "dec" -> parseGenericOneOperandInstruction(programLine, body, InstructionType.Dec)
      "je",
      "jne",
      "jmp" -> parseJxx(programLine, instructionName, body, InstructionType.Jxx)
      "call" -> parseCall(programLine, body, InstructionType.Call)
      "let" -> parseLet(programLine, body, InstructionType.Let)
      else -> throw ParsingException(programLine, "Unknown instruction name ($instructionName)")
    }

    return vmInstructionObfuscator.obfuscate(instruction)
  }

  private fun parseGenericOneOperandInstruction(programLine: Int, body: String, type: InstructionType): Instruction {
    if (body.isEmpty()) {
      throw ParsingException(programLine, "Instruction has name but does not have a body ($body)")
    }

    val operand = parseOperand(programLine, body.trim(), type)

    return when (type) {
      InstructionType.Inc -> Inc(operand)
      InstructionType.Dec -> Dec(operand)
      InstructionType.Add,
      InstructionType.Call,
      InstructionType.Cmp,
      InstructionType.Jxx,
      InstructionType.Let,
      InstructionType.Mov,
      InstructionType.Ret,
      InstructionType.Xor,
      InstructionType.Sub -> {
        throw ParsingException(programLine, "Instruction ${type.instructionName} is not a generic one operand instruction")
      }
    }
  }

  private fun parseGenericTwoOperandsInstruction(programLine: Int, body: String, type: InstructionType): Instruction {
    if (body.isEmpty()) {
      throw ParsingException(programLine, "Instruction has name but does not have a body ($body)")
    }

    if (body.indexOf(',') == -1) {
      throw ParsingException(programLine, "Cannot parse operands because there is no \',\' symbol")
    }

    val (destOperand, srcOperand) = body.split(',')
      .map { it.trim() }

    val dest = parseOperand(programLine, destOperand, type)
    val src = parseOperand(programLine, srcOperand, type)

    return when (type) {
      InstructionType.Add -> Add(dest, src)
      InstructionType.Cmp -> Cmp(dest, src)
      InstructionType.Mov -> Mov(dest, src)
      InstructionType.Xor -> Xor(dest, src)
      InstructionType.Sub -> Sub(dest, src)
      InstructionType.Dec,
      InstructionType.Inc,
      InstructionType.Call,
      InstructionType.Jxx,
      InstructionType.Let,
      InstructionType.Ret -> {
        throw ParsingException(programLine, "Instruction ${type.instructionName} is not a generic two operands instruction")
      }
    }
  }

  private fun parseLet(programLine: Int, body: String, type: InstructionType): Instruction {
    if (body.isEmpty()) {
      throw ParsingException(programLine, "Instruction has name but does not have a body ($body)")
    }

    if (body.indexOf(',') == -1) {
      throw ParsingException(programLine, "Cannot parse operands because there is no \',\' symbol")
    }

    val (variableOperand, initializerOperand) = body.split(',')
      .map { it.trim() }

    val variable = parseOperand(programLine, variableOperand, type) as Variable
    val initializer = parseOperand(programLine, initializerOperand, type)

    when (initializer) {
      is Constant -> {
        when (initializer) {
          is C32 -> vmMemory.putInt(variable.address, initializer.value)
          is C64 -> vmMemory.putLong(variable.address, initializer.value)
          is VmString -> vmMemory.putInt(variable.address, initializer.address)
        }
      }
      else -> throw ParsingException(programLine, "Initialization not implemented for initializer of type (${initializer.operandName})")
    }

    return Let(
      variable,
      initializer
    )
  }

  private fun parseCall(programLine: Int, body: String, type: InstructionType): Instruction {
    val functionBody = body.trim()
    if (functionBody.isEmpty()) {
      throw ParsingException(programLine, "Function body is empty")
    }

    val parametersStart = functionBody.indexOf('(')
    if (parametersStart == -1) {
      throw ParsingException(programLine, "Cannot parse function's variableTypeList start")
    }

    val parametersEnd = functionBody.indexOf(')', parametersStart)
    if (parametersEnd == -1) {
      throw ParsingException(programLine, "Cannot parse function's variableTypeList end")
    }

    val functionName = functionBody.substring(0, parametersStart)
    val functionType = NativeFunctionType.fromString(functionName)
    if (functionType == null) {
      throw ParsingException(programLine, "Cannot parse function operandType ($functionName)")
    }

    //"parametersStart + 1" to skip the opening bracket
    val operandsList = functionBody.substring(parametersStart + 1, parametersEnd).split(',')

    val operands = operandsList
      .map { operandString -> operandString.trim() }
      .map { operandString -> parseOperand(programLine, operandString, type) }

    return Call(
      functionType,
      operands
    )
  }

  private fun parseJxx(programLine: Int, instructionName: String, body: String, type: InstructionType): Instruction {
    val jumpType = JumpType.fromString(instructionName)
    if (jumpType == null) {
      throw ParsingException(programLine, "Cannot parse jump operandType from instruction name ($instructionName)")
    }

    val labelName = body.trim().substring(1)
    if (labels[labelName] == null) {
      throw ParsingException(programLine, "Label with name ($labelName) does not exist in the labels map")
    }

    //TODO: we probably should not check for whether the label was initialized here,
    // it's better to do it after all of the instructions has been parsed
    if (labels[labelName] == null) {
      throw ParsingException(programLine, "Label with name ($labelName) was not initialized, instructionIndex = (${labels[labelName]})")
    }

    return Jxx(jumpType, labelName)
  }

  private fun parseOperand(programLine: Int, operandString: String, type: InstructionType): Operand {
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
        val closingBracketIndex = operandString.indexOf(']')
        if (closingBracketIndex == -1) {
          throw ParsingException(programLine, "Cannot find closing bracket (\']\') for operand ($operandString)")
        }

        val operandName = operandString.substring(1, closingBracketIndex).trim()
        val operand = parseOperand(programLine, operandName, type)

        when (operand) {
          is Variable -> {
            if (!vmMemory.isVariableDefined(operand.name)) {
              throw ParsingException(programLine, "Variable (${operand.name}) is not defined")
            }
          }
          is Register,
          is Constant -> {
            //don't need to check anything here since it's not a variable
          }
          else -> throw ParsingException(programLine, "Operand ($operandName) is not supported by Memory operand")
        }

        return Memory(operand)
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

        val string = operandString.substring(1, stringEndIndex)
        val address = vmMemory.allocString(string)

        return VmString(address)
      }
      ch.isLetter() -> {
        if (operandString.indexOf(':') == -1) {
          if (!vmMemory.isVariableDefined(operandString)) {
            throw ParsingException(programLine, "Cannot determine variable type (${operandString})")
          }

          val definedVariable = vmMemory.getVariable(operandString)
          if (definedVariable == null) {
            throw ParsingException(programLine, "Variable (${operandString}) is defined but does not have an address in the variables map!")
          }

          return Variable(
            operandString,
            definedVariable.first,
            definedVariable.second
          )
        }

        val (variableNameRaw, variableTypeRaw) = operandString.split(':').map { it.trim() }
        val variableName = wordRegex.find(variableNameRaw)?.value
        if (variableName == null) {
          throw ParsingException(programLine, "Cannot parse variable name (${operandString})")
        }

        if (type == InstructionType.Let) {
          if (vmMemory.isVariableDefined(variableName)) {
            throw ParsingException(programLine, "Variable ($variableName) is already defined")
          }
        }

        val variableType = VariableType.fromString(variableTypeRaw)
        if (variableType == null) {
          throw ParsingException(programLine, "Unknown variable type (${variableTypeRaw})")
        }

        return Variable(
          variableName,
          vmMemory.allocVariable(variableName, variableType),
          variableType
        )
      }
      else -> throw ParsingException(programLine, "Cannot parse operand for ($operandString)")
    }
  }

  private fun extractConstant(programLine: Int, constantString: String): Constant {
    if (constantString.isEmpty()) {
      throw ParsingException(programLine, "Constant is empty")
    }

    val extractedValue32 = constantString.toIntOrNull()
    if (extractedValue32 != null) {
      return C32(extractedValue32)
    }

    val extractedValue64 = constantString.toLongOrNull()
    if (extractedValue64 == null) {
      throw ParsingException(programLine, "Cannot parse constant ($extractedValue64), unknown error")
    }

    return C64(extractedValue64)
  }

  companion object {
    val numberRegex = Regex("-?\\d+")
    val wordRegex = Regex("\\w+")
    val labelRegex = Regex("@([a-zA-Z]+)")
  }
}