package crackme.vm.handlers.helpers

import crackme.vm.VM
import crackme.vm.core.VariableType
import crackme.vm.core.VmExecutionException
import crackme.vm.instructions.GenericTwoOperandsInstruction
import crackme.vm.instructions.Instruction
import crackme.vm.operands.*

object GenericTwoOperandsInstructionHandler {

  //TODO: make this function return Long as well as all of the lambdas. This is necessary for vmFlags updating
  fun <T : Instruction> handle(
    vm: VM,
    eip: Int,
    instruction: T,
    //instr r0, 1122334455667788
    handle_Reg_C64: (dest: Register, src: C64, eip: Int) -> Unit,
    //instr r0, 11223344
    handle_Reg_C32: (dest: Register, src: C32, eip: Int) -> Unit,
    //instr r0, [1122334455667788]
    handle_Reg_MemC64: (dest: Register, src: Memory<C64>, eip: Int) -> Unit,
    //instr r0, [11223344]
    handle_Reg_MemC32: (dest: Register, src: Memory<C32>, eip: Int) -> Unit,
    //instr r0, [r0]
    handle_Reg_MemReg: (dest: Register, src: Memory<Register>, eip: Int) -> Unit,
    //instr r0, [abc]
    handle_Reg_MemVar: (dest: Register, src: Memory<Variable>, eip: Int) -> Unit,
    //instr r0, r1
    handle_Reg_Reg: (dest: Register, src: Register, eip: Int) -> Unit,
    //instr r0, abc
    handle_Reg_Var: (dest: Register, src: Variable, eip: Int) -> Unit,
    //mov [r0], r0
    handle_MemReg_Reg: (dest: Memory<Register>, src: Register, eip: Int) -> Unit,
    //mov [abc], r0
    handle_MemVar_Reg: (dest: Memory<Variable>, src: Register, eip: Int) -> Unit,
    //mov [1122334455667788], r0
    handle_MemC64_Reg: (dest: Memory<C64>, src: Register, eip: Int) -> Unit,
    //mov [11223344], r0
    handle_MemC32_Reg: (dest: Memory<C32>, src: Register, eip: Int) -> Unit
  ) {
    if (instruction !is GenericTwoOperandsInstruction) {
      throw RuntimeException("Not implemented for ${instruction.instructionType.instructionName}")
    }

    when (instruction.dest) {
      is Register -> {
        when (val srcOperand = instruction.src) {
          is Constant -> {
            if (srcOperand is VmString) {
              //instr r0, "test"
              throw VmExecutionException(eip, "Operand (${srcOperand.operandName}) cannot be used as source with instruction ($instruction)")
            }

            when (srcOperand) {
              //instr r0, 1122334455667788
              is C64 -> handle_Reg_C64(instruction.dest as Register, srcOperand, eip)
              //instr r0, 11223344
              is C32 -> handle_Reg_C32(instruction.dest as Register, srcOperand, eip)
              else -> throw VmExecutionException(eip, "getConstantValueFromVmMemory not implemented for constant operandType (${srcOperand.operandName})")
            }
          }
          is Memory<*> -> {
            when ((instruction.src as Memory<*>).operand) {
              is Constant -> {
                when ((instruction.src as Memory<*>).operand) {
                  //instr r0, [1122334455667788]
                  is C64 -> handle_Reg_MemC64(instruction.dest as Register, instruction.src as Memory<C64>, eip)
                  //instr r0, [11223344]
                  is C32 -> handle_Reg_MemC32(instruction.dest as Register, instruction.src as Memory<C32>, eip)
                  //instr r0, ["test"] etc
                  else -> throw VmExecutionException(eip, "Operand (${instruction.src.operandName}) cannot be used as source with instruction ($instruction)")
                }
              }
              is Memory<*> -> {
                //instr r0, [[???]]
                throw VmExecutionException(eip, "Operand (${(instruction.src as Memory<*>).operand.operandName}) cannot be used as Memory operand")
              }
              is Register -> {
                //instr r0, [r0]
                handle_Reg_MemReg(instruction.dest as Register, instruction.src as Memory<Register>, eip)
              }
              is Variable -> {
                //instr r0, [abc]
                val variableType = ((instruction.src as Memory<*>).operand as Variable).variableType
                if (variableType == VariableType.AnyType) {
                  throw VmExecutionException(eip, "Variable with operandType (${(instruction.src as Memory<*>).operand.operandType}) cannot be used with Memory operand")
                }

                handle_Reg_MemVar(instruction.dest as Register, instruction.src as Memory<Variable>, eip)
              }
            }
          }
          //instr r0, r1
          is Register -> handle_Reg_Reg(instruction.dest as Register, instruction.src as Register, eip)
          //instr r0, abc
          is Variable -> handle_Reg_Var(instruction.dest as Register, instruction.src as Variable, eip)
        }
      }
      is Memory<*> -> {
        when (instruction.src) {
          //instr [r0], r0
          //instr [abc], r0
          //instr [123], r0
          is Register -> {
            when ((instruction.dest as Memory<*>).operand) {
              is Register -> {
                //mov [r0], r0
                //FIXME: probably a bug here
                handle_MemReg_Reg(instruction.dest as Memory<Register>, instruction.src as Register, eip)
              }
              is Variable -> {
                //instr [abc], r0
                when (val variableType = ((instruction.dest as Memory<*>).operand as Variable).variableType) {
                  VariableType.IntType,
                  VariableType.LongType,
                  VariableType.StringType -> {
                    handle_MemVar_Reg(instruction.dest as Memory<Variable>, instruction.src as Register, eip)
                  }
                  VariableType.AnyType -> {
                    throw VmExecutionException(eip, "Variable with operandType (${(instruction.dest as Memory<*>).operand.operandType}) cannot be used with Memory operand")
                  }
                }
              }
              is Constant -> {
                when ((instruction.dest as Memory<*>).operand) {
                  //instr [1122334455667788], r0
                  is C64 -> handle_MemC64_Reg(instruction.dest as Memory<C64>, instruction.src as Register, eip)
                  is C32 -> handle_MemC32_Reg(instruction.dest as Memory<C32>, instruction.src as Register, eip)
                  is VmString -> {
                    throw VmExecutionException(eip, "VmString cannot be used as a memory address")
                  }
                }
              }
              is Memory<*> -> {
                //instr [[???]], r0
                throw VmExecutionException(eip, "Operand (${(instruction.dest as Memory<*>).operand.operandName}) cannot be used as Memory operand")
              }
            }
          }
          //instr [123], 1234
          is Constant,
          //instr [123], abc
          is Variable,
            //instr [123], [1234]
          is Memory<*> -> {
            throw VmExecutionException(eip, "Operand (${instruction.dest.operandName}) cannot be used as destination with instruction ($instruction)")
          }
        }
      }
      is Constant,
        //instr 123, *
      is Variable -> {
        //instr abc, *
        throw VmExecutionException(eip, "Operand (${instruction.dest.operandName}) cannot be used as destination with instruction ($instruction)")
      }
    }
  }

}