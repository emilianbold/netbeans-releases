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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.mobility.project;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.Icon;
import org.netbeans.api.mobility.project.PropertyDescriptor;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.spi.mobility.project.ProjectPropertiesDescriptor;
import org.netbeans.spi.mobility.project.ui.customizer.support.VisualPropertySupport;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.mobility.project.classpath.J2MEProjectClassPathExtender;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.mobility.project.ui.J2MECustomizerProvider;
import org.netbeans.modules.mobility.project.ui.J2MEPhysicalViewProvider;
import org.netbeans.modules.mobility.project.queries.CompiledSourceForBinaryQuery;
import org.netbeans.modules.mobility.project.queries.JavadocForBinaryQueryImpl;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.j2me.cdc.platform.CDCPlatform;
import org.netbeans.modules.mobility.project.classpath.J2MEClassPathProvider;
import org.netbeans.modules.mobility.project.deployment.DeploymentPropertiesHandler;
import org.netbeans.modules.mobility.project.queries.SourceLevelQueryImpl;
import org.netbeans.modules.mobility.project.queries.FileBuiltQueryImpl;
import org.netbeans.modules.mobility.project.queries.FileEncodingQueryImpl;
import org.netbeans.modules.mobility.project.security.KeyStoreRepository;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.mobility.deployment.DeploymentPlugin;
import org.netbeans.spi.mobility.project.ProjectLookupProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ant.*;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.openide.ErrorManager;
import org.openide.util.Exceptions;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.Node;

/**
 * Represents one plain J2ME project.
 * @author Jesse Glick, Adam Sotona, Tim Boudreau
 */
@AntBasedProjectRegistration(
    type=J2MEProjectType.TYPE,
    iconResource="org/netbeans/modules/mobility/project/ui/resources/mobile-project.png",
    sharedNamespace=J2MEProjectType.PROJECT_CONFIGURATION_NAMESPACE,
    privateNamespace=J2MEProjectType.PRIVATE_CONFIGURATION_NAMESPACE
)
public final class J2MEProject implements Project, AntProjectListener {
    final Icon J2ME_PROJECT_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/mobility/project/ui/resources/mobile-project.png", false); // NOI18N
    private static final URLStreamHandler COMPOSED_STREAM_HANDLER = new URLStreamHandler() {
        protected URLConnection openConnection(URL u) throws IOException {
            return new ComposedConnection(u);
        }
    };
    
    static final String CONFIGS_NAME = "configurations"; // NOI18N
    static final String CONFIG_NAME = "configuration"; // NOI18N
    static final String CONFIGS_NS = "http://www.netbeans.org/ns/project-configurations/1"; // NOI18N
    static final String CLASSPATH = "classpath"; // NOI18N
    
    final AuxiliaryConfiguration aux;
    final AntProjectHelper helper;
    final GeneratedFilesHelper genFilesHelper;
    Lookup lookup;
    final MIDletsCacheHelper midletsCacheHelper; 
    final ProjectConfigurationsHelper configHelper;
    
    private static final Set<FileObject> roots = new HashSet<FileObject>();
    private static final Map<FileObject, Boolean> folders = new WeakHashMap<FileObject, Boolean>();
    private final ReferenceHelper refHelper;
    private final PropertyChangeSupport pcs;
    private final RequestProcessor rp;
    
    private TextSwitcher textSwitcher;
    
    /* Side effect of this methosd is modification of fo - is this correct? */
    public static boolean isJ2MEFile(FileObject fo) {
        if (fo == null) return false;
        final FileObject archiveRoot = FileUtil.getArchiveFile(fo);
        if (archiveRoot != null) {
            fo = archiveRoot;
        }
        return isJ2MEFolder(fo.getParent());
    }
        
    private static boolean isJ2MEFolder(FileObject fo) { 
        if (fo == null) return false;
        synchronized (roots) {
            if (roots.contains(fo)) return true;
        }
        Boolean result;
        synchronized (folders) {
            result = folders.get(fo);
        }
        if (result == null) {
            FileObject xml;
            if (fo.isFolder() && (xml = fo.getFileObject("nbproject/project.xml")) != null) {
                result = isJ2MEProjectXML(xml);
                if (result) synchronized (roots) {
                    roots.add(fo);
                }
            } else {
                result = isJ2MEFolder(fo.getParent());
            }
            synchronized (folders) {
                folders.put(fo, result);
            }
        }
        return result;
    }

