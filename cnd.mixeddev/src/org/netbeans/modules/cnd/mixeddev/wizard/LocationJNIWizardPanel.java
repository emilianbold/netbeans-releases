/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.mixeddev.wizard;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class LocationJNIWizardPanel implements WizardDescriptor.Panel<WizardDescriptor> {
    
    private WizardDescriptor wizardDescriptor;
    private boolean initialized = false;

    public LocationJNIWizardPanel() {
        
    }

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private PanelProjectLocationVisual component;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public PanelProjectLocationVisual getComponent() {
        if (component == null) {
            component = new PanelProjectLocationVisual(this, "CppJNILibrary", false);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx("help.key.here");
    }

    @Override
    public boolean isValid() {
        return getComponent().valid(wizardDescriptor);
    }

    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);

    @Override
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    protected final void fireChangeEvent() {
       fireChangeEvent(new ChangeEvent(this));
    }
    
    protected final void fireChangeEvent(ChangeEvent ev) {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        while (it.hasNext()) {
            (it.next()).stateChanged(ev);
        }
    }    

    @Override
    public void readSettings(WizardDescriptor settings) {
        if (initialized) {
            return;
        }
        wizardDescriptor = settings;
        getComponent().read(wizardDescriptor);
        initialized = true;
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        getComponent().store(settings);
        initialized = false;
    }
}
