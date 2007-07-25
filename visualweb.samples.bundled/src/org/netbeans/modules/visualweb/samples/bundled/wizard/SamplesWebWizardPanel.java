package org.netbeans.modules.visualweb.samples.bundled.wizard;

import java.awt.Component;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import org.netbeans.spi.project.ui.templates.support.Templates;import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class SamplesWebWizardPanel implements WizardDescriptor.Panel {
    private WizardDescriptor wizard;

    /*
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private SamplesWebVisualPanel component;
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new SamplesWebVisualPanel(this);
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx(SampleWizardPanel1.class);
    }
    
    public boolean isValid() {
        FileObject template = Templates.getTemplate( this.wizard );
        String projectLocation = (String) this.component.getProjectLocation();
        File projectLocationFile = new File( projectLocation );
        String projectName = (String) this.component.getProjectName();
        File projectNameFile = new File( projectLocation + File.separator + projectName );
        boolean result = ! projectNameFile.exists() && projectLocationFile.exists();
        if ( ! result ) {
            String errorMessage = NbBundle.getMessage( SamplesWebWizardPanel.class, "MSG_not_valid_project_name", projectName );
            this.wizard.putProperty ("WizardPanel_errorMessage", errorMessage); // NOI18N
        } else {
            this.wizard.putProperty ("WizardPanel_errorMessage", null); // NOI18N
        }
        // If it is always OK to press Next or Finish, then:
        return result;
        // If it depends on some condition (form filled out...), then:
        // return someCondition();
        // and when this condition changes (last form field filled in...) then:
        // fireChangeEvent();
        // and uncomment the complicated stuff below.
    }
    
    //public final void addChangeListener(ChangeListener l) {}
    //public final void removeChangeListener(ChangeListener l) {}
    
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
    
    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(Object settings) {
        this.wizard = (WizardDescriptor) settings;
        this.component.read(wizard);
    }
    
    public void storeSettings(Object settings) {
        WizardDescriptor wd = (WizardDescriptor) settings;
        this.component.store(wd);
    }

}

