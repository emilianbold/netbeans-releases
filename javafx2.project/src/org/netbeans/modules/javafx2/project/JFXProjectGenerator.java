/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
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
package org.netbeans.modules.javafx2.project;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.javafx2.platform.api.JavaFXPlatformUtils;
import org.netbeans.modules.javafx2.project.JavaFXProjectWizardIterator.WizardType;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Creates a JavaFX Project from scratch according to some initial configuration.
 * 
 * TODO use J2SEProjectBuider instead
 */
public class JFXProjectGenerator {

    private static final String METRICS_LOGGER = "org.netbeans.ui.metrics.projects"; //NOI18N
    private static final String JFX_METRICS_LOGGER = "org.netbeans.ui.metrics.jfx";  //NOI18N
    private static final String PROJECT_TYPE = "org.netbeans.modules.javafx2.project.JFXProject";   //NOI18N
    
    enum Action {
        CREATE("USG_PROJECT_CREATE", "USG_PROJECT_CREATE_JFX"),   //NOI18N
        OPEN("USG_PROJECT_OPEN", "USG_PROJECT_OPEN_JFX"),       //NOI18N
        CLOSE("USG_PROJECT_CLOSE", "USG_PROJECT_CLOSE_JFX"),     //NOI18N
        BUILD_NATIVE("USG_PROJECT_BUILD_NATIVE", "USG_PROJECT_BUILD_NATIVE_JFX");     //NOI18N
        
        private final String genericLogMessage;
        private final String specificLogMessage;
        
        private Action(
            @NonNull final String genericLogMessage,
            @NonNull final String specificLogMessage) {
            assert genericLogMessage != null;
            assert specificLogMessage != null;
            this.genericLogMessage = genericLogMessage;
            this.specificLogMessage = specificLogMessage;
        }
        
        @NonNull
        public String getGenericLogMessage() {
            return genericLogMessage;
        }
        
        @NonNull
        public String getSpecificLogMessage() {
            return specificLogMessage;
        }
    }

    private JFXProjectGenerator() {
    }

