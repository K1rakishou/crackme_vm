package crackme.vm.parser

import crackme.vm.VM
import crackme.vm.core.*
import crackme.vm.core.function.NativeFunction
import crackme.vm.core.function.NativeFunctionCallbacks
import crackme.vm.core.function.NativeFunctionType
import crackme.vm.instructions.*
import crackme.vm.obfuscator.NoOpInstructionObfuscator
import crackme.vm.obfuscator.VMInstructionObfuscator
import crackme.vm.operands.*
import platform.windows.GetTickCount
import kotlin.random.Random

class VMParser(
  private val random: Random = Random(0),
  private val constactExtractor: ConstactExtractor = ConstactExtractor(),
  private val operandParser: OperandParser = OperandParser(constactExtractor),
  private val vmInstructionObfuscator: VMInstructionObfuscator = NoOpInstructionObfuscator()
) {
  private lateinit var nativeFunctions: MutableMap<NativeFunctionType, NativeFunction>
  //this list is to keep track of all added string to the memory (their addresses in the memory)
  private lateinit var vmMemory: VmMemory
  private lateinit var vmFlags: VmFlags

  fun parse(program: String): VM {
    nativeFunctions = mutableMapOf()
    vmMemory = VmMemory(1024, Random(GetTickCount().toInt()))
    vmFlags = VmFlags()

    val lines = program.split("\n")
      .map { it.trim() }
      .filterNot { it.isEmpty() }

    val vmFunctionScopes = parseVmFunctionScopes(lines)

    val vmFunctions = mutableMapOf<String, VmFunction>()
    var instructionId = 0

    for ((_, vmScope) in vmFunctionScopes) {
      val funcBody = lines.subList(vmScope.start, vmScope.start + vmScope.length)

      val vmFunction = parseFunctionBody(vmScope, instructionId, funcBody)
      vmFunctions.put(vmFunction.name, vmFunction)
      instructionId += vmFunction.instructions.size
    }

    return VM(
      random,
      vmFunctions,
      nativeFunctions,
      mutableListOf(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L),
      VmStack(1024, random),
      vmMemory,
      vmFlags
    )
  }

  private fun parseFunctionBody(
    vmFunctionScope: VmFunctionScope,
    _instructionId: Int,
    funcBody: List<String>
  ): VmFunction {
    val labels = mutableMapOf<String, Int>()
    val instructions = linkedMapOf<Int, Instruction>()
    var instructionId = _instructionId

    for ((lineIndex, funcLine) in funcBody.withIndex()) {
      val instructionLine = funcLine.trim()

      //function prolog and epilog
      if (funcLine.startsWith("def") || funcLine.startsWith("end")) {
        continue
      }

      //label declaration
      if (funcLine.startsWith('@') && funcLine.endsWith(':')) {
        val labelName = parseLabel(vmFunctionScope.name, lineIndex, funcLine)
        labels[labelName] = instructionId
        continue
      }

      val newInstructions = parseInstruction(vmFunctionScope.name, lineIndex, instructionLine, labels)
      if (newInstructions.isEmpty()) {
        continue
      }

      for (newInstruction in newInstructions) {
        instructions[instructionId++] = newInstruction
      }
    }

    val uninitializedLabels = labels.filter { it.value == -1 }
    if (uninitializedLabels.isNotEmpty()) {
      throw RuntimeException("There are uninitialized labels after parsing vmScope ($vmFunctionScope.name) : (${uninitializedLabels.map { it.key }})")
    }

    return VmFunction(
      vmFunctionScope.name,
      vmFunctionScope.start,
      vmFunctionScope.length,
      labels,
      instructions
    )
  }

  private fun parseVmFunctionScopes(lines: List<String>): Map<String, VmFunctionScope> {
    var programLine = 0
    val vmFunctionScopes = mutableMapOf<String, VmFunctionScope>()

    while (programLine < lines.size) {
      val line = lines.getOrNull(programLine)?.trim()
      if (line == null) {
        throw ScopeParsingException(programLine, "Prepare error, current programLine is out of bounds (programLine = $programLine), (linesCount = ${lines.size})")
      }

      when {
        line.startsWith("use") -> {
          val nativeFunction = parseNativeFunction(programLine, line)
          nativeFunctions[nativeFunction.type] = nativeFunction
        }
        line.startsWith("def") -> {
          val vmFunctionScope = parseFunctionScope(lines, programLine)

          if (vmFunctionScope.start < 0 || vmFunctionScope.length < 0 || vmFunctionScope.start >= vmFunctionScope.length) {
            throw ScopeParsingException(programLine, "Could not parse function starting at line $programLine")
          }

          vmFunctionScopes.put(vmFunctionScope.name, vmFunctionScope)
          programLine += vmFunctionScope.length
        }
        else -> ++programLine
      }
    }

    return vmFunctionScopes
  }

  private fun parseFunctionScope(lines: List<String>, programLineIndex: Int): VmFunctionScope {
    val functionStartLine = lines.getOrNull(programLineIndex)?.substring(4)
    if (functionStartLine == null) {
      throw ScopeParsingException(
        programLineIndex,
        "ParseFunction error: current programLine is out of bounds (programLineIndex = $programLineIndex), (linesCount = ${lines.size})"
      )
    }

    val funcName = funcNameRegex.find(functionStartLine)?.value
    if (funcName == null) {
      throw ScopeParsingException(programLineIndex, "Cannot find function name ($functionStartLine)")
    }

    val parametersStartIndex = functionStartLine.indexOf('(')
    if (parametersStartIndex == -1) {
      throw ScopeParsingException(programLineIndex, "Cannot find parameters start for function ($funcName)")
    }

    val parametersEndIndex = functionStartLine.indexOf(')', parametersStartIndex)
    if (parametersEndIndex == -1) {
      throw ScopeParsingException(programLineIndex, "Cannot find parameters end for function ($funcName)")
    }

    val parametersString = functionStartLine.substring(parametersStartIndex + 1, parametersEndIndex)

    val parameters = if (parametersString.isNotEmpty()) {
      parametersString.split(',')
        .map { it.trim() }
        .map { parameter ->
          val (parameterName, parameterTypeString) = parameter.split(':').map { it.trim() }
          val parameterType = VariableType.fromString(parameterTypeString)
          if (parameterType == null) {
            throw ScopeParsingException(programLineIndex, "Cannot parse parameter type for parameter (${parameter})")
          }

          return@map FunctionParameter(parameterName, parameterType)
        }
    } else {
      //function with no parameters
      emptyList()
    }

    var functionLength = 0

    for (index in programLineIndex until lines.size) {
      val line = lines.getOrNull(index)?.trim()
      if (line == null) {
        throw ScopeParsingException(index, "Cannot find end of the function (${funcName})")
      }

      ++functionLength

      if (line == "end") {
        break
      }
    }

    if (funcName.equals("main", true) && parameters.isNotEmpty()) {
      throw ScopeParsingException(programLineIndex, "Main function must have no parameters!")
    }

    return VmFunctionScope(
      funcName,
      programLineIndex,
      functionLength,
      parameters
    )
  }

  private fun parseLabel(
    functionName: String,
    functionLine: Int,
    line: String
  ): String {
    val labelName = labelRegex.find(line)?.value?.substring(1)
    if (labelName == null) {
      throw ParsingException(functionName, functionLine, "Line contains label symbol but no label name")
    }

    if (labelName.endsWith(':')) {
      return labelName.dropLast(1)
    }

    return labelName
  }

  private fun parseNativeFunction(
    functionLine: Int,
    line: String
  ): NativeFunction {
    val functionBody = line.substring(4)
    if (functionBody.isEmpty()) {
      throw ScopeParsingException(functionLine, "Cannot parse native function body ($line)")
    }

    val parametersStart = functionBody.indexOf('(')
    if (parametersStart == -1) {
      throw ScopeParsingException(functionLine, "Cannot parse function's variableTypeList start")
    }

    val parametersEnd = functionBody.indexOf(')', parametersStart)
    if (parametersEnd == -1) {
      throw ScopeParsingException(functionLine, "Cannot parse function's variableTypeList end")
    }

    val nativeFunctionName = functionBody.substring(0, parametersStart)
    val functionType = NativeFunctionType.fromString(nativeFunctionName)
    if (functionType == null) {
      throw ScopeParsingException(functionLine, "parseNativeFunction Cannot parse function operandType ($nativeFunctionName)")
    }

    //"parametersStart + 1" to skip the opening bracket
    val parametersList = functionBody.substring(parametersStart + 1, parametersEnd).split(',')
    val functionParameters = parametersList
      .map { parameter -> parameter.trim() }
      .map { parameter ->
        val parameterType = VariableType.fromString(parameter)
        if (parameterType == null) {
          throw ScopeParsingException(functionLine, "Unknown parameter operandType ($parameter)")
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
    functionName: String,
    functionLine: Int,
    line: String,
    labels: MutableMap<String, Int>
  ): List<Instruction> {
    val indexOfFirstSpace = line.indexOfFirst { it == ' ' }

    val (instructionName, body) = if (indexOfFirstSpace == -1) {
      //some instruction may not have any operands (like ret)
      Pair(line, "")
    } else {
      val instructionName = line.substring(0, indexOfFirstSpace).toLowerCase()
      val body = line.substring(indexOfFirstSpace)

      Pair(instructionName, body)
    }

    val instruction = when (instructionName) {
      "mov" -> parseGenericTwoOperandsInstruction(functionName, functionLine, body, InstructionType.Mov)
      "add" -> parseGenericTwoOperandsInstruction(functionName, functionLine, body, InstructionType.Add)
      "cmp" -> parseGenericTwoOperandsInstruction(functionName, functionLine, body, InstructionType.Cmp)
      "xor" -> parseGenericTwoOperandsInstruction(functionName, functionLine, body, InstructionType.Xor)
      "sub" -> parseGenericTwoOperandsInstruction(functionName, functionLine, body, InstructionType.Sub)
      "inc" -> parseGenericOneOperandInstruction(functionName, functionLine, body, InstructionType.Inc)
      "dec" -> parseGenericOneOperandInstruction(functionName, functionLine, body, InstructionType.Dec)
      "push" -> parseGenericOneOperandInstruction(functionName, functionLine, body, InstructionType.Push)
      "pop" -> parseGenericOneOperandInstruction(functionName, functionLine, body, InstructionType.Pop)
      "ret" -> parseRet(functionName, functionLine, body, InstructionType.Ret)
      "je",
      "jne",
      "jmp" -> parseJxx(functionName, functionLine, instructionName, body, labels, InstructionType.Jxx)
      "call" -> parseCall(functionName, functionLine, body, InstructionType.Call)
      "let" -> parseLet(functionName, functionLine, body, InstructionType.Let)
      else -> throw ParsingException(functionName, functionLine, "Unknown instruction name ($instructionName)")
    }

    return vmInstructionObfuscator.obfuscate(vmMemory, instruction)
  }

  private fun parseRet(functionName: String, functionLine: Int, body: String, type: InstructionType): Instruction {
    if (body.isEmpty()) {
      return Ret(0)
    }

    val operand = operandParser.parseOperand(functionName, functionLine, body.trim(), type, vmMemory)
    if (operand !is C32) {
      throw ParsingException(functionName, functionLine, "Ret can only be used with C32 operand")
    }

    if (operand.value < 0 || operand.value > Short.MAX_VALUE.toInt()) {
      throw ParsingException(functionName, functionLine, "Ret operand overflow (${operand.value})")
    }

    return Ret(operand.value.toShort())
  }

  private fun parseGenericOneOperandInstruction(
    functionName: String,
    functionLine: Int,
    body: String,
    type: InstructionType
  ): Instruction {
    if (body.isEmpty()) {
      throw ParsingException(
        functionName,
        functionLine,
        "Instruction has name but does not have a body ($body)"
      )
    }

    val operand = operandParser.parseOperand(functionName, functionLine, body.trim(), type, vmMemory)

    return when (type) {
      InstructionType.Inc -> Inc(operand)
      InstructionType.Dec -> Dec(operand)
      InstructionType.Push -> Push(operand)
      InstructionType.Pop -> Pop(operand)
      InstructionType.Ret,
      InstructionType.Add,
      InstructionType.Call,
      InstructionType.Cmp,
      InstructionType.Jxx,
      InstructionType.Let,
      InstructionType.Mov,
      InstructionType.Xor,
      InstructionType.Sub -> {
        throw ParsingException(
          functionName,
          functionLine,
          "Instruction ${type.instructionName} is not a generic one operand instruction"
        )
      }
    }
  }

  private fun parseGenericTwoOperandsInstruction(
    functionName: String,
    functionLine: Int,
    body: String,
    type: InstructionType
  ): Instruction {
    if (body.isEmpty()) {
      throw ParsingException(functionName, functionLine, "Instruction has name but does not have a body ($body)")
    }

    if (body.indexOf(',') == -1) {
      throw ParsingException(functionName, functionLine, "Cannot parse operands because there is no \',\' symbol")
    }

    val (destOperand, srcOperand) = body.split(',')
      .map { it.trim() }

    val dest = operandParser.parseOperand(functionName, functionLine, destOperand, type, vmMemory)
    val src = operandParser.parseOperand(functionName, functionLine, srcOperand, type, vmMemory)

    return when (type) {
      InstructionType.Add -> Add(dest, src)
      InstructionType.Cmp -> Cmp(dest, src)
      InstructionType.Mov -> Mov(dest, src)
      InstructionType.Xor -> Xor(dest, src)
      InstructionType.Sub -> Sub(dest, src)
      InstructionType.Push,
      InstructionType.Pop,
      InstructionType.Dec,
      InstructionType.Inc,
      InstructionType.Call,
      InstructionType.Jxx,
      InstructionType.Let,
      InstructionType.Ret -> {
        throw ParsingException(
          functionName,
          functionLine,
          "Instruction ${type.instructionName} is not a generic two operands instruction"
        )
      }
    }
  }

  private fun parseLet(
    functionName: String,
    functionLine: Int,
    body: String,
    type: InstructionType
  ): Instruction {
    if (body.isEmpty()) {
      throw ParsingException(functionName, functionLine, "Instruction has name but does not have a body ($body)")
    }

    if (body.indexOf(',') == -1) {
      throw ParsingException(functionName, functionLine, "Cannot parse operands because there is no \',\' symbol")
    }

    val (variableOperand, initializerOperand) = body.split(',')
      .map { it.trim() }

    val variable = operandParser.parseOperand(functionName, functionLine, variableOperand, type, vmMemory) as Variable
    val initializer = operandParser.parseOperand(functionName, functionLine, initializerOperand, type, vmMemory)

    when (initializer) {
      is Constant -> {
        when (initializer) {
          is C32 -> vmMemory.putInt(variable.address, initializer.value)
          is C64 -> vmMemory.putLong(variable.address, initializer.value)
          is VmString -> vmMemory.putInt(variable.address, initializer.address)
        }
      }
      else -> throw ParsingException(
        functionName,
        functionLine,
        "Initialization not implemented for initializer of type (${initializer.operandName})"
      )
    }

    return Let(
      variable,
      initializer
    )
  }

  private fun parseCall(
    functionName: String,
    functionLine: Int,
    body: String,
    type: InstructionType
  ): Instruction {
    val instructionFunctionName = body.trim()
    if (instructionFunctionName.isEmpty()) {
      throw ParsingException(functionName, functionLine, "VmFunctionScope body is empty")
    }

    return Call(
      instructionFunctionName
    )
  }

  private fun parseJxx(
    functionName: String,
    functionLine: Int,
    instructionName: String,
    body: String,
    labels: Map<String, Int>,
    type: InstructionType
  ): Instruction {
    val jumpType = JumpType.fromString(instructionName)
    if (jumpType == null) {
      throw ParsingException(
        functionName,
        functionLine,
        "Cannot parse jump operandType from instruction name ($instructionName)"
      )
    }

    val labelName = parseLabel(functionName, functionLine, body)
    return Jxx(jumpType, functionName, labelName)
  }

  companion object {
    val numberRegex = Regex("-?\\d+")
    val hexNumberRegex = Regex("[(-|0x)?0-9abcdefABCDEF]+")
    val wordRegex = Regex("\\w+")
    val funcNameRegex = Regex("^[^0-9][a-zA-Z0-9_]+")
    val labelRegex = Regex("@([a-zA-Z]+)")
  }
}