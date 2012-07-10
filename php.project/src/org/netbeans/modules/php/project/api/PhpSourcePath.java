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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.api;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.project.classpath.CommonPhpSourcePath;
import org.netbeans.modules.php.project.classpath.IncludePathClassPathProvider;
import org.netbeans.modules.php.project.classpath.PhpSourcePathImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Parameters;

/**
 * @author Tomas Mysik
 * @since 2.1
 */
public final class PhpSourcePath {
    public static final String BOOT_CP = "classpath/php-boot"; //NOI18N
    public static final String SOURCE_CP = "classpath/php-source"; //NOI18N

    private static final DefaultPhpSourcePath DEFAULT_PHP_SOURCE_PATH = new DefaultPhpSourcePath();
    // @GuardedBy(PhpSourcePath.class)
    private static FileObject phpStubsFolder = null;

    /**
     * Possible types of a file.
     */
    public static enum FileType {
        /** Internal files (signature files). */
        INTERNAL,
        /** PHP include path. */
        INCLUDE,
        /** Project sources. */
        SOURCE,
        /** Project test sources. */
        TEST,
        /** Unknown file type. */
        UNKNOWN,
    }

    private PhpSourcePath() {
    }

    /**
     * Get the file type for the given file object.
     * @param file the input file.
     * @return the file type for the given file object.
     * @see FileType
     */
    public static FileType getFileType(FileObject file) {
        Parameters.notNull("file", file);

        // classpath performance is bad so first, check internal files
        if (isInternalFile(file)) {
            return FileType.INTERNAL;
        }
        // check classpath
        FileType fileType = getFileTypeFromClassPath(file);
        if (fileType != null) {
            return fileType;
        }
        // perhaps a file without a project or a file on global include path
        // in fact, this is not supported by the editor yet (model does not work for a file without a project)
        return DEFAULT_PHP_SOURCE_PATH.getFileType(file);
    }

    /**
     * Get list of folders, where asignatures file for PHP runtime are.
     * These files are also preindexed.
     * @return list of folders
     */
    public static synchronized List<FileObject> getPreindexedFolders() {
        if (phpStubsFolder == null) {
            // Core classes: Stubs generated for the "builtin" php runtime and extenstions.
            File clusterFile = InstalledFileLocator.getDefault().locate(
                    "modules/org-netbeans-modules-php-project.jar", "org.netbeans.modules.php.project", false);   //NOI18N

            if (clusterFile != null) {
                File phpStubs =
                        new File(clusterFile.getParentFile().getParentFile().getAbsoluteFile(),
                        "phpstubs/phpruntime"); // NOI18N
                assert phpStubs.exists() && phpStubs.isDirectory() : "No stubs found";
                phpStubsFolder = FileUtil.toFileObject(phpStubs);
            } else {
                // During test?
                // HACK - TODO use mock
                String phpDir = System.getProperty("xtest.php.home");   //NOI18N
                if (phpDir == null) {
                    throw new RuntimeException("xtest.php.home property has to be set when running within binary distribution");  //NOI18N
                }
                File phpStubs = new File(phpDir + File.separator + "phpstubs/phpruntime"); // NOI18N
                if (phpStubs.exists()) {
                    phpStubsFolder = FileUtil.toFileObject(phpStubs);
                }
            }
        }

        return Collections.singletonList(phpStubsFolder);
    }

    /**
     * Get all the possible path roots from PHP include path for the given file. If the file equals <code>null</code> then
     * just global PHP include path is returned.
     * @param file a file which could belong to a project or <code>null</code> for gettting global PHP include path.
     * @return all the possible path roots from PHP include path.
     */
    public static List<FileObject> getIncludePath(FileObject file) {
        if (file == null) {
            return DEFAULT_PHP_SOURCE_PATH.getIncludePath();
        }
        PhpSourcePathImplementation phpSourcePath = getPhpSourcePathForProjectFile(file);
        if (phpSourcePath != null) {
            return phpSourcePath.getIncludePath();
        }
        return DEFAULT_PHP_SOURCE_PATH.getIncludePath();
    }

