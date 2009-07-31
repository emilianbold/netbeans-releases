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
package org.netbeans.modules.mercurial;

import org.openide.util.NbBundle;

import java.io.Serializable;
import java.io.File;
import java.util.*;

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
     * The file exists locally but is NOT under version control because it should not be (i.e. is has
     * the Ignore property set or resides under an excluded folder). The file itself IS under a versioned root.
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
     * state after the 'add' command.
     */ 
    public static final int STATUS_VERSIONED_ADDEDLOCALLY       = 4096;

    public static final int STATUS_ALL = ~0;

    /**
     * All statuses except <tt>STATUS_NOTVERSIONED_NOTMANAGED</tt>
     *
     * <p>Note: it covers ignored files.
     */
    public static final int STATUS_MANAGED = FileInformation.STATUS_ALL & ~FileInformation.STATUS_NOTVERSIONED_NOTMANAGED;


    public static final int STATUS_VERSIONED = FileInformation.STATUS_VERSIONED_UPTODATE |
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY |
            FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY |
            FileInformation.STATUS_VERSIONED_CONFLICT |
            FileInformation.STATUS_VERSIONED_MERGE |
            FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY |
            FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY |
            FileInformation.STATUS_VERSIONED_DELETEDLOCALLY |
            FileInformation.STATUS_VERSIONED_ADDEDLOCALLY;

    public static final int STATUS_IN_REPOSITORY = FileInformation.STATUS_VERSIONED_UPTODATE |
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY |
            FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY |
            FileInformation.STATUS_VERSIONED_CONFLICT |
            FileInformation.STATUS_VERSIONED_MERGE |
            FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY |
            FileInformation.STATUS_VERSIONED_NEWINREPOSITORY |
            FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY |
            FileInformation.STATUS_VERSIONED_DELETEDLOCALLY;

    public static final int STATUS_LOCAL_CHANGE =
            FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | 
            FileInformation.STATUS_VERSIONED_ADDEDLOCALLY | 
            FileInformation.STATUS_VERSIONED_CONFLICT | 
            FileInformation.STATUS_VERSIONED_DELETEDLOCALLY |
            FileInformation.STATUS_VERSIONED_MERGE |
            FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY |
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY;

    /**
     * Modified, in conflict, scheduled for removal or addition;
     * or deleted but with existing entry record.
     */
    public static final int STATUS_REVERTIBLE_CHANGE =
            FileInformation.STATUS_VERSIONED_ADDEDLOCALLY | 
            FileInformation.STATUS_VERSIONED_CONFLICT | 
            FileInformation.STATUS_VERSIONED_MERGE |
            FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY |
            FileInformation.STATUS_VERSIONED_DELETEDLOCALLY |
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY;


    public static final int STATUS_REMOTE_CHANGE = 
            FileInformation.STATUS_VERSIONED_MERGE | 
            FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY |
            FileInformation.STATUS_VERSIONED_NEWINREPOSITORY |
            FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY;
    
    public static final int COUNTER_NEW_FILES = 0;
    public static final int COUNTER_DELETED_FILES = 1;
    public static final int COUNTER_MODIFIED_FILES = 2;
    public static final int COUNTER_CONFLICTED_FILES = 3;
    private static final int COUNTER_FLAT_NEW_FILES = 4;
    private static final int COUNTER_FLAT_DELETED_FILES = 5;
    private static final int COUNTER_FLAT_MODIFIED_FILES = 6;
    private static final int COUNTER_FLAT_CONFLICTED_FILES = 7;
    private static final int COUNTER_MAX_INDEX = COUNTER_CONFLICTED_FILES;
    
    /**
     * Status constant.
     */ 
    private final int   status;

    /**
     * More detailed information about a file, you may disregard the field if not needed.
     */
    private transient FileStatus entry;

    /**
     * Directory indicator, mainly because of files that may have been deleted so file.isDirectory() won't work.
     */ 
    private final boolean   isDirectory;
    
    private final int[] counters = new int[(COUNTER_MAX_INDEX + 1) * 2];

    /**
     * For deserialization purposes only.
     */ 
    public FileInformation() {
        status = 0;
        isDirectory = false;
    }

    public FileInformation(int status, FileStatus entry, boolean isDirectory) {
        this.status = status;
        this.entry = entry;
        this.isDirectory = isDirectory;
        initializeCounters();
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
     * Retrieves file's Status.
     *
     * @param file file this information belongs to or null if you do not want the entry to be read from disk 
     * in case it is not loaded yet
     * @return Status parsed entry form the .svn/entries file or null if the file does not exist,
     * is not versioned or its entry is invalid
     */
    public FileStatus getStatus(File file) {
        return entry;
    }

    /**
     * Returns localized text representation of status.
     * 
     * @return status name, for multistatuses prefers local
     * status name.
     */ 
    public String getStatusText() {
        return getStatusText(~0);
    }    

    /**
     * Returns localized text representation of status.
     *
     * @param displayStatuses statuses bitmask
     *
     * @return status name, for multistatuses prefers local
     * status name, for masked <tt>""</tt>. // NOI18N
     */
    public String getStatusText(int displayStatuses) {
        int status = this.status & displayStatuses;
        ResourceBundle loc = NbBundle.getBundle(FileInformation.class);
        if (status == FileInformation.STATUS_UNKNOWN) {
            return loc.getString("CTL_FileInfoStatus_Unknown");    // NOI18N         
        } else if (FileInformation.match(status, FileInformation.STATUS_NOTVERSIONED_EXCLUDED)) {
            return loc.getString("CTL_FileInfoStatus_Excluded"); // NOI18N
        } else if (FileInformation.match(status, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)) {
            return loc.getString("CTL_FileInfoStatus_NewLocally"); // NOI18N
        } else if (FileInformation.match(status, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
            if (entry != null && entry.isCopied()) {
                return loc.getString("CTL_FileInfoStatus_AddedLocallyCopied"); // NOI18N
            }
            return loc.getString("CTL_FileInfoStatus_AddedLocally"); // NOI18N
        } else if (FileInformation.match(status, FileInformation.STATUS_VERSIONED_UPTODATE)) {
            return loc.getString("CTL_FileInfoStatus_UpToDate"); // NOI18N
        } else if (FileInformation.match(status, FileInformation.STATUS_VERSIONED_CONFLICT)) {
            return loc.getString("CTL_FileInfoStatus_Conflict"); // NOI18N
        } else if (FileInformation.match(status, FileInformation.STATUS_VERSIONED_MERGE)) {
            return loc.getString("CTL_FileInfoStatus_Merge");          // NOI18N   
        } else if (FileInformation.match(status, FileInformation.STATUS_VERSIONED_DELETEDLOCALLY)) {
            return loc.getString("CTL_FileInfoStatus_DeletedLocally"); // NOI18N
        } else if (FileInformation.match(status, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) {
            return loc.getString("CTL_FileInfoStatus_RemovedLocally"); // NOI18N
        } else if (FileInformation.match(status, FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY)) {
            return loc.getString("CTL_FileInfoStatus_ModifiedLocally"); // NOI18N

        } else if (FileInformation.match(status, FileInformation.STATUS_VERSIONED_NEWINREPOSITORY)) {
            return loc.getString("CTL_FileInfoStatus_NewInRepository"); // NOI18N
        } else if (FileInformation.match(status, FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY)) {
            return loc.getString("CTL_FileInfoStatus_ModifiedInRepository"); // NOI18N
        } else if (FileInformation.match(status, FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY)) {
            return loc.getString("CTL_FileInfoStatus_RemovedInRepository"); // NOI18N
        } else {
            return "";   // NOI18N                     
        }
    }    

    /**
     * @return short status name for local changes, for remote
     * changes returns <tt>""</tt> // NOI18N
     */
    public String getShortStatusText() {
        ResourceBundle loc = NbBundle.getBundle(FileInformation.class);
        if (FileInformation.match(status, FileInformation.STATUS_NOTVERSIONED_EXCLUDED)) {
            return loc.getString("CTL_FileInfoStatus_Excluded_Short"); // NOI18N
        } else if (FileInformation.match(status, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)) {
            return loc.getString("CTL_FileInfoStatus_NewLocally_Short"); // NOI18N
        } else if (FileInformation.match(status, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
            if (entry != null && entry.isCopied()) {
                return loc.getString("CTL_FileInfoStatus_AddedLocallyCopied_Short"); // NOI18N
            }
            return loc.getString("CTL_FileInfoStatus_AddedLocally_Short"); // NOI18N
        } else if (status == FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY) {
            return loc.getString("CTL_FileInfoStatus_RemovedLocally_Short"); // NOI18N
        } else if (status == FileInformation.STATUS_VERSIONED_DELETEDLOCALLY) {
            return loc.getString("CTL_FileInfoStatus_DeletedLocally_Short"); // NOI18N
        } else if (FileInformation.match(status, FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY)) {
            return loc.getString("CTL_FileInfoStatus_ModifiedLocally_Short"); // NOI18N
        } else if (FileInformation.match(status, FileInformation.STATUS_VERSIONED_CONFLICT)) {
            return loc.getString("CTL_FileInfoStatus_Conflict_Short"); // NOI18N
        } else {
            return "";  // NOI18N                  
        }
    }

    private static boolean match(int status, int mask) {
        return (status & mask) != 0;
    }

    public String toString() {
        return "Text: " + status + " " + getStatusText(status); // NOI18N
    }

    boolean addToCounter (int counterType, int value, boolean directChild) {
        assert counterType >=0 && counterType <= COUNTER_MAX_INDEX;
        boolean importantCounterChanged = false;
        if (counterType >=0 && counterType <= COUNTER_MAX_INDEX) {
            synchronized (counters) {
                int mostImportantCounter = getMostImportantCounter(false, Collections.EMPTY_SET);
                counters[counterType] += value;
                if (directChild) {
                    counters[counterType + 1 + COUNTER_MAX_INDEX] += value;
                }
                assert counters[counterType] >= 0;
                importantCounterChanged = mostImportantCounter != getMostImportantCounter(false, Collections.EMPTY_SET);
            }
        }
        return importantCounterChanged;
    }

    int getCounter (int counterType, boolean onlyDirectChildren) {
        assert counterType >=0 && counterType <= COUNTER_MAX_INDEX;
        int counterValue = -1;
        if (counterType >=0 && counterType <= COUNTER_MAX_INDEX) {
            synchronized (counters) {
                counterValue = counters[onlyDirectChildren ? COUNTER_MAX_INDEX + 1 + counterType : counterType];
            }
        }
        return counterValue;
    }

    int getMoreImportantCounter (int counter, boolean onlyDirectChildren, Set<FileInformation> exclusions) {
        int mostImportantCounter;
        synchronized (counters) {
            mostImportantCounter = getMostImportantCounter(onlyDirectChildren, exclusions);
        }
        if (counter >= 0 && counter <= COUNTER_MAX_INDEX && counter > mostImportantCounter) {
            mostImportantCounter = counter;
        }
        return mostImportantCounter;
    }

    private void initializeCounters () {
        if (!isDirectory) {
            synchronized (counters) {
                if ((status & STATUS_VERSIONED_CONFLICT) != 0) {
                    counters[COUNTER_CONFLICTED_FILES] = counters[COUNTER_FLAT_CONFLICTED_FILES] = 1;
                } else if ((status & (STATUS_VERSIONED_REMOVEDLOCALLY | STATUS_VERSIONED_DELETEDLOCALLY)) != 0) {
                    counters[COUNTER_DELETED_FILES] = counters[COUNTER_FLAT_DELETED_FILES] = 1;
                } else if ((status & (STATUS_VERSIONED_ADDEDLOCALLY | STATUS_NOTVERSIONED_NEWLOCALLY)) != 0) {
                    counters[COUNTER_NEW_FILES] = counters[COUNTER_FLAT_NEW_FILES] = 1;
                } else if ((status & STATUS_VERSIONED_MODIFIEDLOCALLY) != 0) {
                    counters[COUNTER_MODIFIED_FILES] = counters[COUNTER_FLAT_MODIFIED_FILES] = 1;
                }
            }
        }
    }

    private int getMostImportantCounter(boolean onlyDirectChildren, Set<FileInformation> exclusions) {
        int mostImportantCounter = -1;
        int counterBase = onlyDirectChildren ? COUNTER_MAX_INDEX + 1 : 0;
        int exclusionsConflicted = 0;
        int exclusionsModified = 0;
        for (FileInformation info : exclusions) {
            if ((info.getStatus() & STATUS_VERSIONED_CONFLICT) != 0) {
                ++exclusionsConflicted;
            }
            if ((info.getStatus() & (STATUS_VERSIONED_REMOVEDLOCALLY
                    | STATUS_VERSIONED_DELETEDLOCALLY
                    | STATUS_VERSIONED_ADDEDLOCALLY
                    | STATUS_NOTVERSIONED_NEWLOCALLY
                    | STATUS_VERSIONED_MODIFIEDLOCALLY)) != 0) {
                ++exclusionsModified;
            }
        }
        if (counters[counterBase + COUNTER_CONFLICTED_FILES] - exclusionsConflicted > 0) {
            mostImportantCounter = COUNTER_CONFLICTED_FILES;
        } else if (counters[counterBase + COUNTER_DELETED_FILES]
                + counters[counterBase + COUNTER_NEW_FILES]
                + counters[counterBase + COUNTER_MODIFIED_FILES] - exclusionsModified > 0) {
            mostImportantCounter = COUNTER_MODIFIED_FILES;
        }
        return mostImportantCounter;
    }
}
