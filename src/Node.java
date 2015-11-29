import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.List;


/**
 * Node class
 * 
 *
 */

public class Node implements Comparable<Node> {
    private int nodeID;
    private List<Integer> alignment = new LinkedList<Integer>();
    private Map<String, String> contexts = new HashMap<String, String>();
    private String surface;
    private String tag;
    private String label;
    private List<Node> children = new LinkedList<Node>();
    private Node parent;
    private int initialLexicalIndex = 0;
    private boolean hasInitialIndex = false;
    

    /**
     * Constructor
     * 
     * @param isDummy
     * @param isRoot
     */
    public Node(Boolean isDummy, Boolean isRoot) {
	// Dummy node
	if (isDummy) {
	    this.surface = null;

	    // Root node
	    if (isRoot) {
		this.nodeID = 0;
		this.tag = "ROOT";
		this.label = null;
		this.parent = null;
	    }
	}
    }

    /**
     * Constructor for dummy node
     * 
     * @param isDummy
     */
    public Node(Boolean isDummy) {
	this(isDummy, false);
    }

    /**
     * Constructor for terminal node
     * 
     */
    public Node() {
	this(false);
    }

    /**
     * Copy constructor </br> to create a deep copy
     * 
     * @param node
     */
    public Node(Node node) {
	this.nodeID = node.getNodeID();
	this.surface = node.getSurface();
	this.tag = node.getTag();
	this.label = node.getLabel();
	this.parent = node.getParent();
	this.children = new LinkedList<Node>(node.getChildren());
	for (String key : node.getContexts().keySet()) {
	    this.contexts.put(key, node.getContexts().get(key));
	}
	this.alignment = new LinkedList<Integer>(node.getAlignment());
    }

    // Getter and Setter

    public void setInitialLexicalIndex(int i){
	this.initialLexicalIndex = i;
	this.hasInitialIndex = true;
    }

    public int getInitialLexicalIndex(){
	return this.initialLexicalIndex;
    }

    public int getNodeID() {
	return nodeID;
    }

    public void setNodeID(int nodeID) {
	this.nodeID = nodeID;
    }

    public List<Integer> getAlignment() {
	return alignment;
    }

    public void setAlignment(Integer i) {
	this.alignment.add(i);
    }

    public String getSurface() {
	return surface;
    }

    public void setSurface(String surface) {
	this.surface = surface;
    }

    public Map<String, String> getContexts() {
	return contexts;
    }

    public void setContext(String feature, String value) {
	this.contexts.put(feature, value);
    }

    public void setContext(String[] context) {
	this.contexts.put(context[0], context[1]);
    }

    public String getTag() {
	return tag;
    }

    public void setTag(String tag) {
	this.tag = tag;
    }

    public String getLabel() {
	return label;
    }

    public void setLabel(String label) {
	this.label = label;
    }

    public List<Node> getChildren() {
	return children;
    }

    public Node getHeadChild() {
	for (Node child : this.getChildren()) {
	    if (child.getLabel().equals("head")) {
		return child;
	    }
	}
	return null;
    }

    public void removeChildren() {
	this.children = new LinkedList<Node>();
    }

    public void setChild(Node node) {
	this.children.add(node);
	node.setParent(this);
    }

    public void setChild(int i, Node node) {
	this.children.add(i, node);
	node.setParent(this);
    }

    public void setChildren(List<Node> children) {
	this.children = children;
	for (Node n: this.children){
		n.setParent(this);
	}
    }

    public void replaceChild(int i, Node node) {
	this.children.set(i, node);
	node.setParent(this);
    }

    public Node getParent() {
	return parent;
    }

    public void setParent(Node parent) {
	this.parent = parent;
    }

    public void postOrderWalk(List<Node> nodeList){
	nodeList.add(this);
	for (Node n: this.children){
	    n.postOrderWalk(nodeList);
	}
    }

