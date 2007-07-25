
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

package org.netbeans.modules.j2ee.ejbjarproject;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.ejbjarproject.classpath.ClassPathSupport;
import org.netbeans.modules.j2ee.ejbjarproject.classpath.ClassPathProviderImpl;
import org.netbeans.modules.j2ee.ejbjarproject.classpath.EjbJarProjectClassPathExtender;
import org.netbeans.modules.j2ee.ejbjarproject.jaxws.EjbProjectJAXWSClientSupport;
import org.netbeans.modules.j2ee.ejbjarproject.jaxws.EjbProjectJAXWSSupport;
import org.netbeans.modules.j2ee.ejbjarproject.queries.CompiledSourceForBinaryQuery;
import org.netbeans.modules.j2ee.ejbjarproject.queries.JavadocForBinaryQueryImpl;
import org.netbeans.modules.j2ee.ejbjarproject.queries.UnitTestForSourceQueryImpl;
import org.netbeans.modules.j2ee.ejbjarproject.ui.EjbJarLogicalViewProvider;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.j2ee.common.ui.BrokenServerSupport;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarFactory;
import org.netbeans.modules.j2ee.spi.ejbjar.support.EjbEnterpriseReferenceContainerSupport;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.jaxws.spi.JAXWSSupportFactory;
import org.netbeans.modules.websvc.spi.client.WebServicesClientSupportFactory;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientSupportFactory;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.ant.AntBuildExtenderFactory;
import org.netbeans.spi.project.ant.AntBuildExtenderImplementation;
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
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.ejbjarproject.jaxws.EjbProjectJAXWSVersionProvider;
import org.netbeans.modules.j2ee.ejbjarproject.queries.EjbJarProjectEncodingQueryImpl;
import org.netbeans.modules.j2ee.ejbjarproject.ui.BrokenReferencesAlertPanel;
import org.netbeans.modules.j2ee.ejbjarproject.ui.FoldersListSettings;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.j2ee.ejbjarproject.queries.SourceLevelQueryImpl;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.spi.webservices.WebServicesSupportFactory;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.util.Exceptions;

/**
 * Represents one ejb module project
 * @author Chris Webster
 */
public class EjbJarProject implements Project, AntProjectListener, FileChangeListener, PropertyChangeListener {
    
    private static final Icon PROJECT_ICON = new ImageIcon(Utilities.loadImage("org/netbeans/modules/j2ee/ejbjarproject/ui/resources/ejbjarProjectIcon.gif")); // NOI18N
    
    private final AuxiliaryConfiguration aux;
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final ReferenceHelper refHelper;
    private FileObject libFolder = null;
    private final GeneratedFilesHelper genFilesHelper;
    private final Lookup lookup;
    private final UpdateHelper updateHelper;
    private final EjbJarProvider ejbModule;
    private final EjbJar apiEjbJar;
    private WebServicesSupport apiWebServicesSupport;
    private JAXWSSupport apiJaxwsSupport;
    private EjbProjectJAXWSSupport jaxwsSupport;
    private WebServicesClientSupport apiWebServicesClientSupport;
    private JAXWSClientSupport apiJAXWSClientSupport;
    private EjbJarWebServicesSupport ejbJarWebServicesSupport;
    private EjbJarWebServicesClientSupport ejbJarWebServicesClientSupport;
    private EjbProjectJAXWSClientSupport jaxWsClientSupport;
    private SourceRoots sourceRoots;
    private SourceRoots testRoots;
    private PropertyHelper propertyHelper;
    private final EjbJarProjectClassPathExtender classpathExtender; 
    private PropertyChangeListener j2eePlatformListener;
    private PropertyChangeListener evalListener;
    private AntBuildExtender buildExtender;
    
