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
 * TargetPanel.java
 *
 * Created on February 6, 2004, 2:54 PM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import org.openide.util.HelpCtx;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ResourceUtils;

import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;

/**
 *
 * @author  nityad
 */
public class TargetPanel extends ResourceWizardPanel implements WizardConstants{
    
    private org.openide.WizardDescriptor.Panel panel;
    private ResourceConfigHelper helper;
    
    /** Creates a new instance of TargetPanel */
    public TargetPanel(ResourceConfigHelper helper) {
        this.helper = helper;
    }
    
    public void setPanel(org.openide.WizardDescriptor.Panel panel) {
        this.panel = panel;
    }
    
    public TargetPanel getPanel() {
        return this;
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
       return panel.getComponent();
    }
    
    public HelpCtx getHelp() {
      return new HelpCtx("AS_Wiz_Target"); //NOI18N
    }
     
    public boolean isValid() {
        try{
            //Fix for bug# 5025573 - Check for invalid file names
            Component comp[] = ((JPanel)getComponent()).getComponents();
            JPanel pane = (JPanel)comp[0];
            Component paneComp[] = pane.getComponents();
            String targetName = ((javax.swing.JTextField)paneComp[1]).getText();
            if (targetName != null && targetName.length() != 0 && (! targetName.equals("<default name>"))){
               if (! ResourceUtils.isFriendlyFilename(targetName)) 
                    return false;
            }
        }catch(Exception ex){
        }    
        return panel.isValid();
    }
    
    public synchronized void addChangeListener(ChangeListener listener) {
        panel.addChangeListener(listener);
    }

    public synchronized void removeChangeListener(ChangeListener listener) {
        panel.removeChangeListener(listener);
    }

    // You can use a settings object to keep track of state.155
    // Normally the settings object will be the WizardDescriptor,
    // so you can use WizardDescriptor.getProperty & putProperty
    // to store information entered by the user.
    public void readSettings(Object settings) {
       panel.readSettings(settings);
    }
    
    public void storeSettings(Object settings) {
        panel.storeSettings(settings);
        
    }
}
