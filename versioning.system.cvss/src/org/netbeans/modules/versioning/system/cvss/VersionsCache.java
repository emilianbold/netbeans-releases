/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss;

import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.admin.Entry;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

import java.io.*;

/**
 * Takes care about retrieving various revisions of a file and caching them locally. 
 * HEAD revisions are not cached.
 * TODO: Files are never deleted from cache, should we address this? 
 * 
 * @author Maros Sandor
 */
public class VersionsCache {
    
    /**
     * Constant representing the current working revision.
     */ 
    public static final String  REVISION_CURRENT = "";

    /**
     * Constant representing the base revision of the current working revision (the one in Entries).
     */ 
    public static final String  REVISION_BASE = "*";
    
    /**
     * Constant representing the CVS HEAD revision.
     */ 
    public static final String  REVISION_HEAD    = "HEAD";
    
    private static final String CACHE_DIR = "CVS/RevisionCache/";
    
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
     * @return File supplied file in the specified revision (locally cached copy) or null if this file does not exist
     * in the specified revision
     * @throws java.io.IOException
     * @throws org.netbeans.modules.versioning.system.cvss.IllegalCommandException
     * @throws org.netbeans.lib.cvsclient.command.CommandException
     * @throws org.netbeans.lib.cvsclient.connection.AuthenticationException
     * @throws org.netbeans.modules.versioning.system.cvss.NotVersionedException
     */ 
    public synchronized File getRemoteFile(File baseFile, String revision) throws IOException, 
                IllegalCommandException, CommandException, AuthenticationException, NotVersionedException {
        if (revision == REVISION_BASE) {
            revision = getBaseRevision(baseFile);
        }
        File file = getCachedRevision(baseFile, revision);
        if (file != null) return file;
        file = checkoutRemoteFile(baseFile, revision);
        if (file == null) return null;
        file = saveRevision(baseFile, file, revision);
        return file;
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
            file.delete();
            return destFile;
        } catch (IOException e) {
            // ignore errors, cache is not that much important
        }
        return file;
    }

    private File getCachedRevision(File baseFile, String revision) {
        if (revision == REVISION_CURRENT) {
            return baseFile;
        }
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
        return REVISION_HEAD.equals(revision);
    }

    private String getBaseRevision(File file) throws IOException {
        Entry entry = CvsVersioningSystem.getInstance().getAdminHandler().getEntry(file);
        String rawRev = entry.getRevision();
        if (rawRev != null && rawRev.startsWith("-")) {
            // leading - means removed
            return rawRev.substring(1);
        }
        return rawRev;
    }

    private String cachedName(File baseFile, String revision) {
        return "#" + revision + "#" + baseFile.getName();        
    }

    private String getRepositoryForDirectory(File directory) {
        if (directory == null) return null;
        if (!directory.exists()) {
            return getRepositoryForDirectory(directory.getParentFile()) + "/" + directory.getName();
        }
        try {
            return CvsVersioningSystem.getInstance().getAdminHandler().getRepositoryForDirectory(directory.getAbsolutePath(), "").substring(1);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Gets a specific revision of a file from repository. 
     * 
     * @param baseFile location of the file in local workdir (need not exist)
     * @param revision revision number to get
     * @return File file on disk (most probably located in some temp diretory) or null if this file does not exist
     * in repository in the specified revision
     * @throws IOException if some I/O error occurs during checkout
     */ 
    private File checkoutRemoteFile(File baseFile, String revision) throws IOException {
        
        String repositoryPath = getRepositoryForDirectory(baseFile.getParentFile()) + "/" + baseFile.getName();
        
        CheckoutCommand cmd = new CheckoutCommand();
        cmd.setRecursive(false);
        cmd.setModule(repositoryPath);
        cmd.setPipeToOutput(true);
        if (!revision.equals(REVISION_HEAD)) cmd.setCheckoutByRevision(revision);
        cmd.setDisplayName(NbBundle.getMessage(VersionsCache.class, "MSG_VersionsCache_FetchingProgress", revision, baseFile.getName()));
        
        GlobalOptions options = new GlobalOptions();
        options.setCVSRoot(getCvsRoot(baseFile.getParentFile()));
        VersionsCacheExecutor executor = new VersionsCacheExecutor(cmd, options);
        executor.execute();
        try {
            executor.waitFinished();
            return executor.getCheckedOutVersion();
        } catch (Throwable t) {
            IOException ioe = new IOException("Unable to checkout revision " + revision + " of " + baseFile.getName());
            ioe.initCause(t);
            throw ioe;
        }
    }

    private String getCvsRoot(File baseFile) throws IOException {
        File root = new File (new File(baseFile, "CVS"), "Root");
        if (root.isFile()) {
            BufferedReader r = null;
            try {
                r = new BufferedReader(new FileReader(root));
                return r.readLine();
            } finally {
                if (r != null) r.close();
            }
        } else {
            return getCvsRoot(baseFile.getParentFile());
        }
    }

}
