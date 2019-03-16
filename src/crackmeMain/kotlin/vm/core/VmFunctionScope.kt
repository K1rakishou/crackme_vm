package crackme.vm.core

class VmFunctionScope(
  val name: String,
  val start: Int,
  val length: Int,
  val parameters: List<FunctionParameter>
) {

  override fun toString(): String {
    return "[start = $start, end = ${start + length}] def($parameters))"
  }
}

class FunctionParameter(
  val name: String,
  val type: VariableType
) {

  override fun toString(): String {
    return "$name: ${type.str}"
  }
}