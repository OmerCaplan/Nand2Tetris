/**
 * This class represents the information for each symbol stored in the SymbolTable.
 *
 * @author OmerCaplan
 * @author YogevCuperman
 *
 * */

public class Symbol {

    /* FIELDS: */

    private String type;
    private String kind;
    private int index;


    /**
     * Constructor - initializes the tokenizer.
     * Does not advance - meaning that the currentToken is null after this constructor call.
     *
     * @param type is the variable type (int / char/ boolean / class-identifier)
     * @param kind is the variable kind (field / static / argument / var (=local))
     * @param index is an integer greater or equal to 0
     * */
    public Symbol(String type, String kind, int index) {
        this.type = type;
        this.kind = kind;
        this.index = index;
    }

    /**
     * Getter method for type field.
     *
     * @return type field as String
     */
    public String getType() {
        return type;
    }

    /**
     * Getter method for kind field.
     *
     * @return kind field as String
     */
    public String getKind() {
        return kind;
    }

    /**
     * Getter method for index field.
     *
     * @return index field as integer
     */
    public int getIndex() {
        return index;
    }
}
