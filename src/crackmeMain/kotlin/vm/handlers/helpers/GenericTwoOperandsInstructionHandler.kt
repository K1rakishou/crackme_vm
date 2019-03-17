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
    //instr r0, 1122334455667788/11223344
    handle_Reg_Constant: (dest: Register, src: Constant, eip: Int) -> Unit,
    //instr r0, ss/ds@[11223344]
    handle_Reg_MemC32: (dest: Register, src: Memory<C32>, eip: Int) -> Unit,
    //instr r0, ss/ds@[r0]
    handle_Reg_MemReg: (dest: Register, src: Memory<Register>, eip: Int) -> Unit,
    //instr r0, ds@[abc]
    handle_Reg_MemVar: (dest: Register, src: Memory<Variable>, eip: Int) -> Unit,
    //instr r0, r1
    handle_Reg_Reg: (dest: Register, src: Register, eip: Int) -> Unit,
    //instr r0, abc
    handle_Reg_Var: (dest: Register, src: Variable, eip: Int) -> Unit,
    //instr ss/ds@[r0], r0
    handle_MemReg_Reg: (dest: Memory<Register>, src: Register, eip: Int) -> Unit,
    //instr ds@[abc], r0
    handle_MemVar_Reg: (dest: Memory<Variable>, src: Register, eip: Int) -> Unit,
    //instr ss/ds@[11223344], r0
    handle_MemC32_Reg: (dest: Memory<C32>, src: Register, eip: Int) -> Unit,
    //instr ss/ds@[r0], 1234
    handle_MemReg_Const: (dest: Memory<Register>, src: Constant, eip: Int) -> Unit,
    //instr ds@[abc], r0
    handle_MemVar_Const: (dest: Memory<Variable>, src: Constant, eip: Int) -> Unit/*,
    //instr [11223344], 123
    handle_MemC32_Const: (dest: Memory<C32>, src: Constant, eip: Int) -> Unit*/
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
              throw VmExecutionException(eip, "Operand of type (${srcOperand.operandName}) cannot be used as source with instruction ($instruction)")
            }

            when (srcOperand) {
              //instr r0, 1122334455667788
              is C64 -> handle_Reg_Constant(instruction.dest as Register, srcOperand, eip)
              //instr r0, 11223344
              is C32 -> handle_Reg_Constant(instruction.dest as Register, srcOperand, eip)
              else -> throw VmExecutionException(eip, "getConstantValueFromVmMemory not implemented for constant operandType (${srcOperand.operandName})")
            }
          }
          is Memory<*> -> {
            val srcInstruction = instruction.src as Memory<*>

            when (srcInstruction.operand) {
              is Constant -> {
                when (srcInstruction.operand) {
                  //instr r0, [1122334455667788]
                  is C64 -> throw VmExecutionException(eip, "Using C64 as Memory/Stack address is not allowed")
                  //instr r0, [11223344]
                  is C32 -> {
                    when (srcInstruction.segment) {
                      Segment.Memory -> handle_Reg_MemC32(instruction.dest as Register, instruction.src as Memory<C32>, eip)
                      Segment.Stack -> handle_Reg_MemC32(instruction.dest as Register, instruction.src as Memory<C32>, eip)
                      else -> throw VmExecutionException(eip, "Unknown segment (${srcInstruction.segment.segmentName})")
                    }
                  }
                  //instr r0, ["test"] etc
                  else -> throw VmExecutionException(eip, "Operand of type (${instruction.src.operandName}) cannot be used as source with instruction ($instruction)")
                }
              }
              is Memory<*> -> {
                //instr r0, [[???]]
                throw VmExecutionException(eip, "Operand of type (${(instruction.src as Memory<*>).operand.operandName}) cannot be used as Memory operand")
              }
              is Register -> {
                //instr r0, [r0]
                when (srcInstruction.segment) {
                  Segment.Memory -> handle_Reg_MemReg(instruction.dest as Register, instruction.src as Memory<Register>, eip)
                  Segment.Stack -> handle_Reg_MemReg(instruction.dest as Register, instruction.src as Memory<Register>, eip)
                  else -> throw VmExecutionException(eip, "Unknown segment (${srcInstruction.segment.segmentName})")
                }
              }
              is Variable -> {
                //instr r0, [abc]
                when (srcInstruction.segment) {
                  Segment.Memory -> handle_Reg_MemVar(instruction.dest as Register, instruction.src as Memory<Variable>, eip)
                  Segment.Stack -> throw VmExecutionException(eip, "Cannot use stack segment with Memory variable")
                  else -> throw VmExecutionException(eip, "Unknown segment (${srcInstruction.segment.segmentName})")
                }
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
            val sourceReg = instruction.src as Register
            val destInstruction = instruction.dest as Memory<*>

            when (destInstruction.operand) {
              is Register -> {
                //instr [r0], r0
                when (destInstruction.segment) {
                  Segment.Memory -> handle_MemReg_Reg(destInstruction as Memory<Register>, sourceReg, eip)
                  Segment.Stack -> handle_MemReg_Reg(destInstruction as Memory<Register>, sourceReg, eip)
                  else -> throw VmExecutionException(eip, "Unknown segment (${destInstruction.segment.segmentName})")
                }
              }
              is Variable -> {
                //instr [abc], r0
                when (destInstruction.segment) {
                  Segment.Memory -> {
                    when (destInstruction.operand.variableType) {
                      VariableType.IntType,
                      VariableType.LongType,
                      VariableType.StringType -> {
                        handle_MemVar_Reg(destInstruction as Memory<Variable>, sourceReg, eip)
                      }
                    }
                  }
                  Segment.Stack -> throw VmExecutionException(eip, "Cannot use stack segment with Memory variable")
                  else -> throw VmExecutionException(eip, "Unknown segment (${destInstruction.segment.segmentName})")
                }
              }
              is Constant -> {
                when (destInstruction.operand) {
                  //instr [1122334455667788], r0
                  is C64 -> throw VmExecutionException(eip, "Using C64 as Memory/Stack address is not allowed")
                  is C32 -> {
                    when (destInstruction.segment) {
                      Segment.Memory -> handle_MemC32_Reg(destInstruction as Memory<C32>, sourceReg, eip)
                      Segment.Stack -> handle_MemC32_Reg(destInstruction as Memory<C32>, sourceReg, eip)
                      else -> throw VmExecutionException(eip, "Unknown segment (${destInstruction.segment.segmentName})")
                    }
                  }
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
          //instr [r0], 1234
          is Constant -> {
            val sourceConst = instruction.src as Constant
            val destInstruction = instruction.dest as Memory<*>

            when (destInstruction.operand) {
              is Register -> {
                //instr [r0], 1234
                when (destInstruction.segment) {
                  Segment.Memory -> handle_MemReg_Const(destInstruction as Memory<Register>, sourceConst, eip)
                  Segment.Stack -> handle_MemReg_Const(destInstruction as Memory<Register>, sourceConst, eip)
                  else -> throw VmExecutionException(eip, "Unknown segment (${destInstruction.segment.segmentName})")
                }
              }
              is Variable -> {
                //instr [abc], r0
                when (destInstruction.operand.variableType) {
                  VariableType.IntType,
                  VariableType.LongType,
                  VariableType.StringType -> {
                    handle_MemVar_Const(destInstruction as Memory<Variable>, sourceConst, eip)
                  }
                }
              }
              is Constant -> {
                when (destInstruction.operand) {
                  //FIXME: not supported for now
                  //instr [11223344], r0
//                  is C32 -> handle_MemC32_Const(instruction.dest as Memory<C32>, sourceConst, eip)
                  //instr [1122334455667788], r0
                  is C64,
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
          //instr [123], abc
          is Variable,
            //instr [123], [1234]
          is Memory<*> -> {
            throw VmExecutionException(eip, "Operand of type (${instruction.src.operandName}) cannot be used as a source with instruction ($instruction)")
          }
        }
      }
      is Constant,
        //instr 123, *
      is Variable -> {
        //instr abc, *
        throw VmExecutionException(eip, "Operand of type (${instruction.dest.operandName}) cannot be used asa destination with instruction ($instruction)")
      }
    }
  }

}