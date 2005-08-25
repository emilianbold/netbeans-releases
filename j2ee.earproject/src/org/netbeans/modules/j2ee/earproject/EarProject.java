/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.earproject;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.modules.j2ee.earproject.classpath.ClassPathProviderImpl;
import org.netbeans.modules.j2ee.earproject.ui.EarCustomizerProvider;
import org.netbeans.modules.j2ee.earproject.ui.LogicalViewProvider;
import org.netbeans.modules.j2ee.earproject.ui.J2eeArchiveLogicalViewProvider;
import org.netbeans.modules.j2ee.earproject.ui.customizer.ArchiveProjectProperties;
import org.netbeans.modules.j2ee.earproject.ui.customizer.VisualClassPathItem;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarFactory;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.j2ee.earproject.ui.BrokenServerSupport;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.netbeans.modules.j2ee.earproject.ui.IconBaseProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;

import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;

/**
 * Represents an Enterprise Application project.
 *
 * This is the project api centric view of the enterprise application.
 * 
 * @author vince kraemer
 * @see WebProject
 */
public final class EarProject implements Project, AntProjectListener, FileChangeListener, ProjectPropertyProvider {
    
    private static final Icon EAR_PROJECT_ICON = new ImageIcon(Utilities.loadImage("org/netbeans/modules/j2ee/earproject/ui/resources/projectIcon.gif")); // NOI18N
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final ReferenceHelper refHelper;
    private final GeneratedFilesHelper genFilesHelper;
    private final Lookup lookup;
    private final ProjectEar appModule;
    private final Ear ear;
    private AntBasedProjectType abpt;
    private final UpdateHelper updateHelper;
    
    EarProject(final AntProjectHelper helper, AntBasedProjectType abpt) throws IOException {
        this.helper = helper;
        this.abpt = abpt;
        eval = createEvaluator();
        AuxiliaryConfiguration aux = helper.createAuxiliaryConfiguration();
        refHelper = new ReferenceHelper(helper, aux, helper.getStandardPropertyEvaluator ());
        genFilesHelper = new GeneratedFilesHelper(helper);
        appModule = new ProjectEar (this);
        ear = EjbJarFactory.createEar(appModule);
        updateHelper = new UpdateHelper (this, this.helper, aux, this.genFilesHelper, UpdateHelper.createDefaultNotifier());
        lookup = createLookup(aux);
    }

