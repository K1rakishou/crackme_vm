package crackme.vm.parser

import crackme.vm.core.AddressingMode
import crackme.vm.core.ParsingException
import crackme.vm.core.VariableType
import crackme.vm.core.VmMemory
import crackme.vm.instructions.InstructionType
import crackme.vm.operands.*

class OperandParser(
  private val constactExtractor: ConstactExtractor
) {

  fun parseOperand(
    functionName: String,
    functionLine: Int,
    operandString: String,
    type: InstructionType,
    vmMemory: VmMemory
  ): Operand {
    val ch = operandString[0].toLowerCase()

    return when {
      ch == 'r' -> parseRegisterOperand(operandString, functionName, functionLine)
      ch == '-' || ch.isDigit() -> parseNumericConstantOperand(operandString, functionName, functionLine)
      ch == '\"' -> parseStringConstantOperand(operandString, functionName, functionLine, vmMemory)
      ch.isLetter() || ch == '[' -> {
        if (operandString.contains('@') || operandString.contains('[') || operandString.contains(']')) {
          parseMemoryOperand(operandString, functionName, functionLine, type, vmMemory)
        } else {
          parseVariableOperand(operandString, vmMemory, functionName, functionLine, type)
        }
      }
      else -> throw ParsingException(functionName, functionLine, "Cannot parse operand for ($operandString)")
    }
  }

  private fun parseVariableOperand(operandString: String, vmMemory: VmMemory, functionName: String, functionLine: Int, type: InstructionType): Variable {
    //variable
    if (operandString.indexOf(':') == -1) {
      if (!vmMemory.isVariableDefined(operandString)) {
        throw ParsingException(functionName, functionLine, "Cannot determine variable type (${operandString})")
      }

      val definedVariable = vmMemory.getVariable(operandString)
      if (definedVariable == null) {
        throw ParsingException(functionName, functionLine, "Variable (${operandString}) is defined but does not have an address in the variables map!")
      }

      return Variable(
        operandString,
        definedVariable.first,
        definedVariable.second
      )
    }

    val (variableNameRaw, variableTypeRaw) = operandString.split(':').map { it.trim() }
    val variableName = VMParser.wordRegex.find(variableNameRaw)?.value
    if (variableName == null) {
      throw ParsingException(functionName, functionLine, "Cannot parse variable name (${operandString})")
    }

    if (type == InstructionType.Let) {
      if (vmMemory.isVariableDefined(variableName)) {
        throw ParsingException(functionName, functionLine, "Variable ($variableName) is already defined")
      }
    }

    val variableType = VariableType.fromString(variableTypeRaw)
    if (variableType == null) {
      throw ParsingException(functionName, functionLine, "Unknown variable type (${variableTypeRaw})")
    }

    return Variable(
      variableName,
      vmMemory.allocVariable(variableName, variableType),
      variableType
    )
  }

  private fun parseMemoryOperand(
    operandString: String,
    functionName: String,
    functionLine: Int,
    type: InstructionType,
    vmMemory: VmMemory
  ): Memory<Operand> {
    //memory/stack
    println("operandString = $operandString")

    val indexOfSegmentNameEnd = operandString.indexOf('@')
    if (indexOfSegmentNameEnd == -1) {
      throw ParsingException(functionName, functionLine, "Cannot parse segment name for Memory operand ($operandString)")
    }

    val segmentName = operandString.substring(0, indexOfSegmentNameEnd)
    val segment = Segment.fromString(segmentName)

    if (segment == null) {
      throw ParsingException(functionName, functionLine, "Cannot parse segment from string (${segmentName})")
    }

    val closingBracketIndex = operandString.indexOf(']')
    if (closingBracketIndex == -1) {
      throw ParsingException(functionName, functionLine, "Cannot find closing bracket (\']\') for operand ($operandString)")
    }

    val addressingModeStringIndex = operandString.indexOf(" as ")
    val addressingMode = if (addressingModeStringIndex != -1) {
      val addressingModeString = operandString.substring(addressingModeStringIndex + 4).trim().toLowerCase()
      AddressingMode.fromString(addressingModeString)
        ?: throw ParsingException(functionName, functionLine, "Cannot determine addressing mode ($addressingModeString)")
    } else {
      throw ParsingException(functionName, functionLine, "Cannot determine addressing mode ($operandString)")
    }

    //@[a + 4] -> we need to add 2 here to skip "@["
    val addressingParameters = operandString.substring(indexOfSegmentNameEnd + 2, closingBracketIndex).trim()

    val (operand, offsetOperand) = if (addressingParameters.contains('+')) {
      if (addressingParameters.count { it == '+' } > 1) {
        throw ParsingException(functionName, functionLine, "Only one offset operand allowed ($addressingParameters)")
      }

      val (operandName, offsetOperandName) = addressingParameters.split('+').map { it.trim() }
      val operand = parseOperand(functionName, functionLine, operandName, type, vmMemory)
      val offsetOperand = parseOperand(functionName, functionLine, offsetOperandName, type, vmMemory)

      Pair(operand, offsetOperand)
    } else {
      val operand = parseOperand(functionName, functionLine, addressingParameters, type, vmMemory)
      Pair(operand, null)
    }

    if (operand is Memory<*>) {
      throw ParsingException(functionName, functionLine, "Cannot use nested memory operands ($operandString)")
    }

    offsetOperand?.let {
      if (it is Memory<*>) {
        throw ParsingException(functionName, functionLine, "Cannot use Memory as an offset operand ($operandString)")
      }
    }

    when (operand) {
      is Variable -> {
        if (!vmMemory.isVariableDefined(operand.name)) {
          throw ParsingException(functionName, functionLine, "Variable (${operand.name}) is not defined")
        }
      }
      is Register,
      is Constant -> {
        //don't need to check anything here since it's not a variable
      }
      else -> throw ParsingException(functionName, functionLine, "Operand ($operand) is not supported by Memory operand")
    }

    return Memory(operand, offsetOperand, segment, addressingMode)
  }

  private fun parseStringConstantOperand(operandString: String, functionName: String, functionLine: Int, vmMemory: VmMemory): VmString {
    val stringEndIndex = operandString.indexOf('\"', 1)
    if (stringEndIndex == -1) {
      throw ParsingException(functionName, functionLine, "Cannot find end of the string operand (${operandString})")
    }

    val string = operandString.substring(1, stringEndIndex)
    val address = vmMemory.allocString(string)

    return VmString(address)
  }

  private fun parseNumericConstantOperand(operandString: String, functionName: String, functionLine: Int): Constant {
    val constantString = VMParser.hexNumberRegex.find(operandString)?.value
    if (constantString == null) {
      throw ParsingException(functionName, functionLine, "Cannot parse constant operand ($operandString)")
    }

    return constactExtractor.extractConstant(functionName, functionLine, constantString.toLowerCase())
  }

  private fun parseRegisterOperand(operandString: String, functionName: String, functionLine: Int): Register {
    val registerIndex = VMParser.numberRegex.find(operandString)?.value?.toInt()
    if (registerIndex == null) {
      throw ParsingException(functionName, functionLine, "Cannot parse register index for operand ($operandString)")
    }

    return Register(registerIndex)
  }

}