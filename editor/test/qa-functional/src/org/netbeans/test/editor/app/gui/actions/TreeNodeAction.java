/*
 * TreeNodeAction.java
 *
 * Created on November 13, 2002, 6:09 PM
 */

package org.netbeans.test.editor.app.gui.actions;

import org.netbeans.test.editor.app.gui.tree.TestNodeDelegate;

/**
 *
 * @author  eh103527
 */
public abstract class TreeNodeAction {
    
    /** Creates a new instance of TreeNodeAction */
    public TreeNodeAction() {
    }
    
    public abstract boolean enable(TestNodeDelegate[] activatedNodes);
    public abstract void performAction(TestNodeDelegate[] activatedNodes);
    public abstract String getHelpCtx();
    public abstract String getName();        
}
