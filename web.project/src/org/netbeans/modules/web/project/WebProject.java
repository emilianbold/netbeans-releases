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

package org.netbeans.modules.web.project;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.*;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.web.project.classpath.ClassPathProviderImpl;
import org.netbeans.modules.web.project.queries.CompiledSourceForBinaryQuery;
import org.netbeans.modules.web.project.ui.WebCustomizerProvider;
import org.netbeans.modules.web.project.ui.WebPhysicalViewProvider;
import org.netbeans.modules.web.project.ui.customizer.VisualClassPathItem;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.netbeans.modules.web.api.webmodule.SourcesGroupTypes;
import org.netbeans.modules.web.project.ui.BrokenReferencesAlertPanel;
import org.netbeans.modules.web.project.ui.FoldersListSettings;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 * Represents one plain Web project.
 * @author Jesse Glick, et al., Pavel Buzek
 */
final class WebProject implements Project, AntProjectListener, FileChangeListener {
    
    private static final Icon WEB_PROJECT_ICON = new ImageIcon(Utilities.loadImage("org/netbeans/modules/web/project/ui/resources/webProjectIcon.gif")); // NOI18N
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final ReferenceHelper refHelper;
    private final GeneratedFilesHelper genFilesHelper;
    private final Lookup lookup;
    private final ProjectWebModule webModule;
    private FileObject libFolder = null;
    
