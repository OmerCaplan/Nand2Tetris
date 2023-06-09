import java.util.TreeMap;

public class Code {

    /** creating 3 different maps because there are some common keys which should get other values
    based on their meaning (if it is a dest or comp etc.) **/

    static TreeMap<String, String> destMap;
    static TreeMap<String, String> compMap;
    static TreeMap<String, String> jumpMap;

    public Code() {
        // setting the "pre-processed" keys and values:

        destMap = new TreeMap<>();
        compMap = new TreeMap<>();
        jumpMap = new TreeMap<>();

        compMap.put("0", "0101010");
        compMap.put("1", "0111111");
        compMap.put("-1", "0111010");
        compMap.put("D", "0001100");
        compMap.put("A", "0110000");
        compMap.put("!D", "0001101");
        compMap.put("!A", "0110001");
        compMap.put("-D", "0001111");
        compMap.put("-A", "0110011");
        compMap.put("D+1", "0011111");
        compMap.put("A+1", "0110111");
        compMap.put("D-1", "0001110");
        compMap.put("A-1", "0110010");
        compMap.put("D+A", "0000010");
        compMap.put("D-A", "0010011");
        compMap.put("A-D", "0000111");
        compMap.put("D&A", "0000000");
        compMap.put("D|A", "0010101");
        compMap.put("M", "1110000");
        compMap.put("!M", "1110001");
        compMap.put("-M", "1110011");
        compMap.put("M+1", "1110111");
        compMap.put("M-1", "1110010");
        compMap.put("D+M", "1000010");
        compMap.put("D-M", "1010011");
        compMap.put("M-D", "1000111");
        compMap.put("D&M", "1000000");
        compMap.put("D|M", "1010101");

        destMap.put("M", "001");
        destMap.put("D", "010");
        destMap.put("DM", "011");
        destMap.put("MD", "011");
        destMap.put("A", "100");
        destMap.put("AM", "101");
        destMap.put("AD", "110");
        destMap.put("ADM", "111");

        jumpMap.put("JGT", "001");
        jumpMap.put("JEQ", "010");
        jumpMap.put("JGE", "011");
        jumpMap.put("JLT", "100");
        jumpMap.put("JNE", "101");
        jumpMap.put("JLE", "110");
        jumpMap.put("JMP", "111");
    }

    public String dest(String d) {
        if(d == null) return "000";         // if there is no dest we return 000 to the dest field
        return destMap.get(d);
    }

    public String comp(String c) {
        return compMap.get(c);
    }

    public String jump(String j) {
        if(j == null) return "000";         // if there is no jump we return 000 to the jump field
        return jumpMap.get(j);
    }
}
