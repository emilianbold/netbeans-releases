/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
import org.netbeans.modules.etl.codegen.ETLProcessFlowGenerator;
import org.netbeans.modules.etl.codegen.ETLProcessFlowGeneratorFactory;
import org.netbeans.modules.etl.codegen.ETLScriptBuilderModel;
import org.netbeans.modules.etl.model.ETLDefinition;
import org.netbeans.modules.etl.ui.DataObjectHelper;
import org.netbeans.modules.sql.framework.model.ValidationInfo;
import org.netbeans.modules.sql.framework.ui.SwingWorker;
import org.openide.nodes.Node;
import com.sun.etl.engine.ETLEngine;
import com.sun.etl.engine.ETLEngineExecEvent;
import com.sun.etl.engine.ETLEngineListener;
import com.sun.etl.engine.ETLEngineLogEvent;
import com.sun.etl.exception.BaseException;
import net.java.hulp.i18n.Logger;
import com.sun.etl.utils.StringUtil;
import java.awt.Component;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.xml.parsers.DocumentBuilderFactory;
import org.netbeans.modules.etl.codegen.ETLCodegenUtil;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.model.impl.ETLDefinitionImpl;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.w3c.dom.Element;

/**
 * Encapsulates access control and execution of test eTL process on  all
 * SQLDefinition.
 *
 * @author Sailaja k
 */
public class ExecuteAllCollabCookie implements Node.Cookie {

    private static final String NL = System.getProperty("line.separator", "\n");
    private final String logCategory = ExecuteAllCollabCookie.class.getName();
    private ETLEngine engine;
    private static transient final Logger mLogger = Logger.getLogger(ExecuteAllCollabCookie.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    private JScrollPane logView;
    public CollabStatusObject statusObject;
    public Vector dataList = new Vector();

    public ExecuteAllCollabCookie() {
        logView = new JScrollPane();
        logView.setPreferredSize(new Dimension(500, 150));
    }

    public void showSQLLogView() {
        // add to output.
        Vector colNames = new Vector();
        colNames.add("S.No");
        colNames.add("Collaboration\nName");
        colNames.add("Rows\nProcessed");
        colNames.add("Status");
        colNames.add("Failure\nCause");
        JTable tbl = new JTable(getData(), colNames) {

            @Override
            public boolean isCellEditable(int rowIndex, int vColIndex) {
                return false;
            }
        };
        TableColumnModel tcm = tbl.getColumnModel();
        setWidth(tcm.getColumn(0), 5);
        setWidth(tcm.getColumn(2), 30);
        setWidth(tcm.getColumn(3), 10);
        setWidth(tcm.getColumn(4), 200);
        //TableDataRenderer tableDataRenderer = new TableDataRenderer();
        //tcm.getColumn(4).setCellRenderer(tableDataRenderer);

        TableHeaderRenderer renderer = new TableHeaderRenderer();
        Enumeration e = tcm.getColumns();
        while (e.hasMoreElements()) {
            ((TableColumn) e.nextElement()).setHeaderRenderer(renderer);
        }

        logView.add(tbl);
        logView.setViewportView(tbl);
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(logView, NotifyDescriptor.INFORMATION_MESSAGE));
    }

    private void setWidth(TableColumn column, int width) {
        column.setMinWidth(width);
        column.setWidth(width);
        //column.setMaxWidth(width);
        column.setPreferredWidth(width);
    }

    /** Executes the test process for the associated SQLDefinition. */
    public void startExec(ArrayList etlFileList) {
        try {
            for (int i = 0; i < etlFileList.size(); i++) {
                File etlfile = (File) etlFileList.get(i);
                ETLDefinition execModel = getSQLDef(etlfile);

                java.util.logging.Logger.getLogger(ExecuteAllCollabCookie.class.getName()).info("eTL engine execution started... ");
                List invalidObjectList = execModel.validate();
                if (!invalidObjectList.isEmpty()) {
                    Iterator iter = invalidObjectList.iterator();
                    while (iter.hasNext()) {
                        ValidationInfo invalidObj = (ValidationInfo) iter.next();
                        if (invalidObj.getValidationType() == ValidationInfo.VALIDATION_ERROR) {
                            String errMsg = invalidObj.getDescription();
                            errMsg += NL;
                            java.util.logging.Logger.getLogger(ExecuteAllCollabCookie.class.getName()).severe(" Invalid eTL Collaboration." + NL + errMsg);
                        //logView.append("Invalid eTL Collaboration." + NL + errMsg);
                        }
                    }
                }
                statusObject = new CollabStatusObject();
                statusObject.setIndex(i + 1);
                statusObject.setCollabstatus("Failed");
                statusObject.setCollabName(execModel.getDisplayName());
                RunEngineWorkerThread runEThread = new RunEngineWorkerThread(execModel);
                DataObjectHelper.setWaitCursor();
                runEThread.start();
                while (!runEThread.isFinishedStatus()) {
                }
                setVectorList(statusObject);

            }//forloop
            showSQLLogView();

        } catch (Exception e) {
            java.util.logging.Logger.getLogger(ExecuteAllCollabCookie.class.getName()).info("Exception in ExecuteAll " + e.getMessage());
        }

    }

