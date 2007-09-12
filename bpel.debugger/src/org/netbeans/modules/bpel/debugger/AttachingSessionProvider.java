/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        mAttachingCookie = (AttachingCookie) contextProvider.lookupFirst 
            (null, AttachingCookie.class);
    };
    
    
    public String getSessionName () {
        Map arguments = (Map) mContextProvider.lookupFirst(null, Map.class);
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
