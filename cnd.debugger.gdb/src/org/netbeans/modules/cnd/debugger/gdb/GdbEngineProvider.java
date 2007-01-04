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

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.Session;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.netbeans.spi.debugger.ContextProvider;

import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;


/** Cnd Debugger implementatoin of DebuggerEngineProvider */
public class GdbEngineProvider extends DebuggerEngineProvider {

    private DebuggerEngine.Destructor   desctuctor;
    private Session                     session;

    public GdbEngineProvider(ContextProvider contextProvider) {
        session = (Session) contextProvider.lookupFirst(null, Session.class);
    }
    
    public String[] getLanguages() {
        return new String[] {"C", "C++"}; // NOI18N
    }

    public String getEngineTypeID() {
        return GdbDebugger.ENGINE_ID;
    }
    
    public Object[] getServices() {
        return new Object [0];
    }
    
    public void setDestructor(DebuggerEngine.Destructor desctuctor) {
        this.desctuctor = desctuctor;
    }
    
    public DebuggerEngine.Destructor getDestructor() {
        return desctuctor;
    }
    
    public Session getSession() {
        return session;
    }
}

