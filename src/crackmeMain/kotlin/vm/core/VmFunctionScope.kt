package crackme.vm.core

class VmFunctionScope(
  val name: String,
  val start: Int,
  val length: Int,
  val parameters: List<FunctionParameter>
) {

  fun getParameterStackFrameByName(parameterName: String): Int? {
    return parameters.firstOrNull { it.name == parameterName }?.stackFrame
  }

  fun getParameterByName(parameterName: String): FunctionParameter? {
    return parameters.firstOrNull { it.name == parameterName }
  }

  override fun toString(): String {
    return "[start = $start, end = ${start + length}] def($parameters))"
  }
}

class FunctionParameter(
  val name: String,
  val stackFrame: Int,
  val type: VariableType
) {

  override fun toString(): String {
    return "$name: ${type.str}, ss@[$stackFrame]"
  }
}