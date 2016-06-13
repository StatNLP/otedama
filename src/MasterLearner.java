import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.io.*;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.lang.Thread;
import java.util.Collections;

public class MasterLearner {
	
    private Random randomGenerator;
    private Tree[] treebank;
    //private Vector<Tree> testTreebank;
    private Vector<Rule> rulebase;
    private AtomicInteger threadCounter;
    private boolean useTestTreebank;
    private static final int ARRAY_SIZE = 100000;
    private Set<String> rulesChecked;
    
    public MasterLearner() {
	this.randomGenerator = new Random();
	this.treebank = new Tree[ARRAY_SIZE];
	//this.testTreebank = new Vector<Tree>();
	this.rulebase = new Vector<Rule>();
	this.threadCounter = new AtomicInteger(0);
	this.useTestTreebank = true;
	this.rulesChecked = new HashSet<String>();
	
    }
    public Tree[] copyTreebank(Tree[] inputTreebank, int maximumParallelThreads, int inputSize){
        //int inputSize = ARRAY_SIZE;
        Tree[] outputTreebank = new Tree[ARRAY_SIZE];
        //Tree t = new Tree();
        //for (int i = 0; i < inputSize; i++){
        //    outputTreebank.add(i, t);
        //}
        int batchSize = (inputSize / maximumParallelThreads) + 2;
        System.out.println("Nominal batch size: "+batchSize);
        int previousBatchLimit = 0;
        int currentBatchLimit = batchSize;
        int effectiveBatchSize = 0;
        try{ 
            Vector<TreeCopyThread> copyThreads = new Vector<TreeCopyThread>();
            Tree newTree;
            TreeCopyThread tct;
            while(currentBatchLimit < inputSize){
                tct = new TreeCopyThread(inputTreebank, outputTreebank, previousBatchLimit, currentBatchLimit);
                tct.start();
                copyThreads.add(tct);
                previousBatchLimit = currentBatchLimit;
                currentBatchLimit += batchSize;
                effectiveBatchSize = currentBatchLimit - previousBatchLimit;
                System.out.println("Batch:"+effectiveBatchSize);
            }
            tct = new TreeCopyThread(inputTreebank, outputTreebank, previousBatchLimit, inputSize);
            tct.start();
            copyThreads.add(tct);
            effectiveBatchSize = inputSize - previousBatchLimit;
            System.out.println("Batch:"+effectiveBatchSize);
            for (TreeCopyThread tct1 : copyThreads){
                tct1.join();
                //outputTreebank.addAll(tct1.getCopies());
            }
        } catch (Exception e) {
            System.err.println("Could not copy treebank!");
        }
        return outputTreebank;
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

    public void readTreeFile(String treeFile, Tree[] treebank) {
	int line_counter = 1;
	try {
	    BufferedReader treeBuffer = new BufferedReader(new FileReader(treeFile));
	    Tree tree = new Tree();
	    String stringTree = treeBuffer.readLine();
	    int i = 0;
	    while (stringTree != null) {
                if (i == ARRAY_SIZE){
                    System.err.println("Treebank file "+treeFile+" is too large. Only a maximum of "+ARRAY_SIZE+" entries are allowed. Consider using smaller file or recompiling with larger ARRAY_SIZE.");
                    System.exit(5);
                }
		try {
			treebank[i] = tree.fromTreeFormat(stringTree);
		} catch (Exception e) {
			//System.err.println("WARNING: Could not read line "+line_counter+" :"+stringTree);
		}
		stringTree = treeBuffer.readLine();
		line_counter++;
		i++;
	    }
	    treeBuffer.close();
	} catch (Exception e) {
	    e.printStackTrace();
	    System.out.println("Failed to read :" + treeFile);
	    System.exit(4);
	}
    }

    public void readRulesFile(String rulesFile, Vector<Rule> rulesList) {
	try {
	    BufferedReader rulesBuffer = new BufferedReader(new FileReader(
		    rulesFile));
	    String stringRule = rulesBuffer.readLine();
	    while (stringRule != null) {
		Rule r = new Rule(stringRule);
		if (r.getAction().size() > 0) {
		    rulesList.add(r);
		}
		stringRule = rulesBuffer.readLine();
	    }
	    rulesBuffer.close();
	} catch (Exception e) {
	    e.printStackTrace();
	    System.out.println("Failed to read :" + rulesFile);
	    System.exit(3);
	}
    }

    public Vector<Tree> generateSubsetNoUseless(Tree[] treeList, int length, Vector<Tree> output){
    	int treebankLength = 0;
    	//System.out.println("Treebank length:"+treebankLength);
    	//Vector <Tree> output = new Vector<Tree>();
    	Tree t;
    	int n = 0;
        int index;
	for (Tree tree: this.treebank){
                if (tree == null){
                    break;
                }
                treebankLength++;
		tree.setAdded(false);
	}
    	while (output.size() < length){
		index = randomGenerator.nextInt(treebankLength);
		t = this.treebank[index];
		if (!t.isUseless() && !t.hasBeenAdded()){
			output.add(t);
			t.setAdded(true);
        	}
		n = n + 1;
		if (n > treebankLength){
			break;
		}
    	}
	for (Tree tree: this.treebank){
                if (tree==null){
                    break;
                }
		tree.setAdded(false);
	}
	return output;
    }   
    
    public Vector<Tree> generateSubset(Tree[] treeList, int length, Vector<Tree> include){
    int treebankLength = ARRAY_SIZE;
    Vector <Tree> output = new Vector<Tree>();
    if (treebankLength > length){
        for (Tree t: treeList){
            if (t==null){
                break;
            }
            output.add(t);
        }
        int outputLength = output.size();
        while (outputLength > length){
            int index = randomGenerator.nextInt(outputLength);
            output.remove(index);
            outputLength = output.size();
        }
        output.addAll(include);
        return output;
    } else {
        for (Tree t: treeList){
            if (t == null){
                break;
            }
            output.add(t);
        } 
        output.addAll(include);
        return output;
    	}
    }  
    
    public Vector<Tree> generateRandomSubset(Tree[] treeList, int subsetLength) {
	Vector<Tree> subset = new Vector<Tree>();
	int treeListLength = ARRAY_SIZE;
	int index = 0;
	for (int i = 0; i < subsetLength; i++) {
	    index = this.randomGenerator.nextInt(treeListLength);
	    subset.add(treeList[index]);
	}
	return subset;
    }
    
    public Vector<Rule> readRulesFromDirectory(File directory, String filePrefix) {
	Vector<Rule> rules = new Vector<Rule>();
	File[] contentFiles = directory.listFiles();
	
	if (contentFiles != null) {
	    for (File f : contentFiles) {
		if (f.getName().startsWith(filePrefix)) {
		    readRulesFile(f.getAbsolutePath(), rules);
		}
	    }
	} else {	   
	    return rules;
	}
	return rules;
    }

    public void run(
		String originalTreeFile, 
		int n, 
		int learningSet, 
		int scoringSet, 
		int maxCrossingScore, 
		int trials, 
		int minimumMatchingFeatures,
		int maximumParallelThreads,
		String outputRuleFile,
		String testSetFile,
		int maximumOverallReduction,
		double minimumReductionFactor,
		int windowSize,
		String useSubsetOptionString,
		String useVerboseLoggingOption,
		int maxWaitingTimeMins) throws Exception {
	long maxWaitingTime = maxWaitingTimeMins * 60 * 1000;
	long start = System.currentTimeMillis();
	long stop = start;
	long lastRuleLearned = start;
	boolean useSubsets = false;
	if (useSubsetOptionString.startsWith("y")){
            useSubsets = true;
	} else {
            useSubsets = false;
	}
	boolean useVerboseLogging = false;
        if (useVerboseLoggingOption.startsWith("v")){
            useVerboseLogging = true;
        } else {
            useVerboseLogging = false;
        }
	//long startRun = System.currentTimeMillis();
	int learningSetScaled = learningSet;
	int scoringSetScaled = scoringSet;
	Vector<Integer> recordedCSPrevious = new Vector<Integer>(ARRAY_SIZE);
	//Vector<Integer> recordedCSCurrent = new Vector<Integer>(ARRAY_SIZE);
	Vector<RuleApplication> ruleThreads = new Vector<RuleApplication>(ARRAY_SIZE);
	RuleApplication application; 

	//read the original trees file to treebank
	readTreeFile(originalTreeFile, this.treebank);

	Tree[] hypTreebank = new Tree[ARRAY_SIZE];
	
	//create a temporary tress file and write the treebank to it, then refreshes the treebank from it

	
	File temporaryTrees = new File("temp.trees");
	temporaryTrees.createNewFile();
	Writer writer = new Writer(temporaryTrees.getName(), false);

	File temporaryTestTrees = new File("temp_test.trees");
	Writer testWriter = new Writer(temporaryTestTrees.getName(), false);

	int smallestDecimalPlace;
	String suffix;
	int treebankCS = 0;
	int newTreebankCS = 0;
	int testTreebankCS = 0;
	int newTestTreebankCS = 0;
	int treebankSize = 0;

	// compute and report training treebank crossing score

	for (Tree t : this.treebank){
                if (t==null){
                    break;
                }
                treebankSize++;
		//t.fillAlignmentVector();
		newTreebankCS += t.getCrossingScore();
	}
	
	System.out.println("Size of treebank:"+treebankSize);
        //System.out.println("Size of test set treebank:"+this.testTreebank.size());
        System.out.println("Maximum crossing score:"+maxCrossingScore);
        System.out.println("Minimum matching features:"+minimumMatchingFeatures);
	
	Writer cslogger_train = new Writer(outputRuleFile+".cs.log", true);
	cslogger_train.write(Integer.toString(newTreebankCS));
	cslogger_train.close();
	System.out.println("Treebank crossing score:"+newTreebankCS);
	treebankCS = newTreebankCS;
		
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
                     
            // write the new treebank to a new temporary trees files
	    temporaryTrees = new File("temp.trees");
	    temporaryTrees.createNewFile();
	    writer = new Writer(temporaryTrees.getName(), false);
	    for (Tree t : this.treebank) {
                if (t==null){
                    break;
                }
		writer.write(t);
	    }
	    writer.close();

	    System.out.println("============================");
	    stop = System.currentTimeMillis();
	    System.out.println("[" + (stop - start) / 1000 + " sec]: Start of " + i + suffix+" iteration.");
	    //long start = System.currentTimeMillis();
	    
	    Vector<Tree> randomSubset = new Vector<Tree>();
	    Vector<Tree> evalTreebank = new Vector<Tree>();
	    
	    //Generate random subset of trees on which to learn rules	    
	    
	    randomSubset = generateSubsetNoUseless(this.treebank, learningSetScaled, randomSubset);
	    System.out.println("Size of the training subset:"+randomSubset.size());
	    
	    // Create an evaluation subset 
	    
	    evalTreebank = generateSubset(this.treebank, scoringSetScaled, randomSubset);
	    System.out.println("Size of the evaluation subset:"+evalTreebank.size());

	    // Run learners on subset.trees (one learner per tree). 
	    System.out.println("Size of treebank:"+treebankSize);
	    System.out.println("Maximum crossing score:"+maxCrossingScore);
	    System.out.println("Minimum matching features:"+minimumMatchingFeatures);
            Vector<Vector<Rule>> learnedRules = new Vector<Vector<Rule>>();

            Vector<RuleLearner> ruleLearners = new Vector<RuleLearner>();

            for (Tree t: randomSubset){

		while (this.threadCounter.get() > maximumParallelThreads - 1){
			Thread.sleep(100);	
		}

		this.incrementThreadCounter();
		RuleLearner ruleLearner = new RuleLearner(t, evalTreebank, learnedRules, maxCrossingScore, minimumMatchingFeatures, this, windowSize, useSubsets);
		ruleLearners.add(ruleLearner);
		ruleLearner.start();
            }

            for (RuleLearner l: ruleLearners){
                l.join();
            }
    
	    // Select the new rule with the smallest crossing score.
	    boolean success = false;
	    boolean scoresConsistent = true;
	    boolean varianceFail = false;
	    int treeCounter = 0;
	    Tree treeCopy = new Tree();
	    int delta = 0;
	    int ruleCount = 0;
	    int treeCS = 0;
	    int countReduction = 0;
	    int countIncrease = 0;
	    int c = 0;
	    int batchSize = 0;
	    int previousBatchLimit = 0;
	    int currentBatchLimit = 0;
	    long mark;
	    long current;
	    long time;
	    String ruleString;
	    
	    Vector<Rule> ruleCacheValid = new Vector<Rule>();
	    if (learnedRules.size() != 0) {
		success = true;
	    }
	    if (success){
		
		Double minScore = null;
		ruleCount = 0;
		for (Vector<Rule> ruleList : learnedRules){
                    if (useVerboseLogging){
                        stop = System.currentTimeMillis();
                        System.out.println("[" + (stop - start) / 1000 + " sec]: CADIDATE RULES:"+ruleList.size());
                    };
                    for (Rule rule: ruleList){
                        ruleString = rule.toString();
                        if (this.rulesChecked.contains(ruleString)){
                            stop = System.currentTimeMillis();
                            if (stop - lastRuleLearned > maxWaitingTime){
                                System.out.println("[" + (stop - start) / 1000 + " sec]: Maximum waiting time elapsed! Stopping training.");
                                System.exit(0);
                            }
                            if (useVerboseLogging){
                                System.out.println("[" + (stop - start) / 1000 + " sec]: WARNING: Learning step could not be completed successfully, skipping to next. (Rule seen, rule:"+ruleString+")");
                            }
                            continue;
                        }
                        this.rulesChecked.add(ruleString);
                    
			hypTreebank = new Tree[ARRAY_SIZE];
			recordedCSPrevious = new Vector<Integer>(ARRAY_SIZE);
			ruleThreads = new Vector<RuleApplication>();
			countIncrease = 0;
			countReduction = 0;
			varianceFail = false;
			current = System.currentTimeMillis();
			try {
				for (Tree t : this.treebank) {
                                        if (t==null){
                                            break;
                                        }
                                        
					recordedCSPrevious.add(t.getCrossingScore());
				}
				current = System.currentTimeMillis();
				batchSize = ARRAY_SIZE / maximumParallelThreads;
				previousBatchLimit = 0;
				currentBatchLimit = batchSize;
				while (currentBatchLimit < treebankSize){
                                        application = new RuleApplication(this.treebank, hypTreebank, rule, minimumMatchingFeatures, previousBatchLimit, currentBatchLimit);
                                        application.start();
                                        ruleThreads.add(application);
                                        previousBatchLimit = currentBatchLimit;
                                        currentBatchLimit += batchSize;
				}
				application = new RuleApplication(this.treebank, hypTreebank, rule, minimumMatchingFeatures, previousBatchLimit, treebankSize);
				application.start();
				ruleThreads.add(application);
				for (RuleApplication a: ruleThreads){
                                    a.join();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// compute and report training treebank crossing score
			newTreebankCS = 0;
			delta = 0;
			c = 0;
			for (Tree t : hypTreebank){
                                if (t==null){
                                    break;
                                }
				treeCS = t.getCrossingScore();
				newTreebankCS += treeCS;
				if (treeCS < recordedCSPrevious.get(c)){
				      countReduction++;
				} else if (treeCS > recordedCSPrevious.get(c)){
				      countIncrease++;
				}
				c ++;
			}
			rule.setScore("COUNT_REDUCTION", (double) countReduction);
			rule.setScore("COUNT_INCREASE", (double) countIncrease);
			delta = newTreebankCS - treebankCS;
			if (delta >= maximumOverallReduction || countIncrease * minimumReductionFactor > countReduction){
                                stop = System.currentTimeMillis();
                                if (stop - lastRuleLearned > maxWaitingTime){
                                    System.out.println("[" + (stop - start) / 1000 + " sec]: Maximum waiting time elapsed! Stopping training.");
                                    System.exit(0);
                                }
                                if (useVerboseLogging){                                  
                                    System.out.println("[" + (stop - start) / 1000 + " sec]: WARNING: Learning step could not be completed successfully, skipping to next. (No reduction, rule:"+ruleString+")");
                                }
				continue;
			}
			rule.setScore("CROSSING", (double) delta);
			stop = System.currentTimeMillis();
			System.out.println("[" + (stop - start) / 1000 + " sec]: Treebank crossing score:"+newTreebankCS+
				" (delta="+(delta)+")");
			cslogger_train = new Writer(outputRuleFile+".cs.log", true);
			cslogger_train.write(Integer.toString(newTreebankCS));
			cslogger_train.close();
			this.treebank = hypTreebank;
			treebankCS = newTreebankCS;

			// add the Rule to the rulebase
			this.rulebase.add(rule);
			writer = new Writer(outputRuleFile, true);
			
			writer.write(rule);
			writer.close();

			stop = System.currentTimeMillis();
			lastRuleLearned = stop;
			System.out.println("[" + (stop - start) / 1000 + " sec]: Learned new rule: "+rule.toRuleFormat());
			ruleCount++;
			break;
                    }
		}
		if (ruleCount < 20){
			//Scale up learning and scoring sets:
			learningSetScaled = learningSetScaled * 2;
			scoringSetScaled = scoringSetScaled * 4;
		} else if (ruleCount > 1000 && learningSetScaled > 10){
			//Scale down learning and scoring sets:
			learningSetScaled = learningSetScaled / 2;
			scoringSetScaled = scoringSetScaled / 2;
		}
	    } else {
		//Scale up learning and scoring sets:
		learningSetScaled = learningSetScaled * 2;
		scoringSetScaled = scoringSetScaled * 4;
		stop = System.currentTimeMillis();
		System.out.println("[" + (stop - start) / 1000 + " sec]: WARNING: Iteration could not be completed successfully, skipping to next.");
	    }
	    
	   // delete the previous temporary trees file, containing an outdated version of the treebank
	    temporaryTrees.delete();
	    
	    // write the new treebank to a new temporary trees files
	    temporaryTrees = new File("temp.trees");
	    temporaryTrees.createNewFile();
	    writer = new Writer(temporaryTrees.getName(), false);
	    for (Tree t : this.treebank) {
                if (t==null){
                    break;
                }
		writer.write(t);
	    }
	    writer.close();

	    this.treebank = new Tree[ARRAY_SIZE];
	    readTreeFile("temp.trees", this.treebank);
	    stop = System.currentTimeMillis();
	    System.out.println("[" + (stop - start) / 1000 + " sec]: End of " + i + suffix+" iteration. ");
	}
	temporaryTrees.delete();

	int i = 0;
	writer = new Writer(outputRuleFile+".final", false);
	for (Rule r: this.rulebase){
	    r.setRuleID(++i);
	    writer.write(r);
	}
	writer.close();
	
	stop = System.currentTimeMillis();
	System.out.println("[" + (stop - start) / 1000 + " sec]: Done!");

    }

    public static void main(String[] args) throws Exception {
	if (args.length == 16) {
	    MasterLearner master = new MasterLearner();
	    master.run(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]), Integer.parseInt(args[7]), args[8], args[9], Integer.parseInt(args[10]), Double.parseDouble(args[11]), Integer.parseInt(args[12]), args[13], args[14], Integer.parseInt(args[15]));
	} else {
	    System.out.println("Usage: (string) trees file, (int) number of iterations, (int) size of learning set, (int) size of scoring set, (int) maximum crossing score, (int) number of trials, (int) minimum matching features, (int) parallel threads, (string) output file for rules, (string) trees test file (or \"none\"), maximum overall reduction (int), minimum reduction factor (double), window size (int), use feature subsets (y/n), logging (v[erbose]/q[uiet]), (int [mins]) maximum waiting time");
	}
    } 
}
