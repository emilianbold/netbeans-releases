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

package org.netbeans.modules.compapp.projects.jbi;

import org.netbeans.modules.compapp.projects.jbi.api.JbiProjectConstants;
import org.netbeans.modules.compapp.projects.jbi.api.JbiDefaultComponentInfo;
import org.netbeans.modules.compapp.projects.jbi.api.JbiProjectHelper;
import org.netbeans.modules.compapp.projects.jbi.ui.JbiCustomizerProvider;
import org.netbeans.modules.compapp.projects.jbi.ui.JbiLogicalViewProvider;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ant.*;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.*;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.compapp.projects.jbi.ComponentInfoGenerator;
import org.netbeans.modules.compapp.projects.jbi.queries.JbiProjectEncodingQueryImpl;
import org.netbeans.modules.sun.manager.jbi.management.model.ComponentInformationParser;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentDocument;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentStatus;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileUtil;

/**
 * Represents one ejb module project
 *
 * @author Chris Webster
 */
public final class JbiProject implements Project, AntProjectListener, ProjectPropertyProvider {
    private static final Icon PROJECT_ICON = new ImageIcon(
            Utilities.loadImage(
            "org/netbeans/modules/compapp/projects/jbi/ui/resources/composite_application_project.png" // NOI18N
            )
            ); // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String SOURCES_TYPE_JBI = "JBI"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String MODULE_INSTALL_NAME = "modules/org-netbeans-modules-compapp-projects-jbi.jar"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String MODULE_INSTALL_CBN = "org.netbeans.modules.compapp.projects.jbi"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String MODULE_INSTALL_DIR = "module.install.dir"; // NOI18N
    
//    /** Last time in ms when the Broken References alert was shown. */
//    private static long brokenAlertLastTime = 0;
//    
//    /** Is Broken References alert shown now? */
//    private static boolean brokenAlertShown = false;
//    
//    /** Timeout within which request to show alert will be ignored. */
//    private static int BROKEN_ALERT_TIMEOUT = 1000;
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final ReferenceHelper refHelper;
    private final GeneratedFilesHelper genFilesHelper;
    private final Lookup lookup;
    private AntBasedProjectType abpt;
    private JbiLogicalViewProvider lvp;    
    private FileChangeListener casaFileListener;
    
