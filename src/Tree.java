import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.lang.Math;


/**
 * Tree class <br>
 * 
 * represents a sentence structure and word alignments.
 */

public class Tree {
    
    private Node root;
    private boolean useless;
    private boolean added;
    private boolean modified;
    private int crossingScore;
    private Vector<Integer> alignmentVector = new Vector<Integer>();
    private Lock treeLock = new ReentrantLock();
   

    public Tree() {
	this.useless = false;
	this.root = new Node();
	this.added = false;
	this.modified = false;
	this.crossingScore = 0;
	
    }
    
    public int getCrossingScore(){
        return this.crossingScore;
    }
    
    public void setCrossingScore(int i){
        this.crossingScore = i;
    }
    
    public boolean isModified(){
        return this.modified;
    }
    
    public void setModified(boolean b){
        this.modified = b;
    }

    public void lockTree(){
	this.treeLock.lock();
    }

    public void releaseTree(){
	this.treeLock.unlock();
    }

    public boolean hasBeenAdded(){
	return this.added;
    }

    public void setAdded(boolean b){
	this.added = b;
    }

    public boolean isUseless(){
	return this.useless;
    }

    public void setUseless(boolean b){
	this.useless = b;
    }

    public Node getRoot() {
	return this.root;
    }

    public void setRoot(Node n) {
	this.root = n;
    }
    
    public Vector<Integer> getTransformation(){
	Vector<Integer> transformation = new Vector<Integer>();  
	for(Node n: this.getLexicalNodes()){
		transformation.add(n.getInitialLexicalIndex());
	}
	return transformation;
    }
    
    public String getTransformationString(){
	String output = "";
	for(int i: this.getTransformation()){
		output = output + i + " ";
	}
	return output;
    }
    
    public void recordPositions(){
	int i = 0;
	for(Node n: this.getLexicalNodes()){
		n.setInitialLexicalIndex(i);
		i++;
	}
    }

    public Vector<Node> getNodes(){
	//Performs postorder tree walk, constructs a list of nodes in lexical order and returns it
	Vector<Node> nodes = new Vector<Node>();
	this.getRoot().postOrderWalk(nodes);
	return nodes;
    }

    public Vector<Node> getLexicalNodes(){
	//Performs a postorder tree walk, constructs a list of lexical nodes in lexical order and returns it
	Vector<Node> nodes = new Vector<Node>();
	this.getRoot().postOrderWalkLexical(nodes);//Only lexical nodes are added
	return nodes;
    }

    public Node getNode(int i){
	//Returns the ith node of the tree in lexical order, after performing a postorder tree walk
	//TODO more efficient implementation, keep permanent list and update (risky!)
	Vector<Node> nodes = this.getNodes();
	return nodes.get(i);
    }

    public Vector<Vector<Integer>> getAlignments(){
	Vector<Vector<Integer>> alignments = new Vector<Vector<Integer>>();
	for (Node n: this.getNodes()){
	    alignments.add(n.getAlignment());
	}
	return alignments;
    }

    private void setAlignment(String[] alignment) {

	// This is new new-fangled way of doing it:
	// Get lexical nodes (words)
	String [] id;
	int index;
	int aligned;
	Vector<Node> words = this.getLexicalNodes();
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
	this.fillAlignmentVector();
    }

    /**
     * initialize a tree
     * 
     * @param alignment
     */
    public void initialize(String[] alignment) {
	for (Node n: this.getNodes()){
	    n.fillContextTable();
	}
        this.setAlignment(alignment);
    }

    public void initializeUnaligned() {
	for (Node n: this.getNodes()){
	    n.fillContextTable();
	}
    }
    
    public boolean isRuleApplicable(Rule rule, int minimumMatchingFeatures){
        boolean match;
        for (Node node : this.getNodes()){
            match = node.isRuleApplicable(rule, minimumMatchingFeatures);
            if (match){
                return true;
            }
        }
        return false;
    }

    public int applyRuleInPlace(Rule rule, int minimumMatchingFeatures) {
	boolean match;
	int applications = 0;
	for (Node node : this.getNodes()){
	    //node.fillContextTable();//This call is made already by node.applyRule()
	    match = node.applyRule(rule, minimumMatchingFeatures);
	    //node.fillContextTable();//This call is made already by node.applyRule()
	    if (match && this.isUseless()){
		this.setUseless(false);
	    }
	    if (match){
		applications += 1;
	    }
	}
	if (applications > 0){
            this.setModified(true);
	}
	this.fillAlignmentVector();
	this.computeCS();
	return applications;
    }


