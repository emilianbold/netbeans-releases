/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.debug.actions;

import java.util.*;
import java.beans.*;

import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.jpda.*;
import org.netbeans.spi.debugger.*;

import org.openide.util.*;

import org.netbeans.modules.web.debug.Context;
import org.netbeans.modules.web.debug.util.Utils;
import org.netbeans.modules.web.debug.breakpoints.JspLineBreakpoint;

/** 
 * Toggle JSP Breakpoint action provider.
 *
 * @author Martin Grebac
 */
public class JspToggleBreakpointActionProvider extends ActionsProviderSupport implements PropertyChangeListener {
    
    
    private JPDADebugger debugger;

    
    public JspToggleBreakpointActionProvider () {
        Context.addPropertyChangeListener (this);
    }
    
    public JspToggleBreakpointActionProvider (ContextProvider contextProvider) {
        debugger = (JPDADebugger) contextProvider.lookupFirst 
                (null, JPDADebugger.class);
        debugger.addPropertyChangeListener (debugger.PROP_STATE, this);
        Context.addPropertyChangeListener (this);
    }
    
    private void destroy () {
        debugger.removePropertyChangeListener (debugger.PROP_STATE, this);
        Context.removePropertyChangeListener (this);
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        boolean isJsp = Utils.isJsp(Context.getCurrentURL());
        setEnabled(DebuggerManager.ACTION_TOGGLE_BREAKPOINT, isJsp);
        if ( debugger != null && 
             debugger.getState () == debugger.STATE_DISCONNECTED
        ) 
            destroy ();
    }
    
    public Set getActions () {
        return Collections.singleton (DebuggerManager.ACTION_TOGGLE_BREAKPOINT);
    }
    
    public void doAction (Object action) {
        DebuggerManager d = DebuggerManager.getDebuggerManager ();
        
        // 1) get source name & line number
        int ln = Context.getCurrentLineNumber ();
        String url = Context.getCurrentURL ();
        if (url == null) return;
        
        // 2) find and remove existing line breakpoint
        Breakpoint[] bs = d.getBreakpoints ();
        int i, k = bs.length;
        for (i = 0; i < k; i++) {
            if (!(bs [i] instanceof JspLineBreakpoint)) continue;
            JspLineBreakpoint lb = (JspLineBreakpoint) bs [i];
            if (ln != lb.getLineNumber ()) continue;
            if (!url.equals (lb.getURL ())) continue;
            d.removeBreakpoint (lb);
            return;
        }
        Breakpoint b = JspLineBreakpoint.create(url, ln);
        if (b != null) {
            d.addBreakpoint(b);
        }
    }    
}
