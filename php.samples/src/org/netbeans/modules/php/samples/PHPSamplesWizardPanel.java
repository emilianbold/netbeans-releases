/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.php.samples;

import java.awt.Component;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Panel just asking for basic info.
 */
public class PHPSamplesWizardPanel implements WizardDescriptor.Panel,
        WizardDescriptor.ValidatingPanel, WizardDescriptor.FinishablePanel {

    private WizardDescriptor wizardDescriptor;
    private PHPSamplesPanelVisual component;

    static final String SET_AS_MAIN = "setAsMain"; // NOI18N
    
    public PHPSamplesWizardPanel() {
    }

    public Component getComponent() {
        if (component == null) {
            component = new PHPSamplesPanelVisual(this);
            component.setName(NbBundle.getMessage(PHPSamplesWizardPanel.class, "LBL_CreateProjectStep"));
            component.putClientProperty("NewProjectWizard_Title", NbBundle.getMessage(PHPSamplesWizardPanel.class, "LBL_TXT_NewPHPSample"));
        }
        return component;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(PHPSamplesWizardPanel.class);
    }

    public boolean isValid() {
        getComponent();
        return component.valid(wizardDescriptor);
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
        component.read(wizardDescriptor);
        
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's
        // title  this name is used in NewProjectWizard to modify the title
        Object substitute = ((JComponent) component).getClientProperty("NewProjectWizard_Title"); // NOI18N
        if (substitute != null) {
            wizardDescriptor.putProperty("NewProjectWizard_Title", substitute); // NOI18N
        }
    }

    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor) settings;
        component.store(d);
        d.putProperty("NewProjectWizard_Title", null); // NOI18N
        // set as main project - never set as main
        d.putProperty(SET_AS_MAIN, false);
    }

    public boolean isFinishPanel() {
        return true;
    }

    public void validate() throws WizardValidationException {
        getComponent();
        component.validate(wizardDescriptor);
    }
}
