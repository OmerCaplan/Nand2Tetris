import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class HackAssembler {
    public static void main(String[] args) {
        // setting the reading and writing files paths:
        String readFile = args[0];
        String writeFile = readFile.split(".asm")[0] + ".hack";

        SymbolTable symbols = new SymbolTable();

        // FIRST PASS - going over the labels only:
        try {
            Parser parser = new Parser(new File(readFile));
            int lineCounter = 0;        // keeping track on how many lines were before (no empty lines - because of Parser.advance() )
            while(parser.advance()) {
                if(parser.instructionType() == Parser.Instructions.L_INSTRUCTION) {
                    String label = parser.currentLine.substring(1, parser.currentLine.length() - 1);
                    symbols.put(label, lineCounter);
                }
                else lineCounter++;
            }
        } catch (Exception e) {}


        // SECOND PASS - going over S Instructions only (e.g. @SYMBOL and not @17):
        try {
            Parser parser = new Parser(new File(readFile));
            while(parser.advance()) {
                if(parser.instructionType() == Parser.Instructions.S_INSTRUCTION) {
                    String symbol = parser.currentLine.substring(1);
                    //adding entry to the map only if there isn't one for this symbol before
                    if(!symbols.containsKey(symbol)) symbols.addSymbol(symbol);
                }
            }
        } catch (Exception e) {}

        // MAIN LOOP - reading and translating all the commands:
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(writeFile, false));
            Parser parser = new Parser(new File(readFile));
            Code coder = new Code();

            while(parser.advance()) {
                String line = "";
                if (parser.instructionType() == Parser.Instructions.A_INSTRUCTION) {
                    line = "0";
                    int temp = Integer.parseInt(parser.currentLine.split("@")[1]);
                    String binary = Integer.toBinaryString(temp);   // translating to binary
                    for (int i = 0; i < 15 - binary.length(); i++) {    // adding the missing zeros before the number
                        line += "0";
                    }
                    line += binary;
                } else if (parser.instructionType() == Parser.Instructions.C_INSTRUCTION) {
                    // building the instruction by it's parts
                    line = "111";
                    line += coder.comp(parser.comp());
                    line += coder.dest(parser.dest());
                    line += coder.jump(parser.jump());
                } else if (parser.instructionType() == Parser.Instructions.S_INSTRUCTION) {
                    line = "0";
                    String symbol = parser.currentLine.substring(1);
                    int temp = symbols.get(symbol);                  // getting the correct value of the symbol
                    String binary = Integer.toBinaryString(temp);    // translating to binary
                    for (int i = 0; i < 15 - binary.length(); i++) { // adding the missing zeros before the number
                        line += "0";
                    }
                    line += binary;
                } else { // L instruction
                    continue;           // if it's an L instruction we don't do anything on the main loop
                }
                writer.write(line);     // writing the translated command to the writing file
                writer.newLine();
            }
            writer.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
