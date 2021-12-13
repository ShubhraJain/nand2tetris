// push local i

@5  // define variable i
D=A  // copy value to be pushed into D
@LCL  // copy address of LCL in A
D=D+M  // copy value of LCL in D
@addr  // define variable addr
M=D

@addr
A=M
D=M

@SP
A=M
M=D

@SP
M=M+1
@16
0;JMP