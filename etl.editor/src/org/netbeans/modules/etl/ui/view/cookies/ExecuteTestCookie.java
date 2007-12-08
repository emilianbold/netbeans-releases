/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.etl.ui.view.cookies;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.swing.SwingUtilities;
import org.netbeans.modules.etl.codegen.ETLProcessFlowGenerator;
import org.netbeans.modules.etl.codegen.ETLProcessFlowGeneratorFactory;
import org.netbeans.modules.etl.codegen.ETLScriptBuilderModel;
import org.netbeans.modules.etl.model.ETLDefinition;
import org.netbeans.modules.etl.ui.DataObjectHelper;
import org.netbeans.modules.etl.ui.DataObjectProvider;
import org.netbeans.modules.etl.ui.ETLDataObject;
import org.netbeans.modules.etl.ui.model.impl.ETLCollaborationModel;
import org.netbeans.modules.etl.ui.view.ETLCollaborationTopComponent;
import org.netbeans.modules.sql.framework.model.ValidationInfo;
import org.netbeans.modules.sql.framework.ui.SwingWorker;
import org.netbeans.modules.sql.framework.ui.utils.UIUtil;
import org.netbeans.modules.sql.framework.ui.view.SQLLogView;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import com.sun.etl.engine.ETLEngine;
import com.sun.etl.engine.ETLEngineExecEvent;
import com.sun.etl.engine.ETLEngineListener;
import com.sun.etl.engine.ETLEngineLogEvent;
import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.utils.Logger;
import com.sun.sql.framework.utils.StringUtil;

/**
 * Encapsulates access control and execution of test eTL process on a selected
 * SQLDefinition.
 *
 * @author Jonathan Giron
 * @version $Revision$
 */
public class ExecuteTestCookie implements Node.Cookie {

    private static final String NL = System.getProperty("line.separator", "\n");
    private final String logCategory = ExecuteTestCookie.class.getName();
    private ETLEngine engine;
    private ETLCollaborationTopComponent etlView;
    private ETLDataObject dObj;
    private SQLLogView logView;
    private long startTime;
    private long endTime;

    /**
     * Creates a new instance of ETLEditorSaveAction associated with the given
     * data object.
     *
     * @param mObj etldataobject to associate with this cookie.
     */
    public ExecuteTestCookie() {
    }

