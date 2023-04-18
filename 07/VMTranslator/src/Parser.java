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

    public enum Commands {
        ARITHMETIC,
        PUSH,
        POP
    }

    public boolean advance() throws IOException {
        currentLine = reader.readLine();
        if(currentLine == null) return false;       // checking if there is more lines to read
        currentLine = currentLine.split("//")[0];   // ignoring comments
        currentLine = currentLine.trim();           // ignoring whitespaces
        if(currentLine.isEmpty()) return advance(); // if it's an empty line - read the next line
        return true;        // true means that we successfully read a line
    }

    public Commands commandType() {
        if(currentLine.contains("push")) return Commands.PUSH;
        else if (currentLine.contains("pop")) return Commands.POP;
        else return Commands.ARITHMETIC;
    }

    public String arg1() {
        if(commandType() == Commands.ARITHMETIC) return currentLine;
        else {      // push or pop
            String arg1 = currentLine.split(" ")[1];
            return arg1;
        }
    }

    public int arg2() {
        // should be only called if the command is push or pop

        String arg2 = currentLine.split(" ")[2];
        return Integer.parseInt(arg2);
    }


}
