/*
 * TestNodeDelegate.java
 *
 * Created on November 12, 2002, 3:48 PM
 */

package org.netbeans.test.editor.app.gui.tree;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.netbeans.test.editor.app.Main;
import org.netbeans.test.editor.app.core.TestNode;
import org.netbeans.test.editor.app.gui.TreeDialog;

/**
 *
 * @author  eh103527
 */
public class TestNodeDelegate extends DefaultMutableTreeNode implements PropertyChangeListener {
    
    public TestNodeDelegate(TestNode bean) {
        this(bean,false);
    }
    
    public TestNodeDelegate(TestNode bean,boolean allowsChildren) {
        super(bean,allowsChildren);
        bean.addPropertyChangeListener(this);
    }
    
    public void destroy() {
        ((TestNode)getUserObject()).delete();
    }
    
    public boolean canDestroy() {
        return true;
    }
    
    public TestNode getTestNode() {
        return (TestNode)getUserObject();
    }
    
    public String toString() {
        return getTestNode().getName();
    }
    
    /** This method gets called when a bound property is changed.
     * @param evt A PropertyChangeEvent object describing the event source
     *   	and the property that has changed.
     *
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(TestNode.CHANGE_NAME)) {
            TreeDialog dlg=Main.frame.getTree();
            if (dlg != null) {
                DefaultTreeModel model=(DefaultTreeModel)(dlg.getTreeModel());
                model.nodeChanged(this);
            }
        }
    }
    
}
