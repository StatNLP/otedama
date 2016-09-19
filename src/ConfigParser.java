import java.util.Map;
import java.util.TreeMap;
import java.io.*;
import java.lang.*;

public class ConfigParser{
    
    private String trainingTreebankFile;
    private int initialSubsampleSize;
    public int maxRuleCrossingScore;
    public int minMatchingFeatures;
    public int parallelThreads;
    public String ruleOutputFile;
    public double minReductionFactor;
    public int windowSize;
    public boolean useFeatureSubsets;
    public boolean verboseLogging;
    public int maxWaitingTimeMins;
    
    public ConfigParser(){
    
    }
    
    public void parseConfig(String fileName){
        System.out.println("Parsing config file: "+fileName);
        String line;
        String[] tmp;
        Map<String,String> paramValues = new TreeMap<String,String>();
        Reader configFileReader = new Reader(fileName);
        while (true){
            line = configFileReader.getNext();
            if (line == null){
                break;
            }
            try {
                //Cut off comments:
                tmp = line.split("#");
                line = tmp[0];
                //Split line on '='
                tmp = line.split("=");
                paramValues.put(tmp[0].trim(),tmp[1].trim());
           } catch (Exception e){
                System.err.println("Formatting error in config file: "+fileName);
                System.exit(20);
           }
        }
        String[] params = {
        
            "TRAINING_TREEBANK_FILE",
            "INITIAL_SUBSAMPLE_SIZE",
            "MAX_RULE_CROSSING_SCORE",
            "MIN_MATCHING_FEATURES",
            "PARALLEL_THREADS",
            "RULE_OUTPUT_FILE",
            "MIN_REDUCTION_FACTOR",
            "WINDOW_SIZE",
            "USE_FEATURE_SUBSETS",
            "LOGGING",
            "MAX_WAITING_TIME_MINS",      
            
        };
        for (String param: params){
            if (!paramValues.containsKey(param)){
                System.err.println("Parameter "+ param+ " undefined in file "+fileName+".");
                System.exit(2);
            }
        }
        
        this.trainingTreebankFile = paramValues.get("TRAINING_TREEBANK_FILE");
        
        try {
            this.initialSubsampleSize = Integer.parseInt(paramValues.get("INITIAL_SUBSAMPLE_SIZE"));
        } catch (Exception e){
             //System.err.println("Invalid parameter value for INITIAL_SUBSAMPLE_SIZE (int needed).");
            System.exit(10);
        }
        
        try {
            this.maxRuleCrossingScore = Integer.parseInt(paramValues.get("MAX_RULE_CROSSING_SCORE"));
        } catch (Exception e){
            //System.err.println("Invalid parameter value for MAX_RULE_CROSSING_SCORE (int needed).");
            System.exit(11);
        }
        
        try {
            this.minMatchingFeatures = Integer.parseInt(paramValues.get("MIN_MATCHING_FEATURES"));
        } catch (Exception e) {
             //System.err.println("Invalid parameter value for MIN_MATCHING_FEATURES (int needed).");
            System.exit(12);
        }
        
        try {
            this.parallelThreads = Integer.parseInt(paramValues.get("PARALLEL_THREADS"));
        } catch (Exception e) {
             //System.err.println("Invalid parameter value for PARALLEL_THREADS (int needed).");
            System.exit(13);
        }
        
        this.ruleOutputFile = paramValues.get("RULE_OUTPUT_FILE");
        
        try {
            this.minReductionFactor = Double.parseDouble(paramValues.get("MIN_REDUCTION_FACTOR"));
        } catch (Exception e)  {
             //System.err.println("Invalid parameter value for MIN_REDUCTION_FACTOR (double needed).");
            System.exit(14);
        }
        
        try {
            this.windowSize = Integer.parseInt(paramValues.get("WINDOW_SIZE"));
        } catch (Exception e)  {
             //System.err.println("Invalid parameter value for WINDOW_SIZE (int needed).");
            System.exit(15);
        }
        
        if (paramValues.get("USE_FEATURE_SUBSETS").startsWith("y")){
            this.useFeatureSubsets = true;
        } else {
            this.useFeatureSubsets = false;
        }
        
        if (paramValues.get("LOGGING").startsWith("v")){
            this.verboseLogging = true;
        } else {
            this.verboseLogging = false;
        }
        
        try {
            this.maxWaitingTimeMins = Integer.parseInt(paramValues.get("MAX_WAITING_TIME_MINS"));
        } catch (Exception e) {
            //System.err.println("Invalid parameter value for MAX_WAITING_TIME_MINS (int needed).");
            System.exit(16);
        }
        
    }
    
    public String getTrainingTreebankFile(){
        return this.trainingTreebankFile;
    }
    
    public int getInitialSubsampleSize(){
        return this.initialSubsampleSize;
    }
    
    public int getMaxRuleCrossingScore(){
        return this.maxRuleCrossingScore;
    }
    
    public int getMinMatchingFeatures(){
        return this.minMatchingFeatures;
    }
    
    public int getParallelThreads(){
        return this.parallelThreads;
    }
    
    public String getRuleOutputFile(){
        return this.ruleOutputFile;
    }
    
    public double getMinReductionFactor(){
        return this.minReductionFactor;
    }
    
    public int getWindowSize(){
        return this.windowSize;
    }
    
    public boolean getUseFeatureSubsets(){
        return this.useFeatureSubsets;
    }
    
    public boolean getVerboseLogging(){
        return this.verboseLogging;
    }
    
    public int getMaxWaitingTimeMins(){
        return this.maxWaitingTimeMins;
    }
}