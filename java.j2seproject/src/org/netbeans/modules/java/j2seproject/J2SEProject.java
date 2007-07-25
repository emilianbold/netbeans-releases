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

package org.netbeans.modules.java.j2seproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.modules.java.j2seproject.classpath.ClassPathProviderImpl;
import org.netbeans.modules.java.j2seproject.classpath.J2SEProjectClassPathExtender;
import org.netbeans.modules.java.j2seproject.classpath.J2SEProjectClassPathModifier;
import org.netbeans.modules.java.j2seproject.queries.CompiledSourceForBinaryQuery;
import org.netbeans.modules.java.j2seproject.queries.JavadocForBinaryQueryImpl;
import org.netbeans.modules.java.j2seproject.queries.SourceLevelQueryImpl;
import org.netbeans.modules.java.j2seproject.queries.UnitTestForSourceQueryImpl;
import org.netbeans.modules.java.j2seproject.ui.J2SELogicalViewProvider;
import org.netbeans.modules.java.j2seproject.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;
import org.netbeans.modules.java.j2seproject.queries.J2SEProjectEncodingQueryImpl;
import org.netbeans.modules.java.j2seproject.queries.BinaryForSourceQueryImpl;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.ant.AntBuildExtenderFactory;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.ant.AntBuildExtenderImplementation;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.FilterPropertyProvider;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Represents one plain J2SE project.
 * @author Jesse Glick, et al.
 */
public final class J2SEProject implements Project, AntProjectListener {
    
    private static final Icon J2SE_PROJECT_ICON = new ImageIcon(Utilities.loadImage("org/netbeans/modules/java/j2seproject/ui/resources/j2seProject.png")); // NOI18N
    private static final Logger LOG = Logger.getLogger(J2SEProject.class.getName());

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

    private AntBuildExtender buildExtender;

    J2SEProject(AntProjectHelper helper) throws IOException {
        this.helper = helper;
        eval = createEvaluator();
        aux = helper.createAuxiliaryConfiguration();
        refHelper = new ReferenceHelper(helper, aux, eval);
        buildExtender = AntBuildExtenderFactory.createAntExtender(new J2SEExtenderImplementation());
    /// TODO replace this GeneratedFilesHelper with the default one when fixing #101710
        genFilesHelper = new GeneratedFilesHelper(helper, buildExtender);
        this.updateHelper = new UpdateHelper (this, this.helper, this.aux, this.genFilesHelper,
            UpdateHelper.createDefaultNotifier());

        lookup = createLookup(aux);
        helper.addAntProjectListener(this);
    }

