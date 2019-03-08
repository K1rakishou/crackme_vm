package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.core.VariableType
import crackme.vm.core.VmExecutionException
import crackme.vm.instructions.Let
import crackme.vm.operands.*

class LetHandler : Handler<Let>() {

  override fun handle(vm: VM, currentEip: Int, instruction: Let): Int {
    if (instruction.variable.variableType == VariableType.AnyType) {
      throw VmExecutionException(currentEip, "Cannot use Let with \'Any\' type")
    }

    //let a: String, "Hello from VM!" -> OK
    //let a: Int, 123 -> OK
    //let a: Int, 1122334455 -> NOT OK
    //let a: Long, 123 -> OK
    //let a: Long, 11223344556677889900 -> NOT OK
    //let a: Int, r0 -> NOT OK //we can't know what value will be in the register at the parsing stage
    //let a: Long, r0 -> NOT OK //we can't know what value will be in the register at the parsing stage

    when (instruction.initializer) {
      is Constant -> {
        when (val constant = instruction.initializer) {
          is C32 -> {
            if (instruction.variable.variableType != VariableType.IntType && instruction.variable.variableType != VariableType.LongType) {
              throw VmExecutionException(
                currentEip,
                "Initializer type (${constant.operandName}) differs from the variable type ${instruction.variable.variableType.str}"
              )
            }

            if (instruction.variable.variableType == VariableType.IntType) {
              vm.vmMemory.putInt(instruction.variable.address, constant.value)
            } else if (instruction.variable.variableType == VariableType.LongType) {
              vm.vmMemory.putLong(instruction.variable.address, constant.value.toLong())
            }
          }
          is C64 -> {
            if (instruction.variable.variableType != VariableType.LongType) {
              throw VmExecutionException(
                currentEip,
                "2 Initializer type (${constant.operandName}) differs from the variable type ${instruction.variable.variableType.str}"
              )
            }

            vm.vmMemory.putLong(instruction.variable.address, constant.value)
          }
          is VmString -> {
            if (instruction.variable.variableType != VariableType.StringType) {
              throw VmExecutionException(
                currentEip,
                "Initializer type (${constant.operandName}) differs from the variable type ${instruction.variable.variableType.str}"
              )
            }

            //already initialized
          }
        }
      }
      is Register -> {
        when (instruction.variable.variableType) {
          VariableType.IntType -> {
            warning(currentEip, "Unsafe cast from Register to C32 (64 -> 32)")
            vm.vmMemory.putInt(instruction.variable.address, vm.registers[instruction.initializer.index].toInt())
          }
          VariableType.LongType -> {
            vm.vmMemory.putLong(instruction.variable.address, vm.registers[instruction.initializer.index])
          }
          VariableType.AnyType,
          VariableType.StringType -> {
            throw VmExecutionException(currentEip, "Cannot initialize variable of type (${instruction.variable.variableType}) with register")
          }
        }
      }
      is Memory<*> -> throw VmExecutionException(currentEip, "Cannot initialize variable with Memory type operand")
      is Variable -> throw VmExecutionException(currentEip, "Cannot initialize variable with another variable")
    }

    return currentEip + 1
  }

}