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
import org.netbeans.modules.etl.model.ETLDefinition;
import org.netbeans.modules.etl.ui.DataObjectHelper;
import org.netbeans.modules.etl.ui.DataObjectProvider;
import org.netbeans.modules.etl.ui.model.impl.ETLCollaborationModel;
import org.netbeans.modules.etl.ui.view.ETLCollaborationTopPanel;
import org.netbeans.modules.sql.framework.model.ValidationInfo;
import org.netbeans.modules.sql.framework.ui.SwingWorker;
import org.netbeans.modules.sql.framework.ui.utils.UIUtil;
import org.netbeans.modules.sql.framework.ui.output.SQLLogView;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import com.sun.etl.engine.ETLEngine;
import com.sun.etl.engine.ETLEngineExecEvent;
import com.sun.etl.engine.ETLEngineListener;
import com.sun.etl.engine.ETLEngineLogEvent;
import com.sun.etl.exception.BaseException;
import net.java.hulp.i18n.Logger;
import com.sun.etl.utils.StringUtil;
import org.axiondb.ExternalConnectionProvider;
import org.netbeans.modules.etl.codegen.ETLCodegenUtil;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.model.SQLDefinition;

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
    private ETLCollaborationTopPanel topPanel;
    private SQLLogView logView;
    private long startTime;
    private long endTime;
    private static transient final Logger mLogger = Logger.getLogger(ExecuteTestCookie.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

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
            topPanel = DataObjectProvider.getProvider().getActiveDataObject().getETLEditorTopPanel();
        } catch (Exception ex) {
            // ignore
        }
        if (topPanel != null) {
            logView = topPanel.showLog();
        }

        try {
            //System.setProperty(ExternalConnectionProvider.EXTERNAL_CONNECTION_PROVIDER_PROPERTY_NAME, AxionExternalConnectionProvider.class.getName());
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

            String nbBundle1 = mLoc.t("BUND003: Execution started.");
            logView.appendToView(nbBundle1.substring(15));

            startTime = System.currentTimeMillis();
            RunEngineWorkerThread runEThread = new RunEngineWorkerThread(execModel);
            DataObjectHelper.setWaitCursor();
            
            String nbBundle2 = mLoc.t("BUND004: Executing");
            String title = nbBundle2.substring(15);
            String nbBundle3 = mLoc.t("BUND005: Executing, please wait...");
            String msg = nbBundle3.substring(15);
            UIUtil.startProgressDialog(title, msg);
            runEThread.start();
        } catch (Exception e) {
            String nbBundle4 = mLoc.t("BUND006: Execution failed:{0}",e.getMessage());
            String msg = nbBundle4.substring(15);
            // Ensure progress bar dialog box is removed from display even if listener
            // never gets the message that engine is done.
            SwingUtilities.invokeLater(new CloseProgressBarTask());

            logView.appendToView(msg);
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE));
            mLogger.infoNoloc(mLoc.t("EDIT011: Problem in executing engine.")+e);
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
                mLogger.infoNoloc(mLoc.t("EDIT012: eTL engine execution completed..."));
                // Ensure GUI change occurs in the event-dispatch thread.
                SwingUtilities.invokeLater(new CloseProgressBarTask());
                try {
                    endTime = System.currentTimeMillis();
                    StringBuilder msgBuf = new StringBuilder(100);
                    String nbBundle5 = mLoc.t("BUND007: Execution completed successfully.{0}",NL);
                    String msg = (event.getStatus() == ETLEngine.STATUS_COLLAB_COMPLETED) ? nbBundle5.substring(15) : "MSG_executed_errors"; // No I18N
                    String nbBundle11 = mLoc.t("BUND008: Execution completed with {0,choice,0#unknown error|1#error|1<errors}:{1}");
                    String nbBundle20 = mLoc.t("BUND009: {0}",msg);
                    msgBuf.append(nbBundle20.substring(15));
                    String nbBundle6 = mLoc.t("BUND010: Execution time: {0}.{1} seconds.{2}",new Long((endTime - startTime) / 1000),new Long((endTime - startTime) % 1000),NL);
                    msgBuf.append(nbBundle6.substring(15));
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
                    mLogger.infoNoloc(mLoc.t("EDIT013: Problem while handling ETLEngineExecEvent for current execution.")+ex);
                } finally {
                    System.setProperty(ExternalConnectionProvider.EXTERNAL_CONNECTION_PROVIDER_PROPERTY_NAME, "");
                    // Ensure dialog box is removed from display - should be harmless if called twice.
                    SwingUtilities.invokeLater(new CloseProgressBarTask());
                    engine.stopETLEngine();
                    notifyAll();
                }
            }
        }

        public void updateOutputMessage(ETLEngineLogEvent evt) {
            if (evt != null && logView != null) {
                String nbBundle7 = mLoc.t("BUND011: {0}: {1}",evt.getSourceName(),evt.getLogMessage());
                String msg = nbBundle7.substring(15);
                logView.appendToView(msg);
                mLogger.infoNoloc(mLoc.t("EDIT014: evt.getLogLevel(){0}", msg));
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
                SQLDefinition sqlDefn = execModel.getSQLDefinition();
                String instanceDBFolder = ETLCodegenUtil.getEngineInstanceWorkingFolder(sqlDefn.getAxiondbWorkingDirectory());
                String instanceDBParent = new File(instanceDBFolder).getParent();
                try {
                    
                    ETLProcessFlowGenerator flowGen = ETLProcessFlowGeneratorFactory.getCollabFlowGenerator(execModel.getSQLDefinition(), false);
                    flowGen.setWorkingFolder(sqlDefn.getAxiondbWorkingDirectory());
                    flowGen.setInstanceDBName("instancedb");
                    flowGen.setInstanceDBFolder(instanceDBFolder);
                    flowGen.applyConnectionDefinitions(true, true);
                    engine = flowGen.getScript();
                    engine.getContext().putValue("AXIONDB_DATA_DIR", sqlDefn.getAxiondbDataDirectory());
                    engine.getContext().putValue("DESIGN_TIME_ATTRS", engine.getInputAttrMap());


                    //RIT print out the content of etl engine file
                    mLogger.infoNoloc("printing etl engine file content: \n" + engine.toXMLString());

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
                    mLogger.errorNoloc(mLoc.t("EDIT015: Exception: "), ex);
                    throwableList.add(ex);
                } finally {
                    Thread.currentThread().setContextClassLoader(origLoader);
                    removeInstanceDBFolder(instanceDBFolder);
                    removeInstanceDBFolder(instanceDBParent);
                }
            } else {
                throwableList.add(new BaseException("No eTL collaboration model to execute"));
                String nbBundle8 = mLoc.t("BUND012: Execution failed: ");
                logView.appendToView(nbBundle8.substring(15));
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
                        String nbBundle9 = mLoc.t("BUND013: Execution with unknown error.  Please review messages.log under netbeans user home directory for more details. completed with unknown error.  Please review messages.log under netbeans user home directory for more details.");
                        detailMsg = nbBundle9.substring(15);
                    }

                    detailMsg.trim();
                    msgBuf.append(iter.nextIndex()).append(". ").append(detailMsg).append(NL);
                }

                String nbBundle10 = mLoc.t("BUND008: Execution completed with {0,choice,0#unknown error|1#error|1<errors}:{1}",new Integer(throwableList.size()),msgBuf.toString());
                String msg = nbBundle10.substring(15);
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
                mLogger.infoNoloc(mLoc.t("EDIT016: Exceptions caught during engine execution:{0}", logCategory));
                while (iter.hasNext()) {
                    Throwable t = (Throwable) iter.next();
                    mLogger.errorNoloc(mLoc.t("EDIT017: > Exception{0}", Integer.toString(iter.nextIndex())), t);
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

        private void removeInstanceDBFolder(String instanceDBFolder) {
            try {
                File workingFolder = new File(instanceDBFolder);
                deleteFile(workingFolder);
            } catch (Exception ex) {
                mLogger.errorNoloc(mLoc.t("EDIT018: Error deleting working folder.{0}", logCategory), ex);
            }
        }
    }
}
