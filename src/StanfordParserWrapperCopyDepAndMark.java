import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.*;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;


/**
 * Stanford Parser Wrapper (for Stanford Parser Version 2.0.5).
 * 
 */

public class StanfordParserWrapperCopyDepAndMark {

    /**
     * parse sentence and generate .trees file
     * 
     * @param en
     * @param align
     * @param out
     */
    public static void parse(String en, String align, String out, boolean verbose) {

	// use alignments?
	boolean use_alignments = true;
	if (align.startsWith("no_align")){
		use_alignments = false;
		System.err.println("Not using alignments.");
	} else {
                System.err.println("Using alignments from "+align);
	}

	// setup stanfordparser
	String grammar = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
	String[] options = { "-outputFormat", "wordsAndTags, typedDependencies" };
	LexicalizedParser lp = LexicalizedParser.loadModel(grammar, options);
	TreebankLanguagePack tlp = lp.getOp().langpack();
	java.util.function.Predicate<java.lang.String> punctuationFilter = x -> true;	
	
	GrammaticalStructureFactory gsf = new edu.stanford.nlp.trees.EnglishGrammaticalStructureFactory(punctuationFilter);

	// read document
	Iterable<List<? extends HasWord>> sentences;
	Reader r = new Reader(en);
	String line = null;
	List<List<? extends HasWord>> tmp = new ArrayList<List<? extends HasWord>>();
	while ((line = r.getNext()) != null) {
	    Tokenizer<? extends HasWord> token = tlp.getTokenizerFactory()
		    .getTokenizer(new StringReader(line));
	    List<? extends HasWord> sentence = token.tokenize();
	    tmp.add(sentence);
	}
	sentences = tmp;

	// set up alignment file reader
	Reader alignment = new Reader();
	if (use_alignments){
	    alignment = new Reader(align);
	}

	// set up tree file writer	
	Writer treeWriter = new Writer(out);

	// parse
	long start = System.currentTimeMillis();
	// System.err.print("Parsing sentences ");
	int sentID = 0;
	for (List<? extends HasWord> sentence : sentences) {
	    Tree t = new Tree();
	    //t.setSentID(++sentID);
	    System.err.println("parse Sentence :" + sentence + "...");
	    // System.err.print(".");
	    System.err.println("-----------------------------------------------------------------------");
	    edu.stanford.nlp.trees.Tree parse = lp.parse(sentence);
	    //parse.pennPrint();

	    //List for root node and lexical nodes 
	    List<Node> loneNodes = new LinkedList<Node>();
	    List<Node> governingNodes = new LinkedList<Node>();

	    // ROOT node
	    Node root = new Node(true, true);
	    root.setTag("ROOT");
	    t.setRoot(root);
            loneNodes.add(root);
	    governingNodes.add(root);

	    // tagging
	    
	    int counter = 0;
	    String surface = "";
	    String tag= "";
	    
	    for (TaggedWord tw : parse.taggedYield()) {
		Node n = new Node();
		Node governingNode = new Node();
		n.setNodeID(++counter);
		surface = tw.value();
		tag = tw.tag();
		if (surface.startsWith("-LRB-")){
		    surface = "(";
	        } else if (surface.startsWith("-RRB-")){
		    surface = ")";
		//} else if (surface.startsWith("-LSB-")){
                //    surface = "[";
                //} else if (surface.startsWith("-RSB-")){
                //    surface = "]";
		//} else if (surface.startsWith("-LCB-")){
                //    surface = "{";
		//} else if (surface.startsWith("-RCB-")){
                //    surface = "}";
                } else if (surface.startsWith("''")){
                    surface = "\"";
                } 
 		tag = tag.replaceAll("#", "-NUM-");
		surface = surface.replaceAll("&", "-AMP-");
		surface = surface.replaceAll("#", "-NUM-");
		surface = surface.replaceAll(">", "-GRE-");
		surface = surface.replaceAll("=", "-EQU-");
		n.setInitialLexicalIndex(counter);
		governingNode.setInitialLexicalIndex(counter);
		n.setSurface(surface);
		//System.out.print("("+tw.value()+" : ");
		n.setTag(tag);
		governingNode.setTag("_"+tag);
		
		//System.out.print(tw.tag()+")");
		loneNodes.add(n);
		governingNodes.add(governingNode);
		governingNode.setChild(n);
		
	    }
	    
	    //System.out.println("");

	    //t.setSentLength(t.getNodes().size() - 1);
	    //List<Node> loneNodes = new LinkedList<Node>();	
	    Node[] nodes = new Node[2000];
	    // labeling
	    int depIndex;
	    int govIndex;
	    String[] depInfo;
	    String[] govInfo;
	    GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
            List<TypedDependency> tdl = gs.typedDependencies(false);
	    //List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
	    for (TypedDependency td : tdl) {
		depIndex = td.dep().index() ;
		govIndex = td.gov().index() ;
		//System.out.println("Index1:"+depIndex);
		//System.out.println("Index2:"+govIndex);
		//if (nodes[depIndex] == null){
		//	System.out.println("Making node!");
		//	nodes[depIndex] = new Node();
		//}
		//if (nodes[govIndex] == null){
		//	System.out.println("Making node!");
		//	nodes[govIndex] = new Node();
		//}
		Node dep = loneNodes.get((depIndex));
		Node gov = governingNodes.get((govIndex));
		Node governingDep = governingNodes.get((depIndex));
		dep.setLabel(td.reln().toString());
		governingDep.setLabel("_"+td.reln().toString());
		//System.out.println(td.toString());
		govInfo = td.gov().toString().split("/");
		depInfo = td.dep().toString().split("/");
		//System.out.println(td.gov().toString());
		//System.out.println(td.dep().toString());
		//dep.setSurface(depInfo[0]);
		//dep.setTag(depInfo[1]);
		gov.setChild(governingNodes.get(depIndex));
		governingNodes.get(depIndex).setParent(gov);
		//gov.setChild(dep);
		dep.setParent(governingNodes.get(depIndex));
	    }
	    //t.setRoot(nodes[0]);

	    //Collapse tree to remove unneeded governing nodes:

            Node gov;
	    Node dep;
	    Node parent;
	    List<Node> children;

	    for (int i =1; i < governingNodes.size(); i++){//start with index 1 to skip root
		gov = governingNodes.get(i);
		dep = loneNodes.get(i);
		if (gov.getChildren().size() <= 1){
			int k = 0;
			parent = gov.getParent();
			children = parent.getChildren();

			for (Node n: children){
				if (n == gov){
					gov.getParent().replaceChild(k, dep);
					dep.setParent(gov.getParent());
				}
				k++;
			}
		}

	    }
	    // Sort lexical children of each governing node in lexical order
	    
	    for (Node n : governingNodes){
		n.sortChildrenByInitialIndex();
	    }

	    // combine with alignment
            if (use_alignments){
	    	t.initialize(alignment.readNextAlign());
            } else {
                t.initializeUnaligned();
            }

	    //write tree to file
	    treeWriter.write(t);

	    //print tree to console
	
	    
	    System.out.println(t.toSentence());
	    if (verbose){
		System.err.println(t.toString());
		//t.recursivePrint();
	    }
	    System.err.println("#######################################################################");
	}
	long stop = System.currentTimeMillis();
	System.err.println("...done! [" + (stop - start) / 1000 + " sec].");

	treeWriter.close();
    }

    public static void main(String[] args) {
	if (args.length == 4) {
	    parse(args[0], args[1], args[2], args[3].startsWith("v"));
	} else {
	    System.out.println("Usage: (string) input file english, (string) alignment file english-foreign, (string) output file, (v[erbose]|q[uiet]) output mode");
	}
    }
}

