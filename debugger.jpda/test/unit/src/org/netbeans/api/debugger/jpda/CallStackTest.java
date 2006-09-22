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

package org.netbeans.api.debugger.jpda;

import java.util.*;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.debugger.jpda.SourcePath;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;


/**
 * Tests information about stack call stacks.
 *
 * @author Maros Sandor
 */
public class CallStackTest extends NbTestCase {


    public CallStackTest (String s) {
        super (s);
    }

    public void testInstanceCallStackInfo () throws Exception {
        JPDASupport support = null;
        try {
            JPDASupport.removeAllBreakpoints ();
            LineBreakpoint lb = LineBreakpoint.create (
                Utils.getURL(System.getProperty ("test.dir.src") + 
                             "org/netbeans/api/debugger/jpda/testapps/CallStackApp.java"),
                30
            );
            lb.setPreferredClassName("org.netbeans.api.debugger.jpda.testapps.CallStackApp");
            DebuggerManager.getDebuggerManager ().addBreakpoint (lb);
            support = JPDASupport.attach (
                "org.netbeans.api.debugger.jpda.testapps.CallStackApp"
            );
            support.waitState (JPDADebugger.STATE_STOPPED);
            support.stepOver ();
            support.stepInto ();
            support.stepOver ();
            support.stepInto ();
            support.stepOver ();

            CallStackFrame sf = support.getDebugger ().
                getCurrentCallStackFrame ();

            List strata = sf.getAvailableStrata ();
            assertEquals (
                "Available strata", 
                1, 
                strata.size ()
            );
            assertEquals (
                "Java stratum is not available", 
                "Java", 
                strata.get (0)
            );
            assertEquals (
                "Java stratum is not default", 
                "Java", 
                sf.getDefaultStratum ()
            );
            assertEquals (
                "Wrong class name", 
                "org.netbeans.api.debugger.jpda.testapps.CallStackApp", 
                sf.getClassName ()
            );
            assertEquals (
                "Wrong line number", 
                49, 
                sf.getLineNumber (null)
            );
            LocalVariable [] vars = sf.getLocalVariables ();
            assertEquals (
                "Wrong number of local variables", 
                1, vars.length
            );
            assertEquals (
                "Wrong info about local variables", 
                "im2", 
                vars [0].getName ()
            );
            assertEquals (
                "Wrong info about current method", 
                "m2", 
                sf.getMethodName ()
            );
            assertNotNull (
                "Wrong info about this object", 
                sf.getThisVariable ()
            );
            assertFalse (
                "Wrong info about obsolete method", 
                sf.isObsolete ()
            );

            JPDAThread thread = sf.getThread ();
            assertEquals (
                "Callstack and Thread info mismatch", 
                thread.getCallStack () [0], 
                sf
            );
            assertEquals (
                "Callstack and Thread info mismatch", 
                thread.getClassName (), 
                sf.getClassName ()
            );
            assertEquals (
                "Callstack and Thread info mismatch", 
                thread.getMethodName (), 
                sf.getMethodName ()
            );
            assertEquals (
                "Callstack and Thread info mismatch", 
                thread.getSourceName (null), 
                sf.getSourceName (null)
            );
        } finally {
            if (support != null)
                support.doFinish ();
        }
    }

    public void testStaticCallStackInfo() throws Exception {
        JPDASupport support = null;
        try {
            JPDASupport.removeAllBreakpoints ();
            LineBreakpoint lb = LineBreakpoint.create (
                Utils.getURL(System.getProperty ("test.dir.src") + 
                             "org/netbeans/api/debugger/jpda/testapps/CallStackApp.java"),
                30
            );
            lb.setPreferredClassName("org.netbeans.api.debugger.jpda.testapps.CallStackApp");
            DebuggerManager.getDebuggerManager ().addBreakpoint (lb);
            support = JPDASupport.attach (
                "org.netbeans.api.debugger.jpda.testapps.CallStackApp"
            );
            support.waitState (JPDADebugger.STATE_STOPPED);
            CallStackFrame sf = support.getDebugger ().
                getCurrentCallStackFrame ();

            List strata = sf.getAvailableStrata ();
            assertEquals (
                "Available strata", 1, strata.size ()
            );
            assertEquals (
                "Java stratum is not available", "Java", strata.get (0)
            );
            assertEquals (
                "Java stratum is not default", "Java", sf.getDefaultStratum ()
            );
            assertEquals (
                "Wrong class name", 
                "org.netbeans.api.debugger.jpda.testapps.CallStackApp", 
                sf.getClassName ()
            );
            assertEquals (
                "Wrong line number", 
                30, 
                sf.getLineNumber (null)
            );

            LocalVariable [] vars = sf.getLocalVariables ();
            assertEquals (
                "Wrong number of local variables", 1, vars.length
            );
            assertEquals (
                "Wrong info about local variables", 
                "args", 
                vars[0].getName ()
            );
            assertEquals (
                "Wrong info about current method", 
                "main", 
                sf.getMethodName ()
            );
            assertNull (
                "Wrong info about this object", sf.getThisVariable ()
            );
            assertFalse (
                "Wrong info about obsolete method", sf.isObsolete ()
            );

            JPDAThread thread = sf.getThread();
            assertEquals (
                "Callstack and Thread info mismatch", 
                thread.getCallStack () [0], 
                sf
            );
            assertEquals (
                "Callstack and Thread info mismatch", 
                thread.getClassName (), 
                sf.getClassName ()
            );
            assertEquals (
                "Callstack and Thread info mismatch", 
                thread.getMethodName (), 
                sf.getMethodName ()
            );
            assertEquals (
                "Callstack and Thread info mismatch", 
                thread.getSourceName (null), 
                sf.getSourceName (null)
            );
        } finally {
            if (support != null)
                support.doFinish ();
        }
    }
}
