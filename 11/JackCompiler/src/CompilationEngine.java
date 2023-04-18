/**
 * This class writes an .vm file by the tokens parsed from JackTokenizer class.
 *
 * @author OmerCaplan
 * @author YogevCuperman
 *
 * */

import java.io.File;
import java.io.IOException;

public class CompilationEngine {

    /* FIELDS: */

    private final JackTokenizer tokenizer;    // parses the tokens
    private final SymbolTable table;          // the symbol-table
    private final VMWriter writer;            // writes the VM commands

    // global variables for shorter symbol-adding:
    private String className;
    private String currentName;
    private String currentKind;
    private String currentType;

    // counters for procedural code (while / if):
    private int whileCounter;
    private int ifCounter;

    /**
     * Constructor - initializes the compilation engine.
     * Initializes a tokenizer, writer and symbol-table and getting ready to write the file.
     *
     * @param input is a file holds the Jack code
     * @param output is a file that the .vm code will be written to
     * @throws IOException in case of I/O problem
     * */
    public CompilationEngine (File input, File output) throws IOException {
        this.tokenizer = new JackTokenizer(input);
        tokenizer.advance();
        this.writer = new VMWriter(output);
        this.table = new SymbolTable();
        whileCounter = 0;
        ifCounter = 0;
    }

    /**
     * This method adds an entry to the symbol-table, based on global variables that updates
     * throw this class methods.
     */
    private void addToSymbolTable() {
        table.define(currentName, currentType, currentKind);
    }

    /**
     * This method writes the code for a class by the class expected build.
     *
     * @throws IOException in case of I/O problem
     * */
    public void compileClass() throws IOException {
        tokenizer.advance();    // skipping "class"
        currentName = tokenizer.identifier();
        className = tokenizer.identifier();
        tokenizer.advance();    // skipping the class name
        tokenizer.advance();    // skipping "{"

        // class variables:
        while(tokenizer.currentToken.equals("static") || tokenizer.currentToken.equals("field")) {
            compileClassVarDec();
            tokenizer.advance();
        }

        // subroutines:
        while(tokenizer.currentToken.equals("constructor") ||
            tokenizer.currentToken.equals("function") ||
            tokenizer.currentToken.equals("method")) {
            compileSubroutine();
            tokenizer.advance();
        }
    }

    /**
     * This method writes the code for a class variable declaration by the declaration expected build.
     *
     * @throws IOException in case of I/O problem
     * */
    public void compileClassVarDec() throws IOException {
        currentKind = tokenizer.keyWord().toLowerCase();    // field or static
        tokenizer.advance();    // skipping the variable kind
        checkType();
        tokenizer.advance();    // skipping the variable type
        currentName = tokenizer.identifier();

        // adding the variable to the symbol table
        addToSymbolTable();

        tokenizer.advance();    // skipping the variable name

        // in case there are more variables of the same kind and type:
        while(tokenizer.symbol() == ',') {
            tokenizer.advance();    // skipping ","
            currentName = tokenizer.identifier();

            // adding the variable to the symbol table
            addToSymbolTable();
        }

        tokenizer.advance();        // currentToken is now ";"
    }

    /**
     * This method writes the code for a subroutine by the subroutine expected build.
     *
     * @throws IOException in case of I/O problem
     * */
    public void compileSubroutine() throws IOException {
        table.reset();  // new subroutine - fresh inner symbol table
        String subroutineKind = tokenizer.keyWord();
        tokenizer.advance();    // skipping the constructor/function/method token
        checkType();
        tokenizer.advance();    // skipping the type
        currentName = tokenizer.identifier();
        String currentSubroutineName = className + "." + currentName;

        // methods need "this" variable:
        if(subroutineKind.equals("METHOD")) {
            table.define("this", className, "argument");
        }

        tokenizer.advance();    // skipping the subroutine name
        compileParameterList();
        tokenizer.advance();    // skipping ")"
        tokenizer.advance();    // skipping "{"

        // subroutine local variables:
        while(tokenizer.currentToken.equals("var")) {
            compileVarDec();
            tokenizer.advance();    // skipping ";" after each variable declaration
        }

        // writing the function method with the required local variables:
        writer.writeFunction(currentSubroutineName, table.varCount("var"));

        if(subroutineKind.equals("CONSTRUCTOR")) {
            // special requirements for constructors - sequential memory spaces for fields
            int nFields = table.varCount("field");
            if(nFields > 0) {
                writer.writePush("constant", nFields);
            }
            writer.writeCall("Memory.alloc", 1);
            writer.writePop("pointer", 0);  // pointer 0 holds "this" address
        } else if(subroutineKind.equals("METHOD")) {
            // special requirements for methods - setting pointer 0 (this) to argument 0
            writer.writePush("argument", 0);
            writer.writePop("pointer", 0);
        }

        // handling subroutine statements:
        compileStatements();
    }

