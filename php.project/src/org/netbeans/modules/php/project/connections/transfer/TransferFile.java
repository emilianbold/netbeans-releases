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
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.connections.spi.RemoteFile;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * File to be transfered to/from remote server.
 *
 * It is able to resolve remote and local paths against some base directory.
 * This is useful e.g. while uploading:
 * <pre>
 * C:\home\test\Project1\web\test.php => /pub/Project1/web/test.php,
 * </pre>
 * then for base path "C:\home\test\Project1" the remote path would be "web/test.php" and
 * local path "web\test.php" ("web/test.php" for *nix).
 * <p>
 * Path separator for local path is OS-dependent, for remote path it is always
 * {@value #REMOTE_PATH_SEPARATOR}, for all platforms.
 */
public abstract class TransferFile {

    /**
     * Remote path separator ({@value #REMOTE_PATH_SEPARATOR}).
     */
    public static final String REMOTE_PATH_SEPARATOR = "/"; // NOI18N
    /**
     * Remote project root path ({@value #REMOTE_PROJECT_ROOT}).
     */
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
     * @param parent parent remote file, can be {@code null}
     * @param fo local file object to be used
     * @param baseDirectory base directory local and remote paths are resolved to
     * @return new remote file for the given parameters
     */
    public static TransferFile fromFileObject(TransferFile parent, FileObject fo, String baseDirectory) {
        assert fo != null;

        return fromFile(parent, FileUtil.toFile(fo), baseDirectory, fo.isFolder());
    }

    /**
     * Implementation for {@link File} (can be file or directory).
     * <p>
     * Suitable for most cases. Non-existing files are considered to be {@link File#isFile() files}.
     * <p>
     * The given file will be normalized.
     * @param parent parent remote file, can be {@code null}
     * @param file local file to be used
     * @param baseDirectory base directory local and remote paths are resolved to
     * @return new remote file for the given parameters
     * @see #fromDirectory(TransferFile, File, String)
     */
    public static TransferFile fromFile(TransferFile parent, File file, String baseDirectory) {
        return fromFile(parent, file, baseDirectory, false);
    }

    /**
     * Implementation for {@link File directory} (it means
     * that the provided file <b>cannot</b> be a regular file).
     * <p>
     * Suitable for special cases when the given {@code file} should be considered
     * to be a directory (applies e.g. for non-existing files).
     * <p>
     * The given file will be normalized.
     * @param parent parent remote file, can be {@code null}
     * @param file local file to be used
     * @param baseDirectory base directory local and remote paths are resolved to
     * @return new remote file for the given parameters
     * @see #fromFile(TransferFile, File, String)
     */
    public static TransferFile fromDirectory(TransferFile parent, File file, String baseDirectory) {
        return fromFile(parent, file, baseDirectory, true);
    }

    /**
     * Implementation for {@link RemoteFile}.
     * @param parent parent remote file, can be {@code null}
     * @param remoteFile remote file to be used
     * @param baseDirectory base directory local and remote paths are resolved to
     * @return new remote file for the given parameters
     */
    public static TransferFile fromRemoteFile(TransferFile parent, RemoteFile remoteFile, String baseDirectory) {
        TransferFile transferFile = new RemoteTransferFile(remoteFile, parent, baseDirectory);
        if (parent != null) {
            parent.addChild(transferFile);
        }
        return transferFile;
    }

    private static TransferFile fromFile(TransferFile parent, File file, String baseDirectory, boolean forceDirectory) {
        TransferFile transferFile = new LocalTransferFile(FileUtil.normalizeFile(file), parent, baseDirectory, forceDirectory);
        if (parent != null) {
            parent.addChild(transferFile);
        }
        return transferFile;
    }

    /**
     * Return {@code true} if the transfer file has {@link #getChildren() children}.
     * @return {@code true} if the transfer file has {@link #getChildren() children}
     * @see #getChildren()
     */
    public final boolean hasChildren() {
        return !children.isEmpty();
    }

    /**
     * Get children files.
     * @return children files
     */
    public final List<TransferFile> getChildren() {
        return new ArrayList<TransferFile>(children);
    }

    /**
     * Check whether the remote file has a parent file.
     * <p>
     * THis can be {@code false} for {@link #isRoot() root},
     * for {@link #isProjectRoot() project root} or simply if
     * the parent file was not specified when this remote file
     * was created.
     * @return {@code true} if the file has parent remote file
     * @see #getParent()
     * @see #isRoot()
     * @see #isProjectRoot()
     */
    public final boolean hasParent() {
        return parent != null;
    }

    /**
     * Get parent remote file or {@code null} if there's none.
     * @return parent remote file or {@code null} if there's none
     * @see #hasParent()
     */
    public final TransferFile getParent() {
        if (isProjectRoot()) {
            throw new IllegalStateException("Cannot get parent on project root.");
        }
        return parent;
    }

    /**
     * Get base directory local and remote paths are resolved to.
     * @return base directory local and remote paths are resolved to
     */
    public final String getBaseDirectory() {
        return baseDirectory;
    }

    /**
     * Return {@code true} if the remote file does not parent remote file.
     * @return {@code true} if the remote file does not parent remote file
     */
    public boolean isRoot() {
        if (isProjectRoot()) {
            return true;
        }
        return !hasParent();
    }

    /**
     * Return {@code true} if the remote file is the project root.
     * <p>
     * It means that the remote (and local too!) path equals {@link #REMOTE_PROJECT_ROOT}.
     * @return {@code true} if the remote file is the project root
     */
    public boolean isProjectRoot() {
        return REMOTE_PROJECT_ROOT == getRemotePath();
    }

    /**
     * Get simple name of the remote file.
     * @return simple name of the remote file
     */
    public abstract String getName();

    /**
     * Get remote (platform independent) path or {@value #REMOTE_PROJECT_ROOT}
     * if the file is {@link #isProjectRoot() project root}.
     * @see #isProjectRoot()
     */
    public abstract String getRemotePath();

    /**
     * Get remote (platform independent) path of the parent file
     * or throws an exception if the file is {@link #isProjectRoot() project root}.
     * <p>
     * This method can be used to get parent directory even if {@link #getParent() parent}
     * is not set.
     * @return remote path of the parent file
     * @see #getRemotePath()
     */
    public final String getParentRemotePath() {
        if (getParent() != null) {
            return getParent().getRemotePath();
        }
        List<String> fragments = new ArrayList<String>(StringUtils.explode(getRemotePath(), REMOTE_PATH_SEPARATOR));
        fragments.remove(fragments.size() - 1);
        if (fragments.isEmpty()) {
            return REMOTE_PROJECT_ROOT;
        }
        return StringUtils.implode(fragments, REMOTE_PATH_SEPARATOR);
    }

    /**
     * Get local (platform dependent) path or {@value #REMOTE_PROJECT_ROOT}
     * if the file is {@link #isProjectRoot() project root}.
     * @see #isProjectRoot()
     */
    public final String getLocalPath() {
        String remotePath = getRemotePath();
        if (File.separator.equals(REMOTE_PATH_SEPARATOR)) {
            return remotePath;
        }
        return remotePath.replace(REMOTE_PATH_SEPARATOR, File.separator);
    }

    /**
     * Resolve local file for the given directory.
     * @param directory directory (does not need to exist) to be used as a parent
     * @return resolved local file
     */
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
     * Get the size of the file in <b>bytes</b>. For directory it is always 0 (zero).
     * @return get the size of the file in bytes
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

    /**
     * Return {@code true} if the file is directory.
     * @return {@code true} if the file is directory
     */
    public abstract boolean isDirectory();

    /**
     * Return {@code true} if the file is file.
     * @return {@code true} if the file is file
     */
    public abstract boolean isFile();

    /**
     * Return {@code true} if the file is symbolic link.
     * <p>
     * <i>Warning:</i> does not resolve symlinks for local files
     * due to {@link org.netbeans.modules.php.api.util.FileUtils#isDirectoryLink(File) performance}
     * reasons.
     * @return {@code true} if the file is symbolic link
     */
    public abstract boolean isLink();

    /**
     * Get timestamp <b>in seconds</b> of the file last modification or <code>-1</code> if not known.
     * @return timestamp <b>in seconds</b> of the file last modification or <code>-1</code> if not known
     * @see #touch()
     */
    public final long getTimestamp() {
        if (timestamp == null) {
            timestamp = getTimestampImpl();
        }
        return timestamp;
    }

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
                + ", isFile: " + isFile() // NOI18N
                + ", isDirectory: " + isDirectory() // NOI18N
                + ", isLink: " + isLink() // NOI18N
                + "]"; // NOI18N
    }

    private void addChild(TransferFile child) {
        children.add(child);
    }

}
