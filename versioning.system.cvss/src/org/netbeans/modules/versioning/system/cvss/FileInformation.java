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
     * Directory indicator.
     */ 
    private final boolean   isDirectory;
    
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

    /**
     * For deserialization purposes only.
     */ 
    public FileInformation() {
        status = 0;
        isDirectory = false;
    }

    FileInformation(int status, Entry cvsEntry, boolean isDirectory) {
        this.status = status;
        this.cvsEntry = cvsEntry;
        this.isDirectory = isDirectory;
    }

    FileInformation(int status, boolean isDirectory) {
        this(status, null, isDirectory);
    }

    /**
     * Retrieves the status constant representing status of the file.
     * 
     * @return one of status constants
     */ 
    public int getStatus() {
        return status;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    /** Converts to String decodeable by {@link #fromExternalForm} */
    public String toExternalForm() {
        String dir = isDirectory ? "d" : "";
        switch (status) {
            case STATUS_UNKNOWN:
                return STATUS_UNKNOWN_EXT + dir;
            case STATUS_NOTVERSIONED_NOTMANAGED:
                return STATUS_NOTVERSIONED_NOTMANAGED_EXT + dir;
            case STATUS_NOTVERSIONED_EXCLUDED:
                return STATUS_NOTVERSIONED_EXCLUDED_EXT + dir;
            case STATUS_NOTVERSIONED_NEWLOCALLY:
                return STATUS_NOTVERSIONED_NEWLOCALLY_EXT + dir;
            case STATUS_VERSIONED_UPTODATE:
                return STATUS_VERSIONED_UPTODATE_EXT + dir;
            case STATUS_VERSIONED_MODIFIEDLOCALLY:
                return STATUS_VERSIONED_MODIFIEDLOCALLY_EXT + dir;
            case STATUS_VERSIONED_MODIFIEDINREPOSITORY:
                return STATUS_VERSIONED_MODIFIEDINREPOSITORY_EXT + dir;
            case STATUS_VERSIONED_CONFLICT:
                return STATUS_VERSIONED_CONFLICT_EXT + dir;
            case STATUS_VERSIONED_MERGE:
                return STATUS_VERSIONED_MERGE_EXT + dir;
            case STATUS_VERSIONED_REMOVEDLOCALLY:
                return STATUS_VERSIONED_REMOVEDLOCALLY_EXT + dir;
            case STATUS_VERSIONED_NEWINREPOSITORY:
                return STATUS_VERSIONED_NEWINREPOSITORY_EXT + dir;
            case STATUS_VERSIONED_REMOVEDINREPOSITORY:
                return STATUS_VERSIONED_REMOVEDINREPOSITORY_EXT + dir;
            case STATUS_VERSIONED_DELETEDLOCALLY:
                return STATUS_VERSIONED_DELETEDLOCALLY_EXT + dir;
            case STATUS_VERSIONED_ADDEDLOCALLY:
                return STATUS_VERSIONED_ADDEDLOCALLY_EXT + dir;
            default:
                throw new IllegalStateException("Status " + status);
        }
    }

    /** Decodes external form produced by {@link #toExternalForm}. */
    public static FileInformation fromExternalForm(String ext) {
        boolean dir = ext.length() == 2;
        if (dir) {
            ext = ext.substring(0, 1);
        }
        if (STATUS_UNKNOWN_EXT.equals(ext)) {
            return new FileInformation(STATUS_UNKNOWN, dir);
        } else if (STATUS_NOTVERSIONED_NOTMANAGED_EXT.equals(ext)) {
            return new FileInformation(STATUS_NOTVERSIONED_NOTMANAGED, dir);
        } else if (STATUS_NOTVERSIONED_EXCLUDED_EXT.equals(ext)) {
            return new FileInformation(STATUS_NOTVERSIONED_EXCLUDED, dir);
        } else if (STATUS_NOTVERSIONED_NEWLOCALLY_EXT.equals(ext)) {
            return new FileInformation(STATUS_NOTVERSIONED_NEWLOCALLY, dir);
        } else if (STATUS_VERSIONED_UPTODATE_EXT.equals(ext)) {
            return new FileInformation(STATUS_VERSIONED_UPTODATE, dir);
        } else if (STATUS_VERSIONED_MODIFIEDLOCALLY_EXT.equals(ext)) {
            return new FileInformation(STATUS_VERSIONED_MODIFIEDLOCALLY, dir);
        } else if (STATUS_VERSIONED_MODIFIEDINREPOSITORY_EXT.equals(ext)) {
            return new FileInformation(STATUS_VERSIONED_MODIFIEDINREPOSITORY, dir);
        } else if (STATUS_VERSIONED_CONFLICT_EXT.equals(ext)) {
            return new FileInformation(STATUS_VERSIONED_CONFLICT, dir);
        } else if (STATUS_VERSIONED_MERGE_EXT.equals(ext)) {
            return new FileInformation(STATUS_VERSIONED_MERGE, dir);
        } else if (STATUS_VERSIONED_REMOVEDLOCALLY_EXT.equals(ext)) {
            return new FileInformation(STATUS_VERSIONED_REMOVEDLOCALLY, dir);
        } else if (STATUS_VERSIONED_NEWINREPOSITORY_EXT.equals(ext)) {
            return new FileInformation(STATUS_VERSIONED_NEWINREPOSITORY, dir);
        } else if (STATUS_VERSIONED_REMOVEDINREPOSITORY_EXT.equals(ext)) {
            return new FileInformation(STATUS_VERSIONED_REMOVEDINREPOSITORY, dir);
        } else if (STATUS_VERSIONED_DELETEDLOCALLY_EXT.equals(ext)) {
            return new FileInformation(STATUS_VERSIONED_DELETEDLOCALLY, dir);
        } else if (STATUS_VERSIONED_ADDEDLOCALLY_EXT.equals(ext)) {
            return new FileInformation(STATUS_VERSIONED_ADDEDLOCALLY, dir);
        } else {
            throw new IllegalStateException("External form " + ext);
        }
    }

    /**
     * Retrieves file's CVS Entry.
     *
     * @param file file this information belongs to
     * @return Entry parsed entry form the CVS/Entries file or null if the file does not exist,
     * is not versioned or its Entry is invalid
     */
    public Entry getEntry(File file) {
        if (cvsEntry == null) readEntry(file);
        return cvsEntry;
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
        if (!(o instanceof FileInformation)) return false;
        FileInformation other = (FileInformation) o;
        if (status != other.status || isDirectory != other.isDirectory) return false;
        return cvsEntry == other.cvsEntry || cvsEntry != null && entriesEqual(cvsEntry, other.cvsEntry); 
    }

    /**
     * Replacement for missing Entry.equals(). It is implemented as a separate method to maintain compatibility.
     * 
     * @param e1 first entry to compare
     * @param e2 second Entry to compare
     * @return true if supplied entries contain equivalent information
     */ 
    private static boolean entriesEqual(Entry e1, Entry e2) {
        if (e2 == null) return false;
        if (e1.getStickyInformation() != e2.getStickyInformation() && 
                e1.getStickyInformation() != null && !e1.getStickyInformation().equals(e2.getStickyInformation())) return false;
        if (!e1.getRevision().equals(e2.getRevision())) return false;
        return true;
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
        ResourceBundle loc = NbBundle.getBundle(FileInformation.class);
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

