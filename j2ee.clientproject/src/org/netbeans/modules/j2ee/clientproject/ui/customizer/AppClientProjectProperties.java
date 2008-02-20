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

package org.netbeans.modules.j2ee.clientproject.ui.customizer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.j2ee.clientproject.AppClientProject;
import org.netbeans.modules.j2ee.clientproject.AppClientProjectType;
import org.netbeans.modules.j2ee.clientproject.AppClientProjectUtil;
import org.netbeans.modules.j2ee.clientproject.Utils;
import org.netbeans.modules.j2ee.clientproject.classpath.ClassPathSupport;
import org.netbeans.modules.j2ee.deployment.devmodules.api.AntDeploymentHelper;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.ui.PlatformUiSupport;
import org.netbeans.modules.websvc.api.client.WebServicesClientConstants;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.support.ant.ui.StoreGroup;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;

/**
 * @author Petr Hrebejk
 */
public class AppClientProjectProperties {
    
    //Hotfix of the issue #70058
    //Should be removed when the StoreGroup SPI will be extended to allow false default value in ToggleButtonModel
    private static final Integer BOOLEAN_KIND_TF = new Integer( 0 );
    private static final Integer BOOLEAN_KIND_YN = new Integer( 1 );
    private static final Integer BOOLEAN_KIND_ED = new Integer( 2 );
    private Integer javacDebugBooleanKind;
    private Integer javadocPreviewBooleanKind;
    
    public static final String JAVA_EE_5 = "1.5"; // NOI18N
    public static final String J2EE_1_4 = "1.4"; // NOI18N
    public static final String J2EE_1_3 = "1.3"; // NOI18N
    
    // Special properties of the project
    public static final String CAR_PROJECT_NAME = "j2ee.clientproject.name"; // NOI18N
    public static final String JAVA_PLATFORM = "platform.active"; // NOI18N
    public static final String J2EE_PLATFORM = "j2ee.platform"; // NOI18N
    
    public static final String SOURCE_ROOT = "source.root"; // NOI18N
    public static final String SOURCE_ENCODING="source.encoding"; // NOI18N
    public static final String BUILD_FILE = "buildfile"; // NOI18N
    public static final String META_INF = "meta.inf"; // NOI18N
    public static final String SRC_DIR = "src.dir"; // NOI18N
    public static final String LIBRARIES_DIR = "lib.dir"; //NOI18N
    public static final String RESOURCE_DIR = "resource.dir"; // NOI18N
    public static final String DIST_EAR_JAR = "dist.ear.jar"; //NOI18N
    public static final String JAR_NAME = "jar.name"; // NOI18N
    public static final String BUILD_EAR_CLASSES_DIR = "build.ear.classes.dir"; // NOI18N
    public static final String J2EE_SERVER_INSTANCE = "j2ee.server.instance"; // NOI18N
    public static final String J2EE_SERVER_TYPE = "j2ee.server.type"; // NOI18N
    public static final String J2EE_PLATFORM_CLASSPATH = "j2ee.platform.classpath"; //NOI18N
    public static final String J2EE_PLATFORM_SHARED = "j2ee.platform.shared"; //NOI18N
    
    // Properties stored in the PROJECT.PROPERTIES    
    /** root of external web module sources (full path), ".." if the sources are within project folder */
    public static final String DIST_DIR = "dist.dir"; // NOI18N
    public static final String DIST_JAR = "dist.jar"; // NOI18N
    public static final String JAVAC_CLASSPATH = "javac.classpath"; // NOI18N
    public static final String RUN_CLASSPATH = "run.classpath"; // NOI18N
    public static final String RUN_JVM_ARGS = "run.jvmargs"; // NOI18N
    public static final String RUN_WORK_DIR = "work.dir"; // NOI18N
    public static final String DEBUG_CLASSPATH = "debug.classpath"; // NOI18N
    public static final String JAR_COMPRESS = "jar.compress"; // NOI18N
    public static final String MAIN_CLASS = "main.class"; // NOI18N
    public static final String JAVAC_SOURCE = "javac.source"; // NOI18N
    public static final String JAVAC_TARGET = "javac.target"; // NOI18N
    public static final String JAVAC_TEST_CLASSPATH = "javac.test.classpath"; // NOI18N
    public static final String JAVAC_DEBUG = "javac.debug"; // NOI18N
    public static final String JAVAC_DEPRECATION = "javac.deprecation"; // NOI18N
    public static final String JAVAC_COMPILER_ARG = "javac.compilerargs";    //NOI18N
    public static final String RUN_TEST_CLASSPATH = "run.test.classpath"; // NOI18N
    public static final String BUILD_DIR = "build.dir"; // NOI18N
    public static final String BUILD_CLASSES_DIR = "build.classes.dir"; // NOI18N
    public static final String BUILD_TEST_CLASSES_DIR = "build.test.classes.dir"; // NOI18N
    public static final String BUILD_TEST_RESULTS_DIR = "build.test.results.dir"; // NOI18N
    public static final String BUILD_CLASSES_EXCLUDES = "build.classes.excludes"; // NOI18N
    public static final String DIST_JAVADOC_DIR = "dist.javadoc.dir"; // NOI18N
    public static final String NO_DEPENDENCIES="no.dependencies"; // NOI18N
    public static final String DEBUG_TEST_CLASSPATH = "debug.test.classpath"; // NOI18N
    
    
    public static final String JAVADOC_PRIVATE="javadoc.private"; // NOI18N
    public static final String JAVADOC_NO_TREE="javadoc.notree"; // NOI18N
    public static final String JAVADOC_USE="javadoc.use"; // NOI18N
    public static final String JAVADOC_NO_NAVBAR="javadoc.nonavbar"; // NOI18N
    public static final String JAVADOC_NO_INDEX="javadoc.noindex"; // NOI18N
    public static final String JAVADOC_SPLIT_INDEX="javadoc.splitindex"; // NOI18N
    public static final String JAVADOC_AUTHOR="javadoc.author"; // NOI18N
    public static final String JAVADOC_VERSION="javadoc.version"; // NOI18N
    public static final String JAVADOC_WINDOW_TITLE="javadoc.windowtitle"; // NOI18N
    public static final String JAVADOC_ENCODING="javadoc.encoding"; // NOI18N
    public static final String JAVADOC_ADDITIONALPARAM="javadoc.additionalparam"; // NOI18N
    
