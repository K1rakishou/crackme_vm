package crackme

import crackme.misc.extractInstructionsAndGetEntryPoint
import crackme.vm.VMCompiler
import crackme.vm.VMSimulator
import crackme.vm.core.os.WinFile
import crackme.vm.parser.VMParser
import platform.windows.GetTickCount64
import kotlin.random.Random

fun main() {
  val testProgram =

    """
      def_var test: String = "1234567890"

      def main()
        mov r0, 1
        ret
      end
    """

  //TODO: add Lea instruction to be able to get an address of a variable

  //TODO: add modules and ability to include modules (#include "module-path/module-name.asm")
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

  val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)

  WinFile.withFileDo("bytecode.txt", WinFile.OpenType.Write) { file ->
    vmSimulator.simulate(vm, entryPoint, instructions)
//    vmCompiler.compile(file, vm)
  }
}
