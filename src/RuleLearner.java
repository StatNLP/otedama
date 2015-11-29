import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.*;

//import RuleTester;
//import Node;
//import Rule;
//import Tree;
//import Reader;

public class RuleLearner extends Thread {

	private int id;
	private Vector<Rule> ruleList;
	private Tree focusTree;
	private List<Tree> testTreeList;
	private int maxCrossingScore;
	private int minimumMatchingFeatures;
        private MasterLearner supervisor;
	
	public RuleLearner( Tree focusTree, List<Tree> testTreeList, Vector<Rule> ruleList, int maxCrossingScore, int minimumMatchingFeatures, MasterLearner supervisor){

		this.ruleList = ruleList;
		this.focusTree = focusTree;
		this.testTreeList = testTreeList;
		this.ruleList = ruleList;
		this.maxCrossingScore = maxCrossingScore;
		this.minimumMatchingFeatures = minimumMatchingFeatures;
		this.supervisor = supervisor;

	}

	public void processTree(Tree focusTree) {
	    this.supervisor.incrementThreadCounter();
	    List<Rule> candidateRules = focusTree.getAllCandidates("CROSSING");
	    for (Rule candidate : candidateRules) {
		int difference = 0;
		for (Tree tree : this.testTreeList) {
		    //System.out.println("=================");
		    int base = RuleTester.computeCS(tree.getAlignments());
		    //System.out.println("Before: CS:"+base+":");
		    //tree.recursivePrint();
		    //System.out.println(candidate.toRuleFormat());
		    Tree postTree = tree.applyRule(candidate, this.minimumMatchingFeatures);
		    int post = RuleTester.computeCS(postTree.getAlignments());
		    //System.out.println("After: CS:"+post+":");
		    //postTree.recursivePrint();
		    
		    difference += post - base;
		    
		}
		if (difference < this.maxCrossingScore) {
		    if (candidate.getAction().size() != 0){
			candidate.setScore("CROSSING", (double) difference);
			this.ruleList.add(candidate);
		    }		    
		}
	    }
	    this.supervisor.decrementThreadCounter();
	}

	public void run(){

		processTree(this.focusTree);
	}
}
