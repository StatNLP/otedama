import java.util.List;
import java.util.ArrayList;

public class RuleApplication extends Thread{
    private Tree[] treebank;
    private Tree[] hypTreebank;
    private Rule ruleToApply;
    private int minimumMatchingFeatures;
    private int fromIndex;
    private int toIndex;
    
    public RuleApplication(Tree[] treebank, Tree[] hypTreebank, Rule r, int minimumMatchingFeatures, int fromIndex, int toIndex){
        this.treebank = treebank;
        this.hypTreebank = hypTreebank;
        this.ruleToApply = r;
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
        this.minimumMatchingFeatures = minimumMatchingFeatures;
    }
    
    public void run(){
        boolean ruleApplies;
        Tree t;
        Tree newTree = new Tree();
        for (int i=this.fromIndex; i< this.toIndex; i++){
            t = this.treebank[i];
            ruleApplies = t.isRuleApplicable(this.ruleToApply, this.minimumMatchingFeatures);
            if (ruleApplies){
                newTree = newTree.fromTreeFormat(t.toTreeFormat());
                newTree.applyRuleInPlace(this.ruleToApply, this.minimumMatchingFeatures);
                this.hypTreebank[i] = newTree;
            } else {
                this.hypTreebank[i] = t;
            }
        }
    }
}