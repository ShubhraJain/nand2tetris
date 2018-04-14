// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)

// Put your code here.

    @i
    M=0 // i = 0
    @2
    M=0

(LOOP)
    @i
    D=M // D = i
    @0
    D=D-M // D = i - R0
    @END
    D;JEQ // D i.e. i - R0 >= 0, end    

    @1
    D=M // D = R1
    @2
    M=D+M // R2 = R1 + R2
    @i
    M=M+1 // i = i + 1
    @LOOP
    0;JMP

(END)
    @END
    0;JMP