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

import java.net.URL;
import java.util.*;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.junit.NbTestCase;

/**
 * Tests information about local variables.
 *
 * @author Maros Sandor, Jan Jancura
 */
public class LocalVariablesTest extends NbTestCase {

    private JPDASupport     support;
    private DebuggerManager dm = DebuggerManager.getDebuggerManager ();

    private static final String CLASS_NAME =
        "org.netbeans.api.debugger.jpda.testapps.LocalVariablesApp";


    public LocalVariablesTest(String s) {
        super(s);
    }

    public void testWatches () throws Exception {
        try {
            LineBreakpoint lb = LineBreakpoint.create (CLASS_NAME, 34);
            dm.addBreakpoint (lb);

            support = JPDASupport.attach (CLASS_NAME);

            support.waitState (JPDADebugger.STATE_STOPPED);  // breakpoint hit

            CallStackFrame sf = support.getDebugger ().getCurrentCallStackFrame ();
            assertEquals (
                "Debugger stopped at wrong line", 
                lb.getLineNumber (), 
                sf.getLineNumber (null)
            );

            LocalVariable [] vars = sf.getLocalVariables ();
            assertEquals (
                "Wrong number of local variables", 
                4, 
                vars.length
            );
            Arrays.sort (vars, new Comparator () {
                public int compare (Object o1, Object o2) {
                    return ((LocalVariable) o1).getName ().compareTo (
                        ((LocalVariable) o2).getName ()
                    );
                }
            });
            assertEquals (
                "Wrong info about local variables", 
                "g", 
                vars [0].getName ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "20", 
                vars [0].getValue ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "int", 
                vars [0].getDeclaredType ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "int", 
                vars [0].getType ()
            );
            assertEquals (
                "Wrong info about local variables", 
                CLASS_NAME, 
                vars [0].getClassName ()
            );

            assertEquals (
                "Wrong info about local variables", 
                "s", 
                vars [1].getName ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "\"asdfghjkl\"", 
                vars [1].getValue ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "java.lang.Object", 
                vars [1].getDeclaredType ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "java.lang.String", 
                vars [1].getType ()
            );
            assertEquals (
                "Wrong info about local variables", 
                CLASS_NAME, 
                vars [1].getClassName ()
            );

            assertEquals (
                "Wrong info about local variables", 
                "x", 
                vars [2].getName ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "40", 
                vars [2].getValue ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "int", 
                vars [2].getDeclaredType ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "int", 
                vars [2].getType ()
            );
            assertEquals (
                "Wrong info about local variables", 
                CLASS_NAME, 
                vars [2].getClassName ()
            );

            assertEquals (
                "Wrong info about local variables", 
                "y", 
                vars [3].getName ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "50.5", 
                vars [3].getValue ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "float", 
                vars [3].getDeclaredType ()
             );
            assertEquals (
                "Wrong info about local variables", 
                "float", 
                vars [3].getType ()
            );
            assertEquals (
                "Wrong info about local variables", 
                CLASS_NAME, 
                vars [3].getClassName ()
            );

            support.stepOver ();
            support.stepOver ();

            sf = support.getDebugger ().getCurrentCallStackFrame ();
            vars = sf.getLocalVariables ();
            assertEquals ("Wrong number of local variables", 4, vars.length);
            Arrays.sort (vars, new Comparator () {
                public int compare (Object o1, Object o2) {
                    return ((LocalVariable) o1).getName ().compareTo (
                        ((LocalVariable) o2).getName ()
                    );
                }
            });
            assertEquals (
                "Wrong info about local variables", 
                "g", 
                vars [0].getName ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "\"ad\"", 
                vars [0].getValue ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "java.lang.CharSequence", 
                vars [0].getDeclaredType ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "java.lang.String", 
                vars [0].getType ()
             );
            assertEquals (
                "Wrong info about local variables", 
                CLASS_NAME, 
                vars [0].getClassName ()
            );

        } finally {
            support.doFinish ();
        }
    }
}
