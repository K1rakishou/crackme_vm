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
  private val subHandler: SubHandler = SubHandler(),
  private val incHandler: IncHandler = IncHandler(),
  private val decHandler: DecHandler = DecHandler(),
  private val pushHandler: PushHandler = PushHandler(),
  private val popHandler: PopHandler = PopHandler(),
  private val retHandler: RetHandler = RetHandler()
) {

  fun simulate(vm: VM, entryPoint: Int, instructions: List<Instruction>) {
    var eip = entryPoint

    while (true) {
      val currentInstruction = instructions.getOrNull(eip)
      if (currentInstruction == null) {
        throw RuntimeException("ip is out of bounds ip = ($eip), upperBound = ${instructions.size}")
      }

      eip = when (currentInstruction.instructionType) {
        InstructionType.Add -> addHandler.handle(vm, eip, currentInstruction as Add)
        InstructionType.Call -> callHandler.handle(vm, eip, currentInstruction as Call)
        InstructionType.Cmp -> cmpHandler.handle(vm, eip, currentInstruction as Cmp)
        InstructionType.Jxx -> jxxHandler.handle(vm, eip, currentInstruction as Jxx)
        InstructionType.Let -> letHandler.handle(vm, eip, currentInstruction as Let)
        InstructionType.Mov -> movHandler.handle(vm, eip, currentInstruction as Mov)
        InstructionType.Xor -> xorHandler.handle(vm, eip, currentInstruction as Xor)
        InstructionType.Sub -> subHandler.handle(vm, eip, currentInstruction as Sub)
        InstructionType.Inc -> incHandler.handle(vm, eip, currentInstruction as Inc)
        InstructionType.Dec -> decHandler.handle(vm, eip, currentInstruction as Dec)
        InstructionType.Push -> pushHandler.handle(vm, eip, currentInstruction as Push)
        InstructionType.Pop -> popHandler.handle(vm, eip, currentInstruction as Pop)
        InstructionType.Ret -> retHandler.handle(vm, eip, currentInstruction as Ret)
      }

      if (eip == Int.MAX_VALUE) {
        return
      }
    }
  }

}