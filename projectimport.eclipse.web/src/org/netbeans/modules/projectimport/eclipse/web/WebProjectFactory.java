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
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectFactorySupport;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectImportModel;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectTypeFactory;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectTypeFactory.ProjectDescriptor;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectTypeUpdater;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.api.WebProjectCreateData;
import org.netbeans.modules.web.project.api.WebProjectUtilities;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
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

    private static final Logger LOG =
            Logger.getLogger(WebProjectFactory.class.getName());
    private static final String WEB_NATURE = "org.eclipse.wst.common.modulecore.ModuleCoreNature"; // NOI18N
    private static final Icon WEB_PROJECT_ICON = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/web/project/ui/resources/webProjectIcon.gif")); // NOI18
    
    // TODO: check this one as well
    private static final String MYECLIPSE_WEB_NATURE = "com.genuitec.eclipse.j2eedt.core.webnature"; // NOI18N
    
    public WebProjectFactory() {
    }
    
    public boolean canHandle(ProjectDescriptor descriptor) {
        // eclipse ganymede and europa are using facets:
        if (descriptor.getFacets() != null) {
            return descriptor.getFacets().hasInstalledFacet("jst.web");
        }
        if (descriptor.getNatures().contains(WEB_NATURE)) {
            // this is perhaps case of older Eclipse versions??
            // TODO: perhaps not needed
            return true;
        }
        // accept MyEclipse web projects
        return descriptor.getNatures().contains(MYECLIPSE_WEB_NATURE);
    }

    private ServerSelectionWizardPanel findWizardPanel(ProjectImportModel model) {
        assert model.getExtraWizardPanels() != null;
        for (WizardDescriptor.Panel panel : model.getExtraWizardPanels()) {
            if (panel instanceof ServerSelectionWizardPanel) {
                return (ServerSelectionWizardPanel)panel;
            }
        }
        return null;
    }
    
    public Project createProject(final ProjectImportModel model, final List<String> importProblems) throws IOException {
        // create nb project location
        File nbProjectDir = model.getNetBeansProjectLocation(); // NOI18N
        
        WebContentData webData = parseWebContent(model.getEclipseProjectFolder());

        String serverID;
        if (model.getExtraWizardPanels() != null) {
            ServerSelectionWizardPanel wizard = findWizardPanel(model);
            assert wizard != null;
            serverID = wizard.getServerID();
        } else {
            if (Deployment.getDefault().getServerInstanceIDs().length == 0) {
                importProblems.add("Web project cannot be imported without a J2EE server.");
                return null;
            } else {
                serverID = Deployment.getDefault().getServerInstanceIDs()[0];
            }
        }
        
        WebProjectCreateData createData = new WebProjectCreateData();
        createData.setProjectDir(nbProjectDir);
        createData.setName(model.getProjectName());
        createData.setServerInstanceID(serverID);
        createData.setJavaEEVersion("1.5");
        createData.setSourceLevel(model.getSourceLevel());
        if (model.getJavaPlatform() != null) {
            createData.setJavaPlatformName(model.getJavaPlatform().getDisplayName());
        }
        createData.setServerLibraryName(null);

        FileObject root = FileUtil.toFileObject(model.getEclipseProjectFolder());
        if (root.getFileObject(webData.webRoot) == null) {
            importProblems.add("web document root does not exist ('" + webData.webRoot + "'). project will not be imported.");
        }
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
        WebProject nbProject = (WebProject)ProjectManager.getDefault().findProject(helper.getProjectDirectory());
        
        // set labels for source roots
        ProjectFactorySupport.updateSourceRootLabels(model.getEclipseSourceRoots(), nbProject.getSourceRoots());
        ProjectFactorySupport.updateSourceRootLabels(model.getEclipseTestSourceRoots(), nbProject.getTestSourceRoots());
        
        ProjectFactorySupport.setupSourceExcludes(helper, model, importProblems);

        setupCompilerProperties(helper, model);
        
        // Make sure PCPM knows who owns this (J2SEProject will do the same later on anyway):
        if (!nbProjectDir.equals(model.getEclipseProjectFolder())) {
            FileOwnerQuery.markExternalOwner(model.getEclipseProjectFolder().toURI(), nbProject, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        }
        
        // update project classpath
        ProjectFactorySupport.updateProjectClassPath(helper, nbProject.getReferenceHelper(), model, importProblems);
        
        // save project
        ProjectManager.getDefault().saveProject(nbProject);
        return nbProject;
    }

    private static WebContentData parseWebContent(File eclipseProject) throws IOException {
        File f = new File(eclipseProject, ".settings/org.eclipse.wst.common.component"); // NOI18N
        if (!f.exists()) {
            f = new File(eclipseProject, ".settings/.component"); // NOI18N
        }
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

        @Override
        public String toString() {
            return "WebContentData[contextRoot="+contextRoot+", webRoot="+webRoot+"]"; // NOI18N
        }
        
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

    public String update(Project project, ProjectImportModel model, String oldKey, List<String> importProblems) throws IOException {
        if (!(project instanceof WebProject)) {
            throw new IOException("is not web project: "+project.getClass().getName());
        }
        String newKey = calculateKey(model);
        
        // update project classpath
        String actualKey = ProjectFactorySupport.synchronizeProjectClassPath(project, 
                ((WebProject)project).getAntProjectHelper(), 
                ((WebProject)project).getReferenceHelper(), model, oldKey, newKey, importProblems);
        
        setupCompilerProperties(((WebProject) project).getAntProjectHelper(), model);

        // TODO:
        // update source roots and platform and server and web root and context
        
        // save project
        ProjectManager.getDefault().saveProject(project);
        
        return actualKey;
    }

    public Icon getProjectTypeIcon() {
        return WEB_PROJECT_ICON;
    }

    public String getProjectTypeName() {
        return "Web Application";
    }

    public List<WizardDescriptor.Panel> getAdditionalImportWizardPanels() {
        return Collections.<WizardDescriptor.Panel>singletonList(new ServerSelectionWizardPanel());
    }
    
    private void setupCompilerProperties(AntProjectHelper helper, ProjectImportModel model) {
        EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty(WebProjectProperties.JAVAC_SOURCE, model.getSourceLevel());
        ep.setProperty(WebProjectProperties.JAVAC_TARGET, model.getTargetLevel());
        ep.setProperty(WebProjectProperties.JAVAC_DEPRECATION, Boolean.toString(model.isDeprecation()));
        ep.setProperty(WebProjectProperties.JAVAC_COMPILER_ARG, model.getCompilerArgs());
        String enc = model.getEncoding();
        if (enc != null) {
            ep.setProperty(WebProjectProperties.SOURCE_ENCODING, enc);
        } else {
            ep.remove(WebProjectProperties.SOURCE_ENCODING);
        }
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ep = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        ep.setProperty(WebProjectProperties.JAVAC_DEBUG, Boolean.toString(model.isDebug()));
        helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
    }

    public File getProjectFileLocation(ProjectDescriptor descriptor, String token) {
        if (!token.equals(ProjectTypeFactory.FILE_LOCATION_TOKEN_WEBINF)) {
            return null;
        }
        WebContentData data;
        try {
            data = parseWebContent(descriptor.getEclipseProjectFolder());
        } catch (IOException ex) {
            LOG.log(Level.INFO, "cannot parse webmodule data", ex);
            return null;
        }
        if (data != null) {
            File f = new File(descriptor.getEclipseProjectFolder(), data.webRoot+File.separatorChar+"WEB-INF"+File.separator); // NOI18N
            if (f.exists()) {
                return f;
            }
        }
        return null;
    }

}
