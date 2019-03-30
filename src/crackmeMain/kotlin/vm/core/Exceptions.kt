package crackme.vm.core

import crackme.vm.operands.Operand

class ScopeParsingException(
  functionLine: Int,
  message: String
) : Exception("[Error while trying to parse vm scope at line ${functionLine + 1}]: $message")

class GlobalVariableParsingException(
  functionLine: Int,
  message: String
) : Exception("[Error while trying to parse global variable at line ${functionLine + 1}]: $message")

class ParsingException(
  functionName: String,
  functionLine: Int,
  message: String
) : Exception("[Error at function ($functionName) at line ${functionLine + 1}]: $message")

class VmExecutionException(
  eip: Int,
  message: String
) : Exception("Error at ${eip + 1}th instruction, $message")

class ObfuscationException(
  inputOperand: Operand,
  message: String
) : Exception("Error while trying to obfuscate operand ($inputOperand), $message")