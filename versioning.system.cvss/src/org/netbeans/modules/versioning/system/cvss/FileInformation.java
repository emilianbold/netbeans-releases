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

import org.netbeans.lib.cvsclient.admin.Entry;
import org.openide.util.NbBundle;

import java.util.*;
import java.io.Serializable;
import java.io.File;
import java.io.IOException;

/**
 * Immutable class encapsulating status of a file.
 * 
 * @author Maros Sandor
 */
public class FileInformation implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private static final ResourceBundle loc = NbBundle.getBundle(FileInformation.class);
    
    /**
     * There is nothing known about the file, it may not even exist.
     */ 
    public static final int STATUS_UNKNOWN                      = 0;

    /**
     * The file is not managed by the module, i.e. the user does not wish it to be under control of this
     * versioning system module. All files except files under versioned roots have this status.
     */ 
    public static final int STATUS_NOTVERSIONED_NOTMANAGED      = 1;
    
    /**
     * The file exists locally but is NOT under version control because it should not be (i.e. is listed
     * in .cvsignore or resides under an excluded folder). The file itself IS under a versioned root.
     */ 
    public static final int STATUS_NOTVERSIONED_EXCLUDED        = 2;

    /**
     * The file exists locally but is NOT under version control, mostly because it has not been added
     * to the repository yet.
     */ 
    public static final int STATUS_NOTVERSIONED_NEWLOCALLY      = 4;
        
    /**
     * The file is under version control and is in sync with repository.
     */ 
    public static final int STATUS_VERSIONED_UPTODATE           = 8;
    
    /**
     * The file is modified locally and was not yet modified in repository.
     */ 
    public static final int STATUS_VERSIONED_MODIFIEDLOCALLY    = 16;
    
    /**
     * The file was not modified locally but an updated version exists in repository.
     */ 
    public static final int STATUS_VERSIONED_MODIFIEDINREPOSITORY = 32;
    
    /**
     * Merging during update resulted in merge conflict. Conflicts in the local copy must be resolved before
     * the file can be commited.  
     */ 
    public static final int STATUS_VERSIONED_CONFLICT           = 64;

    /**
     * The file was modified both locally and remotely and these changes may or may not result in
     * merge conflict. 
     */ 
    public static final int STATUS_VERSIONED_MERGE              = 128;
    
    /**
     * The file does NOT exist locally and exists in repository, it has beed removed locally, waits
     * for commit.
     */ 
    public static final int STATUS_VERSIONED_REMOVEDLOCALLY     = 256;
    
    /**
     * The file does NOT exist locally but exists in repository and has not yet been downloaded. 
     */ 
    public static final int STATUS_VERSIONED_NEWINREPOSITORY    = 512;

    /**
     * The file has been removed from repository. 
     */ 
    public static final int STATUS_VERSIONED_REMOVEDINREPOSITORY = 1024;

    /**
     * The file does NOT exist locally and exists in repository, it has beed removed locally.
     */ 
    public static final int STATUS_VERSIONED_DELETEDLOCALLY     = 2048;
    
    /**
     * The file exists locally and has beed scheduled for addition to repository. This status represents
     * state after the CVS 'add' command.
     */ 
    public static final int STATUS_VERSIONED_ADDEDLOCALLY       = 4096;

    /**
     * Indicates that the File object is a directory. This flag is only present for non-existing directories!
     * You must first check File.isDirectory() and only if it returns false you can rely on this flag.
     */ 
    public static final int FLAG_DIRECTORY                      = 65536;
    
    public static final int STATUS_ALL = ~0;

    public static final int STATUS_MANAGED = STATUS_ALL & ~STATUS_NOTVERSIONED_NOTMANAGED;
    
    public static final int STATUS_IN_REPOSITORY = STATUS_VERSIONED_UPTODATE | STATUS_VERSIONED_MODIFIEDLOCALLY |
            STATUS_VERSIONED_MODIFIEDINREPOSITORY | STATUS_VERSIONED_CONFLICT | STATUS_VERSIONED_MERGE | 
            STATUS_VERSIONED_REMOVEDLOCALLY | STATUS_VERSIONED_NEWINREPOSITORY | STATUS_VERSIONED_REMOVEDINREPOSITORY |
            STATUS_VERSIONED_DELETEDLOCALLY;
            
    public static final int STATUS_LOCAL_CHANGE = 
            FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | 
            FileInformation.STATUS_VERSIONED_ADDEDLOCALLY | 
            FileInformation.STATUS_VERSIONED_CONFLICT | 
            FileInformation.STATUS_VERSIONED_DELETEDLOCALLY |
            FileInformation.STATUS_VERSIONED_MERGE |
            FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY |
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY;

    public static final int STATUS_REMOTE_CHANGE = 
            FileInformation.STATUS_VERSIONED_MERGE | 
            FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY |
            FileInformation.STATUS_VERSIONED_NEWINREPOSITORY |
            FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY;
    
    
    /**
     * Status constant.
     */ 
    private final int   status;
    
    /**
     * Entry from the CVS directory, if it exists and has been read.
     */ 
    private transient Entry       cvsEntry;

    private static final String STATUS_UNKNOWN_EXT = "W";  // NOI18N
    private static final String STATUS_NOTVERSIONED_NOTMANAGED_EXT = "Z"; // NOI18N
    private static final String STATUS_NOTVERSIONED_EXCLUDED_EXT = "I"; // NOI18N
    private static final String STATUS_NOTVERSIONED_NEWLOCALLY_EXT = "?"; // NOI18N
    private static final String STATUS_VERSIONED_UPTODATE_EXT = "S"; // NOI18N
    private static final String STATUS_VERSIONED_MODIFIEDLOCALLY_EXT = "M"; // NOI18N
    private static final String STATUS_VERSIONED_MODIFIEDINREPOSITORY_EXT = "G"; // NOI18N
    private static final String STATUS_VERSIONED_CONFLICT_EXT = "C"; // NOI18N
    private static final String STATUS_VERSIONED_MERGE_EXT = "P"; // NOI18N
    private static final String STATUS_VERSIONED_REMOVEDLOCALLY_EXT = "R"; // NOI18N
    private static final String STATUS_VERSIONED_NEWINREPOSITORY_EXT = "N"; // NOI18N
    private static final String STATUS_VERSIONED_REMOVEDINREPOSITORY_EXT = "D"; // NOI18N
    private static final String STATUS_VERSIONED_DELETEDLOCALLY_EXT = "E"; // NOI18N
    private static final String STATUS_VERSIONED_ADDEDLOCALLY_EXT = "A"; // NOI18N

    public FileInformation() {
        status = 0;
    }

    public FileInformation(int status, Entry cvsEntry) {
        this.status = status;
        this.cvsEntry = cvsEntry;
    }

    public FileInformation(int status) {
        this(status, null);
    }

    /**
     * Retrieves the status constant representing status of the file.
     * 
     * @return one of status constants
     */ 
    public int getStatus() {
        return status;
    }

    /** Converts to String decodeable by {@link #fromExternalForm} */
    public String toExternalForm() {
        switch (status) {
            case STATUS_UNKNOWN:
                return STATUS_UNKNOWN_EXT;
            case STATUS_NOTVERSIONED_NOTMANAGED:
                return STATUS_NOTVERSIONED_NOTMANAGED_EXT;
            case STATUS_NOTVERSIONED_EXCLUDED:
                return STATUS_NOTVERSIONED_EXCLUDED_EXT;
            case STATUS_NOTVERSIONED_NEWLOCALLY:
                return STATUS_NOTVERSIONED_NEWLOCALLY_EXT;
            case STATUS_VERSIONED_UPTODATE:
                return STATUS_VERSIONED_UPTODATE_EXT;
            case STATUS_VERSIONED_MODIFIEDLOCALLY:
                return STATUS_VERSIONED_MODIFIEDLOCALLY_EXT;
            case STATUS_VERSIONED_MODIFIEDINREPOSITORY:
                return STATUS_VERSIONED_MODIFIEDINREPOSITORY_EXT;
            case STATUS_VERSIONED_CONFLICT:
                return STATUS_VERSIONED_CONFLICT_EXT;
            case STATUS_VERSIONED_MERGE:
                return STATUS_VERSIONED_MERGE_EXT;
            case STATUS_VERSIONED_REMOVEDLOCALLY:
                return STATUS_VERSIONED_REMOVEDLOCALLY_EXT;
            case STATUS_VERSIONED_NEWINREPOSITORY:
                return STATUS_VERSIONED_NEWINREPOSITORY_EXT;
            case STATUS_VERSIONED_REMOVEDINREPOSITORY:
                return STATUS_VERSIONED_REMOVEDINREPOSITORY_EXT;
            case STATUS_VERSIONED_DELETEDLOCALLY:
                return STATUS_VERSIONED_DELETEDLOCALLY_EXT;
            case STATUS_VERSIONED_ADDEDLOCALLY:
                return STATUS_VERSIONED_ADDEDLOCALLY_EXT;
            default:
                throw new IllegalStateException("Status " + status);
        }
    }

    /** Decodes external form produced by {@link #toExternalForm}. */
    public static FileInformation fromExternalForm(String ext) {
        if (STATUS_UNKNOWN_EXT.equals(ext)) {
            return new FileInformation(STATUS_UNKNOWN);
        } else if (STATUS_NOTVERSIONED_NOTMANAGED_EXT.equals(ext)) {
            return new FileInformation(STATUS_NOTVERSIONED_NOTMANAGED);
        } else if (STATUS_NOTVERSIONED_EXCLUDED_EXT.equals(ext)) {
            return new FileInformation(STATUS_NOTVERSIONED_EXCLUDED);
        } else if (STATUS_NOTVERSIONED_NEWLOCALLY_EXT.equals(ext)) {
            return new FileInformation(STATUS_NOTVERSIONED_NEWLOCALLY);
        } else if (STATUS_VERSIONED_UPTODATE_EXT.equals(ext)) {
            return new FileInformation(STATUS_VERSIONED_UPTODATE);
        } else if (STATUS_VERSIONED_MODIFIEDLOCALLY_EXT.equals(ext)) {
            return new FileInformation(STATUS_VERSIONED_MODIFIEDLOCALLY);
        } else if (STATUS_VERSIONED_MODIFIEDINREPOSITORY_EXT.equals(ext)) {
            return new FileInformation(STATUS_VERSIONED_MODIFIEDINREPOSITORY);
        } else if (STATUS_VERSIONED_CONFLICT_EXT.equals(ext)) {
            return new FileInformation(STATUS_VERSIONED_CONFLICT);
        } else if (STATUS_VERSIONED_MERGE_EXT.equals(ext)) {
            return new FileInformation(STATUS_VERSIONED_MERGE);
        } else if (STATUS_VERSIONED_REMOVEDLOCALLY_EXT.equals(ext)) {
            return new FileInformation(STATUS_VERSIONED_REMOVEDLOCALLY);
        } else if (STATUS_VERSIONED_NEWINREPOSITORY_EXT.equals(ext)) {
            return new FileInformation(STATUS_VERSIONED_NEWINREPOSITORY);
        } else if (STATUS_VERSIONED_REMOVEDINREPOSITORY_EXT.equals(ext)) {
            return new FileInformation(STATUS_VERSIONED_REMOVEDINREPOSITORY);
        } else if (STATUS_VERSIONED_DELETEDLOCALLY_EXT.equals(ext)) {
            return new FileInformation(STATUS_VERSIONED_DELETEDLOCALLY);
        } else if (STATUS_VERSIONED_ADDEDLOCALLY_EXT.equals(ext)) {
            return new FileInformation(STATUS_VERSIONED_ADDEDLOCALLY);
        } else {
            throw new IllegalStateException("External form " + ext);
        }
    }

    /**
     * Retrieves file's revision number. This entry is then cached. 
     * 
     * @param file file this information belongs to
     * @return revision number or special value for newly added or removed files. May be null if the file does not exist,
     * is not versioned or its Entry has not been parsed.
     */ 
    public String getRevision(File file) {
        if (cvsEntry == null) readEntry(file);
        return (cvsEntry != null) ? cvsEntry.getRevision() : null; 
    }

    private void readEntry(File file) {
        try {
            cvsEntry = CvsVersioningSystem.getInstance().getAdminHandler().getEntry(file);
        } catch (IOException e) {
            // no entry for this file, ignore
        }
    }

    /**
     * Two FileInformation objects are equal if their status contants are equal. CVS Entries are not tested. 
     *  
     * @param o other object
     * @return true if status constants of both object are equal, false otherwise
     */ 
    public boolean equals(Object o) {
        return o instanceof FileInformation && status == ((FileInformation) o).status;
    }

    public int hashCode() {
        return status;
    }

    /**
     * Returns localized text representation of status.
     * 
     * @return
     */ 
    public String getStatusText() {
        if (status == FileInformation.STATUS_UNKNOWN) {
            return loc.getString("CTL_FileInfoStatus_Unknown");            
        } else if (status == FileInformation.STATUS_NOTVERSIONED_EXCLUDED) {
            return loc.getString("CTL_FileInfoStatus_Excluded");
        } else if (status == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) {
            return loc.getString("CTL_FileInfoStatus_NewLocally");
        } else if (status == FileInformation.STATUS_VERSIONED_ADDEDLOCALLY) {
            return loc.getString("CTL_FileInfoStatus_AddedLocally");
        } else if (status == FileInformation.STATUS_VERSIONED_UPTODATE) {
            return loc.getString("CTL_FileInfoStatus_UpToDate");
        } else if (status == FileInformation.STATUS_VERSIONED_NEWINREPOSITORY) {
            return loc.getString("CTL_FileInfoStatus_NewInRepository");
        } else if (status == FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY) {
            return loc.getString("CTL_FileInfoStatus_RemovedLocally");
        } else if (status == FileInformation.STATUS_VERSIONED_DELETEDLOCALLY) {
            return loc.getString("CTL_FileInfoStatus_DeletedLocally");
        } else if (status == FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY) {
            return loc.getString("CTL_FileInfoStatus_ModifiedLocally");
        } else if (status == FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY) {
            return loc.getString("CTL_FileInfoStatus_ModifiedInRepository");
        } else if (status == FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY) {
            return loc.getString("CTL_FileInfoStatus_RemovedInRepository");
        } else if (status == FileInformation.STATUS_VERSIONED_CONFLICT) {
            return loc.getString("CTL_FileInfoStatus_Conflict");
        } else if (status == FileInformation.STATUS_VERSIONED_MERGE) {
            return loc.getString("CTL_FileInfoStatus_Merge");
        } else {
            return "";                        
        }
    }    
}

