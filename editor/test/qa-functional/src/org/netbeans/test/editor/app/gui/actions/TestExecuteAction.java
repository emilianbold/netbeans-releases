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

import org.netbeans.test.editor.app.core.cookies.PerformCookie;
import org.netbeans.test.editor.app.gui.tree.TestNodeDelegate;

/**
 *
 * @author  ehucka
 * @version
 */
public class TestExecuteAction extends TreeNodeAction {
    
    /** Creates new TestExecuteAction */
    public TestExecuteAction() {
    }
    
    public String getHelpCtx() {
        return "Perform action or all subactions";
    }
    
    public String getName() {
        return "Execute";
    }
    
    public boolean enable(TestNodeDelegate[] activatedNodes) {
        if (activatedNodes.length == 0) {
            return false;
        } else {
            boolean ret=true;
            for (int i=0;i < activatedNodes.length;i++) {
                PerformCookie pc = (PerformCookie) (activatedNodes[i].getTestNode().getCookie(PerformCookie.class));
                
                if (pc != null && !pc.isPerforming()) {
                    ret=true;
                } else {
                    return false;
                }
            }
            return ret;
        }
    }
    
    public void performAction(TestNodeDelegate[] activatedNodes) {
	for(int i=0;i < activatedNodes.length;i++) {
	    PerformCookie pc = (PerformCookie) activatedNodes[i].getTestNode().getCookie(PerformCookie.class);
	    
	    if (pc != null) {
		pc.perform();
	    }
	}
    }
    
}
