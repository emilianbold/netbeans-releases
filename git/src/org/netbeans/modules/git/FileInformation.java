/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.git;

import java.io.File;
import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.modules.versioning.util.common.VCSFileInformation;

/**
 *
 * @author ondra
 */
public class FileInformation extends VCSFileInformation {
    private final EnumSet<Status> status;
    private boolean seenInUI;
    private final boolean directory;
    private final boolean renamed, copied;
    private final File oldFile;

    FileInformation (EnumSet<Status> status, boolean isDirectory) {
        this.status = status;
        this.directory = isDirectory;
        renamed = copied = false;
        oldFile = null;
    }

    FileInformation (GitStatus status) {
        directory = status.isFolder();
        seenInUI = true;
        renamed = status.isRenamed();
        copied = status.isCopied();
        oldFile = status.getOldPath();
        if (!status.isTracked()) {
            this.status = GitStatus.Status.STATUS_IGNORED.equals(status.getStatusIndexWC()) ? EnumSet.of(Status.STATUS_NOTVERSIONED_EXCLUDED)
                    : EnumSet.of(Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE);
        } else if (status.isConflict()) {
            this.status = EnumSet.of(Status.STATUS_VERSIONED_CONFLICT);
        } else {
            GitStatus.Status statusHeadIndex = status.getStatusHeadIndex();
            GitStatus.Status statusIndexWC = status.getStatusIndexWC();
            GitStatus.Status statusHeadWC = status.getStatusHeadWC();
            EnumSet<Status> s = EnumSet.noneOf(Status.class);
            if (GitStatus.Status.STATUS_ADDED.equals(statusHeadIndex)) {
                s.add(Status.STATUS_VERSIONED_ADDED_TO_INDEX);
            } else if (GitStatus.Status.STATUS_MODIFIED.equals(statusHeadIndex)) {
                s.add(Status.STATUS_VERSIONED_MODIFIED_HEAD_INDEX);
            } else if (GitStatus.Status.STATUS_REMOVED.equals(statusHeadIndex)) {
                s.add(Status.STATUS_VERSIONED_REMOVED_IN_INDEX);
            }
            if (GitStatus.Status.STATUS_ADDED.equals(statusIndexWC)) {
                s.add(Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE);
            } else if (GitStatus.Status.STATUS_MODIFIED.equals(statusIndexWC)) {
                s.add(Status.STATUS_VERSIONED_MODIFIED_INDEX_WORKING_TREE);
            } else if (GitStatus.Status.STATUS_REMOVED.equals(statusIndexWC)) {
                s.add(Status.STATUS_VERSIONED_REMOVED_IN_WORKING_TREE);
            }
            if (GitStatus.Status.STATUS_MODIFIED.equals(statusHeadWC)) {
                s.add(Status.STATUS_VERSIONED_MODIFIED_HEAD_WORKING_TREE);
            }
            if (s.isEmpty()) {
                s.add(Status.STATUS_VERSIONED_UPTODATE);
            }
            this.status = s;
        }
    }

    public boolean containsStatus (Set<Status> includeStatus) {
        EnumSet<Status> intersection = status.clone();
        intersection.retainAll(includeStatus);
        return !intersection.isEmpty();
    }

    public boolean containsStatus (Status includeStatus) {
        return containsStatus(EnumSet.of(includeStatus));
    }

    void setSeenInUI (boolean flag) {
        this.seenInUI = flag;
    }

    boolean seenInUI () {
        return seenInUI;
    }

    Set<Status> getStatus() {
        return status;
    }
    public boolean isDirectory () {
        return this.directory;
    }

    /**
     * TODO more complex logic needed.
     * Gets integer status that can be used in comparators. The more important the status is for the user,
     * the lower value it has. Conflict is 0, unknown status is 100.
     *
     * @return status constant suitable for 'by importance' comparators
     */
    public int getComparableStatus () {
        if (containsStatus(Status.STATUS_VERSIONED_CONFLICT)) {
            return 0;
        } else if (containsStatus(Status.STATUS_VERSIONED_CONFLICT)) {
            return 1;
        } else if (containsStatus(Status.STATUS_VERSIONED_REMOVED_IN_WORKING_TREE)) {
            return 10;
        } else if (containsStatus(Status.STATUS_VERSIONED_MODIFIED_HEAD_WORKING_TREE)) {
            return 11;
        } else if (containsStatus(Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE)) {
            return 12;
        } else if (containsStatus(Status.STATUS_VERSIONED_MODIFIED_HEAD_WORKING_TREE)) {
            return 13;
        } else if (containsStatus(Status.STATUS_VERSIONED_UPTODATE)) {
            return 50;
        } else if (containsStatus(Status.STATUS_NOTVERSIONED_EXCLUDED)) {
            return 100;
        } else if (containsStatus(Status.STATUS_NOTVERSIONED_NOTMANAGED)) {
            return 101;
        } else if (containsStatus(Status.STATUS_UNKNOWN)) {
            return 102;
        } else {
            // throw new IllegalArgumentException("Uncomparable status: " + getStatus()); //NOI18N
            Git.LOG.log(Level.WARNING, "Uncomparable status: {0}", getStatus());
            return 0;
        }
    }

