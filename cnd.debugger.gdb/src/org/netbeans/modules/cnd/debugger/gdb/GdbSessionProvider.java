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

import java.util.Map;

import org.openide.util.NbBundle;
import org.netbeans.spi.debugger.SessionProvider;
import org.netbeans.spi.debugger.ContextProvider;


public class GdbSessionProvider extends SessionProvider {

    private ContextProvider contextProvider;

    public GdbSessionProvider(ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
    };

    public String getSessionName () {
        Map arguments = (Map) contextProvider.lookupFirst(null, Map.class);
        if (arguments != null) {
            String processName = (String) arguments.get("name"); // NOI18N
            if (processName != null) {
                //return LaunchingSessionProvider.findUnique(processName);
	    }
        }

        return NbBundle.getMessage(GdbSessionProvider.class, "CTL_GDB_SESSION");
    }
    
    public String getLocationName() {
        return NbBundle.getMessage(GdbSessionProvider.class, "CTL_GDB_SESSION");
    }
    
    public String getTypeID() {
        return GdbDebugger.SESSION_ID;
    }
    
    public Object[] getServices() {
        return new Object [0];
    }

}
