/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.clientproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.j2ee.api.ejbjar.Car;
import org.netbeans.modules.j2ee.clientproject.classpath.AppClientProjectClassPathExtender;
import org.netbeans.modules.j2ee.clientproject.classpath.ClassPathProviderImpl;
import org.netbeans.modules.j2ee.clientproject.classpath.ClassPathSupport;
import org.netbeans.modules.j2ee.clientproject.queries.AppClientProjectEncodingQueryImpl;
import org.netbeans.modules.j2ee.clientproject.queries.CompiledSourceForBinaryQuery;
import org.netbeans.modules.j2ee.clientproject.queries.JavadocForBinaryQueryImpl;
import org.netbeans.modules.j2ee.clientproject.queries.SourceLevelQueryImpl;
import org.netbeans.modules.j2ee.clientproject.queries.UnitTestForSourceQueryImpl;
import org.netbeans.modules.j2ee.clientproject.ui.AppClientLogicalViewProvider;
import org.netbeans.modules.j2ee.clientproject.ui.customizer.AppClientProjectProperties;
import org.netbeans.modules.j2ee.clientproject.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.j2ee.clientproject.wsclient.AppClientProjectJAXWSClientSupport;
import org.netbeans.modules.j2ee.clientproject.wsclient.AppClientProjectJAXWSVersionProvider;
import org.netbeans.modules.j2ee.clientproject.wsclient.AppClientProjectWebServicesClientSupport;
import org.netbeans.modules.j2ee.clientproject.wsclient.AppClientProjectWebServicesSupportProvider;
import org.netbeans.modules.j2ee.common.ui.BrokenServerSupport;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.spi.ejbjar.CarFactory;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.spi.client.WebServicesClientSupportFactory;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientSupportFactory;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.ant.AntBuildExtenderFactory;
import org.netbeans.spi.project.ant.AntBuildExtenderImplementation;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Represents one plain Application Client project.
 * @author Jesse Glick, et al.
 */
public final class AppClientProject implements Project, AntProjectListener, FileChangeListener {
    
    private static final Icon CAR_PROJECT_ICON = new ImageIcon(Utilities.loadImage("org/netbeans/modules/j2ee/clientproject/ui/resources/appclient.gif")); // NOI18N
    
    private final AuxiliaryConfiguration aux;
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final ReferenceHelper refHelper;
    private final GeneratedFilesHelper genFilesHelper;
    private final Lookup lookup;
    private final UpdateHelper updateHelper;
    private MainClassUpdater mainClassUpdater;
    private SourceRoots sourceRoots;
    private SourceRoots testRoots;
    
    // WS client support
    private AppClientProjectWebServicesClientSupport carProjectWebServicesClientSupport;
    private AppClientProjectJAXWSClientSupport jaxWsClientSupport;
    private WebServicesClientSupport apiWebServicesClientSupport;
    private JAXWSClientSupport apiJAXWSClientSupport;
    
    private PropertyChangeListener j2eePlatformListener;
    private final AppClientProvider appClient;
    private final Car apiJar;
    private JarContainerImpl enterpriseResourceSupport;
    private FileObject libFolder;
    private final AppClientProjectClassPathExtender classpathExtender; 
    
    // use AntBuildExtender to enable Ant Extensibility
    private AntBuildExtender buildExtender;
    
