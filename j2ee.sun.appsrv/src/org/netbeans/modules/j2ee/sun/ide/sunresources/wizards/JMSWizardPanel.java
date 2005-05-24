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
 * JMSWizardPanel.java
 *
 * Created on November 17, 2003, 12:57 PM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.ResourceBundle;

import org.openide.WizardDescriptor;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ResourceUtils;

import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroup;
import org.netbeans.modules.j2ee.sun.sunresources.beans.Wizard;
import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;
import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroupHelper;

/**
 *
 * @author  nityad
 */
public class JMSWizardPanel implements WizardDescriptor.FinishPanel, WizardConstants{
    protected ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.j2ee.sun.ide.sunresources.wizards.Bundle"); //NOI18N

    
    /** The visual component that displays this panel.
     * If you need to access the component from this class,
     * just use getComponent().
     */
    private JMSWizardVisualPanel component;
    private ResourceConfigHelper helper;    
    private Wizard wizardInfo;
    private String[] groupNames;
    
    /** Creates a new instance of JMSWizardPanel */
    public JMSWizardPanel(ResourceConfigHelper helper, Wizard wizardInfo) {
        this.helper = helper;
        this.wizardInfo = wizardInfo;
        this.groupNames = new String[]{"general"}; //NOI18N
    }
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
                FieldGroup[] groups = new FieldGroup[groupNames.length];
                for (int i = 0; i < this.groupNames.length; i++) {
                    groups[i] = FieldGroupHelper.getFieldGroup(wizardInfo, this.groupNames[i]);  //NOI18N
                }
                String panelType = null;
                component = new JMSWizardVisualPanel(this, groups); 
        }
        return component;
    }
    
    public boolean createNew() {
        if (component == null)
            return false;
        else{
            return true;
            //return component.createNew();
        }    
    }
    
    public HelpCtx getHelp() {
       return new HelpCtx("AS_Wiz_JMS_general"); //NOI18N
    }
    
    public ResourceConfigHelper getHelper() {
        return helper;
    }
    
    public boolean isValid() {
        //Fix for bug# 5025502 & 5025573 - User should not be allowed to register a 
        //JMS Resource without JNDI name or invalid name 
        String jndiName = helper.getData().getString("jndi-name"); //NOI18N
        if(jndiName.trim().length() == 0 || jndiName.trim().equals("")) //NOI18N
            return false;
        else if(! ResourceUtils.isLegalResourceName(jndiName))
            return false;
        else
            return true;
        
    }
  
    private final Set listeners = new HashSet (1); 
    public final void addChangeListener (ChangeListener l) {
        synchronized (listeners) {
            listeners.add (l);
        }
    }
    public final void removeChangeListener (ChangeListener l) {
        synchronized (listeners) {
            listeners.remove (l);
        }
    }
    protected final void fireChangeEvent () {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet (listeners).iterator ();
        }
        ChangeEvent ev = new ChangeEvent (this);
        while (it.hasNext ()) {
            ((ChangeListener) it.next ()).stateChanged (ev);
        }
    }
     
    
    // You can use a settings object to keep track of state.155
    // Normally the settings object will be the WizardDescriptor,
    // so you can use WizardDescriptor.getProperty & putProperty
    // to store information entered by the user.
    public void readSettings(Object settings) {
        TemplateWizard wizard = (TemplateWizard)settings;
        String targetName = wizard.getTargetName();
        targetName = ResourceUtils.createUniqueFileName(targetName, ResourceUtils.setUpExists(this.helper.getData().getTargetFileObject()), __JMSResource);
        this.helper.getData().setTargetFile(targetName);
        if(component == null)
            getComponent();
        component.setHelper(this.helper);
    }
    
    public void storeSettings(Object settings) {
    }
    
}
