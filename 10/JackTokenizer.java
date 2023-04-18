/**
 * This class parses a .jack file into tokens.
 *
 * @author OmerCaplan
 * @author YogevCuperman
 *
* */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class JackTokenizer {

    /* FIELDS: */

    public BufferedReader reader;
    public String currentLine;      // current line of code read from the file
    public String[] currentWords;   // the currentLine parsed to words or in-line strings
    public int wordIndex;           // the index of the current word we parse in currentWords
    public List<String> wordTokens; // list of tokens we parse from the current word (currentWords[wordIndex])
    public int tokenIndex;          // the index of the current token we parse in wordTokens
    public String currentToken;     // the current token we parse
    public String nextToken;        // to check if there are more tokens
    public HashSet<Character> symbols;  // contains all the special symbols
    public HashSet<String> keywords;    // contains all the special keywords


    /**
     * Constructor - initializes the tokenizer.
     * Does not advance - meaning that the currentToken is null after this constructor call.
     *
     * @param file is a file holds the Jack code
     * @throws IOException in case of I/O problem
     * */
    public JackTokenizer(File file) throws IOException {
        reader = new BufferedReader(new FileReader(file));
        initializeSets();
        currentLine = readNextLine();
        currentWords = splitWordsAndStrings(currentLine);
        wordIndex = 0;
        wordTokens = parseTokens(currentWords[wordIndex]);
        tokenIndex = 0;
        currentToken = null;
        nextToken = wordTokens.get(0);
    }

    /**
     * This method reads the next non-comment and non-empty line of the input file.
     *
     * @return a String representing the next non-comment line with whitespaces ignored
     * @throws IOException in case of I/O problem
     * */
    public String readNextLine() throws IOException {
        String line = reader.readLine();
        if(line == null) return null;       // if it's the end of file
        line = line.trim();                 // cutting wrapping whitespaces

        // ignoring:
        if(line.startsWith("//")                // full line comments
                || line.isEmpty()               // all-whitespaces lines
                || line.startsWith("/*") || line.startsWith("*") || line.startsWith("*/")) { // multi line comments
            return readNextLine();
        }

        // if it's in-line comment - we need only the preceeding part:
        if(line.contains("//")) line = line.split("//")[0].trim();

        return line;
    }

    /**
     * This method splits a whitespaces ignored lines into an array.
     * Every String part (which is inside double quotes) will be together in the same cell in the array.
     * All the others parts will be split by spaces.
     * For example, if the line is: The dog barked and his owner said "good boy"
     * The resulting array will be: ['The', 'dog', 'barked', 'and', 'his', 'owner', 'said', '"good boy"']
     *
     * @param line is a whitespaces-ignored line
     * @return a String array representing the line split by strings and words
     * */
    public String[] splitWordsAndStrings(String line) {
        line = line.trim();
        String[] splitted = line.split("\"");           // split by quotes
        List<String> list = new ArrayList<>();          // will store the cells of the future array

        // iterating on inside-quotes parts and outside-quotes parts:
        for(int i = 0; i < splitted.length; i++) {
            if(i % 2 == 0) {    // outside-quotes

                // ignoring multi-spaces between the words:
                splitted[i] = splitted[i].replaceAll("( )+", " ");

                // split by spaces and then add the words to the list:
                for(String s: splitted[i].split(" ")) {
                    list.add(s);
                }

            } else {    // inside-quotes

                // add the string wrapped with quotes signs
                list.add("\"" + splitted[i] + "\"");
            }
        }

        // copying the list items into array
        String[] answer = new String[list.size()];
        for(int j = 0; j < list.size(); j++) {
            answer[j] = list.get(j);
        }
        return answer;
    }

    /**
     * This method splits a word into the different tokens it holds.
     * For example: the word <code>x=7;</code>
     * will split into the following tokens: ['x', '=', '7', ';'].
     *
     * @param currentWord is the word that will be parsed into tokens
     * @return a String list contains the different tokens
     * */
    public List<String> parseTokens(String currentWord) {
        int start = 0;      // the index of the start of the next token
        List<String> tokens = new ArrayList<>();

        // the main idea is that we keep iterating over the characters of the word until we
        // get a symbol, and at this point we add as token all the previous characters, and
        // separately the symbol itself
        for(int i = 0; i < currentWord.length(); i++) {
            char c = currentWord.charAt(i);
            if(inSymbols(c)) {
                if(start < i) tokens.add(currentWord.substring(start, i));
                tokens.add(c + "");
                start = i + 1;
            }
        }

        // handling cases where there were no symbols or where the last character is a symbol
        if(start < currentWord.length()) tokens.add(currentWord.substring(start));
        return tokens;
    }

    /**
     * This method checks whether a character is in the symbols set.
     *
     * @param c is a character
     * @return true if c is in the symbols set, false otherwise
     * */
    public boolean inSymbols(char c) {
        for(char current: symbols) {
            if(current == c) return true;
        }
        return false;
    }

    /**
     * This method initializes the symbols and keywords sets.
     * Will be called only once, when the object is constructed.
     * */
    public void initializeSets() {
        // adding the relevant items to the sets by splitting a long string rather than add each item individually:

        keywords = new HashSet<>();
        String[] words = ("class,constructor,function,method,field,static,var,int,char,boolean,void," +
                "true,false,null,this,let,do,if,else,while,return").split(",");
        for(String s: words) {
            keywords.add(s);
        }

        symbols = new HashSet<>();
        String symbolsStr = "{}()[].,;+-*/&|><=~";
        for(int i = 0; i < symbolsStr.length(); i++) {
            symbols.add(symbolsStr.charAt(i));
        }
    }

    /**
     * This method checks whether the file has more tokens inside, by checking the next token that
     * being parsed along the advance method.
     *
     * @return true if the file has more tokens in it, false otherwise
     * */
    public boolean hasMoreTokens() {
        return nextToken != null;
    }

    /**
     * This method updates the currentToken and the nextToken fields, as it reads along the file.
     *
     * @throws IOException in case of I/O problem
     * */
    public void advance() throws IOException {
        if(hasMoreTokens()) {       // otherwise it's the end of the file

            // updating the currentToken:
            currentToken = nextToken;

            // updating the nextToken, several cases:
            if(tokenIndex < wordTokens.size() - 1) {
                // there are more tokens in the current word:
                tokenIndex++;
                nextToken = wordTokens.get(tokenIndex);
            } else if(wordIndex < currentWords.length - 1) {
                // this is the last token in the word, so we need to also parse the next word as
                // long as there is more words in the line:
                wordIndex++;
                wordTokens = parseTokens(currentWords[wordIndex]);
                nextToken = wordTokens.get(0);
                tokenIndex = 0;
            } else {
                // this is the last word in the line, so we try to read and parse the next line:
                currentLine = readNextLine();
                if(currentLine == null) return;     // EOF
                currentWords = splitWordsAndStrings(currentLine);
                wordIndex = 0;
                wordTokens = parseTokens(currentWords[wordIndex]);
                nextToken = wordTokens.get(0);
                tokenIndex = 0;
            }
        }
    }

    /**
     * This method checks the type of the currentToken.
     *
     * @return a String representing the type of the current token in capital letters
     * */
    public String tokenType() {
        // if it is a keyword or symbol it must be in the sets we initialized:
        if(keywords.contains(currentToken)) return "KEYWORD";
        else if (inSymbols(currentToken.charAt(0))) return "SYMBOL";

        // if it contains only digits - it's an int constant:
        else if (currentToken.matches("^[0-9]+$")) return "INT_CONST";

        // if it has quote-sign - it's a string constant, otherwise it's an identifier
        else if (currentToken.contains("\"")) return "STRING_CONST";
        else return "IDENTIFIER";
    }

    /**
     * This method returns the current keyword token in capital letters.
     * Should be called only if the current token type is keyword.
     *
     * @return a String representing the current keyword token in capital letters
     * */
    public String keyWord() {
        if (currentToken == null) return "";
        return currentToken.toUpperCase();
    }

    /**
     * This method returns the current symbol token.
     * Should be called only if the current token type is symbol.
     *
     * @return a char representing the current symbol token
     * */
    public char symbol() {
        return currentToken.charAt(0);
    }

    /**
     * This method returns the current identifier token.
     * Should be called only if the current token type is identifier.
     *
     * @return a String representing the current identifier token
     * */
    public String identifier() {
        return currentToken;
    }

    /**
     * This method returns the current integer constant token as a number.
     * Should be called only if the current token type is integer constant.
     *
     * @return an int representing the current integer constant token as a number
     * */
    public int intVal() {
        return Integer.parseInt(currentToken);
    }

    /**
     * This method returns the current string constant token.
     * Should be called only if the current token type is string constant.
     *
     * @return a String representing the current string constant token without the quote signs
     * */
    public String stringVal() {
        return currentToken.substring(1, currentToken.length() - 1);
    }

}