    /**
     * This method sets the currentType global variables to be the currentToken as long as it is a type
     * name (int / char / boolean / class-identifier).
     */
    public void checkType() {
        if(tokenizer.currentToken.equals("int") ||
                tokenizer.currentToken.equals("char") ||
                tokenizer.currentToken.equals("boolean") ||
                tokenizer.currentToken.equals("void") ||
                tokenizer.tokenType().equals("IDENTIFIER")) {
            currentType = tokenizer.currentToken;
        }
    }

    /**
     * This method writes the code for a parameter list by the parameter list expected build.
     *
     * @throws IOException in case of I/O problem
     * */
    public void compileParameterList() throws IOException {
        tokenizer.advance();    // skipping "("

        if(!tokenizer.currentToken.equals(")")) {   // otherwise it's an empty parameter list
            currentKind = "argument";       // every parameter is an argument

            checkType();
            tokenizer.advance();    // skipping the type
            currentName = tokenizer.identifier();
            addToSymbolTable();
            tokenizer.advance();    // skipping the parameter name

            // in case there are more parameters:
            while (tokenizer.currentToken.equals(",")) {
                tokenizer.advance();    // skipping ","
                checkType();
                tokenizer.advance();    // skipping the type
                currentName = tokenizer.identifier();
                addToSymbolTable();
                tokenizer.advance();    // skipping the parameter name
            }
        }
    }

    /**
     * This method writes the code for a variable declaration by the variable declaration expected build.
     *
     * @throws IOException in case of I/O problem
     * */
    public void compileVarDec() throws IOException {
        currentKind = "var";        // all variables here are local
        tokenizer.advance();        // skipping "var"
        checkType();
        tokenizer.advance();        // skipping the variable type
        currentName = tokenizer.identifier();
        addToSymbolTable();
        tokenizer.advance();        // skipping the variable name

        // in case there are more variables of the same type:
        while(tokenizer.symbol() == ',') {
            tokenizer.advance();    // skipping ","
            currentName = tokenizer.identifier();
            addToSymbolTable();
            tokenizer.advance();    // skipping the variable name
        }
    }

    /**
     * This method writes the code for a statements part.
     *
     * @throws IOException in case of I/O problem
     * */
    public void compileStatements() throws IOException {
        // while the next token is the begging of a statement - compile it:
        while (tokenizer.tokenType().equals("KEYWORD")) {
            if(tokenizer.keyWord().equals("LET")) {
                compileLet();
                tokenizer.advance();
            } else if(tokenizer.keyWord().equals("IF")) {
                compileIf();
                // compileIf() advances itself to check for "else" part
            } else if(tokenizer.keyWord().equals("WHILE")) {
                compileWhile();
                tokenizer.advance();
            } else if(tokenizer.keyWord().equals("DO")) {
                compileDo();
                tokenizer.advance();
            } else if(tokenizer.keyWord().equals("RETURN")) {
                compileReturn();
                tokenizer.advance();
            } else break; // no more statements
        }
    }

    /**
     * This method writes the code for a let-statement by the let-statement expected build.
     *
     * @throws IOException in case of I/O problem
     * */
    public void compileLet() throws IOException {
        tokenizer.advance();    // skipping "let"
        currentName = tokenizer.identifier();
        String varName = currentName;
        tokenizer.advance();    // skipping variable name
        boolean arrayCheck = false;

        // if the let-statements sets an array cell:
        if(tokenizer.currentToken.equals("[")) {
            tokenizer.advance();    // skipping "["
            compileExpression();    // pushes the offset
            tokenizer.advance();    // skipping "]"
            writer.writePush(table.kindOf(varName), table.indexOf(varName));    // pushes the array's name
            writer.writeArithmetic("add");
            writer.writePop("temp", 2); // putting (address + offset) into temp 2
            arrayCheck = true;
        }
        tokenizer.advance();    // skipping "="
        compileExpression();

        // more requirements when manipulating array:
        if(arrayCheck) {
            writer.writePush("temp", 2);
            writer.writePop("pointer", 1);
            writer.writePop("that", 0);
        } else writer.writePop(table.kindOf(varName), table.indexOf(varName));
    }

