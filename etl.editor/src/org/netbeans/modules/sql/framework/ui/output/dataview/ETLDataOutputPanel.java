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
package org.netbeans.modules.sql.framework.ui.output.dataview;

import org.netbeans.modules.sql.framework.ui.output.*;
import com.sun.etl.exception.BaseException;
import com.sun.etl.jdbc.DBConstants;
import com.sun.etl.jdbc.SQLPart;
import com.sun.etl.utils.StringUtil;
import java.awt.BorderLayout;
import java.awt.Component;
import org.openide.util.Exceptions;

import org.netbeans.modules.sql.framework.codegen.DB;
import org.netbeans.modules.sql.framework.codegen.DBFactory;
import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLJoinOperator;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SourceTable;
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
import org.axiondb.AxionException;
import org.axiondb.ExternalConnectionProvider;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.dataview.api.DataView;
import org.netbeans.modules.dm.virtual.db.api.AxionExternalConnectionProvider;
import org.netbeans.modules.etl.ui.view.ETLOutputWindowTopComponent;
import org.netbeans.modules.sql.framework.codegen.Statements;
import org.netbeans.modules.sql.framework.codegen.axion.AxionDB;
import org.netbeans.modules.sql.framework.common.utils.XmlUtil;
import org.netbeans.modules.sql.framework.model.DBColumn;
import org.netbeans.modules.sql.framework.model.RuntimeInput;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.model.DBConnectionDefinition;
import org.netbeans.modules.sql.framework.codegen.axion.AxionPipelineStatements;
import org.netbeans.modules.sql.framework.common.jdbc.SQLDBConnectionDefinition;
import org.netbeans.modules.sql.framework.common.jdbc.SQLUtils;
import org.netbeans.modules.sql.framework.common.utils.DBExplorerUtil;
import org.netbeans.modules.sql.framework.model.DBMetaDataFactory;
import org.netbeans.modules.sql.framework.model.DBTable;
import org.netbeans.modules.sql.framework.model.RuntimeDatabaseModel;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

public abstract class ETLDataOutputPanel extends JPanel implements ETLOutputPanel {

    public static final String NL = System.getProperty("line.separator", "\n");
    protected static final String AXION_DRIVER = "org.axiondb.jdbc.AxionDriver";
    protected static final String AXION_URL = "jdbc:axiondb:joinview";
    protected JButton[] btns;
    protected SQLObject sqlObject;
    protected SQLDefinition sqlDef;
    protected DataView dv;
    protected int pageSize = 10;

