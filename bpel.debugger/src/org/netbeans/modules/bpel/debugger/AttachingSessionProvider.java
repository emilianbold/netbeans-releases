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


package org.netbeans.modules.bpel.debugger;

import java.util.HashSet;
import java.util.Map;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.bpel.debugger.api.AttachingCookie;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.SessionProvider;
import org.openide.util.NbBundle;


/**
 * Session provider for attaching to a remote BPEL service engine.
 *
 * @author Sun Microsystems
 * @author Sun Microsystems
 */
public class AttachingSessionProvider extends SessionProvider {
    /**
     * Public ID used for registration in Meta-inf/debugger.
     */
    public static final String SESSION_ID = "netbeans-BpelSession"; // NOI18N
    
    
    private ContextProvider mContextProvider;
    private AttachingCookie mAttachingCookie;
    
    
    public AttachingSessionProvider (ContextProvider contextProvider) {
        this.mContextProvider = contextProvider;
        mAttachingCookie = contextProvider.lookupFirst(null, AttachingCookie.class);
    };
    
    
    public String getSessionName () {
        Map arguments = mContextProvider.lookupFirst(null, Map.class);
        if (arguments != null) {
            String processName = (String) arguments.get("name");
            if (processName != null)
                return findUnique(processName);
        }
        return mAttachingCookie.getHost() + ":" + mAttachingCookie.getPort();
    };
    
    public String getLocationName () {
        if (mAttachingCookie.getHost() != null)
            return mAttachingCookie.getHost();
        return NbBundle.getMessage(AttachingSessionProvider.class, "CTL_Localhost");
    }
    
    public String getTypeID () {
        return SESSION_ID;
    }
    
    public Object[] getServices () {
        return new Object [0];
    }
    
    
    private static String findUnique (String sessionName) {
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
