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

import org.netbeans.modules.jdbc.builder.DBMetaData;
import org.netbeans.modules.jdbc.builder.Table;
import org.netbeans.modules.jdbc.builder.TableColumn;
import org.netbeans.modules.mashup.db.common.Property;
import org.netbeans.modules.mashup.db.model.FlatfileDBColumn;
import org.netbeans.modules.mashup.db.model.FlatfileDBConnectionDefinition;
import org.netbeans.modules.mashup.db.model.FlatfileDBTable;
import org.netbeans.modules.mashup.db.model.FlatfileDatabaseModel;
import org.netbeans.modules.mashup.db.model.impl.FlatfileDBColumnImpl;
import org.netbeans.modules.mashup.db.model.impl.FlatfileDBTableImpl;
import org.netbeans.modules.mashup.db.ui.wizard.PreviewDatabaseVisualPanel;
import org.netbeans.modules.mashup.tables.wizard.MashupTableWizardIterator;
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
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new PreviewDatabaseVisualPanel();
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx(SampleWizardPanel1.class);
    }
    
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        return true;
        // If it depends on some condition (form filled out...), then:
        // return someCondition();
        // and when this condition changes (last form field filled in...) then:
        // fireChangeEvent();
        // and uncomment the complicated stuff below.
    }
    
    //public final void addChangeListener(ChangeListener l) {}
    //public final void removeChangeListener(ChangeListener l) {}
    
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
    
    
    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
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
        DBMetaData dbMeta = new DBMetaData();
        Connection conn = null;
        String catalog = null;
        String schema = null;
        String[][] tableList = null;
        String[] types = {"TABLE"};
        try {
            conn = model.getJDBCConnection();
            catalog = (conn.getCatalog() == null)? "" : conn.getCatalog();
            dbMeta.connectDB(conn);
            tableList = dbMeta.getTables(catalog, "", "", types);
            model = createTable(tableList, model, dbMeta);
            conn.createStatement().execute("shutdown");
            conn.close();
        } catch (Exception ex) {
            //ignore
        }
        return model;
    }
    
    private FlatfileDatabaseModel createTable(String[][] tableList, FlatfileDatabaseModel model, DBMetaData meta) {
        String[] currTable = null;
        FlatfileDBTable dbTable = null;
        FlatfileDBConnectionDefinition def = model.getFlatfileDBConnectionDefinition(true);
        if (tableList != null) {
            for(int i = 0; i < tableList.length; i ++) {
                currTable = tableList[i];
                dbTable = new FlatfileDBTableImpl(currTable[DBMetaData.NAME],
                        currTable[DBMetaData.SCHEMA], currTable[DBMetaData.CATALOG]);
                dbTable = addTableColumns(meta, dbTable);
                HashMap map = getTableMetaData(def, dbTable);
                dbTable.setProperties(map);
                model.addTable(dbTable);
            }
        }
        return model;
    }
    
    private FlatfileDBTable addTableColumns(DBMetaData dbMeta, FlatfileDBTable dbTable) {
        Table tbl = null;
        try {
            tbl = dbMeta.getTableMetaData(dbTable.getCatalog(),
                    dbTable.getSchema(), dbTable.getName(), "TABLE");
        } catch (Exception ex) {
            //ignore
        }
        TableColumn[] cols = tbl.getColumns();
        TableColumn tc = null;
        FlatfileDBColumn ffColumn = null;
        for (int j = 0; j < cols.length; j++) {
            tc = cols[j];
            ffColumn = new FlatfileDBColumnImpl(tc.getName(), tc
                    .getSqlTypeCode(), tc.getNumericScale(), tc
                    .getNumericPrecision(), tc
                    .getIsPrimaryKey(), tc.getIsForeignKey(),
                    false /* isIndexed */, tc.getIsNullable());
            dbTable.addColumn(ffColumn);
        }
        return dbTable;
    }
    
    private HashMap getTableMetaData(FlatfileDBConnectionDefinition condef, FlatfileDBTable element) {
        HashMap map = new HashMap();
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(condef.getConnectionURL());
            Statement stmt = conn.createStatement();
            String query = "select PROPERTY_NAME, PROPERTY_VALUE from AXION_TABLE_PROPERTIES "
                    + "where TABLE_NAME = '" + element.getName() + "' ORDER BY PROPERTY_NAME";
            stmt.execute(query);
            ResultSet rs = stmt.getResultSet();
            while (rs.next()) {
                rs.getMetaData().getColumnCount();
                String value1 = rs.getString(1);
                String value2 = rs.getString(2);
                
                Property prop = new Property();
                if (value2.equals("true") || value2.equals("false")) {
                    prop = new Property(value1,Boolean.class,true);
                    prop.setValue(Boolean.valueOf(value2));
                    map.put(value1, prop);
                } else {
                    try {
                        Integer.parseInt(value2);
                        prop = new Property(value1,Integer.class,true);
                        prop.setValue(Integer.valueOf(value2));
                        map.put(value1, prop);
                        
                    } catch (NumberFormatException e) {
                        prop = new Property(value1,String.class,true);
                        prop.setValue(value2);
                        map.put(value1, prop);
                    }
                }
            }
        } catch (Exception e) {
            //ignore            
        } finally {
            try {
                conn.createStatement().execute("shutdown");
                conn.close();
            } catch (Exception ex) {
                //ignore
            }
        }
        return map;
    }
    
}