    public Vector getData() {

        Iterator it = getVectorList().iterator();
        while (it.hasNext()) {
            Vector v = new Vector(5);

            CollabStatusObject obj = (CollabStatusObject) it.next();
            v.add(0, obj.getIndex());
            v.add(1, obj.getCollabName());
            v.add(2, obj.getRowsProcessed());
            v.add(3, obj.getCollabstatus());
            v.add(4, obj.getFailureCause());

            dataList.add(v);
        }
        return dataList;
    }

    private class UIEngineListener implements ETLEngineListener {

        private ETLDefinition execModel;

        public UIEngineListener() {
        }

        public UIEngineListener(ETLDefinition execModel) {
            this.execModel = execModel;

        }

        /**
         * This method will be called at the end of execution of the workflow.
         * @param event Event associated with Execution of ETL.
         */
        public synchronized void executionPerformed(ETLEngineExecEvent event) {
            String statusValue = "Failed";
            int rowsProcessed = 0;

            if ((event.getStatus() == ETLEngine.STATUS_COLLAB_COMPLETED) || (event.getStatus() == ETLEngine.STATUS_COLLAB_EXCEPTION)) {
                try {
                    StringBuilder msgBuf = new StringBuilder(100);
                    String nbBundle5 = mLoc.t("BUNA217: Execution completed successfully.{0}", NL);
                    String msg = (event.getStatus() == ETLEngine.STATUS_COLLAB_COMPLETED) ? nbBundle5.substring(15) : "MSG_executed_errors"; // No I18N
                    if (event.getStatus() == ETLEngine.STATUS_COLLAB_EXCEPTION) {
                        statusValue = "Failed";
                    }
                    java.util.logging.Logger.getLogger(ExecuteAllCollabCookie.class.getName()).info("msgBuf.toString()... " + msgBuf.toString());
                    if (event.getStatus() == ETLEngine.STATUS_COLLAB_COMPLETED) {
                        statusValue = "Success";
                        Iterator it = engine.getContext().getStatistics().getKnownTableNames().iterator();
                        while (it.hasNext()) {
                            rowsProcessed += engine.getContext().getStatistics().getRowsInsertedCount((String) it.next());
                        }

                    }
                    statusObject.setCollabName(execModel.getDisplayName());
                    statusObject.setRowsProcessed(rowsProcessed);
                    statusObject.setCollabstatus(statusValue);

                } catch (Exception ex) {
                    mLogger.errorNoloc(mLoc.t("EDIT213: Problem while handling ETLEngineExecEvent for current execution .."), ex);
                } finally {
                    engine.stopETLEngine();
                    notifyAll();

                }

            }
        }

        public void updateOutputMessage(ETLEngineLogEvent evt) {
            if (evt != null) {
                String nbBundle7 = mLoc.t("BUNA211: {0} : {1}.", evt.getSourceName(), evt.getLogMessage());
                String msg = nbBundle7.substring(15);
                // logView.append(msg);
                mLogger.infoNoloc(mLoc.t("EDIT214: evt.getLogLevel(){0}.", msg));
            }
        }
    }
    Vector vList = new Vector();

    private void setVectorList(CollabStatusObject v) {
        vList.add(v);
    }

    public Vector getVectorList() {
        return vList;
    }

    private class RunEngineWorkerThread extends SwingWorker {

        private List throwableList = new ArrayList();
        private ETLDefinition execModel;
        public boolean finishedStatus;
        // public CollabStatusObject statusObject;
        public boolean isFinishedStatus() {
            return finishedStatus;
        }

        public void setFinishedStatus(boolean finishedStatus) {
            this.finishedStatus = finishedStatus;
        }

        public RunEngineWorkerThread(ETLDefinition execModel) {
            this.execModel = execModel;
            finishedStatus = false;
        // statusObject = new CollabStatusObject();
        }