    public static final String CLIENT_NAME = "j2ee.clientName"; // NOI18N
    
    // Properties stored in the PRIVATE.PROPERTIES
    public static final String APPLICATION_ARGS = "application.args"; // NOI18N
    public static final String JAVADOC_PREVIEW="javadoc.preview"; // NOI18N
    public static final String DEPLOY_ANT_PROPS_FILE = "deploy.ant.properties.file"; //NOI18N
    
    public static final String ANT_DEPLOY_BUILD_SCRIPT = "nbproject/ant-deploy.xml"; // NOI18N

    public static final String APPCLIENT_MAINCLASS_ARGS = "j2ee.appclient.mainclass.args"; // NOI18N
    
    public static final String APPCLIENT_TOOL_RUNTIME = "j2ee.appclient.tool.runtime"; // NOI18N
    public static final String APPCLIENT_TOOL_MAINCLASS = "j2ee.appclient.tool.mainclass"; // NOI18N
    public static final String APPCLIENT_TOOL_JVMOPTS = "j2ee.appclient.tool.jvmoptions";  // NOI18N
    public static final String APPCLIENT_TOOL_ARGS = "j2ee.appclient.tool.args"; // NOI18N
    
    /**
     * "API" contract between Application Client and Glassfish plugin's
     * J2eePlatformImpl implementation.
     */
    public static final String J2EE_PLATFORM_APPCLIENT_ARGS = "j2ee.appclient.args"; // NOI18N
    
    // Well known paths
    public static final String[] WELL_KNOWN_PATHS = new String[] {            
            "${" + JAVAC_CLASSPATH + "}", // NOI18N
            "${" + JAVAC_TEST_CLASSPATH  + "}", // NOI18N
            "${" + RUN_CLASSPATH  + "}", // NOI18N
            "${" + RUN_TEST_CLASSPATH  + "}", // NOI18N
            "${" + BUILD_CLASSES_DIR  + "}", // NOI18N
            "${" + BUILD_TEST_CLASSES_DIR  + "}", // NOI18N
    };
    
    // Prefixes and suffixes of classpath
    public static final String LIBRARY_PREFIX = "${libs."; // NOI18N
    public static final String LIBRARY_SUFFIX = ".classpath}"; // NOI18N
    // XXX looks like there is some kind of API missing in ReferenceHelper?
    public static final String ANT_ARTIFACT_PREFIX = "${reference."; // NOI18N

    ClassPathSupport cs;
    
    
    // SOURCE ROOTS
    // public static final String SOURCE_ROOTS = "__virtual_source_roots__";   //NOI18N
    // public static final String TEST_ROOTS = "__virtual_test_roots__"; // NOI18N
                        
    // MODELS FOR VISUAL CONTROLS
    
    // CustomizerSources
    DefaultTableModel SOURCE_ROOTS_MODEL;
    DefaultTableModel TEST_ROOTS_MODEL;
    Document META_INF_MODEL;
    ComboBoxModel JAVAC_SOURCE_MODEL;
     
    // CustomizerLibraries
    ClassPathUiSupport.ClassPathTableModel JAVAC_CLASSPATH_MODEL;
    DefaultListModel JAVAC_TEST_CLASSPATH_MODEL;
    DefaultListModel RUN_CLASSPATH_MODEL;
    DefaultListModel RUN_TEST_CLASSPATH_MODEL;
    ComboBoxModel PLATFORM_MODEL;
    ListCellRenderer CLASS_PATH_LIST_RENDERER;
    ListCellRenderer PLATFORM_LIST_RENDERER;
    ListCellRenderer JAVAC_SOURCE_RENDERER;
    
    AppClientClassPathUi.ClassPathTableCellItemRenderer CLASS_PATH_TABLE_ITEM_RENDERER;    
    
    
    // CustomizerCompile
    ButtonModel JAVAC_DEPRECATION_MODEL; 
    ButtonModel JAVAC_DEBUG_MODEL;
    ButtonModel NO_DEPENDENCIES_MODEL;
    Document JAVAC_COMPILER_ARG_MODEL;
    
    // CustomizerCompileTest
                
    // CustomizerJar
    Document DIST_JAR_MODEL; 
    Document BUILD_CLASSES_EXCLUDES_MODEL; 
    ButtonModel JAR_COMPRESS_MODEL;
                
    // CustomizerJavadoc
    ButtonModel JAVADOC_PRIVATE_MODEL;
    ButtonModel JAVADOC_NO_TREE_MODEL;
    ButtonModel JAVADOC_USE_MODEL;
    ButtonModel JAVADOC_NO_NAVBAR_MODEL; 
    ButtonModel JAVADOC_NO_INDEX_MODEL; 
    ButtonModel JAVADOC_SPLIT_INDEX_MODEL; 
    ButtonModel JAVADOC_AUTHOR_MODEL; 
    ButtonModel JAVADOC_VERSION_MODEL;
    Document JAVADOC_WINDOW_TITLE_MODEL;
    ButtonModel JAVADOC_PREVIEW_MODEL; 
    Document JAVADOC_ADDITIONALPARAM_MODEL;

