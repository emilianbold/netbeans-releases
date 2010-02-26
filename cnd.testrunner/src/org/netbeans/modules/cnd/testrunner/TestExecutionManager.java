/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.testrunner;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.modules.gsf.testrunner.api.Manager;
import org.netbeans.modules.gsf.testrunner.api.RerunHandler;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
//import org.netbeans.modules.python.testrunner.ui.Manager;
import org.netbeans.modules.cnd.testrunner.ui.TestHandlerFactory;
import org.netbeans.modules.cnd.testrunner.ui.TestRunnerInputProcessorFactory;
import org.netbeans.modules.cnd.testrunner.ui.TestRunnerLineConvertor;
//import org.netbeans.modules.python.testrunner.ui.TestSession;
import org.openide.LifecycleManager;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * Handles running and re-running of test executions.
 * 
 * <i>This class will probably not be needed after migrating to the new Execution API</i>
 * 
 * @author Erno Mononen
 */
public final class TestExecutionManager implements RerunHandler {

    private final static Logger LOGGER = Logger.getLogger(TestExecutionManager.class.getName());
    
    /**
     * The current execution.
     */
    private ExecutionService execution;
    private Future<Integer> result;
    /**
     * Indicates whether the current execution has finished.
     */
    private boolean finished;
    private TestRunnerLineConvertor outConvertor;
    private TestRunnerLineConvertor errConvertor;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final RequestProcessor testExecutionProcessor = new RequestProcessor("Python Test Execution Processor"); //NOI18N
    
    private static final TestExecutionManager INSTANCE = new TestExecutionManager();
    
    private TestExecutionManager() {
    }

    public static TestExecutionManager getInstance() {
        return INSTANCE;
    }

    synchronized void finish() {
        setFinished(true);
    }

    synchronized void reset() {
        this.finished = false;
    }
    /**
     * Inits our TestExecutionManager with the given PythonExecution. Does not
     * run the execution.
     *
     * @param pythonDescriptor
     */
//    synchronized void init(PythonExecution pythonDescriptor) {
//        try {
//            Callable<Process> rpc = pythonDescriptor.buildProcess();
//            ExecutionDescriptor descriptor = pythonDescriptor.toExecutionDescriptor();
//            execution = ExecutionService.newService(rpc, descriptor, pythonDescriptor.getDisplayName());
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//    }
    /**
     * Starts a PythonExecution with the given executionDescriptor and testRecognizer.
     * 
     * @param executionDescriptor
     * @param testRecognizer
     */
    synchronized void start(/*PythonExecution pythonDescriptor,*/
            TestHandlerFactory handlerFactory, TestSession session) {

//        setFinished(false);
//        session.setRerunHandler(this);
//        final Manager manager = Manager.getInstance();
//        outConvertor = new TestRunnerLineConvertor(manager, session, handlerFactory);
//        errConvertor = new TestRunnerLineConvertor(manager, session, handlerFactory);
//        pythonDescriptor.addOutConvertor(outConvertor);
//        pythonDescriptor.addErrConvertor(errConvertor);
//        pythonDescriptor.setOutProcessorFactory(new TestRunnerInputProcessorFactory(manager, session, handlerFactory.printSummary()));
//        pythonDescriptor.setErrProcessorFactory(new TestRunnerInputProcessorFactory(manager, session, false));
//        pythonDescriptor.lineBased(true);
//
//
//        try {
//            Callable<Process> rpc = pythonDescriptor.buildProcess();
//
//            final Runnable oldPostExecutionHook = pythonDescriptor.getPostExecutionHook();
//            ExecutionDescriptor descriptor = pythonDescriptor.toExecutionDescriptor()
//                    .postExecution(new Runnable() {
//
//                public void run() {
//                    refresh();
//                    if (oldPostExecutionHook != null) {
//                        oldPostExecutionHook.run();
//                    }
//                }
//            });
//            execution = ExecutionService.newService(rpc, descriptor, pythonDescriptor.getDisplayName());
//            runExecution();
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        }
    }

    private void runExecution() {
        result = execution.run();
        testExecutionProcessor.post(new Runnable() {
            public void run() {
                try {
                    result.get();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (CancellationException ex) {
                    // ignore
                }
                setFinished(result.isDone());
            }
        });
    }

    /**
     * Checks whether the current execution is finished.
     * 
     * @return true if the current execution has finished, 
     * false otherwise.
     */
    public synchronized boolean enabled() {
        return finished || (result != null && result.isDone());
    }
    
    private void setFinished(boolean finished) {
        this.finished = finished;
        changeSupport.fireChange();
    }
    /**
     * Re-runs the last run test execution.
     */
    public synchronized void rerun() {
        assert enabled();
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Re-running: " + execution);
        }
        refresh();
        setFinished(false);
        LifecycleManager.getDefault().saveAll();
        runExecution();
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }
    
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    /**
     * Refreshes the current session, i.e. clears all currently
     * computed test statuses.
     */
    public synchronized void refresh() {
        if (outConvertor != null) {
            outConvertor.refreshSession();
        }
        if (errConvertor != null) {
            errConvertor.refreshSession();
        }
    }
}
