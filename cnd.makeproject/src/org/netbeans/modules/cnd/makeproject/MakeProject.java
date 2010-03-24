/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.cnd.makeproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.spi.toolchain.ToolchainProject;
import org.netbeans.modules.cnd.api.remote.RemoteProject;
import org.netbeans.modules.cnd.api.utils.CndFileVisibilityQuery;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifactProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.MakeCustomizerProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.DevelopmentHostConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.ui.MakeLogicalViewProvider;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.FilteringPathResourceImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.support.ant.AntBasedProjectRegistration;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openidex.search.SearchInfo;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Represents one plain Make project.
 */
@AntBasedProjectRegistration(
    iconResource=MakeConfigurationDescriptor.ICON,
    type=MakeProjectType.TYPE,
    sharedNamespace=MakeProjectType.PROJECT_CONFIGURATION_NAMESPACE,
    privateNamespace=MakeProjectType.PRIVATE_CONFIGURATION_NAMESPACE
)
public final class MakeProject implements Project, AntProjectListener, Runnable {

    public static final boolean TRACE_MAKE_PROJECT_CREATION = Boolean.getBoolean("cnd.make.project.creation.trace"); // NOI18N
    private static final boolean UNIT_TEST_MODE = CndUtils.isUnitTestMode();

//    private static final Icon MAKE_PROJECT_ICON = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/makeProject.gif")); // NOI18N
    private static final String HEADER_EXTENSIONS = "header-extensions"; // NOI18N
    private static final String C_EXTENSIONS = "c-extensions"; // NOI18N
    private static final String CPP_EXTENSIONS = "cpp-extensions"; // NOI18N
    private static final String MAKE_PROJECT_TYPE = "make-project-type"; // NOI18N
    private static MakeTemplateListener templateListener = null;
    private final MakeProjectType kind;
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final ReferenceHelper refHelper;
    private final GeneratedFilesHelper genFilesHelper;
    private final Lookup lookup;
    private ConfigurationDescriptorProvider projectDescriptorProvider;
    private int projectType = -1;
    private Set<String> headerExtensions = MakeProject.createExtensionSet();
    private Set<String> cExtensions = MakeProject.createExtensionSet();
    private Set<String> cppExtensions = MakeProject.createExtensionSet();
    private String sourceEncoding = null;
    private boolean isOpenHookDone = false;
    private AtomicBoolean isDeleted = new AtomicBoolean(false);

    private final MakeSources sources;
    private final MutableCP sourcepath;

    public MakeProject(AntProjectHelper helper) throws IOException {
        if (TRACE_MAKE_PROJECT_CREATION){
            System.err.println("Start of creation MakeProject@"+System.identityHashCode(MakeProject.this)+" "+helper.getProjectDirectory().getName()); // NOI18N
        }
        this.kind = new MakeProjectType();
        this.helper = helper;
        eval = createEvaluator();
        AuxiliaryConfiguration aux = helper.createAuxiliaryConfiguration();
        refHelper = new ReferenceHelper(helper, aux, eval);
        projectDescriptorProvider = new ConfigurationDescriptorProvider(helper.getProjectDirectory());
        if (TRACE_MAKE_PROJECT_CREATION){
            System.err.println("Create ConfigurationDescriptorProvider@"+System.identityHashCode(projectDescriptorProvider)+" for MakeProject@"+System.identityHashCode(MakeProject.this)+" "+helper.getProjectDirectory().getName()); // NOI18N
        }
        genFilesHelper = new GeneratedFilesHelper(helper);
        sources = new MakeSources(this, helper);
        sourcepath = new MutableCP(sources);
        lookup = createLookup(aux);
        helper.addAntProjectListener(MakeProject.this);

        // Find the project type from project.xml
        Element data = helper.getPrimaryConfigurationData(true);
        NodeList nl = data.getElementsByTagName(MAKE_PROJECT_TYPE);
        if (nl.getLength() == 1) {
            nl = nl.item(0).getChildNodes();
            String typeTxt = nl.item(0).getNodeValue();
            projectType = new Integer(typeTxt).intValue();
        }

        readProjectExtension(data, HEADER_EXTENSIONS, headerExtensions);
        readProjectExtension(data, C_EXTENSIONS, cExtensions);
        readProjectExtension(data, CPP_EXTENSIONS, cppExtensions);
        sourceEncoding = getSourceEncodingFromProjectXml();

        if (templateListener == null) {
            DataLoaderPool.getDefault().addOperationListener(templateListener = new MakeTemplateListener());
        }
        if (TRACE_MAKE_PROJECT_CREATION){
            System.err.println("End of creation MakeProject@"+System.identityHashCode(MakeProject.this)+" "+helper.getProjectDirectory().getName()); // NOI18N
        }
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

    @Override
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }

