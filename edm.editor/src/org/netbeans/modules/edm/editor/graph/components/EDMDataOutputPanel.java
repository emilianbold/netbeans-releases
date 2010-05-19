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
import java.awt.Component;

import org.netbeans.modules.edm.model.EDMException;
import org.openide.util.Exceptions;

import org.netbeans.modules.edm.codegen.DB;
import org.netbeans.modules.edm.codegen.DBFactory;
import org.netbeans.modules.edm.codegen.StatementContext;
import org.netbeans.modules.edm.model.SQLConstants;
import org.netbeans.modules.edm.model.SQLJoinOperator;
import org.netbeans.modules.edm.model.SQLObject;
import org.netbeans.modules.edm.model.SourceTable;
import org.netbeans.modules.edm.editor.utils.DBConstants;
import org.netbeans.modules.edm.editor.utils.SQLPart;
import org.netbeans.modules.edm.editor.utils.StringUtil;
import java.awt.GridBagLayout;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.axiondb.ExternalConnectionProvider;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.dataview.api.DataView;
import org.netbeans.modules.edm.codegen.Statements;
import org.netbeans.modules.edm.codegen.AxionDB;
import org.netbeans.modules.edm.model.DBColumn;
import org.netbeans.modules.edm.model.RuntimeInput;
import org.netbeans.modules.edm.model.SQLDBModel;
import org.netbeans.modules.edm.model.SQLDBTable;
import org.netbeans.modules.edm.model.SQLDefinition;
import org.netbeans.modules.edm.model.SQLJoinView;
import org.netbeans.modules.edm.model.DBConnectionDefinition;
import org.netbeans.modules.edm.editor.ui.output.DBTableMetadata;
import org.netbeans.modules.edm.codegen.AxionPipelineStatements;
import org.netbeans.modules.dm.virtual.db.api.AxionExternalConnectionProvider;
import org.netbeans.modules.edm.editor.utils.SQLDBConnectionDefinition;
import org.netbeans.modules.edm.model.DBMetaDataFactory;
import org.netbeans.modules.edm.model.DBTable;
import org.netbeans.modules.edm.model.ValidationInfo;
import org.netbeans.modules.edm.model.visitors.SQLValidationVisitor;
import org.netbeans.modules.edm.editor.utils.DBExplorerUtil;
import org.netbeans.modules.edm.editor.utils.XmlUtil;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * Top component which displays something.
 */
public class EDMDataOutputPanel extends JPanel implements EDMOutputPanel {

    private String AXION_DRIVER = "org.axiondb.jdbc.AxionDriver";
    private String AXION_URL = "jdbc:axiondb:joinview";
    private JButton[] btns;
    protected SQLObject table;
    protected DBTableMetadata meta;
    private DataView dv;
    private int pageSize = 10;