    public UpdateHelper getUpdateHelper() {
        return updateHelper;
    }
    
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }

    public String toString() {
        return "EarProject[" + getProjectDirectory() + "]"; // NOI18N
    }
    
    private PropertyEvaluator createEvaluator() {
        // XXX might need to use a custom evaluator to handle active platform substitutions... TBD
        return helper.getStandardPropertyEvaluator();
    }
    
    PropertyEvaluator evaluator() {
        return eval;
    }
    
    public Lookup getLookup() {
        return lookup;
    }

    public AntProjectHelper getAntProjectHelper() {
        return helper;
    }

    private Lookup createLookup(AuxiliaryConfiguration aux) {
        SubprojectProvider spp = refHelper.createSubprojectProvider();
        final SourcesHelper sourcesHelper = new SourcesHelper(helper, evaluator());
        String configFilesLabel = org.openide.util.NbBundle.getMessage(EarProject.class, "LBL_Node_ConfigBase"); //NOI18N
        
        sourcesHelper.addPrincipalSourceRoot("${"+EarProjectProperties.META_INF+"}", configFilesLabel, /*XXX*/null, null);
        ProjectManager.mutex().postWriteRequest(new Runnable() {
            public void run() {
                sourcesHelper.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
            }
        });
        return Lookups.fixed(new Object[] {
            new Info(),
            aux,
            spp,
            new ProjectEarProvider (),
            appModule, //implements J2eeModuleProvider
            new EarActionProvider( this, updateHelper, refHelper, abpt),
            new LogicalViewProvider(this, updateHelper, evaluator (), spp, refHelper, abpt),
            new MyIconBaseProvider(),
            new EarCustomizerProvider( this, helper, refHelper, abpt ),
            new ClassPathProviderImpl(helper, evaluator()),
            new ProjectXmlSavedHookImpl(),
            new ProjectOpenedHookImpl(),
                new EarSources (helper, evaluator()),
            new RecommendedTemplatesImpl(),
            helper.createSharabilityQuery(evaluator(),
                    new String[] {"${"+EarProjectProperties.SOURCE_ROOT+"}"},
                    new String[] {
                    "${"+EarProjectProperties.BUILD_DIR+"}",
                    "${"+EarProjectProperties.DIST_DIR+"}"}
                ),
            this,
            new EarProjectOperations(this)
        });
    }

    public void configurationXmlChanged(AntProjectEvent ev) {
    }

    public void propertiesChanged(AntProjectEvent ev) {
        // currently ignored
        //TODO: should not be ignored!
    }
    
    public String getBuildXmlName () {
        String storedName = helper.getStandardPropertyEvaluator ().getProperty (ArchiveProjectProperties.BUILD_FILE);
        return storedName == null ? GeneratedFilesHelper.BUILD_XML_PATH : storedName;
    }
    
    // Package private methods -------------------------------------------------
    
    public ProjectEar getAppModule () {
        return appModule;
    }
    
    public Ear getEar () {
        return ear;
    }
    
    public FileObject getSourceDirectory() {
        String srcDir = helper.getStandardPropertyEvaluator ().getProperty ("src.dir"); // NOI18N
        return helper.resolveFileObject(srcDir);
    }
    
    public void fileAttributeChanged (org.openide.filesystems.FileAttributeEvent fe) {
    }    
    
    public void fileChanged (org.openide.filesystems.FileEvent fe) {
    }
    
    public void fileDataCreated (org.openide.filesystems.FileEvent fe) {
    }
    
    public void fileDeleted (org.openide.filesystems.FileEvent fe) {
    }
    
    public void fileFolderCreated (org.openide.filesystems.FileEvent fe) {
    }
    
    public void fileRenamed (org.openide.filesystems.FileRenameEvent fe) {
    }
    
    /** Return configured project name. */
    public String getName() {
        return (String) ProjectManager.mutex().readAccess(new Mutex.Action() {
            public Object run() {
                Element data = updateHelper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(EarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                if (nl.getLength() == 1) {
                    nl = nl.item(0).getChildNodes();
                    if (nl.getLength() == 1 && nl.item(0).getNodeType() == Node.TEXT_NODE) {
                        return ((Text) nl.item(0)).getNodeValue();
                    }
                }
                return "EAR????"; // NOI18N
            }
        });
    }
    
    /** Store configured project name. */
    public void setName(final String name) {
        ProjectManager.mutex().writeAccess(new Mutex.Action() {
            public Object run() {
                Element data = helper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(EarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(EarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                    data.insertBefore(nameEl, /* OK if null */data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                helper.putPrimaryConfigurationData(data, true);
                return null;
            }
        });
    }

    // Private innerclasses ----------------------------------------------------
    
    private final class Info implements ProjectInformation {
        
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        
        Info() {}
        
        void firePropertyChange(String prop) {
            pcs.firePropertyChange(prop, null, null);
        }
        
        public String getName() {
            return EarProject.this.getName();
        }
        
        public String getDisplayName() {
            return EarProject.this.getName();
        }
        
        public Icon getIcon() {
            return EAR_PROJECT_ICON;
        }
        
        public Project getProject() {
            return EarProject.this;
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
            genFilesHelper.refreshBuildScript(
                GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                EarProject.class.getResource("resources/build-impl.xsl"),
                false);
            genFilesHelper.refreshBuildScript(
                getBuildXmlName (),
                EarProject.class.getResource("resources/build.xsl"),
                false);
        }
        
    }

    private boolean addLibrary (List cpItems, FileObject lib) {
        boolean needsAdding = true;
        for (Iterator vcpsIter = cpItems.iterator (); vcpsIter.hasNext ();) {
            VisualClassPathItem vcpi = (VisualClassPathItem) vcpsIter.next ();

            if (vcpi.getType () != VisualClassPathItem.TYPE_JAR) {
                continue;
            }
            FileObject fo = FileUtil.toFileObject (new File(helper.getStandardPropertyEvaluator ().evaluate (vcpi.getEvaluated ())));
            if (lib.equals (fo)) {
                needsAdding = false;
                break;
            }
        }
        if (needsAdding) {
            String file = "${"+ArchiveProjectProperties.LIBRARIES_DIR+"}/"+lib.getNameExt ();
            String eval = helper.getStandardPropertyEvaluator ().evaluate (file);
            VisualClassPathItem cpItem = //new VisualClassPathItem( file, VisualClassPathItem.TYPE_JAR, file, eval, VisualClassPathItem.PATH_IN_WAR_LIB);
                VisualClassPathItem.create( file,  VisualClassPathItem.PATH_IN_WAR_LIB);
            cpItems.add (cpItem);
        }
        return needsAdding;
    }
    
    
    
    private final class ProjectOpenedHookImpl extends ProjectOpenedHook {
        
        ProjectOpenedHookImpl() {}
        
        protected void projectOpened() {
            try {
                //Check libraries and add them to classpath automatically
                String libFolderName = helper.getStandardPropertyEvaluator ().getProperty (EarProjectProperties.LIBRARIES_DIR);
                EarProjectProperties wpp = new EarProjectProperties (EarProject.this, updateHelper, eval, refHelper,abpt);
                getAppModule().setModules(wpp.getModuleMap());
                if (libFolderName != null && new File (libFolderName).isDirectory ()) {
                    List cpItems = (List) wpp.get (EarProjectProperties.JAVAC_CLASSPATH);
                    FileObject libFolder = FileUtil.toFileObject (new File (libFolderName));
                    FileObject libs [] = libFolder.getChildren ();
                    boolean anyChanged = false;
                    for (int i = 0; i < libs.length; i++) {
                        anyChanged = addLibrary (cpItems, libs [i]) || anyChanged;
                    }
                    if (anyChanged) {
                        wpp.put (EarProjectProperties.JAVAC_CLASSPATH, cpItems);
                        wpp.store ();
                        ProjectManager.getDefault ().saveProject (EarProject.this);
                    }
                    libFolder.addFileChangeListener (EarProject.this);
                }
                // Check up on build scripts.
                genFilesHelper.refreshBuildScript(
                    GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                    EarProject.class.getResource("resources/build-impl.xsl"),
                    true);
                genFilesHelper.refreshBuildScript(
                    getBuildXmlName (),
                    EarProject.class.getResource("resources/build.xsl"),
                    true);
                
                String servInstID = (String)wpp.get(EarProjectProperties.J2EE_SERVER_INSTANCE);
                J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(servInstID);
                if (platform == null) {
                    // if there is some server instance of the type which was used
                    // previously do not ask and use it
                    String serverType = (String)wpp.get(EarProjectProperties.J2EE_SERVER_TYPE);
                    if (serverType != null) {
                        String[] servInstIDs = Deployment.getDefault().getInstancesOfServer(serverType);
                        if (servInstIDs.length > 0) {
                            wpp.put(EarProjectProperties.J2EE_SERVER_INSTANCE, servInstIDs[0]);
                            wpp.store();
                            platform = Deployment.getDefault().getJ2eePlatform(servInstIDs[0]);
                        }
                    }
                    if (platform == null) {
                        BrokenServerSupport.showAlert();
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
            
            J2eeModuleProvider pwm = (J2eeModuleProvider) EarProject.this.getLookup ().lookup (J2eeModuleProvider.class);
            pwm.getConfigSupport().ensureConfigurationReady();
            
            // Make it easier to run headless builds on the same machine at least.
            ProjectManager.mutex().writeAccess(new Mutex.Action() {
                public Object run() {
                    EditableProperties ep = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    ep.setProperty("netbeans.user", System.getProperty("netbeans.user"));
                    helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                    try {
                        ProjectManager.getDefault().saveProject(EarProject.this);
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                    return null;
                }
            });
            if (J2eeArchiveLogicalViewProvider.hasBrokenLinks(helper, refHelper)) {
                BrokenReferencesSupport.showAlert();
            }
        }
        
        protected void projectClosed() {
            // Probably unnecessary, but just in case:
            try {
                ProjectManager.getDefault().saveProject(EarProject.this);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
            
            // unregister project's classpaths to GlobalPathRegistry
            ClassPathProviderImpl cpProvider = (ClassPathProviderImpl)lookup.lookup(ClassPathProviderImpl.class);
            GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
            GlobalPathRegistry.getDefault().unregister(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));
        }
        
    }

    private static final class RecommendedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates {
        
        // List of primarily supported templates
        
        private static final String[] TYPES = new String[] { 
            "XML",                  // NOI18N
            "simple-files"          // NOI18N
        };

        private static final String[] PRIVILEGED_NAMES = new String[] {
            "Templates/XML/XMLWizard",
            "Templates/Other/Folder"
        };
        public String[] getRecommendedTypes() {
            return TYPES;
        }
        public String[] getPrivilegedTemplates() {
            return PRIVILEGED_NAMES;
        }
    }
        
        class MyIconBaseProvider implements IconBaseProvider {
            public String getIconBase() {
                return "org/netbeans/modules/j2ee/earproject/ui/resources/";
            }
        }
        
        FileObject getFileObject(String propname) {
            String prop = helper.getStandardPropertyEvaluator().getProperty(propname);
            if (prop != null) {
                return helper.resolveFileObject(prop);
            } else {
                return null;
            }
        }

        File getFile(String propname) {
            String prop = helper.getStandardPropertyEvaluator().getProperty(propname);
            if (prop != null) {
                return helper.resolveFile(prop);
            } else {
                return null;
            }
        }

    public String getServerID () {
        return helper.getStandardPropertyEvaluator ().getProperty (EarProjectProperties.J2EE_SERVER_TYPE);
    }

    public String getServerInstanceID () {
        return helper.getStandardPropertyEvaluator ().getProperty (EarProjectProperties.J2EE_SERVER_INSTANCE);
    }
    
    public String getJ2eePlatformVersion () {
        return  helper.getStandardPropertyEvaluator ().getProperty (EarProjectProperties.J2EE_PLATFORM);
    }
    
    public EarProjectProperties getProjectProperties() {
        return new EarProjectProperties(this, updateHelper,eval, refHelper, abpt);
    }
    
}
