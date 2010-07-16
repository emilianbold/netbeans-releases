/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.iep.editor.wizard.database;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class DatabaseTableSelectionWizardPanel1 implements WizardDescriptor.Panel {

    private boolean mIsValid = false;
    
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private DatabaseTableSelectionVisualPanel1 component;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new DatabaseTableSelectionVisualPanel1();
            component.getAvailableTablesList().getSelectionModel().addListSelectionListener(new SelectTableListSelectionListener());
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
        return mIsValid;
    // If it depends on some condition (form filled out...), then:
    // return someCondition();
    // and when this condition changes (last form field filled in...) then:
    // fireChangeEvent();
    // and uncomment the complicated stuff below.
    }

    
    
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
     

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(Object settings) {
    }

    public void storeSettings(Object settings) {
        WizardDescriptor wiz = (WizardDescriptor) settings;
        Connection connection = component.getSelectedConnection();
        List<TableInfo> selectedTables = component.getSelectedTables();
        if(connection != null && selectedTables != null) {
        	//now load table columns, primarykey, foreign keys.
        	try {
	        	Iterator<TableInfo> it = selectedTables.iterator();
	        	while(it.hasNext()) {
	        		TableInfo table = it.next();
	        		table.cleanUp();
	        		DatabaseMetaDataHelper.populateTableColumns(table, connection);
	        		DatabaseMetaDataHelper.populatePrimaryKeys(table, connection);
	        		DatabaseMetaDataHelper.populateForeignKeys(table, connection);
	        	}
        	} catch(Exception ex) {
        		ErrorManager.getDefault().notify(ex);
        	}
        }
        wiz.putProperty(DatabaseTableWizardConstants.PROP_SELECTED_TABLES, selectedTables);
        
        wiz.putProperty(DatabaseTableWizardConstants.PROP_SELECTED_DB_CONNECTION, connection);
    }
    
    class SelectTableListSelectionListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent e) {
            if(component.getSelectedTables() != null && component.getSelectedTables().size() != 0) {
                mIsValid = true;
            } else {
                mIsValid = false;
            }
             
            fireChangeEvent();
            
        }
        
    }
}

