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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.ProjectProblems;
import org.netbeans.api.search.SearchRoot;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.api.search.provider.SearchInfoUtils;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.browser.api.BrowserUISupport;
import org.netbeans.modules.web.clientproject.api.ClientSideModule;
import org.netbeans.modules.web.clientproject.api.ProjectDirectoriesProvider;
import org.netbeans.modules.web.clientproject.api.jstesting.CoverageProviderImpl;
import org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProvider;
import org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProviders;
import org.netbeans.modules.web.clientproject.api.platform.PlatformProvider;
import org.netbeans.modules.web.clientproject.api.platform.PlatformProviders;
import org.netbeans.modules.web.clientproject.api.platform.PlatformProvidersListener;
import org.netbeans.modules.web.clientproject.bower.BowerProblemProvider;
import org.netbeans.modules.web.clientproject.node.NpmProblemProvider;
import org.netbeans.modules.web.clientproject.problems.ProjectPropertiesProblemProvider;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectEnhancedBrowserImplementation;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectEnhancedBrowserProvider;
import org.netbeans.modules.web.clientproject.spi.platform.RefreshOnSaveListener;
import org.netbeans.modules.web.clientproject.ui.ClientSideProjectLogicalView;
import org.netbeans.modules.web.clientproject.ui.action.ProjectOperations;
import org.netbeans.modules.web.clientproject.ui.customizer.ClientSideProjectProperties;
import org.netbeans.modules.web.clientproject.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.web.clientproject.util.ClientSideProjectUtilities;
import org.netbeans.modules.web.common.api.CssPreprocessor;
import org.netbeans.modules.web.common.api.CssPreprocessors;
import org.netbeans.modules.web.common.api.CssPreprocessorsListener;
import org.netbeans.modules.web.common.api.UsageLogger;
import org.netbeans.modules.web.common.spi.ProjectWebRootProvider;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.support.ant.AntBasedProjectRegistration;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.netbeans.spi.search.SearchInfoDefinition;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.windows.WindowManager;
import org.openide.windows.WindowSystemEvent;
import org.openide.windows.WindowSystemListener;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

@AntBasedProjectRegistration(
    type=ClientSideProjectType.TYPE,
    iconResource=ClientSideProject.HTML5_PROJECT_ICON,
    sharedNamespace=ClientSideProjectType.PROJECT_CONFIGURATION_NAMESPACE,
    privateNamespace=ClientSideProjectType.PRIVATE_CONFIGURATION_NAMESPACE
)
public class ClientSideProject implements Project {

    static final Logger LOGGER = Logger.getLogger(ClientSideProject.class.getName());

    @StaticResource
    public static final String HTML5_PROJECT_ICON = "org/netbeans/modules/web/clientproject/ui/resources/html5-project.png"; // NOI18N
    @StaticResource
    public static final String JS_LIBRARY_PROJECT_ICON = "org/netbeans/modules/web/clientproject/ui/resources/js-library-project.png"; // NOI18N

    final UsageLogger projectBrowserUsageLogger = UsageLogger.projectBrowserUsageLogger(ClientSideProjectUtilities.USAGE_LOGGER_NAME);

    final AntProjectHelper projectHelper;
    private final ReferenceHelper referenceHelper;
    private final PropertyEvaluator eval;
    private final Lookup lookup;
    private final AntProjectListener antProjectListenerImpl = new AntProjectListenerImpl();
    volatile String name;
    private RefreshOnSaveListener refreshOnSaveListener;
    private ClassPath sourcePath;
    private ClientProjectEnhancedBrowserImplementation projectEnhancedBrowserImpl;
    private WebBrowser projectWebBrowser;
    private ClientSideProjectBrowserProvider projectBrowserProvider;

    // css preprocessors
    final CssPreprocessorsListener cssPreprocessorsListener = new CssPreprocessorsListener() {
        @Override
        public void preprocessorsChanged() {
            // noop?
        }
        @Override
        public void optionsChanged(CssPreprocessor cssPreprocessor) {
            recompileSources(cssPreprocessor);
        }
        @Override
        public void customizerChanged(Project project, CssPreprocessor cssPreprocessor) {
            if (project.equals(ClientSideProject.this)) {
                recompileSources(cssPreprocessor);
            }
        }
        @Override
        public void processingErrorOccured(Project project, CssPreprocessor cssPreprocessor, String error) {
            // noop
        }
    };