    public void postOrderWalkLexical(List<Node> nodeList){
	if (this.surface != null && !this.surface.startsWith("null") && this.surface.length() > 0 ){
	    nodeList.add(this);
	}
	for (Node n: this.children){
	    n.postOrderWalkLexical(nodeList);
	}
    }

    public void sortChildrenByInitialIndex(){
	boolean childrenHaveIndices = true;
	for (Node n: this.children){
		if (n.hasInitialIndex == false){
			childrenHaveIndices = false;
		}
	}
	if (childrenHaveIndices){
		Collections.sort(this.children);
	}
    }

    @Override
    public int compareTo(Node other){
	int otherInitialIndex = other.getInitialLexicalIndex();
	if (this.initialLexicalIndex > otherInitialIndex){
		return 1;	
	} else if (this.initialLexicalIndex == otherInitialIndex){
		return 0;
	} else {
		return -1;
	}
    }

    public void fillContextTable() {
	this.setContext("nT", this.getTag());
	this.setContext("nL", this.getLabel());

	if (this.getParent() != null) {
	    this.setContext("pT", this.getParent().getTag());
	    this.setContext("pL", this.getParent().getLabel());
	}
	if (this.getChildren() != null) {
	    int j = 0;
	    for (Node child : this.getChildren()) {
		this.setContext(j + "T", child.getTag());
		this.setContext(j + "L", child.getLabel());
		j++;
	    }
	}
    }

