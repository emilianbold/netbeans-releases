
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

package org.netbeans.modules.j2ee.ejbjarproject;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.spi.ejbjar.support.EjbJarSupport;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener.Artifact;
import org.netbeans.modules.j2ee.ejbjarproject.jaxws.EjbProjectJAXWSClientSupport;
import org.netbeans.modules.j2ee.ejbjarproject.jaxws.EjbProjectJAXWSSupport;
import org.netbeans.modules.j2ee.ejbjarproject.ui.EjbJarLogicalViewProvider;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.common.SharabilityUtility;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.j2ee.common.project.ArtifactCopyOnSaveSupport;
import org.netbeans.modules.java.api.common.classpath.ClassPathExtender;
import org.netbeans.modules.java.api.common.classpath.ClassPathModifier;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl;
import org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport;
import org.netbeans.modules.j2ee.common.project.ui.DeployOnSaveUtils;
import org.netbeans.modules.j2ee.common.project.ui.J2EEProjectProperties;
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
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.ejbjarproject.classpath.ClassPathSupportCallbackImpl;
import org.netbeans.modules.j2ee.ejbjarproject.ui.BrokenReferencesAlertPanel;
import org.netbeans.modules.j2ee.common.project.ui.UserProjectSettings;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider.DeployOnSaveSupport;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarImplementation;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarImplementation2;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.ant.UpdateImplementation;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.queries.QuerySupport;
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
import org.netbeans.spi.java.project.support.ExtraSourceJavadocSupport;
import org.netbeans.spi.java.project.support.LookupMergerSupport;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.support.ant.AntBasedProjectRegistration;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.util.Exceptions;

/**
 * Represents one ejb module project
 * @author Chris Webster
 */
@AntBasedProjectRegistration(
    iconResource="org/netbeans/modules/j2ee/ejbjarproject/ui/resources/ejbjarProjectIcon.gif",
    type=EjbJarProjectType.TYPE,
    sharedNamespace=EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,
    privateNamespace=EjbJarProjectType.PRIVATE_CONFIGURATION_NAMESPACE
)
public class EjbJarProject implements Project, AntProjectListener, FileChangeListener {
    
    private static final Icon PROJECT_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/j2ee/ejbjarproject/ui/resources/ejbjarProjectIcon.gif", false); // NOI18N
    
    private static final Logger LOGGER = Logger.getLogger(EjbJarProject.class.getName());
    
    private final AuxiliaryConfiguration aux;
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final ReferenceHelper refHelper;
    private FileObject libFolder = null;
    private final GeneratedFilesHelper genFilesHelper;
    private final Lookup lookup;
    private final UpdateHelper updateHelper;
    private final EjbJarProvider ejbModule;
    private final CopyOnSaveSupport css;
    private final ArtifactCopyOnSaveSupport artifactSupport;
    private final DeployOnSaveSupport deployOnSaveSupport;
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
    private final ClassPathExtender classPathExtender; 
    private final ClassPathModifier classPathModifier; 
    private PropertyChangeListener j2eePlatformListener;
    private AntBuildExtender buildExtender;
    private final ClassPathProviderImpl cpProvider;
    private ClassPathUiSupport.Callback classPathUiSupportCallback;
    
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
    
