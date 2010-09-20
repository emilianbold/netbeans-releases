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
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.filesystems.FileUtil;

/**
 *s
 * @author ondra
 */
public final class GitUtils {

    private static final String DOT_GIT = ".git"; //NOI18N
    private static final Pattern METADATA_PATTERN = Pattern.compile(".*\\" + File.separatorChar + "(\\.)git(\\" + File.separatorChar + ".*|$)"); // NOI18N

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

    public GitUtils() {
    }
}
