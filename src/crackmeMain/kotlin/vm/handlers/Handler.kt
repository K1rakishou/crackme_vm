package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.core.AddressingMode
import crackme.vm.core.Utils
import crackme.vm.core.VariableType
import crackme.vm.core.VmExecutionException
import crackme.vm.instructions.Instruction
import crackme.vm.operands.*

abstract class Handler<T : Instruction> {

  //ip -> new ip
  abstract fun handle(vm: VM, currentEip: Int, instruction: T): Int

  protected fun warning(eip: Int, message: String) {
    println("Warning at $eip instruction, $message")
  }

  private fun applyAddressingMode(value: Long, addressingMode: AddressingMode): Long {
    return when (addressingMode) {
      //FIXME: there may be a bug left. Gotta test this with negative numbers a little bit more
      AddressingMode.ModeByte -> value % Byte.MAX_VALUE
      AddressingMode.ModeWord -> value % Short.MAX_VALUE
      AddressingMode.ModeDword -> value % Int.MAX_VALUE
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
      is C64 -> {
        println("C64 address = ${operand.value.toInt()}, value = ${vm.vmStack.peek64At(operand.value.toInt())}")
        vm.vmStack.peek64At(operand.value.toInt())
      }
      is C32 -> {
        println("C32 address = ${operand.value}, value = ${vm.vmStack.peek32At(operand.value).toLong()}")
        vm.vmStack.peek32At(operand.value).toLong()
      }
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

  //TODO: merge getVmMemoryValueByConstant and getVmStackValueByConstant into one function
  //TODO: merge putVmStackValueByConstant and putVmMemoryValueByConstant into one function
  //TODO: merge getVmMemoryValueByRegister and getVmStackValueByRegister into one function
  //TODO: merge putVmMemoryValueByRegister and putVmStackValueByRegister into one function

  protected fun getVmMemoryValueByRegister(vm: VM, eip: Int, memoryOperand: Memory<Register>): Long {
    if (memoryOperand.segment != Segment.Memory) {
      throw VmExecutionException(eip, "Bad segment (${memoryOperand.segment.segmentName}), supposed to be Memory")
    }

    val operand = memoryOperand.operand
    val offsetOperand = memoryOperand.offsetOperand
    val addressingMode = memoryOperand.addressingMode

    //FIXME: unsafe cast
    val address = vm.registers[operand.index].toInt() + getOffsetOperandValue(vm, eip, offsetOperand)

    val value = when (addressingMode) {
      AddressingMode.ModeByte -> vm.vmMemory.getByte(address).toLong()
      AddressingMode.ModeWord -> vm.vmMemory.getShort(address).toLong()
      AddressingMode.ModeDword -> vm.vmMemory.getInt(address).toLong()
      AddressingMode.ModeQword -> vm.vmMemory.getLong(address)
    }

    return applyAddressingMode(value, addressingMode)
  }

  protected fun getVmStackValueByRegister(vm: VM, eip: Int, memoryOperand: Memory<Register>): Long {
    if (memoryOperand.segment != Segment.Stack) {
      throw VmExecutionException(eip, "Bad segment (${memoryOperand.segment.segmentName}), supposed to be Stack")
    }

    val operand = memoryOperand.operand
    val offsetOperand = memoryOperand.offsetOperand
    val addressingMode = memoryOperand.addressingMode

    //FIXME: unsafe cast
    val address = vm.registers[operand.index].toInt() + getOffsetOperandValue(vm, eip, offsetOperand)

    val value = when (addressingMode) {
      AddressingMode.ModeDword -> vm.vmStack.peek32At(address).toLong()
      AddressingMode.ModeQword -> vm.vmStack.peek64At(address)
      AddressingMode.ModeByte,
      AddressingMode.ModeWord -> throw VmExecutionException(eip, "Stack read with byte/word addressing is not implemented yet")
    }

    return applyAddressingMode(value, addressingMode)
  }

  protected fun getVmMemoryValueByConstant(vm: VM, eip: Int, memoryOperand: Memory<Constant>): Long {
    if (memoryOperand.segment != Segment.Memory) {
      throw VmExecutionException(eip, "Bad segment (${memoryOperand.segment.segmentName}), supposed to be Memory")
    }

    val operand = memoryOperand.operand
    val offsetOperand = memoryOperand.offsetOperand
    val addressingMode = memoryOperand.addressingMode

    val constantValue = when (operand) {
      is C64 -> operand.value.toInt()
      is C32 -> operand.value
      is VmString -> throw VmExecutionException(eip, "Cannot use VmString with Memory operand")
      else -> throw VmExecutionException(eip, "Unknown constant type ($operand)")
    }

    //FIXME: unsafe cast
    val address = constantValue + getOffsetOperandValue(vm, eip, offsetOperand)

    val value = when (addressingMode) {
      AddressingMode.ModeDword -> vm.vmMemory.getInt(address).toLong()
      AddressingMode.ModeQword -> vm.vmMemory.getLong(address)
      AddressingMode.ModeByte,
      AddressingMode.ModeWord -> throw VmExecutionException(eip, "Stack read with byte/word addressing is not implemented yet")
    }

    return applyAddressingMode(value, addressingMode)
  }

  protected fun getVmStackValueByConstant(vm: VM, eip: Int, memoryOperand: Memory<Constant>): Long {
    if (memoryOperand.segment != Segment.Stack) {
      throw VmExecutionException(eip, "Bad segment (${memoryOperand.segment.segmentName}), supposed to be Stack")
    }

    val operand = memoryOperand.operand
    val offsetOperand = memoryOperand.offsetOperand
    val addressingMode = memoryOperand.addressingMode

    val constantValue = when (operand) {
      is C64 -> operand.value.toInt()
      is C32 -> operand.value
      is VmString -> throw VmExecutionException(eip, "Cannot use VmString with Memory operand")
      else -> throw VmExecutionException(eip, "Unknown constant type ($operand)")
    }

    //FIXME: unsafe cast
    val address = constantValue + getOffsetOperandValue(vm, eip, offsetOperand)

    val value = when (addressingMode) {
      AddressingMode.ModeDword -> vm.vmStack.peek32At(address).toLong()
      AddressingMode.ModeQword -> vm.vmStack.peek64At(address)
      AddressingMode.ModeByte,
      AddressingMode.ModeWord -> throw VmExecutionException(eip, "Stack read with byte/word addressing is not implemented yet")
    }

    return applyAddressingMode(value, addressingMode)
  }

  protected fun putVmMemoryValueByRegister(memoryOperand: Memory<Register>, vm: VM, value: Long, eip: Int) {
    if (memoryOperand.segment != Segment.Memory) {
      throw VmExecutionException(eip, "Bad segment (${memoryOperand.segment.segmentName}), supposed to be Memory")
    }

    val operand = memoryOperand.operand
    val offsetOperand = memoryOperand.offsetOperand
    val addressingMode = memoryOperand.addressingMode

    //FIXME: unsafe cast
    val address = vm.registers[operand.index].toInt() + getOffsetOperandValue(vm, eip, offsetOperand)

    when (addressingMode) {
      AddressingMode.ModeByte -> vm.vmMemory.putByte(address, applyAddressingMode(value, addressingMode).toByte())
      AddressingMode.ModeWord -> vm.vmMemory.putShort(address, applyAddressingMode(value, addressingMode).toShort())
      AddressingMode.ModeDword -> vm.vmMemory.putInt(address, applyAddressingMode(value, addressingMode).toInt())
      AddressingMode.ModeQword -> vm.vmMemory.putLong(address, applyAddressingMode(value, addressingMode))
    }
  }

  protected fun putVmStackValueByRegister(memoryOperand: Memory<Register>, vm: VM, value: Long, eip: Int) {
    if (memoryOperand.segment != Segment.Stack) {
      throw VmExecutionException(eip, "Bad segment (${memoryOperand.segment.segmentName}), supposed to be Stack")
    }

    val operand = memoryOperand.operand
    val offsetOperand = memoryOperand.offsetOperand
    val addressingMode = memoryOperand.addressingMode

    //FIXME: unsafe cast
    val address = vm.registers[operand.index].toInt() + getOffsetOperandValue(vm, eip, offsetOperand)

    when (addressingMode) {
      AddressingMode.ModeDword -> vm.vmStack.set32At(address, applyAddressingMode(value, addressingMode).toInt())
      AddressingMode.ModeQword -> vm.vmStack.set64At(address, applyAddressingMode(value, addressingMode))
      AddressingMode.ModeByte,
      AddressingMode.ModeWord -> throw VmExecutionException(eip, "Stack write with byte/word addressing is not implemented yet")
    }
  }

  protected fun putVmStackValueByConstant(memoryOperand: Memory<Constant>, vm: VM, value: Long, eip: Int) {
    if (memoryOperand.segment != Segment.Stack) {
      throw VmExecutionException(eip, "Bad segment (${memoryOperand.segment.segmentName}), supposed to be Stack")
    }

    val operand = memoryOperand.operand
    val offsetOperand = memoryOperand.offsetOperand
    val addressingMode = memoryOperand.addressingMode

    val constantValue = when (operand) {
      is C64 -> operand.value.toInt()
      is C32 -> operand.value
      is VmString -> throw VmExecutionException(eip, "Cannot use VmString with Memory operand")
      else -> throw VmExecutionException(eip, "Unknown constant type ($operand)")
    }

    //FIXME: unsafe cast
    val address = constantValue + getOffsetOperandValue(vm, eip, offsetOperand)
    val convertedValue = applyAddressingMode(value, addressingMode)

    when (addressingMode) {
      AddressingMode.ModeDword -> vm.vmStack.set32At(address, convertedValue.toInt())
      AddressingMode.ModeQword -> vm.vmStack.set64At(address, convertedValue)
      AddressingMode.ModeByte,
      AddressingMode.ModeWord -> throw VmExecutionException(eip, "Stack write with byte/word addressing is not implemented yet")
    }
  }

  protected fun putVmMemoryValueByConstant(memoryOperand: Memory<Constant>, vm: VM, value: Long, eip: Int) {
    if (memoryOperand.segment != Segment.Memory) {
      throw VmExecutionException(eip, "Bad segment (${memoryOperand.segment.segmentName}), supposed to be Memory")
    }

    val operand = memoryOperand.operand
    val offsetOperand = memoryOperand.offsetOperand
    val addressingMode = memoryOperand.addressingMode

    val constantValue = when (operand) {
      is C64 -> operand.value.toInt()
      is C32 -> operand.value
      is VmString -> throw VmExecutionException(eip, "Cannot use VmString with Memory operand")
      else -> throw VmExecutionException(eip, "Unknown constant type ($operand)")
    }

    //FIXME: unsafe cast
    val address = constantValue + getOffsetOperandValue(vm, eip, offsetOperand)

    when (addressingMode) {
      AddressingMode.ModeByte -> vm.vmMemory.putByte(address, applyAddressingMode(value, addressingMode).toByte())
      AddressingMode.ModeWord -> vm.vmMemory.putShort(address, applyAddressingMode(value, addressingMode).toShort())
      AddressingMode.ModeDword -> vm.vmMemory.putInt(address, applyAddressingMode(value, addressingMode).toInt())
      AddressingMode.ModeQword -> vm.vmMemory.putLong(address, applyAddressingMode(value, addressingMode))
    }
  }

  protected fun getVmMemoryValueByVariable(vm: VM, eip: Int, memoryOperand: Memory<Variable>): Long {
    val operand = memoryOperand.operand
    val offsetOperand = memoryOperand.offsetOperand
    val addressingMode = memoryOperand.addressingMode

    return when (memoryOperand.operand.variableType) {
      VariableType.IntType -> {
        val address = operand.address + getOffsetOperandValue(vm, eip, offsetOperand)
        val value = when (addressingMode) {
          AddressingMode.ModeByte -> vm.vmMemory.getByte(address).toLong()
          AddressingMode.ModeWord -> vm.vmMemory.getShort(address).toLong()
          AddressingMode.ModeDword -> vm.vmMemory.getInt(address).toLong()
          AddressingMode.ModeQword -> vm.vmMemory.getLong(address)
        }

        applyAddressingMode(value, addressingMode)
      }
      VariableType.LongType -> {
        val address = operand.address + getOffsetOperandValue(vm, eip, offsetOperand)
        val value = when (addressingMode) {
          AddressingMode.ModeByte -> vm.vmMemory.getByte(address).toLong()
          AddressingMode.ModeWord -> vm.vmMemory.getShort(address).toLong()
          AddressingMode.ModeDword -> vm.vmMemory.getInt(address).toLong()
          AddressingMode.ModeQword -> vm.vmMemory.getLong(address)
        }

        applyAddressingMode(value, addressingMode)
      }
      VariableType.StringType -> {
        //we need to add 4 here, because first 4 bytes of the string are it's size
        val address = operand.address + getOffsetOperandValue(vm, eip, offsetOperand) + 4
        val bytes = vm.vmMemory.slice(address, address + addressingMode.size.toInt())

        when (addressingMode) {
          AddressingMode.ModeByte -> bytes[0].toLong()
          AddressingMode.ModeWord -> Utils.readShortFromByteArray(0, bytes).toLong()
          AddressingMode.ModeDword -> Utils.readIntFromArray(0, bytes).toLong()
          AddressingMode.ModeQword -> Utils.readLongFromByteArray(0, bytes)
        }
      }
    }
  }

  protected fun putVmMemoryValueByVariable(memoryOperand: Memory<Variable>, vm: VM, value: Long, eip: Int) {
    val operand = memoryOperand.operand
    val offsetOperand = memoryOperand.offsetOperand
    val addressingMode = memoryOperand.addressingMode

    when (memoryOperand.operand.variableType) {
      VariableType.IntType -> {
        val address = operand.address + getOffsetOperandValue(vm, eip, offsetOperand)

        when (addressingMode) {
          AddressingMode.ModeByte -> vm.vmMemory.putByte(address, applyAddressingMode(value, addressingMode).toByte())
          AddressingMode.ModeWord -> vm.vmMemory.putShort(address, applyAddressingMode(value, addressingMode).toShort())
          AddressingMode.ModeDword -> vm.vmMemory.putInt(address, applyAddressingMode(value, addressingMode).toInt())
          AddressingMode.ModeQword -> vm.vmMemory.putLong(address, applyAddressingMode(value, addressingMode))
        }
      }
      VariableType.LongType -> {
        val address = operand.address + getOffsetOperandValue(vm, eip, offsetOperand)

        when (addressingMode) {
          AddressingMode.ModeByte -> vm.vmMemory.putByte(address, applyAddressingMode(value, addressingMode).toByte())
          AddressingMode.ModeWord -> vm.vmMemory.putShort(address, applyAddressingMode(value, addressingMode).toShort())
          AddressingMode.ModeDword -> vm.vmMemory.putInt(address, applyAddressingMode(value, addressingMode).toInt())
          AddressingMode.ModeQword -> vm.vmMemory.putLong(address, applyAddressingMode(value, addressingMode))
        }
      }
      VariableType.StringType -> {
        //we need to add 4 here, because first 4 bytes of the string are it's size
        val address = operand.address + getOffsetOperandValue(vm, eip, offsetOperand) + 4

        val bytes = when (addressingMode) {
          AddressingMode.ModeByte -> {
            val array = ByteArray(1)
            array[0] = (value and 0xFF).toByte()
            array
          }
          AddressingMode.ModeWord -> {
            val array = ByteArray(2)
            Utils.writeShortToArray(0, value.toShort(), array)
            array
          }
          AddressingMode.ModeDword -> {
            val array = ByteArray(4)
            Utils.writeIntToArray(0, value.toInt(), array)
            array
          }
          AddressingMode.ModeQword -> {
            val array = ByteArray(8)
            Utils.writeLongToArray(0, value, array)
            array
          }
        }

        vm.vmMemory.putBytes(address, bytes)
      }
    }
  }

}