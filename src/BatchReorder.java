import java.util.Vector;
import java.io.*;

public class BatchReorder{

        private static final int RULEBASE_ARRAY_SIZE = 200000;
        private static final int BATCH_ARRAY_SIZE = 5000;
	private int batchSize;
	private String treebankFile;
	private String rulesFile;
	private String outputTreebankFile;
	private String outputSurfaceFile;
	private Tree[] inputBatch;
	private Tree[] outputBatch;
	private Tree[] processingBatch;
	private Rule[] rulebase;
	private boolean done;
	private Reader treebankReader;
	private Writer outputTreebankWriter;
	private Writer surfaceWriter;
	private Writer transformationWriter;
	private ReorderJob[] reorderJobs;
	private int minimumMatchingFeatures;
	private int parallelThreads;
	
	public BatchReorder(
		String treebankFile, 
		String rulesFile, 
		String outputTreebankFile, 
		String outputSurfaceFile, 
		int batchSize, 
		int minimumMatchingFeatures,
		int parallelThreads){
		if (batchSize > BATCH_ARRAY_SIZE){
                    System.err.println("Batch size too large. Maximum batch size: "+BATCH_ARRAY_SIZE);
                    System.exit(3);
		}
		this.treebankFile = treebankFile;
		this.rulesFile = rulesFile;
		this.outputTreebankFile = outputTreebankFile;
		this.outputSurfaceFile = outputSurfaceFile;
		this.batchSize = batchSize;
		this.inputBatch = new Tree[BATCH_ARRAY_SIZE];
		this.outputBatch = new Tree[BATCH_ARRAY_SIZE];
		this.processingBatch = new Tree[BATCH_ARRAY_SIZE];
		this.rulebase = new Rule[RULEBASE_ARRAY_SIZE];
		this.treebankReader = new Reader(this.treebankFile);
		this.outputTreebankWriter = new Writer(this.outputTreebankFile);
		this.surfaceWriter = new Writer(this.outputSurfaceFile);
		this.transformationWriter = new Writer(this.outputSurfaceFile+".transformations");
		this.reorderJobs = new ReorderJob[BATCH_ARRAY_SIZE];
		this.done = false;
		this.minimumMatchingFeatures = minimumMatchingFeatures;
		this.parallelThreads = parallelThreads;
		
		int c = 0;
		Reader rulesFileReader = new Reader(rulesFile);
		while (true){
			//System.out.println("Adding rule to rulebase:");
			Rule nextRule = rulesFileReader.readNextRule();
			if (nextRule == null){
				break;
			}
			if (c >= RULEBASE_ARRAY_SIZE){
                            System.err.println("Rule file too large! A maximum of "+RULEBASE_ARRAY_SIZE+ " rules are allowed. Try smaller rule file or recompile with larger RULEBASE_ARRAY_SIZE");
                            System.exit(2);
			}
			this.rulebase[c] = nextRule;
			c++;
		}
	}

	public void fillInputBatch(){
                int c = 0;
		this.inputBatch = new Tree[BATCH_ARRAY_SIZE];
		while (c < this.batchSize){
			Tree nextTree = this.treebankReader.readNextTree();
			if (nextTree == null){
				break;
			}
			nextTree.recordPositions();
			this.inputBatch[c] = nextTree;
			c++;
		}
		
		
		if (c == 0){
			this.done = true;
		} else {
			System.out.println("Input batch contains " + c + " trees.");
		 	this.done = false;
		}
		
	}

	public void writeOutputBatch(){
		String treeSurface;
		int c = 0;
		for (Tree tree : this.outputBatch){
                        if (tree == null){
                            break;
                        }
                        c++;
			this.outputTreebankWriter.write(tree);
			this.surfaceWriter.write(tree.toSentence());
			this.transformationWriter.write(tree.getTransformationString());
			//tree.recursivePrint();
		}
		if (c != 0){
                        System.out.println("Wrote output batch containing " + c + " trees.");
                }
	}
		
	public void run() throws InterruptedException {
		System.out.println("Batch reordering in progress.");
		ReorderJob j;
		
		while (true){
			
			this.processingBatch = this.inputBatch;
			this.reorderJobs = new ReorderJob[BATCH_ARRAY_SIZE];
			//Reorder all trees in processingBatch.
			int c = 0;
			int beginIndex = 0;
			int chunkSize =  batchSize/this.parallelThreads;
			int endIndex = chunkSize;
			//System.out.println("Started Reordering.");
			for (int i = chunkSize; i < batchSize+chunkSize; i += chunkSize){
                                System.out.println("Reordering range: "+beginIndex+":"+endIndex);
				j = new ReorderJob(this.processingBatch, this.rulebase, this.minimumMatchingFeatures, beginIndex, endIndex);
				j.start();
				this.reorderJobs[c] = j;
				c++;
				beginIndex = i;
                                endIndex = beginIndex + chunkSize; 
                                if (endIndex > batchSize){
                                    endIndex = batchSize;
                                }
			}
			this.fillInputBatch();
			this.writeOutputBatch();
			//System.out.println(this.rulebase.size());
			//Wait for reordering to complete.

			for (ReorderJob i: this.reorderJobs){
                                if (i == null){
                                    break;
                                }
				i.join();
			}
			this.outputBatch = this.processingBatch;
			
			if (this.done) {
				this.writeOutputBatch();
				this.outputTreebankWriter.close();
				this.surfaceWriter.close();
				this.transformationWriter.close();
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
	if (args.length == 7) {
	    BatchReorder batchReorder = new BatchReorder(args[0], args[1], args[2], args[3], Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]));
	    batchReorder.run();
	} else {
	    System.out.println("Usage: (string) trees file, (string) rules files, (string) output trees file, (string) output file, (int) batch size, (int) minimum matching features, (int) parallel threads");
	}
    }


}
