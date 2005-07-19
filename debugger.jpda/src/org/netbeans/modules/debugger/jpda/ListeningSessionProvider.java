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

package org.netbeans.modules.debugger.jpda;

import java.util.Map;

import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ListeningDICookie;
import org.netbeans.spi.debugger.SessionProvider;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.util.NbBundle;

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
        Map arguments = (Map) contextProvider.lookupFirst 
            (null, Map.class);
        if (arguments != null) {
            String processName = (String) arguments.get ("name");
            if (processName != null)
                return LaunchingSessionProvider.findUnique (processName);
        }
        if (smadic.getSharedMemoryName () != null)
            return NbBundle.getMessage 
                (ListeningSessionProvider.class, "CTL_Listening") + 
                ":" + smadic.getSharedMemoryName ();
        return NbBundle.getMessage 
            (ListeningSessionProvider.class, "CTL_Listening") + 
            ":" + smadic.getPortNumber ();
    }
    
    public String getLocationName () {
        return NbBundle.getMessage 
            (ListeningSessionProvider.class, "CTL_Localhost");
    }
    
    public String getTypeID () {
        return JPDADebugger.SESSION_ID;
    }
    
    public Object[] getServices () {
        return new Object [0];
    }
}

