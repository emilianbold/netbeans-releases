/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.connections.sync;

import java.util.LinkedList;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.php.project.connections.transfer.TransferFile;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Sync item holding remote and local files and providing
 * operations with them.
 */
public final class SyncItem {

    // XXX
    @StaticResource
    static final String NOOP_ICON_PATH = "org/netbeans/modules/php/project/ui/resources/info_icon.png"; // NOI18N
    @StaticResource
    static final String DOWNLOAD_ICON_PATH = "org/netbeans/modules/php/project/ui/resources/info_icon.png"; // NOI18N
    @StaticResource
    static final String DOWNLOAD_REVIEW_ICON_PATH = "org/netbeans/modules/php/project/ui/resources/info_icon.png"; // NOI18N
    @StaticResource
    static final String UPLOAD_ICON_PATH = "org/netbeans/modules/php/project/ui/resources/info_icon.png"; // NOI18N
    @StaticResource
    static final String UPLOAD_REVIEW_ICON_PATH = "org/netbeans/modules/php/project/ui/resources/info_icon.png"; // NOI18N
    @StaticResource
    static final String DELETE_ICON_PATH = "org/netbeans/modules/php/project/ui/resources/info_icon.png"; // NOI18N
    @StaticResource
    static final String FILE_DIR_COLLISION_ICON_PATH = "org/netbeans/modules/php/project/ui/resources/info_icon.png"; // NOI18N
    @StaticResource
    static final String FILE_CONFLICT_ICON_PATH = "org/netbeans/modules/php/project/ui/resources/info_icon.png"; // NOI18N

    @NbBundle.Messages({
        "Operation.noop.title=No operation",
        "Operation.download.title=Download",
        "Operation.downloadReview.title=Download with review",
        "Operation.upload.title=Upload",
        "Operation.uploadReview.title=Upload with review",
        "Operation.delete.title=Delete",
        "Operation.fileDirCollision.title=File vs. directory collision",
        "Operation.fileConflict.title=File conflict",
    })
    public static enum Operation {

        NOOP(Bundle.Operation_noop_title(), NOOP_ICON_PATH),
        DOWNLOAD(Bundle.Operation_download_title(), DOWNLOAD_ICON_PATH),
        DOWNLOAD_REVIEW(Bundle.Operation_downloadReview_title(), DOWNLOAD_REVIEW_ICON_PATH),
        UPLOAD(Bundle.Operation_upload_title(), UPLOAD_ICON_PATH),
        UPLOAD_REVIEW(Bundle.Operation_uploadReview_title(), UPLOAD_REVIEW_ICON_PATH),
        DELETE(Bundle.Operation_delete_title(), DELETE_ICON_PATH),
        FILE_DIR_COLLISION(Bundle.Operation_fileDirCollision_title(), FILE_DIR_COLLISION_ICON_PATH),
        FILE_CONFLICT(Bundle.Operation_fileConflict_title(), FILE_CONFLICT_ICON_PATH);


        private final String title;
        private final String iconPath;


        private Operation(String title, String iconPath) {
            this.title = title;
            this.iconPath = iconPath;
        }

        public String getTitle() {
            return title;
        }

        public Icon getIcon() {
            return ImageUtilities.loadImageIcon(iconPath, false);
        }

    }


    private final SyncItems syncItems;
    private final TransferFile remoteTransferFile;
    private final TransferFile localTransferFile;
    private final Operation defaultOperation;

    private volatile Operation operation;
    private volatile boolean valid;
    private volatile String message = null;


    SyncItem(SyncItems syncItems, TransferFile remoteTransferFile, TransferFile localTransferFile, long lastTimestamp) {
        assert syncItems != null;
        assert remoteTransferFile != null || localTransferFile != null;
        this.syncItems = syncItems;
        this.remoteTransferFile = remoteTransferFile;
        this.localTransferFile = localTransferFile;
        defaultOperation = calculateDefaultOperation(lastTimestamp);
        validate();
    }

    public String getRemotePath() {
        if (remoteTransferFile == null) {
            return null;
        }
        return remoteTransferFile.getRemotePath();
    }

    public String getLocalPath() {
        if (localTransferFile == null) {
            return null;
        }
        return localTransferFile.getLocalPath();
    }

    public TransferFile getRemoteTransferFile() {
        return remoteTransferFile;
    }

    public TransferFile getLocalTransferFile() {
        return localTransferFile;
    }

    public Operation getOperation() {
        if (operation != null) {
            return operation;
        }
        return defaultOperation;
    }

    public void setOperation(Operation operation) {
        assert operation != null;
        this.operation = operation;
    }

    public void resetOperation() {
        operation = null;
    }

