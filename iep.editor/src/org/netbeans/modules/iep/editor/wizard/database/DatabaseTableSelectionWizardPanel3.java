/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.iep.editor.wizard.database;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class DatabaseTableSelectionWizardPanel3 implements WizardDescriptor.Panel {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private DatabaseTableSelectionVisualPanel3 component;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new DatabaseTableSelectionVisualPanel3();
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

    public final void addChangeListener(ChangeListener l) {
    }

    public final void removeChangeListener(ChangeListener l) {
    }
    /*
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0
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
     */

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(Object settings) {
        WizardDescriptor wiz = (WizardDescriptor) settings;
        List<TableInfo> selectedTables = (List<TableInfo>) wiz.getProperty(DatabaseTableWizardConstants.PROP_SELECTED_TABLES);
        List<ColumnInfo> selectedColumns = (List<ColumnInfo>) wiz.getProperty(DatabaseTableWizardConstants.PROP_SELECTED_COLUMNS);
        if(selectedTables != null && selectedColumns != null) { 
            List<TableInfo> newTablesWithSelectedColumns = createNewTableWithSelectedColumns(selectedTables, selectedColumns);
            
            component.setSelectedTables(newTablesWithSelectedColumns);
        }
        
    }

    public void storeSettings(Object settings) {
        WizardDescriptor wiz = (WizardDescriptor) settings;
        
        String pollingInterval = component.getPollingInterval();
        String timeUnit = component.getPollingTimeUnit();
        String pollingRecordSize = component.getPollingRecordSize();
        String jndiName = component.getJNDIName();
        boolean isDeleteRecords = component.isDeleteRecords();
        String isDeleteRecordsString = Boolean.toString(isDeleteRecords);
        List<ColumnInfo> recordIdentifyingColumns = component.getSelectedColumns();
        
        
        wiz.putProperty(DatabaseTableWizardConstants.PROP_POLLING_INTERVAL, pollingInterval);
        wiz.putProperty(DatabaseTableWizardConstants.PROP_POLLING_INTERVAL_TIME_UNIT, timeUnit);
        wiz.putProperty(DatabaseTableWizardConstants.PROP_POLLING_RECORD_SIZE, pollingRecordSize);
        wiz.putProperty(DatabaseTableWizardConstants.PROP_JNDI_NAME, jndiName);
        wiz.putProperty(DatabaseTableWizardConstants.PROP_IS_DELETE_RECORDS, isDeleteRecordsString);
        wiz.putProperty(DatabaseTableWizardConstants.PROP_POLLING_UNIQUE_RECORD_IDENTIFIER_COLUMNS, recordIdentifyingColumns);
    }
    
    private List<TableInfo> createNewTableWithSelectedColumns(List<TableInfo> selectedTables, List<ColumnInfo> selectedColumns) {
        List<TableInfo> tablesWithSelectedColumns = new ArrayList<TableInfo>();
        
        Iterator<TableInfo> it = selectedTables.iterator();
        while(it.hasNext()) {
            TableInfo tInfo = it.next();
            TableInfo newTable = createNewTable(tInfo, selectedColumns);
            tablesWithSelectedColumns.add(newTable);
        }
        
        return tablesWithSelectedColumns;
    }
    
    private TableInfo createNewTable(TableInfo selectedTable, List<ColumnInfo> columns) {
        
        TableInfo table = new TableInfo(selectedTable.getCatalogName(), 
                                        selectedTable.getSchemaName(), 
                                        selectedTable.getTableName(), 
                                        selectedTable.getTableType());
        
        Iterator<ColumnInfo> it = columns.iterator();
        while(it.hasNext()) {
            ColumnInfo c = it.next();
            if(c.getTable().equals(selectedTable)) {
            	ColumnInfo column = new ColumnInfo(c.getColumnName(),
            									   c.getColumnDataType(),
            									   c.getPrecision(),
            									   c.getScale());
                table.addColumn(column);
            }
        }
        
        return table;
    }
}

