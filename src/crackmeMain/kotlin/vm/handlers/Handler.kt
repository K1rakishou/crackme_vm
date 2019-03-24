package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.core.AddressingMode
import crackme.vm.core.VmExecutionException
import crackme.vm.instructions.Instruction
import crackme.vm.operands.*

abstract class Handler<T : Instruction> {

  //ip -> new ip
  abstract fun handle(vm: VM, currentEip: Int, instruction: T): Int

  protected fun warning(eip: Int, message: String) {
    println("Warning at $eip instruction, $message")
  }

  protected fun extractValueFromConstant(eip: Int, constant: Constant, addressingMode: AddressingMode): Long {
    return when (constant) {
      is C64 -> {
        if (addressingMode < AddressingMode.ModeQword) {
          throw VmExecutionException(eip, "Constant size (C64) does not match addressing mode ($addressingMode)")
        }

        constant.value
      }
      is C32 -> {
        if (addressingMode < AddressingMode.ModeDword) {
          throw VmExecutionException(eip, "Constant size (C32) does not match addressing mode ($addressingMode)")
        }

        constant.value.toLong()
      }
      else -> throw VmExecutionException(eip, "Not implemented for constant of type ($constant)")
    }
  }

  private fun applyAddressingMode(value: Long, addressingMode: AddressingMode): Long {
    return when (addressingMode) {
      //FIXME: there may be a bug left. Gotta test this with negative numbers a little bit more
      AddressingMode.ModeByte -> value and 0xFF
      AddressingMode.ModeWord -> value and 0xFFFF
      AddressingMode.ModeDword -> value and 0xFFFFFFFF
      AddressingMode.ModeQword -> value
    }
  }

  private fun getOffsetOperandValue(vm: VM, eip: Int, offsetOperand: Operand?): Int {
    if (offsetOperand == null) {
      return 0
    }

    return when (offsetOperand) {
      is Constant -> {
        when (offsetOperand) {
          is C32 -> offsetOperand.value
          else -> {
            throw VmExecutionException(eip, "Cannot use constant ($offsetOperand) as an offset")
          }
        }
      }
      is Register -> {
        val value = vm.registers[offsetOperand.index]
        if (value < Int.MIN_VALUE || value > Int.MAX_VALUE) {
          warning(eip, "Unsafe cast: register value is out of Int bounds ($value)")
        }

        value.toInt()
      }
      else -> {
        throw VmExecutionException(eip, "Cannot use operand ($offsetOperand) as an offset operand")
      }
    }
  }

  protected fun getConstantValueFromVmMemory(vm: VM, operand: Constant): Long {
    return when (operand) {
      is C64 -> vm.vmMemory.getLong(operand.value.toInt())
      is C32 -> vm.vmMemory.getInt(operand.value).toLong()
      else -> throw NotImplementedError("getConstantValueFromVmMemory not implemented for constant operandType (${operand.operandName})")
    }
  }

  protected fun getConstantValueFromVmStack(vm: VM, operand: Constant): Long {
    return when (operand) {
      is C64 -> vm.vmStack.peek64At(operand.value.toInt())
      is C32 -> vm.vmStack.peek32At(operand.value).toLong()
      else -> throw NotImplementedError("getConstantValueFromVmStack not implemented for constant operandType (${operand.operandName})")
    }
  }

  protected fun putConstantValueIntoMemory(vm: VM, operand: Constant, value: Long) {
    when (operand) {
      is C64 -> vm.vmMemory.putLong(operand.value.toInt(), value)
      is C32 -> vm.vmMemory.putInt(operand.value, value.toInt())
      else -> throw NotImplementedError("getConstantValueFromVmMemory not implemented for constant operandType (${operand.operandName})")
    }
  }

  protected fun putConstantValueIntoStack(vm: VM, operand: Constant, value: Long) {
    when (operand) {
      is C64 -> vm.vmStack.set64At(operand.value.toInt(), value)
      is C32 -> vm.vmStack.set32At(operand.value, value.toInt())
      else -> throw NotImplementedError("putConstantValueIntoStack not implemented for constant operandType (${operand.operandName})")
    }
  }

  protected fun getVmMemoryValueByConstant(vm: VM, eip: Int, memoryOperand: Memory<Constant>): Long {
    val operand = memoryOperand.operand
    val offsetOperand = memoryOperand.offsetOperand
    val addressingMode = memoryOperand.addressingMode

    val constantValue = when (operand) {
      //TODO: probably should forbid C64 here
      is C64 -> operand.value.toInt()
      is C32 -> operand.value
      else -> throw VmExecutionException(eip, "Unknown constant type ($operand)")
    }

    //FIXME: unsafe cast
    val address = constantValue + getOffsetOperandValue(vm, eip, offsetOperand)

    val value = when (memoryOperand.segment) {
      Segment.Memory -> when (addressingMode) {
        AddressingMode.ModeByte -> vm.vmMemory.getByte(address).toLong()
        AddressingMode.ModeWord -> vm.vmMemory.getShort(address).toLong()
        AddressingMode.ModeDword -> vm.vmMemory.getInt(address).toLong()
        AddressingMode.ModeQword -> vm.vmMemory.getLong(address)
      }
      Segment.Stack -> when (addressingMode) {
        AddressingMode.ModeByte -> vm.vmStack.peek8At(address).toLong()
        AddressingMode.ModeWord -> vm.vmStack.peek16At(address).toLong()
        AddressingMode.ModeDword -> vm.vmStack.peek32At(address).toLong()
        AddressingMode.ModeQword -> vm.vmStack.peek64At(address)
      }
    }

    return applyAddressingMode(value, addressingMode)
  }

