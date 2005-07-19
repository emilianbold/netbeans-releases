/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.actions;

import java.util.Collections;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;


import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;


/**
* Representation of a debugging session.
*
* @author   Jan Jancura
*/
public class KillActionProvider extends JPDADebuggerActionProvider {

    
    public KillActionProvider (ContextProvider lookupProvider) {
        super (
            (JPDADebuggerImpl) lookupProvider.lookupFirst 
                (null, JPDADebugger.class)
        );
    }
    
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_KILL);
    }
        
    public void doAction (Object action) {
        getDebuggerImpl ().finish ();
    }
    
    protected void checkEnabled (int debuggerState) {
        setEnabled (ActionsManager.ACTION_KILL, true);
    }
}
