/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.wsdlextensions.ldap;

import java.awt.Component;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardContext;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardDescriptorPanel;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class LDAPServerBrowserWizardPanel2 extends WSDLWizardDescriptorPanel {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private LDAPServerBrowserVisualPanel2 component;

    public LDAPServerBrowserWizardPanel2(WSDLWizardContext context) {
        super(context);
    }
    
    public LDAPServerBrowserWizardPanel2() {
        this(null);
    }    
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new LDAPServerBrowserVisualPanel2();
        }
        return component;
    }
    
    public String getName() {
        return component.getName();
    }
    
    public boolean isFinishPanel() {
        return false;
    }
    
   // public String validate(){
       // String ret=component.validateInput();
        //return ret;
    //}

    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
    // If you have context help:
    // return new HelpCtx(SampleWizardPanel1.class);
    }

    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        return true;
    // If it depends on some condition (form filled out...), then:
    // return someCondition();
    // and when this condition changes (last form field filled in...) then:
    // fireChangeEvent();
    // and uncomment the complicated stuff below.
    }

//    public final void addChangeListener(ChangeListener l) {
//    }
//
//    public final void removeChangeListener(ChangeListener l) {
//    }
    /*
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
     */

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(Object settings) {
        WizardDescriptor wd = (WizardDescriptor) settings;
        component.read(wd);        
    }

    public void storeSettings(Object settings) {
        WizardDescriptor wd = (WizardDescriptor) settings;
        component.store(wd);
    }
    
    /**
     * Sets the ldap server mode flag
     * @param mode
     */
    public void setLDAPServerMode(boolean mode) {
        component.setLDAPServerMode(mode);
    }    
}