    // CustomizerRun
    Document MAIN_CLASS_MODEL;
    Document APPLICATION_ARGS_MODEL;
    Document RUN_JVM_ARGS_MODEL;
    Document RUN_WORK_DIR_MODEL;
    
    ComboBoxModel J2EE_SERVER_INSTANCE_MODEL;
    ComboBoxModel J2EE_PLATFORM_MODEL;
    


    // CustomizerRunTest

    // Private fields ----------------------------------------------------------    
    private final AppClientProject project;
    private final UpdateHelper updateHelper;
    private final PropertyEvaluator evaluator;
    private final ReferenceHelper refHelper;
    private final GeneratedFilesHelper genFileHelper;
    
    private final StoreGroup privateGroup; 
    private final StoreGroup projectGroup;
    
    private final Properties additionalProperties;    

    public static final String JAVA_SOURCE_BASED = "java.source.based";
    
    AppClientProject getProject() {
        return project;
    }
    
    /** Creates a new instance of J2SEUIProperties and initializes them */
    public AppClientProjectProperties( AppClientProject project, UpdateHelper updateHelper, PropertyEvaluator evaluator, ReferenceHelper refHelper, GeneratedFilesHelper genFileHelper ) {
        this.project = project;
        this.updateHelper  = updateHelper;
        this.evaluator = evaluator;
        this.refHelper = refHelper;
        this.genFileHelper = genFileHelper;
        this.cs = new ClassPathSupport( evaluator, refHelper, updateHelper.getAntProjectHelper(), WELL_KNOWN_PATHS, LIBRARY_PREFIX, LIBRARY_SUFFIX, ANT_ARTIFACT_PREFIX );
                
        privateGroup = new StoreGroup();
        projectGroup = new StoreGroup();
        
        additionalProperties = new Properties();
        
        init(); // Load known properties        
    }

    /** Initializes the visual models 
     */
    private void init() {
        
        CLASS_PATH_LIST_RENDERER = new AppClientClassPathUi.ClassPathListCellRenderer( evaluator );
        CLASS_PATH_TABLE_ITEM_RENDERER = new AppClientClassPathUi.ClassPathTableCellItemRenderer( evaluator );
        
        // CustomizerSources
        SOURCE_ROOTS_MODEL = AppClientSourceRootsUi.createModel( project.getSourceRoots() );
        TEST_ROOTS_MODEL = AppClientSourceRootsUi.createModel( project.getTestSourceRoots() );        
        META_INF_MODEL = projectGroup.createStringDocument( evaluator, META_INF );
                
        // CustomizerLibraries
        EditableProperties projectProperties = updateHelper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );                
        EditableProperties privateProperties = updateHelper.getProperties( AntProjectHelper.PRIVATE_PROPERTIES_PATH );
        
        JAVAC_CLASSPATH_MODEL = ClassPathUiSupport.createTableModel( cs.itemsIterator(projectProperties.get( JAVAC_CLASSPATH ), ClassPathSupport.ELEMENT_INCLUDED_LIBRARIES ) );
        JAVAC_TEST_CLASSPATH_MODEL = ClassPathUiSupport.createListModel( cs.itemsIterator(projectProperties.get( JAVAC_TEST_CLASSPATH ), null ) );
        RUN_CLASSPATH_MODEL = ClassPathUiSupport.createListModel( cs.itemsIterator(projectProperties.get( RUN_CLASSPATH ), null ) );
        RUN_TEST_CLASSPATH_MODEL = ClassPathUiSupport.createListModel( cs.itemsIterator(projectProperties.get( RUN_TEST_CLASSPATH ), null ) );
        PLATFORM_MODEL = PlatformUiSupport.createPlatformComboBoxModel (evaluator.getProperty(JAVA_PLATFORM));
        PLATFORM_LIST_RENDERER = PlatformUiSupport.createPlatformListCellRenderer();
        SpecificationVersion minimalSourceLevel = null;
        if (evaluator.getProperty(J2EE_PLATFORM).equals(JAVA_EE_5)) {
            minimalSourceLevel = new SpecificationVersion(JAVA_EE_5);
        }
        JAVAC_SOURCE_MODEL = PlatformUiSupport.createSourceLevelComboBoxModel(PLATFORM_MODEL, evaluator.getProperty(JAVAC_SOURCE), evaluator.getProperty(JAVAC_TARGET), minimalSourceLevel);
        JAVAC_SOURCE_RENDERER = PlatformUiSupport.createSourceLevelListCellRenderer ();
                
        // CustomizerCompile
        JAVAC_DEPRECATION_MODEL = projectGroup.createToggleButtonModel( evaluator, JAVAC_DEPRECATION );
                
        //Hotfix of the issue #70058
        //Should use the StoreGroup when the StoreGroup SPI will be extended to allow false default value in ToggleButtonModel
        Integer[] kind = new Integer[1];
        JAVAC_DEBUG_MODEL = createToggleButtonModel( evaluator, JAVAC_DEBUG, kind);
        javacDebugBooleanKind = kind[0];
        
        NO_DEPENDENCIES_MODEL = projectGroup.createInverseToggleButtonModel( evaluator, NO_DEPENDENCIES );
        JAVAC_COMPILER_ARG_MODEL = projectGroup.createStringDocument( evaluator, JAVAC_COMPILER_ARG );
        
