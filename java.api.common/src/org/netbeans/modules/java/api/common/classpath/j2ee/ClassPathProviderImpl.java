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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.api.common.classpath.j2ee;

import org.netbeans.modules.java.api.common.classpath.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

import java.util.List;
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

// will be moved to j2ee.common
// XXX improve javadoc
/**
 * Defines the various class paths for a web project.
 * @since 1.21
 */
public final class ClassPathProviderImpl implements ClassPathProvider, PropertyChangeListener {

    private final DefaultClassPathProviderImpl delegate;
    private final AntProjectHelper helper;
    private final File projectDirectory;
    private final PropertyEvaluator evaluator;
    private final SourceRoots sourceRoots;
    private final SourceRoots testSourceRoots;

    private final Properties properties;

    private final Map<ClassPathCache, ClassPath> cache = new HashMap<ClassPathCache, ClassPath>();
    private final Map<String, FileObject> dirCache = new HashMap<String, FileObject>();

    /**
     * Type of file classpath is required for.
     */
    private static enum FileType {
        UNKNOWN,
        WEB_SOURCE      // web source
    }

    /**
     * Constants for different cached classpaths.
     */
    private static enum ClassPathCache {
        SOURCE_COMPILATION,
        SOURCE_RUNTIME,
        WEB_SOURCE,
        PLATFORM
    }

    public ClassPathProviderImpl(AntProjectHelper helper, PropertyEvaluator evaluator, SourceRoots sourceRoots,
            SourceRoots testSourceRoots, Properties properties) {

        delegate = new DefaultClassPathProviderImpl(helper, evaluator, sourceRoots, testSourceRoots,
                properties.delegate);
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

    private FileObject getDocumentBaseDir() {
        if (properties.webDocBaseDir == null) {
            return null;
        }
        return getDir(properties.webDocBaseDir);
    }

     /**
     * Find what a given file represents.
     * @param file a file in the project
     * @return one of FileType.* constants
     */
   private FileType getType(FileObject file) {
        FileObject dir = getDocumentBaseDir();
        if (dir != null && (dir.equals(file) || FileUtil.isParentOf(dir, file))) {
            return FileType.WEB_SOURCE;
        }
        return FileType.UNKNOWN;
    }

    private synchronized ClassPath getCompileTimeClasspath(FileType type) {
        ClassPath cp = null;
        switch (type) {
            case WEB_SOURCE:
                // XXX should be same like for (java) SOURCE
                // treat this type as source
                cp = cache.get(ClassPathCache.SOURCE_COMPILATION);
                if (cp == null) {
                    cp = ClassPathFactory.createClassPath(
                            ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                            projectDirectory, evaluator, properties.sourceCompileTimeClassPath));
                    cache.put(ClassPathCache.SOURCE_COMPILATION, cp);
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
            case WEB_SOURCE:
                // XXX should be same like for (java) SOURCE
                // treat this type as source
                cp = cache.get(ClassPathCache.SOURCE_RUNTIME);
                if (cp == null) {
                    cp = ClassPathFactory.createClassPath(
                            ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                            projectDirectory, evaluator, properties.sourceRunTimeClassPath));
                    cache.put(ClassPathCache.SOURCE_RUNTIME, cp);
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
            case WEB_SOURCE:
                cp = cache.get(ClassPathCache.WEB_SOURCE);
                if (cp == null) {
                    List<ClassPath> classPaths;
                    if (properties.additionalSourcePaths != null) {
                        classPaths = Arrays.asList(properties.additionalSourcePaths);
                    } else {
                        classPaths = new ArrayList<ClassPath>(1);
                    }
                    classPaths.add(ClassPathFactory.createClassPath(
                            new SourcePathImplementation(sourceRoots, helper, evaluator)));
                    cp = org.netbeans.spi.java.classpath.support.ClassPathSupport.createProxyClassPath(
                            classPaths.toArray(new ClassPath[classPaths.size()]));
                    cache.put(ClassPathCache.WEB_SOURCE, cp);
                }
                break;

            default:
                // XXX any exception?
                break;
        }
        return cp;
    }

    public synchronized ClassPath getJ2eePlatformClassPath() {
        ClassPath cp = cache.get(ClassPathCache.PLATFORM);
        if (cp == null) {
            cp = ClassPathFactory.createClassPath(
                    ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                    projectDirectory, evaluator, properties.javaEEPlatformClasspath));
            cache.put(ClassPathCache.PLATFORM, cp);
        }
        return cp;
    }

