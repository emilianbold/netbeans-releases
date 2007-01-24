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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.util.*;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.web.project.jaxws.WebJAXWSMetadataFinder;
import org.netbeans.modules.web.project.jaxws.WebProjectJAXWSClientSupport;
import org.netbeans.modules.web.project.jaxws.WebProjectJAXWSSupport;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModelProvider;
import org.netbeans.modules.websvc.api.jaxws.project.GeneratedFilesHelper;
import org.netbeans.modules.websvc.jaxws.spi.JAXWSSupportFactory;
import org.netbeans.modules.websvc.spi.client.WebServicesClientSupportFactory;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientSupportFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.openide.filesystems.FileLock;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleFactory;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.web.project.classpath.ClassPathProviderImpl;
import org.netbeans.modules.web.project.classpath.WebProjectClassPathExtender;
import org.netbeans.modules.web.project.queries.*;
import org.netbeans.modules.web.project.ui.WebPhysicalViewProvider;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.j2ee.common.ui.BrokenServerSupport;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.spi.webservices.WebServicesSupportFactory;

/**
 * Represents one plain Web project.
 * @author Jesse Glick, et al., Pavel Buzek
 */
public final class WebProject implements Project, AntProjectListener, FileChangeListener, PropertyChangeListener {
    private static final Icon WEB_PROJECT_ICON = new ImageIcon(Utilities.loadImage("org/netbeans/modules/web/project/ui/resources/webProjectIcon.gif")); // NOI18
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final ReferenceHelper refHelper;
    private final GeneratedFilesHelper genFilesHelper;
    private final Lookup lookup;
    private final ProjectWebModule webModule;
    private FileObject libFolder = null;
    private CopyOnSaveSupport css;
    private WebModule apiWebModule;
    private WebProjectWebServicesSupport webProjectWebServicesSupport;
    private WebProjectWebServicesClientSupport webProjectWebServicesClientSupport;
    private WebServicesSupport apiWebServicesSupport;
    private JAXWSSupport apiJaxwsSupport;
    private WebProjectJAXWSSupport jaxwsSupport;
    private WebServicesClientSupport apiWebServicesClientSupport;
    private WebProjectJAXWSClientSupport jaxWsClientSupport;
    private JAXWSClientSupport apiJAXWSClientSupport;
    private WebContainerImpl enterpriseResourceSupport;
    private FileWatch webPagesFileWatch;
    private PropertyChangeListener j2eePlatformListener;
    private SourceRoots sourceRoots;
    private SourceRoots testRoots;
    private final UpdateHelper updateHelper;
    private final AuxiliaryConfiguration aux;
    private final WebProjectClassPathExtender classPathExtender;
    private PropertyChangeListener evalListener;
    private JaxWsModel jaxWsModel;
    private JaxWsListener jaxWsListener;
    private FileObject jaxWsFo;
    private JaxWsModel.ServiceListener jaxWsServiceListener;

    private class FileWatch implements AntProjectListener, FileChangeListener {

        private String propertyName;

        private FileObject fileObject = null;
        private boolean watchRename = false;

        public FileWatch(String property) {
            this.propertyName = property;
        }

        public void init() {
            helper.addAntProjectListener(this);
            updateFileChangeListener();
        }

        public void reset() {
            helper.removeAntProjectListener(this);
            setFileObject(null);
        }

        public void updateFileChangeListener() {
            File resolvedFile;
            FileObject fo = null;
            String propertyValue = helper.getStandardPropertyEvaluator().getProperty(propertyName);
            if (propertyValue != null) {
                String resolvedPath = helper.resolvePath(propertyValue);
                resolvedFile = new File(resolvedPath).getAbsoluteFile();
                if (resolvedFile != null) {
                    File f = resolvedFile;
                    while (f != null && (fo = FileUtil.toFileObject(f)) == null) {
                        f = f.getParentFile();
                    }
                    watchRename = f == resolvedFile;
                } else {
                    watchRename = false;
                }
            } else {
                resolvedFile = null;
                watchRename = false;
            }
            setFileObject(fo);
        }

        private void setFileObject(FileObject fo) {
            if (!isEqual(fo, fileObject)) {
                if (fileObject != null) {
                    fileObject.removeFileChangeListener(this);
                }
                fileObject = fo;
                if (fileObject != null) {
                    fileObject.addFileChangeListener(this);
                }
            }
        }

        private boolean isEqual(Object object1, Object object2) {
            if (object1 == object2) {
                return true;
            }
            if(object1 == null) {
                return false;
            }
            return object1.equals(object2);
        }

        // AntProjectListener

        public void configurationXmlChanged(AntProjectEvent ev) {
            updateFileChangeListener();
        }

        public void propertiesChanged(AntProjectEvent ev) {
            updateFileChangeListener();
        }

        // FileChangeListener

        public void fileFolderCreated(FileEvent fe) {
            updateFileChangeListener();
        }

        public void fileDataCreated(FileEvent fe) {
            updateFileChangeListener();
        }

        public void fileChanged(FileEvent fe) {
            updateFileChangeListener();
        }

        public void fileDeleted(FileEvent fe) {
            updateFileChangeListener();
        }

