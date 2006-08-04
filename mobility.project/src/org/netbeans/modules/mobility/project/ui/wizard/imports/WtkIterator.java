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

/*
 * Main.java
 *
 * Created on April 6, 2004, 3:39 PM
 */
package org.netbeans.modules.mobility.project.ui.wizard.imports;

import java.io.File;
import java.util.Collections;
import java.util.NoSuchElementException;
import javax.swing.JComponent;
import org.netbeans.modules.mobility.project.J2MEProjectGenerator;
import org.netbeans.modules.mobility.project.ui.wizard.PlatformInstallPanel;
import org.netbeans.modules.mobility.project.ui.wizard.PlatformSelectionPanel;
import org.netbeans.modules.mobility.project.ui.wizard.ProjectPanel;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/**
 *
 * @author  David Kaspar
 */
public class WtkIterator implements TemplateWizard.Iterator {
    
    private static final long serialVersionUID = 4589843546983L;
    
    boolean platformInstall;
    int currentIndex;
    PlatformInstallPanel.WizardPanel platformPanel;
    WtkPanel.WizardPanel wtkPanel;
    ProjectPanel.WizardPanel projectPanel;
    PlatformSelectionPanel psPanel;
    
    static Object create() {
        return new WtkIterator();
    }
    
    public void addChangeListener(@SuppressWarnings("unused")
	final javax.swing.event.ChangeListener changeListener) {
    }
    
    public void removeChangeListener(@SuppressWarnings("unused")
	final javax.swing.event.ChangeListener changeListener) {
    }
    
    public org.openide.WizardDescriptor.Panel current() {
        if (platformInstall) {
            switch (currentIndex) {
                case 0: return platformPanel;
                case 1: return wtkPanel;
                case 2: return projectPanel;
                case 3: return psPanel;
            }
        } else {
            switch (currentIndex) {
                case 0: return wtkPanel;
                case 1: return projectPanel;
                case 2: return psPanel;
            }
        }
        throw new IllegalStateException();
    }
    
    public boolean hasNext() {
        if (platformInstall)
            return currentIndex < 3;
        return currentIndex < 2;
    }
    
    public boolean hasPrevious() {
        return currentIndex > 0;
    }
    
    public void initialize(final org.openide.loaders.TemplateWizard templateWizard) {
        platformInstall = PlatformInstallPanel.isPlatformInstalled() ^ true;
        if (platformInstall)
            platformPanel = new PlatformInstallPanel.WizardPanel();
        wtkPanel = new WtkPanel.WizardPanel();
        projectPanel = new ProjectPanel.WizardPanel(false, true);
        psPanel = new PlatformSelectionPanel();
        templateWizard.putProperty(PlatformSelectionPanel.REQUIRED_CONFIGURATION, null);
        templateWizard.putProperty(PlatformSelectionPanel.REQUIRED_PROFILE, null);
        templateWizard.putProperty(PlatformSelectionPanel.PLATFORM_DESCRIPTION, null);
        currentIndex = 0;
        updateStepsList();
    }
    
    public void uninitialize(@SuppressWarnings("unused")
	final org.openide.loaders.TemplateWizard templateWizard) {
        platformPanel = null;
        wtkPanel = null;
        projectPanel = null;
        psPanel = null;
        currentIndex = -1;
    }
    
    public java.util.Set<DataObject> instantiate(final org.openide.loaders.TemplateWizard templateWizard) throws java.io.IOException {
        final String appLocation = (String) templateWizard.getProperty(WtkPanel.APP_LOCATION);
        final File projectLocation = (File) templateWizard.getProperty(ProjectPanel.PROJECT_LOCATION);
        PlatformSelectionPanel.PlatformDescription platform = (PlatformSelectionPanel.PlatformDescription) templateWizard.getProperty(PlatformSelectionPanel.PLATFORM_DESCRIPTION);
        if (platform == null) {
            psPanel.readSettings(templateWizard);
            psPanel.storeSettings(templateWizard);
            platform = (PlatformSelectionPanel.PlatformDescription) templateWizard.getProperty(PlatformSelectionPanel.PLATFORM_DESCRIPTION);
        }
        final String name = (String) templateWizard.getProperty(ProjectPanel.PROJECT_NAME);
        
        final AntProjectHelper helper = J2MEProjectGenerator.createProjectFromWtkProject(projectLocation, name, platform, appLocation);
        final FileObject projectDir = helper.getProjectDirectory();
        return Collections.singleton(DataObject.find(projectDir));
    }
    
    public String name() {
        return current().getComponent().getName();
    }
    
    public void nextPanel() {
        if (!hasNext())
            throw new NoSuchElementException();
        currentIndex ++;
        updateStepsList();
    }
    
    public void previousPanel() {
        if (!hasPrevious())
            throw new NoSuchElementException();
        currentIndex --;
        updateStepsList();
    }
    
    void updateStepsList() {
        final JComponent component = (JComponent) current().getComponent();
        if (component == null)
            return;
        String[] list;
        if (platformInstall) {
            list = new String[] {
                NbBundle.getMessage(PlatformInstallPanel.class, "TITLE_Platform"), // NOI18N
                NbBundle.getMessage(WtkPanel.class, "TITLE_Wtk"), // NOI18N
                NbBundle.getMessage(ProjectPanel.class, "TITLE_Project"), // NOI18N
                NbBundle.getMessage(PlatformSelectionPanel.class, "TITLE_PlatformSelection"), // NOI18N
            };
        } else {
            list = new String[] {
                NbBundle.getMessage(WtkPanel.class, "TITLE_Wtk"), // NOI18N
                NbBundle.getMessage(ProjectPanel.class, "TITLE_Project"), // NOI18N
                NbBundle.getMessage(PlatformSelectionPanel.class, "TITLE_PlatformSelection"), // NOI18N
            };
        }
        component.putClientProperty("WizardPanel_contentData", list); // NOI18N
        component.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(currentIndex)); // NOI18N
    }
    
}
