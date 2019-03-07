package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.VMSimulator
import crackme.vm.core.VariableType
import crackme.vm.instructions.GenericTwoOperandsInstruction
import crackme.vm.instructions.Instruction
import crackme.vm.operands.*

object GenericTwoOperandsInstructionHandler {

  fun handle(
    vm: VM,
    eip: Int,
    instruction: Instruction,
    //instr r0, 123
    handle_Reg_C64: () -> Unit,
    //instr r0, [123]
    handle_Reg_MemC64: () -> Unit,
    //instr r0, [r0]
    handle_Reg_MemReg: () -> Unit,
    //instr r0, [abc]
    handle_Reg_MemVar: () -> Unit,
    //instr r0, r1
    handle_Reg_Reg: () -> Unit,
    //instr r0, abc
    handle_Reg_Var: () -> Unit,
    //mov [r0], r0
    handle_MemReg_Reg: () -> Unit,
    //mov [abc], r0
    handle_MemVar_Reg: () -> Unit,
    //mov [123], r0
    handle_MemC64_Reg: () -> Unit
  ) {
    if (instruction !is GenericTwoOperandsInstruction) {
      throw RuntimeException("Not implemented for ${instruction.instructionType.instructionName}")
    }

    when (instruction.dest) {
      is Register -> {
        when (instruction.src) {
          is Constant -> {
            if (instruction.src is VmString) {
              //instr r0, "test"
              throw VMSimulator.VmExecutionException(eip, "Operand (${instruction.src.operandName}) cannot be used as source with instruction ($instruction)")
            }

            when (instruction.src) {
              //instr r0, 123
              is C64 -> handle_Reg_C64()
              else -> throw NotImplementedError("getConstantValue not implemented for constant operandType (${instruction.src.operandName})")
            }
          }
          is Memory -> {
            when ((instruction.src as Memory).operand) {
              is Constant -> {
                if ((instruction.src as Memory).operand is VmString) {
                  //instr r0, ["test"]
                  throw VMSimulator.VmExecutionException(eip, "Operand (${instruction.src.operandName}) cannot be used as source with instruction ($instruction)")
                }

                //instr r0, [123]
                handle_Reg_MemC64()
              }
              is Memory -> {
                //instr r0, [[???]]
                throw VMSimulator.VmExecutionException(eip, "Operand (${(instruction.src as Memory).operand.operandName}) cannot be used as Memory operand")
              }
              is Register -> {
                //instr r0, [r0]
                handle_Reg_MemReg()
              }
              is Variable -> {
                //instr r0, [abc]
                if (((instruction.src as Memory).operand as Variable).variableType == VariableType.AnyType
                  || ((instruction.src as Memory).operand as Variable).variableType == VariableType.StringType) {
                  throw VMSimulator.VmExecutionException(eip, "Variable with operandType (${(instruction.src as Memory).operand.operandType}) cannot be used with Memory operand")
                }

                handle_Reg_MemVar()
              }
            }
          }
          //instr r0, r1
          is Register -> handle_Reg_Reg()
          //instr r0, abc
          is Variable -> handle_Reg_Var()
        }
      }
      is Memory -> {
        when (instruction.src) {
          //mov [r0], r0
          //mov [abc], r0
          //mov [123], r0
          is Register -> {
            when ((instruction.dest as Memory).operand) {
              is Register -> {
                //mov [r0], r0
                handle_MemReg_Reg()
              }
              is Variable -> {
                //mov [abc], r0
                when (((instruction.dest as Memory).operand as Variable).variableType) {
                  VariableType.LongType -> handle_MemVar_Reg()
                  VariableType.AnyType,
                  VariableType.StringType -> {
                    throw VMSimulator.VmExecutionException(eip, "Variable with operandType (${(instruction.dest as Memory).operand.operandType}) cannot be used with Memory operand")
                  }
                }
              }
              is Constant -> {
                //mov [123], r0
                when ((instruction.dest as Memory).operand) {
                  is C64 -> handle_MemC64_Reg()
                  is VmString -> {
                    throw VMSimulator.VmExecutionException(eip, "VmString cannot be used as memory address")
                  }
                }
              }
              is Memory -> {
                //mov [[???]], r0
                throw VMSimulator.VmExecutionException(eip, "Operand (${(instruction.dest as Memory).operand.operandName}) cannot be used as Memory operand")
              }
            }
          }
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
        //instr 123, *
      is Variable -> {
        //instr abc, *
        throw VMSimulator.VmExecutionException(eip, "Operand (${instruction.dest.operandName}) cannot be used as destination with instruction ($instruction)")
      }
    }
  }

}