    WebProject(final AntProjectHelper helper) throws IOException {
        this.helper = helper;
        eval = createEvaluator();
        AuxiliaryConfiguration aux = helper.createAuxiliaryConfiguration();
        refHelper = new ReferenceHelper(helper, aux, helper.getStandardPropertyEvaluator ());
        genFilesHelper = new GeneratedFilesHelper(helper);
        webModule = new ProjectWebModule (this, helper);
        lookup = createLookup(aux);
        helper.addAntProjectListener(this);
    }

    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }

    public String toString() {
        return "WebProject[" + getProjectDirectory() + "]"; // NOI18N
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
        FileBuiltQueryImplementation fileBuilt = helper.createGlobFileBuiltQuery(helper.getStandardPropertyEvaluator (), new String[] {
            "${src.dir}/*.java" // NOI18N
        }, new String[] {
            "${build.classes.dir}/*.class" // NOI18N
        });
        final SourcesHelper sourcesHelper = new SourcesHelper(helper, evaluator());
        String webModuleLabel = org.openide.util.NbBundle.getMessage(WebCustomizerProvider.class, "LBL_Node_WebModule"); //NOI18N
        String webPagesLabel = org.openide.util.NbBundle.getMessage(WebCustomizerProvider.class, "LBL_Node_DocBase"); //NOI18N
        String srcJavaLabel = org.openide.util.NbBundle.getMessage(WebCustomizerProvider.class, "LBL_Node_Sources"); //NOI18N
        
        sourcesHelper.addPrincipalSourceRoot("${"+WebProjectProperties.SOURCE_ROOT+"}", webModuleLabel, /*XXX*/null, null);
        sourcesHelper.addPrincipalSourceRoot("${"+WebProjectProperties.SRC_DIR+"}", srcJavaLabel, /*XXX*/null, null);
        sourcesHelper.addPrincipalSourceRoot("${"+WebProjectProperties.WEB_DOCBASE_DIR+"}", webPagesLabel, /*XXX*/null, null);
        
        sourcesHelper.addTypedSourceRoot("${"+WebProjectProperties.SRC_DIR+"}", JavaProjectConstants.SOURCES_TYPE_JAVA, srcJavaLabel, /*XXX*/null, null);
        sourcesHelper.addTypedSourceRoot("${"+WebProjectProperties.WEB_DOCBASE_DIR+"}", SourcesGroupTypes.TYPE_DOC_ROOT, webPagesLabel, /*XXX*/null, null);
        sourcesHelper.addTypedSourceRoot("${"+WebProjectProperties.WEB_DOCBASE_DIR+"}/WEB-INF", SourcesGroupTypes.TYPE_WEB_INF, /*XXX I18N*/ "WEB-INF", /*XXX*/null, null);
        ProjectManager.mutex().postWriteRequest(new Runnable() {
            public void run() {
                sourcesHelper.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
            }
        });
        return Lookups.fixed(new Object[] {
            new Info(),
            aux,
            helper.createCacheDirectoryProvider(),
            spp,
            webModule,
            new WebActionProvider( this, helper, refHelper ),
            new WebPhysicalViewProvider(this, helper, evaluator (), spp, refHelper),
            new WebCustomizerProvider( this, helper, refHelper ),
            new ClassPathProviderImpl(helper),
            new CompiledSourceForBinaryQuery(helper),
            new AntArtifactProviderImpl(),
            new ProjectXmlSavedHookImpl(),
            new ProjectOpenedHookImpl(),
            fileBuilt,
            sourcesHelper.createSources()
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
    
    String getBuildXmlName () {
        String storedName = helper.getStandardPropertyEvaluator ().getProperty (WebProjectProperties.BUILD_FILE);
        return storedName == null ? GeneratedFilesHelper.BUILD_XML_PATH : storedName;
    }
    
    // Package private methods -------------------------------------------------
    
    ProjectWebModule getWebModule () {
        return webModule;
    }
    
    FileObject getSourceDirectory() {
        String srcDir = helper.getStandardPropertyEvaluator ().getProperty ("src.dir"); // NOI18N
        return helper.resolveFileObject(srcDir);
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
    
    public void fileFolderCreated (org.openide.filesystems.FileEvent fe) {
    }
    
    public void fileRenamed (org.openide.filesystems.FileRenameEvent fe) {
        FileObject fo = fe.getFile ();
        checkLibraryFolder (fo);
    }
    
    private void checkLibraryFolder (FileObject fo) {
        if (fo.getParent ().equals (libFolder)) {
            WebProjectProperties wpp = new WebProjectProperties (this, helper, refHelper);
            List cpItems = (List) wpp.get (WebProjectProperties.JAVAC_CLASSPATH);
            if (addLibrary (cpItems, fo)) {
                wpp.put (WebProjectProperties.JAVAC_CLASSPATH, cpItems);
                wpp.store ();
                try {
                    ProjectManager.getDefault ().saveProject (this);
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
        }
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
                        synchronized (WebProject.class) {
                            brokenAlertLastTime = System.currentTimeMillis();
                            brokenAlertShown = false;
                        }
                    }
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
            return helper.getName();
        }
        
        public String getDisplayName() {
            return helper.getDisplayName();
        }
        
        public Icon getIcon() {
            return WEB_PROJECT_ICON;
        }
        
        public Project getProject() {
            return WebProject.this;
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
                WebProject.class.getResource("resources/build-impl.xsl"),
                false);
            genFilesHelper.refreshBuildScript(
                getBuildXmlName (),
                WebProject.class.getResource("resources/build.xsl"),
                false);
        }
        
    }
    
    private boolean addLibrary (List cpItems, FileObject lib) {
        boolean needsAdding = true;
        for (Iterator vcpsIter = cpItems.iterator (); vcpsIter.hasNext ();) {
            VisualClassPathItem vcpi = (VisualClassPathItem) vcpsIter.next ();

            if (vcpi.getType () != VisualClassPathItem.TYPE_JAR) {
                continue;
            }
            FileObject fo = FileUtil.toFileObject (new File(helper.getStandardPropertyEvaluator ().evaluate (vcpi.getEvaluated ())));
            if (lib.equals (fo)) {
                needsAdding = false;
                break;
            }
        }
        if (needsAdding) {
            String file = "${"+WebProjectProperties.LIBRARIES_DIR+"}/"+lib.getNameExt ();
            String eval = helper.getStandardPropertyEvaluator ().evaluate (file);
            VisualClassPathItem cpItem = new VisualClassPathItem( file, VisualClassPathItem.TYPE_JAR, file, eval, VisualClassPathItem.PATH_IN_WAR_LIB);
            cpItems.add (cpItem);
        }
        return needsAdding;
    }
    
    private final class ProjectOpenedHookImpl extends ProjectOpenedHook {
        
        ProjectOpenedHookImpl() {}
        
        protected void projectOpened() {
            try {
                //Check libraries and add them to classpath automatically
                String libFolderName = helper.getStandardPropertyEvaluator ().getProperty (WebProjectProperties.LIBRARIES_DIR);
                WebProjectProperties wpp = new WebProjectProperties (WebProject.this, helper, refHelper);
                if (libFolderName != null && new File (libFolderName).isDirectory ()) {
                    List cpItems = (List) wpp.get (WebProjectProperties.JAVAC_CLASSPATH);
                    FileObject libFolder = FileUtil.toFileObject (new File (libFolderName));
                    FileObject libs [] = libFolder.getChildren ();
                    boolean anyChanged = false;
                    for (int i = 0; i < libs.length; i++) {
                        anyChanged = addLibrary (cpItems, libs [i]) || anyChanged;
                    }
                    if (anyChanged) {
                        wpp.put (WebProjectProperties.JAVAC_CLASSPATH, cpItems);
                        wpp.store ();
                        ProjectManager.getDefault ().saveProject (WebProject.this);
                    }
                    libFolder.addFileChangeListener (WebProject.this);
                }
                // Check up on build scripts.
                genFilesHelper.refreshBuildScript(
                    GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                    WebProject.class.getResource("resources/build-impl.xsl"),
                    true);
                genFilesHelper.refreshBuildScript(
                    getBuildXmlName (),
                    WebProject.class.getResource("resources/build.xsl"),
                    true);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            //check the config context path
            String ctxRoot = webModule.getContextPath ();
            if (ctxRoot == null || ctxRoot.equals ("")) {
                String sysName = "/" + getProjectDirectory ().getName (); //NOI18N
                sysName = sysName.replace (' ', '_'); //NOI18N
                webModule.setContextPath (sysName);
            }
            
            // register project's classpaths to GlobalPathRegistry
            ClassPathProviderImpl cpProvider = (ClassPathProviderImpl)lookup.lookup(ClassPathProviderImpl.class);
            GlobalPathRegistry.getDefault().register(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
            GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));
            
            // Make it easier to run headless builds on the same machine at least.
            ProjectManager.mutex().writeAccess(new Mutex.Action() {
                public Object run() {
                    EditableProperties ep = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    ep.setProperty("netbeans.user", System.getProperty("netbeans.user"));
                    helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                    try {
                        ProjectManager.getDefault().saveProject(WebProject.this);
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                    return null;
                }
            });
            if (WebPhysicalViewProvider.hasBrokenLinks(evaluator())) {
                showBrokenReferencesAlert();
            }
        }
        
        protected void projectClosed() {
            // Probably unnecessary, but just in case:
            try {
                ProjectManager.getDefault().saveProject(WebProject.this);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
            
            // unregister project's classpaths to GlobalPathRegistry
            ClassPathProviderImpl cpProvider = (ClassPathProviderImpl)lookup.lookup(ClassPathProviderImpl.class);
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
                // XXX provide WAR as an artifact (for j2ee application?):
//                helper.createSimpleAntArtifact(JavaProjectConstants.ARTIFACT_TYPE_JAR, "dist.war", "war", "clean"), // NOI18N
            };
        }

    }
    
}
