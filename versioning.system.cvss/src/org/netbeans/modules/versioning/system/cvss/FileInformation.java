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

    /**
     * Retrieves file's CVS Entry.
     *
     * @param file file this information belongs to or null if you do not want the entry to be read from disk 
     * in case it is not loaded yet
     * @return Entry parsed entry form the CVS/Entries file or null if the file does not exist,
     * is not versioned or its Entry is invalid
     */
    public Entry getEntry(File file) {
        if (cvsEntry == null && file != null) readEntry(file);
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
            return "";   // NOI18N                     
        }
    }    

    public String getShortStatusText() {
        ResourceBundle loc = NbBundle.getBundle(FileInformation.class);
        if (status == FileInformation.STATUS_NOTVERSIONED_EXCLUDED) {
            return loc.getString("CTL_FileInfoStatus_Excluded_Short");
        } else if (status == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) {
            return loc.getString("CTL_FileInfoStatus_NewLocally_Short");
        } else if (status == FileInformation.STATUS_VERSIONED_ADDEDLOCALLY) {
            return loc.getString("CTL_FileInfoStatus_AddedLocally_Short");
        } else if (status == FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY) {
            return loc.getString("CTL_FileInfoStatus_RemovedLocally_Short");
        } else if (status == FileInformation.STATUS_VERSIONED_DELETEDLOCALLY) {
            return loc.getString("CTL_FileInfoStatus_DeletedLocally_Short");
        } else if (status == FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY) {
            return loc.getString("CTL_FileInfoStatus_ModifiedLocally_Short");
        } else if (status == FileInformation.STATUS_VERSIONED_CONFLICT) {
            return loc.getString("CTL_FileInfoStatus_Conflict_Short");
        } else if (status == FileInformation.STATUS_VERSIONED_MERGE) {
            return loc.getString("CTL_FileInfoStatus_ModifiedLocally_Short");
        } else {
            return "";      // NOI18N                  
        }
    }    
}

