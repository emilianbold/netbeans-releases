/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.versioning.system.cvss;

import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.admin.Entry;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

import java.io.*;

/**
 * Takes care about retrieving various revisions of a file and caching them locally. 
 * HEAD revisions are not cached.
 * TODO: Files are never deleted from cache, should we address this? 
 * TODO: cache dead files
 * 
 * @author Maros Sandor
 */
public class VersionsCache {
    
    /**
     * Constant representing the current working revision.
     */ 
    public static final String  REVISION_CURRENT = ""; // NOI18N

    /**
     * Constant representing the base revision of the current working revision (the one in Entries).
     */ 
    public static final String  REVISION_BASE = "*"; // NOI18N
    
    /**
     * Constant representing the CVS HEAD revision.
     */ 
    public static final String  REVISION_HEAD    = "HEAD"; // NOI18N
    
    private static final String CACHE_DIR = "CVS/RevisionCache/"; // NOI18N
    
    private static VersionsCache instance = new VersionsCache();

    private long            purgeTimestamp = Long.MAX_VALUE;

    public static VersionsCache getInstance() {
        return instance;
    }
    
    private VersionsCache() {
    }

    /**
     * Retrieves repository version of the file, either from the local cache of revisions or, it that fails,
     * from the remote repository. CURRENT revision is the file itself. HEAD revisions are considered volatile
     * and their cached versions are purged with the {@link #purgeVolatileRevisions()} method.
     * 
     * @param revision revision to fetch
     * @param group that carries shared state. Note that this group must not be executed later on. This parameter can be null. 
     * @return File supplied file in the specified revision (locally cached copy) or null if this file does not exist
     * in the specified revision or the user cancelled file checkout (in this case group.isCancelled() returns true)
     * @throws java.io.IOException
     * @throws org.netbeans.modules.versioning.system.cvss.IllegalCommandException
     * @throws org.netbeans.lib.cvsclient.command.CommandException
     * @throws org.netbeans.lib.cvsclient.connection.AuthenticationException
     * @throws org.netbeans.modules.versioning.system.cvss.NotVersionedException
     */ 
    public synchronized File getRemoteFile(File baseFile, String revision, ExecutorGroup group) throws IOException,
                IllegalCommandException, CommandException, AuthenticationException, NotVersionedException {
        return getRemoteFile(baseFile, revision, group, false);
    }

    public synchronized File getRemoteFile(File baseFile, String revision, ExecutorGroup group, boolean quiet) throws IOException,
                IllegalCommandException, CommandException, AuthenticationException, NotVersionedException {
        String resolvedRevision = resolveRevision(baseFile, revision);
        if (revision == REVISION_CURRENT) {
            return baseFile.canRead() ? baseFile : null;
        }
        File file = getCachedRevision(baseFile, resolvedRevision);
        if (file != null) return file;
        file = checkoutRemoteFile(baseFile, revision, group, quiet);
        if (file == null) return null;
        return saveRevision(baseFile, file, resolvedRevision);
    }

    private String resolveRevision(File baseFile, String revision) throws IOException {
        if (revision == REVISION_BASE) {
            return getBaseRevision(baseFile);
        }
        if (revision.equals(REVISION_HEAD)) {
            Entry entry = CvsVersioningSystem.getInstance().getAdminHandler().getEntry(baseFile);
            if (entry != null && entry.getTag() != null) {
                return entry.getTag();
            }
        }
        return revision;
    }

    /**
     * Purges volatile revisions (i.g. HEAD) from cache. BEWARE: HEAD revisions are cached but there
     * are multiple HEADs, each branch has its own HEAD!
     */
    public void purgeVolatileRevisions() {
        purgeTimestamp = System.currentTimeMillis();
    }

    private File saveRevision(File baseFile, File file, String revision) {
        // do not create directories if they do not exist (deleted trees) 
        File cacheDir;
        if (baseFile.getParentFile().isDirectory()) {
            cacheDir = new File(baseFile.getParentFile(), CACHE_DIR);
        } else {
            cacheDir = file.getParentFile();
        }
            if (!cacheDir.exists() && !cacheDir.mkdirs()) return file;
        File destFile = new File(cacheDir, cachedName(baseFile, revision));
        try {
            FileInputStream fin = new FileInputStream(file);
            FileOutputStream fos = new FileOutputStream(destFile);
            FileUtil.copy(fin, fos);
            fin.close();
            fos.close();
            // eventually delete the checked out file
            if (file.equals(baseFile)) {
                // safety check
                FileInformation info = CvsVersioningSystem.getInstance().getStatusCache().createFileInformation(baseFile);
                if (info.getStatus() != FileInformation.STATUS_VERSIONED_UPTODATE) {
                    destFile.delete();
                    return null;
                }
            } else {
                file.delete();
            }
            return destFile;
        } catch (IOException e) {
            // ignore errors, cache is not that much important
        }
        return file;
    }

