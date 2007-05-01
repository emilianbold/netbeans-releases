package org.netbeans.modules.mashup.tables.wizard;

import java.awt.Component;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import org.netbeans.modules.jdbc.builder.DBMetaData;
import org.netbeans.modules.jdbc.builder.ForeignKeyColumn;
import org.netbeans.modules.jdbc.builder.KeyColumn;
import org.netbeans.modules.jdbc.builder.Table;
import org.netbeans.modules.jdbc.builder.TableColumn;
import org.netbeans.modules.model.database.DBConnectionDefinition;
import org.netbeans.modules.sql.framework.common.utils.DBExplorerConnectionUtil;
import org.netbeans.modules.sql.framework.evaluators.database.DB;
import org.netbeans.modules.sql.framework.evaluators.database.DBFactory;
import org.netbeans.modules.sql.framework.evaluators.database.StatementContext;
import org.netbeans.modules.sql.framework.evaluators.database.axion.AxionDB;
import org.netbeans.modules.sql.framework.evaluators.database.axion.AxionPipelineStatements;
import org.netbeans.modules.sql.framework.evaluators.database.axion.AxionStatements;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.impl.ForeignKeyImpl;
import org.netbeans.modules.sql.framework.model.impl.PrimaryKeyImpl;
import org.netbeans.modules.sql.framework.model.impl.SourceColumnImpl;
import org.netbeans.modules.sql.framework.model.impl.SourceTableImpl;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.utils.StringUtil;
import java.sql.Statement;

public class JDBCTablePanel implements WizardDescriptor.FinishablePanel {
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private Component component;
    
    private List<String> dblinks = new ArrayList<String>();
    
    private List<String> statements = new ArrayList<String>();
    
    private String dblink;
    
    private String linkname;
    
