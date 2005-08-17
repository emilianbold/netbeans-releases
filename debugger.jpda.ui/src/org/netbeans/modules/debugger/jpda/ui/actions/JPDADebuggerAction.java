/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.ui.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.spi.debugger.ActionsProviderSupport;


/**
* Representation of a debugging session.
*
* @author   Jan Jancura
* @author  Marian Petras
*/
abstract class JPDADebuggerAction extends ActionsProviderSupport implements 
PropertyChangeListener {

    private JPDADebugger debugger;
    
    JPDADebuggerAction (JPDADebugger debugger) {
        this.debugger = debugger;
        debugger.addPropertyChangeListener (debugger.PROP_STATE, this);
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        try {
            checkEnabled (debugger.getState ());
        } catch (com.sun.jdi.VMDisconnectedException e) {
            // Causes kill action when something is being evaluated
        }
    }
    
    protected abstract void checkEnabled (int debuggerState);
    
    JPDADebugger getDebuggerImpl () {
        return debugger;
    }
}
