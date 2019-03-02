package crackme

import crackme.vm.VMParser

fun main() {
  val testProgram = """
        use println(String)

        mov r0, 1
        mov r1, 3
        add r0, r1
        cmp r0, 4
        je BAD

        mov r0, 999
        jmp EXIT
@BAD:
        mov r0, -888
@EXIT:
        ret r0
    """

  val vmParser = VMParser()
  val vm = vmParser.parse(testProgram)

  println("Native functions: ")
  vm.nativeFunctions.forEach { nativeFunction ->
    val funcName = nativeFunction.value.type.funcName
    val parameters = nativeFunction.value.parameters.joinToString(",") { it.str }

    println("native function: $funcName($parameters)")
  }

  println("Instructions: ")
  vm.instructions.forEachIndexed { index, instruction ->
    println("[$index]: $instruction")
  }
}