    public String getShortStatusText() {
        assert status.size() > 0 && status.size() <= 3 : "unexpected statuses" + status;
        
        String sIndex = "";
        String sWorkingTree = "";
                
        if(containsStatus(Status.STATUS_VERSIONED_ADDED_TO_INDEX)) {
            sIndex = "A";
        } else if(containsStatus(Status.STATUS_VERSIONED_MODIFIED_HEAD_INDEX)) {
            sIndex = "M";
        } else if(containsStatus(Status.STATUS_VERSIONED_REMOVED_IN_INDEX)) {
            sIndex = "D";
        }
        
        if(containsStatus(Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE)) {
            sWorkingTree = "A";
        } else if(containsStatus(Status.STATUS_VERSIONED_MODIFIED_INDEX_WORKING_TREE)) {
            sWorkingTree = "M";
        } else if(containsStatus(Status.STATUS_VERSIONED_REMOVED_IN_WORKING_TREE)) {
            sWorkingTree = "D";
        }
        
        if(!sIndex.isEmpty() || !sWorkingTree.isEmpty()) {
            return new MessageFormat("{0}/{1}").format(new Object[] {sIndex, sWorkingTree}, new StringBuffer(), null).toString();
        } else {
            return "Nevim";            
        }        
    }

    public String getStatusText () {
        // TODO implement
        return "Tady ale fakt netusim";
    }

    public boolean isRenamed () {
        return renamed;
    }

    public boolean isCopied () {
        return copied;
    }

    public File getOldFile () {
        return oldFile;
    }

    public static enum Status {

        /**
         * There is nothing known about the file, it may not even exist.
         */
        STATUS_UNKNOWN,
        /**
         * The file is not managed by the module, i.e. the user does not wish it to be under control of this
         * versioning system module. All files except files under versioned roots have this status.
         */
        STATUS_NOTVERSIONED_NOTMANAGED,
        /**
         * The file exists locally but is NOT under version control because it should not be (i.e. is ignored or resides under an excluded folder).
         * The file itself IS under a versioned root.
         */
        STATUS_NOTVERSIONED_EXCLUDED,
        /**
         * The file has been added to index but does not exist in repository yet.
         */
        STATUS_VERSIONED_ADDED_TO_INDEX,
        /**
         * The file exists locally but is NOT under version control, mostly because it has not been added
         * to the repository yet.
         */
        STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE,
        /**
         * The file is under version control and is in sync with repository.
         */
        STATUS_VERSIONED_UPTODATE,
        /**
         * There's a modification between HEAD and index versions of the file
         */
        STATUS_VERSIONED_MODIFIED_HEAD_INDEX,
        /**
         * There's a modification between HEAD and working tree versions of the file
         */
        STATUS_VERSIONED_MODIFIED_HEAD_WORKING_TREE,
        /**
         * There's a modification between index and working tree versions of the file
         */
        STATUS_VERSIONED_MODIFIED_INDEX_WORKING_TREE,
        /**
         * Merging during update resulted in merge conflict. Conflicts in the local copy must be resolved before the file can be commited.
         */
        STATUS_VERSIONED_CONFLICT,
        /**
         * The file does NOT exist in index but does in HEAD, it has beed removed from index, waits for commit.
         */
        STATUS_VERSIONED_REMOVED_IN_INDEX,
        /**
         * The file has been removed in the working tree
         */
        STATUS_VERSIONED_REMOVED_IN_WORKING_TREE,
    }
    public static final EnumSet<Status> STATUS_ALL = EnumSet.allOf(Status.class);
    public static final EnumSet<Status> STATUS_MANAGED = EnumSet.complementOf(EnumSet.of(Status.STATUS_NOTVERSIONED_NOTMANAGED));
    public static final EnumSet<Status> STATUS_VERSIONED = EnumSet.complementOf(EnumSet.of(Status.STATUS_UNKNOWN,
            Status.STATUS_NOTVERSIONED_NOTMANAGED,
            Status.STATUS_NOTVERSIONED_EXCLUDED,
            Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE));
    public static final EnumSet<Status> STATUS_MODIFIED = EnumSet.of(Status.STATUS_VERSIONED_MODIFIED_HEAD_INDEX,
            Status.STATUS_VERSIONED_MODIFIED_HEAD_WORKING_TREE,
            Status.STATUS_VERSIONED_MODIFIED_INDEX_WORKING_TREE);
    public static final EnumSet<Status> STATUS_REMOVED = EnumSet.of(Status.STATUS_VERSIONED_REMOVED_IN_INDEX,
                                                                    Status.STATUS_VERSIONED_REMOVED_IN_WORKING_TREE);
    public static final EnumSet<Status> STATUS_LOCAL_CHANGES = EnumSet.of(Status.STATUS_VERSIONED_ADDED_TO_INDEX,
            Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE,
            Status.STATUS_VERSIONED_CONFLICT,
            Status.STATUS_VERSIONED_REMOVED_IN_INDEX,
            Status.STATUS_VERSIONED_REMOVED_IN_WORKING_TREE,
            Status.STATUS_VERSIONED_MODIFIED_HEAD_INDEX,
            Status.STATUS_VERSIONED_MODIFIED_HEAD_WORKING_TREE,
            Status.STATUS_VERSIONED_MODIFIED_INDEX_WORKING_TREE);
}
