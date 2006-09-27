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

package org.openide.loaders;

import java.util.ArrayList;
import java.util.logging.Level;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;


/** Checks that behaviour of LoggingTestCaseHid is correct.
 *
 * @author  Jaroslav Tulach
 */
public class LoggingControlTest extends LoggingTestCaseHid {

    private ErrorManager err;

    public LoggingControlTest (String name) {
        super (name);
    }

    protected void setUp() throws Exception {
        err = ErrorManager.getDefault().getInstance("TEST-" + getName());
    }
    
    protected Level logLevel() {
        return Level.ALL;
    }

    public void testCorrectThreadSwitching() throws Exception {
        
        class Run implements Runnable {
            public ArrayList events = new ArrayList();
            
            public void run() {
                events.add("A");
                err.log("A");
                events.add("B");
                err.log("B");
                events.add("C");
                err.log("C");
            }
            
            public void directly() {
                err.log("0");
                events.add(new Integer(1));
                err.log("1");
                events.add(new Integer(2));
                err.log("2");
                events.add(new Integer(3));
                err.log("3");
            }
        }
        
        Run run = new Run();
        
        String order = 
            "THREAD:Para MSG:A" + 
            "THREAD:main MSG:0" + 
            "THREAD:main MSG:1" +
            "THREAD:Para MSG:B" +
            "THREAD:main MSG:2" +
            "THREAD:Para MSG:C" +
            "THREAD:main MSG:3";
        registerSwitches(order, 0);
        
        
        RequestProcessor rp = new RequestProcessor("Para");
        RequestProcessor.Task task = rp.post(run);
        run.directly();
        if (!task.waitFinished(10000)) {
            fail("Runnable deadlocked");
        }
        
        String res = run.events.toString();
        
        assertEquals("Really changing the execution according to the provided order: " + res, "[A, 1, B, 2, C, 3]", res);
    }
    
    public void testWorksWithRegularExpressionsAsWell() throws Exception {
        
        class Run implements Runnable {
            public ArrayList events = new ArrayList();
            
            public void run() {
                events.add("A");
                err.log("4329043A");
                events.add("B");
                err.log("B");
                events.add("C");
                err.log("CCCC");
            }
            
            public void directly() {
                err.log("0");
                events.add(new Integer(1));
                err.log("1");
                events.add(new Integer(2));
                err.log("2");
                events.add(new Integer(3));
                err.log("3");
            }
        }
        
        Run run = new Run();
        
        String order = 
            "THREAD:Para MSG:[0-9]*A" + 
            "THREAD:main MSG:0" + 
            "THREAD:main MSG:^1$" +
            "THREAD:Para MSG:B" +
            "THREAD:main MSG:2" +
            "THREAD:Para MSG:C+" +
            "THREAD:main MSG:3";
        registerSwitches(order, 0);
        
        
        RequestProcessor rp = new RequestProcessor("Para");
        RequestProcessor.Task task = rp.post(run);
        run.directly();
        if (!task.waitFinished(10000)) {
            fail("Runnable deadlocked");
        }
        
        String res = run.events.toString();
        
        assertEquals("Really changing the execution according to the provided order: " + res, "[A, 1, B, 2, C, 3]", res);
    }

    public void testLogMessagesCanRepeat() throws Exception {
        
        class Run implements Runnable {
            public ArrayList events = new ArrayList();
            
            public void run() {
                events.add("A");
                err.log("A");
                events.add("A");
                err.log("A");
                events.add("A");
                err.log("A");
            }
            
            public void directly() {
                err.log("0");
                events.add(new Integer(1));
                err.log("1");
                events.add(new Integer(2));
                err.log("2");
                events.add(new Integer(3));
                err.log("3");
            }
        }
        
        Run run = new Run();
        
        String order = 
            "THREAD:Para MSG:A" + 
            "THREAD:main MSG:0" + 
            "THREAD:main MSG:^1$" +
            "THREAD:Para MSG:A" +
            "THREAD:main MSG:2" +
            "THREAD:Para MSG:A" +
            "THREAD:main MSG:3";
        registerSwitches(order, 0);
        
        
        RequestProcessor rp = new RequestProcessor("Para");
        RequestProcessor.Task task = rp.post(run);
        run.directly();
        if (!task.waitFinished(10000)) {
            fail("Runnable deadlocked");
        }
        
        String res = run.events.toString();
        
        assertEquals("Really changing the execution according to the provided order: " + res, "[A, 1, A, 2, A, 3]", res);
    }

    private Exception throwIt;
    public void testRuntimeExceptionsAlsoGenerateLog() throws Exception {
        if (throwIt != null) {
            ErrorManager.getDefault().log("Ahoj");
            throw throwIt;
        }
        
        LoggingControlTest l = new LoggingControlTest("testRuntimeExceptionsAlsoGenerateLog");
        l.throwIt = new NullPointerException();
        TestResult res = l.run();
        assertEquals("No failures", 0, res.failureCount());
        assertEquals("One error", 1, res.errorCount());
        
        Object o = res.errors().nextElement();
        TestFailure f = (TestFailure)o;
        
        if (f.exceptionMessage() == null || f.exceptionMessage().indexOf("Ahoj") == -1) {
            fail("Logged messages shall be in exception message: " + f.exceptionMessage());
        }
    }
}
