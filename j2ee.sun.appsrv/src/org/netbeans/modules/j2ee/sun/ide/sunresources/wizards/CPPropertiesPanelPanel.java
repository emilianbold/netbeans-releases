/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * CPPropertiesPanelPanel.java
 *
 * Created on October 8, 2003
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import java.awt.Component;
import javax.swing.event.ChangeListener;
import java.util.Vector;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;
import org.netbeans.modules.j2ee.sun.sunresources.beans.Wizard;
import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;

/** A single panel descriptor for a wizard.
 * You probably want to make a wizard iterator to hold it.
 *
 * @author nityad
 */
public class CPPropertiesPanelPanel extends ResourceWizardPanel implements WizardConstants, WizardDescriptor.FinishPanel  {
    
    /** The visual component that displays this panel.
     * If you need to access the component from this class,
     * just use getComponent().
     */
    private CPPropertiesPanelVisualPanel component;
    private ResourceConfigHelper helper;
    private Wizard wizard;
    
    /** Create the wizard panel descriptor. */
    public CPPropertiesPanelPanel(ResourceConfigHelper helper, Wizard wizard) {
        this.helper = helper;
        this.wizard = wizard;
    }
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new CPPropertiesPanelVisualPanel(this, this.helper, this.wizard);
        }
        return component;
    }
    
    public void refreshFields(){
        if(component != null){
            component.refreshFields();
            component.setInitialFocus();
        }    
    }
     
    public HelpCtx getHelp() {
        return  new HelpCtx("AS_Wiz_ConnPool_props"); //NOI18N
    }
    
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        ResourceConfigData data = helper.getData();
        /*if((data.get(__DatasourceClassname) == null) || (data.get(__DatasourceClassname).toString().trim().equals("")) )
            return false;*/
        
        Vector vec = data.getProperties(); 
        for (int i = 0; i < vec.size(); i++) {
            NameValuePair pair = (NameValuePair)vec.elementAt(i);
            //why was this there at all?
            /*if (pair.getParamName() !=  null && pair.getParamName().equals(WizardConstants.__Password)) {
                continue;
            }*/
            
            if (pair.getParamName() == null || pair.getParamValue() == null ||
                    pair.getParamName().length() == 0 || pair.getParamValue().length() == 0){
                return false;
            }  
        }
        return true;
        // If it depends on some condition (form filled out...), then:
        // return someCondition();
        // and when this condition changes (last form field filled in...) then:
        // fireChangeEvent();
        // and uncomment the complicated stuff below.
    }
    
    protected final void fireChangeEvent (Object source) {
       super.fireChange(this);
    }
    
    /*
    private final Set listeners = new HashSet(1); // Set<ChangeListener>
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
     */
    
    // You can use a settings object to keep track of state.
    // Normally the settings object will be the WizardDescriptor,
    // so you can use WizardDescriptor.getProperty & putProperty
    // to store information entered by the user.
    public void readSettings(Object settings) {
    }
    public void storeSettings(Object settings) {
    }
    
    public boolean isFinishPanel() {
       return isValid();
    }
}
