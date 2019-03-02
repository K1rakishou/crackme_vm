package crackme.vm.core

object NativeFunctionCallbacks {

  private val vmPrintlnCallback = NativeFunctionType.Println to fun (parameters: List<Any>): Long {
    if (parameters.size != 1) {
      throw BadParametersCount(NativeFunctionType.Println, 1, parameters.size)
    }

    val message = parameters[0] as String
    println(message)

    return 0
  }

  private val parametersMap = mapOf(vmPrintlnCallback)

  fun getCallbackByFunctionType(type: NativeFunctionType): (parameters: List<Any>) -> Long {
    if (parametersMap[type] == null) {
      throw UnknownFunctionType(type)
    }

    return parametersMap.getValue(type)
  }

  class UnknownFunctionType(type: NativeFunctionType) : Exception("Unknown Function type ${type.funcName}")
  class BadParametersCount(val funcType: NativeFunctionType,
                           val expected: Int,
                           val actual: Int) : Exception("Bad parameters count for function ${funcType.funcName} expected ${expected} but actual count is ${actual}")
}