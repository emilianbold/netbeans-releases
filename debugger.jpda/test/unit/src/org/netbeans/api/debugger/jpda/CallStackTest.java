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

package org.netbeans.api.debugger.jpda;

import java.util.*;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.debugger.jpda.SourcePath;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;


/**
 * Tests information about stack call stacks.
 *
 * @author Maros Sandor
 */
public class CallStackTest extends DebuggerJPDAApiTestBase {

    
    public CallStackTest (String s) {
        super(s);
    }

    public void testInstanceCallStackInfo () throws Exception {
        JPDASupport support = null;
        try {
            JPDASupport.removeAllBreakpoints ();
            LineBreakpoint lb = LineBreakpoint.create (
                "org.netbeans.api.debugger.jpda.testapps.CallStackApp", 
                24
            );
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
                43, 
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
                "org.netbeans.api.debugger.jpda.testapps.CallStackApp", 
                24
            );
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
                24, 
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
