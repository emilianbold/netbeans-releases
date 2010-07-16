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
package org.netbeans.modules.edm.project;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.File;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.compapp.projects.base.ui.IcanproCustomizerProvider;
import org.netbeans.modules.compapp.projects.base.ui.IcanproLogicalViewProvider;
import org.netbeans.modules.compapp.projects.base.ui.customizer.IcanproProjectProperties;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.project.SubprojectProvider;
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
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.netbeans.modules.compapp.projects.base.spi.JbiArtifactProvider;
import org.netbeans.modules.edm.project.ui.EdmproLogicalViewProvider;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.modules.InstalledFileLocator;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Represents one ejb module project
 * @author Chris Webster
 */
public final class EdmproProject implements Project, AntProjectListener {

    private static final Icon PROJECT_ICON = new ImageIcon(Utilities.loadImage("org/netbeans/modules/edm/project/ui/resources/edmproProjectIcon.gif")); // NOI18N
    public static final String SOURCES_TYPE_ICANPRO = "BIZPRO";
    public static final String MODULE_INSTALL_NAME = "modules/org-netbeans-modules-edm-project.jar";
    public static final String MODULE_INSTALL_CBN = "org.netbeans.modules.edm.project";
    public static final String MODULE_INSTALL_DIR = "module.install.dir";
    
    public static final String XML_MODULE_INSTALL_NAME = "modules/org-netbeans-modules-xml-wsdl-extensions.jar"; // NOI18N
    public static final String IDE_MODULE_INSTALL_NAME = "modules/org-netbeans-modules-xml-wsdl-model.jar"; // NOI18N
    public static final String SOA_MODULE_INSTALL_NAME = "modules/org-netbeans-modules-compapp-projects-jbi.jar"; // NOI18N
    public static final String PLATFORM_MODULE_NAME = "modules/org-openide-loaders.jar";
    public static final String PLATFORM_CORE_NAME = "core/org-openide-filesystems.jar";
    public static final String PLATFORM_LIB_NAME = "lib/org-openide-util.jar";

    public static final String XML_MODULE_INSTALL_CBN = "org.netbeans.modules.xml.wsdl.model.extensions"; // NOI18N
    public static final String IDE_MODULE_INSTALL_CBN = "org.netbeans.modules.xml.wsdl.model"; // NOI18N
    public static final String SOA_MODULE_INSTALL_CBN = "org.netbeans.modules.compapp.projects.jbi"; // NOI18N
    public static final String PLATFORM_MODULE_CBN = "org.openide.loaders";
    public static final String PLATFORM_CORE_CBN = "org.openide.filesystems";
    public static final String PLATFORM_LIB_CBN = "org.openide.util";

    public static final String XML_MODULE_INSTALL_DIR = "xml.module.install.dir"; // NOI18N
    public static final String IDE_MODULE_INSTALL_DIR = "ide.module.install.dir"; // NOI18N
    public static final String SOA_MODULE_INSTALL_DIR = "soa.module.install.dir"; // NOI18N
    public static final String PLATFORM_MODULE_DIR = "platform.module.dir";
    public static final String PLATFORM_CORE_DIR = "platform.core.dir";
    public static final String PLATFORM_LIB_DIR = "platform.lib.dir";


    public static final String COMMAND_GENWSDL = "gen-wsdl";
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final ReferenceHelper refHelper;
    private final GeneratedFilesHelper genFilesHelper;
    private final Lookup lookup;

