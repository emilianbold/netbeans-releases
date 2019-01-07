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
package org.netbeans.modules.mercurial.remote;

import java.awt.Color;
import java.awt.EventQueue;
import java.io.Serializable;
import java.util.HashSet;
import java.util.ResourceBundle;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.remotefs.versioning.util.common.VCSFileInformation;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.util.NbBundle;

/**
 * Immutable class encapsulating status of a file.
 *
 * 
 */
public class FileInformation extends VCSFileInformation implements Serializable {

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
    
    private static final int STATUS_VERSIONED_REMOVED =
            FileInformation.STATUS_VERSIONED_DELETEDLOCALLY |
            FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY;
    
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

    private final HashSet<VCSFileProxy> modifiedChildren = new HashSet<>();
    private final HashSet<VCSFileProxy> conflictedChildren = new HashSet<>();

    private boolean seenInUI;

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
        this.seenInUI = !isDirectory; // files are always marked as seen
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
    public FileStatus getStatus(VCSFileProxy file) {
        return entry;
    }

    /**
     * Returns localized text representation of status.
     * 
     * @return status name, for multistatuses prefers local
     * status name.
     */ 
    @Override
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
                if (!EventQueue.isDispatchThread() && !entry.getOriginalFile().exists()
                        || (Mercurial.getInstance().getFileStatusCache().getStatus(entry.getOriginalFile()).getStatus()
                        & FileInformation.STATUS_VERSIONED_REMOVED) != 0) {
                    if (entry.getOriginalFile().getParentFile().getPath()
                            .equals(entry.getFile().getParentFile().getPath())) {
                        return loc.getString("CTL_FileInfoStatus_AddedLocallyRenamed"); // NOI18N
                    } else {
                        return loc.getString("CTL_FileInfoStatus_AddedLocallyMoved"); // NOI18N
                    }
                } else {
                    return loc.getString("CTL_FileInfoStatus_AddedLocallyCopied"); // NOI18N
                }
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
                if (!EventQueue.isDispatchThread() && !entry.getOriginalFile().exists()
                        || (Mercurial.getInstance().getFileStatusCache().getStatus(entry.getOriginalFile()).getStatus()
                        & FileInformation.STATUS_VERSIONED_REMOVED) != 0) {
                    if (entry.getOriginalFile().getParentFile().getPath()
                            .equals(entry.getFile().getParentFile().getPath())) {
                        return loc.getString("CTL_FileInfoStatus_AddedLocallyRenamed_Short"); //NOI18N
                    } else {
                        return loc.getString("CTL_FileInfoStatus_AddedLocallyMoved_Short"); //NOI18N
                    }
                } else {
                    return loc.getString("CTL_FileInfoStatus_AddedLocallyCopied_Short"); //NOI18N
                }
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

    @Override
    public String toString() {
        return "Text: " + status + " " + getStatusText(status); // NOI18N
    }

    HashSet<VCSFileProxy> getModifiedChildren (boolean onlyConflicted) {
        HashSet<VCSFileProxy> children;
        synchronized (modifiedChildren) {
            if (onlyConflicted) {
                children = new HashSet<>(conflictedChildren);
            } else {
                children = new HashSet<>(modifiedChildren);
            }
        }
        return children;
    }

    boolean setModifiedChild (VCSFileProxy child, FileInformation newInfo) {
        if ((status & STATUS_NOTVERSIONED_NOTMANAGED) != 0) {
            return false;
        }
        synchronized (modifiedChildren) {
            modifiedChildren.remove(child);
            conflictedChildren.remove(child);
            boolean followOnParent = modifiedChildren.isEmpty(); // information may be only removed, notify parent if this folder has no modified children
            if (newInfo.getStatus() != STATUS_UNKNOWN && (newInfo.getStatus() & STATUS_VERSIONED_UPTODATE) == 0) {
                boolean testBeforeAdd = false;
                assert testBeforeAdd = true;
                if (testBeforeAdd && modifiedChildren.size() > 0) {
                    VCSFileProxy alreadyAdded = modifiedChildren.iterator().next();
                    if (!child.getParentFile().equals(alreadyAdded.getParentFile())) {
                        throw new IllegalStateException("Adding " + child.getPath() + ", already added " //NOI18N
                                + alreadyAdded.getPath() + " under " //NOI18N
                                + alreadyAdded.getParentFile().getPath());
                    }
                }
                modifiedChildren.add(child);
                if ((newInfo.getStatus() & STATUS_VERSIONED_CONFLICT) != 0) {
                    conflictedChildren.add(child);
                }
                followOnParent = true; // information was added, notify the parent
            }
            return followOnParent;
        }
    }

    /**
     * Returns value of the flag indicating if the file associated with this FI is/was visible in the UI
     * @return
     */
    boolean wasSeenInUi () {
        return seenInUI;
    }

    /**
     * Sets value of the flag indicating if the file associated with this FI is/was visible in the UI
     * @param seenInUI
     */
    void setSeenInUI (boolean seenInUI) {
        this.seenInUI = seenInUI;
    }

    @Override
    public int getComparableStatus () {
        return HgUtils.getComparableStatus(status);
    }

    @Override
    public String annotateNameHtml (String name) {
        return Mercurial.getInstance().getMercurialAnnotator().annotateNameHtml(name, this, null);
    }

    @Override
    public Color getAnnotatedColor () {
        return Mercurial.getInstance().getMercurialAnnotator().getAnnotatedColor(this);
    }
}
