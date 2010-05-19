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
package org.netbeans.modules.edm.editor.graph.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import org.openide.util.Exceptions;
import org.netbeans.modules.edm.model.SQLDefinition;
import org.netbeans.modules.edm.editor.ui.output.SQLEditorPanel;
import org.netbeans.modules.edm.codegen.DB;
import org.netbeans.modules.edm.codegen.DBFactory;
import org.netbeans.modules.edm.codegen.StatementContext;
import org.netbeans.modules.edm.model.SQLGroupBy;
import org.netbeans.modules.edm.model.SQLJoinOperator;
import org.netbeans.modules.edm.model.SQLJoinView;
import org.netbeans.modules.edm.model.SQLObject;
import org.netbeans.modules.edm.model.SourceTable;
import org.netbeans.modules.edm.model.EDMException;
import org.netbeans.modules.edm.editor.utils.DBConstants;
import java.sql.Types;
import org.netbeans.modules.edm.model.DBColumn;
import org.netbeans.modules.edm.model.RuntimeInput;
import org.netbeans.modules.edm.editor.graph.components.BasicToolBar;
import org.openide.util.NbBundle;

/**
 *
 * @author jawed
 */
public class EDMSQLStatementPanel extends JPanel implements EDMIMessageView, EDMOutputPanel {

    private EDMOutputTopComponent edmOTC;
    private SQLObject sqlObj;
    private SQLEditorPanel textArea;
    private JButton refreshButton;
    private JButton[] btn = new JButton[1];

