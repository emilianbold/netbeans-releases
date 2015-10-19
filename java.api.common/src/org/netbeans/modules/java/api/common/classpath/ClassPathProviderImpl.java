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
package org.netbeans.modules.java.api.common.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.project.classpath.support.ProjectClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Mutex;
import org.openide.util.Parameters;
import org.openide.util.Union2;
import org.openide.util.WeakListeners;

/**
 * Defines the various class paths for a J2SE project.
 * @since org.netbeans.modules.java.api.common/1 1.5
 */
public final class ClassPathProviderImpl implements ClassPathProvider {

    private static final String buildGeneratedDir = "build.generated.sources.dir"; // NOI18N
    private static final String[] processorTestClasspath = new String[]{"javac.test.processorpath"};  //NOI18N

    private final AntProjectHelper helper;
    private final File projectDirectory;
    private final PropertyEvaluator evaluator;
    private final SourceRoots sourceRoots;
    private final SourceRoots testSourceRoots;
    private final String buildClassesDir;
    private final String distJar;
    private final String buildTestClassesDir;
    private final String[] javacClasspath;
    private final String[] processorClasspath;
    private final String[] javacTestClasspath;
    private final String[] runClasspath;
    private final String[] runTestClasspath;
    private final String[] endorsedClasspath;
    private final String[] modulePath;
    private final String[] testModulePath;
    private final Union2<String,String[]> platform;
    private final String javacSource;
    /**
     * ClassPaths cache
     * Index -> CP mapping
     * 0  -  source path
     * 1  -  test source path
     * 2  -  class path
     * 3  -  test class path
     * 4  -  execute class path
     * 5  -  test execute class path
     * 6  -  execute class path for dist.jar
     * 7  -  boot class path
     * 8  -  endorsed class path
     * 9  -  processor path
     * 10  - test processor path
     * 11  - module boot path
     * 12  - module compile path
     * 13  - test module compile path
     * 14  - module class path
     * 15  - test module class path
     * 16  - JDK8 class path        - internal only
     * 17  - JDK8 test class path   - internal only
     */
    private final ClassPath[/*@GuardedBy("this")*/] cache = new ClassPath[18];
    private final Map</*@GuardedBy("this")*/String,FileObject> dirCache = new HashMap<>();

    //@GuardedBy("this")
    private MuxClassPathImplementation.Selector selector;

    public ClassPathProviderImpl(AntProjectHelper helper, PropertyEvaluator evaluator, SourceRoots sourceRoots,
                                 SourceRoots testSourceRoots) {
        this(
            helper,
            evaluator,
            sourceRoots,
            testSourceRoots,
            Builder.DEFAULT_BUILD_CLASSES_DIR,
            Builder.DEFAULT_DIST_JAR,
            Builder.DEFAULT_BUILD_TEST_CLASSES_DIR,
            Builder.DEFAULT_JAVAC_CLASS_PATH,
            Builder.DEFAULT_JAVAC_TEST_CLASS_PATH,
            Builder.DEFAULT_RUN_CLASS_PATH,
            Builder.DEFAULT_RUN_TEST_CLASS_PATH);
    }

    public ClassPathProviderImpl(AntProjectHelper helper, PropertyEvaluator evaluator,
            SourceRoots sourceRoots, SourceRoots testSourceRoots,
            String buildClassesDir, String distJar, String buildTestClassesDir,
            String[] javacClasspath, String[] javacTestClasspath, String[] runClasspath,
            String[] runTestClasspath) {
        this(
            helper,
            evaluator,
            sourceRoots,
            testSourceRoots,
            buildClassesDir,
            distJar,
            buildTestClassesDir,
            javacClasspath,
            javacTestClasspath,
            runClasspath,
            runTestClasspath,
            Builder.DEFAULT_ENDORSED_CLASSPATH);
    }
    /**
     * Constructor allowing customization of endorsedClasspath property names.
     * @since org.netbeans.modules.java.api.common/0 1.11
     */
    public ClassPathProviderImpl(AntProjectHelper helper, PropertyEvaluator evaluator,
            SourceRoots sourceRoots, SourceRoots testSourceRoots,
            String buildClassesDir, String distJar, String buildTestClassesDir,
            String[] javacClasspath, String[] javacTestClasspath, String[] runClasspath,
            String[] runTestClasspath, String[] endorsedClasspath) {
        this(
            helper,
            evaluator,
            sourceRoots,
            testSourceRoots,
            buildClassesDir,
            distJar,
            buildTestClassesDir,
            javacClasspath,
            Builder.DEFAULT_PROCESSOR_PATH,
            javacTestClasspath,
            runClasspath,
            runTestClasspath,
            endorsedClasspath);
    }

