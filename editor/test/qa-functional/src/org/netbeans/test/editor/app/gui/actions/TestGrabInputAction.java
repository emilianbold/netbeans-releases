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
