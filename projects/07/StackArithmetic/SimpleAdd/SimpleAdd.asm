// C_PUSH constant 7

// *SP=7
@7
D=A
@SP
A=M
M=D

// SP++ 
@SP
M=M+1
// C_PUSH constant 8

// *SP=8
@8
D=A
@SP
A=M
M=D

// SP++ 
@SP
M=M+1

// add
@SP
A=M-1
D=M
A=A-1
D=D+M
M=D

// SP-- 
@SP
M=M-1
@22
0;JMP