    private File getCachedRevision(File baseFile, String revision) {
        File cachedCopy = new File(baseFile.getParentFile(), CACHE_DIR + cachedName(baseFile, revision));
        if (isVolatile(revision)) {
            if (cachedCopy.lastModified() < purgeTimestamp) {
                cachedCopy.delete();
            }
        }
        if (cachedCopy.canRead()) return cachedCopy;
        return null;
    }

    private boolean isVolatile(String revision) {
        return revision.indexOf('.') == -1;
    }

    private String getBaseRevision(File file) throws IOException {
        Entry entry = CvsVersioningSystem.getInstance().getAdminHandler().getEntry(file);
        if (entry == null) {
            throw new IllegalArgumentException("Cannot get BASE revision, there is no Entry for the file: " + file.getAbsolutePath());
        }
        String rawRev = entry.getRevision();
        if (rawRev != null && rawRev.startsWith("-")) { // NOI18N
            // leading - means removed
            return rawRev.substring(1);
        }
        return rawRev;
    }

    private String cachedName(File baseFile, String revision) {
        return baseFile.getName() + "#" + revision;     // NOI18N   
    }

    private String getRepositoryForDirectory(File directory, String repository) {
        if (directory == null) return null;
        if (!directory.exists()) {
            return getRepositoryForDirectory(directory.getParentFile(), repository) + "/" + directory.getName(); // NOI18N
        }
        try {
            return CvsVersioningSystem.getInstance().getAdminHandler().getRepositoryForDirectory(directory.getAbsolutePath(), repository); // NOI18N
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Gets a specific revision of a file from repository. 
     * 
     * @param baseFile location of the file in local workdir (need not exist)
     * @param revision revision number to get
     * @param group that carries shared state. Note that this group must not be executed later on. This parameter can be null.
     * @param quiet
     * @return File file on disk (most probably located in some temp diretory) or null if this file does not exist
     * in repository in the specified revision
     * @throws IOException if some I/O error occurs during checkout
     */ 
    private File checkoutRemoteFile(File baseFile, String revision, ExecutorGroup group, boolean quiet) throws IOException {

        if (revision == REVISION_BASE) {
            // be optimistic, use the file available on disk if possible
            FileInformation info = CvsVersioningSystem.getInstance().getStatusCache().createFileInformation(baseFile);
            if (info.getStatus() == FileInformation.STATUS_VERSIONED_UPTODATE) {
                return baseFile;
            }
        }
        revision = resolveRevision(baseFile, revision);
        
        GlobalOptions options = CvsVersioningSystem.createGlobalOptions();
        String root = Utils.getCVSRootFor(baseFile.getParentFile());
        CVSRoot cvsRoot = CVSRoot.parse(root);
        String repository = cvsRoot.getRepository();
        options.setCVSRoot(root);

        String repositoryPath = getRepositoryForDirectory(baseFile.getParentFile(), repository) + "/" + baseFile.getName(); // NOI18N

        CheckoutCommand cmd = new CheckoutCommand();
        cmd.setRecursive(false);
        assert repositoryPath.startsWith(repository) : repositoryPath + " does not start with: " + repository; // NOI18N

        repositoryPath = repositoryPath.substring(repository.length());
        if (repositoryPath.startsWith("/")) { // NOI18N
            repositoryPath = repositoryPath.substring(1);
        }
        cmd.setModule(repositoryPath);
        cmd.setPipeToOutput(true);
        cmd.setCheckoutByRevision(revision);
        String msg  = NbBundle.getMessage(VersionsCache.class, "MSG_VersionsCache_FetchingProgress", revision, baseFile.getName());
        cmd.setDisplayName(msg);

        VersionsCacheExecutor executor = new VersionsCacheExecutor(cmd, options, quiet);
        if (group != null) {
            group.progress(msg);
            group.addExecutor(executor);
        }
        executor.execute();
        ExecutorSupport.wait(new ExecutorSupport [] { executor });
        if (group == null) {
            executor.getGroup().executed();
        }

        if (executor.isSuccessful()) {
            return executor.getCheckedOutVersion();
        } else {
            if (executor.isCancelled()) {
                return null;
            }
            // XXX note that executor already handles/notifies failures
            IOException ioe = new IOException(NbBundle.getMessage(VersionsCache.class, "Bk4001", revision, baseFile.getName()));
            ioe.initCause(executor.getFailure());
            throw ioe;
        }

    }
}
