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

package org.netbeans.modules.j2ee.earproject;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.earproject.classpath.ClassPathProviderImpl;
import org.netbeans.modules.j2ee.earproject.ui.EarCustomizerProvider;
import org.netbeans.modules.j2ee.earproject.ui.IconBaseProvider;
import org.netbeans.modules.j2ee.earproject.ui.J2eeArchiveLogicalViewProvider;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.netbeans.modules.j2ee.earproject.ui.customizer.VisualClassPathItem;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarFactory;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.ant.AntBuildExtenderFactory;
import org.netbeans.spi.project.ant.AntBuildExtenderImplementation;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
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
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Represents an Enterprise Application project.
 *
 * This is the project api centric view of the enterprise application.
 *
 * @author vince kraemer
 */
public final class EarProject implements Project, AntProjectListener, FileChangeListener, ProjectPropertyProvider {
    
    private static final Icon EAR_PROJECT_ICON = new ImageIcon(Utilities.loadImage("org/netbeans/modules/j2ee/earproject/ui/resources/projectIcon.gif")); // NOI18N
    public static final String ARTIFACT_TYPE_EAR = "ear";
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final ReferenceHelper refHelper;
    private final GeneratedFilesHelper genFilesHelper;
    private final Lookup lookup;
    private final ProjectEar appModule;
    private final Ear ear;
    private final AntBasedProjectType abpt;
    private final UpdateHelper updateHelper;
    private final BrokenProjectSupport brokenProjectSupport;
    
    private AntBuildExtender buildExtender;
            
    EarProject(final AntProjectHelper helper, AntBasedProjectType abpt) throws IOException {
        this.helper = helper;
        this.abpt = abpt;
        eval = createEvaluator();
        AuxiliaryConfiguration aux = helper.createAuxiliaryConfiguration();
        refHelper = new ReferenceHelper(helper, aux, helper.getStandardPropertyEvaluator());
        buildExtender = AntBuildExtenderFactory.createAntExtender(new EarExtenderImplementation());
        genFilesHelper = new GeneratedFilesHelper(helper,buildExtender);
        appModule = new ProjectEar(this);
        ear = EjbJarFactory.createEar(appModule);
        updateHelper = new UpdateHelper(this, this.helper, aux, this.genFilesHelper, UpdateHelper.createDefaultNotifier());
        brokenProjectSupport = new BrokenProjectSupport(this);
        lookup = createLookup(aux);
   }
    
