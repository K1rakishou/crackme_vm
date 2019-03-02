package crackme

import crackme.vm.VMParser

fun main() {
  val testProgram = """
        use vm_exit(Int)
        use println(String)

        mov r0, 1
        mov r1, 3
        add r0, r1
        cmp r0, 4
        je BAD
        call println("OK")
        jmp EXIT

BAD:
        call println("BAD")
EXIT:
        call vm_exit(0)
    """

  val vmParser = VMParser()
  val vm = vmParser.parse(testProgram)

  println("Instructions: ")

  vm.instructions.forEachIndexed { index, instruction ->
    println("[$index]: $instruction")
  }
}