    /**
     * This method writes the code for an if-statement by the if-statement expected build.
     *
     * @throws IOException in case of I/O problem
     * */
    public void compileIf() throws IOException {
        int ifRoutineCounter = ifCounter;   // unique label names
        ifCounter++;
        tokenizer.advance();    // skipping "if"
        tokenizer.advance();    // skipping "("
        compileExpression();
        writer.writeArithmetic("not");  // checking if the condition doesn't apply
        tokenizer.advance();    // skipping ")"
        writer.writeIf("IF_FALSE" + ifRoutineCounter);
        tokenizer.advance();    // skipping "{"
        compileStatements();
        writer.writeGoto("IF_TRUE" + ifRoutineCounter);
        writer.writeLabel("IF_FALSE" + ifRoutineCounter);
        tokenizer.advance();    // skipping "}"
        if(tokenizer.currentToken.equals("else")) {
            tokenizer.advance();    // skipping "else"
            tokenizer.advance();    // skipping "{"
            compileStatements();
            tokenizer.advance();    // skipping "}"
        }
        writer.writeLabel("IF_TRUE" + ifRoutineCounter);
    }

    /**
     * This method writes the code for a while-statement by the while-statement expected build.
     *
     * @throws IOException in case of I/O problem
     * */
    public void compileWhile() throws IOException {
        int whileRoutineCounter = whileCounter;     // unique label names
        whileCounter++;
        writer.writeLabel("WHILE_LOOP" + whileRoutineCounter);
        tokenizer.advance();    // skipping "while"
        tokenizer.advance();    // skipping "("
        compileExpression();
        writer.writeArithmetic("not");  // checking if the condition doesn't apply
        tokenizer.advance();    // skipping ")"
        writer.writeIf("WHILE_END" + whileRoutineCounter);
        tokenizer.advance();    // skipping "{"
        compileStatements();
        writer.writeGoto("WHILE_LOOP" + whileRoutineCounter);
        writer.writeLabel("WHILE_END" + whileRoutineCounter);
    }

    /**
     * This method writes the code for a do-statement by the do-statement expected build.
     *
     * @throws IOException in case of I/O problem
     * */
    public void compileDo() throws IOException {
        tokenizer.advance();    // skipping "do"
        if(tokenizer.tokenType().equals("IDENTIFIER")) {
            currentName = tokenizer.identifier();
            tokenizer.advance();    // skipping the identifier itself

            // for different cases where the identifier is the subroutine itself or the class of the subroutine:
            if(tokenizer.currentToken.equals("(") || tokenizer.currentToken.equals(".")) {
                compileSubroutineCall(currentName);
            }
        }
        tokenizer.advance();    // skipping ")"

        // "dumping" the returned value:
        writer.writePop("temp", 0);
    }

    /**
     * This method writes the code for complex subroutine names and for the rest of the subroutine call.
     *
     * @param iden is the identifier that compileDo() sent
     * @throws IOException in case of I/O problem
     * */
    public void compileSubroutineCall(String iden) throws IOException {
        // boolean variable to check if the identifier is a class or subroutine:
        boolean classIdentifier = iden.matches("^[A-Z].*");
        int nArgs = 0;
        String subroutineName = null;
        if(tokenizer.currentToken.equals("(")) {    // self-class method
            tokenizer.advance();    // skipping "("
            subroutineName = className + "." + iden;
            writer.writePush("pointer", 0);
            nArgs = compileExpressionList(classIdentifier);
        } else {
            tokenizer.advance();
            if(tokenizer.tokenType().equals("IDENTIFIER")) {
                currentName = tokenizer.identifier();
                if (classIdentifier) {   // class function
                    subroutineName = iden + "." + currentName;
                } else {        // field or static object's method
                    subroutineName = table.typeOf(iden) + "." + currentName;
                    writer.writePush(table.kindOf(iden), table.indexOf(iden));
                }
                tokenizer.advance();    // skipping the identifier
                tokenizer.advance();    // skipping "("
                nArgs = compileExpressionList(classIdentifier);
            }
        }
        if (subroutineName != null) writer.writeCall(subroutineName, nArgs);
    }

    /**
     * This method writes the code for a return-statement by the return-statement expected build.
     *
     * @throws IOException in case of I/O problem
     * */
    public void compileReturn() throws IOException {
        tokenizer.advance();    // skipping "return"

        // checking if its "return;" or "return <expression>;":
        if(!tokenizer.currentToken.equals(";")) compileExpression();
        else writer.writePush("constant", 0);

        writer.writeReturn();
    }

