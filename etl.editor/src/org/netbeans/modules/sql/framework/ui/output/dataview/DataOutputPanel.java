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
package org.netbeans.modules.sql.framework.ui.output.dataview;

import com.sun.sql.framework.exception.DBSQLException;
import org.netbeans.modules.sql.framework.ui.view.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import org.netbeans.modules.etl.ui.view.ETLCollaborationTopPanel;
import org.netbeans.modules.sql.framework.codegen.DB;
import org.netbeans.modules.sql.framework.codegen.DBFactory;
import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLJoinOperator;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;
import org.openide.util.Exceptions;

import com.sun.sql.framework.jdbc.DBConstants;
import com.sun.sql.framework.exception.BaseException;
import net.java.hulp.i18n.Logger;
import java.sql.PreparedStatement;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.ui.DataObjectProvider;
import org.netbeans.modules.sql.framework.common.jdbc.SQLUtils;
import org.netbeans.modules.sql.framework.model.RuntimeDatabaseModel;
import org.netbeans.modules.sql.framework.ui.graph.ICommand;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.output.ETLOutputPanel;
import org.netbeans.modules.sql.framework.ui.view.graph.SQLBasicTableArea;

/**
 * TopComponent hosting display of design-level SQL test output.
 *
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public abstract class DataOutputPanel extends JPanel implements ETLOutputPanel {

    private static transient final Logger mLogger = Logger.getLogger(DataOutputPanel.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    public static final String NL = System.getProperty("line.separator", "\n");
    public static final String LOG_CATEGORY = DataOutputPanel.class.getName();
    public JButton commit;
    protected JButton refreshButton;
    protected JButton truncateButton;
    protected SQLObject table;
    protected DBTableMetadata meta;
    protected JButton next;
    protected JButton last;
    protected JButton previous;
    protected JButton first;
    protected JButton deleteRow;
    protected JButton insert;
    protected JTextField refreshField;
    protected ResultSetTablePanel queryView;
    protected int recordToRefresh = 10;
    protected int maxRows = 10;
    protected JLabel totalRowsLabel;
    protected int totalCount;
    protected int nowCount = 1;
    private SQLDefinition sqlDefinition;
    private ETLCollaborationTopPanel etlView;
    private JToolBar toolbar;
    private JLabel limitRow;
    private JButton filterButton;
    private JButton[] btn = new JButton[5];

    /**
     * Creates a new instance of DataOutputPanel with the associated instance of
     * SQLDefinition.
     *
     * @param etlDefinition SQLDefinition instance to associate
     */
    protected DataOutputPanel(SQLObject etlObject, SQLDefinition sqlDefinition, boolean showTruncate, boolean showFilter) {
        this.table = etlObject;
        this.sqlDefinition = sqlDefinition;

        if (etlObject instanceof SQLDBTable) {
            this.meta = new DBTableMetadata(((SQLDBTable) this.table));
        }

        //do not show tab view if there is only one tab
        putClientProperty("TabPolicy", "HideWhenAlone"); //NOI18N
        putClientProperty("PersistenceType", "Never"); //NOI18N
        this.setLayout(new BorderLayout());

        String nbBundle = mLoc.t("BUND332: Output: {0}", sqlDefinition.getDisplayName());
        this.setName(nbBundle.substring(15));
        setBorder(BorderFactory.createEmptyBorder());

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEtchedBorder());
        GridBagLayout gl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        panel.setLayout(gl);

        //toolbar panel
        toolbar = new JToolBar();
        toolbar.setFloatable(false);

        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = GridBagConstraints.RELATIVE;
        c.anchor = GridBagConstraints.FIRST_LINE_START;

        panel.add(toolbar, c);
        validate();

        URL url = null;
        String nbBundle14 = mLoc.t("BUND333: Truncate this table");
        if (showTruncate) {
            //create truncate button
            TruncateTableAction truncAction = new TruncateTableAction(this);
            truncAction.putValue(Action.SHORT_DESCRIPTION,  nbBundle14.substring(15));
            url = getClass().getResource("/org/netbeans/modules/sql/framework/ui/resources/images/stop.gif");
            truncAction.putValue(Action.SMALL_ICON, new ImageIcon(url));
            //add truncate button
            truncateButton = new JButton(truncAction);
            btn[0] = truncateButton;
        }

        if (showFilter) {
            //add Filter button  
            showDataFilter_ActionPerformed filterAction = new showDataFilter_ActionPerformed();
            String nbBundle1 = mLoc.t("BUND334: Enable/Disable of Table Content filtering");
            filterAction.putValue(Action.SHORT_DESCRIPTION, nbBundle1.substring(15));
            url = getClass().getResource("/org/netbeans/modules/sql/framework/ui/resources/images/funnel.png");
            filterAction.putValue(Action.SMALL_ICON, new ImageIcon(url));
            filterButton = new JButton(filterAction);
            btn[0] = filterButton;
        }

        ActionListener outputListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Object src = e.getSource();
                if (src.equals(refreshButton)) {
                    refreshActionPerformed();
                } else if (src.equals(first)) {
                    firstActionPerformed();
                } else if (src.equals(last)) {
                    lastActionPerformed();
                } else if (src.equals(next)) {
                    nextActionPerformed();
                } else if (src.equals(previous)) {
                    previousActionPerformed();
                } else if (src.equals(refreshField)) {
                    setMaxActionPerformed();
                } else if (src.equals(commit)) {
                    commitActionPerformed();
                } else if (src.equals(deleteRow)) {
                    deleteRecordActionPerformed();
                } else if (src.equals(insert)) {
                    insertActionPerformed();
                }
            }
        };

        //add refresh button
        url = getClass().getResource("/org/netbeans/modules/sql/framework/ui/resources/images/refresh.png");
        refreshButton = new JButton(new ImageIcon(url));
        String nbBundle2 = mLoc.t("BUND335: Refresh records");
        refreshButton.setToolTipText(nbBundle2.substring(15));
        refreshButton.addActionListener(outputListener);
        btn[1] = refreshButton;

        // add navigation buttons
        url = getClass().getResource("/org/netbeans/modules/sql/framework/ui/resources/images/navigate_beginning.png");
        first = new JButton(new ImageIcon(url));
        String nbBundle3 = mLoc.t("BUND336: Go to the first page");
        first.setToolTipText(nbBundle3.substring(15));
        first.addActionListener(outputListener);
        first.setEnabled(false);
        toolbar.add(first);
        toolbar.addSeparator(new Dimension(10, 10));

        url = getClass().getResource("/org/netbeans/modules/sql/framework/ui/resources/images/navigate_left.png");
        previous = new JButton(new ImageIcon(url));
        String nbBundle4 = mLoc.t("BUND337: Go to the previous page");
        previous.setToolTipText(nbBundle4.substring(15));
        previous.addActionListener(outputListener);
        previous.setEnabled(false);
        toolbar.add(previous);
        toolbar.addSeparator(new Dimension(10, 10));

        url = getClass().getResource("/org/netbeans/modules/sql/framework/ui/resources/images/navigate_right.png");
        next = new JButton(new ImageIcon(url));
        String nbBundle5 = mLoc.t("BUND338: Go to the next page");
        next.setToolTipText(nbBundle5.substring(15));
        next.addActionListener(outputListener);
        next.setEnabled(false);
        toolbar.add(next);
        toolbar.addSeparator(new Dimension(10, 10));

        url = getClass().getResource("/org/netbeans/modules/sql/framework/ui/resources/images/navigate_end.png");
        last = new JButton(new ImageIcon(url));
        String nbBundle6 = mLoc.t("BUND339: Go to the last page");
        last.setToolTipText(nbBundle6.substring(15));
        last.addActionListener(outputListener);
        last.setEnabled(false);
        toolbar.add(last);
        toolbar.addSeparator(new Dimension(10, 10));
        //add limit row label
        String nbBundle7 = mLoc.t("BUND340: Page size:");
        limitRow = new JLabel(nbBundle7.substring(15));
        limitRow.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 8));
        toolbar.add(limitRow);

        //add refresh text field
        refreshField = new JTextField();
        refreshField.setText("" + recordToRefresh);
        refreshField.setPreferredSize(new Dimension(30, refreshField.getHeight()));
        refreshField.setSize(30, refreshField.getHeight());
        refreshField.addKeyListener(new KeyAdapter() {

            @Override
            public void keyTyped(KeyEvent evt) {
                if (refreshField.getText().length() >= 3) {
                    evt.consume();
                }
            }
        });
        refreshField.addActionListener(outputListener);
        toolbar.add(refreshField);

        String nbBundle8 = mLoc.t("BUND341: Total Rows:");
        JLabel totalRowsNameLabel = new JLabel(nbBundle8.substring(15));
        totalRowsNameLabel.getAccessibleContext().setAccessibleName(nbBundle8.substring(15));
        totalRowsNameLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 8));
        toolbar.add(totalRowsNameLabel);
        totalRowsLabel = new JLabel();
        toolbar.add(totalRowsLabel);
        toolbar.addSeparator(new Dimension(10, 10));

        url = getClass().getResource("/org/netbeans/modules/sql/framework/ui/resources/images/row_add.png");
        insert = new JButton(new ImageIcon(url));
        String nbBundle9 = mLoc.t("BUND342: Insert a record.");
        insert.setToolTipText(nbBundle9.substring(15));
        insert.addActionListener(outputListener);
        insert.setEnabled(false);
        btn[2] = insert;

        url = getClass().getResource("/org/netbeans/modules/sql/framework/ui/resources/images/row_delete.png");
        deleteRow = new JButton(new ImageIcon(url));
        String nbBundle10 = mLoc.t("BUND343: Delete Selected Records.");
        deleteRow.setToolTipText(nbBundle10.substring(15));
        deleteRow.addActionListener(outputListener);
        deleteRow.setEnabled(false);
        btn[3] = deleteRow;

        url = getClass().getResource("/org/netbeans/modules/sql/framework/ui/resources/images/row_preferences.png");
        commit = new JButton(new ImageIcon(url));
        String nbBundle11 = mLoc.t("BUND344: Commit the Changes done on this page.");
        commit.setToolTipText(nbBundle11.substring(15));
        commit.addActionListener(outputListener);
        commit.setEnabled(false);
        btn[4] = commit;

        //add panel
        this.add(panel, BorderLayout.NORTH);

        //add query view
        queryView = new ResultSetTablePanel(this);
        this.add(queryView, BorderLayout.CENTER);
    }
    
    public SQLObject getTable(){
        return table;
    }

    public abstract void generateResult();

    public abstract void generateResult(SQLObject aTable);

    public JButton[] getVerticalToolBar() {
        return btn;
    }

    private void executeUpdate(String key) throws NumberFormatException, SQLException, BaseException, DBSQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String errorMsg = "";

        int row = Integer.parseInt(key.substring(0, key.indexOf(";")));
        int col = Integer.parseInt(key.substring(key.indexOf(";") + 1, key.length()));
        String updateStmt = queryView.getUpdateStmt(key);
        List values = queryView.getValueList(key);
        List<Integer> types = queryView.getTypeList(key);

        int rowCount = 0;
        try {
            conn = meta.createConnection();
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement(updateStmt);
            int pos = 1;
            for (Object val : values) {
                SQLUtils.setAttributeValue(pstmt, pos, types.get(pos - 1), val);
                pos++;
            }
            rowCount = pstmt.executeUpdate();
        } catch (SQLException ex) {
            errorMsg = errorMsg + "Update failed at Row:" + row + "Column:" + col + ";";
            DialogDisplayer.getDefault().notify(new Message(errorMsg, NotifyDescriptor.ERROR_MESSAGE));
        } finally {
            if (rowCount == 0) {
                errorMsg = "No rows updates using " + updateStmt + ";";
                DialogDisplayer.getDefault().notify(new Message(errorMsg, NotifyDescriptor.INFORMATION_MESSAGE));
            } else if (rowCount > 1) {
                errorMsg = "A Distinct row cannot be updated using " + updateStmt + ";";
                DialogDisplayer.getDefault().notify(new Message(errorMsg, NotifyDescriptor.ERROR_MESSAGE));
                conn.rollback();
            } else {
                conn.commit();
            }
            queryView.closeResources(pstmt, conn);
        }
    }

    protected class showDataFilter_ActionPerformed extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            if (src.equals(filterButton)) {
                try {
                    etlView = DataObjectProvider.getProvider().getActiveDataObject().getETLEditorTopPanel();
                    SQLBasicTableArea stArea = (SQLBasicTableArea) etlView.getGraphView().findGraphNode(table);
                    SQLObject sqlObject = (SQLObject) stArea.getDataObject();
                    Object[] args = new Object[]{stArea, sqlObject};
                    IGraphView gv = stArea.getGraphView();
                    gv.execute(ICommand.DATA_EXTRACTION, args);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    protected void refreshActionPerformed() {
        if (table instanceof SQLDBTable) {
            try {
                meta.refresh((SQLDBTable) table);
            } catch (DBSQLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        int intVal = totalCount;
        if (intVal <= 0) {
            String nbBundle12 = mLoc.t("BUND345: Enter a valid number to refresh records.{0}", table.getDisplayName());
            String errorMsg = nbBundle12.substring(15);
            DialogDisplayer.getDefault().notify(new Message(errorMsg, NotifyDescriptor.INFORMATION_MESSAGE));
            return;
        }
        recordToRefresh = intVal;

        // Delegate to concrete class implementation.
        generateResult(this.table);
    }

    protected void setMaxActionPerformed() {
        try {
            maxRows = Integer.parseInt(refreshField.getText().trim());
        } catch (NumberFormatException ex) {
            if (totalCount < 999) {
                maxRows = totalCount;
            } else {
                maxRows = 999;
            }
        }
        nowCount = 1;
        if (maxRows > totalCount) {
            maxRows = totalCount;
        }
        recordToRefresh = nowCount + maxRows - 1;
        if (recordToRefresh > totalCount) {
            recordToRefresh = totalCount;
        }
        // Delegate to concrete class implementation.
        generateResult(this.table);
    }

    protected void firstActionPerformed() {
        boolean doCalculation = true;
        if (commit.isEnabled()) {
            String msg = "You have uncommited Changes in this page. If you continue, you changes will be lost. Do you still want to continue?";
            NotifyDescriptor d = new NotifyDescriptor.Confirmation(msg, "Confirm navigation", NotifyDescriptor.YES_NO_OPTION);
            if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.NO_OPTION) {
                doCalculation = false;
            }
        }
        if (doCalculation) {
            nowCount = 1;
            recordToRefresh = nowCount + maxRows - 1;
            if (recordToRefresh > totalCount) {
                recordToRefresh = totalCount;
            }
            commit.setEnabled(false);
            this.queryView.setDirtyStatus(false);
            // Delegate to concrete class implementation.
            generateResult(this.table);
        }
    }

    protected void previousActionPerformed() {
        boolean doCalculation = true;
        if (commit.isEnabled()) {
            String msg = "You have uncommited Changes in this page. If you continue, you changes will be lost. Do you still want to continue?";
            NotifyDescriptor d = new NotifyDescriptor.Confirmation(msg, "Confirm navigation", NotifyDescriptor.YES_NO_OPTION);
            if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.NO_OPTION) {
                doCalculation = false;
            }
        }
        if (doCalculation) {
            nowCount -= maxRows;
            recordToRefresh = nowCount + maxRows - 1;
            commit.setEnabled(false);
            this.queryView.setDirtyStatus(false);
            // Delegate to concrete class implementation.
            generateResult(this.table);
        }
    }

    protected void nextActionPerformed() {
        boolean doCalculation = true;
        if (commit.isEnabled()) {
            String msg = "You have uncommited Changes in this page. If you continue, your changes will be lost. Do you still want to continue?";
            NotifyDescriptor d = new NotifyDescriptor.Confirmation(msg, "Confirm navigation", NotifyDescriptor.YES_NO_OPTION);
            if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.NO_OPTION) {
                doCalculation = false;
            }
        }
        if (doCalculation) {
            nowCount += maxRows;
            recordToRefresh = nowCount + maxRows - 1;
            // Delegate to concrete class implementation.
            commit.setEnabled(false);
            this.queryView.setDirtyStatus(false);
            generateResult(this.table);
        }
    }

    protected void lastActionPerformed() {
        boolean doCalculation = true;
        if (commit.isEnabled()) {
            String msg = "You have uncommited Changes in this page. If you continue, your changes will be lost. Do you still want to continue?";
            NotifyDescriptor d = new NotifyDescriptor.Confirmation(msg, "Confirm navigation", NotifyDescriptor.YES_NO_OPTION);
            if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.NO_OPTION) {
                doCalculation = false;
            }
        }
        if (doCalculation) {
            try {
                nowCount = totalCount - (totalCount % maxRows) + 1;
            } finally {
            }
            recordToRefresh = totalCount;
            commit.setEnabled(false);
            this.queryView.setDirtyStatus(false);
            // Delegate to concrete class implementation.
            generateResult(this.table);
        }
    }

    protected void commitActionPerformed() {
        if (this.queryView.isDirty()) {
            try {
                for (String key : queryView.getUpdateKeys()) {
                    executeUpdate(key);
                }
            } catch (Exception ex) {
                String errorMsg = "Check the data field type, precision and other constraints." + ex.getMessage();
                DialogDisplayer.getDefault().notify(new Message(errorMsg, NotifyDescriptor.ERROR_MESSAGE));
            } finally {
                commit.setEnabled(false);
                this.queryView.setDirtyStatus(false);
            }
        }
    }

    protected void insertActionPerformed() {
        String[] data = null;
        StringBuilder insertSql = new StringBuilder();

        insertSql.append("Insert into " + meta.getQualifiedTableName() + "Values(");
        data = getDialogData();
        if (data != null) {
            for (int i = 0; i < data.length; i++) {
                if (i != 0) {
                    insertSql.append(",");
                }
                insertSql.append("?");
            }
            insertSql.append(")");
        }


        PreparedStatement pstmt = null;
        Connection conn = null;
        boolean error = false;
        String errorMsg = null;

        try {
            conn = meta.createConnection();
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement(insertSql.toString());
            int pos = 1;
            for (Object val : data) {
                SQLUtils.setAttributeValue(pstmt, pos, meta.getColumnType(pos - 1), val);
                pos++;
            }
            int rows = pstmt.executeUpdate();
            if (rows != 1) {
                error = true;
                errorMsg = errorMsg + "Failed to insert record. Check for datatype mismatch and other key constraints.";
            }

        //return rows;
        } catch (Exception ex) {
            error = true;
            errorMsg = errorMsg + ex.getMessage();
        } finally {
            if (!error) {
                try {
                    String msg = "Commit the INSERT Operation to the database?";
                    NotifyDescriptor d = new NotifyDescriptor.Confirmation(msg, "Confirm delete", NotifyDescriptor.YES_NO_OPTION);
                    if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.YES_OPTION) {
                        conn.commit();
                    } else {
                        msg = "Discarded the Insert operation.";
                        DialogDisplayer.getDefault().notify(new Message(msg, NotifyDescriptor.INFORMATION_MESSAGE));
                        conn.rollback();
                    }
                } catch (SQLException ex) {
                    errorMsg = "Failure while commiting changes to database.";
                    DialogDisplayer.getDefault().notify(new Message(errorMsg, NotifyDescriptor.INFORMATION_MESSAGE));
                }

                if (!error) {
                    errorMsg = "Record successfully inserted.";
                    DialogDisplayer.getDefault().notify(new Message(errorMsg, NotifyDescriptor.INFORMATION_MESSAGE));
                }

            } else {
                errorMsg = "Insert command failed for " + errorMsg;
                DialogDisplayer.getDefault().notify(new Message(errorMsg, NotifyDescriptor.INFORMATION_MESSAGE));
            }
            queryView.closeResources(pstmt, conn);
            if(totalCount <= 0) {
                totalCount = 1;
            }
            refreshActionPerformed();
        }


    }

    protected void deleteRecordActionPerformed() {
        if (queryView.table.getSelectedRowCount() == 0) {
            String msg = "Please select a row to delete.";
            DialogDisplayer.getDefault().notify(new Message(msg, NotifyDescriptor.INFORMATION_MESSAGE));
        } else {
            try {
                int i = queryView.table.getSelectedRow();
                    queryView.executeDeleteRow(meta, i);
                refreshActionPerformed();
            } catch (Exception ex) {
                String msg = "Error Deleting Row(s): " + ex.getMessage();
                DialogDisplayer.getDefault().notify(new Message(msg, NotifyDescriptor.ERROR_MESSAGE));
            }
        }
    }

    protected String getJoinSql(SQLJoinOperator op, boolean useSourceTableAlias) {
        String sql = "";
        try {
            DB db = DBFactory.getInstance().getDatabase(DBConstants.ANSI92);
            StatementContext context = new StatementContext();
            context.setUseSourceTableAliasName(useSourceTableAlias);
            if (!useSourceTableAlias) {
                context.setUsingFullyQualifiedTablePrefix(false);
                context.putClientProperty(StatementContext.USE_FULLY_QUALIFIED_TABLE, Boolean.FALSE);
            }
            sql = sql + db.getGeneratorFactory().generate(op, context);
        } catch (BaseException ex) {
            //ignore
        }
        return sql;
    }

    private String[] getDialogData() {
        List<JTextField> lst = new ArrayList<JTextField>();
        String[] data = null;
        JPanel panel = null;
        data = new String[meta.getColumnCount()];

        //Add fields
        panel = new JPanel();
        panel.setBorder(BorderFactory.createEtchedBorder());
        GridBagLayout gl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        panel.setLayout(gl);

        String nbBundle52 = mLoc.t("BUND346: Field Name");
        JLabel label = new JLabel(nbBundle52.substring(15));
        label.getAccessibleContext().setAccessibleName(nbBundle52.substring(15));
        label.setForeground(Color.RED);
        c.weightx = 0.35;
        c.gridwidth = GridBagConstraints.RELATIVE;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(label, c);

        String nbBundle53 = mLoc.t("BUND347: Field Value");
        label = new JLabel(nbBundle53.substring(15));
        label.getAccessibleContext().setAccessibleName(nbBundle53.substring(15));
        label.setForeground(Color.RED);
        c.weightx = 0.65;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(label, c);

        for (int i = 0; i < meta.getColumnCount(); i++) {
            label = new JLabel(meta.getColumnName(i));
            JTextField txt = new JTextField(meta.getColumn(i).getScale());
            txt.setToolTipText("Field Type: " + meta.getColumn(i).getJdbcTypeString());
            txt.setName(meta.getColumnName(i));
            label.setLabelFor(txt);
            c.weightx = 0.35;
            c.gridwidth = GridBagConstraints.RELATIVE;
            c.fill = GridBagConstraints.HORIZONTAL;
            panel.add(label, c);
            c.weightx = 0.65;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridwidth = GridBagConstraints.REMAINDER;
            panel.add(txt, c);
            lst.add(txt);
        }

        DialogDescriptor desc = new DialogDescriptor(panel, "Enter the Values");
        Dialog dialog = DialogDisplayer.getDefault().createDialog(desc);
        dialog.getAccessibleContext().setAccessibleDescription("This is the dialog which helps user input records into database");
        dialog.setModal(true);
        dialog.pack();
        dialog.setVisible(true);

        if (desc.getValue() == NotifyDescriptor.OK_OPTION) {
            for (int i = 0; i < lst.size(); i++) {
                JTextField textField = lst.get(i);
                for (int j = 0; j < meta.getColumnCount(); j++) {
                    if (meta.getColumnName(j).equals(textField.getName())) {
                        data[j] = textField.getText();
                        break;
                    }
                }
            }
        } else {
            data = null;
        }
        return data;
    }

    protected void setTotalCount(ResultSet rs) {
        try {
            if (rs == null) {
                String nbBundle13 = mLoc.t("BUND051: N/A");
                totalRowsLabel.setText(nbBundle13.substring(15));
            } else {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    totalRowsLabel.setText(String.valueOf(count));
                    totalCount = count;
                }
            }
        } catch (SQLException ex) {
            mLogger.errorNoloc(mLoc.t("EDIT145: Could not get total row count{0}", DataOutputPanel.class.getName()), ex);
        }
    }

    protected RuntimeDatabaseModel getRuntimeDbModel() {
        return sqlDefinition.getRuntimeDbModel();
    }
}
