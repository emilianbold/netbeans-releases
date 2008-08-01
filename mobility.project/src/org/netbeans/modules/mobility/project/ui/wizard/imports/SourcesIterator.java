/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;
import org.netbeans.modules.mobility.project.J2MEProjectGenerator;
import org.netbeans.modules.mobility.project.ui.wizard.PlatformInstallPanel;
import org.netbeans.modules.mobility.project.ui.wizard.PlatformSelectionPanel;
import org.netbeans.modules.mobility.project.ui.wizard.ProjectPanel;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/**
 *
 * @author  David Kaspar
 */
public class SourcesIterator implements TemplateWizard.Iterator {
    
    private static final long serialVersionUID = 4519734756983L;
    
    boolean platformInstall;
    int currentIndex;
    PlatformInstallPanel.WizardPanel platformPanel;
    SourcesPanel.WizardPanel sourcesPanel;
    ProjectPanel.WizardPanel projectPanel;
    PlatformSelectionPanel psPanel;
    
    static Object create() {
        return new SourcesIterator();
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
                case 1: return sourcesPanel;
                case 2: return projectPanel;
                case 3: return psPanel;
            }
        } else {
            switch (currentIndex) {
                case 0: return sourcesPanel;
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
        platformInstall = PlatformInstallPanel.isPlatformInstalled(J2MEPlatform.SPECIFICATION_NAME) ^ true;
        if (platformInstall){
            platformPanel = new PlatformInstallPanel.WizardPanel(J2MEPlatform.SPECIFICATION_NAME);
            ((JComponent)platformPanel.getComponent()).putClientProperty("NewProjectWizard_Title", NbBundle.getMessage(SourcesIterator.class, "TXT_ExistingImport"));
        }
        sourcesPanel = new SourcesPanel.WizardPanel();
        ((JComponent)sourcesPanel.getComponent()).putClientProperty("NewProjectWizard_Title", NbBundle.getMessage(SourcesIterator.class, "TXT_ExistingImport"));
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
        sourcesPanel = null;
        projectPanel = null;
        psPanel = null;
        currentIndex = -1;
    }
    
    public java.util.Set<DataObject> instantiate(final org.openide.loaders.TemplateWizard templateWizard) throws java.io.IOException {
        final String sources = (String) templateWizard.getProperty(SourcesPanel.SOURCES_LOCATION);
        final String jadLocation = (String) templateWizard.getProperty(SourcesPanel.JAD_LOCATION);
        final File projectLocation = (File) templateWizard.getProperty(ProjectPanel.PROJECT_LOCATION);
        final String name = (String) templateWizard.getProperty(ProjectPanel.PROJECT_NAME);
        PlatformSelectionPanel.PlatformDescription platform = (PlatformSelectionPanel.PlatformDescription) templateWizard.getProperty(PlatformSelectionPanel.PLATFORM_DESCRIPTION);
        if (platform == null) {
            psPanel.readSettings(templateWizard);
            psPanel.storeSettings(templateWizard);
            platform = (PlatformSelectionPanel.PlatformDescription) templateWizard.getProperty(PlatformSelectionPanel.PLATFORM_DESCRIPTION);
        }
        
        final AntProjectHelper helper = J2MEProjectGenerator.createProjectFromSources(projectLocation, name, platform, sources, jadLocation);
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
                NbBundle.getMessage(SourcesPanel.class, "TITLE_Sources"), // NOI18N
                NbBundle.getMessage(ProjectPanel.class, "TITLE_Project"), // NOI18N
                NbBundle.getMessage(PlatformSelectionPanel.class, "TITLE_PlatformSelection"), // NOI18N
            };
        } else {
            list = new String[] {
                NbBundle.getMessage(SourcesPanel.class, "TITLE_Sources"), // NOI18N
                NbBundle.getMessage(ProjectPanel.class, "TITLE_Project"), // NOI18N
                NbBundle.getMessage(PlatformSelectionPanel.class, "TITLE_PlatformSelection"), // NOI18N
            };
        }
        component.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, list); // NOI18N
        component.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(currentIndex)); // NOI18N
    }
    
}
