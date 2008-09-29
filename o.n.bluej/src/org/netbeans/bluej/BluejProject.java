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

package org.netbeans.bluej;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.bluej.api.BluejOpenCloseCallback;
import org.netbeans.bluej.classpath.ClassPathProviderImpl;
import org.netbeans.bluej.options.BlueJSettings;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Represents one bluej project.
 * @author Milos Kleint
 */
public final class BluejProject implements Project, AntProjectListener {
    
    private static final Icon BLUEJ_PROJECT_ICON = new ImageIcon(ImageUtilities.loadImage("org/netbeans/bluej/resources/bluejproject.png")); // NOI18N

    private static final String PROP_BLUEJ_HOME = "bluej.userlib"; //NOI18N
    
    // Special properties of the project
    public static final String J2SE_PROJECT_NAME = "j2se.project.name"; // NOI18N
    public static final String JAVA_PLATFORM = "platform.active"; // NOI18N
    
    // Properties stored in the PROJECT.PROPERTIES    
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
                
    // Properties stored in the PRIVATE.PROPERTIES
    public static final String APPLICATION_ARGS = "application.args"; // NOI18N
    public static final String JAVADOC_PREVIEW="javadoc.preview"; // NOI18N
    

    private final AuxiliaryConfiguration aux;
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final ReferenceHelper refHelper;
    private final GeneratedFilesHelper genFilesHelper;
    private final Lookup lookup;
    private final UpdateHelper updateHelper;
////    private MainClassUpdater mainClassUpdater;
////    private SourceRoots sourceRoots;
////    private SourceRoots testRoots;
    

    BluejProject(AntProjectHelper helper) throws IOException {
        this.helper = helper;
        eval = createEvaluator();
        aux = helper.createAuxiliaryConfiguration();
        refHelper = new ReferenceHelper(helper, aux, eval);
        genFilesHelper = new GeneratedFilesHelper(helper);
        this.updateHelper = new UpdateHelper (this, this.helper, this.aux, this.genFilesHelper,
            UpdateHelper.createDefaultNotifier());

        lookup = createLookup(aux);
        helper.addAntProjectListener(this);
    }
    

    /**
     * Returns the project directory
     * @return the directory the project is located in
     */
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }

    public String toString() {
        return "BluejProject[" + getProjectDirectory() + "]"; // NOI18N
    }
    
    private PropertyEvaluator createEvaluator() {
        // XXX might need to use a custom evaluator to handle active platform substitutions... TBD
        // It is currently safe to not use the UpdateHelper for PropertyEvaluator; UH.getProperties() delegates to APH
        return helper.getStandardPropertyEvaluator();
    }
    
    PropertyEvaluator evaluator() {
        return eval;
    }

    ReferenceHelper getReferenceHelper () {
        return this.refHelper;
    }

    public UpdateHelper getUpdateHelper() {
        return this.updateHelper;
    }
    
    public Lookup getLookup() {
        return lookup;
    }
    
    public AntProjectHelper getAntProjectHelper() {
        return helper;
    }

    private Lookup createLookup(AuxiliaryConfiguration aux) {
        SubprojectProvider spp = refHelper.createSubprojectProvider();
        return Lookups.fixed(new Object[] {
            new Info(),
            aux,
            helper.createCacheDirectoryProvider(),
            spp,
            new BluejActionProvider( this, getUpdateHelper()),
            new BluejLogicalViewProvider(this),
            new BJClassPathExtender(this),
            new CustomizerProviderImpl(this, evaluator(), getUpdateHelper()),
////            // new J2SECustomizerProvider(this, this.updateHelper, evaluator(), refHelper),
////            new CustomizerProviderImpl(this, this.updateHelper, evaluator(), refHelper, this.genFilesHelper),        
            new ClassPathProviderImpl(this), 
            new SFBQueryImpl(this, helper, evaluator()),
////            new CompiledSourceForBinaryQuery(this.helper, evaluator(),getSourceRoots(),getTestSourceRoots()), //Does not use APH to get/put properties/cfgdata
            new AntArtifactProviderImpl(),
            new ProjectXmlSavedHookImpl(),
            new ProjectOpenedHookImpl(),
            new BluejUnitTestForSourceQuery(this),
////            new SourceLevelQueryImpl(evaluator()),
            new BluejSources (this),
////            new J2SESharabilityQuery (this.helper, evaluator(), getSourceRoots(), getTestSourceRoots()), //Does not use APH to get/put properties/cfgdata
            new BluejFileBuiltQuery(this.helper, evaluator()), //Does not use APH to get/put properties/cfgdata
            new RecommendedTemplatesImpl(),
////            new J2SEProjectClassPathExtender(this, this.updateHelper, eval,refHelper),
            this // never cast an externally obtained Project to BluejProject - use lookup instead
////            new J2SEProjectOperations(this),
////            new J2SEProjectWebServicesSupportProvider()
        });
    }

    public void configurationXmlChanged(AntProjectEvent ev) {
        if (ev.getPath().equals(AntProjectHelper.PROJECT_XML_PATH)) {
            // Could be various kinds of changes, but name & displayName might have changed.
            Info info = (Info)getLookup().lookup(ProjectInformation.class);
            info.firePropertyChange(ProjectInformation.PROP_NAME);
            info.firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME);
        }
    }

    public void propertiesChanged(AntProjectEvent ev) {
        // currently ignored (probably better to listen to evaluator() if you need to)
    }
    
    // Package private methods -------------------------------------------------

