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
package org.netbeans.modules.iep.project;

import org.netbeans.modules.compapp.projects.base.spi.JbiArtifactProvider;
import org.netbeans.modules.compapp.projects.base.ui.IcanproCustomizerProvider;
import org.netbeans.modules.compapp.projects.base.ui.IcanproLogicalViewProvider;
import org.netbeans.modules.compapp.projects.base.ui.customizer.IcanproProjectProperties;
import org.netbeans.modules.compapp.projects.base.IcanproConstants;
import org.netbeans.modules.iep.project.ui.IepProjectLogicalViewProvider;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.xml.catalogsupport.ProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.compapp.projects.base.queries.IcanproProjectEncodingQueryImpl;
import org.netbeans.modules.iep.project.ui.customizer.IepProjectCustomizerProvider;
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
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;




/**
 * Represents one iep module project
 *
 * @author Bing Lu
 */
public final class IepProject implements Project, AntProjectListener {

    private static final Icon PROJECT_ICON = new ImageIcon(Utilities.loadImage("org/netbeans/modules/iep/project/ui/resources/iepProject.gif")); // NOI18N
    public static final String SOURCES_TYPE_ICANPRO = "BIZPRO";
    public static final String ARTIFACT_TYPE_JBI_ASA = "CAPS.asa";

    public static final String MODULE_INSTALL_NAME = "modules/org-netbeans-modules-iep-project.jar";
    public static final String MODULE_INSTALL_CBN = "org.netbeans.modules.iep.project";
    public static final String MODULE_INSTALL_DIR = "module.install.dir";

    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final ReferenceHelper refHelper;
    private final GeneratedFilesHelper genFilesHelper;
    private final Lookup lookup;

