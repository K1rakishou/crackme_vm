package crackme.vm.handlers.helpers

import crackme.vm.VM
import crackme.vm.core.VmExecutionException
import crackme.vm.instructions.GenericOneOperandInstruction
import crackme.vm.instructions.Instruction
import crackme.vm.operands.*

object GenericOneOperandInstructionHandler {

  fun <T : Instruction> handle(
    vm: VM,
    eip: Int,
    instruction: T,
    //instr r0
    handleReg: (operand: Register, eip: Int) -> Long,
    //instr [r0]
    handleMemReg: (operand: Memory<Register>, eip: Int) -> Long,
    //instr [1122334455667788]/[11223344]
    handleMemConstant: (operand: Memory<Constant>, eip: Int) -> Long,
    //instr 11223344
    handleC64: (operand: C64, eip: Int) -> Long,
    //instr 1122334455667788
    handleC32: (operand: C32, eip: Int) -> Long
  ): Long {
    if (instruction !is GenericOneOperandInstruction) {
      throw RuntimeException("instruction ${instruction} is not a GenericOneOperandInstruction")
    }

    return when (instruction.operand) {
      is Register -> handleReg(instruction.operand as Register, eip)
      is Memory<*> -> {
        val memoryOperand = instruction.operand as Memory<*>

        when (val innerOperand = memoryOperand.operand) {
          is Register -> handleMemReg(instruction.operand as Memory<Register>, eip)
          is Constant -> {
            when (innerOperand) {
              is C64 -> handleMemConstant(memoryOperand as Memory<Constant>, eip)
              is C32 -> handleMemConstant(memoryOperand as Memory<Constant>, eip)
              else -> {
                throw VmExecutionException(eip, "Operand of type (${innerOperand.operandName}) cannot be used with instruction ($instruction)")
              }
            }
          }
          else -> {
            throw VmExecutionException(eip, "Operand of type (${innerOperand.operandName}) cannot be used with instruction ($instruction)")
          }
        }
      }
      is Constant -> {
        when (val constant = instruction.operand) {
          is C64 -> handleC64(constant, eip)
          is C32 -> handleC32(constant, eip)
          else -> {
            throw VmExecutionException(eip, "Operand of type (${instruction.operand.operandName}) cannot be used with instruction ($instruction)")
          }
        }
      }
      else -> {
        throw VmExecutionException(eip, "Operand of type (${instruction.operand.operandName}) cannot be used with instruction ($instruction)")
      }
    }
  }

}