        // CustomizerJar
        DIST_JAR_MODEL = projectGroup.createStringDocument( evaluator, DIST_JAR );
        BUILD_CLASSES_EXCLUDES_MODEL = projectGroup.createStringDocument( evaluator, BUILD_CLASSES_EXCLUDES );
        JAR_COMPRESS_MODEL = projectGroup.createToggleButtonModel( evaluator, JAR_COMPRESS );
        
        // CustomizerJavadoc
        JAVADOC_PRIVATE_MODEL = projectGroup.createToggleButtonModel( evaluator, JAVADOC_PRIVATE );
        JAVADOC_NO_TREE_MODEL = projectGroup.createInverseToggleButtonModel( evaluator, JAVADOC_NO_TREE );
        JAVADOC_USE_MODEL = projectGroup.createToggleButtonModel( evaluator, JAVADOC_USE );
        JAVADOC_NO_NAVBAR_MODEL = projectGroup.createInverseToggleButtonModel( evaluator, JAVADOC_NO_NAVBAR );
        JAVADOC_NO_INDEX_MODEL = projectGroup.createInverseToggleButtonModel( evaluator, JAVADOC_NO_INDEX ); 
        JAVADOC_SPLIT_INDEX_MODEL = projectGroup.createToggleButtonModel( evaluator, JAVADOC_SPLIT_INDEX );
        JAVADOC_AUTHOR_MODEL = projectGroup.createToggleButtonModel( evaluator, JAVADOC_AUTHOR );
        JAVADOC_VERSION_MODEL = projectGroup.createToggleButtonModel( evaluator, JAVADOC_VERSION );
        JAVADOC_WINDOW_TITLE_MODEL = projectGroup.createStringDocument( evaluator, JAVADOC_WINDOW_TITLE );
        //Hotfix of the issue #70058
        //Should use the StoreGroup when the StoreGroup SPI will be extended to allow false default value in ToggleButtonModel        
        JAVADOC_PREVIEW_MODEL = createToggleButtonModel ( evaluator, JAVADOC_PREVIEW, kind);
        javadocPreviewBooleanKind = kind[0];
        
        JAVADOC_ADDITIONALPARAM_MODEL = projectGroup.createStringDocument( evaluator, JAVADOC_ADDITIONALPARAM );
        // CustomizerRun
        MAIN_CLASS_MODEL = projectGroup.createStringDocument( evaluator, MAIN_CLASS ); 
        APPLICATION_ARGS_MODEL = privateGroup.createStringDocument( evaluator, APPLICATION_ARGS );
        RUN_JVM_ARGS_MODEL = projectGroup.createStringDocument( evaluator, RUN_JVM_ARGS );
        RUN_WORK_DIR_MODEL = privateGroup.createStringDocument( evaluator, RUN_WORK_DIR );

