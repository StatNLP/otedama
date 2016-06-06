import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.Vector;

//import RuleTester;
//import Node;
//import Rule;
//import Tree;
//import Reader;

public class RuleLearner extends Thread {

	private int id;
	private Vector<Vector<Rule>> ruleList;
	private Tree focusTree;
	private Vector<Tree> testTreeList;
	private int maxCrossingScore;
	private int minimumMatchingFeatures;
        private MasterLearner supervisor;
        private boolean usePreCheck;
        private int windowSize;
        private boolean useSubsets;
	
	public RuleLearner(Tree focusTree, Vector<Tree> testTreeList, Vector<Vector<Rule>> ruleList, int maxCrossingScore, int minimumMatchingFeatures, MasterLearner supervisor, int windowSize, boolean useSubsets){

		this.ruleList = ruleList;
		this.focusTree = focusTree;
		this.testTreeList = testTreeList;
		this.ruleList = ruleList;
		this.maxCrossingScore = maxCrossingScore;
		this.minimumMatchingFeatures = minimumMatchingFeatures;
		this.supervisor = supervisor;
		this.usePreCheck = false;
		this.windowSize = windowSize;
		this.useSubsets = useSubsets;
	}

	public void processTree(Tree focusTree) {
	    boolean foundUsefulRule = false;
	    //int focusTreeCS = focusTree.computeCS();
            //this.supervisor.incrementThreadCounter();//Now done in main thread (fixing concurrency issues)
            Vector<Vector<Rule>> candidateRules = focusTree.getAllCandidates("CROSSING", this.windowSize, this.useSubsets);
            if (this.usePreCheck){
                Vector<Tree> treeCopies = new Vector<Tree>();
                Tree newTree = new Tree();
                int post = 0;
                int base = 0;
                int difference = 0;
                int count = 0;
                int postSum = 0;
                int baseSum = 0;
                int lc = 0;
                int applications = 0;
                for (Tree tree: this.testTreeList){
                    tree.fillAlignmentVector();
                    newTree = newTree.fromTreeFormat(tree.toTreeFormat());
                    newTree.fillAlignmentVector();
                    treeCopies.add(newTree);
                    base = tree.computeCS();
                    baseSum += base;		
                    }
                for (Vector<Rule> candidateList : candidateRules){
                    Vector<Rule> filteredCandidateList = new Vector<Rule>();
                    for (Rule candidate : candidateList) {

                        treeCopies = new Vector<Tree>();
                   
                        for (Tree tree: this.testTreeList){
                            newTree = newTree.fromTreeFormat(tree.toTreeFormat());
                            newTree.fillAlignmentVector();
                            treeCopies.add(newTree);
                        }
                        post = 0;
                        postSum = 0;
                    
                        difference = 0;
                   
                        for (Tree tree : treeCopies) {
                            applications = tree.applyRuleInPlace(candidate, this.minimumMatchingFeatures);
                        }

                        for(Tree t: treeCopies){
                            post = t.computeCS();
                            postSum = postSum + post;
                        }
                        difference = postSum - baseSum;
                        if (difference < this.maxCrossingScore) {
                            if (candidate.getAction().size() != 0){
                                candidate.setScore("CROSSING", (double) difference);
                                filteredCandidateList.add(candidate);
                                foundUsefulRule = true;
                            }		    
                        }
                    }
                    this.ruleList.add(filteredCandidateList);
                }           
            } else {
                for (Vector<Rule> candidateList : candidateRules){
                    Vector<Rule> outputCandidateList = new Vector<Rule>();
                    for (Rule candidate : candidateList) {
                        outputCandidateList.add(candidate);
                    }
                    this.ruleList.add(outputCandidateList);
                }
            }
            this.supervisor.decrementThreadCounter();
	}

	public void run(){
		processTree(this.focusTree);
	}
}
