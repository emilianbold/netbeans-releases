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
        LineBreakpoint lb = LineBreakpoint.create (
                Utils.getURL(System.getProperty ("test.dir.src")+
                             "org/netbeans/api/debugger/jpda/testapps/EvalApp.java"),
                34
            );
        DebuggerManager.getDebuggerManager ().addBreakpoint (lb);
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
