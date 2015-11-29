import java.util.*;
import java.io.*;

public class BatchReorder{

	private int batchSize;
	private String treebankFile;
	private String rulesFile;
	private String outputTreebankFile;
	private String outputSurfaceFile;
	private Vector<Tree> inputBatch;
	private Vector<Tree> outputBatch;
	private Vector<Tree> processingBatch;
	private List<Rule> rulebase;
	private boolean done;
	private Reader treebankReader;
	private Writer outputTreebankWriter;
	private Writer surfaceWriter;
	private List<ReorderJob> reorderJobs;
	private int minimumMatchingFeatures;
	

	public BatchReorder(
		String treebankFile, 
		String rulesFile, 
		String outputTreebankFile, 
		String outputSurfaceFile, 
		int batchSize, 
		int minimumMatchingFeatures){
		
		this.treebankFile = treebankFile;
		this.rulesFile = rulesFile;
		this.outputTreebankFile = outputTreebankFile;
		this.outputSurfaceFile = outputSurfaceFile;
		this.batchSize = batchSize;
		this.inputBatch = new Vector<Tree>();
		this.outputBatch = new Vector<Tree>();
		this.processingBatch = new Vector<Tree>();
		this.rulebase = new LinkedList<Rule>();
		this.treebankReader = new Reader(this.treebankFile);
		this.outputTreebankWriter = new Writer(this.outputTreebankFile);
		this.surfaceWriter = new Writer(this.outputSurfaceFile);
		this.reorderJobs = new LinkedList<ReorderJob>();
		this.done = false;
		this.minimumMatchingFeatures = minimumMatchingFeatures;
		
		Reader rulesFileReader = new Reader(rulesFile);
		while (true){
			System.out.println("Adding rule to rulebase:");
			Rule nextRule = rulesFileReader.readNextRule();
			if (nextRule == null){
				break;
			}
			this.rulebase.add(nextRule);
		}
	}

	public void fillInputBatch(){
		this.inputBatch = new Vector<Tree>();
		while (this.inputBatch.size() < this.batchSize){
			Tree nextTree = this.treebankReader.readNextTree();
			if (nextTree == null){
				break;
			}
			this.inputBatch.add(nextTree);
		}
		
		
		if (this.inputBatch.size() == 0){
			this.done = true;
		} else {
			System.out.println("Input batch contains " + this.inputBatch.size() + " trees.");
		 	this.done = false;
		}
		
	}

	public void writeOutputBatch(){
		String treeSurface;
		if (this.outputBatch.size() != 0){
			System.out.println("Writing output batch containing " + this.outputBatch.size() + " trees.");
		}
		for (Tree tree : this.outputBatch){
			
			this.outputTreebankWriter.write(tree);
			this.surfaceWriter.write(tree.toSentence());
			//tree.recursivePrint();
		}
	}
		
	public void run() throws InterruptedException {
		System.out.println("Batch reordering in progress.");
		ReorderJob j;
		
		while (true){
			
			this.processingBatch = this.inputBatch;
			this.reorderJobs = new LinkedList<ReorderJob>();
			//Reorder all trees in processingBatch.
			
			for (Tree tree : this.processingBatch){
				j = new ReorderJob(tree, this.rulebase, this.minimumMatchingFeatures);
				j.run();
				this.reorderJobs.add(j);
			}
			this.fillInputBatch();
			this.writeOutputBatch();
			System.out.println(this.rulebase.size());
			//Wait for reordering to complete.

			for (ReorderJob i: this.reorderJobs){
				i.join();
			}
			this.outputBatch = this.processingBatch;
			
			if (this.done) {
				this.writeOutputBatch();
				this.outputTreebankWriter.close();
				this.surfaceWriter.close();
				System.out.println("Done.");			
				break;
			}
		}
		
	}

    /**
     * main
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
	if (args.length == 6) {
	    BatchReorder batchReorder = new BatchReorder(args[0], args[1], args[2], args[3], Integer.parseInt(args[4]), Integer.parseInt(args[5]));
	    batchReorder.run();
	} else {
	    System.out.println("Usage: (string) trees file, (string) rules files, (string) output trees file, (string) output file, (int) batch size, (int) minimum matching features");
	}
    }


}
