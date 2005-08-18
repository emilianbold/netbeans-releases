/*
 * JSFConfigurationPanel.java
 *
 * Created on August 10, 2005, 6:55 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.web.jsf.wizards;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.web.spi.webmodule.FrameworkConfigurationPanel;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

/**
 *
 * @author petr
 */
public class JSFConfigurationPanel implements FrameworkConfigurationPanel, WizardDescriptor.FinishablePanel, WizardDescriptor.ValidatingPanel {
    private WizardDescriptor wizardDescriptor;
    private JSFConfigurationPanelVisual component;

    /** Creates a new instance of JSFConfigurationPanel */
    public JSFConfigurationPanel(boolean customizer) {
        this.customizer = customizer;
        getComponent();
    }
    
    private boolean customizer;
    
    
    
    public boolean isFinishPanel() {
        return true;
    }

    public Component getComponent() {
        if (component == null)
            component = new JSFConfigurationPanelVisual(this, customizer);

        return component;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx(JSFConfigurationPanel .class);
    }
    
    public boolean isValid() {
        getComponent();
        return component.valid(wizardDescriptor);
    }
    
    private final Set/*<ChangeListener>*/ listeners = new HashSet(1);
    
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
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }
    
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        component.read (wizardDescriptor);
        
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewProjectWizard to modify the title
        Object substitute = ((JComponent) component).getClientProperty("NewProjectWizard_Title"); // NOI18N
        if (substitute != null)
            wizardDescriptor.putProperty("NewProjectWizard_Title", substitute); // NOI18N
    }
    
   
    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor) settings;
        component.store(d);
        ((WizardDescriptor) d).putProperty("NewProjectWizard_Title", null); // NOI18N
    }

    public void validate() throws WizardValidationException {
        getComponent ();
        component.validate (wizardDescriptor);
    }

    public void enableComponents(boolean enable) {
        
        
    }

    public String getServletName(){
        return component.getServletName();
    }
    
    public void setServletName(String name){
        component.setServletName(name);
    }
    
    public String getURLPattern(){
        return component.getURLPattern();
    }
    
    public void setURLPattern(String pattern){
        component.setURLPattern(pattern);
    }
    
    public boolean validateXML(){
        return component.validateXML();
    }
    
    public void setValidateXML(boolean ver){
        component.setValidateXML(ver);
    }
    
    public boolean verifyObjects(){
        return component.verifyObjects();
    }
    
    public void setVerifyObjects(boolean val){
        component.setVerifyObjects(val);
    }
    
    public boolean packageJars(){
        return component.packageJars();
    }
    
}
