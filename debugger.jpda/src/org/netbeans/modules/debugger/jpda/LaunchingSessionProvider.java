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

package org.netbeans.modules.debugger.jpda;

import java.util.HashSet;
import java.util.Map;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.LaunchingDICookie;
import org.netbeans.spi.debugger.SessionProvider;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.util.NbBundle;


/**
 *
 * @author Jan Jancura
 */
public class LaunchingSessionProvider extends SessionProvider {

    private ContextProvider         contextProvider;
    private LaunchingDICookie       launchingCookie;
    
    public LaunchingSessionProvider (ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
        launchingCookie = (LaunchingDICookie) contextProvider.lookupFirst 
            (null, LaunchingDICookie.class);
    };
    
    public String getSessionName () {
        Map arguments = (Map) contextProvider.lookupFirst 
            (null, Map.class);
        if (arguments != null) {
            String processName = (String) arguments.get ("name");
            if (processName != null)
                return findUnique (processName);
        }
        String sessionName = launchingCookie.getClassName ();
        int i = sessionName.lastIndexOf ('.');
        if (i >= 0) 
            sessionName = sessionName.substring (i + 1);
        return findUnique (sessionName);
    };
    
    public String getLocationName () {
        return NbBundle.getMessage 
            (LaunchingSessionProvider.class, "CTL_Localhost");
    }
    
    public String getTypeID () {
        return JPDADebugger.SESSION_ID;
    }
    
    public Object[] getServices () {
        return new Object [0];
    }
    
    static String findUnique (String sessionName) {
        DebuggerManager cd = DebuggerManager.getDebuggerManager ();
        Session[] ds = cd.getSessions ();
        
        // 1) finds all already used indexes and puts them to HashSet
        int i, k = ds.length;
        HashSet m = new HashSet ();
        for (i = 0; i < k; i++) {
            String pn = ds [i].getName ();
            if (!pn.startsWith (sessionName)) continue;
            if (pn.equals (sessionName)) {
                m.add (new Integer (0));
                continue;
            }

            try {
                int t = Integer.parseInt (pn.substring (sessionName.length ()));
                m.add (new Integer (t));
            } catch (Exception e) {
            }
        }
        
        // 2) finds first unused index in m
        k = m.size ();
        for (i = 0; i < k; i++)
           if (!m.contains (new Integer (i)))
               break;
        if (i > 0) sessionName = sessionName + i;
        return sessionName;
    };
}