        public void fileRenamed(FileRenameEvent fe) {
            if(watchRename && fileObject.isValid()) {
                File f = new File(helper.getStandardPropertyEvaluator().getProperty(propertyName));
                if(f.getName().equals(fe.getName())) {
                    EditableProperties properties = new EditableProperties(true);
                    properties.setProperty(propertyName, new File(f.getParentFile(), fe.getFile().getName()).getPath());
                    Utils.updateProperties(helper, AntProjectHelper.PROJECT_PROPERTIES_PATH, properties);
                    getWebProjectProperties().store();
                }
            }
            updateFileChangeListener();
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
    };
    
    WebProject(final AntProjectHelper helper) throws IOException {
        this.helper = helper;
        eval = createEvaluator();
        aux = helper.createAuxiliaryConfiguration();
        refHelper = new ReferenceHelper(helper, aux, eval);
        genFilesHelper = new GeneratedFilesHelper(helper);
        this.updateHelper = new UpdateHelper (this, this.helper, this.aux, UpdateHelper.createDefaultNotifier());
        webModule = new ProjectWebModule (this, updateHelper);
        apiWebModule = WebModuleFactory.createWebModule (webModule);
        webProjectWebServicesSupport = new WebProjectWebServicesSupport(this, helper, refHelper);
        jaxwsSupport = new WebProjectJAXWSSupport(this, helper);
        jaxWsClientSupport = new WebProjectJAXWSClientSupport(this);
        webProjectWebServicesClientSupport = new WebProjectWebServicesClientSupport(this, helper, refHelper);
        apiWebServicesSupport = WebServicesSupportFactory.createWebServicesSupport (webProjectWebServicesSupport);
        apiJaxwsSupport = JAXWSSupportFactory.createJAXWSSupport(jaxwsSupport);
        apiWebServicesClientSupport = WebServicesClientSupportFactory.createWebServicesClientSupport (webProjectWebServicesClientSupport);
        apiJAXWSClientSupport = JAXWSClientSupportFactory.createJAXWSClientSupport(jaxWsClientSupport);
        enterpriseResourceSupport = new WebContainerImpl(this, refHelper, helper);
        classPathExtender = new WebProjectClassPathExtender(this, updateHelper, evaluator(), refHelper);
        lookup = createLookup(aux);
        helper.addAntProjectListener(this);
        css = new CopyOnSaveSupport();
        webPagesFileWatch = new FileWatch(WebProjectProperties.WEB_DOCBASE_DIR);
    }

    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }

    public UpdateHelper getUpdateHelper() {
        return updateHelper;
    }
    
    public String toString() {
        return "WebProject[" + getProjectDirectory() + "]"; // NOI18N
    }
    
    private PropertyEvaluator createEvaluator() {
        // XXX might need to use a custom evaluator to handle active platform substitutions... TBD
        // It is currently safe to not use the UpdateHelper for PropertyEvaluator; UH.getProperties() delegates to APH
        PropertyEvaluator e = helper.getStandardPropertyEvaluator();
        evalListener = WeakListeners.propertyChange(this, e);
        e.addPropertyChangeListener(evalListener);
        return e;
    }
    
    PropertyEvaluator evaluator() {
        return eval;
    }
    
    public ReferenceHelper getReferenceHelper () {
        return this.refHelper;
    }

    public Lookup getLookup() {
        return lookup;
    }

    public AntProjectHelper getAntProjectHelper() {
        return helper;
    }

