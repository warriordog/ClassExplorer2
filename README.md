Class Explorer 2:
A tool for examining java classes
---

Class Explorer 2 is a complete redesign of an older tool that
I wrote to examine compiled, obfuscated java classes.  Class
Explorer 1 had a fundamental security flaw that left it basically
useless for real work, so CE2 was created to replace it.
CE2 uses javassist to perform offline static analysis of bytecode, so there is no risk of code becoming live during 
examination.

CE2 does not quite have all the features of CE1, but it does
include a full bytecode disassembler to allow more in-depth
analysis of code.