    /**
     * Create a new empty JavaFX project.
     * @param dir the top-level directory (need not yet exist but if it does it must be empty)
     * @param name the name for the project
     * @param librariesDefinition project relative or absolute OS path to libraries definition; can be null
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    public static AntProjectHelper createProject(final File dir, final String name, final String mainClass,
            final String fxmlName, final String manifestFile, final String librariesDefinition,
            final String platformName, final String preloader, final WizardType type) throws IOException {
        Parameters.notNull("dir", dir); //NOI18N
        Parameters.notNull("name", name);   //NOI18N
        
        final FileObject dirFO = FileUtil.createFolder(dir);
        // if manifestFile is null => it's TYPE_LIB
        final AntProjectHelper[] h = new AntProjectHelper[1];
        dirFO.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            @Override
            public void run() throws IOException {
                h[0] = createProject(dirFO, name, "src", "test", mainClass, manifestFile, //NOI18N
                        librariesDefinition, platformName, preloader, type);
                final Project p = ProjectManager.getDefault().findProject(dirFO);
                createJfxExtension(p, dirFO, type);
                ProjectManager.getDefault().saveProject(p);
                if(type != WizardType.SWING) {
                    JFXGeneratedFilesHelper.generateBuildScriptFromStylesheet(h[0],
                        GeneratedFilesHelper.BUILD_XML_PATH,
                        JFXProjectGenerator.class.getResource("resources/build.xsl")); //NOI18N
                }
                final ReferenceHelper refHelper = getReferenceHelper(p);
                try {
                    ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                        @Override
                        public Void run() throws Exception {
                            copyRequiredLibraries(h[0], refHelper);
                            return null;
                        }
                    });
                } catch (MutexException ex) {
                    Exceptions.printStackTrace(ex.getException());
                }
                FileObject srcFolder = dirFO.createFolder("src"); // NOI18N
//                dirFO.createFolder("test"); // NOI18N
                createFiles(mainClass, fxmlName, srcFolder, type);
            }
        });
        return h[0];
    }
   
    private static ReferenceHelper getReferenceHelper(Project p) {
        try {
            return (ReferenceHelper) p.getClass().getMethod("getReferenceHelper").invoke(p); // NOI18N
        } catch (Exception e) {
            return null;
        }
    }

    static AntProjectHelper createProject(final File dir, final String name,
            final File[] sourceFolders, final File[] testFolders,
            final String manifestFile, final String librariesDefinition,
            final String buildXmlName, final String platformName,
            final String preloader, final WizardType type) throws IOException {
        Parameters.notNull("dir", dir); //NOI18N
        Parameters.notNull("name", name);   //NOI8N
        Parameters.notNull("sourceFolders", sourceFolders); //NOI18N
        Parameters.notNull("testFolders", testFolders); //NOI18N
        
        final FileObject dirFO = FileUtil.createFolder(dir);
        final AntProjectHelper[] h = new AntProjectHelper[1];
        // this constructor creates only java application type
        dirFO.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            @Override
            public void run() throws IOException {
                h[0] = createProject(dirFO, name, null, null, null, manifestFile,
                        librariesDefinition, platformName, preloader, type);
                final Project p = ProjectManager.getDefault().findProject(dirFO);
                final ReferenceHelper refHelper = getReferenceHelper(p);
                try {
                    ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                        @Override
                        public Void run() throws Exception {
                            Element data = h[0].getPrimaryConfigurationData(true);
                            Document doc = data.getOwnerDocument();
                            NodeList nl = data.getElementsByTagNameNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, "source-roots"); // NOI18N
                            assert nl.getLength() == 1;
                            Element sourceRoots = (Element) nl.item(0);
                            nl = data.getElementsByTagNameNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, "test-roots");  //NOI18N
                            assert nl.getLength() == 1;
                            Element testRoots = (Element) nl.item(0);
                            for (int i = 0; i < sourceFolders.length; i++) {
                                String propName;
                                if (i == 0) {
                                    //Name the first src root src.dir to be compatible with NB 4.0
                                    propName = "src.dir";       //NOI18N
                                } else {
                                    String name = sourceFolders[i].getName();
                                    propName = name + ".dir";    //NOI18N
                                }

                                int rootIndex = 1;
                                EditableProperties props = h[0].getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                                while (props.containsKey(propName)) {
                                    rootIndex++;
                                    propName = name + rootIndex + ".dir";   //NOI18N
                                }
                                String srcReference = refHelper.createForeignFileReference(sourceFolders[i], JavaProjectConstants.SOURCES_TYPE_JAVA);
                                Element root = doc.createElementNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, "root");   //NOI18N
                                root.setAttribute("id", propName);   //NOI18N
                                sourceRoots.appendChild(root);
                                props = h[0].getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                                props.put(propName, srcReference);
                                h[0].putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props); // #47609
                            }
                            for (int i = 0; i < testFolders.length; i++) {
                                if (!testFolders[i].exists()) {
                                    testFolders[i].mkdirs();
                                }
                                String propName;
                                if (i == 0) {
                                    //Name the first test root test.src.dir to be compatible with NB 4.0
                                    propName = "test.src.dir";  //NOI18N
                                } else {
                                    String name = testFolders[i].getName();
                                    propName = "test." + name + ".dir"; // NOI18N
                                }
                                int rootIndex = 1;
                                EditableProperties props = h[0].getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                                while (props.containsKey(propName)) {
                                    rootIndex++;
                                    propName = "test." + name + rootIndex + ".dir"; // NOI18N
                                }
                                String testReference = refHelper.createForeignFileReference(testFolders[i], JavaProjectConstants.SOURCES_TYPE_JAVA);
                                Element root = doc.createElementNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, "root"); // NOI18N
                                root.setAttribute("id", propName); // NOI18N
                                testRoots.appendChild(root);
                                props = h[0].getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH); // #47609
                                props.put(propName, testReference);
                                h[0].putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                            }
                            h[0].putPrimaryConfigurationData(data, true);
                            if (buildXmlName != null) {
                                final EditableProperties props = h[0].getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                                props.put(JFXProjectProperties.BUILD_SCRIPT, buildXmlName);
                                h[0].putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                            }
                            createJfxExtension(p, dirFO, type);
                            ProjectManager.getDefault().saveProject(p);
                            if(type != WizardType.SWING) {
                                JFXGeneratedFilesHelper.generateBuildScriptFromStylesheet(h[0],
                                    GeneratedFilesHelper.BUILD_XML_PATH,
                                    JFXProjectGenerator.class.getResource("resources/build.xsl")); //NOI18N
                            }
                            copyRequiredLibraries(h[0], refHelper);
                            ProjectUtils.getSources(p).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                            return null;
                        }
                    });
                } catch (MutexException me) {
                    ErrorManager.getDefault().notify(me);
                }
            }
        });
        return h[0];
    }

    static AntProjectHelper createPreloaderProject(final File dir, final String name,
            final String librariesDefinition, final String platformName, final String preloaderClassName) throws IOException {
        Parameters.notNull("dir", dir); //NOI18N
        Parameters.notNull("name", name);   //NOI18N
        Parameters.notNull("preloaderClassName", preloaderClassName);   //NOI18N

        final FileObject dirFO = FileUtil.createFolder(dir);
        final AntProjectHelper[] h = new AntProjectHelper[1];
        dirFO.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            @Override
            public void run() throws IOException {
//                h[0] = createProject(dirFO, name, "src", "test", preloaderClassName, // NOI18N
//                        JavaFXProjectWizardIterator.MANIFEST_FILE, librariesDefinition,
//                        platformName, null, WizardType.PRELOADER);
                h[0] = createProject(dirFO, name, "src", "test", preloaderClassName, // NOI18N
                        JavaFXProjectWizardIterator.MANIFEST_FILE, librariesDefinition,
                        platformName, null, WizardType.PRELOADER);
                
                final Project p = ProjectManager.getDefault().findProject(dirFO);
                createJfxExtension(p, dirFO, WizardType.PRELOADER);
                ProjectManager.getDefault().saveProject(p);
                JFXGeneratedFilesHelper.generateBuildScriptFromStylesheet(h[0],
                    GeneratedFilesHelper.BUILD_XML_PATH,
                    JFXProjectGenerator.class.getResource("resources/build.xsl")); //NOI18N
                final ReferenceHelper refHelper = getReferenceHelper(p);
                try {
                    ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                        @Override
                        public Void run() throws Exception {
                            copyRequiredLibraries(h[0], refHelper);
                            return null;
                        }
                    });
                } catch (MutexException ex) {
                    Exceptions.printStackTrace(ex.getException());
                }
                
                FileObject srcFolder = dirFO.createFolder("src"); // NOI18N
//                dirFO.createFolder("test"); // NOI18N
                createPreloaderClass(preloaderClassName, srcFolder);
            }
        });
        JavaFXProjectWizardIterator.createManifest(FileUtil.toFileObject(dir), true);

        return h[0];
    }
    
    private static void createJfxExtension(Project p, FileObject dirFO, WizardType type) throws IOException {
        //adding JavaFX buildscript extension
        FileObject templateFO = FileUtil.getConfigFile("Templates/JFX/jfx-impl.xml"); //NOI18N
        if (templateFO != null) {
            FileObject nbprojectFO = dirFO.getFileObject("nbproject");
            FileObject jfxBuildFile = FileUtil.copyFile(templateFO, nbprojectFO, "jfx-impl"); // NOI18N
            if(type == WizardType.SWING) {
                FileObject templatesFO = nbprojectFO.getFileObject("templates"); // NOI18N
                if(templatesFO == null) {
                    templatesFO = nbprojectFO.createFolder("templates"); // NOI18N
                }                
                FileObject swingTemplateFO1 = FileUtil.getConfigFile("Templates/JFX/FXSwingTemplate.html"); //NOI18N
                if(swingTemplateFO1 != null) {
                    FileUtil.copyFile(swingTemplateFO1, templatesFO, "FXSwingTemplate"); // NOI18N
                }
                FileObject swingTemplateFO2 = FileUtil.getConfigFile("Templates/JFX/FXSwingTemplateApplet.jnlp"); //NOI18N
                if(swingTemplateFO1 != null) {
                    FileUtil.copyFile(swingTemplateFO2, templatesFO, "FXSwingTemplateApplet"); // NOI18N
                }
                FileObject swingTemplateFO3 = FileUtil.getConfigFile("Templates/JFX/FXSwingTemplateApplication.jnlp"); //NOI18N
                if(swingTemplateFO1 != null) {
                    FileUtil.copyFile(swingTemplateFO3, templatesFO, "FXSwingTemplateApplication"); // NOI18N
                }
            }
            AntBuildExtender extender = p.getLookup().lookup(AntBuildExtender.class);
            if (extender != null) {
                assert jfxBuildFile != null;
                if (extender.getExtension("jfx") == null) { // NOI18N
                    AntBuildExtender.Extension ext = extender.addExtension("jfx", jfxBuildFile); // NOI18N
                    ext.addDependency("-init-check", "-check-javafx"); // NOI18N
                    ext.addDependency("jar", "-jfx-copylibs"); // NOI18N
                    ext.addDependency("jar", "-rebase-libs"); //NOI18N
                    ext.addDependency("-post-jar", "-jfx-copylibs"); //NOI18N
                    ext.addDependency("-post-jar", "-rebase-libs"); //NOI18N
                    ext.addDependency("-post-jar", "jfx-deployment"); //NOI18N 
                    ext.addDependency("run", "jar"); //NOI18N
                    ext.addDependency("debug", "jar");//NOI18N
                    ext.addDependency("profile", "jar");//NOI18N
                }
            }
        }
    }

    private static AntProjectHelper createProject(FileObject dirFO, String name,
            String srcRoot, String testRoot, String mainClass, String manifestFile,
            String librariesDefinition, String platformName, String preloader,
            WizardType type) throws IOException {
        
        AntProjectHelper h = ProjectGenerator.createProject(dirFO, J2SEProjectType.TYPE, librariesDefinition);
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);
        final Element explicitPlatformEl = doc.createElementNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, "explicit-platform"); //NOI18N
        explicitPlatformEl.setAttribute("explicit-source-supported", "true");   //NOI18N
        data.appendChild(explicitPlatformEl);
        
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        Element sourceRoots = doc.createElementNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, "source-roots");  //NOI18N
        if (srcRoot != null) {
            Element root = doc.createElementNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, "root");   //NOI18N
            root.setAttribute("id", "src.dir");   //NOI18N
            sourceRoots.appendChild(root);
            ep.setProperty("src.dir", srcRoot); // NOI18N
        }
        data.appendChild(sourceRoots);
        Element testRoots = doc.createElementNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, "test-roots");  //NOI18N
        if (testRoot != null) {
            Element root = doc.createElementNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, "root");   //NOI18N
            root.setAttribute("id", "test.src.dir");   //NOI18N
            testRoots.appendChild(root);
            ep.setProperty("test.src.dir", testRoot); // NOI18N
        }
        data.appendChild(testRoots);
        h.putPrimaryConfigurationData(data, true);
        
        // ===========================
        //   JavaFX specific stuff
        // ===========================
        ep.setProperty(JFXProjectProperties.JAVAFX_ENABLED, "true"); // NOI18N
        ep.setComment(JFXProjectProperties.JAVAFX_ENABLED, new String[]{"# " + NbBundle.getMessage(JFXProjectGenerator.class, "COMMENT_javafx")}, false); // NOI18N
        
        ep.setProperty("jnlp.enabled", "false"); // NOI18N
        ep.setComment("jnlp.enabled", new String[]{"# " + NbBundle.getMessage(JFXProjectGenerator.class, "COMMENT_oldjnlp")}, false); // NOI18N
        
        ep.setProperty(ProjectProperties.COMPILE_ON_SAVE, "true"); // NOI18N
        ep.setProperty(ProjectProperties.COMPILE_ON_SAVE_UNSUPPORTED_PREFIX + ".javafx", "true"); // NOI18N
        // JAVAFX_BINARY_ENCODE_CSS should remain "false" until JavaFX issue http://javafx-jira.kenai.com/browse/RT-18126 is resolved
        ep.setProperty(JFXProjectProperties.JAVAFX_BINARY_ENCODE_CSS, "false"); // NOI18N
        ep.setProperty(JFXProjectProperties.JAVAFX_DEPLOY_INCLUDEDT, "true"); // NOI18N
        ep.setProperty(JFXProjectProperties.JAVAFX_DEPLOY_EMBEDJNLP, "true"); // NOI18N
        ep.setProperty(JFXProjectProperties.JAVAFX_REBASE_LIBS, "false"); // NOI18N
        ep.setComment(JFXProjectProperties.JAVAFX_REBASE_LIBS, new String[]{"# " + NbBundle.getMessage(JFXProjectGenerator.class, "COMMENT_rebase_libs")}, false); // NOI18N

        ep.setProperty(JFXProjectProperties.JAVAFX_DISABLE_CONCURRENT_RUNS, "false"); // NOI18N
        ep.setComment(JFXProjectProperties.JAVAFX_DISABLE_CONCURRENT_RUNS, new String[]{"# " + NbBundle.getMessage(JFXProjectGenerator.class, "COMMENT_disable_concurrent_runs")}, false); // NOI18N
        ep.setProperty(JFXProjectProperties.JAVAFX_ENABLE_CONCURRENT_EXTERNAL_RUNS, "false"); // NOI18N
        ep.setComment(JFXProjectProperties.JAVAFX_ENABLE_CONCURRENT_EXTERNAL_RUNS, new String[]{"# " + NbBundle.getMessage(JFXProjectGenerator.class, "COMMENT_enable_concurrent_external_runs")}, false); // NOI18N

        //ep.setProperty(JFXProjectProperties.JAVAFX_NATIVE_BUNDLING_ENABLED, "false"); // NOI18N
        //ep.setProperty(JFXProjectProperties.JAVAFX_NATIVE_BUNDLING_TYPE, JFXProjectProperties.BundlingType.NONE.getString().toLowerCase());
        
        // update mode set to false with signing on to prevent malfunction caused by so-far unresolved issues in JavaFX 2.1.x WebStart
        ep.setProperty(JFXProjectProperties.UPDATE_MODE_BACKGROUND, "false"); // NOI18N
        ep.setComment(JFXProjectProperties.UPDATE_MODE_BACKGROUND, new String[]{"# " + NbBundle.getMessage(JFXProjectGenerator.class, type == WizardType.SWING ? "COMMENT_updatemode_swing" : "COMMENT_updatemode")}, false); // NOI18N
        ep.setProperty(JFXProjectProperties.ALLOW_OFFLINE, "true"); // NOI18N

        ep.setProperty(JavaFXPlatformUtils.PROPERTY_JAVAFX_SDK, JavaFXPlatformUtils.getJavaFXSDKPathReference(platformName));
        ep.setProperty(JavaFXPlatformUtils.PROPERTY_JAVAFX_RUNTIME, JavaFXPlatformUtils.getJavaFXRuntimePathReference(platformName));

        ep.setProperty(ProjectProperties.JAVAC_CLASSPATH, JavaFXPlatformUtils.getJavaFXClassPath()); // NOI18N
        ep.setProperty(ProjectProperties.ENDORSED_CLASSPATH, ""); // NOI18N

        ep.setProperty(JFXProjectProperties.RUN_APP_WIDTH, "800"); // NOI18N
        ep.setProperty(JFXProjectProperties.RUN_APP_HEIGHT, "600"); // NOI18N

        if (type == WizardType.PRELOADER) {
            ep.setProperty(JFXProjectProperties.JAVAFX_PRELOADER, "true"); // NOI18N
            ep.setComment(JFXProjectProperties.JAVAFX_PRELOADER, new String[]{"# " + NbBundle.getMessage(JFXProjectGenerator.class, "COMMENT_preloader")}, false); // NOI18N
            ep.setProperty(JFXProjectProperties.PRELOADER_ENABLED, "false"); // NOI18N
            ep.setComment(JFXProjectProperties.PRELOADER_ENABLED, new String[]{"# " + NbBundle.getMessage(JFXProjectGenerator.class, "COMMENT_prepreloader")}, false); // NOI18N
        } else {
            // Disable J2SE JAR creation
            ep.setProperty("jar.archive.disabled", "true"); // NOI18N
            ep.setComment("jar.archive.disabled", new String[]{"# " + NbBundle.getMessage(JFXProjectGenerator.class, "COMMENT_oldjar")}, false); // NOI18N
            ep.setProperty(ProjectProperties.MAIN_CLASS, type == WizardType.SWING ? (mainClass == null ? "" : mainClass) : "com.javafx.main.Main"); // NOI18N
            ep.setComment(ProjectProperties.MAIN_CLASS, new String[]{"# " + NbBundle.getMessage(JFXProjectGenerator.class, "COMMENT_main.class")}, false); // NOI18N

            if (type != WizardType.LIBRARY) {
                ep.setProperty(JFXProjectProperties.MAIN_CLASS, mainClass == null ? "" : mainClass); // NOI18N
                ep.setComment(JFXProjectProperties.MAIN_CLASS, new String[]{"# " + NbBundle.getMessage(JFXProjectGenerator.class, "COMMENT_main.fxclass")}, false); // NOI18N
            }

            if (preloader != null && preloader.length() > 0) { // this project uses preloader
                String preloaderProjRelative = "../" + preloader; // NOI18N
                String preloaderJarFileName = preloader + ".jar"; // NOI18N
                String copiedPreloaderJarPath = "${dist.dir}/lib/${" + JFXProjectProperties.PRELOADER_JAR_FILENAME + "}"; // NOI18N

                ep.setProperty(JFXProjectProperties.PRELOADER_ENABLED, "true"); // NOI18N
                ep.setProperty(JFXProjectProperties.PRELOADER_TYPE, JFXProjectProperties.PreloaderSourceType.PROJECT.getString());
                ep.setComment(JFXProjectProperties.PRELOADER_ENABLED, new String[]{"# " + NbBundle.getMessage(JFXProjectGenerator.class, "COMMENT_use_preloader")}, false); // NOI18N
                ep.setProperty(JFXProjectProperties.PRELOADER_PROJECT, preloaderProjRelative);
                ep.setProperty(JFXProjectProperties.PRELOADER_CLASS, JavaFXProjectWizardIterator.generatePreloaderClassName(preloader)); // NOI18N
                ep.setProperty(JFXProjectProperties.PRELOADER_JAR_PATH, copiedPreloaderJarPath);
                ep.setProperty(JFXProjectProperties.PRELOADER_JAR_FILENAME, preloaderJarFileName);
            } else {
                ep.setProperty(JFXProjectProperties.PRELOADER_ENABLED, "false"); // NOI18N
                ep.setProperty(JFXProjectProperties.PRELOADER_TYPE, JFXProjectProperties.PreloaderSourceType.NONE.getString());
                ep.setComment(JFXProjectProperties.PRELOADER_ENABLED, new String[]{"# " + NbBundle.getMessage(JFXProjectGenerator.class, "COMMENT_dontuse_preloader")}, false); // NOI18N
                ep.setProperty(JFXProjectProperties.PRELOADER_PROJECT, ""); // NOI18N
                ep.setProperty(JFXProjectProperties.PRELOADER_CLASS, ""); // NOI18N
                ep.setProperty(JFXProjectProperties.PRELOADER_JAR_PATH, ""); // NOI18N
                ep.setProperty(JFXProjectProperties.PRELOADER_JAR_FILENAME, ""); // NOI18N
            }
            
            if (type == WizardType.SWING) {
                ep.setProperty(JFXProjectProperties.JAVAFX_SWING, "true"); // NOI18N
                ep.setComment(JFXProjectProperties.JAVAFX_SWING, new String[]{"# " + NbBundle.getMessage(JFXProjectGenerator.class, "COMMENT_use_swing")}, false); // NOI18N
            }
            
            if (type == WizardType.FXML || type == WizardType.SWING) {
                // Workaround of JavaFX 2.0 issue causing FXML apps crash when signing is disabled
                ep.setProperty(JFXProjectProperties.JAVAFX_SIGNING_ENABLED, "true"); // NOI18N
                ep.setProperty(JFXProjectProperties.JAVAFX_SIGNING_TYPE, JFXProjectProperties.SigningType.SELF.getString());
                ep.setProperty(JFXProjectProperties.PERMISSIONS_ELEVATED, "true"); // NOI18N
            }
        }

        // TODO select from UI
        ep.setProperty(JFXProjectProperties.FALLBACK_CLASS, "com.javafx.main.NoJavaFXFallback"); // NOI18N
        //ep.setProperty(JFXProjectProperties.SIGNED_JAR, "${dist.dir}/" + validatePropertyValue(name) + "_signed.jar"); // NOI18N
                
        // ===========================
        //     J2SE Project stuff
        // ===========================
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_ENABLED, "true"); // NOI18N
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_ENABLED_IN_EDITOR, "false"); // NOI18N
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_RUN_ALL_PROCESSORS, "true"); // NOI18N
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_PROCESSORS_LIST, ""); // NOI18N
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_SOURCE_OUTPUT, "${build.generated.sources.dir}/ap-source-output"); // NOI18N
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_PROCESSOR_OPTIONS, ""); // NOI18N
        
        ep.setProperty("dist.dir", "dist"); // NOI18N
        ep.setComment("dist.dir", new String[]{"# " + NbBundle.getMessage(JFXProjectGenerator.class, "COMMENT_dist.dir")}, false); // NOI18N
        ep.setProperty("dist.jar", "${dist.dir}/" + validatePropertyValue(name) + ".jar"); // NOI18N
        ep.setProperty("application.vendor", System.getProperty("user.name", "User Name")); //NOI18N
        ep.setProperty("application.title", name); // NOI18N
        
        ep.setProperty(ProjectProperties.JAVAC_PROCESSORPATH, new String[]{"${javac.classpath}"}); // NOI18N
        ep.setProperty("javac.test.processorpath", new String[]{"${javac.test.classpath}"}); // NOI18N
        ep.setProperty("build.sysclasspath", "ignore"); // NOI18N
        ep.setComment("build.sysclasspath", new String[]{"# " + NbBundle.getMessage(JFXProjectGenerator.class, "COMMENT_build.sysclasspath")}, false); // NOI18N
        ep.setProperty(ProjectProperties.RUN_CLASSPATH, new String[]{ // NOI18N
                    // note that dist.jar needs to be first to prevent mixups in case of multiple dependent FX projects
                    "${dist.jar}:", // NOI18N
                    "${javac.classpath}", // NOI18N
                });
        ep.setProperty("debug.classpath", new String[]{ // NOI18N
                    "${run.classpath}", // NOI18N
                });
        ep.setComment("debug.classpath", new String[]{ // NOI18N
                    "# " + NbBundle.getMessage(JFXProjectGenerator.class, "COMMENT_debug.transport"), // NOI18N
                    "#debug.transport=dt_socket" // NOI18N
                }, false);
        ep.setProperty("jar.compress", "false"); // NOI18N

        ep.setProperty("javac.compilerargs", ""); // NOI18N
        ep.setComment("javac.compilerargs", new String[]{ // NOI18N
                    "# " + NbBundle.getMessage(JFXProjectGenerator.class, "COMMENT_javac.compilerargs"), // NOI18N
                }, false);
        SpecificationVersion sourceLevel = getPlatformSourceLevel(platformName);
        ep.setProperty("javac.source", sourceLevel.toString()); // NOI18N
        ep.setProperty("javac.target", sourceLevel.toString()); // NOI18N
        ep.setProperty("javac.deprecation", "false"); // NOI18N
        ep.setProperty(ProjectProperties.JAVAC_TEST_CLASSPATH, new String[]{ // NOI18N
                    "${javac.classpath}:", // NOI18N
                    "${build.classes.dir}", // NOI18N
                });
        ep.setProperty(ProjectProperties.RUN_TEST_CLASSPATH, new String[]{ // NOI18N
                    "${javac.test.classpath}:", // NOI18N
                    "${build.test.classes.dir}", // NOI18N
                });
        ep.setProperty("debug.test.classpath", new String[]{ // NOI18N
                    "${run.test.classpath}", // NOI18N
                });

        ep.setProperty("build.generated.dir", "${build.dir}/generated"); // NOI18N
        ep.setProperty("meta.inf.dir", "${src.dir}/META-INF"); // NOI18N

        ep.setProperty(ProjectProperties.BUILD_DIR, "build"); // NOI18N
        ep.setComment(ProjectProperties.BUILD_DIR, new String[]{"# " + NbBundle.getMessage(JFXProjectGenerator.class, "COMMENT_build.dir")}, false); // NOI18N
        ep.setProperty(ProjectProperties.BUILD_CLASSES_DIR, "${build.dir}/classes"); // NOI18N
        ep.setProperty("build.generated.sources.dir", "${build.dir}/generated-sources"); // NOI18N
        ep.setProperty("build.test.classes.dir", "${build.dir}/test/classes"); // NOI18N
        ep.setProperty("build.test.results.dir", "${build.dir}/test/results"); // NOI18N
        ep.setProperty("build.classes.excludes", "**/*.java,**/*.form"); // NOI18N
        ep.setProperty("dist.javadoc.dir", "${dist.dir}/javadoc"); // NOI18N
        ep.setProperty("platform.active", platformName); // NOI18N

        ep.setProperty(JFXProjectProperties.JAVADOC_PRIVATE, "false"); // NOI18N
        ep.setProperty(JFXProjectProperties.JAVADOC_NO_TREE, "false"); // NOI18N
        ep.setProperty(JFXProjectProperties.JAVADOC_USE, "true"); // NOI18N
        ep.setProperty(JFXProjectProperties.JAVADOC_NO_NAVBAR, "false"); // NOI18N
        ep.setProperty(JFXProjectProperties.JAVADOC_NO_INDEX, "false"); // NOI18N
        ep.setProperty(JFXProjectProperties.JAVADOC_SPLIT_INDEX, "true"); // NOI18N
        ep.setProperty(JFXProjectProperties.JAVADOC_AUTHOR, "false"); // NOI18N
        ep.setProperty(JFXProjectProperties.JAVADOC_VERSION, "false"); // NOI18N
        ep.setProperty(JFXProjectProperties.JAVADOC_WINDOW_TITLE, ""); // NOI18N
        ep.setProperty(JFXProjectProperties.JAVADOC_ENCODING, "${" + JFXProjectProperties.SOURCE_ENCODING + "}"); // NOI18N
        ep.setProperty(JFXProjectProperties.JAVADOC_ADDITIONALPARAM, ""); // NOI18N
        Charset enc = FileEncodingQuery.getDefaultEncoding();
        ep.setProperty(JFXProjectProperties.SOURCE_ENCODING, enc.name());
        if (manifestFile != null) {
            ep.setProperty("manifest.file", manifestFile); // NOI18N
        }
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        Map<String,String> browserInfo = JFXProjectUtils.getDefaultBrowserInfo();
        ep = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        ep.setProperty(JFXProjectProperties.JAVAFX_ENDORSED_ANT_CLASSPATH, "."); // NOI18N
        ep.setComment(JFXProjectProperties.JAVAFX_ENDORSED_ANT_CLASSPATH, new String[]{"# " + NbBundle.getMessage(JFXProjectGenerator.class, "COMMENT_endorsed_ant_classpath")}, false); // NOI18N
        if(browserInfo != null && !browserInfo.isEmpty()) {
            for(Map.Entry<String,String> entry : browserInfo.entrySet()) {
                ep.setProperty(JFXProjectProperties.RUN_IN_BROWSER, entry.getKey());
                ep.setProperty(JFXProjectProperties.RUN_IN_BROWSER_PATH, entry.getValue());
                break;
            }
        }
        h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
        JFXProjectUtils.updateDefaultRunAsConfigFile(dirFO, JFXProjectProperties.RunAsType.ASWEBSTART, false);
        JFXProjectUtils.updateDefaultRunAsConfigFile(dirFO, JFXProjectProperties.RunAsType.INBROWSER, false);
        logUsage(Action.CREATE);
        return h;
    }

    /**
     * Logs project specific usage.
     * See: http://wiki.netbeans.org/UsageLoggingSpecification
     * Todo: Should log also J2SE project usage? The JFX project is de facto J2SE project,
     * most of this class should be replaced by J2SEProjectBuider.
     */
    static void logUsage(@NonNull Action action) {
        assert action != null;
        Logger logger = Logger.getLogger(JFXProjectGenerator.METRICS_LOGGER);
        LogRecord logRecord = new LogRecord(Level.INFO, action.getGenericLogMessage());
        logRecord.setLoggerName(logger.getName());
        logRecord.setParameters(new Object[]{
            PROJECT_TYPE
        });
        logger.log(logRecord);
        
        logger = Logger.getLogger(JFXProjectGenerator.JFX_METRICS_LOGGER);
        logRecord = new LogRecord(Level.INFO, action.getSpecificLogMessage());
        logRecord.setLoggerName(logger.getName());
        logger.log(logRecord);
    }

