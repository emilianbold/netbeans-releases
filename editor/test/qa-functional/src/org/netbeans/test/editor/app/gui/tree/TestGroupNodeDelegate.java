/*
 * TestGroupNodeDelegate.java
 *
 * Created on November 12, 2002, 5:00 PM
 */

package org.netbeans.test.editor.app.gui.tree;

import org.netbeans.test.editor.app.core.TestGroup;
import org.netbeans.test.editor.app.core.TestNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import org.netbeans.test.editor.app.Main;
import org.netbeans.test.editor.app.gui.TreeDialog;

/**
 *
 * @author  eh103527
 */
public class TestGroupNodeDelegate extends TestNodeDelegate implements PropertyChangeListener {
    
    public TestGroupNodeDelegate(TestGroup bean) {
	super(bean, true);
    }
    
    /** This method gets called when a bound property is changed.
     * @param evt A PropertyChangeEvent object describing the event source
     *   	and the property that has changed.
     * CHANGE_CHILD = "Change child";
     * REMOVE_CHILD = "Remove node";
     * ADD_CHILD = "Add child";
     * UP_CHILD = "Up child";
     * DOWN_CHILD = "Down child";
     *
     */
    public void propertyChange(PropertyChangeEvent evt) {
	if (evt.getPropertyName().compareTo(TestGroup.ADD_CHILDS) == 0) {
	    TestNode[] nodes=(TestNode[])(evt.getNewValue());
	    int[] indices=new int[nodes.length];
	    TestNodeDelegate tnd;
	    
	    for (int i=0;i < nodes.length;i++) {
		tnd = (TestNodeDelegate)(nodes[i].getNodeDelegate());
		add(tnd);
		indices[i]=getNodeNumber(tnd);
	    }
	    TreeDialog dlg=Main.frame.getTree();
	    if (dlg != null) {
		DefaultTreeModel model=(DefaultTreeModel)(dlg.getTreeModel());
		model.nodesWereInserted(this,indices);
	    }
	} else if (evt.getPropertyName().compareTo(TestGroup.ADD_CHILD) == 0) {
	    TestNodeDelegate n=(TestNodeDelegate)(((TestNode)evt.getNewValue()).getNodeDelegate());
	    add(n);
	    TreeDialog dlg=Main.frame.getTree();
	    if (dlg != null) {
		DefaultTreeModel model=(DefaultTreeModel)(dlg.getTreeModel());
		model.nodesWereInserted(this,new int[] {getNodeNumber(n)});
	    }
	} else if (evt.getPropertyName().equals(TestGroup.REMOVE_CHILD)) {
	    TestNodeDelegate n=(TestNodeDelegate)(((TestNode)evt.getNewValue()).getNodeDelegate());
	    int i=getNodeNumber(n);
	    remove(n);
	    TreeDialog dlg=Main.frame.getTree();
	    if (dlg != null) {
		DefaultTreeModel model=(DefaultTreeModel)(dlg.getTreeModel());
		model.nodesWereRemoved(this,new int[] {i},new Object[] {n});
	    }
	} else if (evt.getPropertyName().equals(TestGroup.REMOVE_CHILDS)) {
	    TestNode[] nodes=(TestNode[])(evt.getNewValue());
	    TestNodeDelegate[] trees=new TestNodeDelegate[nodes.length];
	    int[] indices=new int[nodes.length];
	    int first;
	    for (int i=0;i < nodes.length;i++) {
		trees[i]=(TestNodeDelegate)(nodes[i].getNodeDelegate());
		first=getNodeNumber(trees[i]);
		remove(trees[i]);
		indices[i]=first;
	    }
	    TreeDialog dlg=Main.frame.getTree();
	    if (dlg != null) {
		DefaultTreeModel model=(DefaultTreeModel)(dlg.getTreeModel());
		model.nodesWereRemoved(this,indices,nodes);
	    }
	} else if (evt.getPropertyName().equals(TestGroup.CHANGE_CHILD)) {
	    TestNodeDelegate n=(TestNodeDelegate)(((TestNode)evt.getNewValue()).getNodeDelegate());
	    int i=getNodeNumber(n);
	    TreeDialog dlg=Main.frame.getTree();
	    if (dlg != null) {
		DefaultTreeModel model=(DefaultTreeModel)(dlg.getTreeModel());
		model.nodesChanged(this,new int[] {i});
	    }
	} else if (evt.getPropertyName().equals(TestGroup.UP_CHILD)) {
	    TestNodeDelegate n=(TestNodeDelegate)(((TestNode)evt.getNewValue()).getNodeDelegate());
	    int i=getNodeNumber(n);
	    insert(n, i-1);
	    TreeDialog dlg=Main.frame.getTree();
	    if (dlg != null) {
		DefaultTreeModel model=(DefaultTreeModel)(dlg.getTreeModel());
		model.nodeStructureChanged(this);
	    }
	} else if (evt.getPropertyName().equals(TestGroup.DOWN_CHILD)) {
	    TestNodeDelegate n=(TestNodeDelegate)(((TestNode)evt.getNewValue()).getNodeDelegate());
	    int i=getNodeNumber(n);
	    insert(n, i+1);
	    TreeDialog dlg=Main.frame.getTree();
	    if (dlg != null) {
		DefaultTreeModel model=(DefaultTreeModel)(dlg.getTreeModel());
		model.nodeStructureChanged(this);
	    }
	} else {
	    super.propertyChange(evt);
	}
    }
    
    private int getNodeNumber(TestNodeDelegate node) {
	return getIndex(node);
    }
    
}
