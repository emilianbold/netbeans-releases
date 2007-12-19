package org.netbeans.modules.mashup.tables.wizard;

import java.awt.Component;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import org.netbeans.modules.sql.framework.model.DBMetaDataFactory;
import org.netbeans.modules.sql.framework.common.utils.DBExplorerUtil;
import org.netbeans.modules.sql.framework.codegen.DB;
import org.netbeans.modules.sql.framework.codegen.DBFactory;
import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.codegen.axion.AxionDB;
import org.netbeans.modules.sql.framework.codegen.axion.AxionStatements;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.impl.SourceTableImpl;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.utils.StringUtil;
import org.netbeans.modules.sql.framework.model.DBConnectionDefinition;

public class JDBCTablePanel implements WizardDescriptor.FinishablePanel {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private Component component;
    private List<String> dblinks = new ArrayList<String>();
    private List<String> statements = new ArrayList<String>();
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
            Map<String, String> userMap = ((JDBCTableVisualPanel) getComponent()).getUserMap();
            Map<String, String> passwdMap = ((JDBCTableVisualPanel) getComponent()).getPasswordMap();
            Map<String, String> driverMap = ((JDBCTableVisualPanel) getComponent()).getDriverMap();
            populateStatements(((JDBCTableVisualPanel) getComponent()).getTables(), userMap, passwdMap, driverMap);
            wd.putProperty("dblinks", dblinks);
            wd.putProperty("statements", statements);
        }
    }

    private SQLDBModel getModel(String jdbcUrl, String user,
            String pass, String driver, String schema, String catalog, String table) throws Exception {
        DBMetaDataFactory meta = new DBMetaDataFactory();
        Connection conn = DBExplorerUtil.createConnection(driver, jdbcUrl, user, pass);
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
        SourceTableImpl newTable = getTable(meta, schema, catalog, table);
        meta.populateColumns(newTable);

        newTable.setEditable(true);
        newTable.setSelected(true);
        newTable.setAliasName("MASHUPDB");

        model.addTable(newTable);
        meta.disconnectDB();
        return model;
    }

    private SourceTableImpl getTable(DBMetaDataFactory dbMeta, String schemaName, String catalogName, String tableName) throws Exception {
        String[][] tableList = dbMeta.getTablesAndViews(catalogName, schemaName, "", false);
        SourceTableImpl aTable = null;
        String[] currTable = null;
        if (tableList != null) {
            for (int i = 0; i < tableList.length; i++) {
                currTable = tableList[i];
                if (currTable[DBMetaDataFactory.NAME].equals(tableName)) {
                    aTable = new SourceTableImpl(currTable[DBMetaDataFactory.NAME].trim(),
                            currTable[DBMetaDataFactory.SCHEMA], currTable[DBMetaDataFactory.CATALOG]);
                    break;
                }
            }
        }
        return aTable;
    }

    private void getCreateStatement(String schema, String table, String jdbcUrl,
            String user, String pass, String driver) {
        StringBuilder createStatement = new StringBuilder();
        String catalog = "";
        try {
            SQLDBModel model = getModel(jdbcUrl, user, pass, driver, schema, catalog, table);
            SQLDBTable tbl = (SQLDBTable) model.getTables().get(0);
            AxionDB db = (AxionDB) DBFactory.getInstance().getDatabase(DB.AXIONDB);
            AxionStatements stmts = (AxionStatements) db.getStatements();
            linkname = StringUtil.createSQLIdentifier(model.getConnectionDefinition().getName());
            StatementContext context = new StatementContext();
            context.setUsingUniqueTableName(tbl, true);
            context.setUsingFullyQualifiedTablePrefix(false);
            String localName = db.getUnescapedName(db.getGeneratorFactory().generate(tbl, context));
            createStatement.append(getCreateRemoteTableSQL(stmts, tbl, localName, linkname));
            dblinks.add(getCreateDBLinkSQL(stmts, model.getConnectionDefinition(), linkname, pass));
            statements.add(createStatement.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String getCreateDBLinkSQL(AxionStatements stmts, DBConnectionDefinition connDef,
            String linkName, String pass) throws BaseException {
        StringBuilder stmtBuf = new StringBuilder(50);

        stmtBuf.append(stmts.getCreateDBLinkStatement(connDef, linkName).getSQL());
        int start = stmtBuf.indexOf("PASSWORD='") + "PASSWORD='".length();
        int end = stmtBuf.indexOf("'", start);
        stmtBuf.replace(start, end, pass);
        return stmtBuf.toString();
    }

    private String getCreateRemoteTableSQL(AxionStatements stmts, SQLDBTable table,
            String localName, String linkName) throws BaseException {
        StringBuilder stmtBuf = new StringBuilder(50);
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
        for (int i = 0; i < model.getRowCount(); i++) {
            String table = (String) model.getValueAt(i, 0);
            String schema = (String) model.getValueAt(i, 1);
            String jdbcUrl = (String) model.getValueAt(i, 2);
            getCreateStatement(schema, table, jdbcUrl, userMap.get(jdbcUrl),
                    passwdMap.get(jdbcUrl), driverMap.get(jdbcUrl));
        }
    }

    private boolean canAdvance() {
        return ((JDBCTableVisualPanel) getComponent()).canAdvance();
    }
}
