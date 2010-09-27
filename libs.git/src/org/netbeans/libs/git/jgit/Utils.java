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

package org.netbeans.libs.git.jgit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

/**
 *
 * @author ondra
 */
public final class Utils {
private Utils () {

    }

    public static Repository getRepositoryForWorkingDir (File workDir) throws IOException {
         return new FileRepositoryBuilder().setGitDir(getMetadataFolder(workDir)).readEnvironment().findGitDir().build();
    }

    public static File getMetadataFolder (File workDir) {
        return new File(workDir, Constants.DOT_GIT);
    }

    public static boolean checkExecutable (Repository repository) {
        return repository.getConfig().getBoolean("core", null, "filemode", true); //NOI18N
    }
    
    public static Collection<PathFilter> getPathFilters (File workDir, File[] roots) {
        Collection<String> relativePaths = getRelativePaths(workDir, roots);
        Collection<PathFilter> filters = new LinkedList<PathFilter>();
        for (String path : relativePaths) {
            filters.add(PathFilter.create(path));
        }
        return filters;
    }

    public static Collection<String> getRelativePaths(File workDir, File[] roots) {
        Collection<String> paths = new ArrayList<String>(roots.length);
        for (File root : roots) {
            if (workDir.equals(root)) {
                paths.clear();
                break;
            } else {
                paths.add(getRelativePath(workDir, root));
            }
        }
        return paths;
    }

    public static String getRelativePath (File repo, final File file) {
        StringBuilder relativePath = new StringBuilder("");
        File parent = file;
        if (!parent.equals(repo)) {
            while (parent != null && !parent.equals(repo)) {
                relativePath.insert(0, "/").insert(0, parent.getName()); //NOI18N
                parent = parent.getParentFile();
            }
            if (parent == null) {
                throw new IllegalArgumentException(file.getAbsolutePath() + " is not under " + repo.getAbsolutePath());
            }
            relativePath.deleteCharAt(relativePath.length() - 1);
        }
        return relativePath.toString();
    }

    /**
     * Returns true if any of the given filters denotes a path lying under the current file/folder specified by the given TreeWalk
     * @param filters
     * @param treeWalk
     * @return
     */
    public static boolean isUnderOrEqual (Collection<PathFilter> filters, TreeWalk treeWalk) {
        boolean retval = false;
        for (PathFilter filter : filters) {
            if (filter.include(treeWalk) && treeWalk.getPathString().length() <= filter.getPath().length()) {
                retval = true;
                break;
            }
        }
        return retval;
    }

    /**
     * Returns true if the current file/folder specified by the given TreeWalk lies under any of the given filters
     * @param treeWalk
     * @param filters
     * @return
     */
    public static boolean isUnderOrEqual (TreeWalk treeWalk, Collection<PathFilter> filters) {
        boolean retval = filters.isEmpty();
        for (PathFilter filter : filters) {
            if (filter.include(treeWalk) && treeWalk.getPathString().length() >= filter.getPath().length()) {
                retval = true;
                break;
            }
        }
        return retval;
    }

    public static Collection<byte[]> getPaths (Collection<PathFilter> pathFilters) {
        Collection<byte[]> paths = new LinkedList<byte[]>();
        for (PathFilter filter : pathFilters) {
            paths.add(Constants.encode(filter.getPath()));
        }
        return paths;
    }
}
