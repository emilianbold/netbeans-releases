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
package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.api.remote.RemoteProject;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.cnd.makeproject.FullRemoteExtension;
import org.netbeans.modules.cnd.makeproject.MakeProjectUtils;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectOptions;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent.PredefinedType;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.IntNodeProp;
import org.netbeans.modules.cnd.makeproject.platform.Platforms;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.BooleanNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.CppUtils;
import org.netbeans.modules.cnd.makeproject.configurations.ui.CompilerSetNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.DevelopmentHostNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.RemoteSyncFactoryNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.RequiredProjectsNodeProp;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class MakeConfiguration extends Configuration {

    public static final String NBPROJECT_FOLDER = "nbproject"; // NOI18N
    public static final String NBPROJECT_PRIVATE_FOLDER = "nbproject/private"; // NOI18N
    public static final String PROJECT_XML = "project.xml"; // NOI18N
    public static final String CONFIGURATIONS_XML = "configurations.xml"; // NOI18N
    public static final String MAKEFILE_IMPL = "Makefile-impl.mk"; // NOI18N
    public static final String MAKEFILE_VARIABLES = "Makefile-variables.mk"; // NOI18N
    public static final String BUILD_FOLDER = "build"; // NOI18N
    public static final String DIST_FOLDER = "dist"; // NOI18N
    public static final String EXT_FOLDER = "_ext"; // NOI18N
    public static final String OBJECTDIR_MACRO_NAME = "OBJECTDIR"; // NOI18N
    public static final String OBJECTDIR_MACRO = "${" + OBJECTDIR_MACRO_NAME + "}"; // NOI18N
    public static final String CND_CONF_MACRO = "${CND_CONF}"; // NOI18N
    public static final String CND_PLATFORM_MACRO = "${CND_PLATFORM}"; // NOI18N
    public static final String CND_DISTDIR_MACRO = "${CND_DISTDIR}"; // NOI18N
    public static final String CND_BUILDDIR_MACRO = "${CND_BUILDDIR}"; // NOI18N
    // Project Types
    private static String[] TYPE_NAMES_UNMANAGED = {
        getString("MakefileName")
    };
    private static String[] TYPE_NAMES_MANAGED = {
        getString("ApplicationName"),
        getString("DynamicLibraryName"),
        getString("StaticLibraryName"),};
    private static String[] TYPE_NAMES_MANAGED_DB = {
        getString("DBApplicationName")
    };
    private static String[] TYPE_NAMES_MANAGED_QT = {
        getString("QtApplicationName"),
        getString("QtDynamicLibraryName"),
        getString("QtStaticLibraryName")
    };
    public static final int TYPE_MAKEFILE = 0;
    public static final int TYPE_APPLICATION = 1;
    public static final int TYPE_DYNAMIC_LIB = 2;
    public static final int TYPE_STATIC_LIB = 3;
    public static final int TYPE_QT_APPLICATION = 4;
    public static final int TYPE_QT_DYNAMIC_LIB = 5;
    public static final int TYPE_QT_STATIC_LIB = 6;
    public static final int TYPE_DB_APPLICATION = 7;
    // Configurations
    private IntConfiguration configurationType;
    private MakefileConfiguration makefileConfiguration;
    private CompilerSet2Configuration compilerSet;
    private LanguageBooleanConfiguration cRequired;
    private LanguageBooleanConfiguration cppRequired;
    private LanguageBooleanConfiguration fortranRequired;
    private LanguageBooleanConfiguration assemblerRequired;
    private DevelopmentHostConfiguration developmentHost;
    private BooleanConfiguration dependencyChecking;
    private BooleanConfiguration rebuildPropChanged;
    private CCompilerConfiguration cCompilerConfiguration;
    private CCCompilerConfiguration ccCompilerConfiguration;
    private FortranCompilerConfiguration fortranCompilerConfiguration;
    private AssemblerConfiguration assemblerConfiguration;
    private LinkerConfiguration linkerConfiguration;
    private ArchiverConfiguration archiverConfiguration;
    private PackagingConfiguration packagingConfiguration;
    private RequiredProjectsConfiguration requiredProjectsConfiguration;
    private DebuggerChooserConfiguration debuggerChooserConfiguration;
    private QmakeConfiguration qmakeConfiguration;
    private boolean languagesDirty = true;
    private RemoteSyncFactory fixedRemoteSyncFactory;
    private volatile RemoteProject.Mode remoteMode;
    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.cnd.makeproject"); // NOI18N

    public MakeConfiguration(String baseDir, String name, int configurationTypeValue) {
        this(baseDir, name, configurationTypeValue, null);
    }

    public MakeConfiguration(String baseDir, String name, int configurationTypeValue, String hostUID) {
        this(baseDir, name, configurationTypeValue, hostUID, null, true);
    }

    public MakeConfiguration(String baseDir, String name, int configurationTypeValue, String hostUID, CompilerSet hostCS, boolean defaultToolCollection) {
        super(baseDir, name);
        remoteMode = RemoteProject.DEFAULT_MODE;
        hostUID = (hostUID == null) ? CppUtils.getDefaultDevelopmentHost() : hostUID;
        if (configurationTypeValue == TYPE_MAKEFILE) {
            configurationType = new IntConfiguration(null, configurationTypeValue, TYPE_NAMES_UNMANAGED, null);
        } else if (configurationTypeValue == TYPE_APPLICATION || configurationTypeValue == TYPE_DYNAMIC_LIB || configurationTypeValue == TYPE_STATIC_LIB) {
            configurationType = new ManagedIntConfiguration(null, configurationTypeValue, TYPE_NAMES_MANAGED, null, TYPE_APPLICATION);
        } else if (configurationTypeValue == TYPE_DB_APPLICATION) {
            configurationType = new ManagedIntConfiguration(null, configurationTypeValue, TYPE_NAMES_MANAGED_DB, null, TYPE_DB_APPLICATION);
        } else if (configurationTypeValue == TYPE_QT_APPLICATION || configurationTypeValue == TYPE_QT_DYNAMIC_LIB || configurationTypeValue == TYPE_QT_STATIC_LIB) {
            configurationType = new ManagedIntConfiguration(null, configurationTypeValue, TYPE_NAMES_MANAGED_QT, null, TYPE_QT_APPLICATION);
        } else {
            assert false;
        }
        developmentHost = new DevelopmentHostConfiguration(ExecutionEnvironmentFactory.fromUniqueID(hostUID));
        if (defaultToolCollection) {
            compilerSet = new CompilerSet2Configuration(developmentHost);
        } else {
            CompilerSet defCS = (hostCS != null) ? hostCS : CompilerSetManager.get(developmentHost.getExecutionEnvironment()).getDefaultCompilerSet();
            compilerSet = new CompilerSet2Configuration(developmentHost, defCS);
        }
        cRequired = new LanguageBooleanConfiguration();
        cppRequired = new LanguageBooleanConfiguration();
        fortranRequired = new LanguageBooleanConfiguration();
        assemblerRequired = new LanguageBooleanConfiguration();
        makefileConfiguration = new MakefileConfiguration(this);
        dependencyChecking = new BooleanConfiguration(isMakefileConfiguration() ? false : MakeProjectOptions.getDepencyChecking());
        rebuildPropChanged = new BooleanConfiguration(isMakefileConfiguration() ? false : MakeProjectOptions.getRebuildPropChanged());
        cCompilerConfiguration = new CCompilerConfiguration(baseDir, null, this);
        ccCompilerConfiguration = new CCCompilerConfiguration(baseDir, null, this);
        fortranCompilerConfiguration = new FortranCompilerConfiguration(baseDir, null);
        assemblerConfiguration = new AssemblerConfiguration(baseDir, null);
        linkerConfiguration = new LinkerConfiguration(this);
        archiverConfiguration = new ArchiverConfiguration(this);
        packagingConfiguration = new PackagingConfiguration(this);
        requiredProjectsConfiguration = new RequiredProjectsConfiguration();

        debuggerChooserConfiguration = new DebuggerChooserConfiguration(Lookup.EMPTY);
        qmakeConfiguration = new QmakeConfiguration(this);

        developmentHost.addPropertyChangeListener(compilerSet);
        initAuxObjects();
    }

    public void setMakefileConfiguration(MakefileConfiguration makefileConfiguration) {
        this.makefileConfiguration = makefileConfiguration;
        this.makefileConfiguration.setMakeConfiguration(this);
    }

    public MakefileConfiguration getMakefileConfiguration() {
        return makefileConfiguration;
    }

    public IntConfiguration getConfigurationType() {
        return configurationType;
    }

    public void setConfigurationType(IntConfiguration configurationType) {
        this.configurationType = configurationType;
    }

    public BooleanConfiguration getDependencyChecking() {
        return dependencyChecking;
    }

    public void setDependencyChecking(BooleanConfiguration dependencyChecking) {
        this.dependencyChecking = dependencyChecking;
    }

    public BooleanConfiguration getRebuildPropChanged() {
        return rebuildPropChanged;
    }

    public void setRebuildPropChanged(BooleanConfiguration rebuildPropChanged) {
        this.rebuildPropChanged = rebuildPropChanged;
    }

    public CompilerSet2Configuration getCompilerSet() {
        return compilerSet;
    }

    public void setCompilerSet(CompilerSet2Configuration compilerSet) {
        this.compilerSet = compilerSet;
    }

    public LanguageBooleanConfiguration getCRequired() {
        return cRequired;
    }

    public LanguageBooleanConfiguration getCppRequired() {
        return cppRequired;
    }

    public LanguageBooleanConfiguration getFortranRequired() {
        return fortranRequired;
    }

    public void setCRequired(LanguageBooleanConfiguration cRequired) {
        this.cRequired = cRequired;
    }

    public void setCppRequired(LanguageBooleanConfiguration cppRequired) {
        this.cppRequired = cppRequired;
    }

    public void setFortranRequired(LanguageBooleanConfiguration fortranRequired) {
        this.fortranRequired = fortranRequired;
    }

    public LanguageBooleanConfiguration getAssemblerRequired() {
        return assemblerRequired;
    }

    public void setAssemblerRequired(LanguageBooleanConfiguration assemblerRequired) {
        this.assemblerRequired = assemblerRequired;
    }

    public PlatformInfo getPlatformInfo() {
        PlatformInfo platformInfo = PlatformInfo.getDefault(getDevelopmentHost().getExecutionEnvironment());
//        assert platformInfo.getPlatform() == getPlatform().getValue();
        return platformInfo;

    }

    public DevelopmentHostConfiguration getDevelopmentHost() {
        return developmentHost;
    }

    public void setDevelopmentHost(DevelopmentHostConfiguration developmentHost) {
        this.developmentHost = developmentHost;
    }

    public boolean isApplicationConfiguration() {
        switch (getConfigurationType().getValue()) {
            case TYPE_APPLICATION:
            case TYPE_DB_APPLICATION:
            case TYPE_QT_APPLICATION:
                return true;
            default:
                return false;
        }
    }

    public boolean isCompileConfiguration() {
        return getConfigurationType().getValue() == TYPE_APPLICATION ||
               getConfigurationType().getValue() == TYPE_DB_APPLICATION ||
               getConfigurationType().getValue() == TYPE_DYNAMIC_LIB ||
               getConfigurationType().getValue() == TYPE_STATIC_LIB;
    }

    public boolean isLibraryConfiguration() {
        switch (getConfigurationType().getValue()) {
            case TYPE_DYNAMIC_LIB:
            case TYPE_STATIC_LIB:
            case TYPE_QT_DYNAMIC_LIB:
            case TYPE_QT_STATIC_LIB:
                return true;
            default:
                return false;
        }
    }

    public boolean isLinkerConfiguration() {
        return getConfigurationType().getValue() == TYPE_APPLICATION ||
               getConfigurationType().getValue() == TYPE_DB_APPLICATION ||
               getConfigurationType().getValue() == TYPE_DYNAMIC_LIB;
    }

    public final boolean isMakefileConfiguration() {
        return getConfigurationType().getValue() == TYPE_MAKEFILE;
    }

    public boolean isDynamicLibraryConfiguration() {
        switch (getConfigurationType().getValue()) {
            case TYPE_DYNAMIC_LIB:
            case TYPE_QT_DYNAMIC_LIB:
                return true;
            default:
                return false;
        }
    }

    public boolean isArchiverConfiguration() {
        return getConfigurationType().getValue() == TYPE_STATIC_LIB;
    }

    public boolean isQmakeConfiguration() {
        switch (getConfigurationType().getValue()) {
            case TYPE_QT_APPLICATION:
            case TYPE_QT_DYNAMIC_LIB:
            case TYPE_QT_STATIC_LIB:
                return true;
            default:
                return false;
        }
    }

    public final boolean isStandardManagedConfiguration() {
        switch (getConfigurationType().getValue()) {
            case TYPE_APPLICATION:
            case TYPE_DB_APPLICATION:
            case TYPE_DYNAMIC_LIB:
            case TYPE_STATIC_LIB:
                return true;
            default:
                return false;
        }
    }

    public void setCCompilerConfiguration(CCompilerConfiguration cCompilerConfiguration) {
        this.cCompilerConfiguration = cCompilerConfiguration;
    }

    public CCompilerConfiguration getCCompilerConfiguration() {
        return cCompilerConfiguration;
    }

    public void setCCCompilerConfiguration(CCCompilerConfiguration ccCompilerConfiguration) {
        this.ccCompilerConfiguration = ccCompilerConfiguration;
    }

    public CCCompilerConfiguration getCCCompilerConfiguration() {
        return ccCompilerConfiguration;
    }

    public void setFortranCompilerConfiguration(FortranCompilerConfiguration fortranCompilerConfiguration) {
        this.fortranCompilerConfiguration = fortranCompilerConfiguration;
    }

    public FortranCompilerConfiguration getFortranCompilerConfiguration() {
        return fortranCompilerConfiguration;
    }

    public void setAssemblerConfiguration(AssemblerConfiguration assemblerConfiguration) {
        this.assemblerConfiguration = assemblerConfiguration;
    }

    public AssemblerConfiguration getAssemblerConfiguration() {
        return assemblerConfiguration;
    }

    public void setLinkerConfiguration(LinkerConfiguration linkerConfiguration) {
        this.linkerConfiguration = linkerConfiguration;
        this.linkerConfiguration.setMakeConfiguration(this);
    }

    public LinkerConfiguration getLinkerConfiguration() {
        return linkerConfiguration;
    }

    public void setArchiverConfiguration(ArchiverConfiguration archiverConfiguration) {
        this.archiverConfiguration = archiverConfiguration;
        this.archiverConfiguration.setMakeConfiguration(this);
    }

    public ArchiverConfiguration getArchiverConfiguration() {
        return archiverConfiguration;
    }

    public void setPackagingConfiguration(PackagingConfiguration packagingConfiguration) {
        this.packagingConfiguration = packagingConfiguration;
        this.packagingConfiguration.setMakeConfiguration(this);
    }

    public PackagingConfiguration getPackagingConfiguration() {
        return packagingConfiguration;
    }

    // LibrariesConfiguration
    public RequiredProjectsConfiguration getRequiredProjectsConfiguration() {
        return requiredProjectsConfiguration;
    }

    public void setRequiredProjectsConfiguration(RequiredProjectsConfiguration requiredProjectsConfiguration) {
        this.requiredProjectsConfiguration = requiredProjectsConfiguration;
    }

    public DebuggerChooserConfiguration getDebuggerChooserConfiguration() {
        return debuggerChooserConfiguration;
    }

    public void setDebuggerChooserConfiguration(DebuggerChooserConfiguration debuggerChooserConfiguration) {
        this.debuggerChooserConfiguration = debuggerChooserConfiguration;
    }

    public QmakeConfiguration getQmakeConfiguration() {
        return qmakeConfiguration;
    }

    public void setQmakeConfiguration(QmakeConfiguration qmakeConfiguration) {
        this.qmakeConfiguration = qmakeConfiguration;
    }

    @Override
    public void assign(Configuration conf) {
        MakeConfiguration makeConf = (MakeConfiguration) conf;
        setName(makeConf.getName());
        setBaseDir(makeConf.getBaseDir());
        getConfigurationType().assign(makeConf.getConfigurationType());
        getDevelopmentHost().assign(makeConf.getDevelopmentHost());
        fixedRemoteSyncFactory = makeConf.fixedRemoteSyncFactory;
        remoteMode = makeConf.remoteMode;
        getCompilerSet().assign(makeConf.getCompilerSet());
        getCRequired().assign(makeConf.getCRequired());
        getCppRequired().assign(makeConf.getCppRequired());
        getFortranRequired().assign(makeConf.getFortranRequired());
        getAssemblerRequired().assign(makeConf.getAssemblerRequired());
        getDependencyChecking().assign(makeConf.getDependencyChecking());
        getRebuildPropChanged().assign(makeConf.getRebuildPropChanged());

        getMakefileConfiguration().assign(makeConf.getMakefileConfiguration());
        getCCompilerConfiguration().assign(makeConf.getCCompilerConfiguration());
        getCCompilerConfiguration().setOwner(makeConf);
        getCCCompilerConfiguration().assign(makeConf.getCCCompilerConfiguration());
        getCCCompilerConfiguration().setOwner(makeConf);
        getFortranCompilerConfiguration().assign(makeConf.getFortranCompilerConfiguration());
        getAssemblerConfiguration().assign(makeConf.getAssemblerConfiguration());
        getLinkerConfiguration().assign(makeConf.getLinkerConfiguration());
        getArchiverConfiguration().assign(makeConf.getArchiverConfiguration());
        getPackagingConfiguration().assign(makeConf.getPackagingConfiguration());
        getRequiredProjectsConfiguration().assign(makeConf.getRequiredProjectsConfiguration());
        getDebuggerChooserConfiguration().assign(makeConf.getDebuggerChooserConfiguration());
        getQmakeConfiguration().assign(makeConf.getQmakeConfiguration());

        // do assign on all aux objects
        ConfigurationAuxObject[] auxs = getAuxObjects(); // from this profile
        //ConfigurationAuxObject[] p_auxs = conf.getAuxObjects(); // from the 'other' profile
        for (int i = 0; i < auxs.length; i++) {
            // unsafe using! suppose same set of objects and same object order
            String id = auxs[i].getId();
            ConfigurationAuxObject object = conf.getAuxObject(id);
            if (object != null) {
                // safe using
                auxs[i].assign(object);
            } else {
                System.err.println("Configuration - assign: Object ID " + id + " do not found"); // NOI18N
            }
        }
    }

    @Override
    public Configuration cloneConf() {
        return (Configuration) clone();
    }

    /**
     * Make a copy of configuration requested from Project Properties
     * @return Copy of configuration
     */
    @Override
    public Configuration copy() {
        MakeConfiguration copy = new MakeConfiguration(getBaseDir(), getName(), getConfigurationType().getValue());
        copy.assign(this);
        // copy aux objects
        ConfigurationAuxObject[] auxs = getAuxObjects();
        List<ConfigurationAuxObject> copiedAuxs = new ArrayList<ConfigurationAuxObject>();
        for (int i = 0; i < auxs.length; i++) {
            if (auxs[i] instanceof ItemConfiguration) {
                copiedAuxs.add(((ItemConfiguration) auxs[i]).copy(copy));
            } else if (auxs[i] instanceof FolderConfiguration) {
                copiedAuxs.add(((FolderConfiguration) auxs[i]).copy(copy));
            } else {
                String id = auxs[i].getId();
                ConfigurationAuxObject copyAux = copy.getAuxObject(id);
                if (copyAux != null) {
                    copyAux.assign(auxs[i]);
                    copiedAuxs.add(copyAux);
                } else {
                    copiedAuxs.add(auxs[i]);
                }
            }
        }
        copy.setAuxObjects(copiedAuxs);
        // Fixup folder and item configuration links links
        fixupMasterLinks(copy);

        return copy;
    }

    private void fixupMasterLinks(MakeConfiguration makeConf) {
        FileObject projectDirFO = CndFileUtils.toFileObject(getBaseDir());
        Project project = null;
        try {
            project = ProjectManager.getDefault().findProject(projectDirFO);
        } catch (IOException ioe) {
            // Error
            return;
        }
        if (project == null) {
            return; // IZ 172628 (basedir is a valid directory but doesn't contain a project!)
        }
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        MakeConfigurationDescriptor makeConfigurationDescriptor = pdp.getConfigurationDescriptor();

        Folder root = makeConfigurationDescriptor.getLogicalFolders();
        fixupFolderItemLinks(makeConf, root, makeConf.getCCompilerConfiguration(), makeConf.getCCCompilerConfiguration());
    }

    private void fixupFolderItemLinks(MakeConfiguration makeConf, Folder folder, BasicCompilerConfiguration cCompilerConf, BasicCompilerConfiguration ccCompilerConf) {
        if (!folder.isProjectFiles()) {
            return;
        }
        FolderConfiguration folderConfiguration = (FolderConfiguration) makeConf.getAuxObject(folder.getId());
        if (folderConfiguration == null) {
            return;
        }
        if (folderConfiguration.getCCompilerConfiguration() != null) {
            folderConfiguration.getCCompilerConfiguration().setMaster(cCompilerConf);
        }
        if (folderConfiguration.getCCCompilerConfiguration() != null) {
            folderConfiguration.getCCCompilerConfiguration().setMaster(ccCompilerConf);
        }
        for (Item item : folder.getItemsAsArray()) {
            ItemConfiguration itemConfiguration = (ItemConfiguration) makeConf.getAuxObject(item.getId());
            if (itemConfiguration.getCCompilerConfiguration() != null) {
                itemConfiguration.getCCompilerConfiguration().setMaster(folderConfiguration.getCCompilerConfiguration());
                itemConfiguration.getCCompilerConfiguration().fixupMasterLinks(makeConf.getCCompilerConfiguration());
            }
            if (itemConfiguration.getCCCompilerConfiguration() != null) {
                itemConfiguration.getCCCompilerConfiguration().setMaster(folderConfiguration.getCCCompilerConfiguration());
                itemConfiguration.getCCCompilerConfiguration().fixupMasterLinks(makeConf.getCCCompilerConfiguration());
            }
        }
        for (Folder subfolder : folder.getFoldersAsArray()) {
            fixupFolderItemLinks(makeConf, subfolder, folderConfiguration.getCCompilerConfiguration(), folderConfiguration.getCCCompilerConfiguration());
        }
    }

    /**
     * Clone object
     */
    @Override
    public MakeConfiguration clone() {
        MakeConfiguration clone = new MakeConfiguration(getBaseDir(), getName(),
                getConfigurationType().getValue(), getDevelopmentHost().getHostKey());
        super.cloneConf(clone);
        clone.setCloneOf(this);

        DevelopmentHostConfiguration dhconf = getDevelopmentHost().clone();
        clone.setDevelopmentHost(dhconf);
        clone.fixedRemoteSyncFactory = this.fixedRemoteSyncFactory;
        clone.remoteMode = this.remoteMode;
        CompilerSet2Configuration csconf = getCompilerSet().clone();
        csconf.setDevelopmentHostConfiguration(dhconf);
        clone.setCompilerSet(csconf);
        clone.setCRequired(getCRequired().clone());
        clone.setCppRequired(getCppRequired().clone());
        clone.setFortranRequired(getFortranRequired().clone());
        clone.setAssemblerRequired(getAssemblerRequired().clone());
        clone.setMakefileConfiguration(getMakefileConfiguration().clone());
        clone.setDependencyChecking(getDependencyChecking().clone());
        clone.setRebuildPropChanged(getRebuildPropChanged().clone());
        clone.setCCompilerConfiguration(getCCompilerConfiguration().clone());
        clone.getCCompilerConfiguration().setOwner(clone);
        clone.setCCCompilerConfiguration(getCCCompilerConfiguration().clone());
        clone.getCCCompilerConfiguration().setOwner(clone);
        clone.setFortranCompilerConfiguration(getFortranCompilerConfiguration().clone());
        clone.setAssemblerConfiguration(getAssemblerConfiguration().clone());
        clone.setLinkerConfiguration(getLinkerConfiguration().clone());
        clone.setArchiverConfiguration(getArchiverConfiguration().clone());
        clone.setPackagingConfiguration(getPackagingConfiguration().clone());
        clone.setRequiredProjectsConfiguration(getRequiredProjectsConfiguration().clone());
        clone.setDebuggerChooserConfiguration(getDebuggerChooserConfiguration().clone());
        clone.setQmakeConfiguration(getQmakeConfiguration().clone());

        dhconf.addPropertyChangeListener(csconf);

        // Clone all the aux objects
        //Vector clonedAuxObjects = new Vector();
        //for (Enumeration e = auxObjects.elements() ; e.hasMoreElements() ;) {
        //    ConfigurationAuxObject o = (ConfigurationAuxObject)e.nextElement();
        //    ConfigurationAuxObject clone2 = (ConfigurationAuxObject)o.clone();
        //    clonedAuxObjects.add(clone2);
        //}
        ConfigurationAuxObject[] objects = getAuxObjects();
        List<ConfigurationAuxObject> clonedAuxObjects = new ArrayList<ConfigurationAuxObject>();
        for (int i = 0; i < objects.length; i++) {
            clonedAuxObjects.add(objects[i].clone(this));
        }
        clone.setAuxObjects(clonedAuxObjects);
        return clone;
    }

    public Sheet getBuildSheet(Project project) {
        Sheet sheet = new Sheet();

        Sheet.Set set = new Sheet.Set();
        set.setName("ProjectDefaults"); // NOI18N
        set.setDisplayName(getString("ProjectDefaultsTxt"));
        set.setShortDescription(getString("ProjectDefaultsHint"));
        boolean canEditHost = MakeProjectUtils.isFullRemote(project) ? FullRemoteExtension.canChangeHost(this) : true;
        set.put(new DevelopmentHostNodeProp(getDevelopmentHost(), canEditHost, getString("DevelopmentHostTxt"), getString("DevelopmentHostHint"))); // NOI18N
        RemoteSyncFactoryNodeProp rsfNodeProp = new RemoteSyncFactoryNodeProp(this);
        set.put(rsfNodeProp);
//        set.put(new BuildPlatformNodeProp(getDevelopmentHost().getBuildPlatformConfiguration(), developmentHost, makeCustomizer, getDevelopmentHost().isLocalhost(), "builtPlatform", getString("PlatformTxt"), getString("PlatformHint"))); // NOI18N
        set.put(new CompilerSetNodeProp(getCompilerSet(), getDevelopmentHost(), true, "CompilerSCollection2", getString("CompilerCollectionTxt"), getString("CompilerCollectionHint"))); // NOI18N
//        set.put(new BooleanNodeProp(getCRequired(), true, "cRequired", getString("CRequiredTxt"), getString("CRequiredHint"))); // NOI18N
//        set.put(new BooleanNodeProp(getCppRequired(), true, "cppRequired", getString("CppRequiredTxt"), getString("CppRequiredHint"))); // NOI18N
//        set.put(new BooleanNodeProp(getFortranRequired(), true, "fortranRequired", getString("FortranRequiredTxt"), getString("FortranRequiredHint"))); // NOI18N
//        set.put(new BooleanNodeProp(getAssemblerRequired(), true, "assemblerRequired", getString("AssemblerRequiredTxt"), getString("AssemblerRequiredHint"))); // NOI18N
        set.put(new IntNodeProp(getConfigurationType(), true, "ConfigurationType", getString("ConfigurationTypeTxt"), getString("ConfigurationTypeHint"))); // NOI18N
        sheet.put(set);

        if (isCompileConfiguration()) {
            set = Sheet.createExpertSet();
            set.put(new BooleanNodeProp(getDependencyChecking(), true, "DependencyChecking", getString("DependencyCheckingTxt"), getString("DependencyCheckingHint"))); // NOI18N
            set.put(new BooleanNodeProp(getRebuildPropChanged(), true, "RebuildPropChanged", getString("RebuildPropChangedTxt"), getString("RebuildPropChangedHint"))); // NOI18N
            sheet.put(set);
        }

        return sheet;
    }

    public RemoteSyncFactory getRemoteSyncFactory() {
        RemoteSyncFactory result = fixedRemoteSyncFactory;
        synchronized (this) {
            if (result != null) {
                return result;
            }
        }
        ExecutionEnvironment execEnv = getDevelopmentHost().getExecutionEnvironment();
        return (execEnv.isLocal()) ? null : ServerList.get(execEnv).getSyncFactory(); // FIXUP: temporary solution
    }

    public RemoteSyncFactory getFixedRemoteSyncFactory() {
        return fixedRemoteSyncFactory;
    }

    public void setFixedRemoteSyncFactory(RemoteSyncFactory fixedRemoteSyncFactory) {
        this.fixedRemoteSyncFactory = fixedRemoteSyncFactory;
    }

    public RemoteProject.Mode getRemoteMode() {
        return remoteMode;
    }

    public ExecutionEnvironment getFileSystemHost() {
        if (remoteMode == RemoteProject.Mode.REMOTE_SOURCES) {
            return getDevelopmentHost().getExecutionEnvironment();
        } else {
            return ExecutionEnvironmentFactory.getLocal();
        }
    }
    
    public String getSourceBaseDir() {        
        if (remoteMode == RemoteProject.Mode.REMOTE_SOURCES) {
            FileObject projectDirFO = CndFileUtils.toFileObject(getBaseDir());
            try {                
                Project project = ProjectManager.getDefault().findProject(projectDirFO);
                if (project != null) {
                    RemoteProject remoteProject = project.getLookup().lookup(RemoteProject.class);
                    CndUtils.assertNotNullInConsole(remoteProject, "Null RemoteProject"); //NOI18N
                    return remoteProject.getSourceBaseDir();
                }
            } catch (IOException ioe) {
                LOGGER.log(Level.FINE, "Can not find a project in: " + projectDirFO, ioe);
            }
        }
        return getBaseDir();
    }

    public void setRemoteMode(RemoteProject.Mode mode) {
        CndUtils.assertNotNull(mode, "Null remote mode"); //NOI18N
        remoteMode = mode;
    }

    public Sheet getRequiredProjectsSheet(Project project, MakeConfiguration conf) {
        Sheet sheet = new Sheet();
        String[] texts = new String[]{getString("ProjectsTxt1"), getString("ProjectsHint"), getString("ProjectsTxt2"), getString("AllOptionsTxt2")};

        Sheet.Set set2 = new Sheet.Set();
        set2.setName("Projects"); // NOI18N
        set2.setDisplayName(getString("ProjectsTxt1"));
        set2.setShortDescription(getString("ProjectsHint"));
        set2.put(new RequiredProjectsNodeProp(getRequiredProjectsConfiguration(), project, conf, getBaseDir(), texts));
        sheet.put(set2);

        return sheet;
    }

    public void setRequiredLanguagesDirty(boolean b) {
        languagesDirty = b;
    }

    public boolean getRequiredLanguagesDirty() {
        return languagesDirty;
    }

    public boolean hasCFiles(MakeConfigurationDescriptor configurationDescriptor) {
        reCountLanguages(configurationDescriptor);
        return cRequired.getValue();
    }

    public boolean hasCPPFiles(MakeConfigurationDescriptor configurationDescriptor) {
        reCountLanguages(configurationDescriptor);
        return cppRequired.getValue();
    }

    public boolean hasFortranFiles(MakeConfigurationDescriptor configurationDescriptor) {
        reCountLanguages(configurationDescriptor);
        return fortranRequired.getValue();
    }

    public boolean hasAssemblerFiles(MakeConfigurationDescriptor configurationDescriptor) {
        reCountLanguages(configurationDescriptor);
        return assemblerRequired.getValue();
    }

//    public boolean hasAsmFiles(MakeConfigurationDescriptor configurationDescriptor) {
//        if (getLanguagesDirty())
//            reCountLanguages(configurationDescriptor);
//        return asmRequired.getValue();
//    }
    public void reCountLanguages(MakeConfigurationDescriptor configurationDescriptor) {
        boolean hasCFiles = false;
        boolean hasCPPFiles = false;
        boolean hasFortranFiles = false;
        boolean hasAssemblerFiles = false;
        //boolean hasCAsmFiles = false;


        if (!getRequiredLanguagesDirty()) {
            return;
        }

        Item[] items = configurationDescriptor.getProjectItems();
        if (items.length == 0 && isMakefileConfiguration()) {
            // This may not be true but is our best guess. No way to know since no files have been added to project.
            hasCFiles = true;
            hasCPPFiles = true;
        } else {
            // Base it on actual files added to project
            for (int x = 0; x < items.length; x++) {
                ItemConfiguration itemConfiguration = items[x].getItemConfiguration(this);
                if (itemConfiguration == null
                        || itemConfiguration.getExcluded() == null
                        || itemConfiguration.getExcluded().getValue()) {
                    continue;
                }
                if (itemConfiguration.getTool() == PredefinedToolKind.CCompiler) {
                    hasCFiles = true;
                }
                if (itemConfiguration.getTool() == PredefinedToolKind.CCCompiler) {
                    hasCPPFiles = true;
                }
                if (itemConfiguration.getTool() == PredefinedToolKind.FortranCompiler) {
                    hasFortranFiles = true;
                }
                if (itemConfiguration.getTool() == PredefinedToolKind.Assembler) {
                    hasAssemblerFiles = true;
                }
                //            if (itemConfiguration.getTool() == Tool.AsmCompiler) {
                //                hasCAsmFiles = false;
                //            }
            }
        }
        cRequired.setDefault(hasCFiles);
        cppRequired.setDefault(hasCPPFiles);
        fortranRequired.setDefault(hasFortranFiles);
        assemblerRequired.setDefault(hasAssemblerFiles);
        //asmRequired.setValueDef(hasCAsmFiles);

        languagesDirty = false;
    }

    public class LanguageBooleanConfiguration extends BooleanConfiguration {

        private boolean notYetSet = true;

        LanguageBooleanConfiguration() {
            super(false);
        }

        @Override
        public void setValue(boolean b) {
            if (notYetSet) {
                setValue(b, b);
            } else {
                super.setValue(b);
            }
            notYetSet = false;
        }

        @Override
        public void setDefault(boolean b) {
            if (getValue() == getDefault()) {
                setValue(b, b);
            } else {
                super.setDefault(b);
            }
            notYetSet = false;
        }

        public void setValue(boolean v, boolean d) {
            super.setValue(v);
            super.setDefault(d);
            notYetSet = false;
        }

        @Override
        public LanguageBooleanConfiguration clone() {
            LanguageBooleanConfiguration clone = new LanguageBooleanConfiguration();
            clone.setValue(getValue(), getDefault());
            clone.setModified(getModified());
            return clone;
        }

        public void assign(LanguageBooleanConfiguration conf) {
            setValue(conf.getValue(), conf.getDefault());
            setModified(conf.getModified());
        }
    }

    public String getVariant() {
        String ret = "";
        if (getCompilerSet().getCompilerSet() == null) {
            return ret;
        }
        return getVariant(getCompilerSet().getCompilerSet(), getDevelopmentHost().getBuildPlatform());
//        ret += getCompilerSet().getCompilerSet().getName() + "-"; // NOI18N
//        ret += Platforms.getPlatform(getPlatform().getValue()).getName();
//        return ret;
    }

    public static String getVariant(CompilerSet compilerSet, int platform) {
        return compilerSet.getName() + "-" + Platforms.getPlatform(platform).getName(); // NOI18N
    }

    public Set<Project> getSubProjects() {
        Set<Project> subProjects = new HashSet<Project>();
        LibrariesConfiguration librariesConfiguration = getLinkerConfiguration().getLibrariesConfiguration();
        for (LibraryItem item : librariesConfiguration.getValue()) {
            if (item instanceof LibraryItem.ProjectItem) {
                LibraryItem.ProjectItem projectItem = (LibraryItem.ProjectItem) item;
                Project project = projectItem.getProject(getBaseDir());
                if (project != null) {
                    subProjects.add(project);
                } else {
                    // FIXUP ERROR
                }
            }
        }
        for (LibraryItem.ProjectItem libProject : getRequiredProjectsConfiguration().getValue()) {
            Project project = libProject.getProject(getBaseDir());
            if (project != null) {
                subProjects.add(project);
            }
        }
        return subProjects;
    }

    public Set<String> getSubProjectLocations() {
        Set<String> subProjectLocations = new HashSet<String>();
        LibrariesConfiguration librariesConfiguration = getLinkerConfiguration().getLibrariesConfiguration();
        for (LibraryItem item : librariesConfiguration.getValue()) {
            if (item instanceof LibraryItem.ProjectItem) {
                LibraryItem.ProjectItem projectItem = (LibraryItem.ProjectItem) item;
                subProjectLocations.add(projectItem.getMakeArtifact().getProjectLocation());
            }
        }
        return subProjectLocations;
    }

    public Set<String> getSubProjectOutputLocations() {
        Set<String> subProjectOutputLocations = new HashSet<String>();
        LibrariesConfiguration librariesConfiguration = getLinkerConfiguration().getLibrariesConfiguration();
        for (LibraryItem item : librariesConfiguration.getValue()) {
            if (item instanceof LibraryItem.ProjectItem) {
                LibraryItem.ProjectItem projectItem = (LibraryItem.ProjectItem) item;
                String outputLocation = CndPathUtilitities.getDirName(projectItem.getMakeArtifact().getOutput());
                if (CndPathUtilitities.isPathAbsolute(outputLocation)) {
                    subProjectOutputLocations.add(outputLocation);
                } else {
                    subProjectOutputLocations.add(projectItem.getMakeArtifact().getProjectLocation() + "/" + outputLocation); // NOI18N
                } // NOI18N
            }
        }
        return subProjectOutputLocations;
    }

    public String getOutputValue() {
        String output = null;
        if (isLinkerConfiguration()) {
            output = getLinkerConfiguration().getOutputValue();
        } else if (isArchiverConfiguration()) {
            output = getArchiverConfiguration().getOutputValue();
        } else if (isMakefileConfiguration()) {
            output = getMakefileConfiguration().getOutput().getValue();
        } else if (isQmakeConfiguration()) {
            output = getQmakeConfiguration().getOutputValue();
        } else {
            assert false;
        }
        return output;
    }

    public String getAbsoluteOutputValue() {
        String output = getOutputValue();

        if (output == null) {
            return output;
        }
        if (!CndPathUtilitities.isPathAbsolute(output)) {
            output = getSourceBaseDir() + "/" + output; // NOI18N
            output = CndPathUtilitities.normalizeSlashes(output);
        }
        return expandMacros(output);
    }

    public boolean hasDebugger() {
        return ProjectActionSupport.getInstance().canHandle(this, PredefinedType.DEBUG);
    }

    public String expandMacros(String val) {
        // Substitute macros
        val = CndPathUtilitities.expandMacro(val, "${TESTDIR}", MakeConfiguration.CND_BUILDDIR_MACRO + '/' + MakeConfiguration.CND_CONF_MACRO + '/' + MakeConfiguration.CND_PLATFORM_MACRO + "/" + "tests"); // NOI18N
        val = CndPathUtilitities.expandMacro(val, "${OUTPUT_PATH}", getOutputValue()); // NOI18N
        val = CndPathUtilitities.expandMacro(val, "${OUTPUT_BASENAME}", CndPathUtilitities.getBaseName(getOutputValue())); // NOI18N
        val = CndPathUtilitities.expandMacro(val, "${PLATFORM}", getVariant()); // Backward compatibility // NOI18N
        val = CndPathUtilitities.expandMacro(val, MakeConfiguration.CND_PLATFORM_MACRO, getVariant()); // NOI18N
        val = CndPathUtilitities.expandMacro(val, MakeConfiguration.CND_CONF_MACRO, getName()); // NOI18N
        val = CndPathUtilitities.expandMacro(val, MakeConfiguration.CND_DISTDIR_MACRO, MakeConfiguration.DIST_FOLDER); // NOI18N
        val = CndPathUtilitities.expandMacro(val, MakeConfiguration.CND_BUILDDIR_MACRO, MakeConfiguration.BUILD_FOLDER); // NOI18N
        return val;
    }

    /*
     * Special version of IntConfiguration
     * Names are shifted by offset to match value and limit choice
     */
    private final static class ManagedIntConfiguration extends IntConfiguration {

        private int offset;

        public ManagedIntConfiguration(IntConfiguration master, int def, String[] names, String[] options, int offset) {
            super(master, def, names, options);
            this.offset = offset;
        }

        @Override
        public void setValue(String s) {
            String[] names = getNames();
            if (s != null) {
                for (int i = 0; i < names.length; i++) {
                    if (s.equals(names[i])) {
                        setValue(i + offset);
                        break;
                    }
                }
            }
        }

        @Override
        public String getName() {
            return getNames()[getValue() - offset];
        }
    }

    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(MakeConfiguration.class, s);
    }
}
