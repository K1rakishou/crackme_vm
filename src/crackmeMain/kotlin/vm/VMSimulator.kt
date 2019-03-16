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
  private val popHandler: PopHandler = PopHandler()
) {

  fun simulate(vm: VM) {
//    for (vmFunctionEntry in vm.vmFunctions) {
//      val vmFunction = vmFunctionEntry.value
//
//      println("function ${vmFunction.name}")
//
//      for (label in vmFunction.labels) {
//        println("label name = ${label.key}, label address = ${label.value}")
//      }
//
//      for (instruction in vmFunction.instructions) {
//        when {
//          instruction.value is Jxx -> {
//            val jxx = instruction.value as Jxx
//            println("[${instruction.key}] ${jxx.jumpType.jumpName} @${vmFunctionEntry.value.labels[jxx.labelName]}")
//          }
//          instruction.value is Call -> {
//            val call = instruction.value as Call
//            println("[${instruction.key}] call @${vm.vmFunctions[call.functionName]!!.instructions.keys.first()}")
//          }
//          else -> {
//            println("[${instruction.key}] ${instruction.value}")
//          }
//        }
//      }
//    }

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
      val currentInstruction = instructions[eip]
      println("eip = $eip, instruction = $currentInstruction")

      eip = when (currentInstruction.instructionType) {
        InstructionType.Add -> addHandler.handle(vm, eip, currentInstruction as Add)
        InstructionType.Call -> {
          val call = currentInstruction as Call

          val vmFunction = vm.vmFunctions[call.functionName]
          if (vmFunction == null) {
            throw RuntimeException("No function defined with name (${call.functionName})")
          }

          println("Pushing eip (${eip + 1}) into stack")
          vm.vmStack.push64(eip + 1L)
          vmFunction.start
        }
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
        InstructionType.Ret -> {
          if (vm.vmStack.isEmpty()) {
            return
          }

          val newEip = vm.vmStack.pop64()
          println("new eip = ${newEip}")

          newEip.toInt()
        }
      }
    }

//    var instructionPointer = 0
//
//    while (true) {
//      val currentInstruction = currentInstructions[0]
////      simulateInstruction(currentInstruction)
//    }

//    while (true) {
//      if (eip < 0 || eip > vm.instructions.size) {
//        throw RuntimeException("ip is out of bounds ip = ($eip), upperBound = ${vm.instructions.size}")
//      }
//
//      val instruction = if (debugMode) {
//        vm.instructions.getOrNull(eip) ?: return
//      } else {
//        vm.instructions[eip]
//      }
//
//
//    }
  }

  fun simulateInstruction(instruction: Instruction) {

  }
}