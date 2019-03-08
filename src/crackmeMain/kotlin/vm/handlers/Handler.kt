package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.core.VariableType
import crackme.vm.core.VmExecutionException
import crackme.vm.instructions.Instruction
import crackme.vm.operands.*

abstract class Handler<T : Instruction> {

  //eip -> new eip
  abstract fun handle(vm: VM, currentEip: Int, instruction: T): Int

  protected fun getVmMemoryVariableValue(vm: VM, eip: Int, operand: Variable): Long {
    return when (operand.variableType) {
      VariableType.IntType -> vm.vmMemory.getInt(operand.address).toLong()
      VariableType.LongType -> vm.vmMemory.getLong(operand.address)
      //FIXME why are we casting a string from the vmMemory to long??? We should probably return it's address
      VariableType.StringType -> vm.vmMemory.getString(operand.address).toLong()
      VariableType.AnyType -> {
        throw VmExecutionException(eip, "Variable with operandType (${operand.operandType}) cannot be used with Memory operand")
      }
    }
  }

  protected fun getConstantValue(vm: VM, operand: Constant): Long {
    return when (operand) {
      is C64 -> vm.vmMemory.getLong(operand.value.toInt())
      is C32 -> vm.vmMemory.getInt(operand.value).toLong()
      else -> throw NotImplementedError("getConstantValue not implemented for constant operandType (${operand.operandName})")
    }
  }

  protected fun putVmMemoryVariableValueFromRegister(dest: Memory<Variable>, vm: VM, src: Register, eip: Int) {
    when (dest.operand.variableType) {
      VariableType.IntType -> vm.vmMemory.putInt(dest.operand.address, vm.registers[src.index].toInt())
      VariableType.LongType -> vm.vmMemory.putLong(dest.operand.address, vm.registers[src.index])
      VariableType.AnyType,
      VariableType.StringType -> {
        throw VmExecutionException(eip, "Variable with operandType (${dest.operand.operandType}) cannot be used with Memory operand")
      }
    }
  }

  protected fun warning(eip: Int, message: String) {
    println("Warning at $eip instruction, $message")
  }
}