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

package org.netbeans.modules.java.api.common.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.HashMap;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.WeakListeners;

/**
 * Defines the various class paths for various project types. Each project type has to implement
 * {@link ClassPathProviderImplCustomization}.
 * @author Tomas Zezula, David Konecny, Tomas Mysik
 * @since org.netbeans.modules.java.api.common/0 1.0
 */
public final class ClassPathProviderImpl implements ClassPathProvider, PropertyChangeListener {

    /**
     * <ul>Constants for different paths
     * <li>BUILD_CLASSES_DIR - for binary files of application sources
     * <li>BUILD_TEST_CLASSES_DIR - for binary files of application tests
     * <li>DIST_JAR - for application archive
     * <li>WEB_ROOT - for web sources, typically <samp>web</samp> directory
     * </ul>
     */
    public static enum Path {
        BUILD_CLASSES_DIR,
        BUILD_TEST_CLASSES_DIR,
        DIST_JAR,
        WEB_ROOT
    }

    /**
     * <ul>Constants for different classpath properties
     * <li>JAVAC_CLASSPATH - for compile classpath of sources
     * <li>RUN_CLASSPATH - for execute classpath of sources
     * <li>JAVAC_TEST_CLASSPATH - for compile classpath of tests
     * <li>RUN_TEST_CLASSPATH - for execute classpath of tests
     * </ul>
     */
    public static enum ClasspathProperty {
        JAVAC_CLASSPATH,
        RUN_CLASSPATH,
        JAVAC_TEST_CLASSPATH,
        RUN_TEST_CLASSPATH
    }

    /**
     * <ul>Type of file classpath is required for.
     * <li>UNKNOWN - unknown
     * <li>SOURCE - java source
     * <li>TEST_SOURCE - junit test source
     * <li>CLASS - compiled java class
     * <li>TEST_CLASS - compiled junit test class
     * <li>CLASS_IN_JAR - compiled java class packaged in jar
     * <li>WEB_SOURCE - web source
     * </ul>
     */
    public static enum FileType {
        UNKNOWN,
        SOURCE,         // java source
        TEST_SOURCE,    // junit test source
        CLASS,          // compiled java class
        TEST_CLASS,     // compiled junit test class
        CLASS_IN_JAR,   // compiled java class packaged in jar
        WEB_SOURCE     // web source
    }

    /**
     * Constants for different cached classpaths.
     */
    private static enum ClassPathCache {
        SOURCE_COMPILATION,
        TEST_SOURCE_COMPILATION,
        SOURCE,
        TEST_SOURCE,
        WEB_SOURCE,
        SOURCE_RUNTIME,
        TEST_SOURCE_RUNTIME,
        BOOT,
        PLATFORM
    }

    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final SourceRoots sourceRoots;
    private final SourceRoots testSourceRoots;

    private final ClassPathProviderImplCustomization customization;
    private Context customizationContext = null;

    private final Map<ClassPathCache, ClassPath> cache = new HashMap<ClassPathCache, ClassPath>();
    private final Map<Path, FileObject> dirCache = new HashMap<Path, FileObject>();


    public ClassPathProviderImpl(AntProjectHelper helper, PropertyEvaluator evaluator, SourceRoots sourceRoots,
            SourceRoots testSourceRoots, ClassPathProviderImplCustomization customization) {
        assert helper != null;
        assert evaluator != null;
        assert sourceRoots != null;
        assert testSourceRoots != null;
        assert customization != null;

        this.helper = helper;
        this.evaluator = evaluator;
        this.sourceRoots = sourceRoots;
        this.testSourceRoots = testSourceRoots;
        evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
        this.customization = customization;
    }

    private synchronized Context getCustomizationContext() {
        if (customizationContext == null) {
            customizationContext = new Context(helper, evaluator, sourceRoots,
                    testSourceRoots);
        }
        return customizationContext;
    }

    private synchronized FileObject getDir(Path path) {
        FileObject dir = dirCache.get(path);
        if (dir == null || !dir.isValid()) {
            dir = customization.getDirectory(path);
            if (dir != null) {
                dirCache.put(path, dir);
            }
        }
        return dir;
    }

    private FileObject[] getPrimarySrcPath() {
        return sourceRoots.getRoots();
    }

    private FileObject[] getTestSrcDir() {
        return testSourceRoots.getRoots();
    }

    private FileObject getBuildClassesDir() {
        return getDir(Path.BUILD_CLASSES_DIR);
    }

    private FileObject getDistJar() {
        return getDir(Path.DIST_JAR);
    }

    private FileObject getBuildTestClassesDir() {
        return getDir(Path.BUILD_TEST_CLASSES_DIR);
    }

    private FileObject getDocumentBaseDir() {
        return getDir(Path.WEB_ROOT);
    }

