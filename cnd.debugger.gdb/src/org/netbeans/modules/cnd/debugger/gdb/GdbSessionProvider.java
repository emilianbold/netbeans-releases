/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.debugger.gdb;

import org.openide.util.NbBundle;
import org.netbeans.spi.debugger.SessionProvider;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;

public class GdbSessionProvider extends SessionProvider {

    private ContextProvider contextProvider;
    private String sessionName = NbBundle.getMessage(GdbSessionProvider.class, "CTL_GDB_SESSION");
    private String locationName = NbBundle.getMessage(GdbSessionProvider.class, "CTL_GDB_SESSION");
    private String [] supportedLanguages = new String [] { "C++", "C", "Fortran" };
    private ProjectActionEvent projectActionEvent;

    public GdbSessionProvider(ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
        projectActionEvent = (ProjectActionEvent) contextProvider.lookupFirst(null, ProjectActionEvent.class);
    };

    public String getSessionName () {
        String sn = null;
        if (projectActionEvent != null)
            sn = projectActionEvent.getExecutable();
        if (sn == null) return sessionName;
        if (sn.length() > 8) {
            // Name is too long - get base name
            if (sn.lastIndexOf('/') >= 0) {
                sn = sn.substring(sn.lastIndexOf('/') + 1);
            }
        }
        if (sn.length() > 0) {
            // Set session name
            sessionName = sn; 
        }
        return sessionName;
    }
    
    public String getLocationName() {
        locationName = "localhost"; // NOI18N
        return locationName;
    }
    
    public String getTypeID() {
        return GdbDebugger.SESSION_ID;
    }
    
    public Object[] getServices() {
        return supportedLanguages; 
    }

}
