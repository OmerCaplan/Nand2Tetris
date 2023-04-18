/**
 * This class is the main class handles the compilation and translation of the Jack code file.
 *
 * @author OmerCaplan
 * @author YogevCuperman
 *
 * */

import java.io.File;
import java.io.IOException;

public class JackAnalyzer {

    public static void main(String[] args) throws IOException {
        // setting input file and declaring output file and compiler:
        String inputPath = args[0];
        File inputFile = new File(inputPath);
        String outputPath ;
        File outputFile;
        CompilationEngine compiler;

        // checking if the input file is a directory:
        if (inputFile.isDirectory()) {
            for (File f : inputFile.listFiles()){
                // handling every relevant file in the directory:
                if (f.getName().contains(".jack")) {
                    // setting the output file details:
                    outputPath = f.getAbsolutePath().replace(".jack", ".xml");
                    outputFile = new File(outputPath);

                    // a new compiler for each file:
                    compiler = new CompilationEngine(f, outputFile);
                    compiler.compileClass();    // start compiling
                    compiler.close();           // closing the current file
                }
            }
        } else {    // it is a single file:
            // setting the output file details:
            outputPath = inputPath.replace(".jack", ".xml");
            outputFile = new File(outputPath);

            // setting the compiler:
            compiler = new CompilationEngine(inputFile, outputFile);
            compiler.compileClass();        // start compiling
            compiler.close();               // closing the file
        }

    }
}