    @NbBundle.Messages({
        "SyncItem.error.fileConflict=File must be merged before synchronization.",
        "SyncItem.error.fileDirCollision=Cannot synchronize file with directory.",
        "SyncItem.error.childNotDeleted=Not all children marked for deleting.",
        "SyncItem.warn.downloadReview=File should be reviewed before download.",
        "SyncItem.warn.uploadReview=File should be reviewed before upload."
    })
    public void validate() {
        Operation op = getOperation();
        if (op == Operation.FILE_CONFLICT) {
            valid = false;
            message = Bundle.SyncItem_error_fileConflict();
            return;
        }
        if (op == Operation.FILE_DIR_COLLISION) {
            valid = false;
            message = Bundle.SyncItem_error_fileDirCollision();
            return;
        }
        if (op == Operation.DELETE) {
            if (localTransferFile != null
                    && !verifyChildrenOperation(localTransferFile, Operation.DELETE)) {
                valid = false;
                message = Bundle.SyncItem_error_childNotDeleted();
                return;
            }
            if (remoteTransferFile != null
                    && !verifyChildrenOperation(remoteTransferFile, Operation.DELETE)) {
                valid = false;
                message = Bundle.SyncItem_error_childNotDeleted();
                return;
            }
        }
        if (op == Operation.DOWNLOAD_REVIEW) {
            valid = true;
            message = Bundle.SyncItem_warn_downloadReview();
            return;
        }
        if (op == Operation.UPLOAD_REVIEW) {
            valid = true;
            message = Bundle.SyncItem_warn_uploadReview();
            return;
        }
        message = null;
        valid = true;
    }

    public boolean hasError() {
        // XXX optimize?
        validate();
        return !valid;
    }

    public boolean hasWarning() {
        // XXX optimize?
        validate();
        return valid && message != null;
    }

    public String getMessage() {
        // XXX optimize?
        validate();
        return message;
    }

    public boolean isDiffPossible() {
        if (remoteTransferFile != null
                && localTransferFile != null) {
            return remoteTransferFile.isFile() && localTransferFile.isFile();
        } else if (remoteTransferFile != null) {
            return remoteTransferFile.isFile();
        }
        return localTransferFile.isFile();
    }

    private Operation calculateDefaultOperation(long lastTimestamp) {
        if (localTransferFile.isFile() && !remoteTransferFile.isFile()) {
            return Operation.FILE_DIR_COLLISION;
        }
        if (localTransferFile.isDirectory() && !remoteTransferFile.isDirectory()) {
            return Operation.FILE_DIR_COLLISION;
        }
        if (localTransferFile.isDirectory() && remoteTransferFile.isDirectory()) {
            return Operation.NOOP;
        }
        if (lastTimestamp == -1) {
            // running for the first time
            return calculateFirstDefaultOperation();
        }
        return calculateNewDefaultOperation(lastTimestamp);
    }

    private Operation calculateFirstDefaultOperation() {
        if (localTransferFile == null
                || remoteTransferFile == null) {
            if (localTransferFile == null) {
                return Operation.DOWNLOAD;
            }
            return Operation.UPLOAD;
        }
        long localTimestamp = localTransferFile.getTimestamp();
        long remoteTimestamp = remoteTransferFile.getTimestamp();
        if (localTimestamp == remoteTimestamp) {
            // in fact, cannot happen
            return Operation.NOOP;
        }
        if (localTimestamp > remoteTimestamp) {
            return Operation.UPLOAD_REVIEW;
        }
        return Operation.DOWNLOAD_REVIEW;
    }

    private Operation calculateNewDefaultOperation(long lastTimestamp) {
        if (localTransferFile == null
                || remoteTransferFile == null) {
            if (localTransferFile == null) {
                return remoteTransferFile.getTimestamp() > lastTimestamp ? Operation.DOWNLOAD : Operation.DELETE;
            }
            return localTransferFile.getTimestamp() > lastTimestamp ? Operation.UPLOAD : Operation.DELETE;
        }
        long localTimestamp = localTransferFile.getTimestamp();
        long remoteTimestamp = remoteTransferFile.getTimestamp();
        if (localTimestamp == remoteTimestamp) {
            // in fact, cannot happen
            return Operation.NOOP;
        }
        if (localTimestamp <= lastTimestamp
                && remoteTimestamp <= lastTimestamp) {
            // already synchronized
            return Operation.NOOP;
        }
        if (localTimestamp > lastTimestamp
                && remoteTimestamp > lastTimestamp) {
            // both files are newer
            return Operation.FILE_CONFLICT;
        }
        // only one file is newer
        if (localTimestamp > remoteTimestamp) {
            return Operation.UPLOAD;
        }
        return Operation.DOWNLOAD;
    }

    private boolean verifyChildrenOperation(TransferFile transferFile, Operation operation) {
        LinkedList<TransferFile> children = new LinkedList<TransferFile>();
        children.addAll(transferFile.getChildren());
        while (!children.isEmpty()) {
            TransferFile child = children.pop();
            SyncItem syncItem = syncItems.getByRemotePath(child.getRemotePath());
            if (syncItem.getOperation() != operation) {
                return false;
            }
            children.addAll(child.getChildren());
        }
        return true;
    }

    @Override
    public String toString() {
        return "SyncItem{" // NOI18N
                + "path: " + (localTransferFile != null ? localTransferFile.getRemotePath() : remoteTransferFile.getRemotePath()) // NOI18N
                + ", localFile: " + (localTransferFile != null) // NOI18N
                + ", remoteFile: " + (remoteTransferFile != null) // NOI18N
                + ", operation: " + getOperation() // NOI18N
                + ", valid: " + valid // NOI18N
                + "}"; // NOI18N
    }

}
