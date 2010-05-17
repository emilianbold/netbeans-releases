package org.netbeans.modules.etl.ui.view.wizardsloader;

import java.awt.Component;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.openide.ErrorManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.java.hulp.i18n.Logger;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBTable;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBDefinition;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBConnectionDefinition;
import org.netbeans.modules.dm.virtual.db.model.VirtualDatabaseModel;
import org.netbeans.modules.dm.virtual.db.ui.wizard.VirtualDBTableWizardIterator;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.ui.view.wizards.ETLWizardContext;
import org.netbeans.modules.sql.framework.common.utils.DBExplorerUtil;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * Panel for Selecting an existing Database or creating a new one from a common panel
 * @author Manish Bharani
 */
public class SelectOrCreateDBPanel implements WizardDescriptor.Panel {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private Component component;
    private int currentIndex = -1;
    private static final String DEFAULT_FLATFILE_JDBC_URL_PREFIX = "jdbc:axiondb:";
    private static transient final Logger mLogger = Logger.getLogger(SelectOrCreateDBPanel.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    public Component getComponent() {
        if (component == null) {
            component = new SelectOrCreateDBVisualPanel(this);
        }
        return component;
    }

    protected boolean createNewAxionDbInstance(String dbname, String location) {
        java.io.File f = new java.io.File(location);
        if (!f.exists()) {
            f.mkdirs();
        }
        String url = DEFAULT_FLATFILE_JDBC_URL_PREFIX + dbname + ":" + location;
        boolean status = false;
        Connection dbconn = null;
        try {
            dbconn = DBExplorerUtil.createConnection("org.axiondb.jdbc.AxionDriver", url, "sa", "sa");
            if (dbconn != null) {
                status = true;
            }
        } catch (Exception ex) {
            String nbBundle5 = "BUND273: Axion driver could not be loaded.";
            NotifyDescriptor d =
                    new NotifyDescriptor.Message(nbBundle5.substring(15), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        } finally {
            try {
                if (dbconn != null) {
                    dbconn.createStatement().execute("shutdown");
                    dbconn.close();
                }
            } catch (SQLException ex) {
                mLogger.errorNoloc("Unable to close db connection for url :" + url, ex);
            }
        }
        return status;
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
        WizardDescriptor wizard = null;
        if (settings instanceof ETLWizardContext) {
            ETLWizardContext wizardContext = (ETLWizardContext) settings;
            wizard = (WizardDescriptor) wizardContext.getProperty(ETLWizardContext.WIZARD_DESCRIPTOR);
        } else if (settings instanceof WizardDescriptor) {
            wizard = (WizardDescriptor) settings;
        }

        Connection conn = (Connection) wizard.getProperty(VirtualDBTableWizardIterator.CONNECTION);
        if (conn != null) {
            try {
                conn.createStatement().execute("shutdown");
                conn.close();
            } catch (SQLException ex) {
                //ignore
                }
        }
        wizard.putProperty(VirtualDBTableWizardIterator.CONNECTION, null);

    }

    public void storeSettings(Object settings) {
        WizardDescriptor wizard = null;
        if (settings instanceof ETLWizardContext) {
            ETLWizardContext wizardContext = (ETLWizardContext) settings;
            wizard = (WizardDescriptor) wizardContext.getProperty(ETLWizardContext.WIZARD_DESCRIPTOR);
        } else if (settings instanceof WizardDescriptor) {
            wizard = (WizardDescriptor) settings;
        }

        final Object selectedOption = wizard.getValue();
        if (NotifyDescriptor.CANCEL_OPTION == selectedOption || NotifyDescriptor.CLOSED_OPTION == selectedOption) {
            return;
        }

        boolean isAdvancingPanel = (selectedOption == WizardDescriptor.NEXT_OPTION) || (selectedOption == WizardDescriptor.FINISH_OPTION);
        if (isAdvancingPanel) {
        }

        if (wizard != null && isAdvancingPanel) {
            SelectOrCreateDBVisualPanel panel = (SelectOrCreateDBVisualPanel) getComponent();
            String database = panel.getSelectedDatabase();
            if (database != null) {
                database = database.trim();
                int end = database.indexOf(":", "jdbc.axiondb:".length());
                if (end == -1) {
                    end = database.trim().length();
                }
                String dbName = database.substring("jdbc:axiondb:".length(), end);
                wizard.putProperty("url", database);

                VirtualDatabaseModel model = (VirtualDatabaseModel) wizard.getProperty(VirtualDBTableWizardIterator.PROP_VIRTUALDBMODEL);
                if (model == null) {
                    VirtualDBConnectionDefinition def = new VirtualDBConnectionDefinition(dbName);
                    def.setConnectionURL(database);
                    model = new VirtualDatabaseModel(database, def);
                    model.setConnectionName(dbName);
                    VirtualDBDefinition ffDefn;
                    try {
                        ffDefn = new VirtualDBDefinition(dbName);
                        ffDefn.setInstanceName(dbName);
                        ffDefn.setVirtualDatabaseModel(model);
                    } catch (Exception ex) {
                        ErrorManager.getDefault().log(ErrorManager.ERROR, ex.getMessage());
                        ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);

                    }
                    wizard.putProperty(VirtualDBTableWizardIterator.PROP_VIRTUALDBMODEL, model);
                    wizard.putProperty(VirtualDBTableWizardIterator.TABLE_MAP, new HashMap<String, VirtualDBTable>());
                }
            } else {
                ErrorManager.getDefault().log("Create the database before adding tables.");
            }
        }
    }

    private boolean canAdvance() {
        return ((SelectOrCreateDBVisualPanel) getComponent()).canAdvance();
    }

    public boolean isFinishPanel() {
        //XXX Implement finishable conditions
        return false;
    }
}
