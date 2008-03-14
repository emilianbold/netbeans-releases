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
import java.io.File;
import java.util.Map;
import java.util.HashMap;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.project.classpath.support.ProjectClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 * Defines the various class paths for various project types.
 * @author Tomas Zezula, David Konecny, Tomas Mysik
 */
public final class DefaultClassPathProviderImpl implements ClassPathProvider, PropertyChangeListener {

    private final AntProjectHelper helper;
    private final File projectDirectory;
    private final PropertyEvaluator evaluator;
    private final SourceRoots sourceRoots;
    private final SourceRoots testSourceRoots;

    private final Properties properties;

    private final Map<ClassPathCache, ClassPath> cache = new HashMap<ClassPathCache, ClassPath>();
    private final Map<String, FileObject> dirCache = new HashMap<String, FileObject>();

    private static enum FileType {
        UNKNOWN,
        SOURCE,         // java source
        TEST_SOURCE,    // junit test source
        CLASS,          // compiled java class
        TEST_CLASS,     // compiled junit test class
        CLASS_IN_JAR    // compiled java class packaged in jar
    }

    /**
     * Constants for different cached classpaths.
     */
    private static enum ClassPathCache {
        SOURCE_COMPILATION,
        TEST_SOURCE_COMPILATION,
        SOURCE,
        TEST_SOURCE,
        SOURCE_RUNTIME,
        TEST_SOURCE_RUNTIME,
        BOOT
    }

    public DefaultClassPathProviderImpl(AntProjectHelper helper, PropertyEvaluator evaluator, SourceRoots sourceRoots,
            SourceRoots testSourceRoots, Properties properties) {
        Parameters.notNull("helper", helper);
        Parameters.notNull("evaluator", evaluator);
        Parameters.notNull("sourceRoots", sourceRoots);
        Parameters.notNull("testSourceRoots", testSourceRoots);
        Parameters.notNull("properties", properties);

        this.helper = helper;
        this.projectDirectory = FileUtil.toFile(helper.getProjectDirectory());
        this.evaluator = evaluator;
        this.sourceRoots = sourceRoots;
        this.testSourceRoots = testSourceRoots;
        evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
        this.properties = properties;
    }

    private synchronized FileObject getDir(String propname) {
        FileObject fo = dirCache.get(propname);
        if (fo == null || !fo.isValid()) {
            String prop = evaluator.getProperty(propname);
            if (prop != null) {
                fo = helper.resolveFileObject(prop);
                dirCache.put(propname, fo);
            }
        }
        return fo;
    }

    private FileObject[] getPrimarySrcPath() {
        return sourceRoots.getRoots();
    }

    private FileObject[] getTestSrcDir() {
        return testSourceRoots.getRoots();
    }

    private FileObject getBuildClassesDir() {
        return getDir(properties.buildClassesDir);
    }

    private FileObject getDistJar() {
        return getDir(properties.distJar);
    }

    private FileObject getBuildTestClassesDir() {
        return getDir(properties.buildTestClassesDir);
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
        FileObject dir = getBuildClassesDir();
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
                // treat all these types as source
                cp = cache.get(ClassPathCache.SOURCE_COMPILATION);
                if (cp == null) {
                    cp = ClassPathFactory.createClassPath(
                            ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                            projectDirectory, evaluator, properties.sourceCompileTimeClassPath));
                    cache.put(ClassPathCache.SOURCE_COMPILATION, cp);
                }
                break;

            case TEST_SOURCE:
                cp = cache.get(ClassPathCache.TEST_SOURCE_COMPILATION);
                if (cp == null) {
                    cp = ClassPathFactory.createClassPath(
                            ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                            projectDirectory, evaluator, properties.testSourceCompileTimeClassPath));
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
            // XXX this one as well? see ClassPathProviderImpl in J2SE, line 221
            case CLASS_IN_JAR:
                // treat all these types as source
                cp = cache.get(ClassPathCache.SOURCE_RUNTIME);
                if (cp == null) {
                    cp = ClassPathFactory.createClassPath(
                            ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                            projectDirectory, evaluator, properties.sourceRunTimeClassPath));
                    cache.put(ClassPathCache.SOURCE_RUNTIME, cp);
                }
                break;

            case TEST_SOURCE:
            case TEST_CLASS:
                cp = cache.get(ClassPathCache.TEST_SOURCE_RUNTIME);
                if (cp == null) {
                    cp = ClassPathFactory.createClassPath(
                            ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                            projectDirectory, evaluator, properties.testSourceRunTimeClassPath));
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
                    cp = ClassPathFactory.createClassPath(new SourcePathImplementation(sourceRoots, helper, evaluator));
                    cache.put(ClassPathCache.SOURCE, cp);
                }
                break;

            case TEST_SOURCE:
                cp = cache.get(ClassPathCache.TEST_SOURCE);
                if (cp == null) {
                    cp = ClassPathFactory.createClassPath(
                            new SourcePathImplementation(testSourceRoots, helper, evaluator));
                    cache.put(ClassPathCache.TEST_SOURCE, cp);
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
     * Clear the related cache if any property changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        dirCache.remove(evt.getPropertyName());
    }

    /**
     * Clear directory cache. This method can be suitable while listening to some properties,
     * e.g. {@link org.netbeans.spi.project.support.ant.AntProjectListener#configurationXmlChanged(AntProjectEvent)}.
     */
    public synchronized void clearDirectoryCache() {
        dirCache.clear();
    }

    /**
     * Class holding different properties like "javac.classpath" etc.
     */
    public static final class Properties {
        final String buildClassesDir;
        final String buildTestClassesDir;
        final String distJar;

        final String[] sourceCompileTimeClassPath;
        final String[] testSourceCompileTimeClassPath;
        final String[] sourceRunTimeClassPath;
        final String[] testSourceRunTimeClassPath;

        public Properties(String buildClassesDir, String buildTestClassesDir, String distJar,
                String[] sourceCompileTimeClassPath, String[] testSourceCompileTimeClassPath,
                String[] sourceRunTimeClassPath, String[] testSourceRunTimeClassPath) {
            Parameters.notNull("buildClassesDir", buildClassesDir);
            Parameters.notNull("buildTestClassesDir", buildTestClassesDir);
            Parameters.notNull("distJar", distJar);
            Parameters.notNull("sourceCompileTimeClassPath", sourceCompileTimeClassPath);
            Parameters.notNull("testSourceCompileTimeClassPath", testSourceCompileTimeClassPath);
            Parameters.notNull("sourceRunTimeClassPath", sourceRunTimeClassPath);
            Parameters.notNull("testSourceRunTimeClassPath", testSourceRunTimeClassPath);

            this.buildClassesDir = buildClassesDir;
            this.buildTestClassesDir = buildTestClassesDir;
            this.distJar = distJar;
            this.sourceCompileTimeClassPath = sourceCompileTimeClassPath;
            this.testSourceCompileTimeClassPath = testSourceCompileTimeClassPath;
            this.sourceRunTimeClassPath = sourceRunTimeClassPath;
            this.testSourceRunTimeClassPath = testSourceRunTimeClassPath;
        }
    }
}
