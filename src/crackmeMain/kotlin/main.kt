package crackme

import crackme.vm.VMCompiler
import crackme.vm.VMParser
import crackme.vm.core.os.WinFile

fun main() {
  val testProgram = """
        use println(String)
        use sizeof(Any)
        use alloc(Int)

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
        call sizeof("Hello from VM!")
        call alloc(r0)
        mov [r0], "Hello from VM!"
        call println([r0])
        ret r0
    """

  val vmParser = VMParser()
  val vm = vmParser.parse(testProgram)

  println("Native functions: ")
  vm.nativeFunctions.forEach { nativeFunction ->
    val funcName = nativeFunction.value.type.funcName
    val parameters = nativeFunction.value.variableTypeList.joinToString(",") { it.str }

    println("$funcName($parameters)")
  }

  println("Instructions: ")
  vm.instructions.forEachIndexed { index, instruction ->
    println("[$index]: $instruction")
  }

  WinFile.withFileDo("bytecode.txt", WinFile.OpenType.Write) { file ->
    val vmCompiler = VMCompiler(vm)
    vmCompiler.compile(file)
  }

//  val vmExecutor = VMExecutor(vm)
//  vmExecutor.run()
}
