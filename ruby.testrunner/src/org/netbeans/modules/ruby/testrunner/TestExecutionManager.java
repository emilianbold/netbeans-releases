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

package org.netbeans.modules.ruby.testrunner;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.extexecution.api.ExecutionDescriptor;
import org.netbeans.modules.extexecution.api.ExecutionService;
import org.netbeans.modules.ruby.platform.execution.RubyExecutionDescriptor;
import org.netbeans.modules.ruby.platform.execution.RubyProcessCreator;
import org.netbeans.modules.ruby.testrunner.ui.Manager;
import org.netbeans.modules.ruby.testrunner.ui.TestHandlerFactory;
import org.netbeans.modules.ruby.testrunner.ui.TestRunnerInputProcessorFactory;
import org.netbeans.modules.ruby.testrunner.ui.TestRunnerLineConvertor;
import org.netbeans.modules.ruby.testrunner.ui.TestSession;
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
public final class TestExecutionManager {

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
    private final RequestProcessor testExecutionProcessor = new RequestProcessor("Ruby Test Execution Processor"); //NOI18N
    
    private static final TestExecutionManager INSTANCE = new TestExecutionManager();
    
    private TestExecutionManager() {
    }

    public static TestExecutionManager getInstance() {
        return INSTANCE;
    }

    synchronized void reset() {
        this.finished = false;
    }
    /**
     * Starts a RubyExecution with the given executionDescriptor and testRecognizer.
     * 
     * @param executionDescriptor
     * @param testRecognizer
     */
    synchronized void start(RubyExecutionDescriptor rubyDescriptor,
            TestHandlerFactory handlerFactory, TestSession session) {

        setFinished(false);
        final Manager manager = Manager.getInstance();
        outConvertor = new TestRunnerLineConvertor(manager, session, handlerFactory);
        errConvertor = new TestRunnerLineConvertor(manager, session, handlerFactory);
        rubyDescriptor.addOutConvertor(outConvertor);
        rubyDescriptor.addErrConvertor(errConvertor);
        rubyDescriptor.setOutProcessorFactory(new TestRunnerInputProcessorFactory(manager, session, handlerFactory.printSummary()));
        rubyDescriptor.setErrProcessorFactory(new TestRunnerInputProcessorFactory(manager, session, false));
        rubyDescriptor.lineBased(true);


        RubyProcessCreator rpc = new RubyProcessCreator(rubyDescriptor);

        ExecutionDescriptor descriptor = rubyDescriptor.toExecutionDescriptor()
                .postExecution(new Runnable() {

            public void run() {
                refresh();
            }
        });
        execution = ExecutionService.newService(rpc, descriptor, rubyDescriptor.getDisplayName());
        runExecution();
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
    public synchronized boolean isFinished() {
        return result != null && result.isDone();
    }
    
    private void setFinished(boolean finished) {
        this.finished = finished;
        changeSupport.fireChange();
    }
    /**
     * Re-runs the last run test execution.
     */
    public synchronized void rerun() {
        assert isFinished();
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
        outConvertor.refreshSession();
        errConvertor.refreshSession();
    }
}
