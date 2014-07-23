/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.karma.mapping;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.karma.exec.KarmaServers;
import org.netbeans.modules.web.clientproject.api.ProjectDirectoriesProvider;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.modules.web.common.spi.ProjectWebRootQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

public final class ServerMapping {

    private static final Logger LOGGER = Logger.getLogger(ServerMapping.class.getName());

    private static final String BASE_PREFIX = "base/"; // NOI18N
    private static final String ABSOLUTE_PREFIX = "absolute/"; // NOI18N


    public FileObject fromServer(Project project, URL serverUrl) {
        String serverUrlString = WebUtils.urlToString(serverUrl);
        // try absolute first
        String prefix = KarmaServers.getInstance().getServerUrl(project, ABSOLUTE_PREFIX);
        if (prefix == null) {
            return null;
        }
        assert prefix.endsWith("/") : prefix;
        if (serverUrlString.startsWith(prefix)) {
            String absolutePath = serverUrlString.substring(prefix.length());
            if (Utilities.isUnix()) {
                // add leading slash
                absolutePath = "/" + absolutePath; // NOI18N
            }
            try {
                absolutePath = URLDecoder.decode(absolutePath, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return FileUtil.toFileObject(new File(absolutePath));
        }
        // now relative
        prefix = KarmaServers.getInstance().getServerUrl(project, BASE_PREFIX);
        assert prefix != null;
        if (!serverUrlString.startsWith(prefix)) {
            return null;
        }
        assert prefix.endsWith("/") : prefix;
        String projectRelativePath = serverUrlString.substring(prefix.length());
        try {
            projectRelativePath = URLDecoder.decode(projectRelativePath, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        if (!projectRelativePath.isEmpty()) {
            return project.getProjectDirectory().getFileObject(projectRelativePath);
        }
        return null;
    }

    public URL toServer(Project project, FileObject projectFile) {
        String serverUrl = KarmaServers.getInstance().getServerUrl(project, null);
        if (serverUrl == null) {
            return null;
        }
        assert serverUrl.endsWith("/") : serverUrl;
        if (KarmaServers.getInstance().isAbsoluteUrls(project)) {
            return createAbsoluteUrl(serverUrl, projectFile);
        }
        FileObject projectDirectory = project.getProjectDirectory();
        // try relative first
        String filePath = FileUtil.getRelativePath(projectDirectory, projectFile);
        if (filePath != null) {
            return createUrl(serverUrl, BASE_PREFIX, filePath);
        }
        // now absolute (tests or web root outside project folder)
        // web root first
        for (FileObject webRoot : ProjectWebRootQuery.getWebRoots(project)) {
            if (isUnderneath(webRoot, projectFile)) {
                return createAbsoluteUrl(serverUrl, projectFile);
            }
        }
        // now tests
        FileObject testsFolder = getTestsFolder(project);
        if (testsFolder == null) {
            // no tests
            return null;
        }
        boolean testsUnderneath = isUnderneath(projectDirectory, testsFolder);
        if (testsUnderneath) {
            return null;
        }
        if (isUnderneath(testsFolder, projectFile)) {
            return createAbsoluteUrl(serverUrl, projectFile);
        }
        return null;
    }

    private URL createAbsoluteUrl(String server, FileObject file) {
        assert server.endsWith("/") : server;
        assert file != null;
        String filePath = FileUtil.toFile(file).getAbsolutePath();
        if (filePath.startsWith("/")) { // NOI18N
            // remove leading slash
            filePath = filePath.substring(1);
        }
        return createUrl(server, ABSOLUTE_PREFIX, filePath);
    }

    private URL createUrl(String server, String prefix, String filePath) {
        assert server.endsWith("/") : server;
        assert prefix.endsWith("/") : prefix;
        assert !filePath.startsWith("/") : filePath;
        try {
            return new URL(server + prefix + filePath);
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        return null;
    }

    @CheckForNull
    private FileObject getTestsFolder(Project project) {
        ProjectDirectoriesProvider directoriesProvider = project.getLookup().lookup(ProjectDirectoriesProvider.class);
        if (directoriesProvider == null) {
            return null;
        }
        return directoriesProvider.getTestDirectory(false);
    }

    private boolean isUnderneath(FileObject root, FileObject folder) {
        return root.equals(folder)
                || FileUtil.isParentOf(root, folder);
    }

}
