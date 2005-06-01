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

import org.openide.filesystems.*;
import org.netbeans.modules.versioning.system.cvss.settings.MetadataAttic;
import org.netbeans.modules.versioning.system.cvss.settings.CvsModuleConfig;
import org.netbeans.modules.masterfs.providers.InterceptionListener;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.admin.Entry;
import org.netbeans.lib.cvsclient.admin.AdminHandler;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * Handles events fired from the filesystem such as file/folder create/delete/move.
 * 
 * @author Maros Sandor
 */
class FilesystemHandler implements FileChangeListener, InterceptionListener, PropertyChangeListener {
        
    private static final String FILENAME_CVS = "CVS";

    private final CvsVersioningSystem   cvs;
    private final FileStatusCache       cache;
    
    private final Set                   hookedFilesystems = new HashSet(1);

    public FilesystemHandler(CvsVersioningSystem cvs) {
        this.cvs = cvs;
        cache = cvs.getStatusCache();
        CvsModuleConfig.getDefault().addPropertyChangeListener(this);
    }
    
    /**
     * Deregisters current Filesystem listeners, re-reads CVS module configuration and registers
     * new listeners based on the configuration.
     */ 
    private synchronized void restart() {
        for (Iterator i = hookedFilesystems.iterator(); i.hasNext();) {
            FileSystem fileSystem = (FileSystem) i.next();
            fileSystem.removeFileChangeListener(this);
            i.remove();
        }
        
        Set setups = CvsModuleConfig.getDefault().getManagedRoots();
        for (Iterator i = setups.iterator(); i.hasNext();) {
            CvsModuleConfig.ManagedRoot managedRoot = (CvsModuleConfig.ManagedRoot) i.next();
            addFilesystemHandler(new File(managedRoot.getPath()));
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
        
        File parent = FileUtil.toFile(newFile.getParent());
        File removed = new File(parent, oldName + "." + oldExtension);
        
        fileDeletedImpl(removed);
        addNewFile(FileUtil.toFile(newFile));
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
        // not interested
    }
    
    private void addFilesystemHandler(File root) {
        root = FileUtil.normalizeFile(root);
        FileSystem fs = getFileSystem(root);
        if (hookedFilesystems.contains(fs)) return;
        fs.addFileChangeListener(this);
        hookedFilesystems.add(fs);
    }

    private FileSystem getFileSystem(File file) {
        FileObject rootFileObject = FileUtil.toFileObject(file);
        if (rootFileObject == null) throw new IllegalArgumentException("File must exist!");
        try {
            return rootFileObject.getFileSystem();
        } catch (FileStateInvalidException e) {
            IllegalArgumentException iae = new IllegalArgumentException("File resides in an invalid Filesystem");
            iae.initCause(e);
            throw iae;
        }
    }
    
    private void addNewFile(File file) {
        if (!cvs.isManaged(file)) return;
        // TODO: do automatic handling for .cvignore files
        cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (CvsModuleConfig.PROP_MANAGED_ROOTS.equals(evt.getPropertyName())) {
            restart();
        }
    }

    /**
     * If a regular file is deleted then update its Entries as if it has been removed.
     * 
     * @param file deleted file
     */ 
    private void fileDeletedImpl(File file) {
        if (!cvs.isManaged(file)) return;
        
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
        if (!cvs.isManaged(file)) return;
        file = file.getParentFile();
        saveMetadata(file);
    }

    public void deleteSuccess(FileObject fo) {
    }

    public void deleteFailure(FileObject fo) {
    
    }
}
