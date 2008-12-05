/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.python.project.templates;

import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

/**
 * Panel just asking for basic info.
 */
public class PanelConfigureProject implements WizardDescriptor.Panel,
        WizardDescriptor.ValidatingPanel, WizardDescriptor.FinishablePanel {

    private WizardDescriptor wizardDescriptor;
    private PanelConfigureProjectVisual component;
    private final NewPythonProjectWizardIterator.WizardType type;
    private final String[] steps;

    public PanelConfigureProject (final NewPythonProjectWizardIterator.WizardType type, String[] steps) {
        assert type != null;
        assert steps != null;
        this.type = type;
        this.steps = steps;
    }

    public PanelConfigureProjectVisual getComponent() {
        if (component == null) {
            component = new PanelConfigureProjectVisual(this, type);            
            component.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
        }
        return component;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(PanelConfigureProject.class);
    }

    public boolean isValid() {
        getComponent();
        return getComponent().valid(wizardDescriptor);
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
        getComponent().read(wizardDescriptor);
    }

    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor) settings;
        getComponent().store(d);
    }

    public boolean isFinishPanel() {
        return true;
    }

    public void validate() throws WizardValidationException {
        getComponent();
        getComponent().validate(wizardDescriptor);
    }
}
