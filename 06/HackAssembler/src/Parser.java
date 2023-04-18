import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Parser {

    BufferedReader reader;
    public String currentLine;

    
    public Parser(File file) throws IOException{
        // opening the file we need to read
        this.reader = new BufferedReader(new FileReader(file));
        this.currentLine = "";
    }

    public enum Instructions {
        A_INSTRUCTION,
        C_INSTRUCTION,
        L_INSTRUCTION,
        S_INSTRUCTION       // an A instruction of a symbol
    }
    public Instructions instructionType() {
        // deciding which instruction it is based on the beginning
        if(currentLine.charAt(0) == '@' && currentLine.charAt(1) >= '0' && currentLine.charAt(1) <= '9') return Instructions.A_INSTRUCTION;
        else if(currentLine.charAt(0) == '@') return Instructions.S_INSTRUCTION;
        else if(currentLine.charAt(0) == '(') return Instructions.L_INSTRUCTION;
        else return Instructions.C_INSTRUCTION;
    }

    public boolean advance() throws IOException {
        currentLine = reader.readLine();
        if(currentLine == null) return false;       // checking if there is more lines to read
        currentLine = currentLine.split("//")[0];   // ignoring comments
        currentLine = currentLine.trim();           // ignoring whitespaces
        if(currentLine.isEmpty()) return advance(); // if it's an empty line - read the next line
        return true;        // true means that we successfully read a line
    }

    public String dest() {
        String s = "";
        if(currentLine.contains("=")) {         // checking if there is a dest in the command
            s = currentLine.split("=")[0];
        }
        if(s.length() > 0) return s;
        return null;
    }

    public String comp() {
        String s;

        // deciding how we need to "cut" the string to get the comp field of the command:

        if(currentLine.contains("=") && currentLine.contains(";")) {
            s = currentLine.split("=")[1];
            s = s.split(";")[0];
        }
        else if(currentLine.contains("=")) {
            s = currentLine.split("=")[1];
        }
        else {
            s = currentLine.split(";")[0];
        }
        if(s.length() > 0) return s;
        return null;
    }

    public String jump() {
        // checking if there is a jump field in the command
        if(currentLine.contains(";")) return currentLine.split(";")[1];
        return null;
    }
    
    
}