    AppClientProject(AntProjectHelper helper) throws IOException {
        this.helper = helper;
        eval = createEvaluator();
        aux = helper.createAuxiliaryConfiguration();
        refHelper = new ReferenceHelper(helper, aux, eval);
        buildExtender = AntBuildExtenderFactory.createAntExtender(new AppClientExtenderImplementation());
        genFilesHelper = new GeneratedFilesHelper(helper, buildExtender);
        this.updateHelper = new UpdateHelper(this, this.helper, this.aux, this.genFilesHelper,
                UpdateHelper.createDefaultNotifier());
        carProjectWebServicesClientSupport = new AppClientProjectWebServicesClientSupport(this, helper, refHelper);
        jaxWsClientSupport = new AppClientProjectJAXWSClientSupport(this, helper);
        apiWebServicesClientSupport = WebServicesClientSupportFactory.createWebServicesClientSupport(carProjectWebServicesClientSupport);
        apiJAXWSClientSupport = JAXWSClientSupportFactory.createJAXWSClientSupport(jaxWsClientSupport);
        ClassPathProviderImpl cpProvider = new ClassPathProviderImpl(this.helper, evaluator(), getSourceRoots(),getTestSourceRoots());
        appClient = new AppClientProvider(this, helper, cpProvider);
        apiJar = CarFactory.createCar(appClient);
        enterpriseResourceSupport = new JarContainerImpl(this, refHelper, helper);
        classpathExtender = new AppClientProjectClassPathExtender(this, updateHelper, evaluator(), refHelper);
        lookup = createLookup(aux, cpProvider);
        helper.addAntProjectListener(this);
    }
    
