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

package org.netbeans.modules.debugger.jpda;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.connect.ListeningConnector;

import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ListeningDICookie;
import org.netbeans.spi.debugger.SessionProvider;
import org.netbeans.spi.debugger.ContextProvider;

/**
 *
 * @author Jan Jancura
 */
public class ListeningSessionProvider extends SessionProvider {
    
    private ContextProvider contextProvider;
    private ListeningDICookie smadic;
    
    public ListeningSessionProvider (ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
        smadic = (ListeningDICookie) contextProvider.lookupFirst 
            (null, ListeningDICookie.class);
    };
    
    public String getSessionName () {
        String processName = (String) contextProvider.lookupFirst 
            (null, String.class);
        if (processName != null)
            return processName;
        if (smadic.getSharedMemoryName () != null)
            return "listenning:" + smadic.getSharedMemoryName ();
        return "listenning:" + smadic.getPortNumber ();
    }
    
    public String getLocationName () {
        return "localhost";
    }
    
    public String getTypeID () {
        return JPDADebugger.SESSION_ID;
    }
    
    public Object[] getServices () {
        return new Object [0];
    }
}

