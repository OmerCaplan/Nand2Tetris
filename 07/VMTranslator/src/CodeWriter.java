import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CodeWriter {

    BufferedWriter writer;
    String fileName;

    public CodeWriter(File writeFile, String fileName) throws IOException {
        this.writer = new BufferedWriter(new FileWriter(writeFile, false));
        this.fileName = fileName;
    }

    public void close() throws IOException {
        writer.close();
    }

    public void writePush(String segment, int index) throws IOException {
        if (segment.equals("constant")) {
            writeLine("@" + index); //A=i
            writeLine("D=A");       //D=A (=i)
            writeLine("@SP");       //A=SP
            writeLine("A=M");       //A=RAM[SP]
            writeLine("M=D");       //RAM[SP]=D
            IncSP();                //SP++
        } else if (segment.equals("static")) {
            writeLine("@" + fileName + "." + index);
            writeLine("D=M");
            IncSP();
            writeLine("@SP");
            writeLine("A=M-1");
            writeLine("M=D");
        } else if(segment.equals("pointer")) {
            if(index == 0) writeLine("@THIS");
            else if(index == 1) writeLine("@THAT");
            writeLine("D=M");
            IncSP();
            writeLine("@SP");
            writeLine("A=M-1");
            writeLine("M=D");
        }else {    // segment != constant or static
            writeLine("@" + getSegmentKey(segment));      //A=segmentKey
            if(segment.equals("temp")) writeLine("D=A");    //D=5
            else writeLine("D=M");       //D=RAM[SEG]
            writeLine("@" + index); //A=i
            writeLine("A=D+A");     //A=SEG+i
            writeLine("D=M");       //D=RAM[addr]
            writeLine("@SP");       //A=SP
            writeLine("A=M");       //A=RAM[SP]
            writeLine("M=D");       //RAM[RAM[SP]] = RAM[addr]
            IncSP();                //SP++
        }
    }
    public void writePop(String segment, int index) throws IOException {
        if(segment.equals("static")) {
            DecSP();
            writeLine("@SP");
            writeLine("A=M");
            writeLine("D=M");
            writeLine("@" + fileName + "." + index);
            writeLine("M=D");
        } else if(segment.equals("pointer")) {
            DecSP();
            writeLine("@SP");
            writeLine("A=M");
            writeLine("D=M");
            if(index == 0) writeLine("@THIS");
            else if(index == 1) writeLine("@THAT");
            writeLine("M=D");
        } else {
            writeLine("@" + getSegmentKey(segment));      //A=segmentKey
            if(segment.equals("temp")) writeLine("D=A");    //D=5
            else writeLine("D=M");       //D=RAM[SEG]
            writeLine("@" + index); //A=i
            writeLine("D=D+A");     //D=SEG+i (addr)
            writeLine("@R14");
            writeLine("M=D");       //R14 stores addr
            DecSP();                //SP--
            writeLine("@SP");
            writeLine("A=M");
            writeLine("D=M");       //D=RAM[SP]
            writeLine("@R14");
            writeLine("A=M");       //A=addr
            writeLine("M=D");       //RAM[addr] = RAM[SP]
        }
    }

    public void writeArithmetic(String command) throws IOException {
        if(command.equals("add") || command.equals("sub") || command.equals("and") || command.equals("or")) {
            writeLine("@SP");
            writeLine("AM=M-1");
            writeLine("D=M");   // D = y (first pop)
            writeLine("A=A-1"); // A = address of x (second pop)
            if(command.equals("add")) writeLine("M=M+D");
            else if (command.equals("sub")) writeLine("M=M-D");
            else if (command.equals("and")) writeLine("M=M&D");
            else writeLine("M=M|D");
        } else if(command.equals("neg")) {
            writeLine("@SP");
            writeLine("A=M-1");
            writeLine("M=-M");  // RAM[RAM[SP] - 1] *= -1
        } else if(command.equals("not")) {
            writeLine("@SP");
            writeLine("A=M-1");
            writeLine("M=!M");  // ! RAM[RAM[SP] - 1]
        } else {
            // command is eq / lt / gt
            writeLine("@SP");
            writeLine("AM=M-1");
            writeLine("D=M");   // D = y (first pop)
            writeLine("A=A-1"); // A = address of x (second pop)
            writeLine("D=M-D"); // D = x - y
            writeLine("@" + (VMTranslator.lineCounter + 8));
            if(command.equals("eq")) writeLine("D;JEQ");
            else if(command.equals("lt")) writeLine("D;JLT");
            else if(command.equals("gt")) writeLine("D;JGT");
            // if didn't jump:
            writeLine("@SP");
            writeLine("A=M-1");
            writeLine("M=0");       // writing false
            writeLine("@" + (VMTranslator.lineCounter + 6));         // jump to what comes after
            writeLine("0;JMP");

            //if did jump:
            writeLine("@SP");
            writeLine("A=M-1");
            writeLine("M=-1");      // writing true
        }
    }

    private void writeLine(String line) throws IOException {
        // helper method - to avoid writing "writer.newLine()" every time
        writer.write(line);
        writer.newLine();
        VMTranslator.lineCounter++;
    }

    private void IncSP() throws IOException {
        // writing the hack code of SP++
        writeLine("@SP");
        writeLine("M=M+1");
    }

    private void DecSP() throws IOException {
        // writing the hack code of SP--
        writeLine("@SP");
        writeLine("M=M-1");
    }

    private String getSegmentKey(String segment) {
        if(segment.equals("local")) return "LCL";
        if(segment.equals("argument")) return "ARG";
        if(segment.equals("this")) return "THIS";
        if(segment.equals("that")) return "THAT";
        if(segment.equals("temp")) return "5";
        else return "";
    }
}