   /**
    * Find what a given file represents.
    * @param file a file in the project
    * @return one of FileType.* constants
    */
   private FileType getType(FileObject file) {
        for (FileObject root : getPrimarySrcPath()) {
            if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                return FileType.SOURCE;
            }
        }
        for (FileObject root : getTestSrcDir()) {
            if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                return FileType.TEST_SOURCE;
            }
        }
        FileObject dir = getDocumentBaseDir();
        if (dir != null && (dir.equals(file) || FileUtil.isParentOf(dir, file))) {
            return FileType.WEB_SOURCE;
        }
        dir = getBuildClassesDir();
        if (dir != null && (dir.equals(file) || FileUtil.isParentOf(dir, file))) {
            return FileType.CLASS;
        }
        dir = getDistJar(); // not really a dir at all, of course
        if (dir != null && dir.equals(FileUtil.getArchiveFile(file))) {
            // XXX check whether this is really the root
            return FileType.CLASS_IN_JAR;
        }
        dir = getBuildTestClassesDir();
        if (dir != null && (dir.equals(file) || FileUtil.isParentOf(dir, file))) {
            return FileType.TEST_CLASS;
        }
        return FileType.UNKNOWN;
    }

    private synchronized ClassPath getCompileTimeClasspath(FileType type) {
        ClassPath cp = null;
        switch (type) {
            case SOURCE:
            case CLASS:
            case WEB_SOURCE:
                // treat all these types as source
                cp = cache.get(ClassPathCache.SOURCE_COMPILATION);
                if (cp == null) {
                    cp = customization.getCompileTimeClasspath(FileType.SOURCE, getCustomizationContext());
                    assert cp != null;
                    cache.put(ClassPathCache.SOURCE_COMPILATION, cp);
                }
                break;

            case TEST_SOURCE:
                cp = cache.get(ClassPathCache.TEST_SOURCE_COMPILATION);
                if (cp == null) {
                    cp = customization.getCompileTimeClasspath(FileType.TEST_SOURCE, getCustomizationContext());
                    assert cp != null;
                    cache.put(ClassPathCache.TEST_SOURCE_COMPILATION, cp);
                }
                break;

            default:
                // XXX any exception?
                break;
        }
        return cp;
    }

    private synchronized ClassPath getRunTimeClasspath(FileType type) {
        ClassPath cp = null;
        switch (type) {
            case SOURCE:
            case CLASS:
            case CLASS_IN_JAR:
            case WEB_SOURCE:
                // treat all these types as source
                cp = cache.get(ClassPathCache.SOURCE_RUNTIME);
                if (cp == null) {
                    cp = customization.getRunTimeClasspath(FileType.SOURCE, getCustomizationContext());
                    assert cp != null;
                    cache.put(ClassPathCache.SOURCE_RUNTIME, cp);
                }
                break;

            case TEST_SOURCE:
            case TEST_CLASS:
                cp = cache.get(ClassPathCache.TEST_SOURCE_RUNTIME);
                if (cp == null) {
                    cp = customization.getRunTimeClasspath(FileType.TEST_SOURCE, getCustomizationContext());
                    assert cp != null;
                    cache.put(ClassPathCache.TEST_SOURCE_RUNTIME, cp);
                }
                break;

            default:
                // XXX any exception?
                break;
        }
        return cp;
    }

    private synchronized ClassPath getSourcePath(FileType type) {
        ClassPath cp = null;
        switch (type) {
            case SOURCE:
            case CLASS:
                // treat all these types as source
                cp = cache.get(ClassPathCache.SOURCE);
                if (cp == null) {
                    cp = customization.getSourcePath(FileType.SOURCE, getCustomizationContext());
                    assert cp != null;
                    cache.put(ClassPathCache.SOURCE, cp);
                }
                break;

            case TEST_SOURCE:
                cp = cache.get(ClassPathCache.TEST_SOURCE);
                if (cp == null) {
                    cp = customization.getSourcePath(FileType.TEST_SOURCE, getCustomizationContext());
                    assert cp != null;
                    cache.put(ClassPathCache.TEST_SOURCE, cp);
                }
                break;

            case WEB_SOURCE:
                cp = cache.get(ClassPathCache.WEB_SOURCE);
                if (cp == null) {
                    cp = customization.getSourcePath(FileType.WEB_SOURCE, getCustomizationContext());
                    assert cp != null;
                    cache.put(ClassPathCache.WEB_SOURCE, cp);
                }
                break;

            default:
                // XXX any exception?
                break;
        }
        return cp;
    }

    private synchronized ClassPath getBootClassPath() {
        ClassPath cp = cache.get(ClassPathCache.BOOT);
        if (cp == null) {
            cp = ClassPathFactory.createClassPath(new BootClassPathImplementation(evaluator));
            cache.put(ClassPathCache.BOOT, cp);
        }
        return cp;
    }

    // XXX will be moved to j2ee/utilities or similar
    /**
     * Get the classpath of the active Java EE platform.
     * @return the classpath of Java EE platform.
     */
    public synchronized ClassPath getJ2eePlatformClassPath() {
        ClassPath cp = cache.get(ClassPathCache.PLATFORM);
        if (cp == null) {
            cp = customization.getJ2eePlatformClassPath(getCustomizationContext());
            assert cp != null;
            cache.put(ClassPathCache.PLATFORM, cp);
        }
        return cp;
    }

    /**
     * @see ClassPathProvider#findClassPath()
     */
    public ClassPath findClassPath(FileObject file, String type) {
        FileType fileType = getType(file);
        if (type.equals(ClassPath.COMPILE)) {
            return getCompileTimeClasspath(fileType);
        } else if (type.equals(ClassPath.EXECUTE)) {
            return getRunTimeClasspath(fileType);
        } else if (type.equals(ClassPath.SOURCE)) {
            return getSourcePath(fileType);
        } else if (type.equals(ClassPath.BOOT)) {
            return getBootClassPath();
        }
        return null;
    }

    /**
     * Return array of all classpaths of the given type in the project.
     * The result is used for example for GlobalPathRegistry registrations.
     * @param type classpath type - boot, compile or source, see {@link ClassPath} for more information.
     * @return array of all classpaths of the given type in the project.
     */
    public ClassPath[] getProjectClassPaths(String type) {
        if (ClassPath.BOOT.equals(type)) {
            return new ClassPath[] {
                getBootClassPath()
            };
        } else if (ClassPath.COMPILE.equals(type)) {
            return new ClassPath[] {
                getCompileTimeClasspath(FileType.SOURCE),
                getCompileTimeClasspath(FileType.TEST_SOURCE),
            };
        } else if (ClassPath.SOURCE.equals(type)) {
            return new ClassPath[] {
                getSourcePath(FileType.SOURCE),
                getSourcePath(FileType.WEB_SOURCE),
                getSourcePath(FileType.TEST_SOURCE),
            };
        }
        assert false;
        return null;
    }

    /**
     * Return the given type of the classpath for the project sources
     * (i.e., excluding tests roots).
     * @param type classpath type - boot, compile or source, see {@link ClassPath} for more information.
     * @return the given type of the classpath for the project sources.
     */
    public ClassPath getProjectSourcesClassPath(String type) {
        if (ClassPath.BOOT.equals(type)) {
            return getBootClassPath();
        } else if (ClassPath.COMPILE.equals(type)) {
            return getCompileTimeClasspath(FileType.SOURCE);
        } else if (ClassPath.SOURCE.equals(type)) {
            return getSourcePath(FileType.SOURCE);
        }
        assert false;
        return null;
    }

    /**
     * Clear the cache if any property changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        dirCache.remove(evt.getPropertyName());
    }

    /**
     * Get the name of the property of the classpath for the given source group and classpath type.
     * The property is searched in sources as well as tests and can be <code>null</code> if the root of the given
     * source group is not found.
     * @param sg source group the property is searched for.
     * @param type classpath type - compile or execute, see {@link ClassPath} for more information.
     * @return the property name or <code>null</code> if nothing found.
     */
    public String getPropertyName(SourceGroup sg, String type) {
        FileObject root = sg.getRootFolder();
        for (FileObject fo : getPrimarySrcPath()) {
            if (root.equals(fo)) {
                if (ClassPath.COMPILE.equals(type)) {
                    return customization.getPropertyName(ClasspathProperty.JAVAC_CLASSPATH);
                } else if (ClassPath.EXECUTE.equals(type)) {
                    return customization.getPropertyName(ClasspathProperty.RUN_CLASSPATH);
                }
                return null;
            }
        }
        for (FileObject fo : getTestSrcDir()) {
            if (root.equals(fo)) {
                if (ClassPath.COMPILE.equals(type)) {
                    return customization.getPropertyName(ClasspathProperty.RUN_CLASSPATH);
                } else if (ClassPath.EXECUTE.equals(type)) {
                    return customization.getPropertyName(ClasspathProperty.RUN_TEST_CLASSPATH);
                }
                return null;
            }
        }
        return null;
    }

    /**
     * This class serves as a container for context provided by {@link ClassPathProviderImpl} instance. It contains its
     * {@link AntProjectHelper}, {@link PropertyEvaluator} etc.
     * @author Tomas Mysik
     * @since org.netbeans.modules.java.api.common/0 1.0
     */
    public static final class Context {
        private final AntProjectHelper helper;
        private final PropertyEvaluator evaluator;
        private final SourceRoots sourceRoots;
        private final SourceRoots testSourceRoots;

        public Context(AntProjectHelper helper, PropertyEvaluator evaluator, SourceRoots sourceRoots,
                SourceRoots testSourceRoots) {
            this.helper = helper;
            this.evaluator = evaluator;
            this.sourceRoots = sourceRoots;
            this.testSourceRoots = testSourceRoots;
        }

        public PropertyEvaluator getEvaluator() {
            return evaluator;
        }

        public AntProjectHelper getHelper() {
            return helper;
        }

        public SourceRoots getSourceRoots() {
            return sourceRoots;
        }

        public SourceRoots getTestSourceRoots() {
            return testSourceRoots;
        }
    }
}
