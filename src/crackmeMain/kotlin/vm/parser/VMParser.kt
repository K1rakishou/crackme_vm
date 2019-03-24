package crackme.vm.parser

import crackme.vm.VM
import crackme.vm.core.*
import crackme.vm.core.function.NativeFunction
import crackme.vm.core.function.NativeFunctionCallbacks
import crackme.vm.core.function.NativeFunctionType
import crackme.vm.instructions.*
import crackme.vm.obfuscator.NoOpInstructionObfuscator
import crackme.vm.obfuscator.VMInstructionObfuscator
import crackme.vm.operands.C32
import crackme.vm.operands.Register
import crackme.vm.operands.Variable
import platform.windows.GetTickCount
import kotlin.random.Random

class VMParser(
  private val random: Random = Random(0),
  private val constactExtractor: ConstactExtractor = ConstactExtractor(),
  private val operandParser: OperandParser = OperandParser(constactExtractor),
  private val vmInstructionObfuscator: VMInstructionObfuscator = NoOpInstructionObfuscator(),
  private val letTransformer: LetTransformer = LetTransformer()
) {
  private val nativeFunctions: MutableMap<NativeFunctionType, NativeFunction> = mutableMapOf()

  //r0, r1, r2, r3, r4, r5, r6, r7, sp, eip
  val registers: MutableList<Long> = mutableListOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

  //this list is to keep track of all added string to the memory (their addresses in the memory)
  private val vmMemory: VmMemory = VmMemory(1024, registers, Random(GetTickCount().toInt()))

  fun parse(program: String): VM {

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

    return VM.createVM(
      vmFunctions,
      nativeFunctions,
      vmMemory,
      registers
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

    /**
     * Function prolog is inserted here:
     *
     * def test(p1: Int, p2: Int)
     *  let a: Int, 1
     *  let b: Int 2
     *  let c: Int, 3
     * end
     *
     *    |
     *    V
     *
     * def test(p1: Int, p2: Int)
     *  add sp, 12 <- allocate 12 byte on the stack for local variables
     * end
     *
     * If a function does not have any parameters nor local variables we need to insert the prolog
     *
     * */
    instructionId = insertFunctionProlog(vmFunctionScope, instructions, instructionId)

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

      val newInstructions = parseInstruction(vmFunctionScope, lineIndex, instructionLine, labels)
      if (newInstructions.isEmpty()) {
        continue
      }

      for (newInstruction in newInstructions) {
        instructions[instructionId++] = newInstruction
      }
    }

    val uninitializedLabels = labels.filter { it.value == -1 }
    if (uninitializedLabels.isNotEmpty()) {
      throw RuntimeException(
        "There are uninitialized labels after parsing vmScope (${vmFunctionScope.name}) : (${uninitializedLabels.map { it.key }})"
      )
    }

    //insert function epilog here

    return VmFunction(
      vmFunctionScope.name,
      vmFunctionScope.start,
      vmFunctionScope.length,
      labels,
      instructions
    )
  }

  private fun insertFunctionProlog(
    vmFunctionScope: VmFunctionScope,
    instructions: LinkedHashMap<Int, Instruction>,
    instructionId: Int
  ): Int {
    val totalStackAllocated = vmFunctionScope.getLocalVariablesTotalStackSize()

    if (totalStackAllocated <= 0) {
      return instructionId
    }

    instructions[instructionId] = Add(
      Register(VM.spRegOffset),
      C32(totalStackAllocated)
    )

    return instructionId + 1
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
    val functionParameters = parseFunctionParameters(parametersString, programLineIndex)

    var functionLength = 0
    var prevStackFrame = functionParameters.lastOrNull()?.stackFrame ?: 0
    val localVariables = mutableListOf<FunctionLocalVariable>()

    for (index in programLineIndex until lines.size) {
      val line = lines.getOrNull(index)?.trim()
      if (line == null) {
        throw ScopeParsingException(index, "Cannot find end of the function (${funcName})")
      }

      ++functionLength

      val localVariable = parseLocalVariable(lines[index], prevStackFrame)
      if (localVariable != null) {
        if (localVariables.any { it.name == localVariable.name }) {
          throw ScopeParsingException(index, "Variable (${localVariable.name}) is already defined")
        }

        localVariables += localVariable
        prevStackFrame += localVariable.type.size
      }

      if (line == "end") {
        break
      }
    }

    if (funcName.equals("main", true) && functionParameters.isNotEmpty()) {
      throw ScopeParsingException(programLineIndex, "Main function must have no parameters!")
    }

    return VmFunctionScope(
      funcName,
      programLineIndex,
      functionLength,
      functionParameters,
      localVariables
    )
  }

  private fun parseLocalVariable(line: String, prevStackFrame: Int): FunctionLocalVariable? {
    if (!line.startsWith("let")) {
      return null
    }

    val indexOfComma = line.indexOf(',')
    if (indexOfComma == -1) {
      throw RuntimeException("Cannot parse operands for Let instruction ($line)")
    }

    val (variableName, variableTypeStr) = line.substring(3, indexOfComma).split(':')
      .map { it.trim() }
      .takeIf { it.size == 2 }
      ?: listOf(null, null)

    if (variableName == null) {
      throw RuntimeException("Let instruction does not contain variable name ($line)")
    }

    if (variableTypeStr == null) {
      throw RuntimeException("Let instruction does not contain variable type ($line)")
    }

    val variableType = VariableType.fromString(variableTypeStr)
    if (variableType == null) {
      throw RuntimeException("Unknown variable type ($line)")
    }

    return FunctionLocalVariable(
      variableName,
      prevStackFrame,
      variableType
    )
  }

  private fun parseFunctionParameters(
    parametersString: String,
    programLineIndex: Int
  ): List<FunctionParameter> {
    if (parametersString.isEmpty()) {
      return emptyList()
    }

    /**
     * def test(a: Int, b: Int, c: Int)
     *
     * stack[0] -> a's address
     * stack[8] -> b's address
     * stack[12] -> c's address
     * stack[16] -> return address
     * */

    val rawParameters = parametersString.split(',').map { it.trim() }
    val functionParameters = mutableListOf<FunctionParameter>()

    var stackPointerOffset = 0

    for (rawParameter in rawParameters) {
      val (parameterName, parameterTypeString) = rawParameter.split(':').map { it.trim() }
      val parameterType = VariableType.fromString(parameterTypeString)
      if (parameterType == null) {
        throw ScopeParsingException(programLineIndex, "Cannot parse parameter type for parameter ($rawParameter)")
      }

      functionParameters += FunctionParameter(parameterName, stackPointerOffset, parameterType)
      stackPointerOffset += parameterType.size
    }

    return functionParameters
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
      .mapNotNull { parameter ->
        val parameterType = VariableType.fromString(parameter)
        if (parameterType == null) {
          throw ScopeParsingException(functionLine, "Unknown parameter operandType ($parameter)")
        }

        return@mapNotNull parameterType
      }

    return NativeFunction(
      functionType,
      functionParameters,
      NativeFunctionCallbacks.getCallbackByFunctionType(functionType)
    )
  }

  private fun parseInstruction(
    vmFunctionScope: VmFunctionScope,
    functionLine: Int,
    line: String,
    labels: MutableMap<String, Int>
  ): List<Instruction> {
    if (line.startsWith("ret")) {
      return parseRet(vmFunctionScope)
    }

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
      "mov" -> parseGenericTwoOperandsInstruction(vmFunctionScope, functionLine, body, InstructionType.Mov)
      "add" -> parseGenericTwoOperandsInstruction(vmFunctionScope, functionLine, body, InstructionType.Add)
      "cmp" -> parseGenericTwoOperandsInstruction(vmFunctionScope, functionLine, body, InstructionType.Cmp)
      "xor" -> parseGenericTwoOperandsInstruction(vmFunctionScope, functionLine, body, InstructionType.Xor)
      "sub" -> parseGenericTwoOperandsInstruction(vmFunctionScope, functionLine, body, InstructionType.Sub)
      "inc" -> parseGenericOneOperandInstruction(vmFunctionScope, functionLine, body, InstructionType.Inc)
      "dec" -> parseGenericOneOperandInstruction(vmFunctionScope, functionLine, body, InstructionType.Dec)
      "pushq" -> parseGenericOneOperandInstruction(vmFunctionScope, functionLine, body, InstructionType.Push, AddressingMode.ModeQword)
      "pushd" -> parseGenericOneOperandInstruction(vmFunctionScope, functionLine, body, InstructionType.Push, AddressingMode.ModeDword)
      "popq" -> parseGenericOneOperandInstruction(vmFunctionScope, functionLine, body, InstructionType.Pop, AddressingMode.ModeQword)
      "popd" -> parseGenericOneOperandInstruction(vmFunctionScope, functionLine, body, InstructionType.Pop, AddressingMode.ModeDword)
      "je",
      "jne",
      "jmp" -> parseJxx(vmFunctionScope, functionLine, instructionName, body, InstructionType.Jxx)
      "call" -> parseCall(vmFunctionScope, functionLine, body, InstructionType.Call)
      "let" -> {
        val let = parseLet(vmFunctionScope, functionLine, body, InstructionType.Let)
        letTransformer.transform(vmMemory, vmFunctionScope, functionLine, let as Let)
      }
      else -> throw ParsingException(vmFunctionScope.name, functionLine, "Unknown instruction name ($instructionName)")
    }

    return vmInstructionObfuscator.obfuscate(vmMemory, instruction)
  }

  private fun parseRet(vmFunctionScope: VmFunctionScope): List<Ret> {
    val ret = Ret(
      vmFunctionScope.getTotalStackSizeAllocated().toShort(),
      vmFunctionScope.isMainFunctionScope()
    )

    return listOf(ret)
  }

  private fun parseGenericOneOperandInstruction(
    vmFunctionScope: VmFunctionScope,
    functionLine: Int,
    body: String,
    type: InstructionType,
    addressingMode: AddressingMode? = null
  ): Instruction {
    if (body.isEmpty()) {
      throw ParsingException(
        vmFunctionScope.name,
        functionLine,
        "Instruction has name but does not have a body ($body)"
      )
    }

    val operand = operandParser.parseOperand(vmFunctionScope, functionLine, body.trim(), type, vmMemory)

    return when (type) {
      InstructionType.Inc -> Inc(operand)
      InstructionType.Dec -> Dec(operand)
      InstructionType.Push -> {
        if (addressingMode == null) {
          throw ParsingException(vmFunctionScope.name, functionLine, "Push instruction must have an addressing mode")
        }

        Push(addressingMode, operand)
      }
      InstructionType.Pop -> {
        if (addressingMode == null) {
          throw ParsingException(vmFunctionScope.name, functionLine, "Push instruction must have an addressing mode")
        }

        Pop(addressingMode, operand)
      }
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
          vmFunctionScope.name,
          functionLine,
          "Instruction ${type.instructionName} is not a generic one operand instruction"
        )
      }
    }
  }

  private fun parseGenericTwoOperandsInstruction(
    vmFunctionScope: VmFunctionScope,
    functionLine: Int,
    body: String,
    type: InstructionType
  ): Instruction {
    if (body.isEmpty()) {
      throw ParsingException(vmFunctionScope.name, functionLine, "Instruction has name but does not have a body ($body)")
    }

    if (body.indexOf(',') == -1) {
      throw ParsingException(vmFunctionScope.name, functionLine, "Cannot parse operands because there is no \',\' symbol")
    }

    val (destOperand, srcOperand) = body.split(',')
      .map { it.trim() }

    val dest = operandParser.parseOperand(vmFunctionScope, functionLine, destOperand, type, vmMemory)
    val src = operandParser.parseOperand(vmFunctionScope, functionLine, srcOperand, type, vmMemory)

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
          vmFunctionScope.name,
          functionLine,
          "Instruction ${type.instructionName} is not a generic two operands instruction"
        )
      }
    }
  }

  private fun parseLet(
    vmFunctionScope: VmFunctionScope,
    functionLine: Int,
    body: String,
    type: InstructionType
  ): Instruction {
    if (body.isEmpty()) {
      throw ParsingException(vmFunctionScope.name, functionLine, "Instruction has name but does not have a body ($body)")
    }

    if (body.indexOf(',') == -1) {
      throw ParsingException(vmFunctionScope.name, functionLine, "Cannot parse operands because there is no \',\' symbol")
    }

    val (variableOperand, initializerOperand) = body.split(',')
      .map { it.trim() }

    val variable = operandParser.parseOperand(vmFunctionScope, functionLine, variableOperand, type, vmMemory) as Variable
    val initializer = operandParser.parseOperand(vmFunctionScope, functionLine, initializerOperand, type, vmMemory)

    return Let(
      variable,
      initializer
    )
  }

  private fun parseCall(
    vmFunctionScope: VmFunctionScope,
    functionLine: Int,
    body: String,
    type: InstructionType
  ): Instruction {
    val instructionFunctionName = body.trim()
    if (instructionFunctionName.isEmpty()) {
      throw ParsingException(vmFunctionScope.name, functionLine, "VmFunctionScope body is empty")
    }

    return Call(
      instructionFunctionName
    )
  }

  private fun parseJxx(
    vmFunctionScope: VmFunctionScope,
    functionLine: Int,
    instructionName: String,
    body: String,
    type: InstructionType
  ): Instruction {
    val jumpType = JumpType.fromString(instructionName)
    if (jumpType == null) {
      throw ParsingException(
        vmFunctionScope.name,
        functionLine,
        "Cannot parse jump operandType from instruction name ($instructionName)"
      )
    }

    val labelName = parseLabel(vmFunctionScope.name, functionLine, body)
    return Jxx(jumpType, vmFunctionScope.name, labelName)
  }

  companion object {
    val numberRegex = Regex("-?\\d+")
    val hexNumberRegex = Regex("[(-|0x)?0-9abcdefABCDEF]+")
    val wordRegex = Regex("\\w+")
    val funcNameRegex = Regex("^[^0-9][a-zA-Z0-9_]+")
    val labelRegex = Regex("@([a-zA-Z]+)")
  }
}