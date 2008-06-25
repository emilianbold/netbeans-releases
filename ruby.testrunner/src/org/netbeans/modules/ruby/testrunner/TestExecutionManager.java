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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.ruby.platform.RubyExecution;
import org.netbeans.modules.ruby.platform.execution.ExecutionDescriptor;
import org.netbeans.modules.ruby.platform.execution.ExecutionService;
import org.netbeans.modules.ruby.testrunner.ui.TestRecognizer;
import org.openide.util.ChangeSupport;
import org.openide.util.Task;
import org.openide.util.TaskListener;

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
    /**
     * Indicates whether the current execution has finished.
     */
    private boolean finished;
    private TestRecognizer recognizer;
    private ChangeSupport changeSupport = new ChangeSupport(this);
    
    private static final TestExecutionManager INSTANCE = new TestExecutionManager();
    
    private TestExecutionManager() {
    }

    public static TestExecutionManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Starts a RubyExecution with the given executionDescriptor and testRecognizer.
     * 
     * @param executionDescriptor
     * @param testRecognizer
     */
    synchronized void start(ExecutionDescriptor executionDescriptor, TestRecognizer testRecognizer) {
        assert executionDescriptor != null;
        assert testRecognizer != null;
        
        if (!isFinished() && execution != null) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Killing unfinished execution: " + execution);
            }
            execution.kill();
        } 
        
        recognizer = testRecognizer;
        executionDescriptor.addOutputRecognizer(recognizer);
        execution = new RubyExecution(executionDescriptor);

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Starting: " + execution);
        }
        setFinished(false);
        handleTask(execution.run());
    }
    
    private void handleTask(Task task) {
        // workaround for not being able to attach listeners
        // directly to execution service
        task.addTaskListener(new TaskListener() {

            public void taskFinished(Task task) {
                setFinished(true);
            }
        });
        setFinished(task.isFinished());
    }
    
    /**
     * Checks whether the current execution is finished.
     * 
     * @return true if the current execution has finished, 
     * false otherwise.
     */
    public synchronized boolean isFinished() {
        return finished;
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
        
        recognizer.refreshSession();
        setFinished(false);
        handleTask(execution.rerun());
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }
    
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }
    
}
