package crackme

import crackme.vm.VMCompiler
import crackme.vm.VMParser
import crackme.vm.VMSimulator
import crackme.vm.core.os.Time
import crackme.vm.core.os.WinFile
import crackme.vm.core.os.WinTime
import crackme.vm.obfuscator.SimpleVMInstructionObfuscator
import crackme.vm.obfuscator.engine.ConstantObfuscationEngine
import crackme.vm.obfuscator.generator.SimpleVMInstructionGenerator
import crackme.vm.obfuscator.mutation.SimpleMutationEngine
import platform.windows.GetTickCount64
import kotlin.random.Random

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

  val time: Time = WinTime()
  val random = Random(GetTickCount64().toLong() * time.getCurrentTime())

  val mutationEngine = SimpleMutationEngine(random)
  val vmInstructionGenerator = SimpleVMInstructionGenerator(random, mutationEngine)
  val constantObfuscationEngine = ConstantObfuscationEngine(vmInstructionGenerator)
  val vmInstructionObfuscator = SimpleVMInstructionObfuscator(constantObfuscationEngine)

  val vmParser = VMParser(random, vmInstructionObfuscator)
  val vm = vmParser.parse(testProgram)

  val vmSimulator = VMSimulator()
  val vmCompiler = VMCompiler()

  WinFile.withFileDo("bytecode.txt", WinFile.OpenType.Write) { file ->
    vmSimulator.simulate(vm)
    vmCompiler.compile(file, vm)
  }
}