    public EDMSQLStatementPanel(SQLObject obj) {
        this.sqlObj = obj;
        edmOTC = EDMOutputTopComponent.findInstance();
        this.setLayout(new BorderLayout());
        setFocusable(true);
        setBackground(UIManager.getColor("text")); //NOI18N

        //do not show tab view if there is only one tab
        putClientProperty("TabPolicy", "HideWhenAlone"); //NOI18N
        putClientProperty("PersistenceType", "Never"); //NOI18N

        SQLViewActionListener aListener = new SQLViewActionListener();

        //add refresh button
        URL url = getClass().getResource("/org/netbeans/modules/edm/editor/resources/refresh.png");
        refreshButton = new JButton(new ImageIcon(url));
        refreshButton.setMnemonic('R');
        refreshButton.setToolTipText(NbBundle.getMessage(EDMSQLStatementPanel.class, "TOOLTIP_Refresh_SQL"));
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

    public void showSql(SQLObject obj, SQLDefinition sqlDef) {
        StringBuilder buf = null;
        String name = null;
        String sql = null;
        try {
            DB db = DBFactory.getInstance().getDatabase(DBConstants.AXION);
            db.setQuoteAlways(false); // quote only when required.
            StatementContext context = new StatementContext();
            context.setUsingOriginalSourceTableName(true);
            context.setUseSourceColumnAliasName(false);
            if (obj instanceof SourceTable) {
                buf = new StringBuilder(db.getStatements().
                        getSelectStatement((SourceTable) obj, context).getSQL());
                name = obj.getDisplayName();
            } else if (obj instanceof SQLJoinView) {
                SQLGroupBy grpby = ((SQLJoinView) obj).getSQLGroupBy();
                ((SQLJoinView) obj).setSQLGroupBy(null);
                buf = new StringBuilder(db.getStatements().getSelectStatement((SQLJoinView) obj, context).getSQL());
                ((SQLJoinView) obj).setSQLGroupBy(grpby);
                name = "JoinView";
            } else if (obj instanceof SQLJoinOperator) {
                buf = new StringBuilder(db.getStatements().
                        getSelectStatement((SQLJoinOperator) obj, context).getSQL());
                name = "JoinView";
            } else if (obj instanceof SQLGroupBy) {
                SQLObject parent = (SQLObject) ((SQLGroupBy) obj).getParentObject();
                if (parent instanceof SQLJoinView) {
                    buf = new StringBuilder(db.getStatements().
                            getSelectStatement((SQLJoinView) parent, context).getSQL());
                } else if (parent instanceof SourceTable) {
                    buf = new StringBuilder(db.getStatements().
                            getSelectStatement((SourceTable) parent, context).getSQL());
                }
            } else {
                buf = new StringBuilder(db.getGeneratorFactory().generate(obj, context));
            }
            sql = buf.toString();
            RuntimeInput runInput = sqlDef.getRuntimeDbModel().getRuntimeInput();
            if (runInput != null) {
                for (DBColumn col : runInput.getColumnList()) {
                    String varName = col.getName();
                    String defaultValue = col.getDefaultValue();
                    int jdbcType = col.getJdbcType();
                    if (jdbcType == Types.VARCHAR || jdbcType == Types.CHAR || jdbcType == Types.TIMESTAMP) {
                        defaultValue = "\'" + defaultValue + "\'";
                    }
                    sql = sql.replaceAll("\\$" + varName, defaultValue);
                }
            }
        } catch (EDMException ex) {
            Exceptions.printStackTrace(ex);
        }
        this.clearView();
        this.setName("SQL:" + name + "  ");
        this.appendToView(sql);
        edmOTC.addPanel(this, btn, this.getName());
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
     * Refreshes view with the given string.
     * 
     * @param newStr String to refresh with
     */
    public synchronized void refreshView(String newStr) {
        textArea.setText(newStr);
    }

    /**
     * Adjusts viewport to show data at bottom of screen.
     */
    private void adjustViewport() {
        // setCaretPosition() and scrollRectToVisible() are not thread-safe,
        // so make sure they are executed on the event dispatch thread.
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    adjustViewport();
                }
            });
        } else {
            textArea.setCaretPosition(textArea.getText().length());
            textArea.scrollRectToVisible(new Rectangle(0, Math.max(0, textArea.getHeight() - 2), 1, 1));
        }
    }

    private class SQLViewActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            if (src.equals(refreshButton)) {
                refreshView(getSQLQueryString(sqlObj));
            //sqlView.execute(ICommand.SHOW_SQL_CMD, new Object[]{sqlObj});
            }
        }
    }

    public JButton[] getVerticalToolBar() {
        return btn;
    }

    public String getSQLQueryString(SQLObject sqlObj) {
        StringBuilder buf = null;
        try {
            DB db = DBFactory.getInstance().getDatabase(DBConstants.AXION);
            db.setQuoteAlways(false); // quote only when required.
            StatementContext context = new StatementContext();
            context.setUsingOriginalSourceTableName(true);
            context.setUseSourceColumnAliasName(false);
            if (sqlObj instanceof SourceTable) {
                buf = new StringBuilder(db.getStatements().
                        getSelectStatement((SourceTable) sqlObj, context).getSQL());
            } else if (sqlObj instanceof SQLJoinView) {
                SQLGroupBy grpby = ((SQLJoinView) sqlObj).getSQLGroupBy();
                ((SQLJoinView) sqlObj).setSQLGroupBy(null);
                buf = new StringBuilder(db.getStatements().getSelectStatement((SQLJoinView) sqlObj, context).getSQL());
                ((SQLJoinView) sqlObj).setSQLGroupBy(grpby);
            } else if (sqlObj instanceof SQLJoinOperator) {
                buf = new StringBuilder(db.getStatements().
                        getSelectStatement((SQLJoinOperator) sqlObj, context).getSQL());
            } else if (sqlObj instanceof SQLGroupBy) {
                SQLObject parent = (SQLObject) ((SQLGroupBy) sqlObj).getParentObject();
                if (parent instanceof SQLJoinView) {
                    buf = new StringBuilder(db.getStatements().
                            getSelectStatement((SQLJoinView) parent, context).getSQL());
                } else if (parent instanceof SourceTable) {
                    buf = new StringBuilder(db.getStatements().
                            getSelectStatement((SourceTable) parent, context).getSQL());
                }
            } else {
                buf = new StringBuilder(db.getGeneratorFactory().generate(sqlObj, context));
            }
        } catch (EDMException ex) {
            Exceptions.printStackTrace(ex);
        }
        return buf.toString().trim();
    }
}