        J2EE_SERVER_INSTANCE_MODEL = J2eePlatformUiSupport.createPlatformComboBoxModel(
                privateProperties.getProperty(J2EE_SERVER_INSTANCE), projectProperties.getProperty(J2EE_PLATFORM));
        J2EE_PLATFORM_MODEL = J2eePlatformUiSupport.createSpecVersionComboBoxModel(
            projectProperties.getProperty( J2EE_PLATFORM ));
    }
    
    public void save() {
        try {                        
            // Store properties 
            Boolean result = ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Boolean>() {
                public Boolean run() throws IOException {
                    if ((genFileHelper.getBuildScriptState(GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                        AppClientProject.class.getResource("resources/build-impl.xsl")) &
                            GeneratedFilesHelper.FLAG_MODIFIED) == GeneratedFilesHelper.FLAG_MODIFIED) {  //NOI18N
                        if (showModifiedMessage (NbBundle.getMessage(AppClientProjectProperties.class,"TXT_ModifiedTitle"))) {
                            //Delete user modified build-impl.xml
                            FileObject fo = updateHelper.getAntProjectHelper().getProjectDirectory().
                                getFileObject(GeneratedFilesHelper.BUILD_IMPL_XML_PATH);
                            if (fo != null) {
                                fo.delete();
                            }
                        }
                        else {
                            return Boolean.FALSE;
                        }
                    }
                    storeProperties();
                    return Boolean.TRUE;
                }
            });
            // and save the project
            if (result == Boolean.TRUE) {
                ProjectManager.getDefault().saveProject(project);
            }
        } 
        catch (MutexException e) {
            Exceptions.printStackTrace((IOException) e.getException());
        }
        catch ( IOException ex ) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    
        
    private void storeProperties() throws IOException {
        // Store special properties
        
        // Modify the project dependencies properly        
        resolveProjectDependencies();
        
        // Encode all paths (this may change the project properties)
        String[] javac_cp = cs.encodeToStrings( ClassPathUiSupport.getIterator( JAVAC_CLASSPATH_MODEL.getDefaultListModel() ), ClassPathSupport.ELEMENT_INCLUDED_LIBRARIES  );
        String[] javac_test_cp = cs.encodeToStrings( ClassPathUiSupport.getIterator( JAVAC_TEST_CLASSPATH_MODEL ), null );
        String[] run_test_cp = cs.encodeToStrings( ClassPathUiSupport.getIterator( RUN_TEST_CLASSPATH_MODEL ), null );
        String[] run_cp = cs.encodeToStrings( ClassPathUiSupport.getIterator( RUN_CLASSPATH_MODEL ), null );
                
        // Store source roots
        storeRoots( project.getSourceRoots(), SOURCE_ROOTS_MODEL );
        storeRoots( project.getTestSourceRoots(), TEST_ROOTS_MODEL );
                
        // Store standard properties
        EditableProperties projectProperties = updateHelper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );        
        EditableProperties privateProperties = updateHelper.getProperties( AntProjectHelper.PRIVATE_PROPERTIES_PATH );
        
        // Assure inegrity which can't shound not be assured in UI
        if ( !JAVADOC_NO_INDEX_MODEL.isSelected() ) {
            JAVADOC_SPLIT_INDEX_MODEL.setSelected( false ); // Can't split non existing index
        }
                                
        // Standard store of the properties
        projectGroup.store( projectProperties );        
        privateGroup.store( privateProperties );
        
        //Hotfix of the issue #70058
        //Should use the StoreGroup when the StoreGroup SPI will be extended to allow false default value in ToggleButtonModel
        //Save javac.debug
        privateProperties.setProperty(JAVAC_DEBUG, encodeBoolean (JAVAC_DEBUG_MODEL.isSelected(), javacDebugBooleanKind));
                
        //Hotfix of the issue #70058
        //Should use the StoreGroup when the StoreGroup SPI will be extended to allow false default value in ToggleButtonModel
        //Save javadoc.preview
        privateProperties.setProperty(JAVADOC_PREVIEW, encodeBoolean (JAVADOC_PREVIEW_MODEL.isSelected(), javadocPreviewBooleanKind));
                
        // Save all paths
        projectProperties.setProperty( JAVAC_CLASSPATH, javac_cp );
        projectProperties.setProperty( JAVAC_TEST_CLASSPATH, javac_test_cp );
        projectProperties.setProperty( RUN_CLASSPATH, run_cp );
        projectProperties.setProperty( RUN_TEST_CLASSPATH, run_test_cp );
        
        //Handle platform selection and javac.source javac.target properties
        PlatformUiSupport.storePlatform (projectProperties, updateHelper, AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE, PLATFORM_MODEL.getSelectedItem(), JAVAC_SOURCE_MODEL.getSelectedItem());
                                
        // Handle other special cases
        if ( NO_DEPENDENCIES_MODEL.isSelected() ) { // NOI18N
            projectProperties.remove( NO_DEPENDENCIES ); // Remove the property completely if not set
        }

        if ( getDocumentText( RUN_WORK_DIR_MODEL ).trim().equals( "" ) ) { // NOI18N
            privateProperties.remove( RUN_WORK_DIR ); // Remove the property completely if not set
        }
        
        if (getDocumentText(MAIN_CLASS_MODEL).trim().equals("")) { // NOI18N
            projectProperties.remove(MAIN_CLASS); // Remove the property completely if not set
        }
        
        // Set new server instance ID
        if (J2EE_SERVER_INSTANCE_MODEL.getSelectedItem() != null) {
            setNewServerInstanceValue(J2eePlatformUiSupport.getServerInstanceID(J2EE_SERVER_INSTANCE_MODEL.getSelectedItem()), project, projectProperties, privateProperties);
        }
        
        storeAdditionalProperties(projectProperties);
        List<ClassPathSupport.Item> cpItems = ClassPathUiSupport.getList(JAVAC_CLASSPATH_MODEL.getDefaultListModel());
        storeLibrariesLocations(cpItems.iterator(), privateProperties);
        
        // Store the property changes into the project
        updateHelper.putProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties );
        updateHelper.putProperties( AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProperties );        
        
        String value = (String)additionalProperties.get(SOURCE_ENCODING);
        if (value != null) {
            try {
                FileEncodingQuery.setDefaultEncoding(Charset.forName(value));
            } catch (UnsupportedCharsetException e) {
                //When the encoding is not supported by JVM do not set it as default
            }
        }
        
    }
  
    private void storeAdditionalProperties(EditableProperties projectProperties) {
        for (Iterator i = additionalProperties.keySet().iterator(); i.hasNext();) {
            String key = i.next().toString();
            projectProperties.put(key, additionalProperties.getProperty(key));
        }
    }
    
    private static String getDocumentText( Document document ) {
        try {
            return document.getText( 0, document.getLength() );
        }
        catch( BadLocationException e ) {
            return ""; // NOI18N
        }
    }
    
    /** Finds out what are new and removed project dependencies and 
     * applyes the info to the project
     */
    private void resolveProjectDependencies() {
            
        // Create a set of old and new artifacts.
        Set<ClassPathSupport.Item> oldArtifacts = new HashSet<ClassPathSupport.Item>();
        EditableProperties projectProperties = updateHelper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );        
        oldArtifacts.addAll( cs.itemsList(projectProperties.get( JAVAC_CLASSPATH ), ClassPathSupport.ELEMENT_INCLUDED_LIBRARIES ) );
        oldArtifacts.addAll( cs.itemsList(projectProperties.get( JAVAC_TEST_CLASSPATH ), null ) );
        oldArtifacts.addAll( cs.itemsList(projectProperties.get( RUN_CLASSPATH ), null ) );
        oldArtifacts.addAll( cs.itemsList(projectProperties.get( RUN_TEST_CLASSPATH ), null ) );
                   
        Set<ClassPathSupport.Item> newArtifacts = new HashSet<ClassPathSupport.Item>();
        newArtifacts.addAll( ClassPathUiSupport.getList( JAVAC_CLASSPATH_MODEL.getDefaultListModel() ) );
        newArtifacts.addAll( ClassPathUiSupport.getList( JAVAC_TEST_CLASSPATH_MODEL ) );
        newArtifacts.addAll( ClassPathUiSupport.getList( RUN_CLASSPATH_MODEL ) );
        newArtifacts.addAll( ClassPathUiSupport.getList( RUN_TEST_CLASSPATH_MODEL ) );
                
        // Create set of removed artifacts and remove them
        Set<ClassPathSupport.Item> removed = new HashSet<ClassPathSupport.Item>( oldArtifacts );
        removed.removeAll( newArtifacts );
        Set<ClassPathSupport.Item> added = new HashSet<ClassPathSupport.Item>(newArtifacts);
        added.removeAll(oldArtifacts);
        
        // 1. first remove all project references. The method will modify
        // project property files, so it must be done separately
        for( Iterator it = removed.iterator(); it.hasNext(); ) {
            ClassPathSupport.Item item = (ClassPathSupport.Item)it.next();
            if ( item.getType() == ClassPathSupport.Item.TYPE_ARTIFACT ||
                    item.getType() == ClassPathSupport.Item.TYPE_JAR ) {
                refHelper.destroyReference(item.getReference());
            }
        }
        
        // 2. now read project.properties and modify rest
        EditableProperties ep = updateHelper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );
        boolean changed = false;
        
        for( Iterator it = removed.iterator(); it.hasNext(); ) {
            ClassPathSupport.Item item = (ClassPathSupport.Item)it.next();
            if (item.getType() == ClassPathSupport.Item.TYPE_LIBRARY) {
                // remove helper property pointing to library jar if there is any
                String prop = item.getReference();
                prop = prop.substring(2, prop.length()-1);
                ep.remove(prop);
                changed = true;
            }
        }
        if (changed) {
            updateHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        }
    }
    

    
    private void storeRoots( SourceRoots roots, DefaultTableModel tableModel ) throws MalformedURLException {
        Vector data = tableModel.getDataVector();
        URL[] rootURLs = new URL[data.size()];
        String []rootLabels = new String[data.size()];
        for (int i=0; i<data.size();i++) {
            File f = (File) ((Vector)data.elementAt(i)).elementAt(0);
            rootURLs[i] = AppClientProjectUtil.getRootURL(f,null);            
            rootLabels[i] = (String) ((Vector)data.elementAt(i)).elementAt(1);
        }
        roots.putRoots(rootURLs,rootLabels);
    }
    
    /* This is used by CustomizerWSServiceHost */
    public void putAdditionalProperty(String propertyName, String propertyValue) {
        additionalProperties.setProperty(propertyName, propertyValue);
    }
    
    private static boolean showModifiedMessage (String title) {
        String message = NbBundle.getMessage(AppClientProjectProperties.class,"TXT_Regenerate");
        JButton regenerateButton = new JButton (NbBundle.getMessage(AppClientProjectProperties.class,"CTL_RegenerateButton"));
        regenerateButton.setDefaultCapable(true);
        regenerateButton.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage(AppClientProjectProperties.class,"AD_RegenerateButton"));
        NotifyDescriptor d = new NotifyDescriptor.Message (message, NotifyDescriptor.WARNING_MESSAGE);
        d.setTitle(title);
        d.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
        d.setOptions(new Object[] {regenerateButton, NotifyDescriptor.CANCEL_OPTION});        
        return DialogDisplayer.getDefault().notify(d) == regenerateButton;
    }
    
    //Hotfix of the issue #70058
    //Should be removed when the StoreGroup SPI will be extended to allow false default value in ToggleButtonModel
    private static String encodeBoolean (boolean value, Integer kind) {
        if ( kind == BOOLEAN_KIND_ED ) {
            return value ? "on" : "off"; // NOI18N
        }
        else if ( kind == BOOLEAN_KIND_YN ) { // NOI18N
            return value ? "yes" : "no"; // NOI18N
        }
        else {
            return value ? "true" : "false"; // NOI18N
        }
    }
    
    //Hotfix of the issue #70058
    //Should be removed when the StoreGroup SPI will be extended to allow false default value in ToggleButtonModel
    private static JToggleButton.ToggleButtonModel createToggleButtonModel (final PropertyEvaluator evaluator, final String propName, Integer[] kind) {
        assert evaluator != null && propName != null && kind != null && kind.length == 1;
        String value = evaluator.getProperty( propName );
        boolean isSelected = false;
        if (value == null) {
            isSelected = true;
        }
        else {
           String lowercaseValue = value.toLowerCase();
           if ( lowercaseValue.equals( "yes" ) || lowercaseValue.equals( "no" ) ) { // NOI18N
               kind[0] = BOOLEAN_KIND_YN;
           }
           else if ( lowercaseValue.equals( "on" ) || lowercaseValue.equals( "off" ) ) { // NOI18N
               kind[0] = BOOLEAN_KIND_ED;
           }
           else {
               kind[0] = BOOLEAN_KIND_TF;
           }

           if ( lowercaseValue.equals( "true") || // NOI18N
                lowercaseValue.equals( "yes") ||  // NOI18N
                lowercaseValue.equals( "on") ) {  // NOI18N
               isSelected = true;                   
           } 
        }
        JToggleButton.ToggleButtonModel bm = new JToggleButton.ToggleButtonModel();
        bm.setSelected(isSelected );
        return bm;
    }
    
    public static void setServerInstance(final Project project, final AntProjectHelper helper, final String serverInstanceID) {
        ProjectManager.mutex().postWriteRequest(new Runnable() {
            public void run() {
                try {
                    EditableProperties projectProps = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    EditableProperties privateProps = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    setNewServerInstanceValue(serverInstanceID, project, projectProps, privateProps);
                    helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProps);
                    helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProps);
                    ProjectManager.getDefault().saveProject(project);
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        });
    }
    
    private static void setNewServerInstanceValue(String newServInstID, Project project, EditableProperties projectProps, EditableProperties privateProps) {
        // update j2ee.platform.classpath
        String oldServInstID = privateProps.getProperty(J2EE_SERVER_INSTANCE);
        if (oldServInstID != null) {
            J2eePlatform oldJ2eePlatform = Deployment.getDefault().getJ2eePlatform(oldServInstID);
            if (oldJ2eePlatform != null) {
                ((AppClientProject)project).unregisterJ2eePlatformListener(oldJ2eePlatform);
            }
        }
        J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(newServInstID);
        if (j2eePlatform == null) {
            // probably missing server error
            Logger.getLogger("global").log(Level.INFO, "J2EE platform is null."); // NOI18N
            
            // update j2ee.server.type (throws NPE)
            //projectProps.setProperty(J2EE_SERVER_TYPE, Deployment.getDefault().getServerID(newServInstID));
            // update j2ee.server.instance
            privateProps.setProperty(J2EE_SERVER_INSTANCE, newServInstID);
            privateProps.remove(WebServicesClientConstants.J2EE_PLATFORM_WSIMPORT_CLASSPATH);
            privateProps.remove(WebServicesClientConstants.J2EE_PLATFORM_WSCOMPILE_CLASSPATH);
            privateProps.remove(DEPLOY_ANT_PROPS_FILE);
            privateProps.remove("wa.copy.client.jar.from"); // NOI18N
            return;
        }
        ((AppClientProject)project).registerJ2eePlatformListener(j2eePlatform);
        if(!Boolean.parseBoolean(projectProps.getProperty(J2EE_PLATFORM_SHARED))) {        
            String classpath = Utils.toClasspathString(j2eePlatform.getClasspathEntries());
            privateProps.setProperty(J2EE_PLATFORM_CLASSPATH, classpath);
        }

        // set j2ee.appclient environment
        File[] accrt = j2eePlatform.getToolClasspathEntries(J2eePlatform.TOOL_APP_CLIENT_RUNTIME);
        privateProps.setProperty(APPCLIENT_TOOL_RUNTIME, Utils.toClasspathString(accrt));
        String jvmOpts = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, J2eePlatform.TOOL_PROP_JVM_OPTS);
        if (jvmOpts != null) {
            privateProps.setProperty(APPCLIENT_TOOL_JVMOPTS, jvmOpts);
        }
        String acMain = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, J2eePlatform.TOOL_PROP_MAIN_CLASS);
        if (acMain != null) {
            privateProps.setProperty(APPCLIENT_TOOL_MAINCLASS, acMain);
        }
        String args = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, J2EE_PLATFORM_APPCLIENT_ARGS);
        if (args != null) {
            privateProps.setProperty(AppClientProjectProperties.APPCLIENT_TOOL_ARGS, args);
        }

        String mainClassArgs = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, J2eePlatform.TOOL_PROP_MAIN_CLASS_ARGS);
        if (mainClassArgs != null && !mainClassArgs.equals("")) {
            if (projectProps.getProperty(CLIENT_NAME) != null) {
                projectProps.remove(CLIENT_NAME);
            }
            projectProps.put(APPCLIENT_MAINCLASS_ARGS, mainClassArgs);
        } else if ((mainClassArgs = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, CLIENT_NAME)) != null) {
            if (projectProps.getProperty(APPCLIENT_MAINCLASS_ARGS) != null) {
                projectProps.remove(APPCLIENT_MAINCLASS_ARGS);
            }
            projectProps.put(CLIENT_NAME, mainClassArgs);
        }
        // update j2ee.platform.wsimport.classpath
        if (j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSIMPORT)) {
            File[] wsClasspath = j2eePlatform.getToolClasspathEntries(J2eePlatform.TOOL_WSIMPORT);
            privateProps.setProperty(WebServicesClientConstants.J2EE_PLATFORM_WSIMPORT_CLASSPATH, 
                    Utils.toClasspathString(wsClasspath));
        } else {
            privateProps.remove(WebServicesClientConstants.J2EE_PLATFORM_WSIMPORT_CLASSPATH);
        }      
        // update j2ee.platform.wscompile.classpath
        if (j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSCOMPILE)) {
            File[] wsClasspath = j2eePlatform.getToolClasspathEntries(J2eePlatform.TOOL_WSCOMPILE);
            privateProps.setProperty(WebServicesClientConstants.J2EE_PLATFORM_WSCOMPILE_CLASSPATH, 
                    Utils.toClasspathString(wsClasspath));
        } else {
            privateProps.remove(WebServicesClientConstants.J2EE_PLATFORM_WSCOMPILE_CLASSPATH);
        }
        
        // update j2ee.server.type
        projectProps.setProperty(J2EE_SERVER_TYPE, Deployment.getDefault().getServerID(newServInstID));
        
        // update j2ee.server.instance
        privateProps.setProperty(J2EE_SERVER_INSTANCE, newServInstID);
        
        // ant deployment support
        File projectFolder = FileUtil.toFile(project.getProjectDirectory());
        try {
            AntDeploymentHelper.writeDeploymentScript(new File(projectFolder, ANT_DEPLOY_BUILD_SCRIPT), J2eeModule.CLIENT, newServInstID); // NOI18N
        } catch (IOException ioe) {
            Logger.getLogger("global").log(Level.INFO, null, ioe);
        }
        File antDeployPropsFile = AntDeploymentHelper.getDeploymentPropertiesFile(newServInstID);
        if (antDeployPropsFile == null) {
            privateProps.remove(DEPLOY_ANT_PROPS_FILE);
        } else {
            privateProps.setProperty(DEPLOY_ANT_PROPS_FILE, antDeployPropsFile.getAbsolutePath());
        }
        
        //WORKAROUND for --retrieve option in asadmin deploy command
        //works only for local domains
        //see also http://www.netbeans.org/issues/show_bug.cgi?id=82929
        if ("J2EE".equals(Deployment.getDefault().getServerID(newServInstID))) { // NOI18N
            File asRoot = j2eePlatform.getPlatformRoots()[0];
            File exFile = new File(asRoot, "lib/javaee.jar"); // NOI18N
            InstanceProperties ip = InstanceProperties.getInstanceProperties(newServInstID);
            if (exFile.exists()) {
                privateProps.setProperty("wa.copy.client.jar.from", // NOI18N
                        new File(ip.getProperty("LOCATION"), ip.getProperty("DOMAIN") + "/generated/xml/j2ee-modules").getAbsolutePath()); // NOI18N
            } else {
                privateProps.setProperty("wa.copy.client.jar.from", // NOI18N
                        new File(ip.getProperty("LOCATION"), ip.getProperty("DOMAIN") + "/applications/j2ee-modules").getAbsolutePath()); // NOI18N
            }
        } else {
            privateProps.remove("wa.copy.client.jar.from"); // NOI18N
        }
        
    }

    public static final void getFilesForItem (ClassPathSupport.Item item, List<File> files, List<File> dirs) {
        if (item.isBroken()) {
            return ;
        }
        if (item.getType() == ClassPathSupport.Item.TYPE_LIBRARY) {
            @SuppressWarnings("unchecked")
            List<URL> roots = item.getLibrary().getContent("classpath");  //NOI18N
            for (Iterator it = roots.iterator(); it.hasNext();) {
                URL rootUrl = (URL) it.next();
                FileObject root = URLMapper.findFileObject (rootUrl);
                if ("jar".equals(rootUrl.getProtocol())) {  //NOI18N
                    root = FileUtil.getArchiveFile (root);
                }
                File f = FileUtil.toFile(root);
                if (f != null) {
                    if (f.isFile()) {
                        files.add(f); 
                    } else {
                        dirs.add(f);
                    }
                }
            }
        }
        if (item.getType() == ClassPathSupport.Item.TYPE_JAR) {
            File root = item.getFile();
            if (root != null) {
                if (root.isFile()) {
                    files.add(root); 
                } else {
                    dirs.add(root);
                }
            }
        }
        if (item.getType() == ClassPathSupport.Item.TYPE_ARTIFACT) {
            String artifactFolder = item.getArtifact().getScriptLocation().getParent();
            URI roots[] = item.getArtifact().getArtifactLocations();
            for (int i = 0; i < roots.length; i++) {
                String root = artifactFolder + File.separator + roots [i];
                if (root.endsWith(File.separator)) {
                    dirs.add(new File (root));
                } else {
                    files.add(new File (root));
                }
            }
        }
    }
    
     /** Store locations of libraries in the classpath param that have more the one
     * file into the properties in the following format:
     * 
     * <ul>
     * <li>libs.foo.classpath.libdir.1=C:/foo
     * <li>libs.foo.classpath.libdirs=1
     * <li>libs.foo.classpath.libfile.1=C:/bar/a.jar
     * <li>libs.foo.classpath.libfile.2=C:/bar/b.jar
     * <li>libs.foo.classpath.libfiles=1
     * </ul>
     * This is needed for the Ant copy task as it cannot copy more the one file
     * and it needs different handling for files and directories.
     * <br>
     * It removes all properties that match this format that were in the {@link #properties}
     * but are not in the {@link #classpath}.
     */
    public static void storeLibrariesLocations(Iterator<ClassPathSupport.Item> classpath, EditableProperties privateProps) {
        List<String> exLibs = new ArrayList<String>();
        Iterator propKeys = privateProps.keySet().iterator();
        while (propKeys.hasNext()) {
            String key = (String) propKeys.next();
            if (key.endsWith(".libdirs") || key.endsWith(".libfiles") || //NOI18N
                    (key.indexOf(".libdir.") > 0) || (key.indexOf(".libfile.") > 0)) { //NOI18N
                exLibs.add(key);
            }
        }
        while (classpath.hasNext()) {
            ClassPathSupport.Item item = classpath.next();
            List<File> files = new ArrayList<File>();
            List<File> dirs = new ArrayList<File>();
            getFilesForItem (item, files, dirs);
            String key;
            if (files.size() > 1 || (files.size()>0 && dirs.size()>0)) {
                String ref = item.getReference() == null ? item.getRaw() : item.getReference();
                for (int i = 0; i < files.size(); i++) {
                    File f = files.get(i);
                    key = getAntPropertyName(ref)+".libfile." + (i+1); //NOI18N
                    privateProps.setProperty (key, "" + f.getAbsolutePath()); //NOI18N
                    exLibs.remove(key);
                }
            }
            if (dirs.size() > 1 || (files.size()>0 && dirs.size()>0)) {
                String ref = item.getReference() == null ? item.getRaw() : item.getReference();
                for (int i = 0; i < dirs.size(); i++) {
                    File f = dirs.get(i);
                    key = getAntPropertyName(ref)+".libdir." + (i+1); //NOI18N
                    privateProps.setProperty (key, "" + f.getAbsolutePath()); //NOI18N
                    exLibs.remove(key);
                }
            }
        }
        Iterator unused = exLibs.iterator();
        while (unused.hasNext()) {
            privateProps.remove(unused.next());
        }
    }
    
    public static String getProperty(final String property, final AntProjectHelper helper, final String path) {
        EditableProperties props = helper.getProperties(path);
        return props.getProperty(property);
    }
    
    
    public static String getAntPropertyName( String property ) {
        if ( property != null && 
             property.startsWith( "${" ) && // NOI18N
             property.endsWith( "}" ) ) { // NOI18N
            return property.substring( 2, property.length() - 1 ); 
        }
        else {
            return property;
        }
    }
    
}