    public boolean isConfigBroken (ProjectConfiguration cfg) {
        cfg = cfg == null ? configHelper.getActiveConfiguration() : cfg;
        String[] breakableConfigProperties = getBreakableProperties (cfg);
        String[] breakableConfigPlatformProperties = getBreakablePlatformProperties (cfg);
        boolean result = BrokenReferencesSupport.isBroken( helper, refHelper, 
                breakableConfigProperties, breakableConfigPlatformProperties);
        //also check for unresolved ant properties
        return result || breakableConfigProperties != null &&
                breakableConfigProperties[1] != null && 
                breakableConfigProperties[1].contains("${");
    }

    public RequestProcessor getRequestProcessor() {
        return rp;
    }

    public boolean hasBrokenLinks() {
        return BrokenReferencesSupport.isBroken(helper, refHelper,
                getBreakableProperties(), getBreakablePlatformProperties());
    }

    private String[] getBreakableProperties(ProjectConfiguration cfg) {
        String s[] = new String[3];
        s[0] = DefaultPropertiesDescriptor.SRC_DIR;
        s[1] = usedLibs(cfg);
        if (configHelper.getDefaultConfiguration().equals(cfg)) {
            s[2] = DefaultPropertiesDescriptor.SIGN_KEYSTORE;
        } else {
            s[2] = J2MEProjectProperties.CONFIG_PREFIX + cfg.getDisplayName() +
                    "." + DefaultPropertiesDescriptor.SIGN_KEYSTORE; //NOI18N
        }
        return s;
    }

    private String usedActive(final ProjectConfiguration cfg) {
        String libs;
        /* Check for default lib config */
        if (cfg.equals(getConfigurationHelper().getDefaultConfiguration())) {
            libs = DefaultPropertiesDescriptor.PLATFORM_ACTIVE;
        } else {
            libs = helper.getStandardPropertyEvaluator().getProperty(
                    J2MEProjectProperties.CONFIG_PREFIX + cfg.getDisplayName() +
                    "." + DefaultPropertiesDescriptor.PLATFORM_ACTIVE);
            if (libs == null) {
                libs = DefaultPropertiesDescriptor.PLATFORM_ACTIVE;
            } else {
                libs = J2MEProjectProperties.CONFIG_PREFIX + 
                        cfg.getDisplayName() + "." +
                        DefaultPropertiesDescriptor.PLATFORM_ACTIVE;
            }
        }
        return libs;
    }

    private String[] getBreakablePlatformProperties(ProjectConfiguration cfg) {
        String s[]=new String[1];
        s[0]=usedActive(cfg);
        return s;
    }

    public String[] getBreakableProperties() {
        final ProjectConfiguration pc[] = configHelper.getConfigurations().toArray(
                new ProjectConfiguration[0]);
        String s[] = new String[2*pc.length+1];
        s[0] = DefaultPropertiesDescriptor.SRC_DIR;
        for (int i= 0; i<pc.length; i++) {
            if (configHelper.getDefaultConfiguration().equals(pc[i])) {
                s[2*i+1] = DefaultPropertiesDescriptor.LIBS_CLASSPATH;
                s[2*i+2] = DefaultPropertiesDescriptor.SIGN_KEYSTORE;
            } else {
                s[2*i+1] = J2MEProjectProperties.CONFIG_PREFIX + 
                        pc[i].getDisplayName() + "." +
                        DefaultPropertiesDescriptor.LIBS_CLASSPATH; //NOI18N
                s[2*i+2] = J2MEProjectProperties.CONFIG_PREFIX + 
                        pc[i].getDisplayName() + "." +
                        DefaultPropertiesDescriptor.SIGN_KEYSTORE; //NOI18N
            }
        }
        return s;
    }

