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
package org.netbeans.modules.xslt.project;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.xml.catalogsupport.ProjectConstants;

import org.netbeans.modules.compapp.projects.base.spi.JbiArtifactProvider;
import org.netbeans.modules.compapp.projects.base.ui.customizer.IcanproProjectProperties;
import org.netbeans.modules.compapp.projects.base.IcanproConstants;
import org.netbeans.modules.compapp.projects.base.queries.IcanproProjectEncodingQueryImpl;
import org.netbeans.modules.compapp.projects.base.ui.IcanproXmlCustomizerProvider;

import static org.netbeans.modules.xslt.project.XsltproConstants.*;
import org.netbeans.modules.xml.catalogsupport.DefaultProjectCatalogSupport;
import org.netbeans.modules.xslt.project.spi.ProjectsFilesChangeHandler;
import org.netbeans.modules.xslt.project.wizard.IcanproLogicalViewProvider;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 *
 * @author Chris Webster
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class XsltproProject implements Project, AntProjectListener {
    
    private static final Icon PROJECT_ICON = new ImageIcon(Utilities.loadImage(XSLT_PROJECT_ICON)); // NOI18N
    public static final String SOURCES_TYPE_XSLTPRO = "BIZPRO";
    public static final String ARTIFACT_TYPE_JBI_ASA = "CAPS.asa";
    
    public static final String MODULE_INSTALL_NAME = "modules/org-netbeans-modules-xslt-project.jar";
    public static final String MODULE_INSTALL_CBN = "org.netbeans.modules.xslt.project";
    public static final String MODULE_INSTALL_DIR = "module.install.dir";
    
    private static final Logger LOG = Logger.getLogger(XsltproProject.class.getName());

    private final AntProjectHelper helper;
    private Lookup lookup;
    private PropertyEvaluator evaluator;
    private ReferenceHelper refHelper;
    private GeneratedFilesHelper genFilesHelper;
    private ProjectsFilesChangeHandler myProjectsChangeHandler;
    
    public XsltproProject(AntProjectHelper helper) throws IOException {
        this.helper = helper;
        
        this.evaluator = createEvaluator();
        AuxiliaryConfiguration aux = helper.createAuxiliaryConfiguration();
        this.refHelper = new ReferenceHelper(helper, aux, helper.getStandardPropertyEvaluator());
        this.genFilesHelper = new GeneratedFilesHelper(helper);
        myProjectsChangeHandler = new ProjectsFilesChangeHandler(this);
        this.lookup = createLookup(aux);
        helper.addAntProjectListener(this);
    }
    
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }
    
    public AntProjectHelper getAntProjectHelper() {
        return helper;
    }

    public ReferenceHelper getReferenceHelper() {
        return this.refHelper;
    }
    
    @Override
    public String toString() {
        return "XsltproProject[" + getProjectDirectory() + "]"; // NOI18N
    }
    
    public Lookup getLookup() {
        return lookup;
    }
    
    /** Return configured project name. */
    public String getName() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<String>() {
            public String run() {
                Element data = helper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(XsltproProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
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
        ProjectManager.mutex().writeAccess(new Mutex.Action<Object>() {
            public Object run() {
                Element data = helper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(XsltproProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(XsltproProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                    data.insertBefore(nameEl, /* OK if null */data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                helper.putPrimaryConfigurationData(data, true);
                return null;
            }
        });
    }

    public void configurationXmlChanged(AntProjectEvent ev) {
        // TODO m
        if (ev.getPath().equals(AntProjectHelper.PROJECT_XML_PATH)) {
            // Could be various kinds of changes, but name & displayName might have changed.
            Info info = (Info)getLookup().lookup(ProjectInformation.class);
            info.firePropertyChange(ProjectInformation.PROP_NAME);
            info.firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME);
        }
    }
    
    public void propertiesChanged(AntProjectEvent ev) {
    }
    
    PropertyEvaluator evaluator() {
        return evaluator;
    }
    
    String getBuildXmlName() {
        String storedName = helper.getStandardPropertyEvaluator().getProperty(IcanproProjectProperties.BUILD_FILE);
        return storedName == null ? GeneratedFilesHelper.BUILD_XML_PATH : storedName;
    }
    
    
    FileObject getSourceDirectory() {
        String srcDir = helper.getStandardPropertyEvaluator().getProperty("src.dir"); // NOI18N
        return helper.resolveFileObject(srcDir);
    }
    
    // TODO r
//    /** Last time in ms when the Broken References alert was shown. */
//    private static long brokenAlertLastTime = 0;
//
//    /** Is Broken References alert shown now? */
//    private static boolean brokenAlertShown = false;
//
//    /** Timeout within which request to show alert will be ignored. */
//    private static int BROKEN_ALERT_TIMEOUT = 1000;
//
    
    private PropertyEvaluator createEvaluator() {
        // XXX might need to use a custom evaluator to handle active platform substitutions... TBD
        return helper.getStandardPropertyEvaluator();
    }
    
    private Lookup createLookup(AuxiliaryConfiguration aux) {
        SubprojectProvider spp = refHelper.createSubprojectProvider();
        FileBuiltQueryImplementation fileBuilt = helper.createGlobFileBuiltQuery(helper.getStandardPropertyEvaluator(),
                new String[] {"${src.dir}/*.java"}, // NOI18N
                new String[] {"${build.classes.dir}/*.class"} // NOI18N
        );
        final SourcesHelper sourcesHelper = new SourcesHelper(helper, evaluator());
        String webModuleLabel = org.openide.util.NbBundle.getMessage(XsltproProject.class, "LBL_Node_EJBModule"); //NOI18N
        String srcJavaLabel = org.openide.util.NbBundle.getMessage(XsltproProject.class, "LBL_Node_Sources"); //NOI18N
        
        sourcesHelper.addPrincipalSourceRoot("${"+IcanproProjectProperties.SOURCE_ROOT+"}", webModuleLabel, /*XXX*/null, null);
        sourcesHelper.addPrincipalSourceRoot("${"+IcanproProjectProperties.SRC_DIR+"}", srcJavaLabel, /*XXX*/null, null);
        
        sourcesHelper.addTypedSourceRoot("${"+IcanproProjectProperties.SRC_DIR+"}", SOURCES_TYPE_XSLTPRO, srcJavaLabel, /*XXX*/null, null);
//        sourcesHelper.addTypedSourceRoot("${"+SRC_DIR+"}", JavaProjectConstants.SOURCES_TYPE_JAVA, srcJavaLabel, /*XXX*/null, null);
        sourcesHelper.addTypedSourceRoot("${"+IcanproProjectProperties.SRC_DIR+"}", ProjectConstants.SOURCES_TYPE_XML, srcJavaLabel, /*XXX*/null, null);
        
        ProjectManager.mutex().postWriteRequest(new Runnable() {
            public void run() {
                sourcesHelper.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
            }
        });
        
        return Lookups.fixed(new Object[] {
            new Info(),
            aux,
            helper.createCacheDirectoryProvider(),
//B            new ProjectWebServicesSupportProvider(),
            // XXX the helper should not be exposed
            helper,
            spp,
            new XsltproActionProvider( this, helper, refHelper ),
            new IcanproLogicalViewProvider(this, helper, evaluator(), spp, refHelper),
//            new XsltProjectCustomizerProvider(this),
            new IcanproXmlCustomizerProvider(this, helper, refHelper, 
                    XsltproProjectType.PROJECT_CONFIGURATION_NAMESPACE),
            // provides information about added/removed schema and wsdl files
            // forwards related model property change events
            myProjectsChangeHandler,
            new JbiArtifactProviderImpl(),
            new ProjectXmlSavedHookImpl(),
            //todo m
            new ProjectOpenedHookImpl(this),
            new XsltProjectOperations(this),
            fileBuilt,
            new RecommendedTemplatesImpl(),
            refHelper,
            new IcanproProjectEncodingQueryImpl(evaluator()),
            sourcesHelper.createSources(),
            helper.createSharabilityQuery(evaluator(),
                    new String[] {"${"+IcanproProjectProperties.SOURCE_ROOT+"}"},
                    new String[] {
                "${"+IcanproProjectProperties.BUILD_DIR+"}",
                "${"+IcanproProjectProperties.DIST_DIR+"}"}
            )
            ,
            new DefaultProjectCatalogSupport(this, helper, refHelper)
            
        
            
        });
    }
    
    // private inner classes ---------------------------------------------------
    
    /**
     * @see org.netbeans.api.project.ProjectInformation
     */
    private final class Info implements ProjectInformation {
        
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        
        Info() {}
        
        void firePropertyChange(String prop) {
            pcs.firePropertyChange(prop, null, null);
        }
        
        public String getName() {
            return XsltproProject.this.getName();
        }
        
        public String getDisplayName() {
            return XsltproProject.this.getName();
        }
        
        public Icon getIcon() {
            return PROJECT_ICON;
        }
        
        public Project getProject() {
            return XsltproProject.this;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }
        
    }
    
    /**
     * @see org.netbeans.spi.project.support.ant.ProjectXmlSavedHook
     */
    private final class ProjectXmlSavedHookImpl extends ProjectXmlSavedHook {
        
        ProjectXmlSavedHookImpl() {}

        protected void projectXmlSaved() throws IOException {
            genFilesHelper.refreshBuildScript(
                    GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                    XsltproProject.class.getResource("resources/build-impl.xsl"),
                    false);
            genFilesHelper.refreshBuildScript(
                    getBuildXmlName(),
                    XsltproProject.class.getResource("resources/build.xsl"),
                    false);
        }
        
    }
    
    /**
     * @see org.netbeans.spi.project.ui.ProjectOpenedHook
     */
    private final class ProjectOpenedHookImpl extends ProjectOpenedHook {
        
        // TODO m
        ProjectOpenedHookImpl(Project project) {
        }
        
        protected void projectOpened() {
            
            try {
                // Check up on build scripts.
                genFilesHelper.refreshBuildScript(
                        GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                        XsltproProject.class.getResource("resources/build-impl.xsl"),
                        true);
                genFilesHelper.refreshBuildScript(
                        getBuildXmlName(),
                        XsltproProject.class.getResource("resources/build.xsl"),
                        true);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            
            // Make it easier to run headless builds on the same machine at least.
            try {
                getProjectDirectory().getFileSystem().runAtomicAction(
                        new FileSystem.AtomicAction() {

                    public void run() throws IOException {
                        ProjectManager.mutex().writeAccess(new Mutex.Action<Object>() {

                            public Object run() {
                                EditableProperties ep = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                                ep.setProperty("netbeans.user", System.getProperty("netbeans.user"));

                                File f = InstalledFileLocator.getDefault().locate(MODULE_INSTALL_NAME, MODULE_INSTALL_CBN, false);
                                if (f != null) {
                                    ep.setProperty(MODULE_INSTALL_DIR, f.getParentFile().getPath());
                                }

                                helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);

                                // Add project encoding for old projects
                                EditableProperties projectEP = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                                if (projectEP.getProperty(IcanproProjectProperties.SOURCE_ENCODING) == null) {
                                    projectEP.setProperty(IcanproProjectProperties.SOURCE_ENCODING, FileEncodingQuery.getDefaultEncoding().name());
                                }
                                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectEP);

                                try {
                                    ProjectManager.getDefault().saveProject(XsltproProject.this);
                                } catch (IOException e) {
                                    ErrorManager.getDefault().notify(e);
                                }

                                return null;
                            }
                        });
                    }
                });
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
            
            if (IcanproLogicalViewProvider.hasBrokenLinks(helper, refHelper)) {
                BrokenReferencesSupport.showAlert();
            }
            
            checkEncoding();
            
            myProjectsChangeHandler.subscribes();
            helper.removeAntProjectListener(XsltproProject.this);
        }
        
        private void checkEncoding() {
            // TODO m
            // Should we show ErrorManager dialog to inform user in case wrong encoding parameter ?
            String prop = evaluator.getProperty(IcanproProjectProperties.SOURCE_ENCODING);
            if (prop != null) {
                try {
                    Charset c = Charset.forName(prop);
                } catch (IllegalCharsetNameException e) {
                    //Broken property, log & ignore
                    LOG.warning("Illegal charset: " + prop+ " in project: " + // NOI18N
                            getProjectDirectory()); 
                } catch (UnsupportedCharsetException e) {
                    //todo: Needs UI notification like broken references.
                    LOG.warning("Unsupported charset: " + prop+ " in project: " + // NOI18N
                            getProjectDirectory()); 
                }
            }            
        }
        
        protected void projectClosed() {
            // Probably unnecessary, but just in case:
            try {
                ProjectManager.getDefault().saveProject(XsltproProject.this);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
            myProjectsChangeHandler.unsubscribes();
        }
        
    }
    
    /**
     * Exports the main JAR as an official build product for use from other scripts.
     * The type of the artifact will be {@link AntArtifact#TYPE_JAR}.
     *
     * @see org.netbeans.spi.project.ant.AntArtifactProvider
     */
    private final class JbiArtifactProviderImpl implements JbiArtifactProvider {
        
        public AntArtifact[] getBuildArtifacts() {
            return new AntArtifact[] {
                helper.createSimpleAntArtifact(XsltproProject.ARTIFACT_TYPE_JBI_ASA + ":" +
                        helper.getStandardPropertyEvaluator().getProperty(IcanproProjectProperties.JBI_SE_TYPE),
                        IcanproProjectProperties.SE_DEPLOYMENT_JAR,
                        helper.getStandardPropertyEvaluator(), "dist_se", "clean"), // NOI18N
                
                helper.createSimpleAntArtifact(IcanproConstants.ARTIFACT_TYPE_JAR,
                        IcanproProjectProperties.SE_DEPLOYMENT_JAR,
                        helper.getStandardPropertyEvaluator(), "dist_se", "clean"), // NOI18N
                
            };
        }

        public String getJbiServiceAssemblyType() {
            return helper.getStandardPropertyEvaluator().getProperty(IcanproProjectProperties.JBI_SE_TYPE);
        }
    }
    
    /**
     * @see org.netbeans.spi.project.ui.RecommendedTemplates
     * @see org.netbeans.spi.project.ui.PrivilegedTemplates
     */
    private static final class RecommendedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates {
        
        // List of primarily supported templates
        
        private static final String[] TYPES = new String[] {
            "SOA",
            "XML",                  // NOI18N
            "simple-files"          // NOI18N
        };
        
        private static final String[] PRIVILEGED_NAMES = new String[] {
            "Templates/SOA/xslt.service",    // NOI18N
            "Templates/XML/XmlSchema.xsd",    // NOI18N
            "Templates/XML/WSDL.wsdl"    // NOI18N
        };
        
        public String[] getRecommendedTypes() {
            return TYPES;
        }
        
        public String[] getPrivilegedTemplates() {
            return PRIVILEGED_NAMES;
        }
        
    }
    
    public IcanproProjectProperties getProjectProperties() {
        return new IcanproProjectProperties(this, helper, refHelper, XsltproProjectType.PROJECT_CONFIGURATION_NAMESPACE);
    }
}
