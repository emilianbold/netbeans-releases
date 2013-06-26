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
package org.netbeans.modules.web.clientproject.ui.customizer;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.ClientSideProjectConstants;
import org.netbeans.modules.web.clientproject.api.jslibs.JavaScriptLibrarySelectionPanel;
import org.netbeans.modules.web.clientproject.api.jslibs.JavaScriptLibrarySelectionPanel.SelectedLibrary;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectEnhancedBrowserImplementation;
import org.netbeans.modules.web.clientproject.util.ClientSideProjectUtilities;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ui.CustomizerUtilities;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Becicka
 */
public final class ClientSideProjectProperties {

    private static final Logger LOGGER = Logger.getLogger(ClientSideProjectProperties.class.getName());

    final ClientSideProject project;
    private final List<JavaScriptLibrarySelectionPanel.SelectedLibrary> newJsLibraries = new CopyOnWriteArrayList<JavaScriptLibrarySelectionPanel.SelectedLibrary>();

    private volatile String siteRootFolder = null;
    private volatile String testFolder = null;
    private volatile String configFolder = null;
    private volatile String jsLibFolder = null;
    private volatile String encoding = null;
    private volatile String startFile = null;
    private volatile String selectedBrowser = null;
    private volatile String webRoot = null;
    private volatile String projectUrl = null;
    private volatile ProjectServer projectServer = null;
    private volatile ClientProjectEnhancedBrowserImplementation enhancedBrowserSettings = null;

    //customizer license headers
    private LicensePanelSupport licenseSupport;

    public ClientSideProjectProperties(ClientSideProject project) {
        this.project = project;
    }

