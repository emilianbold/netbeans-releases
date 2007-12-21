package org.netbeans.modules.mashup.db.wizard;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class NewFlatfileDatabaseWizardPanel implements WizardDescriptor.Panel {

    private Component component;

    public Component getComponent() {
        if (component == null) {
            component = new NewFlatfileDatabaseVisualPanel(this);
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
        NewFlatfileDatabaseVisualPanel panel = (NewFlatfileDatabaseVisualPanel) getComponent();
        panel.clearText();
    }

    public void storeSettings(Object settings) {
        if (settings instanceof WizardDescriptor) {
            WizardDescriptor wd = (WizardDescriptor) settings;
            NewFlatfileDatabaseVisualPanel panel = (NewFlatfileDatabaseVisualPanel) getComponent();
            wd.putProperty("dbName", panel.getDBName());
        }
    }

    private boolean canAdvance() {
        NewFlatfileDatabaseVisualPanel panel = (NewFlatfileDatabaseVisualPanel) getComponent();
        return panel.canProceed();
    }
}