    /**
     * Constructor allowing customization of processorPath.
     * @since org.netbeans.modules.java.api.common/0 1.14
     */
    public ClassPathProviderImpl(AntProjectHelper helper, PropertyEvaluator evaluator,
            SourceRoots sourceRoots, SourceRoots testSourceRoots,
            String buildClassesDir, String distJar, String buildTestClassesDir,
            String[] javacClasspath, String[] processorPath, String[] javacTestClasspath, String[] runClasspath,
            String[] runTestClasspath, String[] endorsedClasspath) {
        this(
            helper,
            evaluator,
            sourceRoots,
            testSourceRoots,
            buildClassesDir,
            distJar,
            buildTestClassesDir,
            javacClasspath,
            processorPath,
            javacTestClasspath,
            runClasspath,
            runTestClasspath,
            endorsedClasspath,
            Builder.DEFAULT_MODULE_PATH,
            Builder.DEFAULT_TEST_MODULE_PATH,
            Union2.<String,String[]>createFirst(CommonProjectUtils.J2SE_PLATFORM_TYPE),
            Builder.DEFAULT_JAVAC_SOURCE);
    }

    private ClassPathProviderImpl(
        @NonNull final AntProjectHelper helper,
        @NonNull final PropertyEvaluator evaluator,
        @NonNull final SourceRoots sourceRoots,
        @NonNull final SourceRoots testSourceRoots,
        @NonNull final String buildClassesDir,
        @NonNull final String distJar,
        @NonNull final String buildTestClassesDir,
        @NonNull final String[] javacClasspath,
        @NonNull final String[] processorPath,
        @NonNull final String[] javacTestClasspath,
        @NonNull final String[] runClasspath,
        @NonNull final String[] runTestClasspath,
        @NonNull final String[] endorsedClasspath,
        @NonNull final String[] modulePath,
        @NonNull final String[] testModulePath,
        @NonNull final Union2<String,String[]> platform,
        @NonNull final String javacSource) {
        Parameters.notNull("helper", helper);   //NOI18N
        Parameters.notNull("evaluator", evaluator); //NOI18N
        Parameters.notNull("sourceRoots", sourceRoots); //NOI18N
        Parameters.notNull("testSourceRoots", testSourceRoots); //NOI18N
        Parameters.notNull("buildClassesDir", buildClassesDir); //NOI18N
        Parameters.notNull("distJar", distJar); //NOI18N
        Parameters.notNull("buildTestClassesDir", buildTestClassesDir); //NOI18N
        Parameters.notNull("javacClasspath", javacClasspath);   //NOI18N
        Parameters.notNull("processorPath", processorPath); //NOI18N
        Parameters.notNull("javacTestClasspath", javacTestClasspath);   //NOI18N
        Parameters.notNull("runClasspath", runClasspath);   //NOI18N
        Parameters.notNull("runTestClasspath", runTestClasspath);   //NOI18N
        Parameters.notNull("endorsedClasspath", endorsedClasspath); //NOI18N
        Parameters.notNull("modulePath", modulePath);   //NOI18N
        Parameters.notNull("testModulePath", testModulePath);   //NOI18N
        Parameters.notNull("platform", platform); //NOI18N
        Parameters.notNull("javacSource", javacSource); //NOI18N
        this.helper = helper;
        this.projectDirectory = FileUtil.toFile(helper.getProjectDirectory());
        assert this.projectDirectory != null;
        this.evaluator = evaluator;
        this.sourceRoots = sourceRoots;
        this.testSourceRoots = testSourceRoots;
        this.buildClassesDir = buildClassesDir;
        this.distJar = distJar;
        this.buildTestClassesDir = buildTestClassesDir;
        this.javacClasspath = javacClasspath;
        this.processorClasspath = processorPath;
        this.javacTestClasspath = javacTestClasspath;
        this.runClasspath = runClasspath;
        this.runTestClasspath = runTestClasspath;
        this.endorsedClasspath = endorsedClasspath;
        this.modulePath = modulePath;
        this.testModulePath = testModulePath;
        this.platform = platform;
        this.javacSource = javacSource;
    }

    /**
     * Builder to create ClassPathProviderImpl.
     * @since 1.59
     */
    public static final class Builder {

        private static final String DEFAULT_BUILD_CLASSES_DIR = "build.classes.dir";   //NOI18N
        private static final String DEFAULT_BUILD_TEST_CLASSES_DIR = "build.test.classes.dir"; // NOI18N
        private static final String DEFAULT_DIST_JAR = "dist.jar"; // NOI18N
        private static final String[] DEFAULT_JAVAC_CLASS_PATH = new String[]{"javac.classpath"};    //NOI18N
        private static final String[] DEFAULT_PROCESSOR_PATH = new String[]{ProjectProperties.JAVAC_PROCESSORPATH};    //NOI18N
        private static final String[] DEFAULT_JAVAC_TEST_CLASS_PATH = new String[]{"javac.test.classpath"};  //NOI18N
        private static final String[] DEFAULT_RUN_CLASS_PATH = new String[]{"run.classpath"};    //NOI18N
        private static final String[] DEFAULT_RUN_TEST_CLASS_PATH = new String[]{"run.test.classpath"};  //NOI18N
        private static final String[] DEFAULT_ENDORSED_CLASSPATH = new String[]{ProjectProperties.ENDORSED_CLASSPATH};  //NOI18N
        private static final String[] DEFAULT_MODULE_PATH = new String[]{ProjectProperties.MODULE_COMPILE_PATH};
        private static final String[] DEFAULT_TEST_MODULE_PATH = new String[] {ProjectProperties.TEST_MODULE_COMPILE_PATH};
        private static final String DEFAULT_JAVAC_SOURCE = ProjectProperties.JAVAC_SOURCE;

