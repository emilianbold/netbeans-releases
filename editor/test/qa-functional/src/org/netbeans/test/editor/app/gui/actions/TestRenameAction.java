/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.test.editor.app.gui.actions;

import org.netbeans.test.editor.app.Main;
import org.netbeans.test.editor.app.core.TestNode;
import org.netbeans.test.editor.app.gui.RenameDialog;
import org.netbeans.test.editor.app.gui.actions.TreeNodeAction;
import org.netbeans.test.editor.app.gui.tree.TestNodeDelegate;

/**
 *
 * @author  ehucka
 * @version
 */
public class TestRenameAction extends TreeNodeAction {
    
    /** Creates new TestRenameAction */
    public TestRenameAction() {
    }
    
    public boolean enable(TestNodeDelegate[] activatedNodes) {
        if (activatedNodes.length == 1)
            return true;
        else
            return false;
    }
    
    public void performAction(TestNodeDelegate[] activatedNodes) {
        if (activatedNodes.length == 1) {
            TestNode node=activatedNodes[0].getTestNode();
            RenameDialog dlg=new RenameDialog(Main.frame,node.getName());
            dlg.show();
            if (dlg.getState()) {
                node.setName(dlg.getName());
            }
        }
    }
    
    public String getHelpCtx() {
        return "Rename selected node.";
    }
    
    public String getName() {
        return "Rename";
    }
    
}
