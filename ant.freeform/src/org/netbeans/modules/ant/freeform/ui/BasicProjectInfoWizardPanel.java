/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ant.freeform.ui;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import java.util.Collections;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.modules.ant.freeform.FreeformProjectGenerator;
import org.openide.util.NbBundle;

/**
 * @author  David Konecny
 */
public class BasicProjectInfoWizardPanel implements WizardDescriptor.Panel, ChangeListener {

    private BasicProjectInfoPanel component;
    private WizardDescriptor wizardDescriptor;
    private final Set/*<ChangeListener>*/ listeners = new HashSet(1);

    public BasicProjectInfoWizardPanel() {
    }
    
    public Component getComponent() {
        if (component == null) {
            component = new BasicProjectInfoPanel("", "", "", "", this); // NOI18N
            ((JComponent)component).getAccessibleContext ().setAccessibleDescription (NbBundle.getMessage(BasicProjectInfoWizardPanel.class, "ACSD_BasicProjectInfoWizardPanel")); // NOI18N
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
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
        wizardDescriptor = (WizardDescriptor)settings;        
        wizardDescriptor.putProperty("NewProjectWizard_Title", component.getClientProperty("NewProjectWizard_Title")); //NOI18N
    }
    
    public void storeSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor)settings;        
        wizardDescriptor.putProperty(NewJ2SEFreeformProjectWizardIterator.PROP_PROJECT_LOCATION, component.getProjectLocation());
        wizardDescriptor.putProperty(NewJ2SEFreeformProjectWizardIterator.PROP_ANT_SCRIPT, component.getAntScript());
        wizardDescriptor.putProperty(NewJ2SEFreeformProjectWizardIterator.PROP_PROJECT_FOLDER, component.getProjectFolder());
        wizardDescriptor.putProperty(NewJ2SEFreeformProjectWizardIterator.PROP_PROJECT_NAME, component.getProjectName());
        wizardDescriptor.putProperty("NewProjectWizard_Title", null); //NOI18N
        wizardDescriptor.putProperty("setAsMain", component.getMainProject()); // NOI18N

        PropertyEvaluator evaluator = PropertyUtils.sequentialPropertyEvaluator(null, new PropertyProvider[]{
            PropertyUtils.fixedPropertyProvider(
            Collections.singletonMap(FreeformProjectGenerator.PROP_PROJECT_LOCATION, component.getProjectLocation().getAbsolutePath()))});

        wizardDescriptor.putProperty(NewJ2SEFreeformProjectWizardIterator.PROP_EVALUATOR, evaluator); // NOI18N
    }
    
    public void stateChanged(ChangeEvent e) {
        fireChangeEvent();
    }
    
}
