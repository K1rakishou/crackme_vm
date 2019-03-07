package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.VMSimulator
import crackme.vm.core.VariableType
import crackme.vm.instructions.Mov
import crackme.vm.operands.*

class MovHandler : Handler<Mov> {

  override fun handle(vm: VM, eip: Int, instruction: Mov) {
    when (instruction.dest) {
      is Register -> {
        when (instruction.src) {
          //mov r0, 123
          is Constant -> executeMovRegConst(vm, eip, instruction, instruction.dest, instruction.src)
          //mov r0, [123]
          //mov r0, [r0]
          //mov r0, [abc] //TODO: test
          is Memory -> executeMovRegMemory(vm, eip, instruction, instruction.dest, instruction.src)
          //mov r0, r1
          is Register -> executeMovRegReg(vm, eip, instruction, instruction.dest, instruction.src)
          //mov r0, abc
          is Variable -> executeMovRegVariable(vm, eip, instruction, instruction.dest, instruction.src)
        }
      }
      is Memory -> {
        when (instruction.src) {
          //mov [123], r0
          //mov [r0], r0
          //mov [abc], r0
          is Register -> executeMovMemoryReg(vm, eip, instruction, instruction.dest, instruction.src)
          //mov [123], 1234
          is Constant,
          //mov [123], abc
          is Variable,
          //mov [123], [1234]
          is Memory -> {
            throw VMSimulator.VmExecutionException(eip, "Operand (${instruction.dest.operandName}) cannot be used as destination with instruction ($instruction)")
          }
        }
      }
      is Constant,
        //mov 123, *
      is Variable -> {
        //mov abc, *
        throw VMSimulator.VmExecutionException(eip, "Operand (${instruction.dest.operandName}) cannot be used as destination with instruction ($instruction)")
      }
    }
  }

  private fun executeMovMemoryReg(vm: VM, eip: Int, instruction: Mov, dest: Memory, src: Register) {
    when (dest.operand) {
      is Register -> {
        //mov [r0], r0
        vm.vmMemory.putLong(vm.registers[dest.operand.index].toInt(), vm.registers[src.index])
      }
      is Variable -> {
        //mov [abc], r0
        when (dest.operand.variableType) {
          VariableType.AnyType -> {
            throw VMSimulator.VmExecutionException(eip, "Variable with operandType (${dest.operand.operandType}) cannot be used with Memory operand")
          }
          VariableType.LongType -> {
            vm.registers[dest.operand.address] = vm.vmMemory.getLong(src.index)
          }
          VariableType.StringType -> {
            throw VMSimulator.VmExecutionException(eip, "Variable with operandType (${dest.operand.operandType}) cannot be used with Memory operand")
          }
        }
      }
      is Constant -> {
        //mov [123], r0
        when (dest.operand) {
          is C64 -> vm.vmMemory.putLong(dest.operand.value.toInt(), vm.registers[src.index])
          is VmString -> {
            throw VMSimulator.VmExecutionException(eip, "VmString cannot be used as memory address")
          }
        }
      }
      is Memory -> {
        //mov [[???]], r0
        throw VMSimulator.VmExecutionException(eip, "Operand (${dest.operand.operandName}) cannot be used as Memory operand")
      }
    }
  }

  private fun executeMovRegConst(vm: VM, eip: Int, instruction: Mov, dest: Register, src: Constant) {
    if (src is VmString) {
      //mov r0, "test"
      throw VMSimulator.VmExecutionException(eip, "Operand (${src.operandName}) cannot be used as source with instruction ($instruction)")
    }

    when (src) {
      is C64 -> vm.registers[dest.index] = src.value
      else -> throw NotImplementedError("getConstantValue not implemented for constant operandType (${src.operandName})")
    }
  }

  private fun executeMovRegMemory(vm: VM, eip: Int, instruction: Mov, dest: Register, src: Memory) {
    when (src.operand) {
      is Constant -> {
        if (src.operand is VmString) {
          //mov r0, ["test"]
          throw VMSimulator.VmExecutionException(eip, "Operand (${src.operandName}) cannot be used as source with instruction ($instruction)")
        }

        vm.registers[dest.index] = getConstantValue(vm, src.operand)
      }
      is Memory -> {
        //mov r0, [[???]]
        throw VMSimulator.VmExecutionException(eip, "Operand (${src.operand.operandName}) cannot be used as Memory operand")
      }
      is Register -> {
        //mov r0, [r0]
        vm.registers[dest.index] = vm.vmMemory.getLong(vm.registers[src.operand.index].toInt())
      }
      is Variable -> {
        //mov r0, [abc]
        if (src.operand.variableType == VariableType.AnyType || src.operand.variableType == VariableType.StringType) {
          throw VMSimulator.VmExecutionException(eip, "Variable with operandType (${src.operand.operandType}) cannot be used with Memory operand")
        }

        vm.registers[dest.index] = getVmMemoryVariableValue(vm, eip, src.operand)
      }
    }
  }

  private fun executeMovRegReg(vm: VM, eip: Int, instruction: Mov, dest: Register, src: Register) {
    vm.registers[dest.index] = vm.registers[src.index]
  }

  private fun executeMovRegVariable(vm: VM, eip: Int, instruction: Mov, dest: Register, src: Variable) {
    vm.registers[dest.index] = src.address.toLong()
  }

  private fun getVmMemoryVariableValue(vm: VM, eip: Int, operand: Variable): Long {
    return when (operand.variableType) {
      VariableType.LongType -> vm.vmMemory.getLong(operand.address)
      VariableType.StringType -> vm.vmMemory.getString(operand.address).toLong()
      VariableType.AnyType -> {
        throw VMSimulator.VmExecutionException(eip, "Variable with operandType (${operand.operandType}) cannot be used with Memory operand")
      }
    }
  }

  private fun getConstantValue(vm: VM, operand: Constant): Long {
    return when (operand) {
      is C64 -> vm.vmMemory.getLong(operand.value.toInt())
      else -> throw NotImplementedError("getConstantValue not implemented for constant operandType (${operand.operandName})")
    }
  }
}