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

        mov r0, 999
        jmp @EXIT
@BAD:
        mov r0, 888
        let b: String, "Test string"
        call println([b])
        ret
@EXIT:
        let a: String, "Hello from VM!"
        call println([a])
        ret
    """

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