    // TODO: AB: replace the code in EjbJarProjectProperties.setNewServerInstanceValue with this 
    /*private String propJ2eeServerInstance;
    private PropertyChangeListener evalListener = new PropertyChangeListener() {
        
        public void propertyChange(final PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            if (propertyName == null || propertyName.equals(EjbJarProjectProperties.J2EE_SERVER_INSTANCE)) {
                org.openide.util.RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        ProjectManager.mutex().writeAccess(new Runnable() {
                            public void run() {
                                // try to unregister the old J2EE platform
                                String oldJ2eeServerInstance = null;
                                if (propJ2eeServerInstance != null) {
                                    oldJ2eeServerInstance = propJ2eeServerInstance;
                                } else {
                                    oldJ2eeServerInstance = (String)evt.getOldValue();
                                }
                                if (oldJ2eeServerInstance != null) {
                                    J2eePlatform oldJ2eePlatform = Deployment.getDefault().getJ2eePlatform(oldJ2eeServerInstance);
                                    unregisterJ2eePlatformListener(oldJ2eePlatform);
                                    propJ2eeServerInstance = null;
                                }

                                // now register the new platform
                                //String newJ2eeServerInstance = (String)evt.getNewValue();
                                EditableProperties props = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                                //if (newJ2eeServerInstance == null)
                                String newJ2eeServerInstance = props.getProperty(EjbJarProjectProperties.J2EE_SERVER_INSTANCE); 

                                if (newJ2eeServerInstance != null) {
                                    J2eePlatform newJ2eePlatform  = Deployment.getDefault().getJ2eePlatform(newJ2eeServerInstance);
                                    registerJ2eePlatformListener(newJ2eePlatform);
                                    propJ2eeServerInstance = newJ2eeServerInstance;

                                    putJ2eePlatformClassPath(newJ2eePlatform, props);
                                    String serverType = Deployment.getDefault().getServerID(newJ2eeServerInstance);
                                    props.setProperty(EjbJarProjectProperties.J2EE_SERVER_TYPE, serverType);
                                    
                                    // TODO: AB: should update wscompile classpath too

                                    helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, props);
                                    try {
                                        ProjectManager.getDefault().saveProject(EjbJarProject.this);
                                    }
                                    catch (IOException e) {
                                        ErrorManager.getDefault().notify(e);
                                    }
                                }
                            }
                        });
                    }
                });
            }
        }
    };*/
    
    EjbJarProject(final AntProjectHelper helper) throws IOException {
        this.helper = helper;
        eval = createEvaluator();
        //eval.addPropertyChangeListener(evalListener);
        aux = helper.createAuxiliaryConfiguration();
        refHelper = new ReferenceHelper(helper, aux, helper.getStandardPropertyEvaluator());
        buildExtender = AntBuildExtenderFactory.createAntExtender(new EjbExtenderImplementation());
        genFilesHelper = new GeneratedFilesHelper(helper, buildExtender);
        this.updateHelper = new UpdateHelper (this, this.helper, this.aux, this.genFilesHelper, UpdateHelper.createDefaultNotifier());
        ClassPathProviderImpl cpProvider = new ClassPathProviderImpl(helper, evaluator(), getSourceRoots(), getTestSourceRoots());
        ejbModule = new EjbJarProvider(this, helper, cpProvider);
        apiEjbJar = EjbJarFactory.createEjbJar(ejbModule);
        ejbJarWebServicesSupport = new EjbJarWebServicesSupport(this, helper, refHelper);
        jaxwsSupport = new EjbProjectJAXWSSupport(this, helper);
        ejbJarWebServicesClientSupport = new EjbJarWebServicesClientSupport(this, helper, refHelper);
        jaxWsClientSupport = new EjbProjectJAXWSClientSupport(this);
        apiWebServicesSupport = WebServicesSupportFactory.createWebServicesSupport(ejbJarWebServicesSupport);
        apiJaxwsSupport = JAXWSSupportFactory.createJAXWSSupport(jaxwsSupport);
        apiWebServicesClientSupport = WebServicesClientSupportFactory.createWebServicesClientSupport(ejbJarWebServicesClientSupport);
        apiJAXWSClientSupport = JAXWSClientSupportFactory.createJAXWSClientSupport(jaxWsClientSupport);
        classpathExtender = new EjbJarProjectClassPathExtender(this, updateHelper, evaluator(), refHelper);
        lookup = createLookup(aux, cpProvider);
        helper.addAntProjectListener(this);
        ProjectManager.mutex().postWriteRequest(
             new Runnable () {
                 public void run() {
                     try {
                         updateProjectXML ();
                     } catch (IOException ioe) {
                         Logger.getLogger("global").log(Level.INFO, null, ioe);
                     }
                 }
             }
         );    
    }

