/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.cnd.makeproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.api.search.SearchRoot;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectRegistry;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.remote.RemoteProject;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.utils.CndFileVisibilityQuery;
import org.netbeans.modules.cnd.debug.DebugUtils;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifactProvider;
import org.netbeans.modules.cnd.makeproject.api.MakeCustomizerProvider;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectCustomizer;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.DevelopmentHostConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder.FileObjectNameMatcher;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectEvent;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectHelper;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectListener;
import org.netbeans.modules.cnd.makeproject.ui.FolderSearchInfo.FileObjectNameMatcherImpl;
import org.netbeans.modules.cnd.makeproject.ui.MakeLogicalViewProvider;
import org.netbeans.modules.cnd.makeproject.ui.options.FullFileIndexer;
import org.netbeans.modules.cnd.repository.api.CacheLocation;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.cnd.spi.toolchain.ToolchainProject;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.FilteringPathResourceImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.netbeans.spi.search.SearchInfoDefinition;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataLoaderPool;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Represents one plain Make project.
 */
public final class MakeProject implements Project, MakeProjectListener, Runnable {

    public static final String REMOTE_MODE = "remote-sources-mode"; // NOI18N
    public static final String REMOTE_FILESYSTEM_HOST = "remote-filesystem-host"; // NOI18N
    public static final String REMOTE_FILESYSTEM_BASE_DIR = "remote-filesystem-base-dir"; // NOI18N
    private static final boolean UNIT_TEST_MODE = CndUtils.isUnitTestMode();
    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.cnd.makeproject"); // NOI18N
    private static final String HEADER_EXTENSIONS = "header-extensions"; // NOI18N
    private static final String C_EXTENSIONS = "c-extensions"; // NOI18N
    private static final String CPP_EXTENSIONS = "cpp-extensions"; // NOI18N
    private static final RequestProcessor RP = new RequestProcessor("Open project", 4); // NOI18N
    private static MakeTemplateListener templateListener = null;
    private final MakeProjectTypeImpl kind;
    private final MakeProjectHelper helper;
    //private final PropertyEvaluator eval;
    //private final ReferenceHelper refHelper;
    private final NativeProject nativeProject;
    private final Lookup lookup;
    private final ConfigurationDescriptorProvider projectDescriptorProvider;
    private final Set<String> headerExtensions = MakeProject.createExtensionSet();
    private final Set<String> cExtensions = MakeProject.createExtensionSet();
    private final Set<String> cppExtensions = MakeProject.createExtensionSet();
    private String sourceEncoding = null;
    private boolean isOpenHookDone = false;
    private final AtomicBoolean isDeleted = new AtomicBoolean(false);
    private final AtomicBoolean isDeleting = new AtomicBoolean(false);
    private final MakeSources sources;
    private final MutableCP sourcepath;
    private final PropertyChangeListener indexerListener = new IndexerOptionsListener();
    private /*final*/ RemoteProject.Mode remoteMode;
    private final String remoteBaseDir;
    private ExecutionEnvironment fileSystemHost;

    public MakeProject(MakeProjectHelper helper) throws IOException {
        LOGGER.log(Level.FINE, "Start of creation MakeProject@{0} {1}", new Object[]{System.identityHashCode(MakeProject.this), helper.getProjectDirectory().getNameExt()}); // NOI18N
        this.kind = MakeBasedProjectFactorySingleton.TYPE_INSTANCE;
        this.helper = helper;
        //eval = createEvaluator();
        AuxiliaryConfiguration aux = helper.createAuxiliaryConfiguration();
        //refHelper = new ReferenceHelper(helper, aux, eval);
        projectDescriptorProvider = new ConfigurationDescriptorProvider(this, helper.getProjectDirectory());
        LOGGER.log(Level.FINE, "Create ConfigurationDescriptorProvider@{0} for MakeProject@{1} {2}", new Object[]{System.identityHashCode(projectDescriptorProvider), System.identityHashCode(MakeProject.this), helper.getProjectDirectory().getNameExt()}); // NOI18N
        sources = new MakeSources(this, helper);
        sourcepath = new MutableCP(sources);
        lookup = createLookup(aux);
        nativeProject = lookup.lookup(NativeProject.class);

        // Find the project type from project.xml
        Element data = helper.getPrimaryConfigurationData(true);

        remoteMode = RemoteProject.DEFAULT_MODE;
        NodeList remoteModeNodeList = data.getElementsByTagName(REMOTE_MODE);
        if (remoteModeNodeList.getLength() == 1) {
            remoteModeNodeList = remoteModeNodeList.item(0).getChildNodes();
            String t = remoteModeNodeList.item(0).getNodeValue();
            RemoteProject.Mode mode = RemoteProject.Mode.valueOf(t);
            CndUtils.assertNotNull(mode, "can not restore remote mode " + t); //NOI18N
            if (mode != null) {
                remoteMode = mode;
            }
        } else if (remoteModeNodeList.getLength() > 0) {
            CndUtils.assertTrueInConsole(false, "Wrong project.xml structure"); //NOI18N
        }

        fileSystemHost = FileSystemProvider.getExecutionEnvironment(helper.getProjectDirectory());        
        NodeList remoteFSHostNodeList = data.getElementsByTagName(REMOTE_FILESYSTEM_HOST);
        if (remoteFSHostNodeList.getLength() == 1) {
            remoteFSHostNodeList = remoteFSHostNodeList.item(0).getChildNodes();
            String hostID = remoteFSHostNodeList.item(0).getNodeValue();
            // XXX:fullRemote: separate user from host!
            fileSystemHost = ExecutionEnvironmentFactory.fromUniqueID(hostID);
        } else if (remoteFSHostNodeList.getLength() > 0) {
            CndUtils.assertTrueInConsole(false, "Wrong project.xml structure"); //NOI18N
        }

        NodeList remoteFSMountPoint = data.getElementsByTagName(REMOTE_FILESYSTEM_BASE_DIR);
        if (remoteFSMountPoint.getLength() > 0) {
            remoteBaseDir = remoteFSMountPoint.item(0).getTextContent();
            CndUtils.assertTrueInConsole(remoteFSMountPoint.getLength() == 1,
                    "Wrong project.xml structure: too many remote base dirs " + remoteFSMountPoint); //NOI18N
        } else {
            remoteBaseDir = null;
        }

        readProjectExtension(data, HEADER_EXTENSIONS, headerExtensions);
        readProjectExtension(data, C_EXTENSIONS, cExtensions);
        readProjectExtension(data, CPP_EXTENSIONS, cppExtensions);
        sourceEncoding = getSourceEncodingFromProjectXml();

        synchronized(MakeProject.class) {
            if (templateListener == null) {
                DataLoaderPool.getDefault().addOperationListener(templateListener = new MakeTemplateListener());
            }
        }
        LOGGER.log(Level.FINE, "End of creation MakeProject@{0} {1}", new Object[]{System.identityHashCode(MakeProject.this), helper.getProjectDirectory().getNameExt()}); // NOI18N
    }

    private void readProjectExtension(Element data, String key, Set<String> set) {
        NodeList nl = data.getElementsByTagName(key);
        if (nl.getLength() == 1) {
            nl = nl.item(0).getChildNodes();
            if (nl.getLength() == 1) {
                String extensions = nl.item(0).getNodeValue();
                set.addAll(Arrays.asList(extensions.split(","))); // NOI18N
            }
        }
    }

    /*package*/ void setRemoteMode(RemoteProject.Mode mode) {
        remoteMode = mode;
    }

