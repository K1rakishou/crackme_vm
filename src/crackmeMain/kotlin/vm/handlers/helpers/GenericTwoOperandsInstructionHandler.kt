package crackme.vm.handlers.helpers

import crackme.vm.VM
import crackme.vm.core.VmExecutionException
import crackme.vm.instructions.GenericTwoOperandsInstruction
import crackme.vm.instructions.Instruction
import crackme.vm.operands.*

object GenericTwoOperandsInstructionHandler {

  fun <T : Instruction> handle(
    vm: VM,
    eip: Int,
    instruction: T,
    //instr r0, 1122334455667788/11223344
    handle_Reg_Constant: (dest: Register, src: Constant, eip: Int) -> Long,
    //instr r0, ss/ds@[11223344]
    handle_Reg_MemC32: (dest: Register, src: Memory<C32>, eip: Int) -> Long,
    //instr r0, ss/ds@[r0]
    handle_Reg_MemReg: (dest: Register, src: Memory<Register>, eip: Int) -> Long,
    //instr r0, r1
    handle_Reg_Reg: (dest: Register, src: Register, eip: Int) -> Long,
    //instr ss/ds@[r0], r0
    handle_MemReg_Reg: (dest: Memory<Register>, src: Register, eip: Int) -> Long,
    //instr ss/ds@[11223344], r0
    handle_MemC32_Reg: (dest: Memory<C32>, src: Register, eip: Int) -> Long,
    //instr ss/ds@[r0], 1234
    handle_MemReg_Const: (dest: Memory<Register>, src: Constant, eip: Int) -> Long,
    //instr [11223344], 123
    handle_MemC32_Const: (dest: Memory<C32>, src: Constant, eip: Int) -> Long
  ): Long {
    if (instruction !is GenericTwoOperandsInstruction) {
      throw RuntimeException("instruction ${instruction} is not a GenericTwoOperandsInstruction")
    }

    return when (instruction.dest) {
      is Register -> {
        when (val srcOperand = instruction.src) {
          is Constant -> {
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
              else -> throw VmExecutionException(eip, "Unknown operand (${srcInstruction.operand})")
            }
          }
          //instr r0, r1
          is Register -> handle_Reg_Reg(instruction.dest as Register, instruction.src as Register, eip)
          else -> throw VmExecutionException(eip, "Unknown operand ($srcOperand)")
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
                  else -> throw VmExecutionException(eip, "Unknown operand (${destInstruction.operand})")
                }
              }
              is Memory<*> -> {
                //instr [[???]], r0
                throw VmExecutionException(eip, "Operand (${(instruction.dest as Memory<*>).operand.operandName}) cannot be used as Memory operand")
              }
              else -> throw VmExecutionException(eip, "Unknown operand (${destInstruction.operand})")
            }
          }
          //instr [123], 1234
          //instr [r0], 1234
          is Constant -> {
            val sourceConst = instruction.src as Constant
            val destOperand = instruction.dest as Memory<*>

            when (destOperand.operand) {
              is Register -> {
                //instr [r0], 1234
                when (destOperand.segment) {
                  Segment.Memory -> handle_MemReg_Const(destOperand as Memory<Register>, sourceConst, eip)
                  Segment.Stack -> handle_MemReg_Const(destOperand as Memory<Register>, sourceConst, eip)
                  else -> throw VmExecutionException(eip, "Unknown segment (${destOperand.segment.segmentName})")
                }
              }
              is Constant -> {
                when (destOperand.operand) {
                  //instr [11223344], 1234
                  is C32 -> handle_MemC32_Const(instruction.dest as Memory<C32>, sourceConst, eip)
                  is C64 -> {
                    val convertedConstant = C32(destOperand.operand.value.toInt())

                    //TODO: this convertation may be wrong (especially the addressingMode)
                    val convertedOperand = Memory(
                      convertedConstant,
                      destOperand.offsetOperand,
                      destOperand.segment,
                      destOperand.addressingMode
                    )

                    handle_MemC32_Const(convertedOperand, sourceConst, eip)
                  }
                  else -> throw VmExecutionException(eip, "Unknown constant type (${destOperand.operand})")
                }
              }
              is Memory<*> -> {
                //instr [[???]], 1234
                throw VmExecutionException(eip, "Operand (${(instruction.dest as Memory<*>).operand.operandName}) cannot be used as Memory operand")
              }
              else -> throw VmExecutionException(eip, "Unknown operand (${destOperand.operand})")
            }
          }
          //instr [123], abc
          is Variable,
            //instr [123], [1234]
          is Memory<*> -> {
            throw VmExecutionException(eip, "Operand of type (${instruction.src.operandName}) cannot be used as a source with instruction ($instruction)")
          }
          else -> throw VmExecutionException(eip, "Unknown operand (${instruction.src})")
        }
      }
      is Constant,
        //instr 123, *
      is Variable -> {
        //instr abc, *
        throw VmExecutionException(eip, "Operand of type (${instruction.dest.operandName}) cannot be used asa destination with instruction ($instruction)")
      }
      else -> throw VmExecutionException(eip, "Unknown operand (${instruction.dest})")
    }
  }

}