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

import org.netbeans.modules.versioning.system.cvss.settings.MetadataAttic;
import org.netbeans.modules.masterfs.providers.InterceptionListener;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.admin.Entry;
import org.netbeans.lib.cvsclient.admin.AdminHandler;
import org.openide.filesystems.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Handles events fired from the filesystem such as file/folder create/delete/move.
 * 
 * @author Maros Sandor
 */
class FilesystemHandler implements FileChangeListener, InterceptionListener {
        
    private static final String FILENAME_CVS = "CVS";

    private final FileStatusCache       cache;
    
    public FilesystemHandler(CvsVersioningSystem cvs) {
        cache = cvs.getStatusCache();
    }
    
    /**
     * Registers listeners to all disk filesystems.
     */ 
    void init() {
        Set filesystems = new HashSet();
        File [] roots = File.listRoots();
        for (int i = 0; i < roots.length; i++) {
            File root = roots[i];
            FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(root));
            if (fo == null) continue;
            try {
                filesystems.add(fo.getFileSystem());
            } catch (FileStateInvalidException e) {
                // ignore invalid filesystems
            }
        }
        for (Iterator i = filesystems.iterator(); i.hasNext();) {
            FileSystem fileSystem = (FileSystem) i.next();
            fileSystem.addFileChangeListener(this);
        }
    }

    public void fileFolderCreated(FileEvent fe) {
        addNewFile(FileUtil.toFile(fe.getFile()));
    }

    public void fileDataCreated(FileEvent fe) {
        addNewFile(FileUtil.toFile(fe.getFile()));
    }
    
    public void fileChanged(FileEvent fe) {
        cache.refreshCached(FileUtil.toFile(fe.getFile()), FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
    }

    public void fileDeleted(FileEvent fe) {
        fileDeletedImpl(FileUtil.toFile(fe.getFile()));
    }

    public void fileRenamed(FileRenameEvent fe) {
        FileObject newFile = fe.getFile();
        String oldName = fe.getName();
        String oldExtension = fe.getExt();
        if (oldExtension.length() > 0) oldExtension = "." + oldExtension;
        
        File parent = FileUtil.toFile(newFile.getParent());
        File removed = new File(parent, oldName + oldExtension);
        
        fileDeletedImpl(removed);
        addNewFile(FileUtil.toFile(newFile));
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
        // not interested
    }
    
    private void addNewFile(File file) {
        int status = cache.getStatus(file).getStatus();
        if ((status & FileInformation.STATUS_MANAGED) == 0) return;
        // TODO: if .cvignore is added, refresh statuses of whole directory

        StandardAdminHandler sah = new StandardAdminHandler();
        Entry entry = null;
        try {
            entry = sah.getEntry(file);
        } catch (IOException e) {
        }
        if (entry != null && !entry.isDirectory() && entry.isUserFileToBeRemoved()) {
            cvsUndoRemoveLocally(sah, file, entry);    
        }
        cache.directoryContentChanged(file);
    }

    /**
     * If a regular file is deleted then update its Entries as if it has been removed.
     * 
     * @param file deleted file
     */ 
    private void fileDeletedImpl(File file) {
        if (file == null) return;
        
        StandardAdminHandler sah = new StandardAdminHandler();
        Entry entry = null;
        try {
            entry = sah.getEntry(file);
        } catch (IOException e) {
        }
        if (entry != null && !entry.isDirectory() && !entry.isUserFileToBeRemoved()) {
            cvsRemoveLocally(sah, file, entry);    
        }
        cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
    }
    
    /**
     * Emulates the 'cvs remove' command by modifying Entries. We do this to avoid contacting the
     * server.
     * 
     * @param ah
     * @param file
     * @param entry
     */ 
    private void cvsRemoveLocally(AdminHandler ah, File file, Entry entry) {
        entry.setRevision("-" + entry.getRevision());
        entry.setConflict(Entry.DUMMY_TIMESTAMP);
        try {
            ah.setEntry(file, entry);
        } catch (IOException e) {
            // failed to set entry, the file will be probably resurrected during update
        }
    }

    private void cvsUndoRemoveLocally(AdminHandler ah, File file, Entry entry) {
        entry.setRevision(entry.getRevision().substring(1));
        entry.setConflict(Entry.getLastModifiedDateFormatter().format(new Date(System.currentTimeMillis() - 1000)));
        try {
            ah.setEntry(file, entry);
        } catch (IOException e) {
            // failed to set entry, the file will be probably resurrected during update
        }
    }
    
    /**
     * The folder's metadata is about to be deleted. We have to save all metadata information.
     * 
     * @param dir
     */ 
    private void saveMetadata(File dir) {
        dir = FileUtil.normalizeFile(dir);
        if (MetadataAttic.getMetadata(dir) != null) return;
        try {
            CvsMetadata data = CvsMetadata.readAndRemove(dir);
            MetadataAttic.setMetadata(dir, data);
        } catch (IOException e) {
            // cannot read folder metadata, the folder is most probably not versioned
            return;
        }
    }

    /**
     * We save all CVS metadata to be able to commit files that were
     * in that directory.
     * 
     * @param fo
     */ 
    public void beforeDelete(FileObject fo) {
        File file = FileUtil.toFile(fo);
        if (file.isFile()) {
            file = file.getParentFile();
        }
        if (!file.getName().equals(FILENAME_CVS)) return;
        if ((cache.getStatus(file.getParentFile()).getStatus() & FileInformation.STATUS_MANAGED) == 0) return;
        file = file.getParentFile();
        saveMetadata(file);
    }

    public void deleteSuccess(FileObject fo) {
    }

    public void deleteFailure(FileObject fo) {
    
    }
}
