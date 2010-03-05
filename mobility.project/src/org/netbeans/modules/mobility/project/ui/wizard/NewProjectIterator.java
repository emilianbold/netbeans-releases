/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.mobility.project.ui.wizard;

import java.io.File;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;
import org.netbeans.modules.mobility.project.J2MEProjectGenerator;
import org.netbeans.spi.mobility.cfgfactory.ProjectConfigurationFactory.ConfigurationTemplateDescriptor;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/**
 *
 * @author  David Kaspar
 */
public class NewProjectIterator implements TemplateWizard.Iterator {
    
    private static final long serialVersionUID = 4589834546983L;

    private static final String IS_LIBRARY = "is_library";//NOI18N
    boolean platformInstall;
    int currentIndex;
    PlatformInstallPanel.WizardPanel platformPanel;
    ProjectPanel.WizardPanel projectPanel;
    PlatformSelectionPanel psPanel;
    ConfigurationsSelectionPanel csPanel;
    
    static Object create() {
        return new NewProjectIterator();
    }

    static {
        ClassPreloader.start(); //#147403 - preload some classes 
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
                case 3: return csPanel;
            }
        } else {
            switch (currentIndex) {
                case 0: return projectPanel;
                case 1: return psPanel;
                case 2: return csPanel;
            }
        }
        throw new IllegalStateException();
    }
    
    public boolean hasNext() {
        if (platformInstall) {
            return currentIndex < 3;
        }
        return currentIndex < 2;
    }
    
    public boolean hasPrevious() {
        return currentIndex > 0;
    }
    
    public void initialize(final org.openide.loaders.TemplateWizard templateWizard) {
        boolean create = true;
        if (!(Templates.getTemplate(templateWizard).getAttribute("application") instanceof Boolean)) // NOI18N
            create = false;
        
        platformInstall =  PlatformInstallPanel.isPlatformInstalled(J2MEPlatform.SPECIFICATION_NAME) ^ true;
        if (platformInstall){
            platformPanel = new PlatformInstallPanel.WizardPanel(J2MEPlatform.SPECIFICATION_NAME);
            ((JComponent)platformPanel.getComponent()).putClientProperty("NewProjectWizard_Title", create ? NbBundle.getMessage(NewProjectIterator.class, "TXT_MobileApplication") : NbBundle.getMessage(NewProjectIterator.class, "TXT_MobileLibrary"));
        }
        projectPanel = new ProjectPanel.WizardPanel(create, create);
        ((JComponent)projectPanel.getComponent()).putClientProperty("NewProjectWizard_Title", create ? NbBundle.getMessage(NewProjectIterator.class, "TXT_MobileApplication") : NbBundle.getMessage(NewProjectIterator.class, "TXT_MobileLibrary"));
        
        psPanel = new PlatformSelectionPanel();
        csPanel = new ConfigurationsSelectionPanel();
        templateWizard.putProperty(PlatformSelectionPanel.REQUIRED_CONFIGURATION, null);
        templateWizard.putProperty(PlatformSelectionPanel.REQUIRED_PROFILE, null);
        templateWizard.putProperty(PlatformSelectionPanel.PLATFORM_DESCRIPTION, null);
        templateWizard.putProperty(ConfigurationsSelectionPanel.CONFIGURATION_TEMPLATES, null);
        templateWizard.putProperty(IS_LIBRARY, !create);
        final DataObject dao = templateWizard.getTemplate();
        templateWizard.putProperty(ProjectPanel.PROJECT_NAME, dao != null ? dao.getPrimaryFile().getName()+'1' : null);
        currentIndex = 0;
        updateStepsList();
    }
    
    public void uninitialize(@SuppressWarnings("unused")
	final org.openide.loaders.TemplateWizard templateWizard) {
        platformPanel = null;
        projectPanel = null;
        psPanel = null;
        csPanel = null;
        currentIndex = -1;
    }
    
    public java.util.Set<DataObject> instantiate(final org.openide.loaders.TemplateWizard templateWizard) throws java.io.IOException {
        final File projectLocation = (File) templateWizard.getProperty(ProjectPanel.PROJECT_LOCATION);
        final String name = (String) templateWizard.getProperty(ProjectPanel.PROJECT_NAME);
        PlatformSelectionPanel.PlatformDescription platform = (PlatformSelectionPanel.PlatformDescription) templateWizard.getProperty(PlatformSelectionPanel.PLATFORM_DESCRIPTION);
        if (platform == null) {
            psPanel.readSettings(templateWizard);
            psPanel.storeSettings(templateWizard);
            platform = (PlatformSelectionPanel.PlatformDescription) templateWizard.getProperty(PlatformSelectionPanel.PLATFORM_DESCRIPTION);
        }
        final Boolean createMIDlet = (Boolean) templateWizard.getProperty(ProjectPanel.PROJECT_CREATE_MIDLET);
        HashSet<DataObject> result = createMIDlet != null && createMIDlet.booleanValue() ? new HashSet<DataObject>() : null;
        
        final AntProjectHelper helper = J2MEProjectGenerator.createNewProject(projectLocation, name, platform, result, (Set<ConfigurationTemplateDescriptor>)templateWizard.getProperty(ConfigurationsSelectionPanel.CONFIGURATION_TEMPLATES), (Boolean)templateWizard.getProperty(IS_LIBRARY));
        if (result == null) result = new HashSet<DataObject>();
        result.add(DataObject.find(helper.getProjectDirectory()));
        return result;
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
                NbBundle.getMessage(PlatformSelectionPanel.class, "TITLE_ConfigurationsSelection"), // NOI18N
            };
        } else {
            list = new String[] {
                NbBundle.getMessage(ProjectPanel.class, "TITLE_Project"), // NOI18N
                NbBundle.getMessage(PlatformSelectionPanel.class, "TITLE_PlatformSelection"), // NOI18N
                NbBundle.getMessage(PlatformSelectionPanel.class, "TITLE_ConfigurationsSelection"), // NOI18N
            };
        }
        component.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, list); // NOI18N
        component.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(currentIndex)); // NOI18N
    }
    
}
