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
package org.netbeans.modules.web.clientproject;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;

// XXX use project mutex everywhere!
/**
 *
 * @author david
 */
public final class ClientSideProjectUtilities {

    private ClientSideProjectUtilities() {
    }

    public static AntProjectHelper setupProject(FileObject dirFO, String name) throws IOException {
        AntProjectHelper h = ProjectGenerator.createProject(dirFO, ClientSideProjectType.TYPE);
        return h;
    }

    public static void initializeProject(AntProjectHelper h) throws IOException {
        initializeProject(h, ClientSideProjectConstants.DEFAULT_SITE_ROOT_FOLDER,
                ClientSideProjectConstants.DEFAULT_TEST_FOLDER, ClientSideProjectConstants.DEFAULT_CONFIG_FOLDER);
    }

    public static void initializeProject(AntProjectHelper h, String siteRoot, String test, String config) throws IOException {
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty(ClientSideProjectConstants.PROJECT_SITE_ROOT_FOLDER, siteRoot);
        ep.setProperty(ClientSideProjectConstants.PROJECT_TEST_FOLDER, test);
        ep.setProperty(ClientSideProjectConstants.PROJECT_CONFIG_FOLDER, config);
        h.getProjectDirectory().createFolder(siteRoot);
        h.getProjectDirectory().createFolder(test);
        h.getProjectDirectory().createFolder(config);
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        Project p = ProjectManager.getDefault().findProject(h.getProjectDirectory());
        ProjectManager.getDefault().saveProject(p);
    }

    // XXX "merge" with the method above
    public static void initializeProject(AntProjectHelper projectHelper, String siteRoot) throws IOException {
        assert projectHelper.getProjectDirectory().getFileObject(siteRoot) != null : "Site root must exist: " + siteRoot;
        EditableProperties editableProperties = projectHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        editableProperties.setProperty(ClientSideProjectConstants.PROJECT_SITE_ROOT_FOLDER, siteRoot);
        projectHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, editableProperties);
        Project project = ProjectManager.getDefault().findProject(projectHelper.getProjectDirectory());
        ProjectManager.getDefault().saveProject(project);
    }

    public static FileObject getSiteRootFolder(AntProjectHelper h) throws IOException, IllegalArgumentException {
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String s = ep.getProperty(ClientSideProjectConstants.PROJECT_SITE_ROOT_FOLDER);
        if (s == null || s.length() == 0) {
            return null;
        }
        return h.getProjectDirectory().getFileObject(s);
    }

    /**
     * Relativize the given {@code file} to the given {@code baseDir}.
     * If the path cannot be relativized, the full absolute path of the {@code file} is returned.
     * @param baseDir base directory
     * @param file file to be relativized
     * @return relative path or absolute path if relative path does not exist
     * @see PropertyUtils#relativizeFile(File, File)
     */
    public static String relativizeFile(File baseDir, File file) {
        String relPath = PropertyUtils.relativizeFile(baseDir, file);
        if (relPath != null) {
            return relPath;
        }
        return file.getAbsolutePath();
    }

}
