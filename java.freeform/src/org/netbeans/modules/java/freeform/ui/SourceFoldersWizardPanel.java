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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.modules.ant.freeform.spi.ProjectConstants;
import org.netbeans.modules.ant.freeform.spi.support.NewFreeformProjectSupport;
import org.netbeans.modules.java.freeform.JavaProjectGenerator;
import org.netbeans.modules.java.freeform.JavaProjectNature;
import org.netbeans.modules.java.freeform.spi.support.NewJavaFreeformProjectSupport;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.util.NbBundle;

/**
 * @author  David Konecny
 */
public class SourceFoldersWizardPanel implements WizardDescriptor.Panel, ChangeListener, WizardDescriptor.FinishablePanel {

    private SourceFoldersPanel component;
    private WizardDescriptor wizardDescriptor;

    public SourceFoldersWizardPanel() {
        getComponent().setName(NbBundle.getMessage (NewJ2SEFreeformProjectWizardIterator.class, "TXT_NewJ2SEFreeformProjectWizardIterator_SourcePackageFolders"));
    }
    
    public Component getComponent() {
        if (component == null) {
            component = new SourceFoldersPanel();
            component.setChangeListener(this);
            ((JComponent)component).getAccessibleContext ().setAccessibleDescription (NbBundle.getMessage(SourceFoldersWizardPanel.class, "ACSD_SourceFoldersWizardPanel")); // NOI18N            
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx( SourceFoldersWizardPanel.class );
    }
    
    public boolean isValid() {
        getComponent();
        // Panel is valid without any source folder specified, but
        // Next button is enabled only when there is some soruce 
        // folder specified -> see NewJ2SEFreeformProjectWizardIterator
        // which enables/disables Next button
        wizardDescriptor.putProperty("WizardPanel_errorMessage", ""); // NOI18N
        return true;
    }

    public boolean isFinishPanel() {
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
        File projectLocation = (File)wizardDescriptor.getProperty(NewFreeformProjectSupport.PROP_PROJECT_LOCATION);
        File projectFolder = (File)wizardDescriptor.getProperty(NewFreeformProjectSupport.PROP_PROJECT_FOLDER);
        PropertyEvaluator evaluator = PropertyUtils.sequentialPropertyEvaluator(null, new PropertyProvider[]{
            PropertyUtils.fixedPropertyProvider(
            Collections.singletonMap(ProjectConstants.PROP_PROJECT_LOCATION, projectLocation.getAbsolutePath()))});

        ProjectModel pm = (ProjectModel)wizardDescriptor.getProperty(NewJ2SEFreeformProjectWizardIterator.PROP_PROJECT_MODEL);
        if (pm == null ||
                !pm.getBaseFolder().equals(projectLocation) ||
                !pm.getNBProjectFolder().equals(projectFolder)) {
            pm = ProjectModel.createEmptyModel(projectLocation, projectFolder, evaluator);
            wizardDescriptor.putProperty(NewJ2SEFreeformProjectWizardIterator.PROP_PROJECT_MODEL, pm);
        }
        List l = (List)wizardDescriptor.getProperty(NewJavaFreeformProjectSupport.PROP_EXTRA_JAVA_SOURCE_FOLDERS);
        if (l != null) {
            Iterator it = l.iterator();
            while (it.hasNext()) {
                String path = (String)it.next();
                assert it.hasNext();
                String label = (String)it.next();
                // try to find if the model already contains this source folder
                boolean found = false;
                for (int i = 0; i < pm.getSourceFoldersCount(); i++) {
                    JavaProjectGenerator.SourceFolder existingSf = pm.getSourceFolder(i);
                    if (existingSf.location.equals(path)) {
                        found = true;
                        break;
                    }
                }
                // don't add the folder if it is already in the model
                if (!found) {
                    JavaProjectGenerator.SourceFolder sf = new JavaProjectGenerator.SourceFolder();
                    sf.location = path;
                    sf.label = label;
                    sf.type = JavaProjectConstants.SOURCES_TYPE_JAVA;
                    sf.style = JavaProjectNature.STYLE_PACKAGES;
                    pm.addSourceFolder(sf, false);
                }
            }
        }
        
        wizardDescriptor.putProperty("NewProjectWizard_Title", component.getClientProperty("NewProjectWizard_Title")); // NOI18N
        component.setModel(pm, null);
    }
    
    public void storeSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor)settings;
        wizardDescriptor.putProperty("NewProjectWizard_Title", null); // NOI18N
    }
    
    public void stateChanged(ChangeEvent e) {
        fireChangeEvent();
    }
    
}
