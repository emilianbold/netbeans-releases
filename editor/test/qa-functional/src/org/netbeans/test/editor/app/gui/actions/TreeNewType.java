/*
 * TreeNewType.java
 *
 * Created on November 14, 2002, 3:54 PM
 */

package org.netbeans.test.editor.app.gui.actions;

import org.netbeans.test.editor.app.core.TestGroup;
import org.netbeans.test.editor.app.gui.tree.TestNodeDelegate;

/**
 *
 * @author  eh103527
 */
public abstract class TreeNewType extends TreeNodeAction {
    
    /** Creates a new instance of TreeNewType */
    public TreeNewType() {
    }
    
    
    public boolean enable(TestNodeDelegate[] activatedNodes) {
        return true;
    }
    
    public void performAction(TestNodeDelegate[] activatedNodes) {
        if (activatedNodes.length == 1) {
            create((TestGroup)(activatedNodes[0].getTestNode()));
        }
    }
    
    public abstract void create(TestGroup group);
}
