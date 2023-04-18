import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class HackAssembler {
    public static void main(String[] args) {
        String writeFile = "C:/Users/yogev/Desktop/Nand2Tetris/projects/06/OUTPUTS/Max.hack";
        String readFile = "C:/Users/yogev/Desktop/Nand2Tetris/projects/06/max/Max.asm";

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(writeFile, false));
            Parser parser = new Parser(new File(readFile));
            Code coder = new Code();

            while(parser.advance()) {
                String line = "";
                if (parser.instructionType() == Parser.Instructions.A_INSTRUCTION) {
                    line = "0";
                    int temp = Integer.parseInt(parser.currentLine.split("@")[1]);
                    String binary = Integer.toBinaryString(temp);
                    for (int i = 0; i < 15 - binary.length(); i++) {
                        line += "0";
                    }
                    line += binary;
                } else if (parser.instructionType() == Parser.Instructions.C_INSTRUCTION) {
                    line = "111";
                    line += coder.comp(parser.comp());
                    line += coder.dest(parser.dest());
                    line += coder.jump(parser.jump());
                }
                writer.write(line);
                writer.newLine();
                System.out.println(line);
            }
            writer.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
