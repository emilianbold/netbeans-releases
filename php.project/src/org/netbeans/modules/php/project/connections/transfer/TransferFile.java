/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.connections.transfer;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.php.project.connections.spi.RemoteFile;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * File to be transfered to/from remote server. It is able to resolve different relative paths
 * against some base directory. This is useful e.g. while uploading:
 * <pre>
 * /home/test/Project1/web/test.php => /pub/Project1/web/test.php,
 * </pre>
 * then for base path "/home/test/Project1" the relative path would be "web/test.php".
 * <p>
 * Instances can be neither directories nor files; this applies for {@link #fromPath(java.lang.String)}.
 * <p>
 * Path separator is always {@value #REMOTE_PATH_SEPARATOR}, for all platforms.
 */
public abstract class TransferFile {

    public static final String REMOTE_PATH_SEPARATOR = "/"; // NOI18N
    public static final String REMOTE_PROJECT_ROOT = "."; // NOI18N

    protected final String baseDirectory;
    protected final TransferFile parent;
    private final List<TransferFile> children = new LinkedList<TransferFile>();

    private Long timestamp = null; // in seconds, default -1
    private Long size = null; // 0 for directory

    TransferFile(TransferFile parent, String baseDirectory) {
        this.parent = parent;
        this.baseDirectory = baseDirectory;
    }

    /**
     * Implementation for {@link FileObject}.
     */
    public static TransferFile fromFileObject(TransferFile parent, FileObject fo, String baseDirectory) {
        assert fo != null;

        return fromFile(parent, FileUtil.toFile(fo), baseDirectory);
    }

    /**
     * Implementation for {@link File}.
     * <p>
     * The given file will be normalized.
     */
    public static TransferFile fromFile(TransferFile parent, File file, String baseDirectory) {
        TransferFile transferFile = new LocalTransferFile(FileUtil.normalizeFile(file), parent, baseDirectory);
        if (parent != null) {
            parent.addChild(transferFile);
        }
        return transferFile;
    }

    /**
     * Implementation for {@link RemoteFile}.
     */
    public static TransferFile fromRemoteFile(TransferFile parent, RemoteFile remoteFile, String baseDirectory) {
        TransferFile transferFile = new RemoteTransferFile(remoteFile, parent, baseDirectory);
        if (parent != null) {
            parent.addChild(transferFile);
        }
        return transferFile;
    }

    public final boolean hasChildren() {
        return !children.isEmpty();
    }

    public final boolean hasParent() {
        return parent != null;
    }

    public final List<TransferFile> getChildren() {
        return new ArrayList<TransferFile>(children);
    }

    public final TransferFile getParent() {
        if (isProjectRoot()) {
            throw new IllegalStateException("Cannot get parent on project root.");
        }
        return parent;
    }

    public final String getBaseDirectory() {
        return baseDirectory;
    }

    /**
     * @return
     */
    public boolean isRoot() {
        if (isProjectRoot()) {
            return true;
        }
        return !hasParent();
    }

    /**
     * rel path equals REMOTE_PROJECT_ROOT
     * @return
     */
    public boolean isProjectRoot() {
        return REMOTE_PROJECT_ROOT.equals(getRemotePath());
    }

    public abstract String getName();

    /**
     * Get platform independent relative path or {@value #REMOTE_PROJECT_ROOT} if absolute path equals relative path.
     * @see #REMOTE_PROJECT_ROOT
     */
    public abstract String getRemotePath();

    /**
     * Get relative path or {@value #REMOTE_PROJECT_ROOT} if absolute path equals relative path.
     * @param platformDependent <code>true</code> for platform dependent relative path
     * @see #REMOTE_PROJECT_ROOT
     */
    public final String getLocalPath() {
        String remotePath = getRemotePath();
        if (File.separator.equals(REMOTE_PATH_SEPARATOR)) {
            return remotePath;
        }
        return remotePath.replace(REMOTE_PATH_SEPARATOR, File.separator);
    }

    public File resolveLocalFile(File directory) {
        if (directory == null) {
            throw new NullPointerException();
        }
        if (directory.exists() && !directory.isDirectory()) {
            throw new IllegalArgumentException("Directory must be provided (both existing and non-existing allowed)");
        }
        if (isProjectRoot()) {
            return directory;
        }
        return new File(directory, getLocalPath());
    }

    /**
     * Get the size of the file in bytes. For directory it is always 0 (zero).
     * @return get the size of the file in bytes.
     */
    public final long getSize() {
        if (size == null) {
            size = getSizeImpl();
            if (size < 0L) {
                throw new IllegalArgumentException("Size cannot be smaller than 0");
            }
            if (isDirectory() && size != 0L) {
                throw new IllegalArgumentException("Size of a directory has to be 0 bytes");
            }
        }
        return size;
    }

    protected abstract long getSizeImpl();

    public abstract boolean isDirectory();

    public abstract boolean isFile();

    public abstract boolean isLink();

    /**
     * @return timestamp <b>in seconds</b> of the file last modification or <code>-1</code> if not known.
     * @see #touch()
     */
    public final long getTimestamp() {
        if (timestamp == null) {
            timestamp = getTimestampImpl();
        }
        return timestamp;
    }

    /**
     * @return timestamp <b>in seconds</b> of the file last modification or <code>-1</code> if not known.
     * @see #touch()
     */
    protected abstract long getTimestampImpl();

    /**
     * Set the file modification time to the current time.
     */
    public final void touch() {
        timestamp = new Date().getTime();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TransferFile)) {
            return false;
        }
        final TransferFile other = (TransferFile) obj;
        return getRemotePath().equals(other.getRemotePath());
    }

    @Override
    public int hashCode() {
        return getRemotePath().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "[name: " + getName() // NOI18N
                + ", remotePath: " + getRemotePath() // NOI18N
                + ", baseDirectory: " + getBaseDirectory() // NOI18N
                + ", hasParent: " + hasParent() // NOI18N
                + "]"; // NOI18N
    }

    private void addChild(TransferFile child) {
        children.add(child);
    }

}
