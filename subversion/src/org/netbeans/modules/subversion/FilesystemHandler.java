/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.subversion;

import javax.swing.SwingUtilities;
import org.netbeans.modules.subversion.util.FileUtils;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.subversion.client.SvnClient;
import org.openide.ErrorManager;
import java.io.File;
import java.io.IOException;
import java.util.*;   
import java.util.logging.Logger;
import java.util.logging.Level;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.modules.versioning.util.Utils;
import org.tigris.subversion.svnclientadapter.*;

/**
 * Handles events fired from the filesystem such as file/folder create/delete/move.
 * 
 * @author Maros Sandor
 */
class FilesystemHandler extends VCSInterceptor {
        
    private final Subversion svn;
    private final FileStatusCache   cache;
    
    /**
     * Stores .svn folders that should be deleted ASAP.
     */ 
    private final Set<File> invalidMetadata = new HashSet<File>(5);
    
    public FilesystemHandler(Subversion svn) {
        this.svn = svn;
        cache = svn.getStatusCache();
    }

    public boolean beforeDelete(File file) {
        if (SvnUtils.isPartOfSubversionMetadata(file)) return true;
        // calling cache results in SOE, we must check manually
        return !file.isFile() && hasMetadata(file);
    }

    /**
     * This interceptor ensures that subversion metadata is NOT deleted. 
     * 
     * @param file file to delete
     */ 
    public void doDelete(File file) throws IOException {
        boolean isMetadata = SvnUtils.isPartOfSubversionMetadata(file);
        if (!isMetadata) {
            remove(file);
        }
    }

    public void afterDelete(final File file) {
        Utils.post(new Runnable() {
            public void run() {
                // If a regular file is deleted then update its Entries as if it has been removed.
                if (file == null) return;
                int status = cache.getStatus(file).getStatus();
                if (status != FileInformation.STATUS_NOTVERSIONED_EXCLUDED && status != FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) {
                    try {
                        SvnClient client = Subversion.getInstance().getClient(false);
                        client.remove(new File [] { file }, true);

                    } catch (SVNClientException e) {
                        // ignore; we do not know what to do here; does no harm, the file was probably Locally New
                    }
                }
                // fire event explicitly because the file is already gone
                // so svnClientAdapter does not fire ISVNNotifyListener event
                cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
            }
        });
    }

    public boolean beforeMove(File from, File to) {
        File destDir = to.getParentFile();
        if (from != null && destDir != null) {            
            // a direct cache call could, because of the synchrone beforeMove handling, 
            // trigger an reentrant call on FS => we have to check manually            
            if (isVersioned(from)) {
                return Subversion.getInstance().isManaged(to);
            }
            // else XXX handle file with saved administative
            // right now they have old status in cache but is it guaranteed?
        }

        return false;
    }

    public void doMove(final File from, final File to) throws IOException {
        if (SwingUtilities.isEventDispatchThread()) {
            
            Logger.getLogger("org.netbeans.modules.subversion").log(Level.INFO, "Warning: launching external process in AWT", new Exception().fillInStackTrace());
            final Throwable innerT[] = new Throwable[1];
            Runnable outOfAwt = new Runnable() {
                public void run() {
                    try {
                        svnMoveImplementation(from, to);
                    } catch (Throwable t) {
                        innerT[0] = t;
                    }
                }
            };
            
            Subversion.getInstance().getRequestProcessor().post(outOfAwt).waitFinished();
            if (innerT[0] != null) {
                if (innerT[0] instanceof IOException) {
                    throw (IOException) innerT[0];
                } else if (innerT[0] instanceof RuntimeException) {
                    throw (RuntimeException) innerT[0];
                } else if (innerT[0] instanceof Error) {
                    throw (Error) innerT[0];
                } else {
                    throw new IllegalStateException("Unexpected exception class: " + innerT[0]);  // NOI18N
                }
            }
            
            // end of hack
            
        } else {
            svnMoveImplementation(from, to);
        }
    }

