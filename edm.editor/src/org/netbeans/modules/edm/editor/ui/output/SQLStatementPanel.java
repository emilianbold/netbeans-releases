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
package org.netbeans.modules.edm.editor.ui.output;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.BadLocationException;

import org.netbeans.modules.edm.codegen.DB;
import org.netbeans.modules.edm.codegen.DBFactory;
import org.netbeans.modules.edm.codegen.StatementContext;
import org.netbeans.modules.edm.model.SQLConstants;
import org.netbeans.modules.edm.model.SQLObject;
import org.netbeans.modules.edm.model.SourceTable;
import org.netbeans.modules.edm.model.ValidationInfo;
import org.netbeans.modules.edm.model.visitors.SQLValidationVisitor;
import org.netbeans.modules.edm.model.visitors.SQLVisitedObject;
import org.netbeans.modules.edm.editor.utils.SwingWorker;
import org.netbeans.modules.edm.editor.graph.jgo.ICommand;
import org.netbeans.modules.edm.editor.utils.UIUtil;
import org.netbeans.modules.edm.editor.ui.view.conditionbuilder.ConditionBuilderUtil;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;

import org.netbeans.modules.edm.model.EDMException;
import org.netbeans.modules.edm.editor.utils.DBConstants;
import org.netbeans.modules.edm.editor.graph.components.BasicToolBar;
import org.netbeans.modules.edm.editor.ui.view.IGraphViewContainer;
import org.openide.util.NbBundle;

/**
 * This is a view to show sql code.
 *
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class SQLStatementPanel extends JPanel implements IMessageView {

    private static transient final Logger mLogger = Logger.getLogger(SQLStatementPanel.class.getName());
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
            String title = NbBundle.getMessage(SQLStatementPanel.class, "MSG_ShowSQL");
            String message = NbBundle.getMessage(SQLStatementPanel.class, "MSG_GeneratingSQL");
            UIUtil.startProgressDialog(title, message);
        }

        protected void stopProgressBar() {
            UIUtil.stopProgressDialog();
        }

        public boolean hasValidationErrors() throws EDMException {
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

                DB db = DBFactory.getInstance().getDatabase(DBConstants.AXION);
                db.setQuoteAlways(false); // quote only when required.
                StatementContext context = new StatementContext();

                if (sqlObjectLocalRef.getObjectType() == SQLConstants.SOURCE_TABLE) {
                    sql = NbBundle.getMessage(SQLStatementPanel.class, "LBL_source_select_sql",new Object[] {NL});
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
                sqlText = NbBundle.getMessage(SQLStatementPanel.class, "MSG_cant_evaluate_sql",new Object[] {sqlObj.getDisplayName()});
                mLogger.log(Level.INFO,NbBundle.getMessage(SQLStatementPanel.class, "MSG_cant_evaluate_sql",new Object[] {sqlObj.getDisplayName()}), ex);
                mLogger.log(Level.INFO,NbBundle.getMessage(SQLStatementPanel.class, "MSG_Cannot_get_contents_for_table",new Object[] {(sqlObj != null) ? sqlObj.getDisplayName() : ""}), ex);
            }
            return "";
        }

        // Runs on the event-dispatching thread.
        @Override
        public void finished() {
            SQLStatementPanel.this.textArea.setText(this.sqlText);
            stopProgressBar();
            if (this.ex != null) {
                String errorMsg = NbBundle.getMessage(SQLStatementPanel.class, "MSG_Error_fetching_data",new Object[] {sqlObj.getDisplayName(), this.ex.getMessage()});
                DialogDisplayer.getDefault().notify(new Message(errorMsg, NotifyDescriptor.ERROR_MESSAGE));
            }
        }
    }
    private static final String NL = System.getProperty("line.separator", "\n");
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
        this.setName(NbBundle.getMessage(SQLStatementPanel.class, "LBL_tab_sql",new Object[] {obj.getDisplayName()}));

        SQLViewActionListener aListener = new SQLViewActionListener();

        //add refresh button
        URL url = getClass().getResource("/org/netbeans/modules/edm/editor/resources/refresh.png");
        refreshButton = new JButton(new ImageIcon(url));
        refreshButton.setMnemonic('R');
        refreshButton.setToolTipText(NbBundle.getMessage(SQLStatementPanel.class, "TOOLTIP_Refresh_SQL"));
        refreshButton.addActionListener(aListener);
        BasicToolBar.processButton(refreshButton);
        btn[0] = refreshButton;

        textArea = new SQLEditorPanel();
        textArea.setBackground(Color.white);
        textArea.setBorder(BorderFactory.createEmptyBorder());
        textArea.setEditable(false);
        textArea.setFont(new Font("monospaced", Font.PLAIN, 12));

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
    public boolean hasValidationErrorsDisplayed(SQLObject object) throws EDMException {
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
                    throw new EDMException(errMsg);
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
        textArea.setText(str);
    }

    public void updateSQLObject(SQLObject obj) {
        this.sqlObj = obj;
        this.setName(NbBundle.getMessage(SQLStatementPanel.class, "LBL_tab_sql",new Object[] {obj.getDisplayName()}));
    }

    public JButton[] getVerticalToolBar() {
        return btn;
    }
}

