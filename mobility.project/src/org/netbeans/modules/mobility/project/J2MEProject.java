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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.mobility.project;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.mobility.project.PropertyDescriptor;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.spi.mobility.project.ProjectPropertiesDescriptor;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.mobility.project.classpath.J2MEProjectClassPathExtender;
import org.netbeans.modules.mobility.project.queries.UnitTestForSourceQueryImpl;
import org.netbeans.modules.mobility.j2meunit.J2MEUnitPlugin;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.netbeans.modules.mobility.project.ui.J2MECustomizerProvider;
import org.netbeans.modules.mobility.project.ui.J2MEPhysicalViewProvider;
import org.netbeans.modules.mobility.project.queries.CompiledSourceForBinaryQuery;
import org.netbeans.modules.mobility.project.queries.JavadocForBinaryQueryImpl;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.mobility.project.classpath.J2MEClassPathProvider;
import org.netbeans.modules.mobility.project.queries.SourceLevelQueryImpl;
import org.netbeans.modules.mobility.project.queries.FileBuiltQueryImpl;
import org.netbeans.modules.mobility.project.queries.FileEncodingQueryImpl;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ant.*;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.openide.ErrorManager;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.Node;

/**
 * Represents one plain J2ME project.
 * @author Jesse Glick, Adam Sotona
 */
public final class J2MEProject implements Project, AntProjectListener {
    
    static final Icon J2ME_PROJECT_ICON = new ImageIcon(Utilities.loadImage( "org/netbeans/modules/mobility/project/ui/resources/mobile-project.png" )); // NOI18N
    private static final URLStreamHandler COMPOSED_STREAM_HANDLER = new URLStreamHandler() {
        protected URLConnection openConnection(URL u) throws IOException {
            return new ComposedConnection(u);
        }
    };
    
    static final String CONFIGS_NAME = "configurations"; // NOI18N
    static final String CONFIG_NAME = "configuration"; // NOI18N
    static final String CONFIGS_NS = "http://www.netbeans.org/ns/project-configurations/1"; // NOI18N

    final AuxiliaryConfiguration aux;
    final AntProjectHelper helper;
    final GeneratedFilesHelper genFilesHelper;
    Lookup lookup;
    final MIDletsCacheHelper midletsCacheHelper; 
    final ProjectConfigurationsHelper configHelper;
    
    private static final Set<FileObject> roots = new HashSet<FileObject>();
    private final ReferenceHelper refHelper;
    private final PropertyChangeSupport pcs;
    public FileBuiltQueryImpl fileBuiltQuery;
    
    /* Side effect of this methosd is modification of fo - is this correct? */
    public static boolean isJ2MEFile(FileObject fo) {
        if (fo == null) return false;
        final FileObject archiveRoot = FileUtil.getArchiveFile(fo);
        if (archiveRoot != null) {
            fo = archiveRoot;
        }
        while (fo != null) {
            synchronized (roots) {
                if (roots.contains(fo)) return true;
            }
            FileObject xml;
            if (fo.isFolder() && (xml = fo.getFileObject("nbproject/project.xml")) != null) { //NOI18N
                if (isJ2MEProjectXML(xml)) {
                    synchronized (roots) {
                        roots.add(fo);
                    }
                    return true;
                }
            }
            fo = fo.getParent();
        }
        return false;
    }
    
    protected static void addRoots(final AntProjectHelper helper) {
        final String src = helper.getStandardPropertyEvaluator().getProperty("src.dir"); //NOI18N
        if (src != null) addRoot(helper.resolveFileObject(src));
    }
    
    private static void addRoot(final FileObject fo) {
        if (fo != null && !isJ2MEFile(fo)) {
            synchronized (roots) {
                if (roots.add(fo))
                    RequestProcessor.getDefault().post(new Runnable() {
                        public void run() {
                            final Enumeration en = fo.getChildren(true);
                            while (en.hasMoreElements()) try {
                                final FileObject f2 = (FileObject)en.nextElement();
                                if (f2.getExt().equals("java")) DataObject.find(f2).setValid(false); //NOI18N
                            } catch (Exception e) {}
                        }
                    });
            }
        }
    }
    
