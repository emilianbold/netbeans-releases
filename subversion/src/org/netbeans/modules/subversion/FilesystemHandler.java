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

package org.netbeans.modules.subversion;

import javax.swing.SwingUtilities;
import org.netbeans.modules.subversion.util.FileUtils;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.subversion.client.SvnClient;
import java.io.File;
import java.io.IOException;
import java.util.*;   
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
        Subversion.LOG.fine("beforeDelete " + file);
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
        Subversion.LOG.fine("doDelete " + file);
        boolean isMetadata = SvnUtils.isPartOfSubversionMetadata(file);        
        if (!isMetadata) {
            remove(file);
        }
    }

    public void afterDelete(final File file) {
        Subversion.LOG.fine("afterDelete " + file);
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
        Subversion.LOG.fine("beforeMove " + from +  " -> " + to);
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
        Subversion.LOG.fine("doMove " + from +  " -> " + to);
        if (SwingUtilities.isEventDispatchThread()) {
            
            Subversion.LOG.log(Level.INFO, "Warning: launching external process in AWT", new Exception().fillInStackTrace());
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
        Subversion.LOG.fine("afterMove " + from +  " -> " + to);
        Utils.post(new Runnable() {
            public void run() {                
                // there might have been no notification 
                // for the children files - refresh them all
                SvnUtils.refreshRecursively(to);
                cache.onNotify(to, null); // as if there were an event
                File parent = to.getParentFile();
                if (parent != null) {
                    if (from.equals(to)) {
                        Subversion.LOG.warning( "Wrong (identity) rename event for " + from.getAbsolutePath());                        
                    }
                    cache.onNotify(from, null); // as if there were an event
                }
            }
        });
    }
    
    public boolean beforeCreate(File file, boolean isDirectory) {
        Subversion.LOG.fine("beforeCreate " + file);
        if ( SvnUtils.isPartOfSubversionMetadata(file)) {            
            synchronized(invalidMetadata) {
                File p = file;
                while(!p.getName().equals(".svn") && !p.getName().equals("_svn")) {                    
                    p = p.getParentFile();
                    assert p != null : "file " + file + " doesn't have a .svn parent";
                }                            
                invalidMetadata.add(p);
            }            
            return false;
        } else {
            if (!file.exists()) {                
                try {
                    SvnClient client = Subversion.getInstance().getClient(true);                                        
                    // check if the file wasn't just deleted in this session
                    revertDeleted(client, file, true); 
                } catch (SVNClientException ex) {
                    SvnClientExceptionHandler.notifyException(ex, false, false);
                }
            }
            return false;
        }
    }

    /**
     * Returns all direct parent folders from the given file which are scheduled for deletion
     * 
     * @param file
     * @param client
     * @return a list of folders 
     * @throws org.tigris.subversion.svnclientadapter.SVNClientException
     */
    private static List<File> getDeletedParents(File file, SvnClient client) throws SVNClientException {
        List<File> ret = new ArrayList<File>();
        for(File parent = file.getParentFile(); parent != null; parent = parent.getParentFile()) {        
            ISVNStatus status = getStatus(client, parent);
            if (status == null || !status.getTextStatus().equals(SVNStatusKind.DELETED)) {                                                            
                return ret;
            }
            ret.add(parent);                                      
        }        
        return ret;
    }        
    
    public void doCreate(File file, boolean isDirectory) throws IOException {
        // do nothing
    }

    public void afterCreate(final File file) {   
        Subversion.LOG.fine("afterCreate " + file);
        Utils.post(new Runnable() {
            public void run() {
                if (file == null) return;
                int status = cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN).getStatus();
                if ((status & FileInformation.STATUS_MANAGED) == 0) {
                    return;
                }
                if (file.isDirectory()) cache.directoryContentChanged(file);
            }
        });
    }
    
    public void afterChange(final File file) {        
        Subversion.LOG.fine("afterChange " + file);
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

    private void revertDeleted(SvnClient client, final File file, boolean checkParents) {

        try {
            ISVNStatus status = getStatus(client, file);
            if (status != null && status.getTextStatus().equals(SVNStatusKind.DELETED)) {
                if(checkParents) {
                    // we have a file scheduled for deletion but it's giong to created again,
                    // so it's parent folder can't stay deleted either
                    List<File> deletedParents = getDeletedParents(file, client);
                    client.revert(deletedParents.toArray(new File[deletedParents.size()]), false);                        
                }        
                        
                // reverting the file will set the metadata uptodate
                client.revert(file, false);
                // our goal was ony to fix the metadata ->
                //  -> get rid of the reverted file
                file.delete();
            }
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, false, false);
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
                        // check if the file wasn't just deleted in this session
                        revertDeleted(client, dstFile, false);
                        
                        // check the status - if the file isn't in the repository yet ( ADDED | UNVERSIONED )
                        // then it also can't be moved via the svn client
                        ISVNStatus status = getStatus(client, srcFile);
                        if (status != null && status.getTextStatus().equals(SVNStatusKind.ADDED)) {                                            
                            client.revert(srcFile, true);  
                            renameFile(srcFile, dstFile);                
                        } else if (status != null && status.getTextStatus().equals(SVNStatusKind.UNVERSIONED)) {                                            
                            renameFile(srcFile, dstFile);                            
                        } else {                            
                            List<File> srcChildren = listAllChildren(srcFile);                
                            client.move(srcFile, dstFile, force);
                            
                            // fire events explicitly for all children which are already gone
                            for(File f : srcChildren) {
                                cache.onNotify(f, null);    
                            }                                 
                        }                        
                                                
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
                        
                        IOException ex = new IOException("Subversion failed to rename " + srcFile.getAbsolutePath() + " to: " + dstFile.getAbsolutePath()); // NOI18N
                        ex.initCause(e);
                        throw ex;
                            
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

    private void renameFile(File srcFile, File dstFile) {
        List<File> srcChildren = listAllChildren(srcFile);        
        srcFile.renameTo(dstFile);

        // notify the cache
        cache.onNotify(srcFile, null);
        for(File f : srcChildren) {
            // fire events explicitly for 
            // all children which are already gone
            cache.onNotify(f, null);    
        }        
        cache.onNotify(dstFile, null);        
    }
        
    private List<File> listAllChildren(File file) {
        if(file.isFile()) return new ArrayList<File>(0);        
        List<File> ret = new ArrayList<File>();     
        File[] files = file.listFiles();
        if(files != null) {
            for(File f : files) {
                ret.add(f);
                ret.addAll(listAllChildren(f));
            }
        }
        return ret;
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
    
    private static ISVNStatus getStatus(SvnClient client, File file) throws SVNClientException {
        // a direct cache call could, because of the synchrone beforeCreate handling, 
        // trigger an reentrant call on FS => we have to check manually 
        return client.getSingleStatus(file);
    }
    
}
