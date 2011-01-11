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

package org.netbeans.modules.remote.impl.fs;

import java.io.FileNotFoundException;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import sun.security.util.SecurityConstants;

/**
 *
 * @author Vladimir Kvashin
 */
public class RemoteFileURLConnection extends URLConnection {

    private final ExecutionEnvironment execEnv;
    private final String path;

    private FileObject fileObject;

    /** a stream per connection */
    private InputStream iStream = null;

    /** a stream per connection */
    private OutputStream oStream = null;

    public RemoteFileURLConnection(URL url) throws IOException {
        super(url);
        if (! RemoteFileURLStreamHandler.PROTOCOL.equals(url.getProtocol())) {
            throw new IllegalArgumentException("Illegal url protocol: " + url.getProtocol()); //NOI18N
        }
        execEnv = ExecutionEnvironmentFactory.createNew(url.getUserInfo(), url.getHost(), url.getPort());
        path = url.getFile();
    }

    @Override
    public void connect() throws IOException {
        RemoteFileSystem fs = RemoteFileSystemManager.getInstance().getFileSystem(execEnv);
        fileObject = fs.findResource(path);
        if (fileObject == null || !fileObject.isValid()) {
            throw newFileNotFoundException();
        }
    }

    private FileNotFoundException newFileNotFoundException() {
        return new FileNotFoundException(execEnv.getDisplayName() + ':' + path); //NOI18N
    }

    @Override
    public InputStream getInputStream() throws IOException {
	if (fileObject == null) {
            newFileNotFoundException();
        }
        if (iStream == null) {
            iStream = fileObject.getInputStream();
        }
        return iStream;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
	if (fileObject == null) {
            newFileNotFoundException();
        }
        if (oStream == null) {
            FileLock flock = fileObject.lock();
            oStream = new LockOS(fileObject.getOutputStream(flock), flock);
        }
        return oStream;
    }

    @Override
    public int getContentLength() {
        try {
            connect();
        } catch (IOException ex) {
            return 0;
        }
        return (int) fileObject.getSize();
    }

    @Override
    public Permission getPermission() throws IOException {
        connect();
        StringBuilder actions = new StringBuilder();
        if (fileObject != null) {
            if (fileObject.isValid()) {
                if (fileObject.canRead()) {
                    actions.append(SecurityConstants.FILE_READ_ACTION).append(' ');
                }
                if (fileObject.canWrite()) {
                    actions.append(SecurityConstants.FILE_WRITE_ACTION).append(' ');
                }
            }
        }
        return new FilePermission(path, actions.toString());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ' ' + url;
    }

    private static class LockOS extends java.io.BufferedOutputStream {

        private final FileLock flock;

        public LockOS(OutputStream os, FileLock lock) throws IOException {
            super(os);
            flock = lock;
        }

        @Override
        public void close() throws IOException {
            flock.releaseLock();
            super.close();
        }
    }
}

