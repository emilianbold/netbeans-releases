/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
 */
package org.netbeans.modules.mashup.db.wizard;

import java.awt.Component;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.modules.sql.framework.model.DBMetaDataFactory;
import org.netbeans.modules.mashup.db.common.Property;
import org.netbeans.modules.mashup.db.model.FlatfileDBTable;
import org.netbeans.modules.mashup.db.model.FlatfileDatabaseModel;
import org.netbeans.modules.mashup.db.model.impl.FlatfileDBTableImpl;
import org.netbeans.modules.mashup.db.ui.wizard.PreviewDatabaseVisualPanel;
import org.netbeans.modules.mashup.tables.wizard.MashupTableWizardIterator;
import org.netbeans.modules.sql.framework.model.DBConnectionDefinition;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 *
 * @author  ks161616
 */
public class FlatfileViewerTreePanel implements WizardDescriptor.Panel {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private PreviewDatabaseVisualPanel component;

    public Component getComponent() {
        if (component == null) {
            component = new PreviewDatabaseVisualPanel();
        }
        return component;
    }

    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    public boolean isValid() {
        return true;
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

    public final void fireChangeEvent() {
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

            FlatfileDatabaseModel folder = (FlatfileDatabaseModel) wd.getProperty(MashupTableWizardIterator.PROP_FLATFILEDBMODEL);
            folder = addDBTables(folder);
            if (folder == null || folder.getTables().size() == 0) {
                throw new IllegalStateException("Context must contain a populated FlatfileDatabaseModel.");
            }

            component.setModel(folder);
            fireChangeEvent();
        }
    }

    public void storeSettings(Object settings) {
        if (settings instanceof WizardDescriptor) {
            WizardDescriptor wd = (WizardDescriptor) settings;
            wd.putProperty(MashupTableWizardIterator.PROP_FLATFILEDBMODEL, null);
            fireChangeEvent();
        }
    }

    private FlatfileDatabaseModel addDBTables(FlatfileDatabaseModel model) {
        DBMetaDataFactory dbMeta = new DBMetaDataFactory();
        Connection conn = null;
        String catalog = null;
        String[][] tableList = null;
        String[] types = {"TABLE"};
        try {
            conn = model.getJDBCConnection();
            catalog = (conn.getCatalog() == null) ? "" : conn.getCatalog();
            dbMeta.connectDB(conn);
            tableList = dbMeta.getTables(catalog, "", "", types);
            model = createTable(tableList, model, dbMeta);
            conn.createStatement().execute("shutdown");
        } catch (Exception ex) {
            //ignore
        } finally {
            dbMeta.disconnectDB();
        }
        return model;
    }

    private FlatfileDatabaseModel createTable(String[][] tableList, FlatfileDatabaseModel model, DBMetaDataFactory meta) throws Exception {
        String[] currTable = null;
        FlatfileDBTable dbTable = null;
        DBConnectionDefinition def = model.getFlatfileDBConnectionDefinition(true);
        if (tableList != null) {
            for (int i = 0; i < tableList.length; i++) {
                currTable = tableList[i];
                dbTable = new FlatfileDBTableImpl(currTable[DBMetaDataFactory.NAME],
                        currTable[DBMetaDataFactory.SCHEMA], currTable[DBMetaDataFactory.CATALOG]);
                meta.populateColumns(dbTable);
                HashMap map = getTableMetaData(def, dbTable);
                dbTable.setProperties(map);
                model.addTable(dbTable);
            }
        }
        return model;
    }

    private HashMap getTableMetaData(DBConnectionDefinition condef, FlatfileDBTable element) {
        HashMap<String, Property> map = new HashMap<String, Property>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DriverManager.getConnection(condef.getConnectionURL());
            stmt = conn.createStatement();
            String query = "select PROPERTY_NAME, PROPERTY_VALUE from AXION_TABLE_PROPERTIES " + "where TABLE_NAME = '" + element.getName() + "' ORDER BY PROPERTY_NAME";
            stmt.execute(query);
            rs = stmt.getResultSet();
            while (rs.next()) {
                rs.getMetaData().getColumnCount();
                String value1 = rs.getString(1);
                String value2 = rs.getString(2);

                Property prop = new Property();
                if (value2.equals("true") || value2.equals("false")) {
                    prop = new Property(value1, Boolean.class, true);
                    prop.setValue(Boolean.valueOf(value2));
                    map.put(value1, prop);
                } else {
                    try {
                        Integer.parseInt(value2);
                        prop = new Property(value1, Integer.class, true);
                        prop.setValue(Integer.valueOf(value2));
                        map.put(value1, prop);

                    } catch (NumberFormatException e) {
                        prop = new Property(value1, String.class, true);
                        prop.setValue(value2);
                        map.put(value1, prop);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (Exception ex) {
            //ignore
            }

            try {
                stmt.close();
            } catch (Exception ex) {
            //ignore
            }

            try {
                conn.close();
            } catch (Exception ex) {
            //ignore
            }
        }
        return map;
    }
}

