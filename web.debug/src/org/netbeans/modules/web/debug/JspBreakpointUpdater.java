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

package org.netbeans.modules.web.debug;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LazyActionsManagerListener;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.spi.debugger.ContextProvider;

/**
 * @author Martin Grebac
 */
public class JspBreakpointUpdater extends LazyActionsManagerListener {
    
    private JPDADebugger debugger;
    
    public JspBreakpointUpdater (ContextProvider lookupProvider) {
        JPDADebugger debugger = (JPDADebugger) lookupProvider.lookupFirst (
            null, JPDADebugger.class
        );
        this.debugger = debugger;
        Context.createTimeStamp (debugger);
        JspBreakpointAnnotationListener bal = (JspBreakpointAnnotationListener) 
            DebuggerManager.getDebuggerManager ().lookupFirst 
            (null, JspBreakpointAnnotationListener.class);
        bal.updateJspLineBreakpoints ();
    }
    
    protected void destroy () {
        Context.disposeTimeStamp (debugger);
    }
    
    public String[] getProperties () {
        return new String [0];
    }
}
