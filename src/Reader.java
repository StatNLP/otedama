import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

//import Rule;
//import Tree;

/**
 * A file reader.
 * 
 *
 */
public class Reader {

    private BufferedReader br;

    public Reader(){
         br = null;
    }

    /**
     * Constructor
     * 
     * @param file
     */
    public Reader(String file) {
	try {
	    br = new BufferedReader(new InputStreamReader(new FileInputStream(
		    file), "UTF8"));
	} catch (Exception e) {
	    e.getStackTrace();
	}
    }

    /**
     * read line
     * 
     * @return String
     */
    public String getNext() {
	try {
	    String line = br.readLine();
	    if (line == null) {
		br.close();
		return null;
	    }
	    return line;
	} catch (IOException e) {
	    e.getStackTrace();
	}
	return null;
    }
	
    /**
     * read .align file
     * 
     * @return String[] alignment
     */
    public String[] readNextAlign() {
	try {
	    String line = br.readLine();
	    if (line == null) {
		br.close();
		return null;
	    }
	    return line.split(" ");
	} catch (IOException e) {
	    e.getStackTrace();
	}
	return null;
    }

    /**
     * Read .trees file
     * 
     * @return Tree
     */
    public Tree readNextTree() {
	try {
	    String line = br.readLine();
	    if (line == null) {
		br.close();
		return null;
	    }
	    Tree t = new Tree();
	    return t.fromTreeFormat(line);
	} catch (IOException e) {
	    e.getStackTrace();
	}
	return null;
    }

    /**
     * Read base.rules
     * 
     * @return Rule
     */
    public Rule readNextRule() {
	try {
	    String line = br.readLine();
	    System.out.println(line);
	    if (line == null) {
		br.close();
		return null;
	    }
	    return new Rule(line);
	} catch (IOException e) {
	    e.getStackTrace();
	}
	return null;
    }

    /**
     * read FINAL_RULEBASE.txt
     * 
     * @return Rule
     */
    public Rule readNextRuleFromRuleFormat() {
	try {
	    String line = br.readLine();
	    if (line == null) {
		br.close();
		return null;
	    }
	    Rule r = new Rule();
	    return r.fromRuleFormatAndScores(line);
	} catch (IOException e) {
	    e.getStackTrace();
	}
	return null;
    }

}
