/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.debugger.gdb.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.BreakpointAnnotationListener;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.EditorContextBridge;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.GdbBreakpoint;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.LineBreakpoint;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author gordonp
 */
public class ToggleBreakpointActionProvider extends ActionsProviderSupport implements PropertyChangeListener {
    
    private GdbDebugger debugger;    
    private BreakpointAnnotationListener breakpointAnnotationListener;
    
    /** Creates a new instance of ToggleBreakpointActionProvider */
    public ToggleBreakpointActionProvider() {
        EditorContextBridge.getContext().addPropertyChangeListener(this);
    } 
    
    /** Creates a new instance of ToggleBreakpointActionProvider */
    public ToggleBreakpointActionProvider(ContextProvider lookupProvider) {
        debugger = (GdbDebugger) lookupProvider.lookupFirst(null, GdbDebugger.class);
        EditorContextBridge.getContext().addPropertyChangeListener(this);       
    }
    
    public void doAction(Object o) {
        DebuggerManager d = DebuggerManager.getDebuggerManager();
        
        // 1) get source name & line number
        int ln = EditorContextBridge.getContext().getCurrentLineNumber();
        String url = EditorContextBridge.getContext().getCurrentURL();
        if (url.trim().equals ("")) {
            return;
        }
        
        // 2) find and remove existing line breakpoint
        GdbBreakpoint lb = getBreakpointAnnotationListener().findBreakpoint(url, ln);
        if (lb != null) {
            d.removeBreakpoint(lb);
            return;
        }
        
        // 3) create a new line breakpoint
        lb = LineBreakpoint.create(url, ln);
        lb.setPrintText(
            NbBundle.getBundle(ToggleBreakpointActionProvider.class).getString("CTL_Line_Breakpoint_Print_Text")
        );
        d.addBreakpoint(lb);
    }
    
    private BreakpointAnnotationListener getBreakpointAnnotationListener() {
        if (breakpointAnnotationListener == null) {
            breakpointAnnotationListener = (BreakpointAnnotationListener) 
                DebuggerManager.getDebuggerManager().lookupFirst(null, BreakpointAnnotationListener.class);
        }
        return breakpointAnnotationListener;
    }
    
    public Set getActions() {
        return Collections.singleton(ActionsManager.ACTION_TOGGLE_BREAKPOINT);
    }
        
    public void propertyChange(PropertyChangeEvent evt) {
        int lnum = EditorContextBridge.getContext().getCurrentLineNumber();
        String mimeType = EditorContextBridge.getContext().getCurrentMIMEType();
	boolean isValid = (mimeType.equals("text/x-c") // NOI18N
                        || mimeType.equals("text/x-c++")) // NOI18N
                        && lnum > 0;
        setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, isValid);
    }
}