    /**
     * Returns the project directory
     * @return the directory the project is located in
     */
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }
    
    @Override
    public String toString() {
        return "CarProject[" + getProjectDirectory() + "]"; // NOI18N
    }
    
    private PropertyEvaluator createEvaluator() {
        // XXX might need to use a custom evaluator to handle active platform substitutions... TBD
        // It is currently safe to not use the UpdateHelper for PropertyEvaluator; UH.getProperties() delegates to APH
        return helper.getStandardPropertyEvaluator();
    }
    
    public PropertyEvaluator evaluator() {
        return eval;
    }
    
    public ReferenceHelper getReferenceHelper() {
        return this.refHelper;
    }
    
    public UpdateHelper getUpdateHelper() {
        return this.updateHelper;
    }
    
    public Lookup getLookup() {
        return lookup;
    }
    
    public AntProjectHelper getAntProjectHelper() {
        return helper;
    }
    
    private Lookup createLookup(AuxiliaryConfiguration aux, ClassPathProviderImpl cpProvider) {
        SubprojectProvider spp = refHelper.createSubprojectProvider();

        final SourcesHelper sourcesHelper = new SourcesHelper(helper, evaluator());
        String configFilesLabel = NbBundle.getMessage(AppClientLogicalViewProvider.class, "LBL_Node_ConfFiles"); //NOI18N
        
        //sourcesHelper.addPrincipalSourceRoot("${"+AppClientProjectProperties.SOURCE_ROOT+"}", ejbModuleLabel, /*XXX*/null, null);
        sourcesHelper.addPrincipalSourceRoot("${"+AppClientProjectProperties.META_INF+"}", configFilesLabel, /*XXX*/null, null); // NOI18N
        
        ProjectManager.mutex().postWriteRequest(new Runnable() {
            public void run() {
                sourcesHelper.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
            }
        });
        Lookup base = Lookups.fixed(new Object[] {
            new Info(),
            aux,
            helper.createCacheDirectoryProvider(),
            spp,
            new AppClientActionProvider( this, helper, this.updateHelper ),
            new AppClientLogicalViewProvider(this, this.updateHelper, evaluator(), refHelper),
            // new J2SECustomizerProvider(this, this.updateHelper, evaluator(), refHelper),
            new CustomizerProviderImpl(this, this.updateHelper, evaluator(), refHelper, this.genFilesHelper),
            cpProvider,
            new CompiledSourceForBinaryQuery(this.helper, evaluator(),getSourceRoots(),getTestSourceRoots()), //Does not use APH to get/put properties/cfgdata
            new JavadocForBinaryQueryImpl(this.helper, evaluator()), //Does not use APH to get/put properties/cfgdata
            new AntArtifactProviderImpl(),
            new ProjectXmlSavedHookImpl(),
            UILookupMergerSupport.createProjectOpenHookMerger(new ProjectOpenedHookImpl()),
            new UnitTestForSourceQueryImpl(getSourceRoots(),getTestSourceRoots()),
            new SourceLevelQueryImpl(evaluator()),
            new AppClientSources(this.helper, evaluator(), getSourceRoots(), getTestSourceRoots()),
            new AppClientSharabilityQuery(this.helper, evaluator(), getSourceRoots(), getTestSourceRoots()), //Does not use APH to get/put properties/cfgdata
            new AppClientFileBuiltQuery(this.helper, evaluator(),getSourceRoots(),getTestSourceRoots()), //Does not use APH to get/put properties/cfgdata
            new AppClientProjectEncodingQueryImpl(evaluator()), 
            new RecommendedTemplatesImpl(this.updateHelper),
            classpathExtender,
            buildExtender,
            AppClientProject.this, // never cast an externally obtained Project to AppClientProject - use lookup instead
            new AppClientProjectOperations(this),
            new AppClientProjectWebServicesSupportProvider(),
            
            new ProjectAppClientProvider(this),
            appClient,
            new AppClientPersistenceProvider(this, evaluator(), cpProvider),
            new AppClientProjectJAXWSVersionProvider(helper),
            enterpriseResourceSupport,
            UILookupMergerSupport.createPrivilegedTemplatesMerger(),
            UILookupMergerSupport.createRecommendedTemplatesMerger(),
            LookupProviderSupport.createSourcesMerger()
        });
        return LookupProviderSupport.createCompositeLookup(base, "Projects/org-netbeans-modules-j2ee-clientproject/Lookup"); //NOI18N
    }
    
    public void configurationXmlChanged(AntProjectEvent ev) {
        if (ev.getPath().equals(AntProjectHelper.PROJECT_XML_PATH)) {
            // Could be various kinds of changes, but name & displayName might have changed.
            Info info = (Info)getLookup().lookup(ProjectInformation.class);
            info.firePropertyChange(ProjectInformation.PROP_NAME);
            info.firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME);
        }
    }
    
    public void propertiesChanged(AntProjectEvent ev) {
        // currently ignored (probably better to listen to evaluator() if you need to)
    }
    
    // Package private methods -------------------------------------------------
    
    /**
     * Returns the source roots of this project
     * @return project's source roots
     */
    public synchronized SourceRoots getSourceRoots() {
        if (this.sourceRoots == null) { //Local caching, no project metadata access
            this.sourceRoots = new SourceRoots(this.updateHelper, evaluator(), getReferenceHelper(), "source-roots", false, "src.{0}{1}.dir"); //NOI18N
        }
        return this.sourceRoots;
    }
    
    public synchronized SourceRoots getTestSourceRoots() {
        if (this.testRoots == null) { //Local caching, no project metadata access
            this.testRoots = new SourceRoots(this.updateHelper, evaluator(), getReferenceHelper(), "test-roots", true, "test.{0}{1}.dir"); //NOI18N
        }
        return this.testRoots;
    }
    
    File getTestClassesDirectory() {
        String testClassesDir = evaluator().getProperty(AppClientProjectProperties.BUILD_TEST_CLASSES_DIR);
        if (testClassesDir == null) {
            return null;
        }
        return helper.resolveFile(testClassesDir);
    }
    
    // Currently unused (but see #47230):
    /** Store configured project name. */
    public void setName(final String name) {
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                Element data = helper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                    data.insertBefore(nameEl, /* OK if null */data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                helper.putPrimaryConfigurationData(data, true);
            }
        });
    }
    
    /** Return configured project name. */
    public String getName() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<String>() {
            public String run() {
                Element data = updateHelper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                if (nl.getLength() == 1) {
                    nl = nl.item(0).getChildNodes();
                    if (nl.getLength() == 1 && nl.item(0).getNodeType() == Node.TEXT_NODE) {
                        return ((Text) nl.item(0)).getNodeValue();
                    }
                }
                return "CAR???"; // NOI18N
            }
        });
    }

    public void fileAttributeChanged (FileAttributeEvent fe) {
    }    
    
    public void fileChanged (FileEvent fe) {
    }
    
    public void fileDataCreated (FileEvent fe) {
        FileObject fo = fe.getFile ();
        checkLibraryFolder (fo);
    }
    
    public void fileDeleted (FileEvent fe) {
    }
    
    public void fileFolderCreated (FileEvent fe) {
    }
    
    public void fileRenamed (FileRenameEvent fe) {
        FileObject fo = fe.getFile ();
        checkLibraryFolder (fo);
    }

    private void checkLibraryFolder (FileObject fo) {
        if (!FileUtil.isArchiveFile(fo)) {
            return;
        }
        
        if (fo.getParent ().equals (libFolder)) {
            try {
                classpathExtender.addArchiveFile(fo);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }
    
    public void registerJ2eePlatformListener(final J2eePlatform platform) {
        // listen to classpath changes
        j2eePlatformListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(J2eePlatform.PROP_CLASSPATH)) {
                    ProjectManager.mutex().writeAccess(new Runnable() {
                        public void run() {
                            EditableProperties ep = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                            String classpath = Utils.toClasspathString(platform.getClasspathEntries());
                            ep.setProperty(AppClientProjectProperties.J2EE_PLATFORM_CLASSPATH, classpath);
                            helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                            try {
                                ProjectManager.getDefault().saveProject(AppClientProject.this);
                            } catch (IOException e) {
                                Exceptions.printStackTrace(e);
                            }
                        }
                    });
                }
            }
        };
        platform.addPropertyChangeListener(j2eePlatformListener);
    }
    
    public void unregisterJ2eePlatformListener(J2eePlatform platform) {
        if (j2eePlatformListener != null) {
            platform.removePropertyChangeListener(j2eePlatformListener);
        }
    }
    
    public Car getAPICar() {
        return apiJar;
    }
    
    public AppClientProvider getCarModule() {
        return appClient;
    }
    
    
    private String getProperty(String path, String name) {
        return helper.getProperties(path).getProperty(name);
    }
    
    String getBuildXmlName() {
        String storedName = helper.getStandardPropertyEvaluator().getProperty(AppClientProjectProperties.BUILD_FILE);
        return storedName == null ? GeneratedFilesHelper.BUILD_XML_PATH : storedName;
    }
        
    /**
     * Refreshes the build-impl.xml script. If it was modified by the user, it 
     * displays a confirmation dialog.
     *
     * @param askUserIfFlags only display the dialog if the state of the build script
     * contains these flags (along with {@link GeneratedFilesHelper#FLAG_MODIFIED}, 
     * which is always checked)
     * @param askInCurrentThread if false, asks in another thread
     */
    private void refreshBuildImplXml(int askUserIfFlags, boolean askInCurrentThread) {
        askUserIfFlags |= GeneratedFilesHelper.FLAG_MODIFIED;
        int flags = genFilesHelper.getBuildScriptState(
            GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
            AppClientProject.class.getResource("resources/build-impl.xsl")); // NOI18N
        if ((flags & askUserIfFlags) == askUserIfFlags) {
            Runnable run = new Runnable () {
                public void run () {
                    JButton updateOption = new JButton (NbBundle.getMessage(AppClientProject.class, "CTL_Regenerate"));
                    if (DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor (NbBundle.getMessage(AppClientProject.class,"TXT_BuildImplRegenerate"),
                            NbBundle.getMessage(AppClientProject.class,"TXT_BuildImplRegenerateTitle"),
                            NotifyDescriptor.DEFAULT_OPTION,
                            NotifyDescriptor.WARNING_MESSAGE,
                            new Object[] {
                                updateOption,
                                NotifyDescriptor.CANCEL_OPTION
                            },
                            updateOption)) == updateOption) {
                        try {
                            genFilesHelper.generateBuildScriptFromStylesheet(
                                GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                                AppClientProject.class.getResource("resources/build-impl.xsl")); // NOI18N
                        } catch (IOException e) {
                            Exceptions.printStackTrace(e);
                        } catch (IllegalStateException e) {
                            Exceptions.printStackTrace(e);
                        }
                    }
                }
            };
            if (askInCurrentThread) {
                run.run();
            } else {
                RequestProcessor.getDefault().post(run);
            }
        } else {
            try {
                genFilesHelper.refreshBuildScript(
                    GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                    AppClientProject.class.getResource("resources/build-impl.xsl"), // NOI18N
                    false);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }
    
    // Private innerclasses ----------------------------------------------------
    //when #110886 gets implemented, this class is obsolete    
    private final class Info implements ProjectInformation {
        
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        
        private WeakReference<String> cachedName = null;
        
        Info() {}
        
        void firePropertyChange(String prop) {
            pcs.firePropertyChange(prop, null, null);
            synchronized (pcs) {
                cachedName = null;
            }
        }
        
        public String getName() {
            return PropertyUtils.getUsablePropertyName(getDisplayName());
        }
        
        public String getDisplayName() {
            synchronized (pcs) {
                if (cachedName != null) {
                    String dn = cachedName.get();
                    if (dn != null) {
                        return dn;
                    }
                }
            }
            String dn = ProjectManager.mutex().readAccess(new Mutex.Action<String>() {
                public String run() {
                    Element data = updateHelper.getPrimaryConfigurationData(true);
                    // XXX replace by XMLUtil when that has findElement, findText, etc.
                    NodeList nl = data.getElementsByTagNameNS(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                    if (nl.getLength() == 1) {
                        nl = nl.item(0).getChildNodes();
                        if (nl.getLength() == 1 && nl.item(0).getNodeType() == Node.TEXT_NODE) {
                            return ((Text) nl.item(0)).getNodeValue();
                        }
                    }
                    return "???"; // NOI18N
                }
            });
            synchronized (pcs) {
                cachedName = new WeakReference<String>(dn);
            }
            return dn;
        }
        
        public Icon getIcon() {
            return CAR_PROJECT_ICON;
        }
        
        public Project getProject() {
            return AppClientProject.this;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }
        
    }
    
    private final class ProjectXmlSavedHookImpl extends ProjectXmlSavedHook {
        
        ProjectXmlSavedHookImpl() {}
        
        protected void projectXmlSaved() throws IOException {
            refreshBuildImplXml(0, false);
            
            genFilesHelper.refreshBuildScript(
                getBuildXmlName(),
                AppClientProject.class.getResource("resources/build.xsl"), //NOI18N
                false);
        }
        
    }
    
    /** Package-private for unit tests only. */
    final class ProjectOpenedHookImpl extends ProjectOpenedHook {
        
        ProjectOpenedHookImpl() {}
        
        protected void projectOpened() {
            // Check up on build scripts.
            try {
                //Check libraries and add them to classpath automatically
                String libFolderName = helper.getStandardPropertyEvaluator ().getProperty (AppClientProjectProperties.LIBRARIES_DIR);

                //DDDataObject initialization to be ready to listen on changes (#49656)
                try {
                    FileObject ddFO = appClient.getDeploymentDescriptor();
                    if (ddFO != null) {
                        DataObject.find(ddFO);
                    }
                } catch (DataObjectNotFoundException ex) {}
                
                if (libFolderName != null && helper.resolveFile (libFolderName).isDirectory ()) {
                    libFolder = helper.resolveFileObject(libFolderName);
                        FileObject children [] = libFolder.getChildren ();
                        List<FileObject> libs = new LinkedList<FileObject>();
                        for (int i = 0; i < children.length; i++) {
                            if (FileUtil.isArchiveFile(children[i])) {
                                libs.add(children[i]);
                            }
                        }
                        FileObject[] libsArray = new FileObject[libs.size()];
                        libs.toArray(libsArray);
                        classpathExtender.addArchiveFiles(AppClientProjectProperties.JAVAC_CLASSPATH, libsArray, ClassPathSupport.ELEMENT_INCLUDED_LIBRARIES);
                        libFolder.addFileChangeListener (AppClientProject.this);
                }
                
                // Check up on build scripts.
                
                refreshBuildImplXml( GeneratedFilesHelper.FLAG_OLD_PROJECT_XML, true);
                
                genFilesHelper.refreshBuildScript(
                    getBuildXmlName(),
                    AppClientProject.class.getResource("resources/build.xsl"), //NOI18N
                    true);
                
                String servInstID = getProperty(AntProjectHelper.PRIVATE_PROPERTIES_PATH, AppClientProjectProperties.J2EE_SERVER_INSTANCE);
                J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(servInstID);
                if (platform != null) {
                    // updates j2ee.platform.cp & wscompile.cp & reg. j2ee platform listener
                    AppClientProjectProperties.setServerInstance(AppClientProject.this, AppClientProject.this.helper, servInstID);
                } else {
                    // if there is some server instance of the type which was used
                    // previously do not ask and use it
                    String serverType = getProperty(AntProjectHelper.PROJECT_PROPERTIES_PATH, AppClientProjectProperties.J2EE_SERVER_TYPE);
                    if (serverType != null) {
                        String[] servInstIDs = Deployment.getDefault().getInstancesOfServer(serverType);
                        if (servInstIDs.length > 0) {
                            AppClientProjectProperties.setServerInstance(AppClientProject.this, AppClientProject.this.helper, servInstIDs[0]);
                            platform = Deployment.getDefault().getJ2eePlatform(servInstIDs[0]);
                        }
                    }
                    if (platform == null) {
                        BrokenServerSupport.showAlert();
                    }
                }
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }

            
            // register project's classpaths to GlobalPathRegistry
            ClassPathProviderImpl cpProvider = lookup.lookup(ClassPathProviderImpl.class);
            GlobalPathRegistry.getDefault().register(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
            GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));
            
            // initialize the server configuration
            // it MUST BE called AFTER classpaths are registered to GlobalPathRegistry!
            // AppClient DDProvider (used here) needs classpath set correctly when resolving Java Extents for annotations
            appClient.getConfigSupport().ensureConfigurationReady();
            
            //register updater of main.class
            //the updater is active only on the opened projects
            mainClassUpdater = new MainClassUpdater(AppClientProject.this, eval, updateHelper,
                    cpProvider.getProjectClassPaths(ClassPath.SOURCE)[0], AppClientProjectProperties.MAIN_CLASS);

            try {
                getProjectDirectory().getFileSystem().runAtomicAction(new AtomicAction() {
                    public void run() throws IOException {
                        ProjectManager.mutex().writeAccess(new Runnable() {
                            public void run() {
                                updateProject();
                            }
                        });
                    }
                });
                
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
            
            AppClientLogicalViewProvider physicalViewProvider =  AppClientProject.this.getLookup().lookup(AppClientLogicalViewProvider.class);
            if (physicalViewProvider != null &&  physicalViewProvider.hasBrokenLinks()) {
                BrokenReferencesSupport.showAlert();
            }
        }
        
        private void updateProject() {
            // Make it easier to run headless builds on the same machine at least.
            EditableProperties ep = updateHelper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
            File buildProperties = new File(System.getProperty("netbeans.user"), "build.properties"); // NOI18N
            ep.setProperty("user.properties.file", buildProperties.getAbsolutePath()); //NOI18N

            // set jaxws.endorsed.dir property (for endorsed mechanism to be used with wsimport, wsgen)
            WSUtils.setJaxWsEndorsedDirProperty(ep);

            updateHelper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);

            // update a dual build directory project to use a single build directory
            ep = updateHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            String earBuildDir = ep.getProperty(AppClientProjectProperties.BUILD_EAR_CLASSES_DIR);
            if (null != earBuildDir) {
                // there is an BUILD_EAR_CLASSES_DIR property... we may
                //  need to change its value
                String buildDir = ep.getProperty(AppClientProjectProperties.BUILD_CLASSES_DIR);
                if (null != buildDir) {
                    // there is a value that we may need to change the
                    // BUILD_EAR_CLASSES_DIR property value to match.
                    if (!buildDir.equals(earBuildDir)) {
                        // the values do not match... update the property and save it
                        ep.setProperty(AppClientProjectProperties.BUILD_EAR_CLASSES_DIR,
                                buildDir);
                        updateHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,
                                ep);
                    }
                    // else {
                    //   the values match and we don't need to do anything
                    // }
                }
                // else {
                //   the project doesn't have a BUILD_CLASSES_DIR property
                //   ** This is not an expected state, but if the project 
                //      properties evolve, this property may go away...
                // }
            }
            // else {
            //   there isn't a BUILD_EAR_CLASSES_DIR in this project...
            //     so we should not create one, by setting it.
            // }

            try {
                ProjectManager.getDefault().saveProject(AppClientProject.this);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        
        protected void projectClosed() {
            
            // unregister j2ee platform classpath change listener
            String servInstID = getProperty(AntProjectHelper.PRIVATE_PROPERTIES_PATH, AppClientProjectProperties.J2EE_SERVER_INSTANCE);
            J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(servInstID);
            if (platform != null) {
                unregisterJ2eePlatformListener(platform);
            }

            // Probably unnecessary, but just in case:
            try {
                ProjectManager.getDefault().saveProject(AppClientProject.this);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
            
            // unregister project's classpaths to GlobalPathRegistry
            ClassPathProviderImpl cpProvider = lookup.lookup(ClassPathProviderImpl.class);
            GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
            GlobalPathRegistry.getDefault().unregister(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));
            if (mainClassUpdater != null) {
                mainClassUpdater.unregister();
                mainClassUpdater = null;
            }
        }
        
    }
    
    public WebServicesClientSupport getAPIWebServicesClientSupport() {
        return apiWebServicesClientSupport;
    }

    public JAXWSClientSupport getAPIJAXWSClientSupport() {
        return apiJAXWSClientSupport;
    }
    
    /**
     * Exports the main JAR as an official build product for use from other scripts.
     * The type of the artifact will be {@link AntArtifact#TYPE_JAR}.
     */
    private final class AntArtifactProviderImpl implements AntArtifactProvider {
        
        public AntArtifact[] getBuildArtifacts() {
            return new AntArtifact[] {
                helper.createSimpleAntArtifact(JavaProjectConstants.ARTIFACT_TYPE_JAR, "dist.jar", helper.getStandardPropertyEvaluator(), "dist", "clean"), // NOI18N
                //new CarAntArtifact(helper.createSimpleAntArtifact(AppClientProjectConstants.ARTIFACT_TYPE_CAR, "dist.jar", helper.getStandardPropertyEvaluator(), "dist", "clean")), // NOI18N
                new CarAntArtifact(helper.createSimpleAntArtifact(AppClientProjectConstants.ARTIFACT_TYPE_J2EE_MODULE_IN_EAR_ARCHIVE, "dist.ear.jar", helper.getStandardPropertyEvaluator(), "dist-ear", "clean-ear")) // NOI18N
        };
        }
        
    }
    
    private static final class RecommendedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates {
        
        RecommendedTemplatesImpl(UpdateHelper helper) {
            this.helper = helper;
        }
        
        transient private final UpdateHelper helper;
        transient private boolean isArchive = false;
        
        // List of primarily supported templates
        
        private static final String[] APPLICATION_TYPES = new String[] {
            "java-classes",         // NOI18N
            "java-main-class",      // NOI18N
            "j2ee-types",           // NOI18N                    
            "java-forms",           // NOI18N
            "gui-java-application", // NOI18N
            "java-beans",           // NOI18N
            "persistence",          // NOI18N
            "oasis-XML-catalogs",   // NOI18N
            "XML",                  // NOI18N
            "ant-script",           // NOI18N
            "ant-task",             // NOI18N
            "web-service-clients",  // NOI18N
            "wsdl",                 // NOI18N
            "sunresource-types",     // NOI18N
            // "web-types",         // NOI18N
            "junit",                // NOI18N
            // "MIDP",              // NOI18N
            "simple-files"          // NOI18N
        };

        private static final String[] PRIVILEGED_NAMES = new String[] {
            "Templates/Classes/Class.java", // NOI18N
            "Templates/Classes/Package", // NOI18N
            "Templates/Classes/Interface.java", // NOI18N
            "Templates/GUIForms/JPanel.java", // NOI18N
            "Templates/GUIForms/JFrame.java", // NOI18N
            "Templates/J2EE/CachingServiceLocator.java", //NOI18N
            "Templates/WebServices/WebServiceClient"   // NOI18N
        };
        
        private static final String[] APPLICATION_TYPES_ARCHIVE = new String[] {
            "deployment-descriptor",           // NOI18N                    
            "XML",                             // NOI18N
        };
        
        private static final String[] PRIVILEGED_NAMES_ARCHIVE = new String[] {
            "Templates/J2EE/applicationClientXml,"  // NOI18N
        };

        public String[] getRecommendedTypes() {
            String[] retVal = null;
            checkEnvironment();
            if (isArchive) {
                retVal = APPLICATION_TYPES_ARCHIVE.clone();
            } else {
                retVal = APPLICATION_TYPES.clone();
            }
            return retVal;
        }
        
        public String[] getPrivilegedTemplates() {
            String[] retVal = null;
            checkEnvironment();
            if (isArchive) {
                retVal = PRIVILEGED_NAMES_ARCHIVE.clone();
            } else {
                retVal = PRIVILEGED_NAMES.clone();
            }
            return retVal;
        }

        transient private boolean checked = false;
        
        private void checkEnvironment() {
            if (!checked) {
                final Object srcType = helper.getAntProjectHelper().
                        getStandardPropertyEvaluator().getProperty(AppClientProjectProperties.JAVA_SOURCE_BASED);
                if ("false".equals(srcType)) {
                    isArchive = true;
                }
                checked = true;
            }
        }
        
    }

    private final class CarAntArtifact extends AntArtifact {
    
        private final AntArtifact impl;
    
        CarAntArtifact(AntArtifact aa) {
            impl = aa;
        }
    
        @Override
        public String getID() {
            if (AppClientProjectConstants.ARTIFACT_TYPE_J2EE_MODULE_IN_EAR_ARCHIVE.equals(getType())) {
                return AppClientProjectConstants.CAR_ANT_ARTIFACT_ID;
            }
            return impl.getID();
        }

        public String getType() {
            return impl.getType();
        }

        public String getTargetName() {
            return impl.getTargetName();
        }

        public File getScriptLocation() {
            return impl.getScriptLocation();
        }

        public String getCleanTargetName() {
            return impl.getCleanTargetName();
        }

        @Override
        public URI[] getArtifactLocations() {
            return impl.getArtifactLocations();
        }
    
        @Override
        public Project getProject() {
            return impl.getProject();
        }
    
    }
    
    private class AppClientExtenderImplementation implements AntBuildExtenderImplementation {
        //add targets here as required by the external plugins..
        public List<String> getExtensibleTargets() {
            String[] targets = new String[] {
                "-do-init", "-init-check", "-post-clean", "jar", "-pre-pre-compile","-do-compile","-do-compile-single", "-pre-dist" //NOI18N
            };
            return Arrays.asList(targets);
        }

        public Project getOwningProject() {
            return AppClientProject.this;
        }

    }
    
}

