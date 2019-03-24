package crackme.vm.core

import crackme.vm.VM

class VmFunctionScope(
  val name: String,
  val start: Int,
  val length: Int,
  val functionParameters: List<FunctionParameter>,
  private val localVariables: MutableList<FunctionLocalVariable>
) {

  fun isMainFunctionScope(): Boolean {
    return name == VM.mainFunctionName
  }

  fun isVariableDefined(name: String): Boolean {
    val isFunctionParameter = functionParameters.any { it.name == name }
    if (isFunctionParameter) {
      return true
    }

    return localVariables.any { it.name == name }
  }

  fun getParameterStackFrameByName(parameterName: String): Int? {
    return functionParameters.firstOrNull { it.name == parameterName }?.stackFrame
  }

  fun getLocalVariableStackFrameByName(localVariableName: String): Int? {
    return localVariables.firstOrNull { it.name == localVariableName }?.stackFrame
  }

  fun getParameterByName(parameterName: String): FunctionParameter? {
    return functionParameters.firstOrNull { it.name == parameterName }
  }

  fun getLocalVariableByName(variableName: String): FunctionLocalVariable? {
    return localVariables.firstOrNull { it.name == variableName }
  }

  fun getLocalVariablesTotalStackSize(): Int {
    return localVariables.sumBy { it.type.size }
  }

  fun getFunctionParametersTotalStackSize(): Int {
    return functionParameters.sumBy { it.type.size }
  }

  fun getTotalStackSizeAllocated(): Int {
    return localVariables.sumBy { it.type.size } + functionParameters.sumBy { it.type.size }
  }

  override fun toString(): String {
    return "[start = $start, end = ${start + length}] def($functionParameters) [$localVariables])"
  }
}

interface FunctionVariable {
  val name: String
  val stackFrame: Int
  val type: VariableType
}

class FunctionParameter(
  override val name: String,
  override val stackFrame: Int,
  override val type: VariableType
) : FunctionVariable {

  override fun toString(): String {
    return "parameter $name: ${type.str}, ss@[$stackFrame]"
  }
}

class FunctionLocalVariable(
  override val name: String,
  override val stackFrame: Int,
  override val type: VariableType
) : FunctionVariable {

  override fun toString(): String {
    return "variable $name: ${type.str}, ss@[$stackFrame]"
  }
}