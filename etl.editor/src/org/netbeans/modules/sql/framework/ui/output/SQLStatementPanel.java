/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.sql.framework.ui.output;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.text.BadLocationException;

import org.netbeans.modules.sql.framework.common.jdbc.SQLDBConnectionDefinition;
import org.netbeans.modules.sql.framework.common.jdbc.SQLUtils;
import org.netbeans.modules.sql.framework.codegen.DB;
import org.netbeans.modules.sql.framework.codegen.DBFactory;
import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.ValidationInfo;
import org.netbeans.modules.sql.framework.model.utils.SQLObjectUtil;
import org.netbeans.modules.sql.framework.model.visitors.SQLValidationVisitor;
import org.netbeans.modules.sql.framework.model.visitors.SQLVisitedObject;
import org.netbeans.modules.sql.framework.ui.SwingWorker;
import org.netbeans.modules.sql.framework.ui.graph.ICommand;
import org.netbeans.modules.sql.framework.ui.utils.UIUtil;
import org.netbeans.modules.sql.framework.ui.view.conditionbuilder.ConditionBuilderUtil;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;

import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.jdbc.DBConstants;
import net.java.hulp.i18n.Logger;
import com.sun.sql.framework.utils.StringUtil;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.model.DBMetaDataFactory;
import org.netbeans.modules.sql.framework.model.DBTable;
import org.netbeans.modules.sql.framework.ui.view.IGraphViewContainer;

