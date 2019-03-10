package crackme.vm.core

import crackme.vm.operands.Operand

class ParsingException(
  programLine: Int,
  message: String
) : Exception("[Error at line ${programLine + 1}]: $message")

class VmExecutionException(
  eip: Int,
  message: String
) : Exception("Error at ${eip + 1}th instruction, $message")

class ObfuscationException(
  inputOperand: Operand,
  message: String
) : Exception("Error while trying to obfuscate operand ($inputOperand), $message")