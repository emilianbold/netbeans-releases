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

import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerEngineProvider;

import java.util.*;

/**
 * A dummy debugger implementation.
 *
 * @author Maros Sandor
 */
public class TestDebugger {

    public static final String  ENGINE_ID   = "netbeans-TestSession/Basic";

    public static final String  SESSION_ID  = "netbeans-TestSession";

    private ContextProvider      lookupProvider;
    private TestEngineProvider  testEngineProvider;

    public TestDebugger(ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
        List l = lookupProvider.lookup(null, DebuggerEngineProvider.class);
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            if (l.get (i) instanceof TestEngineProvider) testEngineProvider = (TestEngineProvider) l.get (i);
        }
        if (testEngineProvider == null) throw new IllegalArgumentException("TestEngineProvider have to be used to start TestDebugger!");
    }

    public void finish() {
        testEngineProvider.getDestructor().killEngine();
    }
}
