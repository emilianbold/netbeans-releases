/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.iep.editor.wizard;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;

public class IEPWizardPanel2 implements WizardDescriptor.Panel {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private IEPVisualPanel2 component;

    private WizardDescriptor mDescriptor;
    
    private Project mProject;
    
    public IEPWizardPanel2(Project project) {
        this.mProject = project;
    }
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new IEPVisualPanel2(this.mProject);
            component.addPropertyChangeListener(new MyPropertyChangeListener());
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
        return component.getSelectedSchemaComponent() != null;
//        // If it is always OK to press Next or Finish, then:
//        return true;
//    // If it depends on some condition (form filled out...), then:
//    // return someCondition();
//    // and when this condition changes (last form field filled in...) then:
//    // fireChangeEvent();
//    // and uncomment the complicated stuff below.
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
    public void readSettings(Object settings) {
        this.mDescriptor = (WizardDescriptor) settings;
    }

    public void storeSettings(Object settings) {
//        SchemaComponent sc = component.getSelectedSchemaComponent();
//        
//        mDescriptor.putProperty(WizardConstants.WIZARD_SELECTED_ELEMENT_OR_TYPE_KEY, sc);
    }
    
    class MyPropertyChangeListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if(ElementOrTypeChooserPanel.PROP_SELECTED_SCHEMA_COMPONENT.equals(propName)) {
                fireChangeEvent();
            }
            
        }
        
    }
}

