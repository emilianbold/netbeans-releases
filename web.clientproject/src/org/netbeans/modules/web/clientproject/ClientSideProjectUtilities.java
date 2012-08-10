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

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.openide.filesystems.FileObject;

/**
 *
 * @author david
 */
public class ClientSideProjectUtilities {

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
    
    public static FileObject getSiteRootFolder(AntProjectHelper h) throws IOException, IllegalArgumentException {
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String s = ep.getProperty(ClientSideProjectConstants.PROJECT_SITE_ROOT_FOLDER);
        if (s == null || s.length() == 0) {
            return null;
        }
        return h.getProjectDirectory().getFileObject(s);
    }
}
