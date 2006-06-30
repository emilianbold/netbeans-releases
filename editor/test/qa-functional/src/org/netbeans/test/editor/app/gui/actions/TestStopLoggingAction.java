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

import org.netbeans.test.editor.app.core.cookies.LoggingCookie;
import org.netbeans.test.editor.app.gui.tree.TestNodeDelegate;

/**
 *
 * @author  ehucka
 * @version
 */
public class TestStopLoggingAction extends TreeNodeAction {

    private static String STOPLOG="Stop logging";
    /** Creates new TestLogAction */
    public TestStopLoggingAction() {
    }

    public String getName() {
        return STOPLOG;
    }

    public boolean enable(TestNodeDelegate[] activatedNodes) {
        if (activatedNodes.length == 1) {
            LoggingCookie lc = (LoggingCookie) activatedNodes[0].getTestNode().getCookie(LoggingCookie.class);
            if (lc != null) {
                if (lc.isLogging()) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }
    
    public void performAction(TestNodeDelegate[] activatedNodes) {
        for(int i=0;i < activatedNodes.length;i++) {
            LoggingCookie lc = (LoggingCookie) activatedNodes[i].getTestNode().getCookie(LoggingCookie.class);
            if (lc != null) {
                if (lc.isLogging()) {
                    lc.stop();
                }
            }
        }
    }
    
    public String getHelpCtx() {
        return "Stop Logging - write logged events to node.";
    }
}
