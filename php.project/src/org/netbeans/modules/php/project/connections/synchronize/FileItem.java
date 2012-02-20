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
package org.netbeans.modules.php.project.connections.synchronize;

import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.php.project.connections.transfer.TransferFile;

/**
 * File item holding remote and local files.
 */
public final class FileItem {

    public static enum Operation {

        NOOP,
        DOWNLOAD,
        DOWNLOAD_REVIEW,
        UPLOAD,
        UPLOAD_REVIEW,
        DELETE_LOCALLY,
        DELETE_REMOTELY,
        FILE_DIR_COLLISION;


        public static List<Operation> possibleOperations() {
            return Arrays.asList(
                    NOOP,
                    DOWNLOAD,
                    UPLOAD,
                    DELETE_LOCALLY,
                    DELETE_REMOTELY);
        }

    }

    private final TransferFile remoteTransferFile;
    private final TransferFile localTransferFile;

    private volatile Operation operation;


    public FileItem(TransferFile remoteTransferFile, TransferFile localTransferFile, Long lastTimestamp) {
        assert remoteTransferFile != null || localTransferFile != null;
        this.remoteTransferFile = remoteTransferFile;
        this.localTransferFile = localTransferFile;
        operation = calculateOperation(lastTimestamp);
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
        return operation;
    }

    public void setOperation(Operation operation) {
        assert operation != null;
        this.operation = operation;
    }

    private Operation calculateOperation(Long lastTimestamp) {
        if (lastTimestamp == null) {
            // perhaps running for the first time
            lastTimestamp = Long.MIN_VALUE;
        }
        if (localTransferFile == null
                || remoteTransferFile == null) {
            if (localTransferFile == null) {
                return Operation.DOWNLOAD;
            }
            return Operation.UPLOAD;
        }
        if (localTransferFile.isFile() && !remoteTransferFile.isFile()) {
            return Operation.FILE_DIR_COLLISION;
        }
        if (localTransferFile.isDirectory() && !remoteTransferFile.isDirectory()) {
            return Operation.FILE_DIR_COLLISION;
        }
        if (localTransferFile.isDirectory() && remoteTransferFile.isDirectory()) {
            return Operation.NOOP;
        }
        long localTimestamp = localTransferFile.getTimestamp();
        long remoteTimestamp = remoteTransferFile.getTimestamp();
        long localSize = localTransferFile.getSize();
        long remoteSize = remoteTransferFile.getSize();
        if (localTimestamp == remoteTimestamp) {
            // in fact, cannot happen
            return Operation.NOOP;
        }
        if (localTimestamp <= lastTimestamp) {
            // no change in local file
            if (remoteTimestamp <= lastTimestamp
                    && localSize == remoteSize) {
                return Operation.NOOP;
            }
            if (localTimestamp > remoteTimestamp) {
                return Operation.UPLOAD;
            }
            return Operation.DOWNLOAD;
        }
        // change in local file
        if (remoteTimestamp <= lastTimestamp) {
            return Operation.UPLOAD;
        }
        if (localTimestamp > remoteTimestamp) {
            return Operation.UPLOAD_REVIEW;
        }
        return Operation.DOWNLOAD_REVIEW;
    }

    @Override
    public String toString() {
        return "FileItem{" // NOI18N
                + "path: " + (localTransferFile != null ? localTransferFile.getRemotePath() : remoteTransferFile.getRemotePath()) // NOI18N
                + ", localFile: " + (localTransferFile != null) // NOI18N
                + ", remoteFile: " + (remoteTransferFile != null) // NOI18N
                + ", operation: " + operation // NOI18N
                + "}"; // NOI18N
    }

}
