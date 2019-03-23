package crackme.vm.parser

import crackme.vm.core.*
import crackme.vm.instructions.InstructionType
import crackme.vm.operands.*

class OperandParser(
  private val constactExtractor: ConstactExtractor
) {

  fun parseOperand(
    vmFunctionScope: VmFunctionScope,
    functionLine: Int,
    operandString: String,
    type: InstructionType,
    vmMemory: VmMemory
  ): Operand {
    val ch = operandString[0].toLowerCase()

    return when {
      ch == 'r' -> parseRegisterOperand(vmFunctionScope, operandString, functionLine)
      ch == '-' || ch.isDigit() -> parseNumericConstantOperand(vmFunctionScope, operandString, functionLine)
      ch == '\"' -> parseStringConstantOperand(vmFunctionScope, operandString, functionLine, vmMemory)
      ch.isLetter() || ch == '[' -> {
        if (operandString.contains('@') || operandString.contains('[') || operandString.contains(']')) {
          parseMemoryOperand(vmFunctionScope, operandString, functionLine, type, vmMemory)
        } else {
          parseVariableOperand(vmFunctionScope, operandString, vmMemory, functionLine, type)
        }
      }
      else -> throw ParsingException(vmFunctionScope.name, functionLine, "Cannot parse operand for ($operandString)")
    }
  }

  //returns either Memory<*> or Variable operand type
  //it returns Memory<*> type operand when variable is a stack segment variable (like a function's parameter)
  private fun parseVariableOperand(
    vmFunctionScope: VmFunctionScope,
    operandString: String,
    vmMemory: VmMemory,
    functionLine: Int,
    type: InstructionType
  ): Operand {
    //variable
    if (operandString.indexOf(':') != -1) {
      /**
       * variable declaration, e.g:.
       * let a: Int, 123456
       * */
      return parseVariableDeclaration(vmFunctionScope, operandString, functionLine, type, vmMemory)
    }

    /**
     * variable usage, e.g.:
     *  mov r0, ss@[a] as dword
     *  mov ds@[b] as dword, r0
     * */
    val isStackVariable = vmFunctionScope.getParameterStackFrameByName(operandString)?.let { true } ?: false
    val isMemoryVariable = vmMemory.isVariableDefined(operandString)

    if (isStackVariable && isMemoryVariable) {
      throw ParsingException(
        vmFunctionScope.name,
        functionLine,
        "Variable $operandString cannot be used for both memory and stack at the same time"
      )
    }

    if (!isStackVariable && !isMemoryVariable) {
      throw ParsingException(
        vmFunctionScope.name,
        functionLine,
        "Variable $operandString is defined neither as a function parameter nor as a global variable"
      )
    }

    if (isMemoryVariable) {
      val definedVariable = vmMemory.getVariable(operandString)
      if (definedVariable == null) {
        throw ParsingException(
          vmFunctionScope.name,
          functionLine,
          "Variable (${operandString}) is defined but does not have an address in the variables map!"
        )
      }

      //TODO: replace with memory address
      return Variable(
        operandString,
        definedVariable.address,
        definedVariable.variableType
      )
    } else {
      val functionParameter = vmFunctionScope.getParameterByName(operandString)
      if (functionParameter == null) {
        throw ParsingException(vmFunctionScope.name, functionLine, "Stack frame is null for variable with name ($operandString)")
      }

      /**
       * Here we are transforming access from Memory<Variable> into Memory<C32> where memory segment is stack, e.g.:
       *
       *  def sum_of_three(a: Int, b: Int, c: Int)
       *  mov r0, ss@[a] as qword
       *  mov r1, ss@[b] as qword
       *  mov r2, ss@[c] as qword
       *
       *    |
       *    V
       *  mov r0, ss@[8] as qword
       *  mov r1, ss@[12] as qword
       *  mov r2, ss@[16] as qword
       * */

      return Memory(
        C32(functionParameter.stackFrame),
        null,
        Segment.Stack,
        functionParameter.type.addressingMode
      )
    }
  }

  private fun parseVariableDeclaration(
    vmFunctionScope: VmFunctionScope,
    operandString: String,
    functionLine: Int,
    type: InstructionType,
    vmMemory: VmMemory
  ): Variable {
    val (variableNameRaw, variableTypeRaw) = operandString.split(':').map { it.trim() }
    val variableName = VMParser.wordRegex.find(variableNameRaw)?.value
    if (variableName == null) {
      throw ParsingException(vmFunctionScope.name, functionLine, "Cannot parse variable name (${operandString})")
    }

    if (type == InstructionType.Let) {
      if (vmMemory.isVariableDefined(variableName)) {
        throw ParsingException(vmFunctionScope.name, functionLine, "Variable ($variableName) is already defined")
      }
    }

    val variableType = VariableType.fromString(variableTypeRaw)
    if (variableType == null) {
      throw ParsingException(vmFunctionScope.name, functionLine, "Unknown variable type (${variableTypeRaw})")
    }

    return Variable(
      variableName,
      vmMemory.allocVariable(variableName, variableType),
      variableType
    )
  }

  private fun parseMemoryOperand(
    vmFunctionScope: VmFunctionScope,
    operandString: String,
    functionLine: Int,
    type: InstructionType,
    vmMemory: VmMemory
  ): Operand {
    //memory/stack

    val indexOfSegmentNameEnd = operandString.indexOf('@')
    if (indexOfSegmentNameEnd == -1) {
      throw ParsingException(vmFunctionScope.name, functionLine, "Cannot parse segment name for Memory operand ($operandString)")
    }

    val segmentName = operandString.substring(0, indexOfSegmentNameEnd)
    val segment = Segment.fromString(segmentName)

    if (segment == null) {
      throw ParsingException(vmFunctionScope.name, functionLine, "Cannot parse segment from string (${segmentName})")
    }

    val closingBracketIndex = operandString.indexOf(']')
    if (closingBracketIndex == -1) {
      throw ParsingException(vmFunctionScope.name, functionLine, "Cannot find closing bracket (\']\') for operand ($operandString)")
    }

    val addressingModeStringIndex = operandString.indexOf(" as ")

    val addressingMode = if (addressingModeStringIndex != -1) {
      val addressingModeString = operandString.substring(addressingModeStringIndex + 4).trim().toLowerCase()

      val addressingMode = AddressingMode.fromString(addressingModeString)
      if (addressingMode == null) {
        throw ParsingException(vmFunctionScope.name, functionLine, "Cannot determine addressing mode ($addressingModeString)")
      }

      addressingMode
    } else {
      throw ParsingException(vmFunctionScope.name, functionLine, "Cannot determine addressing mode ($operandString)")
    }

    //@[a + 4] -> we need to add 2 here to skip "@["
    val addressingParameters = operandString.substring(indexOfSegmentNameEnd + 2, closingBracketIndex).trim()

    val (operand, offsetOperand) = if (addressingParameters.contains('+')) {
      if (addressingParameters.count { it == '+' } > 1) {
        throw ParsingException(vmFunctionScope.name, functionLine, "Only one offset operand allowed ($addressingParameters)")
      }

      val (operandName, offsetOperandName) = addressingParameters.split('+').map { it.trim() }
      val operand = parseOperand(vmFunctionScope, functionLine, operandName, type, vmMemory)
      val offsetOperand = parseOperand(vmFunctionScope, functionLine, offsetOperandName, type, vmMemory)

      Pair(operand, offsetOperand)
    } else {
      val operand = parseOperand(vmFunctionScope, functionLine, addressingParameters, type, vmMemory)

      //FIXME: HACK - this should be refactored.
      //this may happen when addressingParameters is a variable name and the variable has a stack segment
      if (operand is Memory<*>) {
        return operand
      }

      Pair(operand, null)
    }

    if (operand is Memory<*>) {
      throw ParsingException(vmFunctionScope.name, functionLine, "Cannot use nested memory operands ($addressingParameters)")
    }

    if (offsetOperand != null && offsetOperand is Memory<*>) {
      throw ParsingException(vmFunctionScope.name, functionLine, "Cannot use Memory as an offset operand ($operandString)")
    }

    when (operand) {
      is Variable -> {
        if (!vmMemory.isVariableDefined(operand.name)) {
          throw ParsingException(vmFunctionScope.name, functionLine, "Variable (${operand.name}) is not defined")
        }
      }
      is Register,
      is Constant -> {
        //don't need to check anything here
      }
      else -> throw ParsingException(vmFunctionScope.name, functionLine, "Operand ($operand) is not supported by Memory operand")
    }

    //TODO: replace with memory address
    return Memory(operand, offsetOperand, segment, addressingMode)
  }

  private fun parseStringConstantOperand(
    vmFunctionScope: VmFunctionScope,
    operandString: String,
    functionLine: Int,
    vmMemory: VmMemory
  ): VmString {
    val stringEndIndex = operandString.indexOf('\"', 1)
    if (stringEndIndex == -1) {
      throw ParsingException(vmFunctionScope.name, functionLine, "Cannot find end of the string operand (${operandString})")
    }

    val string = operandString.substring(1, stringEndIndex)
    val address = vmMemory.allocString(string)

    return VmString(address)
  }

  private fun parseNumericConstantOperand(
    vmFunctionScope: VmFunctionScope,
    operandString: String,
    functionLine: Int
  ): Constant {
    val constantString = VMParser.hexNumberRegex.find(operandString)?.value
    if (constantString == null) {
      throw ParsingException(vmFunctionScope.name, functionLine, "Cannot parse constant operand ($operandString)")
    }

    return constactExtractor.extractConstant(vmFunctionScope.name, functionLine, constantString.toLowerCase())
  }

  private fun parseRegisterOperand(
    vmFunctionScope: VmFunctionScope,
    operandString: String,
    functionLine: Int
  ): Register {
    val registerIndex = VMParser.numberRegex.find(operandString)?.value?.toInt()
    if (registerIndex == null) {
      throw ParsingException(vmFunctionScope.name, functionLine, "Cannot parse register index for operand ($operandString)")
    }

    //TODO: check whether registerIndex is not out of bounds
    return Register(registerIndex)
  }

}