    private static boolean isJ2MEProjectXML(final FileObject fo) {
        BufferedReader in = null;
        try {
            try {
                in = new BufferedReader(new InputStreamReader(fo.getInputStream()));
                String s;
                while ((s = in.readLine()) != null) {
                    if (s.indexOf("<type>"+J2MEProjectType.TYPE+"</type>") >= 0) return true; //NOI18N
                }
            } finally {
                if (in != null) in.close();
            }
        } catch (IOException ioe) {}
        return false;
    }
    
    J2MEProject(AntProjectHelper helper) {
        this.helper = helper;
        addRoots(helper);
        aux = helper.createAuxiliaryConfiguration();
        refHelper = new ReferenceHelper(helper, aux, helper.getStandardPropertyEvaluator());
        configHelper = new ProjectConfigurationsHelper(helper, this);
        genFilesHelper = new GeneratedFilesHelper(helper);
        midletsCacheHelper = new MIDletsCacheHelper(helper, configHelper);
        helper.addAntProjectListener(new CDCMainClassHelper(helper));
        pcs = new PropertyChangeSupport(this);
        fileBuiltQuery = new FileBuiltQueryImpl(helper, configHelper);
        this.lookup = this.createLookup(aux);
        helper.addAntProjectListener(this);
        configHelper.addPropertyChangeListener(new TextSwitcher(this, helper));
    }
    