    public EdmproProject(final AntProjectHelper helper) throws IOException {
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

    @Override
    public String toString() {
        return "EdmproProject[" + getProjectDirectory() + "]"; // NOI18N
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
                new String[]{"${src.dir}/*.java"}, // NOI18N
                new String[]{"${build.classes.dir}/*.class"} // NOI18N
                );
        final SourcesHelper sourcesHelper = new SourcesHelper(helper, evaluator());
        String webModuleLabel = org.openide.util.NbBundle.getMessage(IcanproCustomizerProvider.class, "LBL_Node_EJBModule"); //NOI18N
        String srcJavaLabel = org.openide.util.NbBundle.getMessage(IcanproCustomizerProvider.class, "LBL_Node_Sources"); //NOI18N

        sourcesHelper.addPrincipalSourceRoot("${" + IcanproProjectProperties.SOURCE_ROOT + "}", webModuleLabel, /*XXX*/ null, null);
        sourcesHelper.addPrincipalSourceRoot("${" + IcanproProjectProperties.SRC_DIR + "}", srcJavaLabel, /*XXX*/ null, null);

        sourcesHelper.addTypedSourceRoot("${" + IcanproProjectProperties.SRC_DIR + "}", SOURCES_TYPE_ICANPRO, srcJavaLabel, /*XXX*/ null, null);
        sourcesHelper.addTypedSourceRoot("${" + IcanproProjectProperties.SRC_DIR + "}", JavaProjectConstants.SOURCES_TYPE_JAVA, srcJavaLabel, /*XXX*/ null, null);
        ProjectManager.mutex().postWriteRequest(new Runnable() {

            public void run() {
                sourcesHelper.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
            }
        });
        return Lookups.fixed(new Object[]{
                    new Info(),
                    aux,
                    helper.createCacheDirectoryProvider(),
                    helper,
                    spp, new EdmproActionProvider(this, helper, refHelper), new EdmproLogicalViewProvider(this, helper, evaluator(), spp, refHelper),
                    new IcanproCustomizerProvider(this, helper, refHelper),
                    new JbiArtifactProviderImpl(),
                    new ProjectXmlSavedHookImpl(),
                    new ProjectOpenedHookImpl(),
                    new EdmProjectOperations(this),
                    fileBuilt,
                    new RecommendedTemplatesImpl(),
                    refHelper,
                    sourcesHelper.createSources(),
                    helper.createSharabilityQuery(evaluator(),
                    new String[]{"${" + IcanproProjectProperties.SOURCE_ROOT + "}"},
                    new String[]{
                        "${" + IcanproProjectProperties.BUILD_DIR + "}",
                        "${" + IcanproProjectProperties.DIST_DIR + "}"
                    })
                });
    }

