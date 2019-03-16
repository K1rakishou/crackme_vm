package crackme.vm.core

import crackme.vm.instructions.Instruction

class VmFunction(
  val name: String,
  val start: Int,
  val length: Int,
  val labels: Map<String, Int>,
  val instructions: LinkedHashMap<Int, Instruction>
)