    private static void copyRequiredLibraries(AntProjectHelper h, ReferenceHelper rh) throws IOException {
        if (!h.isSharableProject()) {
            return;
        }
        if (rh.getProjectLibraryManager().getLibrary("junit") == null // NOI18N
                && LibraryManager.getDefault().getLibrary("junit") != null) { // NOI18N
            rh.copyLibrary(LibraryManager.getDefault().getLibrary("junit")); // NOI18N
        }
        if (rh.getProjectLibraryManager().getLibrary("junit_4") == null // NOI18N
                && LibraryManager.getDefault().getLibrary("junit_4") != null) { // NOI18N
            rh.copyLibrary(LibraryManager.getDefault().getLibrary("junit_4")); // NOI18N
        }
        if (rh.getProjectLibraryManager().getLibrary("CopyLibs") == null // NOI18N
                && LibraryManager.getDefault().getLibrary("CopyLibs") != null) { // NOI18N
            rh.copyLibrary(LibraryManager.getDefault().getLibrary("CopyLibs")); // NOI18N
        }
        if (rh.getProjectLibraryManager().getLibrary("JavaFX2Runtime") == null // NOI18N
                && LibraryManager.getDefault().getLibrary("JavaFX2Runtime") != null) { // NOI18N
            File mainPropertiesFile = h.resolveFile(h.getLibrariesLocation());
            referenceLibrary(LibraryManager.getDefault().getLibrary("JavaFX2Runtime"), mainPropertiesFile.toURI().toURL(), true); //NOI18N

        }
    }