    public EjbJarProject(final AntProjectHelper helper) throws IOException {
        this.helper = helper;
        eval = createEvaluator();
        aux = helper.createAuxiliaryConfiguration();
        refHelper = new ReferenceHelper(helper, aux, helper.getStandardPropertyEvaluator());
        buildExtender = AntBuildExtenderFactory.createAntExtender(new EjbExtenderImplementation());
        genFilesHelper = new GeneratedFilesHelper(helper, buildExtender);
        UpdateImplementation updateProject = new UpdateProjectImpl(this, helper, aux, genFilesHelper);
        this.updateHelper = new UpdateHelper(updateProject, helper);
        this.cpProvider = new ClassPathProviderImpl(helper, evaluator(), getSourceRoots(), getTestSourceRoots(),
                ProjectProperties.BUILD_CLASSES_DIR, EjbJarProjectProperties.DIST_JAR, ProjectProperties.BUILD_TEST_CLASSES_DIR,
                new String[] {"javac.classpath", EjbJarProjectProperties.J2EE_PLATFORM_CLASSPATH }, // NOI18N
                new String[] {"javac.test.classpath", EjbJarProjectProperties.J2EE_PLATFORM_CLASSPATH }, // NOI18N
                new String[] {"debug.classpath", EjbJarProjectProperties.J2EE_PLATFORM_CLASSPATH }, // NOI18N
                new String[] {"run.test.classpath", EjbJarProjectProperties.J2EE_PLATFORM_CLASSPATH }); // NOI18N
        ejbModule = new EjbJarProvider(this, helper, cpProvider);
        apiEjbJar = EjbJarFactory.createEjbJar(new EjbJarImpl2(ejbModule));
        ejbJarWebServicesSupport = new EjbJarWebServicesSupport(this, helper, refHelper);
        jaxwsSupport = new EjbProjectJAXWSSupport(this, helper);
        ejbJarWebServicesClientSupport = new EjbJarWebServicesClientSupport(this, helper, refHelper);
        jaxWsClientSupport = new EjbProjectJAXWSClientSupport(this);
        apiWebServicesSupport = WebServicesSupportFactory.createWebServicesSupport(ejbJarWebServicesSupport);
        apiJaxwsSupport = JAXWSSupportFactory.createJAXWSSupport(jaxwsSupport);
        apiWebServicesClientSupport = WebServicesClientSupportFactory.createWebServicesClientSupport(ejbJarWebServicesClientSupport);
        apiJAXWSClientSupport = JAXWSClientSupportFactory.createJAXWSClientSupport(jaxWsClientSupport);
        classPathModifier = new ClassPathModifier(this, this.updateHelper, eval, refHelper,
            new ClassPathSupportCallbackImpl(helper), createClassPathModifierCallback(), 
            getClassPathUiSupportCallback());
        classPathExtender = new ClassPathExtender(classPathModifier, ProjectProperties.JAVAC_CLASSPATH, ClassPathSupportCallbackImpl.ELEMENT_INCLUDED_LIBRARIES);
        lookup = createLookup(aux, cpProvider);
        css = new CopyOnSaveSupport();
        artifactSupport = new ArtifactCopySupport();
        deployOnSaveSupport = new DeployOnSaveSupportProxy();
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

    private ClassPathModifier.Callback createClassPathModifierCallback() {
        return new ClassPathModifier.Callback() {
            public String getClassPathProperty(SourceGroup sg, String type) {
                assert sg != null : "SourceGroup cannot be null";  //NOI18N
                assert type != null : "Type cannot be null";  //NOI18N
                final String[] classPathProperty = getClassPathProvider().getPropertyName (sg, type);
                if (classPathProperty == null || classPathProperty.length == 0) {
                    throw new UnsupportedOperationException ("Modification of [" + sg.getRootFolder().getPath() +", " + type + "] is not supported"); //NOI18N
                }
                return classPathProperty[0];
            }

            public String getElementName(String classpathProperty) {
                if (ProjectProperties.JAVAC_CLASSPATH.equals(classpathProperty)) {
                    return ClassPathSupportCallbackImpl.ELEMENT_INCLUDED_LIBRARIES;
                }
                return null;
            }
        };        
    }

    public synchronized ClassPathUiSupport.Callback getClassPathUiSupportCallback() {
        if (classPathUiSupportCallback == null) {
            classPathUiSupportCallback = new ClassPathUiSupport.Callback() {
                public void initItem(ClassPathSupport.Item item) {
                    if (item.getType() != ClassPathSupport.Item.TYPE_LIBRARY || !item.getLibrary().getType().equals(J2eePlatform.LIBRARY_TYPE)) {
                        item.setAdditionalProperty(ClassPathSupportCallbackImpl.INCLUDE_IN_DEPLOYMENT, "true");
                    }
                }
            };
            
        }
        return classPathUiSupportCallback;
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
        return "EjbJarProject[" + getProjectDirectory() + "]"; // NOI18N
    }
    
    private PropertyEvaluator createEvaluator() {
        return helper.getStandardPropertyEvaluator();
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
    
    public DeployOnSaveSupport getDeployOnSaveSupport() {
        return deployOnSaveSupport;
    }
    
    private Lookup createLookup(AuxiliaryConfiguration aux, ClassPathProviderImpl cpProvider) {
        SubprojectProvider spp = refHelper.createSubprojectProvider();
        FileEncodingQueryImplementation encodingQuery = QuerySupport.createFileEncodingQuery(evaluator(), EjbJarProjectProperties.SOURCE_ENCODING);
        EjbJarSources sources = new EjbJarSources(this, helper, evaluator(), getSourceRoots(), getTestSourceRoots());
        Lookup base = Lookups.fixed(new Object[] {
                EjbJarProject.this, // never cast an externally obtained Project to EjbJarProject - use lookup instead
                buildExtender,
                new Info(),
                aux,
                helper.createCacheDirectoryProvider(),
                helper.createAuxiliaryProperties(),
                new ProjectWebServicesSupportProvider(), // implementation of WebServicesClientSupportProvider commented out
                spp,
                EjbEnterpriseReferenceContainerSupport.createEnterpriseReferenceContainer(this, helper),
                EjbJarSupport.createEjbJarProvider(this, apiEjbJar),
                EjbJarSupport.createEjbJarsInProject(apiEjbJar),
                ejbModule, //implements J2eeModuleProvider
                // FIXME this is just fallback for code searching for the old SPI in lookup
                // remove in next release
                new EjbJarImpl(apiEjbJar),
                new EjbJarActionProvider( this, helper, refHelper, updateHelper, eval ),
                new EjbJarLogicalViewProvider(this, updateHelper, evaluator(), spp, refHelper),
                new CustomizerProviderImpl( this, updateHelper, evaluator(), refHelper ),
                LookupMergerSupport.createClassPathProviderMerger(cpProvider),
                QuerySupport.createCompiledSourceForBinaryQuery(helper, evaluator(), getSourceRoots(), getTestSourceRoots()),
                QuerySupport.createJavadocForBinaryQuery(helper, evaluator()),
                new AntArtifactProviderImpl(),
                new ProjectXmlSavedHookImpl(),
                UILookupMergerSupport.createProjectOpenHookMerger(new ProjectOpenedHookImpl()),
                QuerySupport.createUnitTestForSourceQuery(getSourceRoots(), getTestSourceRoots()),
                QuerySupport.createSourceLevelQuery(evaluator()),
                sources,
                sources.getSourceGroupModifierImplementation(),
                QuerySupport.createSharabilityQuery(helper, evaluator(), getSourceRoots(), getTestSourceRoots(),
                        EjbJarProjectProperties.META_INF),
                QuerySupport.createFileBuiltQuery(helper, evaluator(), getSourceRoots(), getTestSourceRoots()),
                encodingQuery,
                new RecommendedTemplatesImpl(updateHelper),
                refHelper,
                classPathExtender,
                classPathModifier,
                new EjbJarProjectOperations(this),
                new EjbJarPersistenceProvider(this, evaluator(), cpProvider),
                new EjbJarEMGenStrategyResolver(),
                new EjbJarJPASupport(this),
                Util.createServerStatusProvider(getEjbModule()),
                new EjbJarJPAModuleInfo(this),
                UILookupMergerSupport.createPrivilegedTemplatesMerger(),
                UILookupMergerSupport.createRecommendedTemplatesMerger(),
                LookupProviderSupport.createSourcesMerger(),
                QuerySupport.createTemplateAttributesProvider(helper, encodingQuery),
                ExtraSourceJavadocSupport.createExtraSourceQueryImplementation(this, helper, eval),
                LookupMergerSupport.createSFBLookupMerger(),
                ExtraSourceJavadocSupport.createExtraJavadocQueryImplementation(this, helper, eval),
                LookupMergerSupport.createJFBLookupMerger(),
                QuerySupport.createBinaryForSourceQueryImplementation(sourceRoots, testRoots, helper, eval),
                // TODO: AB: maybe add "this" to the lookup. You should not cast a Project to EjbJarProject, but use the lookup instead.
            });
            return LookupProviderSupport.createCompositeLookup(base, "Projects/org-netbeans-modules-j2ee-ejbjarproject/Lookup"); //NOI18N
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
            this.sourceRoots = SourceRoots.create(this.updateHelper, evaluator(), getReferenceHelper(), EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "source-roots", false, "src.{0}{1}.dir"); //NOI18N
        }
        return this.sourceRoots;
    }
    
    public synchronized SourceRoots getTestSourceRoots() {
        if (this.testRoots == null) { //Local caching, no project metadata access
            this.testRoots = SourceRoots.create(this.updateHelper, evaluator(), getReferenceHelper(), EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "test-roots", true, "test.{0}{1}.dir"); //NOI18N
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
        !UserProjectSettings.getDefault().isShowAgainBrokenRefAlert()) {
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
                        if (dlg != null) {
                            dlg.dispose();
                        }
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
        return ProjectManager.mutex().readAccess(new Mutex.Action<String>() {
            public String run() {
                Element data = updateHelper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); //NOI18N
                if (nl.getLength() == 1) {
                    nl = nl.item(0).getChildNodes();
                    if (nl.getLength() == 1 && nl.item(0).getNodeType() == Node.TEXT_NODE) {
                        return ((Text) nl.item(0)).getNodeValue();
                    }
                }
                return "A Broken EJB Project"; // NOI18N
            }
        });
    }

    /** Store configured project name. */
    public void setName(final String name) {
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                Element data = helper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); //NOI18N
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); //NOI18N
                    data.insertBefore(nameEl, /* OK if null */data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                helper.putPrimaryConfigurationData(data, true);
            }
        });
    }

    private void updateProjectXML () throws IOException {
        Element element = aux.getConfigurationFragment("data","http://www.netbeans.org/ns/EjbJar-project/1",true);    //NOI18N
        if (element != null) {
            Document doc = element.getOwnerDocument();
            Element newRoot = doc.createElementNS (EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,"data"); //NOI18N
            copyDocument (doc, element, newRoot);
            Element srcRoots = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "source-roots");  //NOI18N
            Element root = doc.createElementNS (EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
            root.setAttribute ("id","src.dir");   //NOI18N
            srcRoots.appendChild(root);
            newRoot.appendChild (srcRoots);
            Element tstRoots = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,"test-roots");  //NOI18N
            root = doc.createElementNS (EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
            root.setAttribute ("id","test.src.dir");   //NOI18N
            tstRoots.appendChild (root);
            newRoot.appendChild (tstRoots);
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
                    ProjectManager.mutex().writeAccess(new Runnable() {
                        public void run() {
                            EditableProperties ep = helper.getProperties(
                                    AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                            EditableProperties projectProps = helper.getProperties(
                                    AntProjectHelper.PROJECT_PROPERTIES_PATH);

                            if (!J2EEProjectProperties.isUsingServerLibrary(projectProps,
                                    EjbJarProjectProperties.J2EE_PLATFORM_CLASSPATH)) {
                                String classpath = Utils.toClasspathString(platform.getClasspathEntries());
                                ep.setProperty(EjbJarProjectProperties.J2EE_PLATFORM_CLASSPATH, classpath);
                            }
                            helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                            try {
                                ProjectManager.getDefault().saveProject(EjbJarProject.this);
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
                classPathExtender.addArchiveFile(fo);
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
            if (askInCurrentThread) {
                run.run();
            } else {
                RequestProcessor.getDefault().post(run);
            }
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
                
                // Register copy on save support
                css.initialize();
                
                if (libFolderName != null && helper.resolveFile (libFolderName).isDirectory ()) {
                    libFolder = helper.resolveFileObject(libFolderName);
                        FileObject[] children = libFolder.getChildren ();
                        List<FileObject> libs = new LinkedList<FileObject>();
                        for (int i = 0; i < children.length; i++) {
                            if (FileUtil.isArchiveFile(children[i])) {
                                libs.add(children[i]);
                            }
                        }
                        FileObject[] libsArray = new FileObject[libs.size()];
                        libs.toArray(libsArray);
                        classPathExtender.addArchiveFiles(ProjectProperties.JAVAC_CLASSPATH, libsArray, ClassPathSupportCallbackImpl.ELEMENT_INCLUDED_LIBRARIES);
                        libFolder.addFileChangeListener (EjbJarProject.this);
                }
                
                // Check up on build scripts.
                
                 refreshBuildImplXml( GeneratedFilesHelper.FLAG_OLD_PROJECT_XML, true, true);
                
                genFilesHelper.refreshBuildScript(
                    getBuildXmlName(),
                    EjbJarProject.class.getResource("resources/build.xsl"), // NOI18N
                    true);
                
                String servInstID = getProperty(AntProjectHelper.PRIVATE_PROPERTIES_PATH, EjbJarProjectProperties.J2EE_SERVER_INSTANCE);
                String serverType = null;
                J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(servInstID);
                if (platform != null) {
                    // updates j2ee.platform.cp & wscompile.cp & reg. j2ee platform listener
                    EjbJarProjectProperties.setServerInstance(EjbJarProject.this, EjbJarProject.this.helper, servInstID);
                } else {
                    // if there is some server instance of the type which was used
                    // previously do not ask and use it
                    serverType = getProperty(AntProjectHelper.PROJECT_PROPERTIES_PATH, EjbJarProjectProperties.J2EE_SERVER_TYPE);
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
                // UI Logging
                Utils.logUI(NbBundle.getBundle(EjbJarProject.class), "UI_EJB_PROJECT_OPENED", // NOI18N
                        new Object[] {(serverType != null ? serverType : Deployment.getDefault().getServerID(servInstID)), servInstID});

                String serverName = "";  // NOI18N
                try {
                    if (servInstID != null) {
                        serverName = Deployment.getDefault().getServerInstance(servInstID).getServerDisplayName();
                    }
                }
                catch (InstanceRemovedException ier) {
                    // ignore
                }

                Utils.logUsage(EjbJarProject.class, "USG_PROJECT_OPEN_EJB", new Object[] { serverName }); // NOI18N
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
            
            // register project's classpaths to GlobalPathRegistry;
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
            
            String deployOnSave = getProperty(AntProjectHelper.PROJECT_PROPERTIES_PATH, EjbJarProjectProperties.J2EE_DEPLOY_ON_SAVE);
            if (Boolean.parseBoolean(deployOnSave)) {
                Deployment.getDefault().enableCompileOnSaveSupport(ejbModule);
            }
            artifactSupport.enableArtifactSynchronization(true);
            
            EjbJarLogicalViewProvider physicalViewProvider = EjbJarProject.this.getLookup().lookup(EjbJarLogicalViewProvider.class);
            if (physicalViewProvider != null &&  physicalViewProvider.hasBrokenLinks()) {   
                BrokenReferencesSupport.showAlert();
            }
            if(apiWebServicesSupport.isBroken(EjbJarProject.this)) {
                apiWebServicesSupport.showBrokenAlert(EjbJarProject.this);
            }
            else if(apiWebServicesClientSupport.isBroken(EjbJarProject.this)) {
                apiWebServicesClientSupport.showBrokenAlert(EjbJarProject.this);
            }
        }
        
        private void updateProject() {
            // Make it easier to run headless builds on the same machine at least.
            EditableProperties ep = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
            ep.setProperty("netbeans.user", System.getProperty("netbeans.user"));

            // set jaxws.endorsed.dir property (for endorsed mechanism to be used with wsimport, wsgen)
            WSUtils.setJaxWsEndorsedDirProperty(ep);

            // #134642 - use Ant task from copylibs library
            SharabilityUtility.makeSureProjectHasCopyLibsLibrary(helper, refHelper);
            
            //update lib references in project properties
            EditableProperties props = updateHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            J2EEProjectProperties.removeObsoleteLibraryLocations(ep);
            J2EEProjectProperties.removeObsoleteLibraryLocations(props);
            
            if (!props.containsKey(ProjectProperties.INCLUDES)) {
                props.setProperty(ProjectProperties.INCLUDES, "**"); // NOI18N
            }
            if (!props.containsKey(ProjectProperties.EXCLUDES)) {
                props.setProperty(ProjectProperties.EXCLUDES, ""); // NOI18N
            }
            if (!props.containsKey("build.generated.sources.dir")) { // NOI18N
                props.setProperty("build.generated.sources.dir", "${build.dir}/generated-sources"); // NOI18N
            }
            

            updateHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);            
            
            helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
            // update a dual build directory project to use a single build directory
            ep = updateHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            String earBuildDir = ep.getProperty(EjbJarProjectProperties.BUILD_EAR_CLASSES_DIR);
            if (null != earBuildDir) {
                // there is an BUILD_EAR_CLASSES_DIR property... we may 
                //  need to change its value
                String buildDir = ep.getProperty(ProjectProperties.BUILD_CLASSES_DIR);
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
            
            // Probably unnecessary, but just in case:
            try {
                ProjectManager.getDefault().saveProject(EjbJarProject.this);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
            
            // Unregister copy on save support
            try {
                css.cleanup();
            } 
            catch (FileStateInvalidException e) {
                LOGGER.log(Level.INFO, null, e);
            }
            
            artifactSupport.enableArtifactSynchronization(false);
            Deployment.getDefault().disableCompileOnSaveSupport(ejbModule);
            
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
                helper.createSimpleAntArtifact(JavaProjectConstants.ARTIFACT_TYPE_JAR, "dist.jar", helper.getStandardPropertyEvaluator(), "dist", "clean"), // NOI18N
                helper.createSimpleAntArtifact(EjbProjectConstants.ARTIFACT_TYPE_EJBJAR, "dist.jar", helper.getStandardPropertyEvaluator(), "dist", "clean"), // NOI18N
                helper.createSimpleAntArtifact(EjbProjectConstants.ARTIFACT_TYPE_J2EE_MODULE_IN_EAR_ARCHIVE, "dist.ear.jar", helper.getStandardPropertyEvaluator(), "dist-ear", "clean-ear") // NOI18N
            };
        }
    }

    private class DeployOnSaveSupportProxy implements DeployOnSaveSupport {

        public synchronized void addArtifactListener(ArtifactListener listener) {
            css.addArtifactListener(listener);
            artifactSupport.addArtifactListener(listener);
        }

        public synchronized void removeArtifactListener(ArtifactListener listener) {
            css.removeArtifactListener(listener);
            artifactSupport.removeArtifactListener(listener);
        }

        public boolean containsIdeArtifacts() {
            return DeployOnSaveUtils.containsIdeArtifacts(eval, updateHelper, "build.classes.dir");
        }

    }
    
    /**
     * This class handle copying of meta-inf resources to appropriate place in build
     * dir. This class is used in true Deploy On Save.
     *
     * Class should not request project lock from FS listener methods
     * (deadlock prone).
     */
    public class CopyOnSaveSupport extends FileChangeAdapter implements PropertyChangeListener {

        private static final String META_INF_FOLDER = "META-INF";

        private FileObject metaBase = null;

        private String metaBaseValue = null;

        private String buildClasses = null;

        private final List<ArtifactListener> listeners = new CopyOnWriteArrayList<ArtifactListener>();

        /** Creates a new instance of CopyOnSaveSupport */
        public CopyOnSaveSupport() {
            super();
        }

        public void addArtifactListener(ArtifactListener listener) {
            listeners.add(listener);
        }

        public void removeArtifactListener(ArtifactListener listener) {
            listeners.remove(listener);
        }

        public void initialize() throws FileStateInvalidException {
            metaBase = getEjbModule().getMetaInf();
            metaBaseValue = evaluator().getProperty(EjbJarProjectProperties.META_INF);
            buildClasses = evaluator().getProperty(ProjectProperties.BUILD_CLASSES_DIR);

            if (metaBase != null) {
                metaBase.getFileSystem().addFileChangeListener(this);
            }

            LOGGER.log(Level.FINE, "Meta directory is {0}", metaBaseValue);

            EjbJarProject.this.evaluator().addPropertyChangeListener(this);
        }

        public void cleanup() throws FileStateInvalidException {
            if (metaBase != null) {
                metaBase.getFileSystem().removeFileChangeListener(this);
            }
            EjbJarProject.this.evaluator().removePropertyChangeListener(this);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (EjbJarProjectProperties.META_INF.equals(evt.getPropertyName())) {
                try {
                    cleanup();
                    initialize();
                } catch (org.openide.filesystems.FileStateInvalidException e) {
                    LOGGER.log(Level.INFO, null, e);
                }
            } else if (ProjectProperties.BUILD_CLASSES_DIR.equals(evt.getPropertyName())) {
                // TODO copy all files ?
                Object value = evt.getNewValue();
                buildClasses = value == null ? null : value.toString();
            }
        }

        @Override
        public void fileChanged(FileEvent fe) {
            try {
                handleCopyFileToDestDir(fe.getFile());
            } catch (IOException e) {
                LOGGER.log(Level.INFO, null, e);
            }
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            try {
                handleCopyFileToDestDir(fe.getFile());
            } catch (IOException e) {
                LOGGER.log(Level.INFO, null, e);
            }
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            try {
                FileObject fo = fe.getFile();
                FileObject metaBase = getEjbModule().resolveMetaInf(metaBaseValue);
                if (metaBase != null && FileUtil.isParentOf(metaBase, fo)) {
                    // inside docbase
                    handleCopyFileToDestDir(fo);
                    FileObject parent = fo.getParent();
                    String path;
                    if (FileUtil.isParentOf(metaBase, parent)) {
                        path = META_INF_FOLDER + "/" + FileUtil.getRelativePath(metaBase, fo.getParent()) +
                            "/" + fe.getName() + "." + fe.getExt();
                    } else {
                        path = META_INF_FOLDER + "/" + fe.getName() + "." + fe.getExt();
                    }
                    if (!isSynchronizationAppropriate(path)) {
                        return;
                    }
                    handleDeleteFileInDestDir(path);
                }
            } catch (IOException e) {
                LOGGER.log(Level.INFO, null, e);
            }
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            try {
                FileObject fo = fe.getFile();
                FileObject metaBase = getEjbModule().resolveMetaInf(metaBaseValue);
                if (metaBase != null && FileUtil.isParentOf(metaBase, fo)) {
                    // inside docbase
                    String path = META_INF_FOLDER + "/" + FileUtil.getRelativePath(metaBase, fo); // NOI18N
                    if (!isSynchronizationAppropriate(path)) {
                        return;
                    }
                    handleDeleteFileInDestDir(path);
                }
            } catch (IOException e) {
                LOGGER.log(Level.INFO, null, e);
            }
        }

        private boolean isSynchronizationAppropriate(String filePath) {
            return true;
        }

        private void fireArtifactChange(Iterable<ArtifactListener.Artifact> files) {
            for (ArtifactListener listener : listeners) {
                listener.artifactsUpdated(files);
            }
        }

        private void handleDeleteFileInDestDir(String resourcePath) throws IOException {
            File deleted = null;
            FileObject ejbBuildBase = buildClasses == null ? null : helper.resolveFileObject(buildClasses);
            if (ejbBuildBase != null) {
                // project was built
                FileObject toDelete = ejbBuildBase.getFileObject(resourcePath);
                if (toDelete != null) {
                    deleted = FileUtil.toFile(toDelete);
                    toDelete.delete();
                }
                if (deleted != null) {
                    fireArtifactChange(Collections.singleton(ArtifactListener.Artifact.forFile(deleted)));
                }
            }
        }

        private void handleCopyFileToDestDir(FileObject fo) throws IOException {
            if (fo.isVirtual()) {
                return;
            }

            FileObject metaBase = getEjbModule().resolveMetaInf(metaBaseValue);
            if (metaBase != null && FileUtil.isParentOf(metaBase, fo)) {
                // inside docbase
                String path = META_INF_FOLDER + "/" + FileUtil.getRelativePath(metaBase, fo); // NOI18N
                if (!isSynchronizationAppropriate(path)) {
                    return;
                }
                FileObject ejbBuildBase = buildClasses == null ? null : helper.resolveFileObject(buildClasses);
                if (ejbBuildBase != null) {
                    // project was built
                    if (FileUtil.isParentOf(metaBase, ejbBuildBase) || FileUtil.isParentOf(ejbBuildBase, metaBase)) {
                        //cannot copy into self
                        return;
                    }
                    FileObject destFile = ensureDestinationFileExists(ejbBuildBase, path, fo.isFolder());
                    assert destFile != null : "ejbBuildBase: " + ejbBuildBase + ", path: " + path + ", isFolder: " + fo.isFolder();
                    if (!fo.isFolder()) {
                        InputStream is = null;
                        OutputStream os = null;
                        FileLock fl = null;
                        try {
                            is = fo.getInputStream();
                            fl = destFile.lock();
                            os = destFile.getOutputStream(fl);
                            FileUtil.copy(is, os);
                        } finally {
                            if (is != null) {
                                is.close();
                            }
                            if (os != null) {
                                os.close();
                            }
                            if (fl != null) {
                                fl.releaseLock();
                            }
                            File file = FileUtil.toFile(destFile);
                            if (file != null) {
                                fireArtifactChange(Collections.singleton(ArtifactListener.Artifact.forFile(file)));
                            }
                        }
                    }
                }
            }
        }

        /**
         * Returns the destination (parent) directory needed to create file
         * with relative path path under ejbBuilBase.
         */
        private FileObject ensureDestinationFileExists(FileObject ejbBuildBase, String path, boolean isFolder) throws IOException {
            FileObject current = ejbBuildBase;
            StringTokenizer st = new StringTokenizer(path, "/");
            while (st.hasMoreTokens()) {
                String pathItem = st.nextToken();
                FileObject newCurrent = current.getFileObject(pathItem);
                if (newCurrent == null) {
                    // need to create it
                    if (isFolder || st.hasMoreTokens()) {
                        // create a folder
                        newCurrent = FileUtil.createFolder(current, pathItem);
                        assert newCurrent != null : "ejbBuildBase: " + ejbBuildBase + ", path: " + path + ", isFolder: " + isFolder;
                    } else {
                        newCurrent = FileUtil.createData(current, pathItem);
                        assert newCurrent != null : "ejbBuildBase: " + ejbBuildBase + ", path: " + path + ", isFolder: " + isFolder;
                    }
                }
                assert newCurrent != null : "ejbBuildBase: " + ejbBuildBase + ", path: " + path + ", isFolder: " + isFolder;
                current = newCurrent;
            }
            assert current != null : "ejbBuildBase: " + ejbBuildBase + ", path: " + path + ", isFolder: " + isFolder;
            return current;
        }
    }

    private class ArtifactCopySupport extends ArtifactCopyOnSaveSupport {

        public ArtifactCopySupport() {
            super("build.classes.dir", evaluator(), getAntProjectHelper()); // NOI18N
        }

        @Override
        public Map<Item, String> getArtifacts() {
            final AntProjectHelper helper = getAntProjectHelper();

            ClassPathSupport cs = new ClassPathSupport(evaluator(), getReferenceHelper(), helper,
                getUpdateHelper(), new ClassPathSupportCallbackImpl(helper));

            Map<Item, String> result = new HashMap<Item, String>();
            for (ClassPathSupport.Item item : cs.itemsList(
                    helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(ProjectProperties.JAVAC_CLASSPATH),
                    ClassPathSupportCallbackImpl.ELEMENT_INCLUDED_LIBRARIES)) {

                if (!item.isBroken() && item.getType() == ClassPathSupport.Item.TYPE_ARTIFACT) {
                    String included = item.getAdditionalProperty(ClassPathSupportCallbackImpl.INCLUDE_IN_DEPLOYMENT);
                    if (Boolean.parseBoolean(included)) {
                        result.put(item, "");
                    }
                }
            }
            return result;
        }

        @Override
        protected Artifact filterArtifact(Artifact artifact) {
            return artifact.relocatable();
        }

    }

    // List of primarily supported templates
    private static final String[] TYPES = new String[] {
        "java-classes",         // NOI18N
        "ejb-types_2_1",      // NOI18N
        "ejb-types",            // NOI18N
        "ejb-types-server",     // NOI18N
        "web-services",         // NOI18N
        "message-handler",         // NOI18N
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
     * Supported template categories for Java EE 6 projects (full?).
     */
    private static final String[] JAVAEE6_TYPES = new String[] {
        "java-classes",         // NOI18N
        "ejb-types",            // NOI18N
        "ejb-types-server",     // NOI18N
        "ejb-types_3_1",        // NOI18N
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

    private static final String[] PRIVILEGED_NAMES_EE6 = PRIVILEGED_NAMES_EE5;
    
    private static final String[] PRIVILEGED_NAMES_ARCHIVE = new String[] {
        "Templates/J2EE/ejbJarXml", // NOI18N
    };

    private final class RecommendedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates {
        transient private boolean isEE5 = false;
        transient private boolean isEE6 = false;//if project support ee6 full version
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
            } else if (isEE6) {
                retVal = JAVAEE6_TYPES;
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
            } else if(isEE6) {
                retVal = PRIVILEGED_NAMES_EE6;
            } else {
                retVal = PRIVILEGED_NAMES;
            } 
            return retVal;
        }
        
        private void checkEnvironment(){
            if (!checked){
                Profile version=Profile.fromPropertiesString(evaluator().getProperty(EjbJarProjectProperties.J2EE_PLATFORM));
                isEE5 = Profile.JAVA_EE_5==version;
                isEE6 = Profile.JAVA_EE_6_FULL==version;
                final Object srcType = helper.getAntProjectHelper().
                        getStandardPropertyEvaluator().getProperty(EjbJarProjectProperties.JAVA_SOURCE_BASED);
                if ("false".equals(srcType)) {
                    isArchive = true;
                }
                checked = true;
            }
        }
    }

    // FIXME this is just fallback for code searching for the old SPI in lookup
    // remove in next release
    @SuppressWarnings("deprecation")
    private class EjbJarImpl implements EjbJarImplementation {

        private final EjbJar apiModule;

        public EjbJarImpl(EjbJar apiModule) {
            this.apiModule = apiModule;
        }

        public FileObject getDeploymentDescriptor() {
            return apiModule.getDeploymentDescriptor();
        }

        public String getJ2eePlatformVersion() {
            return apiModule.getJ2eePlatformVersion();
        }

        public FileObject[] getJavaSources() {
            return apiModule.getJavaSources();
        }

        public FileObject getMetaInf() {
            return apiModule.getMetaInf();
        }

        public MetadataModel<EjbJarMetadata> getMetadataModel() {
            return apiModule.getMetadataModel();
        }
    }

    private class EjbJarImpl2 implements EjbJarImplementation2 {

        private final EjbJarProvider provider;

        public EjbJarImpl2(EjbJarProvider provider) {
            this.provider = provider;
        }

        public FileObject getDeploymentDescriptor() {
            return provider.getDeploymentDescriptor();
        }

        public Profile getJ2eeProfile() {
            return provider.getJ2eeProfile();
        }

        public FileObject[] getJavaSources() {
            return provider.getJavaSources();
        }

        public FileObject getMetaInf() {
            return provider.getMetaInf();
        }

        public MetadataModel<EjbJarMetadata> getMetadataModel() {
            return provider.getMetadataModel();
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