    public ClassPath findClassPath(FileObject file, String type) {
        ClassPath cp = null;
        FileType fileType = getType(file);
        switch (fileType) {
            case WEB_SOURCE:
                // XXX should be solved somehow better
                if (type.equals(ClassPath.COMPILE)) {
                    cp = getCompileTimeClasspath(fileType);
                } else if (type.equals(ClassPath.EXECUTE)) {
                    cp = getRunTimeClasspath(fileType);
                } else if (type.equals(ClassPath.SOURCE)) {
                    cp = getSourcePath(fileType);
                }
                break;
            default:
                cp = delegate.findClassPath(file, type);
                break;
        }
        return cp;
    }

    /**
     * Return array of all classpaths of the given type in the project.
     * The result is used for example for GlobalPathRegistry registrations.
     * @param type classpath type - boot, compile or source, see {@link ClassPath} for more information.
     * @return array of all classpaths of the given type in the project.
     */
    public ClassPath[] getProjectClassPaths(String type) {
        if (ClassPath.SOURCE.equals(type)) {
            // XXX
            List<ClassPath> sourcePaths = Arrays.asList(delegate.getProjectClassPaths(type));
            sourcePaths.add(1, getSourcePath(FileType.WEB_SOURCE));
            return sourcePaths.toArray(new ClassPath[sourcePaths.size()]);
        }
        return delegate.getProjectClassPaths(type);
    }

    /**
     * Return the given type of the classpath for the project sources
     * (i.e., excluding tests roots).
     * @param type classpath type - boot, compile or source, see {@link ClassPath} for more information.
     * @return the given type of the classpath for the project sources.
     */
    public ClassPath getProjectSourcesClassPath(String type) {
        return delegate.getProjectSourcesClassPath(type);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (properties.webDocBaseDir.equals(evt.getPropertyName())) {
            dirCache.remove(evt.getPropertyName());
        } else {
            delegate.propertyChange(evt);
        }
    }

    /**
     * Clear directory cache. This method can be suitable while listening to some properties,
     * e.g. {@link org.netbeans.spi.project.support.ant.AntProjectListener#configurationXmlChanged(AntProjectEvent)}.
     */
    public synchronized void clearDirectoryCache() {
        delegate.clearDirectoryCache();
        dirCache.clear();
    }

    /**
     * Class holding different properties like "javac.classpath" etc.
     */
    public static final class Properties {

        final DefaultClassPathProviderImpl.Properties delegate;

        final String webDocBaseDir;

        final String[] sourceCompileTimeClassPath;
        final String[] sourceRunTimeClassPath;
        final String[] javaEEPlatformClasspath;

        final ClassPath[] additionalSourcePaths;

        // XXX add javadoc
        public Properties(String buildClassesDir, String buildTestClassesDir, String distJar, String webDocBaseDir,
                String[] sourceCompileTimeClassPath, String[] testSourceCompileTimeClassPath,
                String[] sourceRunTimeClassPath, String[] testSourceRunTimeClassPath,
                String[] javaEEPlatformClasspath, ClassPath[] additionalSourcePaths) {
            Parameters.notNull("javaEEPlatformClasspath", javaEEPlatformClasspath); // NOI18N

            delegate = new DefaultClassPathProviderImpl.Properties(buildClassesDir, buildTestClassesDir, distJar,
                    sourceCompileTimeClassPath, testSourceCompileTimeClassPath, sourceRunTimeClassPath,
                    testSourceRunTimeClassPath);
            this.webDocBaseDir = webDocBaseDir;
            this.sourceCompileTimeClassPath = sourceCompileTimeClassPath;
            this.sourceRunTimeClassPath = sourceRunTimeClassPath;
            this.javaEEPlatformClasspath = javaEEPlatformClasspath;
            this.additionalSourcePaths = additionalSourcePaths;
        }
    }
}
