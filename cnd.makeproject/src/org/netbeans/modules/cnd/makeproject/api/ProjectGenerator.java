/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.makeproject.api;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.MakeProject;
import org.netbeans.modules.cnd.makeproject.MakeProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.ui.wizards.MakeSampleProjectGenerator;

public class ProjectGenerator {

    public static String getDefaultProjectFolder() {
        return MakeProjectGenerator.getDefaultProjectFolder();
    }

    public static String getValidProjectName(String projectFolder) {
        return MakeProjectGenerator.getValidProjectName(projectFolder);
    }

    public static String getValidProjectName(String projectFolder, String suggestedProjectName) {
        return MakeProjectGenerator.getValidProjectName(projectFolder, suggestedProjectName);
    }
    
    public static Project createBlankProject(String projectName, String projectFolder) throws IOException {
        return MakeProjectGenerator.createBlankProject(projectName, projectFolder, false);
    }

    public static Project createBlankProject(String projectName, String makefileName, String projectFolder) throws IOException {
        return MakeProjectGenerator.createBlankProject(projectName, makefileName, projectFolder, false);
    }

    public static Project createBlankProject(String projectName, String projectFolder, MakeConfiguration[] confs, boolean open) throws IOException {
        return MakeProjectGenerator.createBlankProject(projectName, projectFolder, confs, open);
    }

    public static Project createProject(File dir, String name, String makefileName, MakeConfiguration[] confs, Iterator<SourceFolderInfo> sourceFolders, String sourceFoldersFilter, Iterator<SourceFolderInfo> testFolders, Iterator<String> importantItems) throws IOException {
        MakeProject createdProject = MakeProjectGenerator.createProject(dir, name, makefileName, confs, sourceFolders, sourceFoldersFilter, testFolders, importantItems, null);
        ConfigurationDescriptorProvider.recordCreatedProjectMetrics(confs);
        return createdProject;
    }

    /*
     * Used by Sun Studio
     */
    public static void createProjectFromTemplate(URL url, String projectName, String projectFolder) throws IOException {
        MakeSampleProjectGenerator.createProjectFromTemplate(url.openStream(), new File(projectFolder, projectName), projectName); // NOI18N
    }

    private ProjectGenerator() {
    }
}