    /**
     * Add or replace project and/or private properties.
     * <p>
     * This method cannot be called in the UI thread.
     * @param projectProperties project properties to be added to (replaced in) the current project properties
     * @param privateProperties private properties to be added to (replaced in) the current private properties
     */
    public void save(final Map<String, String> projectProperties, final Map<String, String> privateProperties) {
        assert !EventQueue.isDispatchThread();
        assert !projectProperties.isEmpty() || !privateProperties.isEmpty() : "Neither project nor private properties to be saved";
        try {
            // store properties
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws IOException {
                    AntProjectHelper helper = project.getProjectHelper();

                    mergeProperties(helper, AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties);
                    mergeProperties(helper, AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProperties);

                    ProjectManager.getDefault().saveProject(project);
                    return null;
                }

                private void mergeProperties(AntProjectHelper helper, String path, Map<String, String> properties) {
                    if (properties.isEmpty()) {
                        return;
                    }
                    EditableProperties currentProperties = helper.getProperties(path);
                    for (Map.Entry<String, String> entry : properties.entrySet()) {
                        currentProperties.put(entry.getKey(), entry.getValue());
                    }
                    helper.putProperties(path, currentProperties);
                }
            });
        } catch (MutexException e) {
            LOGGER.log(Level.WARNING, null, e.getException());
        }
    }

    public void save() {
        assert !EventQueue.isDispatchThread();
        try {
            getLicenseSupport().saveLicenseFile();
            // store properties
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws IOException {
                    saveProperties();
                    saveEnhancedBrowserConfiguration();
                    ProjectManager.getDefault().saveProject(project);
                    return null;
                }
            });
        } catch (MutexException | IOException e) {
            LOGGER.log(Level.WARNING, null, e);
        }
    }

    void saveProperties() {
        // first, create possible foreign file references
        String siteRootFolderReference = createForeignFileReference(siteRootFolder);
        String testFolderReference = createForeignFileReference(testFolder);
        String configFolderReference = createForeignFileReference(configFolder);
        // save properties
        EditableProperties privateProperties = project.getProjectHelper().getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        EditableProperties projectProperties = project.getProjectHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        putProperty(projectProperties, ClientSideProjectConstants.PROJECT_SITE_ROOT_FOLDER, siteRootFolderReference);
        putProperty(projectProperties, ClientSideProjectConstants.PROJECT_TEST_FOLDER, testFolderReference);
        putProperty(projectProperties, ClientSideProjectConstants.PROJECT_CONFIG_FOLDER, configFolderReference);
        putProperty(projectProperties, ClientSideProjectConstants.PROJECT_ENCODING, encoding);
        putProperty(projectProperties, ClientSideProjectConstants.PROJECT_START_FILE, startFile);
        // #227995: store PROJECT_SELECTED_BROWSER in private.properties:
        projectProperties.remove(ClientSideProjectConstants.PROJECT_SELECTED_BROWSER);
        putProperty(privateProperties, ClientSideProjectConstants.PROJECT_SELECTED_BROWSER, selectedBrowser);
        if (projectServer != null) {
            // #230903: store PROJECT_SERVER in private.properties:
            projectProperties.remove(ClientSideProjectConstants.PROJECT_SERVER);
            putProperty(privateProperties, ClientSideProjectConstants.PROJECT_SERVER, projectServer.name());
        }
        // #230903: store PROJECT_PROJECT_URL in private.properties:
        projectProperties.remove(ClientSideProjectConstants.PROJECT_PROJECT_URL);
        putProperty(privateProperties, ClientSideProjectConstants.PROJECT_PROJECT_URL, projectUrl);
        putProperty(projectProperties, ClientSideProjectConstants.PROJECT_WEB_ROOT, webRoot);
        getLicenseSupport().updateProperties(projectProperties);
        project.getProjectHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties);
        project.getProjectHelper().putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProperties);
    }

    void saveEnhancedBrowserConfiguration() {
        assert ProjectManager.mutex().isWriteAccess() : "Write mutex required"; //NOI18N
        if (enhancedBrowserSettings != null) {
            enhancedBrowserSettings.save();
        }
    }

    ClientProjectEnhancedBrowserImplementation createEnhancedBrowserSettings(WebBrowser wb) {
        enhancedBrowserSettings =
                ClientSideProject.createEnhancedBrowserImpl(project, wb);
        return enhancedBrowserSettings;
    }

    public ClientSideProject getProject() {
        return project;
    }

    public String getSiteRootFolder() {
        if (siteRootFolder == null) {
            siteRootFolder = getProjectProperty(ClientSideProjectConstants.PROJECT_SITE_ROOT_FOLDER, ""); // NOI18N
        }
        return siteRootFolder;
    }

    public void setSiteRootFolder(String siteRootFolder) {
        this.siteRootFolder = siteRootFolder;
    }

    public String getTestFolder() {
        if (testFolder == null) {
            testFolder = getProjectProperty(ClientSideProjectConstants.PROJECT_TEST_FOLDER, ""); // NOI18N
        }
        return testFolder;
    }

    public void setTestFolder(String testFolder) {
        if (testFolder == null) {
            // we need to find out that some value was set ("no value" in this case)
            testFolder = ""; // NOI18N
        }
        this.testFolder = testFolder;
    }

    public String getConfigFolder() {
        if (configFolder == null) {
            configFolder = getProjectProperty(ClientSideProjectConstants.PROJECT_CONFIG_FOLDER, ""); // NOI18N
        }
        return configFolder;
    }

    public void setConfigFolder(String configFolder) {
        if (configFolder == null) {
            // we need to find out that some value was set ("no value" in this case)
            configFolder = ""; // NOI18N
        }
        this.configFolder = configFolder;
    }

    public String getEncoding() {
        if (encoding == null) {
            encoding = getProjectProperty(ClientSideProjectConstants.PROJECT_ENCODING, ClientSideProjectUtilities.DEFAULT_PROJECT_CHARSET.name());
        }
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getStartFile() {
        if (startFile == null) {
            startFile = project.getStartFile();
        }
        return startFile;
    }

    public void setStartFile(String startFile) {
        this.startFile = startFile;
    }

    public String getSelectedBrowser() {
        if (selectedBrowser == null) {
            selectedBrowser = project.getSelectedBrowser();
        }
        return selectedBrowser;
    }

    public void setSelectedBrowser(String selectedBrowser) {
        this.selectedBrowser = selectedBrowser;
    }

    public String getWebRoot() {
        if (webRoot == null) {
            webRoot = project.getWebContextRoot();
        }
        return webRoot;
    }

    public void setWebRoot(String webRoot) {
        this.webRoot = webRoot;
    }

    public String getProjectUrl() {
        if (projectUrl == null) {
            projectUrl = getProjectProperty(ClientSideProjectConstants.PROJECT_PROJECT_URL, ""); // NOI18N
        }
        return projectUrl;
    }

    public void setProjectUrl(String projectUrl) {
        this.projectUrl = projectUrl;
    }

    public ProjectServer getProjectServer() {
        if (projectServer == null) {
            String value = getProjectProperty(ClientSideProjectConstants.PROJECT_SERVER, ProjectServer.INTERNAL.name());
            // toUpperCase() so we are backward compatible, can be later removed
            try {
                projectServer = ProjectServer.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException ex) {
                LOGGER.log(Level.INFO, "Unknown project server type", ex);
                // fallback
                projectServer = ProjectServer.INTERNAL;
            }
        }
        return projectServer;
    }

    public void setProjectServer(ProjectServer projectServer) {
        this.projectServer = projectServer;
    }

    public void setNewJsLibraries(List<SelectedLibrary> newJsLibraries) {
        assert newJsLibraries != null;
        // not needed to be locked, called always by just one caller
        this.newJsLibraries.clear();
        this.newJsLibraries.addAll(newJsLibraries);
    }

    List<SelectedLibrary> getNewJsLibraries() {
        return newJsLibraries;
    }

    public static String createListOfJsLibraries(List<SelectedLibrary> libs) {
        StringBuilder sb = new StringBuilder(200);
        List<SelectedLibrary> selectedLibraries = libs;
        for (SelectedLibrary lib : selectedLibraries) {
            if (lib.isDefault()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("|"); // NOI18N
            }
            JavaScriptLibrarySelectionPanel.LibraryVersion libraryVersion = lib.getLibraryVersion();
            assert libraryVersion != null;
            sb.append(libraryVersion.getLibrary().getName());
        }
        return sb.toString();
    }

    public void setJsLibFolder(String jsLibFolder) {
        assert jsLibFolder != null;
        this.jsLibFolder = jsLibFolder;
    }

    public String getJsLibFolder() {
        return jsLibFolder;
    }

    public File getResolvedSiteRootFolder() {
        File resolvedFile = resolveFile(getSiteRootFolder());
        if (resolvedFile != null) {
            return resolvedFile;
        }
        return FileUtil.toFile(project.getProjectDirectory());
    }

    @CheckForNull
    public File getResolvedStartFile() {
        return resolveFile(getSiteRootFolder() + "/" + getStartFile());
    }

    private String getProjectProperty(String property, String defaultValue) {
        String value = project.getEvaluator().getProperty(property);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }

    private void putProperty(EditableProperties properties, String property, String value) {
        if (value != null) {
            properties.put(property, value);
        }
    }

    private String createForeignFileReference(String filePath) {
        if (filePath == null) {
            // not set at all
            return null;
        }
        if (filePath.isEmpty()) {
            // empty value will be saved
            return ""; // NOI18N
        }
        File file = project.getProjectHelper().resolveFile(filePath);
        return project.getReferenceHelper().createForeignFileReference(file, null);
    }

    @CheckForNull
    private File resolveFile(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        return project.getProjectHelper().resolveFile(path);
    }

    public LicensePanelSupport getLicenseSupport() {
        if (licenseSupport == null) {
            licenseSupport = new LicensePanelSupport(project.getEvaluator(), project.getProjectHelper(),
                getProjectProperty(LicensePanelSupport.LICENSE_PATH, null),
                getProjectProperty(LicensePanelSupport.LICENSE_NAME, null));
        }
        return licenseSupport;
    }

    //~ Inner classes

    @NbBundle.Messages({
        "ProjectServer.internal.title=Embedded Lightweight",
        "ProjectServer.external.title=External"
    })
    public static enum ProjectServer {
        INTERNAL(Bundle.ProjectServer_internal_title()),
        EXTERNAL(Bundle.ProjectServer_external_title());

        private final String title;

        private ProjectServer(String title) {
            assert title != null;
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

    }

}
