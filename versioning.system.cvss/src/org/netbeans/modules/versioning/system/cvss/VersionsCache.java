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
import org.netbeans.lib.cvsclient.command.PipedFileInformation;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.event.*;
import org.netbeans.lib.cvsclient.admin.Entry;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.text.MessageFormat;

/**
 * Takes care about retrieving various revisions of a file and caching them locally. 
 * HEAD revisions are not cached.
 * TODO: Files are never deleted from cache, should we address this? 
 * 
 * @author Maros Sandor
 */
public class VersionsCache {
    
    private static final ResourceBundle loc = NbBundle.getBundle(VersionsCache.class);
    
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
     * @return File supplied file in the specified revision (locally cached copy)
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
        File cacheDir = new File(baseFile.getParentFile(), CACHE_DIR);
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
    
    private File checkoutRemoteFile(File baseFile, String revision) throws IOException,
            IllegalCommandException, CommandException, AuthenticationException, NotVersionedException {
        
        UpdateCommand cmd = new UpdateCommand();
        cmd.setRecursive(false); 
        cmd.setFiles(new File [] { baseFile });
        cmd.setPipeToOutput(true);
        if (!revision.equals(REVISION_HEAD)) cmd.setUpdateByRevision(revision);
        cmd.setDisplayName(MessageFormat.format(loc.getString("MSG_VersionsCache_FetchingProgress"),
                                                new Object [] { revision, baseFile.getName() }));

        VersionsCacheExecutor executor = new VersionsCacheExecutor(cmd);
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


}
