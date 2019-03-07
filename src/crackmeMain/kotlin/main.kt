package crackme

import crackme.vm.VMCompiler
import crackme.vm.VMParser
import crackme.vm.VMSimulator
import crackme.vm.core.os.WinFile

fun main() {
//  val testProgram = """
//        use println(String)
//        use sizeof(Any)
//        use alloc(Int)
//
//        mov r0, 1
//        mov r1, 3
//        add r0, r1
//        cmp r0, 4
//        je BAD
//
//        mov r0, 999
//        jmp EXIT
//@BAD:
//        mov r0, -888
//@EXIT:
//        call sizeof("Hello from VM!")
//        call alloc(r0)
//        mov [r0], "Hello from VM!"
//        call println([r0])
//        ret r0
//    """

  val testProgram =
    """
      mov r0, 0
      mov r1, 11
      mov r2, 22
      mov r3, 33
      mov r4, 44
      mov r5, 55

      add r0, r1
      add r0, r2
      add r0, r3
      add r0, r4
      add r0, r5

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

  val vmSimulator = VMSimulator()
  val vmCompiler = VMCompiler()


  WinFile.withFileDo("bytecode.txt", WinFile.OpenType.Write) { file ->
    vmSimulator.simulate(vm)
    vmCompiler.compile(file, vm)
  }
}
