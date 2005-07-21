/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seproject;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.java.j2seproject.classpath.ClassPathProviderImpl;
import org.netbeans.modules.java.j2seproject.classpath.J2SEProjectClassPathExtender;
import org.netbeans.modules.java.j2seproject.queries.CompiledSourceForBinaryQuery;
import org.netbeans.modules.java.j2seproject.queries.JavadocForBinaryQueryImpl;
import org.netbeans.modules.java.j2seproject.queries.SourceLevelQueryImpl;
import org.netbeans.modules.java.j2seproject.queries.UnitTestForSourceQueryImpl;
import org.netbeans.modules.java.j2seproject.ui.J2SELogicalViewProvider;
import org.netbeans.modules.java.j2seproject.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Represents one plain J2SE project.
 * @author Jesse Glick, et al.
 */
public final class J2SEProject implements Project, AntProjectListener {
    
    private static final Icon J2SE_PROJECT_ICON = new ImageIcon(Utilities.loadImage("org/netbeans/modules/java/j2seproject/ui/resources/j2seProject.gif")); // NOI18N

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

    J2SEProject(AntProjectHelper helper) throws IOException {
        this.helper = helper;
        eval = createEvaluator();
        aux = helper.createAuxiliaryConfiguration();
        refHelper = new ReferenceHelper(helper, aux, eval);
        genFilesHelper = new GeneratedFilesHelper(helper);
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
        return "J2SEProject[" + getProjectDirectory() + "]"; // NOI18N
    }
    
    private PropertyEvaluator createEvaluator() {
        // XXX might need to use a custom evaluator to handle active platform substitutions... TBD
        // It is currently safe to not use the UpdateHelper for PropertyEvaluator; UH.getProperties() delegates to APH
        return helper.getStandardPropertyEvaluator();
    }
    
    PropertyEvaluator evaluator() {
        return eval;
    }

    ReferenceHelper getReferenceHelper () {
        return this.refHelper;
    }

    public Lookup getLookup() {
        return lookup;
    }
    
    public AntProjectHelper getAntProjectHelper() {
        return helper;
    }

    private Lookup createLookup(AuxiliaryConfiguration aux) {
        SubprojectProvider spp = refHelper.createSubprojectProvider();
        return Lookups.fixed(new Object[] {
            new Info(),
            aux,
            helper.createCacheDirectoryProvider(),
            spp,
            new J2SEActionProvider( this, this.updateHelper ),
            new J2SELogicalViewProvider(this, this.updateHelper, evaluator(), spp, refHelper),
            // new J2SECustomizerProvider(this, this.updateHelper, evaluator(), refHelper),
            new CustomizerProviderImpl(this, this.updateHelper, evaluator(), refHelper, this.genFilesHelper),        
            new ClassPathProviderImpl(this.helper, evaluator(), getSourceRoots(),getTestSourceRoots()), //Does not use APH to get/put properties/cfgdata
            new CompiledSourceForBinaryQuery(this.helper, evaluator(),getSourceRoots(),getTestSourceRoots()), //Does not use APH to get/put properties/cfgdata
            new JavadocForBinaryQueryImpl(this.helper, evaluator()), //Does not use APH to get/put properties/cfgdata
            new AntArtifactProviderImpl(),
            new ProjectXmlSavedHookImpl(),
            new ProjectOpenedHookImpl(),
            new UnitTestForSourceQueryImpl(getSourceRoots(),getTestSourceRoots()),
            new SourceLevelQueryImpl(evaluator()),
            new J2SESources (this.helper, evaluator(), getSourceRoots(), getTestSourceRoots()),
            new J2SESharabilityQuery (this.helper, evaluator(), getSourceRoots(), getTestSourceRoots()), //Does not use APH to get/put properties/cfgdata
            new J2SEFileBuiltQuery (this.helper, evaluator(),getSourceRoots(),getTestSourceRoots()), //Does not use APH to get/put properties/cfgdata
            new RecommendedTemplatesImpl (this.updateHelper),
            new J2SEProjectClassPathExtender(this, this.updateHelper, eval,refHelper),
            this, // never cast an externally obtained Project to J2SEProject - use lookup instead
            new J2SEProjectOperations(this),
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
    /** Store configured project name. * /
    public void setName(final String name) {
        ProjectManager.mutex().writeAccess(new Mutex.Action() {
            public Object run() {
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
                    data.insertBefore(nameEl, / * OK if null * /data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                helper.putPrimaryConfigurationData(data, true);
                return null;
            }
        });
    }
     */




    // Private innerclasses ----------------------------------------------------
    
    private final class Info implements ProjectInformation {
        
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        
        Info() {}
        
        void firePropertyChange(String prop) {
            pcs.firePropertyChange(prop, null, null);
        }
        
        public String getName() {
            return PropertyUtils.getUsablePropertyName(getDisplayName());
        }
        
        public String getDisplayName() {
            return (String) ProjectManager.mutex().readAccess(new Mutex.Action() {
                public Object run() {
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
            ClassPathProviderImpl cpProvider = (ClassPathProviderImpl)lookup.lookup(ClassPathProviderImpl.class);
            GlobalPathRegistry.getDefault().register(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
            GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));

            //register updater of main.class
            //the updater is active only on the opened projects
            mainClassUpdater = new MainClassUpdater (J2SEProject.this, eval, updateHelper,
                    cpProvider.getProjectClassPaths(ClassPath.SOURCE)[0], J2SEProjectProperties.MAIN_CLASS);

            // Make it easier to run headless builds on the same machine at least.
            ProjectManager.mutex().writeAccess(new Mutex.Action() {
                public Object run() {
                    EditableProperties ep = updateHelper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    File buildProperties = new File(System.getProperty("netbeans.user"), "build.properties"); // NOI18N
                    ep.setProperty("user.properties.file", buildProperties.getAbsolutePath()); //NOI18N
                    updateHelper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                    try {
                        ProjectManager.getDefault().saveProject(J2SEProject.this);
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                    return null;
                }
            });
            J2SELogicalViewProvider physicalViewProvider = (J2SELogicalViewProvider)
                J2SEProject.this.getLookup().lookup (J2SELogicalViewProvider.class);
            if (physicalViewProvider != null &&  physicalViewProvider.hasBrokenLinks()) {   
                BrokenReferencesSupport.showAlert();
            }
        }
        
        protected void projectClosed() {
            // Probably unnecessary, but just in case:
            try {
                ProjectManager.getDefault().saveProject(J2SEProject.this);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
            
            // unregister project's classpaths to GlobalPathRegistry
            ClassPathProviderImpl cpProvider = (ClassPathProviderImpl)lookup.lookup(ClassPathProviderImpl.class);
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
            "oasis-XML-catalogs",   // NOI18N
            "XML",                  // NOI18N
            "ant-script",           // NOI18N
            "ant-task",             // NOI18N
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
            "oasis-XML-catalogs",   // NOI18N
            "XML",                  // NOI18N
            "ant-script",           // NOI18N
            "ant-task",             // NOI18N
            "servlet-types",        // NOI18N
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

}
