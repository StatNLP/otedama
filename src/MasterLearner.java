//import Writer;
//import Node;
//import Tree;
//import Rule;

import java.util.*;
import java.io.*;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.lang.Thread;

/**
 * MasterLeaener class
 * 
 * 
 */
public class MasterLearner {
	
    // TODO Debugging
    private Random randomGenerator;
    private List<Tree> treebank;
    private List<Rule> rulebase;
    private AtomicInteger threadCounter;
    

    /**
     * Constructor
     * 
     */
    public MasterLearner() {
	this.randomGenerator = new Random();
	this.treebank = new LinkedList<Tree>();
	this.rulebase = new LinkedList<Rule>();
	this.threadCounter = new AtomicInteger(0);
	
    }

    public int getThreadCount(){
	return this.threadCounter.get();
    }
	
    public void incrementThreadCounter(){
	this.threadCounter.incrementAndGet();
    }

    public void decrementThreadCounter(){
	this.threadCounter.decrementAndGet();
    }

    /**
     * read tree file
     * 
     * @param treeFile
     */
    public void readTreeFile(String treeFile) {
	int line_counter = 1;
	try {
	    BufferedReader treeBuffer = new BufferedReader(new FileReader(treeFile));
	    Tree tree = new Tree();
	    String stringTree = treeBuffer.readLine();
	    while (stringTree != null) {
		try {
			this.treebank.add(tree.fromTreeFormat(stringTree));
		} catch (Exception e) {
			System.err.println("WARNING: Could not read line "+line_counter+" :"+stringTree);
		}
		stringTree = treeBuffer.readLine();
		line_counter++;
	    }
	    treeBuffer.close();
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    System.out.println("Failed to read :" + treeFile);
	    System.exit(4);
	}
    }

    /**
     * read rule file
     * 
     * @param rulesFile
     * @param rulesList
     */
    public void readRulesFile(String rulesFile, List<Rule> rulesList) {
	try {
	    BufferedReader rulesBuffer = new BufferedReader(new FileReader(
		    rulesFile));
	    String stringRule = rulesBuffer.readLine();
	    //System.out.println("Reading rules file: "+rulesFile);
	    while (stringRule != null) {
		Rule r = new Rule(stringRule);
		if (r.getAction().size() > 0) {
		    //System.out.println("Read rule: "+r.toRuleFormat());
		    rulesList.add(r);
		}
		stringRule = rulesBuffer.readLine();
	    }
	    rulesBuffer.close();
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    System.out.println("Failed to read :" + rulesFile);
	    System.exit(3);
	}
    }

    /**
     * generate random subset of trees
     * 
     * @param treeList
     * @param percentage
     * @return subset of trees
     */
    
    
    
    public List<Tree> generateSubset(List<Tree> treeList, int length){
    int treebankLength = treeList.size();
    List <Tree> output = new LinkedList<Tree>();
    if (treebankLength > length){
        for (Tree t: treeList){
            output.add(t);
        }
        int outputLength = output.size();
        while (outputLength > length){
            int index = randomGenerator.nextInt(outputLength);
            output.remove(index);
            outputLength = output.size();
        }
        return output;
    } else {
        for (Tree t: treeList){
            output.add(t);
        } 
        return output;
    	}
    }
    
    
    
    
    
    public List<Tree> generateRandomSubset(List<Tree> treeList, int subsetLength) {
	List<Tree> subset = new LinkedList<Tree>();
	int treeListLength = treeList.size();
	// System.out.println("Size of treelist:" + treeListLength);
	// System.out.println("Size of subset:" + subsetLength);
	int index = 0;
	for (int i = 0; i < subsetLength; i++) {
	    index = this.randomGenerator.nextInt(treeListLength - 1);
	    subset.add(treeList.get(index));
	}
	return subset;
    }
    
    

