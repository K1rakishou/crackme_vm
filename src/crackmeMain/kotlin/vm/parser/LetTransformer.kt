package crackme.vm.parser

import crackme.vm.core.ParsingException
import crackme.vm.core.VmFunctionScope
import crackme.vm.core.VmMemory
import crackme.vm.instructions.Instruction
import crackme.vm.instructions.Let
import crackme.vm.instructions.Mov
import crackme.vm.operands.*

class LetTransformer {

  fun transform(vmMemory: VmMemory, vmFunctionScope: VmFunctionScope, functionLine: Int, instruction: Let): Instruction {
    /**
     * ====== C32 ==========
     * let a: Int, 11223344
     *
     *    |
     *    V
     *
     * add sp, 4
     * mov ss@[0] as dword, 11223344
     *
     * ====== C64 ==========
     * let a: Long, 77223344556677889900
     *
     *    |
     *    V
     *
     * add sp, 8
     * mov ss@[0] as qword, 77223344556677889900
     *
     * ====== VmString ==========
     * add sp, 4
     * mov ss@[0] as dword, address_of_the_string
     * */

    val variableStackFrame = vmFunctionScope.getLocalVariableStackFrameByName(instruction.variable.name)
    if (variableStackFrame == null) {
      throw ParsingException(
        vmFunctionScope.name,
        functionLine,
        "Local variable (${instruction.variable.name}) is not defined"
      )
    }

    return when (val initializer = instruction.initializer) {
      is Constant -> {
        when (initializer) {
          is C32 -> {
            Mov(
              Memory(C32(variableStackFrame), null, Segment.Stack, instruction.variable.variableType.addressingMode),
              initializer
            )
          }
          is C64 -> {
            Mov(
              Memory(C64(variableStackFrame.toLong()), null, Segment.Stack, instruction.variable.variableType.addressingMode),
              initializer
            )
          }
          else -> throw ParsingException(
            vmFunctionScope.name,
            functionLine,
            "Constant of type ($initializer) is not supported as initializer yet"
          )
        }
      }
      else -> throw ParsingException(
        vmFunctionScope.name,
        functionLine,
        "Initializer of type (${initializer.operandType}) is not supported yet"
      )
    }
  }

}