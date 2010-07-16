package org.netbeans.modules.etl.ui.view.wizardsloader;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.modules.etl.ui.view.wizards.ETLCollaborationWizard;
import org.netbeans.modules.etl.ui.view.wizards.ETLWizardContext;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * Panel for configuring table details such as table type,etc.
 * @author karthik
 */
public class SelectSchemaGenPanel implements WizardDescriptor.Panel {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private Component component;
    private int currentIndex = -1;
    private WizardDescriptor wd;

    public Component getComponent() {
        if (component == null) {
            component = new SelectSchemaGenVisualPanel(this);
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
        WizardDescriptor wizard = null;
        if (settings instanceof ETLWizardContext) {
            ETLWizardContext wizardContext = (ETLWizardContext) settings;
            wizard = (WizardDescriptor) wizardContext.getProperty(ETLWizardContext.WIZARD_DESCRIPTOR);

        } else if (settings instanceof WizardDescriptor) {
            wizard = (WizardDescriptor) settings;
        }

    }

    public void storeSettings(Object settings) {
        WizardDescriptor wizard = null;
        if (settings instanceof ETLWizardContext) {
            ETLWizardContext wizardContext = (ETLWizardContext) settings;
            //WizardDescriptor wizDes = (WizardDescriptor) settings;
            wizard = (WizardDescriptor) wizardContext.getProperty(ETLWizardContext.WIZARD_DESCRIPTOR);
        //DefaultTableModel tables = ((SourceSelectionVisualPanel) getComponent()).getSelectedTables();
        } else if (settings instanceof WizardDescriptor) {
            wizard = (WizardDescriptor) settings;
        }

        if (wizard != null) {
            final Object selectedOption = wizard.getValue();
            if (NotifyDescriptor.CANCEL_OPTION == selectedOption || NotifyDescriptor.CLOSED_OPTION == selectedOption) {
                return;
            }

            boolean isAdvancingPanel = (selectedOption == WizardDescriptor.NEXT_OPTION) || (selectedOption == WizardDescriptor.FINISH_OPTION);
            //MANISH
            if (isAdvancingPanel) {
                wizard.putProperty(ETLCollaborationWizard.SCHEMA_GEN_DB_CONN, ((SelectSchemaGenVisualPanel) getComponent()).getGeneratedSchemaDbConnectionURI());
                wizard.putProperty(ETLCollaborationWizard.SCHEMA_GEN_DB_TABLES, ((SelectSchemaGenVisualPanel) getComponent()).getGeneratedSchemaDbTables());
            }
        }
    }

    private boolean canAdvance() {
        return ((SelectSchemaGenVisualPanel) getComponent()).canAdvance();
    }

    public boolean isFinishPanel() {
        //XXX Implement finishable conditions
        return false;
    }
}
