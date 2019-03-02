package crackme

class ParsingException(lineNumber: Int, message: String) : Exception("[Error at line ${lineNumber}]: ${message}")

enum class Type(val str: String) {
  IntType("Int"),
  LongType("Long"),
  FloatType("Float"),
  DoubleType("Double"),
  StringType("String");

  companion object {
    private val map = mapOf(
      "Int" to IntType,
      "Long" to LongType,
      "Float" to FloatType,
      "Double" to DoubleType,
      "String" to StringType
    )

    fun fromString(str: String): Type {
      return map.getValue(str)
    }
  }
}

enum class NativeFunctionType(val funcName: String) {
  VmExit("vm_exit"),
  Println("println");

  companion object {
    private val map = mapOf(
      "vm_exit" to VmExit,
      "println" to Println
    )

    fun fromString(funcName: String): NativeFunctionType {
      return map.getValue(funcName)
    }
  }
}

interface Operand

class Register(
  val index: Int
) : Operand {

  override fun toString(): String {
    return "r$index"
  }
}

abstract class Constant : Operand

class C32(
  val value: UInt
) : Constant() {

  override fun toString(): String {
    return value.toString()
  }
}

class C64(
  val value: ULong
) : Constant() {

  override fun toString(): String {
    return value.toString()
  }
}

interface VmParameter

class NativeFunction(
  val parameters: List<Type>
) : VmParameter

interface Instruction

class Mov(
  val dest: Operand,
  val src: Operand
) : Instruction {

  override fun toString(): String {
    return "mov $dest, $src"
  }
}

class Add(
  val dest: Operand,
  val src: Operand
) : Instruction {

  override fun toString(): String {
    return "add $dest, $src"
  }
}

class Cmp(
  val dest: Operand,
  val src: Operand
) : Instruction {

  override fun toString(): String {
    return "cmp $dest, $src"
  }
}

class VM {
  val nativeFunctions = mutableMapOf<NativeFunctionType, NativeFunction>()
  val instructions = mutableListOf<Instruction>()
  val registers = listOf(0UL, 0UL, 0UL, 0UL, 0UL, 0UL, 0UL, 0UL)

  fun parse(program: String) {
    val lines = program.split("\n")
      .map { it.trim() }
      .filterNot { it.isEmpty() }

    val instructionList = lines.mapIndexed { index, str ->
      val instruction = tryParseInstruction(index + 1, str)
      if (instruction == null) {
        //TODO: parse labels etc
      } else {
        println(instruction)
      }

      instruction
    }.filterNotNull()

    instructions.addAll(0, instructionList)
  }

  private fun tryParseInstruction(lineNum: Int, line: String): Instruction? {
    val indexOfFirstSpace = line.indexOfFirst { it == ' ' }
    if (indexOfFirstSpace == -1) {
      return null
    }

    val instructionName = line.substring(0, indexOfFirstSpace).toLowerCase()
    val body = line.substring(indexOfFirstSpace)

    return when (instructionName) {
      "mov" -> parseMov(lineNum, body)
      "add" -> parseAdd(lineNum, body)
      "cmp" -> parseCmp(lineNum, body)
      "je",
      "jne",
      "jmp" -> parseJxx(lineNum, instructionName, body)
      "call" -> parseCall(lineNum, body)
      else -> null
    }
  }

  private fun parseCall(lineNum: Int, body: String): Instruction? {
    return null
  }

  private fun parseJxx(lineNum: Int, instructionName: String, body: String): Instruction? {
    return null
  }

  private fun parseCmp(lineNum: Int, body: String): Instruction? {
    if (body.isEmpty()) {
      throw ParsingException(lineNum, "Instruction has name but does not have a body")
    }

    if (body.indexOf(',') == -1) {
      throw ParsingException(lineNum, "Cannot parse operands because there is no \',\' symbol")
    }

    val (destOperand, srcOperand) = body.split(',')
      .map { it.trim() }

    return Cmp(
      parseOperand(lineNum, destOperand),
      parseOperand(lineNum, srcOperand)
    )
  }

  private fun parseAdd(lineNum: Int, body: String): Instruction? {
    if (body.isEmpty()) {
      throw ParsingException(lineNum, "Instruction has name but does not have a body")
    }

    if (body.indexOf(',') == -1) {
      throw ParsingException(lineNum, "Cannot parse operands because there is no \',\' symbol")
    }

    val (destOperand, srcOperand) = body.split(',')
      .map { it.trim() }

    return Add(
      parseOperand(lineNum, destOperand),
      parseOperand(lineNum, srcOperand)
    )
  }

  private fun parseMov(lineNum: Int, body: String): Instruction? {
    if (body.isEmpty()) {
      throw ParsingException(lineNum, "Instruction has name but does not have a body")
    }

    if (body.indexOf(',') == -1) {
      throw ParsingException(lineNum, "Cannot parse operands because there is no \',\' symbol")
    }

    val (destOperand, srcOperand) = body.split(',')
      .map { it.trim() }

    return Mov(
      parseOperand(lineNum, destOperand),
      parseOperand(lineNum, srcOperand)
    )
  }

  private fun parseOperand(lineNum: Int, operandString: String): Operand {
    val ch = operandString[0].toLowerCase()

    when {
      ch == 'r' -> {
        val registerIndex = numberRegex.find(operandString)?.value?.toInt()
        if (registerIndex == null) {
          throw ParsingException(lineNum, "Cannot parse register index for operand ${operandString}")
        }

        return Register(registerIndex)
      }
      ch == '[' -> {
        TODO("memory type")
      }
      ch.isDigit() -> {
        val constantString = numberRegex.find(operandString)?.value
        if (constantString == null) {
          throw ParsingException(lineNum, "Cannot parse constant operand ${operandString}")
        }

        return extractConstant(lineNum, constantString)
      }
      else -> throw ParsingException(lineNum, "Cannot parse operand for \'${operandString}\'")
    }
  }

  private fun extractConstant(lineNum: Int, constantString: String): Constant {
    if (constantString.isEmpty()) {
      throw ParsingException(lineNum, "Constant is empty")
    }

    val extractedValue = constantString.toULongOrNull()
    if (extractedValue == null) {
      throw ParsingException(lineNum, "Cannot parse constant ${constantString}, unknown error")
    }

    //TODO: add C16 and C8 here when needed

    if (extractedValue >= UInt.MIN_VALUE && extractedValue <= UInt.MAX_VALUE) {
      return C32(extractedValue.toUInt())
    }

    return C64(extractedValue)
  }

  companion object {
    val numberRegex = Regex("[0-9]+")
  }
}

fun main() {
  val testProgram = """
        use vm_exit(Int)
        use println(String)

        mov r0, 1
        mov r1, 3
        add r0, r1
        cmp r0, 4
        je BAD
        call println("OK")
        jmp EXIT

BAD:
        call println("BAD")
EXIT:
        call vm_exit(0)
    """

  val vm = VM()
  vm.parse(testProgram)
}