    @Override
    public String toString() {
        return "MakeProject[" + getProjectDirectory() + "]"; // NOI18N
    }

    private PropertyEvaluator createEvaluator() {
        // XXX might need to use a custom evaluator to handle active platform substitutions... TBD
        return helper.getStandardPropertyEvaluator();
    }

    PropertyEvaluator evaluator() {
        return eval;
    }

    ReferenceHelper getReferenceHelper() {
        return this.refHelper;
    }

    public AntProjectHelper getAntProjectHelper() {
        return helper;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    private Lookup createLookup(AuxiliaryConfiguration aux) {
        SubprojectProvider spp = new MakeSubprojectProvider(); //refHelper.createSubprojectProvider();
        Lookup lkp = Lookups.fixed(new Object[]{
                    new Info(),
                    aux,
                    helper.createCacheDirectoryProvider(),
                    spp,
                    new MakeActionProvider(this),
                    new MakeLogicalViewProvider(this),
                    new MakeCustomizerProvider(this, projectDescriptorProvider),
                    new MakeArtifactProviderImpl(),
                    new ProjectXmlSavedHookImpl(),
                    UILookupMergerSupport.createProjectOpenHookMerger(new ProjectOpenedHookImpl()),
                    new MakeSharabilityQuery(projectDescriptorProvider, FileUtil.toFile(getProjectDirectory())),
                    sources,
                    new AntProjectHelperProvider(),
                    projectDescriptorProvider,
                    new MakeProjectConfigurationProvider(this, projectDescriptorProvider),
                    new NativeProjectProvider(this, projectDescriptorProvider),
                    new RecommendedTemplatesImpl(projectDescriptorProvider),
                    new MakeProjectOperations(this),
                    new FolderSearchInfo(projectDescriptorProvider),
                    kind,
                    new MakeProjectEncodingQueryImpl(this),
                    new RemoteProjectImpl(),
                    new ToolchainProjectImpl(),
                    new CPPImpl(sources)
                });
        return LookupProviderSupport.createCompositeLookup(lkp, kind.getLookupMergerPath());
    }

    @Override
    public void configurationXmlChanged(AntProjectEvent ev) {
        if (ev.getPath().equals(AntProjectHelper.PROJECT_XML_PATH)) {
            // Could be various kinds of changes, but name & displayName might have changed.
            Info info = (Info) getLookup().lookup(ProjectInformation.class);
            info.firePropertyChange(ProjectInformation.PROP_NAME);
            info.firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME);
        }
    }

