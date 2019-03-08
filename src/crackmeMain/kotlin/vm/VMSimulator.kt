package crackme.vm

import crackme.vm.handlers.*
import crackme.vm.instructions.*

class VMSimulator(
  private val movHandler: MovHandler = MovHandler(),
  private val addHandler: AddHandler = AddHandler(),
  private val callHandler: CallHandler = CallHandler(),
  private val letHandler: LetHandler = LetHandler(),
  private val cmpHandler: CmpHandler = CmpHandler(),
  private val jxxHandler: JxxHandler = JxxHandler()
) {
  private var eip = 0

  fun simulate(vm: VM) {
    while (true) {
      if (eip < 0 || eip > vm.instructions.size) {
        throw RuntimeException("eip is out of bounds eip = ($eip), upperBound = ${vm.instructions.size}")
      }

      when (val instruction = vm.instructions[eip]) {
        is Add -> eip = addHandler.handle(vm, eip, instruction)
        is Call -> eip = callHandler.handle(vm, eip, instruction)
        is Cmp -> eip = cmpHandler.handle(vm, eip, instruction)
        is Jxx -> eip = jxxHandler.handle(vm, eip, instruction)
        is Let -> eip = letHandler.handle(vm, eip, instruction)
        is Mov -> eip = movHandler.handle(vm, eip, instruction)
        is Ret -> return
      }
    }
  }
}