// push local i

@5  // define variable i
D=A  // copy value to be pushed into D
@LCL  // copy address of LCL in A
D=D+M  // copy value of LCL in D
A=D 
D=M  // now d contains *addr
@sp
A=M
M=D  // write *addr into *sp
@sp
M=M+1  // increment SP



//push
@THIS
D=M
@SP
A=M
M=D
@SP
M=M+1



// pop
@SP
M=M-1
A=M
D=M
@THIS
M=D