package crackme.vm

import crackme.vm.handlers.AddHandler
import crackme.vm.handlers.CallHandler
import crackme.vm.handlers.LetHandler
import crackme.vm.handlers.MovHandler
import crackme.vm.instructions.*

class VMSimulator(
  private val movHandler: MovHandler = MovHandler(),
  private val addHandler: AddHandler = AddHandler(),
  private val callHandler: CallHandler = CallHandler(),
  private val letHandler: LetHandler = LetHandler(),
  private val cmpHandler: CmpHandler = CmpHandler()
) {
  private var eip = 0

  fun simulate(vm: VM) {
    while (true) {
      when (val instruction = vm.instructions[eip]) {
        is Add -> addHandler.handle(vm, eip, instruction)
        is Call -> callHandler.handle(vm, eip, instruction)
        is Cmp -> cmpHandler.handle(vm, eip, instruction)
        is Jxx -> TODO("Jxx")
        is Let -> letHandler.handle(vm, eip, instruction)
        is Mov -> movHandler.handle(vm, eip, instruction)
        is Ret -> return
      }

      ++eip
    }
  }
}