    @Override
    public void propertiesChanged(AntProjectEvent ev) {
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
        if (UNIT_TEST_MODE) {
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

    private void saveAdditionalExtensions() {
        Element data = helper.getPrimaryConfigurationData(true);
        saveAdditionalHeaderExtensions(data, MakeProject.C_EXTENSIONS, cExtensions);
        saveAdditionalHeaderExtensions(data, MakeProject.CPP_EXTENSIONS, cppExtensions);
        saveAdditionalHeaderExtensions(data, MakeProject.HEADER_EXTENSIONS, headerExtensions);
        helper.putPrimaryConfigurationData(data, true);
    }

    private void saveAdditionalHeaderExtensions(Element data, String key, Set<String> set) {
        Element element;
        NodeList nodeList = data.getElementsByTagName(key);
        if (nodeList.getLength() == 1) {
            element = (Element) nodeList.item(0);
            NodeList deadKids = element.getChildNodes();
            while (deadKids.getLength() > 0) {
                element.removeChild(deadKids.item(0));
            }
        } else {
            element = data.getOwnerDocument().createElementNS(MakeProjectType.PROJECT_CONFIGURATION_NAMESPACE, key);
            data.appendChild(element);
        }
        StringBuilder buf = new StringBuilder();
        for (String e : set) {
            if (buf.length() > 0) {
                buf.append(',');
            }
            buf.append(e);
        }
        element.appendChild(data.getOwnerDocument().createTextNode(buf.toString()));
    }

    private boolean addNewExtensionDialog(Set<String> usedExtension, String type) {
        if (UNIT_TEST_MODE){
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
    private final class AntProjectHelperProvider {

        AntProjectHelper getAntProjectHelper() {
            return helper;
        }
    }

    private static final class RecommendedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates {

        private static final String[] RECOMMENDED_TYPES = new String[]{
            "c-types", // NOI18N
            "cpp-types", // NOI18N
            "shell-types", // NOI18N
            "makefile-types", // NOI18N
            "simple-files", // NOI18N
            "fortran-types", // NOI18N
            "asm-types", // NOI18N
            "qt-types"}; // NOI18N
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
            MakeConfigurationDescriptor configurationDescriptor =
                    configurationProvider.getConfigurationDescriptor(false);
            if (configurationDescriptor != null) {
                MakeConfiguration conf = configurationDescriptor.getActiveConfiguration();
                if (conf != null && conf.isQmakeConfiguration()) {
                    return PRIVILEGED_NAMES_QT;
                }
            }
            return PRIVILEGED_NAMES;
        }
    }

    /** Return configured project name. */
    public String getName() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<String>() {

            @Override
            public String run() {
                Element data = helper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(MakeProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
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

    public void setName(final String name) {
        ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {

            @Override
            public Void run() {
                Element data = helper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(MakeProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(MakeProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                    data.insertBefore(nameEl, data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                helper.putPrimaryConfigurationData(data, true);
                return null;
            }
        });
    }

    /*
     * Return source encoding if in project.xml (only project version >= 50)
     */
    public String getSourceEncodingFromProjectXml() {
        Element data = helper.getPrimaryConfigurationData(true);

        NodeList nodeList = data.getElementsByTagName(MakeProjectType.SOURCE_ENCODING_TAG);
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
            if (!projectDescriptorProvider.gotDescriptor()){
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
            if (!projectDescriptorProvider.gotDescriptor() && data.getElementsByTagName(MakeProjectType.MAKE_DEP_PROJECTS).getLength() > 0) {
                NodeList nl4 = data.getElementsByTagName(MakeProjectType.MAKE_DEP_PROJECT);
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

            String baseDir = FileUtil.toFile(getProjectDirectory()).getPath();
            for (String loc : subProjectLocations) {
                String location = CndPathUtilitities.toAbsolutePath(baseDir, loc);
                try {
                    FileObject fo = FileUtil.toFileObject(new File(location).getCanonicalFile());
                    Project project = ProjectManager.getDefault().findProject(fo);
                    if (project != null) {
                        subProjects.add(project);
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

    private final class Info implements ProjectInformation {

        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

        Info() {
        }

        void firePropertyChange(String prop) {
            pcs.firePropertyChange(prop, null, null);
        }

        @Override
        public String getName() {
            String name = PropertyUtils.getUsablePropertyName(MakeProject.this.getName());
            return name;
        }

        @Override
        public String getDisplayName() {
            String name = MakeProject.this.getName();

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
                            sep = nextSep+1;
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
                        name = NbBundle.getMessage(getClass(), "PRJ_DISPLAY_NAME_WITH_FOLDER", name, prjDirDispName); // NOI18N
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
            return name;
        }

        @Override
        public Icon getIcon() {
            Icon icon = null;
            icon = MakeConfigurationDescriptor.MAKEFILE_ICON;
            // First 'projectType' (from project.xml)
            /*
            switch (projectType) {
            case ProjectDescriptor.TYPE_APPLICATION :
            icon = NeoProjectDescriptor.MAKE_NEW_APP_ICON;
            break;
            case ProjectDescriptor.TYPE_DYNAMIC_LIB :
            icon = NeoProjectDescriptor.MAKE_NEW_LIB_ICON;
            break;
            case ProjectDescriptor.TYPE_MAKEFILE :
            icon = MakeProjectDescriptor.MAKE_EXT_APP_ICON;
            break;
            case ProjectDescriptor.TYPE_STATIC_LIB :
            icon = MakeProjectDescriptor.MAKE_EXT_LIB_ICON;
            break;
            };
            // Then lookup the projectDescriptor and get it from there
            if (icon == null) {
            ProjectDescriptorProvider pdp = (ProjectDescriptorProvider)getLookup().lookup(ProjectDescriptorProvider.class);
            if (pdp != null) {
            icon = pdp.getProjectDescriptor().getIcon();
            projectType = pdp.getProjectDescriptor().getProjectType();
            }
            }
            // Then ...
            if (icon == null) {
            icon = MAKE_PROJECT_ICON;
            System.err.println("Cannot recognize make project type!"); // NOI18N
            }
             */
            return icon;
        }

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

    private static final class ProjectXmlSavedHookImpl extends ProjectXmlSavedHook {

        @Override
        protected void projectXmlSaved() throws IOException {
            /*
            genFilesHelper.refreshBuildScript(
            GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
            MakeProject.class.getResource("resources/build-impl.xsl"),
            false);
            genFilesHelper.refreshBuildScript(
            GeneratedFilesHelper.BUILD_XML_PATH,
            MakeProject.class.getResource("resources/build.xsl"),
            false);
             */
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

    private synchronized void onProjectOpened(){
        if (!isOpenHookDone) {
            checkNeededExtensions();
            if (openedTasks != null) {
                for (Runnable runnable : openedTasks) {
                    runnable.run();
                }
                openedTasks.clear();
                openedTasks = null;
            }

            GlobalPathRegistry.getDefault().register(MakeProjectPaths.SOURCES, sourcepath.getClassPath());
            isOpenHookDone = true;
        }
    }

    void setDeleted(){
        isDeleted.set(true);
    }

    private synchronized void onProjectClosed(){
        if (!isDeleted.get()) {
            if (projectDescriptorProvider.getConfigurationDescriptor() != null) {
                projectDescriptorProvider.getConfigurationDescriptor().saveAndClose();
            }
        }
        if (isOpenHookDone) {
            GlobalPathRegistry.getDefault().unregister(MakeProjectPaths.SOURCES, sourcepath.getClassPath());
            isOpenHookDone = false;
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
            Configuration[] confs = projectDescriptor.getConfs().toArray();

//            String projectLocation = null;
//            int configurationType = 0;
//            String configurationName = null;
//            boolean active = false;
//            String workingDirectory = null;
//            String buildCommand = null;
//            String cleanCommand = null;
//            String output = null;

//            projectLocation = FileUtil.toFile(helper.getProjectDirectory()).getPath();
            for (int i = 0; i < confs.length; i++) {
                MakeConfiguration makeConfiguration = (MakeConfiguration) confs[i];
                artifacts.add(new MakeArtifact(projectDescriptor, makeConfiguration));
            }
            return artifacts.toArray(new MakeArtifact[artifacts.size()]);
        }
    }

    private static class FolderSearchInfo implements SearchInfo {

        private ConfigurationDescriptorProvider projectDescriptorProvider;

        FolderSearchInfo(ConfigurationDescriptorProvider projectDescriptorProvider) {
            this.projectDescriptorProvider = projectDescriptorProvider;
        }

        @Override
        public boolean canSearch() {
            return true;
        }

        @Override
        public Iterator<DataObject> objectsToSearch() {
            MakeConfigurationDescriptor projectDescriptor = projectDescriptorProvider.getConfigurationDescriptor();
            Folder rootFolder = projectDescriptor.getLogicalFolders();
            return rootFolder.getAllItemsAsDataObjectSet(false, "text/").iterator(); // NOI18N
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
        private ClassPath [] classpath = null;

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
            SourceGroup [] groups = sources.getSourceGroups("generic"); // NOI18N
            for(SourceGroup g : groups) {
                try {
                    list.add(new PathResourceImpl(ClassPathSupport.createResource(g.getRootFolder().getURL())));
                } catch (FileStateInvalidException ex) {
                    Logger.getLogger(MakeProject.class.getName()).log(Level.WARNING, null, ex);
                }
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

        public synchronized ClassPath [] getClassPath() {
            if (classpath == null) {
                classpath = new ClassPath [] { ClassPathFactory.createClassPath(this) };
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
                for (SourceGroup sg : sources.getSourceGroups("generic")) { // NOI18N
                    if (sg.getRootFolder().equals(file)) {
                        try {
                            return ClassPathSupport.createClassPath(Arrays.asList(new PathResourceImpl(ClassPathSupport.createResource(file.getURL()))));
                        } catch (FileStateInvalidException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }

            return null;
        }

    }
}
