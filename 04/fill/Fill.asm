// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

	@8192
	D=A
	@R1
	M=D
(LOOP)
	// i = 0 (for coloring later)
	@i
	M=0
	@KBD
	D=M
	// if (KBD != 0) goto BLACK; else goto WHITE
	@BLACK
	D;JNE
	@WHITE
	0;JMP

(BLACK)
	// if(i == R1) goto LOOP
	@i
	D=M
	@R1
	D=D-M
	@LOOP
	D;JEQ
	// RAM[SCREEN + i] = -1 (black)
	@SCREEN
	D=A
	@i
	D=D+M
	A=D
	M=-1
	// i = i + 1
	@i
	M=M+1
	//goto BLACK
	@BLACK
	0;JMP

(WHITE)
	// if(i == R1) goto LOOP
	@i
	D=M
	@R1
	D=D-M
	@LOOP
	D;JEQ
	// RAM[SCREEN + i] = 0 (white)
	@SCREEN
	D=A
	@i
	D=D+M
	A=D
	M=0
	// i = i + 1
	@i
	M=M+1
	//goto WHITE
	@WHITE
	0;JMP