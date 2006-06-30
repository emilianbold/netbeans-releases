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