    /**
     * This method writes the code for an expression by the expression expected build.
     *
     * @throws IOException in case of I/O problem
     * */
    public void compileExpression() throws IOException {
        compileTerm();

        // creating the "stack behaviour":
        while(checkExpressionSymbol(tokenizer.symbol())) {
            char c = tokenizer.symbol();
            tokenizer.advance();    // skipping the operation sign
            compileTerm();

            // writing the operation command after compiling the other term:
            if(c == '+') writer.writeArithmetic("add");
            else if(c == '-') writer.writeArithmetic("sub");
            else if(c == '*') writer.writeArithmetic("call Math.multiply 2");
            else if(c == '/') writer.writeArithmetic("call Math.divide 2");
            else if(c == '&') writer.writeArithmetic("and");
            else if(c == '|') writer.writeArithmetic("or");
            else if(c == '<') writer.writeArithmetic("lt");
            else if(c == '>') writer.writeArithmetic("gt");
            else if(c == '=') writer.writeArithmetic("eq");
        }
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

        // distinguishing numerous cases:

        if(tokenizer.tokenType().equals("INT_CONST")) {
            // int constant is a number, so we only push itself:
            writer.writePush("constant", tokenizer.intVal());
            tokenizer.advance();
        } else if(tokenizer.tokenType().equals("STRING_CONST")) {
            // string constant is an array of chars,
            // first we create the array instance, and then we add the chars on by on:
            String str = tokenizer.stringVal();
            writer.writePush("constant", str.length());
            writer.writeCall("String.new", 1);
            for(int i = 0 ; i < str.length(); i++) {
                int charValue = str.charAt(i);
                writer.writePush("constant", charValue);
                writer.writeCall("String.appendChar", 2);   // the string itself and the new character
            }
            tokenizer.advance();
        } else if(tokenizer.currentToken.equals("null") || tokenizer.currentToken.equals("false")) {
            // null and false both mean to push 0:
            writer.writePush("constant", 0);
            tokenizer.advance();
        } else if(tokenizer.currentToken.equals("true")) {
            // true is -1:
            writer.writePush("constant", 1);
            writer.writeArithmetic("neg");
            tokenizer.advance();
        } else if(tokenizer.currentToken.equals("this")) {
            // this means push pointer 0:
            writer.writePush("pointer", 0);
            tokenizer.advance();
        } else if(tokenizer.currentToken.equals("~") || tokenizer.currentToken.equals("-")) {
            // in case of unary terms - we save the operation and then compile the term and push the operation:
            char c = tokenizer.symbol();
            tokenizer.advance();
            compileTerm();
            if(c == '~') writer.writeArithmetic("not");
            else if(c == '-') writer.writeArithmetic("neg");
        } else if(tokenizer.currentToken.equals("(")) {
            // for terms with parenthesis:
            tokenizer.advance(); // skipping "("
            compileExpression();
            tokenizer.advance(); // skipping ")"
        } else if(tokenizer.tokenType().equals("IDENTIFIER")) {
            // if the term is an array manipulation ot subroutine call:
            String iden = tokenizer.identifier();
            tokenizer.advance();    // skipping the identifier
            if(tokenizer.currentToken.equals("[")) {
                // array manipulation, same as done in compileLet():
                writer.writePush(table.kindOf(iden), table.indexOf(iden));
                tokenizer.advance();    // skipping "["
                compileExpression();
                tokenizer.advance();    // skipping "]"
                writer.writeArithmetic("add");
                writer.writePop("pointer", 1);
                writer.writePush("that", 0);
            } else if(tokenizer.currentToken.equals("(") || tokenizer.currentToken.equals(".")) {
                // subroutine call:
                compileSubroutineCall(iden);
                tokenizer.advance();    // skipping ")"
            } else {
                writer.writePush(table.kindOf(iden), table.indexOf(iden));
            }
        }
    }

    /**
     * This method writes the code for a list of expressions.
     *
     * @param classIdentifier tells if the expression list is of a Class.Subroutine(ExpressionList)
     *                        or of a Subroutine(ExpressionList)
     * @return the number of expressions in the subroutine call argument list
     * @throws IOException in case of I/O problem
     * */
    public int compileExpressionList(boolean classIdentifier) throws IOException {
        int args = 0;

        // if it is a self-class subroutine:
        if(!classIdentifier) args++;

        if(!tokenizer.currentToken.equals(")")) {
            compileExpression();
            args++;

            // if there are more expressions:
            while(tokenizer.currentToken.equals(",")) {
                tokenizer.advance();    // skipping ","
                compileExpression();
                args++;
            }
        }
        return args;
    }

    /**
     * This method closes the file it writes to.
     *
     * @throws IOException in case of I/O problem
     */
    public void close() throws IOException {
        writer.close();
    }

}
