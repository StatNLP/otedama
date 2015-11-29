import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

//import Rule;
//import Tree;

/**
 * A writer to create files.
 *
 */
public class Writer {

    private BufferedWriter bw;
    private static final String newLine = System.getProperty("line.separator");

    /**
     * constructor
     * 
     * @param file
     * @param append
     */
    public Writer(String file, boolean append) {
	try {
	    bw = new BufferedWriter(new OutputStreamWriter(
		    new FileOutputStream(
		file, append), "UTF8"));
	} catch (IOException e) {
	    e.getStackTrace();
	}
    }

    /**
     * constructor
     * 
     * @param file
     */
    public Writer(String file) {
	this(file, false);
    }

    /**
     * write a tree
     * 
     * @param tree
     */
    public void write(Tree tree) {
	try {
	    bw.write(tree.toTreeFormat() + newLine);
	} catch (IOException e) {
	    e.getStackTrace();
	}
    }

    /**
     * write a rule in RuleFormat
     * 
     * @param rule
     */
    public void write(Rule rule) {
	try {
	    bw.write(rule.toRuleFormat() + newLine);
	} catch (IOException e) {
	    e.getStackTrace();
	}
    }

    /**
     * write a String
     * 
     * @param line
     */
    public void write(String line) {
	try {
	    bw.write(line + newLine);
	} catch (IOException e) {
	    e.getStackTrace();
	}
    }

    /**
     * close the writer
     */
    public void close() {
	try {
	    bw.flush();
	    bw.close();
	} catch (Exception e) {
	    e.getStackTrace();
	}
    }
}
