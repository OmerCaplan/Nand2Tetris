/**
 * This class writes an .xml file by the tokens parsed from JackTokenizer class.
 *
 * @author OmerCaplan
 * @author YogevCuperman
 *
 * */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CompilationEngine {

    /* FIELDS: */

    private JackTokenizer tokenizer;
    private BufferedWriter writer;

    /**
     * Constructor - initializes the compilation engine.
     * Initializes a tokenizer and getting ready to write the file.
     *
     * @param input is a file holds the Jack code
     * @param output is a file that the .xml code will be written to
     * @throws IOException in case of I/O problem
     * */
    public CompilationEngine (File input, File output) throws IOException {
        this.tokenizer = new JackTokenizer(input);
        tokenizer.advance();
        this.writer = new BufferedWriter(new FileWriter(output));
    }

    /**
     * This method writes the code for a class by the class expected build.
     *
     * @throws IOException in case of I/O problem
     * */
    public void compileClass() throws IOException {
        writeLine("<class>");
        process("class");
        process(tokenizer.identifier());    // class name
        process("{");

        // compiling class variables declarations:
        while(tokenizer.keyWord().equals("STATIC") || tokenizer.keyWord().equals("FIELD")) {
            compileClassVarDec();
        }

        // compiling subroutines:
        while (tokenizer.keyWord().equals("CONSTRUCTOR") ||
                tokenizer.keyWord().equals("FUNCTION") ||
                tokenizer.keyWord().equals("METHOD")) {
            compileSubroutine();
        }

        process("}");
        writeLine("</class>");
    }

    /**
     * This method writes the code for a class variable declaration by the declaration expected build.
     *
     * @throws IOException in case of I/O problem
     * */
    public void compileClassVarDec() throws IOException {
        writeLine("<classVarDec>");
        process(tokenizer.currentToken);    // static or field
        processType();                      // type or identifier (object type)
        process(tokenizer.identifier());    // variable name

        // if there is more than one variable of the same type:
        while(tokenizer.symbol() == ',') {
            process(",");
            process(tokenizer.identifier());
        }

        process(";");
        writeLine("</classVarDec>");
    }

    /**
     * This method writes the code for a subroutine by the subroutine expected build.
     *
     * @throws IOException in case of I/O problem
     * */
    public void compileSubroutine() throws IOException {
        writeLine("<subroutineDec>");
        process(tokenizer.currentToken);        // constructor or function or method

        // checking if it's void or not:
        if(tokenizer.currentToken.equals("void")) process("void");
        else processType();

        process(tokenizer.identifier());    // subroutine name

        // compiling the parameters part:
        process("(");
        compileParameterList();
        process(")");

        // compiling the subroutine body:
        compileSubroutineBody();

        writeLine("</subroutineDec>");
    }

    /**
     * This method writes the code for a parameter list by the parameter list expected build.
     *
     * @throws IOException in case of I/O problem
     * */
    public void compileParameterList() throws IOException {
        writeLine("<parameterList>");

        // if the next token is ')' then there are no parameters. otherwise:
        if(!tokenizer.currentToken.equals(")")) {
            processType();                          // parameter type
            process(tokenizer.identifier());        // parameter name

            // if there are more parameters:
            while(!tokenizer.currentToken.equals(")")) {
                process(",");
                processType();                      // parameter type
                process(tokenizer.identifier());    // parameter name
            }
        }
        writeLine("</parameterList>");
    }

    /**
     * This method writes the code for a subroutine body by the subroutine body expected build.
     *
     * @throws IOException in case of I/O problem
     * */
    public void compileSubroutineBody() throws IOException {
        writeLine("<subroutineBody>");
        process("{");

        // handling variable declatrations:
        while(tokenizer.currentToken.equals("var")) {
            compileVarDec();
        }

        // compiling statements until the end of the subroutine:
        compileStatements();

        process("}");
        writeLine("</subroutineBody>");
    }

    /**
     * This method writes the code for a variable declaration by the variable declaration expected build.
     *
     * @throws IOException in case of I/O problem
     * */
    public void compileVarDec() throws IOException {
        writeLine("<varDec>");

        process("var");                 // 'var' keyword
        processType();                      // variable type
        process(tokenizer.identifier());    // variable name

        // if there are more variables of the same type:
        while(tokenizer.currentToken.equals(",")) {
            process(",");
            process(tokenizer.identifier());
        }

        process(";");

        writeLine("</varDec>");
    }

    /**
     * This method writes the code for a type part (int/char/boolean/class identifier).
     * Should be called only when expecting to get type.
     *
     * @throws IOException in case of I/O problem
     * */
    public void processType() throws IOException {
        // writing the script for the relevant <keyword> or <identifier>:
        if(tokenizer.tokenType().equals("KEYWORD") || tokenizer.tokenType().equals("IDENTIFIER")) {
          process(tokenizer.currentToken);
        } else {
            writer.write("SYNTAX ERROR - processType");     // for self-debugging
        }
    }

    /**
     * This method writes the code for a statements part.
     *
     * @throws IOException in case of I/O problem
     * */
    public void compileStatements() throws IOException {
        writeLine("<statements>");

        // while the next token is the begging of a statement - compile it:
        while (tokenizer.tokenType().equals("KEYWORD")) {
            if(tokenizer.keyWord().equals("LET")) compileLet();
            else if(tokenizer.keyWord().equals("IF")) compileIf();
            else if(tokenizer.keyWord().equals("WHILE")) compileWhile();
            else if(tokenizer.keyWord().equals("DO")) compileDo();
            else if(tokenizer.keyWord().equals("RETURN")) compileReturn();
            else break; // no more statements
        }

        writeLine("</statements>");
    }

    /**
     * This method writes the code for a let-statement by the let-statement expected build.
     *
     * @throws IOException in case of I/O problem
     * */
    public void compileLet() throws IOException {
        writeLine("<letStatement>");

        process("let");
        process(tokenizer.identifier());        // variable name

        // if the variable is an array cell:
        if(tokenizer.currentToken.equals("[")) {
            process("[");
            compileExpression();
            process("]");
        }

        process("=");
        compileExpression();    // compile the expression that should be assigned to the variable
        process(";");

        writeLine("</letStatement>");
    }

    /**
     * This method writes the code for an if-statement by the if-statement expected build.
     *
     * @throws IOException in case of I/O problem
     * */
    public void compileIf() throws IOException {
        writeLine("<ifStatement>");

        process("if");
        process("(");
        compileExpression();    // the if-statement condition
        process(")");
        process("{");
        compileStatements();    // the statements executed if the condition satisfied
        process("}");

        // if there is an 'else' part after the if-statement body:
        if(tokenizer.currentToken.equals("else")) {
            process("else");
            process("{");
            compileStatements();
            process("}");
        }

        writeLine("</ifStatement>");
    }

    /**
     * This method writes the code for a while-statement by the while-statement expected build.
     *
     * @throws IOException in case of I/O problem
     * */
    public void compileWhile() throws IOException {
        // pretty similar to the if-statement build, but without the 'else' part

        writeLine("<whileStatement>");
        process("while");
        process("(");
        compileExpression();
        process(")");
        process("{");
        compileStatements();
        process("}");
        writeLine("</whileStatement>");
    }

    /**
     * This method writes the code for a do-statement by the do-statement expected build.
     *
     * @throws IOException in case of I/O problem
     * */
    public void compileDo() throws IOException {
        writeLine("<doStatement>");
        process("do");
        process(tokenizer.identifier());    // the subroutine name

        // to check if the subroutine name is more complex and to complete the subroutine call:
        compileSubroutineCall();

        process(";");
        writeLine("</doStatement>");
    }

    /**
     * This method writes the code for complex subroutine names and for the rest of the subroutine call.
     *
     * @throws IOException in case of I/O problem
     * */
    public void compileSubroutineCall() throws IOException {
        // checking if the subroutine name is another class subroutine:
        if(tokenizer.currentToken.equals(".")) {
            process(".");
            process(tokenizer.identifier());    // the subroutine name
        }

        // finishing the code for the subroutine call:
        process("(");
        compileExpressionList();
        process(")");
    }

    /**
     * This method writes the code for a return-statement by the return-statement expected build.
     *
     * @throws IOException in case of I/O problem
     * */
    public void compileReturn() throws IOException {
        writeLine("<returnStatement>");
        process("return");

        // if there is a value returned:
        if(!tokenizer.currentToken.equals(";")) {
            compileExpression();    // the value that returns
        }

        process(";");
        writeLine("</returnStatement>");
    }

    /**
     * This method writes the code for an expression by the expression expected build.
     *
     * @throws IOException in case of I/O problem
     * */
    public void compileExpression() throws IOException {
        writeLine("<expression>");

        // the build is: term (op term)*, so we first compile the first term:
        compileTerm();

        // while there is more (op term) couples:
        while(checkExpressionSymbol(tokenizer.symbol())) {
            process(tokenizer.currentToken);    // the op
            compileTerm();
        }
        writeLine("</expression>");
    }

    /**
     * This method checks whether a symbol is an operation symbol.
     *
     * @param c is a character symbol
     * @return true if c is an operation symbol, false otherwise
     * */
    public boolean checkExpressionSymbol(char c) {
        return (c == '+' || c == '-' || c == '*' || c == '/' || c == '&' || c == '|'
                || c == '>' || c == '<' || c == '=');
    }

    /**
     * This method writes the code for a term by the term expected build.
     *
     * @throws IOException in case of I/O problem
     * */
    public void compileTerm() throws IOException {
        writeLine("<term>");

        // dividing for several cases:
        if(tokenizer.tokenType().equals("KEYWORD")) {
            // key word can be a term only if it is one of those:
            if(tokenizer.keyWord().equals("THIS") || tokenizer.keyWord().equals("NULL")
                || tokenizer.keyWord().equals("TRUE") || tokenizer.keyWord().equals("FALSE")) {
                process(tokenizer.keyWord().toLowerCase()); // so process will be able to handle it
            }
        } else if(tokenizer.tokenType().equals("SYMBOL")) {
            // if it's a symbol we also divide to several cases:
            if(tokenizer.symbol() == '(') {
                // an expression:
                process("(");
                compileExpression();
                process(")");
            } else if(tokenizer.symbol() == '-' || tokenizer.symbol() == '~') {
                // a unary term:
                process(tokenizer.currentToken);
                compileTerm();
            }
        } else if(tokenizer.tokenType().equals("STRING_CONST")) {
            // if it's a string constant - just write it as it is:
            process(tokenizer.stringVal());
        } else if(tokenizer.tokenType().equals("INT_CONST")) {
            // if it's an integer constant - just write it as it is:
            process(tokenizer.intVal() + "");
        } else if(tokenizer.tokenType().equals("IDENTIFIER")) {
            process(tokenizer.identifier());

            // handle cases where the identifier is a subroutine call or an array cell:
            if(tokenizer.currentToken.equals("[")) {
                process("[");
                compileExpression();
                process("]");
            } else if(tokenizer.currentToken.equals(".") || tokenizer.currentToken.equals("(")) {
                compileSubroutineCall();
            }
        }

        writeLine("</term>");
    }

    /**
     * This method writes the code for a list of expressions.
     *
     * @throws IOException in case of I/O problem
     * */
    public void compileExpressionList() throws IOException {
        writeLine("<expressionList>");

        // if the next token is ')' then there are no expressions. otherwise:
        if(!tokenizer.currentToken.equals(")")) {
            compileExpression();

            // while there are more expressions:
            while(tokenizer.currentToken.equals(",")) {
                process(",");
                compileExpression();
            }
        }

        writeLine("</expressionList>");
    }

    /**
     * This method writes the code of an expected string with it's xml tags.
     * For example: process("{") should write <symbol> { </symbol>.
     * This method does most of the writing work.
     *
     * @param str is a string that will be processed
     * @throws IOException in case of I/O problem
     * */
    public void process(String str) throws IOException {
        // self-debugging case:
        if(str == null) {
            System.out.println("process got null");
            writeLine("NULL");
            return;
        }

        // if str is not the current token expected - it's a syntax error.
        // otherwise we write the proper tags as needed:
        if(str.equals(tokenizer.currentToken) || str.equals(tokenizer.stringVal())) {
            if(tokenizer.tokenType().equals("KEYWORD")) {
                writeTagLine(str, "keyword");
            } else if (tokenizer.tokenType().equals("SYMBOL")) {
                // for the symbols (>, <, &) we have special behaviour:
                if(str.equals("<")) str = "&lt;";
                else if(str.equals(">")) str = "&gt;";
                else if(str.equals("&")) str = "&amp;";
                writeTagLine(str, "symbol");
            } else if (tokenizer.tokenType().equals("IDENTIFIER")) {
                writeTagLine(str, "identifier");
            } else if (tokenizer.tokenType().equals("INT_CONST")) {
                writeTagLine(str, "integerConstant");
            } else if (tokenizer.tokenType().equals("STRING_CONST")) {
                writeTagLine(str, "stringConstant");
            }

            // advancing to update the current token:
            tokenizer.advance();

        } else {
            // for self-debugging:
            writeLine("------------------------------");
            writeLine("SYNTAX ERROR - process");
            writeLine("STR: " + str);
            writeLine("CURRENT TOKEN: " + tokenizer.currentToken);
            writeLine("TOKEN TYPE: " + tokenizer.tokenType());
            writeLine("------------------------------");
        }
    }

    /**
     * This method writes the tag of a token with it's content inside.
     *
     * @param token is the content of the token
     * @param tag is the tagName written as <tagName></tagName>
     * @throws IOException in case of I/O problem
     * */
    public void writeTagLine(String token, String tag) throws IOException {
        writeLine("<" + tag + "> " + token + " </" + tag + ">");
    }

    /**
     * This method writes a string and then a new line (same as '\n').
     *
     * @param str is the string to be written
     * @throws IOException in case of I/O problem
     * */
    public void writeLine(String str) throws IOException {
        writer.write(str);
        writer.newLine();
    }

    /**
     * This method closes the BufferedWriter.
     *
     * @throws IOException in case of I/O problem
     * */
    public void close() throws IOException {
        writer.close();
    }
}