    /**
     * Resolve absolute path for the given file name. The order is the given directory then PHP include path.
     * @param directory the directory to which the PHP <code>include()</code> or <code>require()</code> functions
     *                  could be resolved. Typically the directory containing the given script.
     * @param fileName a file name or a relative path delimited by '/'.
     * @return resolved file path or <code>null</code> if the given file is not found.
     */
    public static FileObject resolveFile(FileObject directory, String fileName) {
        Parameters.notNull("directory", directory);
        Parameters.notNull("fileName", fileName);
        if (!directory.isFolder()) {
            throw new IllegalArgumentException("valid directory needed");
        }

        PhpSourcePathImplementation phpSourcePath = getPhpSourcePathForProjectFile(directory);
        if (phpSourcePath != null) {
            return phpSourcePath.resolveFile(directory, fileName);
        }
        return DEFAULT_PHP_SOURCE_PATH.resolveFile(directory, fileName);
    }

    private static PhpSourcePathImplementation getPhpSourcePathForProjectFile(FileObject file) {
        Project project = FileOwnerQuery.getOwner(file);
        if (project == null) {
            return null;
        }
        PhpSourcePathImplementation phpSourcePath = project.getLookup().lookup(PhpSourcePathImplementation.class);
        // XXX disabled because of runtime.php underneath nbbuild directory
        //assert phpSourcePath != null : "Not PHP project (interface PhpSourcePath not found in lookup)! [" + project + "]";
        return phpSourcePath;
    }

    private static FileType getFileTypeFromClassPath(FileObject file) {
        // first, check source CP
        ClassPath classPath = ClassPath.getClassPath(file, SOURCE_CP);
        if (classPath != null && classPath.contains(file)) {
            // it is a source file
            PhpSourcePathImplementation phpSourcePath = getPhpSourcePathForProjectFile(file);
            if (phpSourcePath != null) {
                return phpSourcePath.getFileType(file);
            }
            // happens at least in tests
            return FileType.UNKNOWN;
        }
        // now, check include path of opened projects
        classPath = IncludePathClassPathProvider.findProjectIncludePath(file);
        if (classPath != null && classPath.contains(file)) {
            // internal?
            if (isInternalFile(file)) {
                return FileType.INTERNAL;
            }
            // include
            return FileType.INCLUDE;
        }
        return null;
    }

    private static boolean isInternalFile(FileObject file) {
        for (FileObject dir : CommonPhpSourcePath.getInternalPath()) {
            if (dir.equals(file)
                    || FileUtil.isParentOf(dir, file)) {
                return true;
            }
        }
        return false;
    }

    // PhpSourcePathImplementation implementation for file which does not belong to any project
    private static class DefaultPhpSourcePath implements org.netbeans.modules.php.project.classpath.PhpSourcePathImplementation {

        @Override
        public FileType getFileType(FileObject file) {
            for (FileObject dir : CommonPhpSourcePath.getInternalPath()) {
                if (dir.equals(file) || FileUtil.isParentOf(dir, file)) {
                    return FileType.INTERNAL;
                }
            }
            for (FileObject dir : getPlatformPath()) {
                if (dir.equals(file) || FileUtil.isParentOf(dir, file)) {
                    return FileType.INCLUDE;
                }
            }
            return FileType.UNKNOWN;
        }

        @Override
        public List<FileObject> getIncludePath() {
            return new ArrayList<FileObject>(getPlatformPath());
        }

        @Override
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

        // XXX cache?
        private List<FileObject> getPlatformPath() {
            String[] paths = PhpOptions.getInstance().getPhpGlobalIncludePathAsArray();
            List<FileObject> internalPath = CommonPhpSourcePath.getInternalPath();
            List<FileObject> dirs = new ArrayList<FileObject>(paths.length + internalPath.size());
            dirs.addAll(internalPath);
            for (String path : paths) {
                FileObject resolvedFile = FileUtil.toFileObject(FileUtil.normalizeFile(new File(path)));
                if (resolvedFile != null) { // XXX check isValid() as well?
                    dirs.add(resolvedFile);
                }
            }
            return dirs;
        }
    }
}