    public ETLDataOutputPanel(SQLObject sqlObj, SQLDefinition sqlDefinition) {
        this.sqlObject = sqlObj;
        this.sqlDef = sqlDefinition;

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

    public SQLObject getTable(){
        return sqlObject;
    }    
    
    public abstract void generateResult();

    public abstract void generateResult(SQLObject aTable);    
    
    public void generateOutput(SQLObject obj, RuntimeInput runInput) {
        this.sqlObject = obj;
        switch (obj.getObjectType()) {
            case SQLConstants.SOURCE_TABLE:
                showDataForDBTable(sqlObject, runInput);
                break;
            case SQLConstants.TARGET_TABLE:
                showDataForDBTable(sqlObject, runInput);
                break;
            case SQLConstants.JOIN_VIEW:
                showDataForJoinView(sqlObject, runInput);
                break;
            case SQLConstants.JOIN:
                showDataForJoinOperator(sqlObject, runInput);
            }
    }
    
    private void showDataForDBTable(SQLObject sqlobj, RuntimeInput runInput) {
        ETLOutputWindowTopComponent topComp = ETLOutputWindowTopComponent.findInstance();
        String sql = null;
        
        try {
            StatementContext context = new StatementContext();

            int dbType = getDBType(sqlobj);
            DB db = DBFactory.getInstance().getDatabase(dbType);
            DatabaseConnection dbconn = getDBConnection(sqlobj);
            
            if (sqlobj.getObjectType() == SQLConstants.TARGET_TABLE) {
                shutdownIfAxion(dbType, dbconn.getDatabaseURL());
            }
            
            Statements stmts = db.getStatements();
            if (sqlobj.getObjectType() == SQLConstants.SOURCE_TABLE) {
                SQLPart sqlPart = stmts.getSelectStatement((SourceTable) sqlobj, context);
                sql = sqlPart.getSQL();
            } else {
                SQLPart sqlPart = stmts.getSelectStatement((TargetTable) sqlobj, context);
                sql = sqlPart.getSQL();
            }

            sql = parseSQLForRuntimeInput(runInput, sql);
            dv = DataView.create(dbconn, sql.trim(), pageSize, true);
            Component comp = dv.createComponents().get(0);
            btns = dv.getEditButtons();
            this.add(comp);
            this.setName("Data:" + sqlobj.getDisplayName() + "  ");
            String tooltip = "<html><table border=0 cellspacing=0 cellpadding=0><tr><td>" +
                    XmlUtil.escapeHTML(sql).replaceAll("\\n", "<br>").replaceAll(" ", "&nbsp;") + "</td></tr></table></html>";
            this.setToolTipText(tooltip);
            topComp.addPanel(this, btns, tooltip);
        } catch (BaseException ex) {
            Exceptions.printStackTrace(ex);
        }
    } 
    
    public void shutdownIfAxion(int dbType, String url) {
        if (dbType == DBConstants.AXION) {
            try {
                url = DBExplorerUtil.adjustDatabaseURL(url);
                DBExplorerUtil.getAxionDBFromURL(url).shutdown();
            } catch (AxionException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private void showDataForJoinOperator(SQLObject obj, RuntimeInput runInput) {
        ETLOutputWindowTopComponent topComp = ETLOutputWindowTopComponent.findInstance();
        Statement stmt = null;
        Connection conn = null;
        String sql = null;
        String joinViewUrl = AXION_URL + System.currentTimeMillis();

        try {
            SQLJoinOperator joinOperator = (SQLJoinOperator) obj;
            Iterator it = joinOperator.getAllSourceTables().iterator();
            DBConnectionDefinition connDef = null;
            DBConnectionDefinition tempDef = null;
            boolean isSameDB = true;
            while (it.hasNext()) {
                SourceTable tbl = (SourceTable) it.next();
                connDef = tbl.getParent().getConnectionDefinition();
                if (tempDef == null) {
                    tempDef = connDef;
                }
                if (!connDef.getConnectionURL().equals(tempDef.getConnectionURL())) {
                    isSameDB = false;
                    break;
                }
            }
            String joinSql = "";
            StringBuilder buf = null;
            if (isSameDB) {
                conn = DBExplorerUtil.createConnection(connDef.getDriverClass(), connDef.getConnectionURL(), connDef.getUserName(), connDef.getPassword());
                StatementContext context = new StatementContext();
                DB db = DBFactory.getInstance().getDatabase(SQLUtils.getSupportedDBType(connDef.getDBType()));
                context.setUseSourceTableAliasName(true);
                buf = new StringBuilder(db.getStatements().getSelectStatement(joinOperator, context).getSQL());
                joinSql = joinSql + db.getGeneratorFactory().generate(joinOperator, context);
            } else {
                System.setProperty(ExternalConnectionProvider.EXTERNAL_CONNECTION_PROVIDER_PROPERTY_NAME, AxionExternalConnectionProvider.class.getName());
                conn = DBExplorerUtil.createConnection(AXION_DRIVER, joinViewUrl, "sa", "sa");
                stmt = conn.createStatement();
                it = joinOperator.getAllSourceTables().iterator();
                StatementContext joinContext = new StatementContext();
                while (it.hasNext()) {
                    SourceTable tbl = (SourceTable) it.next();
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
                    StatementContext context = new StatementContext();
                    context.setUsingUniqueTableName(tbl, true);
                    context.setUsingFullyQualifiedTablePrefix(false);
                    String localName = db.getUnescapedName(db.getGeneratorFactory().generate(tbl, context));
                    String remoteTableSql = stmts.getCreateRemoteTableStatement(tbl, localName, linkName).getSQL();
                    try {
                        stmt.execute(remoteTableSql);
                    } catch (SQLException e) {
                    }

                    // add sqlObject properties to statementcontext.
                    joinContext.setUsingFullyQualifiedTablePrefix(false);
                    joinContext.setUsingUniqueTableName(tbl, true);
                }
                DB db = DBFactory.getInstance().getDatabase(DBConstants.AXION);

                joinContext.setUsingUniqueTableName(true);
                buf = new StringBuilder(db.getStatements().getSelectStatement(joinOperator, joinContext).getSQL());
                joinSql = joinSql + db.getGeneratorFactory().generate(joinOperator, joinContext);
            }
            sql = buf.toString();
            sql = parseSQLForRuntimeInput(runInput, sql);
            DatabaseConnection dbconn = null;
            if (!isSameDB) {
                dbconn = DBExplorerUtil.createDatabaseConnection(AXION_DRIVER, joinViewUrl, "sa", "sa", false);
            } else {
                dbconn = DBExplorerUtil.createDatabaseConnection(connDef.getDriverClass(), connDef.getConnectionURL(),
                        connDef.getUserName(), connDef.getPassword(), false);
            }
            
            dv = DataView.create(dbconn, sql, pageSize, true);
            List<Component> compList = dv.createComponents();
            if (compList.isEmpty()) {
                throw new Exception("Unable to create ResultSet...");
            }
            Component comp = dv.createComponents().get(0);
            btns = dv.getEditButtons();

            this.add(comp);
            this.setName("Data:JoinView   ");
            String tooltip = "<html><table border=0 cellspacing=0 cellpadding=0><tr><td>" + ":  " +
                    XmlUtil.escapeHTML(sql).replaceAll("\\n", "<br>").replaceAll(" ", "&nbsp;") + "</td></tr></table></html>";
            this.setToolTipText(tooltip);
            topComp.addPanel(this, btns, tooltip);
            //topComp.addPanel(this, btns, this.filterButton, tooltip);
        } catch (Exception ex) {
            notifyDescriptor(ex);
        } finally {
            System.setProperty(ExternalConnectionProvider.EXTERNAL_CONNECTION_PROVIDER_PROPERTY_NAME, "");                
        }
    }

    private void showDataForJoinView(SQLObject obj, RuntimeInput runInput) {
        ETLOutputWindowTopComponent topComp = ETLOutputWindowTopComponent.findInstance();
        Statement stmt = null;
        Connection conn = null;
        String sql = null;
        String joinViewUrl = AXION_URL + System.currentTimeMillis();

        try {
            SQLJoinView joinView = (SQLJoinView) obj;
            Iterator it = joinView.getSourceTables().iterator();
            DBConnectionDefinition connDef = null;
            DBConnectionDefinition tempDef = null;
            boolean isSameDB = true;
            while (it.hasNext()) {
                SourceTable tbl = (SourceTable) it.next();
                connDef = tbl.getParent().getConnectionDefinition();
                if (tempDef == null) {
                    tempDef = connDef;
                }
                if (!connDef.getConnectionURL().equals(tempDef.getConnectionURL())) {
                    isSameDB = false;
                    break;
                }
            }
            StringBuilder buf = null;
            if (isSameDB) {
                conn = DBExplorerUtil.createConnection(connDef.getDriverClass(), connDef.getConnectionURL(), connDef.getUserName(), connDef.getPassword());
                StatementContext context = new StatementContext();
                DB db = DBFactory.getInstance().getDatabase(SQLUtils.getSupportedDBType(connDef.getDBType()));
                context.setUseSourceTableAliasName(true);
                buf = new StringBuilder(db.getStatements().getSelectStatement(joinView, context).getSQL());
            } else {
                System.setProperty(ExternalConnectionProvider.EXTERNAL_CONNECTION_PROVIDER_PROPERTY_NAME, AxionExternalConnectionProvider.class.getName());
                conn = DBExplorerUtil.createConnection(AXION_DRIVER, joinViewUrl, "sa", "sa");
                stmt = conn.createStatement();
                it = joinView.getSourceTables().iterator();
                StatementContext joinContext = new StatementContext();
                while (it.hasNext()) {
                    SourceTable tbl = (SourceTable) it.next();
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
                    StatementContext context = new StatementContext();
                    context.setUsingUniqueTableName(tbl, true);
                    context.setUsingFullyQualifiedTablePrefix(false);
                    String localName = db.getUnescapedName(db.getGeneratorFactory().generate(tbl, context));
                    String remoteTableSql = stmts.getCreateRemoteTableStatement(tbl, localName, linkName).getSQL();
                    try {
                        stmt.execute(remoteTableSql);
                    } catch (SQLException e) {
                    }

                    // add sqlObject properties to statementcontext.
                    joinContext.setUsingFullyQualifiedTablePrefix(false);
                    joinContext.setUsingUniqueTableName(tbl, true);
                }
                DB db = DBFactory.getInstance().getDatabase(DBConstants.AXION);
                joinContext.setUsingUniqueTableName(true);
                buf = new StringBuilder(db.getStatements().getSelectStatement(joinView, joinContext).getSQL());
            }
            sql = buf.toString();
            sql = parseSQLForRuntimeInput(runInput, sql);
            
            DatabaseConnection dbconn = null;
            if (!isSameDB) {
                dbconn = DBExplorerUtil.createDatabaseConnection(AXION_DRIVER, joinViewUrl,"sa", "sa", false);
            } else {
                dbconn = DBExplorerUtil.createDatabaseConnection(connDef.getDriverClass(), connDef.getConnectionURL(),
                        connDef.getUserName(), connDef.getPassword(), false);
            }
            dv = DataView.create(dbconn, sql, pageSize, true);
            List<Component> compList = dv.createComponents();
            if(compList.isEmpty()){
                throw new Exception("Unable to create ResultSet...");
            }
            
            Component comp = compList.get(0);
            btns = dv.getEditButtons();
            
            this.add(comp);
            this.setName("Data:JoinView   ");
            String tooltip = "<html><table border=0 cellspacing=0 cellpadding=0><tr><td>" + ":  " +
                    XmlUtil.escapeHTML(sql).replaceAll("\\n", "<br>").replaceAll(" ", "&nbsp;") + "</td></tr></table></html>";
            this.setToolTipText(tooltip);
            topComp.addPanel(this, btns, tooltip);
            //topComp.addPanel(this, btns, this.filterButton, tooltip);
        } catch (Exception ex) {
            notifyDescriptor(ex);
            System.setProperty(ExternalConnectionProvider.EXTERNAL_CONNECTION_PROVIDER_PROPERTY_NAME, "");
        }
    }

    protected String parseSQLForRuntimeInput(RuntimeInput runInput, String sql) {
        String mSql = sql;
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
    
    protected DatabaseConnection getDBConnection(SQLObject sqlObj) {
        DatabaseConnection dbConn = null;
        SQLDBModel sqlDBM = null;
        try {
            if(sqlObj instanceof SourceTable){
                sqlDBM = (SQLDBModel) ((SourceTable) sqlObj).getParentObject();
            } else if(sqlObj instanceof TargetTable){
                sqlDBM = (SQLDBModel) ((TargetTable) sqlObj).getParentObject();
            }
            DBConnectionDefinition dbCondef = sqlDBM.getConnectionDefinition();
            dbConn = DBExplorerUtil.createDatabaseConnection(dbCondef.getDriverClass(), dbCondef.getConnectionURL(),
                    dbCondef.getUserName(), dbCondef.getPassword());
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return dbConn;
    }

    private int getDBType(SQLObject obj) {
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
        return SQLUtils.getSupportedDBType(dbType);
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

    protected RuntimeDatabaseModel getRuntimeDbModel() {
        return sqlDef.getRuntimeDbModel();
    }
    
}