    public RemoteProject.Mode getRemoteMode() {
        return remoteMode;
    }

    public ExecutionEnvironment getRemoteFileSystemHost() {
        return fileSystemHost;
    }

    private FileSystem getSourceFileSystem() {
        if (fileSystemHost == null || fileSystemHost.isLocal()) {
            return CndFileUtils.getLocalFileSystem();
        } else {
            return FileSystemProvider.getFileSystem(fileSystemHost);
        }
    }

    /*package*/ void setRemoteFileSystemHost(ExecutionEnvironment remoteFileSystemHost) {
        this.fileSystemHost = remoteFileSystemHost;
    }

    @Override
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }

    @Override
    public String toString() {
        return "MakeProject[" + getProjectDirectory() + "]"; // NOI18N
    }

    public MakeProjectHelper getMakeProjectHelper() {
        return helper;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    private Lookup createLookup(AuxiliaryConfiguration aux) {
        SubprojectProvider spp = new MakeSubprojectProvider(); //refHelper.createSubprojectProvider();
        Info info = new Info();
        Object[] lookups = new Object[] {
                    info,
                    aux,
                    helper.createCacheDirectoryProvider(),
                    spp,
                    new MakeActionProvider(this),
                    new MakeLogicalViewProvider(this),
                    new MakeCustomizerProvider(this, projectDescriptorProvider),
                    new MakeArtifactProviderImpl(),
                    UILookupMergerSupport.createProjectOpenHookMerger(new ProjectOpenedHookImpl()),
                    new MakeSharabilityQuery(projectDescriptorProvider, getProjectDirectory()),
                    sources,
                    helper,
                    projectDescriptorProvider,
                    new MakeProjectConfigurationProvider(this, projectDescriptorProvider, info),
                    new NativeProjectSettingsImpl(this, this.kind.getPrimaryConfigurationDataElementNamespace(false), false),
                    new RecommendedTemplatesImpl(projectDescriptorProvider),
                    new MakeProjectOperations(this),
                    new MakeProjectSearchInfo(projectDescriptorProvider),
                    kind,
                    new MakeProjectEncodingQueryImpl(this),
                    new RemoteProjectImpl(),
                    new ToolchainProjectImpl(),
                    new CPPImpl(sources),
                    this,
                    createCacheLocation(helper),
                };
        
        MakeProjectCustomizer makeProjectCustomizer = getProjectCustomizer(getProjectCustomizerId());
        if (makeProjectCustomizer != null) {
            lookups = makeProjectCustomizer.getLookup(getProjectDirectory(), lookups);
        }
        boolean containsNativeProject = false;
        for (Object object : lookups) {
            if(object instanceof NativeProject) {
                containsNativeProject = true;
                break;
            }
        }
        if(!containsNativeProject) {
            ArrayList<Object> newLookups = new ArrayList<Object>();
            newLookups.addAll(Arrays.asList(lookups));
            newLookups.add(new NativeProjectProvider(this, projectDescriptorProvider));
            lookups = newLookups.toArray();
        }
        Lookup lkp = Lookups.fixed(lookups);
        return LookupProviderSupport.createCompositeLookup(lkp, kind.getLookupMergerPath());
    }

    /**
     * Tries getting cache path from project.properties -
     * first private, then public
     */
    private static CacheLocation createCacheLocationFromProperties(MakeProjectHelper helper) {

        FileObject projectDirectory = helper.getProjectDirectory();

        String[] propertyPaths = new String[] {
            MakeProjectHelper.PRIVATE_PROPERTIES_PATH,
            MakeProjectHelper.PROJECT_PROPERTIES_PATH
        };

        for (int i = 0; i < propertyPaths.length; i++) {
            FileObject propsFO = projectDirectory.getFileObject(propertyPaths[i]);
            if (propsFO != null && propsFO.isValid()) {
                Properties props = new Properties();
                try {
                    props.load(propsFO.getInputStream());
                    String path = props.getProperty("cache.location"); //NOI18N
                    if (path != null) {
                        if (CndPathUtilitities.isPathAbsolute(path)) {
                            return new CacheLocation(new File(path));
                        } else {
                            return new CacheLocation(new File(projectDirectory.getPath() + '/' + path)); //NOI18N
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
            }
        }
        return null;
    }

    private static CacheLocation createCacheLocation(MakeProjectHelper helper) {
        CacheLocation location = createCacheLocationFromProperties(helper);
        if (location != null) {
            return location;
        }
        if (DebugUtils.getBoolean("cnd.cache.in.project", false)) { //NOI18N
            FileObject projectDirectory = helper.getProjectDirectory();
            if (CndFileUtils.isLocalFileSystem(projectDirectory)) {
                File cache = new File(projectDirectory.getPath() + "/nbproject/private/cache/model"); //NOI18N
                cache.mkdirs();
                if (cache.exists()) {
                    return new CacheLocation(cache);
                }
            }
        }
        return CacheLocation.DEFAULT;
    }

    @Override
    public void configurationXmlChanged(MakeProjectEvent ev) {
        if (ev.getPath().equals(MakeProjectHelper.PROJECT_XML_PATH)) {
            // Could be various kinds of changes, but name & displayName might have changed.
            Info info = (Info) getLookup().lookup(ProjectInformation.class);
            info.firePropertyChange(ProjectInformation.PROP_NAME);
            info.firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME);
        }
    }

    @Override
    public void propertiesChanged(MakeProjectEvent ev) {
        // currently ignored (probably better to listen to evaluator() if you need to)
    }

    /**
     * Check needed header extensions and store list in the NB/project properties.
     * @param needAdd list of needed extensions of header files.
     */
    public boolean addAdditionalHeaderExtensions(Collection<String> needAdd) {
        Set<String> headerExtension = MakeProject.getHeaderSuffixes();
        Set<String> sourceExtension = MakeProject.getSourceSuffixes();
        Set<String> usedExtension = MakeProject.createExtensionSet();
        for (String extension : needAdd) {
            if (extension.length() > 0) {
                if (!headerExtension.contains(extension) && !sourceExtension.contains(extension)) {
                    usedExtension.add(extension);
                }
            }
        }
        if (usedExtension.size() > 0) {
            // add unknown extension to header files
            addMIMETypeExtensions(usedExtension, MIMENames.HEADER_MIME_TYPE);
            headerExtensions.addAll(usedExtension);
            saveAdditionalExtensions();
            return true;
        }
        return false;
    }

    private void addMIMETypeExtensions(Collection<String> extensions, String mime) {
        MIMEExtensions exts = MIMEExtensions.get(mime);
        for (String ext : extensions) {
            exts.addExtension(ext);
        }
        CndFileVisibilityQuery.getDefault().stateChanged(null);
    }

    private Set<String> getUnknownExtensions(Set<String> inLoader, Set<String> inProject) {
        Set<String> unknown = MakeProject.createExtensionSet();
        for (String extension : inProject) {
            if (extension.length() > 0) {
                if (!inLoader.contains(extension)) {
                    unknown.add(extension);
                }
            }
        }
        return unknown;
    }

    private void checkNeededExtensions() {
        if (UNIT_TEST_MODE || CndUtils.isStandalone()) {
            return;
        }
        Set<String> unknownC = getUnknownExtensions(MakeProject.getCSuffixes(), cExtensions);
        Set<String> unknownCpp = getUnknownExtensions(MakeProject.getCppSuffixes(), cppExtensions);
        Set<String> unknownH = getUnknownExtensions(MakeProject.getHeaderSuffixes(), headerExtensions);
        if (!unknownC.isEmpty() && unknownCpp.isEmpty() && unknownH.isEmpty()) {
            if (unknownC.size() > 0 && addNewExtensionDialog(unknownC, "C")) { // NOI18N
                addMIMETypeExtensions(unknownC, MIMENames.C_MIME_TYPE);
            }
        } else if (unknownC.isEmpty() && !unknownCpp.isEmpty() && unknownH.isEmpty()) {
            if (addNewExtensionDialog(unknownCpp, "CPP")) { // NOI18N
                addMIMETypeExtensions(unknownCpp, MIMENames.CPLUSPLUS_MIME_TYPE);
            }
        } else if (unknownC.isEmpty() && unknownCpp.isEmpty() && !unknownH.isEmpty()) {
            if (addNewExtensionDialog(unknownH, "H")) { // NOI18N
                addMIMETypeExtensions(unknownH, MIMENames.HEADER_MIME_TYPE);
            }
        } else if (!(unknownC.isEmpty() && unknownCpp.isEmpty() && unknownH.isEmpty())) {
            ConfirmExtensions panel = new ConfirmExtensions(unknownC, unknownCpp, unknownH);
            DialogDescriptor dialogDescriptor = new DialogDescriptor(panel,
                    getString("ConfirmExtensions.dialog.title")); // NOI18N
            DialogDisplayer.getDefault().notify(dialogDescriptor);
            if (dialogDescriptor.getValue() == DialogDescriptor.OK_OPTION) {
                if (panel.isC()) {
                    addMIMETypeExtensions(unknownC, MIMENames.C_MIME_TYPE);
                }
                if (panel.isCpp()) {
                    addMIMETypeExtensions(unknownCpp, MIMENames.CPLUSPLUS_MIME_TYPE);
                }
                if (panel.isHeader()) {
                    addMIMETypeExtensions(unknownH, MIMENames.HEADER_MIME_TYPE);
                }
            }
        }
    }

    public void updateExtensions(Set<String> cSet, Set<String> cppSet, Set<String> hSet) {
        cExtensions.clear();
        cExtensions.addAll(cSet);
        cppExtensions.clear();
        cppExtensions.addAll(cppSet);
        headerExtensions.clear();
        headerExtensions.addAll(hSet);
        saveAdditionalExtensions();
    }

    private synchronized void registerClassPath(boolean register) {
        if (isOpenHookDone) {
            if (register) {
                GlobalPathRegistry.getDefault().register(MakeProjectPaths.SOURCES, sourcepath.getClassPath());
            } else {
                try {
                    GlobalPathRegistry.getDefault().unregister(MakeProjectPaths.SOURCES, sourcepath.getClassPath());
                } catch (Throwable ex) {
                    // do nothing because register depends on make options
                }
            }
        }
    }

    private void saveAdditionalExtensions() {
        Element data = helper.getPrimaryConfigurationData(true);
        boolean changed = false;
        changed |= saveAdditionalHeaderExtensions(data, MakeProject.C_EXTENSIONS, cExtensions);
        changed |= saveAdditionalHeaderExtensions(data, MakeProject.CPP_EXTENSIONS, cppExtensions);
        changed |= saveAdditionalHeaderExtensions(data, MakeProject.HEADER_EXTENSIONS, headerExtensions);
        if (changed) {
            helper.putPrimaryConfigurationData(data, true);
        }
    }

    private boolean saveAdditionalHeaderExtensions(Element data, String key, Set<String> set) {
        StringBuilder buf = new StringBuilder();
        for (String e : set) {
            if (buf.length() > 0) {
                buf.append(',');
            }
            buf.append(e);
        }
        String newText = buf.toString();
        Element element;
        NodeList nodeList = data.getElementsByTagName(key);
        if (nodeList.getLength() == 1) {
            element = (Element) nodeList.item(0);
            NodeList deadKids = element.getChildNodes();
            if (deadKids.getLength() == 1) {
                String text = deadKids.item(0).getTextContent();
                if (text.equals(newText)) {
                    return false;
                }
            } else if (deadKids.getLength() == 0) {
                if(newText.isEmpty()) {
                    return false;
                }
            }
            while (deadKids.getLength() > 0) {
                element.removeChild(deadKids.item(0));
            }
        } else {
            element = data.getOwnerDocument().createElementNS(MakeProjectTypeImpl.PROJECT_CONFIGURATION_NAMESPACE, key);
            data.appendChild(element);
        }
        element.appendChild(data.getOwnerDocument().createTextNode(buf.toString()));
        return true;
    }

    private boolean addNewExtensionDialog(Set<String> usedExtension, String type) {
        if (UNIT_TEST_MODE || CndUtils.isStandalone()) {
            return true;
        }
        String message = getString("ADD_EXTENSION_QUESTION" + type + (usedExtension.size() == 1 ? "" : "S")); // NOI18N
        StringBuilder extensions = new StringBuilder();
        for (String ext : usedExtension) {
            if (extensions.length() > 0) {
                extensions.append(',');
            }
            extensions.append(ext);
        }
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(
                MessageFormat.format(message, new Object[]{extensions.toString()}),
                getString("ADD_EXTENSION_DIALOG_TITLE" + type + (usedExtension.size() == 1 ? "" : "S")), // NOI18N
                NotifyDescriptor.YES_NO_OPTION);
        return DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.YES_OPTION;
    }

    public static Set<String> createExtensionSet() {
        if (CndFileUtils.isSystemCaseSensitive()) {
            return new TreeSet<String>();
        } else {
            return new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        }
    }

    private static Set<String> getSourceSuffixes() {
        Set<String> suffixes = createExtensionSet();
        suffixes.addAll(MIMEExtensions.get(MIMENames.CPLUSPLUS_MIME_TYPE).getValues());
        suffixes.addAll(MIMEExtensions.get(MIMENames.C_MIME_TYPE).getValues());
        return suffixes;
    }

    private static Set<String> getCSuffixes() {
        Set<String> suffixes = createExtensionSet();
        suffixes.addAll(MIMEExtensions.get(MIMENames.C_MIME_TYPE).getValues());
        return suffixes;
    }

    private static Set<String> getCppSuffixes() {
        Set<String> suffixes = createExtensionSet();
        suffixes.addAll(MIMEExtensions.get(MIMENames.CPLUSPLUS_MIME_TYPE).getValues());
        return suffixes;
    }

    private static Set<String> getHeaderSuffixes() {
        Set<String> suffixes = createExtensionSet();
        suffixes.addAll(MIMEExtensions.get(MIMENames.HEADER_MIME_TYPE).getValues());
        return suffixes;
    }

    private static String getString(String s) {
        return NbBundle.getMessage(MakeProject.class, s);
    }

    // Package private methods -------------------------------------------------
    private static final class RecommendedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates {

        private static final String[] RECOMMENDED_TYPES = new String[]{
            "c-types", // NOI18N
            "cpp-types", // NOI18N
            "shell-types", // NOI18N
            "makefile-types", // NOI18N
            "simple-files", // NOI18N
            "fortran-types", // NOI18N
            "asm-types", // NOI18N
            "qt-types", // NOI18N
            "cncpp-test-types"}; // NOI18N
        private static final String[] PRIVILEGED_NAMES = new String[]{
            "Templates/cFiles/main.c", // NOI18N
            "Templates/cFiles/file.c", // NOI18N
            "Templates/cFiles/file.h", // NOI18N
            "Templates/cppFiles/class.cc", // NOI18N
            "Templates/cppFiles/main.cc", // NOI18N
            "Templates/cppFiles/file.cc", // NOI18N
            "Templates/cppFiles/file.h", // NOI18N
            "Templates/fortranFiles/fortranFreeFormatFile.f90", // NOI18N
            "Templates/MakeTemplates/ComplexMakefile", // NOI18N
            "Templates/MakeTemplates/SimpleMakefile/ExecutableMakefile", // NOI18N
            "Templates/MakeTemplates/SimpleMakefile/SharedLibMakefile", // NOI18N
            "Templates/MakeTemplates/SimpleMakefile/StaticLibMakefile"}; // NOI18N
        private static final String[] PRIVILEGED_NAMES_QT = new String[]{
            // Qt-specific templates fist:
            "Templates/qtFiles/main.cc", // NOI18N
            "Templates/qtFiles/form.ui", // NOI18N
            "Templates/qtFiles/resource.qrc", // NOI18N
            "Templates/qtFiles/translation.ts", // NOI18N
            // the rest is exact copy of PRIVILEGED_NAMES:
            "Templates/cFiles/main.c", // NOI18N
            "Templates/cFiles/file.c", // NOI18N
            "Templates/cFiles/file.h", // NOI18N
            "Templates/cppFiles/class.cc", // NOI18N
            "Templates/cppFiles/main.cc", // NOI18N
            "Templates/cppFiles/file.cc", // NOI18N
            "Templates/cppFiles/file.h", // NOI18N
            "Templates/fortranFiles/fortranFreeFormatFile.f90", // NOI18N
            "Templates/MakeTemplates/ComplexMakefile", // NOI18N
            "Templates/MakeTemplates/SimpleMakefile/ExecutableMakefile", // NOI18N
            "Templates/MakeTemplates/SimpleMakefile/SharedLibMakefile", // NOI18N
            "Templates/MakeTemplates/SimpleMakefile/StaticLibMakefile"}; // NOI18N
        private final ConfigurationDescriptorProvider configurationProvider;

        public RecommendedTemplatesImpl(ConfigurationDescriptorProvider configurationProvider) {
            this.configurationProvider = configurationProvider;
        }

        @Override
        public String[] getRecommendedTypes() {
            return RECOMMENDED_TYPES;
        }

        @Override
        public String[] getPrivilegedTemplates() {
            if (configurationProvider.gotDescriptor()) {
                MakeConfigurationDescriptor configurationDescriptor = configurationProvider.getConfigurationDescriptor(false);
                if (configurationDescriptor != null) {
                    MakeConfiguration conf = configurationDescriptor.getActiveConfiguration();
                    if (conf != null && conf.isQmakeConfiguration()) {
                        return PRIVILEGED_NAMES_QT;
                    }
                }
            }
            return PRIVILEGED_NAMES;
        }
    }

    /*
     * Return source encoding if in project.xml (only project version >= 50)
     */
    public String getSourceEncodingFromProjectXml() {
        Element data = helper.getPrimaryConfigurationData(true);

        NodeList nodeList = data.getElementsByTagName(MakeProjectTypeImpl.SOURCE_ENCODING_TAG);
        if (nodeList != null && nodeList.getLength() > 0) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                return node.getTextContent();
            }
        }

        return null;
    }

    public String getSourceEncoding() {
        if (sourceEncoding == null) {
            // Read configurations.xml. That's where encoding is stored for project version < 50)
            if (!projectDescriptorProvider.gotDescriptor()) {
                return FileEncodingQuery.getDefaultEncoding().name();
            }
        }
        if (sourceEncoding == null) {
            sourceEncoding = FileEncodingQuery.getDefaultEncoding().name();
        }
        return sourceEncoding;
    }

    public void setSourceEncoding(String sourceEncoding) {
        this.sourceEncoding = sourceEncoding;
    }
    
    private MakeProjectCustomizer getProjectCustomizer(String customizerId) {
        if (customizerId == null) {
            return null;
        }
        MakeProjectCustomizer makeProjectCustomizer = null;
        Collection<? extends MakeProjectCustomizer> mwc = Lookup.getDefault().lookupAll(MakeProjectCustomizer.class);
        for (MakeProjectCustomizer instance : mwc) {
            if (customizerId.equals(instance.getCustomizerId())) {
                makeProjectCustomizer = instance;
                break;
            }
        }
        return makeProjectCustomizer;
    }
    
    private String getProjectCustomizerId() {
        String id = getCustomizerIdFromProjectXML();
        if (id == null) {
            FileObject[] children = getProjectDirectory().getChildren();
            for (FileObject c : children) {
                String name = c.getName();
                String ext = c.getExt();
                if (name.equals("cndcustomizerid")) { // NOI18N
                    id = ext;
                    break;
                }
            }
        }
        //System.out.println("getProjectCustomizerId " + id);
        return id;
    }

    /**
     * @return 
     */
    private String getCustomizerIdFromProjectXML() {
        Element data = helper.getPrimaryConfigurationData(true);
        NodeList nodeList = data.getElementsByTagName(MakeProjectTypeImpl.CUSTOMIZERID_ELEMENT);
        if (nodeList != null && nodeList.getLength() > 0) {
            Node typeNode = nodeList.item(0).getFirstChild();
            if (typeNode != null) {
                String type = typeNode.getNodeValue();
                return type;
            }
        }
        return null;
    }

    /**
     * @return active configuration type (doesn't force reading configuration metadata)
     * If metadata already read, get type from the active configuration (it may have changed)
     * If not read, try private.xml (V >= V77)
     * If not found, try project.xml (V >= V78)
     */
    private int getActiveConfigurationType() {
        // If configurations already read, get it from active configuration (it may have changed)
        MakeConfiguration makeConfiguration = getActiveConfiguration();
        if (makeConfiguration != null) {
            return makeConfiguration.getConfigurationType().getValue();
        }

        // Get it from private.xml (version >= V77)
        int type = getActiveConfigurationTypeFromPrivateXML();
        if (type >= 0) {
            return type;
        }

        // Get it from project.xml (version >= V77)
        type = getActiveConfigurationTypeFromProjectXML();
        if (type >= 0) {
            return type;
        }

        return type;
    }

    /**
     * @return active configuration type (doesn't force reading configuration metadata) (V >= V78). Returns -1 if not found.
     */
    private int getActiveConfigurationTypeFromProjectXML() {
        Element data = helper.getPrimaryConfigurationData(true);
        NodeList nodeList = data.getElementsByTagName(MakeProjectTypeImpl.CONFIGURATION_TYPE_ELEMENT);
        if (nodeList != null && nodeList.getLength() > 0) {
            Node typeNode = nodeList.item(0).getFirstChild();
            if (typeNode != null) {
                String type = typeNode.getNodeValue();
                try {
                    return new Integer(type).intValue();
                } catch (NumberFormatException nfe) {
                }
            }
        }
        return -1;
    }

    /**
     * @return active configuration type from private.xml (doesn't force reading configuration metadata) (V >= V77). Returns -1 if not found.
     */
    private int getActiveConfigurationTypeFromPrivateXML() {
        Element data = helper.getPrimaryConfigurationData(false);
        NodeList nodeList = data.getElementsByTagName(MakeProjectTypeImpl.ACTIVE_CONFIGURATION_TYPE_ELEMENT);
        if (nodeList != null && nodeList.getLength() > 0) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                return new Integer(node.getTextContent()).intValue();
            }
        }
        return -1;
    }

    /**
     * @return active configuration index (doesn't force reading configuration metadata) from private.xml, if exists. Returns -1 if not found.
     */
    public int getActiveConfigurationIndexFromPrivateXML() {
        // Get it from xml (version >= V77)
        Element data = helper.getPrimaryConfigurationData(false);

        NodeList nodeList = data.getElementsByTagName(MakeProjectTypeImpl.ACTIVE_CONFIGURATION_INDEX_ELEMENT);
        if (nodeList != null && nodeList.getLength() > 0) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                return new Integer(node.getTextContent()).intValue();
            }
        }

        return -1;
    }