    /** Executes the test process for the associated SQLDefinition. */
    public void start() {
        try {
            etlView = DataObjectProvider.getProvider().getActiveDataObject().getETLEditorTC();
        } catch (Exception ex) {
            // ignore
        }
        if (etlView != null) {
            logView = etlView.showLog();
        }

        try {
            ETLCollaborationModel collabModel = DataObjectProvider.getProvider().getActiveDataObject().getModel();
            if (collabModel == null) {
                throw new BaseException("Collaboration model is null");
            }

            ETLDefinition execModel = collabModel.getETLDefinition();
            if (execModel == null) {
                throw new BaseException("Repository model is null");
            }

            // WT #67452: Validate collab prior to executing engine.
            List invalidObjectList = execModel.validate();
            if (!invalidObjectList.isEmpty()) {
                String errMsg = "";

                Iterator iter = invalidObjectList.iterator();
                boolean validationErr = false;
                while (iter.hasNext()) {
                    ValidationInfo invalidObj = (ValidationInfo) iter.next();
                    if (invalidObj.getValidationType() == ValidationInfo.VALIDATION_ERROR) {
                        errMsg += invalidObj.getDescription();
                        errMsg += NL;
                        validationErr = true;
                    }
                }

                if (validationErr) {
                    throw new BaseException("Invalid eTL Collaboration." + NL + errMsg);
                }
                logView.appendToView(errMsg + NL);
            }

            logView.appendToView(NbBundle.getMessage(ExecuteTestCookie.class, "MSG_execution_started"));

            startTime = System.currentTimeMillis();
            RunEngineWorkerThread runEThread = new RunEngineWorkerThread(execModel);
            DataObjectHelper.setWaitCursor();
            String title = NbBundle.getMessage(ExecuteTestCookie.class, "MSG_ExecuteTest");
            String msg = NbBundle.getMessage(ExecuteTestCookie.class, "MSG_ExecuteProgress");
            UIUtil.startProgressDialog(title, msg);
            runEThread.start();
        } catch (Exception e) {
            String msg = NbBundle.getMessage(ExecuteTestCookie.class, "MSG_execute_failed", e.getMessage());

            // Ensure progress bar dialog box is removed from display even if listener
            // never gets the message that engine is done.
            SwingUtilities.invokeLater(new CloseProgressBarTask());

            logView.appendToView(msg);
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE));
            Logger.printThrowable(Logger.ERROR, logCategory, null, "Problem in executing engine.", e);
        }
    }

    private final class CloseProgressBarTask implements Runnable {

        public void run() {
            UIUtil.stopProgressDialog();
            DataObjectHelper.setDefaultCursor();
        }
    }

    private class UIEngineListener implements ETLEngineListener {

        /**
         * This method will be called at the end of execution of the workflow.
         * @param event Event associated with Execution of ETL.
         */
        public synchronized void executionPerformed(ETLEngineExecEvent event) {
            if ((event.getStatus() == ETLEngine.STATUS_COLLAB_COMPLETED) || (event.getStatus() == ETLEngine.STATUS_COLLAB_EXCEPTION)) {

                Logger.print(Logger.INFO, logCategory, "eTL engine execution completed...");
                // Ensure GUI change occurs in the event-dispatch thread.
                SwingUtilities.invokeLater(new CloseProgressBarTask());
                try {
                    endTime = System.currentTimeMillis();
                    StringBuilder msgBuf = new StringBuilder(100);
                    String msg = (event.getStatus() == ETLEngine.STATUS_COLLAB_COMPLETED) ? "MSG_executed_success" : "MSG_executed_errors"; // No I18N
                    msgBuf.append(NbBundle.getMessage(ExecuteTestCookie.class, msg));
                    msgBuf.append(NbBundle.getMessage(ExecuteTestCookie.class, "MSG_execution_time", new Long((endTime - startTime) / 1000), new Long((endTime - startTime) % 1000)));
                    logView.appendToView(msgBuf.toString());
                    if (event.getStatus() == ETLEngine.STATUS_COLLAB_COMPLETED) {
                        int rowsProcessed = 0;
                        Iterator it = engine.getContext().getStatistics().getKnownTableNames().iterator();
                        while (it.hasNext()) {
                            rowsProcessed += engine.getContext().getStatistics().getRowsInsertedCount((String) it.next());
                        }
                        msgBuf.append("Rows Processed: " + String.valueOf(rowsProcessed));
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msgBuf.toString(), NotifyDescriptor.INFORMATION_MESSAGE));
                    }
                } catch (Exception ex) {
                    Logger.printThrowable(Logger.ERROR, logCategory, null, "Problem while handling ETLEngineExecEvent for current execution.", ex);
                } finally {
                    // Ensure dialog box is removed from display - should be harmless if called twice.
                    SwingUtilities.invokeLater(new CloseProgressBarTask());
                    engine.stopETLEngine();
                    notifyAll();
                }
            }
        }

        public void updateOutputMessage(ETLEngineLogEvent evt) {
            if (evt != null && logView != null) {
                String msg = NbBundle.getMessage(ExecuteTestCookie.class, "MSG_output_template", evt.getSourceName(), evt.getLogMessage());
                logView.appendToView(msg);
                Logger.print(evt.getLogLevel(), logCategory, msg);
            }
        }
    }

    private class RunEngineWorkerThread extends SwingWorker {

        private ETLDefinition execModel;
        private List throwableList = new ArrayList();

        public RunEngineWorkerThread(ETLDefinition execModel) {
            this.execModel = execModel;
        }

        @SuppressWarnings(value = "unchecked")
        public Object construct() {
            if (execModel != null) {
                ClassLoader origLoader = Thread.currentThread().getContextClassLoader();
                try {
                    ETLProcessFlowGenerator flowGen = ETLProcessFlowGeneratorFactory.getCollabFlowGenerator(execModel.getSQLDefinition(), false);
                    flowGen.applyConnectionDefinitions();
                    engine = flowGen.getScript();

                    //RIT print out the content of etl engine file
                    //System.out.println("printing etl engine file content: \n" + engine.toXMLString());

                    UIEngineListener listener = new UIEngineListener();

                    // WT #67399: Ensure we use the class loader associated with
                    // SQLFramework moudle, to avoid instantiating Axion classes which are
                    // associated with eBAM.
                    Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

                    engine.exec(listener);
                    synchronized (listener) {
                        listener.wait();
                    }
                    throwableList = engine.getContext().getThrowableList();
                } catch (Exception ex) {
                    Logger.printThrowable(Logger.ERROR, "Temp", this, "Exception:", ex);
                    throwableList.add(ex);
                } finally {
                    Thread.currentThread().setContextClassLoader(origLoader);
                    removeInstanceDBFolder();
                }
            } else {
                throwableList.add(new BaseException("No eTL collaboration model to execute"));
                logView.appendToView(NbBundle.getMessage(ExecuteTestCookie.class, "MSG_execution_failed"));
            }

            return "";
        }

        // Runs on the event-dispatching thread.
        @Override
        public void finished() {
            if (throwableList.size() != 0) {
                new CloseProgressBarTask().run();

                writeToAppLog(throwableList);

                StringBuilder msgBuf = new StringBuilder(100);
                ListIterator iter = throwableList.listIterator();
                while (iter.hasNext()) {
                    Throwable t = (Throwable) iter.next();
                    String detailMsg = t.getMessage();
                    if (StringUtil.isNullString(detailMsg)) {
                        detailMsg = NbBundle.getMessage(ExecuteTestCookie.class, "MSG_executed_error_unknown");
                    }

                    detailMsg.trim();
                    msgBuf.append(iter.nextIndex()).append(". ").append(detailMsg).append(NL);
                }

                String msg = NbBundle.getMessage(ExecuteTestCookie.class, "MSG_executed_errors", new Integer(throwableList.size()), msgBuf.toString());

                logView.appendToView(msg);
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE));
            }
        }

        /**
         * Logs all Throwables, if any, encountered during an execution run to the system
         * log.
         *
         * @param throwableList List of Throwables to be logged
         */
        private void writeToAppLog(List throwables) {
            if (throwables.size() != 0) {
                ListIterator iter = throwables.listIterator();
                Logger.print(Logger.ERROR, logCategory, "Exceptions caught during engine execution:");
                while (iter.hasNext()) {
                    Throwable t = (Throwable) iter.next();
                    Logger.printThrowable(Logger.ERROR, logCategory, null, "> Exception " + Integer.toString(iter.nextIndex()), t);
                }
            }
        }

        private boolean deleteFile(File file) throws Exception {
            if (!file.exists()) {
                return true;
            }
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    deleteFile(files[i]);
                }
            }
            return file.delete();
        }

        private void removeInstanceDBFolder() {
            try {
                File workingFolder = new File(ETLScriptBuilderModel.ETL_DESIGN_WORK_FOLDER);
                deleteFile(workingFolder);
            } catch (Exception ex) {
                Logger.printThrowable(Logger.WARN, logCategory, this, "Error deleting working folder.", ex);
            }
        }
    }
}
