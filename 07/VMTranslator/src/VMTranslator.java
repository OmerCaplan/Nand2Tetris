import java.io.File;
import java.io.IOException;

public class VMTranslator {

    public static int lineCounter = -1;     // public counter of which line was written last

    public static void main(String[] args) throws IOException {
        String readFile = args[0];
        String writeFile = readFile.replace(".vm", ".asm");
        String[] fileNameTemp = readFile.split(".vm")[0].split("/");
        String fileName = fileNameTemp[fileNameTemp.length - 1];



        Parser parser = new Parser(new File(readFile));
        CodeWriter coder = new CodeWriter(new File(writeFile), fileName);

        while(parser.advance()) {
            // checking which command should we translate:

            if(parser.commandType() == Parser.Commands.ARITHMETIC) {
                coder.writeArithmetic(parser.arg1());
            } else if(parser.commandType() == Parser.Commands.PUSH) {
                coder.writePush(parser.arg1(), parser.arg2());
            } else if(parser.commandType() == Parser.Commands.POP) {
                coder.writePop(parser.arg1(), parser.arg2());
            }
        }

        coder.close();
    }
}
