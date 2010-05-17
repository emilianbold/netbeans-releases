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

package org.netbeans.modules.uml.project.ui.customizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.ComboBoxModel;
import javax.swing.table.DefaultTableModel;

import org.netbeans.modules.uml.project.UMLProject;
import org.netbeans.modules.uml.project.UMLProjectGenerator;
import org.netbeans.modules.uml.project.UMLProjectHelper;
import org.netbeans.modules.uml.project.ui.common.CommonUiSupport;
import org.netbeans.modules.uml.project.ui.common.JavaSourceRootsUI.JavaSourceRootsModel;
import org.netbeans.modules.uml.project.ui.common.ReferencedJavaProjectModel;
import org.netbeans.modules.uml.project.ui.common.ReferencedJavaProjectSupport;

import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.Project;
import org.netbeans.modules.uml.util.StringTokenizer2;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.support.ant.ui.StoreGroup;

import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 * @author Mike Frisino
 */
public class UMLProjectProperties 
{
    // Special properties of the project, stored in project.xml by project.save
    
    // Properties stored in the PROJECT.PROPERTIES
    public static final String MODELING_MODE = "uml.modeling.mode"; // NOI18N
    public static final String UML_PROJECT_ANT_ARTIFACT = "uml.umlproject"; // NOI18N
    public static final String ANT_ARTIFACT_PREFIX = "${reference."; // NOI18N
    public static final String REFERENCED_JAVA_PROJECT = "uml.javaproject"; // NOI18N
    public static final String REFERENCED_JAVA_PROJECT_ARTIFACTS = "uml.javaproject.artifacts"; // NOI18N
    public static final String REFERENCED_JAVA_PROJECT_SRC = "uml.javaproject.src"; // NOI18N
    public static final String UML_PROJECT_IMPORTS = "uml.imports"; // NOI18N
    public static final String UML_ARTIFACT_PREFIX = "${uml.reference."; // NOI18N
    public static final String CODE_GEN_FOLDER_LOCATION = "code.gen.folder.location"; // NOI18N
    public static final String CODE_GEN_TEMPLATES = "code.gen.templates"; // NOI18N
    public static final String CODE_GEN_BACKUP_SOURCES = "code.gen.backup.sources"; // NOI18N
    public static final String CODE_GEN_USE_MARKERS = "code.gen.use.markers"; // NOI18N
    public static final String CODE_GEN_ADD_MARKERS = "code.gen.add.markers"; // NOI18N
    public static final String CODE_GEN_SHOW_DIALOG = "code.gen.show.dialog"; // NOI18N
    
    public static final String DEFAULT_JAVA_TEMPLATES = 
        "Java:Basic Class|Java:Basic Interface|Java:Basic Enumeration"; // NOI18N
        
    // MODELS FOR VISUAL CONTROLS
    
    // CustomizerSources
    public ComboBoxModel modelingModeModel;
    public String modelingModeValue;
    public ReferencedJavaProjectModel referencedJavaProjectModel;
    public JavaSourceRootsModel referencedJavaSourceRootsModel;
    public DefaultTableModel umlProjectImportsModel;
    public String codeGenTemplates;
    public String codeGenFolderLocation;
    public String codeGenBackupSources;
    public String codeGenUseMarkers;
    public String codeGenAddMarkers;
    public String codeGenShowDialog;
    
    UMLImportsSupport importsSupport;
    
    // CustomizerRunTest
    
    // Private fields ---------------------------------------------------------
    private UMLProject project;
    private UMLProjectHelper updateHelper;
    private PropertyEvaluator evaluator;
    private ReferenceHelper refHelper;
    private ReferencedJavaProjectSupport javaRefSupport;
    
    private StoreGroup privateGroup;
    private StoreGroup projectGroup;
    
    public UMLProject getProject()
    {
        return project;
    }
    