    private static final Logger LOG = Logger.getLogger(JbiProject.class.getName());
    
    
    /**
     * Creates a new JbiProject object.
     *
     * @param helper DOCUMENT ME!
     * @param abpt DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public JbiProject(final AntProjectHelper helper, AntBasedProjectType abpt)
    throws IOException {
        this.helper = helper;
        this.abpt = abpt;
        eval = createEvaluator();
        
        AuxiliaryConfiguration aux = helper.createAuxiliaryConfiguration();
        refHelper = new ReferenceHelper(helper, aux, helper.getStandardPropertyEvaluator());
        genFilesHelper = new GeneratedFilesHelper(helper);
        lookup = createLookup(aux);
        helper.addAntProjectListener(this);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {     
                JbiProject.this.lvp.refreshRootNode();
                CasaHelper.registerCasaFileListener(JbiProject.this);
            }
        });
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public AntBasedProjectType getAntBasedProjectType() {
        return abpt;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String toString() {
        return "JbiProject[" + getProjectDirectory() + "]"; // NOI18N
    }
    
    private PropertyEvaluator createEvaluator() {
        // XXX might need to use a custom evaluator to handle active platform substitutions... TBD
        return helper.getStandardPropertyEvaluator();
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public PropertyEvaluator evaluator() {
        return eval;
    }
    
    public ReferenceHelper getReferenceHelper() {
        return this.refHelper;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Lookup getLookup() {
        return lookup;
    }
    
    public AntProjectHelper getAntProjectHelper() {
        return helper;
    }
    
    private Lookup createLookup(AuxiliaryConfiguration aux) {
        SubprojectProvider spp = refHelper.createSubprojectProvider();
        FileBuiltQueryImplementation fileBuilt = helper.createGlobFileBuiltQuery(
                helper.getStandardPropertyEvaluator(), new String[] {"${src.dir}/*.java"}, // NOI18N
                new String[] {"${build.classes.dir}/*.class"} // NOI18N
        );
        final SourcesHelper sourcesHelper = new SourcesHelper(helper, evaluator());
        String webModuleLabel = org.openide.util.NbBundle.getMessage(
                JbiCustomizerProvider.class, "LBL_Node_EJBModule" // NOI18N
                );
        String srcJavaLabel = org.openide.util.NbBundle.getMessage(
                JbiCustomizerProvider.class, "LBL_Node_Sources" // NOI18N
                );
        
        sourcesHelper.addPrincipalSourceRoot(
                "${" + JbiProjectProperties.SOURCE_ROOT + "}", webModuleLabel, // NOI18N
                null, null
                );
        sourcesHelper.addPrincipalSourceRoot(
                "${" + JbiProjectProperties.SRC_DIR + "}", srcJavaLabel, // NOI18N
                null, null
                );
        
        sourcesHelper.addTypedSourceRoot(
                "${" + JbiProjectProperties.SRC_DIR + "}", SOURCES_TYPE_JBI, srcJavaLabel, // NOI18N
                null, null
                );
        sourcesHelper.addTypedSourceRoot(
                "${" + JbiProjectProperties.SRC_DIR + "}", JavaProjectConstants.SOURCES_TYPE_JAVA, // NOI18N
                srcJavaLabel, /*XXX*/
                null, null
                );
        ProjectManager.mutex().postWriteRequest(
                new Runnable() {
            public void run() {
                sourcesHelper.registerExternalRoots(
                        FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT
                        );
            }
        }
        );
        
        casaFileListener = new FileChangeAdapter() {
            public void fileChanged(FileEvent fe) {
                JbiProject.this.lvp.refreshRootNode();
            }
            public void fileDeleted(FileEvent fe) {
                JbiProject.this.lvp.refreshRootNode();
            }
        };
        
        return Lookups.fixed(new Object[] {
            new Info(),
            aux,
            helper.createCacheDirectoryProvider(),
            helper,
            spp,
            new JbiActionProvider(this, helper, refHelper),
            lvp = new JbiLogicalViewProvider(this, helper, evaluator(), spp, refHelper),
            new JbiCustomizerProvider(this, helper, refHelper),
            new AntArtifactProviderImpl(), 
            new ProjectXmlSavedHookImpl(),
            new ProjectOpenedHookImpl(),
            new JbiProjectOperations(this),
            new HashSet<TopComponent>(),
            fileBuilt,
            new RecommendedTemplatesImpl(),
            new JbiProjectEncodingQueryImpl(evaluator()),
            refHelper,
            sourcesHelper.createSources(),
            casaFileListener,
            helper.createSharabilityQuery(
                    evaluator(), new String[] {"${" + JbiProjectProperties.SOURCE_ROOT + "}"}, // NOI18N
                    new String[] {
                "${" + JbiProjectProperties.BUILD_DIR + "}", // NOI18N
                "${" + JbiProjectProperties.DIST_DIR + "}", // NOI18N
                "${" + JbiProjectProperties.TEST_RESULTS_DIR + "}", // NOI18N
                "${" + JbiProjectProperties.SRC_BUILD_DIR + "}" // NOI18N
            }
            )
        }
        );
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param ev DOCUMENT ME!
     */
    public void configurationXmlChanged(AntProjectEvent ev) {
        if (ev.getPath().equals(AntProjectHelper.PROJECT_XML_PATH)) {
            // Could be various kinds of changes, but name & displayName might have changed.
            Info info = (Info) getLookup().lookup(ProjectInformation.class);
            info.firePropertyChange(ProjectInformation.PROP_NAME);
            info.firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME);
        }
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param ev DOCUMENT ME!
     */
    public void propertiesChanged(AntProjectEvent ev) {
        if (lvp != null) {
            lvp.refreshRootNode();
        }
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getBuildXmlName() {
        String storedName = helper.getStandardPropertyEvaluator().getProperty(
                JbiProjectProperties.BUILD_FILE
                );
        
        return (storedName == null) ? GeneratedFilesHelper.BUILD_XML_PATH : storedName;
    }
    
    /**
     * Return the test directory of a JBI project
     *
     * @return JBI test directory
     */
    public FileObject getTestDirectory() {
        String testDir = helper.getStandardPropertyEvaluator().getProperty(JbiProjectProperties.TEST_DIR);
        
        try {
            return helper.resolveFileObject(testDir);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Create the test directory of a JBI project
     *
     * @return JBI test directory
     */
    public FileObject createTestDirectory() {
        String testDir = helper.getStandardPropertyEvaluator().getProperty(JbiProjectProperties.TEST_DIR); // NOI18N
        
        if (helper.resolveFileObject(testDir) == null) {
            try {
                getProjectDirectory().createFolder(testDir);
            } catch (java.io.IOException ex) {
                ex.printStackTrace();
            }
        }
        
        return helper.resolveFileObject(testDir);
    }
    
    /**
     * Return the test results directory of a JBI project
     *
     * @return JBI test results directory
     */
    public FileObject getTestResultsDirectory() {
        String testResultsDir = helper.getStandardPropertyEvaluator().getProperty(JbiProjectProperties.TEST_RESULTS_DIR);
        
        return helper.resolveFileObject(testResultsDir);
    }
    
    // Package private methods -------------------------------------------------
    FileObject getSourceDirectory() {
        String srcDir = helper.getStandardPropertyEvaluator().getProperty(JbiProjectProperties.SRC_DIR); // NOI18N
        
        return helper.resolveFileObject(srcDir);
    }
    
    /**
     * Return configured project name.
     *
     * @return DOCUMENT ME!
     */
    public String getName() {
        return (String) ProjectManager.mutex().readAccess(
                new Mutex.Action() {
            public Object run() {
                Element data = helper.getPrimaryConfigurationData(true);
                
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(
                        JbiProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name" // NOI18N
                        );
                
                if (nl.getLength() == 1) {
                    nl = nl.item(0).getChildNodes();
                    
                    if ((nl.getLength() == 1) && (nl.item(0).getNodeType() == Node.TEXT_NODE)) {
                        return ((Text) nl.item(0)).getNodeValue();
                    }
                }
                
                return "???"; // NOI18N
            }
        }
        );
    }
    
    /** Store configured project name. */
    public void setName(final String name) {
        ProjectManager.mutex().writeAccess(new Mutex.Action() {
            public Object run() {
                Element data = helper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(JbiProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(JbiProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                    data.insertBefore(nameEl, /* OK if null */data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                helper.putPrimaryConfigurationData(data, true);
                return null;
            }
        });
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public JbiProjectProperties getProjectProperties() {
        return new JbiProjectProperties(this, helper, refHelper);
    }
       
    // Private innerclasses ----------------------------------------------------
    private final class Info implements ProjectInformation {
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        
        /**
         * Creates a new Info object.
         */
        Info() {
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param prop DOCUMENT ME!
         */
        void firePropertyChange(String prop) {
            pcs.firePropertyChange(prop, null, null);
        }
        
        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public String getName() {
            return JbiProject.this.getName();
        }
        
        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public String getDisplayName() {
            return JbiProject.this.getName();
        }
        
        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public Icon getIcon() {
            return PROJECT_ICON;
        }
        
        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public Project getProject() {
            return JbiProject.this;
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param listener DOCUMENT ME!
         */
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param listener DOCUMENT ME!
         */
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }
    }
    
    private final class ProjectXmlSavedHookImpl extends ProjectXmlSavedHook {
        /**
         * Creates a new ProjectXmlSavedHookImpl object.
         */
        ProjectXmlSavedHookImpl() {
        }
        
        /**
         * DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        protected void projectXmlSaved() throws IOException {
            genFilesHelper.refreshBuildScript(
                    GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                    JbiProject.class.getResource("resources/build-impl.xsl"), false // NOI18N
                    );
            genFilesHelper.refreshBuildScript(
                    getBuildXmlName(), JbiProject.class.getResource("resources/build.xsl"), false // NOI18N
                    );
        }
    }
    
    private final class ProjectOpenedHookImpl extends ProjectOpenedHook {
        /**
         * Creates a new ProjectOpenedHookImpl object.
         */
        ProjectOpenedHookImpl() {
        }
        
        /**
         * DOCUMENT ME!
         */
        protected void projectOpened() {
            try {
                // Check up on build scripts.
                genFilesHelper.refreshBuildScript(
                        GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                        JbiProject.class.getResource("resources/build-impl.xsl"), true // NOI18N
                        );
                genFilesHelper.refreshBuildScript(
                        getBuildXmlName(), 
                        JbiProject.class.getResource("resources/build.xsl"), true // NOI18N
                        );
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            
            // Make it easier to run headless builds on the same machine at least.
            ProjectManager.mutex().writeAccess(
                    new Mutex.Action() {
                public Object run() {
                         
                    // 1. Update component info and binding component info if needed...
                    String name = JbiProject.this.getName();
                    // On Solaris x86, JbiProject.this.getProjectDirectory().getPath() 
                    // is missing the preceding "/".
                    //System.out.println("projPath is " + JbiProject.this.getProjectDirectory().getPath());
                    //System.out.println("projPath (absolute) is " + FileUtil.toFile(JbiProject.this.getProjectDirectory()).getAbsolutePath());
                    //String confDir = JbiProject.this.getProjectDirectory().getPath() + "/src/conf"; // NOI18N
                    FileObject projDir = JbiProject.this.getProjectDirectory();
                    String confDir = FileUtil.toFile(projDir).getAbsolutePath() + 
                            File.separator + "src" + File.separator + "conf"; // NOI18N                    
                    updateComponentDocuments(confDir);
                    
                    // 2. Make sure the component target list is not corrupted.
                    JbiProjectProperties projectProperties = getProjectProperties(); 
                    try {
                        projectProperties.fixComponentTargetList();
                    } catch (Exception e) {
                        // The failure is probably due to unresolved references.
                        // Once the reference problem is fixed, we will try
                        // fixing the component target list again.
                        return null;
                    }
                    
                    // 3.1 Migrate old casa.wsdl to <Proj>.wsdl, if applicable
                    String projDirLoc = JbiProject.this.getProjectDirectory().getPath();
                    String srcDirLoc =
                            projDirLoc + File.separator +
                            helper.getStandardPropertyEvaluator().getProperty(
                            JbiProjectProperties.SOURCE_ROOT);
                    String projName = JbiProjectHelper.getJbiProjectName(JbiProject.this);
                    MigrationHelper.migrateCasaWSDL(srcDirLoc, projName);
                    
                    // 3.2 Migrate old compapp properties
                    EditableProperties projectEP = helper.getProperties(
                            AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    MigrationHelper.migrateCompAppProperties(projDirLoc, projectEP);
                    
                    // 3.3 Add project encoding for old projects
                    if (projectEP.getProperty(JbiProjectProperties.SOURCE_ENCODING) == null) {
                        projectEP.setProperty(JbiProjectProperties.SOURCE_ENCODING,
                                // FIXME: maybe we should use Charset.defaultCharset() instead?
                                // See comments in JbiProjectEncodingQueryImpl.java
                                FileEncodingQuery.getDefaultEncoding().name());
                    }
                    
                    helper.putProperties(
                            AntProjectHelper.PROJECT_PROPERTIES_PATH, projectEP);
                    
                    // 4. Set private properties
                    EditableProperties privateEP = helper.getProperties(
                            AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    privateEP.setProperty(
                            "netbeans.user", System.getProperty("netbeans.user")); // NOI18N
                    
                    File f = InstalledFileLocator.getDefault().locate(
                            MODULE_INSTALL_NAME, MODULE_INSTALL_CBN, false);                    
                    if (f != null) {
                        privateEP.setProperty(MODULE_INSTALL_DIR, f.getParentFile().getPath());
                    }
                    
                    helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateEP);
                    
                    try {
                        ProjectManager.getDefault().saveProject(JbiProject.this);
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                    
                    // 5. Create casa file if it doesn't exist yet.
                    CasaHelper.getCasaFileObject(JbiProject.this, true);
                                                        
                    // 6. Update ASI.xml
                    getProjectProperties().saveAssemblyInfo();
                    
                    return null;
                }
            }
            );
            
            if (JbiLogicalViewProvider.hasBrokenLinks(helper, refHelper)) {
                BrokenReferencesSupport.showAlert();
            }
            
            String prop = eval.getProperty(JbiProjectProperties.SOURCE_ENCODING);
            if (prop != null) {
                try {
                    Charset c = Charset.forName(prop);
                } catch (IllegalCharsetNameException e) {
                    //Broken property, log & ignore
                    LOG.warning("Illegal charset: " + prop+ " in project: " + // NOI18N
                            FileUtil.getFileDisplayName(getProjectDirectory())); 
                } catch (UnsupportedCharsetException e) {
                    //todo: Needs UI notification like broken references.
                    LOG.warning("Unsupported charset: " + prop+ " in project: " + // NOI18N
                            FileUtil.getFileDisplayName(getProjectDirectory())); 
                }
            }
        }
        
        private void updateComponentDocuments(String confDir) {
            
            try {
                // Load component info..
                File compFile = new File(confDir + File.separator + "ComponentInformation.xml");
                JBIComponentDocument compDoc = ComponentInformationParser.parse(compFile);
                List<JBIComponentStatus> compList = compDoc.getJbiComponentList();
                
                // Load binding component info..
                File bindingCompFile = new File(confDir + File.separator + "BindingComponentInformation.xml");
                JBIComponentDocument bindingCompDoc = ComponentInformationParser.parse(bindingCompFile);
                List<JBIComponentStatus> bindingCompList = bindingCompDoc.getJbiComponentList();
                
                // Update component namespaces using info from binding component doc
                for (JBIComponentStatus bindingComp : bindingCompList) {
                    String name = bindingComp.getName();
                    List<String> nsList = bindingComp.getNamespaceList();
                    for (JBIComponentStatus comp : compList) {
                        if (comp.getName().equals(name)) {
                            comp.setNamespace(nsList);
                            break;
                        }
                    }
                }
                
                // Get known SE/BCs at design-time
                JbiDefaultComponentInfo defaultCompInfo =
                        JbiDefaultComponentInfo.getJbiDefaultComponentInfo();
                Map<String, JBIComponentStatus> defaultCompInfoMap =
                        defaultCompInfo.getComponentHash();
                
                List<JBIComponentStatus> deltaList = new ArrayList<JBIComponentStatus>();
                
                boolean nsListUpdated = false;
                
                for (String name : defaultCompInfoMap.keySet()) {
                    JBIComponentStatus compInMap = defaultCompInfoMap.get(name);
                    boolean found = false;
                    for (JBIComponentStatus comp : compList) {
                        if (comp.getName().equals(name)) {
                            for (String ns : compInMap.getNamespaceList()) {
                                if (comp.addNamespace(ns)) {
                                    nsListUpdated = true;
                                }
                            }
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        deltaList.add(compInMap);
                    }
                }
                
                if (deltaList != null && deltaList.size() > 0 || nsListUpdated) {
                    List<JBIComponentStatus> list = new ArrayList<JBIComponentStatus>();
                    list.addAll(compList);
                    list.addAll(deltaList);
                    new ComponentInfoGenerator(confDir, list).doIt();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        /**
         * DOCUMENT ME!
         */
        protected void projectClosed() {
            Set topComponentSet = (Set) JbiProject.this.getLookup().lookup(HashSet.class);
            if (topComponentSet != null) {
                for (Object tc : topComponentSet) {
                    if (tc instanceof TopComponent) {
                        ((TopComponent)tc).close();
                    }
                }
            }
            
            // Probably unnecessary, but just in case:
            try {
                ProjectManager.getDefault().saveProject(JbiProject.this);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }
    
    /**
     * Exports the main JAR as an official build product for use from other scripts. The type of
     * the artifact will be {@link AntArtifact}.
     */
    private final class AntArtifactProviderImpl implements AntArtifactProvider {
        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public AntArtifact[] getBuildArtifacts() {
            return new AntArtifact[] {
                helper.createSimpleAntArtifact(
                        JbiProjectConstants.ARTIFACT_TYPE_JBI_AU, "dist.jar", // NOI18N
                        helper.getStandardPropertyEvaluator(), "dist", "clean" // NOI18N
                        ), // NOI18N
            };
        }
    }
    
    private static final class RecommendedTemplatesImpl 
            implements RecommendedTemplates, PrivilegedTemplates {
        
        // List of primarily supported templates
        private static final String[] TYPES = new String[] {
            "XML", // NOI18N
            "simple-files" // NOI18N
        };
        
        private static final String[] PRIVILEGED_NAMES = new String[] {
            "Templates/XML/WSDL.wsdl",    // NOI18N
            "Templates/XML/XmlSchema.xsd", // NOI18N
            "Templates/XML/retrieveSchemaResource", // NOI18N
            "Templates/XML/retrieveWSDLResource", // NOI18N
        };
        
        public String[] getRecommendedTypes() {
            return TYPES;
        }
        
        public String[] getPrivilegedTemplates() {
            return PRIVILEGED_NAMES;
        }
    }
}
