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

package org.netbeans.modules.git.utils;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.filesystems.FileUtil;

/**
 *s
 * @author ondra
 */
public final class GitUtils {

    public static final String DOT_GIT = ".git"; //NOI18N
    private static final Pattern METADATA_PATTERN = Pattern.compile(".*\\" + File.separatorChar + "(\\.)git(\\" + File.separatorChar + ".*|$)"); // NOI18N
    private static final String FILENAME_GITIGNORE = ".gitignore"; // NOI18N

    /**
     * Checks file location to see if it is part of git metadata
     *
     * @param file file to check
     * @return true if the file or folder is a part of git metadata, false otherwise
     */
    public static boolean isPartOfGitMetadata (File file) {
        return METADATA_PATTERN.matcher(file.getAbsolutePath()).matches();
    }

    /**
     * Tests <tt>.hg</tt> directory itself.
     */
    public static boolean isAdministrative (File file) {
        String name = file.getName();
        return isAdministrative(name) && file.isDirectory();
    }

    public static boolean isAdministrative (String fileName) {
        return fileName.equals(DOT_GIT); // NOI18N
    }

    public static boolean repositoryExistsFor (File file) {
        return new File(file, DOT_GIT).exists();
    }

    /**
     * Returns the administrative git folder for the given repository and normalizes the file
     * @param repositoryRoot root of the repository
     * @return administrative git folder
     */
    public static File getGitFolderForRoot (File repositoryRoot) {
        return FileUtil.normalizeFile(new File(repositoryRoot, DOT_GIT));
    }

    /**
     * Adds the given file into filesUnderRoot:
     * <ul>
     * <li>if the file was already in the set, does nothing and returns true</li>
     * <li>if the file lies under a folder already present in the set, does nothing and returns true</li>
     * <li>if the file and none of it's ancestors is not in the set yet, this adds the file into the set,
     * removes all it's children and returns false</li>
     * @param repository repository root
     * @param filesUnderRoot set of repository roots
     * @param file file to add
     * @return false if the file was added or true if it was already contained
     */
    public static boolean prepareRootFiles (File repository, Set<File> filesUnderRoot, File file) {
        boolean added = false;
        Set<File> filesToRemove = new HashSet<File>();
        for (File fileUnderRoot : filesUnderRoot) {
            if (file.equals(fileUnderRoot) || fileUnderRoot.equals(repository)) {
                // file has already been inserted or scan is planned for the whole repository root
                added = true;
                break;
            }
            if (file.equals(repository)) {
                // plan the scan for the whole repository root
                // adding the repository, there's no need to leave all other files
                filesUnderRoot.clear();
                break;
            } else {
                if (file.getAbsolutePath().length() < fileUnderRoot.getAbsolutePath().length()) {
                    if (Utils.isAncestorOrEqual(file, fileUnderRoot)) {
                        filesToRemove.add(fileUnderRoot);
                    }
                } else {
                    if (Utils.isAncestorOrEqual(fileUnderRoot, file)) {
                        added = true;
                        break;
                    }
                }
            }
        }
        filesUnderRoot.removeAll(filesToRemove);
        if (!added) {
            // not added yet
            filesUnderRoot.add(file);
        }
        return added;
    }
    
    public static boolean isIgnored(File file, boolean checkSharability){
        if (file == null) return false;
        String path = file.getPath();
        File topFile = Git.getInstance().getRepositoryRoot(file);
        
        // We assume that the toplevel directory should not be ignored.
        if (topFile == null || topFile.equals(file)) {
            return false;
        }
        
//        Set<Pattern> patterns = getIgnorePatterns(topFile);
//        try {
//        path = path.substring(topFile.getAbsolutePath().length() + 1);
//        } catch(StringIndexOutOfBoundsException e) {
//            throw e;
//        }
//        if (File.separatorChar != '/') {
//            path = path.replace(File.separatorChar, '/');
//        }
//
//        for (Iterator i = patterns.iterator(); i.hasNext();) {
//            Pattern pattern = (Pattern) i.next();
//            if (pattern.matcher(path).find()) {
//                return true;
//            }
//        }

        // check cached not sharable folders and files
        if (isNotSharable(path, topFile)) {
            return true;
        }

        // If a parent of the file matches a pattern ignore the file
        File parentFile = file.getParentFile();
        if (!parentFile.equals(topFile)) {
            if (isIgnored(parentFile, false)) return true;
        }

        if (FILENAME_GITIGNORE.equals(file.getName())) return false;
        if (checkSharability) {
            int sharability = SharabilityQuery.getSharability(FileUtil.normalizeFile(file));
            if (sharability == SharabilityQuery.NOT_SHARABLE) {
                addNotSharable(topFile, path);
                return true;
            }
        }
        return false;
    }

    // cached not sharable files and folders
    private static final Map<File, Set<String>> notSharable = Collections.synchronizedMap(new HashMap<File, Set<String>>(5));
    private static void addNotSharable (File topFile, String ignoredPath) {
        synchronized (notSharable) {
            // get cached patterns
            Set<String> ignores = notSharable.get(topFile);
            if (ignores == null) {
                ignores = new HashSet<String>();
            }
            String patternCandidate = ignoredPath;
            // test for duplicate patterns
            for (Iterator<String> it = ignores.iterator(); it.hasNext();) {
                String storedPattern = it.next();
                if (storedPattern.equals(ignoredPath) // already present
                        || ignoredPath.startsWith(storedPattern + '/')) { // path already ignored by its ancestor
                    patternCandidate = null;
                    break;
                } else if (storedPattern.startsWith(ignoredPath + '/')) { // stored pattern matches a subset of ignored path
                    // remove the stored pattern and add the ignored path
                    it.remove();
                }
            }
            if (patternCandidate != null) {
                ignores.add(patternCandidate);
            }
            notSharable.put(topFile, ignores);
        }
    }

    private static boolean isNotSharable (String path, File topFile) {
        boolean retval = false;
        Set<String> notSharablePaths = notSharable.get(topFile);
        if (notSharablePaths == null) {
            notSharablePaths = Collections.emptySet();
        }
        retval = notSharablePaths.contains(path);
        return retval;
    }

    public GitUtils() {
    }
}
