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
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.ant.freeform.FreeformProjectGenerator;
import org.netbeans.modules.ant.freeform.Util;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author  David Konecny
 */
public class TargetMappingWizardPanel implements WizardDescriptor.Panel {

    private TargetMappingPanel component;
    private WizardDescriptor wizardDescriptor;
    private String projectType;
    
    public TargetMappingWizardPanel(String projectType) {
        this.projectType = projectType;
    }
    
    public Component getComponent() {
        if (component == null) {
            component = new TargetMappingPanel(projectType);
            ((JComponent)component).getAccessibleContext ().setAccessibleDescription (NbBundle.getMessage(TargetMappingWizardPanel.class, "ACSD_TargetMappingWizardPanel")); // NOI18N
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
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
        wizardDescriptor.putProperty("NewProjectWizard_Title", component.getClientProperty("NewProjectWizard_Title")); //NOI18N
        File f = (File)wizardDescriptor.getProperty(NewJ2SEFreeformProjectWizardIterator.PROP_ANT_SCRIPT);
        FileObject fo = FileUtil.toFileObject(f);
        // Util.getAntScriptTargetNames can return null when script is 
        // invalid but first panel checks script validity so it is OK here.
        List l = Util.getAntScriptTargetNames(fo);
        component.setTargetNames(l, true);
        File projDir = (File)wizardDescriptor.getProperty(NewJ2SEFreeformProjectWizardIterator.PROP_PROJECT_FOLDER);
        File antScript = (File)wizardDescriptor.getProperty(NewJ2SEFreeformProjectWizardIterator.PROP_ANT_SCRIPT);
        if (!antScript.getParentFile().equals(projDir) && antScript.getName().equals("build.xml")) { // NOI18N
            // NON-DEFAULT location of build file
            component.setScript("${"+FreeformProjectGenerator.PROP_ANT_SCRIPT+"}");
        }
    }
    
    public void storeSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor)settings;        
        wizardDescriptor.putProperty(NewJ2SEFreeformProjectWizardIterator.PROP_TARGET_MAPPINGS, component.getMapping());
        wizardDescriptor.putProperty("NewProjectWizard_Title", null); //NOI18N
    }
    
}