    public void configurationXmlChanged(AntProjectEvent ev) {
        if (ev.getPath().equals(AntProjectHelper.PROJECT_XML_PATH)) {
            // Could be various kinds of changes, but name & displayName might have changed.
            Info info = (Info) getLookup().lookup(ProjectInformation.class);
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
                NodeList nl = data.getElementsByTagNameNS(EdmproProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
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

    // Private innerclasses ----------------------------------------------------
    private final class Info implements ProjectInformation {

        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

        Info() {
        }

        void firePropertyChange(String prop) {
            pcs.firePropertyChange(prop, null, null);
        }

        public String getName() {
            return EdmproProject.this.getName();
        }

        public String getDisplayName() {
            return EdmproProject.this.getName();
        }

        public Icon getIcon() {
            return PROJECT_ICON;
        }

        public Project getProject() {
            return EdmproProject.this;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }
    }

    private final class ProjectXmlSavedHookImpl extends ProjectXmlSavedHook {

        ProjectXmlSavedHookImpl() {
        }

        protected void projectXmlSaved() throws IOException {
            genFilesHelper.refreshBuildScript(
                    GeneratedFilesHelper.BUILD_IMPL_XML_PATH, EdmproProject.class.getResource("resources/build-impl.xsl"),
                    false);
            genFilesHelper.refreshBuildScript(
                    getBuildXmlName(), EdmproProject.class.getResource("resources/build.xsl"),
                    false);
        }
    }

    private final class ProjectOpenedHookImpl extends ProjectOpenedHook {

        ProjectOpenedHookImpl() {
        }

        protected void projectOpened() {
            try {
                // Check up on build scripts.
                genFilesHelper.refreshBuildScript(
                        GeneratedFilesHelper.BUILD_IMPL_XML_PATH, EdmproProject.class.getResource("resources/build-impl.xsl"),
                        true);
                genFilesHelper.refreshBuildScript(
                        getBuildXmlName(), EdmproProject.class.getResource("resources/build.xsl"),
                        true);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }

            // Make it easier to run headless builds on the same machine at least.
            ProjectManager.mutex().writeAccess(new Mutex.Action() {

                public Object run() {
                    EditableProperties ep = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    ep.setProperty("netbeans.user", System.getProperty("netbeans.user"));
                    //ep.setProperty("netbeans.home", System.getProperty("netbeans.home"));
                    File f = InstalledFileLocator.getDefault().locate(MODULE_INSTALL_NAME, MODULE_INSTALL_CBN, false);
                    if (f != null) {
                        ep.setProperty(MODULE_INSTALL_DIR, f.getParentFile().getPath());
                    }

                    f = InstalledFileLocator.getDefault().locate(
                            SOA_MODULE_INSTALL_NAME, SOA_MODULE_INSTALL_CBN, false);
                    if (f != null) {
                        ep.setProperty(SOA_MODULE_INSTALL_DIR, f.getParentFile().getPath());
                    }

                    f = InstalledFileLocator.getDefault().locate(
                            XML_MODULE_INSTALL_NAME, XML_MODULE_INSTALL_CBN, false);
                    if (f != null) {
                        ep.setProperty(XML_MODULE_INSTALL_DIR, f.getParentFile().getPath());
                    }

                    f = InstalledFileLocator.getDefault().locate(
                            IDE_MODULE_INSTALL_NAME, IDE_MODULE_INSTALL_CBN, false);
                    if (f != null) {
                        ep.setProperty(IDE_MODULE_INSTALL_DIR, f.getParentFile().getPath());
                    }

                    f = InstalledFileLocator.getDefault().locate(
                            PLATFORM_MODULE_NAME, PLATFORM_MODULE_CBN, false);
                    if (f != null) {
                        ep.setProperty(PLATFORM_MODULE_DIR, f.getParentFile().getPath());
                    }

                    f = InstalledFileLocator.getDefault().locate(
                            PLATFORM_CORE_NAME, PLATFORM_CORE_CBN, false);
                    if (f != null) {
                        ep.setProperty(PLATFORM_CORE_DIR, f.getParentFile().getPath());
                    }

                    f = InstalledFileLocator.getDefault().locate(
                            PLATFORM_LIB_NAME, PLATFORM_LIB_CBN, false);
                    if (f != null) {
                        ep.setProperty(PLATFORM_LIB_DIR, f.getParentFile().getPath());
                    }

                    helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                    try {
                        ProjectManager.getDefault().saveProject(EdmproProject.this);
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
                ProjectManager.getDefault().saveProject(EdmproProject.this);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }

    /**
     * Exports the main JAR as an official build product for use from other scripts.
     * The type of the artifact will be {@link AntArtifact}.
     */
    private final class JbiArtifactProviderImpl implements JbiArtifactProvider {

        public AntArtifact[] getBuildArtifacts() {
            return new AntArtifact[]{
                        helper.createSimpleAntArtifact(JbiArtifactProvider.ARTIFACT_TYPE_JBI_ASA + ":" +
                        helper.getStandardPropertyEvaluator().getProperty(IcanproProjectProperties.JBI_SETYPE_PREFIX),
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
        private static final String[] TYPES = new String[]{
            "ESB", // NOI18N
            "XML", // NOI18N
            "simple-files"          // NOI18N
        };
        private static final String[] PRIVILEGED_NAMES = new String[]{
            "Templates/ESB/untitled.edm",
        };

        public String[] getRecommendedTypes() {
            return TYPES;
        }

        public String[] getPrivilegedTemplates() {
            return PRIVILEGED_NAMES;
        }
    }

    public AntProjectHelper getAntProjectHelper() {
        return helper;
    }

    public ReferenceHelper getReferenceHelper() {
        return this.refHelper;
    }

    public void setName(final String name) {
        ProjectManager.mutex().writeAccess(new Mutex.Action() {

            public Object run() {
                Element data = helper.getPrimaryConfigurationData(true);
                NodeList nl = data.getElementsByTagNameNS(EdmproProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(EdmproProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                    data.insertBefore(nameEl, data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                helper.putPrimaryConfigurationData(data, true);
                return null;
            }
        });
    }
}