  protected fun putVmMemoryValueByConstant(memoryOperand: Memory<Constant>, vm: VM, value: Long, eip: Int) {
    val operand = memoryOperand.operand
    val offsetOperand = memoryOperand.offsetOperand
    val addressingMode = memoryOperand.addressingMode

    val constantValue = when (operand) {
      //TODO: probably should forbid C64 here
      is C64 -> operand.value.toInt()
      is C32 -> operand.value
      else -> throw VmExecutionException(eip, "Unknown constant type ($operand)")
    }

    //FIXME: unsafe cast
    val address = constantValue + getOffsetOperandValue(vm, eip, offsetOperand)
    val convertedValue = applyAddressingMode(value, addressingMode)

    when (memoryOperand.segment) {
      Segment.Memory -> when (addressingMode) {
        AddressingMode.ModeByte -> vm.vmMemory.putByte(address, convertedValue.toByte())
        AddressingMode.ModeWord -> vm.vmMemory.putShort(address, convertedValue.toShort())
        AddressingMode.ModeDword -> vm.vmMemory.putInt(address, convertedValue.toInt())
        AddressingMode.ModeQword -> vm.vmMemory.putLong(address, convertedValue)
      }
      Segment.Stack -> when (addressingMode) {
        AddressingMode.ModeByte -> vm.vmStack.set8At(address, convertedValue.toByte())
        AddressingMode.ModeWord -> vm.vmStack.set16At(address, convertedValue.toShort())
        AddressingMode.ModeDword -> vm.vmStack.set32At(address, convertedValue.toInt())
        AddressingMode.ModeQword -> vm.vmStack.set64At(address, convertedValue)
      }
    }
  }

  protected fun getVmMemoryValueByRegister(vm: VM, eip: Int, memoryOperand: Memory<Register>): Long {
    val operand = memoryOperand.operand
    val offsetOperand = memoryOperand.offsetOperand
    val addressingMode = memoryOperand.addressingMode

    //FIXME: unsafe cast
    val address = vm.registers[operand.index].toInt() + getOffsetOperandValue(vm, eip, offsetOperand)

    val value = when (memoryOperand.segment) {
      Segment.Memory -> when (addressingMode) {
        AddressingMode.ModeByte -> vm.vmMemory.getByte(address).toLong()
        AddressingMode.ModeWord -> vm.vmMemory.getShort(address).toLong()
        AddressingMode.ModeDword -> vm.vmMemory.getInt(address).toLong()
        AddressingMode.ModeQword -> vm.vmMemory.getLong(address)
      }
      Segment.Stack -> when (addressingMode) {
        AddressingMode.ModeByte -> vm.vmStack.peek8At(address).toLong()
        AddressingMode.ModeWord -> vm.vmStack.peek16At(address).toLong()
        AddressingMode.ModeDword -> vm.vmStack.peek32At(address).toLong()
        AddressingMode.ModeQword -> vm.vmStack.peek64At(address)
      }
    }

    return applyAddressingMode(value, addressingMode)
  }

  protected fun putVmMemoryValueByRegister(memoryOperand: Memory<Register>, vm: VM, value: Long, eip: Int) {
    val operand = memoryOperand.operand
    val offsetOperand = memoryOperand.offsetOperand
    val addressingMode = memoryOperand.addressingMode

    //FIXME: unsafe cast
    val address = vm.registers[operand.index].toInt() + getOffsetOperandValue(vm, eip, offsetOperand)

    when (memoryOperand.segment) {
      Segment.Memory -> when (addressingMode) {
        AddressingMode.ModeByte -> vm.vmMemory.putByte(address, applyAddressingMode(value, addressingMode).toByte())
        AddressingMode.ModeWord -> vm.vmMemory.putShort(address, applyAddressingMode(value, addressingMode).toShort())
        AddressingMode.ModeDword -> vm.vmMemory.putInt(address, applyAddressingMode(value, addressingMode).toInt())
        AddressingMode.ModeQword -> vm.vmMemory.putLong(address, applyAddressingMode(value, addressingMode))
      }
      Segment.Stack ->  when (addressingMode) {
        AddressingMode.ModeByte -> vm.vmStack.set8At(address, applyAddressingMode(value, addressingMode).toByte())
        AddressingMode.ModeWord -> vm.vmStack.set16At(address, applyAddressingMode(value, addressingMode).toShort())
        AddressingMode.ModeDword -> vm.vmStack.set32At(address, applyAddressingMode(value, addressingMode).toInt())
        AddressingMode.ModeQword -> vm.vmStack.set64At(address, applyAddressingMode(value, addressingMode))
      }
    }
  }

}