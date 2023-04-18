// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)
//
// This program only needs to handle arguments that satisfy
// R0 >= 0, R1 >= 0, and R0*R1 < 32768.

	@R2		// R2 = 0
	M=0
	@R1
	D=M
	@i		// i = R1
	M=D
	@END	// if (i == 0) goto END
	D;JEQ
(LOOP)
	@R0
	D=M
	@R2		// R2 = R2 + R0
	M=M+D
	@i		// i--
	M=M-1
	D=M
	@LOOP
	D;JGT	// if(i > 0) goto LOOP
(END)
	@END
	0;JMP