////    /**
////     * Returns the source roots of this project
////     * @return project's source roots
////     */
////    public synchronized SourceRoots getSourceRoots() {        
////        if (this.sourceRoots == null) { //Local caching, no project metadata access
////            this.sourceRoots = new SourceRoots(this.updateHelper, evaluator(), getReferenceHelper(), "source-roots", false, "src.{0}{1}.dir"); //NOI18N
////        }
////        return this.sourceRoots;
////    }
////    
////    public synchronized SourceRoots getTestSourceRoots() {
////        if (this.testRoots == null) { //Local caching, no project metadata access
////            this.testRoots = new SourceRoots(this.updateHelper, evaluator(), getReferenceHelper(), "test-roots", true, "test.{0}{1}.dir"); //NOI18N
////        }
////        return this.testRoots;
////    }
////    
////    File getTestClassesDirectory() {
////        String testClassesDir = evaluator().getProperty(J2SEProjectProperties.BUILD_TEST_CLASSES_DIR);
////        if (testClassesDir == null) {
////            return null;
////        }
////        return helper.resolveFile(testClassesDir);
////    }
    
    // Currently unused (but see #47230):
    /** Store configured project name. */
    public void setName(final String name) {
        ProjectManager.mutex().writeAccess(new Mutex.Action() {
            public Object run() {
                Element data = helper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(BluejProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");  // NOI18N
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(BluejProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                    data.insertBefore(nameEl, /* OK if null */data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                helper.putPrimaryConfigurationData(data, true);
                return null;
            }
        });
    }




    // Private innerclasses ----------------------------------------------------
    
    private final class Info implements ProjectInformation {
        
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        
        Info() {}
        
        void firePropertyChange(String prop) {
            pcs.firePropertyChange(prop, null, null);
        }
        
        public String getName() {
            return PropertyUtils.getUsablePropertyName(getProjectDirectory().getName());
        }
        
        public String getDisplayName() {
            return (String) ProjectManager.mutex().readAccess(new Mutex.Action() {
                public Object run() {
                    Element data = updateHelper.getPrimaryConfigurationData(true);
                    // XXX replace by XMLUtil when that has findElement, findText, etc.
                    NodeList nl = data.getElementsByTagNameNS(BluejProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                    if (nl.getLength() == 1) {
                        nl = nl.item(0).getChildNodes();
                        if (nl.getLength() == 1 && nl.item(0).getNodeType() == Node.TEXT_NODE) {
                            return ((Text) nl.item(0)).getNodeValue() + " " + getProjectDirectory().getName();  // NOI18N
                        }
                    }
                    return getProjectDirectory().getName(); // NOI18N
                }
            });
        }
        
        public Icon getIcon() {
            return BLUEJ_PROJECT_ICON;
        }
        
        public Project getProject() {
            return BluejProject.this;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }
        
    }
    
    private static final class ProjectXmlSavedHookImpl extends ProjectXmlSavedHook {
        
        ProjectXmlSavedHookImpl() {}
        
        protected void projectXmlSaved() throws IOException {
            //May be called by {@link AuxiliaryConfiguration#putConfigurationFragment}
            //which didn't affect the j2seproject 
////            if (updateHelper.isCurrent()) {
////                //Refresh build-impl.xml only for j2seproject/2
////                genFilesHelper.refreshBuildScript(
////                    GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
////                    BluejProject.class.getResource("resources/build-impl.xsl"),
////                    false);
////                genFilesHelper.refreshBuildScript(
////                    GeneratedFilesHelper.BUILD_XML_PATH,
////                    BluejProject.class.getResource("resources/build.xsl"),
////                    false);
////            }
        }
        
    }
    
    public static File getUserLibPath(File bjHome) {
        
        File userlib;
        if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
            userlib = new File(bjHome.getParentFile(), bjHome.getName() + ".app/Contents/Resources/Java/userlib");
        } else {
            userlib = new File(new File(bjHome, "lib"), "userlib");
        }
        return userlib;
    }
    
    private final class ProjectOpenedHookImpl extends ProjectOpenedHook implements PropertyChangeListener {
        
        ProjectOpenedHookImpl() {}
        
        protected void projectOpened() {
            // Make it easier to run headless builds on the same machine at least.
            ProjectManager.mutex().writeAccess(new Mutex.Action() {
                public Object run() {
                    EditableProperties ep = updateHelper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    File buildProperties = new File(System.getProperty("netbeans.user"), "build.properties"); // NOI18N
                    ep.setProperty("user.properties.file", buildProperties.getAbsolutePath()); // NOI18N                    
                    File bjHome = BlueJSettings.getDefault().getHome();
                    if (bjHome != null) {
                        
                        ep.setProperty(PROP_BLUEJ_HOME, getUserLibPath(bjHome).getAbsolutePath());
                        ep.setComment(PROP_BLUEJ_HOME, new String[] {
                            "## the bluej.userlib property is reset everytime the project is opened in netbeans according to the",
                            "## setting in the IDE that point to the location of the bluej installation's userlib directory.",
                            "## It is required to find and use the libraries located in BLUEJ_HOME/lib/userlib when building the project" 
                        }, true);
                    } else {
                        ep.remove(PROP_BLUEJ_HOME);
                    }
                    ep.setProperty("bluej.config.libraries", BlueJSettings.getDefault().getUserLibrariesAsClassPath());  // NOI18N
                    ep.setComment("bluej.config.libraries", new String[] {  // NOI18N
                        "## classpath entry that is composed from content of bluej.userlib.*.location properties in the user home's bluej.properties file..",
                        "## rebuilt on every opening of the project in netbeans"
                    }, true);
                    updateHelper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                    try {
                        ProjectManager.getDefault().saveProject(BluejProject.this);
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                    return null;
                }
            });
            BlueJSettings.getDefault().addPropertyChangeListener(this);
            
////            // Check up on build scripts.
////            try {
////                if (updateHelper.isCurrent()) {
////                    //Refresh build-impl.xml only for j2seproject/2
////                    genFilesHelper.refreshBuildScript(
////                        GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
////                        BluejProject.class.getResource("resources/build-impl.xsl"),
////                        true);
////                    genFilesHelper.refreshBuildScript(
////                        GeneratedFilesHelper.BUILD_XML_PATH,
////                        BluejProject.class.getResource("resources/build.xsl"),
////                        true);
////                }                
////            } catch (IOException e) {
////                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
////            }
            
            // register project's classpaths to GlobalPathRegistry
            ClassPathProviderImpl cpProvider = (ClassPathProviderImpl)lookup.lookup(ClassPathProviderImpl.class);
            GlobalPathRegistry.getDefault().register(ClassPath.BOOT, cpProvider.getBootPath());
            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, cpProvider.getSourcePath());
            GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, cpProvider.getCompileTimeClasspath());

////            //register updater of main.class
////            //the updater is active only on the opened projects
////            mainClassUpdater = new MainClassUpdater (BluejProject.this, eval, updateHelper,
////                    cpProvider.getProjectClassPaths(ClassPath.SOURCE)[0], J2SEProjectProperties.MAIN_CLASS);

////            J2SELogicalViewProvider physicalViewProvider = (J2SELogicalViewProvider)
////                BluejProject.this.getLookup().lookup (J2SELogicalViewProvider.class);
////            if (physicalViewProvider != null &&  physicalViewProvider.hasBrokenLinks()) {   
////                BrokenReferencesSupport.showAlert();
////            }
            BluejOpenCloseCallback callback = (BluejOpenCloseCallback) Lookup.getDefault().lookup(BluejOpenCloseCallback.class);
            if (callback != null) {
                callback.projectOpened(BluejProject.this);
            }
        }
        
        protected void projectClosed() {
            BlueJSettings.getDefault().removePropertyChangeListener(this);
            // Probably unnecessary, but just in case:
            try {
                ProjectManager.getDefault().saveProject(BluejProject.this);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
            BluejOpenCloseCallback callback = (BluejOpenCloseCallback) Lookup.getDefault().lookup(BluejOpenCloseCallback.class);
            if (callback != null) {
                callback.projectClosed(BluejProject.this);
            }
            
            // unregister project's classpaths to GlobalPathRegistry
            ClassPathProviderImpl cpProvider = (ClassPathProviderImpl)lookup.lookup(ClassPathProviderImpl.class);
            GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, cpProvider.getBootPath());
            GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, cpProvider.getSourcePath());
            GlobalPathRegistry.getDefault().unregister(ClassPath.COMPILE, cpProvider.getCompileTimeClasspath());
////            if (mainClassUpdater != null) {
////                mainClassUpdater.unregister ();
////                mainClassUpdater = null;
////            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            ProjectManager.mutex().writeAccess(new Mutex.Action() {
                public Object run() {
                    EditableProperties ep = updateHelper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    File bjHome = BlueJSettings.getDefault().getHome();
                    if (bjHome != null) {
                        ep.setProperty(PROP_BLUEJ_HOME, getUserLibPath(bjHome).getAbsolutePath());
                    } else {
                        ep.remove(PROP_BLUEJ_HOME);
                    }
                    updateHelper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                    try {
                        ProjectManager.getDefault().saveProject(BluejProject.this);
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                    return null;
                }
            });
        }
        
    }
    

    /**
     * Exports the main JAR as an official build product for use from other scripts.
     * The type of the artifact will be {@link AntArtifact#TYPE_JAR}.
     */
    private final class AntArtifactProviderImpl implements AntArtifactProvider {

        public AntArtifact[] getBuildArtifacts() {
            return new AntArtifact[] {
                helper.createSimpleAntArtifact(JavaProjectConstants.ARTIFACT_TYPE_JAR, "dist.jar", evaluator(), "jar", "clean"), // NOI18N
            };
        }

    }
    
    private static final class RecommendedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates {
        RecommendedTemplatesImpl() {
        }
        
        // List of primarily supported templates
        
        private static final String[] APPLICATION_TYPES = new String[] { 
            "java-classes",         // NOI18N
            "java-main-class",      // NOI18N
            "java-forms",           // NOI18N
            "gui-java-application", // NOI18N
            "java-beans",           // NOI18N
            "oasis-XML-catalogs",   // NOI18N
            "XML",                  // NOI18N
            "ant-script",           // NOI18N
            "ant-task",             // NOI18N
//            "web-service-clients",  // NOI18N
//            "wsdl",                 // NOI18N
            // "servlet-types",     // NOI18N
            // "web-types",         // NOI18N
            "junit",                // NOI18N
            // "MIDP",              // NOI18N
            "simple-files",         // NOI18N
            "bluej"                 // NOI18N
        };
        
        private static final String[] PRIVILEGED_NAMES = new String[] {
            "Templates/Bluej/StdClass.java", // NOI18N
            "Templates/Bluej/MainClass.java", // NOI18N
            "Templates/Classes/Package", // NOI18N
            "Templates/Bluej/Interface.java", // NOI18N
            "Templates/Bluej/Enum.java", // NOI18N
            "Templates/Bluej/Abstract.java", // NOI18N
            "Templates/Bluej/UnitTest.java", // NOI18N

        };
        
        public String[] getRecommendedTypes() {
            return APPLICATION_TYPES;
        }
        
        public String[] getPrivilegedTemplates() {
            return PRIVILEGED_NAMES;
        }
        
    }

}