    /**
     * generate all candidate rules for the sentence
     * 
     * @return list of rules
     */
    public Vector<Vector<Rule>> getAllCandidates(String metric, int windowSize, boolean useSubsets) {
	//final int WINDOW_SIZE = 4;

        Vector<Vector<Rule>> candidateRules = new Vector<Vector<Rule>>();
	
	//For all nodes:
	for (Node currentNode : this.getNodes()){
	    // for all permutations
	    int size = currentNode.getChildren().size();
	    if (size > windowSize) {
		for (int j = 0; j < size - windowSize + 1; j++) {
		    Node tmp = new Node();
		    tmp = currentNode;
		    appendRules(tmp, windowSize, j, candidateRules, windowSize, useSubsets);
		}
	    } else {
		appendRules(currentNode, size, 0, candidateRules, windowSize, useSubsets);
            }
        }
	
        return candidateRules;
    }
    
    private Vector<Vector<Rule>> appendRules(Node node, int size, int start,
	    Vector<Vector<Rule>> ruleSet, int windowSize, boolean useSubsets) {
	int i = 0;
	double j = 0.0;
	int baseCS = this.crossingScore(node.getAlignments());
	int postCS = 0;
	int maxContextSize = 4 + (2*windowSize);
	Vector<Map<Integer, Integer>> actionList = generateAction(size, start);
	if (actionList == null){
            //System.out.println("Extracted 0 candidates.");
            return ruleSet;
	}
	Vector<Map<Integer, Integer>> filteredActionList = new Vector<Map<Integer, Integer>>();
	String[] nodeStrings = node.toNodeFormat(0, "&", '>').split("&");
	Node nodeCopy = new Node(false, false);
	for (Map<Integer,Integer> candidateAction: actionList){
            nodeCopy = nodeCopy.fromNodeFormat(nodeStrings, false, true, 0, '>', ">");
            nodeCopy.reorderChildren(candidateAction);
            postCS = this.crossingScore(nodeCopy.getAlignments());
            if (postCS < baseCS){
                filteredActionList.add(candidateAction);
            }
	}
	
	if (filteredActionList != null && filteredActionList.size() != 0) {
	    // for all permutations
	    for (Map<Integer, Integer> action : filteredActionList) {
		// for all contexts
		Map<String,String> context = node.limitContextTable(start, windowSize);
		if (context.size() > maxContextSize){
                    //System.out.println("Extracted 0 candidates.");
                    return ruleSet;
		}
		Map<String,String> ruleContext;
                Map<String,Boolean> mask = node.initializeContextMask(context);
                Vector<String> contextKeys = new Vector<String>();
                
                for (String s: context.keySet()){
                    contextKeys.add(s);
                }
                i = 0;
                j = Math.pow(2, context.size())-1.0;
                Vector<Rule> contextRules = new Vector<Rule>((int) j);
                //j = context.size() * 1.0;
                if (!useSubsets){
                    i = (int) j - 1; //only extract one rule, the most specific one
                }
		while (i < j){    
                    ruleContext = node.applyMask(context, mask);
                    Rule rule = new Rule();
                    rule.setContext(ruleContext);
                    rule.setAction(action);
		
                    // Check if context and action tables are actually filled with anything
		
                    if (!rule.getAction().isEmpty() && !rule.getContext().isEmpty()) {
                        contextRules.add(rule);
                        //System.out.println("Found candidate:"+rule.toString());
                        mask = node.decrementContextMask(mask, contextKeys);
                        i++;
                    }
		}
		Collections.sort(contextRules);
		ruleSet.add(contextRules);
	    }
	}
	//System.out.println("Extracted "+ruleSet.size()+" candidates.");
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
     * @return Vector<Map<Integer, Integer>> list of action
     */
    private Vector<Map<Integer, Integer>> generateAction(int size, int k) {
	Vector<Map<Integer, Integer>> actionList = new Vector<Map<Integer, Integer>>();
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
	} else if (size == 4){

            int[] O = {k, k+1, k+2, k+3};
            Map<Integer, Integer> action1 = new TreeMap<Integer, Integer>();
            action1.put(O[0], O[1]);
            action1.put(O[1], O[0]);
            action1.put(O[2], O[2]);
            action1.put(O[3], O[3]);
            actionList.add(action1);
            
            Map<Integer, Integer> action2 = new TreeMap<Integer, Integer>();
            action1.put(O[0], O[2]);
            action1.put(O[1], O[0]);
            action1.put(O[2], O[1]);
            action1.put(O[3], O[3]);
            actionList.add(action2);
            
            Map<Integer, Integer> action3 = new TreeMap<Integer, Integer>();
            action1.put(O[0], O[0]);
            action1.put(O[1], O[2]);
            action1.put(O[2], O[1]);
            action1.put(O[3], O[3]);
            actionList.add(action3);
            
            Map<Integer, Integer> action4 = new TreeMap<Integer, Integer>();
            action1.put(O[0], O[1]);
            action1.put(O[1], O[2]);
            action1.put(O[2], O[0]);
            action1.put(O[3], O[3]);
            actionList.add(action4);
            
            Map<Integer, Integer> action5 = new TreeMap<Integer, Integer>();
            action1.put(O[0], O[2]);
            action1.put(O[1], O[1]);
            action1.put(O[2], O[0]);
            action1.put(O[3], O[3]);
            actionList.add(action5);
            
            Map<Integer, Integer> action6 = new TreeMap<Integer, Integer>();
            action1.put(O[0], O[3]);
            action1.put(O[1], O[1]);
            action1.put(O[2], O[0]);
            action1.put(O[3], O[2]);
            actionList.add(action6);
            
            Map<Integer, Integer> action7 = new TreeMap<Integer, Integer>();
            action1.put(O[0], O[1]);
            action1.put(O[1], O[3]);
            action1.put(O[2], O[0]);
            action1.put(O[3], O[2]);
            actionList.add(action7);
            
            Map<Integer, Integer> action8 = new TreeMap<Integer, Integer>();
            action1.put(O[0], O[0]);
            action1.put(O[1], O[3]);
            action1.put(O[2], O[1]);
            action1.put(O[3], O[2]);
            actionList.add(action8);
            
            Map<Integer, Integer> action9 = new TreeMap<Integer, Integer>();
            action1.put(O[0], O[3]);
            action1.put(O[1], O[0]);
            action1.put(O[2], O[1]);
            action1.put(O[3], O[2]);
            actionList.add(action9);
            
            Map<Integer, Integer> action10 = new TreeMap<Integer, Integer>();
            action1.put(O[0], O[1]);
            action1.put(O[1], O[0]);
            action1.put(O[2], O[3]);
            action1.put(O[3], O[2]);
            actionList.add(action10);
            
            Map<Integer, Integer> action11 = new TreeMap<Integer, Integer>();
            action1.put(O[0], O[0]);
            action1.put(O[1], O[1]);
            action1.put(O[2], O[3]);
            action1.put(O[3], O[2]);
            actionList.add(action11);
            
            Map<Integer, Integer> action12 = new TreeMap<Integer, Integer>();
            action1.put(O[0], O[0]);
            action1.put(O[1], O[2]);
            action1.put(O[2], O[3]);
            action1.put(O[3], O[1]);
            actionList.add(action12);
            
            Map<Integer, Integer> action13 = new TreeMap<Integer, Integer>();
            action1.put(O[0], O[2]);
            action1.put(O[1], O[0]);
            action1.put(O[2], O[3]);
            action1.put(O[3], O[1]);
            actionList.add(action13);
            
            Map<Integer, Integer> action14 = new TreeMap<Integer, Integer>();
            action1.put(O[0], O[3]);
            action1.put(O[1], O[0]);
            action1.put(O[2], O[2]);
            action1.put(O[3], O[1]);
            actionList.add(action14);
            
            Map<Integer, Integer> action15 = new TreeMap<Integer, Integer>();
            action1.put(O[0], O[0]);
            action1.put(O[1], O[3]);
            action1.put(O[2], O[2]);
            action1.put(O[3], O[1]);
            actionList.add(action15);
            
            Map<Integer, Integer> action16 = new TreeMap<Integer, Integer>();
            action1.put(O[0], O[2]);
            action1.put(O[1], O[3]);
            action1.put(O[2], O[0]);
            action1.put(O[3], O[1]);
            actionList.add(action16);
            
            Map<Integer, Integer> action17 = new TreeMap<Integer, Integer>();
            action1.put(O[0], O[3]);
            action1.put(O[1], O[2]);
            action1.put(O[2], O[0]);
            action1.put(O[3], O[1]);
            actionList.add(action17);
            
            Map<Integer, Integer> action18 = new TreeMap<Integer, Integer>();
            action1.put(O[0], O[3]);
            action1.put(O[1], O[2]);
            action1.put(O[2], O[1]);
            action1.put(O[3], O[0]);
            actionList.add(action18);
            
            Map<Integer, Integer> action19 = new TreeMap<Integer, Integer>();
            action1.put(O[0], O[2]);
            action1.put(O[1], O[3]);
            action1.put(O[2], O[1]);
            action1.put(O[3], O[0]);
            actionList.add(action19);
            
            Map<Integer, Integer> action20 = new TreeMap<Integer, Integer>();
            action1.put(O[0], O[1]);
            action1.put(O[1], O[3]);
            action1.put(O[2], O[2]);
            action1.put(O[3], O[0]);
            actionList.add(action20);
            
            Map<Integer, Integer> action21 = new TreeMap<Integer, Integer>();
            action1.put(O[0], O[3]);
            action1.put(O[1], O[1]);
            action1.put(O[2], O[2]);
            action1.put(O[3], O[0]);
            actionList.add(action21);
            
            Map<Integer, Integer> action22 = new TreeMap<Integer, Integer>();
            action1.put(O[0], O[2]);
            action1.put(O[1], O[1]);
            action1.put(O[2], O[3]);
            action1.put(O[3], O[0]);
            actionList.add(action22);
            
            Map<Integer, Integer> action23 = new TreeMap<Integer, Integer>();
            action1.put(O[0], O[1]);
            action1.put(O[1], O[2]);
            action1.put(O[2], O[3]);
            action1.put(O[3], O[0]);
            actionList.add(action23);
            
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
	String format;
	this.lockTree();
	format = this.getNode(0).toNodeFormat(0, "\n", '\t');
	this.releaseTree();
	return format;
    }

    public String toSentence() {
	String sentence = "";
	//Words are surfaces of all lexical nodes:
	Vector<Node> nodes = this.getLexicalNodes();
	for(Node n: nodes){
	    sentence = sentence + n.getSurface() + " ";
	}
	return sentence;	     
    }

    /**
     * convert to .trees format
     * 
     * @return String in .trees format (one sentence p;er line)
     */
    public String toTreeFormat() {
	String format;
	this.lockTree();
        format = this.getNode(0).toNodeFormat(0, "&", '>');
	this.releaseTree();
	return format;    
	}

    public Tree fromTreeFormat(String treeFormat) {
	Tree t = new Tree();
	Node n = new Node();
	String[] nodeStrings = treeFormat.split("&");
	t.setRoot(n.fromNodeFormat(nodeStrings, false, true, 0, '>', ">"));
	for (Node node: t.getNodes()){
	    node.fillContextTable();
	}
	t.fillAlignmentVector();
	t.computeCS();
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
	return result;
    }
    
    public int crossingScore(Vector<Integer> alignmentVector){
        int score = 0;
        for(int i = 0; i < alignmentVector.size(); i++){
                for(int j = 0; j < i; j++){
                        if (alignmentVector.get(i) < alignmentVector.get(j)) {
                            score++;
                        }
                }                       
        }
        return score;
    }
    
    public int computeCS() {
	this.lockTree();
	int score = this.crossingScore(this.alignmentVector);
	if (score <= 0){
		this.setUseless(true);
	} else {
		this.setUseless(false);
	}
	this.setCrossingScore(score);
	this.releaseTree();
	return score;
    }

    public void fillAlignmentVector(){
	this.lockTree();
	Vector<Vector<Integer>> align = this.getAlignments();
	this.alignmentVector = new Vector<Integer>();
	for (Vector<Integer> k : align){
		for (int i: k){
			this.alignmentVector.add(i);
		}
	}
	this.releaseTree();
    }
}
