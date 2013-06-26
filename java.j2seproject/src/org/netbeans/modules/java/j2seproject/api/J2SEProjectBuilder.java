/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.j2seproject.api;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroupModifier;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;
import org.netbeans.spi.project.support.ant.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
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
 * Builder for creating a new J2SE project.
 * Typical usage is:
 * <pre>
 *      new J2SEProjectBuilder(projectFolder, projectName).
 *      addSourceRoots(sourceFolders).
 *      addTestRoots(testFolders).
        setMainClass(mainClass).
 *      build();
 * </pre>
 * XXX: Uses Bundle from org.netbeans.modules.java.j2seproject package not to affect
 * existing localizations.
 * @author Tomas Zezula
 * @since 1.42
 */
public class J2SEProjectBuilder {

    private static final Logger LOG = Logger.getLogger(J2SEProjectBuilder.class.getName());
    private static final String PLATFORM_ANT_NAME = "platform.ant.name";    //NOI18N
    private static final String DEFAULT_PLATFORM_ID = "default_platform";   //NOI18N
    
    private final File projectDirectory;
    private final String name;
    private final Collection<File> sourceRoots;
    private final Collection<File> testRoots;
    private final Collection<Library> compileLibraries;
    private final Collection<Library> runtimeLibraries;
    private final StringBuilder jvmArgs;
    
    private boolean hasDefaultRoots;
    private boolean skipTests;
    private SpecificationVersion defaultSourceLevel;
    private String mainClass;
    private String manifest;
    private String librariesDefinition;
    private String buildXmlName;
    private String distFolder;
    private String mainClassTemplate;
    private JavaPlatform platform;
    
    /**
     * Creates a new instance of {@link J2SEProjectBuilder}
     * @param projectDirectory the directory in which the project should be created
     * @param name the name of the project
     */
    public J2SEProjectBuilder(
            final @NonNull File projectDirectory,
            final @NonNull String name) {
        Parameters.notNull("projectDirectory", projectDirectory);   //NOI18N
        Parameters.notNull("name", name);                           //NOI18N
        this.projectDirectory = projectDirectory;
        this.name = name;
        this.sourceRoots = new ArrayList<File>();
        this.testRoots = new ArrayList<File>();
        this.jvmArgs = new StringBuilder();
        this.compileLibraries = new ArrayList<Library>();
        this.runtimeLibraries = new ArrayList<Library>();
        this.platform = JavaPlatformManager.getDefault().getDefaultPlatform();
    }
    
    /**
     * Adds the default source roots, "src" and "test".
     * @return the builder
     */    
    public J2SEProjectBuilder addDefaultSourceRoots() {
        this.hasDefaultRoots = true;
        return this;
    }

    /**
     * Avoids creating the test root folder and adding JUnit dependencies.
     * The test folder is still registered so {@link SourceGroupModifier} with {@link JavaProjectConstants#SOURCES_HINT_TEST} will work later.
     * @return the builder
     */
    public J2SEProjectBuilder skipTests(boolean skipTests) {
        this.skipTests = skipTests;
        return this;
    }
    
    /**
     * Adds source roots into the project
     * @param sourceRoots the roots to be added
     * @return the builder
     */
    public J2SEProjectBuilder addSourceRoots(final @NonNull File... sourceRoots) {
        Parameters.notNull("sourceRoots", sourceRoots); //NOI18N
        this.sourceRoots.addAll(Arrays.asList(sourceRoots));
        return this;
    }
    
    /**
     * Adds test roots into the project
     * @param testRoots the roots to be added
     * @return the builder
     */
    public J2SEProjectBuilder addTestRoots(final @NonNull File... testRoots) {
        Parameters.notNull("testRoots", testRoots);     //NOI18N
        this.testRoots.addAll(Arrays.asList(testRoots));
        return this;
    }
    
    /**
     * Adds compile time libraries
     * @param libraries the libraries to be added to compile classpath.
     * @return the builder
     */
    public J2SEProjectBuilder addCompileLibraries(@NonNull final Library... libraries) {
        Parameters.notNull("libraries", libraries); //NOI18N
        this.compileLibraries.addAll(Arrays.asList(libraries));
        return this;
    }
    
