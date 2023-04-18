/**
 * This class represents symbol-table of a class and all it's inner subroutines.
 *
 * @author OmerCaplan
 * @author YogevCuperman
 *
 * */

import java.util.Hashtable;

public class SymbolTable {

    /* FIELDS: */
    private int fieldIndex = 0;
    private int staticIndex = 0;
    private int argIndex = 0;
    private int varIndex = 0;
    public Hashtable<String, Symbol> classTable;
    public Hashtable<String, Symbol> subroutineTable;

    /**
     * Constructor method.
     * Basic initializations.
     */
    public SymbolTable() {
        classTable = new Hashtable<>();
        subroutineTable = new Hashtable<>();
    }

    /**
     * This method resets the subroutineTable index counters and clears the subroutineTable.
     */
    public void reset() {
        subroutineTable.clear();
        argIndex = 0;
        varIndex = 0;
    }

    /**
     * This method adds an entry to the relevant table.
     *
     * @param name is the name of the variable
     * @param type is one of the following: (int, char, boolean, class-identifier)
     * @param kind is one of the following: (field, static, argument, var (=local))
     */
    public void define(String name, String type, String kind) {
        // specific table and specific index for each kind:
        if(kind.equals("field")) {
            classTable.put(name, new Symbol(type, kind, fieldIndex++));
        } else if(kind.equals("static")) {
            classTable.put(name, new Symbol(type, kind, staticIndex++));
        } else if(kind.equals("argument")) {
            subroutineTable.put(name, new Symbol(type, kind, argIndex++));
        } else if(kind.equals("var")) {
            subroutineTable.put(name, new Symbol(type, kind, varIndex++));
        }
    }

    /**
     * This method counts how many variables of a specific kind there are in the tables.
     *
     * @param kind is one of the following: (field, static, argument, var (=local))
     * @return an integer greater or equal to 0, or -1 if kind is invalid
     */
    public int varCount(String kind) {
        if(kind.equals("field")) return fieldIndex;
        if(kind.equals("static")) return staticIndex;
        if(kind.equals("argument")) return argIndex;
        if(kind.equals("var")) return varIndex;
        return -1;  // invalid kind
    }

    /**
     * This method returns the kind of specific variable by its name.
     *
     * @param name is the variable name
     * @return the kind of the variable as a string, or "NONE" if there is no variable in the table with such name
     */
    public String kindOf(String name) {
        // looking first in the subroutine table and then in the class table:
        if(subroutineTable.containsKey(name)) return subroutineTable.get(name).getKind();
        if(classTable.containsKey(name)) return classTable.get(name).getKind();
        return "NONE";
    }

    /**
     * This method returns the type of specific variable by its name.
     *
     * @param name is the variable name
     * @return the type of the variable as a string, or "NO SUCH VARIABLE if there is no variable
     *          in the table with such name
     */
    public String typeOf(String name) {
        // looking first in the subroutine table and then in the class table:
        if(subroutineTable.containsKey(name)) return subroutineTable.get(name).getType();
        if(classTable.containsKey(name)) return classTable.get(name).getType();
        return "NO SUCH VARIABLE";
    }

    /**
     * This method returns the index of specific variable by its name.
     *
     * @param name is the variable name
     * @return the index of the variable as an integer (greater or equal to 0), or -1 if there is no variable
     *          in the table with such name
     */
    public int indexOf(String name) {
        if(subroutineTable.containsKey(name)) return subroutineTable.get(name).getIndex();
        if(classTable.containsKey(name)) return classTable.get(name).getIndex();
        return -1;
    }
}
