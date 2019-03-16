package crackme.vm

import crackme.vm.core.VmExecutionException
import crackme.vm.handlers.*
import crackme.vm.instructions.*

class VMSimulator(
  private val debugMode: Boolean = false,
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

  fun simulate(vm: VM) {
    var entryPoint = 0

    for (vmFunction in vm.vmFunctions) {
      if (vmFunction.key == "main") {
        break
      }

      entryPoint += vmFunction.value.instructions.size
    }

    var eip = entryPoint
    val instructions = mutableListOf<Instruction>()

    for (vmFunction in vm.vmFunctions) {
      instructions.addAll(vmFunction.value.instructions.map { it.value })
    }

    while (true) {
      if (eip < 0 || eip > instructions.size) {
        throw RuntimeException("ip is out of bounds ip = ($eip), upperBound = ${instructions.size}")
      }

      val currentInstruction = instructions[eip]
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