
import java.io.File;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JFrame;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelListener;

public class TreeModelExample1 implements TreeModel {
        
    public Object[] getChildren (Object parent, int from, int to) {
        if (parent == ROOT)
            return File.listRoots ();
        return ((File) parent).listFiles ();
    }
    
    public Object getRoot () {
        return ROOT;
    }
    
    public boolean isLeaf (Object node) {
        if (node == ROOT)
            return false;
        return ((File) node).isFile ();
    }
    
    public void addTreeModelListener (TreeModelListener l) {}
    public void removeTreeModelListener (TreeModelListener l) {}
    
    public static void main (String[] args) {
        TreeModelExample1 tme = new TreeModelExample1 ();
        JComponent ttv = Models.createView (
            tme,              // TreeModel
            null,             // NodeModel
            null,             // TableModel
            null,             // NodeActionsProvider
            new ArrayList ()  // list of ColumnModels
        );
        JFrame f = new JFrame ("Tree Model Example 1");
        f.getContentPane ().add (ttv);
        f.pack ();
        f.show ();
    }
}