    /** Creates a new instance of UMLProjectProperties and initializes them */
    public UMLProjectProperties(
        UMLProject project,
        UMLProjectHelper updateHelper,
        PropertyEvaluator evaluator,
        ReferenceHelper refHelper
        )
    {
        this.project = project;
        this.updateHelper  = updateHelper;
        this.evaluator = evaluator;
        this.refHelper = refHelper;
        this.javaRefSupport = new ReferencedJavaProjectSupport(
            evaluator, refHelper, updateHelper.getAntProjectHelper(),
            null, ANT_ARTIFACT_PREFIX );
        
        this.importsSupport =
            new UMLImportsSupport( evaluator, refHelper,
            updateHelper.getAntProjectHelper(),
            UML_ARTIFACT_PREFIX);
        
        privateGroup = new StoreGroup();
        projectGroup = new StoreGroup();
        
        init(); // Load known properties
    }
    
    /** Initializes the visual models
     */
    public void init()
    {
        // Customizer Modeling
        // fetch the stored value
        // SELECTED_MODELING_MODE_MODEL = projectGroup.createStringDocument(
        //  evaluator, MODELING_MODE );
        // construct model with stored value as selected item.
        
        EditableProperties projectProperties = updateHelper.getProperties(
            AntProjectHelper.PROJECT_PROPERTIES_PATH );
        
        modelingModeModel = CommonUiSupport.createModelingModeComboBoxModel(
            evaluator.getProperty(MODELING_MODE));
        
        referencedJavaProjectModel = javaRefSupport.createReferencedJavaProjectModel(
            REFERENCED_JAVA_PROJECT, evaluator.getProperty(REFERENCED_JAVA_PROJECT));
        
        referencedJavaSourceRootsModel = javaRefSupport.
            createReferencedJavaSourceRootsModel(
            referencedJavaProjectModel,
            (String) projectProperties.get(REFERENCED_JAVA_PROJECT_SRC));
        
        umlProjectImportsModel = UMLImportsUiSupport.createTableModel(
            importsSupport.itemsIterator(
            (String)projectProperties.get(UML_PROJECT_IMPORTS)));
        
        codeGenFolderLocation = projectProperties.get(CODE_GEN_FOLDER_LOCATION);
        codeGenTemplates = projectProperties.get(CODE_GEN_TEMPLATES);
        codeGenBackupSources = projectProperties.get(CODE_GEN_BACKUP_SOURCES);
        codeGenUseMarkers = projectProperties.get(CODE_GEN_USE_MARKERS);
        codeGenAddMarkers = projectProperties.get(CODE_GEN_ADD_MARKERS);
        codeGenShowDialog = projectProperties.get(CODE_GEN_SHOW_DIALOG);
    }
    
    public void save()
    {
        try
        {
            // Store properties
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction()
            {
                public Object run() throws IOException
                {
                    storeProperties();
                    return null;
                }
            });
            
            // and save the project
            ProjectManager.getDefault().saveProject(project);
            
            // MCF - I am not sure if this bad practice. But I want to
            // avoid having to recreate props in my AssociatedSoruceProvider
            // which will be called a lot.
            // So I am trying to cache these properties in the project.
            project.setUMLProjectProperties(this);
        }
        
        catch (MutexException e)
        {
            ErrorManager.getDefault().notify((IOException)e.getException());
        }
        