    /** for jar uri this method returns path wihtin jar or null*/
    private static String getJarFolder(URI uri) {
        String u = uri.toString();
        int index = u.indexOf("!/"); //NOI18N
        if (index != -1 && index + 2 < u.length()) {
            return u.substring(index + 2);
        }
        return null;
    }

    /** append path to given jar root uri */
    private static URI appendJarFolder(URI u, String jarFolder) {
        try {
            if (u.isAbsolute()) {
                return new URI("jar:" + u.toString() + "!/" + (jarFolder == null ? "" : jarFolder.replace('\\', '/'))); // NOI18N
            } else {
                return new URI(u.toString() + "!/" + (jarFolder == null ? "" : jarFolder.replace('\\', '/'))); // NOI18N
            }
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    private static Library referenceLibrary(final Library lib, final URL location,
            final boolean generateLibraryUniqueName) throws IOException {
        final File libBaseFolder = new File(URI.create(location.toExternalForm())).getParentFile();
        final Map<String, List<URI>> content = new HashMap<String, List<URI>>();
        String[] volumes = LibrariesSupport.getLibraryTypeProvider(lib.getType()).getSupportedVolumeTypes();
        for (String volume : volumes) {
            List<URI> volumeContent = new ArrayList<URI>();
            for (URL origlibEntry : lib.getContent(volume)) {
                URL libEntry = origlibEntry;
                String jarFolder = null;
                if ("jar".equals(libEntry.getProtocol())) { // NOI18N
                    jarFolder = getJarFolder(URI.create(libEntry.toExternalForm()));
                    libEntry = FileUtil.getArchiveFile(libEntry);
                }
                FileObject libEntryFO = URLMapper.findFileObject(libEntry);
                if (libEntryFO == null) {
                    if (!"file".equals(libEntry.getProtocol()) && // NOI18N
                            !"nbinst".equals(libEntry.getProtocol())) { // NOI18N
                        Logger.getLogger(JFXProjectGenerator.class.getName()).log(Level.INFO,
                                "referenceLibrary is ignoring entry {0}", libEntry); // NOI18N
                        //this is probably exclusively urls to maven poms.
                        continue;
                    } else {
                        Logger.getLogger(JFXProjectGenerator.class.getName()).log(Level.WARNING,
                                "Library '{0}' contains entry ({1}) which does not exist. " // NOI18N
                                + "This entry is ignored and will not be refernced from sharable " // NOI18N
                                + "libraries.", new Object[]{lib.getDisplayName(), libEntry}); // NOI18N
                        continue;
                    }
                }
                URI u;
                String name = PropertyUtils.relativizeFile(libBaseFolder, FileUtil.toFile(libEntryFO));
                if (name == null) { // #198955
                    Logger.getLogger(JFXProjectGenerator.class.getName()).log(Level.WARNING,
                            "Can not relativize file: {0}", libEntryFO.getPath()); // NOI18N
                    continue;
                }
                u = LibrariesSupport.convertFilePathToURI(name);
                if (FileUtil.isArchiveFile(libEntryFO)) {
                    u = appendJarFolder(u, jarFolder);
                }
                volumeContent.add(u);
            }
            content.put(volume, volumeContent);
        }
        final LibraryManager man = LibraryManager.forLocation(location);
        try {
            return ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Library>() {
                @Override
                public Library run() throws IOException {
                    String name = lib.getName();
                    if (generateLibraryUniqueName) {
                        int index = 2;
                        while (man.getLibrary(name) != null) {
                            name = lib.getName() + "-" + index;
                            index++;
                        }
                    }
                    return man.createURILibrary(lib.getType(), name, content);
                }
            });
        } catch (MutexException ex) {
            throw (IOException) ex.getException();
        }
    }

    private static void createFiles(String mainClassName, String fxmlName, FileObject srcFolder, WizardType type) throws IOException {
        DataFolder pDf = DataFolder.findFolder(srcFolder);
        if(mainClassName != null && mainClassName.length() > 0)
        {
            int lastDotIdx = mainClassName.lastIndexOf('.'); // NOI18N
            String mName, pName;
            if (lastDotIdx == -1) {
                mName = mainClassName.trim();
                pName = null;
            } else {
                mName = mainClassName.substring(lastDotIdx + 1).trim();
                pName = mainClassName.substring(0, lastDotIdx).trim();
            }

            if (mName.length() > 0) {
                Map<String, String> params = null;
                FileObject template = null;
                switch (type) {
                    case APPLICATION:
                        template = FileUtil.getConfigFile("Templates/javafx/FXMain.java"); // NOI18N
                        break;
                    case PRELOADER:
                        template = FileUtil.getConfigFile("Templates/javafx/FXPreloader.java"); // NOI18N
                        break;
                    case SWING:
                        template = FileUtil.getConfigFile("Templates/javafx/FXSwingMain.java"); // NOI18N
                        break;
                    case FXML:
                        template = FileUtil.getConfigFile("Templates/javafx/FXML.java"); // NOI18N
                        params = new HashMap<String, String>(1);
                        params.put("fxmlname", fxmlName); // NOI18N
                }

                if (template == null) {
                    return; // Don't know the template
                }

                DataObject mt = DataObject.find(template);
                if (pName != null) {
                    String fName = pName.replace('.', '/'); // NOI18N
                    FileObject pkgFolder = FileUtil.createFolder(srcFolder, fName);
                    pDf = DataFolder.findFolder(pkgFolder);
                }
                if (params != null) {
                    mt.createFromTemplate(pDf, mName, params);
                } else {
                    mt.createFromTemplate(pDf, mName);
                }
            }
        }
        if (type == WizardType.FXML) {
            FileObject xmlTemplate = FileUtil.getConfigFile("Templates/javafx/NewProjectFXML.fxml"); // NOI18N
            if (xmlTemplate == null) {
                return; // Don't know the template
            }
            Map<String, String> params = new HashMap<String, String>(1);
            params.put("postfix", NbBundle.getMessage(JFXProjectGenerator.class, "TXT_FileNameControllerPostfix")); // NOI18N
            DataObject dXMLTemplate = DataObject.find(xmlTemplate);
            dXMLTemplate.createFromTemplate(pDf, fxmlName, params);

            FileObject javaTemplate = FileUtil.getConfigFile("Templates/javafx/FXML2.java"); // NOI18N
            if (javaTemplate == null) {
                return; // Don't know the template
            }
            DataObject dJavaTemplate = DataObject.find(javaTemplate);
            dJavaTemplate.createFromTemplate(pDf, fxmlName + NbBundle.getMessage(JFXProjectGenerator.class, "TXT_FileNameControllerPostfix")); //NOI18N
        }
    }
    
    private static void createPreloaderClass(String preloaderClassName, FileObject srcFolder) throws IOException {
        int lastDotIdx = preloaderClassName.lastIndexOf('.'); // NOI18N
        String mName, pName;
        if (lastDotIdx == -1) {
            mName = preloaderClassName.trim();
            pName = null;
        } else {
            mName = preloaderClassName.substring(lastDotIdx + 1).trim();
            pName = preloaderClassName.substring(0, lastDotIdx).trim();
        }

        if (mName.length() == 0) {
            return;
        }

        FileObject template = FileUtil.getConfigFile("Templates/javafx/FXPreloader.java"); // NOI18N
        if (template == null) {
            return; // Don't know the template
        }

        DataObject mt = DataObject.find(template);
        FileObject pkgFolder = srcFolder;
        if (pName != null) {
            String fName = pName.replace('.', '/'); // NOI18N
            pkgFolder = FileUtil.createFolder(srcFolder, fName);
        }
        DataFolder pDf = DataFolder.findFolder(pkgFolder);
        mt.createFromTemplate(pDf, mName);
    }
    
    private static SpecificationVersion getPlatformSourceLevel(String fxPlatformName) {
        JavaPlatform platform = null;
        JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms();
        for (JavaPlatform javaPlatform : platforms) {
            String platformName = javaPlatform.getProperties().get(JavaFXPlatformUtils.PLATFORM_ANT_NAME);
            if (JFXProjectProperties.isEqual(platformName, fxPlatformName)) {
                platform = javaPlatform;
                break;
            }
        }
        if(platform == null) {
            // default JavaFX2 specification version is 1.6
            return new SpecificationVersion("1.6"); // NOI18N
        }
        SpecificationVersion v = platform.getSpecification().getVersion();
        return v;
    }

    //------------ Used by unit tests -------------------
    private static SpecificationVersion defaultSourceLevel;

    private static SpecificationVersion getDefaultSourceLevel() {
        if (defaultSourceLevel != null) {
            return defaultSourceLevel;
        } else {
            JavaPlatform defaultPlatform = JavaPlatformManager.getDefault().getDefaultPlatform();
            SpecificationVersion v = defaultPlatform.getSpecification().getVersion();
            return v;
        }
    }
    private static final Pattern INVALID_NAME = Pattern.compile("[$/\\\\\\p{Cntrl}]");  //NOI18N

    private static String validatePropertyValue(String value) {
        final Matcher m = INVALID_NAME.matcher(value);
        if (m.find()) {
            value = m.replaceAll("_");  //NOI18N
        }
        return value;
    }

    /**
     * Unit test only method. Sets the default source level for tests
     * where the default platform is not available.
     * @param version the default source level set to project when it is created
     *
     */
    public static void setDefaultSourceLevel(SpecificationVersion version) {
        defaultSourceLevel = version;
    }
}
