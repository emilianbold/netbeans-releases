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
package org.netbeans.modules.php.project.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.gsfpath.api.classpath.ClassPath;
import org.netbeans.modules.gsfpath.spi.classpath.ClassPathFactory;
import org.netbeans.modules.gsfpath.spi.classpath.ClassPathProvider;
import org.netbeans.modules.php.project.PhpSources;
import org.netbeans.modules.php.project.api.PhpSourcePath.FileType;
import org.netbeans.modules.php.project.classpath.support.ProjectClassPathSupport;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.ui.options.PhpOptions;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 * Defines the various (BOOT and SOURCE) class paths for a PHP project.
 */
public final class ClassPathProviderImpl implements ClassPathProvider, PhpSourcePathImplementation, PropertyChangeListener {

    /**
     * Constants for different cached classpaths.
     */
    private static enum ClassPathCache {
        PLATFORM,
        SOURCE,
    }

    private final AntProjectHelper helper;
    private final File projectDirectory;
    private final PropertyEvaluator evaluator;
    private final PhpSources sourceRoots;

    // GuardedBy(dirCache)
    private final Map<String, List<FileObject>> dirCache = new HashMap<String, List<FileObject>>();
    // GuardedBy(cache)
    private final Map<ClassPathCache, ClassPath> cache = new EnumMap<ClassPathCache, ClassPath>(ClassPathCache.class);

    public ClassPathProviderImpl(AntProjectHelper helper, PropertyEvaluator evaluator, PhpSources sources) {
        this.helper = helper;
        projectDirectory = FileUtil.toFile(helper.getProjectDirectory());
        assert projectDirectory != null;
        this.evaluator = evaluator;
        this.sourceRoots = sources;
        evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
    }

    private List<FileObject> getDirs(String propname) {
        synchronized (dirCache) {
            List<FileObject> dirs = dirCache.get(propname);
            if (!checkDirs(dirs)) {
                String prop = evaluator.getProperty(propname);
                if (prop == null) {
                    return Collections.<FileObject>emptyList();
                }
                String[] paths = PropertyUtils.tokenizePath(prop);
                dirs = new ArrayList<FileObject>(paths.length);
                for (String path : paths) {
                    FileObject resolvedFile = helper.resolveFileObject(path);
                    if (resolvedFile != null) {
                        dirs.add(resolvedFile);
                    }
                }
                dirCache.put(propname, dirs);
            }
            return dirs;
        }
    }

    private boolean checkDirs(List<FileObject> dirs) {
        if (dirs == null) {
            return false;
        }
        for (FileObject fo : dirs) {
            if (!fo.isValid()) {
                return false;
            }
        }
        return true;
    }

    private List<FileObject> getPlatformPath() {
        return getDirs(PhpProjectProperties.INCLUDE_PATH);
    }

    private FileObject getSrcPath() {
        List<FileObject> dirs = getDirs(PhpProjectProperties.SRC_DIR);
        if (dirs.size() == 0) {
            // non-existing directory??
            return null;
        }
        assert dirs.size() == 1; // one source directory is allowed
        return dirs.get(0);
    }

    public FileType getFileType(FileObject file) {
        Parameters.notNull("file", file);

        for (FileObject dir : CommonPhpSourcePath.getInternalPath()) {
            if (dir.equals(file) || FileUtil.isParentOf(dir, file)) {
                return FileType.INTERNAL;
            }
        }
        
//        for (FileObject dir : PhpSourcePath.getPreindexedFolders()) {
//            if (dir.equals(file) || FileUtil.isParentOf(dir, file)) {
//                return FileType.INTERNAL;
//            }
//        }
        
        for (FileObject dir : getPlatformPath()) {
            if (dir.equals(file) || FileUtil.isParentOf(dir, file)) {
                return FileType.INCLUDE;
            }
        }
        FileObject path = getSrcPath();
        if (path != null
                && (path.equals(file) || FileUtil.isParentOf(path, file))) {
            return FileType.SOURCE;
        }
        return FileType.UNKNOWN;
    }

    public List<FileObject> getIncludePath() {
        return new ArrayList<FileObject>(getPlatformPath());
    }

    public FileObject resolveFile(FileObject directory, String fileName) {
        FileObject resolved = directory.getFileObject(fileName);
        if (resolved != null) {
            return resolved;
        }
        for (FileObject dir : getPlatformPath()) {
            resolved = dir.getFileObject(fileName);
            if (resolved != null) {
                return resolved;
            }
        }
        return null;
    }

    private ClassPath getSourcePath(FileObject file) {
        return getSourcePath(getFileType(file));
    }

    private ClassPath getSourcePath(FileType type) {
        ClassPath cp = null;
        switch (type) {
            case SOURCE:
                synchronized (cache) {
                    cp = cache.get(ClassPathCache.SOURCE);
                    if (cp == null) {
                        // XXX sources cannot be changed
                        cp = ClassPathFactory.createClassPath(
                                ProjectClassPathSupport.createPropertyBasedClassPathImplementation(projectDirectory,
                                evaluator, new String[] {PhpProjectProperties.SRC_DIR}));
                        cache.put(ClassPathCache.SOURCE, cp);
                    }
                }
                break;
            default:
                // XXX any exception?
                break;
        }
        return cp;
    }

    private ClassPath getBootClassPath() {
        // because of global include path, we need to ensure that it is known for poperty evaluator
        //  (=> need to be written in global properties)
        PhpOptions.getInstance().getPhpGlobalIncludePath();
        ClassPath cp;
        // #141746
        synchronized (cache) {
            cp = cache.get(ClassPathCache.PLATFORM);
            if (cp == null) {
                List<FileObject> internalFolders = CommonPhpSourcePath.getInternalPath();
                ClassPath internalClassPath =
                        org.netbeans.modules.gsfpath.spi.classpath.support.ClassPathSupport.createClassPath(
                        internalFolders.toArray(new FileObject[internalFolders.size()]));
                ClassPath includePath = ClassPathFactory.createClassPath(
                        ProjectClassPathSupport.createPropertyBasedClassPathImplementation(projectDirectory, evaluator,
                        new String[] {PhpProjectProperties.INCLUDE_PATH}));
                cp = org.netbeans.modules.gsfpath.spi.classpath.support.ClassPathSupport.createProxyClassPath(
                        internalClassPath, includePath);
                cache.put(ClassPathCache.PLATFORM, cp);
            }
        }
        return cp;
    }

    public ClassPath findClassPath(FileObject file, String type) {
        if (type.equals(ClassPath.BOOT)) {
            return getBootClassPath();
        } else if (type.equals(ClassPath.SOURCE)) {
            return getSourcePath(file);
        } else if (type.equals(ClassPath.COMPILE)) {
            // ???
            return getBootClassPath();
        } else if (type.equals("js/library")) { // NOI18N
            return getSourcePath(FileType.SOURCE);
        }
        assert false : "Unknown classpath type requested: " + type;
        return null;
    }

    /**
     * Returns array of all classpaths of the given type in the project.
     * The result is used for example for GlobalPathRegistry registrations.
     */
    public ClassPath[] getProjectClassPaths(String type) {
        if (ClassPath.BOOT.equals(type)) {
            return new ClassPath[] {getBootClassPath()};
        } else if (ClassPath.SOURCE.equals(type)) {
            return new ClassPath[] {getSourcePath(FileType.SOURCE)};
        }
        assert false : "Unknown classpath type requested: " + type;
        return null;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        synchronized (dirCache) {
            dirCache.remove(evt.getPropertyName());
        }
    }
}
