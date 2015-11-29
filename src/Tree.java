import java.util.Collections;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;
import java.util.TreeMap;


/**
 * Tree class <br>
 * 
 * represents a sentence structure and word alignments.
 */

public class Tree {
    //private int sentID;
    private Node root;
    //private int sentLength;

    public Tree() {
	//this.sentID = 0;
	this.root = new Node();
	//this.sentLength = 0;
    }

    //public int getSentID() {
    //    return sentID;
    //}

    //public void setSentID(int sentID) {
    //    this.sentID = sentID;
    //}

    public Node getRoot() {
	return this.root;
    }

    public void setRoot(Node n) {
	this.root = n;
    }

    //public int getSentLength() {
    //    return sentLength;
    //}

    //public void setSentLength(int sentLength) {
    //    this.sentLength = sentLength;
    //}

    public List<Node> getNodes(){
	//Performs postorder tree walk, constructs a list of nodes in lexical order and returns it
	List<Node> nodes = new LinkedList<Node>();
	this.getRoot().postOrderWalk(nodes);
	return nodes;
    }

    public List<Node> getLexicalNodes(){
	//Performs a postorder tree walk, constructs a list of lexical nodes in lexical order and returns it
	List<Node> nodes = new LinkedList<Node>();
	this.getRoot().postOrderWalkLexical(nodes);//Only lexical nodes are added
	return nodes;
    }

    public Node getNode(int i){
	//Returns the ith node of the tree in lexical order, after performing a postorder tree walk
	//TODO more efficient implementation, keep permanent list and update (risky!)
	List<Node> nodes = this.getNodes();
	return nodes.get(i);
    }

    public List<List<Integer>> getAlignments(){
	List<List<Integer>> alignments = new LinkedList<List<Integer>>();
	for (Node n: this.getNodes()){
	    alignments.add(n.getAlignment());
	}
	return alignments;
    }

    /**
     * set alignment
     * 
     * this function is invoked by initialize()
     * 
     * @param alignment
     */
    private void setAlignment(String[] alignment) {
        //This is the way folks used to do it, but no more:

	//for (String a : alignment) {
	//    if (!a.isEmpty()) {
	//	String[] id = a.split("-");
	//	if (getNode(Integer.parseInt(id[0]) + 1) != null) {
	//	    this.getNode(Integer.parseInt(id[0]) + 1).setAlignment(Integer.parseInt(id[1]) + 1);
	//	} else {
	//	    System.err.println("setAlignment(): Error on " + id[0]
	//		    + "-th word");
	//	}
	//    }
        //}

	// This is new new-fangled way of doing it:
	// Get lexical nodes (words)
	String [] id;
	int index;
	int aligned;
	List<Node> words = this.getLexicalNodes();
	int sentenceLength = words.size();
     
	for (String a : alignment){
	    if (!a.isEmpty()){
			id = a.split("-");
			index = Integer.parseInt(id[0]);
			if (index >= sentenceLength){ //Assuming indices that start with 0!
			    System.err.println("Alignment mismatch!");
			    break;
			}
			aligned = Integer.parseInt(id[1]);
			words.get(index).setAlignment(aligned);//No longer adding +1!
	    }
	}
    }

    /**
     * initialize a tree
     * 
     * @param alignment
     */
    public void initialize(String[] alignment) {
        //this.addDummy()
        //this.getNode(0).fillContextTable();
        //for (int i = this.getSentLength(); i < this.getNodes().size(); i++) {
        //    this.getNodes().get(i).fillContextTable();
        //}
	for (Node n: this.getNodes()){
	    n.fillContextTable();
	}
        this.setAlignment(alignment);
    }

    public void initializeUnaligned() {
        //this.addDummy();
        //this.getNode(0).fillContextTable();
        //for (int i = this.getSentLength(); i < this.getNodes().size(); i++) {
        //    this.getNodes().get(i).fillContextTable();
        
	//}
	for (Node n: this.getNodes()){
	    n.fillContextTable();
	}
    }
    
    public Tree applyRule(Rule rule, int minimumMatchingFeatures) {
	Tree newTree = new Tree();
	newTree = newTree.fromTreeFormat(this.toTreeFormat());
	for (Node node : newTree.getNodes()){
	    //node.fillContextTable();//This call is made already by node.applyRule()
	    node.applyRule(rule, minimumMatchingFeatures);
	    //node.fillContextTable();//This call is made already by node.applyRule()
	}
	return newTree;
    }

    public void applyRuleInPlace(Rule rule, int minimumMatchingFeatures) {
	
	for (Node node : this.getNodes()){
	    //node.fillContextTable();//This call is made already by node.applyRule()
	    node.applyRule(rule, minimumMatchingFeatures);
	    //node.fillContextTable();//This call is made already by node.applyRule()
	}
    }


