/**
 * 
 */
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author laura
 *
 */
public class ConllToTree {

	/**
	 * read parser output in CONLL-Format (e.g. from ParZu for German)
	 * and write as .tree file (optionally with alignment)
	 *   
	 * @param in : input file in CONLL Format
	 * @param align : alignment file
	 */
	public static void convert(String in, String align, String out ) {

		// use alignments?
		boolean use_alignments = true;
		if (align.startsWith("no_align")){
			use_alignments = false;
			System.err.println("Not using alignments.");
		} else {
			System.err.println("Using alignments from "+align);
		}

		// read input file
		Reader r = new Reader(in);

		// set up alignment file reader
		Reader alignment = new Reader();
		if (use_alignments){
			alignment = new Reader(align);
		}


		Writer treeWriter = new Writer(out);
		String line = null;
		int counter = 0;
		Tree t = new Tree();
		// ROOT node
		Node root = new Node(true, true);
		root.setTag("ROOT");
		t.setRoot(root);

		//List for root node and lexical nodes 
		List<Node> leafNodes = new LinkedList<Node>();
		List<Node> governingNodes = new LinkedList<Node>();
		List<Integer> deps = new LinkedList<Integer>(); // store dependencies here
		// remember that words are 1-indexed!
		deps.add(-1); // root has not dependency
		leafNodes.add(root);
		governingNodes.add(root);
		String surface;


		// each line corresponds to a word. An empty line indicates a new tree
		while ((line = r.getNext()) != null) {

			if (! line.isEmpty()) {

				// TODO replace bad words: (, ), ', &, #, >, =

				String[] tokens = line.trim().split("\t");
				int tokenID = new Integer(tokens[0]);
				String word = tokens[1];
				String pos = tokens[4];
				// USE PROJECTIVE OUTPUT!
				int headID = new Integer(tokens[8]);
				String label = tokens[9];

				// create new leaf node
				Node n = new Node();
				n.setNodeID(tokenID);
				surface = word;
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
				surface = surface.replaceAll("&", "-AMP-");
				surface = surface.replaceAll("#", "-NUM-");
				surface = surface.replaceAll(">", "-GRE-");
				surface = surface.replaceAll("=", "-EQU-");

				n.setSurface(surface);
				n.setTag(pos);
				n.setLabel(label);			
				n.setInitialLexicalIndex(++counter);
				leafNodes.add(n);

				Node governingNode = new Node();
				governingNode.setInitialLexicalIndex(counter);
				governingNode.setTag("_"+pos);
				governingNode.setLabel(label);
				governingNode.setChild(n);
				governingNodes.add(governingNode);

				deps.add(headID); // store dependency
			} 
			else 
			{
				// build dependencies
				int govIndex;
				int depIndex;
				for (int i=1; i < deps.size(); i++) {
					depIndex =i;
					govIndex = deps.get(i);
					Node dep = leafNodes.get(depIndex);
					Node gov = governingNodes.get(govIndex);
					gov.setChild(governingNodes.get(depIndex));
					governingNodes.get(depIndex).setParent(gov);
					dep.setParent(governingNodes.get(depIndex));
				}

				// remove unneeded nodes
				Node gov;
				Node dep;
				Node parent;
				List<Node> children;

				for (int i=1; i < governingNodes.size(); i++) {
					gov = governingNodes.get(i);
					dep = leafNodes.get(i);
					if (gov.getChildren().size() <= 1) {
						int k = 0;
						parent = gov.getParent();
						children = parent.getChildren();

						for (Node n: children) {
							if (n == gov) {
								gov.getParent().replaceChild(k, dep);
								dep.setParent(gov.getParent());
							}
							k++;
						}
					} else {
						Node head = leafNodes.get(gov.getInitialLexicalIndex());
//						head.setLabel("head");						
					}
				}

				for (Node n : governingNodes){
					n.sortChildrenByInitialIndex();
				}
			    // combine with alignment
				// TODO
	            if (use_alignments){
		    	t.initialize(alignment.readNextAlign());
	            } else {
	                t.initializeUnaligned();
	            }


				//write tree to file
				treeWriter.write(t);

				//print tree to console


				System.out.println(t.toSentence());
				//	    if (verbose){
				System.err.println(t.toString());
				//		t.recursivePrint();
				//	    }



				root.removeChildren();
				governingNodes.clear();
				leafNodes.clear();
				deps.clear();
				deps.add(-1); // root has not dependency
				leafNodes.add(root);
				governingNodes.add(root);
				counter=0;

			}
		}
		System.err.println("#######################################################################");
		treeWriter.close();

	}



	public static void main(String[] args) {
		if (args.length == 3) {
		convert(args[0],args[1], args[2]);
		// TODO Auto-generated method stub

	} else {
	    System.out.println("Usage: (string) input file english, (string) alignment file english-foreign, (string) output file");
	}

	}

}
