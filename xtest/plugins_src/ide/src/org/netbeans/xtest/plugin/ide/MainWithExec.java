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

package org.netbeans.xtest.plugin.ide;

import java.awt.event.ActionEvent;
import java.util.Collection;
import org.netbeans.core.NbPlaces;
import org.netbeans.core.execution.ProcessNode;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.Iterator;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestListener;
import junit.framework.TestResult;
import org.netbeans.xtest.plugin.ide.services.XTestErrorManager;
import org.openide.util.actions.SystemAction;
import org.netbeans.xtest.testrunner.JUnitTestRunner;

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
    
    // finds terminate process action in the action array
    private static SystemAction findTerminateAction(SystemAction[] actions) {
        if (actions == null) {
            throw new IllegalArgumentException();
        }
        // I need to get the string from resource bundle
        // bundle = org.netbeans.core.execution.Bundle.properties
        // key = terminateProcess
        
        String terminateString = NbBundle.getMessage(ProcessNode.class,"terminateProcess");
        for (int i=0; i<actions.length; i++) {
            if (terminateString.equals(actions[i].getName())) {
                return actions[i];
            }
        }
        return null;
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
            
            
            // first - try to use ExecutionEngine ...
            /*
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
             **/
            
            // second - use Runtime and Processes Nodes (to be sure) ...
            
            // get runtime node
            Node runtime = NbPlaces.getDefault().environment();
            
            // find Processes node
            // bundle = org.netbeans.core.execution.Bundle.properties
            // key = Processes
            //String processesString = "Processes";
            String processesString = NbBundle.getMessage(ProcessNode.class,"Processes");
            if (processesString == null) {
                processesString = "Processes";
            }
            Node processesNode = runtime.getChildren().findChild(processesString);
            
            // get all running processes
            if (processesNode != null) {
                Node[] runningProcesses = processesNode.getChildren().getNodes();
                
                if (runningProcesses != null) {
                    for (int i=0; i<runningProcesses.length; i++) {
                        // if process does not start with org.netbeans.xtest - these are
                        // processes driving the tests, we should not kill them
                        if (!runningProcesses[i].getName().startsWith("org.netbeans.xtest")) {
                            // get actions for the processes
                            SystemAction[] actions = runningProcesses[i].getActions();
                            if (actions != null) {
                                final SystemAction terminate = findTerminateAction(actions);
                                if (terminate != null) {
                                    final ActionEvent av = new ActionEvent(runningProcesses[i],
                                    TERMINATE_CODE,TERMINATE_NAME);
                                    Main.errMan.log(ErrorManager.USER,"XTest: Stopping process: (via Execution View) "+runningProcesses[i].getName());
                                    // is there any status returned from the method ?
                                    // need to use dispatch thread - for details see issue #35755 and #35800
                                    try {
                                        EventQueue.invokeAndWait(new Runnable() {
                                            public void run() {
                                                terminate.actionPerformed(av);
                                            }
                                        });
                                    } catch (InterruptedException ie) {
                                        // nothing
                                    } catch (java.lang.reflect.InvocationTargetException ite) {
                                        // nothing again (might change)
                                    }
                                    
                                    terminatedProcesses++;
                                } else {
                                    // cannot terminate this process - does not have
                                    // terminate action (highly unlikely)
                                    //errMan.log(ErrorManager.USER,"XTest: Process "+runningProcesses[i].getName() + " has not terminate action. Can't terminate.");
                                    notTerminatedProcesses++;
                                }
                            } else {
                                // process does not have any actions - cannot terminate
                                // again, this is highly unlikely
                                //errMan.log(ErrorManager.USER,"XTest: Process "+runningProcesses[i].getName() + " has not any action. Can't terminate.");
                                notTerminatedProcesses++;
                            }
                        } else {
                            // not killing my own processes
                            // they are not even counted
                        }
                    }
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
            if("true".equals(System.getProperty("xtest.ide.error.manager"))) {
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
        
        public XTestResultListener(TestResult result) {
            this.result = result;
        }
        
        /** An error occurred. */
        public void addError(Test test, Throwable t) {}
        
        /** A failure occurred.*/
        public void addFailure(Test test, AssertionFailedError t){}

        /* A test ended. */
        public void endTest(Test test) {
            try {
                Iterator it = XTestErrorManager.getExceptions().iterator();
                if (it.hasNext()) {
                    // exception was thrown => add the first found exception as
                    // an error (i.e. its stack trace will be printed in results)
                    result.addError(test, (Throwable)it.next());
                    XTestErrorManager.clearExceptions();
                }
            } catch (Exception e) {
                // ClassNotFound exception, etc
                e.printStackTrace();
            }
        }
        
        /** A test started. */
        public void startTest(Test test){};
        
    } // XTestResultListener
}
