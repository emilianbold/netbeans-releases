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

package org.netbeans.modules.java.freeform.ui;

import java.awt.Component;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.util.NbBundle;

/**
 * @author  David Konecny
 */
public class ClasspathWizardPanel implements WizardDescriptor.Panel {

    private ClasspathPanel component;
    private WizardDescriptor wizardDescriptor;

    public ClasspathWizardPanel() {
        getComponent().setName(NbBundle.getMessage (NewJ2SEFreeformProjectWizardIterator.class, "TXT_NewJ2SEFreeformProjectWizardIterator_Classpath"));
    }
    
    public Component getComponent() {
        if (component == null) {
            component = new ClasspathPanel();
            ((JComponent)component).getAccessibleContext ().setAccessibleDescription (NbBundle.getMessage(ClasspathWizardPanel.class, "ACSD_ClasspathWizardPanel")); // NOI18N
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx( ClasspathWizardPanel.class );
    }
    
    public boolean isValid() {
        getComponent();
        return true;
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
        wizardDescriptor = (WizardDescriptor)settings;
        ProjectModel pm = (ProjectModel) wizardDescriptor.getProperty(NewJ2SEFreeformProjectWizardIterator.PROP_PROJECT_MODEL);
        component.setModel(pm);
        wizardDescriptor.putProperty("NewProjectWizard_Title", component.getClientProperty("NewProjectWizard_Title")); // NOI18N
    }
    
    public void storeSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor)settings;
        wizardDescriptor.putProperty("NewProjectWizard_Title", null); // NOI18N
    }
    
}
