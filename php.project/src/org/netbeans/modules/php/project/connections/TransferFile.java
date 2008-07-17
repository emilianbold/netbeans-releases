/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.connections;

import java.io.File;
import org.apache.commons.net.ftp.FTPFile;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * File to be transfered to/from remote server. It is able to resolve different relative paths
 * against some base directory. This is useful e.g. while uploading:
 * <pre>
 * /home/test/Project1/web/test.php => /pub/Project1/web/test.php,
 * </pre>
 * then for base path "/home/test/Project1" the relative path would be "web/test.php".
 * @author Tomas Mysik
 */
public final class TransferFile {
    public static final String CWD = "."; // NOI18N
    private static final int FILE_SEPARATOR_LENGTH = File.separator.length();

    private final String name;
    private final String absolutePath;
    private final String relativePath;
    private final String parentPath;
    private final String parentRelativePath;
    private final boolean directory;
    private final boolean file;

    private TransferFile(String name, String absolutePath, String relativePath, String parentPath, String parentRelativePath, boolean directory, boolean file) {
        this.name = name;
        this.absolutePath = absolutePath;
        this.relativePath = relativePath;
        this.parentPath = parentPath;
        this.parentRelativePath = parentRelativePath;
        this.directory = directory;
        this.file = file;
    }

    /**
     * Implementation for {@link FileObject}.
     */
    public static TransferFile fromFile(File file, String baseDirectory) {
        assert file != null;
        assert !baseDirectory.endsWith(File.separator) : "Base directory cannot end with File.separator";
        assert (file.getAbsolutePath() + File.separator).startsWith(baseDirectory + File.separator) : "File must be underneath base directory";

        String name = file.getName();
        String absolutePath = file.getPath();
        String relativePath = getRelativePath(absolutePath, baseDirectory);
        String parentPath = file.getParent();
        String parentRelativePath = getParentRelativePath(parentPath, baseDirectory);
        boolean directory = file.isDirectory();
        boolean f = file.isFile();

        return new TransferFile(name, absolutePath, relativePath, parentPath, parentRelativePath, directory, f);
    }

    /**
     * Implementation for {@link FileObject}.
     */
    public static TransferFile fromFileObject(FileObject fo, String baseDirectory) {
        assert fo != null;

        return fromFile(FileUtil.toFile(fo), baseDirectory);
    }

    /**
     * Implementation for {@link FTPFile}.
     */
    public static TransferFile fromFtpFile(FTPFile ftpFile, String baseDirectory, String parentDirectory) {
        assert ftpFile != null;
        assert !baseDirectory.endsWith("/") && !parentDirectory.endsWith("/") : "Both base and parent directory cannot end with '/'";
        assert parentDirectory.startsWith(baseDirectory) : "Parent directory must be underneath parent directory";

        String name = ftpFile.getName();
        String absolutePath = parentDirectory + "/" + name; // NOI18N
        String relativePath = getRelativePath(absolutePath, baseDirectory);
        String parentRelativePath = getParentRelativePath(parentDirectory, baseDirectory);
        boolean directory = ftpFile.isDirectory();
        boolean file = ftpFile.isFile();

        return new TransferFile(name, absolutePath, relativePath, parentDirectory, parentRelativePath, directory, file);
    }

    /**
     * Dummy implementation for file path.
     */
    public static TransferFile fromPath(String path) {
        assert path != null;

        return new TransferFile(path, path, path, path, path, false, false);
    }

    private static String getRelativePath(String absolutePath, String baseDirectory) {
        if (absolutePath.equals(baseDirectory)) {
            return CWD;
        }
        // +1 => remove '/' from the beginning of the relative path
        return absolutePath.substring(baseDirectory.length() + FILE_SEPARATOR_LENGTH);
    }

    private static String getParentRelativePath(String parentPath, String baseDirectory) {
        if (parentPath.length() < baseDirectory.length()) {
            // out of scope
            return null;
        } else if (parentPath.equals(baseDirectory)) {
            return CWD;
        }
        // +1 => remove '/' from the beginning of the relative path
        return parentPath.substring(baseDirectory.length() + FILE_SEPARATOR_LENGTH);
    }

    public String getName() {
        return name;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    /**
     * Get relative path or {@value #CWD} if absolute path equals relative path.
     * @see #CWD
     */
    public String getRelativePath() {
        return relativePath;
    }

    /**
     * Can be <code>null</code>.
     */
    String getParentPath() {
        return parentPath;
    }

    /**
     * Get relative parent path, {@value #CWD} if parent path equals base directory
     * and <code>null</code> if parent path is not underneath base directory
     * (normally, it would start with "..").
     * @see #CWD
     */
    String getParentRelativePath() {
        return parentRelativePath;
    }

    public boolean isDirectory() {
        return directory;
    }

    public boolean isFile() {
        return file;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TransferFile other = (TransferFile) obj;
        return absolutePath.equals(other.absolutePath);
    }

    @Override
    public int hashCode() {
        return absolutePath.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append(getClass().getName());
        sb.append(" [ name: "); // NOI18N
        sb.append(name);
        sb.append(", absolutePath: "); // NOI18N
        sb.append(absolutePath);
        sb.append(", relativePath: "); // NOI18N
        sb.append(relativePath);
        sb.append(", parentPath: "); // NOI18N
        sb.append(parentPath);
        sb.append(", parentRelativePath: "); // NOI18N
        sb.append(parentRelativePath);
        sb.append(", directory: "); // NOI18N
        sb.append(directory);
        sb.append(", file: "); // NOI18N
        sb.append(file);
        sb.append(" ]"); // NOI18N
        return sb.toString();
    }
}
