package org.netbeans.modules.etl.ui.view.wizardsloader;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.dm.virtual.db.ui.wizard.VirtualDBTableWizardIterator;

public class FileSelectionLoaderPanel implements WizardDescriptor.Panel {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private Component component;
    private WizardDescriptor wd;

    public Component getComponent() {
        if (component == null) {
            component = new FileSelectionLoaderVisualPanel(this);
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
            wd = (WizardDescriptor) settings;
        }
    }

    public void storeSettings(Object settings) {
        if (settings instanceof WizardDescriptor) {
            WizardDescriptor wizDes = (WizardDescriptor) settings;
            DefaultTableModel tables = ((FileSelectionLoaderVisualPanel) getComponent()).getSelectedTables();
            wizDes.putProperty(VirtualDBTableWizardIterator.TABLE_LIST, setTables(tables));
            wizDes.putProperty(VirtualDBTableWizardIterator.URL_LIST, setTables(tables));
        }
    }

    private List<String> setTables(DefaultTableModel model) {
        List<String> tables = new ArrayList<String>();
        for (int i = 0; i < model.getRowCount(); i++) {
            tables.add((String) model.getValueAt(i, 1));
        }
        return tables;
    }

    private boolean canAdvance() {
        return ((FileSelectionLoaderVisualPanel) getComponent()).canAdvance();
    }
}
