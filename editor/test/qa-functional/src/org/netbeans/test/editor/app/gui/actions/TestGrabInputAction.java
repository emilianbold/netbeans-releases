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

import org.netbeans.test.editor.app.core.TestCallAction;
import org.netbeans.test.editor.app.gui.tree.TestNodeDelegate;

/**
 *
 * @author  ehucka
 * @version
 */
public class TestGrabInputAction extends TreeNodeAction {
    
    /** Creates new TestGrabInputAction */
    public TestGrabInputAction() {
    }
    
    public String getName() {
        return "Grab Input";
    }
    
    public boolean enable(TestNodeDelegate[] activatedNodes) {
        return true;
    }
    
    public String getHelpCtx() {
        return "Set actual editor content as Input to actual Call action.";
    }
    
    public void performAction(TestNodeDelegate[] activatedNodes) {
        TestNodeDelegate n;
        for(int i=0;i < activatedNodes.length;i++) {
            n=(TestNodeDelegate)activatedNodes[i];
            ((TestCallAction)(n.getTestNode())).grabInput();
        }
    }
    
}
