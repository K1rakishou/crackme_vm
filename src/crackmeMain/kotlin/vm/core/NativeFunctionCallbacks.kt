package crackme.vm.core

import crackme.misc.safe
import crackme.vm.VM
import crackme.vm.operands.*

object NativeFunctionCallbacks {

  private val vmPrintlnCallback = NativeFunctionType.Println to fun (vm: VM, parameters: List<Any>): Long {
    if (parameters.size != 1) {
      throw BadParametersCount(NativeFunctionType.Println, 1, parameters.size)
    }

    val operand = parameters[0] as Operand

    when (operand) {
      is Constant -> {
        when (operand) {
          is C32 -> println(operand.value)
          is C64 -> println(operand.value)
          is VmString -> {
            val string = vm.vmMemory.getString(operand.address)
            println(string)
          }
          else -> throw RuntimeException("Not implemented for ${operand.operandName}")
        }.safe
      }
      is Register -> {
        val regValue = vm.registers[operand.index]
        println(regValue)
      }
      else -> throw RuntimeException("Not implemented for ${operand.operandName}")
    }.safe

    return 0
  }

  private val vmSizeofCallback = NativeFunctionType.Sizeof to fun (vm: VM, parameters: List<Any>): Long {
    if (parameters.size != 2) {
      throw BadParametersCount(NativeFunctionType.Sizeof, 2, parameters.size)
    }

    val type = parameters[1] as VariableType

    return when (type) {
      VariableType.AnyType -> throw ParameterTypeNotSupportedForThisFunction(NativeFunctionType.Sizeof, type)
      VariableType.IntType -> 4
      VariableType.LongType -> 8
      VariableType.StringType -> {
        val obj = parameters[0] as String
        //TODO: should we really add 4 here?
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
          is C32 -> vm.vmMemory.alloc(operand.value).toLong()
          is C64 -> throw OperandNotSupportedForThisFunction(NativeFunctionType.Alloc, operand)
          is VmString -> throw OperandNotSupportedForThisFunction(NativeFunctionType.Alloc, operand)
          else -> throw RuntimeException("Not implemented for ${operand.operandName}")
        }
      }
      is Register -> {
        val regValue = vm.registers[operand.index]
        vm.vmMemory.alloc(regValue.toInt()).toLong()
      }
      else -> throw RuntimeException("Not implemented for ${operand.operandName}")
    }
  }

  private val parametersMap = mapOf(
    vmPrintlnCallback,
    vmSizeofCallback,
    vmAllocCallback
  )

  fun getCallbackByFunctionType(type: NativeFunctionType): (vm: VM, parameters: List<Any>) -> Long {
    if (parametersMap[type] == null) {
      throw UnknownFunctionType(type)
    }

    return parametersMap.getValue(type)
  }

  class OperandNotSupportedForThisFunction(val funcType: NativeFunctionType,
                                           val operand: Operand) : Exception("Operand (${operand.operandName}) is not supported by function (${funcType.funcName})")
  class ParameterTypeNotSupportedForThisFunction(val funcType: NativeFunctionType,
                                                 val type: VariableType) : Exception("Parameter of type (${type.str} is not supported by function (${funcType.funcName}))")
  class UnknownFunctionType(type: NativeFunctionType) : Exception("Unknown Function type ${type.funcName}")
  class BadParametersCount(val funcType: NativeFunctionType,
                           val expected: Int,
                           val actual: Int) : Exception("Bad parameters count for function ${funcType.funcName} expected ${expected} but actual count is ${actual}")
}