        catch (IOException ex)
        {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    private void storeProperties() throws IOException
    {
        // Store special properties
        
        // Modify the project dependencies properly
        resolveProjectDependencies();
        
        String[] umlImports = importsSupport.encodeToStrings(
            UMLImportsUiSupport.getIterator(umlProjectImportsModel));
        
        
        String[] refJavaSrcRoots = javaRefSupport
            .encodeSrcGroupsToStrings(referencedJavaSourceRootsModel);
        
        // Store standard properties
        EditableProperties projectProperties = updateHelper.getProperties(
            AntProjectHelper.PROJECT_PROPERTIES_PATH);
        
        EditableProperties privateProperties = updateHelper.getProperties(
            AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        
        
        // Standard store of the properties
        projectGroup.store(projectProperties);
        privateGroup.store(privateProperties);
        
        projectProperties.setProperty(MODELING_MODE, getProjectMode());
        
        projectProperties.setProperty(
            REFERENCED_JAVA_PROJECT_SRC, refJavaSrcRoots);
        
        if (codeGenFolderLocation != null)
            projectProperties.setProperty(CODE_GEN_FOLDER_LOCATION, codeGenFolderLocation);

        if (codeGenTemplates != null)
            projectProperties.setProperty(CODE_GEN_TEMPLATES, codeGenTemplates);

        if (codeGenBackupSources != null)
            projectProperties.setProperty(CODE_GEN_BACKUP_SOURCES, codeGenBackupSources);
        
        if (codeGenUseMarkers != null)
            projectProperties.setProperty(CODE_GEN_USE_MARKERS, codeGenUseMarkers);

        if (codeGenAddMarkers != null)
            projectProperties.setProperty(CODE_GEN_ADD_MARKERS, codeGenAddMarkers);

        if (codeGenShowDialog != null)
            projectProperties.setProperty(CODE_GEN_SHOW_DIALOG, codeGenShowDialog);
        
        projectProperties.setProperty(UML_PROJECT_IMPORTS, umlImports);
       
        // Store the property changes into the project
        updateHelper.putProperties(
            AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties );
        
        updateHelper.putProperties(
            AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProperties );
		
        UMLProjectGenerator.fixJavaProjectReferences(
            updateHelper.getAntProjectHelper(),referencedJavaProjectModel.getProject());
    }

    /** Finds out what are new and removed project dependencies and
     * applyes the info to the project
     */
    private void resolveProjectDependencies()
    {
        
        AntProjectHelper h = updateHelper.getAntProjectHelper();
        
        EditableProperties ep = updateHelper.getProperties(
            AntProjectHelper.PROJECT_PROPERTIES_PATH);
        
        String oldJavaArtifactRefStr =
            (String)ep.get(UMLProjectProperties.REFERENCED_JAVA_PROJECT_ARTIFACTS);
        
        if (oldJavaArtifactRefStr != null && oldJavaArtifactRefStr.length() > 0)
        {
            
            String[] oldJavaArtifactRefs = new String[]{oldJavaArtifactRefStr};
            
            for (int i=0; i < oldJavaArtifactRefs.length; i++)
            {
                // Debug.out.println("MCF destroy reference "  // NOI18N
                //	+ oldJavaArtifactRefs[i]);
                refHelper.destroyReference(oldJavaArtifactRefs[i]);
            }
        }
        
        
        // got to get fresh ep, after refHelper modified
        ep = updateHelper.getProperties(
            AntProjectHelper.PROJECT_PROPERTIES_PATH);
        
        // now add the new ref
        // refHelper.addReference(artifact, uri);
        Project javaSrcProj = referencedJavaProjectModel.getProject();
        
        if (javaSrcProj != null )
        {
            String[] refStrs = UMLProjectGenerator
                .addJavaSrcProjRef(h, refHelper, javaSrcProj);
            ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            ep.setProperty(UMLProjectProperties
                .REFERENCED_JAVA_PROJECT_ARTIFACTS, refStrs);
            h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        }
        
        // Create a set of old and new artifacts.
        Set oldArtifacts = new HashSet();
        Set newArtifacts = new HashSet();
        
        // Create set of removed artifacts and remove them
        Set removed = new HashSet( oldArtifacts );
        removed.removeAll( newArtifacts );
        Set added = new HashSet(newArtifacts);
        added.removeAll(oldArtifacts);

        // 2. now read project.properties and modify rest
        ep = updateHelper.getProperties(
            AntProjectHelper.PROJECT_PROPERTIES_PATH);
        
        boolean changed = false;
        String javaSrcProjRefVal = ""; // NOI18N
        
        if (javaSrcProj != null)
        {
            javaSrcProjRefVal =
                UMLProjectGenerator.getJavaSrcProjRefVal(h, javaSrcProj);
            
            changed = true;
        }
        
        ep.setProperty(
            UMLProjectProperties.REFERENCED_JAVA_PROJECT, javaSrcProjRefVal);
        
        if (changed)
        {
            updateHelper.putProperties(
                AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        }
    }
    
    // TODO - MCF - I kept this method from copied J2SE code, thinking we might
    // need it or something like it. I have not yet actually used it, and if we
    // do not use it we can delete it.
    /**
     * Tokenize library classpath and try to relativize all the jars.
     * @param property library property name ala "libs.someLib.classpath"
     * @param projectDir project dir for relativization
     * @return relativized library classpath or null if some jar is not collocated
     */
    private String relativizeLibraryClasspath(String property, File projectDir)
    {
        String value = PropertyUtils.getGlobalProperties().getProperty(property);
        // bugfix #42852, check if the classpath
        // property is set, otherwise return null
        if (value == null)
        {
            return null;
        }
        
        String[] paths = PropertyUtils.tokenizePath(value);
        StringBuffer sb = new StringBuffer();
        
        if (sb.length() == 0)
            return null;
        
        else
            return sb.toString();
    }
    
    
    public static String getAntPropertyName( String property )
    {
        if (property != null &&
            property.startsWith( "${") && // NOI18N
            property.endsWith( "}")) // NOI18N
        {
            return property.substring( 2, property.length() - 1 );
        }
        
        else
            return property;
    }
    
    
    public String getProjectMode()
    {
        return evaluator.getProperty(MODELING_MODE);
    }
    
    public String getCurrentProjectMode()
    {
        return evaluator.getProperty(MODELING_MODE);
    }

    
    public String getCodeGenTemplates()
    {
        String templates = evaluator.getProperty(CODE_GEN_TEMPLATES);
        
        if (templates == null)
            templates = "";
        
        return templates;
    }

    public void setCodeGenFolderLocation(String val)
    {
        codeGenFolderLocation = val;
    }
    

    public String getCodeGenFolderLocation()
    {
        String val = evaluator.getProperty(CODE_GEN_FOLDER_LOCATION);
        
        if (val == null)
            val = "";
        
        return val;
    }
    
    public final static String TEMPLATE_DELIMITER = "|";
    
    public List<String> getCodeGenTemplatesArray()
    {
        return new ArrayList<String>(Arrays.asList(StringTokenizer2.toArray(
            getCodeGenTemplates(), TEMPLATE_DELIMITER))); // NOI18N
    }

    public void setCodeGenTemplates(List<String> val)
    {
        codeGenTemplates = 
            StringTokenizer2.delimitedString(val.toArray(), TEMPLATE_DELIMITER);
    }

    public boolean isCodeGenBackupSources()
    {
        String val = evaluator.getProperty(CODE_GEN_BACKUP_SOURCES);
        
        if (val == null)
            val = "true"; // NOI18N
        
        return Boolean.valueOf(val);
    }

    public void setCodeGenBackupSources(boolean val)
    {
        codeGenBackupSources = String.valueOf(val);
    }

    public boolean isCodeGenUseMarkers()
    {
        String val = evaluator.getProperty(CODE_GEN_USE_MARKERS);
        
        if (val == null)
            val = "true";
        
        return Boolean.valueOf(val);
    }

    public void setCodeGenUseMarkers(boolean val)
    {
        codeGenUseMarkers = String.valueOf(val);
    }

    public boolean isCodeGenAddMarkers()
    {
        String val = evaluator.getProperty(CODE_GEN_ADD_MARKERS);
        
        if (val == null)
            val = "false";
        
        return Boolean.valueOf(val);
    }

    public void setCodeGenAddMarkers(boolean val)
    {
        codeGenAddMarkers = String.valueOf(val);
    }

    public boolean isCodeGenShowDialog()
    {
        String val = evaluator.getProperty(CODE_GEN_SHOW_DIALOG);
        
        if (val == null)
            val = "true";
        
        return Boolean.valueOf(val);
    }

    public void setCodeGenShowDialog(boolean val)
    {
        codeGenShowDialog = String.valueOf(val);
    }

    
    public File getJavaSourceRootFolder()
    {
        return 
            (referencedJavaSourceRootsModel == null ||
             referencedJavaSourceRootsModel.getSourceGroups().length == 0 ||
             referencedJavaSourceRootsModel.getSourceGroup(0) == null)
             ? null
             : FileUtil.toFile(referencedJavaSourceRootsModel.
                getSourceGroup(0).getRootFolder());
    }
}
