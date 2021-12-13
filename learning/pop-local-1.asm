// pop static 8
// @SP
// M=M-1
// A=M
// D=M
// @pop-local-1.8
// M=D
// @7
// 0;JMP

// push static 4
@pop-local-1.4
D=M
@SP
A=M
M=D
@SP
M=M+1
@7
0;JMP



// push pointer 0
// @THIS
// D=M
// @SP
// A=M
// M=D
// @SP
// M=M+1
// @7
// 0;JMP


// pop pointer 0
// @SP
// M=M-1
// A=M
// D=M
// @THIS
// M=D

// pop temp 6
// @11
// D=A
// @temp
// D=D+M
// @addr
// M=D
// @SP
// M=M-1
// A=M
// D=M  // now D contains *SP
// @addr
// A=M
// M=D
// @13
// 0;JMP


// push temp 6
// @11  // define variable i
// D=A  // copy value to be pushed into D
// @temp  // copy address of LCL in A
// D=D+M  // copy value of LCL in D
// A=D 
// D=M  // now d contains *addr
// @sp
// A=M
// M=D  // write *addr into *sp
// @sp
// M=M+1 