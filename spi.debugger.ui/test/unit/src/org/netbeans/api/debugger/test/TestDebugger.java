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
