/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.debugger.test;

import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.spi.debugger.SessionProvider;

/**
 * Provider for Test debugger session.
 *
 * @author Maros Sandor
 */
public class TestSessionProvider extends SessionProvider {
    
    private DebuggerInfo info;
    private TestDICookie cookie;
    
    public TestSessionProvider (DebuggerInfo info) {
        this.info = info;
        cookie = (TestDICookie) info.lookupFirst(TestDICookie.class);
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
