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
import java.util.ArrayList;
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
    private List targetNames;
    
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
        return new HelpCtx( TargetMappingWizardPanel.class );
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
        wizardDescriptor.putProperty("NewProjectWizard_Title", component.getClientProperty("NewProjectWizard_Title")); // NOI18N
        File f = (File)wizardDescriptor.getProperty(NewJ2SEFreeformProjectWizardIterator.PROP_ANT_SCRIPT);
        FileObject fo = FileUtil.toFileObject(f);
        // Util.getAntScriptTargetNames can return null when script is 
        // invalid but first panel checks script validity so it is OK here.
        List l = Util.getAntScriptTargetNames(fo);
        // #47784 - update panel only once or when Ant script has changed
        if (targetNames == null || !targetNames.equals(l)) {
            targetNames = new ArrayList(l);
            component.setTargetNames(l, true);
        }
        ProjectModel pm = (ProjectModel) wizardDescriptor.getProperty(NewJ2SEFreeformProjectWizardIterator.PROP_PROJECT_MODEL);
        File projDir = pm.getNBProjectFolder();//(File)wizardDescriptor.getProperty(NewJ2SEFreeformProjectWizardIterator.PROP_PROJECT_FOLDER);
        File antScript = (File)wizardDescriptor.getProperty(NewJ2SEFreeformProjectWizardIterator.PROP_ANT_SCRIPT);
        if (!(antScript.getParentFile().equals(projDir) && antScript.getName().equals("build.xml"))) { // NOI18N
            // NON-DEFAULT location of build file
            component.setScript("${"+FreeformProjectGenerator.PROP_ANT_SCRIPT+"}"); // NOI18N
        } else {
            component.setScript(null);
        }
    }
    
    public void storeSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor)settings;        
        wizardDescriptor.putProperty(NewJ2SEFreeformProjectWizardIterator.PROP_TARGET_MAPPINGS, component.getMapping());
        wizardDescriptor.putProperty("NewProjectWizard_Title", null); // NOI18N
    }
    
}