    /**
     * read rule files from directory
     * 
     * @param directory
     * @param filePrefix
     * @return list of rules
     */
    public List<Rule> readRulesFromDirectory(File directory, String filePrefix) {
	List<Rule> rules = new LinkedList<Rule>();
	//System.out.println(directory.exists());
	File[] contentFiles = directory.listFiles();
	//System.out.println("Reading rules directory"+directory.getName());
	
	if (contentFiles != null) {
	    // System.out.println(contentFiles);
	    // System.out.println("Contains files:" + contentFiles.length);
	    for (File f : contentFiles) {
		if (f.getName().startsWith(filePrefix)) {
		    readRulesFile(f.getAbsolutePath(), rules);
		    //System.out.println("Size of rulelist(1):"+rules.size());
		}
	    }
	} else {
	    // System.out.println("NO CONTENT");
	    return rules;
	}
	//System.out.println("Size of rulelist(2):"+rules.size());
	return rules;
    }

    /**
     * run
     * 
     * @param rulesFile
     * @param originalTreeFile
     * @param n
     *            number of iteration
     * @throws Exception
     */
    public void run(
		String originalTreeFile, 
		int n, 
		int learningSet, 
		int scoringSet, 
		int maxCrossingScore, 
		int trials, 
		int minimumMatchingFeatures,
		int maximumParallelThreads,
		String outputRuleFile) throws Exception {
	long startRun = System.currentTimeMillis();
	
	//read the original trees file to treebank
	readTreeFile(originalTreeFile);
	// System.out.println(this.treebank);
	
	//create an empty rules fife and "read" its contents to the rulebase
	//File rules = new File(rulesFile);
	//rules.createNewFile();
	//readRulesFile(rulesFile, this.rulebase);
	// System.out.println(this.rulebase);
	
	//create a temporary tress file and write the treebank to it, then refreshes the treebank from it
	System.out.println("Size of treebank:"+this.treebank.size());
	System.out.println("Maximum crossing score:"+maxCrossingScore);
	System.out.println("Minimum matching features:"+minimumMatchingFeatures);

	File temporaryTrees = new File("temp.trees");
	temporaryTrees.createNewFile();
	Writer writer = new Writer(temporaryTrees.getName(), false);

	/**
	for (Tree t: this.treebank){
	    writer.write(t);
	}
	writer.close();
	**/

	// System.out.println(this.treebank);
	int smallestDecimalPlace;
	String suffix;
	
	// Iterative learning steps: One rule is added per iteration
	for (int i=0; i < trials; i++){
	    if (this.rulebase.size()>= n){
		break;
	    }
		
            smallestDecimalPlace = i % 10;
            if (smallestDecimalPlace == 1 && i % 100 != 11){
			suffix = "st";
	    } else if (smallestDecimalPlace == 2 && i % 100 != 12) {
			suffix = "nd";
	    } else if (smallestDecimalPlace == 3 && i % 100 != 13) {
			suffix = "rd";
	    } else {
			suffix = "th";
            }
            	    

	    System.out.println("============================");
	    System.out.println("Start of " + i + suffix+" iteration.");
	    long start = System.currentTimeMillis();

	    // read the current temporary trees file to treebank
	    //this.treebank = new LinkedList<Tree>();
	    //readTreeFile("temp.trees");
	    // System.out.println(this.treebank);
	    
	    // Create an evaluation subset and write it to /home/public/test.trees

	    List<Tree> evalTreebank = generateSubset(this.treebank, scoringSet);
	    System.out.println("Size of the evaluation subset:"+evalTreebank.size());

            //Generate random subset of trees on which to learn rules	    

	    List <Tree> randomSubset = generateSubset(this.treebank, learningSet);
	    System.out.println("Size of the training subset:"+randomSubset.size());

	    
	    // Run learners on subset.trees (one learner per tree). 
	    System.out.println("Size of treebank:"+this.treebank.size());
	    System.out.println("Maximum crossing score:"+maxCrossingScore);
	    System.out.println("Minimum matching features:"+minimumMatchingFeatures);
            Vector<Rule> learnedRules = new Vector<Rule>();

            List<RuleLearner> ruleLearners = new LinkedList<RuleLearner>();

            for (Tree t: randomSubset){

		while (this.threadCounter.get() >= maximumParallelThreads){
			Thread.sleep(100);	
		}


		RuleLearner ruleLearner = new RuleLearner(t, evalTreebank, learnedRules, maxCrossingScore, minimumMatchingFeatures, this);
		ruleLearners.add(ruleLearner);
		ruleLearner.start();
            }

            for (RuleLearner l: ruleLearners){
                l.join();
            }
	    
	    // Select the new rule with the smallest crossing score.
	    boolean success = false;
	    if (learnedRules.size() != 0) {
		success = true;
	    }
	    if (success){
		System.out.println("CADIDATE RULES:"+learnedRules.size());
		Double minScore = null;
		Rule minimumScoringRule = new Rule();
		for (Rule r: learnedRules){
		    if (minScore == null){
			minScore = r.getScore("CROSSING");
			minimumScoringRule = r;
		    }
		    Double currentScore = r.getScore("CROSSING");
		    if (currentScore < minScore){
			minScore = currentScore;
			minimumScoringRule = r;
		    }
		    // System.out.println("MINIMUM SCORE:"+minScore+":"+minimumScoringRule+minimumScoringRule.getAction().size());
		
		}
	    
		// apply the minimum scoring Rule to the treebank
		try {
		    //List<Tree> newTreebank = new LinkedList<Tree>();
		    for (Tree t : this.treebank) {
			t.applyRuleInPlace(minimumScoringRule, minimumMatchingFeatures);
			//newTreebank.add(newTree);
		    }
		    //this.treebank = newTreebank;
		} catch (Exception e) {
		    e.printStackTrace();
		    System.err.println("WARNING: Could not apply rule: "+minimumScoringRule.toString());
		}
	    
		// delete the previous temporary trees file, containing an outdated version of the treebank
		temporaryTrees.delete();
	    
		// write the new treebank to a new temporary trees files
		temporaryTrees = new File("temp.trees");
		temporaryTrees.createNewFile();
		writer = new Writer(temporaryTrees.getName(), false);
		for (Tree t : this.treebank) {
		    writer.write(t);
		}
		writer.close();
	    
		// add the minimum scoring Rule to the rulebase
		this.rulebase.add(minimumScoringRule);
		writer = new Writer(outputRuleFile, false);
		for (Rule r: this.rulebase){
		    writer.write(r);
		}
		writer.close();
		System.out.println("Learnd new rule: "+minimumScoringRule.toRuleFormat());
		

	    } else {
		System.out
			.println("WARNING: Learning step could not be completed successfully, skipping to next.");
		
	    }
	    
	// cleanup and temporary output
	//subsetTreesFile.delete();
	//Process p = Runtime.getRuntime().exec("rm -r hadoop_output1");
	//p.waitFor();
	//p.destroy();
	//p = Runtime.getRuntime().exec("rm /home/public/temp.trees");
	//p.waitFor();
	//p.destroy();	

	    long stop = System.currentTimeMillis();
	    System.out.println("End of " + i + suffix+" iteration: [" + (stop - start)
		    / 1000 + " sec]");
	}
	temporaryTrees.delete();
	//rules.delete();
	int i = 0;
	writer = new Writer(outputRuleFile, false);
	for (Rule r: this.rulebase){
	    r.setRuleID(++i);
	    writer.write(r);
	}
	writer.close();
	System.out.println(this.rulebase.size());
	//Process p = Runtime.getRuntime().exec("hadoop fs -rm hadopp_output1");
	//p.waitFor();
	//p.destroy();
	
	long stopRun = System.currentTimeMillis();
	System.out.println("Done! [" + (stopRun - startRun) / 1000 + " sec]");

    }

    /**
     * main
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
	if (args.length == 9) {
	    MasterLearner master = new MasterLearner();
	    master.run(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]), Integer.parseInt(args[7]), args[8]);
	} else {
	    System.out.println("Usage: (string) trees_file, (int) number of iterations, (int) size of learning set, (int) size of scoring set, (int) maximum crossing score, (int) number of trials, (int) minimum matching features, (int) parallel threads, (string) output file for rules");
	}
    } 
}
