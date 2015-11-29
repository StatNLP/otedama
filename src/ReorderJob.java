import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.*;

public class ReorderJob extends Thread {

	private Tree tree;
	private List<Rule> rulebase;
	private int minimumMatchingFeatures;

	public ReorderJob (Tree tree, List<Rule> rulebase, int minimumMatchingFeatures){
		this.tree = tree;
		this.rulebase = rulebase;
		this.minimumMatchingFeatures = minimumMatchingFeatures;
	}

	public void run(){		
		for (Rule rule : this.rulebase) {
		     this.tree.applyRuleInPlace(rule, minimumMatchingFeatures); 
	        }		
	}
}