    public Component getComponent() {
        if (component == null) {
            component = new JDBCTableVisualPanel(this);
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public boolean isValid() {
        return canAdvance();
    }
    
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }
    
    public void readSettings(Object settings) {
        if (settings instanceof WizardDescriptor) {
            WizardDescriptor wd = (WizardDescriptor) settings;
        }
    }
    public void storeSettings(Object settings) {
        if (settings instanceof WizardDescriptor) {
            WizardDescriptor wd = (WizardDescriptor) settings;
            Map<String, String> userMap = ((JDBCTableVisualPanel)getComponent()).getUserMap();
            Map<String, String> passwdMap = ((JDBCTableVisualPanel)getComponent()).getPasswordMap();
            Map<String, String> driverMap = ((JDBCTableVisualPanel)getComponent()).getDriverMap();
            populateStatements(((JDBCTableVisualPanel)getComponent()).getTables(), userMap, passwdMap, driverMap);
            wd.putProperty("dblinks", dblinks);
            wd.putProperty("statements", statements);
        }
    }
    
    private SQLDBModel getModel(String jdbcUrl, String user,
            String pass, String driver, String schema, String catalog, String table) throws Exception {
        DBMetaData meta = new DBMetaData();
        Connection conn = DBExplorerConnectionUtil.createConnection(driver, jdbcUrl, user, pass);
        meta.connectDB(conn);
        DBConnectionDefinition def = null;
        SQLDBModel model = SQLModelObjectFactory.getInstance().createDBModel(SQLConstants.SOURCE_DBMODEL);
        try {
            def = SQLModelObjectFactory.getInstance().createDBConnectionDefinition(jdbcUrl,
                    meta.getDBType(), driver, jdbcUrl, user, pass, "Descriptive info here");
        } catch (Exception ex) {
            // ignore
        }
        model.setModelName(jdbcUrl);
        model.setConnectionDefinition(def);
        SQLDBTable ffTable = getTable(meta, schema, catalog, table);
        
        Table t = null;
        t = meta.getTableMetaData(((SourceTableImpl)ffTable).getCatalog(),
                ((SourceTableImpl)ffTable).getSchema(), ((SourceTableImpl)ffTable).getName(), "TABLE");
        
        meta.checkForeignKeys(t);
        meta.checkPrimaryKeys(t);
        
        TableColumn[] cols = t.getColumns();
        TableColumn tc = null;
        List pks = t.getPrimaryKeyColumnList();
        List pkCols = new ArrayList();
        Iterator it = pks.iterator();
        while(it.hasNext()) {
            KeyColumn kc = (KeyColumn)it.next();
            pkCols.add(kc.getColumnName());
        }
        if(pks.size()!=0) {
            PrimaryKeyImpl pkImpl = new PrimaryKeyImpl(((KeyColumn)t.getPrimaryKeyColumnList().get(0)).getName(), pkCols, true);
            ((SourceTableImpl)ffTable).setPrimaryKey(pkImpl);
        }
        List fkList = t.getForeignKeyColumnList();
        it = fkList.iterator();
        while(it.hasNext()) {
            ForeignKeyColumn fkCol = (ForeignKeyColumn)it.next();
            ForeignKeyImpl fkImpl = new ForeignKeyImpl((SQLDBTable)ffTable, fkCol.getName(), fkCol.getImportKeyName(),
                    fkCol.getImportTableName(), fkCol.getImportSchemaName(), fkCol.getImportCatalogName(), fkCol.getUpdateRule(),
                    fkCol.getDeleteRule(), fkCol.getDeferrability());
            List fkColumns = new ArrayList();
            fkColumns.add(fkCol.getColumnName());
            String cat = fkCol.getImportCatalogName();
            if (cat == null) {
                cat = "";
            }
            String sch = fkCol.getImportSchemaName();
            if(sch == null) {
                sch = "";
            }
            pks = meta.getPrimaryKeys(cat, sch, fkCol.getImportTableName());
            List pkColumns = new ArrayList();
            Iterator pksIt = pks.iterator();
            while(pksIt.hasNext()) {
                KeyColumn kc = (KeyColumn)pksIt.next();
                pkColumns.add(kc.getColumnName());
            }
            fkImpl.setColumnNames(fkColumns, pkColumns);
            ((SourceTableImpl)ffTable).addForeignKey(fkImpl);
        }
        for (int j = 0; j < cols.length; j++) {
            tc = cols[j];
            SourceColumnImpl ffColumn = new SourceColumnImpl(tc.getName(), tc
                    .getSqlTypeCode(), tc.getNumericScale(), tc
                    .getNumericPrecision(), tc
                    .getIsPrimaryKey(), tc.getIsForeignKey(),
                    false, tc.getIsNullable());
            ((SourceTableImpl)ffTable).addColumn(ffColumn);
        }
        ((SourceTableImpl)ffTable).setEditable(true);
        ((SourceTableImpl)ffTable).setSelected(true);
        ((SourceTableImpl)ffTable).setAliasName("MASHUPDB");
        model.addTable((SourceTableImpl)ffTable);
        meta.disconnectDB();
        return model;
    }
    
    private SQLDBTable getTable(DBMetaData dbMeta, String schemaName, String catalogName, String tableName) throws Exception {
        String[][] tableList = dbMeta.getTablesOnly(catalogName, schemaName, "", false);
        SQLDBTable aTable = null;
        String[] currTable = null;
        if (tableList != null) {
            for (int i = 0; i < tableList.length; i++) {
                currTable = tableList[i];
                if(currTable[DBMetaData.NAME].equals(tableName)) {
                    aTable = new SourceTableImpl(currTable[DBMetaData.NAME].trim(),
                            currTable[DBMetaData.SCHEMA], currTable[DBMetaData.CATALOG]);
                    break;
                }
            }
        }
        return aTable;
    }
    
    private void getCreateStatement(String schema, String table, String jdbcUrl,
            String user, String pass, String driver) {
        StringBuffer createStatement = new StringBuffer();
        String catalog = "";
        try {
            SQLDBModel model = getModel(jdbcUrl, user, pass, driver, schema, catalog, table);
            SQLDBTable tbl = (SQLDBTable) model.getTables().get(0);
            AxionDB db = (AxionDB) DBFactory.getInstance().getDatabase(DB.AXIONDB);
            AxionStatements stmts = (AxionStatements) db.getStatements();
            AxionPipelineStatements pipelineStmts = db.getAxionPipelineStatements();
            linkname = StringUtil.createSQLIdentifier(model.getConnectionDefinition().getName());
            StatementContext context = new StatementContext();
            context.setUsingUniqueTableName(tbl, true);
            context.setUsingFullyQualifiedTablePrefix(false);
            String localName = db.getUnescapedName(db.getEvaluatorFactory().evaluate(tbl, context));
            createStatement.append(getCreateRemoteTableSQL(stmts, tbl, localName, linkname));
            dblinks.add(getCreateDBLinkSQL(stmts, model.getConnectionDefinition(), linkname, pass));
            statements.add(createStatement.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private String getCreateDBLinkSQL(AxionStatements stmts, DBConnectionDefinition connDef,
            String linkName, String pass) throws BaseException {
        StringBuffer stmtBuf = new StringBuffer(50);
        
        stmtBuf.append(stmts.getCreateDBLinkStatement(connDef, linkName).getSQL());
        int start = stmtBuf.indexOf("PASSWORD='") + "PASSWORD='".length();
        int end = stmtBuf.indexOf("'", start);
        stmtBuf.replace(start, end, pass);
        return stmtBuf.toString();
    }
    
    private String getCreateRemoteTableSQL(AxionStatements stmts, SQLDBTable table,
            String localName, String linkName) throws BaseException {
        StringBuffer stmtBuf = new StringBuffer(50);
        if (StringUtil.isNullString(localName)) {
            localName = table.getName();
        }
        
        // Generate a "create external table" statement that references its DB link
        stmtBuf.append(stmts.getCreateRemoteTableStatement(table, localName, linkName).getSQL());
        return stmtBuf.toString();
    }
    
    public boolean isFinishPanel() {
        return canAdvance();
    }
    
    private void populateStatements(DefaultTableModel model, Map<String, String> userMap, 
            Map<String, String> passwdMap, Map<String, String> driverMap) {
        dblinks.clear();
        statements.clear();
        for(int i = 0; i < model.getRowCount(); i++) {
            String table = (String) model.getValueAt(i, 0);
            String schema = (String) model.getValueAt(i, 1);
            String jdbcUrl = (String) model.getValueAt(i, 2);
            getCreateStatement(schema, table, jdbcUrl, userMap.get(jdbcUrl), 
                    passwdMap.get(jdbcUrl), driverMap.get(jdbcUrl));
        }
    }

    private boolean canAdvance() {
        return ((JDBCTableVisualPanel)getComponent()).canAdvance();
    }
}