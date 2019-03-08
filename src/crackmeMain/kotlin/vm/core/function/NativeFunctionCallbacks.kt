package crackme.vm.core.function

import crackme.misc.safe
import crackme.vm.VM
import crackme.vm.core.VariableType
import crackme.vm.operands.*

object NativeFunctionCallbacks {

  private val vmTestAddNumbers = NativeFunctionType.TestAddNumbers to fun (vm: VM, parameters: List<Any>): Long {
    if (parameters.isEmpty()) {
      throw BadParametersCount(NativeFunctionType.TestAddNumbers, 1, parameters.size)
    }

    val paramsCount = parameters[0] as C32
    var sum = 0L

    for (i in 0 until paramsCount.value) {
      val operand = parameters[i + 1] as Operand

      when (operand) {
        is Constant -> {
          when (operand) {
            is C32 -> sum += operand.value
            is C64 -> sum += operand.value
            else -> throw RuntimeException("Not implemented for ${operand.operandName}")
          }.safe
        }
        is Register -> sum += vm.registers[operand.index]
        else -> throw RuntimeException("Not implemented for ${operand.operandName}")
      }.safe
    }

    return sum
  }

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
    vmTestAddNumbers,
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
                                                 val type: VariableType) : Exception("Parameter of operandType (${type.str} is not supported by function (${funcType.funcName}))")
  class UnknownFunctionType(type: NativeFunctionType) : Exception("Unknown Function operandType ${type.funcName}")
  class BadParametersCount(val funcType: NativeFunctionType,
                           val expected: Int,
                           val actual: Int) : Exception("Bad parameters count for function ${funcType.funcName} expected ${expected} but actual count is ${actual}")
}