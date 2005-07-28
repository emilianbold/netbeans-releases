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

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.junit.NbTestCase;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;


/**
 * Tests JPDAstep (step in, step out and step over).
 *
 * @author Roman Ondruska
 */
public class JPDAStepTest extends NbTestCase {

    private DebuggerManager dm = DebuggerManager.getDebuggerManager ();
    private String          sourceRoot = System.getProperty ("test.dir.src");
    private JPDASupport     support;
    
    private Object STEP_LOCK = new Object();
    
    private boolean stepExecFired = false;

    public JPDAStepTest (String s) {
        super (s);
    }

     public void testStepInto () throws Exception {
        try {
            JPDASupport.removeAllBreakpoints ();
            LineBreakpoint lb = LineBreakpoint.create (
                sourceRoot + 
                    "org/netbeans/api/debugger/jpda/testapps/StepApp.java",
                24
            );
            dm.addBreakpoint (lb);
            support = JPDASupport.attach
                ("org.netbeans.api.debugger.jpda.testapps.StepApp");
            support.waitState (JPDADebugger.STATE_STOPPED);
            dm.removeBreakpoint (lb);
            assertEquals (
                "Execution stopped in wrong class on breakpoint", 
                getCurrentClassName(),
                "org.netbeans.api.debugger.jpda.testapps.StepApp"
            );
            assertEquals (
                "Execution stopped at wrong line", 
                24, 
                getCurrentLineNumber()
            );
            stepCheck (
                JPDAStep.STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                25
            );
            stepCheck (
                JPDAStep.STEP_INTO, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                36
            );
            stepCheck (
                JPDAStep.STEP_INTO, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                37
            );
            stepCheck (
                JPDAStep.STEP_INTO, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                42
            );
            stepCheck (
                JPDAStep.STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                43
            );
            stepCheck (
                JPDAStep.STEP_INTO, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                48
            );
           
            // resume VM
            ((JPDADebuggerImpl)support.getDebugger()).getVirtualMachine().resume();
            support.waitState (JPDADebugger.STATE_DISCONNECTED);
         
        } finally {
            support.doFinish ();
        }
    }
     
     public void testStepOver () throws Exception {
        try {
            JPDASupport.removeAllBreakpoints ();
            LineBreakpoint lb = LineBreakpoint.create (
                sourceRoot + 
                    "org/netbeans/api/debugger/jpda/testapps/StepApp.java",
                24
            );
            dm.addBreakpoint (lb);
            support = JPDASupport.attach
                ("org.netbeans.api.debugger.jpda.testapps.StepApp");
            support.waitState (JPDADebugger.STATE_STOPPED);
            dm.removeBreakpoint (lb);
            assertEquals (
                "Execution stopped in wrong class", 
                getCurrentClassName(),
                "org.netbeans.api.debugger.jpda.testapps.StepApp"
            );
            assertEquals (
                "Execution stopped at wrong line", 
                24, 
                getCurrentLineNumber()
            );
            stepCheck (
                JPDAStep.STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                25
            );
            stepCheck (
                JPDAStep.STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                26
            );
            stepCheck (
                JPDAStep.STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                27
            );
            stepCheck (
                JPDAStep.STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                28
            );
            stepCheck (
                JPDAStep.STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                29
            );
            
            // resume VM
            ((JPDADebuggerImpl)support.getDebugger()).getVirtualMachine().resume();
            support.waitState (JPDADebugger.STATE_DISCONNECTED);
        } finally {
            support.doFinish ();
        }
            
    }

    public void testStepOut () throws Exception {
        try {
            JPDASupport.removeAllBreakpoints ();
            LineBreakpoint lb = LineBreakpoint.create (
                sourceRoot + 
                    "org/netbeans/api/debugger/jpda/testapps/StepApp.java",
                24
            );
            dm.addBreakpoint (lb);
            support = JPDASupport.attach
                ("org.netbeans.api.debugger.jpda.testapps.StepApp");
            support.waitState (JPDADebugger.STATE_STOPPED);
            dm.removeBreakpoint (lb);
            assertEquals (
                "Execution stopped in wrong class", 
                getCurrentClassName(), 
                "org.netbeans.api.debugger.jpda.testapps.StepApp"
            );
            assertEquals (
                "Execution stopped at wrong line", 
                24, 
                getCurrentLineNumber()
            );
            stepCheck (
                JPDAStep.STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                25
            );
            stepCheck (
                JPDAStep.STEP_INTO, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                36
            );
            stepCheck (
                JPDAStep.STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                37
            );
            stepCheck (
                JPDAStep.STEP_INTO, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                42
            );
            stepCheck (
                JPDAStep.STEP_OUT, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                37
            );
            stepCheck (
                JPDAStep.STEP_OUT, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                25
            );
            
            // resume VM
            ((JPDADebuggerImpl)support.getDebugger()).getVirtualMachine().resume();
            support.waitState (JPDADebugger.STATE_DISCONNECTED);
        } finally {
            support.doFinish ();
        }
    }
    
    private void stepCheck (
        int stepType, 
        String clsExpected, 
        int lineExpected
    ) {
        stepExecFired = false;
        JPDAStep step = support.getDebugger().createJPDAStep(JPDAStep.STEP_LINE, stepType);
 
        step.addPropertyChangeListener(JPDAStep.PROP_STATE_EXEC, new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        synchronized (STEP_LOCK) {
                            stepExecFired = true;
                            STEP_LOCK.notify();
                        }
                        
                        
                    }
        });
        step.addStep(support.getDebugger().getCurrentThread());
        ((JPDADebuggerImpl)support.getDebugger()).getVirtualMachine().resume();              
     
        synchronized (STEP_LOCK) {
            while (! stepExecFired) {
                try {
                    STEP_LOCK.wait ();
                } catch (InterruptedException ex) {
                    ex.printStackTrace ();
                }
            }
        }

        assertEquals(
            "Execution stopped in wrong class", 
            clsExpected, 
            getCurrentClassName()
        );
        assertEquals(
            "Execution stopped at wrong line", 
            lineExpected, 
            getCurrentLineNumber()
        );
        
    }
    
    private String getCurrentClassName() {
        JPDADebuggerImpl debugger = (JPDADebuggerImpl) support.getDebugger();
        
        String className = null;
        
        try {
            className = ((JPDAThreadImpl)debugger.getCurrentThread()).
                getThreadReference().frame(0).location().declaringType().name();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return className;
    }
    
    private int getCurrentLineNumber() {
        JPDADebuggerImpl debugger = (JPDADebuggerImpl) support.getDebugger();
        
        int lineNumber = -1;
        
        try {
            lineNumber = ((JPDAThreadImpl)debugger.getCurrentThread()).
                getThreadReference().frame(0).location().lineNumber();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lineNumber;
    }
        
}