    final PlatformProvidersListener platformProvidersListener = new PlatformProvidersListener() {

        @Override
        public void platformProvidersChanged() {
            // noop
        }

        @Override
        public void propertyChanged(Project project, PlatformProvider platformProvider, PropertyChangeEvent event) {
            if (ClientSideProject.this.equals(project)) {
                if (PlatformProvider.PROP_ENABLED.equals(event.getPropertyName())) {
                    Info info = getLookup().lookup(Info.class);
                    assert info != null;
                    info.firePropertyChange(ProjectInformation.PROP_ICON);
                }
            }
        }
    };

    // #233052
    private final WindowSystemListener windowSystemListener = new WindowSystemListener() {

        @Override
        public void beforeLoad(WindowSystemEvent event) {
        }

        @Override
        public void afterLoad(WindowSystemEvent event) {
        }

        @Override
        public void beforeSave(WindowSystemEvent event) {
            // browser
            ClientProjectEnhancedBrowserImplementation enhancedBrowserImpl = getEnhancedBrowserImpl();
            if (enhancedBrowserImpl != null) {
                enhancedBrowserImpl.close();
            }
        }

        @Override
        public void afterSave(WindowSystemEvent event) {
        }

    };


    public ClientSideProject(AntProjectHelper helper) {
        this.projectHelper = helper;
        AuxiliaryConfiguration configuration = helper.createAuxiliaryConfiguration();
        eval = createEvaluator();
        referenceHelper = new ReferenceHelper(helper, configuration, eval);
        projectBrowserProvider = new ClientSideProjectBrowserProvider(this);
        lookup = createLookup(configuration);
        eval.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (ClientSideProjectConstants.PROJECT_SELECTED_BROWSER.equals(evt.getPropertyName())) {
                    projectBrowserUsageLogger.reset();
                    refreshOnSaveListener = null;
                    ClientProjectEnhancedBrowserImplementation ebi = projectEnhancedBrowserImpl;
                    if (ebi != null) {
                        ebi.deactivate();
                    }
                    projectEnhancedBrowserImpl = null;
                    projectWebBrowser = null;
                    projectBrowserProvider.activeBrowserHasChanged();
                }
            }
        });
        projectHelper.addAntProjectListener(WeakListeners.create(AntProjectListener.class, antProjectListenerImpl, projectHelper));
        WindowManager windowManager = WindowManager.getDefault();
        windowManager.addWindowSystemListener(WeakListeners.create(WindowSystemListener.class, windowSystemListener, windowManager));
    }

    public void logBrowserUsage() {
        WebBrowser webBrowser = getProjectWebBrowser();
        projectBrowserUsageLogger.log(ClientSideProjectType.TYPE, webBrowser.getId(), webBrowser.getBrowserFamily().name());
    }

    public synchronized ClientProjectEnhancedBrowserImplementation getEnhancedBrowserImpl() {
        if (projectEnhancedBrowserImpl == null) {
            projectEnhancedBrowserImpl = createEnhancedBrowserImpl(this, getProjectWebBrowser());
        }
        return projectEnhancedBrowserImpl;
    }

    public static ClientProjectEnhancedBrowserImplementation createEnhancedBrowserImpl(Project p, WebBrowser wb) {
        for (ClientProjectEnhancedBrowserProvider provider : p.getLookup().lookupAll(ClientProjectEnhancedBrowserProvider.class)) {
            ClientProjectEnhancedBrowserImplementation impl = provider.getEnhancedBrowser(wb);
            if (impl != null) {
                return impl;
            }
        }
        return null;
    }

    public synchronized WebBrowser getProjectWebBrowser() {
        if (projectWebBrowser == null) {
            String id = getSelectedBrowser();
            if (id != null) {
                projectWebBrowser = BrowserUISupport.getBrowser(id);
            }
            if (projectWebBrowser == null) {
                projectWebBrowser = BrowserUISupport.getDefaultBrowserChoice(false);
            }
        }
        return projectWebBrowser;
    }

    private RefreshOnSaveListener getRefreshOnSaveListener() {
        ClientProjectEnhancedBrowserImplementation ebi = getEnhancedBrowserImpl();
        if (ebi != null) {
            return ebi.getRefreshOnSaveListener();
        } else {
            return null;
        }
    }

    public boolean isUsingEmbeddedServer() {
        // equalsIgnoreCase for backward compatibility, can be removed later
        return !ClientSideProjectProperties.ProjectServer.EXTERNAL.name().equalsIgnoreCase(getEvaluator().getProperty(ClientSideProjectConstants.PROJECT_SERVER));
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "ClientSideProject.error.broken=<html>Project <b>{0}</b> is broken, resolve project problems first."
    })
    public boolean isBroken(boolean showCustomizer) {
        boolean broken = getSourcesFolder() == null
                && getSiteRootFolder() == null;
        if (broken
                && showCustomizer) {
            NotifyDescriptor descriptor = new NotifyDescriptor.Message(
                    Bundle.ClientSideProject_error_broken(getName()), NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(descriptor);
            ProjectProblems.showCustomizer(this);
        }
        return broken;
    }

    public boolean isJsLibrary() {
        return getEvaluator().getProperty(ClientSideProjectConstants.PROJECT_SITE_ROOT_FOLDER) == null;
    }

    public boolean isHtml5Project() {
        return !isJsLibrary();
    }

    @CheckForNull
    public FileObject getSourcesFolder() {
        String sourceFolder = getEvaluator().getProperty(ClientSideProjectConstants.PROJECT_SOURCE_FOLDER);
        if (sourceFolder == null) {
            return null;
        }
        if (sourceFolder.isEmpty()) {
            return getProjectDirectory();
        }
        return projectHelper.resolveFileObject(sourceFolder);
    }

    @CheckForNull
    public FileObject getSiteRootFolder() {
        String siteRootFolder = getEvaluator().getProperty(ClientSideProjectConstants.PROJECT_SITE_ROOT_FOLDER);
        if (siteRootFolder == null) {
            return null;
        }
        if (siteRootFolder.isEmpty()) {
            return getProjectDirectory();
        }
        return projectHelper.resolveFileObject(siteRootFolder);
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "ClientSideProject.chooser.tests.title=Select Unit Tests folder ({0})",
        "ClientSideProject.props.saving=Saving project metadata...",
    })
    @CheckForNull
    public FileObject getTestsFolder(boolean showFileChooser) {
        String tests = getEvaluator().getProperty(ClientSideProjectConstants.PROJECT_TEST_FOLDER);
        if (tests == null || tests.trim().length() == 0) {
            if (showFileChooser) {
                final File folder = new FileChooserBuilder(ClientSideProject.class)
                        .setTitle(Bundle.ClientSideProject_chooser_tests_title(ProjectUtils.getInformation(this).getDisplayName()))
                        .setDirectoriesOnly(true)
                        .setDefaultWorkingDirectory(FileUtil.toFile(getProjectDirectory()))
                        .forceUseOfDefaultWorkingDirectory(true)
                        .showOpenDialog();
                if (folder != null) {
                    ProgressUtils.runOffEventDispatchThread(new Runnable() {
                        @Override
                        public void run() {
                            ClientSideProjectProperties projectProperties = new ClientSideProjectProperties(ClientSideProject.this);
                            projectProperties.setTestFolder(folder.getAbsolutePath());
                            projectProperties.save();
                        }
                    }, Bundle.ClientSideProject_props_saving(), new AtomicBoolean(), false);
                    FileObject fo = FileUtil.toFileObject(folder);
                    assert fo != null : "FileObject should be found for " + folder;
                    return fo;
                }
            }
            return null;
        }
        return getProjectDirectory().getFileObject(tests);
    }

    public String getStartFile() {
        String s = getEvaluator().getProperty(ClientSideProjectConstants.PROJECT_START_FILE);
        if (s == null) {
            s = "index.html"; //NOI18N
        }
        return s;
    }

    public String getSelectedBrowser() {
        String s = getEvaluator().getProperty(ClientSideProjectConstants.PROJECT_SELECTED_BROWSER);
        return s;
    }

    public String getWebContextRoot() {
        String ctx = getEvaluator().getProperty(ClientSideProjectConstants.PROJECT_WEB_ROOT);
        if (ctx == null) {
            ctx = "/"+getProjectDirectory().getName(); //NOI18N
        }
        if (!ctx.startsWith("/")) { //NOI18N
            ctx = "/" + ctx; //NOI18N
        }
        return ctx;
    }

    public AntProjectHelper getProjectHelper() {
        return projectHelper;
    }

    @Override
    public FileObject getProjectDirectory() {
        return getProjectHelper().getProjectDirectory();
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    public PropertyEvaluator getEvaluator() {
        return eval;
    }

    public ReferenceHelper getReferenceHelper() {
        return referenceHelper;
    }

    @CheckForNull
    public JsTestingProvider getJsTestingProvider(boolean showSelectionPanel) {
        return JsTestingProviders.getDefault().getJsTestingProvider(this, showSelectionPanel);
    }

    public List<PlatformProvider> getPlatformProviders() {
        List<PlatformProvider> allProviders = PlatformProviders.getDefault().getPlatformProviders();
        List<PlatformProvider> enabledProviders = new ArrayList<>(allProviders.size());
        for (PlatformProvider provider : allProviders) {
            if (provider.isEnabled(this)) {
                enabledProviders.add(provider);
            }
        }
        return enabledProviders;
    }

    public String getName() {
        if (name == null) {
            ProjectManager.mutex().readAccess(new Mutex.Action<Void>() {
                @Override
                public Void run() {
                    Element data = projectHelper.getPrimaryConfigurationData(true);
                    NodeList nameList = data.getElementsByTagNameNS(ClientSideProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                    if (nameList.getLength() == 1) {
                        nameList = nameList.item(0).getChildNodes();
                        if (nameList.getLength() == 1
                                && nameList.item(0).getNodeType() == Node.TEXT_NODE) {
                            name = ((Text) nameList.item(0)).getNodeValue();
                        }
                    }
                    if (name == null) {
                        name = getProjectDirectory().getNameExt();
                    }
                    return null;
                }
            });
        }
        assert name != null;
        return name;
    }

    public void setName(String name) {
        ClientSideProjectUtilities.setProjectName(projectHelper, name);
    }


    private PropertyEvaluator createEvaluator() {
        PropertyEvaluator baseEval2 = PropertyUtils.sequentialPropertyEvaluator(
                projectHelper.getStockPropertyPreprovider(),
                projectHelper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH));
        return PropertyUtils.sequentialPropertyEvaluator(
                projectHelper.getStockPropertyPreprovider(),
                projectHelper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH),
                PropertyUtils.userPropertiesProvider(baseEval2,
                    "user.properties.file", FileUtil.toFile(getProjectDirectory())), // NOI18N
                projectHelper.getPropertyProvider(AntProjectHelper.PROJECT_PROPERTIES_PATH));
    }

    private Lookup createLookup(AuxiliaryConfiguration configuration) {
        FileEncodingQueryImplementation fileEncodingQuery =
                new FileEncodingQueryImpl(getEvaluator(), ClientSideProjectConstants.PROJECT_ENCODING);
        Lookup base = Lookups.fixed(new Object[] {
               this,
               new Info(),
               new ClientSideProjectXmlSavedHook(),
               new ProjectOperations(this),
               ProjectSearchInfo.create(this),
               fileEncodingQuery,
               new ServerURLMappingImpl(this),
               configuration,
               projectHelper.createCacheDirectoryProvider(),
               projectHelper.createAuxiliaryProperties(),
               getEvaluator(),
               new ClientSideProjectLogicalView(this),
               new RecommendedAndPrivilegedTemplatesImpl(),
               new ClientSideProjectActionProvider(this),
               new OpenHookImpl(this),
               new CustomizerProviderImpl(this),
               //getBrowserSupport(),
               new ClassPathProviderImpl(this),
               new PageInspectorCustomizerImpl(this),
               new ProjectWebRootProviderImpl(),
               new ClientSideProjectSources(this, projectHelper, eval),
               new ClientSideModuleImpl(this),
               ProjectPropertiesProblemProvider.createForProject(this),
               CssPreprocessors.getDefault().createProjectProblemsProvider(this),
               NpmProblemProvider.create(this),
               BowerProblemProvider.create(this),
               UILookupMergerSupport.createProjectProblemsProviderMerger(),
               new TemplateAttributesProviderImpl(projectHelper, fileEncodingQuery),
               SharabilityQueryImpl.create(projectHelper, eval, ClientSideProjectConstants.PROJECT_SITE_ROOT_FOLDER,
                    ClientSideProjectConstants.PROJECT_TEST_FOLDER),
               projectBrowserProvider,
               new ProjectDirectoriesProviderImpl(),
               new CoverageProviderImpl(this),
       });
       return LookupProviderSupport.createCompositeLookup(base, "Projects/org-netbeans-modules-web-clientproject/Lookup");
    }

    void recompileSources(CssPreprocessor cssPreprocessor) {
        assert cssPreprocessor != null;
        FileObject siteRootFolder = getSiteRootFolder();
        if (siteRootFolder == null) {
            return;
        }
        // force recompiling
        CssPreprocessors.getDefault().process(cssPreprocessor, this, siteRootFolder);
    }

    ClassPath getSourceClassPath() {
        if (sourcePath == null) {
            sourcePath = ClassPathProviderImpl.createProjectClasspath(this);
        }
        return sourcePath;
    }

    private final class Info implements ProjectInformation {

        private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);


        @Override
        public String getName() {
            return PropertyUtils.getUsablePropertyName(getDisplayName());
        }

        @Override
        public String getDisplayName() {
            return ClientSideProject.this.getName();
        }

        @Override
        public Icon getIcon() {
            String icon = isJsLibrary() ? ClientSideProject.JS_LIBRARY_PROJECT_ICON : ClientSideProject.HTML5_PROJECT_ICON;
            return new ImageIcon(ImageUtilities.loadImage(icon));
        }

        @Override
        public Project getProject() {
            return ClientSideProject.this;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            propertyChangeSupport.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            propertyChangeSupport.removePropertyChangeListener(listener);
        }

        void firePropertyChange(String prop) {
            propertyChangeSupport.firePropertyChange(prop , null, null);
        }

    }

    private final class ClientSideProjectXmlSavedHook extends ProjectXmlSavedHook {

        @Override
        protected void projectXmlSaved() throws IOException {
            Info info = getLookup().lookup(Info.class);
            assert info != null;
            info.firePropertyChange(ProjectInformation.PROP_NAME);
            info.firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME);
        }
    }

    private static final class RecommendedAndPrivilegedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates {

        @Override
        public String[] getRecommendedTypes() {
            return new String[] {
                "html5",     // NOI18N
                "html5-test",     // NOI18N
                "XML",                  // NOI18N
                "simple-files"          // NOI18N
            };
        }

        @Override
        public String[] getPrivilegedTemplates() {
            return new String[] {
                "Templates/ClientSide/html.html",            // NOI18N
                "Templates/ClientSide/javascript.js",            // NOI18N
                "Templates/ClientSide/css.css",            // NOI18N
                "Templates/ClientSide/style.scss",            // NOI18N
                "Templates/ClientSide/style.less",            // NOI18N
                "Templates/ClientSide/json.json",            // NOI18N
                "Templates/Other/org-netbeans-modules-project-ui-NewFileIterator-folderIterator", // NOI18N
            };
        }

    }

    private static class OpenHookImpl extends ProjectOpenedHook implements PropertyChangeListener {

        private final ClientSideProject project;
        private FileChangeListener siteRootChangesListener;

        // @GuardedBy("this")
        private File siteRootFolder;


        public OpenHookImpl(ClientSideProject project) {
            this.project = project;
        }

        @Override
        protected void projectOpened() {
            project.getEvaluator().addPropertyChangeListener(this);
            addSiteRootListener();
            GlobalPathRegistry.getDefault().register(ClassPathProviderImpl.SOURCE_CP, new ClassPath[]{project.getSourceClassPath()});
            String browserId = "";
            WebBrowser wb = project.getProjectWebBrowser();
            if (wb != null) {
                browserId = wb.getId();
            }
            CssPreprocessors.getDefault().addCssPreprocessorsListener(project.cssPreprocessorsListener);
            JsTestingProvider jsTestingProvider = project.getJsTestingProvider(false);
            if (jsTestingProvider != null) {
                jsTestingProvider.projectOpened(project);
            }
            PlatformProviders.getDefault().addPlatformProvidersListener(project.platformProvidersListener);
            PlatformProviders.getDefault().projectOpened(project);
            FileObject projectDirectory = project.getProjectDirectory();
            // usage logging
            FileObject cordova = projectDirectory.getFileObject(".cordova"); // NOI18N
            if (cordova == null) {
                cordova = projectDirectory.getFileObject("hooks"); // NOI18N
            }
            FileObject testsFolder = project.getTestsFolder(false);

            boolean hasGrunt = projectDirectory.getFileObject("Gruntfile.js") != null;
            boolean hasBower = projectDirectory.getFileObject("bower.json") !=null;
            boolean hasPackage = projectDirectory.getFileObject("package.json") !=null;
            ClientSideProjectUtilities.logUsage(ClientSideProject.class, "USG_PROJECT_HTML5_OPEN", // NOI18N
                    new Object[] {
                        browserId,
                        testsFolder != null && testsFolder.getChildren().length > 0 ? "YES" : "NO", // NOI18N
                        cordova != null && cordova.isFolder() ? "YES" : "NO", // NOI18N
                        hasGrunt ? "YES" : "NO", // NOI18N
                        hasBower ? "YES" : "NO", // NOI18N
                        hasPackage ? "YES" : "NO", // NOI18N
                    });
        }

        @Override
        protected void projectClosed() {
            project.getEvaluator().removePropertyChangeListener(this);
            removeSiteRootListener();
            GlobalPathRegistry.getDefault().unregister(ClassPathProviderImpl.SOURCE_CP, new ClassPath[]{project.getSourceClassPath()});
            CssPreprocessors.getDefault().removeCssPreprocessorsListener(project.cssPreprocessorsListener);
            JsTestingProvider jsTestingProvider = project.getJsTestingProvider(false);
            if (jsTestingProvider != null) {
                jsTestingProvider.projectClosed(project);
            }
            PlatformProviders.getDefault().projectClosed(project);
            PlatformProviders.getDefault().removePlatformProvidersListener(project.platformProvidersListener);
            // browser
            ClientProjectEnhancedBrowserImplementation enhancedBrowserImpl = project.getEnhancedBrowserImpl();
            if (enhancedBrowserImpl != null) {
                enhancedBrowserImpl.close();
            }
        }

        private synchronized void addSiteRootListener() {
            assert siteRootFolder == null : "Should not be listening to " + siteRootFolder;
            FileObject siteRoot = project.getSiteRootFolder();
            if (siteRoot == null) {
                return;
            }
            siteRootFolder = FileUtil.toFile(siteRoot);
            if (siteRootFolder == null) {
                // should not happen
                LOGGER.log(Level.WARNING, "File not found for FileObject: {0}", siteRoot);
                return;
            }
            siteRootChangesListener = new SiteRootFolderListener(project);
            FileUtil.addRecursiveListener(siteRootChangesListener, siteRootFolder);
        }

        private synchronized void removeSiteRootListener() {
            if (siteRootFolder == null) {
                // no listener
                return;
            }
            try {
                FileUtil.removeRecursiveListener(siteRootChangesListener, siteRootFolder);
            } catch (IllegalArgumentException ex) {
                // #216349
                LOGGER.log(Level.INFO, null, ex);
            }
            siteRootFolder = null;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            // change in project properties
            if (ClientSideProjectConstants.PROJECT_SITE_ROOT_FOLDER.equals(evt.getPropertyName())) {
                synchronized (this) {
                    removeSiteRootListener();
                    addSiteRootListener();
                }
            }
        }

    }

    private static class SiteRootFolderListener implements FileChangeListener {

        private final ClientSideProject p;
        private final FileObject siteRootFolder;;

        SiteRootFolderListener(ClientSideProject p) {
            this.p = p;
            siteRootFolder = p.getSiteRootFolder();
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            checkPreprocessors(fe.getFile());
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            checkPreprocessors(fe.getFile());
        }

        @Override
        public void fileChanged(FileEvent fe) {
            RefreshOnSaveListener r = p.getRefreshOnSaveListener();
            if (r != null) {
                r.fileChanged(fe.getFile());
            }
            checkPreprocessors(fe.getFile());
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            RefreshOnSaveListener r = p.getRefreshOnSaveListener();
            if (r != null) {
                r.fileDeleted(fe.getFile());
            }
            checkPreprocessors(fe.getFile());
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            // XXX: notify BrowserReload about filename change
            checkPreprocessors(fe.getFile(), fe.getName(), fe.getExt());

            if (fe.getFile().equals(siteRootFolder)) {
                final ClientSideProjectProperties projectProperties = new ClientSideProjectProperties(p);
                projectProperties.setSiteRootFolder(siteRootFolder.getNameExt());
                projectProperties.save();
            }

        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }

        private void checkPreprocessors(FileObject fileObject) {
            CssPreprocessors.getDefault().process(p, fileObject);
        }

        private void checkPreprocessors(FileObject fileObject, String originalName, String originalExtension) {
            CssPreprocessors.getDefault().process(p, fileObject, originalName, originalExtension);
        }

    }

    private final class ProjectWebRootProviderImpl implements ProjectWebRootProvider {

        @Override
        public FileObject getWebRoot(FileObject file) {
            FileObject siteRoot = getSiteRootFolder();
            if (siteRoot == null) {
                return null;
            }
            if (siteRoot.equals(file)
                    || FileUtil.isParentOf(siteRoot, file)) {
                return siteRoot;
            }
            return null;
        }

        @Override
        public Collection<FileObject> getWebRoots() {
            FileObject siteRoot = getSiteRootFolder();
            if (siteRoot == null) {
                return Collections.emptyList();
            }
            return Collections.singleton(siteRoot);
        }

    }

    private static final class ProjectSearchInfo extends SearchInfoDefinition {

        private static final Set<String> WATCHED_PROPERTIES = new HashSet<String>(Arrays.asList(
                ClientSideProjectConstants.PROJECT_SITE_ROOT_FOLDER,
                ClientSideProjectConstants.PROJECT_TEST_FOLDER));

        private final ClientSideProject project;
        // @GuardedBy("this")
        private SearchInfo delegate = null;


        public ProjectSearchInfo(ClientSideProject project) {
            this.project = project;
        }

        public static SearchInfoDefinition create(ClientSideProject project) {
            final ProjectSearchInfo searchInfo = new ProjectSearchInfo(project);
            project.getEvaluator().addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (WATCHED_PROPERTIES.contains(evt.getPropertyName())) {
                        searchInfo.resetDelegate();
                    }
                }
            });
            return searchInfo;
        }

        @Override
        public boolean canSearch() {
            return true;
        }

        @Override
        public Iterator<FileObject> filesToSearch(SearchScopeOptions options, SearchListener listener, AtomicBoolean terminated) {
            return getDelegate().getFilesToSearch(options, listener, terminated).iterator();
        }

        @Override
        public List<SearchRoot> getSearchRoots() {
            return getDelegate().getSearchRoots();
        }

        private synchronized SearchInfo getDelegate() {
            assert Thread.holdsLock(this);
            if (delegate == null) {
                delegate = createDelegate();
            }
            return delegate;
        }

        private SearchInfo createDelegate() {
            return SearchInfoUtils.createSearchInfoForRoots(getRoots(), true);
        }

        synchronized void resetDelegate() {
            assert Thread.holdsLock(this);
            delegate = null;
        }

        private FileObject[] getRoots() {
            List<FileObject> roots = new ArrayList<FileObject>();
            addRoots(roots, project.getSiteRootFolder(), project.getTestsFolder(false));
            return roots.toArray(new FileObject[roots.size()]);
        }

        private void addRoots(List<FileObject> result, FileObject... roots) {
            for (FileObject root : roots) {
                if (root != null) {
                    result.add(root);
                }
            }
        }

    }

    private static final class ClientSideModuleImpl implements ClientSideModule {

        private final ClientSideProject project;


        public ClientSideModuleImpl(ClientSideProject project) {
            this.project = project;
        }

        @Override
        public Properties getProperties() {
            return new PropertiesImpl();
        }

        private final class PropertiesImpl implements ClientSideModule.Properties {

            @Override
            public FileObject getStartFile() {
                File startFile = getProjectProperties().getResolvedStartFile();
                if (startFile == null) {
                    return null;
                }
                return FileUtil.toFileObject(startFile);
            }

            @Override
            public String getWebContextRoot() {
                return getProjectProperties().getWebRoot();
            }

            private ClientSideProjectProperties getProjectProperties() {
                return new ClientSideProjectProperties(project);
            }

        }

    }

    private final class AntProjectListenerImpl implements AntProjectListener {

        @Override
        public void configurationXmlChanged(AntProjectEvent ev) {
            name = null;
        }

        @Override
        public void propertiesChanged(AntProjectEvent ev) {
        }

    }

    private final class ProjectDirectoriesProviderImpl implements ProjectDirectoriesProvider {

        @Override
        public FileObject getTestDirectory(boolean showFileChooser) {
            return ClientSideProject.this.getTestsFolder(showFileChooser);
        }

    }

}
