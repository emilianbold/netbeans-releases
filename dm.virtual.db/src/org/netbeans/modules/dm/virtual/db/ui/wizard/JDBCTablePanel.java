package org.netbeans.modules.dm.virtual.db.ui.wizard;

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

import org.netbeans.modules.dm.virtual.db.bootstrap.PropertyKeys;
import org.netbeans.modules.dm.virtual.db.model.DBExplorerUtil;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBConnectionDefinition;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBException;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBMetaDataFactory;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBTable;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBUtil;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

import org.netbeans.modules.dm.virtual.db.model.VirtualDatabaseModel;

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

    private VirtualDatabaseModel getModel(String jdbcUrl, String user,
            String pass, String driver, String schema, String catalog, String table) throws Exception {
        VirtualDBMetaDataFactory meta = new VirtualDBMetaDataFactory();
        Connection conn = DBExplorerUtil.createConnection(driver, jdbcUrl, user, pass);
        meta.connectDB(conn);
        VirtualDBConnectionDefinition def = null;
        VirtualDatabaseModel model = new VirtualDatabaseModel();
        try {
            def = new VirtualDBConnectionDefinition(jdbcUrl,driver, jdbcUrl, user, pass);
        } catch (Exception ex) {
        // ignore
        }
        model.setModelName(jdbcUrl);
        model.setVirtualDBConnectionDefinition(def);
        VirtualDBTable newTable = getTable(meta, schema, catalog, table);
        meta.populateColumns(newTable);


        model.addTable(newTable);
        meta.disconnectDB();
        return model;
    }

    private VirtualDBTable getTable(VirtualDBMetaDataFactory dbMeta, String schemaName, String catalogName, String tableName) throws Exception {
        String[][] tableList = dbMeta.getTablesAndViews(catalogName, schemaName, "", false);
        VirtualDBTable aTable = null;
        String[] currTable = null;
        if (tableList != null) {
            for (int i = 0; i < tableList.length; i++) {
                currTable = tableList[i];
                if (currTable[VirtualDBMetaDataFactory.NAME].equals(tableName)) {
                    aTable = new VirtualDBTable(currTable[VirtualDBMetaDataFactory.NAME].trim(),
                            currTable[VirtualDBMetaDataFactory.SCHEMA], currTable[VirtualDBMetaDataFactory.CATALOG]);
                    break;
                }
            }
        }
        return aTable;
    }

    private void getCreateStatement(String schema, String table, String jdbcUrl,
            String user, String pass, String driver) {
        String catalog = "";
        try {
            VirtualDatabaseModel model = getModel(jdbcUrl, user, pass, driver, schema, catalog, table);
            VirtualDBTable tbl = model.getTables().get(0);
            linkname = VirtualDBUtil.createSQLIdentifier(model.getVirtualDBConnectionDefinition().getName());
            dblinks.add(getCreateDBLinkSQL(model.getVirtualDBConnectionDefinition(), linkname, pass));
            tbl.setOrPutProperty(PropertyKeys.LOADTYPE, PropertyKeys.JDBC);
            tbl.setOrPutProperty(PropertyKeys.DBLINK, linkname);
            tbl.setOrPutProperty(PropertyKeys.REMOTETABLE, table);
            statements.add(tbl.getCreateStatementSQL());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String getCreateDBLinkSQL(VirtualDBConnectionDefinition connDef,
            String linkName, String pass) throws VirtualDBException {
        StringBuilder stmtBuf = new StringBuilder(50);

        stmtBuf.append(getCreateDBLinkStatement(connDef, linkName));
        int start = stmtBuf.indexOf("PASSWORD='") + "PASSWORD='".length();
        int end = stmtBuf.indexOf("'", start);
        stmtBuf.replace(start, end, pass);
        return stmtBuf.toString();
    }

    public String getCreateDBLinkStatement(VirtualDBConnectionDefinition connDef, String linkName){
        StringBuilder buffer = new StringBuilder(100);
        buffer.append("CREATE DATABASE LINK \"").append(linkName).append("\" (");
        buffer.append(" DRIVER=\'").append(connDef.getDriverClass()).append("\'").append(" URL=\'").append(connDef.getConnectionURL()).append("\'");
        buffer.append(" USERNAME=\'").append(connDef.getUserName()).append("\'").append(" PASSWORD=\'").append(connDef.getPassword()).append("\'").append(")");
        return buffer.toString();
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
