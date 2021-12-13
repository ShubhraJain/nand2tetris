// pop local 2

// addr = LCL + 2
@LCL  // copy address of LCL in A
D=M  // copy value of LCL in D
@addr  // define variable addr
M=D
@i   // define variable i
D=M  // copy value of i into D
@addr 
M=M+D

// SP--
@SP
M=M-1

// *addr = *SP
A=M       
D=M       
@addr
A=M       
M=D
@15
0; JMP