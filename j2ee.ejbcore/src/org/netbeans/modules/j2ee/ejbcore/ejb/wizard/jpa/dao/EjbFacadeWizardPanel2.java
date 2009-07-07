package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.jpa.dao;

import java.awt.Component;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.J2eeProjectCapabilities;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class EjbFacadeWizardPanel2 implements WizardDescriptor.Panel, ChangeListener {
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private EjbFacadeVisualPanel2 component;
    private WizardDescriptor wizardDescriptor;
    private Project project;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    
    public EjbFacadeWizardPanel2(Project project, WizardDescriptor wizardDescriptor) {
        this.project = project;
        this.wizardDescriptor = wizardDescriptor;
    }
    
    public Component getComponent() {
        if (component == null) {
            component = new EjbFacadeVisualPanel2(wizardDescriptor);
            component.addChangeListener(this);
        }
        return component;
    }
    
    public HelpCtx getHelp() {
         return new HelpCtx(EjbFacadeWizardPanel2.class);
    }
    
    public boolean isValid() {
        getComponent();
        if (!(component.isRemote() || component.isLocal())) {
            if(J2eeProjectCapabilities.forProject(project).isEjb31LiteSupported())
            {
                //if it's jee6 project, ejb 3.1 allow to omit any interfaces
                return true;
            }
            else
            {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(EjbFacadeWizardPanel2.class, "ERR_ChooseInterface")); // NOI18N
                return false;
            }
        }
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, ""); // NOI18N
        return true;
    }
    
    public boolean isFinishPanel() {
        return true;
    }
    
    public final void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    
    public final void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    public String getPackage() {
        return component.getPackage();
    }

    public void stateChanged(ChangeEvent e) {
        changeSupport.fireChange();
    }

    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        component.read(wizardDescriptor);
    }
    
    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor) settings;
        component.store(d);
    }
    
    boolean isRemote() {
        return component.isRemote();
    }
    
    boolean isLocal() {
        return component.isLocal();
    }
    
}

