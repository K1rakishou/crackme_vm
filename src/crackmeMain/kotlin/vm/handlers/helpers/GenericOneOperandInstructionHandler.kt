package crackme.vm.handlers.helpers

import crackme.vm.VM
import crackme.vm.core.VariableType
import crackme.vm.core.VmExecutionException
import crackme.vm.instructions.GenericOneOperandInstruction
import crackme.vm.instructions.Instruction
import crackme.vm.operands.*

object GenericOneOperandInstructionHandler {

  //TODO: make this function return Long as well as all of the lambdas. This is necessary for vmFlags updating
  fun <T : Instruction> handle(
    vm: VM,
    eip: Int,
    instruction: T,
    //instr r0
    handleReg: (operand: Register, eip: Int) -> Unit,
    //instr [r0]
    handleMemReg: (operand: Memory<Register>, eip: Int) -> Unit,
    //instr [abc]
    handleMemVar: (operand: Memory<Variable>, eip: Int) -> Unit,
    //instr [1122334455667788]
    handleMemC64: (operand: Memory<C64>, eip: Int) -> Unit,
    //instr [11223344]
    handleMemC32: (operand: Memory<C32>, eip: Int) -> Unit,
    //instr 11223344
    handleC64: (operand: C64, eip: Int) -> Unit,
    //instr 1122334455667788
    handleC32: (operand: C32, eip: Int) -> Unit
  ) {
    if (instruction !is GenericOneOperandInstruction) {
      throw RuntimeException("Not implemented for ${instruction.instructionType.instructionName}")
    }

    when (instruction.operand) {
      is Register -> handleReg(instruction.operand as Register, eip)
      is Memory<*> -> {
        when (val memoryOperand = (instruction.operand as Memory<*>).operand) {
          is Register -> handleMemReg(memoryOperand as Memory<Register>, eip)
          is Variable -> {
            when (memoryOperand.variableType) {
              VariableType.IntType,
              VariableType.LongType -> handleMemVar(memoryOperand as Memory<Variable>, eip)
              VariableType.AnyType ,
              VariableType.StringType -> {
                throw VmExecutionException(eip, "Variable with operandType (${(memoryOperand as Memory<*>).operand.operandType}) cannot be used with Memory operand")
              }
            }
          }
          is Constant -> {
            when (memoryOperand) {
              is C64 -> handleMemC64(memoryOperand as Memory<C64>, eip)
              is C32 -> handleMemC32(memoryOperand as Memory<C32>, eip)
              else -> {
                throw VmExecutionException(eip, "Operand (${instruction.operand.operandName}) cannot be used with instruction ($instruction)")
              }
            }
          }
          else -> {
            throw VmExecutionException(eip, "Operand (${instruction.operand.operandName}) cannot be used with instruction ($instruction)")
          }
        }
      }
      is Constant -> {
        when (val constant = instruction.operand) {
          is C64 -> handleC64(constant, eip)
          is C32 -> handleC32(constant, eip)
          else -> {
            throw VmExecutionException(eip, "Operand (${instruction.operand.operandName}) cannot be used with instruction ($instruction)")
          }
        }
      }
      else -> {
        throw VmExecutionException(eip, "Operand (${instruction.operand.operandName}) cannot be used with instruction ($instruction)")
      }
    }
  }

}