    public EDMDataOutputPanel(SQLObject sqlObj) {
        this.table = sqlObj;

        if (sqlObj instanceof SQLDBTable) {
            this.meta = new DBTableMetadata(((SQLDBTable) this.table));
        }
        //do not show tab view if there is only one tab
        putClientProperty("TabPolicy", "HideWhenAlone"); //NOI18N
        putClientProperty("PersistenceType", "Never"); //NOI18N
        this.setLayout(new BorderLayout());

        setBorder(BorderFactory.createEmptyBorder());

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEtchedBorder());
        GridBagLayout gl = new GridBagLayout();
        panel.setLayout(gl);
    }

    public void generateOutput(SQLObject obj, SQLDefinition sqlDef, String prefix) {
        switch (obj.getObjectType()) {
            case SQLConstants.SOURCE_TABLE:
                showDataForTable(obj, sqlDef, prefix);
                break;
            case SQLConstants.JOIN_VIEW:
                showDataForJoinView(obj, sqlDef, prefix);
                break;
            case SQLConstants.JOIN:
                showDataForJoinOperator(obj, sqlDef, prefix);
            }
    }

    private void showDataForTable(SQLObject obj, SQLDefinition sqlDef, String prefix) {
        EDMOutputTopComponent topComp = EDMOutputTopComponent.findInstance();
        DatabaseConnection dbconn = null;
        try {
            StatementContext context = new StatementContext();
            SourceTable tbl = (SourceTable) obj;
            SQLValidationVisitor visitor = new SQLValidationVisitor();
            visitor.visit(tbl, true);
            List<ValidationInfo> vInfoList = visitor.getValidationInfoList();
            for (ValidationInfo vInfo: vInfoList) {
                if(vInfo.getValidationType() == ValidationInfo.VALIDATION_ERROR) {
                    throw new Exception(vInfo.getDescription() + "...");
                }
            }
            if (getDBType(obj).equalsIgnoreCase("AXION")) {
                dbconn = getDBConnection(obj);
            } else {
                Statement stmt = null;
                Connection conn = null;
                String dbLinkURL = AXION_URL + System.currentTimeMillis();
                System.setProperty(ExternalConnectionProvider.EXTERNAL_CONNECTION_PROVIDER_PROPERTY_NAME, AxionExternalConnectionProvider.class.getName());
                conn = DBExplorerUtil.createConnection(AXION_DRIVER, dbLinkURL, "sa", "sa");
                stmt = conn.createStatement();
                remoteTableForShowData(tbl, stmt, context);
                dbconn = DBExplorerUtil.createDatabaseConnection(AXION_DRIVER, dbLinkURL, "sa", "sa", false);
            }
            DB db = DBFactory.getInstance().getDatabase(DBConstants.AXION);
            Statements stmts = db.getStatements();
            SQLPart sqlPart = stmts.getSelectStatement(tbl, context);
            String sql = sqlPart.getSQL();
            sql = parseSQLForRuntimeInput(sqlDef, sql);
            dv = DataView.create(dbconn, sql.trim(), pageSize, true);
            List<Component> compList = dv.createComponents();
            dv.setEditable(false);
            if(compList.isEmpty()){
                throw new Exception("Unable to create ResultSet...");
            }
            
            Component comp = dv.createComponents().get(0);
            btns = dv.getEditButtons();
            if (!topComp.isOpened()) {
                topComp.open();
            }
            this.add(comp);
            this.setName("Data:" + obj.getDisplayName() + "  ");
            String tooltip = "<html><table border=0 cellspacing=0 cellpadding=0><tr><td>" + prefix + ":  " +
                    XmlUtil.escapeHTML(sql).replaceAll("\\n", "<br>").replaceAll(" ", "&nbsp;") + "</td></tr></table></html>";
            this.setToolTipText(tooltip);
            topComp.addPanel(this, btns, tooltip);
        } catch (Exception ex) {
            notifyDescriptor(ex);
        } finally {
            System.setProperty(ExternalConnectionProvider.EXTERNAL_CONNECTION_PROVIDER_PROPERTY_NAME, "");
        }
    }

    private void showDataForJoinOperator(SQLObject obj, SQLDefinition sqlDef, String prefix) {
        EDMOutputTopComponent topComp = EDMOutputTopComponent.findInstance();
        Statement stmt = null;
        Connection conn = null;
        String sql = null;
        String joinViewUrl = AXION_URL + System.currentTimeMillis();

        try {
            SQLJoinOperator joinOperator = (SQLJoinOperator) obj;
            SQLValidationVisitor visitor = new SQLValidationVisitor();
            visitor.visit(joinOperator, true);
            List<ValidationInfo> vInfoList = visitor.getValidationInfoList();
            for (ValidationInfo vInfo: vInfoList) {
                if(vInfo.getValidationType() == ValidationInfo.VALIDATION_ERROR) {
                    throw new Exception(vInfo.getDescription() + "...");
                }
            }
            
            Iterator it = joinOperator.getAllSourceTables().iterator();
            String joinSql = "";
            StringBuilder buf = null;
            System.setProperty(ExternalConnectionProvider.EXTERNAL_CONNECTION_PROVIDER_PROPERTY_NAME, AxionExternalConnectionProvider.class.getName());
            conn = DBExplorerUtil.createConnection(AXION_DRIVER, joinViewUrl, "sa", "sa");
            stmt = conn.createStatement();
            StatementContext joinContext = new StatementContext();
            while (it.hasNext()) {
                SourceTable tbl = (SourceTable) it.next();
                StatementContext context = new StatementContext();
                remoteTableForShowData(tbl, stmt, context);

                // add table properties to statementcontext.
                joinContext.setUsingFullyQualifiedTablePrefix(false);
                joinContext.setUsingUniqueTableName(tbl, true);
            }
            DB db = DBFactory.getInstance().getDatabase(DBConstants.AXION);

            joinContext.setUsingUniqueTableName(true);
            buf = new StringBuilder(db.getStatements().getSelectStatement(joinOperator, joinContext).getSQL());
            joinSql = joinSql + db.getGeneratorFactory().generate(joinOperator, joinContext);
            sql = buf.toString();
            sql = parseSQLForRuntimeInput(sqlDef, sql);
            DatabaseConnection dbconn = null;
            dbconn = DBExplorerUtil.createDatabaseConnection(AXION_DRIVER, joinViewUrl, "sa", "sa", false);
            dv = DataView.create(dbconn, sql, pageSize, true);
            List<Component> compList = dv.createComponents();
            if(compList.isEmpty()){
                throw new Exception("Unable to create ResultSet...");
            }
            
            Component comp = dv.createComponents().get(0);
            btns = dv.getEditButtons();
            if (!topComp.isOpened()) {
                topComp.open();
            }
            this.add(comp);
            this.setName("Data:JoinView   ");
            String tooltip = "<html><table border=0 cellspacing=0 cellpadding=0><tr><td>" + prefix + ":  " +
                    XmlUtil.escapeHTML(sql).replaceAll("\\n", "<br>").replaceAll(" ", "&nbsp;") + "</td></tr></table></html>";
            this.setToolTipText(tooltip);
            topComp.addPanel(this, btns, tooltip);
        } catch (Exception ex) {
            notifyDescriptor(ex);
        } finally {
            System.setProperty(ExternalConnectionProvider.EXTERNAL_CONNECTION_PROVIDER_PROPERTY_NAME, "");
        }
    }

    private void showDataForJoinView(SQLObject obj, SQLDefinition sqlDef, String prefix) {
        EDMOutputTopComponent topComp = EDMOutputTopComponent.findInstance();
        Statement stmt = null;
        Connection conn = null;
        String sql = null;
        String joinViewUrl = AXION_URL + System.currentTimeMillis();

        try {
            SQLJoinView joinView = (SQLJoinView) obj;
            Iterator it = joinView.getSourceTables().iterator();
            StringBuilder buf = null;
            System.setProperty(ExternalConnectionProvider.EXTERNAL_CONNECTION_PROVIDER_PROPERTY_NAME, AxionExternalConnectionProvider.class.getName());
            conn = DBExplorerUtil.createConnection(AXION_DRIVER, joinViewUrl, "sa", "sa");
            stmt = conn.createStatement();
            it = joinView.getSourceTables().iterator();
            StatementContext joinContext = new StatementContext();
            while (it.hasNext()) {
                SourceTable tbl = (SourceTable) it.next();
                StatementContext context = new StatementContext();
                remoteTableForShowData(tbl, stmt, context);

                // add table properties to statementcontext.
                joinContext.setUsingFullyQualifiedTablePrefix(false);
                joinContext.setUsingUniqueTableName(tbl, true);
            }
            DB db = DBFactory.getInstance().getDatabase(DBConstants.AXION);
            joinContext.setUsingUniqueTableName(true);
            buf = new StringBuilder(db.getStatements().getSelectStatement(joinView, joinContext).getSQL());
            sql = buf.toString();
            sql = parseSQLForRuntimeInput(sqlDef, sql);
            DatabaseConnection dbconn = null;
            dbconn = DBExplorerUtil.createDatabaseConnection(AXION_DRIVER, joinViewUrl, "sa", "sa", false);
            dv = DataView.create(dbconn, sql, pageSize, true);
            List<Component> compList = dv.createComponents();
            if(compList.isEmpty()){
                throw new Exception("Unable to create ResultSet...");
            }
            
            Component comp = compList.get(0);
            btns = dv.getEditButtons();
            if (!topComp.isOpened()) {
                topComp.open();
            }
            this.add(comp);
            this.setName("Data:JoinView   ");
            String tooltip = "<html><table border=0 cellspacing=0 cellpadding=0><tr><td>" + prefix + ":  " +
                    XmlUtil.escapeHTML(sql).replaceAll("\\n", "<br>").replaceAll(" ", "&nbsp;") + "</td></tr></table></html>";
            this.setToolTipText(tooltip);
            topComp.addPanel(this, btns, tooltip);
        } catch (Exception ex) {
            notifyDescriptor(ex);
        } finally {
            System.setProperty(ExternalConnectionProvider.EXTERNAL_CONNECTION_PROVIDER_PROPERTY_NAME, "");
        }
    }

    private void remoteTableForShowData(SQLObject obj, Statement stmt, StatementContext context) throws EDMException, SQLException {
        SourceTable tbl = (SourceTable) obj;
        AxionDB db = (AxionDB) DBFactory.getInstance().getDatabase(DBConstants.AXION);
        DBConnectionDefinition connDefn = tbl.getParent().getConnectionDefinition();
        AxionPipelineStatements stmts = db.getAxionPipelineStatements();
        String linkName = StringUtil.createSQLIdentifier(connDefn.getName());
        String dbLinkSql = stmts.getCreateDBLinkStatement(connDefn, linkName).getSQL();
        String dropDBLinkSql = stmts.getDropDBLinkStatement(linkName).getSQL();
        try {
            stmt.execute(dropDBLinkSql);
        } catch (SQLException e) {
        }
        stmt.execute(dbLinkSql);
        context.setUsingUniqueTableName(tbl, true);
        context.setUsingFullyQualifiedTablePrefix(false);
        String localName = db.getUnescapedName(db.getGeneratorFactory().generate(tbl, context));
        String remoteTableSql = stmts.getCreateRemoteTableStatement(tbl, localName, linkName).getSQL();
        try {
            stmt.execute(remoteTableSql);
        } catch (SQLException e) {
            Exceptions.printStackTrace(e);
        }
    }

    private String parseSQLForRuntimeInput(SQLDefinition sqlDef, String sql) {
        String mSql = sql;
        RuntimeInput runInput = sqlDef.getRuntimeDbModel().getRuntimeInput();
        if (runInput != null) {
            for (DBColumn col : runInput.getColumnList()) {
                String varName = col.getName();
                String defaultValue = col.getDefaultValue();
                int jdbcType = col.getJdbcType();
                if (jdbcType == Types.VARCHAR || jdbcType == Types.CHAR || jdbcType == Types.TIMESTAMP) {
                    defaultValue = "\'" + defaultValue + "\'";
                }
                mSql = mSql.replaceAll("\\$" + varName, defaultValue);
            }
        }
        return mSql;
    }
    
    private DatabaseConnection getDBConnection(SQLObject sqlObj) {
        DatabaseConnection dbConn = null;
        try {
            SQLDBModel sqlDBM = (SQLDBModel) ((SourceTable) sqlObj).getParentObject();
            DBConnectionDefinition dbCondef = sqlDBM.getConnectionDefinition();
            dbConn = DBExplorerUtil.createDatabaseConnection(dbCondef.getDriverClass(), dbCondef.getConnectionURL(),
                    dbCondef.getUserName(), dbCondef.getPassword());
        } catch (EDMException ex) {
            Exceptions.printStackTrace(ex);
        }
        return dbConn;
    }

    private String getDBType(SQLObject obj) {
        String dbType = DBConstants.AXION_STR;
        if (obj instanceof DBTable) {
            DBTable tbl = (DBTable) obj;
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

    private void notifyDescriptor(Exception ex) {
        NotifyDescriptor nd = new NotifyDescriptor.Message(ex.getMessage());
        DialogDisplayer.getDefault().notify(nd);
    }
    
    public JButton[] getVerticalToolBar() {
        if (dv != null) {
            return dv.getEditButtons();
        } else {
            return new JButton[0];
        }
    }
}
