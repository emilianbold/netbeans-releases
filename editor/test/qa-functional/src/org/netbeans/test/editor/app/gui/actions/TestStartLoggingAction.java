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

import org.netbeans.test.editor.app.core.cookies.LoggingCookie;
import org.netbeans.test.editor.app.gui.tree.TestNodeDelegate;

/**
 *
 * @author  ehucka
 * @version
 */
public class TestStartLoggingAction extends TreeNodeAction {
    
    private static String STARTLOG="Start logging";
    
    /** Creates new TestLogAction */
    public TestStartLoggingAction() {
    }
    
    public String getName() {
        return STARTLOG;
    }
    
    public boolean enable(TestNodeDelegate[] activatedNodes) {
        if (activatedNodes.length == 1) {
            LoggingCookie lc = (LoggingCookie) activatedNodes[0].getTestNode().getCookie(LoggingCookie.class);
            if (lc != null) {
                if (lc.isLogging()) {
                    return false;
                } else {
                    return true;
                }
            }
        }
        return false;
    }
    
    public void performAction(TestNodeDelegate[] activatedNodes) {
        LoggingCookie lc = (LoggingCookie) activatedNodes[0].getTestNode().getCookie(LoggingCookie.class);
        if (lc != null) {
            if (!lc.isLogging()) {
                lc.start();
            }
        }
    }
    
    public String getHelpCtx() {
        return "Start Logging - all events invoked in editor.";
    }
}
