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
 * CommonGeneralFinishPanel.java
 *
 * Created on October 10, 2002
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.loaders.TemplateWizard;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ResourceUtils;

import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroup;
import org.netbeans.modules.j2ee.sun.sunresources.beans.Wizard;
import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;
import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroupHelper;

/** A single panel descriptor for a wizard.
 * You probably want to make a wizard iterator to hold it.
 *
 * @author  shirleyc
 */
public class CommonGeneralFinishPanel implements WizardDescriptor.FinishablePanel, WizardConstants {
    protected ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.j2ee.sun.ide.sunresources.wizards.Bundle"); //NOI18N

    
    /** The visual component that displays this panel.
     * If you need to access the component from this class,
     * just use getComponent().
     */
    private CommonGeneralFinishVisualPanel component;
    private ResourceConfigHelper helper;    
    private Wizard wizardInfo;
    private String[] groupNames;
    
    /** Create the wizard panel descriptor. */
    public CommonGeneralFinishPanel(ResourceConfigHelper helper, Wizard wizardInfo, String[] groupNames) {
        this.helper = helper;
        this.wizardInfo = wizardInfo;
        this.groupNames = groupNames;
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
                if (wizardInfo.getName().equals(__MailResource)) {
                    panelType = CommonGeneralFinishVisualPanel.TYPE_MAIL_RESOUCE;
                }
                component = new CommonGeneralFinishVisualPanel(this, groups, panelType);
        }
        return component;
    }
    
    public boolean createNew() {
        if (component == null)
            return false;
        else
            return component.createNew();
    }
    
    public String getResourceName() {
        return this.wizardInfo.getName();
    }
    
    public HelpCtx getHelp() {
        if (wizardInfo.getName().equals(__MailResource)) {
            return new HelpCtx("AS_Wiz_Mail_general"); //NOI18N
        }else{
            return HelpCtx.DEFAULT_HELP;
        }
        
    }
    
    public ResourceConfigHelper getHelper() {
        return helper;
    }
    
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        if (component != null && component.jLabels != null && component.jFields != null) {
            int i;
            for (i=0; i < component.jLabels.length; i++) {
                String jLabel = (String)component.jLabels[i].getText();
                if (jLabel.equals(bundle.getString("LBL_" + __JndiName))) { //NOI18N
                    String jndiName = (String)((JTextField)component.jFields[i]).getText();
                    if (jndiName == null || jndiName.length() == 0) {
                        return false;
                    }else if(! ResourceUtils.isLegalResourceName(jndiName)){
                        return false;
                    }    
                }
                if (wizardInfo.getName().equals(__MailResource)) {
                    if (jLabel.equals(bundle.getString("LBL_" + __Host))) { // NO18N
                        String host = (String)((JTextField)component.jFields[i]).getText();
                        if (host == null || host.length() == 0) {
                            return false;
                        }
                    }
                    if (jLabel.equals(bundle.getString("LBL_" + __MailUser))) { // NO18N
                        String user = (String)((JTextField)component.jFields[i]).getText();
                        if (user == null || user.length() == 0) {
                            return false;
                        }
                    }
                    if (jLabel.equals(bundle.getString("LBL_" + __From))) { //NOI18N
                        String from = (String)((JTextField)component.jFields[i]).getText();
                        if (from == null || from.length() == 0) {
                            return false;
                        }
                    }
                } //Validity checks applicable to only Mail Resource Wizard
            }//for
        }
        return true;
        // If it depends on some condition (form filled out...), then:
        // return someCondition ();
        // and when this condition changes (last form field filled in...) then:
        // fireChangeEvent ();
        // and uncomment the complicated stuff below.
    }
  
    public boolean isFinishPanel() {
        return isValid();
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
        if (wizardInfo.getName().equals(__MailResource)) {
            TemplateWizard wizard = (TemplateWizard)settings;
            String targetName = wizard.getTargetName();
            targetName = ResourceUtils.createUniqueFileName(targetName, ResourceUtils.setUpExists(this.helper.getData().getTargetFileObject()), __MAILResource);
            this.helper.getData().setTargetFile(targetName);
            if(component == null)
                getComponent();
            component.setHelper(this.helper);
        }
    }
    public void storeSettings(Object settings) {
    }
    
    public void initData() {
        this.component.initData();
    }
}

