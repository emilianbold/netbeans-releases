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
import org.netbeans.modules.java.j2seproject.ui.J2SECustomizerProvider;
import org.netbeans.modules.java.j2seproject.ui.J2SEPhysicalViewProvider;
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
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.Comment;
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
    private MainClassUpdater mainClassUpdater;
    private SourceRoots sourceRoots;
    private SourceRoots testRoots;

    J2SEProject(AntProjectHelper helper) throws IOException {
        this.helper = helper;
        eval = createEvaluator();
        aux = helper.createAuxiliaryConfiguration();
        refHelper = new ReferenceHelper(helper, aux, eval);
        genFilesHelper = new GeneratedFilesHelper(helper);
        lookup = createLookup(aux);
        helper.addAntProjectListener(this);
        ProjectManager.mutex().postWriteRequest(
                new Runnable () {
                    public void run() {
                        try {
                            updateProjectXML ();
                        } catch (IOException ioe) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
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
        return "J2SEProject[" + getProjectDirectory() + "]"; // NOI18N
    }
    
    private PropertyEvaluator createEvaluator() {
        // XXX might need to use a custom evaluator to handle active platform substitutions... TBD
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

    private Lookup createLookup(AuxiliaryConfiguration aux) {
        SubprojectProvider spp = refHelper.createSubprojectProvider();
        return Lookups.fixed(new Object[] {
            new Info(),
            aux,
            helper.createCacheDirectoryProvider(),
            spp,
            new J2SEActionProvider( this, helper ),
            new J2SEPhysicalViewProvider(this, helper, evaluator(), spp, refHelper),
            new J2SECustomizerProvider(this, helper, evaluator(), refHelper),
            new ClassPathProviderImpl(helper, evaluator(), getSourceRoots(),getTestSourceRoots()),
            new CompiledSourceForBinaryQuery(helper, evaluator(),getSourceRoots(),getTestSourceRoots()),
            new JavadocForBinaryQueryImpl(helper, evaluator()),
            new AntArtifactProviderImpl(),
            new ProjectXmlSavedHookImpl(),
            new ProjectOpenedHookImpl(),
            new UnitTestForSourceQueryImpl(getSourceRoots(),getTestSourceRoots()),
            new SourceLevelQueryImpl(helper, evaluator()),
            new J2SESources (helper, evaluator(), getSourceRoots(), getTestSourceRoots()),
            helper.createSharabilityQuery(evaluator(), new String[] {
                "${" + J2SEProjectProperties.SRC_DIR + "}", // NOI18N
                "${" + J2SEProjectProperties.TEST_SRC_DIR + "}", // NOI18N
            }, new String[] {
                "${" + J2SEProjectProperties.DIST_DIR + "}", // NOI18N
                "${" + J2SEProjectProperties.BUILD_DIR + "}", // NOI18N
            }),
            new J2SEFileBuiltQuery (helper, evaluator(),getSourceRoots(),getTestSourceRoots()),
            new RecommendedTemplatesImpl (helper),
            new J2SEProjectClassPathExtender(this, helper, eval,refHelper),
            this, // never cast an externally obtained Project to J2SEProject - use lookup instead
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
        if (this.sourceRoots == null) {
            this.sourceRoots = new SourceRoots(this.helper, evaluator(),"source-roots"); //NOI18N
        }
        return this.sourceRoots;
    }
    
    synchronized SourceRoots getTestSourceRoots() {
        if (this.testRoots == null) {
            this.testRoots = new SourceRoots(this.helper, evaluator(),"test-roots"); //NOI18N
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

    private void updateProjectXML () throws IOException {
        Element element = aux.getConfigurationFragment("data","http://www.netbeans.org/ns/j2se-project/1",true);    //NOI18N
        if (element != null) {
            Document doc = element.getOwnerDocument();
            Element newRoot = doc.createElementNS (J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE,"data"); //NOI18N
            copyDocument (doc, element, newRoot);
            Element sourceRoots = doc.createElementNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");  //NOI18N
            Element root = doc.createElementNS (J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
            root.setAttributeNS (J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE,"id","src.dir");   //NOI18N
            sourceRoots.appendChild(root);
            newRoot.appendChild (sourceRoots);
            Element testRoots = doc.createElementNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE,"test-roots");  //NOI18N
            root = doc.createElementNS (J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
            root.setAttributeNS (J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE,"id","test.src.dir");   //NOI18N
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
                    newNode = doc.createElementNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE,oldElement.getTagName());
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
                    Element data = helper.getPrimaryConfigurationData(true);
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
    
    private final class ProjectOpenedHookImpl extends ProjectOpenedHook {
        
        ProjectOpenedHookImpl() {}
        
        protected void projectOpened() {
            // Check up on build scripts.
            try {
                genFilesHelper.refreshBuildScript(
                    GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                    J2SEProject.class.getResource("resources/build-impl.xsl"),
                    true);
                genFilesHelper.refreshBuildScript(
                    GeneratedFilesHelper.BUILD_XML_PATH,
                    J2SEProject.class.getResource("resources/build.xsl"),
                    true);
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
            mainClassUpdater = new MainClassUpdater (J2SEProject.this, eval, helper,
                    cpProvider.getProjectClassPaths(ClassPath.SOURCE)[0], J2SEProjectProperties.MAIN_CLASS);

            // Make it easier to run headless builds on the same machine at least.
            ProjectManager.mutex().writeAccess(new Mutex.Action() {
                public Object run() {
                    EditableProperties ep = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    File buildProperties = new File(System.getProperty("netbeans.user"), "build.properties"); // NOI18N
                    ep.setProperty("user.properties.file", buildProperties.getAbsolutePath()); //NOI18N
                    helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                    try {
                        ProjectManager.getDefault().saveProject(J2SEProject.this);
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                    return null;
                }
            });
            if (J2SEPhysicalViewProvider.hasBrokenLinks(helper, refHelper)) {
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
        RecommendedTemplatesImpl (AntProjectHelper helper) {
            this.helper = helper;
        }
        
        private AntProjectHelper helper;
        
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
