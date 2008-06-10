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

package org.netbeans.modules.projectimport.eclipse.web;

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
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectFactorySupport;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectImportModel;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectTypeUpdater;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.api.WebProjectCreateData;
import org.netbeans.modules.web.project.api.WebProjectUtilities;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

// TODO: current detection of whether NB project is uptodate with Eclipse or not
// is based just on .classpath/.project. For web support file
// ".settings/org.eclipse.wst.common.component" should be checked as well.

/**
 *
 */
public class WebProjectFactory implements ProjectTypeUpdater {

    private static final String WEB_NATURE = "org.eclipse.wst.common.modulecore.ModuleCoreNature"; // NOI18N
    private static final Icon WEB_PROJECT_ICON = new ImageIcon(Utilities.loadImage("org/netbeans/modules/web/project/ui/resources/webProjectIcon.gif")); // NOI18
    
    // TODO: check this one as well
    private static final String MYECLIPSE_WEB_NATURE = "com.genuitec.eclipse.j2eedt.core.webnature"; // NOI18N

    public WebProjectFactory() {
    }
    
    public boolean canHandle(Set<String> natures) {
        return natures.contains(WEB_NATURE);
    }

    public Project createProject(final ProjectImportModel model, final List<String> importProblems) throws IOException {
        // create nb project location
        File nbProjectDir = FileUtil.normalizeFile(new File(model.getNetBeansProjectLocation())); // NOI18N
        
        WebContentData webData = parseWebContent(model.getEclipseProjectFolder());

        //
        //
        // TODO: most of the values defaulted for now:
        //
        //
        
        WebProjectCreateData createData = new WebProjectCreateData();
        createData.setProjectDir(nbProjectDir);
        createData.setName(model.getProjectName());
        assert Deployment.getDefault().getServerInstanceIDs().length > 0 : "sorry , for now you have to have at least one server";
        createData.setServerInstanceID(Deployment.getDefault().getServerInstanceIDs()[0]);
        createData.setJavaEEVersion("1.5");
        createData.setSourceLevel("1.5");
        createData.setJavaPlatformName(model.getJavaPlatform().getDisplayName());
        createData.setServerLibraryName(null);

        FileObject root = FileUtil.toFileObject(model.getEclipseProjectFolder());
        createData.setWebModuleFO(root);
        createData.setSourceFolders(model.getEclipseSourceRootsAsFileArray());
        createData.setTestFolders(model.getEclipseTestSourceRootsAsFileArray());
        createData.setContextPath(webData.contextRoot);
        createData.setDocBase(root.getFileObject(webData.webRoot));
        createData.setLibFolder(root.getFileObject(webData.webRoot+"/WEB-INF/lib"));
        createData.setWebInfFolder(root.getFileObject(webData.webRoot+"/WEB-INF"));
        createData.setLibrariesDefinition(null);
        createData.setBuildfile("build.xml");
        
        AntProjectHelper helper = WebProjectUtilities.importProject(createData);
        Project nbProject = ProjectManager.getDefault().findProject(helper.getProjectDirectory());
        
        // set labels for source roots
//        ProjectFactorySupport.updateSourceRootLabels(model.getEclipseSourceRoots(), nbProject.getSourceRoots());
//        ProjectFactorySupport.updateSourceRootLabels(model.getEclipseTestSourceRoots(), nbProject.getTestSourceRoots());
        
        // TODO: setup include/exclude here
        
        // Make sure PCPM knows who owns this (J2SEProject will do the same later on anyway):
        if (!nbProjectDir.equals(model.getEclipseProjectFolder())) {
            FileOwnerQuery.markExternalOwner(model.getEclipseProjectFolder().toURI(), nbProject, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        }
        
        // update project classpath
        ProjectFactorySupport.updateProjectClassPath(helper, model, importProblems);
        
        // save project
        ProjectManager.getDefault().saveProject(nbProject);
        return nbProject;
    }

    private static WebContentData parseWebContent(File eclipseProject) throws IOException {
        File f = new File(eclipseProject, ".settings/org.eclipse.wst.common.component"); // NOI18N
        Document webContent;
        try {
            webContent = XMLUtil.parse(new InputSource(f.toURI().toString()), false, true, Util.defaultErrorHandler(), null);
        } catch (SAXException e) {
            IOException ioe = (IOException) new IOException(f + ": " + e.toString()).initCause(e);
            throw ioe;
        }
        Element modulesEl = webContent.getDocumentElement();
        if (!"project-modules".equals(modulesEl.getLocalName())) { // NOI18N
            return null;
        }
        WebContentData data = new WebContentData();
        Element moduleEl = Util.findElement(modulesEl, "wb-module", null);
        assert modulesEl != null;
        for (Element el : Util.findSubElements(moduleEl)) {
            if ("wb-resource".equals(el.getNodeName())) {
                if ("/".equals(el.getAttribute("deploy-path"))) {
                    data.webRoot = el.getAttribute("source-path");
                }
            }
            if ("property".equals(el.getNodeName())) {
                if ("context-root".equals(el.getAttribute("name"))) {
                    data.contextRoot = el.getAttribute("value");
                }
            }
        }
        return data;
    }
    
    private static class WebContentData {
        private String contextRoot;
        private String webRoot;
    }

    public String calculateKey(ProjectImportModel model) {
        WebContentData webData;
        try {
            webData = parseWebContent(model.getEclipseProjectFolder());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            webData = new WebContentData();
            webData.contextRoot = "??";
            webData.webRoot = "??";
        }
        return ProjectFactorySupport.calculateKey(model) + "web=" + webData.webRoot + ";" + "context=" + webData.contextRoot + ";";
    }

    public void update(Project project, ProjectImportModel model, String oldKey) throws IOException {
        String newKey = calculateKey(model);
        
        // update project classpath
        ProjectFactorySupport.synchronizeProjectClassPath(project, ((WebProject)project).getAntProjectHelper(), model, oldKey, newKey, new ArrayList<String>());
        
        // TODO:
        // update source roots and platform and server and web root and context
        
        // save project
        ProjectManager.getDefault().saveProject(project);
    }

    public Icon getProjectTypeIcon() {
        return WEB_PROJECT_ICON;
    }

    public String getProjectTypeName() {
        return "Web Application";
    }
    
}
