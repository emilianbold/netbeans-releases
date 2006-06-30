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

import org.netbeans.test.editor.app.core.*;
import org.netbeans.test.editor.app.gui.tree.TestNodeDelegate;

/**
 *
 * @author  ehucka
 * @version
 */
public class TestStopAction extends TreeNodeAction {

    /** Creates new TestStopAction */
    public TestStopAction() {
    }

    /** Perform the action based on the currently activated nodes.
     * Note that if the source of the event triggering this action was itself
     * a node, that node will be the sole argument to this method, rather
     * than the activated nodes.
     *
     * @param activatedNodes current activated nodes, may be empty but not <code>null</code>
     */
    public void performAction(TestNodeDelegate[] activatedNodes) {
        
        for(int i=0;i < activatedNodes.length;i++) {
            ((TestNodeDelegate)(activatedNodes[i])).getTestNode().stop();
        }
    }
    
    /** Test whether the action should be enabled based
     * on the currently activated nodes.
     *
     * @param activatedNodes current activated nodes, may be empty but not <code>null</code>
     * @return <code>true</code> to be enabled, <code>false</code> to be disabled
     */
    public boolean enable(TestNodeDelegate[] activatedNodes) {
        if (activatedNodes.length == 0) return false;
        if (((TestNodeDelegate)activatedNodes[0]).getTestNode().isPerfoming())
            return true;
        else
            return false;
    }
    
    public String getHelpCtx() {
        return "Stop running test";
    }
    
    public String getName() {
        return "Stop running";
    }
    
}