    /**
     * Adds runtime libraries
     * @param libraries the libraries to be added to runtime classpath.
     * @return the builder
     */
    public J2SEProjectBuilder addRuntimeLibraries(@NonNull final Library... libraries) {
        Parameters.notNull("libraries", libraries); //NOI18N
        this.runtimeLibraries.addAll(Arrays.asList(libraries));
        return this;
    }
    
    /**
     * Sets a main class
     * @param mainClass the fully qualified name of the main class,
     * if null main class is not created
     * @return the builder
     */
    public J2SEProjectBuilder setMainClass (final @NullAllowed String mainClass) {
        this.mainClass = mainClass;
        return this;
    }
    
    /**
     * Sets a path to manifest file
     * @param manifest the name (path) to manifest file,
     * if not manifest is not set
     * @return the builder
     */
    public J2SEProjectBuilder setManifest (final @NullAllowed String manifest) {
        this.manifest = manifest;
        return this;
    }
    
    /**
     * Sets a library definition file for per project libraries,
     * @param librariesDefinition the name (path) to libraries definition file,
     * if null project libraries are not used
     * @return the builder
     */
    public J2SEProjectBuilder setLibrariesDefinitionFile (final @NullAllowed String librariesDefinition) {
        this.librariesDefinition = librariesDefinition;
        return this;
    }
    
    /**
     * Sets a source level of the project
     * @param sourceLevel the source level,
     * if null the default source level is used.
     * @return the builder
     */
    public J2SEProjectBuilder setSourceLevel (final @NullAllowed SpecificationVersion sourceLevel) {
        this.defaultSourceLevel = sourceLevel;
        return this;
    }
    
    /**
     * Sets a name of build.xml file
     * @param name the name of build.xml file,
     * if null the default 'build.xml' is used
     * @return the builder
     */
    public J2SEProjectBuilder setBuildXmlName(final @NullAllowed String name) {
        this.buildXmlName = name;
        return this;
    }

    /**
     * Sets a name of dist (build artifact) folder
     * @param distFolderName the name of the dist folder
     * if null the default 'dist' is used
     * @return the builder
     * @since 1.49
     */
    @NonNull
    public J2SEProjectBuilder setDistFolder(@NullAllowed final String distFolderName) {
        this.distFolder = distFolderName;
        return this;
    }

    /**
     * Sets a main class template
     * @param mainClassTemplatePath the path to main class template on the system filesystem,
     * if null the default template is used
     * @return the builder
     */
    public J2SEProjectBuilder setMainClassTemplate(final @NullAllowed String mainClassTemplatePath) {
        this.mainClassTemplate = mainClassTemplatePath;
        return this;
    }
    
    /**
     * Adds a JVM arguments
     * @param jvmArgs the arguments to be added
     * @return the builder
     */
    public J2SEProjectBuilder addJVMArguments(final @NonNull String jvmArgs) {
        Parameters.notNull("jvmArgs", jvmArgs); //NOI18N
        if (this.jvmArgs.length() != 0) {
            this.jvmArgs.append(' ');   //NOI18N
        }
        this.jvmArgs.append(jvmArgs);
        return this;
    }
    
    /**
     * Sets a platform to be used for a new project
     * @param platform to be used
     * @return the builder
     * @since 1.53
     */
    public J2SEProjectBuilder setJavaPlatform (@NonNull final JavaPlatform platform) {
        Parameters.notNull("platform", platform);
        if (platform.getProperties().get(PLATFORM_ANT_NAME) == null) {
            throw new IllegalArgumentException("Invalid platform, the platform has no platform.ant.name");  //NOI18N
        }
        this.platform = platform;
        return this;
    }
    
