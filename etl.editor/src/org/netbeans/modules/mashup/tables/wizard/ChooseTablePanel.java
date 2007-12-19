package org.netbeans.modules.mashup.tables.wizard;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.modules.mashup.db.model.FlatfileDBTable;
import org.netbeans.modules.mashup.db.model.impl.FlatfileDBTableImpl;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class ChooseTablePanel implements WizardDescriptor.Panel {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private Component component;

    public Component getComponent() {
        if (component == null) {
            component = new ChooseTableVisualPanel(this);
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
            FlatfileDBTable table = (FlatfileDBTable) wd.getProperty(
                    MashupTableWizardIterator.PROP_CURRENTTABLE);
            List<String> urls = (List<String>) wd.getProperty(MashupTableWizardIterator.URL_LIST);
            int index = Integer.parseInt((String) wd.getProperty(
                    MashupTableWizardIterator.TABLE_INDEX));
            ((FlatfileDBTableImpl) table).setOrPutProperty("URL", urls.get(index));
            if (table != null) {
                ((ChooseTableVisualPanel) getComponent()).populateTablesList(urls.get(index));
            }
        }
    }

    public void storeSettings(Object settings) {
        if (settings instanceof WizardDescriptor) {
            WizardDescriptor wd = (WizardDescriptor) settings;
            FlatfileDBTable table = (FlatfileDBTable) wd.getProperty(
                    MashupTableWizardIterator.PROP_CURRENTTABLE);
            ((FlatfileDBTableImpl) table).setOrPutProperty("TABLENUMBER",
                    String.valueOf(((ChooseTableVisualPanel) getComponent()).getTableDepth()));
        }
    }

    private boolean canAdvance() {
        return ((ChooseTableVisualPanel) getComponent()).canAdvance();
    }
}