    public void afterMove(final File from, final File to) {
        Utils.post(new Runnable() {
            public void run() {
                cache.refresh(to, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                File parent = to.getParentFile();
                if (parent != null) {
                    if (from.equals(to)) {
                        ErrorManager.getDefault().log(ErrorManager.WARNING, "Wrong (identity) rename event for " + from.getAbsolutePath()); // NOI18N
                    }
                    cache.refresh(from, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                }
            }
        });
    }
    
    public boolean beforeCreate(File file, boolean isDirectory) {
        if ( SvnUtils.isPartOfSubversionMetadata(file)) {
            synchronized(invalidMetadata) {
                invalidMetadata.add(file);   
            }            
            return false;
        } else {
            if (!file.exists()) {
                try {
                    SvnClient client = Subversion.getInstance().getClient(true);

                    // a direct cache call could, because of the synchrone beforeCreate handling, 
                    // trigger an reentrant call on FS => we have to check manually           
                    ISVNStatus status = client.getSingleStatus(file);

                    if (status != null && status.getTextStatus().equals(SVNStatusKind.DELETED)) {                                            
                        client.revert(file, false);
                        file.delete();
                    }
                } catch (SVNClientException ex) {
                    SvnClientExceptionHandler.notifyException(ex, false, false);
                }
            }
            return false;
        }
    }

    public void doCreate(File file, boolean isDirectory) throws IOException {
    }

    public void afterCreate(final File file) {
        Utils.post(new Runnable() {
            public void run() {
                if (file == null) return;
                int status = cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN).getStatus();

                if ((status & FileInformation.STATUS_MANAGED) == 0) return;

//        if (properties_changed) cache.directoryContentChanged(file.getParentFile());
                if (file.isDirectory()) cache.directoryContentChanged(file);
            }
        });
    }
    
    public void afterChange(final File file) {
        Utils.post(new Runnable() {
            public void run() {
                if ((cache.getStatus(file).getStatus() & FileInformation.STATUS_MANAGED) != 0) {
                    cache.refreshCached(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                }
            }
        });
    }

    /**
     * Removes invalid metadata from all known folders.
     */ 
    void removeInvalidMetadata() {
        synchronized(invalidMetadata) {
            for (File file : invalidMetadata) {
                Utils.deleteRecursively(file);
            }
            invalidMetadata.clear();
        }
    }
    
    // private methods ---------------------------
    
    private boolean hasMetadata(File file) {
        return new File(file, ".svn/entries").canRead() || new File(file, "_svn/entries").canRead();
    }
    
    private boolean isVersioned(File file) {
        if (SvnUtils.isPartOfSubversionMetadata(file)) return false;            
        return  ( !file.isFile() && hasMetadata(file) ) || ( file.isFile() && hasMetadata(file.getParentFile()) );        
    }

    private boolean remove(File file) {
        try {
            SvnClient client = Subversion.getInstance().getClient(false);
            // funny thing is, the command will delete all files recursively
            client.remove(new File [] { file }, true);
            return true;
        } catch (SVNClientException e) {
            return false;
        }
    }

    private void svnMoveImplementation(final File srcFile, final File dstFile) throws IOException {
        try {                        
            boolean force = true; // file with local changes must be forced
            SvnClient client = Subversion.getInstance().getClient(false);
            
            File tmpMetadata = null;
            try {
                // prepare destination, it must be under Subversion control
                removeInvalidMetadata();

                File parent;
                if (dstFile.isDirectory()) {
                    parent = dstFile;
                } else {
                    parent = dstFile.getParentFile();
                }

                if (parent != null) {
                    assert Subversion.getInstance().isManaged(parent);  // see implsMove above                                        
                    // a direct cache call could, because of the synchrone svnMoveImplementation handling, 
                    // trigger an reentrant call on FS => we have to check manually            
                    if (!hasMetadata(parent)) {
                        addDirectories(parent);
                    }
                }

                // perform
                int retryCounter = 6;
                while (true) {
                    try {
                        client.move(srcFile, dstFile, force);
                        break;
                    } catch (SVNClientException e) {                        
                        // svn: Working copy '/tmp/co/svn-prename-19/AnagramGame-pack-rename/src/com/toy/anagrams/ui2' locked
                        if (e.getMessage().endsWith("' locked") && retryCounter > 0) { // NOI18N
                            // XXX HACK AWT- or FS Monitor Thread performs
                            // concurrent operation
                            try {
                                Thread.sleep(107);
                            } catch (InterruptedException ex) {
                                // ignore
                            }
                            retryCounter--;
                            continue;
                        }

                        // XXX loosing file history is less harm than raising IOException
                        // that completelly breaks clients (namely refactoring can not handle IOEx)
                        if (srcFile.renameTo(dstFile)) {
                            ErrorManager.getDefault().annotate(e, "Relaxing Subversion rename error...."); // NOI18N
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e); // NOI18N
                            break;
                        } else {
                            IOException ex = new IOException("Subversion failed to rename " + srcFile.getAbsolutePath() + " to: " + dstFile.getAbsolutePath()); // NOI18N
                        ex.initCause(e);
                        throw ex;
                    }
                }
                }
            } finally {
                if (tmpMetadata != null) {
                    FileUtils.deleteRecursively(tmpMetadata);
                }
            }
        } catch (SVNClientException e) {
            IOException ex = new IOException("Subversion failed to rename " + srcFile.getAbsolutePath() + " to: " + dstFile.getAbsolutePath()); // NOI18N
            ex.initCause(e);
            throw ex;
        }
    }

    /**
     * Seeks versioned root and then adds all folders
     * under Subversion (so it contains metadata),
     */
    private void addDirectories(final File dir) throws SVNClientException  {
        File parent = dir.getParentFile();
        if (parent != null) {            
            if (Subversion.getInstance().isManaged(parent) && !hasMetadata(parent)) {
                addDirectories(parent);  // RECURSION
            }
            SvnClient client = Subversion.getInstance().getClient(false);
            client.addDirectory(dir, false);
            Utils.post(new Runnable() {
                public void run() {
                    cache.refresh(dir, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                }
            });                
        } else {
            throw new SVNClientException("Reached FS root, but it's still not Subversion versioned!"); // NOI18N
        }
    }
}
