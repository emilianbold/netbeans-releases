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

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.junit.NbTestCase;

/**
 * Tests evaluation of various expressions.
 *
 * @author Maros Sandor, Jan Jancura
 */
public class EvaluationTest extends NbTestCase {

    private JPDASupport     support;

    
    public EvaluationTest (String s) {
        super (s);
    }

    protected void setUp () throws Exception {
        super.setUp ();
        JPDASupport.removeAllBreakpoints ();
        DebuggerManager.getDebuggerManager ().addBreakpoint (
            LineBreakpoint.create (
                "org.netbeans.api.debugger.jpda.testapps.EvalApp",
                28
            )
        );
        support = JPDASupport.attach (
            "org.netbeans.api.debugger.jpda.testapps.EvalApp"
        );
        support.waitState (JPDADebugger.STATE_STOPPED);
    }

    public void testStaticEvaluation () throws Exception {
        try {
            checkEval ("1", 1);
            checkEval ("4.3", 4.3);
            checkEval ("ix", 74);

            checkEvalFails ("this");
            checkEvalFails ("NoSuchClass.class");
        } finally {
            support.doFinish ();
        }
    }

    public void testStaticExpressions () throws Exception {
        try {
            checkEval ("ix * fx", 740.0f);
            checkEval ("sx % 3", 1);

            checkEvalFails ("ix * fx ** fx");
        } finally {
            support.doFinish ();
        }
    }

    private void checkEval (String expression, int value) {
        try {
            Variable var = support.getDebugger ().evaluate (expression);
            assertEquals (
                "Evaluation of expression failed (wrong value): " + expression, 
                value, 
                Integer.parseInt (var.getValue ()), 0
            );
            assertEquals (
                "Evaluation of expression failed (wrong type of result): " + 
                    expression, 
                "int", 
                var.getType ()
            );
        } catch (InvalidExpressionException e) {
            fail (
                "Evaluation of expression was unsuccessful: " + e
            );
        }
    }

    private void checkEval(String expression, float value) {
        try {
            Variable var = support.getDebugger ().evaluate (expression);
            assertEquals (
                "Evaluation of expression failed (wrong value): " + expression, 
                value, 
                Float.parseFloat (var.getValue ()), 
                0
            );
            assertEquals (
                "Evaluation of expression failed (wrong type of result): " + 
                    expression, 
                "float", 
                var.getType ()
            );
        } catch (InvalidExpressionException e) {
            fail (
                "Evaluation of expression was unsuccessful: " + e
            );
        }
    }

    private void checkEval (String expression, double value) {
        try {
            Variable var = support.getDebugger ().evaluate (expression);
            assertEquals (
                "Evaluation of expression failed (wrong value): " + expression, 
                value, 
                Double.parseDouble (var.getValue ()), 
                0
            );
            assertEquals (
                "Evaluation of expression failed (wrong type of result): " + 
                    expression, 
                "double", 
                var.getType ()
            );
        } catch (InvalidExpressionException e) {
            fail (
                "Evaluation of expression was unsuccessful: " + e
            );
        }
    }

    private void checkEval (String expression, String type, String value) {
        try {
            Variable var = support.getDebugger ().evaluate (expression);
            assertEquals (
                "Evaluation of expression failed (wrong value): " + expression, 
                value, 
                var.getValue ()
            );
            assertEquals (
                "Evaluation of expression failed (wrong type of result): " +  
                    expression, 
                type, 
                var.getType ()
            );
        } catch (InvalidExpressionException e) {
            fail (
                "Evaluation of expression was unsuccessful: " + e
            );
        }
    }

    private void checkEvalFails (String expression) {
        try {
            Variable var = support.getDebugger ().evaluate (expression);
            fail (
                "Evaluation of expression was unexpectedly successful: " + 
                expression + " = " + var.getValue ()
            );
        } catch (InvalidExpressionException e) {
            // its ok
            return;
        }
    }
}