    /**
     * Returns the project directory
     * @return the directory the project is located in
     */
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }

    public String toString() {
        return "J2SEProject[" + FileUtil.getFileDisplayName(getProjectDirectory()) + "]"; // NOI18N
    }
    
    private PropertyEvaluator createEvaluator() {
        // It is currently safe to not use the UpdateHelper for PropertyEvaluator; UH.getProperties() delegates to APH
        // Adapted from APH.getStandardPropertyEvaluator (delegates to ProjectProperties):
        PropertyEvaluator baseEval1 = PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(),
                helper.getPropertyProvider(J2SEConfigurationProvider.CONFIG_PROPS_PATH));
        PropertyEvaluator baseEval2 = PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(),
                helper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH));
        return PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(),
                helper.getPropertyProvider(J2SEConfigurationProvider.CONFIG_PROPS_PATH),
                new ConfigPropertyProvider(baseEval1, "nbproject/private/configs", helper), // NOI18N
                helper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH),
                PropertyUtils.userPropertiesProvider(baseEval2,
                    "user.properties.file", FileUtil.toFile(getProjectDirectory())), // NOI18N
                new ConfigPropertyProvider(baseEval1, "nbproject/configs", helper), // NOI18N
                helper.getPropertyProvider(AntProjectHelper.PROJECT_PROPERTIES_PATH));
    }
    private static final class ConfigPropertyProvider extends FilterPropertyProvider implements PropertyChangeListener {
        private final PropertyEvaluator baseEval;
        private final String prefix;
        private final AntProjectHelper helper;
        public ConfigPropertyProvider(PropertyEvaluator baseEval, String prefix, AntProjectHelper helper) {
            super(computeDelegate(baseEval, prefix, helper));
            this.baseEval = baseEval;
            this.prefix = prefix;
            this.helper = helper;
            baseEval.addPropertyChangeListener(this);
        }
        public void propertyChange(PropertyChangeEvent ev) {
            if (J2SEConfigurationProvider.PROP_CONFIG.equals(ev.getPropertyName())) {
                setDelegate(computeDelegate(baseEval, prefix, helper));
            }
        }
        private static PropertyProvider computeDelegate(PropertyEvaluator baseEval, String prefix, AntProjectHelper helper) {
            String config = baseEval.getProperty(J2SEConfigurationProvider.PROP_CONFIG);
            if (config != null) {
                return helper.getPropertyProvider(prefix + "/" + config + ".properties"); // NOI18N
            } else {
                return PropertyUtils.fixedPropertyProvider(Collections.<String,String>emptyMap());
            }
        }
    }
    
    public PropertyEvaluator evaluator() {
        return eval;
    }

    ReferenceHelper getReferenceHelper () {
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

    private Lookup createLookup(AuxiliaryConfiguration aux) {
        SubprojectProvider spp = refHelper.createSubprojectProvider();
        final J2SEProjectClassPathModifier cpMod = new J2SEProjectClassPathModifier(this, this.updateHelper, eval, refHelper);
        ClassPathProviderImpl cpProvider = new ClassPathProviderImpl(this.helper, evaluator(), getSourceRoots(),getTestSourceRoots()); //Does not use APH to get/put properties/cfgdata
        Lookup base = Lookups.fixed(new Object[] {
            J2SEProject.this,
            new Info(),
            aux,
            helper.createCacheDirectoryProvider(),
            spp,
            new J2SEActionProvider( this, this.updateHelper ),
            new J2SELogicalViewProvider(this, this.updateHelper, evaluator(), spp, refHelper),
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
            new J2SESources (this.helper, evaluator(), getSourceRoots(), getTestSourceRoots()),
            new J2SESharabilityQuery (this.helper, evaluator(), getSourceRoots(), getTestSourceRoots()), //Does not use APH to get/put properties/cfgdata
            new J2SEFileBuiltQuery (this.helper, evaluator(),getSourceRoots(),getTestSourceRoots()), //Does not use APH to get/put properties/cfgdata
            new RecommendedTemplatesImpl (this.updateHelper),
            new J2SEProjectClassPathExtender(cpMod),
            buildExtender,
            cpMod,
            this, // never cast an externally obtained Project to J2SEProject - use lookup instead
            new J2SEProjectOperations(this),
            new J2SEConfigurationProvider(this),
            new J2SEPersistenceProvider(this, cpProvider),
            UILookupMergerSupport.createPrivilegedTemplatesMerger(),
            UILookupMergerSupport.createRecommendedTemplatesMerger(),
            LookupProviderSupport.createSourcesMerger(),
            new J2SEProjectEncodingQueryImpl (evaluator()),
            new J2SEPropertyEvaluatorImpl(evaluator()),
            new J2SETemplateAttributesProvider(this.helper),
            new BinaryForSourceQueryImpl(this.sourceRoots, this.testRoots, this.helper, this.eval) //Does not use APH to get/put properties/cfgdata
        });
        return LookupProviderSupport.createCompositeLookup(base, "Projects/org-netbeans-modules-java-j2seproject/Lookup"); //NOI18N
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
        String testClassesDir = evaluator().getProperty(J2SEProjectProperties.BUILD_TEST_CLASSES_DIR);
        if (testClassesDir == null) {
            return null;
        }
        return helper.resolveFile(testClassesDir);
    }
    
    // Currently unused (but see #47230):
    /** Store configured project name. */
    public void setName(final String name) {
        ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
            public Void run() {
                Element data = helper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
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
            String dn = ProjectManager.mutex().readAccess(new Mutex.Action<String>() {
                public String run() {
                    Element data = updateHelper.getPrimaryConfigurationData(true);
                    // XXX replace by XMLUtil when that has findElement, findText, etc.
                    NodeList nl = data.getElementsByTagNameNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
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
            return J2SE_PROJECT_ICON;
        }
        
        public Project getProject() {
            return J2SEProject.this;
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
            //May be called by {@link AuxiliaryConfiguration#putConfigurationFragment}
            //which didn't affect the j2seproject 
            if (updateHelper.isCurrent()) {
                //Refresh build-impl.xml only for j2seproject/2
                genFilesHelper.refreshBuildScript(
                    GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                    J2SEProject.class.getResource("resources/build-impl.xsl"),
                    false);
                genFilesHelper.refreshBuildScript(
                    GeneratedFilesHelper.BUILD_XML_PATH,
                    J2SEProject.class.getResource("resources/build.xsl"),
                    false);
            }
        }
        
    }
    
    private final class ProjectOpenedHookImpl extends ProjectOpenedHook {
        
        ProjectOpenedHookImpl() {}
        private static final String JAX_RPC_NAMESPACE="http://www.netbeans.org/ns/j2se-project/jax-rpc"; //NOI18N
        private static final String JAX_RPC_CLIENTS="web-service-clients"; //NOI18N
        private static final String JAX_RPC_CLIENT="web-service-client"; //NOI18N
        
        protected void projectOpened() {
            // Check up on build scripts.
            try {
                if (updateHelper.isCurrent()) {
                    //Refresh build-impl.xml only for j2seproject/2
                    genFilesHelper.refreshBuildScript(
                        GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                        J2SEProject.class.getResource("resources/build-impl.xsl"),
                        true);
                    genFilesHelper.refreshBuildScript(
                        GeneratedFilesHelper.BUILD_XML_PATH,
                        J2SEProject.class.getResource("resources/build.xsl"),
                        true);
                }                
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            
            // register project's classpaths to GlobalPathRegistry
            ClassPathProviderImpl cpProvider = lookup.lookup(ClassPathProviderImpl.class);
            GlobalPathRegistry.getDefault().register(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
            GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));

            //register updater of main.class
            //the updater is active only on the opened projects
	    mainClassUpdater = new MainClassUpdater (J2SEProject.this, eval, updateHelper,
                    cpProvider.getProjectClassPaths(ClassPath.SOURCE)[0], J2SEProjectProperties.MAIN_CLASS);

            // Make it easier to run headless builds on the same machine at least.
            try {
                getProjectDirectory().getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                    public void run () throws IOException {
                        ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
                            public Void run() {
                                EditableProperties ep = updateHelper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                                File buildProperties = new File(System.getProperty("netbeans.user"), "build.properties"); // NOI18N
                                ep.setProperty("user.properties.file", buildProperties.getAbsolutePath()); //NOI18N

                                // set jaxws.endorsed.dir property (for endorsed mechanism to be used with wsimport, wsgen)
                                setJaxWsEndorsedDirProperty(ep);

                                // move web-service-clients one level up from in project.xml
                                // WS should be part of auxiliary configuration
                                Element data = helper.getPrimaryConfigurationData(true);
                                NodeList nodes = data.getElementsByTagName(JAX_RPC_CLIENTS);
                                if(nodes.getLength() > 0) {                        
                                    Element oldJaxRpcClients = (Element) nodes.item(0);
                                    Document doc = createNewDocument();
                                    Element newJaxRpcClients = doc.createElementNS(JAX_RPC_NAMESPACE, JAX_RPC_CLIENTS);
                                    NodeList childNodes = oldJaxRpcClients.getElementsByTagName(JAX_RPC_CLIENT);
                                    for (int i=0;i<childNodes.getLength();i++) {                            
                                        Element oldJaxRpcClient = (Element) childNodes.item(i);
                                        Element newJaxRpcClient = doc.createElementNS(JAX_RPC_NAMESPACE, JAX_RPC_CLIENT);
                                        NodeList nodeProps = oldJaxRpcClient.getChildNodes();
                                        for (int j=0;j<nodeProps.getLength();j++) {
                                            Node n = nodeProps.item(j);
                                            if (n instanceof Element) {
                                                Element oldProp = (Element) n;
                                                Element newProp = doc.createElementNS(JAX_RPC_NAMESPACE, oldProp.getLocalName());
                                                String text = oldProp.getTextContent();
                                                newProp.setTextContent(text);
                                                newJaxRpcClient.appendChild(newProp);
                                            }
                                        }
                                        newJaxRpcClients.appendChild(newJaxRpcClient);
                                    }
                                    aux.putConfigurationFragment(newJaxRpcClients, true);
                                    data.removeChild(oldJaxRpcClients);
                                    helper.putPrimaryConfigurationData(data, true);
                                }

                                updateHelper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                                ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                                if (!ep.containsKey(J2SEProjectProperties.INCLUDES)) {
                                    ep.setProperty(J2SEProjectProperties.INCLUDES, "**"); // NOI18N
                                }
                                if (!ep.containsKey(J2SEProjectProperties.EXCLUDES)) {
                                    ep.setProperty(J2SEProjectProperties.EXCLUDES, ""); // NOI18N
                                }
                                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                                try {
                                    ProjectManager.getDefault().saveProject(J2SEProject.this);
                                } catch (IOException e) {
                                    //#91398 provide a better error message in case of read-only location of project.
                                    if (!J2SEProject.this.getProjectDirectory().canWrite()) {
                                        NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(J2SEProject.class, "ERR_ProjectReadOnly",
                                                J2SEProject.this.getProjectDirectory().getName()));
                                        DialogDisplayer.getDefault().notify(nd);
                                    } else {
                                        ErrorManager.getDefault().notify(e);
                                    }
                                }
                                return null;
                            }
                        });
                    }
                });            
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
            J2SELogicalViewProvider physicalViewProvider = getLookup().lookup(J2SELogicalViewProvider.class);
            if (physicalViewProvider != null &&  physicalViewProvider.hasBrokenLinks()) {   
                BrokenReferencesSupport.showAlert();
            }
            String prop = eval.getProperty(J2SEProjectProperties.SOURCE_ENCODING);
            if (prop != null) {
                try {
                    Charset c = Charset.forName(prop);
                } catch (IllegalCharsetNameException e) {
                    //Broken property, log & ignore
                    LOG.warning("Illegal charset: " + prop+ " in project: " + FileUtil.getFileDisplayName(getProjectDirectory())); //NOI18N
                }
                catch (UnsupportedCharsetException e) {
                    //todo: Needs UI notification like broken references.
                    LOG.warning("Unsupported charset: " + prop+ " in project: " + FileUtil.getFileDisplayName(getProjectDirectory())); //NOI18N
                }
            }
        }
        
        protected void projectClosed() {
            // just do if the whole project was not deleted...
            if (getProjectDirectory().isValid()) {
                // Probably unnecessary, but just in case:
                try {
                    ProjectManager.getDefault().saveProject(J2SEProject.this);
                } catch (IOException e) {
                    if (!J2SEProject.this.getProjectDirectory().canWrite()) {
                        // #91398 - ignore, we already reported on project open. 
                        // not counting with someone setting the ro flag while the project is opened.
                    } else {
                        ErrorManager.getDefault().notify(e);
                    }
                }
            }
            
            // unregister project's classpaths to GlobalPathRegistry
            ClassPathProviderImpl cpProvider = lookup.lookup(ClassPathProviderImpl.class);
            GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
            GlobalPathRegistry.getDefault().unregister(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));
            if (mainClassUpdater != null) {
                mainClassUpdater.unregister ();
                mainClassUpdater = null;
            }
        }
        
    }
    
    /**
     * Exports the main JAR as an official build product for use from other scripts.
     * The type of the artifact will be {@link AntArtifact#TYPE_JAR}.
     */
    private final class AntArtifactProviderImpl implements AntArtifactProvider {

        public AntArtifact[] getBuildArtifacts() {
            return new AntArtifact[] {
                helper.createSimpleAntArtifact(JavaProjectConstants.ARTIFACT_TYPE_JAR, "dist.jar", evaluator(), "jar", "clean"), // NOI18N
            };
        }

    }
    
    private static final class RecommendedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates {
        RecommendedTemplatesImpl (UpdateHelper helper) {
            this.helper = helper;
        }
        
        private UpdateHelper helper;
        
        // List of primarily supported templates
        
        private static final String[] APPLICATION_TYPES = new String[] { 
            "java-classes",         // NOI18N
            "java-main-class",      // NOI18N
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
            // "servlet-types",     // NOI18N
            // "web-types",         // NOI18N
            "junit",                // NOI18N
            // "MIDP",              // NOI18N
            "simple-files"          // NOI18N
        };
        
        private static final String[] LIBRARY_TYPES = new String[] { 
            "java-classes",         // NOI18N
            "java-main-class",      // NOI18N
            "java-forms",           // NOI18N
            //"gui-java-application", // NOI18N
            "java-beans",           // NOI18N
            "persistence",          // NOI18N
            "oasis-XML-catalogs",   // NOI18N
            "XML",                  // NOI18N
            "ant-script",           // NOI18N
            "ant-task",             // NOI18N
            "servlet-types",        // NOI18N
            "web-service-clients",  // NOI18N
            "wsdl",                 // NOI18N
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
            "Templates/Persistence/Entity.java", // NOI18N
            "Templates/Persistence/RelatedCMP", // NOI18N                    
            "Templates/WebServices/WebServiceClient"   // NOI18N                    
        };
        
        public String[] getRecommendedTypes() {
            
            EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            // if the project has no main class, it's not really an application
            boolean isLibrary = ep.getProperty (J2SEProjectProperties.MAIN_CLASS) == null || "".equals (ep.getProperty (J2SEProjectProperties.MAIN_CLASS)); // NOI18N
            return isLibrary ? LIBRARY_TYPES : APPLICATION_TYPES;
        }
        
        public String[] getPrivilegedTemplates() {
            return PRIVILEGED_NAMES;
        }
        
    }
    
    private static final class J2SEPropertyEvaluatorImpl implements J2SEPropertyEvaluator {
        private PropertyEvaluator evaluator;
        public J2SEPropertyEvaluatorImpl (PropertyEvaluator eval) {
            evaluator = eval;
        }
        public PropertyEvaluator evaluator() {
            return evaluator;
        }
    }

    private class J2SEExtenderImplementation implements AntBuildExtenderImplementation {
        //add targets here as required by the external plugins..
        public List<String> getExtensibleTargets() {
            String[] targets = new String[] {
                "-do-init", "-init-check", "-post-clean", "jar", "-pre-pre-compile","-do-compile","-do-compile-single" //NOI18N
            };
            return Arrays.asList(targets);
        }

        public Project getOwningProject() {
            return J2SEProject.this;
        }

    }
    
    private static final String ENDORSED_DIR_PROPERTY="jaxws.endorsed.dir"; //NOI18N
    
    /** Set jaxws.endorsed.dir property for wsimport, wsgen tasks
     *  to specify jvmarg value : -Djava.endorsed.dirs=${jaxws.endorsed.dir}"
     */
    public static void setJaxWsEndorsedDirProperty(EditableProperties ep) {
        String oldJaxWsEndorsedDirs = ep.getProperty(ENDORSED_DIR_PROPERTY);
        String javaVersion = System.getProperty("java.specification.version"); //NOI18N
        if ("1.6".equals(javaVersion)) { //NOI18N
            String jaxWsEndorsedDirs = getJaxWsApiDir();
            if (jaxWsEndorsedDirs!=null && !jaxWsEndorsedDirs.equals(oldJaxWsEndorsedDirs))
                ep.setProperty(ENDORSED_DIR_PROPERTY, jaxWsEndorsedDirs);
        } else {
            if (oldJaxWsEndorsedDirs!=null) {
                ep.remove(ENDORSED_DIR_PROPERTY);
            }
        }
    }
    
    private static String getJaxWsApiDir() {
        File file = InstalledFileLocator.getDefault().locate("modules/ext/jaxws21/api/jaxws-api.jar", null, false); // NOI18N
        if (file!=null) {
            return file.getParent();
        }
        return null;
    }
    
    private static final DocumentBuilder db;
    static {
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new AssertionError(e);
        }
    }
    private static Document createNewDocument() {
        // #50198: for thread safety, use a separate document.
        // Using XMLUtil.createDocument is much too slow.
        synchronized (db) {
            return db.newDocument();
        }
    }

}
