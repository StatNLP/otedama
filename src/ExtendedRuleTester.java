import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

//import Reader;
//import Writer;
//import StanfordParserWrapper;
//import Node;
//import Rule;
//import Tree;

/**
 * Rule Tester class
 * 
 * 
 */
public class ExtendedRuleTester {
    
    List<Tree> testTreebank;
    
    public ExtendedRuleTester(File testCorpus){
	this.testTreebank = new LinkedList<Tree>();
	readTreeFile(testCorpus.getName());
    }
    
    public void crossingScore(Rule r){
	r.setScore("CROSSING", 0.0);
	for (Tree t: this.testTreebank){
	    testRule(r, t);
	}
    }
    
    public void readTreeFile(String treeFile){	
		try {
			BufferedReader treeBuffer = new BufferedReader(new FileReader( treeFile ));
			String stringTree = treeBuffer.readLine();
			while (stringTree != null){
				this.testTreebank.add(new Tree(stringTree));
				stringTree = treeBuffer.readLine();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Failed to read :"+treeFile);
			System.exit(4);
			
		}
	}

    public static void testRule(Rule candidate, Tree tree) {
	Tree tmp = tree; // copy
	int base = RuleTester.computeCS(tmp.depthFirstSearch(tmp.getNode(0)));
	candidate.setScore("CROSSING", (double) base + candidate.getScore("CROSSING"));
	int check = 0;
	for (int i = tmp.getSentLength() + 1; i < tmp.getNodes().size(); i++) {
	    Node node = tmp.getNodes().get(i);
	    check += tmp.applyRule(candidate, node);
	}
	int post = RuleTester.computeCS(tmp.depthFirstSearch(tmp.getNode(0)));
	if (check > 0 && base - post > 0) {
	    candidate.setScore("CROSSING", (double) post + candidate.getScore("CROSSING"));
	    tree = tmp; // actualize
	}
    }
    /**
     * compute Crossing Score
     * 
     * @param align
     * @return Crossing Score
     */
    public static int computeCS(List<List<Integer>> align) {
	int score = 0;
	List<Integer> list = new LinkedList<Integer>();
	for (List<Integer> k : align) {
	    list.addAll(k);
	}
	for (int i = 1; i < list.size(); i++) {
	    List<Integer> sub = list.subList(0, i);
	    int counter = 0;
	    for (int j = 0; j < sub.size(); j++) {
		if (list.get(i) < sub.get(j)) {
		    counter++;
		}
	    }
	    score += counter;
	}
	return score;
    }

    /*
     * // Test driver public static void main(String[] args) throws IOException
     * { Reader tr = new Reader("test.trees"); Writer rw = new
     * Writer("test3.rules", false); Writer tw = new Writer("test3.trees",
     * false);
     * 
     * Tree tree = null; while ((tree = tr.readNextTree()) != null) {
     * System.out.println("\n" + tree); List<Rule> candidateRules =
     * tree.getAllCandidates("CS"); for (Rule candidate : candidateRules) { Tree
     * tmp = tree; // copy int base =
     * computeCS(tmp.depthFirstSearch(tmp.getNode(0))); candidate.setScore("CS",
     * (double) base); System.out.println("candidate rule: " +
     * candidate.toRuleFormat()); int sum = 0; for (int i = tmp.getSentLength()
     * + 1; i < tmp.getNodes().size(); i++) { Node node = tmp.getNodes().get(i);
     * int check = tmp.applyRule(candidate, node); if (check > 0) sum += check;
     * } int post = computeCS(tmp.depthFirstSearch(tmp.getNode(0))); if (sum > 0
     * && base - post > 0) { candidate.setScore("CS", (double) post);
     * System.err.println("applied rule " + sum + " time(s) :" +
     * candidate.toRuleFormat()); rw.write(candidate); tree = tmp; // actualize
     * } tw.write(tree); } } rw.close(); tw.close(); }
     */
}
