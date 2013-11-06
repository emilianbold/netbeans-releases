/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.jstestdriver;

import java.awt.EventQueue;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.javascript.jstestdriver.api.JsTestDriver;
import org.netbeans.modules.javascript.jstestdriver.api.RunTests;
import org.netbeans.modules.web.clientproject.api.ProjectDirectoriesProvider;
import org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProviders;
import org.netbeans.modules.web.clientproject.api.jstesting.TestRunInfo;
import org.netbeans.modules.web.clientproject.spi.jstesting.JsTestingProviderImplementation;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.spi.project.ui.support.NodeList;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = JsTestingProviderImplementation.class, path = JsTestingProviders.JS_TESTING_PATH, position = 200)
public class JsTestingProviderImpl implements JsTestingProviderImplementation {

    private static final Logger LOGGER = Logger.getLogger(JsTestingProviderImpl.class.getName());


    @Override
    public String getIdentifier() {
        return "JsTestDriver"; // NOI18N
    }

    @NbBundle.Messages("JsTestingProviderImpl.displayName=JS Test Driver")
    @Override
    public String getDisplayName() {
        return Bundle.JsTestingProviderImpl_displayName();
    }

    @Override
    public void runTests(Project project, TestRunInfo runInfo) {
        assert !EventQueue.isDispatchThread();
        FileObject configFile = getConfigFolder(project).getFileObject("jsTestDriver.conf"); // NOI18N
        if (configFile == null) {
            // XXX inform user, show dialog etc.
            LOGGER.log(Level.INFO, "Cannot run tests for \"{0}\" project, no jsTestDriver.conf found", ProjectUtils.getInformation(project).getName());
            return;
        }
        try {
            RunTests.runAllTests(project, project.getProjectDirectory(), configFile);
        } catch (Throwable t) {
            LOGGER.log(Level.SEVERE, "cannot execute tests", t); // NOI18N
        }
    }

    @Override
    public FileObject fromServer(Project project, URL serverUrl) {
        String serverU = WebUtils.urlToString(serverUrl);
        String prefix = JsTestDriver.getServerURL();
        if (!prefix.endsWith("/")) { // NOI18N
            prefix += "/"; // NOI18N
        }
        prefix += "test/"; // NOI18N
        if (!serverU.startsWith(prefix)) {
            return null;
        }
        String projectRelativePath = serverU.substring(prefix.length());
        try {
            projectRelativePath = URLDecoder.decode(projectRelativePath, "UTF-8"); // NOI18N
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        if (projectRelativePath.length() > 0) {
            return project.getProjectDirectory().getFileObject(projectRelativePath);
        }
        return null;
    }

    @Override
    public URL toServer(Project project, FileObject projectFile) {
        String prefix = JsTestDriver.getServerURL();
        if (!prefix.endsWith("/")) { // NOI18N
            prefix += "/"; // NOI18N
        }
        prefix += "test/"; // NOI18N
        String relativePath = FileUtil.getRelativePath(project.getProjectDirectory(), projectFile);
        if (relativePath != null) {
            try {
                return new URL(prefix + relativePath);
            } catch (MalformedURLException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }
        return null;
    }

    @Override
    public ProjectCustomizer.CompositeCategoryProvider createCustomizer(Project project) {
        return null;
    }

    @Override
    public void notifyEnabled(Project project, boolean enabled) {
        // noop
    }

    @Override
    public void projectOpened(Project project) {
        // noop
    }

    @Override
    public void projectClosed(Project project) {
        // noop
    }

    @Override
    public NodeList<Node> createNodeList(Project project) {
        return null;
    }

    private FileObject getConfigFolder(Project project) {
        ProjectDirectoriesProvider directoriesProvider = project.getLookup().lookup(ProjectDirectoriesProvider.class);
        if (directoriesProvider != null) {
            FileObject configDirectory = directoriesProvider.getConfigDirectory();
            if (configDirectory != null) {
                return configDirectory;
            }
        }
        return project.getProjectDirectory();
    }

}
