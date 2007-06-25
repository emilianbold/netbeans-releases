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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.ui.actions;

import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.modules.debugger.jpda.ui.EditorContextBridge;
import org.netbeans.modules.debugger.jpda.ui.models.BreakpointsActionsProvider;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Customize action for line breakpoint, which is available from the gutter popup.
 *
 * @author Martin Entlicher
 */
public class BreakpointCustomizeAction extends NodeAction {
    
    /** Creates a new instance of BreakpointCustomizeAction */
    public BreakpointCustomizeAction() {
    }
    
    static LineBreakpoint getCurrentLineBreakpoint() {
        String currentURLStr = EditorContextBridge.getCurrentURL();
        if (currentURLStr == null) return null;
        URL currentURL;
        try {
            currentURL = new URL(currentURLStr);
        } catch (MalformedURLException muex) {
            return null;
        }
        int lineNumber = EditorContextBridge.getCurrentLineNumber();
        if (lineNumber < 0) return null;
        Breakpoint[] bs = DebuggerManager.getDebuggerManager ().
                getBreakpoints ();
        for (int i = 0; i < bs.length; i++) {
            if (bs[i] instanceof LineBreakpoint) {
                LineBreakpoint lb = (LineBreakpoint) bs[i];
                URL url;
                try {
                    url = new URL(lb.getURL());
                } catch (MalformedURLException muex) {
                    continue;
                }
                if (currentURL.equals(url)) {
                    if (lineNumber == lb.getLineNumber()) {
                        return lb;
                    }
                }
            }
        }
        return null;
    }
    
    public boolean isEnabled() { // overriden, because orig impl caches the nodes
        return enable(null);
    }

    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
        JPDABreakpoint b = getCurrentLineBreakpoint();
        if (b == null) {
            try {
                b = ToggleMethodFieldBreakpointAction.getCurrentFieldMethodBreakpoint();
            } catch (java.awt.IllegalComponentStateException icsex) {}
        }
        return b != null;
    }

    public String getName() {
        return NbBundle.getMessage(BreakpointCustomizeAction.class, "CTL_customize");
    }

    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
        JPDABreakpoint b = getCurrentLineBreakpoint();
        if (b == null) {
            try {
                b = ToggleMethodFieldBreakpointAction.getCurrentFieldMethodBreakpoint();
            } catch (java.awt.IllegalComponentStateException icsex) {}
        }
        if (b == null) return ;
        BreakpointsActionsProvider.customize(b);
    }
    
    protected boolean asynchronous() {
        return false; //This action should run in AWT.
    }
    
    public HelpCtx getHelpCtx() {
        return null;
    }
    
}
