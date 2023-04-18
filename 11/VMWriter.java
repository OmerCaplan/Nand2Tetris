/**
 * This class writes VM commands to a .vm file.
 *
 * @author OmerCaplan
 * @author YogevCuperman
 *
 * */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class VMWriter {

    /* FIELDS:*/
    private BufferedWriter writer;

    /**
     * Constructor method.
     *
     * @param file is the file the VMWriter will write to
     * @throws IOException in case of I/O problem
     */
    public VMWriter(File file) throws IOException {
        writer = new BufferedWriter(new FileWriter(file));
    }

    /**
     * This method writes a string to the file and create a new line (same as '\n').
     *
     * @param str is the string will be written to the file
     * @throws IOException in case of I/O problem
     */
    public void writeLine(String str) throws IOException {
        writer.write(str);
        writer.newLine();
    }

    /**
     * This method writes a VM push command.
     *
     * @param segment is on of the following: (constant, static, temp, pointer, this, that, local, argument)
     * @param index is an integer greater or equal to 0
     * @throws IOException in case of I/O problem
     */
    public void writePush(String segment, int index) throws IOException {
        // interpreted as "var", actually is "local":
        if(segment.equals("var")) segment = "local";
        writeLine("push " + segment + " " + index);
    }

    /**
     * This method writes a VM pop command.
     *
     * @param segment is on of the following: (static, temp, pointer, this, that, local, argument)
     * @param index is an integer greater or equal to 0
     * @throws IOException in case of I/O problem
     */
    public void writePop(String segment, int index) throws IOException {
        // interpreted as "var", actually is "local":
        if(segment.equals("var")) segment = "local";
        writeLine("pop " + segment + " " + index);
    }

    /**
     * This method writes the VM arithmetic commands.
     *
     * @param command is one of the following: (add, sub, neg, not, and, or, gt, lt, eq,
     *                call Math.multiply 2, call Math.divide 2)
     * @throws IOException in case of I/O problem
     */
    public void writeArithmetic(String command) throws IOException {
        writeLine(command);
    }

    /**
     * This method writes the VM label command.
     *
     * @param label is the label name as string
     * @throws IOException in case of I/O problem
     */
    public void writeLabel(String label) throws IOException {
        writeLine("label " + label.toUpperCase());
    }

    /**
     * This method writes the VM goto command.
     *
     * @param label is the name of the label to jump to
     * @throws IOException in case of I/O problem
     */
    public void writeGoto(String label) throws IOException {
        writeLine("goto " + label.toUpperCase());
    }

    /**
     * This method writes the VM if-goto command.
     *
     * @param label is the name of the label to jump to
     * @throws IOException in case of I/O problem
     */
    public void writeIf(String label) throws IOException {
        writeLine("if-goto " + label.toUpperCase());
    }

    /**
     * This method writes the VM call command.
     *
     * @param name is the name of the subroutine called
     * @param nArgs is the number of arguments sent as parameters
     * @throws IOException in case of I/O problem
     */
    public void writeCall(String name, int nArgs) throws IOException {
        writeLine("call " + name + " " + nArgs);
    }

    /**
     * This method writes the VM function command.
     *
     * @param name is the name of the subroutine declared
     * @param nVars is the number of local variables needed
     * @throws IOException in case of I/O problem
     */
    public void writeFunction(String name, int nVars) throws IOException {
        writeLine("function " + name + " " + nVars);
    }

    /**
     * This method writes the VM return command.
     *
     * @throws IOException in case of I/O problem
     */
    public void writeReturn() throws IOException {
        writeLine("return");
    }

    /**
     * This method closes the file that was written.
     *
     * @throws IOException in case of I/O problem
     */
    public void close() throws IOException {
        writer.close();
    }
}