        public Object construct() {
            ClassLoader origLoader = Thread.currentThread().getContextClassLoader();
            try {
                // WT #67399: Ensure we use the class loader associated with
                // SQLFramework moudle, to avoid instantiating Axion classes which are
                // associated with eBAM.
                Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

                if (execModel != null) {
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

                        //print out the content of etl engine file
                        //mLogger.infoNoloc("printing etl engine file content: \n" + engine.toXMLString());

                        UIEngineListener listener = new UIEngineListener(execModel);
                        engine.exec(listener);
                        synchronized (listener) {
                            listener.wait();
                        }
                        throwableList = engine.getContext().getThrowableList();

                    } catch (Exception ex) {
                        mLogger.errorNoloc(mLoc.t("EDIT215: Exception :  "), ex);
                        throwableList.add(ex);
                    } finally {
                        removeInstanceDBFolder(instanceDBFolder);
                        removeInstanceDBFolder(instanceDBParent);
                    }
                } else {
                    throwableList.add(new BaseException("Invalid eTL collaboration model to execute"));
                    String nbBundle8 = mLoc.t("BUNA212: Execution failed. ");
                    mLogger.infoNoloc(nbBundle8.substring(15));
                }

            } finally {
                Thread.currentThread().setContextClassLoader(origLoader);
            }// end of for 

            return "";
        }

        // Runs on the event-dispatching thread.
        @Override
        public void finished() {
            String emsg = null;
            if (throwableList.size() != 0) {
                //new CloseProgressBarTask().run();
                writeToAppLog(throwableList);

                StringBuilder msgBuf = new StringBuilder(100);
                ListIterator iter = throwableList.listIterator();
                while (iter.hasNext()) {
                    Throwable t = (Throwable) iter.next();
                    String detailMsg = t.getMessage();
                    emsg = detailMsg;
                    if (StringUtil.isNullString(detailMsg)) {
                        String nbBundle9 = mLoc.t("BUNA313: Execution with unknown error. Please review messages.log under netbeans user home directory for more details .");
                        detailMsg = nbBundle9.substring(15);
                    }

                    detailMsg.trim();
                    msgBuf.append(iter.nextIndex()).append(". ").append(detailMsg).append(NL);
                    mLogger.infoNoloc("FailureCause detailMsg " + detailMsg);
                }
                String nbBundle10 = mLoc.t("BUNA318: Execution completed with  {0,choice,0#unknown error|1#error|1<errors}: {1} ", new Integer(throwableList.size()), msgBuf.toString());
                String msg = nbBundle10.substring(15);
                mLogger.infoNoloc(msg);
            }
            statusObject.setFailureCause(emsg);
            mLogger.infoNoloc("FailureCause detailMsg " + emsg);
            mLogger.infoNoloc("end after show finished() ");
            setFinishedStatus(true);
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
                mLogger.infoNoloc(mLoc.t("EDIT316: Exceptions caught during engine execution : {0}", logCategory));
                while (iter.hasNext()) {
                    Throwable t = (Throwable) iter.next();
                    mLogger.errorNoloc(mLoc.t("EDIT317:  Exception{0}", Integer.toString(iter.nextIndex())), t);
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
                mLogger.errorNoloc(mLoc.t("EDIT318: Error deleting working folder. {0}.", logCategory), ex);
            }
        }
    }

    public ETLDefinition getSQLDef(File etlfile) {
        ETLDefinitionImpl def = null;
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            Element root = f.newDocumentBuilder().parse(etlfile).getDocumentElement();

            def = new ETLDefinitionImpl();
            def.parseXML(root);
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(ExecuteAllCollabCookie.class.getName()).info("Exception in getSQLDef(): " + e.getMessage());
        }
        return def;
    }

    public class TableDataRenderer extends JTextArea implements TableCellRenderer {

        public int rowheight;

        TableDataRenderer() {
            setOpaque(true);
            setLineWrap(true);
            setWrapStyleWord(true);
            rowheight = this.getRowHeight();
        }

        public Component getTableCellRendererComponent(JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
            int height;
            TableModel model = table.getModel();
            int rowCount = model.getRowCount();
            int columnCount = model.getColumnCount();

            for (int i = 0; i < rowCount; i++) {
                int max = 0;
                for (int j = 0; j < columnCount; j++) {
                    int rows = table.getModel().getValueAt(0, i).toString().length() * 6 /
                            table.getColumn(table.getColumnName(i)).getWidth() + 1;
                    height = this.rowheight * rows * 2;
                    if (max < height) {
                        max = height;
                    }
                }
                table.setRowHeight(i, 30);
            }

            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    class TableHeaderRenderer extends JList implements TableCellRenderer {

        public TableHeaderRenderer() {
            setOpaque(true);
            setForeground(UIManager.getColor("TableHeader.foreground"));
            setBackground(UIManager.getColor("TableHeader.background"));
            setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            ListCellRenderer renderer = getCellRenderer();
            ((JLabel) renderer).setHorizontalAlignment(JLabel.CENTER);
            setCellRenderer(renderer);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setFont(table.getFont());
            String str = (value == null) ? "" : value.toString();
            BufferedReader br = new BufferedReader(new StringReader(str));
            String line;
            Vector v = new Vector();
            try {
                while ((line = br.readLine()) != null) {
                    v.addElement(line);
                }
            } catch (Exception ex) {               
            }
            setListData(v);
            return this;
        }
    }
}
