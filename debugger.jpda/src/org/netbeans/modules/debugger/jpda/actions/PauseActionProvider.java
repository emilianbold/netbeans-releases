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

package org.netbeans.modules.debugger.jpda.actions;

import java.util.Collections;
import java.util.Set;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LookupProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;


/**
* Representation of a debugging session.
*
* @author   Jan Jancura
* @author  Marian Petras
*/
public class PauseActionProvider extends JPDADebuggerActionProvider {
    
    public PauseActionProvider (LookupProvider lookupProvider) {
        super (
            (JPDADebuggerImpl) lookupProvider.lookupFirst 
                (JPDADebugger.class)
        );
    }
    
    public Set getActions () {
        return Collections.singleton (DebuggerManager.ACTION_PAUSE);
    }

    public void doAction (Object action) {
        ((JPDADebuggerImpl) getDebuggerImpl ()).suspend ();
    }
    
    protected void checkEnabled (int debuggerState) {
        setEnabled (
            DebuggerManager.ACTION_PAUSE,
            debuggerState == getDebuggerImpl ().STATE_RUNNING
        );
    }
}
