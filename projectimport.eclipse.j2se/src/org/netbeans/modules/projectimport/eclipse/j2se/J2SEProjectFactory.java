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

package org.netbeans.modules.projectimport.eclipse.j2se;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.modules.java.j2seproject.J2SEProjectType;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectFactorySupport;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectImportModel;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectTypeUpdater;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;
import org.w3c.dom.Element;

/**
 *
 */
public class J2SEProjectFactory implements ProjectTypeUpdater {

    private static final String JAVA_NATURE = "org.eclipse.jdt.core.javanature"; // NOI18N
    private static final Icon J2SE_PROJECT_ICON = new ImageIcon(Utilities.loadImage("org/netbeans/modules/java/j2seproject/ui/resources/j2seProject.png")); // NOI18N

    public J2SEProjectFactory() {
    }
    
    public boolean canHandle(Set<String> natures) {
        return natures.contains(JAVA_NATURE);
    }

    public Project createProject(final ProjectImportModel model, final List<String> importProblems) throws IOException {
        // calculate nb project location
        File nbProjectDir = FileUtil.normalizeFile(new File(model.getNetBeansProjectLocation())); // NOI18N
        
        // create basic NB project
        final AntProjectHelper helper = J2SEProjectGenerator.createProject(
                nbProjectDir, model.getProjectName(), model.getEclipseSourceRootsAsFileArray(), 
                model.getEclipseTestSourceRootsAsFileArray(), null, null, null);
        
        // get NB project
        J2SEProject nbProject = (J2SEProject) ProjectManager.getDefault().
                findProject(FileUtil.toFileObject(
                FileUtil.normalizeFile(nbProjectDir)));
        
        // set labels for source roots
        ProjectFactorySupport.updateSourceRootLabels(model.getEclipseSourceRoots(), nbProject.getSourceRoots());
        ProjectFactorySupport.updateSourceRootLabels(model.getEclipseTestSourceRoots(), nbProject.getTestSourceRoots());
        
        // TODO: setup include/exclude here
        
        // Make sure PCPM knows who owns this (J2SEProject will do the same later on anyway):
        if (!nbProjectDir.equals(model.getEclipseProjectFolder())) {
            FileOwnerQuery.markExternalOwner(model.getEclipseProjectFolder().toURI(), nbProject, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        }
        
        // update project classpath
        ProjectFactorySupport.updateProjectClassPath(helper, model, importProblems);
        
        // set platform used by an Eclipse project
        if (model.getJavaPlatform() != null) {
            setExplicitJavaPlatform(helper, model);
        }

        // save project
        ProjectManager.getDefault().saveProject(nbProject);
        return nbProject;
    }

    private void setExplicitJavaPlatform(final AntProjectHelper helper, final ProjectImportModel model) {
        Element pcd = helper.getPrimaryConfigurationData(true);
        Element el = pcd.getOwnerDocument().createElementNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, "explicit-platform"); // NOI18N
        pcd.appendChild(el);
        helper.putPrimaryConfigurationData(pcd, true);
        EditableProperties prop = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String ver = model.getJavaPlatform().getSpecification().getVersion().toString();
        String normalizedName = (String) model.getJavaPlatform().getProperties().get(
                    "platform.ant.name"); // NOI18N
        prop.setProperty(J2SEProjectProperties.JAVAC_SOURCE, ver);
        prop.setProperty(J2SEProjectProperties.JAVAC_TARGET, ver);
        prop.setProperty(J2SEProjectProperties.JAVA_PLATFORM, normalizedName);
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, prop);
    }

    public String calculateKey(ProjectImportModel model) {
        return ProjectFactorySupport.calculateKey(model);
    }

    public String update(Project project, ProjectImportModel model, String oldKey, List<String> importProblems) throws IOException {
        String newKey = calculateKey(model);
        
        // update project classpath
        String actualKey = ProjectFactorySupport.synchronizeProjectClassPath(project, ((J2SEProject)project).getAntProjectHelper(), model, oldKey, newKey, importProblems);
        
        // TODO:
        // update source roots and platform
        
        // save project
        ProjectManager.getDefault().saveProject(project);
        
        return actualKey;
    }

    public Icon getProjectTypeIcon() {
        return J2SE_PROJECT_ICON;
    }

    public String getProjectTypeName() {
        return "Java Project";
    }
    
}
