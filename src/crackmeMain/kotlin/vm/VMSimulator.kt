package crackme.vm

import crackme.vm.handlers.*
import crackme.vm.instructions.*

class VMSimulator(
  private val movHandler: MovHandler = MovHandler(),
  private val addHandler: AddHandler = AddHandler(),
  private val callHandler: CallHandler = CallHandler(),
  private val letHandler: LetHandler = LetHandler(),
  private val cmpHandler: CmpHandler = CmpHandler(),
  private val jxxHandler: JxxHandler = JxxHandler(),
  private val xorHandler: XorHandler = XorHandler(),
  private val subHandler: SubHandler = SubHandler()
) {
  private var eip = 0

  fun simulate(vm: VM) {
    while (true) {
      if (eip < 0 || eip > vm.instructions.size) {
        throw RuntimeException("eip is out of bounds eip = ($eip), upperBound = ${vm.instructions.size}")
      }

      val instruction = vm.instructions[eip]

      eip = when (instruction.instructionType) {
        InstructionType.Add -> addHandler.handle(vm, eip, instruction as Add)
        InstructionType.Call -> callHandler.handle(vm, eip, instruction as Call)
        InstructionType.Cmp -> cmpHandler.handle(vm, eip, instruction as Cmp)
        InstructionType.Jxx -> jxxHandler.handle(vm, eip, instruction as Jxx)
        InstructionType.Let -> letHandler.handle(vm, eip, instruction as Let)
        InstructionType.Mov -> movHandler.handle(vm, eip, instruction as Mov)
        InstructionType.Xor -> xorHandler.handle(vm, eip, instruction as Xor)
        InstructionType.Sub -> subHandler.handle(vm, eip, instruction as Sub)
        InstructionType.Ret -> return
      }
    }
  }
}