    /**
     * generate all candidate rules for the sentence
     * 
     * @return list of rules
     */
    public List<Rule> getAllCandidates(String metric) {
	final int WINDOW_SIZE = 3;

        List<Rule> candidateRules = new LinkedList<Rule>();
        // for all dummy nodes (obsolete)
	//for (int i = getSentLength() + 1; i < getNodes().size(); i++) {
	//Node currentNode = getNodes().get(i);
	
	//For all nodes:
	for (Node currentNode : this.getNodes()){
	    // for all permutations
	    int size = currentNode.getChildren().size();
	    if (size > WINDOW_SIZE) {
		for (int j = 0; j < size - WINDOW_SIZE + 1; j++) {
		    Node tmp = new Node();
		    tmp = currentNode;
		    appendRules(tmp, WINDOW_SIZE, j, candidateRules);
		}
	    } else {
		appendRules(currentNode, size, 0, candidateRules);
            }
        }
	
        return candidateRules;
    }
    
    private List<Rule> appendRules(Node node, int size, int start,
	    List<Rule> ruleSet) {
	List<Map<Integer, Integer>> actionList = generateAction(size, start);
	if (actionList != null && actionList.size() != 0) {
	    // for all permutations
	    for (Map<Integer, Integer> action : actionList) {
		// for all contexts
		Rule rule = new Rule();
		rule.setContext(node.limitContextTable(start));
		rule.setAction(action);
		
		// Check if context and action tables are actually filled with anything
		
		if (!rule.getAction().isEmpty() && !rule.getContext().isEmpty()) {
		    ruleSet.add(rule);
		}
	    }
	}
	return ruleSet;
    }

    /**
     * create all permutations <br>
     * 
     * only for less than 3 children
     * 
     * @param size
     *            number of children
     * @param k
     *            start index of the first child
     * @return List<Map<Integer, Integer>> list of action
     */
    private List<Map<Integer, Integer>> generateAction(int size, int k) {
	List<Map<Integer, Integer>> actionList = new LinkedList<Map<Integer, Integer>>();
	if (size == 2) {
	    Map<Integer, Integer> action = new TreeMap<Integer, Integer>();
	    action.put(0, 1);
	    action.put(1, 0);
	    actionList.add(action);
	    return actionList;
	} else if (size == 3) {
	    Map<Integer, Integer> action1 = new TreeMap<Integer, Integer>();
	    action1.put(k, k);
	    action1.put(k + 1, k + 2);
	    action1.put(k + 2, k + 1);
	    actionList.add(action1);
	    Map<Integer, Integer> action2 = new TreeMap<Integer, Integer>();
	    action2.put(k, k + 1);
	    action2.put(k + 1, k + 0);
	    action2.put(k + 2, k + 2);
	    actionList.add(action2);
	    Map<Integer, Integer> action3 = new TreeMap<Integer, Integer>();
	    action3.put(k, k + 1);
	    action3.put(k + 1, k + 2);
	    action3.put(k + 2, k);
	    actionList.add(action3);
	    Map<Integer, Integer> action4 = new TreeMap<Integer, Integer>();
	    action4.put(k, k + 2);
	    action4.put(k + 1, k);
	    action4.put(k + 2, k + 1);
	    actionList.add(action4);
	    Map<Integer, Integer> action5 = new TreeMap<Integer, Integer>();
	    action5.put(k, k + 2);
	    action5.put(k + 1, k + 1);
	    action5.put(k + 2, k);
	    actionList.add(action5);
	    return actionList;
	}
	return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return this.getNode(0).toNodeFormat(0, "\n", '\t');
    }

    public String toSentence() {
	String sentence = "";
	//Words are surfaces of all lexical nodes:
	List<Node> nodes = this.getLexicalNodes();
	for(Node n: nodes){
	    sentence = sentence + n.getSurface() + " ";
	}
	return sentence;
	     
    }

    /**
     * convert to .trees format
     * 
     * @return String in .trees format (one sentence per line)
     */
    public String toTreeFormat() {
        return this.getNode(0).toNodeFormat(0, "&", '>');
    }

    public Tree fromTreeFormat(String treeFormat) {
	Tree t = new Tree();
	Node n = new Node();
	String[] nodeStrings = treeFormat.split("&");
	t.setRoot(n.fromNodeFormat(nodeStrings, false, true, 0, '>', ">"));
	//t.setSentLength(t.getNodes().size());
	
	for (Node node: t.getNodes()){
	    node.fillContextTable();
	}

	return t;
    }

    public void recursivePrint(){
	System.out.println(this.toString());
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((root == null) ? 0 : root.hashCode());
	//result = prime * result + sentID;
	//result = prime * result + sentLength;
	return result;
    }
}
