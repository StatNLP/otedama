import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ReorderJob extends Thread {

	private Tree[] trees;
	private Rule[] rulebase;
	private int minimumMatchingFeatures;
	private int startIndex;
	private int endIndex;

	public ReorderJob (Tree[] trees, Rule[] rulebase, int minimumMatchingFeatures, int startIndex, int endIndex){
		this.trees = trees;
		this.rulebase = rulebase;
		this.minimumMatchingFeatures = minimumMatchingFeatures;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
	}

	public void run(){
            Tree t = trees[0];
            //System.out.println(this.startIndex+";"+this.endIndex);
            for (int i=this.startIndex; i < this.endIndex; i++){
                    if (trees[i] == null){
                        break;
                    }
                    t = this.trees[i];
                    //System.out.println(t.toTreeFormat());
                    for (Rule rule : this.rulebase) {
                        if (rule == null){
                            break;
                        }
                        t.applyRuleInPlace(rule, minimumMatchingFeatures); 
                    }		        
            }
	}
}
