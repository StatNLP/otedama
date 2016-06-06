import java.util.Vector;
import java.util.List;

public class TreeCopyThread extends Thread{
    private Tree[] treebank;
    private Tree[] copyTreebank;
    private int fromIndex;
    private int toIndex;
    
    public TreeCopyThread(Tree[] treebank, Tree[] copyTreebank, int fromIndex, int toIndex){
        this.treebank = treebank;
        this.copyTreebank = copyTreebank ;
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
    }
    
    public void run(){
        Tree t_old = new Tree();
        Tree t_new = new Tree();
        for (int i = this.fromIndex; i < this.toIndex; i++){
            t_old = this.treebank[i];
            t_new = t_new.fromTreeFormat(t_old.toTreeFormat());
            this.copyTreebank[i] = t_new;
        }
    }
}