        private final AntProjectHelper helper;
        private final PropertyEvaluator evaluator;
        private final SourceRoots sourceRoots;
        private final SourceRoots testSourceRoots;

        private String platformType = CommonProjectUtils.J2SE_PLATFORM_TYPE;
        private String buildClassesDir = DEFAULT_BUILD_CLASSES_DIR;
        private String buildTestClassesDir = DEFAULT_BUILD_TEST_CLASSES_DIR;
        private String distJar = DEFAULT_DIST_JAR;
        private String[] javacClasspath = DEFAULT_JAVAC_CLASS_PATH;
        private String[] processorPath = DEFAULT_PROCESSOR_PATH;
        private String[] javacTestClasspath = DEFAULT_JAVAC_TEST_CLASS_PATH;
        private String[] runClasspath = DEFAULT_RUN_CLASS_PATH;
        private String[] runTestClasspath = DEFAULT_RUN_TEST_CLASS_PATH;
        private String[] endorsedClasspath = DEFAULT_ENDORSED_CLASSPATH;
        private String[] modulePath = DEFAULT_MODULE_PATH;
        private String[] testModulePath = DEFAULT_TEST_MODULE_PATH;
        private String[] bootClasspathProperties;
        private String javacSource = DEFAULT_JAVAC_SOURCE;

        private Builder(
            @NonNull final AntProjectHelper helper,
            @NonNull final PropertyEvaluator evaluator,
            @NonNull final SourceRoots sourceRoots,
            @NonNull final SourceRoots testSourceRoots) {
            Parameters.notNull("helper", helper);   //NOI18N
            Parameters.notNull("evaluator", evaluator); //NOI18N
            Parameters.notNull("sourceRoots", sourceRoots); //NOI18N
            Parameters.notNull("testSourceRoots", testSourceRoots); //NOI18N
            this.helper = helper;
            this.evaluator = evaluator;
            this.sourceRoots = sourceRoots;
            this.testSourceRoots = testSourceRoots;
        }

        /**
         * Sets a {@link JavaPlatform} type for boot classpath lookup.
         * @param platformType the type of {@link JavaPlatform}, by default "j2se"
         * @return {@link Builder}
         */
        @NonNull
        public Builder setPlatformType(@NonNull final String platformType) {
            Parameters.notNull("platformType", platformType);   //NOI18N
            this.platformType = platformType;
            return this;
        }

        /**
         * Sets a property name containing build classes directory.
         * @param buildClassesDirProperty the name of property containing the build classes directory, by default "build.classes.dir"
         * @return {@link Builder}
         */
        @NonNull
        public Builder setBuildClassesDirProperty(@NonNull final String buildClassesDirProperty) {
            Parameters.notNull("buildClassesDirProperty", buildClassesDirProperty); //NOI18N
            this.buildClassesDir = buildClassesDirProperty;
            return this;
        }

        /**
         * Sets a property name containing build test classes directory.
         * @param buildTestClassesDirProperty the name of property containing the build test classes directory, by default "build.test.classes.dir"
         * @return {@link Builder}
         */
        @NonNull
        public Builder setBuildTestClassesDirProperty(@NonNull final String buildTestClassesDirProperty) {
            Parameters.notNull("buildTestClassesDirProperty", buildTestClassesDirProperty); //NOI18N
            this.buildTestClassesDir = buildTestClassesDirProperty;
            return this;
        }

        /**
         * Sets a property name containing the distribution jar.
         * @param distJarProperty the name of property containing the distribution jar reference, by default "dist.jar"
         * @return {@link Builder}
         */
        @NonNull
        public Builder setDistJarProperty(@NonNull final String distJarProperty) {
            Parameters.notNull("distJarProperty", distJarProperty); //NOI18N
            this.distJar = distJarProperty;
            return this;
        }

        /**
         * Sets javac classpath properties for source roots.
         * @param javacClassPathProperties the names of properties containing the compiler classpath for sources, by default "javac.classpath"
         * @return {@link Builder}
         */
        @NonNull
        public Builder setJavacClassPathProperties(@NonNull final String[] javacClassPathProperties) {
            Parameters.notNull("javacClassPathProperties", javacClassPathProperties);   //NOI18N
            this.javacClasspath = Arrays.copyOf(javacClassPathProperties, javacClassPathProperties.length);
            return this;
        }

        /**
         * Sets javac processor path properties for source roots.
         * @param processorPathProperties the names of properties containing the compiler processor path for sources, by default "javac.processorpath"
         * @return {@link Builder}
         */
        @NonNull
        public Builder setProcessorPathProperties(@NonNull final String[] processorPathProperties) {
            Parameters.notNull("processorPathProperties", processorPathProperties);
            this.processorPath = Arrays.copyOf(processorPathProperties, processorPathProperties.length);
            return this;
        }

