/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.web.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.web.project.api.WebPropertyEvaluator;
import org.netbeans.modules.web.project.jaxws.WebProjectJAXWSClientSupport;
import org.netbeans.modules.web.project.jaxws.WebProjectJAXWSSupport;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.jaxws.spi.JAXWSSupportFactory;
import org.netbeans.modules.websvc.spi.client.WebServicesClientSupportFactory;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientSupportFactory;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.web.project.classpath.ClassPathProviderImpl;
import org.netbeans.modules.web.project.ui.WebLogicalViewProvider;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.j2ee.common.project.classpath.ClassPathExtender;
import org.netbeans.modules.j2ee.common.project.classpath.ClassPathModifier;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.queries.QuerySupport;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.ant.AntBuildExtenderFactory;
import org.netbeans.spi.project.ant.AntBuildExtenderImplementation;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.modules.j2ee.common.project.classpath.ClassPathSupport;
import org.netbeans.modules.j2ee.common.project.classpath.LibrariesLocationUpdater;
import org.netbeans.modules.j2ee.common.project.ui.ProjectProperties;
import org.netbeans.modules.j2ee.common.ui.BrokenServerSupport;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.project.classpath.ClassPathSupportCallbackImpl;
import org.netbeans.modules.web.project.classpath.WebProjectLibrariesModifierImpl;
import org.netbeans.modules.web.project.jaxws.WebProjectJAXWSVersionProvider;
import org.netbeans.modules.web.project.spi.BrokenLibraryRefFilter;
import org.netbeans.modules.web.project.spi.BrokenLibraryRefFilterProvider;
import org.netbeans.modules.web.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.web.spi.webmodule.WebPrivilegedTemplates;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.spi.webservices.WebServicesSupportFactory;
import org.netbeans.spi.java.project.support.ExtraSourceJavadocSupport;
import org.netbeans.spi.java.project.support.LookupMergerSupport;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Represents one plain Web project.
 * @author Jesse Glick, et al., Pavel Buzek
 */
public final class WebProject implements Project, AntProjectListener {
    
    private static final Logger LOGGER = Logger.getLogger(WebProject.class.getName());
    
    private static final String UI_LOGGER_NAME = "org.netbeans.ui.web.project"; //NOI18N
    
    private static final Icon WEB_PROJECT_ICON = new ImageIcon(Utilities.loadImage("org/netbeans/modules/web/project/ui/resources/webProjectIcon.gif")); // NOI18
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final ReferenceHelper refHelper;
    private final GeneratedFilesHelper genFilesHelper;
    private final Lookup lookup;
    private final ProjectWebModule webModule;
    private CopyOnSaveSupport css;
    private WebModule apiWebModule;
    private WebServicesSupport apiWebServicesSupport;
    private JAXWSSupport apiJaxwsSupport;
    private WebServicesClientSupport apiWebServicesClientSupport;
    private JAXWSClientSupport apiJAXWSClientSupport;
    private WebContainerImpl enterpriseResourceSupport;
    private FileWatch webPagesFileWatch;
    private FileWatch webInfFileWatch;
    private PropertyChangeListener j2eePlatformListener;
    private SourceRoots sourceRoots;
    private SourceRoots testRoots;
    private final UpdateHelper updateHelper;
    private final UpdateProjectImpl updateProject;
    private final AuxiliaryConfiguration aux;
    private final ClassPathExtender classPathExtender;
    private final ClassPathModifier cpMod;
    private final WebProjectLibrariesModifierImpl libMod;
    private final ClassPathProviderImpl cpProvider;
    private LibrariesLocationUpdater librariesLocationUpdater;
    
