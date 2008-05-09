package org.netbeans.modules.mashup.db.ui.wizard;

import java.awt.Component;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.mashup.db.model.FlatfileDBConnectionDefinition;
import org.netbeans.modules.mashup.db.model.FlatfileDBTable;
import org.netbeans.modules.mashup.db.model.FlatfileDatabaseModel;
import org.netbeans.modules.mashup.db.model.FlatfileDefinition;
import org.netbeans.modules.mashup.db.model.impl.FlatfileDBConnectionDefinitionImpl;
import org.netbeans.modules.mashup.db.model.impl.FlatfileDatabaseModelImpl;
import org.netbeans.modules.mashup.tables.wizard.MashupTableWizardIterator;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;


public class SelectDatabasePanel extends AbstractWizardPanel {
    private static transient final Logger mLogger = Logger.getLogger(SelectDatabasePanel.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private SelectDatabaseVisualPanel component;
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if(component == null) {
            component = new SelectDatabaseVisualPanel(this);            
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public boolean isValid() {
        return someCondition();
    }
    
    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(Object settings) {
        if (settings instanceof WizardDescriptor) {
            WizardDescriptor wd = (WizardDescriptor) settings;
            Connection conn = (Connection) wd.getProperty(MashupTableWizardIterator.CONNECTION);
            if(conn != null) {
                try {
                    conn.createStatement().execute("shutdown");
                    conn.close();
                } catch (SQLException ex) {
                    //ignore
                }
            }
            wd.putProperty(MashupTableWizardIterator.CONNECTION, null);
        }
    }
    
    public void storeSettings(Object settings) {
        if (settings instanceof WizardDescriptor) {
            WizardDescriptor wd = (WizardDescriptor) settings;
            SelectDatabaseVisualPanel panel = (SelectDatabaseVisualPanel)getComponent();
            String database = panel.getSelectedDatabase();
            if(database != null) {
                database = database.trim();
                int end = database.indexOf(":", "jdbc.axiondb:".length());
                if(end == -1) {
                    end = database.trim().length();
                }
                String dbName = database.substring("jdbc:axiondb:".length(), end);
                wd.putProperty("url", database);
                
                FlatfileDatabaseModel model = (FlatfileDatabaseModel) wd.getProperty(MashupTableWizardIterator.PROP_FLATFILEDBMODEL);
                if(model == null) {
                    FlatfileDBConnectionDefinition def = new FlatfileDBConnectionDefinitionImpl(dbName);
                    def.setConnectionURL(database);
                    model = new FlatfileDatabaseModelImpl(database, def);
                    model.setConnectionName(dbName);
                    model.setDescription(dbName);
                    FlatfileDefinition ffDefn;
                    try {
                        ffDefn = new FlatfileDefinition(dbName);
                        ffDefn.setInstanceName(dbName);
                        ffDefn.setFlatfileDatabaseModel(model);
                    } catch (Exception ex) {
                        ErrorManager.getDefault().log(ErrorManager.ERROR, ex.getMessage());
                        ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
                        
                    }
                    wd.putProperty(MashupTableWizardIterator.PROP_FLATFILEDBMODEL, model);
                    wd.putProperty(MashupTableWizardIterator.TABLE_MAP, 
                            new HashMap<String, FlatfileDBTable>());
                }                
            } else {
                ErrorManager.getDefault().log("Create the database before adding tables.");
            }
        }
    }
    
    private boolean someCondition() {
        SelectDatabaseVisualPanel panel = (SelectDatabaseVisualPanel)getComponent();
        String dbUrl = panel.getSelectedDatabase();
        if(dbUrl == null) {
            return false;
        } else if(panel.isPopulated()){
            return true;
        }
        return true;
    }
    
    public String getStepLabel() {
        String nbBundle1 = mLoc.t("BUND227: Select Database");
        return nbBundle1.substring(15);
    }
    
    public String getTitle() {
        return "Select Database";
    }
}
