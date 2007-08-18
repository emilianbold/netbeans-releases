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

package org.netbeans.modules.debugger.jpda.ui.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;


import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.modules.debugger.jpda.ui.EditorContextBridge;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

import org.openide.util.NbBundle;


/** 
 *
 * @author   Jan Jancura
 */
public class ToggleBreakpointActionProvider extends ActionsProviderSupport 
implements PropertyChangeListener {
    
    private JPDADebugger debugger;

    
    public ToggleBreakpointActionProvider () {
        EditorContextBridge.getContext().addPropertyChangeListener (this);
    }
    
    public ToggleBreakpointActionProvider (ContextProvider lookupProvider) {
        debugger = (JPDADebugger) lookupProvider.lookupFirst 
                (null, JPDADebugger.class);
        debugger.addPropertyChangeListener (debugger.PROP_STATE, this);
        EditorContextBridge.getContext().addPropertyChangeListener (this);
    }
    
    private void destroy () {
        debugger.removePropertyChangeListener (debugger.PROP_STATE, this);
        EditorContextBridge.getContext().removePropertyChangeListener (this);
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        String url = EditorContextBridge.getContext().getCurrentURL();
        FileObject fo;
        try {
            fo = URLMapper.findFileObject(new URL(url));
        } catch (MalformedURLException muex) {
            fo = null;
        }
        setEnabled (
            ActionsManager.ACTION_TOGGLE_BREAKPOINT,
            (EditorContextBridge.getContext().getCurrentLineNumber () >= 0) && 
            (fo != null && "text/x-java".equals(fo.getMIMEType()))  // NOI18N
            //(EditorContextBridge.getCurrentURL ().endsWith (".java"))
        );
        if ( debugger != null && 
             debugger.getState () == debugger.STATE_DISCONNECTED
        ) 
            destroy ();
    }
    
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_TOGGLE_BREAKPOINT);
    }
    
    public void doAction (Object action) {
        DebuggerManager d = DebuggerManager.getDebuggerManager ();
        
        // 1) get source name & line number
        int ln = EditorContextBridge.getContext().getCurrentLineNumber ();
        String url = EditorContextBridge.getContext().getCurrentURL ();
        if ("".equals (url.trim ())) return;
        
        // 2) find and remove existing line breakpoint
        LineBreakpoint lb = findBreakpoint(url, ln);
        if (lb != null) {
            d.removeBreakpoint (lb);
            return;
        }
//        Breakpoint[] bs = d.getBreakpoints ();
//        int i, k = bs.length;
//        for (i = 0; i < k; i++) {
//            if (!(bs [i] instanceof LineBreakpoint)) continue;
//            LineBreakpoint lb = (LineBreakpoint) bs [i];
//            if (ln != lb.getLineNumber ()) continue;
//            if (!url.equals (lb.getURL ())) continue;
//            d.removeBreakpoint (lb);
//            return;
//        }
        
        // 3) create a new line breakpoint
        lb = LineBreakpoint.create (
            url,
            ln
        );
        lb.setPrintText (
            NbBundle.getBundle (ToggleBreakpointActionProvider.class).getString 
                ("CTL_Line_Breakpoint_Print_Text")
        );
        d.addBreakpoint (lb);
    }
    
    static LineBreakpoint findBreakpoint (String url, int lineNumber) {
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
        for (int i = 0; i < breakpoints.length; i++) {
            if (!(breakpoints[i] instanceof LineBreakpoint)) {
                continue;
            }
            LineBreakpoint lb = (LineBreakpoint) breakpoints[i];
            if (!lb.getURL ().equals (url)) continue;
            if (lb.getLineNumber() == lineNumber) {
                return lb;
            }
        }
        return null;
    }
    
}
