/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.mobility.project.ui.wizard;

import java.io.File;
import java.util.Collections;
import java.util.NoSuchElementException;
import javax.swing.JComponent;
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;
import org.netbeans.modules.mobility.project.J2MEProjectGenerator;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/**
 *
 */
public class SampleProjectIterator implements TemplateWizard.Iterator {
    
    private static final long serialVersionUID = 4589834546983L;
    
    boolean platformInstall;
    int currentIndex;
    PlatformInstallPanel.WizardPanel platformPanel;
    ProjectPanel.WizardPanel projectPanel;
    PlatformSelectionPanel psPanel;
    
    static Object create() {
        return new SampleProjectIterator();
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
                case 1: return projectPanel;
                case 2: return psPanel;
            }
        } else {
            switch (currentIndex) {
                case 0: return projectPanel;
                case 1: return psPanel;
            }
        }
        throw new IllegalStateException();
    }
    
    public boolean hasNext() {
        if (platformInstall) {
            return currentIndex < 2;
        }
        return currentIndex < 1;
    }
    
    public boolean hasPrevious() {
        return currentIndex > 0;
    }
    
    public void initialize(final org.openide.loaders.TemplateWizard templateWizard) {
        platformInstall = PlatformInstallPanel.isPlatformInstalled(J2MEPlatform.SPECIFICATION_NAME) ^ true;
        if (platformInstall){
            platformPanel = new PlatformInstallPanel.WizardPanel(J2MEPlatform.SPECIFICATION_NAME);
            ((JComponent)platformPanel.getComponent()).putClientProperty("NewProjectWizard_Title", NbBundle.getMessage(NewProjectIterator.class, "TXT_SampleProject"));
        }
        projectPanel = new ProjectPanel.WizardPanel(false);
        ((JComponent)projectPanel.getComponent()).putClientProperty("NewProjectWizard_Title", NbBundle.getMessage(NewProjectIterator.class, "TXT_SampleProject"));
        psPanel = new PlatformSelectionPanel();
        String configuration = null;
        String profile = null;
        final DataObject dao = templateWizard.getTemplate();
        if (dao != null) {
            final FileObject fo = dao.getPrimaryFile();
            if (fo != null) {
                configuration = (String) fo.getAttribute("MicroEdition-Configuration"); // NOI18N
                profile = (String) fo.getAttribute("MicroEdition-Profile"); // NOI18N
                templateWizard.putProperty(ProjectPanel.PROJECT_NAME, fo.getName()+'1');
            }
        }
        templateWizard.putProperty(PlatformSelectionPanel.REQUIRED_CONFIGURATION, configuration);
        templateWizard.putProperty(PlatformSelectionPanel.REQUIRED_PROFILE, profile);
        templateWizard.putProperty(PlatformSelectionPanel.PLATFORM_DESCRIPTION, null);
        currentIndex = 0;
        updateStepsList();
    }
    
    public void uninitialize(@SuppressWarnings("unused")
	final org.openide.loaders.TemplateWizard templateWizard) {
        platformPanel = null;
        projectPanel = null;
        psPanel = null;
        currentIndex = -1;
    }
    
    public java.util.Set <DataObject> instantiate(final org.openide.loaders.TemplateWizard templateWizard) throws java.io.IOException {
        final File projectLocation = (File) templateWizard.getProperty(ProjectPanel.PROJECT_LOCATION);
        final String name = (String) templateWizard.getProperty(ProjectPanel.PROJECT_NAME);
        PlatformSelectionPanel.PlatformDescription platform = (PlatformSelectionPanel.PlatformDescription) templateWizard.getProperty(PlatformSelectionPanel.PLATFORM_DESCRIPTION);
        if (platform == null) {
            psPanel.readSettings(templateWizard);
            psPanel.storeSettings(templateWizard);
            platform = (PlatformSelectionPanel.PlatformDescription) templateWizard.getProperty(PlatformSelectionPanel.PLATFORM_DESCRIPTION);
        }
        
        final AntProjectHelper helper = J2MEProjectGenerator.createProjectFromTemplate(templateWizard.getTemplate().getPrimaryFile(), projectLocation, name, platform);
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
                NbBundle.getMessage(ProjectPanel.class, "TITLE_Project"), // NOI18N
                NbBundle.getMessage(PlatformSelectionPanel.class, "TITLE_PlatformSelection"), // NOI18N
            };
        } else {
            list = new String[] {
                NbBundle.getMessage(ProjectPanel.class, "TITLE_Project"), // NOI18N
                NbBundle.getMessage(PlatformSelectionPanel.class, "TITLE_PlatformSelection"), // NOI18N
            };
        }
        component.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, list); // NOI18N
        component.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(currentIndex)); // NOI18N
    }
    
}
