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
    return parseOperandInternal(vmFunctionScope, functionLine, operandString, type, vmMemory, false)
  }

  private fun parseOperandInternal(
    vmFunctionScope: VmFunctionScope,
    functionLine: Int,
    operandString: String,
    type: InstructionType,
    vmMemory: VmMemory,
    isMemoryInnerOperand: Boolean
  ): Operand {
    val ch = operandString[0].toLowerCase()

    return when {
      ch == 'r' -> parseRegisterOperand(vmFunctionScope, operandString, functionLine)
      ch == '-' || ch.isDigit() -> parseNumericConstantOperand(vmFunctionScope, operandString, functionLine)
      ch.isLetter() || ch == '[' -> {
        if (operandString.contains('@') || operandString.contains('[') || operandString.contains(']')) {
          parseMemoryOperand(vmFunctionScope, operandString, functionLine, type, vmMemory)
        } else {
          parseVariableOperand(vmFunctionScope, operandString, vmMemory, isMemoryInnerOperand, functionLine, type)
        }
      }
      ch == '\"' -> throw ParsingException(vmFunctionScope.name, functionLine, "Old strings are not supported anymore!!!")
      else -> throw ParsingException(vmFunctionScope.name, functionLine, "Cannot parse operand for ($operandString)")
    }
  }

  //returns either Memory<*> or Variable operand type
  //it returns Memory<*> type operand when variable is a stack segment variable (like a function's parameter)
  private fun parseVariableOperand(
    vmFunctionScope: VmFunctionScope,
    operandString: String,
    vmMemory: VmMemory,
    isMemoryInnerOperand: Boolean,
    functionLine: Int,
    type: InstructionType
  ): Operand {
    //variable
    if (operandString.indexOf(':') != -1) {
      /**
       * variable declaration, e.g:.
       * let a: Int, 123456
       * */

      return parseVariableDeclaration(vmFunctionScope, operandString, functionLine, type)
    }

    /**
     * variable usage, e.g.:
     *  mov r0, ss@[a] as dword
     *  mov ds@[b] as dword, r0
     * */
    val isStackVariable = vmFunctionScope.isVariableDefined(operandString)

    //TODO: we don't memory variables anymore (probably)
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

      //Hack to make "mov r0, a" return address of the variable "a"
      if (!isMemoryInnerOperand) {
        return C32(definedVariable.address)
      }

      //TODO: replace with memory address
      return Variable(
        operandString,
        definedVariable.address,
        definedVariable.variableType
      )
    } else {
      // if variable operand is not of Memory type then it is probably either a function's parameter or a function local
      // variable so we need to get it's stack frame and transform this operand to the Memory type operand

      val functionParameter = vmFunctionScope.getParameterByName(operandString)
      if (functionParameter != null) {
        //function parameter

        //Hack to make "mov r0, a" return address of the variable "a"
        if (!isMemoryInnerOperand) {
          return C32(functionParameter.stackFrame)
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

        //FIXME: wrong addressing mode
        return Memory(
          C32(functionParameter.stackFrame),
          null,
          Segment.Stack,
          functionParameter.type.addressingMode
        )
      } else {
        //local variable

        val localVariable = vmFunctionScope.getLocalVariableByName(operandString)
        if (localVariable == null) {
          throw ParsingException(
            vmFunctionScope.name,
            functionLine,
            "Variable with name ($operandString) is not a function parameter nor a local variable"
          )
        }

        //Hack to make "mov r0, a" return address of the variable "a"
        if (!isMemoryInnerOperand) {
          return C32(localVariable.stackFrame)
        }

        //FIXME: wrong addressing mode
        return Memory(
          C32(localVariable.stackFrame),
          null,
          Segment.Stack,
          localVariable.type.addressingMode
        )
      }
    }
  }

  private fun parseVariableDeclaration(
    vmFunctionScope: VmFunctionScope,
    operandString: String,
    functionLine: Int,
    type: InstructionType
  ): Variable {
    val (variableNameRaw, variableTypeRaw) = operandString.split(':').map { it.trim() }
    val variableName = VMParser.wordRegex.find(variableNameRaw)?.value
    if (variableName == null) {
      throw ParsingException(vmFunctionScope.name, functionLine, "Cannot parse variable name (${operandString})")
    }

    val variableType = VariableType.fromString(variableTypeRaw)
    if (variableType == null) {
      throw ParsingException(vmFunctionScope.name, functionLine, "Unknown variable type (${variableTypeRaw})")
    }

    val stackFrame = vmFunctionScope.getLocalVariableStackFrameByName(variableName)
    if (stackFrame == null) {
      throw ParsingException(vmFunctionScope.name, functionLine, "Variable ($variableName) was not defined")
    }

    return Variable(
      variableName,
      stackFrame,
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
      val operand = parseOperandInternal(vmFunctionScope, functionLine, operandName, type, vmMemory, true)
      val offsetOperand = parseOperandInternal(vmFunctionScope, functionLine, offsetOperandName, type, vmMemory, true)

      Pair(operand, offsetOperand)
    } else {
      val operand = parseOperandInternal(vmFunctionScope, functionLine, addressingParameters, type, vmMemory, true)

      //FIXME: HACK - this should be refactored.
      //this may happen when addressingParameters is a variable name and the variable has a stack segment
      if (operand is Memory<*> && operand.segment == Segment.Stack) {
        return Memory(operand.operand, operand.offsetOperand, operand.segment, addressingMode)
      }

      Pair(operand, null)
    }

    val (transformedOperand, transformedOffsetOperand) = transformOperands(
      operand,
      offsetOperand,
      vmFunctionScope,
      functionLine
    )

    //offset operand may be a Constant (C32) and a Register
    if (transformedOffsetOperand != null && (transformedOffsetOperand !is C32 || transformedOffsetOperand !is Register)) {
      throw ParsingException(
        vmFunctionScope.name,
        functionLine,
        "Cannot use operand of type (${transformedOffsetOperand.operandType}) as an offset operand ($operandString)"
      )
    }

    //TODO: replace with memory address
    return Memory(transformedOperand, transformedOffsetOperand, segment, addressingMode)
  }

  private fun transformOperands(
    operand: Operand,
    offsetOperand: Operand?,
    vmFunctionScope: VmFunctionScope,
    functionLine: Int
  ): Pair<Operand, Operand?> {
    val transformedOperand = if (operand is Memory<*>) {
      if (operand.operand !is C32) {
        throw ParsingException(
          vmFunctionScope.name,
          functionLine,
          "Operand ($operand) should be transformed into a Memory<C32> operand but it isn't (innerOperand = ${operand.operand})"
        )
      }

      operand.operand as C32
    } else {
      operand
    }

    if (offsetOperand != null && offsetOperand is C32 && transformedOperand is C32) {
      //here we have transformed operand from Memory<C32> to C32 and then combined it with offsetOperand which is
      //C32 as well, now we are getting rid og offsetOperand since we don't need it anymore
      return Pair(C32(transformedOperand.value + offsetOperand.value), null)
    }

    return Pair(transformedOperand, offsetOperand)
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