        /**
         * Sets javac classpath properties for test roots.
         * @param javacTestClasspathProperties  the names of properties containing the compiler classpath for tests, by default "javac.test.classpath"
         * @return {@link Builder}
         */
        @NonNull
        public Builder setJavacTestClasspathProperties(@NonNull final String[] javacTestClasspathProperties) {
            Parameters.notNull("javacTestClasspathProperties", javacTestClasspathProperties);   //NOI18N
            this.javacTestClasspath = Arrays.copyOf(javacTestClasspathProperties, javacTestClasspathProperties.length);
            return this;
        }

        /**
         * Sets runtime classpath properties for source roots.
         * @param runClasspathProperties the names of properties containing the runtime classpath for sources, by default "run.classpath"
         * @return {@link Builder}
         */
        @NonNull
        public Builder setRunClasspathProperties(@NonNull final String[] runClasspathProperties) {
            Parameters.notNull("runClasspathProperties", runClasspathProperties);   //NOI18N
            this.runClasspath = Arrays.copyOf(runClasspathProperties, runClasspathProperties.length);
            return this;
        }

        /**
         * Sets runtime classpath properties for test roots.
         * @param runTestClasspathProperties  the names of properties containing the runtime classpath for tests, by default "run.test.classpath"
         * @return {@link Builder}
         */
        @NonNull
        public Builder setRunTestClasspathProperties(@NonNull final String[] runTestClasspathProperties) {
            Parameters.notNull("runTestClasspathProperties", runTestClasspathProperties);   //NOI18N
            this.runTestClasspath = Arrays.copyOf(runTestClasspathProperties, runTestClasspathProperties.length);
            return this;
        }

        /**
         * Sets endorsed classpath properties.
         * @param endorsedClasspathProperties the names of properties containing the endorsed classpath, by default "endorsed.classpath"
         * @return {@link Builder}
         */
        @NonNull
        public Builder setEndorsedClasspathProperties(@NonNull final String[] endorsedClasspathProperties) {
            Parameters.notNull("endorsedClasspathProperties", endorsedClasspathProperties); //NOI18N
            this.endorsedClasspath = Arrays.copyOf(endorsedClasspathProperties, endorsedClasspathProperties.length);
            return this;
        }

        /**
         * Sets module path properties.
         * @param modulePathProperties  the names of properties containing the module path, by default {@link ProjectProperties#MODULE_COMPILE_PATH}
         * @return {@link Builder}
         * @since 1.77
         */
        @NonNull
        public Builder setModulepathProperties(@NonNull final String[] modulePathProperties) {
            Parameters.notNull("modulePathProperties", modulePathProperties);
            this.modulePath = Arrays.copyOf(modulePathProperties, modulePathProperties.length);
            return this;
        }

        /**
         * Sets test module path properties.
         * @param modulePathProperties  the names of properties containing the test module path, by default {@link ProjectProperties#TEST_MODULE_COMPILE_PATH}
         * @return {@link Builder}
         * @since 1.77
         */
        @NonNull
        public Builder setTestModulepathProperties(@NonNull final String[] modulePathProperties) {
            Parameters.notNull("modulePathProperties", modulePathProperties);
            this.testModulePath = Arrays.copyOf(modulePathProperties, modulePathProperties.length);
            return this;
        }

        /**
         * Sets boot classpath properties.
         * Some project types do not use {@link JavaPlatform#getBootstrapLibraries()} as boot classpath but
         * have a project property specifying the boot classpath. Setting the boot classpath properties
         * causes that the {@link Project}'s boot classpath is not taken from {@link Project}'s {@link JavaPlatform}
         * but from these properties.
         * @param bootClasspathProperties  the names of properties containing the boot classpath
         * @return {@link Builder}
         * @since 1.67
         */
        @NonNull
        public Builder setBootClasspathProperties(@NonNull final String... bootClasspathProperties) {
            Parameters.notNull("bootClasspathProperties", bootClasspathProperties); //NOI18N
            this.bootClasspathProperties = Arrays.copyOf(bootClasspathProperties, bootClasspathProperties.length);
            return this;
        }

        /**
         * Sets javac source level property.
         * @param javacSource the name of the property containing the javac source level
         * @return {@link Builder}
         * @since 1.76
         */
        @NonNull
        public Builder setJavacSourceProperty(@NonNull final String javacSource) {
            Parameters.notNull("javacSource", javacSource); //NOI18N
            this.javacSource = javacSource;
            return this;
        }

        /**
         * Creates a configured {@link ClassPathProviderImpl}.
         * @return the {@link ClassPathProviderImpl}
         */
        @NonNull
        public ClassPathProviderImpl build() {
            final Union2<String,String[]> platform =
                bootClasspathProperties == null ?
                    Union2.<String,String[]>createFirst(platformType) :
                    Union2.<String,String[]>createSecond(bootClasspathProperties);
            return new ClassPathProviderImpl (
                helper,
                evaluator,
                sourceRoots,
                testSourceRoots,
                buildClassesDir,
                distJar,
                buildTestClassesDir,
                javacClasspath,
                processorPath,
                javacTestClasspath,
                runClasspath,
                runTestClasspath,
                endorsedClasspath,
                modulePath,
                testModulePath,
                platform,
                javacSource);
        }