    public String[] getBreakablePlatformProperties() {
        final ProjectConfiguration pc[] =
                configHelper.getConfigurations().toArray(new ProjectConfiguration[0]);
        String s[] = new String[pc.length];
        for (int i= 0; i<pc.length; i++) {
            if (configHelper.getDefaultConfiguration().equals(pc[i])) {
                s[i] = DefaultPropertiesDescriptor.PLATFORM_ACTIVE;
            } else {
                s[i] = J2MEProjectProperties.CONFIG_PREFIX + 
                        pc[i].getDisplayName() + "." +
                        DefaultPropertiesDescriptor.PLATFORM_ACTIVE; //NOI18N
            }
        }
        return s;
    }

    public boolean isUsingDefaultLibs(ProjectConfiguration config) {
        ProjectConfiguration cfg = config == null ? getConfigurationHelper().getActiveConfiguration() : config;
        return cfg == null ? false : usedLibs(cfg) == null;
    }

    public boolean canModifyLibraries(ProjectConfiguration config) {
        return !isUsingDefaultLibs(config);
    }

    public boolean isInDefaultConfiguration() {
        return configHelper.getDefaultConfiguration().equals(configHelper.getActiveConfiguration());
    }

    public String usedLibs(final ProjectConfiguration cfg) {
        if (cfg == null) {
            return null;
        }
        String libs;
        /* Check for default lib config */
        if (cfg.getDisplayName().equals(getConfigurationHelper().getDefaultConfiguration().getDisplayName())) {
            libs = helper.getStandardPropertyEvaluator().getProperty(DefaultPropertiesDescriptor.LIBS_CLASSPATH);
        } else {
            libs = helper.getStandardPropertyEvaluator().getProperty(J2MEProjectProperties.CONFIG_PREFIX +
                    cfg.getDisplayName() + "." + DefaultPropertiesDescriptor.LIBS_CLASSPATH); //NOI18N
        }
        return libs;
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
    
    private static final String LOOK_FOR_PROJECT = ".*<type>org.netbeans.modules.kjava.j2meproject</type>";
    private static final Pattern PROJECT_PATTERN = Pattern.compile(LOOK_FOR_PROJECT, Pattern.DOTALL);
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static boolean isJ2MEProjectXML(final FileObject fo) {
        File file = FileUtil.toFile(fo);
        if (file == null) return false;
        boolean result;
        FileInputStream in = null;
        FileChannel channel = null;
        try {
            in = new FileInputStream(file);
            channel = in.getChannel();
            ByteBuffer buf = ByteBuffer.allocate((int) file.length());
            channel.read(buf);
            buf.rewind();
            CharBuffer chars = UTF8.decode(buf);
            result = PROJECT_PATTERN.matcher(chars).lookingAt();
        } catch (IOException ioe) {
            result = false;
            Exceptions.printStackTrace(ioe);
        } finally {
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                    result = false;
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    result = false;
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return result;
    }

    
    public J2MEProject(AntProjectHelper helper) {
        rp = new RequestProcessor ("RP for " +
                helper.getProjectDirectory().getPath(), 2, true);
        this.helper = helper;
        addRoots(helper);
        aux = helper.createAuxiliaryConfiguration();
        refHelper = new ReferenceHelper(helper, aux, helper.getStandardPropertyEvaluator());
        configHelper = new ProjectConfigurationsHelper(helper, this);
        genFilesHelper = new GeneratedFilesHelper(helper);
        midletsCacheHelper = new MIDletsCacheHelper(helper, configHelper);
        helper.addAntProjectListener(new CDCMainClassHelper(helper));
        pcs = new PropertyChangeSupport(this);
        this.lookup = this.createLookup(aux);
        helper.addAntProjectListener(this);
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
        SourcesHelper sourcesHelper = new SourcesHelper(this, helper, helper.getStandardPropertyEvaluator());
        sourcesHelper.addPrincipalSourceRoot("${src.dir}", NbBundle.getMessage(J2MEProject.class, "LBL_J2MEProject_Source_Packages"), null, null); //NOI18N
        sourcesHelper.addTypedSourceRoot("${src.dir}", JavaProjectConstants.SOURCES_TYPE_JAVA, NbBundle.getMessage(J2MEProject.class, "LBL_J2MEProject_Source_Packages"), null, null); //NOI18N
        final SubprojectProvider spp = refHelper.createSubprojectProvider();
        
        Object stdLookups[]=new Object[] {
            this,
            new Info(),
            rp,
            aux,
            spp,
            configHelper,
            helper,
            midletsCacheHelper,
            refHelper,
            new FileBuiltQueryImpl(helper, configHelper),
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
            sourcesHelper.createSources(),
            new RecommendedTemplatesImpl(),
            new SourceLevelQueryImpl(helper),
            new J2MEProjectClassPathExtender(this, helper, refHelper, configHelper),
            new J2MEProjectOperations(this, helper, refHelper),
            new PreprocessorFileFilterImplementation(configHelper, helper),
            new FileEncodingQueryImpl(helper)
        };
        ArrayList<Object> list=new ArrayList<Object>();
        list.addAll(Arrays.asList(stdLookups));
        for (ProjectLookupProvider provider : Lookup.getDefault().lookupAll(ProjectLookupProvider.class))
        {
            list.addAll(provider.createLookupElements(this,helper,refHelper,configHelper));
        }
        return Lookups.fixed(list.toArray());
//        return new AnalysisLookup (list.toArray());
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
    
    private final class ProjectOpenedHookImpl extends ProjectOpenedHook implements LookupListener {
        
        private boolean skipCloseHook = false;
        private PropertyChangeListener platformListener;
        private Lookup.Result deployments;

        //We need those listners to be able to check for changes on paltform bootclasspath
        private final class PlatformInstalledListener implements PropertyChangeListener 
        {
            final List<JavaPlatform> knownPlatforms;
            private final PropertyChangeListener platformChange = new PropertyChangeListener()
            {
                public void propertyChange(PropertyChangeEvent evt)
                {
                    if (CLASSPATH.equals(evt.getPropertyName()) && evt.getSource() instanceof CDCPlatform)
                    {
                       CDCPlatform platform = (CDCPlatform)evt.getSource(); 
                       if (platform != null)
                       {
                           List<ProjectConfiguration> configs = J2MEProject.this.getMatchingConfigs((String)platform.getProperties().get("platform.ant.name"));
                           J2MEProject.this.updateBootClassPathProperty(configs, platform);
                       }
                    }
                }
            };

            PlatformInstalledListener(JavaPlatform known[])
            {
                knownPlatforms=new ArrayList(Arrays.asList(known));

                for (JavaPlatform plat : knownPlatforms)
                {
                    plat.addPropertyChangeListener(platformChange);
                    List<ProjectConfiguration> configs = J2MEProject.this.getMatchingConfigs(plat.getProperties().get("platform.ant.name"));
                    J2MEProject.this.updateBootClassPathProperty(configs, (CDCPlatform)plat);
                }
            }

            public void propertyChange(PropertyChangeEvent evt)
            {
                if (evt.getPropertyName().equals(JavaPlatformManager.PROP_INSTALLED_PLATFORMS))
                {
                    JavaPlatform[] known=JavaPlatformManager.getDefault().getPlatforms(null, new Specification (CDCPlatform.PLATFORM_CDC,null));
                    List<JavaPlatform> list=Arrays.asList(known);
                    List<JavaPlatform> added = new ArrayList(Arrays.asList(known));
                    added.removeAll(knownPlatforms);
                    knownPlatforms.removeAll(list);
                    for (JavaPlatform platform : knownPlatforms)
                    {
                        platform.removePropertyChangeListener(platformChange);
                    }
                    for (JavaPlatform platform : added)
                    {
                        platform.addPropertyChangeListener(platformChange);
                    }
                    knownPlatforms.clear();
                    knownPlatforms.addAll(list);
                }   
            }
        };
    
        
        ProjectOpenedHookImpl() {
            // Just to avoid creating accessor class
        }
        
        protected synchronized void projectOpened() {
            //inicialize deployment plugins
            deployments = Lookup.getDefault().lookup(new Lookup.Template<DeploymentPlugin>(DeploymentPlugin.class));
            deployments.addLookupListener(this);
            resultChanged(new LookupEvent(deployments));
            //init keystore, safer than warmup
            KeyStoreRepository.getDefault();
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
                refreshBootClasspath();
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            
            // register project's classpaths to GlobalPathRegistry
            final J2MEClassPathProvider cpProvider = lookup.lookup(J2MEClassPathProvider.class);
            assert cpProvider != null;
            GlobalPathRegistry.getDefault().register(ClassPath.BOOT, new ClassPath[] {cpProvider.getBootClassPath()});
            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[] {cpProvider.getSourcepath()});
            GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, new ClassPath[] {cpProvider.getCompileTimeClasspath()});
            
            configHelper.addPropertyChangeListener(textSwitcher = new TextSwitcher(J2MEProject.this, helper));

            final J2MEPhysicalViewProvider phvp  = lookup.lookup(J2MEPhysicalViewProvider.class);
            //Don't block opening other projects with this - see issue 155808
            getRequestProcessor().post(new Runnable() {
                public void run() {
                    if (hasBrokenLinks()) {
                        BrokenReferencesSupport.showAlert();
                    }
                    midletsCacheHelper.refresh();
                }
            });
            
        }
        
        protected synchronized void projectClosed() {
            if (skipCloseHook) return;
            //do not listen on deployments for this project
            deployments.removeLookupListener(this);

            // Probably unnecessary, but just in case:
            try {
                ProjectManager.getDefault().saveProject(J2MEProject.this);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
            
            configHelper.removePropertyChangeListener(textSwitcher);

            // unregister project's classpaths to GlobalPathRegistry
            final J2MEClassPathProvider cpProvider = lookup.lookup(J2MEClassPathProvider.class);

            unregisterPath(ClassPath.BOOT, new ClassPath[] {cpProvider.getBootClassPath()});
            unregisterPath(ClassPath.SOURCE, new ClassPath[] {cpProvider.getSourcepath()});
            unregisterPath(ClassPath.COMPILE, new ClassPath[] {cpProvider.getCompileTimeClasspath()});
            
            JavaPlatformManager.getDefault().removePropertyChangeListener(platformListener);
        }

        private void unregisterPath (String type, ClassPath[] paths) {
            try {
                GlobalPathRegistry.getDefault().unregister(type, paths);
            } catch (IllegalArgumentException iae) {
                Logger.getLogger(J2MEProject.class.getName()).log(Level.INFO,
                        "Issue http://www.netbeans.org/nonav/issues/show_bug.cgi?id=150469 - " +
                        "unregistering non-existent path", iae);
            }
        }

        
        private void refreshBootClasspath()
        {
            
            JavaPlatform[] installedPlatforms = JavaPlatformManager.getDefault().
                    getPlatforms(null, new Specification (CDCPlatform.PLATFORM_CDC,null));   //NOI18N
            platformListener = new PlatformInstalledListener(installedPlatforms);
            JavaPlatformManager.getDefault().addPropertyChangeListener(platformListener);
        }
        
        public void resultChanged(final LookupEvent e) {
            final Collection<Lookup.Result> result = ((Lookup.Result) e.getSource()).allInstances();
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    DeploymentPropertiesHandler.loadDeploymentProperties(result);
                }
            }, 200);
        }        
    }
    
    private void refreshBuildScripts(final boolean checkForProjectXmlModified) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                final FileObject root = FileUtil.getConfigFile("Buildsystem/org.netbeans.modules.kjava.j2meproject"); //NOI18N
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
        
        @Override
        public Project getProject() {
            return J2MEProject.this;
        }
        
        public String getID() {
            return configuration == null ? super.getID() : super.getID() + "." + configuration;//NOI18N
        }
        
        @Override
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
        
        @Override
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
            FileObject root = FileUtil.getConfigFile(LOCATION);
            HashSet<String> result = new HashSet();
            for (FileObject fo : root.getChildren()) {
                String s = (String) fo.getAttribute("RecommendedTemplates"); //NOI18N
                if (s != null) result.addAll(Arrays.asList(s.split(","))); //NOI18N
            }
            return result.toArray(new String[result.size()]);
        }
        
        public String[] getPrivilegedTemplates() {
            //priviledged templates are ordered by module layer
            DataFolder root = DataFolder.findFolder(FileUtil.getConfigFile(LOCATION));
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
                DataFolder root = DataFolder.findFolder(FileUtil.getConfigFile(getURL().getPath()));
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
    
    
    private static String normalizePath (File path,  File jdkHome, String propName) {
        String jdkLoc = jdkHome.getAbsolutePath();
        if (!jdkLoc.endsWith(File.separator)) {
            jdkLoc = jdkLoc + File.separator;
        }
        String loc = path.getAbsolutePath();
        if (loc.startsWith(jdkLoc)) {
            return "${"+propName+"}"+File.separator+loc.substring(jdkLoc.length());           //NOI18N
        }
        return loc;
    }
    
    private List<ProjectConfiguration> getMatchingConfigs(final String actualPlatformId) {
        List<ProjectConfiguration> configs = new ArrayList<ProjectConfiguration>();
        
        for (ProjectConfiguration config : getConfigurationHelper().getConfigurations())
        {
            boolean useDef= config.equals(getConfigurationHelper().getDefaultConfiguration());
            String platformProp=VisualPropertySupport.translatePropertyName(config.getDisplayName(), 
                                                  DefaultPropertiesDescriptor.PLATFORM_ACTIVE, useDef);
            String platformId=helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(platformProp);
        
            //platformId is null when non default config, which use default values, is queried
            //This one is not important for us as the change will be/have been done using DefaultConfiguration
            if (platformId != null && platformId.equals(actualPlatformId))
            {
                configs.add(config);
            }
        }
        return configs;
    }
    
    private void generatePlatformProperties (CDCPlatform platform,ProjectConfiguration config, String activeDevice, String activeProfile, EditableProperties props)  {
        Collection<FileObject> installFolders = platform.getInstallFolders();
        if (installFolders.size()>0) {            
            File jdkHome = FileUtil.toFile (installFolders.iterator().next());
            StringBuffer sbootcp = new StringBuffer();
            ClassPath bootCP = platform.getBootstrapLibrariesForProfile(activeDevice, activeProfile);
            for (ClassPath.Entry entry : (List<ClassPath.Entry>)bootCP.entries()) {
                URL url = entry.getURL();
                if ("jar".equals(url.getProtocol())) {              //NOI18N
                    url = FileUtil.getArchiveFile(url);
                }
                File root = new File (URI.create(url.toExternalForm()));
                if (sbootcp.length()>0) {
                    sbootcp.append(File.pathSeparator);
                }
                sbootcp.append(normalizePath(root, jdkHome, "platform.home"));
            }
            boolean useDef= config.equals(getConfigurationHelper().getDefaultConfiguration());
            props.setProperty(VisualPropertySupport.translatePropertyName(config.getDisplayName(),
                    DefaultPropertiesDescriptor.PLATFORM_BOOTCLASSPATH,useDef),sbootcp.toString());   //NOI18N
        }
    }

    private void updateBootClassPathProperty(List<ProjectConfiguration> configs, CDCPlatform platform)
    {
        if (configs != null)
        {
            try
            {
                EditableProperties props=helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                for (ProjectConfiguration config : configs)
                {
                    boolean useDef= config.equals(getConfigurationHelper().getDefaultConfiguration());
                    generatePlatformProperties(
                            platform,
                            config,
                            props.getProperty(VisualPropertySupport.translatePropertyName(config.getDisplayName(),DefaultPropertiesDescriptor.PLATFORM_DEVICE,useDef)),
                            props.getProperty(VisualPropertySupport.translatePropertyName(config.getDisplayName(),DefaultPropertiesDescriptor.PLATFORM_PROFILE,useDef)),
                            props
                            ); 
                }
                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,props);
                ProjectManager.getDefault().saveProject(this);
            } catch (IOException ex)
            {
                ErrorManager.getDefault().notify(ex);
            } 
        }
    }

    @Override
    public String toString() {
        //Better logging for issue 153666
        return super.toString() + "[root=" + helper.getProjectDirectory().getPath() + ']'; //NOI18N
    }

}
