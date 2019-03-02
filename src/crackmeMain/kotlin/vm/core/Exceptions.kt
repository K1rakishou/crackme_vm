package crackme.vm.core

class ParsingException(programLine: Int, message: String) : Exception("[Error at line ${programLine + 1}]: ${message}")