    /**
     * Creates the J2SEProject
     * @return the {@link AntProjectHelper} of the created project
     * @throws IOException when creation fails
     */
    public AntProjectHelper build() throws IOException {
        final FileObject dirFO = FileUtil.createFolder(this.projectDirectory);
        final AntProjectHelper[] h = new AntProjectHelper[1];        
        dirFO.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            @Override
            public void run() throws IOException {
                final SpecificationVersion sourceLevel = getSourceLevel();
                h[0] = createProject(
                        dirFO,
                        name, 
                        sourceLevel,
                        hasDefaultRoots ? "src" : null,     //NOI18N
                        hasDefaultRoots ? "test" : null,    //NOI18N
                        skipTests,
                        buildXmlName,
                        distFolder,
                        mainClass,
                        manifest,
                        manifest == null,
                        librariesDefinition,
                        jvmArgs.toString(),
                        toClassPathElements(compileLibraries),
                        toClassPathElements(runtimeLibraries, "${javac.classpath}:", "${build.classes.dir}"),
                        platform.getProperties().get(PLATFORM_ANT_NAME));   //NOI18N
                final J2SEProject p = (J2SEProject) ProjectManager.getDefault().findProject(dirFO);
                ProjectManager.getDefault().saveProject(p);
                final ReferenceHelper refHelper = p.getReferenceHelper();
                try {
                    ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                        @Override
                        public Void run() throws Exception {
                            registerRoots(h[0], refHelper, sourceRoots, false);
                            registerRoots(h[0], refHelper, testRoots, true);
                            ProjectManager.getDefault().saveProject (p);
                            final List<Library> libsToCopy = new ArrayList<Library>();
                            libsToCopy.addAll(getMandatoryLibraries(skipTests));
                            libsToCopy.addAll(compileLibraries);
                            libsToCopy.addAll(runtimeLibraries);
                            copyRequiredLibraries(h[0], refHelper, libsToCopy);                                                                                    
                            ProjectUtils.getSources(p).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);                            
                            return null;
                        }
                    });
                } catch (MutexException ex) {
                    Exceptions.printStackTrace(ex.getException());
                }
                
                FileObject srcFolder = null;
                if (hasDefaultRoots) {
                    srcFolder = dirFO.createFolder("src") ; // NOI18N
                    if (!skipTests) {
                        dirFO.createFolder("test"); // NOI18N
                    }
                } else if (!sourceRoots.isEmpty()) {
                    srcFolder = FileUtil.toFileObject(sourceRoots.iterator().next());
                }
                if ( mainClass != null && srcFolder != null) {
                    createMainClass(mainClass, srcFolder, mainClassTemplate);
                }
            }
        });        
        return h[0];
    }
    
    private static AntProjectHelper createProject(
            FileObject dirFO,
            String name,
            SpecificationVersion sourceLevel,
            String srcRoot,
            String testRoot,
            boolean skipTests,
            String buildXmlName,
            String distFolder,
            String mainClass,
            String manifestFile,
            boolean isLibrary,
            String librariesDefinition,
            String jvmArgs,
            String[] compileClassPath,
            String[] runtimeClassPath,
            @NonNull final String platformId
            ) throws IOException {
        
        AntProjectHelper h = ProjectGenerator.createProject(dirFO, J2SEProject.TYPE, librariesDefinition);
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(J2SEProject.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        if (!DEFAULT_PLATFORM_ID.equals(platformId)) {
            final Element platformEl = doc.createElementNS(J2SEProject.PROJECT_CONFIGURATION_NAMESPACE, "explicit-platform");   //NOI18N
            final SpecificationVersion jdk13 = new SpecificationVersion("1.3");     //NOI18N
            final boolean supportsExplicitSource = jdk13.compareTo(sourceLevel) < 0;
            platformEl.setAttribute("explicit-source-supported", Boolean.toString(supportsExplicitSource)); //NOI18N
            data.appendChild(platformEl);
        }
        Element sourceRoots = doc.createElementNS(J2SEProject.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");  //NOI18N
        if (srcRoot != null) {
            Element root = doc.createElementNS (J2SEProject.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
            root.setAttribute ("id","src.dir");   //NOI18N
            sourceRoots.appendChild(root);
            ep.setProperty("src.dir", srcRoot); // NOI18N
        }
        data.appendChild (sourceRoots);
        Element testRoots = doc.createElementNS(J2SEProject.PROJECT_CONFIGURATION_NAMESPACE,"test-roots");  //NOI18N
        if (testRoot != null) {
            Element root = doc.createElementNS (J2SEProject.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
            root.setAttribute ("id","test.src.dir");   //NOI18N
            testRoots.appendChild (root);
            ep.setProperty("test.src.dir", testRoot); // NOI18N
        }
        data.appendChild (testRoots);
        h.putPrimaryConfigurationData(data, true);
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_ENABLED, "true"); // NOI18N
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_ENABLED_IN_EDITOR, "false"); // NOI18N
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_RUN_ALL_PROCESSORS, "true"); // NOI18N
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_PROCESSORS_LIST, ""); // NOI18N
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_SOURCE_OUTPUT, "${build.generated.sources.dir}/ap-source-output"); // NOI18N
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_PROCESSOR_OPTIONS, ""); // NOI18N
        ep.setProperty("dist.dir", distFolder != null ? distFolder : "dist"); // NOI18N
        ep.setComment("dist.dir", new String[] {"# " + NbBundle.getMessage(J2SEProjectGenerator.class, "COMMENT_dist.dir")}, false); // NOI18N
        ep.setProperty("dist.jar", "${dist.dir}/" + PropertyUtils.getUsablePropertyName(name) + ".jar"); // NOI18N
        ep.setProperty("javac.classpath", compileClassPath); // NOI18N
        ep.setProperty(ProjectProperties.JAVAC_PROCESSORPATH, new String[] {"${javac.classpath}"}); // NOI18N
        ep.setProperty("javac.test.processorpath", new String[] {"${javac.test.classpath}"}); // NOI18N
        ep.setProperty("build.sysclasspath", "ignore"); // NOI18N
        ep.setComment("build.sysclasspath", new String[] {"# " + NbBundle.getMessage(J2SEProjectGenerator.class, "COMMENT_build.sysclasspath")}, false); // NOI18N
        ep.setProperty("run.classpath", runtimeClassPath);
        ep.setProperty("debug.classpath", new String[] { // NOI18N
            "${run.classpath}", // NOI18N
        });
        ep.setComment("debug.classpath", new String[] { // NOI18N
            "# " + NbBundle.getMessage(J2SEProjectGenerator.class, "COMMENT_debug.transport"),
            "#debug.transport=dt_socket"
        }, false);
        ep.setProperty("jar.compress", "false"); // NOI18N
        if (mainClass != null) {
            ep.setProperty("main.class", mainClass); // NOI18N
        } else if (!isLibrary) {
            ep.setProperty("main.class", ""); // NOI18N
        }
        
        ep.setProperty("javac.compilerargs", ""); // NOI18N
        ep.setComment("javac.compilerargs", new String[] {
            "# " + NbBundle.getMessage(J2SEProjectGenerator.class, "COMMENT_javac.compilerargs"), // NOI18N
        }, false);
        ep.setProperty("javac.source", sourceLevel.toString()); // NOI18N
        ep.setProperty("javac.target", sourceLevel.toString()); // NOI18N
        ep.setProperty("javac.deprecation", "false"); // NOI18N
        ep.setProperty("javac.test.classpath", skipTests ? new String[] { // NOI18N
            "${javac.classpath}:", // NOI18N
            "${build.classes.dir}", // NOI18N
        } : new String[] { // NOI18N
            "${javac.classpath}:", // NOI18N
            "${build.classes.dir}:", // NOI18N
            "${libs.junit.classpath}:", // NOI18N
            "${libs.junit_4.classpath}",  //NOI18N
        });
        ep.setProperty("run.test.classpath", new String[] { // NOI18N
            "${javac.test.classpath}:", // NOI18N
            "${build.test.classes.dir}", // NOI18N
        });
        ep.setProperty("debug.test.classpath", new String[] { // NOI18N
            "${run.test.classpath}", // NOI18N
        });

        ep.setProperty("build.generated.dir", "${build.dir}/generated"); // NOI18N
        ep.setProperty("meta.inf.dir", "${src.dir}/META-INF"); // NOI18N
        
        ep.setProperty("build.dir", "build"); // NOI18N
        ep.setComment("build.dir", new String[] {"# " + NbBundle.getMessage(J2SEProjectGenerator.class, "COMMENT_build.dir")}, false); // NOI18N
        ep.setProperty("build.classes.dir", "${build.dir}/classes"); // NOI18N
        ep.setProperty("build.generated.sources.dir", "${build.dir}/generated-sources"); // NOI18N
        ep.setProperty("build.test.classes.dir", "${build.dir}/test/classes"); // NOI18N
        ep.setProperty("build.test.results.dir", "${build.dir}/test/results"); // NOI18N
        ep.setProperty("build.classes.excludes", "**/*.java,**/*.form"); // NOI18N
        ep.setProperty("dist.javadoc.dir", "${dist.dir}/javadoc"); // NOI18N
        ep.setProperty("platform.active", platformId); // NOI18N

        ep.setProperty(ProjectProperties.RUN_JVM_ARGS, jvmArgs); // NOI18N
        ep.setComment(ProjectProperties.RUN_JVM_ARGS, new String[] {
            "# " + NbBundle.getMessage(J2SEProjectGenerator.class, "COMMENT_run.jvmargs"), // NOI18N
            "# " + NbBundle.getMessage(J2SEProjectGenerator.class, "COMMENT_run.jvmargs_2"), // NOI18N
            "# " + NbBundle.getMessage(J2SEProjectGenerator.class, "COMMENT_run.jvmargs_3"), // NOI18N
        }, false);

        ep.setProperty(J2SEProjectProperties.JAVADOC_PRIVATE, "false"); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_NO_TREE, "false"); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_USE, "true"); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_NO_NAVBAR, "false"); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_NO_INDEX, "false"); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_SPLIT_INDEX, "true"); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_AUTHOR, "false"); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_VERSION, "false"); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_WINDOW_TITLE, ""); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_ENCODING, "${"+J2SEProjectProperties.SOURCE_ENCODING+"}"); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_ADDITIONALPARAM, ""); // NOI18N
        Charset enc = FileEncodingQuery.getDefaultEncoding();
        ep.setProperty(J2SEProjectProperties.SOURCE_ENCODING, enc.name());
        if (manifestFile != null) {
            ep.setProperty("manifest.file", manifestFile); // NOI18N
        }
        if (buildXmlName != null) {
            ep.put(J2SEProjectProperties.BUILD_SCRIPT, buildXmlName);
        }
        ep.setProperty(J2SEProjectProperties.MKDIST_DISABLED, isLibrary ? "true" : "false");
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ep = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        ep.setProperty(ProjectProperties.COMPILE_ON_SAVE, "true"); // NOI18N
        h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
        logUsage();
        return h;
    }
    
    private static void registerRoots(
            final AntProjectHelper helper,
            final ReferenceHelper refHelper,
            final Collection<? extends File> sourceFolders,
            final boolean tests) {
        if (sourceFolders.isEmpty()) {
            //Nothing to do.
            return;
        }
        final Element data = helper.getPrimaryConfigurationData(true);
        final Document doc = data.getOwnerDocument();
        NodeList nl = data.getElementsByTagNameNS(
                J2SEProject.PROJECT_CONFIGURATION_NAMESPACE,
                tests ? "test-roots" : "source-roots");
        assert nl.getLength() == 1;
        final Element sourceRoots = (Element) nl.item(0);
        boolean first = true;
        for (File sourceFolder : sourceFolders) {
            String name;
            if (first) {
                //Name the first src root src.dir to be compatible with NB 4.0
                name = "src";               //NOI18N
                first = false;
            } else {
                name = sourceFolder.getName();
            }
            String propName = (tests ? "test." : "") + name + ".dir";    //NOI18N
            int rootIndex = 1;
            EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            while (props.containsKey(propName)) {
                rootIndex++;
                propName = (tests ? "test." : "") + name + rootIndex + ".dir";   //NOI18N
            }
            String srcReference = refHelper.createForeignFileReference(sourceFolder, JavaProjectConstants.SOURCES_TYPE_JAVA);
            Element root = doc.createElementNS (J2SEProject.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
            root.setAttribute ("id",propName);   //NOI18N
            sourceRoots.appendChild(root);
            props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            props.put(propName,srcReference);
            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props); // #47609
        }                                                            
        helper.putPrimaryConfigurationData(data,true);
    }
    
    private static final String loggerName = "org.netbeans.ui.metrics.j2se"; // NOI18N
    private static final String loggerKey = "USG_PROJECT_CREATE_J2SE"; // NOI18N
    
    // http://wiki.netbeans.org/UsageLoggingSpecification
    private static void logUsage() {
        LogRecord logRecord = new LogRecord(Level.INFO, loggerKey);
        logRecord.setLoggerName(loggerName);
        //logRecord.setParameters(new Object[] {""}); // NOI18N
        Logger.getLogger(loggerName).log(logRecord);
    }

    private SpecificationVersion getSourceLevel () {
        if (defaultSourceLevel != null) {
            return defaultSourceLevel;
        } else {
            final SpecificationVersion v = platform.getSpecification().getVersion();
            return v;
        }
    }

    private String[] toClassPathElements(
            final @NonNull Collection<? extends Library> libraries,
            final @NonNull String... additionalEntries) {
        final String[] result = new String[libraries.size() + additionalEntries.length];
        final Iterator<? extends Library> it = libraries.iterator();
        for (int i=0; it.hasNext(); i++) {
            final Library lib = it.next();
            result[i] = "${libs." + lib.getName() + ".classpath}" + (it.hasNext() || additionalEntries.length != 0 ? ":":"");    //NOI18N
        }
        System.arraycopy(additionalEntries, 0, result, libraries.size(), additionalEntries.length);
        return result;
    }
    
    private static void createMainClass( 
            final @NonNull String mainClassName,
            final @NonNull FileObject srcFolder,
            @NullAllowed String mainClassTemplate) throws IOException {
        
        int lastDotIdx = mainClassName.lastIndexOf( '.' );
        String mName, pName;
        if ( lastDotIdx == -1 ) {
            mName = mainClassName.trim();
            pName = null;
        }
        else {
            mName = mainClassName.substring( lastDotIdx + 1 ).trim();
            pName = mainClassName.substring( 0, lastDotIdx ).trim();
        }
        
        if ( mName.length() == 0 ) {
            return;
        }
        
        if (mainClassTemplate == null) {
            mainClassTemplate = "Templates/Classes/Main.java";  //NOI18N
        }
        final FileObject mainTemplate = FileUtil.getConfigFile(mainClassTemplate);

        if ( mainTemplate == null ) {
            LOG.log(
                Level.WARNING,
                "Template {0} not found!",  //NOI18N
                mainClassTemplate);
            return; // Don't know the template
        }
                
        DataObject mt = DataObject.find( mainTemplate );
        
        FileObject pkgFolder = srcFolder;
        if ( pName != null ) {
            String fName = pName.replace( '.', '/' ); // NOI18N
            pkgFolder = FileUtil.createFolder( srcFolder, fName );        
        }
        DataFolder pDf = DataFolder.findFolder( pkgFolder );        
        DataObject res = mt.createFromTemplate( pDf, mName );
        if (res == null || !res.isValid()) {
            LOG.log(
                Level.WARNING,
                "Template {0} created an invalid DataObject in folder {1}!",  //NOI18N
                new Object[] {
                    mainClassTemplate,
                    FileUtil.getFileDisplayName(pkgFolder)
                });
        }
    }
    
    private static void copyRequiredLibraries(
            final AntProjectHelper h,
            final ReferenceHelper rh,
            final Collection<? extends Library> libraries) throws IOException {
        if (!h.isSharableProject()) {
            return; 
        }
        for (Library library : libraries) {
            final String libName = library.getName();
            if (rh.getProjectLibraryManager().getLibrary(libName) == null 
                && LibraryManager.getDefault().getLibrary(libName) != null) {
                rh.copyLibrary(LibraryManager.getDefault().getLibrary(libName)); // NOI18N
            }
        }        
    }
    
    private static Collection<? extends Library> getMandatoryLibraries(boolean skipTests) {
        final List<Library> result = new ArrayList<Library>();
        final LibraryManager manager = LibraryManager.getDefault();
        for (final String mandatoryLib : skipTests ? new String[] {"CopyLibs"} : new String[] {"junit", "junit_4", "CopyLibs"}) {   //NOI18N
            final Library lib = manager.getLibrary(mandatoryLib);
            if (lib != null) {
                result.add(lib);
            }
        }
        return result;
    }

}