    public IepProject(final AntProjectHelper helper) throws IOException {
        this.helper = helper;
        eval = createEvaluator();
        AuxiliaryConfiguration aux = helper.createAuxiliaryConfiguration();
        refHelper = new ReferenceHelper(helper, aux, helper.getStandardPropertyEvaluator());
        genFilesHelper = new GeneratedFilesHelper(helper);
        lookup = createLookup(aux);
        helper.addAntProjectListener(this);
    }

    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }

    public String toString() {
        return "IepproProject[" + getProjectDirectory() + "]"; // NOI18N
    }

    public ReferenceHelper getReferenceHelper() {
        return this.refHelper;
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

    private Lookup createLookup(AuxiliaryConfiguration aux) {
        SubprojectProvider spp = refHelper.createSubprojectProvider();
        FileBuiltQueryImplementation fileBuilt = helper.createGlobFileBuiltQuery(helper.getStandardPropertyEvaluator(), 
            new String[] {"${src.dir}/*.java"}, // NOI18N
            new String[] {"${build.classes.dir}/*.class"} // NOI18N
        );
        final SourcesHelper sourcesHelper = new SourcesHelper(helper, evaluator());
        String webModuleLabel = org.openide.util.NbBundle.getMessage(IcanproCustomizerProvider.class, "LBL_Node_EJBModule"); //NOI18N
        String srcJavaLabel = org.openide.util.NbBundle.getMessage(IepProjectLogicalViewProvider.class, "LBL_Node_Sources"); //NOI18N

        sourcesHelper.addPrincipalSourceRoot("${"+IcanproProjectProperties.SOURCE_ROOT+"}", webModuleLabel, /*XXX*/null, null);
        sourcesHelper.addPrincipalSourceRoot("${"+IcanproProjectProperties.SRC_DIR+"}", srcJavaLabel, /*XXX*/null, null);

        sourcesHelper.addTypedSourceRoot("${"+IcanproProjectProperties.SRC_DIR+"}", SOURCES_TYPE_ICANPRO, srcJavaLabel, /*XXX*/null, null);
        sourcesHelper.addTypedSourceRoot("${"+IcanproProjectProperties.SRC_DIR+"}", 
                org.netbeans.modules.xml.catalogsupport.ProjectConstants.SOURCES_TYPE_XML,
                srcJavaLabel, /*XXX*/null, null);
        ProjectManager.mutex().postWriteRequest(new Runnable() {
            public void run() {
                sourcesHelper.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
            }
        });
        return Lookups.fixed(new Object[] {
            new Info(),
            aux,
            helper.createCacheDirectoryProvider(),
            helper,
            spp,
            new IepProjectActionProvider( this, helper, refHelper ),
            new IepProjectLogicalViewProvider(this, helper, evaluator(), refHelper),
            new IcanproCustomizerProvider( this, helper, refHelper ),
            //new IepProjectCustomizerProvider(this),
            new JbiArtifactProviderImpl(),
            new ProjectXmlSavedHookImpl(),
            new ProjectOpenedHookImpl(),
            new IepProjectOperations(this),
            fileBuilt,
            new RecommendedTemplatesImpl(),
            refHelper,
            new IcanproProjectEncodingQueryImpl(evaluator()),
            sourcesHelper.createSources(),
            sourcesHelper.createSources(),
            helper.createSharabilityQuery(evaluator(),
                new String[] {"${"+IcanproProjectProperties.SOURCE_ROOT+"}"},
                new String[] {
                    "${"+IcanproProjectProperties.BUILD_DIR+"}",
                    "${"+IcanproProjectProperties.DIST_DIR+"}"}
                )
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
        // currently ignored
        //TODO: should not be ignored!
    }

    String getBuildXmlName() {
        String storedName = helper.getStandardPropertyEvaluator().getProperty(IcanproProjectProperties.BUILD_FILE);
        return storedName == null ? GeneratedFilesHelper.BUILD_XML_PATH : storedName;
    }

    // Package private methods -------------------------------------------------

    FileObject getSourceDirectory() {
        String srcDir = helper.getStandardPropertyEvaluator().getProperty("src.dir"); // NOI18N
        return helper.resolveFileObject(srcDir);
    }

    /** Return configured project name. */
    public String getName() {
        return (String) ProjectManager.mutex().readAccess(new Mutex.Action() {
            public Object run() {
                Element data = helper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(IepProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
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

    public AntProjectHelper getAntProjectHelper() {
        return helper;
    }

    /** Store configured project name. */
    public void setName(final String name) {
        ProjectManager.mutex().writeAccess(new Mutex.Action() {
            public Object run() {
                Element data = helper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(IepProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(IepProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
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
            return IepProject.this.getName();
        }

        public String getDisplayName() {
            return IepProject.this.getName();
        }

        public Icon getIcon() {
            return PROJECT_ICON;
        }

        public Project getProject() {
            return IepProject.this;
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
                IepProject.class.getResource("resources/build-impl.xsl"),
                false);
            genFilesHelper.refreshBuildScript(
                getBuildXmlName(),
                IepProject.class.getResource("resources/build.xsl"),
                false);
        }

    }

    private final class ProjectOpenedHookImpl extends ProjectOpenedHook {

        ProjectOpenedHookImpl() {}

        protected void projectOpened() {
            try {
                // Check up on build scripts.
                genFilesHelper.refreshBuildScript(
                GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                IepProject.class.getResource("resources/build-impl.xsl"),
                true);
                genFilesHelper.refreshBuildScript(
                getBuildXmlName(),
                IepProject.class.getResource("resources/build.xsl"),
                true);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }

            // Make it easier to run headless builds on the same machine at least.
            ProjectManager.mutex().writeAccess(new Mutex.Action() {
                public Object run() {
                    EditableProperties ep = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    ep.setProperty("netbeans.user", System.getProperty("netbeans.user"));
                    ep.setProperty("netbeans.buildnumber", System.getProperty("netbeans.buildnumber"));
                    
                    
                    File f = InstalledFileLocator.getDefault().locate(MODULE_INSTALL_NAME, MODULE_INSTALL_CBN, false);
                    if (f != null) {
                        ep.setProperty(MODULE_INSTALL_DIR, f.getParentFile().getPath());
                    }

                    helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                    
                    
                    // Add project encoding for old projects
                  EditableProperties projectEP = helper.getProperties(
                                  AntProjectHelper.PROJECT_PROPERTIES_PATH);
                  if (projectEP.getProperty(IcanproProjectProperties.SOURCE_ENCODING) == null) {
                      projectEP.setProperty(IcanproProjectProperties.SOURCE_ENCODING,
                              // FIXME: maybe we should use Charset.defaultCharset() instead?
                              // See comments in IcanproProjectEncodingQueryImpl.java
                              FileEncodingQuery.getDefaultEncoding().name());
                  } 
                  
                  //Add allow.build.with.error for old projects
                   if (projectEP.getProperty(org.netbeans.modules.iep.project.ProjectConstants.ALLOW_BUILD_WITH_ERROR) == null) {
                      projectEP.setProperty(org.netbeans.modules.iep.project.ProjectConstants.ALLOW_BUILD_WITH_ERROR,
                              "false");
                  }
                  
                  //Add always.generate.abstract.wsdl for old projects
                   if (projectEP.getProperty(org.netbeans.modules.iep.project.ProjectConstants.ALWAYS_GENERATE_ABSTRACT_WSDL) == null) {
                      projectEP.setProperty(org.netbeans.modules.iep.project.ProjectConstants.ALWAYS_GENERATE_ABSTRACT_WSDL,
                              "false");
                  }
                  
                  helper.putProperties(
                          AntProjectHelper.PROJECT_PROPERTIES_PATH, projectEP);
                  
                    try {
                        ProjectManager.getDefault().saveProject(IepProject.this);
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                    return null;
                }
            });
            if (IcanproLogicalViewProvider.hasBrokenLinks(helper, refHelper)) {
                BrokenReferencesSupport.showAlert();
            }
        }

        protected void projectClosed() {
            // Probably unnecessary, but just in case:
            try {
                ProjectManager.getDefault().saveProject(IepProject.this);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }

    }

    /**
     * Exports the main JAR as an official build product for use from other scripts.
     * The type of the artifact will be {@link AntArtifact#TYPE_JAR}.
     */
    private final class JbiArtifactProviderImpl implements JbiArtifactProvider {

        public AntArtifact[] getBuildArtifacts() {
            return new AntArtifact[] {
                helper.createSimpleAntArtifact(IepProject.ARTIFACT_TYPE_JBI_ASA + ":" +
                        helper.getStandardPropertyEvaluator().getProperty(IcanproProjectProperties.JBI_SETYPE_PREFIX),
                        IcanproProjectProperties.SE_DEPLOYMENT_JAR,
                        helper.getStandardPropertyEvaluator(), "dist_se", "clean"), // NOI18N
                helper.createSimpleAntArtifact(IcanproConstants.ARTIFACT_TYPE_JAR,
                        IcanproProjectProperties.SE_DEPLOYMENT_JAR,
                        helper.getStandardPropertyEvaluator(), "dist_se", "clean"), // NOI18N
            };
        }

        public String getJbiServiceAssemblyType() {
            return helper.getStandardPropertyEvaluator().getProperty(IcanproProjectProperties.JBI_SETYPE_PREFIX);
        }
    }

    private static final class RecommendedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates {

        // List of primarily supported templates

        private static final String[] TYPES = new String[] {
            "SOA_IEP",              // NOI18N
            "XML",                  // NOI18N
            "simple-files"          // NOI18N
        };

        private static final String[] PRIVILEGED_NAMES = new String[] {
            "Templates/SOA_IEP/EventProcessor.iep", // NOI18N
            "Templates/XML/WSDL.wsdl",            // NOI18N
        };

        public String[] getRecommendedTypes() {
            return TYPES;
        }

        public String[] getPrivilegedTemplates() {
            return PRIVILEGED_NAMES;
        }

    }
}
