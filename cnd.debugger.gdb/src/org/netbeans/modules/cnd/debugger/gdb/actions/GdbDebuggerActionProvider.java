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

package org.netbeans.modules.cnd.debugger.gdb.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;

import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;

/**
* Representation of a debugging session.
*
* @author  Gordon Prieur (copied from Jan Jancura's and Marian Petras' JPDA implementation)
*/
abstract class GdbDebuggerActionProvider extends ActionsProviderSupport 
                implements PropertyChangeListener {
    
    private GdbDebugger debugger;
    
    private volatile boolean disabled;
    
    GdbDebuggerActionProvider(ContextProvider lookupProvider) {
        debugger = (GdbDebugger) lookupProvider.lookupFirst(null, GdbDebugger.class);
        this.debugger = debugger;
        debugger.addPropertyChangeListener(debugger.PROP_STATE, this);
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        checkEnabled(debugger.getState());
    }
    
    protected abstract void checkEnabled(String debuggerState);
    
    public boolean isEnabled(Object action) {
        if (!disabled) {
            checkEnabled(debugger.getState());
        }
        return super.isEnabled(action);
    }
    
    GdbDebugger getDebuggerImpl() {
        return debugger;
    }
    
    
}
