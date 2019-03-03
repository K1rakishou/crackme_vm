package crackme.vm

import crackme.vm.handlers.AddHandler
import crackme.vm.handlers.CallHandler
import crackme.vm.handlers.MovHandler
import crackme.vm.instructions.*

class VMExecutor(
  private val vm: VM,
  private val movHandler: MovHandler = MovHandler(),
  private val addHandler: AddHandler = AddHandler(),
  private val callHandler: CallHandler = CallHandler()
) {
  private var eip = 0

  fun run(): Long {
    while (true) {
      val instruction = vm.instructions[eip]

      when (instruction) {
        is Add -> addHandler.handle(vm, eip, instruction)
        is Call -> callHandler.handle(vm, eip, instruction)
        is Cmp -> executeCmp(instruction)
        is Jxx -> executeJxx(instruction)
        is Let -> executeLet(instruction)
        is Mov -> movHandler.handle(vm, eip, instruction)
        is Ret -> {
          return vm.registers[instruction.result.index]
        }
      }

      ++eip
    }
  }

  private fun executeCmp(instruction: Cmp) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  private fun executeJxx(instruction: Jxx) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  private fun executeLet(instruction: Let) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  class VmExecutionException(eip: Int,
                             message: String) : Exception("Error at $eip instruction, $message")
}