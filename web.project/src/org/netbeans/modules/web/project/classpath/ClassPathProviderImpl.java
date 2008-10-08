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

package org.netbeans.modules.web.project.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Map;
import java.util.HashMap;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.common.project.classpath.BootClassPathImplementation;
import org.netbeans.modules.j2ee.common.project.classpath.SourcePathImplementation;
import org.netbeans.modules.j2ee.common.project.ui.ProjectProperties;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.project.classpath.support.ProjectClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.WeakListeners;

/**
 * Defines the various class paths for a web project.
 */
public final class ClassPathProviderImpl implements ClassPathProvider, PropertyChangeListener {
    
    private final AntProjectHelper helper;
    private final File projectDirectory;
    private final PropertyEvaluator evaluator;
    private final SourceRoots sourceRoots;
    private final SourceRoots testSourceRoots;
    private final Map<ClassPathCache, ClassPath> cache = new HashMap<ClassPathCache, ClassPath>();

    private final Map<String,FileObject> dirCache = new HashMap<String,FileObject>();

    /**
     * Type of file classpath is required for.
     */
    private static enum FileType {
        SOURCE,         // java source
        TEST_SOURCE,    // junit test source
        CLASS,          // compiled java class
        TEST_CLASS,     // compiled junit test class
        CLASS_IN_JAR,   // compiled java class packaged in jar
        WEB_SOURCE,     // web source
        UNKNOWN }

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
        PLATFORM,
        PACKAGED, // #131785
    }
    
    public ClassPathProviderImpl(AntProjectHelper helper, PropertyEvaluator evaluator, SourceRoots sourceRoots, SourceRoots testSourceRoots) {
        this.helper = helper;
        this.projectDirectory = FileUtil.toFile(helper.getProjectDirectory());
        assert this.projectDirectory != null;
        this.evaluator = evaluator;
        this.sourceRoots = sourceRoots;
        this.testSourceRoots = testSourceRoots;
        evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
    }

    private FileObject getDir(final String propname) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<FileObject>() {
            public FileObject run() {
                synchronized (ClassPathProviderImpl.this) {
                    FileObject fo = (FileObject) ClassPathProviderImpl.this.dirCache.get (propname);
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
        return getDir(ProjectProperties.BUILD_CLASSES_DIR);
    }
    
    private FileObject getDistJar() {
        return getDir(WebProjectProperties.DIST_WAR);
    }
    
    private FileObject getBuildTestClassesDir() {
        return getDir(ProjectProperties.BUILD_TEST_CLASSES_DIR);
    }

    private FileObject getDocumentBaseDir() {
        return getDir(WebProjectProperties.WEB_DOCBASE_DIR);
    }
    
     /**
     * Find what a given file represents.
     * @param file a file in the project
     * @return one of FileType.* constants
     */
   private FileType getType(FileObject file) {
        FileObject[] srcPath = getPrimarySrcPath();
        for (int i=0; i < srcPath.length; i++) {
            FileObject root = srcPath[i];
            if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                return FileType.SOURCE;
            }
        }        
        srcPath = getTestSrcDir();
        for (int i=0; i< srcPath.length; i++) {
            FileObject root = srcPath[i];
            if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                return FileType.TEST_SOURCE;
            }
        }
        FileObject dir = getDocumentBaseDir();
        if (dir != null && (dir.equals(file) || FileUtil.isParentOf(dir,file))) {
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
        if (dir != null && (dir.equals(file) || FileUtil.isParentOf(dir,file))) {
            return FileType.TEST_CLASS;
        }
        
        return FileType.UNKNOWN;
    }
    
    private synchronized ClassPath getCompileTimeClasspath(FileType type) {        
        if (type == FileType.SOURCE || type == FileType.CLASS || type == FileType.WEB_SOURCE)
        {
            // treat all these types as source:
            ClassPath cp = cache.get(ClassPathCache.SOURCE_COMPILATION);
            if (cp == null)
            {
                cp = ClassPathFactory.createClassPath(ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                    projectDirectory, evaluator, new String[] {"javac.classpath", WebProjectProperties.J2EE_PLATFORM_CLASSPATH }));
                cache.put(ClassPathCache.SOURCE_COMPILATION, cp);
            }
            return cp;
        }
        if (type == FileType.TEST_SOURCE)
        {
            ClassPath cp = cache.get(ClassPathCache.TEST_SOURCE_COMPILATION);
            if (cp == null)
            {
                cp = ClassPathFactory.createClassPath(ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                    projectDirectory, evaluator, new String[] {"javac.test.classpath", WebProjectProperties.J2EE_PLATFORM_CLASSPATH }));
                cache.put(ClassPathCache.TEST_SOURCE_COMPILATION, cp);
            }
            return cp;
        }
        return null;
    }
    
    // packaged classpath = compilation time classpath - J2EE platform classpath
    private synchronized ClassPath getPackagedClasspath(FileType type) {        
        if (type == FileType.SOURCE || type == FileType.CLASS || type == FileType.WEB_SOURCE) {
            // treat all these types as source:
            ClassPath cp = cache.get(ClassPathCache.PACKAGED);
            if (cp == null) {
                cp = ClassPathFactory.createClassPath(ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                    projectDirectory, evaluator, new String[] {"javac.classpath"})); // NOI18N
                cache.put(ClassPathCache.PACKAGED, cp);
            }
            return cp;
        }
        return null;
    }
    
    private synchronized ClassPath getRunTimeClasspath(FileType type) {
        if (type == FileType.SOURCE || type == FileType.CLASS || 
            type == FileType.CLASS_IN_JAR || type == FileType.WEB_SOURCE)
        {
            // treat all these types as source:
            ClassPath cp = cache.get(ClassPathCache.SOURCE_RUNTIME);
            if (cp == null)
            {
                cp = 
                    ClassPathFactory.createClassPath(ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                    projectDirectory, evaluator, new String[] {"debug.classpath", WebProjectProperties.J2EE_PLATFORM_CLASSPATH }));
                cache.put(ClassPathCache.SOURCE_RUNTIME, cp);
            }
            return cp;
        }
        if (type == FileType.TEST_SOURCE || type == FileType.TEST_CLASS)
        {
            // treat all these types as source:
            ClassPath cp = cache.get(ClassPathCache.TEST_SOURCE_RUNTIME);
            if (cp == null)
            {
                cp = ClassPathFactory.createClassPath(ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                    projectDirectory, evaluator, new String[] {"run.test.classpath", WebProjectProperties.J2EE_PLATFORM_CLASSPATH }));
                cache.put(ClassPathCache.TEST_SOURCE_RUNTIME, cp);
            }
            return cp;
        }
        return null;
    }
    
    private synchronized ClassPath getSourcepath(FileType type) {
        if (type == FileType.SOURCE || type == FileType.CLASS)
        {
            // treat all these types as source:
            ClassPath cp = cache.get(ClassPathCache.SOURCE);
            if (cp == null)
            {
                cp = ClassPathFactory.createClassPath(new SourcePathImplementation(this.sourceRoots,helper, evaluator));
                cache.put(ClassPathCache.SOURCE, cp);
            }
            return cp;
        }
        if (type == FileType.TEST_SOURCE)
        {
            ClassPath cp = cache.get(ClassPathCache.TEST_SOURCE);
            if (cp == null)
            {
                cp = ClassPathFactory.createClassPath(new SourcePathImplementation(this.testSourceRoots,helper, evaluator));
                cache.put(ClassPathCache.TEST_SOURCE, cp);
            }
            return cp;
        }
        if (type == FileType.WEB_SOURCE)
        {
            ClassPath cp = cache.get(ClassPathCache.WEB_SOURCE);
            if (cp == null)
            {
                cp = ClassPathSupport.createProxyClassPath(new ClassPath[] {
                        ClassPathFactory.createClassPath(new JspSourcePathImplementation(helper, evaluator)),
                        ClassPathFactory.createClassPath(new SourcePathImplementation (this.sourceRoots, helper, evaluator)),
                    });
                cache.put(ClassPathCache.WEB_SOURCE, cp);

            }
            return cp;
        }
        return null;
    }
    
    private synchronized ClassPath getBootClassPath() {
        ClassPath cp = cache.get(ClassPathCache.BOOT);
        if (cp == null ) {
            cp = ClassPathFactory.createClassPath(new BootClassPathImplementation(evaluator));
            cache.put(ClassPathCache.BOOT, cp);
        }
        return cp;
    }
    
    public synchronized ClassPath getJ2eePlatformClassPath() {
        ClassPath cp = cache.get(ClassPathCache.PLATFORM);
        if (cp == null) {
            cp = ClassPathFactory.createClassPath(ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                    projectDirectory, evaluator, new String[] {WebProjectProperties.J2EE_PLATFORM_CLASSPATH }));
            cache.put(ClassPathCache.PLATFORM, cp);
        }
        return cp;
    }
    
    public ClassPath findClassPath(FileObject file, String type) {
        FileType fileType = getType(file);
        if (type.equals(ClassPath.COMPILE)) {
            return getCompileTimeClasspath(fileType);
        } else if (type.equals(ClassPath.EXECUTE)) {
            return getRunTimeClasspath(fileType);
        } else if (type.equals(ClassPath.SOURCE)) {
            return getSourcepath(fileType);
        } else if (type.equals(ClassPath.BOOT)) {
            return getBootClassPath();
        } else if (type.equals("classpath/packaged")) { // NOI18N
            return getPackagedClasspath(fileType);
        } else {
            return null;
        }
    }
    
    /**
     * Returns array of all classpaths of the given type in the project.
     * The result is used for example for GlobalPathRegistry registrations.
     */
    public ClassPath[] getProjectClassPaths(String type) {
        if (ClassPath.BOOT.equals(type)) {
            return new ClassPath[]{getBootClassPath()};
        }
        if (ClassPath.COMPILE.equals(type)) {
            ClassPath[] l = new ClassPath[2];
            l[0] = getCompileTimeClasspath(FileType.SOURCE);
            l[1] = getCompileTimeClasspath(FileType.TEST_SOURCE);
            return l;
        }
        if (ClassPath.SOURCE.equals(type)) {
            ClassPath[] l = new ClassPath[3];
            l[0] = getSourcepath(FileType.SOURCE);
            l[1] = getSourcepath(FileType.WEB_SOURCE);
            l[2] = getSourcepath(FileType.TEST_SOURCE);
            return l;
        }
        assert false;
        return null;
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
            return getCompileTimeClasspath(FileType.SOURCE);
        }
        if (ClassPath.SOURCE.equals(type)) {
            return getSourcepath(FileType.SOURCE);
        }
        assert false;
        return null;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        dirCache.remove(evt.getPropertyName());
    }
    
    public String getPropertyName (SourceGroup sg, String type) {
        FileObject root = sg.getRootFolder();
        FileObject[] path = getPrimarySrcPath();
        for (int i=0; i<path.length; i++) {
            if (root.equals(path[i])) {
                if (ClassPath.COMPILE.equals(type)) {
                    return ProjectProperties.JAVAC_CLASSPATH;
                }
                else if (ClassPath.EXECUTE.equals(type)) {
                    return WebProjectProperties.DEBUG_CLASSPATH;
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
                    return ProjectProperties.JAVAC_TEST_CLASSPATH;
                }
                else if (ClassPath.EXECUTE.equals(type)) {
                    return ProjectProperties.RUN_TEST_CLASSPATH;
                }
                else {
                    return null;
                }
            }
        }
        return null;
    }

}