        @NonNull
        public static Builder create(
            @NonNull final AntProjectHelper helper,
            @NonNull final PropertyEvaluator evaluator,
            @NonNull final SourceRoots sourceRoots,
            @NonNull final SourceRoots testSourceRoots) {
            return new Builder(helper, evaluator, sourceRoots, testSourceRoots);
        }

    }

    
    private FileObject getDir(final String propname) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<FileObject>() {
            public FileObject run() {
                synchronized (ClassPathProviderImpl.this) {
                    FileObject fo = ClassPathProviderImpl.this.dirCache.get(propname);
                    if (fo == null ||  !fo.isValid()) {
                        String prop = evaluator.getProperty(propname);
                        if (prop != null) {
                            fo = helper.resolveFileObject(prop);
                            ClassPathProviderImpl.this.dirCache.put (propname, fo);
                        }
                    }
                    return fo;
                }
            }});
    }
    
    private FileObject[] getPrimarySrcPath() {
        return this.sourceRoots.getRoots();
    }
    
    private FileObject[] getTestSrcDir() {
        return this.testSourceRoots.getRoots();
    }
    
    private FileObject getBuildClassesDir() {
        return getDir(buildClassesDir);
    }

    private FileObject getBuildGeneratedDir() {
        return getDir(buildGeneratedDir);
    }

    private FileObject getDistJar() {
        return getDir(distJar);
    }
    
    private FileObject getBuildTestClassesDir() {
        return getDir(buildTestClassesDir);
    }
    
    private FileObject getAnnotationProcessingSourceOutputDir() {
        return getDir(ProjectProperties.ANNOTATION_PROCESSING_SOURCE_OUTPUT);
    }

    /**
     * Find what a given file represents.
     * @param file a file in the project
     * @return one of: <dl>
     *         <dt>0</dt> <dd>normal source</dd>
     *         <dt>1</dt> <dd>test source</dd>
     *         <dt>2</dt> <dd>built class (unpacked)</dd>
     *         <dt>3</dt> <dd>built test class</dd>
     *         <dt>4</dt> <dd>built class (in dist JAR)</dd>
     *         <dt>-1</dt> <dd>something else</dd>
     *         </dl>
     */
    private int getType(FileObject file) {
        FileObject[] srcPath = getPrimarySrcPath();
        for (int i=0; i < srcPath.length; i++) {
            FileObject root = srcPath[i];
            if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                return 0;
            }
        }        
        srcPath = getTestSrcDir();
        for (int i=0; i< srcPath.length; i++) {
            FileObject root = srcPath[i];
            if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                return 1;
            }
        }
        FileObject dir = getBuildClassesDir();
        if (dir != null && (dir.equals(file) || FileUtil.isParentOf(dir, file))) {
            return 2;
        }
        dir = getDistJar(); // not really a dir at all, of course
        if (dir != null && dir.equals(FileUtil.getArchiveFile(file))) {
            // XXX check whether this is really the root
            return 4;
        }
        dir = getBuildTestClassesDir();
        if (dir != null && (dir.equals(file) || FileUtil.isParentOf(dir,file))) {
            return 3;
        }
        dir = getBuildGeneratedDir();
        if (dir != null && FileUtil.isParentOf(dir, file) /* but dir != file */) { // #105645
            dir = getAnnotationProcessingSourceOutputDir();
            if (dir != null && (dir.equals(file) || FileUtil.isParentOf(dir, file))) { //not the annotation processing source output
                return -1;
            }
            return 0;
        }
        return -1;
    }
    
    private ClassPath getCompileTimeClasspath(FileObject file) {
        int type = getType(file);
        return this.getCompileTimeClasspath(type);
    }
    
    private synchronized ClassPath getCompileTimeClasspath(int type) {
        if (type < 0 || type > 1) {
            // Not a source file.
            return null;
        }
        ClassPath cp = cache[2+type];
        if (cp == null) {
            final ClassPath filteredModulePath = ClassPathFactory.createClassPath(
                ModuleClassPaths.createModuleInfoBasedPath(
                    getModuleCompilePath(type),
                    type == 0 ? sourceRoots : testSourceRoots));
            final ClassPath filteredModulePathWithLegacyClassPath = org.netbeans.spi.java.classpath.support.ClassPathSupport.createProxyClassPath(
                filteredModulePath,
                getJava8ClassPath(type));
            cp = ClassPathFactory.createClassPath(new MuxClassPathImplementation(
                new ClassPath[]{
                    getJava8ClassPath(type),
                    filteredModulePathWithLegacyClassPath
                },
                getSourceLevelSelector()));
            cache[2+type] = cp;
        }
        return cp;
    }
    
    private ClassPath getProcessorClasspath(FileObject file) {
        int type = getType(file);
        return this.getProcessorClasspath(type);
    }

    private synchronized ClassPath getProcessorClasspath(int type) {
        if (type < 0 || type > 1) {
            // Not a source file.
            return null;
        }
        ClassPath cp = cache[9+type];
        if ( cp == null) {
            if (type == 0) {
                cp = ClassPathFactory.createClassPath(
                    ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                    projectDirectory, evaluator, processorClasspath)); // NOI18N
            }
            else {
                cp = ClassPathFactory.createClassPath(
                    ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                    projectDirectory, evaluator, processorTestClasspath)); // NOI18N
            }
            cache[9+type] = cp;
        }
        return cp;
    }

    private ClassPath getRunTimeClasspath(FileObject file) {
        int type = getType(file);
        if (type < 0 || type > 4) {
            // Unregistered file, or in a JAR.
            // For jar:file:$projdir/dist/*.jar!/**/*.class, it is misleading to use
            // run.classpath since that does not actually contain the file!
            // (It contains file:$projdir/build/classes/ instead.)
            return null;
        }
        return getRunTimeClasspath(type);
    }
    
    private synchronized ClassPath getRunTimeClasspath(final int type) {
        int cacheIndex;
        if (type == 0 || type == 2) {
            cacheIndex = 4;
        } else if (type == 1 || type == 3) {
            cacheIndex = 5;
        } else if (type == 4) {
            cacheIndex = 6;
        } else {
            return null;
        }
        
        ClassPath cp = cache[cacheIndex];
        if ( cp == null) {
            if (type == 0 || type == 2) {
                cp = ClassPathFactory.createClassPath(
                    ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                    projectDirectory, evaluator, runClasspath)); // NOI18N
            }
            else if (type == 1 || type == 3) {
                cp = ClassPathFactory.createClassPath(
                    ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                    projectDirectory, evaluator, runTestClasspath)); // NOI18N
            }
            else if (type == 4) {
                final String[] props = new String[runClasspath.length+1];
                System.arraycopy(runClasspath, 0, props, 1, runClasspath.length);
                props[0] = distJar;
                cp = ClassPathFactory.createClassPath(
                    ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                    projectDirectory, evaluator, props));
            }
            cache[cacheIndex] = cp;
        }
        return cp;
    }
    
    private synchronized ClassPath getEndorsedClasspath() {
        ClassPath cp = cache[8];
        if ( cp == null) {
            cp = ClassPathFactory.createClassPath(
                ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                    projectDirectory, evaluator, endorsedClasspath)); // NOI18N
            cache[8] = cp;
        }
        return cp;
    }

    private ClassPath getSourcepath(FileObject file) {
        int type = getType(file);
        return this.getSourcepath(type);
    }
    
    @CheckForNull
    private synchronized ClassPath getSourcepath(final int type) {
        if (type < 0 || type > 1) {
            return null;
        }
        ClassPath cp = cache[type];
        if (cp == null) {
            switch (type) {
                case 0:
                    cp = ClassPathFactory.createClassPath(ClassPathSupportFactory.createSourcePathImplementation (this.sourceRoots, helper, evaluator));
                    break;
                case 1:
                    cp = ClassPathFactory.createClassPath(ClassPathSupportFactory.createSourcePathImplementation (this.testSourceRoots, helper, evaluator));
                    break;
                default:
                    throw new IllegalStateException("Invalid classpath type: " + type); //NOI18N
            }
            cache[type] = cp;
        }
        return cp;
    }
    
    private synchronized ClassPath getBootClassPath() {
        ClassPath cp = cache[7];
        if ( cp == null ) {
            if (platform.hasFirst()) {
                cp = ClassPathFactory.createClassPath(
                    ClassPathSupportFactory.createBootClassPathImplementation(
                        evaluator,
                        getEndorsedClasspath(),
                        platform.first()));
                final ClassPath moduleSytemPath = ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                    getModuleBootPath(),
                    sourceRoots));
                cp = ClassPathFactory.createClassPath(new MuxClassPathImplementation(
                    new ClassPath[] {
                        cp,
                        moduleSytemPath
                    },
                    getSourceLevelSelector()));
            } else {
                assert platform.hasSecond();
                cp = org.netbeans.spi.java.classpath.support.ClassPathSupport.createProxyClassPath(
                    getEndorsedClasspath(),
                    ClassPathFactory.createClassPath(
                        ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                            projectDirectory,
                            evaluator,
                            platform.second())));
            }
            cache[7] = cp;
        }
        return cp;
    }

    @NonNull
    private synchronized ClassPath getModuleBootPath() {
        ClassPath cp = cache[11];
        if ( cp == null ) {
            if (platform.hasFirst()) {
                cp = ClassPathFactory.createClassPath(
                    ClassPathSupportFactory.createBootClassPathImplementation(
                        evaluator,
                        getEndorsedClasspath(),
                        platform.first()));
                final ClassPath moduleSytemPath = ClassPathFactory.createClassPath(ModuleClassPaths.createPlatformModulePath(
                    evaluator,
                    platform.first()));
                cp = ClassPathFactory.createClassPath(new MuxClassPathImplementation(
                    new ClassPath[]{
                        cp,
                        moduleSytemPath
                    },
                    getSourceLevelSelector()));
            } else {
                cp = ClassPath.EMPTY;
            }
            cache[11] = cp;
        }
        return cp;
    }

    @CheckForNull
    private ClassPath getModuleCompilePath(@NonNull final FileObject fo) {
        final int type = getType(fo);
        if (type < 0 || type > 1) {
            return null;
        }
        return getModuleCompilePath(type);
    }

    @NonNull
    private synchronized ClassPath getModuleCompilePath(final int type) {
        assert type >=0 && type <=1;
        ClassPath cp = cache[12+type];
        if ( cp == null ) {
            final ClassPath modules = ClassPathFactory.createClassPath(ModuleClassPaths.createPropertyBasedModulePath(
                    projectDirectory,
                    evaluator,
                    type == 0 ? modulePath : testModulePath));
            cp = ClassPathFactory.createClassPath(new MuxClassPathImplementation(
                new ClassPath[] {
                    ClassPath.EMPTY,
                    modules
                },
                getSourceLevelSelector()));
            cache[12+type] = cp;
        }
        return cp;
    }

    @CheckForNull
    private ClassPath getModuleLegacyClassPath(@NonNull final FileObject fo) {
        final int type = getType(fo);
        if (type < 0 || type > 1) {
            return null;
        }
        return getModuleLegacyClassPath(type);
    }

    @NonNull
    private synchronized ClassPath getModuleLegacyClassPath(final int type) {
        assert type >=0 && type <=1;
        ClassPath cp = cache[14+type];
        if (cp == null) {
            cp = ClassPathFactory.createClassPath(new MuxClassPathImplementation(
                new ClassPath[]{
                    ClassPath.EMPTY,
                    getJava8ClassPath(type),
                },
                getSourceLevelSelector()));
            cache[14+type] = cp;
        }
        return cp;
    }

    @NonNull
    private synchronized ClassPath getJava8ClassPath(int type) {
        assert type >= 0 && type <=1;
        ClassPath cp = cache[16+type];
        if (cp == null) {
            cp = ClassPathFactory.createClassPath(
                    ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                    projectDirectory, evaluator, type == 0 ? javacClasspath : javacTestClasspath));
            cache[16+type] = cp;
        }
        return cp;
    }

    public ClassPath findClassPath(FileObject file, String type) {
        if (type.equals(ClassPath.COMPILE)) {
            return getCompileTimeClasspath(file);
        } else if (type.equals(JavaClassPathConstants.PROCESSOR_PATH)) {
            return getProcessorClasspath(file);
        } else if (type.equals(ClassPath.EXECUTE)) {
            return getRunTimeClasspath(file);
        } else if (type.equals(ClassPath.SOURCE)) {
            return getSourcepath(file);
        } else if (type.equals(ClassPath.BOOT)) {
            return getBootClassPath();
        } else if (type.equals(ClassPathSupport.ENDORSED)) {
            return getEndorsedClasspath();
        } else if (type.equals(JavaClassPathConstants.MODULE_BOOT_PATH)) {
            return getModuleBootPath();
        } else if (type.equals(JavaClassPathConstants.MODULE_COMPILE_PATH)) {
            return getModuleCompilePath(file);
        } else if (type.equals(JavaClassPathConstants.MODULE_CLASS_PATH)) {
            return getModuleLegacyClassPath(file);
        }else {
            return null;
        }
    }
    
    /**
     * Returns array of all classpaths of the given type in the project.
     * The result is used for example for GlobalPathRegistry registrations.
     */
    public ClassPath[] getProjectClassPaths(final String type) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<ClassPath[]>() {
            public ClassPath[] run() {
                if (ClassPath.BOOT.equals(type)) {
                    return new ClassPath[]{getBootClassPath()};
                }
                if (ClassPath.COMPILE.equals(type)) {
                    ClassPath[] l = new ClassPath[2];
                    l[0] = getCompileTimeClasspath(0);
                    l[1] = getCompileTimeClasspath(1);
                    return l;
                }
                if (ClassPath.SOURCE.equals(type)) {
                    ClassPath[] l = new ClassPath[2];
                    l[0] = getSourcepath(0);
                    l[1] = getSourcepath(1);
                    return l;
                }
                if (ClassPath.EXECUTE.equals(type)) {
                    ClassPath[] l = new ClassPath[2];
                    l[0] = getRunTimeClasspath(0);
                    l[1] = getRunTimeClasspath(1);
                    return l;
                }
                if (JavaClassPathConstants.PROCESSOR_PATH.equals(type)) {
                    ClassPath[] l = new ClassPath[2];
                    l[0] = getProcessorClasspath(0);
                    l[1] = getProcessorClasspath(1);
                    return l;
                }
                if (JavaClassPathConstants.MODULE_BOOT_PATH.equals(type)) {
                    return new ClassPath[] {getModuleBootPath()};
                }
                if (JavaClassPathConstants.MODULE_COMPILE_PATH.equals(type)) {
                    ClassPath[] l = new ClassPath[2];
                    l[0] = getModuleCompilePath(0);
                    l[1] = getModuleCompilePath(1);
                    return l;
                }
                if (JavaClassPathConstants.MODULE_CLASS_PATH.equals(type)) {
                    ClassPath[] l = new ClassPath[2];
                    l[0] = getModuleLegacyClassPath(0);
                    l[1] = getModuleLegacyClassPath(1);
                    return l;
                }
                assert false;
                return null;
            }});
    }

    /**
     * Returns the given type of the classpath for the project sources
     * (i.e., excluding tests roots).
     */
    public ClassPath getProjectSourcesClassPath(String type) {
        if (ClassPath.BOOT.equals(type)) {
            return getBootClassPath();
        }
        if (ClassPath.COMPILE.equals(type)) {
            return getCompileTimeClasspath(0);
        }
        if (JavaClassPathConstants.PROCESSOR_PATH.equals(type)) {
            return getProcessorClasspath(0);
        }
        if (ClassPath.SOURCE.equals(type)) {
            return getSourcepath(0);
        }
        if (ClassPath.EXECUTE.equals(type)) {
            return getRunTimeClasspath(0);
        }
        assert false : "Unknown classpath type: " + type;   //NOI18N
        return null;
    }

    public String[] getPropertyName (final SourceRoots roots, final String type) {
        if (ClassPathSupport.ENDORSED.equals(type)) {
            return endorsedClasspath;
        }
        if (roots.isTest()) {
            if (ClassPath.COMPILE.equals(type)) {
                return javacTestClasspath;
            }
            else if (ClassPath.EXECUTE.equals(type)) {
                return runTestClasspath;
            } else if (JavaClassPathConstants.PROCESSOR_PATH.equals(type)) {
                return processorTestClasspath;
            }
            else {
                return null;
            }
        }
        else {
            if (ClassPath.COMPILE.equals(type)) {
                return javacClasspath;
            }
            else if (ClassPath.EXECUTE.equals(type)) {
                return runClasspath;
            }
            else if (JavaClassPathConstants.PROCESSOR_PATH.equals(type)) {
                return processorClasspath;
            }
            else {
                return null;
            }
        }
    }
    
    public String[] getPropertyName (SourceGroup sg, String type) {
        if (ClassPathSupport.ENDORSED.equals(type)) {
            return endorsedClasspath;
        }
        FileObject root = sg.getRootFolder();
        FileObject[] path = getPrimarySrcPath();
        for (int i=0; i<path.length; i++) {
            if (root.equals(path[i])) {
                if (ClassPath.COMPILE.equals(type)) {
                    return javacClasspath;
                }
                else if (ClassPath.EXECUTE.equals(type)) {
                    return runClasspath;
                }
                else if (JavaClassPathConstants.PROCESSOR_PATH.equals(type)) {
                    return processorClasspath;
                }
                else {
                    return null;
                }
            }
        }
        path = getTestSrcDir();
        for (int i=0; i<path.length; i++) {
            if (root.equals(path[i])) {
                if (ClassPath.COMPILE.equals(type)) {
                    return javacTestClasspath;
                }
                else if (ClassPath.EXECUTE.equals(type)) {
                    return runTestClasspath;
                }
                else {
                    return null;
                }
            }
        }
        return null;
    }

    @NonNull
    private synchronized MuxClassPathImplementation.Selector getSourceLevelSelector() {
        if (selector == null) {
            selector = new SourceLevelSelector(evaluator, javacSource);
        }
        return selector;
    }

    private static final class SourceLevelSelector implements MuxClassPathImplementation.Selector, PropertyChangeListener {

        private static final SpecificationVersion JDK9 = new SpecificationVersion("1.9");   //NOI18N

        private final PropertyEvaluator eval;
        private final String sourceLevelPropName;
        private final PropertyChangeSupport listeners;
        private volatile int active;


        SourceLevelSelector(
                @NonNull final PropertyEvaluator eval,
                @NonNull final String sourceLevelPropName) {
            Parameters.notNull("eval", eval);   //NOI18N
            Parameters.notNull("sourceLevelPropName", sourceLevelPropName); //NOI18N
            this.eval = eval;
            this.sourceLevelPropName = sourceLevelPropName;
            this.active = -1;
            this.listeners = new PropertyChangeSupport(this);
            this.eval.addPropertyChangeListener(WeakListeners.propertyChange(this, this.eval));
        }

        @Override
        public int getActiveIndex() {
            int res = active;
            if (res == -1) {
                res = 0;
                final String sl = eval.getProperty(this.sourceLevelPropName);
                if (sl != null) {
                    try {
                        if (JDK9.compareTo(new SpecificationVersion(sl)) <= 0) {
                            res = 1;
                        }
                    } catch (NumberFormatException e) {
                        //pass
                    }
                }
                active = res;
            }
            return res;
        }

        @Override
        public void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            this.listeners.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            this.listeners.removePropertyChangeListener(listener);
        }

        @Override
        public void propertyChange(@NonNull final PropertyChangeEvent evt) {
            final String propName = evt.getPropertyName();
            if (propName == null || sourceLevelPropName.equals(propName)) {
                this.active = -1;
                this.listeners.firePropertyChange(PROP_ACTIVE_INDEX, null, null);
            }
        }
    }
}
