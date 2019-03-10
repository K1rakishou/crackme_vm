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
        push [b] as dword
        call println()
        mov r0, r2
        ret
@GOOD:
        let a: String, "GOOD"
        push [a] as dword
        call println()
        mov r0, r2
        ret
    """

  //TODO: maybe should have no default addressing? It's kinda error prone.
  //TODO: make Let instruction allocate variable on stack instead of the global memory
  //TODO: Introduce 32, 16 and maybe even 8 bit registers

//  val time: Time = WinTime()
  val random = Random(GetTickCount64().toLong() /** time.getCurrentTime()*/)

//  val mutationEngine = SimpleMutationEngine(random)
//  val vmInstructionGenerator = SimpleVMInstructionGenerator(random, mutationEngine)
//  val constantObfuscationEngine = ConstantObfuscationEngine(vmInstructionGenerator)
//  val vmInstructionObfuscator = SimpleVMInstructionObfuscator(constantObfuscationEngine)

  val vmParser = VMParser(random)
  val vm = vmParser.parse(testProgram)

  val vmSimulator = VMSimulator()
  val vmCompiler = VMCompiler()

  WinFile.withFileDo("bytecode.txt", WinFile.OpenType.Write) { file ->
    vmSimulator.simulate(vm)
    vmCompiler.compile(file, vm)
  }
}