    public void hookNewProjectCreated() {
        midletsCacheHelper.refresh();
    }
    
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }
    
    public Lookup getLookup() {
        return this.lookup;
    }
    
    public ProjectConfigurationsHelper getConfigurationHelper() {
        return configHelper;
    }
    
    private Lookup createLookup(final AuxiliaryConfiguration aux) {
        final SourcesHelper sourcesHelper = new SourcesHelper(helper, helper.getStandardPropertyEvaluator());
        sourcesHelper.addPrincipalSourceRoot("${src.dir}", NbBundle.getMessage(J2MEProject.class, "LBL_J2MEProject_Source_Packages"), /*XXX*/null, null); //NOI18N
        // XXX add build dir too?
        sourcesHelper.addTypedSourceRoot("${src.dir}", JavaProjectConstants.SOURCES_TYPE_JAVA, NbBundle.getMessage(J2MEProject.class, "LBL_J2MEProject_Source_Packages"), /*XXX*/null, null); //NOI18N
        
        final SubprojectProvider spp = refHelper.createSubprojectProvider();
        return Lookups.fixed(new Object[] {
            new Info(),
            aux,
            spp,
            new J2MEActionProvider( this, helper ),
            new J2MEPhysicalViewProvider(this, helper, refHelper, configHelper),
            new J2MECustomizerProvider( this, helper, refHelper, configHelper),
            new J2MEClassPathProvider(helper),
            new CompiledSourceForBinaryQuery(this, helper),
            new AntArtifactProviderImpl(),
            new ProjectXmlSavedHookImpl(),
            new ProjectOpenedHookImpl(),
            new JavadocForBinaryQueryImpl(this, helper),
            helper.createSharabilityQuery(helper.getStandardPropertyEvaluator(), new String[]{"${src.dir}"}, new String[]{"${dist.root.dir}", "${build.root.dir}", "${deployment.copy.target}"}), //NOI18N
            configHelper,
            helper,
            sourcesHelper.createSources(),
            new RecommendedTemplatesImpl(),
            new SourceLevelQueryImpl(helper),
            midletsCacheHelper,
            fileBuiltQuery,
            refHelper,
            new J2MEProjectClassPathExtender(this, helper, refHelper, configHelper),
            new J2MEProjectOperations(this, helper, refHelper),
            new J2MEUnitPlugin(this,helper),
            new UnitTestForSourceQueryImpl(this.helper),
            new PreprocessorFileFilterImplementation(configHelper, helper),
            new FileEncodingQueryImpl(helper)
        });
    }
    
    /** Store configured project name. */
    public void setName(final String name) {
        ProjectManager.mutex().writeAccess(new Mutex.Action<Object>() {
            public Object run() {
                final Element data = helper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                final NodeList nl = data.getElementsByTagNameNS(J2MEProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    final NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(J2MEProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                    data.insertBefore(nameEl, /* OK if null */data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                helper.putPrimaryConfigurationData(data, true);
                return null;
            }
        });
    }
    
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
    
    public void configurationXmlChanged(final AntProjectEvent ev) {
        if (ev.getPath().equals(AntProjectHelper.PROJECT_XML_PATH)) {
            // Could be various kinds of changes, but name & displayName might have changed.
            final Info info = (Info)getLookup().lookup(ProjectInformation.class);
            info.firePropertyChange(ProjectInformation.PROP_NAME);
            info.firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME);
        }
    }
    
    public void propertiesChanged(@SuppressWarnings("unused")
	final AntProjectEvent ev) {
        // currently ignored
    }
 
    
    protected void refreshPrivateProperties() throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Object>() {
                public Object run()  {
                    boolean modified = false;
                    EditableProperties proj = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    EditableProperties priv = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    priv.setProperty("netbeans.user",  System.getProperty("netbeans.user")); //NOI18N
                    for (ProjectPropertiesDescriptor p : Lookup.getDefault().lookup(new Lookup.Template<ProjectPropertiesDescriptor>(ProjectPropertiesDescriptor.class)).allInstances() ) {
                        for (PropertyDescriptor d : p.getPropertyDescriptors()) {
                            if (d.getDefaultValue() != null) {
                                EditableProperties ep = d.isShared() ? proj : priv;
                                if (!ep.containsKey(d.getName())) {
                                    ep.setProperty(d.getName(), d.getDefaultValue());
                                    modified = true;
                                }
                            }
                        }
                    }
                    Set<String> cfgs = removeConfigurationsFromProjectXml();
                    if (!cfgs.isEmpty()) {
                        modified = true;
                        cfgs.addAll(Arrays.asList(proj.getProperty(DefaultPropertiesDescriptor.ALL_CONFIGURATIONS).split(",")));
                        cfgs.remove(" "); cfgs.remove(""); //NOI18N
                        StringBuffer sb = new StringBuffer(" "); //NOI18N
                        for (String s : cfgs) {
                            sb.append(',').append(s);
                        }
                        proj.setProperty(DefaultPropertiesDescriptor.ALL_CONFIGURATIONS, sb.toString());
                    }
                    helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, priv);
                    if (modified) helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, proj);
                    try {
                        refHelper.addExtraBaseDirectory("netbeans.user"); //NOI18N
                    } catch (IllegalArgumentException iae) {
                        //ignore - see issue #102148
                    }
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException)e.getException();
        }
        // Probably unnecessary, but just in case:
        try {
            ProjectManager.getDefault().saveProject(J2MEProject.this);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    private Set<String> removeConfigurationsFromProjectXml() {
        TreeSet<String> cfgs = new TreeSet();
        Element configs = aux.getConfigurationFragment(CONFIGS_NAME, CONFIGS_NS, true);
        if (configs != null) {
            try {
                NodeList subEls = configs.getElementsByTagNameNS(CONFIGS_NS, CONFIG_NAME);
                for (int i=0; i<subEls.getLength(); i++) {
                    final NodeList l = subEls.item(i).getChildNodes();
                    for (int j = 0; j < l.getLength(); j++) {
                        if (l.item(j).getNodeType() == Node.TEXT_NODE) {
                            cfgs.add(((Text)l.item(j)).getNodeValue());
                        }
                    }
                }
                aux.removeConfigurationFragment(CONFIGS_NAME, CONFIGS_NS, true);
            } catch (IllegalArgumentException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }

        }
        return cfgs;
    }
    
    /**
     * Return configured project name.
     */
    public String getName() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<String>() {
            public String run() {
                final Element data = helper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(J2MEProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
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
    
    private final class ProjectXmlSavedHookImpl extends ProjectXmlSavedHook {
        
        ProjectXmlSavedHookImpl() {
            // Just to avoid creating accessor class
        }
        
        protected void projectXmlSaved() throws IOException {
            refreshBuildScripts(false);
        }
        
    }
    
    private final class ProjectOpenedHookImpl extends ProjectOpenedHook {
        
        private boolean skipCloseHook = false;
        
        ProjectOpenedHookImpl() {
            // Just to avoid creating accessor class
        }
        
        protected void projectOpened() {
            // Check up on build scripts.
            addRoots(helper);
            final SourcesHelper sourcesHelper = getLookup().lookup(SourcesHelper.class);
            final String srcDir = helper.getStandardPropertyEvaluator().getProperty(DefaultPropertiesDescriptor.SRC_DIR);
            final FileObject srcRoot = srcDir == null ? null : helper.resolveFileObject(srcDir);
            final Project other = srcRoot == null ? null : FileOwnerQuery.getOwner(srcRoot);
            if (other != null && !J2MEProject.this.equals(other)) {
                if (Arrays.asList(OpenProjects.getDefault().getOpenProjects()).contains(other)) {
                    final ProjectInformation pi = other.getLookup().lookup(ProjectInformation.class);
                    final String name = pi == null ? other.getProjectDirectory().getPath() : pi.getDisplayName();
                    if (NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Confirmation(NbBundle.getMessage(J2MEProject.class, "MSG_ClashingSourceRoots", J2MEProject.this.getName(), name), NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.WARNING_MESSAGE)))) { //NOI18N
                        OpenProjects.getDefault().close(new Project[]{other});
                    } else {
                        skipCloseHook = true;
                        OpenProjects.getDefault().close(new Project[]{J2MEProject.this});
                        return;
                    }
                }
            }
            ProjectManager.mutex().postWriteRequest(new Runnable() {
                public void run() {
                    try {
                        if (sourcesHelper != null) sourcesHelper.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
                    } catch (IllegalStateException ise) {}
                    if (srcRoot != null) FileOwnerQuery.markExternalOwner(srcRoot, J2MEProject.this, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
                }
            });
            try {
                refreshBuildScripts(true);
                refreshPrivateProperties();
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            
            // register project's classpaths to GlobalPathRegistry
            final J2MEClassPathProvider cpProvider = lookup.lookup(J2MEClassPathProvider.class);
            GlobalPathRegistry.getDefault().register(ClassPath.BOOT, new ClassPath[] {cpProvider.getBootClassPath()});
            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[] {cpProvider.getSourcepath()});
            GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, new ClassPath[] {cpProvider.getCompileTimeClasspath()});
            
            final J2MEPhysicalViewProvider phvp  = lookup.lookup(J2MEPhysicalViewProvider.class);
            if (phvp.hasBrokenLinks()) {
                BrokenReferencesSupport.showAlert();
            }
            
            midletsCacheHelper.refresh();
        }
        
        protected void projectClosed() {
            if (skipCloseHook) return;
            // Probably unnecessary, but just in case:
            try {
                ProjectManager.getDefault().saveProject(J2MEProject.this);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
            
            // unregister project's classpaths to GlobalPathRegistry
            final J2MEClassPathProvider cpProvider = lookup.lookup(J2MEClassPathProvider.class);
            GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, new ClassPath[] {cpProvider.getBootClassPath()});
            GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, new ClassPath[] {cpProvider.getSourcepath()});
            GlobalPathRegistry.getDefault().unregister(ClassPath.COMPILE, new ClassPath[] {cpProvider.getCompileTimeClasspath()});
        }
        
    }
    
    private void refreshBuildScripts(final boolean checkForProjectXmlModified) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                final FileObject root = Repository.getDefault().getDefaultFileSystem().findResource("Buildsystem/org.netbeans.modules.kjava.j2meproject"); //NOI18N
                final LinkedList<FileObject> files = new LinkedList();
                files.addAll(Arrays.asList(root.getChildren()));
                ProjectManager.mutex().postWriteRequest(new Runnable() {
                    public void run() {
                        try {
                            ProjectManager.getDefault().saveProject(J2MEProject.this);
                        } catch (IOException ioe) {
                            ErrorManager.getDefault().notify(ioe);
                        }
                        URL u = null;
                        while (!files.isEmpty()) try {
                            FileObject fo = files.removeFirst();
                            if (fo.getExt().equals("xml") && isAuthorized(fo)) { //NOI18N
                                u = fo.isData() ? fo.getURL() : new URL("", null, -1, fo.getPath(), COMPOSED_STREAM_HANDLER); //NOI18N
                                genFilesHelper.refreshBuildScript(FileUtil.getRelativePath(root, fo), u, checkForProjectXmlModified);
                            } else if (fo.isFolder()) {
                                files.addAll(Arrays.asList(fo.getChildren()));
                            }
                        } catch (IOException ioe) {
                            ErrorManager.getDefault().notify(ioe);
                            BufferedReader br = null;
                            if (u != null) try {
                                br = new BufferedReader(new InputStreamReader(u.openStream()));
                                String s;
                                while ((s = br.readLine()) != null) ErrorManager.getDefault().log(ErrorManager.ERROR, s);
                            } catch (Exception e) {
                            } finally {
                                if (br != null) try {br.close();} catch (IOException e) {}
                            }
                        }
                    }
                });
            }
        });
    }
    
    /**
     * Exports the main JAR as an official build product for use from other scripts.
     * The type of the artifact will be {@link AntArtifact#TYPE_JAR}.
     */
    private final class AntArtifactProviderImpl implements AntArtifactProvider {
        
        private AntArtifactProviderImpl()
        {
            // Just to avoid creating accessor class
        }
        
        public AntArtifact[] getBuildArtifacts() {
            final ProjectConfiguration cfgs[] = configHelper.getConfigurations().toArray(new ProjectConfiguration[0]);
            AntArtifact art[] = new AntArtifact[cfgs.length];
            for (int i=0; i<cfgs.length; i++) {
                art[i] = new J2MEAntArtifact(configHelper.getDefaultConfiguration().equals(cfgs[i]) ? null : cfgs[i].getDisplayName());
            }
            return art;
        }
        
    }
    
    private class J2MEAntArtifact extends AntArtifact {
        
        private final String configuration;
        
        public J2MEAntArtifact(String configuration) {
            this.configuration = configuration;//NOI18N
        }
        
        public String getCleanTargetName() {
            return "clean"; //NOI18N
        }
        
        public File getScriptLocation() {
            return helper.resolveFile(GeneratedFilesHelper.BUILD_XML_PATH);
        }
        
        public String getTargetName() {
            return "jar"; //NOI18N
        }
        
        public String getType() {
            return JavaProjectConstants.ARTIFACT_TYPE_JAR;
        }
        
        public Project getProject() {
            return J2MEProject.this;
        }
        
        public String getID() {
            return configuration == null ? super.getID() : super.getID() + "." + configuration;//NOI18N
        }
        
        public URI[] getArtifactLocations() {
            final PropertyEvaluator eval = helper.getStandardPropertyEvaluator();
            String path = "dist/"; //NOI18N
            if (configuration != null) path += configuration + "/"; //NOI18N
            final String locationResolved = eval.evaluate(path + J2MEProjectUtils.evaluateProperty(helper, "dist.jar", configuration)); //NOI18N
            if (locationResolved == null) {
                return new URI[0];
            }
            return new URI[] {getScriptLocation().getParentFile().toURI().relativize(helper.resolveFile(locationResolved).toURI())};
        }
        
        public Properties getProperties() {
            final Properties p = new Properties();
            p.put(DefaultPropertiesDescriptor.CONFIG_ACTIVE, configuration == null ? "" : configuration);
            return p;
        }
        
    }
    private final class Info implements ProjectInformation {
        
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        
        Info() {
            // Just to avoid creating accessor class
        }
        
        void firePropertyChange(final String prop) {
            pcs.firePropertyChange(prop, null, null);
        }
        
        public String getName() {
            return PropertyUtils.getUsablePropertyName(J2MEProject.this.getName());
        }
        
        public String getDisplayName() {
            return J2MEProject.this.getName();
        }
        
        public Icon getIcon() {
            return J2ME_PROJECT_ICON;
        }
        
        public Project getProject() {
            return J2MEProject.this;
        }
        
        public void addPropertyChangeListener(final PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }
        
        public void removePropertyChangeListener(final PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }
        
    }
    
    private static final class RecommendedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates {
        
        private static final String LOCATION = "RecommendedTemplates/org.netbeans.modules.kjava.j2meproject"; //NOI18N
        
        private RecommendedTemplatesImpl() {
        }
        
        public String[] getRecommendedTypes() {
            FileObject root = Repository.getDefault().getDefaultFileSystem().findResource(LOCATION);
            HashSet<String> result = new HashSet();
            for (FileObject fo : root.getChildren()) {
                String s = (String) fo.getAttribute("RecommendedTemplates"); //NOI18N
                if (s != null) result.addAll(Arrays.asList(s.split(","))); //NOI18N
            }
            return result.toArray(new String[result.size()]);
        }
        
        public String[] getPrivilegedTemplates() {
            //priviledged templates are ordered by module layer
            DataFolder root = DataFolder.findFolder(Repository.getDefault().getDefaultFileSystem().findResource(LOCATION));
            ArrayList<String> result = new ArrayList();
            for (DataObject ch : root.getChildren()) {
                String s = (String) ch.getPrimaryFile().getAttribute("PriviledgedTemplates"); //NOI18N
                if (s != null) result.addAll(Arrays.asList(s.split(","))); //NOI18N
            }
            return result.toArray(new String[result.size()]);
        }
        
    }
    
    private static final class ComposedConnection extends URLConnection {
        
        private static WeakHashMap<URL, byte[]> cache = new WeakHashMap();
        
        public ComposedConnection(URL u) {
            super(u);
        }

        public synchronized InputStream getInputStream() throws IOException {
            boolean log = Boolean.getBoolean("mobility.report.composed.stylesheets");//NOI18N
            byte[] data = cache.get(getURL());
            if (data == null) {
                DataFolder root = DataFolder.findFolder(Repository.getDefault().getDefaultFileSystem().findResource(getURL().getPath()));
                DataObject mainParts[] = root.getChildren();
                StringBuffer sb = new StringBuffer();
                String lastTarget = ""; //NOI18N
                for (int i=0; i<mainParts.length; i++) {
                    if (mainParts[i] instanceof DataFolder) {
                        DataObject subParts[] = ((DataFolder)mainParts[i]).getChildren();
                        StringBuffer subTargets = new StringBuffer(lastTarget);
                        for (int j=0; j<subParts.length; j++) {
                            FileObject fo = subParts[j].getPrimaryFile();
                            if (fo.isData() && isAuthorized(fo)) {
                                String s = read(subParts[j].getPrimaryFile(), lastTarget);
                                sb.append(s);
                                subTargets.append(',').append(subParts[j].getName());
                                if (log) ErrorManager.getDefault().log(ErrorManager.WARNING, fo.getURL().toExternalForm() + '\n' + s + '\n');
                            } 
                        }
                        lastTarget = subTargets.toString();
                    } else {
                        FileObject fo = mainParts[i].getPrimaryFile();
                        if (isAuthorized(fo)) {
                            String s = read(fo, lastTarget);
                            sb.append(s);
                            lastTarget = mainParts[i].getName();
                            if (log) ErrorManager.getDefault().log(ErrorManager.WARNING, fo.getURL().toExternalForm() + '\n' + s + '\n');
                        }
                    }
                }
                data = sb.toString().getBytes("UTF-8"); //NOI18N
                synchronized (cache) {
                    cache.put(getURL(), data);
                }
                if (log) ErrorManager.getDefault().log(ErrorManager.WARNING, getURL().toExternalForm() + '\n' + sb.toString() + '\n');
            }
            return new ByteArrayInputStream(data);
        }
        
        public void connect() throws IOException {}
    
        private String read(FileObject fo, String dependencies) throws IOException {
            int i = (int)fo.getSize();
            byte buff[] = new byte[i];
            DataInputStream in = new DataInputStream(fo.getInputStream());
            try {
                in.readFully(buff);
                assert in.read() == -1;
            } finally {
                in.close();
            }
            return new String(buff, "UTF-8").replace("__DEPENDS__", dependencies); //NOI18N
        }
        
    }
    
