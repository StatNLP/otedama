import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

//import Node;
//import Tree;
//import Rule
//import Reader;
//import Writer;
//import StanfordParserWrapper;

/**
 * Rule Tester class
 * 
 * 
 */
public class RuleTester {


    public static Rule testRule(Rule candidate, Tree tree, int minimumMatchingFeatures) {
	int base = computeCS(tree.getAlignments());
	candidate.setScore("CROSSING", (double) base);
	tree.applyRule(candidate, minimumMatchingFeatures);
	int post = computeCS(tree.getAlignments());
	if (base - post > 0) {
	    candidate.setScore("CROSSING", (double) post);
	    return candidate;
	}
	return null;
    }

    /**
     * compute Crossing Score
     * 
     * @param align
     * @return Crossing Score
     */
    // Test driver
    public static void main(String[] args) throws IOException {

	int minimumMatchingFeatures = 8;
	Reader tr = new Reader("data/train/test01.en.trees");
	Tree tree = new Tree();
	int j = 2;
	while ((tree = tr.readNextTree()) != null) {
	    j --;
	    if (j<0){
		break;
	    }
	    System.out.println("\n************************");
	    System.out.println("\n" + tree);

	    //Tree tmpTree = new Tree(tree);
	    //Tree iniTree = new Tree(tree.toTreeFormat());
	    
	   // Tree tmpTree = iniTree;
	    
		Tree iniTree = tree.fromTreeFormat(tree.toTreeFormat());
		Tree tmpTree = tree.fromTreeFormat(tree.toTreeFormat());
	    
	    

		//for (int i = tmpTree.getSentLength() + 1; i < tmpTree.getNodes()
		//  .size(); i++) {
		//Node n = tmpTree.getNodes().get(i);
		//System.out.println("Node: " + n.toPrint() + " "
		//	+ n.getChildren() + "\t" + tmpTree.depthFirstSearch(n));
		//}
		//System.out.println("init:"
		//  + iniTree.depthFirstSearch(iniTree.getRoot()));
	    List<Rule> candidateRules = tree.getAllCandidates("CS");
	    for (Rule candidate : candidateRules) {
		
		//iniTree = new Tree(tree.toTreeFormat());
		//tmpTree = new Tree(tree.toTreeFormat());
		
		//iniTree = new Tree(new Tree(tree, true));
		//tmpTree = new Tree(new Tree(tree, true));
		
		iniTree = tree.fromTreeFormat(tree.toTreeFormat());
		tmpTree = tree.fromTreeFormat(tree.toTreeFormat());
		
		System.out.println("\tcandidate: " + candidate);
		System.out.println("\tbefore:" + tmpTree.toString());
		tmpTree = iniTree;
		int base = computeCS(tmpTree.getAlignments());
		tmpTree.applyRule(candidate, minimumMatchingFeatures);
		int post = computeCS(tmpTree.getAlignments());

		//for (int i = tmpTree.getSentLength() + 1; i < tmpTree.getNodes().size(); i++) {
		//    Node n = tmpTree.getNodes().get(i);
		//    System.out.println("\tNode: " + n.toPrint() + " "
		//	    + n.getChildren() + "\t"
		//	    + tmpTree.depthFirstSearch(n));
		//}
		System.out.println("\tafter: "+ tmpTree.getAlignments());
		
		System.out.println("\toriginal:"+tree.getAlignments());
		System.out.println("\t-----------------");
	    }
	}	  
    }	    
	    
}
    

    

