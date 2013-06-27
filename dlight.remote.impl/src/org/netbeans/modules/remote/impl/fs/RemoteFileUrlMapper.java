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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 *
 * @author Vladimir Kvashin
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.filesystems.URLMapper.class)
public class RemoteFileUrlMapper extends URLMapper {

    @Override
    public FileObject[] getFileObjects(URL url) {
        if (url.getProtocol().equals(RemoteFileURLStreamHandler.PROTOCOL)) {
            ExecutionEnvironment env;
            String user = url.getUserInfo();
            if (user != null) {
                env = ExecutionEnvironmentFactory.createNew(user, url.getHost(), url.getPort());
            } else {
                RemoteLogger.assertTrue(false, "Trying to access remote file system without user name");
                env = RemoteFileSystemUtils.getExecutionEnvironment(url.getHost(), url.getPort());
                if (env == null) {
                    user = System.getProperty("user.name");
                    if (user != null) {
                        env = ExecutionEnvironmentFactory.createNew(user, url.getHost(), url.getPort());
                    }
                }
            }
            if (env != null) {
                RemoteFileSystem fs = RemoteFileSystemManager.getInstance().getFileSystem(env);
                FileObject fo = fs.findResource(unescapePath(url));
                return new FileObject[] { fo };
            }
        }
        return null;
    }

    @Override
    public URL getURL(FileObject fo, int type) {
        if (fo instanceof RemoteFileObject) {
            RemoteFileObject rfo = (RemoteFileObject) fo;
            try {
                ExecutionEnvironment env = rfo.getExecutionEnvironment();
                return toURL(env, rfo.getPath(), rfo.isFolder());
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

    public static URI toURI(ExecutionEnvironment env, String path, boolean folder) throws URISyntaxException {
        return new URI(toURLString(env, path, folder));
    }
    
    public static URL toURL(ExecutionEnvironment env, String path, boolean folder) throws MalformedURLException {
        return new URL(toURLString(env, path, folder));
    }

    private static String toURLString(ExecutionEnvironment env, String path, boolean folder) {
        /*
         * Prepare URL here as a string to be used in the URL(String spec)
         * constructor as it works with userinfo as expected (ipv6 address case).
         * URL(String protocol, String host, int port, String file) cannot be
         * used here, as 'host' should contain only host information, without 
         * username/password etc... 
         */
        StringBuilder sb = new StringBuilder(RemoteFileURLStreamHandler.PROTOCOL);
        sb.append("://"); // NOI18N
        sb.append(env.getUser()).append('@').append(env.getHost());
        sb.append(':').append(env.getSSHPort()).append(escapePath(path));
        if (folder && !(path.endsWith("/"))) { // NOI18N
            sb.append('/'); // NOI18N
        }
        return sb.toString();
    }

    private static String escapePath(String path) {
        if (path.indexOf('#') >= 0) { //NOI18N
            path = path.replace("#", "%23"); //NOI18N
        }
        if (path.indexOf('?') >= 0) { //NOI18N
            path = path.replace("?", "%3f"); //NOI18N
        }
        if (path.indexOf(' ') >= 0) { //NOI18N
            path = path.replace(" ", "%20"); //NOI18N
        }
        return path;
    }

    private static String unescapePath(URL url) {
        String path = url.getFile();
        if (path.contains("%23") || path.contains("%3f") || path.contains("%20")) { //NOI18N
            try {
                return url.toURI().getPath();
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return path;
    }
}
