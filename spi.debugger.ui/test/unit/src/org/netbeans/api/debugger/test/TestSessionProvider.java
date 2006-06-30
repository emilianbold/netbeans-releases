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

package org.netbeans.api.debugger.test;

import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.spi.debugger.SessionProvider;
import org.netbeans.spi.debugger.ContextProvider;

/**
 * Provider for Test debugger session.
 *
 * @author Maros Sandor
 */
public class TestSessionProvider extends SessionProvider {

    private DebuggerInfo info;
    private TestDICookie cookie;

    public TestSessionProvider (ContextProvider info) {
        this.info = (DebuggerInfo) info.lookupFirst(null, DebuggerInfo.class);
        cookie = (TestDICookie) info.lookupFirst(null, TestDICookie.class);
    };
    
    public String getSessionName () {
        return "Test session";
    }
    
    public String getLocationName () {
        return "localhost";
    }
    
    public String getTypeID () {
        return TestDebugger.SESSION_ID;
    }
    
    public Object[] getServices () {
        return new Object [0];
    }
}