/**
 * This is a view to show sql code.
 *
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class SQLStatementPanel extends JPanel implements IMessageView, ETLOutputPanel {

    private static transient final Logger mLogger = Logger.getLogger(SQLStatementPanel.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    private JButton[] btn = new JButton[1];

    private class SQLViewActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            if (src.equals(refreshButton)) {
                sqlView.execute(ICommand.SHOW_SQL_CMD, new Object[]{sqlObj});
            }
        }
    }

    public class ShowSQLWorkerThread extends SwingWorker {

        protected Exception ex;
        protected SQLObject sqlObjectLocalRef;
        protected String sqlText = "";

        public ShowSQLWorkerThread() {
            sqlObjectLocalRef = sqlObj;
        }

        /** Must be executed in AWT Thread and before stopProgressBar() is called. **/
        protected void startProgressBar() {
            String nbBundle1 = mLoc.t("BUND365: Show SQL");
            String title = nbBundle1.substring(15);
            String nbBundle2 = mLoc.t("BUND366: Generating SQL, please wait...");
            String message = nbBundle2.substring(15);
            UIUtil.startProgressDialog(title, message);
        }

        protected void stopProgressBar() {
            UIUtil.stopProgressDialog();
        }

        public boolean hasValidationErrors() throws BaseException {
            return hasValidationErrorsDisplayed(sqlObj);
        }

        public Object construct() {
            try {
                startProgressBar();

                String sql = null;
                SQLStatementPanel.this.textArea.setText("");

                // Sql object is not valid we just want to show validation messages
                if (hasValidationErrors()) {
                    return "";
                }

                refreshDBType();

                DB db = DBFactory.getInstance().getDatabase(currentDbType);
                StatementContext context = new StatementContext();

                if (sqlObjectLocalRef.getObjectType() == SQLConstants.SOURCE_TABLE) {
                    String nbBundle3 = mLoc.t("BUND367: -- Select statement for Source Table {0}", NL);
                    sql = nbBundle3.substring(15);
                    sql += db.getStatements().getSelectStatement((SourceTable) sqlObj, context).getSQL();
                } else {
                    context.setUseSourceTableAliasName(true);
                    sql = db.getGeneratorFactory().generate(sqlObj, context);
                }

                if (sql != null) {
                    sqlText = sql;
                }

            } catch (Exception exp) {
                this.ex = exp;
                String nbBundle4 = mLoc.t("BUND368: Cannot evaluate SQL:{0}", sqlObj.getDisplayName());
                sqlText = nbBundle4.substring(15);
                mLogger.errorNoloc(mLoc.t("EDIT171: Cannot evaluate SQL for{0}", sqlObj.getDisplayName()), ex);
                mLogger.errorNoloc(mLoc.t("EDIT177: Cannot get contents for table{0}", (sqlObj != null) ? sqlObj.getDisplayName() : ""), ex);

            }
            return "";
        }

        // Runs on the event-dispatching thread.
        @Override
        public void finished() {
            SQLStatementPanel.this.textArea.setText(this.sqlText);
            stopProgressBar();
            if (this.ex != null) {
                String nbBundle5 = mLoc.t("BUND369: Error fetching data for table {0}.Cause: {1}", sqlObj.getDisplayName(), this.ex.getMessage());
                String errorMsg = nbBundle5.substring(15);
                DialogDisplayer.getDefault().notify(new Message(errorMsg, NotifyDescriptor.ERROR_MESSAGE));
            }
        }
    }
    private static final String NL = System.getProperty("line.separator", "\n");
    /* Indicates initial DB type name as indicated by the SQLObject or its parent. */
    private String actualDbTypeName;
    /* Indicates current DB type; determines "flavor" of generated SQL */
    private int currentDbType;
    private JTextField dbTypesBox;
    private JButton refreshButton;
    /* SQLObject whose applicable SQL content will be generated */
    private SQLObject sqlObj;
    private IGraphViewContainer sqlView;
    /* Text area to contain SQL content */
    private SQLEditorPanel textArea;

    /**
     * Creates a new instance of SQLSqlView associated with the given SQLObject.
     *
     * @param sqlView TopComponent which will host this view
     * @param obj SQLObject whose SQL content will be generated
     */
    public SQLStatementPanel(IGraphViewContainer sqlView, SQLObject obj) {
        this.sqlObj = obj;
        this.sqlView = sqlView;

        //do not show tab view if there is only one tab
        putClientProperty("TabPolicy", "HideWhenAlone"); //NOI18N
        putClientProperty("PersistenceType", "Never"); //NOI18N
        this.setLayout(new BorderLayout());
        String nbBundle6 = mLoc.t("BUND370: SQL: {0}", obj.getDisplayName());
        this.setName(nbBundle6.substring(15));


        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEmptyBorder());

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        panel.add(toolbar);

        String nbBundle7 = mLoc.t("BUND371: Database Type:");
        JLabel dbTypeLabel = new JLabel(nbBundle7.substring(15));
        dbTypeLabel.getAccessibleContext().setAccessibleName(nbBundle7.substring(15));
        dbTypeLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 8));
        toolbar.add(dbTypeLabel);

        String dbName = getDBType(obj);
        actualDbTypeName = dbName;
        SQLDefinition sqlDefinition = SQLObjectUtil.getAncestralSQLDefinition(sqlObj);
        if (sqlDefinition != null && sqlDefinition.requiresPipelineProcess()) {
            dbName = DBConstants.AXION_STR;
        }

        currentDbType = SQLUtils.getSupportedDBType(dbName);
        dbTypesBox = new JTextField(dbName);
        toolbar.add(dbTypesBox);
        dbTypesBox.setEnabled(false);

        SQLViewActionListener aListener = new SQLViewActionListener();

        //add refresh button
        URL url = getClass().getResource("/org/netbeans/modules/sql/framework/ui/resources/images/refresh.png");
        refreshButton = new JButton(new ImageIcon(url));
        refreshButton.setMnemonic('R');
        refreshButton.setToolTipText("Refresh SQL");
        refreshButton.addActionListener(aListener);
        btn[0] = refreshButton;
        this.add(panel, BorderLayout.NORTH);

        textArea = new SQLEditorPanel();
        textArea.setBackground(Color.white);
        textArea.setBorder(BorderFactory.createEmptyBorder());
        textArea.setEditable(false);
        textArea.setFont(new Font("monospaced", Font.PLAIN, 12));

        List<DBTable> tableList = new ArrayList<DBTable>();
        tableList.addAll(sqlDefinition.getSourceTables());
        tableList.addAll(sqlDefinition.getTargetTables());
        textArea.setTables(tableList);

        JScrollPane sPane = new JScrollPane(textArea);
        this.add(sPane, BorderLayout.CENTER);
    }

    /**
     * Appends given String to the conetnt view.
     *
     * @param str String to append
     */
    public synchronized void appendToView(String str) {
        try {
            textArea.getDocument().insertString(textArea.getDocument().getLength(), str, null);
        } catch (BadLocationException e) {
            //ignore
        }
    }

    public void clearView() {
        textArea.setText(" ");
    }

    /**
     * Writes validation errors, if any, associated with the given SQLObject.
     *
     * @param sqlObj2
     * @return
     */
    public boolean hasValidationErrorsDisplayed(SQLObject object) throws BaseException {
        boolean validationErrors = false;
        String errMsg = "";

        if (object instanceof SQLVisitedObject) {
            SQLVisitedObject vObject = (SQLVisitedObject) object;
            SQLValidationVisitor vVisitor = new SQLValidationVisitor();
            vObject.visit(vVisitor);
            List invalidObjectList = vVisitor.getValidationInfoList();
            // Ignore validation errors related to custom operator as they can be deferred
            // to test colloboration. validation at this stage has no meaning as
            // the reference for validation is only the host database
            invalidObjectList = ConditionBuilderUtil.filterValidations(invalidObjectList);
            if (invalidObjectList.size() > 0) {
                Iterator iter = invalidObjectList.iterator();
                while (iter.hasNext()) {
                    ValidationInfo invalidObj = (ValidationInfo) iter.next();
                    if (invalidObj.getValidationType() == ValidationInfo.VALIDATION_ERROR) {
                        errMsg += invalidObj.getDescription().trim() + NL;
                        validationErrors = true;
                    }
                }
                if (validationErrors) {
                    throw new BaseException(errMsg);
                }
            }
        }

        return validationErrors;
    }

    /**
     * Regenerates SQL content based on current settings and state of the associated
     * SQLObject.
     */
    public synchronized void refreshSql() {
        ShowSQLWorkerThread showSqlThread = new ShowSQLWorkerThread();
        showSqlThread.start();
    }

    /**
     * refresh view with this new string
     *
     * @param str String to refresh with
     */
    public void refreshView(String str) {
        refreshDBType();
        textArea.setText(str);
    }

    public void updateSQLObject(SQLObject obj) {
        this.sqlObj = obj;
        String nbBundle8 = mLoc.t("BUND370: SQL: {0}", obj.getDisplayName());
        this.setName(nbBundle8.substring(15));
    }

    private String getDBType(SQLObject obj) {
        String dbType = DBConstants.ANSI92_STR;
        if (obj instanceof DBTable) {
            DBTable tbl = (DBTable) obj;

            // Connection definition should always be of ETL
            SQLDBConnectionDefinition connDef = (SQLDBConnectionDefinition) tbl.getParent().getConnectionDefinition();
            dbType = connDef.getDBType();
            if (StringUtil.isNullString(dbType)) {
                try {
                    dbType = DBMetaDataFactory.getDBTypeFromURL(connDef.getConnectionURL());
                } catch (Exception ex) {
                    //Ignore, assume JDBC/ANSI
                }
            }
        }
        return dbType;
    }

    private void refreshDBType() {
        //here we check if we need to force use of axion db
        SQLDefinition sqlDefinition = SQLObjectUtil.getAncestralSQLDefinition(sqlObj);
        if (sqlDefinition != null && sqlDefinition.requiresPipelineProcess()) {
            currentDbType = DBConstants.AXION;
            dbTypesBox.setText(DBConstants.AXION_STR);
        } else {
            currentDbType = SQLUtils.getSupportedDBType(actualDbTypeName);
            dbTypesBox.setText(actualDbTypeName);
        }
    }

    public JButton[] getVerticalToolBar() {
        return btn;
    }
}

