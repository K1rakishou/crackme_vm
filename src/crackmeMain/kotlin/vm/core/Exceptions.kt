package crackme.vm

class ParsingException(lineNumber: Int, message: String) : Exception("[Error at line ${lineNumber}]: ${message}")