    /**
     * limit context table to 3 children
     * 
     * @param start
     *            index of 3 children
     * @return limited context table
     */
    public Map<String, String> limitContextTable(int start) {
	Map<String, String> context = this.getContexts();
	Map<String, String> limit = new HashMap<String, String>();
	if (start >= context.size() - 3) {
	    System.err.println("start index is out of range!");
	    return null;
	}
	if (context.size() <= 10)
	    // the node has less than 3 children
	    return context;
	for (String key : context.keySet()) {
	    if (key.startsWith("n") || key.startsWith("p")
		    || key.startsWith("" + start)
		    || key.startsWith("" + (start + 1))
		    || key.startsWith("" + (start + 2))) {
		limit.put(key, context.get(key));
	    }
	}
	return limit;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */

    public String toNodeFormat(int indentation, String nodeSeparator, char indentChar){
	StringBuilder b = new StringBuilder();
	for(int i = 0; i<indentation; i++){
   	 b.append(indentChar);
	}
	String repr = b.toString();

	repr = repr+this.tag+"#"+this.label+"#"+this.surface+"#"+this.alignment.toString()+nodeSeparator;
	for (Node n : this.children){
	    repr = repr + n.toNodeFormat(indentation + 1, nodeSeparator, indentChar);
	}
        return repr;
    }

    public Node fromNodeFormat(String[] representations, boolean isDummy, boolean isRoot, int linePntr, char indentChar, String indentString){
	Node n = new Node(isDummy, isRoot);
	String firstLine = representations[linePntr];
	//System.out.println(firstLine);
	String[] fields = firstLine.split("#");
	//Set tag, label and algnment of node, stored in fields[0:2]
	String[] tagC = fields[0].split(indentString);
	n.setTag(tagC[tagC.length - 1]);
	n.setLabel(fields[1]);
	n.setSurface(fields[2]);
	//Parse and set alignmets stored in fields[3]
	// First, strip off "[" and "]" at beginning and end:
	String alignments = fields[3].substring(1, fields[3].length() -1 );
	alignments = alignments.replaceAll(" ", "");
	// Set individual alignments:
	if (alignments.length() > 0){
		String[] alignmentReprs = alignments.split(",");
		for (int j = 0; j < alignmentReprs.length; j++){
			n.setAlignment(Integer.parseInt(alignmentReprs[j]));
		}
	}
	// Parse and set children
	int level = this.getLevel(firstLine, indentChar);
	String s;
	int levelOfLine;
	for (int i = linePntr + 1; i < representations.length; i++){//We examine all lines after the current one, so linePntr + 1 to end
	    s = representations[i];
	    //System.out.println("Child index:"+ i);
	    //System.out.println("Child rep:"+ s);
	    // If identation level is exactly level + 1, it's a child
	    levelOfLine = this.getLevel(s, indentChar); 
	    
	    if (levelOfLine == level + 1){
		//System.out.println("A:Level:"+level+", Level of Line:"+ levelOfLine);
		//Recursively call fromNodeFormat to create children from string reps.
		n.setChild(fromNodeFormat(representations, false, false, i, indentChar, indentString));
	    // If identation level is greater than level + 1, it's a descendant, but not a child
	    } else if (levelOfLine > level + 1){
		//System.out.println("B:Level:"+level+", Level of Line:"+ levelOfLine);
		continue;
	    // First time we see and identation level equal or greater than level, we are done, there will be no more children
	    } else {
		//System.out.println("C:Level:"+level+", Level of Line:"+ levelOfLine);
		break;
	    }
	}
	n.fillContextTable();
	return n;
    }
    
    private int getLevel(String s, char indentChar){
	int level = 0;
	char c;
        for (int j = 0; j < s.length(); j++){
            c = s.charAt(j);
            if (c == indentChar){
                level++;
            } else {
                break;
            }
        }
	return level;
    }

    public void applyRule(Rule rule, int minimumMatchingFeatures){
	//Applies rule to node, in-place.
	boolean match = false;
	Node tmp;
	int j;
	int nChildren = this.children.size();
	int featureMatchUpperBound = ((nChildren*2)+4); //Two features per child, plus 2 for node, plus 2 for parent
	if (featureMatchUpperBound >= 8 && featureMatchUpperBound < minimumMatchingFeatures){ // Has to have at least 3 children for reordering
		minimumMatchingFeatures = featureMatchUpperBound;
	}
	this.fillContextTable();//Has to be called both before and after rule application, as other nodes may have changed
	//System.out.println("Checking applicability:"+minimumMatchingFeatures);
	if (Collections.max(rule.getAction().keySet()) < nChildren) {
            int match_count = 0;
            for (String key : this.getContexts().keySet()) {
                if (rule.getContext().containsKey(key) && rule.getContext().get(key) != null) {
                    if (rule.getContext().get(key).equals(this.getContexts().get(key))) {
                        match_count++;
			//System.out.println(rule.getContext().get(key).toString()+"="+this.getContexts().get(key)+":"+match_count);
                    }
                }
	    }
	    if (match_count >= minimumMatchingFeatures){
		match = true;
	    }
	}
	if (match){
	    //System.out.println("Rule applies!");
	    List<Node> childrenPreviousOrder = new LinkedList(this.children);
	    //this.setSurface("*"+this.getSurface());
	    for (int i: rule.getAction().keySet()){
		    j = rule.getAction().get(i);
		    tmp = childrenPreviousOrder.get(i);
		    this.children.set(j, tmp);
		}
	    this.fillContextTable();//Second call to fillContextTable(), if anything's changed
	}
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((label == null) ? 0 : label.hashCode());
	result = prime * result + nodeID;
	result = prime * result + ((surface == null) ? 0 : surface.hashCode());
	result = prime * result + ((tag == null) ? 0 : tag.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (!(obj instanceof Node))
	    return false;
	Node other = (Node) obj;
	if (label == null) {
	    if (other.label != null)
		return false;
	} else if (!label.equals(other.label))
	    return false;
	if (nodeID != other.nodeID)
	    return false;
	if (surface == null) {
	    if (other.surface != null)
		return false;
	} else if (!surface.equals(other.surface))
	    return false;
	if (tag == null) {
	    if (other.tag != null)
		return false;
	} else if (!tag.equals(other.tag))
	    return false;
	return true;
    }

}
