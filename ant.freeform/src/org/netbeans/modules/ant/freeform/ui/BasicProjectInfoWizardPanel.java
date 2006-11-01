/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ant.freeform.ui;

import java.awt.Component;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.ant.freeform.spi.support.NewFreeformProjectSupport;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author  David Konecny
 */
public class BasicProjectInfoWizardPanel implements WizardDescriptor.Panel, ChangeListener {

    private BasicProjectInfoPanel component;
    private WizardDescriptor wizardDescriptor;
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);

    public BasicProjectInfoWizardPanel() {
        getComponent().setName(NbBundle.getMessage(BasicProjectInfoWizardPanel.class, "WizardPanel_NameAndLocation"));
    }
    
    public Component getComponent() {
        if (component == null) {
            component = new BasicProjectInfoPanel("", "", "", "", this); // NOI18N
            component.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(BasicProjectInfoWizardPanel.class, "ACSD_BasicProjectInfoWizardPanel")); // NOI18N
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx( BasicProjectInfoWizardPanel.class );
    }
    
    public boolean isValid() {
        getComponent();
        String error = component.getError();
        if (error != null) {
            wizardDescriptor.putProperty( "WizardPanel_errorMessage", error); // NOI18N
            return false;
        }
        wizardDescriptor.putProperty( "WizardPanel_errorMessage", ""); // NOI18N
        return true;
    }
    
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
    
    final void fireChangeEvent() {
        Set<ChangeListener> ls;
        synchronized (listeners) {
            ls = new HashSet<ChangeListener>(listeners);
        }
        ChangeEvent ev = new ChangeEvent(this);
        for (ChangeListener l : ls) {
            l.stateChanged(ev);
        }
    }
    
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor)settings;        
        wizardDescriptor.putProperty("NewProjectWizard_Title", component.getClientProperty("NewProjectWizard_Title")); // NOI18N
    }
    
    public void storeSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor)settings;        
        wizardDescriptor.putProperty(NewFreeformProjectSupport.PROP_ANT_SCRIPT, component.getAntScript());
        wizardDescriptor.putProperty(NewFreeformProjectSupport.PROP_PROJECT_NAME, component.getProjectName());
        wizardDescriptor.putProperty(NewFreeformProjectSupport.PROP_PROJECT_LOCATION, component.getProjectLocation());
        wizardDescriptor.putProperty(NewFreeformProjectSupport.PROP_PROJECT_FOLDER, component.getProjectFolder());
        wizardDescriptor.putProperty("NewProjectWizard_Title", null); // NOI18N
        wizardDescriptor.putProperty("setAsMain", component.getMainProject()); // NOI18N
    }
    
    public void stateChanged(ChangeEvent e) {
        fireChangeEvent();
    }
    
}
