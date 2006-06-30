/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.test.editor.app.gui.actions;

import org.netbeans.test.editor.app.core.Test;
import org.netbeans.test.editor.app.core.TestNode;
import org.netbeans.test.editor.app.gui.actions.TreeNodeAction;
import org.netbeans.test.editor.app.gui.tree.TestNodeDelegate;

/**
 *
 * @author  ehucka
 * @version
 */
public class TestDeleteAction extends TreeNodeAction {

    /** Creates new TestRenameAction */
    public TestDeleteAction() {
    }

    public boolean enable(TestNodeDelegate[] activatedNodes) {
        TestNode n;
        for (int i=0;i < activatedNodes.length;i++) {
            n=(TestNode)(activatedNodes[i].getTestNode());
            if (n instanceof Test) {
                return false;
            }
        }
        return true;
    }
    
    public void performAction(TestNodeDelegate[] activatedNodes) {
        TestNode[] n=new TestNode[activatedNodes.length];
        
        for (int i=0;i < activatedNodes.length;i++) {
            n[i]=(TestNode)(activatedNodes[i].getTestNode());
        }
        n[0].getOwner().removeNodes(n);
    }
    
    public String getHelpCtx() {
        return "Delete selected nodes.";
    }
    
    public String getName() {
        return "Delete";
    }
    
}
