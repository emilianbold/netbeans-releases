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

package org.netbeans.xtest.plugin.ide;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestListener;
import junit.framework.TestResult;
import org.netbeans.core.execution.ExecutionEngine;
import org.netbeans.xtest.plugin.ide.services.XTestErrorManager;
import org.netbeans.xtest.testrunner.JUnitTestRunner;
import org.openide.ErrorManager;
import org.openide.execution.ExecutorTask;

/**
 * Portion of Main that needs to run with access to Execution API & impl.
 * @author Jan Chalupa, Jesse Glick
 */
public class MainWithExec implements Main.MainWithExecInterface {
    
    // terminate code :-)
    public static final int TERMINATE_CODE = 666;
    // terminate command :-)
    public static final String TERMINATE_NAME = "kill";
    
    
    
    private static boolean executionEngineAvailable() {
        try {
            // don't know whether this is appropriate class, but
            // it works for now
            Class.forName("org.netbeans.core.execution.ProcessNode");
            return true;
        } catch (ClassNotFoundException cnfe) {
            // execution is not available
            return false;
        }
    }
    
    /*
     * terminates processes shown in the Runtime/Processes node
     * return number of processes which the method was not able
     * to kill, -1 if any problem was encoutered
     * If execution engine is not available -> method does nothing
     */
    public int terminateProcesses() {
        if (executionEngineAvailable()) {
            System.out.println("Trying to terminate processes spawned by IDE");
            // number of processes which were not terminated
            int notTerminatedProcesses = 0;
            // number of processes which were terminated
            int terminatedProcesses = 0;
            
            ExecutionEngine engine = ExecutionEngine.getExecutionEngine();
            Collection tasks = engine.getRunningTasks();
            Object items[] = tasks.toArray();
            for (int i=0; i< items.length; i++) {
                ExecutorTask task = (ExecutorTask)items[i];
                // do not kill anything starting with org.netbeans.xtest.
                if (task.toString().indexOf("org.netbeans.xtest.") == -1) {
                    Main.errMan.log(ErrorManager.USER,"XTest: Stopping task (via Execution Engine): "+task.toString());
                    task.stop();
                    int result = task.result();
                    Main.errMan.log(ErrorManager.USER,"XTest: Task exited with result: "+result);
                    terminatedProcesses++;
                }
            }
            if (terminatedProcesses > 0) {
                // better sleep for a sec, so they can be really killed
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    // nothing
                }
            }
            return notTerminatedProcesses;
        }
        
        
        // discard all changes in modified files
        Object[] dobs = org.openide.loaders.DataObject.getRegistry().getModifiedSet().toArray();
        if(dobs.length > 0) {
            Main.errMan.log(ErrorManager.USER, new java.util.Date().toString() + ": discarding changes in unsaved files:");
            for(int i=0;i<dobs.length;i++) {
                org.openide.loaders.DataObject obj = (org.openide.loaders.DataObject)dobs[i];
                Main.errMan.log(ErrorManager.USER, "        "+obj.getPrimaryFile().getPath());
                obj.setModified(false);
            }
        }
        
        return 0;
    }
    
    public void exit() {
        org.openide.LifecycleManager.getDefault().exit ();
    }
    
    public void run() throws Exception {
        try {
            if("true".equals(System.getProperty("xtest.ide.error.manager")) ||
               "true".equals(System.getProperty("xtest.ide.handler"))) {
                // install xtest error manager
                MyJUnitTestRunner testRunner = new MyJUnitTestRunner();
                testRunner.runTests();
            } else {
                JUnitTestRunner testRunner = new JUnitTestRunner(null, System.out);
                testRunner.runTests();
            }
        } catch (Throwable t) {
            System.out.println("Error - during test run caught exception: "+t.getMessage());
            t.printStackTrace();
        }
    }
    
    /** This class adds XTestResultListener to be able to track exceptions 
     * caugth by XTestErrorManager.
     */
    private class MyJUnitTestRunner extends JUnitTestRunner {

        public MyJUnitTestRunner() throws IOException {
            super(null, System.out);
            if("true".equals(System.getProperty("xtest.ide.handler"))) {
                // adds handler to track exceptions thrown by logger
                Logger.getLogger("").addHandler(new XTestIDEHandler());
            }
        }

        protected void addTestListeners(TestResult testResult) {
            // [pzajac] XTestErrorListenr listener must be first
            testResult.addListener(new XTestResultListener(testResult));
            super.addTestListeners(testResult);
        }
    }
   
    /** This TestListener reports error for a exceptions from ErrorManager
    */
    private static class XTestResultListener implements TestListener {
        
        private  TestResult result;
        private boolean checkXTestErrorManager = "true".equals(System.getProperty("xtest.ide.error.manager"));
        private boolean errorInTest = false;
        
        public XTestResultListener(TestResult result) {
            this.result = result;
        }
        
        /** An error occurred. */
        public void addError(Test test, Throwable t) {
            // report this error and ignore possible additional errors from IDE
            errorInTest = true;
        }
        
        /** A failure occurred.*/
        public void addFailure(Test test, AssertionFailedError t){};

        /* A test ended. */
        public void endTest(Test test) {
            if(!errorInTest) {
                try {
                    Iterator it = XTestErrorManager.getExceptions().iterator();
                    if (checkXTestErrorManager && it.hasNext()) {
                        // exception was thrown => add the first found exception as
                        // an error (i.e. its stack trace will be printed in results)
                        result.addError(test, (Throwable)it.next());
                        XTestErrorManager.clearExceptions();
                    } else {
                        // check XTestIDEHandler
                        Iterator itHandler = XTestIDEHandler.getExceptions().iterator();
                        if(itHandler.hasNext()) {
                            // exception was thrown => add the first found exception as
                            // an error (i.e. its stack trace will be printed in results)
                            result.addError(test, (Throwable)itHandler.next());
                            XTestIDEHandler.clearExceptions();
                        }
                    }
                } catch (Exception e) {
                    // ClassNotFound exception, etc
                    e.printStackTrace();
                }
            }
            errorInTest = false;
        }
        
        /** A test started. */
        public void startTest(Test test){};
        
    } // XTestResultListener
}
