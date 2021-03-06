package crackme.vm.core.function

import crackme.vm.VM
import crackme.vm.core.AddressingMode
import crackme.vm.core.VariableType
import crackme.vm.operands.C64
import crackme.vm.operands.Constant
import crackme.vm.operands.Operand
import crackme.vm.operands.Register

object NativeFunctionCallbacks {

  private fun <T : Any> getParameterFromStack(vm: VM, variableType: VariableType): T {
    return when (variableType) {
      //TODO addressing mode for push/pop
      //pop64 here because C64 is the default constant size
      VariableType.IntType -> vm.vmStack.pop(AddressingMode.ModeDword) as T
      VariableType.LongType -> vm.vmStack.pop(AddressingMode.ModeQword) as T
      //TODO addressing mode for push/pop
      //pop64 here because C64 is the default constant size
      //FIXME: should this be C64????
      VariableType.StringType -> vm.vmStack.pop(AddressingMode.ModeQword) as T
    }
  }

  private val vmTestAddNumbers = NativeFunctionType.TestAddNumbers to fun (vm: VM, parameters: List<VariableType>): Long {
    val paramsCount = getParameterFromStack<Int>(vm, parameters[0])
    var sum = 0L

    for (i in 0 until paramsCount) {
      sum += getParameterFromStack<Int>(vm, parameters[i + 1])
    }

    return sum
  }

  private val vmSizeofCallback = NativeFunctionType.Sizeof to fun (vm: VM, parameters: List<Any>): Long {
    if (parameters.size != 2) {
      throw BadParametersCount(NativeFunctionType.Sizeof, 2, parameters.size)
    }

    val type = parameters[1] as VariableType

    return when (type) {
      VariableType.IntType -> 4
      VariableType.LongType -> 8
      VariableType.StringType -> {
        val obj = parameters[0] as String
        //TODO: should we really add 4 here? This is getting called in runtime and we probably don't need to reserve memory for string length

        //4 bytes reserved for string length + it's real length
        return (obj.length + 4).toLong()
      }
    }
  }

  private val vmAllocCallback = NativeFunctionType.Alloc to fun (vm: VM, parameters: List<Any>): Long {
    if (parameters.size != 1) {
      throw BadParametersCount(NativeFunctionType.Alloc, 1, parameters.size)
    }

    val operand = parameters[0] as Operand
    return when (operand) {
      is Constant -> {
        when (operand) {
          is C64 -> throw OperandNotSupportedForThisFunction(NativeFunctionType.Alloc, operand)
          else -> throw RuntimeException("Not implemented for ${operand.operandName}")
        }
      }
      is Register -> {
        val regValue = vm.registers[operand.index]
        vm.vmMemory.allocArray(regValue.toInt()).toLong()
      }
      else -> throw RuntimeException("Not implemented for ${operand.operandName}")
    }
  }

  private val parametersMap = mapOf(
    vmTestAddNumbers
//    vmSizeofCallback,
//    vmAllocCallback
  )

  fun getCallbackByFunctionType(type: NativeFunctionType): (vm: VM, List<VariableType>) -> Long {
    if (parametersMap[type] == null) {
      throw UnknownFunctionType(type)
    }

    return parametersMap.getValue(type)
  }

  class OperandNotSupportedForThisFunction(val funcType: NativeFunctionType,
                                           val operand: Operand) : Exception("Operand (${operand.operandName}) is not supported by function (${funcType.funcName})")
  class ParameterTypeNotSupportedForThisFunction(val funcType: NativeFunctionType,
                                                 val type: VariableType) : Exception("Parameter of operandType (${type.str} is not supported by function (${funcType.funcName}))")
  class UnknownFunctionType(type: NativeFunctionType) : Exception("Unknown VmFunctionScope operandType ${type.funcName}")
  class BadParametersCount(val funcType: NativeFunctionType,
                           val expected: Int,
                           val actual: Int) : Exception("Bad parameters count for function ${funcType.funcName} expected ${expected} but actual count is ${actual}")
}