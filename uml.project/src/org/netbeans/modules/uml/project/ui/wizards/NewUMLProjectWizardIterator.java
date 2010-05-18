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

package org.netbeans.modules.uml.project.ui.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.support.ProjectChooser;

import org.netbeans.modules.uml.project.UMLProject;
import org.netbeans.modules.uml.project.UMLProjectGenerator;
import org.netbeans.modules.uml.project.ui.UMLProjectSettings;
import org.netbeans.modules.uml.project.ui.common.JavaSourceRootsUI;


/**
 * Wizard to create a new UML project.
 */
public class NewUMLProjectWizardIterator 
    implements WizardDescriptor.InstantiatingIterator 
{
    private static final long serialVersionUID = 1L;
    
    public static final int TYPE_UML = 0;  // platform independent
    public static final int TYPE_UML_JAVA = 1; // Java platform
    public static final int TYPE_REVERSE_ENGINEER = 2; // reverse engineer Java project
//    public static final int TYPE_ROSE_IMPORT = 3; // import Rose model
    
    public static final String PROP_WIZARD_TYPE = "uml-wizard-type"; //NOI18N
    public static final String PROP_NAME_INDEX = "nameIndex"; //NOI18N
    public static final String PROP_PROJECT_NAME = "name"; //NOI18N
    public static final String PROP_PROJECT_DIR = "projdir"; //NOI18N
    public static final String PROP_WIZARD_TITLE = "NewProjectWizard_Title"; //NOI18N
    public static final String PROP_WIZARD_ERROR_MESSAGE = WizardDescriptor.PROP_ERROR_MESSAGE; //NOI18N
    public static final String PROP_SET_AS_MAIN = "setAsMain"; //NOI18N
    public static final String PROP_JAVA_SOURCE_PROJECT = "javaSrcProject"; //NOI18N
    public static final String PROP_JAVA_SOURCE_ROOTS_MODEL= "javaSourceRootsModel"; //NOI18N
//    public static final String PROP_ROSE_FILE= "roseFile"; //NOI18N
    public static final String PROP_MODELING_MODE = "modelingMode"; //NOI18N
    public static final String PROP_PROJECT_MODEL_TYPE = "projectModelType"; // NOI18N

    private int type;
    private File projectDir;
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    private boolean hideJavaProjectPanel = true;

    public NewUMLProjectWizardIterator() 
    {
        this(TYPE_UML);
    }
    
    public NewUMLProjectWizardIterator(int type) 
    {
        this.type = type;
    }
    
    public static NewUMLProjectWizardIterator umlJava() 
    {
        return new NewUMLProjectWizardIterator(TYPE_UML_JAVA);
    }
    
    public static NewUMLProjectWizardIterator reverseEngineer() 
    {
        return new NewUMLProjectWizardIterator(TYPE_REVERSE_ENGINEER);
    }
    
//    public static NewUMLProjectWizardIterator roseImport() 
//    {
//        return new NewUMLProjectWizardIterator( TYPE_ROSE_IMPORT );
//    }
    
    private WizardDescriptor.Panel[] createPanels () 
    {
        // UML Analysis project
        if (this.type == TYPE_UML) 
        {
            return new WizardDescriptor.Panel[] 
            {
                new PanelConfigureProject(this.type)
            };       
        }
        
        // UML Java Project
        else if (this.type == TYPE_UML_JAVA) 
        {
            return new WizardDescriptor.Panel[] 
            {
                new PanelConfigureProject(this.type)
                // new PanelAssociateJavaProject(this.type)            
            };       
        }
        
        else 
        {
            return new WizardDescriptor.Panel[] 
            {
                new PanelConfigureProject(this.type)
            };
        }         
    }
    
    private String[] createSteps()
    {
        // UML Analysis project
        if (this.type == TYPE_UML)
        {
            return new String[]
            {
                NbBundle.getMessage(NewUMLProjectWizardIterator.class,
                    "LAB_ConfigureProject")  //NOI18N
            };
        }
        
        //UML Java Project
        else if(this.type == TYPE_UML_JAVA)
        {
            return new String[] 
            {
                NbBundle.getMessage(NewUMLProjectWizardIterator.class,
                    "LAB_ConfigureProject")  //NOI18N
//                NbBundle.getMessage(NewUMLProjectWizardIterator.class,
//                    "LAB_AssociateJavaProject")  //NOI18N
            };
        }

        else
        {
            return new String[] 
            {
                NbBundle.getMessage(NewUMLProjectWizardIterator.class,
                    "LAB_ConfigureProject"),  //NOI18N
            };
        }
    }
    
    
    public Set/*<FileObject>*/ instantiate() throws IOException 
    {
        Set resultSet = new HashSet ();
        
        File dirF = (File)wiz.getProperty(
                NewUMLProjectWizardIterator.PROP_PROJECT_DIR);   
        
        if (dirF != null) 
        {
            dirF = FileUtil.normalizeFile(dirF);
            projectDir = dirF;
        }
        
        String name = (String)wiz.getProperty(
            NewUMLProjectWizardIterator.PROP_PROJECT_NAME);     

        AntProjectHelper helper = null;
        
        // MCF - we need slightly different handling for the basic UML project
        // depending on the mode      
        if (this.type == TYPE_UML || this.type == TYPE_UML_JAVA) 
        {
           String modelingMode = null ; 
//                (String)wiz.getProperty(NewUMLProjectWizardIterator.PROP_MODELING_MODE);
            
            if (this.type == TYPE_UML_JAVA)
               modelingMode = UMLProject.PROJECT_MODE_IMPL_STR ;
            else if (this.type == TYPE_UML)
               modelingMode = UMLProject.PROJECT_MODE_DESIGN_STR ;
            else
                modelingMode = UMLProject.PROJECT_MODE_ANALYSIS_STR ;
            
            // If the user chose the Mode Impl path, then we actually treat this
            // like a reverse enginneering case.
            if (modelingMode.equalsIgnoreCase(UMLProject.PROJECT_MODE_IMPL_STR)) 
            {
                Project srcProj = (Project)wiz.getProperty(
                    NewUMLProjectWizardIterator.PROP_JAVA_SOURCE_PROJECT);
            
                JavaSourceRootsUI.JavaSourceRootsModel rootsModel =
                    (JavaSourceRootsUI.JavaSourceRootsModel)
                
                wiz.getProperty(
                    NewUMLProjectWizardIterator.PROP_JAVA_SOURCE_ROOTS_MODEL);   
                
                helper = UMLProjectGenerator.createRevEngProject(
                    dirF, name, srcProj, rootsModel, null, type);               
            }
            
            else 
            {
                helper = UMLProjectGenerator.createEmptyProject(
                    dirF, name, modelingMode, null, new String[0], type);                  
            }
        }

        else if (this.type == TYPE_REVERSE_ENGINEER) 
        {
            Project srcProj = (Project)wiz.getProperty(
                NewUMLProjectWizardIterator.PROP_JAVA_SOURCE_PROJECT);
            
            JavaSourceRootsUI.JavaSourceRootsModel rootsModel =
                (JavaSourceRootsUI.JavaSourceRootsModel)wiz.getProperty(
                NewUMLProjectWizardIterator.PROP_JAVA_SOURCE_ROOTS_MODEL);
            
            helper = UMLProjectGenerator.createRevEngProject(
                dirF, name, srcProj, rootsModel, null, type);
        }

//        else if (this.type == TYPE_ROSE_IMPORT)    
//        {
//            File roseFile = (File)wiz.getProperty(PROP_ROSE_FILE);
//            
//            String modelingMode = (String)
//                wiz.getProperty(NewUMLProjectWizardIterator.PROP_MODELING_MODE);
//           
//            helper = UMLProjectGenerator.createRoseImportProject(
//                dirF, name, roseFile, modelingMode);
//        }

        FileObject dir = FileUtil.toFileObject(dirF);
        Project prj = ProjectManager.getDefault().findProject(dir);

        // Returning FileObject of project diretory. 
        // Project will be open and set as main
        Integer index = (Integer)wiz.getProperty(PROP_NAME_INDEX);
        
        // MCF 
	// This project count stuff is intended to help automatically increment
	// the default project name that appears in wizard.
        UMLProjectSettings.getDefault().setNewProjectCount(index.intValue());
             
        resultSet.add (dir);
        dirF = (dirF != null) ? dirF.getParentFile() : null;
        
        if (dirF != null && dirF.exists()) 
            ProjectChooser.setProjectsFolder (dirF);    
                        
        return resultSet;
    }
    
    public void initialize(WizardDescriptor wiz) 
    {
        this.wiz = wiz;
        index = 0;
        panels = createPanels();
        
        // Make sure list of steps is accurate.
        String[] steps = createSteps();
        
        for (int i = 0; i < panels.length; i++) 
        {
            Component c = panels[i].getComponent();
        
            if (steps[i] == null) 
            {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            
            if (c instanceof JComponent) 
            { 
                // assume Swing components
                JComponent jc = (JComponent)c;
                // Step #.
                jc.putClientProperty(
                    WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i)); // NOI18N
                
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
            }
        }
    }

    public void uninitialize(WizardDescriptor wiz) 
    {
        // why bother with this if we set wiz to null?
        this.wiz.putProperty(PROP_PROJECT_DIR,null);
        this.wiz.putProperty(PROP_PROJECT_NAME,null);
        this.wiz = null;
        panels = null;
    }
    
    public File getProjectDir()
    {
        return projectDir;
    }
    
	
    public String name() 
    {
        return MessageFormat.format(NbBundle.getMessage(
            NewUMLProjectWizardIterator.class,"LAB_IteratorName"), // NO18N
            new Object[] {new Integer(index + 1), new Integer(panels.length)});
    }
    
    public boolean hasNext() 
    {
        PanelConfigureProject configPanel = (PanelConfigureProject)panels[0];
        
        if( this.type == TYPE_UML_JAVA && 
            (!configPanel.isImplementationModeSelected())) 
        {
            return index < panels.length - 2;
        }
        
        else 
            return index < panels.length - 1;       
    }

    private boolean hideJavaProjectPanel()
    {
        return hideJavaProjectPanel;
    }
    
    public void hideJavaProjectPanel(boolean hide)
    {
        hideJavaProjectPanel = hide;
    }
    
    public boolean hasPrevious()
    {
        return index > 0;
    }
    
    public void nextPanel()
    {
        if (!hasNext()) throw new NoSuchElementException();
        index++;
    }
    
    public void previousPanel()
    {
        if (!hasPrevious()) throw new NoSuchElementException();
        index--;
    }
    
    public WizardDescriptor.Panel current()
    {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l){}
    public final void removeChangeListener(ChangeListener l){}
    
}
