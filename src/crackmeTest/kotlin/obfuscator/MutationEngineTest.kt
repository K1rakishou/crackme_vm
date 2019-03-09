package sample.helloworld.obfuscator

import crackme.vm.instructions.Mov
import crackme.vm.operands.C32
import crackme.vm.operands.Register
import kotlin.test.Test
import kotlin.test.assertEquals

class MutationEngineTest : AbstractMutationEngineTest() {

  @Test
  fun testSimpleMutateMov() {
    test<Mov>(
      mutator = { mutationEngine -> mutationEngine.mutateMov(Mov(Register(3), C32(1234))) },
      checker = { vm -> assertEquals(1234, vm.registers[3]) }
    )
  }
}