    /**
     * Returns the project directory
     * @return the directory the project is located in
     */
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }
    
    public String toString() {
        return "EjbJarProject[" + getProjectDirectory() + "]"; // NOI18N
    }
    
    private PropertyEvaluator createEvaluator() {
        PropertyEvaluator eval = helper.getStandardPropertyEvaluator();
        evalListener = WeakListeners.propertyChange(this, eval);
        eval.addPropertyChangeListener(evalListener);
        return eval;
    }
    
    public PropertyEvaluator evaluator() {
        return eval;
    }

    public ReferenceHelper getReferenceHelper () {
        return this.refHelper;
    }
    
    public UpdateHelper getUpdateHelper() {
        return updateHelper;
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
        String ejbModuleLabel = org.openide.util.NbBundle.getMessage(EjbJarLogicalViewProvider.class, "LBL_Node_EJBModule"); //NOI18N
        String configFilesLabel = org.openide.util.NbBundle.getMessage(EjbJarLogicalViewProvider.class, "LBL_Node_DocBase"); //NOI18N
        
        sourcesHelper.addPrincipalSourceRoot("${"+EjbJarProjectProperties.SOURCE_ROOT+"}", ejbModuleLabel, /*XXX*/null, null);
        sourcesHelper.addPrincipalSourceRoot("${"+EjbJarProjectProperties.META_INF+"}", configFilesLabel, /*XXX*/null, null);
        
        ProjectManager.mutex().postWriteRequest(new Runnable() {
            public void run() {
                sourcesHelper.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
            }
        });
        Lookup base = Lookups.fixed(new Object[] {
                EjbJarProject.this, // never cast an externally obtained Project to EjbJarProject - use lookup instead
                buildExtender,
                new Info(),
                aux,
                helper.createCacheDirectoryProvider(),
                new ProjectWebServicesSupportProvider(), // implementation of WebServicesClientSupportProvider commented out
                spp,
                EjbEnterpriseReferenceContainerSupport.createEnterpriseReferenceContainer(this, helper),
                new ProjectEjbJarProvider(this),
                ejbModule, //implements J2eeModuleProvider
                new EjbJarActionProvider( this, helper, refHelper ),
                new EjbJarLogicalViewProvider(this, updateHelper, evaluator(), spp, refHelper),
                new CustomizerProviderImpl( this, updateHelper, evaluator(), refHelper ),
                cpProvider,
                new CompiledSourceForBinaryQuery(helper,evaluator(),getSourceRoots(),getTestSourceRoots()),
                new JavadocForBinaryQueryImpl(helper, evaluator()),
                new AntArtifactProviderImpl(),
                new ProjectXmlSavedHookImpl(),
                UILookupMergerSupport.createProjectOpenHookMerger(new ProjectOpenedHookImpl()),
                new UnitTestForSourceQueryImpl(getSourceRoots(),getTestSourceRoots()),
                new SourceLevelQueryImpl(helper, evaluator()),
                new EjbJarSources (helper, evaluator(), getSourceRoots(), getTestSourceRoots()),
                new EjbJarSharabilityQuery(helper, evaluator(), getSourceRoots(), getTestSourceRoots()),
                new EjbJarFileBuiltQuery (helper, evaluator(),getSourceRoots(),getTestSourceRoots()),
                new EjbJarProjectEncodingQueryImpl(evaluator()),
                new RecommendedTemplatesImpl(updateHelper),
                refHelper,
                classpathExtender,
                new EjbJarProjectOperations(this),
                new EjbJarPersistenceProvider(this, evaluator(), cpProvider),
                new EjbJarEMGenStrategyResolver(),
                new EjbJarJPASupport(this),
                new EjbJarServerStatusProvider(this),
                new EjbJarJPAModuleInfo(this),
                new EjbProjectJAXWSVersionProvider(helper),
                UILookupMergerSupport.createPrivilegedTemplatesMerger(),
                UILookupMergerSupport.createRecommendedTemplatesMerger(),
                LookupProviderSupport.createSourcesMerger(),
                new EjbJarTemplateAttributesProvider(helper),
                // TODO: AB: maybe add "this" to the lookup. You should not cast a Project to EjbJarProject, but use the lookup instead.
            });
            return LookupProviderSupport.createCompositeLookup(base, "Projects/org-netbeans-modules-j2ee-ejbjarproject/Lookup"); //NOI18N
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
        // currently ignored
        //TODO: should not be ignored!
    }
    
    String getBuildXmlName() {
        String storedName = helper.getStandardPropertyEvaluator().getProperty(EjbJarProjectProperties.BUILD_FILE);
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

    
    
    WebServicesSupport getAPIWebServicesSupport() {
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
    
    /*public EjbJarProjectProperties getEjbJarProjectProperties() {
        return new EjbJarProjectProperties (this, helper, refHelper);
    }*/
    
    public PropertyHelper getPropertyHelper() {
        if (propertyHelper == null) {
            this.propertyHelper = new PropertyHelper(this, this.updateHelper);
        }
        return this.propertyHelper;
    }
    
    public EjbJarProvider getEjbModule() {
        return ejbModule;
    }
    
    public EjbJar getAPIEjbJar() {
        return apiEjbJar;
    }
    
    /** Last time in ms when the Broken References alert was shown. */
    private static long brokenAlertLastTime = 0;
    
    /** Is Broken References alert shown now? */
    private static boolean brokenAlertShown = false;
    
    /** Timeout within which request to show alert will be ignored. */
    private static int BROKEN_ALERT_TIMEOUT = 1000;
    
    private static synchronized void showBrokenReferencesAlert() {
        // Do not show alert if it is already shown or if it was shown
        // in last BROKEN_ALERT_TIMEOUT milliseconds or if user do not wish it.
        if (brokenAlertShown ||
        brokenAlertLastTime+BROKEN_ALERT_TIMEOUT > System.currentTimeMillis() ||
        !FoldersListSettings.getDefault().isShowAgainBrokenRefAlert()) {
            return;
        }
        brokenAlertShown = true;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    Object ok = NbBundle.getMessage(BrokenReferencesAlertPanel.class,"MSG_Broken_References_OK");
                    DialogDescriptor dd = new DialogDescriptor(new BrokenReferencesAlertPanel(),
                    NbBundle.getMessage(BrokenReferencesAlertPanel.class, "MSG_Broken_References_Title"),
                    true, new Object[] {ok}, ok, DialogDescriptor.DEFAULT_ALIGN, null, null);
                    Dialog dlg = null;
                    try {
                        dlg = DialogDisplayer.getDefault().createDialog(dd);
                        dlg.setVisible(true);
                    } finally {
                        if (dlg != null)
                            dlg.dispose();
                    }
                } finally {
                    synchronized (EjbJarProject.class) {
                        brokenAlertLastTime = System.currentTimeMillis();
                        brokenAlertShown = false;
                    }
                }
            }
        });
    }
    
    /** Return configured project name. */
    public String getName() {
        return (String) ProjectManager.mutex().readAccess(new Mutex.Action() {
            public Object run() {
                Element data = updateHelper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                if (nl.getLength() == 1) {
                    nl = nl.item(0).getChildNodes();
                    if (nl.getLength() == 1 && nl.item(0).getNodeType() == Node.TEXT_NODE) {
                        return ((Text) nl.item(0)).getNodeValue();
                    }
                }
                return "EJB???"; // NOI18N
            }
        });
    }

    /** Store configured project name. */
    public void setName(final String name) {
        ProjectManager.mutex().writeAccess(new Mutex.Action() {
            public Object run() {
                Element data = helper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                    data.insertBefore(nameEl, /* OK if null */data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                helper.putPrimaryConfigurationData(data, true);
                return null;
            }
        });
    }

    private void updateProjectXML () throws IOException {
        Element element = aux.getConfigurationFragment("data","http://www.netbeans.org/ns/EjbJar-project/1",true);    //NOI18N
        if (element != null) {
            Document doc = element.getOwnerDocument();
            Element newRoot = doc.createElementNS (EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,"data"); //NOI18N
            copyDocument (doc, element, newRoot);
            Element sourceRoots = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");  //NOI18N
            Element root = doc.createElementNS (EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
            root.setAttribute ("id","src.dir");   //NOI18N
            sourceRoots.appendChild(root);
            newRoot.appendChild (sourceRoots);
            Element testRoots = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,"test-roots");  //NOI18N
            root = doc.createElementNS (EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
            root.setAttribute ("id","test.src.dir");   //NOI18N
            testRoots.appendChild (root);
            newRoot.appendChild (testRoots);
            helper.putPrimaryConfigurationData (newRoot, true);
            ProjectManager.getDefault().saveProject(this);
        }
    }

    private static void copyDocument (Document doc, Element from, Element to) {
        NodeList nl = from.getChildNodes();
        int length = nl.getLength();
        for (int i=0; i< length; i++) {
            Node node = nl.item (i);
            Node newNode = null;
            switch (node.getNodeType()) {
                case Node.ELEMENT_NODE:
                    Element oldElement = (Element) node;
                    newNode = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,oldElement.getTagName());
                    copyDocument(doc,oldElement,(Element)newNode);
                    break;
                case Node.TEXT_NODE:
                    Text oldText = (Text) node;
                    newNode = doc.createTextNode(oldText.getData());
                    break;
                case Node.COMMENT_NODE:
                    Comment oldComment = (Comment) node;
                    newNode = doc.createComment(oldComment.getData());
                    break;
            }
            if (newNode != null) {
                to.appendChild (newNode);
            }
        }
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
    
    public void registerJ2eePlatformListener(final J2eePlatform platform) {
        // listen to classpath changes
        j2eePlatformListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(J2eePlatform.PROP_CLASSPATH)) {
                    ProjectManager.mutex().writeAccess(new Mutex.Action() {
                        public Object run() {
                            EditableProperties ep = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                            String classpath = Utils.toClasspathString(platform.getClasspathEntries());
                            ep.setProperty(EjbJarProjectProperties.J2EE_PLATFORM_CLASSPATH, classpath);
                            helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                            try {
                                ProjectManager.getDefault().saveProject(EjbJarProject.this);
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
        
    public void fileFolderCreated (org.openide.filesystems.FileEvent fe) {
    }
    
    public void fileRenamed (org.openide.filesystems.FileRenameEvent fe) {
        FileObject fo = fe.getFile ();
        checkLibraryFolder (fo);
    }

    private void checkLibraryFolder (FileObject fo) {
        if (!FileUtil.isArchiveFile(fo))
            return;
        
        if (fo.getParent ().equals (libFolder)) {
            try {
                classpathExtender.addArchiveFile(fo);
            }
            catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }
    
    private String getProperty(String path, String name) {
        return helper.getProperties(path).getProperty(name);
    }
    
    /**
     * Refreshes the build-impl.xml script. If it was modified by the user, it 
     * displays a confirmation dialog.
     *
     * @param askUserIfFlags only display the dialog if the state of the build script
     * contains these flags (along with {@link GeneratedFilesHelper#FLAG_MODIFIED}, 
     * which is always checked)
     * @param askInCurrentThread if false, asks in another thread
     * @param checkForProjectXmlModified true if it is necessary to check whether the
     * script is out of date with respect to <code>project.xml</code> and/or the stylesheet
     */
    private void refreshBuildImplXml(int askUserIfFlags, boolean askInCurrentThread, boolean checkForProjectXmlModified) {
        askUserIfFlags |= GeneratedFilesHelper.FLAG_MODIFIED;
        int flags = genFilesHelper.getBuildScriptState(
            GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
            EjbJarProject.class.getResource("resources/build-impl.xsl")); //NOI18N
        if ((flags & askUserIfFlags) == askUserIfFlags) {
            Runnable run = new Runnable () {
                public void run () {
                    JButton updateOption = new JButton (NbBundle.getMessage(EjbJarProject.class, "CTL_Regenerate"));
                    if (DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor (NbBundle.getMessage(EjbJarProject.class,"TXT_BuildImplRegenerate"),
                            NbBundle.getMessage(EjbJarProject.class,"TXT_BuildImplRegenerateTitle"),
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
                                EjbJarProject.class.getResource("resources/build-impl.xsl")); // NOI18N
                        } catch (IOException e) {
                            Exceptions.printStackTrace(e);
                        } catch (IllegalStateException e) {
                            Exceptions.printStackTrace(e);
                        }
                    }
                }
            };
            if (askInCurrentThread)
                run.run();
            else
                RequestProcessor.getDefault().post(run);
        } else {
            try {
                genFilesHelper.refreshBuildScript(
                    GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                    EjbJarProject.class.getResource("resources/build-impl.xsl"), // NOI18N
                    checkForProjectXmlModified);
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
            String dn = EjbJarProject.this.getName();
            synchronized (pcs) {
                cachedName = new WeakReference<String>(dn);
            }
            return dn;
        }
        
        public Icon getIcon() {
            return PROJECT_ICON;
        }
        
        public Project getProject() {
            return EjbJarProject.this;
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
            refreshBuildImplXml(0, false, false);
            
            genFilesHelper.refreshBuildScript(
                getBuildXmlName(),
                EjbJarProject.class.getResource("resources/build.xsl"),
                false);
        }
        
    }

    private final class ProjectOpenedHookImpl extends ProjectOpenedHook {
        
        ProjectOpenedHookImpl() {}
        
        protected void projectOpened() {
            try {
                //Check libraries and add them to classpath automatically
                String libFolderName = helper.getStandardPropertyEvaluator ().getProperty (EjbJarProjectProperties.LIBRARIES_DIR);
                //EjbJarProjectProperties ejbpp = getEjbJarProjectProperties();

                //DDDataObject initialization to be ready to listen on changes (#49656)
                try {
                    FileObject ddFO = ejbModule.getDeploymentDescriptor();
                    if (ddFO != null) {
                        DataObject.find(ddFO);
                    }
                } catch (org.openide.loaders.DataObjectNotFoundException ex) {}
                
                if (libFolderName != null && helper.resolveFile (libFolderName).isDirectory ()) {
                    libFolder = helper.resolveFileObject(libFolderName);
                        FileObject children [] = libFolder.getChildren ();
                        List libs = new LinkedList();
                        for (int i = 0; i < children.length; i++) {
                            if (FileUtil.isArchiveFile(children[i]))
                                libs.add(children[i]);
                        }
                        FileObject[] libsArray = new FileObject[libs.size()];
                        libs.toArray(libsArray);
                        classpathExtender.addArchiveFiles(EjbJarProjectProperties.JAVAC_CLASSPATH, libsArray, ClassPathSupport.ELEMENT_INCLUDED_LIBRARIES);
                        libFolder.addFileChangeListener (EjbJarProject.this);
                }
                
                // Check up on build scripts.
                
                 refreshBuildImplXml( GeneratedFilesHelper.FLAG_OLD_PROJECT_XML, true, true);
                
                genFilesHelper.refreshBuildScript(
                    getBuildXmlName(),
                    EjbJarProject.class.getResource("resources/build.xsl"),
                    true);
                
                String servInstID = getProperty(AntProjectHelper.PRIVATE_PROPERTIES_PATH, EjbJarProjectProperties.J2EE_SERVER_INSTANCE);
                J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(servInstID);
                if (platform != null) {
                    // updates j2ee.platform.cp & wscompile.cp & reg. j2ee platform listener
                    EjbJarProjectProperties.setServerInstance(EjbJarProject.this, EjbJarProject.this.helper, servInstID);
                } else {
                    // if there is some server instance of the type which was used
                    // previously do not ask and use it
                    String serverType = getProperty(AntProjectHelper.PROJECT_PROPERTIES_PATH, EjbJarProjectProperties.J2EE_SERVER_TYPE);
                    if (serverType != null) {
                        String[] servInstIDs = Deployment.getDefault().getInstancesOfServer(serverType);
                        if (servInstIDs.length > 0) {
                            EjbJarProjectProperties.setServerInstance(EjbJarProject.this, EjbJarProject.this.helper, servInstIDs[0]);
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
            ClassPathProviderImpl cpProvider = (ClassPathProviderImpl)lookup.lookup(ClassPathProviderImpl.class);
            GlobalPathRegistry.getDefault().register(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
            GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));

            // initialize the server configuration
            // it MUST BE called AFTER classpaths are registered to GlobalPathRegistry!
            // EJB DDProvider (used here) needs classpath set correctly when resolving Java Extents for annotations
            ejbModule.getConfigSupport().ensureConfigurationReady();

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
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
            
            EjbJarLogicalViewProvider physicalViewProvider = (EjbJarLogicalViewProvider)
                EjbJarProject.this.getLookup().lookup (EjbJarLogicalViewProvider.class);
            if (physicalViewProvider != null &&  physicalViewProvider.hasBrokenLinks()) {   
                BrokenReferencesSupport.showAlert();
            }
        }
        
        private void updateProject() {
            // Make it easier to run headless builds on the same machine at least.
            EditableProperties ep = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
            ep.setProperty("netbeans.user", System.getProperty("netbeans.user"));

            // set jaxws.endorsed.dir property (for endorsed mechanism to be used with wsimport, wsgen)
            WSUtils.setJaxWsEndorsedDirProperty(ep);

            helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
            // update a dual build directory project to use a single build directory
            ep = updateHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            String earBuildDir = ep.getProperty(EjbJarProjectProperties.BUILD_EAR_CLASSES_DIR);
            if (null != earBuildDir) {
                // there is an BUILD_EAR_CLASSES_DIR property... we may 
                //  need to change its value
                String buildDir = ep.getProperty(EjbJarProjectProperties.BUILD_CLASSES_DIR);
                if (null != buildDir) {
                    // there is a value that we may need to change the 
                    // BUILD_EAR_CLASSES_DIR property value to match.
                    if (!buildDir.equals(earBuildDir)) {
                        // the values do not match... update the property and save it
                        ep.setProperty(EjbJarProjectProperties.BUILD_EAR_CLASSES_DIR,
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
                ProjectManager.getDefault().saveProject(EjbJarProject.this);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        
        protected void projectClosed() {
            
            // unregister j2ee platform classpath change listener
            /*EjbJarProjectProperties wpp = getEjbJarProjectProperties();
            String servInstID = (String)wpp.get(EjbJarProjectProperties.J2EE_SERVER_INSTANCE);*/
            String servInstID = getProperty(AntProjectHelper.PRIVATE_PROPERTIES_PATH, EjbJarProjectProperties.J2EE_SERVER_INSTANCE);
            J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(servInstID);
            if (platform != null) {
                unregisterJ2eePlatformListener(platform);
            }
            
            // unregister the property change listener on the prop evaluator
            if (evalListener != null) {
                evaluator().removePropertyChangeListener(evalListener);
            }
            
            // Probably unnecessary, but just in case:
            try {
                ProjectManager.getDefault().saveProject(EjbJarProject.this);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
            
            // unregister project's classpaths to GlobalPathRegistry
            ClassPathProviderImpl cpProvider = (ClassPathProviderImpl)lookup.lookup(ClassPathProviderImpl.class);
            GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
            GlobalPathRegistry.getDefault().unregister(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));
        }
        
    }
         
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(EjbJarProjectProperties.JAVAC_CLASSPATH)) {
            ProjectManager.mutex().postWriteRequest(new Runnable () {
                public void run() {
                    EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);    //Reread the properties, PathParser changes them
                    //update lib references in private properties
                    EditableProperties privateProps = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    Iterator cpItems = classpathExtender.getClassPathSupport().itemsIterator(props.getProperty(EjbJarProjectProperties.JAVAC_CLASSPATH),  ClassPathSupport.ELEMENT_INCLUDED_LIBRARIES);
                    EjbJarProjectProperties.storeLibrariesLocations(cpItems, privateProps);
                    helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProps);
                }
            });
        }
    }
    
    /**
     * Exports the main JAR as an official build product for use from other scripts.
     * The type of the artifact will be {@link AntArtifact#TYPE_JAR}.
     */
    private final class AntArtifactProviderImpl implements AntArtifactProvider {
        
        public AntArtifact[] getBuildArtifacts() {
            return new AntArtifact[] {
                helper.createSimpleAntArtifact(JavaProjectConstants.ARTIFACT_TYPE_JAR, "dist.jar", helper.getStandardPropertyEvaluator(), "dist", "clean"), // NOI18N
                helper.createSimpleAntArtifact(EjbProjectConstants.ARTIFACT_TYPE_EJBJAR, "dist.jar", helper.getStandardPropertyEvaluator(), "dist", "clean"), // NOI18N
                helper.createSimpleAntArtifact(EjbProjectConstants.ARTIFACT_TYPE_J2EE_MODULE_IN_EAR_ARCHIVE, "dist.ear.jar", helper.getStandardPropertyEvaluator(), "dist-ear", "clean-ear") // NOI18N
            };
        }
    }
    
    // List of primarily supported templates
    private static final String[] TYPES = new String[] {
        "java-classes",         // NOI18N
        "ejb-types_2_1",      // NOI18N
        "ejb-types",            // NOI18N
        "ejb-types-server",     // NOI18N
        "web-services",         // NOI18N
        "wsdl",                 // NOI18N
        "j2ee-14-types",           // NOI18N
        "j2ee-types",           // NOI18N
        "java-beans",           // NOI18N
        "java-main-class",      // NOI18N
        "persistence",          // NOI18N
        "oasis-XML-catalogs",   // NOI18N
        "XML",                  // NOI18N
        "ant-script",           // NOI18N
        "ant-task",             // NOI18N
        "junit",                // NOI18N
        "simple-files"          // NOI18N
    };
    
    /**
     * Supported template categories for Java EE 5 projects.
     */
    private static final String[] JAVAEE5_TYPES = new String[] {
        "java-classes",         // NOI18N
        "ejb-types",            // NOI18N
        "ejb-types-server",     // NOI18N
        "ejb-types_3_0",        // NOI18N
        "web-services",         // NOI18N
        "web-service-clients",  // NOI18N
        "wsdl",                 // NOI18N
        "j2ee-types",           // NOI18N
        "java-beans",           // NOI18N
        "java-main-class",      // NOI18N
        "persistence",          // NOI18N
        "oasis-XML-catalogs",   // NOI18N
        "XML",                  // NOI18N
        "ant-script",           // NOI18N
        "ant-task",             // NOI18N
        "junit",                // NOI18N
        "simple-files"          // NOI18N
    };
    
    /**
     * Supported template categories for archive projects.
     */
    private static final String[] ARCHIVE_TYPES = new String[] {
        "ejb-deployment-descriptor",            // NOI18N
        "deployment-descriptor",                // NOI18N
        "XML",                                  // NOI18N
    };

    private static final String[] PRIVILEGED_NAMES = new String[] {
        "Templates/J2EE/Session", // NOI18N
        "Templates/J2EE/Entity",  // NOI18N
        "Templates/J2EE/RelatedCMP", // NOI18N                    
        "Templates/J2EE/Message", //NOI18N
        "Templates/Classes/Class.java",             // NOI18N
        "Templates/Classes/Package",                // NOI18N
        "Templates/WebServices/WebService.java", // NOI18N
        "Templates/WebServices/MessageHandler" // NOI18N
    };
    
    private static final String[] PRIVILEGED_NAMES_EE5 = new String[] {
        "Templates/J2EE/Session", // NOI18N
        "Templates/J2EE/Message", // NOI18N
        "Templates/Classes/Class.java",             // NOI18N
        "Templates/Classes/Package",                // NOI18N
        "Templates/Persistence/Entity.java", // NOI18N
        "Templates/Persistence/RelatedCMP", // NOI18N
        "Templates/WebServices/WebService.java", // NOI18N
        "Templates/WebServices/WebServiceClient"   // NOI18N      
    };
    
    private static final String[] PRIVILEGED_NAMES_ARCHIVE = new String[] {
        "Templates/J2EE/ejbJarXml", // NOI18N
    };

    private final class RecommendedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates {
        transient private boolean isEE5 = false;
        transient private boolean checked = false;
        transient private boolean isArchive = false;
        transient private UpdateHelper helper = null;

        RecommendedTemplatesImpl(UpdateHelper helper) {
            this.helper = helper;
        }

        public String[] getRecommendedTypes() {
            checkEnvironment();
            String[] retVal = null;
            if (isArchive) {
                retVal = ARCHIVE_TYPES; 
            } else if (isEE5) {
                retVal = JAVAEE5_TYPES;
            } else {
                retVal = TYPES;
            }
            return retVal;
        }
        
        public String[] getPrivilegedTemplates() {
            checkEnvironment();
            String[] retVal = null;
            if (isArchive) {
                retVal = PRIVILEGED_NAMES_ARCHIVE;
            } else if (isEE5) {
                retVal = PRIVILEGED_NAMES_EE5;
            } else {
                retVal = PRIVILEGED_NAMES;
            } 
            return retVal;
        }
        
        private void checkEnvironment(){
            if (!checked){
                isEE5 = J2eeModule.JAVA_EE_5.equals(getEjbModule().getJ2eePlatformVersion());
                final Object srcType = helper.getAntProjectHelper().
                        getStandardPropertyEvaluator().getProperty(EjbJarProjectProperties.JAVA_SOURCE_BASED);
                if ("false".equals(srcType)) {
                    isArchive = true;
                }
                checked = true;
            }
        }
    }



    private class EjbExtenderImplementation implements AntBuildExtenderImplementation {
        //add targets here as required by the external plugins..
        public List<String> getExtensibleTargets() {
            String[] targets = new String[] {
                "-do-init", "-init-check", "-post-clean", "jar", "-pre-pre-compile","-do-compile","-do-compile-single", "-pre-dist" //NOI18N
            };
            return Arrays.asList(targets);
        }

        public Project getOwningProject() {
            return EjbJarProject.this;
        }

    }
}