    private AntBuildExtender buildExtender;
            
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
        buildExtender = AntBuildExtenderFactory.createAntExtender(new WebExtenderImplementation());
        genFilesHelper = new GeneratedFilesHelper(helper, buildExtender);
        updateProject = new UpdateProjectImpl(this, this.helper, aux);
        this.updateHelper = new UpdateHelper(updateProject, helper);
        updateProject.setUpdateHelper(updateHelper);
        this.cpProvider = new ClassPathProviderImpl(this.helper, evaluator(), getSourceRoots(),getTestSourceRoots());
        webModule = new ProjectWebModule (this, updateHelper, cpProvider);
        apiWebModule = WebModuleFactory.createWebModule (webModule);
        WebProjectWebServicesSupport webProjectWebServicesSupport = new WebProjectWebServicesSupport(this, helper, refHelper);
        WebProjectJAXWSSupport jaxwsSupport = new WebProjectJAXWSSupport(this, helper);
        WebProjectJAXWSClientSupport jaxWsClientSupport = new WebProjectJAXWSClientSupport(this);
        WebProjectWebServicesClientSupport webProjectWebServicesClientSupport = new WebProjectWebServicesClientSupport(this, helper, refHelper);
        apiWebServicesSupport = WebServicesSupportFactory.createWebServicesSupport (webProjectWebServicesSupport);
        apiJaxwsSupport = JAXWSSupportFactory.createJAXWSSupport(jaxwsSupport);
        apiWebServicesClientSupport = WebServicesClientSupportFactory.createWebServicesClientSupport (webProjectWebServicesClientSupport);
        apiJAXWSClientSupport = JAXWSClientSupportFactory.createJAXWSClientSupport(jaxWsClientSupport);
        enterpriseResourceSupport = new WebContainerImpl(this, refHelper, helper);
        cpMod = new ClassPathModifier(this, this.updateHelper, eval, refHelper,
            new ClassPathSupportCallbackImpl(helper), createClassPathModifierCallback());
        libMod = new WebProjectLibrariesModifierImpl(this, this.updateHelper, eval, refHelper);
        classPathExtender = new ClassPathExtender(cpMod, ProjectProperties.JAVAC_CLASSPATH, ClassPathSupportCallbackImpl.TAG_WEB_MODULE_LIBRARIES);
        librariesLocationUpdater = new LibrariesLocationUpdater(this, updateHelper, eval, cpMod.getClassPathSupport(),
                ProjectProperties.JAVAC_CLASSPATH, WebProjectProperties.TAG_WEB_MODULE_LIBRARIES, 
                WebProjectProperties.WAR_CONTENT_ADDITIONAL, WebProjectProperties.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES);
        lookup = createLookup(aux, cpProvider);
        helper.addAntProjectListener(this);
        css = new CopyOnSaveSupport();
        webPagesFileWatch = new FileWatch(WebProjectProperties.WEB_DOCBASE_DIR);
        webInfFileWatch = new FileWatch(WebProjectProperties.WEBINF_DIR);
    }
    
    private ClassPathModifier.Callback createClassPathModifierCallback() {
        return new ClassPathModifier.Callback() {
            public String getClassPathProperty(SourceGroup sg, String type) {
                assert sg != null : "SourceGroup cannot be null";  //NOI18N
                assert type != null : "Type cannot be null";  //NOI18N
                final String classPathProperty = getClassPathProvider().getPropertyName (sg, type);
                if (classPathProperty == null) {
                    throw new UnsupportedOperationException ("Modification of [" + sg.getRootFolder().getPath() +", " + type + "] is not supported"); //NOI8N
                }
                return classPathProperty;
            }

            public String getElementName(String classpathProperty) {
                if (ProjectProperties.JAVAC_CLASSPATH.equals(classpathProperty)) {
                    return ClassPathSupportCallbackImpl.TAG_WEB_MODULE_LIBRARIES;
                }
                return null;
            }
        };        
    }

    public UpdateProjectImpl getUpdateImplementation() {
        return updateProject;
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
        return helper.getStandardPropertyEvaluator();
    }
    
    public PropertyEvaluator evaluator() {
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

    private Lookup createLookup(AuxiliaryConfiguration aux, ClassPathProviderImpl cpProvider) {
        SubprojectProvider spp = refHelper.createSubprojectProvider();
        Lookup base = Lookups.fixed(new Object[] {            
            new Info(),
            aux,
            helper.createCacheDirectoryProvider(),
            spp,
            new ProjectWebModuleProvider (),
            new ProjectWebServicesSupportProvider(),
            webModule, //implements J2eeModuleProvider
            enterpriseResourceSupport,
            new WebActionProvider( this, this.updateHelper ),
            new WebLogicalViewProvider(this, this.updateHelper, evaluator (), refHelper),
            new CustomizerProviderImpl(this, this.updateHelper, evaluator(), refHelper),        
            new ClassPathProviderMerger(cpProvider),
            QuerySupport.createCompiledSourceForBinaryQuery(helper, evaluator(), getSourceRoots(), getTestSourceRoots()),
            QuerySupport.createJavadocForBinaryQuery(helper, evaluator()),
            new AntArtifactProviderImpl(),
            new ProjectXmlSavedHookImpl(),
            UILookupMergerSupport.createProjectOpenHookMerger(new ProjectOpenedHookImpl()),
            QuerySupport.createUnitTestForSourceQuery(getSourceRoots(), getTestSourceRoots()),
            QuerySupport.createSourceLevelQuery(evaluator()),
            new WebSources (this.helper, evaluator(), getSourceRoots(), getTestSourceRoots()),
            QuerySupport.createSharabilityQuery(helper, evaluator(), getSourceRoots(), getTestSourceRoots(),
                    WebProjectProperties.WEB_DOCBASE_DIR),
            new RecommendedTemplatesImpl(),
            QuerySupport.createFileBuiltQuery(helper, evaluator(), getSourceRoots(), getTestSourceRoots()),
            classPathExtender,
            buildExtender,
            cpMod,
            new WebProjectOperations(this),
            new WebPersistenceProvider(this, evaluator(), cpProvider),
            new WebPersistenceProviderSupplier(this),
            new WebEMGenStrategyResolver(),
            new WebJPADataSourceSupport(this), 
            new WebServerStatusProvider(this),
            new WebJPAModuleInfo(this),
            new WebProjectJAXWSVersionProvider(helper, this),
            UILookupMergerSupport.createPrivilegedTemplatesMerger(),
            UILookupMergerSupport.createRecommendedTemplatesMerger(),
            LookupProviderSupport.createSourcesMerger(),
            new WebPropertyEvaluatorImpl(evaluator()),
            WebProject.this, // never cast an externally obtained Project to WebProject - use lookup instead
            libMod,
            QuerySupport.createFileEncodingQuery(evaluator(), WebProjectProperties.SOURCE_ENCODING),
            new WebTemplateAttributesProvider(this.helper),
            ExtraSourceJavadocSupport.createExtraSourceQueryImplementation(this, helper, eval),
            LookupMergerSupport.createSFBLookupMerger(),
            ExtraSourceJavadocSupport.createExtraJavadocQueryImplementation(this, helper, eval),
            LookupMergerSupport.createJFBLookupMerger(),
        });
        return LookupProviderSupport.createCompositeLookup(base, "Projects/org-netbeans-modules-web-project/Lookup"); //NOI18N
    }
    
    public ClassPathProviderImpl getClassPathProvider () {
        return this.cpProvider;
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
            this.sourceRoots = SourceRoots.create(updateHelper, evaluator(), getReferenceHelper(), WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "source-roots", false, "src.{0}{1}.dir"); //NOI18N
        }
        return this.sourceRoots;
    }
    
    public synchronized SourceRoots getTestSourceRoots() {
        if (this.testRoots == null) { //Local caching, no project metadata access
            this.testRoots = SourceRoots.create(this.updateHelper, evaluator(), getReferenceHelper(), WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "test-roots", true, "test.{0}{1}.dir"); //NOI18N
        }
        return this.testRoots;
    }

    File getTestClassesDirectory() {
        String testClassesDir = evaluator().getProperty(ProjectProperties.BUILD_TEST_CLASSES_DIR);
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
    
    public WebProjectProperties getWebProjectProperties() {
        return new WebProjectProperties (this, updateHelper, eval, refHelper);
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
                            if (!Boolean.parseBoolean(ep.getProperty(WebProjectProperties.J2EE_PLATFORM_SHARED))) {
                                String classpath = Utils.toClasspathString(platform.getClasspathEntries());
                                ep.setProperty(WebProjectProperties.J2EE_PLATFORM_CLASSPATH, classpath);
                            }
                            helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                            try {
                                ProjectManager.getDefault().saveProject(WebProject.this);
                            } catch (IOException e) {
                                Exceptions.printStackTrace(e);
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
            return WebProject.this.getName();
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
            synchronized (pcs) {
                cachedName = new WeakReference<String>(dn);
            }
            return dn;
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
                WebProject.class.getResource("resources/build-impl.xsl"));
            if ((flags & GeneratedFilesHelper.FLAG_MODIFIED) != 0) {
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        JButton updateOption = new JButton(NbBundle.getMessage(WebProject.class, "CTL_Regenerate"));
                        if (DialogDisplayer.getDefault().notify(
                                new NotifyDescriptor(NbBundle.getMessage(WebProject.class, "TXT_BuildImplRegenerate"),
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
                                        WebProject.class.getResource("resources/build-impl.xsl"));
                            } catch (IOException e) {
                                Exceptions.printStackTrace(e);
                            } catch (IllegalStateException e) {
                                Exceptions.printStackTrace(e);
                            }
                        }
                    }
                });
            } else {
                genFilesHelper.refreshBuildScript(org.netbeans.modules.websvc.api.jaxws.project.GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                                                  org.netbeans.modules.web.project.WebProject.class.getResource("resources/build-impl.xsl"),
                                                  false);
            }
            genFilesHelper.refreshBuildScript(
                getBuildXmlName (),
                WebProject.class.getResource("resources/build.xsl"),false);
        }
        
    }
    
    final class ProjectOpenedHookImpl extends ProjectOpenedHook {
        
        ProjectOpenedHookImpl() {}
        
        protected void projectOpened() {
            try {
                getProjectDirectory().getFileSystem().runAtomicAction(new AtomicAction() {
                    public void run() throws IOException {
                        ProjectManager.mutex().writeAccess(new Runnable() {
                            public void run()  {
                                updateProject();
                            }
                        });
                    }
                });
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
            
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
                        WebProject.class.getResource("resources/build-impl.xsl"));
                    if ((flags & GeneratedFilesHelper.FLAG_MODIFIED) != 0
                        && (flags & GeneratedFilesHelper.FLAG_OLD_PROJECT_XML) != 0) {
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
                                WebProject.class.getResource("resources/build-impl.xsl"));
                        }
                    } else {
                        genFilesHelper.refreshBuildScript(
                            GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                            WebProject.class.getResource("resources/build-impl.xsl"), true);
                    }
                    genFilesHelper.refreshBuildScript(
                        getBuildXmlName(),
                        WebProject.class.getResource("resources/build.xsl"), true);
                    
                    WebProjectProperties wpp = getWebProjectProperties();
                    String servInstID = (String) wpp.get(WebProjectProperties.J2EE_SERVER_INSTANCE);
                    String serverType = null;
                    J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(servInstID);
                    if (platform != null) {
                        // updates j2ee.platform.cp & wscompile.cp & reg. j2ee platform listener
                        WebProjectProperties.setServerInstance(WebProject.this, WebProject.this.updateHelper, servInstID);
                    } else {
                        // if there is some server instance of the type which was used
                        // previously do not ask and use it
                        serverType = (String) wpp.get(WebProjectProperties.J2EE_SERVER_TYPE);
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
                    // UI Logging
                    LogRecord logRecord = new LogRecord(Level.INFO, "UI_WEB_PROJECT_OPENED");  //NOI18N
                    logRecord.setLoggerName(UI_LOGGER_NAME);                   //NOI18N
                    logRecord.setResourceBundle(NbBundle.getBundle(WebProject.class));
                    logRecord.setParameters(new Object[] {
                        (serverType != null ? serverType : Deployment.getDefault().getServerID(servInstID)),
                        servInstID});
                    Logger.getLogger(UI_LOGGER_NAME).log(logRecord);
                }
                
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
            
            // register project's classpaths to GlobalPathRegistry
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

            WebLogicalViewProvider logicalViewProvider = (WebLogicalViewProvider) WebProject.this.getLookup().lookup (WebLogicalViewProvider.class);
            if (logicalViewProvider != null &&  logicalViewProvider.hasBrokenLinks()) {   
                BrokenReferencesSupport.showAlert();
            }
            webPagesFileWatch.init();
            webInfFileWatch.init();
        }
        
        private void updateProject() {
            // Make it easier to run headless builds on the same machine at least.
            EditableProperties ep = updateHelper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
            File buildProperties = new File(System.getProperty("netbeans.user"), "build.properties"); // NOI18N
            ep.setProperty("user.properties.file", buildProperties.getAbsolutePath()); //NOI18N

            // set jaxws.endorsed.dir property (for endorsed mechanism to be used with wsimport, wsgen)
            WSUtils.setJaxWsEndorsedDirProperty(ep);
            
            filterBrokenLibraryRefs();

            EditableProperties props = updateHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);    //Reread the properties, PathParser changes them

            //update lib references in private properties
            ArrayList l = new ArrayList();
            l.addAll(cpMod.getClassPathSupport().itemsList(props.getProperty(ProjectProperties.JAVAC_CLASSPATH),  WebProjectProperties.TAG_WEB_MODULE_LIBRARIES));
            l.addAll(cpMod.getClassPathSupport().itemsList(props.getProperty(WebProjectProperties.WAR_CONTENT_ADDITIONAL),  WebProjectProperties.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES));
            ProjectProperties.storeLibrariesLocations(l.iterator(), props, getProjectDirectory());

            //add webinf.dir required by 6.0 projects
            if (props.getProperty(WebProjectProperties.WEBINF_DIR) == null) {
                //we can do this because in previous versions WEB-INF was expected under docbase
                String web = props.get(WebProjectProperties.WEB_DOCBASE_DIR);
                props.setProperty(WebProjectProperties.WEBINF_DIR, web + "/WEB-INF"); //NOI18N
            }

            updateHelper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);

            // update a dual build directory project to use a single directory
            if (updateHelper.isCurrent()) { // #113297, #118187
                // this operation should be safe in future as well - of course if properties with the same name weren't re-introduced
                props.remove("build.ear.web.dir");      // used to be WebProjectProperties.BUILD_EAR_WEB_DIR    // NOI18N
                props.remove("build.ear.classes.dir");  // used to be WebProjectProperties.BUILD_EAR_CLASSES_DIR    // NOI18N
            }
            // check debug.classpath - can be done every time, whenever
            String debugClassPath = props.getProperty(WebProjectProperties.DEBUG_CLASSPATH);
            props.setProperty(WebProjectProperties.DEBUG_CLASSPATH, Utils.correctDebugClassPath(debugClassPath));

            updateHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);

            try {
                ProjectManager.getDefault().saveProject(WebProject.this);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        
        /**
         * Filters the broken library references (see issue 110040).
         */
        private void filterBrokenLibraryRefs() {
            // filter the compilation CP
            EditableProperties props = updateHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            List<ClassPathSupport.Item> toRemove = filterBrokenLibraryItems(cpMod.getClassPathSupport().itemsList(props.getProperty(ProjectProperties.JAVAC_CLASSPATH), WebProjectProperties.TAG_WEB_MODULE_LIBRARIES));
            if (!toRemove.isEmpty()) {
                LOGGER.log(Level.FINE, "Will remove broken classpath library references: " + toRemove);
                try {
                    cpMod.handleLibraryClassPathItems(toRemove, ProjectProperties.JAVAC_CLASSPATH, WebProjectProperties.TAG_WEB_MODULE_LIBRARIES, ClassPathModifier.REMOVE, false);
                } catch (IOException e) {
                    // should only occur when passing true as the saveProject parameter which we are not doing here
                    Exceptions.printStackTrace(e);
                }
            }
            // filter the additional (packaged) items
            // need to re-read the properites as the handleLibraryClassPathItems() might have changed them
            props = updateHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            toRemove = filterBrokenLibraryItems(libMod.getClassPathSupport().itemsList(props.getProperty(WebProjectProperties.WAR_CONTENT_ADDITIONAL), WebProjectProperties.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES));
            if (!toRemove.isEmpty()) {
                LOGGER.log(Level.FINE, "Will remove broken additional library references: " + toRemove);
                try {
                    libMod.handlePackageLibraryClassPathItems(toRemove, WebProjectLibrariesModifierImpl.REMOVE, false);
                } catch (IOException e) {
                    // should only occur when passing true as the saveProject parameter which we are not doing here
                    Exceptions.printStackTrace(e);
                }
            }
        }
        
        private List<ClassPathSupport.Item> filterBrokenLibraryItems(List<ClassPathSupport.Item> items) {
            List<ClassPathSupport.Item> toRemove = new LinkedList<ClassPathSupport.Item>();
            Collection<? extends BrokenLibraryRefFilter> filters = null;
            for (ClassPathSupport.Item item : items) {
                if (!item.isBroken() || item.getType() != ClassPathSupport.Item.TYPE_LIBRARY) {
                    continue;
                }
                String libraryName = ClassPathSupport.getLibraryNameFromReference(item.getReference());
                LOGGER.log(Level.FINE, "Broken reference to library: " + libraryName);
                if (filters == null) {
                    // initializing the filters lazily because usually they will not be needed anyway
                    // (most projects have no broken references)
                    filters = createFilters(WebProject.this);
                }
                for (BrokenLibraryRefFilter filter : filters) {
                    if (filter.removeLibraryReference(libraryName)) {
                        LOGGER.log(Level.FINE, "Will remove broken reference to library " + libraryName + " because of filter " + filter.getClass().getName());
                        toRemove.add(item);
                        break;
                    }
                }
            }
            return toRemove;
        }
        
        private List<BrokenLibraryRefFilter> createFilters(Project project) {
            List<BrokenLibraryRefFilter> filters = new LinkedList<BrokenLibraryRefFilter>();
            for (BrokenLibraryRefFilterProvider provider : Lookups.forPath("Projects/org-netbeans-modules-web-project/BrokenLibraryRefFilterProviders").lookupAll(BrokenLibraryRefFilterProvider.class)) { // NOI18N
                BrokenLibraryRefFilter filter = provider.createFilter(project);
                if (filter != null) {
                    filters.add(filter);
                }
            }
            return filters;
        }
        
        protected void projectClosed() {
            webPagesFileWatch.reset();
            webInfFileWatch.reset();

            // listen to j2ee platform classpath changes
            WebProjectProperties wpp = getWebProjectProperties();
            String servInstID = (String)wpp.get(WebProjectProperties.J2EE_SERVER_INSTANCE);
            J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(servInstID);
            if (platform != null) {
                unregisterJ2eePlatformListener(platform);
            }
            
            // unregister the property change listener on the prop evaluator
            if (librariesLocationUpdater != null) {
                librariesLocationUpdater.unregister();
            }

            // remove ServiceListener from jaxWsModel            
            //if (jaxWsModel!=null) jaxWsModel.removeServiceListener(jaxWsServiceListener);

            // Probably unnecessary, but just in case:
            try {
                ProjectManager.getDefault().saveProject(WebProject.this);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
            
            // Unregister copy on save support
            try {
                css.cleanup();
            } 
            catch (FileStateInvalidException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
            
            // unregister project's classpaths to GlobalPathRegistry
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
        "Templates/Other/Folder"                    // NOI18N
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
    
    // guarded by this, #115809
    private String[] privilegedTemplatesEE5 = null;
    private String[] privilegedTemplates = null;

    // Path where instances of privileged templates are registered
    private static final String WEBTEMPLATE_PATH = "j2ee/webtier/templates"; //NOI18N
    
    synchronized String[] getPrivilegedTemplates() {
        ensureTemplatesInitialized();
        return privilegedTemplates;
    }

    synchronized String[] getPrivilegedTemplatesEE5() {
        ensureTemplatesInitialized();
        return privilegedTemplatesEE5;
    }
    
    public synchronized void resetTemplates() {
        privilegedTemplates = null;
        privilegedTemplatesEE5 = null;
    }
    
    private void ensureTemplatesInitialized() {
        assert Thread.holdsLock(this);
        if (privilegedTemplates != null
                && privilegedTemplatesEE5 != null) {
            return;
        }
        
        ArrayList<String>templatesEE5 = new ArrayList(PRIVILEGED_NAMES_EE5.length + 1);
        ArrayList<String>templates = new ArrayList(PRIVILEGED_NAMES.length + 1);

        // how many templates are added
        int countTemplate = 0;
        Collection <WebPrivilegedTemplates> pfTemplates = 
                (Collection<WebPrivilegedTemplates>)Lookups.forPath(WEBTEMPLATE_PATH).lookupAll(WebPrivilegedTemplates.class);
        
        for (WebPrivilegedTemplates webPrivililegedTemplates : pfTemplates) {
            String[] addedTemplates = webPrivililegedTemplates.getPrivilegedTemplates(apiWebModule);
            if (addedTemplates != null && addedTemplates.length > 0){
                countTemplate = countTemplate + addedTemplates.length;
                List addedList = Arrays.asList(addedTemplates);
                templatesEE5.addAll(addedList);
                templates.addAll(addedList);
            }
        }

        if(countTemplate > 0){
            templatesEE5.addAll(Arrays.asList(PRIVILEGED_NAMES_EE5));
            privilegedTemplatesEE5 = templatesEE5.toArray(new String[templatesEE5.size()]);
            templates.addAll(Arrays.asList(PRIVILEGED_NAMES));
            privilegedTemplates = templates.toArray(new String[templates.size()]);
        }
        else {
            privilegedTemplatesEE5 = PRIVILEGED_NAMES_EE5;
            privilegedTemplates = PRIVILEGED_NAMES;
        }
    }
    
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
            } else {
                if (isEE5) {
                    retVal = getPrivilegedTemplatesEE5();
                } else {
                    retVal = WebProject.this.getPrivilegedTemplates();
                }
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
                    Logger.getLogger("global").log(Level.INFO, null, e);
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
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
        }

        public void fileDataCreated (FileEvent fe) {
            try {
                handleCopyFileToDestDir(fe.getFile());
            }
            catch (IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
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
                Logger.getLogger("global").log(Level.INFO, null, e);
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
                Logger.getLogger("global").log(Level.INFO, null, e);
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
    
    public boolean isJavaEE5(Project project) {
        return J2eeModule.JAVA_EE_5.equals(getAPIWebModule().getJ2eePlatformVersion());
    }
    
    private static final class WebPropertyEvaluatorImpl implements WebPropertyEvaluator {
        private PropertyEvaluator evaluator;
        public WebPropertyEvaluatorImpl (PropertyEvaluator eval) {
            evaluator = eval;
        }
        public PropertyEvaluator evaluator() {
            return evaluator;
        }
    }
    
    private class WebExtenderImplementation implements AntBuildExtenderImplementation {
        //add targets here as required by the external plugins..
        public List<String> getExtensibleTargets() {
            String[] targets = new String[] {
                "-do-init", "-init-check", "-post-clean", "jar", "-pre-pre-compile","-do-ws-compile","-do-compile","-do-compile-single", "-post-compile", "-pre-dist", //NOI18N
            };
            return Arrays.asList(targets);
        }

        public Project getOwningProject() {
            return WebProject.this;
        }

    }
}
