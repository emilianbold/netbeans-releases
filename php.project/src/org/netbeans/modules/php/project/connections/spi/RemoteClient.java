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

package org.netbeans.modules.php.project.connections.spi;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import org.netbeans.modules.php.project.connections.RemoteException;
import org.netbeans.modules.php.project.connections.TransferFile;

/**
 * The particular implementation of the remote client (e.g. FTP, SFTP).
 * @author Tomas Mysik
 */
public interface RemoteClient {

    /**
     * Connect to a remote server.
     * @throws RemoteException if any unexpected error occurs.
     */
    void connect() throws RemoteException;

    /**
     * Disconnect from a remote server.
     * @throws RemoteException if any unexpected error occurs.
     */
    void disconnect() throws RemoteException;

    /**
     * Change working directory to the given file path.
     * @param pathname file path to change directory to.
     * @return <code>true</code> if the directory change was successful, <code>false</code> otherwise.
     * @throws RemoteException if any unexpected error occurs.
     */
    boolean changeWorkingDirectory(String pathname) throws RemoteException;

    /**
     * Get the file path of the current working directory.
     * @return the file path of the current working directory.
     * @throws RemoteException if any unexpected error occurs.
     */
    String printWorkingDirectory() throws RemoteException;

    /**
     * Create a directory for the given file path.
     * <p>
     * Note that a remote server can support only creating directory in the current working directory.
     * @param pathname file path of the directory to be created.
     * @return <code>true</code> if the directory creation was successful, <code>false</code> otherwise.
     * @throws RemoteException if any unexpected error occurs.
     */
    boolean makeDirectory(String pathname) throws RemoteException;

    /**
     * Get the last message from a remote server (from the last operation).
     * @return the last message from a remote server or <code>null</code> if the server does not support it.
     */
    String getReplyString();

    /**
     * Get the last negative message from a remote server (from the last operation).
     * @return the last negative message from a remote server or <code>null</code> if the server does not support it.
     */
    String getNegativeReplyString();

    /**
     * Return <code>true</code> if the remote client is connected, <code>false</code> otherwise.
     * @return <code>true</code> if the remote client is connected, <code>false</code> otherwise.
     */
    boolean isConnected();

    /**
     * Store a file on a remote server.
     * <p>
     * <b>Avoid closing of the given {@link InputStream input stream}!</b>
     * @param remote the name of the file to be stored on a server.
     * @param local {@link InputStream input stream} of the local file to be stored on a server.
     * @return <code>true</code> if the file was successfully uploaded, <code>false</code> otherwise.
     * @throws RemoteException if any unexpected error occurs.
     */
    boolean storeFile(String remote, InputStream local) throws RemoteException;

    /**
     * Retrieve a file from a remote server.
     * <p>
     * <b>Avoid closing of the given {@link OutputStream output stream}!</b>
     * @param remote the name of the file to be retrieved from a server.
     * @param local {@link OutputStream output stream} of the local file to be retrieved from a server.
     * @return <code>true</code> if the file was successfully downloaded, <code>false</code> otherwise.
     * @throws RemoteException if any unexpected error occurs.
     */
    boolean retrieveFile(String remote, OutputStream local) throws RemoteException;

    /**
     * Delete file from a remote server.
     * @param pathname file path of the file to be deleted.
     * @return <code>true</code> if the file deletion was successful, <code>false</code> otherwise.
     * @throws RemoteException if any unexpected error occurs.
     */
    boolean deleteFile(String pathname) throws RemoteException;

    /**
     * Rename the file on a remote server.
     * @param from the old name.
     * @param to the new name.
     * @return <code>true</code> if the file renaming was successful, <code>false</code> otherwise.
     * @throws RemoteException if any unexpected error occurs.
     */
    boolean rename(String from, String to) throws RemoteException;

    /**
     * Get the list of the {@link RemoteFile files} of the current directory.
     * <p>
     * {@link PathInfo} contains information about the current working directory
     * and should be used only when throwing an {@link RemoteException remote exception}.
     * @param pathInfo information about the current working directory.
     * @return the list of the {@link RemoteFile files} of the current directory, never <code>null</code>.
     * @throws RemoteException if any unexpected error occurs.
     */
    List<RemoteFile> listFiles(PathInfo pathInfo) throws RemoteException;

    /**
     * This class contains information about the current working directory.
     * Its typical usage is only for providing additional information if any error occurs.
     */
    final class PathInfo {
        private final String baseDirectory;
        private final String parentDirectory;

        /**
         * Create a new instance of {@link PathInfo}.
         * @param baseDirectory base directory of the connection; it means the starting directory
         *        when one connects to a remote server.
         * @param parentDirectory parent directory of the current working directory, relative to <code>base directory</code>.
         */
        public PathInfo(String baseDirectory, String parentDirectory) {
            assert baseDirectory != null;
            assert parentDirectory != null;

            this.baseDirectory = baseDirectory;
            this.parentDirectory = parentDirectory;
        }

        /**
         * Get the base directory of the connection; it means the starting directory
         * when one connects to a remote server.
         * @return the base directory.
         */
        public String getBaseDirectory() {
            return baseDirectory;
        }

        /**
         * Get parent directory of the current working directory, relative to <code>base directory</code>.
         * @return parent directory of the current working directory, relative to <code>base directory</code>.
         */
        public String getParentDirectory() {
            return parentDirectory;
        }

        /**
         * Get the full path of the parent directory of the current working directory; it means
         * {@link #getParentDirectory() parent directory} prepended with {@link #getBaseDirectory() base directory}.
         * @return the full path of the parent directory of the current working directory.
         */
        public String getFullPath() {
            return baseDirectory + TransferFile.SEPARATOR + parentDirectory;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(200);
            sb.append(getClass().getName());
            sb.append(" [baseDirectory: "); // NOI18N
            sb.append(baseDirectory);
            sb.append(", parentDirectory: "); // NOI18N
            sb.append(parentDirectory);
            sb.append("]"); // NOI18N
            return sb.toString();
        }
    }
}