    private Lookup createLookup(AuxiliaryConfiguration aux) {
        SubprojectProvider spp = refHelper.createSubprojectProvider();
        return Lookups.fixed(new Object[] {
            new Info(),
            aux,
            helper.createCacheDirectoryProvider(),
            spp,
            new ProjectWebModuleProvider (),
            new ProjectWebServicesSupportProvider(),
            webModule, //implements J2eeModuleProvider
            enterpriseResourceSupport,
            new WebActionProvider( this, this.updateHelper ),
            new WebPhysicalViewProvider(this, this.updateHelper, evaluator (), refHelper),
            new CustomizerProviderImpl(this, this.updateHelper, evaluator(), refHelper),        
            new ClassPathProviderImpl(this.helper, evaluator(), getSourceRoots(),getTestSourceRoots()),
            new CompiledSourceForBinaryQuery(this.helper, evaluator(),getSourceRoots(),getTestSourceRoots()),
            new JavadocForBinaryQueryImpl(this.helper, evaluator()),
            new AntArtifactProviderImpl(),
            new ProjectXmlSavedHookImpl(),
            new ProjectOpenedHookImpl(),
            new UnitTestForSourceQueryImpl(getSourceRoots(),getTestSourceRoots()),
            new SourceLevelQueryImpl(evaluator()),
            new WebSources (this.helper, evaluator(), getSourceRoots(), getTestSourceRoots()),
            new WebSharabilityQuery (this.helper, evaluator(), getSourceRoots(), getTestSourceRoots()), //Does not use APH to get/put properties/cfgdata
            new RecommendedTemplatesImpl(),
            new WebFileBuiltQuery (this.helper, evaluator(),getSourceRoots(),getTestSourceRoots()),
            classPathExtender,
            new WebProjectOperations(this),
            new WebPersistenceProvider(this, evaluator()),
            new WebJAXWSMetadataFinder(this),
            getJaxWsModel(),
            new WebPersistenceProviderSupplier(this),
            new WebEMGenStrategyResolver()
        });
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
    
    String getBuildXmlName () {
        String storedName = helper.getStandardPropertyEvaluator ().getProperty (WebProjectProperties.BUILD_FILE);
        return storedName == null ? GeneratedFilesHelper.BUILD_XML_PATH : storedName;
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
        String testClassesDir = evaluator().getProperty(WebProjectProperties.BUILD_TEST_CLASSES_DIR);
        if (testClassesDir == null) {
            return null;
        }
        return helper.resolveFile(testClassesDir);
    }
    
    public ProjectWebModule getWebModule () {
        return webModule;
    }

    public WebModule getAPIWebModule () {
        return apiWebModule;
    }
    
    WebServicesSupport getAPIWebServicesSupport () {
            return apiWebServicesSupport;
    }
    
    JAXWSSupport getAPIJAXWSSupport () {
            return apiJaxwsSupport;
    }
    
    WebServicesClientSupport getAPIWebServicesClientSupport () {
            return apiWebServicesClientSupport;
    }
    
    JAXWSClientSupport getAPIJAXWSClientSupport () {
            return apiJAXWSClientSupport;
    }
    
    public void fileAttributeChanged (org.openide.filesystems.FileAttributeEvent fe) {
    }    
    
    public void fileChanged (org.openide.filesystems.FileEvent fe) {
    }
    
    public void fileDataCreated (org.openide.filesystems.FileEvent fe) {
        FileObject fo = fe.getFile ();
        checkLibraryFolder (fo);
    }
    
    public void fileDeleted (org.openide.filesystems.FileEvent fe) {
    }
    
    public void fileFolderCreated (org.openide.filesystems.FileEvent fe) {
    }
    
    public void fileRenamed (org.openide.filesystems.FileRenameEvent fe) {
        FileObject fo = fe.getFile ();
        checkLibraryFolder (fo);
    }
    
    public WebProjectProperties getWebProjectProperties() {
        return new WebProjectProperties (this, updateHelper, eval, refHelper);
    }

    private void checkLibraryFolder (FileObject fo) {
        if (!FileUtil.isArchiveFile(fo))
            return;
        
        if (fo.getParent ().equals (libFolder)) {
            try {
                classPathExtender.addArchiveFile(fo);
            }
            catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }

    /** Return configured project name. */
    public String getName() {
        return (String) ProjectManager.mutex().readAccess(new Mutex.Action() {
            public Object run() {
                Element data = helper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                if (nl.getLength() == 1) {
                    nl = nl.item(0).getChildNodes();
                    if (nl.getLength() == 1 && nl.item(0).getNodeType() == Node.TEXT_NODE) {
                        return ((Text) nl.item(0)).getNodeValue();
                    }
                }
                return "???"; // NOI18N
            }
        });
    }
    
    /** Store configured project name. */
    public void setName(final String name) {
        ProjectManager.mutex().writeAccess(new Mutex.Action() {
            public Object run() {
                Element data = helper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                    data.insertBefore(nameEl, /* OK if null */data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                helper.putPrimaryConfigurationData(data, true);
                return null;
            }
        });
    }

    public void registerJ2eePlatformListener(final J2eePlatform platform) {
        // listen to classpath changes
        j2eePlatformListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(J2eePlatform.PROP_CLASSPATH)) {
                    ProjectManager.mutex().writeAccess(new Mutex.Action() {
                        public Object run() {
                            EditableProperties ep = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                            String classpath = Utils.toClasspathString(platform.getClasspathEntries());
                            ep.setProperty(WebProjectProperties.J2EE_PLATFORM_CLASSPATH, classpath);
                            helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                            try {
                                ProjectManager.getDefault().saveProject(WebProject.this);
                            } catch (IOException e) {
                                ErrorManager.getDefault().notify(e);
                            }
                            return null;
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
    // Private innerclasses ----------------------------------------------------
    
    private final class Info implements ProjectInformation {
        
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        
        Info() {}
        
        void firePropertyChange(String prop) {
            pcs.firePropertyChange(prop, null, null);
        }
        
        public String getName() {
            return WebProject.this.getName();
        }
        
        public String getDisplayName() {
            return (String) ProjectManager.mutex().readAccess(new Mutex.Action() {
                public Object run() {
                    Element data = updateHelper.getPrimaryConfigurationData(true);
                    // XXX replace by XMLUtil when that has findElement, findText, etc.
                    NodeList nl = data.getElementsByTagNameNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                    if (nl.getLength() == 1) {
                        nl = nl.item(0).getChildNodes();
                        if (nl.getLength() == 1 && nl.item(0).getNodeType() == Node.TEXT_NODE) {
                            return ((Text) nl.item(0)).getNodeValue();
                        }
                    }
                    return "???"; // NOI18N
                }
            });
        }
        
        public Icon getIcon() {
            return WEB_PROJECT_ICON;
        }
        
        public Project getProject() {
            return WebProject.this;
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
            int flags = genFilesHelper.getBuildScriptState(
                GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                WebProject.class.getResource("resources/build-impl.xsl"),jaxWsFo);
            if ((flags & GeneratedFilesHelper.FLAG_MODIFIED) != 0) {
                RequestProcessor.getDefault().post(new Runnable () {
                    public void run () {
                        JButton updateOption = new JButton (NbBundle.getMessage(WebProject.class, "CTL_Regenerate"));
                        if (DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor (NbBundle.getMessage(WebProject.class,"TXT_BuildImplRegenerate"),
                                NbBundle.getMessage(WebProject.class,"TXT_BuildImplRegenerateTitle"),
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
                                    WebProject.class.getResource("resources/build-impl.xsl"),jaxWsFo);
                            } catch (IOException e) {
                                ErrorManager.getDefault().notify(e);
                            } catch (IllegalStateException e) {
                                ErrorManager.getDefault().notify(e);
                            }
                        }
                    }
                });
            } else {
                genFilesHelper.refreshBuildScript(
                    GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                    WebProject.class.getResource("resources/build-impl.xsl"),
                    jaxWsFo, false);
            }
            genFilesHelper.refreshBuildScript(
                getBuildXmlName (),
                WebProject.class.getResource("resources/build.xsl"),
                jaxWsFo, false);
        }
        
    }
    
    final class ProjectOpenedHookImpl extends ProjectOpenedHook {
        
        ProjectOpenedHookImpl() {}
        
        protected void projectOpened() {
            // Make it easier to run headless builds on the same machine at least.
            ProjectManager.mutex().writeAccess(new Mutex.Action() {
                public Object run() {
                    EditableProperties ep = updateHelper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    File buildProperties = new File(System.getProperty("netbeans.user"), "build.properties"); // NOI18N
                    ep.setProperty("user.properties.file", buildProperties.getAbsolutePath()); //NOI18N
                    
                    EditableProperties props = updateHelper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);    //Reread the properties, PathParser changes them
                    if (props.getProperty(WebProjectProperties.WAR_PACKAGE) == null)
                        props.setProperty(WebProjectProperties.WAR_PACKAGE, "true"); //NOI18N
                    //update lib references in private properties
                    ArrayList l = new ArrayList ();
                    l.addAll(classPathExtender.getClassPathSupport().itemsList(props.getProperty(WebProjectProperties.JAVAC_CLASSPATH),  WebProjectProperties.TAG_WEB_MODULE_LIBRARIES));
                    l.addAll(classPathExtender.getClassPathSupport().itemsList(props.getProperty(WebProjectProperties.WAR_CONTENT_ADDITIONAL),  WebProjectProperties.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES));
                    WebProjectProperties.storeLibrariesLocations(l.iterator(), ep);
                    updateHelper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                    updateHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                    
                    try {
                        ProjectManager.getDefault().saveProject(WebProject.this);
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                    return null;
                }
            });
            try {
                //DDDataObject initialization to be ready to listen on changes (#45771)
                try {
                    FileObject ddFO = webModule.getDeploymentDescriptor();
                    if (ddFO != null) {
                        DataObject dobj = DataObject.find(ddFO);
                    }
                } catch (org.openide.loaders.DataObjectNotFoundException ex) {
                    //PENDING
                }
                
                // Register copy on save support
                css.initialize();
                
                
                // Check up on build scripts.
                if (updateHelper.isCurrent()) {
                    int flags = genFilesHelper.getBuildScriptState(
                        GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                        WebProject.class.getResource("resources/build-impl.xsl"),jaxWsFo);
                    if ((flags & GeneratedFilesHelper.FLAG_MODIFIED) != 0
                        && (flags & (GeneratedFilesHelper.FLAG_OLD_PROJECT_XML | GeneratedFilesHelper.FLAG_OLD_JAX_WS)) != 0) {
                        JButton updateOption = new JButton (NbBundle.getMessage(WebProject.class, "CTL_Regenerate"));
                        if (DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor (NbBundle.getMessage(WebProject.class,"TXT_BuildImplRegenerate"),
                                NbBundle.getMessage(WebProject.class,"TXT_BuildImplRegenerateTitle"),
                                NotifyDescriptor.DEFAULT_OPTION,
                                NotifyDescriptor.WARNING_MESSAGE,
                                new Object[] {
                                    updateOption,
                                    NotifyDescriptor.CANCEL_OPTION
                                },
                                updateOption)) == updateOption) {
                            genFilesHelper.generateBuildScriptFromStylesheet(
                                GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                                WebProject.class.getResource("resources/build-impl.xsl"), jaxWsFo);
                        }
                    } else {
                        genFilesHelper.refreshBuildScript(
                            GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                            WebProject.class.getResource("resources/build-impl.xsl"),
                            jaxWsFo, true);
                    }
                    genFilesHelper.refreshBuildScript(
                        getBuildXmlName(),
                        WebProject.class.getResource("resources/build.xsl"),
                        jaxWsFo, true);
                    
                    WebProjectProperties wpp = getWebProjectProperties();
                    String servInstID = (String) wpp.get(WebProjectProperties.J2EE_SERVER_INSTANCE);
                    J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(servInstID);
                    if (platform != null) {
                        // updates j2ee.platform.cp & wscompile.cp & reg. j2ee platform listener
                        WebProjectProperties.setServerInstance(WebProject.this, WebProject.this.updateHelper, servInstID);
                    } else {
                        // if there is some server instance of the type which was used
                        // previously do not ask and use it
                        String serverType = (String) wpp.get(WebProjectProperties.J2EE_SERVER_TYPE);
                        if (serverType != null) {
                            String[] servInstIDs = Deployment.getDefault().getInstancesOfServer(serverType);
                            if (servInstIDs.length > 0) {
                                WebProjectProperties.setServerInstance(WebProject.this, WebProject.this.updateHelper, servInstIDs[0]);
                                platform = Deployment.getDefault().getJ2eePlatform(servInstIDs[0]);
                            }
                        }
                        if (platform == null) {
                            BrokenServerSupport.showAlert();
                        }
                    }
                }
                
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            
            // register project's classpaths to GlobalPathRegistry
            ClassPathProviderImpl cpProvider = (ClassPathProviderImpl)lookup.lookup(ClassPathProviderImpl.class);
            GlobalPathRegistry.getDefault().register(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
            GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));
            
            // initialize the server configuration
            // it MUST BE called AFTER classpaths are registered to GlobalPathRegistry!
            // DDProvider (used here) needs classpath set correctly when resolving Java Extents for annotations
            webModule.getConfigSupport().ensureConfigurationReady();
            
            //check the config context path
            String ctxRoot = webModule.getContextPath ();
            if (ctxRoot == null) {
                String sysName = getProjectDirectory ().getName (); //NOI18N
                sysName = Utils.createDefaultContext(sysName); //NOI18N
                webModule.setContextPath (sysName);
            }

            WebPhysicalViewProvider physicalViewProvider = (WebPhysicalViewProvider)
                WebProject.this.getLookup().lookup (WebPhysicalViewProvider.class);
            if (physicalViewProvider != null &&  physicalViewProvider.hasBrokenLinks()) {   
                BrokenReferencesSupport.showAlert();
            }
            webPagesFileWatch.init();
            
            if (getWebProjectProperties().get("jsf.pagebean.package") != null) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        DialogDisplayer.getDefault().notify(
                                new NotifyDescriptor.Message(NbBundle.getMessage(WebProject.class,
                                "MSG_Creator_Project"), NotifyDescriptor.WARNING_MESSAGE));
                    }
                });
            }
        }

        protected void projectClosed() {
            webPagesFileWatch.reset();

            // listen to j2ee platform classpath changes
            WebProjectProperties wpp = getWebProjectProperties();
            String servInstID = (String)wpp.get(WebProjectProperties.J2EE_SERVER_INSTANCE);
            J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(servInstID);
            if (platform != null) {
                unregisterJ2eePlatformListener(platform);
            }
            
            // unregister the property change listener on the prop evaluator
            if (evalListener != null) {
                evaluator().removePropertyChangeListener(evalListener);
            }
            
            // remove file change listener from jax-ws.xml
            if (jaxWsFo!=null) jaxWsFo.removeFileChangeListener(jaxWsListener);
            
            // remove ServiceListener from jaxWsModel            
            if (jaxWsModel!=null) jaxWsModel.removeServiceListener(jaxWsServiceListener);

            // Probably unnecessary, but just in case:
            try {
                ProjectManager.getDefault().saveProject(WebProject.this);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
            
            // Unregister copy on save support
            try {
                css.cleanup();
            } 
            catch (FileStateInvalidException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            
            // unregister project's classpaths to GlobalPathRegistry
            ClassPathProviderImpl cpProvider = (ClassPathProviderImpl)lookup.lookup(ClassPathProviderImpl.class);
            GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
            GlobalPathRegistry.getDefault().unregister(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));
        }
        
    }
    
    /**
     * Exports the main JAR as an official build product for use from other scripts.
     * The type of the artifact will be {@link AntArtifact#TYPE_JAR}.
     */
    private final class AntArtifactProviderImpl implements AntArtifactProvider {

        public AntArtifact[] getBuildArtifacts() {
            return new AntArtifact[] {
                helper.createSimpleAntArtifact(WebProjectConstants.ARTIFACT_TYPE_WAR, "dist.war", evaluator(), "dist", "clean"), // NOI18N
                helper.createSimpleAntArtifact(WebProjectConstants.ARTIFACT_TYPE_WAR_EAR_ARCHIVE, "dist.ear.war", evaluator(), "dist-ear", "clean-ear") // NOI18N
            };
        }

    }
    
    // List of primarily supported templates

    private static final String[] TYPES = new String[] { 
        "java-classes",         // NOI18N
        "java-main-class",      // NOI18N
        "java-beans",           // NOI18N
        "persistence",          // NOI18N
        "oasis-XML-catalogs",   // NOI18N
        "XML",                  // NOI18N
        "ant-script",           // NOI18N
        "ant-task",             // NOI18N
        "servlet-types",        // NOI18N
        "web-types",            // NOI18N
        "web-types-server",     // NOI18N
        "web-services",         // NOI18N
        "web-service-clients",  // NOI18N
        "wsdl",                 // NOI18N
        "j2ee-types",           // NOI18N                    
        "junit",                // NOI18N
        "simple-files"          // NOI18N
    };

    private static final String[] TYPES_ARCHIVE = new String[] { 
        "deployment-descriptor",          // NOI18N
        "XML",                            // NOI18N
    };
    
    private static final String[] PRIVILEGED_NAMES = new String[] {
        "Templates/JSP_Servlet/JSP.jsp",            // NOI18N
        "Templates/JSP_Servlet/Html.html",          // NOI18N
        "Templates/JSP_Servlet/Servlet.java",       // NOI18N
        "Templates/Classes/Class.java",             // NOI18N
        "Templates/Classes/Package",                // NOI18N
        "Templates/WebServices/WebService.java",    // NOI18N
        "Templates/WebServices/WebServiceClient",   // NOI18N                    
        "Templates/Other/Folder",                   // NOI18N
    };
    
    private static final String[] PRIVILEGED_NAMES_EE5 = new String[] {
        "Templates/JSP_Servlet/JSP.jsp",            // NOI18N
        "Templates/JSP_Servlet/Html.html",          // NOI18N
        "Templates/JSP_Servlet/Servlet.java",       // NOI18N
        "Templates/Classes/Class.java",             // NOI18N
        "Templates/Classes/Package",                // NOI18N
        "Templates/Persistence/Entity.java", // NOI18N
        "Templates/Persistence/RelatedCMP", // NOI18N                    
        "Templates/Persistence/JsfFromDB", // NOI18N                    
        "Templates/WebServices/WebService.java",    // NOI18N
        "Templates/WebServices/WebServiceFromWSDL.java",    // NOI18N
        "Templates/WebServices/WebServiceClient",   // NOI18N                    
        "Templates/Other/Folder",                   // NOI18N
    };

    private static final String[] PRIVILEGED_NAMES_ARCHIVE = new String[] {
        "Templates/JSP_Servlet/webXml",     // NOI18N  --- 
    };
    
    private final class RecommendedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates {
        RecommendedTemplatesImpl () {
        }
        
        private boolean isEE5 = false;
        private boolean checked = false;
        private boolean isArchive = false;

        public String[] getRecommendedTypes() {
            String[] retVal = null;
            checkEnvironment();
            if (isArchive) {
                retVal = TYPES_ARCHIVE;
            } else {
                retVal = TYPES;
            }
           
            return retVal;
        }
        
        public String[] getPrivilegedTemplates() {
            String[] retVal = null;
            checkEnvironment();
            if (isArchive) {
                retVal = PRIVILEGED_NAMES_ARCHIVE;                        
            } else if (isEE5) {
                retVal = PRIVILEGED_NAMES_EE5;
            } else {
                retVal = PRIVILEGED_NAMES;
            }
            return retVal;
        }
        
        private void checkEnvironment() {
            if (!checked) {
                final Object srcType = helper.getStandardPropertyEvaluator().
                        getProperty(WebProjectProperties.JAVA_SOURCE_BASED);
                if ("false".equals(srcType)) {
                    isArchive = true;
                }
                isEE5 = J2eeModule.JAVA_EE_5.equals(getAPIWebModule().getJ2eePlatformVersion());
                checked = true;
            }
        }
        
    }

    public class CopyOnSaveSupport extends FileChangeAdapter implements PropertyChangeListener {
        private FileObject docBase = null;

        /** Creates a new instance of CopyOnSaveSupport */
        public CopyOnSaveSupport() {
        }

        public void initialize() throws FileStateInvalidException {
            docBase = getWebModule().getDocumentBase();
            if (docBase != null) {
                docBase.getFileSystem().addFileChangeListener(this);
            }
            ProjectInformation info = (ProjectInformation) getLookup().lookup(ProjectInformation.class);
            info.addPropertyChangeListener (this);
        }

        public void cleanup() throws FileStateInvalidException {
            if (docBase != null) {
                docBase.getFileSystem().removeFileChangeListener(this);
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(WebProjectProperties.WEB_DOCBASE_DIR)) {
                try {
                    cleanup();
                    initialize();
                } catch (org.openide.filesystems.FileStateInvalidException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
        }
    
        /** Fired when a file is changed.
        * @param fe the event describing context where action has taken place
        */
        public void fileChanged (FileEvent fe) {
            try {
                handleCopyFileToDestDir(fe.getFile());
            }
            catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }

        public void fileDataCreated (FileEvent fe) {
            try {
                handleCopyFileToDestDir(fe.getFile());
            }
            catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        
        public void fileRenamed(FileRenameEvent fe) {
            try {
                FileObject fo = fe.getFile();
                FileObject docBase = getWebModule().getDocumentBase();
                if (docBase != null && FileUtil.isParentOf(docBase, fo)) {
                    // inside docbase
                    handleCopyFileToDestDir(fo);
                    FileObject parent = fo.getParent();
                    String path;
                    if (FileUtil.isParentOf(docBase, parent)) {
                        path = FileUtil.getRelativePath(docBase, fo.getParent()) +
                            "/" + fe.getName() + "." + fe.getExt();
                    }
                    else {
                        path = fe.getName() + "." + fe.getExt();
                    }
                    if (!isSynchronizationAppropriate(path)) 
                        return;
                    handleDeleteFileInDestDir(path);
                }
            }
            catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        
        public void fileDeleted(FileEvent fe) {
            try {
                FileObject fo = fe.getFile();
                FileObject docBase = getWebModule().getDocumentBase();
                if (docBase != null && FileUtil.isParentOf(docBase, fo)) {
                    // inside docbase
                    String path = FileUtil.getRelativePath(docBase, fo);
                    if (!isSynchronizationAppropriate(path)) 
                        return;
                    handleDeleteFileInDestDir(path);
                }
            }
            catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        
        private boolean isSynchronizationAppropriate(String filePath) {
            if (filePath.startsWith("WEB-INF/classes")) {
                return false;
            }
            if (filePath.startsWith("WEB-INF/src")) {
                return false;
            }
            if (filePath.startsWith("WEB-INF/lib")) {
                return false;
            }
            return true;
        }
        
        private void handleDeleteFileInDestDir(String resourcePath) throws IOException {
            FileObject webBuildBase = getWebModule().getContentDirectory();
            if (webBuildBase != null) {
                // project was built
                FileObject toDelete = webBuildBase.getFileObject(resourcePath);
                if (toDelete != null) {
                    toDelete.delete();
                }
            }
        }
        
        /** Copies a content file to an appropriate  destination directory, 
         * if applicable and relevant.
         */
        private void handleCopyFileToDestDir(FileObject fo) throws IOException {
            if (!fo.isVirtual()) {
                FileObject docBase = getWebModule().getDocumentBase();
                if (docBase != null && FileUtil.isParentOf(docBase, fo)) {
                    // inside docbase
                    String path = FileUtil.getRelativePath(docBase, fo);
                    if (!isSynchronizationAppropriate(path)) 
                        return;
                    FileObject webBuildBase = getWebModule().getContentDirectory();
                    if (webBuildBase != null) {
                        // project was built
                        if (FileUtil.isParentOf(docBase, webBuildBase) || FileUtil.isParentOf(webBuildBase, docBase)) {
                            //cannot copy into self
                            return;
                        }
                        FileObject destFile = ensureDestinationFileExists(webBuildBase, path, fo.isFolder());
                        if (!fo.isFolder()) {
                            InputStream is = null;
                            OutputStream os = null;
                            FileLock fl = null;
                            try {
                                is = fo.getInputStream();
                                fl = destFile.lock();
                                os = destFile.getOutputStream(fl);
                                FileUtil.copy(is, os);
                            }
                            finally {
                                if (is != null) {
                                    is.close();
                                }
                                if (os != null) {
                                    os.close();
                                }
                                if (fl != null) {
                                    fl.releaseLock();
                                }
                            }
                            //System.out.println("copied + " + FileUtil.copy(fo.getInputStream(), destDir, fo.getName(), fo.getExt()));
                        }
                    }
                }
            }
        }

        /** Returns the destination (parent) directory needed to create file with relative path path under webBuilBase
         */
        private FileObject ensureDestinationFileExists(FileObject webBuildBase, String path, boolean isFolder) throws IOException {
            FileObject current = webBuildBase;
            StringTokenizer st = new StringTokenizer(path, "/");
            while (st.hasMoreTokens()) {
                String pathItem = st.nextToken();
                FileObject newCurrent = current.getFileObject(pathItem);
                if (newCurrent == null) {
                    // need to create it
                    if (isFolder || st.hasMoreTokens()) {
                        // create a folder
                        newCurrent = FileUtil.createFolder(current, pathItem);
                    }
                    else {
                        newCurrent = FileUtil.createData(current, pathItem);
                    }
                }
                current = newCurrent;
            }
            return current;
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(WebProjectProperties.JAVAC_CLASSPATH) ||
                evt.getPropertyName().equals(WebProjectProperties.WAR_CONTENT_ADDITIONAL)) {
            ProjectManager.mutex().postWriteRequest(new Runnable () {
                public void run() {
		    if (ProjectManager.getDefault().isValid(((ProjectInformation) getLookup().lookup(ProjectInformation.class)).getProject())) {
			EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);    //Reread the properties, PathParser changes them
			//update lib references in private properties
			EditableProperties privateProps = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
			ArrayList l = new ArrayList ();
			l.addAll(classPathExtender.getClassPathSupport().itemsList(props.getProperty(WebProjectProperties.JAVAC_CLASSPATH),  WebProjectProperties.TAG_WEB_MODULE_LIBRARIES));
			l.addAll(classPathExtender.getClassPathSupport().itemsList(props.getProperty(WebProjectProperties.WAR_CONTENT_ADDITIONAL),  WebProjectProperties.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES));
			WebProjectProperties.storeLibrariesLocations(l.iterator(), privateProps);
			helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProps);
		    }
                }
            });
        }
    }
    
    /** copy jax-ws.xml from resource to nbproject directory,
     *  generate JaxWsModel,
     *  add FileChangeListener to jax-ws.xml file object
     */
    public void createJaxWsFileObject() throws IOException {
        FileObject projectDir = helper.getProjectDirectory();
        WSUtils.retrieveJaxWsFromResource(projectDir);
        
        jaxWsFo = findJaxWsFileObject(projectDir);
        if (jaxWsFo!=null) {
            jaxWsListener = new JaxWsListener();
            jaxWsFo.addFileChangeListener(jaxWsListener);
         
            if (jaxWsModel!=null) {
                jaxWsModel.setJaxWsFile(jaxWsFo);
            } else {
                jaxWsModel = JaxWsModelProvider.getDefault().getJaxWsModel(jaxWsFo);
                if (jaxWsModel!=null) {
                    attachServiceListener(jaxWsModel);
                }
            }
        }
    }
    
    public FileObject findJaxWsFileObject() {
        return findJaxWsFileObject(helper.getProjectDirectory());
    }
    
    private FileObject findJaxWsFileObject(FileObject projectDir) {
        return projectDir.getFileObject(GeneratedFilesHelper.JAX_WS_XML_PATH);
    }
    
    private JaxWsModel getJaxWsModel() {
        if (jaxWsModel==null)
            try {
                final FileObject projectDir = helper.getProjectDirectory();
                jaxWsFo = findJaxWsFileObject(projectDir);
                if (jaxWsFo==null) {
                    // create jaxWsModel from the resource
                    jaxWsModel = JaxWsModelProvider.getDefault().getJaxWsModel(
                            WSUtils.class.getResourceAsStream("/org/netbeans/modules/websvc/jaxwsmodel/resources/jax-ws.xml"));//NOI18N
                    jaxWsModel.setJaxWsFile(projectDir);
                } else {
                    jaxWsListener = new JaxWsListener();
                    try {
                        jaxWsModel = JaxWsModelProvider.getDefault().getJaxWsModel(jaxWsFo);
                        jaxWsFo.addFileChangeListener(jaxWsListener);
                    } catch (RuntimeException ex) {
                        // create jaxWsModel from the resource
                        jaxWsModel = JaxWsModelProvider.getDefault().getJaxWsModel(
                                WSUtils.class.getResourceAsStream("/org/netbeans/modules/websvc/jaxwsmodel/resources/jax-ws.xml"));//NOI18N
                        jaxWsModel.setJaxWsFile(projectDir);
                        final FileObject oldJaxWsFo = jaxWsFo;
                        jaxWsFo=null;
                        final RuntimeException exception = ex;
                        RequestProcessor.getDefault().post(new Runnable() {
                            public void run() {
                                try {
                                    jaxWsFo = WSUtils.backupAndGenerateJaxWs(projectDir, oldJaxWsFo, exception);
                                    if (jaxWsFo!=null) {
                                        jaxWsModel.setJaxWsFile(jaxWsFo);
                                        jaxWsFo.addFileChangeListener(jaxWsListener);
                                    }
                                } catch (IOException ex) {
                                    ErrorManager.getDefault().log(ex.getLocalizedMessage());
                                }
                            }
                        });
                    }
                }
                if (jaxWsModel!=null) {
                    attachServiceListener(jaxWsModel);
                }
            } catch (IOException ex) {
                ErrorManager.getDefault().log(ex.getLocalizedMessage());
            }
        return jaxWsModel;
    }
    
    private void attachServiceListener(JaxWsModel jaxWsModel) {
        jaxWsServiceListener = new JaxWsModel.ServiceListener() {
            public void serviceAdded(String name, String implementationClass) {
                getAPIJAXWSSupport().addService(name, implementationClass, isJsr109Supported() && isJavaEE5(WebProject.this));
            }

            public void serviceRemoved(String name) {
                getAPIJAXWSSupport().serviceFromJavaRemoved(name);
            }
        };
        jaxWsModel.addServiceListener(jaxWsServiceListener);
    }
    
    public boolean isJavaEE5(Project project) {
        return J2eeModule.JAVA_EE_5.equals(getAPIWebModule().getJ2eePlatformVersion());
    }
    
    private boolean isJsr109Supported() {
        boolean jsr109Supported = true;
        String serverInstance = evaluator().getProperty(WebProjectProperties.J2EE_SERVER_INSTANCE);
        if (serverInstance != null) {
            J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstance);
            if (j2eePlatform != null) {
                jsr109Supported = j2eePlatform.isToolSupported(J2eePlatform.TOOL_JSR109);
            }
        }
        return jsr109Supported;
    }
   
    private class JaxWsListener extends FileChangeAdapter {
        public void fileChanged(FileEvent fe) {
            try {
                final JaxWsModel newModel = JaxWsModelProvider.getDefault().getJaxWsModel(fe.getFile());
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        if (jaxWsModel!=null && newModel!=null) jaxWsModel.merge(newModel);
                        try {
                            genFilesHelper.refreshBuildScript(
                            GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                            WebProject.class.getResource("resources/build-impl.xsl"),
                            jaxWsFo, false);
                        } catch (IOException ex) {}
                    }
                    
                });
                
            } catch (IOException ex) {}
        }
    }
}