//    private void dumpNode(Node node, int indent) {
//        System.out.print("            ".subSequence(0, indent));
//        System.out.println("-----------------------------");
//        System.out.print("            ".subSequence(0, indent));
//        System.out.println("nodeName    " + node.getNodeName());
//        System.out.print("            ".subSequence(0, indent));
//        System.out.println("nodeValue   " + node.getNodeValue());
//        System.out.print("            ".subSequence(0, indent));
//        System.out.println("localName   " + node.getLocalName());
//        System.out.print("            ".subSequence(0, indent));
//        System.out.println("prefix      " + node.getPrefix());
////        System.out.print("            ".subSequence(0, indent));
////        System.out.println("textContent " + node.getTextContent());
//        NodeList nodeList = node.getChildNodes();
//        for (int i = 0; i < nodeList.getLength(); i++) {
//            dumpNode(nodeList.item(i), indent+2);
//        }
//    }
    /** NPE-safe method for getting active configuration */
    public MakeConfiguration getActiveConfiguration() {
        if (projectDescriptorProvider.gotDescriptor()) {
            MakeConfigurationDescriptor projectDescriptor = projectDescriptorProvider.getConfigurationDescriptor();
            if (projectDescriptor != null) {
                return projectDescriptor.getActiveConfiguration();
            }
        }
        return null;
    }

    private static final Pattern VALID_PROPERTY_NAME = Pattern.compile("[-._a-zA-Z0-9]+"); // NOI18N

    /**
     * Checks whether the name is usable as Ant property name.
     * @param name name to check for usability as Ant property
     * @return true if name is usable otherwise false
     */
    private static boolean isUsablePropertyName(String name) {
        return VALID_PROPERTY_NAME.matcher(name).matches();
    }

    /**
     * Returns name usable as Ant property which is based on the given
     * name. All forbidden characters are either removed or replaced with
     * suitable ones.
     * @param name name to use as base for Ant property name
     * @return name usable as Ant property name
     */
    private static String getUsablePropertyName(String name) {
        if (isUsablePropertyName(name)) {
            return name;
        }
        StringBuilder sb = new StringBuilder(name);
        for (int i=0; i<sb.length(); i++) {
            if (!isUsablePropertyName(sb.substring(i,i+1))) {
                sb.replace(i,i+1,"_"); // NOI18N
            }
        }
        return sb.toString();
    }


    // Private innerclasses ----------------------------------------------------

    /*
    private class CustomActionsHookImpl implements CustomActionsHook {
    private Vector customActions = null;
    public CustomActionsHookImpl() {
    customActions = new Vector();
    }
    public void addCustomAction(Action action) {
    synchronized (customActions) {
    customActions.add(action);
    }
    }
    public void removeCustomAction(Action action) {
    synchronized (customActions) {
    customActions.add(action);
    }
    }
    public Vector getCustomActions() {
    return customActions;
    }
    }
     */
    private class MakeSubprojectProvider implements SubprojectProvider {

        // Add a listener to changes in the set of subprojects.
        @Override
        public void addChangeListener(ChangeListener listener) {
        }

        // Get a set of projects which this project can be considered to depend upon somehow.
        @Override
        public Set<Project> getSubprojects() {
            Set<Project> subProjects = new HashSet<Project>();
            Set<String> subProjectLocations = new HashSet<String>();

            // Try project.xml first if project not already read (this is cheap)
            Element data = helper.getPrimaryConfigurationData(true);
            if (!projectDescriptorProvider.gotDescriptor() && data.getElementsByTagName(MakeProjectTypeImpl.MAKE_DEP_PROJECTS).getLength() > 0) {
                NodeList nl4 = data.getElementsByTagName(MakeProjectTypeImpl.MAKE_DEP_PROJECT);
                if (nl4.getLength() > 0) {
                    for (int i = 0; i < nl4.getLength(); i++) {
                        Node node = nl4.item(i);
                        NodeList nl2 = node.getChildNodes();
                        for (int j = 0; j < nl2.getLength(); j++) {
                            String typeTxt = nl2.item(j).getNodeValue();
                            subProjectLocations.add(typeTxt);
                        }
                    }
                }
            } else {
                // Then read subprojects from configuration.zml (expensive)
                ConfigurationDescriptor projectDescriptor = projectDescriptorProvider.getConfigurationDescriptor();
                if (projectDescriptor == null) {
                    // Something serious wrong. Return nothing...
                    return subProjects;
                }
                subProjectLocations = ((MakeConfigurationDescriptor) projectDescriptor).getSubprojectLocations();
            }

            FileObject baseDir = getProjectDirectory();
            for (String loc : subProjectLocations) {
                String location = CndPathUtilitities.toAbsolutePath(baseDir, loc);
                try {
		    FileObject fo = RemoteFileUtil.getFileObject(baseDir, location);
                    if (fo != null && fo.isValid()) {
                        fo = CndFileUtils.getCanonicalFileObject(fo);
                    }
                    if (fo != null && fo.isValid()) {
                        Project project = ProjectManager.getDefault().findProject(fo);
                        if (project != null) {
                            subProjects.add(project);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Cannot find subproject in '" + location + "' " + e); // FIXUP // NOI18N
                }
            }

            return subProjects;
        }

        //Remove a listener to changes in the set of subprojects.
        @Override
        public void removeChangeListener(ChangeListener listener) {
        }
    }
    /**
     * if specified => project name will have information about directory in project view
     */
    private final static String PROJECT_NAME_WITH_HIDDEN_PATHS = System.getProperty("cnd.project.name.hidden.paths"); //NOI18N
    private final static int PROJECT_NAME_NUM_SHOWN_FOLDERS = Integer.getInteger("cnd.project.name.folders.num", 1); //NOI18N

    interface InfoInterface extends ProjectInformation, PropertyChangeListener {

        void firePropertyChange(String prop);

        void setName(String name);
    }

    private final class Info implements InfoInterface {

        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private String name;

        Info() {
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE.equals(evt.getPropertyName())) {
                firePropertyChange(ProjectInformation.PROP_ICON);
            }
        }

        @Override
        public void firePropertyChange(String prop) {
            if (ProjectInformation.PROP_NAME.equals(prop)) {
                name = null;
            }
            pcs.firePropertyChange(prop, null, null);
        }

        @Override
        public String getName() {
            return MakeProject.getUsablePropertyName(_getName());
        }

        /** Return configured project name. */
        private String _getName() {
            String res = name;
            if (res == null) {
                res = getNameImpl();
                name = res;
            }
            return res;
        }

        /** Return configured project name. */
        private String getNameImpl() {
            return ProjectManager.mutex().readAccess(new Mutex.Action<String>() {

                @Override
                public String run() {
                    Element data = helper.getPrimaryConfigurationData(true);
                    Element nameEl =  getNameElement(data);
                    if (nameEl != null) {
                        NodeList nl = nameEl.getChildNodes();
                        if (nl.getLength() == 1 && nl.item(0).getNodeType() == Node.TEXT_NODE) {
                            return ((Text) nl.item(0)).getNodeValue();
                        }
                    }
                    FileObject fo = MakeProject.this.getProjectDirectory();
                    if (fo != null && fo.isValid()) {
                        return fo.getNameExt();
                    }
                    return "???"; // NOI18N
                }
            });
        }

        @Override
        public void setName(final String name) {
            ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {

                @Override
                public Void run() {
                    Element data = helper.getPrimaryConfigurationData(true);
                    Element nameEl =  getNameElement(data);
                    if (nameEl != null) {
                        NodeList deadKids = nameEl.getChildNodes();
                        while (deadKids.getLength() > 0) {
                            nameEl.removeChild(deadKids.item(0));
                        }
                    } else {
                        nameEl = data.getOwnerDocument().createElementNS(MakeProjectTypeImpl.PROJECT_CONFIGURATION_NAMESPACE, MakeProjectTypeImpl.PROJECT_CONFIGURATION__NAME_NAME);
                        data.insertBefore(nameEl, data.getChildNodes().item(0));
                    }
                    nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                    helper.putPrimaryConfigurationData(data, true);
                    return null;
                }
            });
            // reinit cache
            _getName();
        }

        private Element getNameElement(Element data) {
            NodeList nl = data.getElementsByTagNameNS(MakeProjectTypeImpl.PROJECT_CONFIGURATION_NAMESPACE, MakeProjectTypeImpl.PROJECT_CONFIGURATION__NAME_NAME);
            if (nl.getLength() > 0) {
                for(int i = 0; i < nl.getLength(); i++) {
                    if (nl.item(i) instanceof Element) {
                        Element e = (Element) nl.item(i);
                        Node parentNode = e.getParentNode();
                        if (parentNode != null && parentNode.getLocalName().equals(MakeProjectTypeImpl.PROJECT_CONFIGURATION_NAME)) {
                            return e;
                        }
                    }
                }
            }
            return null;
        }
        
        @Override
        public String getDisplayName() {
            String aName = _getName();

            if (PROJECT_NAME_WITH_HIDDEN_PATHS != null) {
                FileObject fo = MakeProject.this.getProjectDirectory();
                if (fo != null && fo.isValid()) {
                    String prjDirDispName = FileUtil.getFileDisplayName(fo);
                    String[] split = PROJECT_NAME_WITH_HIDDEN_PATHS.split(":"); // NOI18N
                    for (String skipPath : split) {
                        if (prjDirDispName.startsWith(skipPath)) {
                            prjDirDispName = prjDirDispName.substring(skipPath.length());
                            break;
                        }
                    }
                    if (prjDirDispName.startsWith("/") || prjDirDispName.startsWith("\\")) { // NOI18N
                        prjDirDispName = prjDirDispName.substring(1);
                    }
                    int sep = 0;
                    for (int i = 0; i < PROJECT_NAME_NUM_SHOWN_FOLDERS; i++) {
                        int nextSep = prjDirDispName.indexOf('\\', sep);
                        nextSep = (nextSep == -1) ? prjDirDispName.indexOf('/', sep) : nextSep;
                        if (nextSep > 0) {
                            sep = nextSep + 1;
                        } else {
                            // name has less elements than asked
                            sep = prjDirDispName.length();
                            break;
                        }
                    }
                    if (sep > 0) {
                        prjDirDispName = prjDirDispName.substring(0, sep);
                    }
                    if (prjDirDispName.length() > 0) {
                        if (prjDirDispName.endsWith("/") || prjDirDispName.endsWith("\\")) { // NOI18N
                            prjDirDispName = prjDirDispName.substring(0, prjDirDispName.length() - 1);
                        }
                        aName = NbBundle.getMessage(getClass(), "PRJ_DISPLAY_NAME_WITH_FOLDER", aName, prjDirDispName); // NOI18N
                    }
                }
            }
//            if (OpenProjects.getDefault().isProjectOpen(MakeProject.this)){
//                DevelopmentHostConfiguration devHost = getDevelopmentHostConfiguration();
//                if (devHost != null && ! devHost.isLocalhost()) {
//                    name = NbBundle.getMessage(getClass(), "PRJ_DISPLAY_NAME",
//                            name, devHost.getHostDisplayName(false));
//                }
//            }
            return aName;
        }

        private int getProjectType() {
            int aProjectType = getActiveConfigurationType();
            if (aProjectType != -1) {
                return aProjectType;
            }
            return -1;
        }

        @Override
        public Icon getIcon() {
            int type = getProjectType();
            Icon icon = MakeConfigurationDescriptor.MAKEFILE_ICON;
            switch (type) {
                case MakeConfiguration.TYPE_MAKEFILE: {
                    MakeConfiguration activeConfiguration = MakeProject.this.getActiveConfiguration();
                    if (activeConfiguration != null) {
                        String outputValue = activeConfiguration.getOutputValue();
                        if (outputValue != null) {
                            if (outputValue.endsWith(".so") || // NOI18N
                                    outputValue.endsWith(".dll") || // NOI18N
                                    outputValue.endsWith(".dylib")) { // NOI18N
                                icon = ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/makeproject/ui/resources/projects-unmanaged-dynamic.png", false); // NOI18N
                            } else if (outputValue.endsWith(".a")) { // NOI18N
                                icon = ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/makeproject/ui/resources/projects-unmanaged-static.png", false); // NOI18N
                            } else {
                                icon = ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/makeproject/ui/resources/projects-unmanaged.png", false); // NOI18N
                            }
                        } else {
                            icon = ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/makeproject/ui/resources/projects-unmanaged.png", false); // NOI18N
                        }
                    }
                    break;
                }
                case MakeConfiguration.TYPE_APPLICATION:
                    icon = ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/makeproject/ui/resources/projects-managed.png", false); // NOI18N
                    break;
                case MakeConfiguration.TYPE_DB_APPLICATION:
                    icon = ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/makeproject/ui/resources/projects-database.png", false); // NOI18N
                    break;
                case MakeConfiguration.TYPE_DYNAMIC_LIB:
                    icon = ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/makeproject/ui/resources/projects-managed-dynamic.png", false); // NOI18N
                    break;
                case MakeConfiguration.TYPE_STATIC_LIB:
                    icon = ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/makeproject/ui/resources/projects-managed-static.png", false); // NOI18N
                    break;
                case MakeConfiguration.TYPE_QT_APPLICATION:
                    icon = ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/makeproject/ui/resources/projects-Qt.png", false); // NOI18N
                    break;
                case MakeConfiguration.TYPE_QT_DYNAMIC_LIB:
                    icon = ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/makeproject/ui/resources/projects-Qt-dynamic.png", false); // NOI18N
                    break;
                case MakeConfiguration.TYPE_QT_STATIC_LIB:
                    icon = ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/makeproject/ui/resources/projects-Qt-static.png", false); // NOI18N
                    break;
                case MakeConfiguration.TYPE_CUSTOM:
                    MakeProjectCustomizer makeProjectCustomizer = getProjectCustomizer(getProjectCustomizerId());
                    if (makeProjectCustomizer != null) {
                        icon = ImageUtilities.loadImageIcon(makeProjectCustomizer.getIconPath(), false); // NOI18N
                    }
                    break;
            }
            return icon;
        }
        
//        private String getProjectCustomizerId() {
//            return getCustomizerIdFromProjectXML();
//        }
//        
//        private MakeProjectCustomizer getProjectCustomizer(String customizerId) {
//            MakeProjectCustomizer makeProjectCustomizer = null;
//            Collection<? extends MakeProjectCustomizer> mwc = Lookup.getDefault().lookupAll(MakeProjectCustomizer.class);
//            for (MakeProjectCustomizer instance : mwc) {
//                if (customizerId.equals(instance.getCustomizerId())) {
//                    makeProjectCustomizer = instance;
//                    break;
//                }
//            }
//            return makeProjectCustomizer;
//        }

        @Override
        public Project getProject() {
            return MakeProject.this;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }
    }

    private List<Runnable> openedTasks;

    public void addOpenedTask(Runnable task) {
        if (openedTasks == null) {
            openedTasks = new ArrayList<Runnable>();
        }
        openedTasks.add(task);
    }

    @Override
    public void run() {
        // This is ugly solution introduced for waiting finished opened tasks in discovery module.
        // see method org.netbeans.modules.cnd.discovery.projectimport.ImportProject.doWork().
        // TODO: refactor this solution
        onProjectOpened();
    }

    private synchronized void onProjectOpened() {
        if (!isOpenHookDone) {
            FileObject dir = getProjectDirectory();
            if (dir != null) { // high resistance mode paranoia
                final ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(dir);
                ConnectionHelper.INSTANCE.ensureConnection(env);
            }            
            helper.addMakeProjectListener(MakeProject.this);
            checkNeededExtensions();
            if (openedTasks != null) {
                for (Runnable runnable : openedTasks) {
                    runnable.run();
                }
                openedTasks.clear();
                openedTasks = null;
            }
            isOpenHookDone = true;
            if (MakeOptions.getInstance().isFullFileIndexer()) {
                registerClassPath(true);
            }
            MakeOptions.getInstance().addPropertyChangeListener(indexerListener);
            RP.post(new Runnable() {
                @Override
                public void run() {
                    projectDescriptorProvider.getConfigurationDescriptor(true);
                    if(nativeProject instanceof NativeProjectProvider) {
                        NativeProjectRegistry.getDefault().register(nativeProject);
                    }
                }
            });
        }
    }

    void setDeleted() {
        LOGGER.log(Level.FINE, "set deleted MakeProject@{0} {1}", new Object[]{System.identityHashCode(MakeProject.this), helper.getProjectDirectory().getNameExt()}); // NOI18N
        isDeleted.set(true);
    }

    void setDeleting(boolean value) {
        LOGGER.log(Level.FINE, "set deleting MakeProject@{0} {1}", new Object[]{System.identityHashCode(MakeProject.this), helper.getProjectDirectory().getNameExt()}); // NOI18N
        isDeleting.set(value);
    }

    private synchronized void onProjectClosed() {
        LOGGER.log(Level.FINE, "on project close MakeProject@{0} {1}", new Object[]{System.identityHashCode(MakeProject.this), helper.getProjectDirectory().getNameExt()}); // NOI18N
        helper.removeMakeProjectListener(this);
        save();
        if (projectDescriptorProvider.getConfigurationDescriptor() != null) {
            projectDescriptorProvider.getConfigurationDescriptor().closed();
        }
        MakeOptions.getInstance().removePropertyChangeListener(indexerListener);
        if (isOpenHookDone) {
            registerClassPath(false);
            isOpenHookDone = false;
        }
        MakeProjectFileProviderFactory.removeSearchBase(this);
        if(nativeProject instanceof NativeProjectProvider) {
            NativeProjectRegistry.getDefault().unregister(nativeProject);
        }
    }

    public synchronized void save() {
        if (!isDeleted.get() && !isDeleting.get()) {
            if (projectDescriptorProvider.getConfigurationDescriptor() != null) {
                projectDescriptorProvider.getConfigurationDescriptor().save();
            }
        }
    }

    private final class ProjectOpenedHookImpl extends ProjectOpenedHook {

        ProjectOpenedHookImpl() {
        }

        @Override
        protected void projectOpened() {
            onProjectOpened();
        }

        @Override
        protected void projectClosed() {
            onProjectClosed();
        }
    }

    private final class MakeArtifactProviderImpl implements MakeArtifactProvider {

        @Override
        public MakeArtifact[] getBuildArtifacts() {
            List<MakeArtifact> artifacts = new ArrayList<MakeArtifact>();

            MakeConfigurationDescriptor projectDescriptor = projectDescriptorProvider.getConfigurationDescriptor();
            if (projectDescriptor != null) {
                Configuration[] confs = projectDescriptor.getConfs().toArray();
                for (int i = 0; i < confs.length; i++) {
                    MakeConfiguration makeConfiguration = (MakeConfiguration) confs[i];
                    artifacts.add(new MakeArtifact(projectDescriptor, makeConfiguration));
                }
            }
            return artifacts.toArray(new MakeArtifact[artifacts.size()]);
        }
    }

    private static class MakeProjectSearchInfo extends SearchInfoDefinition {

        private ConfigurationDescriptorProvider projectDescriptorProvider;

        MakeProjectSearchInfo(ConfigurationDescriptorProvider projectDescriptorProvider) {
            this.projectDescriptorProvider = projectDescriptorProvider;
        }

        @Override
        public boolean canSearch() {
            return true;
        }

        @Override
        public List<SearchRoot> getSearchRoots() {
            List<SearchRoot> roots = new ArrayList<SearchRoot>();
            FileObject baseDirFileObject = projectDescriptorProvider.getConfigurationDescriptor().getBaseDirFileObject();
            roots.add(new SearchRoot(baseDirFileObject, null));
            for (String root : projectDescriptorProvider.getConfigurationDescriptor().getAbsoluteSourceRoots()) {
                try {
                    FileObject fo = new FSPath(baseDirFileObject.getFileSystem(), root).getFileObject();
                    if (fo != null) {
                        roots.add(new SearchRoot(fo, null));
                    }
                } catch (FileStateInvalidException ex) {
                }
            }
            return roots;
        }

        @Override
        public Iterator<FileObject> filesToSearch(final SearchScopeOptions options, SearchListener listener, final AtomicBoolean terminated) {
            FileObjectNameMatcherImpl matcher = new FileObjectNameMatcherImpl(options, terminated);
            MakeConfigurationDescriptor projectDescriptor = projectDescriptorProvider.getConfigurationDescriptor();
            Folder rootFolder = projectDescriptor.getLogicalFolders();
            Set<FileObject> res = rootFolder.getAllItemsAsFileObjectSet(false, matcher);
            FileObject baseDirFileObject = projectDescriptorProvider.getConfigurationDescriptor().getBaseDirFileObject();
            addFolder(res, baseDirFileObject.getFileObject("nbproject"), matcher); // NOI18N
            addFolder(res, baseDirFileObject.getFileObject("nbproject/private"), matcher); // NOI18N
            return res.iterator();
        }

        private void addFolder(Set<FileObject> res, FileObject fo, FileObjectNameMatcher matcher) {
            if (fo != null && fo.isFolder() && fo.isValid()) {
                if (matcher.isTerminated()) {
                    return;
                }
                for (FileObject f : fo.getChildren()) {
                    if (matcher.isTerminated()) {
                        return;
                    }
                    if (f.isData() && matcher.pathMatches(f)) {
                        res.add(f);
                    }
                }
            }
        }
    }

    /** NPE-safe method for getting active DevelopmentHostConfiguration */
    public DevelopmentHostConfiguration getDevelopmentHostConfiguration() {
        MakeConfiguration conf = getActiveConfiguration();
        if (conf != null) {
            return conf.getDevelopmentHost();
        }
        return null;
    }

    /** NPE-safe method for getting active ExecutionEnvironment */
    public ExecutionEnvironment getDevelopmentHostExecutionEnvironment() {
        DevelopmentHostConfiguration dc = getDevelopmentHostConfiguration();
        return (dc == null) ? null : dc.getExecutionEnvironment();
    }

    private class RemoteProjectImpl implements RemoteProject {
        
        @Override
        public ExecutionEnvironment getDevelopmentHost() {
            DevelopmentHostConfiguration devHost = getDevelopmentHostConfiguration();
            return (devHost == null) ? null : devHost.getExecutionEnvironment();
        }

        @Override
        public ExecutionEnvironment getSourceFileSystemHost() {
            if (remoteMode == RemoteProject.Mode.REMOTE_SOURCES) {
                return fileSystemHost;
            } else {
                return FileSystemProvider.getExecutionEnvironment(helper.getProjectDirectory());
            }
        }

        @Override
        public FileSystem getSourceFileSystem() {
            if (remoteMode == RemoteProject.Mode.REMOTE_SOURCES) {
                return FileSystemProvider.getFileSystem(fileSystemHost);
            } else {
                try {
                    return getProjectDirectory().getFileSystem();
                } catch (FileStateInvalidException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }

        
        @Override
        public Mode getRemoteMode() {
            return remoteMode;
        }

        @Override
        public RemoteSyncFactory getSyncFactory() {
            // TODO:fullRemote: think over, should mode be checked here?
            // Probably noit sice fixed factory is set to configurations in the cae of full remote
            switch (remoteMode) {
                case LOCAL_SOURCES:
                {
                    MakeConfiguration activeConfiguration = getActiveConfiguration();
                    if (activeConfiguration != null) {
                        if (CndFileUtils.isLocalFileSystem(activeConfiguration.getBaseFSPath().getFileSystem())) {
                            return activeConfiguration.getRemoteSyncFactory();                            
                        } else {
                            return RemoteSyncFactory.fromID(RemoteProject.FULL_REMOTE_SYNC_ID);
                        }
                    } else {
                        return null;
                    }
                }
                case REMOTE_SOURCES:
                    return RemoteSyncFactory.fromID(RemoteProject.FULL_REMOTE_SYNC_ID);
                default:
                {
                    CndUtils.assertTrue(false, "Unexpected remote mode " + remoteMode); //NOI18N
                    MakeConfiguration activeConfiguration = getActiveConfiguration();
                    if (activeConfiguration != null) {
                        return getActiveConfiguration().getRemoteSyncFactory();
                    } else {
                        return null;
                    }
                }
            }
        }

        @Override
        public String getSourceBaseDir() {
            return (remoteBaseDir == null) ? helper.getProjectDirectory().getPath() : remoteBaseDir;
        }

        @Override
        public FileObject getSourceBaseDirFileObject() {
            if (remoteMode == RemoteProject.Mode.REMOTE_SOURCES) {
                CndUtils.assertNotNull(remoteBaseDir, "Null remote base directory"); //NOI18N
                if (remoteBaseDir != null) {
                    return FileSystemProvider.getFileObject(fileSystemHost, remoteBaseDir);
                }
            }
            return getProjectDirectory();
        }

        @Override
        public String resolveRelativeRemotePath(String path) {
            if (!CndPathUtilitities.isPathAbsolute(path)) {
                if (remoteMode == RemoteProject.Mode.REMOTE_SOURCES && remoteBaseDir != null && !remoteBaseDir.isEmpty()) {
                    String resolved = remoteBaseDir;
                    if (!resolved.endsWith("/")) { //NOI18N
                        resolved += "/"; //NOI18N
                    }
                    resolved = resolved + path;
                    return CndFileUtils.normalizeAbsolutePath(getSourceFileSystem(), resolved);
                }
            }
            return path;
        }
    }

    private class ToolchainProjectImpl implements ToolchainProject {

        @Override
        public CompilerSet getCompilerSet() {
            MakeConfiguration conf = getActiveConfiguration();
            if (conf != null) {
                return conf.getCompilerSet().getCompilerSet();
            }
            return null;
        }
    }

    private static final class MutableCP implements ClassPathImplementation, ChangeListener {

        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private final MakeSources sources;
        private List<PathResourceImplementation> resources = null;
        private long eventId = 0;
        private ClassPath[] classpath = null;

        public MutableCP(MakeSources sources) {
            this.sources = sources;
            this.sources.addChangeListener(WeakListeners.change(MutableCP.this, this.sources));
        }

        @Override
        public List<? extends PathResourceImplementation> getResources() {
            final long currentEventId;
            synchronized (this) {
                if (resources != null) {
                    return resources;
                }
                currentEventId = eventId;
            }

            List<PathResourceImplementation> list = new LinkedList<PathResourceImplementation>();
            SourceGroup[] groups = sources.getSourceGroups("generic"); // NOI18N
            for (SourceGroup g : groups) {
                FileObject rootFolder = g.getRootFolder();
                URL url = rootFolder.toURL();
                // A workaround for #196328 - IllegalArgumentException on save Project properties
                if (rootFolder.isFolder() && !url.toExternalForm().endsWith("/")) { //NOI18N
                    try {
                        URL url2 = new URL(url.toExternalForm() + '/'); //NOI18N                     
                        FileObject fo = URLMapper.findFileObject(url2);
                        if (fo != null && fo.equals(rootFolder)) {
                            url = url2;
                        }                            
                    } catch (MalformedURLException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                // end of workaround for #196328
                list.add(new PathResourceImpl(ClassPathSupport.createResource(url)));
            }

            synchronized (this) {
                if (currentEventId == eventId) {
                    resources = list;
                }
            }
            return list;
        }
        
        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            synchronized (this) {
                resources = null;
                eventId++;
            }

            pcs.firePropertyChange(PROP_RESOURCES, null, null);
        }

        public synchronized ClassPath[] getClassPath() {
            if (classpath == null) {
                classpath = new ClassPath[]{ClassPathFactory.createClassPath(this)};
            }
            return classpath;
        }
    } // End of ClassPathImplementation class

    private static final class PathResourceImpl implements FilteringPathResourceImplementation, PropertyChangeListener {

        private final PathResourceImplementation delegate;

        public PathResourceImpl(PathResourceImplementation delegate) {
            this.delegate = delegate;
            this.delegate.addPropertyChangeListener(PathResourceImpl.this);
        }

        @Override
        public boolean includes(URL root, String resource) {
            return !CndFileVisibilityQuery.getDefault().isIgnored(resource);
        }

        @Override
        public URL[] getRoots() {
            return delegate.getRoots();
        }

        @Override
        public ClassPathImplementation getContent() {
            return delegate.getContent();
        }
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            pcs.firePropertyChange(new PropertyChangeEvent(this, evt.getPropertyName(), evt.getOldValue(), evt.getNewValue()));
        }
    }

    private static final class CPPImpl implements ClassPathProvider {

        private final MakeSources sources;

        public CPPImpl(MakeSources sources) {
            this.sources = sources;
        }

        @Override
        public ClassPath findClassPath(FileObject file, String type) {
            if (MakeProjectPaths.SOURCES.equals(type)) {
                for (SourceGroup sg : sources.getSourceGroups(MakeSources.GENERIC)) {
                    if (sg.getRootFolder().equals(file)) {
                        return ClassPathSupport.createClassPath(Arrays.asList(new PathResourceImpl(ClassPathSupport.createResource(file.toURL()))));
                    }
                }
            }

            return null;
        }
    }

    private final class IndexerOptionsListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (FullFileIndexer.FULL_FILE_INDEXER.equals(evt.getPropertyName())) {
                registerClassPath(Boolean.TRUE.equals(evt.getNewValue()));
            }
        }
    }
}
