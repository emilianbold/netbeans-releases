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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.vmd.componentssupport.ui.wizard;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.ui.wizard.spi.ModuleTypePanel;
import org.netbeans.modules.vmd.componentssupport.ui.helpers.BaseHelper;
import org.netbeans.modules.vmd.componentssupport.ui.helpers.CustomComponentHelper;
import org.netbeans.modules.vmd.componentssupport.ui.helpers.JavaMELibsConfigurationHelper;
import org.netbeans.modules.vmd.componentssupport.ui.helpers.ProjectTemplateZipHelper;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public class CustomComponentWizardIterator implements
        WizardDescriptor./* Progress */InstantiatingIterator
{

    // wizard properties
    public static final String WIZARD_PANEL_ERROR_MESSAGE 
                                            = WizardDescriptor.PROP_ERROR_MESSAGE;        // NOI18N
    private static final String LBL_WIZARD_STEPS_COUNT 
                                            = "LBL_WizardStepsCount";            // NOI18N
    public static final String CONTENT_DATA = WizardDescriptor.PROP_CONTENT_DATA;         // NOI18N
    public static final String SELECTED_INDEX 
                                            = WizardDescriptor.PROP_CONTENT_SELECTED_INDEX;// NOI18N

    public static final String BUNDLE_PROPERTIES
                                            = BaseHelper.BUNDLE_PROPERTIES;
    public static final String LAYER_XML    = BaseHelper.LAYER_XML;
    // steps
    public static final String STEP_BASIC_PARAMS 
                                            = "LBL_BasicProjectParamsStep";      // NOI18N
    public static final String LBL_LIBRARIES 
                                            = "LBL_LibrariesDescStep";           // NOI18N 
    public static final String LBL_COMPONENT_DESC 
                                            = "LBL_ComponentsDescStep";          // NOI18N
    public static final String FINAL_STEP   = "LBL_FinalStep";                   // NOI18N

    // properties
    public static final String PROJECT_DIR  = "projDir";                         // NOI18N
    public static final String PROJECT_NAME = "projName";                        // NOI18N
    public static final String SET_AS_MAIN  = "setAsMain";                       // NOI18N
    public static final String LAYER_PATH   = "layer";                           // NOI18N
    public static final String BUNDLE_PATH  = "bundle";                          // NOI18N
    public static final String CODE_BASE_NAME
                                            = "codeBaseName";                    // NOI18N
    public static final String DISPLAY_NAME = "displayName";                     // NOI18N

    // added library descriptors
    public static final String LIBRARIES    = "libraries";                       // NOI18N
    public static final String LIB_DISPLAY_NAMES
                                            = "libDisplayNames";                 // NOI18N
    public static final String LIB_NAMES    = "libNames";                        // NOI18N
    // added Custom components
    public static final String CUSTOM_COMPONENTS  
                                            = "customComponents";                // NOI18N
    
    private static final String TEMPLATE_PROJECT_NETBEANSORG 
                                = "CustomComponentProject_netbeansorg.zip";     //NOI18N
    private static final String TEMPLATE_PROJECT_STANDALONE 
                                = "CustomComponentProject_standalone.zip";      //NOI18N
    private static final String TEMPLATE_PROJECT_SUITECOMPONENT 
                                = "CustomComponentProject_suitecomponent.zip";  //NOI18N

    private CustomComponentWizardIterator() {
    }

    public static CustomComponentWizardIterator createIterator() {
        return new CustomComponentWizardIterator();
    }

    WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] { 
              new CustomComponentWizardPanel(), 
              new BasicModuleConfWizardPanel(),
              new JavaMELibsWizardPanel(),
              new DescriptorsWizardPanel()
        };
    }

    private String[] createSteps() {
        return new String[] { 
                NbBundle.getMessage(
                        CustomComponentWizardIterator.class, STEP_BASIC_PARAMS) ,
                NbBundle.getMessage(
                        CustomComponentWizardIterator.class, FINAL_STEP),
                NbBundle.getMessage(
                        CustomComponentWizardIterator.class, LBL_LIBRARIES),
                NbBundle.getMessage(
                        CustomComponentWizardIterator.class, LBL_COMPONENT_DESC)
                        };
    }

    // TODO add all created elements into result set.
    public Set/* <FileObject> */instantiate(/* ProgressHandle handle */)
            throws IOException
    {
        Set<FileObject> resultSet = new LinkedHashSet<FileObject>();
        File dirF = FileUtil.normalizeFile((File) myWizard
                .getProperty(PROJECT_DIR));
        dirF.mkdirs();

        FileObject template = getProjectTemplate(myWizard);
        FileObject dir = FileUtil.toFileObject(dirF);
        
        ProjectTemplateZipHelper.
                unZipFile(template.getInputStream(), dir , myWizard );

        
        // Always open top dir as a project:
        resultSet.add(dir);
        // Look for nested projects to open as well:
        Enumeration<? extends FileObject> e = dir.getFolders(true);
        while (e.hasMoreElements()) {
            FileObject subfolder = e.nextElement();
            if (ProjectManager.getDefault().isProject(subfolder)) {
                resultSet.add(subfolder);
            }
        }

        File parent = dirF.getParentFile();
        if (parent != null && parent.exists()) {
            ProjectChooser.setProjectsFolder(parent);
        }
        
        ProjectManager.getDefault().clearNonProjectCache();
        Project createdProject = FileOwnerQuery.getOwner(dir);
        assert createdProject != null : "crated project is null";
        // store ME Libraries
        JavaMELibsConfigurationHelper
                .configureJavaMELibs(createdProject, myWizard);
        // store custom component descriptors
        configureComponents(createdProject, myWizard);
        return resultSet;
    }
    
    private FileObject getProjectTemplate(WizardDescriptor wizard){
        if (BaseHelper.isNetBeansOrg(wizard)){
            return BaseHelper.getTemplate(TEMPLATE_PROJECT_NETBEANSORG);
        } else if (BaseHelper.isSuiteComponent(wizard)){
            return BaseHelper.getTemplate(TEMPLATE_PROJECT_SUITECOMPONENT);
        } else if (BaseHelper.isStandalone(wizard)){
            return BaseHelper.getTemplate(TEMPLATE_PROJECT_STANDALONE);
        }
        throw new IllegalArgumentException("unsupported wizard type");
    }

    public void initialize( WizardDescriptor wiz ) {
        myWizard = wiz;
        index = 0;
        panels = createPanels();
        // Make sure list of steps is accurate.
        String[] steps = createSteps();
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components

                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(SELECTED_INDEX, new Integer(i));
                // Step name (actually the whole list for reference).
                jc.putClientProperty(CONTENT_DATA, steps);
            }
        }
    }

    public void uninitialize( WizardDescriptor wiz ) {
        wiz.putProperty(PROJECT_DIR, null);
        wiz.putProperty(PROJECT_NAME, null);
        wiz.putProperty(LIBRARIES, null );
        wiz.putProperty(LIB_NAMES, null);
        wiz.putProperty(LIB_DISPLAY_NAMES, null);
        wiz.putProperty(CUSTOM_COMPONENTS, null);
        wiz = null;
        panels = null;
    }

    public String name() {
        return MessageFormat.format(NbBundle.getBundle(
                CustomComponentWizardIterator.class).getString(
                LBL_WIZARD_STEPS_COUNT), new Object[] {
                new Integer(index + 1) + "", new Integer(panels.length) + "" });
    }

    public boolean hasNext() {
        return index < panels.length - 1;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener( ChangeListener l ) {
    }

    public final void removeChangeListener( ChangeListener l ) {
    }
    
    private static Set<FileObject> configureComponents(Project project, WizardDescriptor wizard)
            throws IOException 
    {
            List<Map<String, Object>> components = 
                    (List<Map<String, Object>>) wizard.getProperty(
                    CustomComponentWizardIterator.CUSTOM_COMPONENTS);
            if (components == null){
                return Collections.EMPTY_SET;
            }
            
            Set<FileObject> resultSet = new LinkedHashSet<FileObject>();
            for(Map<String, Object> component : components){
                CustomComponentHelper helper = new CustomComponentHelper.
                        RealInstantiationHelper(project, component);
                resultSet.addAll( helper.instantiate() );
            }
            
            return Collections.EMPTY_SET;
    }
    
    
    private int index;
    private WizardDescriptor.Panel[] panels;
    private WizardDescriptor myWizard;
}
