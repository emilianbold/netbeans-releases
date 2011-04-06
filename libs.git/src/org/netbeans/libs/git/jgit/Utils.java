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
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.NotTreeFilter;
import org.eclipse.jgit.treewalk.filter.OrTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitObjectType;
import org.openide.util.NbBundle;

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
        return repository.getConfig().getBoolean(ConfigConstants.CONFIG_CORE_SECTION, null, ConfigConstants.CONFIG_KEY_FILEMODE, true);
    }
    
    public static Collection<PathFilter> getPathFilters (File workDir, File[] roots) {
        Collection<String> relativePaths = getRelativePaths(workDir, roots);
        return getPathFilters(relativePaths);
    }

    public static TreeFilter getExcludeExactPathsFilter (File workDir, File[] roots) {
        Collection<String> relativePaths = getRelativePaths(workDir, roots);
        TreeFilter filter = null;
        if (relativePaths.size() > 0) {
            Collection<PathFilter> filters = getPathFilters(relativePaths);
            List<TreeFilter> exactPathFilters = new LinkedList<TreeFilter>();
            for (PathFilter f : filters) {
                exactPathFilters.add(ExactPathFilter.create(f));
            }
            return NotTreeFilter.create(exactPathFilters.size() == 1 ? exactPathFilters.get(0) : OrTreeFilter.create(exactPathFilters));
        }
        return filter;
    }

    private static Collection<PathFilter> getPathFilters (Collection<String> relativePaths) {
        Collection<PathFilter> filters = new LinkedList<PathFilter>();
        for (String path : relativePaths) {
            filters.add(PathFilter.create(path));
        }
        return filters;
    }

    public static List<String> getRelativePaths(File workDir, File[] roots) {
        List<String> paths = new ArrayList<String>(roots.length);
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

    public static RevCommit findCommit (Repository repository, String revision) throws GitException.MissingObjectException, GitException {
        try {
            ObjectId commitId = parseObjectId(repository, revision);
            if (commitId == null) {
                throw new GitException.MissingObjectException(revision, GitObjectType.COMMIT);
            }
            return new RevWalk(repository).parseCommit(commitId);
        } catch (MissingObjectException ex) {
            throw new GitException.MissingObjectException(revision, GitObjectType.COMMIT);
        } catch (IncorrectObjectTypeException ex) {
            throw new GitException(NbBundle.getMessage(Utils.class, "MSG_Exception_IdNotACommit", revision)); //NOI18N
        } catch (IOException ex) {
            throw new GitException(ex);
        }
    }

    public static ObjectId parseObjectId (Repository repository, String objectId) throws GitException {
        try {
            return repository.resolve(objectId);
        } catch (AmbiguousObjectException ex) {
            throw new GitException(NbBundle.getMessage(Utils.class, "MSG_Exception_IdNotACommit", objectId), ex); //NOI18N
        } catch (IOException ex) {
            throw new GitException(ex);
        }
    }

    /**
     * Recursively deletes the file or directory.
     *
     * @param file file/directory to delete
     */
    public static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File [] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteRecursively(files[i]);
            }
        }
        file.delete();
    }
    
    /**
     * Eliminates part of the ref's name that equals knon prefixes such as refs/heads/, refs/remotes/ etc.
     * @param ref
     * @return 
     */
    public static String getRefName (Ref ref) {
        String name = ref.getName();
        for (String prefix : Arrays.asList(Constants.R_HEADS, Constants.R_REMOTES, Constants.R_TAGS, Constants.R_REFS)) {
            if (name.startsWith(prefix)) {
                name = name.substring(prefix.length());
            }
        }
        return name;
    }
}
