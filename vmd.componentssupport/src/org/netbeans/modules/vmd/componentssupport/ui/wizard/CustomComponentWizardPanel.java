/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.vmd.componentssupport.ui.wizard;

import java.awt.Component;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Panel just asking for basic info.
 */
public class CustomComponentWizardPanel implements WizardDescriptor.Panel,
        WizardDescriptor.ValidatingPanel 
{

    private WizardDescriptor wizardDescriptor;
    private CustomComponentPanelVisual component;

    public CustomComponentWizardPanel() {
    }

    public Component getComponent() {
        if (component == null) {
            component = new CustomComponentPanelVisual(this);
            component.setName(
                    NbBundle.getMessage(CustomComponentWizardPanel.class, 
                    CustomComponentWizardIterator.STEP_BASIC_PARAMS));
        }
        return component;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(CustomComponentWizardPanel.class);
    }

    public boolean isValid() {
        getComponent();
        return component.valid(wizardDescriptor);
    }
    
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
        Set<ChangeListener> ls;
        synchronized (listeners) {
            ls = new HashSet<ChangeListener>(listeners);
        }
        ChangeEvent ev = new ChangeEvent(this);
        for (ChangeListener l : ls) {
            l.stateChanged(ev);
        }
    }

    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        component.read(wizardDescriptor);
    }

    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor) settings;
        component.store(d);
    }

    public void validate() throws WizardValidationException {
        getComponent();
        component.validate(wizardDescriptor);
    }
    
    private final Set<ChangeListener> listeners 
        = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0
}
