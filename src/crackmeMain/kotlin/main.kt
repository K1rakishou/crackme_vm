package crackme

import crackme.vm.VMCompiler
import crackme.vm.VMParser
import crackme.vm.VMSimulator
import crackme.vm.core.os.WinFile
import crackme.vm.obfuscator.SimpleVMInstructionObfuscator

fun main() {
  val testProgram = """
        use println(String)

        mov r0, 1
        mov r1, 3
        add r0, r1
        cmp r0, 4
        je @BAD

        mov r2, 999
        jmp @GOOD
@BAD:
        mov r2, 888
        let b: String, "BAD"
        call println([b])
        mov r0, r2
        ret
@GOOD:
        let a: String, "GOOD"
        call println([a])
        mov r0, r2
        ret
    """

  //TODO: sub instruction
  //TODO: inc instruction
  //TODO: dec instruction

  val vmInstructionObfuscator = SimpleVMInstructionObfuscator()
  val vmParser = VMParser(vmInstructionObfuscator)
  val vm = vmParser.parse(testProgram)

  val vmSimulator = VMSimulator()
  val vmCompiler = VMCompiler()

  WinFile.withFileDo("bytecode.txt", WinFile.OpenType.Write) { file ->
    vmSimulator.simulate(vm)
    vmCompiler.compile(file, vm)
  }
}