    public UpdateHelper getUpdateHelper() {
        return updateHelper;
    }
    
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }
    
    @Override
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
        
        // XXX unnecessarily creates a SourcesHelper, which is then GC's
        // as it is not hold. This is probably unneeded now that issue 63359 was fixed.
        final SourcesHelper sourcesHelper = new SourcesHelper(helper, evaluator());
        String configFilesLabel = NbBundle.getMessage(EarProject.class, "LBL_Node_ConfigBase"); //NOI18N
        
        sourcesHelper.addPrincipalSourceRoot("${"+EarProjectProperties.META_INF+"}", configFilesLabel, /*XXX*/null, null); // NOI18N
        ProjectManager.mutex().postWriteRequest(new Runnable() {
            public void run() {
                sourcesHelper.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
            }
        });
        Lookup base = Lookups.fixed(new Object[] {
            new Info(),
            aux,
            spp,
            new ProjectEarProvider(),
            appModule, //implements J2eeModuleProvider
            new EarActionProvider(this, updateHelper),
            new J2eeArchiveLogicalViewProvider(this, updateHelper, evaluator(), refHelper, abpt),
            new MyIconBaseProvider(),
            new EarCustomizerProvider( this, helper, refHelper, abpt ),
            new ClassPathProviderImpl(helper, evaluator()),
            new ProjectXmlSavedHookImpl(),
            UILookupMergerSupport.createProjectOpenHookMerger(new ProjectOpenedHookImpl()),
            new EarSources(helper, evaluator()),
            new RecommendedTemplatesImpl(),
            helper.createSharabilityQuery(evaluator(),
                    new String[] {"${"+EarProjectProperties.SOURCE_ROOT+"}"}, // NOI18N
                    new String[] {
                "${"+EarProjectProperties.BUILD_DIR+"}", // NOI18N
                "${"+EarProjectProperties.DIST_DIR+"}"} // NOI18N
            ),
            this,
            new EarProjectOperations(this),
            brokenProjectSupport,
            new AntArtifactProviderImpl(),
            UILookupMergerSupport.createPrivilegedTemplatesMerger(),
            UILookupMergerSupport.createRecommendedTemplatesMerger(),
            LookupProviderSupport.createSourcesMerger(),
            buildExtender,
        });
        return LookupProviderSupport.createCompositeLookup(base, "Projects/org-netbeans-modules-j2ee-earproject/Lookup"); //NOI18N
    }
    
    public void configurationXmlChanged(AntProjectEvent ev) {
    }
    
    public void propertiesChanged(AntProjectEvent ev) {
        // currently ignored
        //TODO: should not be ignored!
    }
    
    public String getBuildXmlName() {
        String storedName = helper.getStandardPropertyEvaluator().getProperty(EarProjectProperties.BUILD_FILE);
        return storedName == null ? GeneratedFilesHelper.BUILD_XML_PATH : storedName;
    }
    
    // Package private methods -------------------------------------------------
    
    public ProjectEar getAppModule() {
        return appModule;
    }
    
    public Ear getEar() {
        return ear;
    }
    
    public void fileAttributeChanged(FileAttributeEvent fe) {
    }
    
    public void fileChanged(FileEvent fe) {
    }
    
    public void fileDataCreated(FileEvent fe) {
    }
    
    public void fileDeleted(FileEvent fe) {
    }
    
    public void fileFolderCreated(FileEvent fe) {
    }
    
    public void fileRenamed(FileRenameEvent fe) {
    }
    
    /** Return configured project name. */
    public String getName() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<String>() {
            public String run() {
                Element data = updateHelper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(EarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
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
        ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
            public Void run() {
                Element data = helper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(EarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(EarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                    data.insertBefore(nameEl, /* OK if null */data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                helper.putPrimaryConfigurationData(data, true);
                return null;
            }
        });
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
            String dn = EarProject.this.getName();
            synchronized (pcs) {
                cachedName = new WeakReference<String>(dn);
            }
            return dn;
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
                    getBuildXmlName(),
                    EarProject.class.getResource("resources/build.xsl"),
                    false);
        }
        
    }
    
    private boolean addLibrary(List<VisualClassPathItem> cpItems, FileObject lib) {
        boolean needsAdding = true;
        for (Iterator vcpsIter = cpItems.iterator(); vcpsIter.hasNext();) {
            VisualClassPathItem vcpi = (VisualClassPathItem) vcpsIter.next();
            
            if (vcpi.getType() != VisualClassPathItem.Type.JAR) {
                continue;
            }
            FileObject fo = FileUtil.toFileObject(new File(helper.getStandardPropertyEvaluator().evaluate(vcpi.getEvaluated())));
            if (lib.equals(fo)) {
                needsAdding = false;
                break;
            }
        }
        if (needsAdding) {
            String file = "${"+EarProjectProperties.LIBRARIES_DIR+"}/"+lib.getNameExt(); // NOI18N
            VisualClassPathItem cpItem = VisualClassPathItem.createClassPath(
                    file,  VisualClassPathItem.PATH_IN_WAR_LIB);
            cpItems.add(cpItem);
        }
        return needsAdding;
    }
    
    /** Package-private for unit tests only. */
    final class ProjectOpenedHookImpl extends ProjectOpenedHook {
        
        ProjectOpenedHookImpl() {}
        
        protected void projectOpened() {
            try {
                //Check libraries and add them to classpath automatically
                String libFolderName = helper.getStandardPropertyEvaluator().getProperty(EarProjectProperties.LIBRARIES_DIR);
                EarProjectProperties epp = new EarProjectProperties(EarProject.this, refHelper, abpt);
                getAppModule().setModules(epp.getModuleMap());
                if (libFolderName != null && new File(libFolderName).isDirectory()) {
                    @SuppressWarnings("unchecked")
                    List<VisualClassPathItem> cpItems = (List<VisualClassPathItem>) epp.get(EarProjectProperties.JAVAC_CLASSPATH);
                    FileObject libFolder = FileUtil.toFileObject(new File(libFolderName));
                    FileObject libs [] = libFolder.getChildren();
                    boolean anyChanged = false;
                    for (int i = 0; i < libs.length; i++) {
                        anyChanged = addLibrary(cpItems, libs [i]) || anyChanged;
                    }
                    if (anyChanged) {
                        epp.put(EarProjectProperties.JAVAC_CLASSPATH, cpItems);
                        epp.store();
                        ProjectManager.getDefault().saveProject(EarProject.this);
                    }
                    libFolder.addFileChangeListener(EarProject.this);
                }
                // Check up on build scripts.
                genFilesHelper.refreshBuildScript(
                        GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                        EarProject.class.getResource("resources/build-impl.xsl"),
                        true);
                genFilesHelper.refreshBuildScript(
                        getBuildXmlName(),
                        EarProject.class.getResource("resources/build.xsl"),
                        true);
                
                epp.ensurePlatformIsSet(true);
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
            
            // register project's classpaths to GlobalPathRegistry
            ClassPathProviderImpl cpProvider = lookup.lookup(ClassPathProviderImpl.class);
            GlobalPathRegistry.getDefault().register(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));
            
            J2eeModuleProvider pwm = EarProject.this.getLookup().lookup(J2eeModuleProvider.class);
            pwm.getConfigSupport().ensureConfigurationReady();
            
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
                
            } catch (IOException e ) {
                Exceptions.printStackTrace(e);
            }
            
            if (J2eeArchiveLogicalViewProvider.hasBrokenLinks(helper, refHelper)) {
                BrokenReferencesSupport.showAlert();
            }
        }
        
        private void updateProject() {
            // Make it easier to run headless builds on the same machine at least.
            EditableProperties ep = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
            ep.setProperty("netbeans.user", System.getProperty("netbeans.user"));
            helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
            try {
                ProjectManager.getDefault().saveProject(EarProject.this);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        
        protected void projectClosed() {
            // Probably unnecessary, but just in case:
            try {
                ProjectManager.getDefault().saveProject(EarProject.this);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
            
            brokenProjectSupport.cleanUp();
            
            // unregister project's classpaths to GlobalPathRegistry
            ClassPathProviderImpl cpProvider = lookup.lookup(ClassPathProviderImpl.class);
            GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            GlobalPathRegistry.getDefault().unregister(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));
        }
        
    }
    
    private static final class RecommendedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates {
        
        // List of primarily supported templates
        
        private static final String[] TYPES = new String[] {
            "XML",                  // NOI18N
            "ear-types",            // NOI18N
            "wsdl",                 // NOI18N
            "simple-files",         // NOI18N
            "ant-script",           // NOI18N
            "ant-task",             // NOI18N
        };
        
        private static final String[] PRIVILEGED_NAMES = new String[] {
            "Templates/J2EE/ApplicationXml",                // NOI18N
            "deployment-descriptor",                // NOI18N
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
            return "org/netbeans/modules/j2ee/earproject/ui/resources/"; // NOI18N
        }
    }
    
    /** May return <code>null</code>. */
    public FileObject getOrCreateMetaInfDir() {
        String metaInfProp = helper.getStandardPropertyEvaluator().
                getProperty(EarProjectProperties.META_INF);
        if (metaInfProp == null) {
            // IZ 91941
            // does project.properties exist? if yes, something is probably wrong...
            File projectProperties = helper.resolveFile(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            if (projectProperties.exists()) {
                // file exists, log warning
                Logger.getLogger("global").log(Level.WARNING,
                        "Cannot resolve " + EarProjectProperties.META_INF + // NOI18N
                        " property for " + this); // NOI18N
            }
            return null;
        }
        FileObject metaInfFO = null;
        try {
            File prjDirF = FileUtil.toFile(getProjectDirectory());
            File rootF = prjDirF;
            while (rootF.getParentFile() != null) {
                rootF = rootF.getParentFile();
            }
            File metaInfF = PropertyUtils.resolveFile(prjDirF, metaInfProp);
            String metaInfPropRel = PropertyUtils.relativizeFile(rootF, metaInfF);
            assert metaInfPropRel != null;
            metaInfFO = FileUtil.createFolder(FileUtil.toFileObject(rootF), metaInfPropRel);
        } catch (IOException ex) {
            assert false : ex;
        }
        return metaInfFO;
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
    
    public String getServerID() {
        return helper.getStandardPropertyEvaluator().getProperty(EarProjectProperties.J2EE_SERVER_TYPE);
    }
    
    public String getServerInstanceID() {
        return helper.getStandardPropertyEvaluator().getProperty(EarProjectProperties.J2EE_SERVER_INSTANCE);
    }
    
    public String getJ2eePlatformVersion() {
        return  helper.getStandardPropertyEvaluator().getProperty(EarProjectProperties.J2EE_PLATFORM);
    }
    
    public EarProjectProperties getProjectProperties() {
        return new EarProjectProperties(this, refHelper, abpt);
    }
    
    public GeneratedFilesHelper getGeneratedFilesHelper() {
        return genFilesHelper;
    }
    
    private final class AntArtifactProviderImpl implements AntArtifactProvider{
        public AntArtifact[] getBuildArtifacts() {
            return new AntArtifact[] {
                helper.createSimpleAntArtifact(ARTIFACT_TYPE_EAR, "dist.jar", evaluator(), "dist", "clean"), // NOI18N
            };
        }
        
    }
    
   private class EarExtenderImplementation implements AntBuildExtenderImplementation {
        //add targets here as required by the external plugins..
        public List<String> getExtensibleTargets() {
            String[] targets = new String[] {
                "pre-dist", //NOI18N
            };
            return Arrays.asList(targets);
        }

        public Project getOwningProject() {
            return EarProject.this;
        }

    }
}
