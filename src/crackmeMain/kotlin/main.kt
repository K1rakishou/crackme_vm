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
  val testProgram =

    """
      def main()
        mov r0, 1
        ret
      end
    """

  //TODO: stack operand
  //TODO: update flags in two operand instructions
  //TODO: rework println and other native functions
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
//    vmCompiler.compile(file, vm)
  }
}