//    private static final Set<File> FRIENDS_JARS = collectFriendJars();
//    
//    private static Set<String> getFriends() {
//        Iterator<? extends ModuleInfo> it = Lookup.getDefault().lookupResult(ModuleInfo.class).allInstances().iterator();
//        while (it.hasNext()) {
//            ModuleInfo mi = it.next();
//            if ("org.netbeans.modules.mobility.project".equals(mi.getCodeNameBase())) {  //NOI18N
//                HashSet<String> friends = new HashSet<String>(Arrays.asList(((String)mi.getAttribute("OpenIDE-Module-Friends")).split("[,\\s]+"))); //NOI18N
//                friends.add("org.netbeans.modules.mobility.project"); //NOI18N
//                return friends;
//            }
//        }
//        return null;
//    }
//    
//    private static Set<File> collectFriendJars() {
//        Set<String> friends = getFriends();
//        if (friends == null) return null;
//        Set<File> jars = new HashSet<File>();
//        Iterator<? extends ModuleInfo> it = Lookup.getDefault().lookupResult(ModuleInfo.class).allInstances().iterator();
//        while (it.hasNext()) {
//            ModuleInfo mi = it.next();
//            if (friends.contains(mi.getCodeNameBase())) try {
//                Field f = mi.getClass().getDeclaredField("jar");//NOI18N
//                f.setAccessible(true);
//                File ff = (File)f.get(mi); //gettings field jar from StandardModule
//                if (ff != null) jars.add(ff);
//            } catch (Exception e) {};
//        }
//        if (jars.size() == 0) {
//            ErrorManager.getDefault().log(ErrorManager.WARNING, "Mobility Project Buildsystem cannot collect list of friend JARs."); //NOI18N
//            return null;
//        }
//        return jars;
//    }
    
    private static boolean isAuthorized(FileObject fo) {
//        if (fo.isFolder() || FRIENDS_JARS == null) return true;
//        URL u = null;
//        try {
//            u = fo.getURL();
//            //looking for MultiFileObject.leader field
//            Field f = fo.getClass().getDeclaredField("leader"); //NOI18N
//            f.setAccessible(true);
//            fo = (FileObject)f.get(fo); //getting the leader FileObject...
//            fo = (FileObject)f.get(fo); //...twice
//            File ff = FileUtil.toFile(fo);
//            if (ff == null) { //FileObject does not represent physical file
//                f = fo.getClass().getDeclaredField("uri"); //looking for BFSFile.uri field //NOI18N
//                f.setAccessible(true);
//                String s = (String)f.get(fo);
//                if (s == null) return true; //the uri field is not declared - empty file
//                u = new URL(s);
//                 //URL points to module jar content, no other protocols are allowed and the jar must be listed as friend
//                if ("jar".equals(u.getProtocol()) && FRIENDS_JARS.contains(new File(FileUtil.getArchiveFile(u).toURI()))) return true;  //NOI18N
//            } else {  //FileObject represents physical file (userdir or installdir / config /...) - this is not allowed
//                u = ff.toURL();
//            }
//            ErrorManager.getDefault().log(ErrorManager.WARNING, "Unauthorized access to Mobility Project Build System from: " + String.valueOf(u)); //NOI18N
//            return false;
//        } catch (Exception e) {
//            ErrorManager.getDefault().log(ErrorManager.WARNING, "Cannot verify access authorization to Mobility Project Build System from: " + String.valueOf(u)); //NOI18N
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
//            return true;
//        }
        return true;
    }
}
