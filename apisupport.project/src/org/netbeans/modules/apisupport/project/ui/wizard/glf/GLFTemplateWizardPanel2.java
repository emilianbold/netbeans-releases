package org.netbeans.modules.apisupport.project.ui.wizard.glf;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;


public class GLFTemplateWizardPanel2 implements WizardDescriptor.Panel {
    
    private GLFTemplateWizardIterator iterator;
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private GLFTemplateVisualPanel2 component;
    
    GLFTemplateWizardPanel2 (GLFTemplateWizardIterator iterator) {
        this.iterator = iterator;
    }
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public GLFTemplateVisualPanel2 getComponent () {
        if (component == null) {
            component = new GLFTemplateVisualPanel2 (this);
        }
        return component;
    }
    
    public HelpCtx getHelp () {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx(SampleWizardPanel1.class);
    }
    
    private boolean valid = true;
    
    public boolean isValid () {
        return valid;
    }
    
    void setValid (boolean valid) {
        if (this.valid == valid) return;
        this.valid = valid;
        fireChangeEvent();
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
    public void readSettings (Object settings) {}
    public void storeSettings (Object settings) {}
    
    String getMimeType () {
        return component.getMimeType ();
    }
    
    String getExtensions () {
        return component.getExtensions ();
    }
    
    GLFTemplateWizardIterator getIterator () {
        return iterator;
    }
}

