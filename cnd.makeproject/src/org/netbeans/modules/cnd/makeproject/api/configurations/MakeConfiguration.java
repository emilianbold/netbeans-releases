/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.MakeOptions;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.makeproject.configurations.ui.IntNodeProp;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platforms;
import org.netbeans.modules.cnd.makeproject.configurations.ui.BooleanNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.ProjectsNodeProp;
import org.netbeans.modules.cnd.settings.CppSettings;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

public class MakeConfiguration extends Configuration {
    public static final String MAKEFILE_IMPL = "Makefile-impl.mk"; // NOI18N
    public static final String BUILD_FOLDER = "build"; // NOI18N
    public static final String DIST_FOLDER = "dist"; // NOI18N
    public static final String EXT_FOLDER = "_ext"; // NOI18N
    
    public static final String OBJECTDIR_MACRO_NAME = "OBJECTDIR"; // NOI18N
    public static final String OBJECTDIR_MACRO = "${" + OBJECTDIR_MACRO_NAME + "}"; // NOI18N
    
    // Project Types
    private static String[] TYPE_NAMES = {
        getString("MakefileName"),
        getString("ApplicationName"),
        getString("DynamicLibraryName"),
        getString("StaticLibraryName"),
    };
    public static final int TYPE_MAKEFILE = 0;
    public static final int TYPE_APPLICATION = 1;
    public static final int TYPE_DYNAMIC_LIB = 2;
    public static final int TYPE_STATIC_LIB = 3;
    
    // Configurations
    private IntConfiguration configurationType;
    private MakefileConfiguration makefileConfiguration;
    private CompilerSetConfiguration compilerSet;
    private BooleanConfiguration gdbRequired; // GRP - FIXME: Do we need gdb here?
    private BooleanConfiguration cRequired;
    private BooleanConfiguration cppRequired;
    private BooleanConfiguration fortranRequired;
    private IntConfiguration platform;
    private BooleanConfiguration dependencyChecking;
    private CCompilerConfiguration cCompilerConfiguration;
    private CCCompilerConfiguration ccCompilerConfiguration;
    private FortranCompilerConfiguration fortranCompilerConfiguration;
    private LinkerConfiguration linkerConfiguration;
    private ArchiverConfiguration archiverConfiguration;
    private RequiredProjectsConfiguration requiredProjectsConfiguration;
    
    // Constructors
    public MakeConfiguration(MakeConfigurationDescriptor makeConfigurationDescriptor, String name, int configurationTypeValue) {
        this(makeConfigurationDescriptor.getBaseDir(), name, configurationTypeValue);
    }
    
    public MakeConfiguration(String baseDir, String name, int configurationTypeValue) {
        super(baseDir, name);
        configurationType = new IntConfiguration(null, configurationTypeValue, TYPE_NAMES, null);
        compilerSet = new CompilerSetConfiguration(null, getDefaultCompilerSetIndex(), getCompilerSetDisplayNames(), getCompilerSetNames());
        cRequired = new BooleanConfiguration(null, CppSettings.getDefault().isCRequired());
        cppRequired = new BooleanConfiguration(null, CppSettings.getDefault().isCppRequired());
        fortranRequired = new BooleanConfiguration(null, CppSettings.getDefault().isFortranRequired());
        platform = new IntConfiguration(null, MakeOptions.getInstance().getPlatform(), Platforms.getPlatformDisplayNames(), null);
        makefileConfiguration = new MakefileConfiguration(this);
        dependencyChecking = new BooleanConfiguration(null, isMakefileConfiguration() ? false : MakeOptions.getInstance().getDepencyChecking());
        cCompilerConfiguration = new CCompilerConfiguration(baseDir, null);
        ccCompilerConfiguration = new CCCompilerConfiguration(baseDir, null);
        fortranCompilerConfiguration = new FortranCompilerConfiguration(baseDir, null);
        linkerConfiguration = new LinkerConfiguration(this);
        archiverConfiguration = new ArchiverConfiguration(this);
	requiredProjectsConfiguration = new RequiredProjectsConfiguration();
    }
    
    public void setMakefileConfiguration(MakefileConfiguration makefileConfiguration) {
        this.makefileConfiguration = makefileConfiguration;
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
    
    public CompilerSetConfiguration getCompilerSet() {
        return compilerSet;
    }
    
    public BooleanConfiguration getCRequired() {
        return cRequired;
    }
    
    public BooleanConfiguration getCppRequired() {
        return cppRequired;
    }
    
    public BooleanConfiguration getFortranRequired() {
        return fortranRequired;
    }
    
    public void setCompilerSet(CompilerSetConfiguration compilerSet) {
        this.compilerSet = compilerSet;
    }
    
    public void setCRequired(BooleanConfiguration cRequired) {
        this.cRequired = cRequired;
    }
    
    public void setCppRequired(BooleanConfiguration cppRequired) {
        this.cppRequired = cppRequired;
    }
    
    public void setFortranRequired(BooleanConfiguration fortranRequired) {
        this.fortranRequired = fortranRequired;
    }
    
    public IntConfiguration getPlatform() {
        return platform;
    }
    
    public void setPlatform(IntConfiguration platform) {
        this.platform = platform;
    }
    
    public boolean isApplicationConfiguration() {
        return getConfigurationType().getValue() == TYPE_APPLICATION;
    }
    
    public boolean isCompileConfiguration() {
        return getConfigurationType().getValue() == TYPE_APPLICATION || getConfigurationType().getValue() == TYPE_DYNAMIC_LIB || getConfigurationType().getValue() == TYPE_STATIC_LIB;
    }
    
    public boolean isLibraryConfiguration() {
        return getConfigurationType().getValue() == TYPE_DYNAMIC_LIB || getConfigurationType().getValue() == TYPE_STATIC_LIB;
    }
    
    public boolean isLinkerConfiguration() {
        return getConfigurationType().getValue() == TYPE_APPLICATION || getConfigurationType().getValue() == TYPE_DYNAMIC_LIB;
    }
    
    public boolean isMakefileConfiguration() {
        return getConfigurationType().getValue() == TYPE_MAKEFILE;
    }
    
    public boolean isDynamicLibraryConfiguration() {
        return getConfigurationType().getValue() == TYPE_DYNAMIC_LIB;
    }
    
    public boolean isArchiverConfiguration() {
        return getConfigurationType().getValue() == TYPE_STATIC_LIB;
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
    
    public void setLinkerConfiguration(LinkerConfiguration linkerConfiguration) {
        this.linkerConfiguration = linkerConfiguration;
    }
    
    public LinkerConfiguration getLinkerConfiguration() {
        return linkerConfiguration;
    }
    
    public void setArchiverConfiguration(ArchiverConfiguration archiverConfiguration) {
        this.archiverConfiguration = archiverConfiguration;
    }
    
    public ArchiverConfiguration getArchiverConfiguration() {
        return archiverConfiguration;
    }
    
    // LibrariesConfiguration
    public RequiredProjectsConfiguration getRequiredProjectsConfiguration() {
	return requiredProjectsConfiguration;
    }
    public void setRequiredProjectsConfiguration(RequiredProjectsConfiguration requiredProjectsConfiguration) {
	this.requiredProjectsConfiguration = requiredProjectsConfiguration;
    }
    
    public void assign(Configuration conf) {
        MakeConfiguration makeConf = (MakeConfiguration)conf;
        setName(makeConf.getName());
        setBaseDir(makeConf.getBaseDir());
        getConfigurationType().assign(makeConf.getConfigurationType());
        getCompilerSet().assign(makeConf.getCompilerSet());
        getCRequired().assign(makeConf.getCRequired());
        getCppRequired().assign(makeConf.getCppRequired());
        getFortranRequired().assign(makeConf.getFortranRequired());
        getPlatform().assign(makeConf.getPlatform());
        getDependencyChecking().assign(makeConf.getDependencyChecking());
        
        getMakefileConfiguration().assign(makeConf.getMakefileConfiguration());
        getCCompilerConfiguration().assign(makeConf.getCCompilerConfiguration());
        getCCCompilerConfiguration().assign(makeConf.getCCCompilerConfiguration());
        getFortranCompilerConfiguration().assign(makeConf.getFortranCompilerConfiguration());
        getLinkerConfiguration().assign(makeConf.getLinkerConfiguration());
        getArchiverConfiguration().assign(makeConf.getArchiverConfiguration());
	getRequiredProjectsConfiguration().assign(makeConf.getRequiredProjectsConfiguration());
        
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
                System.err.println("Configuration - assign: Object ID "+id+" do not found"); // NOI18N
            }
        }
    }
    
    public Configuration cloneConf() {
        return (Configuration)clone();
    }
    
    public Configuration copy() {
        MakeConfiguration copy = new MakeConfiguration(getBaseDir(), getName(), getConfigurationType().getValue());
        copy.assign(this);
        // copy aux objects
        ConfigurationAuxObject[] auxs = getAuxObjects();
        Vector copiedAuxs = new Vector();
        for (int i = 0; i < auxs.length; i++) {
            if (auxs[i] instanceof ItemConfiguration) {
                copiedAuxs.add(((ItemConfiguration)auxs[i]).copy(copy));
            } else {
                copiedAuxs.add(auxs[i]);
            }
        }
        copy.setAuxObjects(copiedAuxs);
        return copy;
    }
    
    // Cloning
    public Object clone() {
        MakeConfiguration clone = new MakeConfiguration(getBaseDir(), getName(), getConfigurationType().getValue());
        super.cloneConf(clone);
        clone.setCloneOf(this);
        
        clone.setCompilerSet((CompilerSetConfiguration) getCompilerSet().clone());
        clone.setCRequired((BooleanConfiguration) getCRequired().clone());;
        clone.setCppRequired((BooleanConfiguration) getCppRequired().clone());;
        clone.setFortranRequired((BooleanConfiguration) getFortranRequired().clone());
        clone.setPlatform((IntConfiguration)getPlatform().clone());
        clone.setMakefileConfiguration((MakefileConfiguration)getMakefileConfiguration().clone());
        clone.setDependencyChecking((BooleanConfiguration)getDependencyChecking().clone());
        clone.setCCompilerConfiguration((CCompilerConfiguration)getCCompilerConfiguration().clone());
        clone.setCCCompilerConfiguration((CCCompilerConfiguration)getCCCompilerConfiguration().clone());
        clone.setFortranCompilerConfiguration((FortranCompilerConfiguration)getFortranCompilerConfiguration().clone());
        clone.setLinkerConfiguration((LinkerConfiguration)getLinkerConfiguration().clone());
        clone.setArchiverConfiguration((ArchiverConfiguration)getArchiverConfiguration().clone());
	clone.setRequiredProjectsConfiguration((RequiredProjectsConfiguration)getRequiredProjectsConfiguration().clone());
        
        // Clone all the aux objects
        //Vector clonedAuxObjects = new Vector();
        //for (Enumeration e = auxObjects.elements() ; e.hasMoreElements() ;) {
        //    ConfigurationAuxObject o = (ConfigurationAuxObject)e.nextElement();
        //    ConfigurationAuxObject clone2 = (ConfigurationAuxObject)o.clone();
        //    clonedAuxObjects.add(clone2);
        //}
        ConfigurationAuxObject[] objects = getAuxObjects();
        List<ConfigurationAuxObject> clonedAuxObjects = new ArrayList<ConfigurationAuxObject>();
        for(int i = 0; i < objects.length; i++){
            clonedAuxObjects.add((ConfigurationAuxObject)objects[i].clone());
        }
        clone.setAuxObjects(clonedAuxObjects);
        return clone;
    }
    
    public Sheet getGeneralSheet(Project project) {
        Sheet sheet = new Sheet();
        
        Sheet.Set set = new Sheet.Set();
        set.setName("Project Defaults"); // NOI18N
        set.setDisplayName(getString("ProjectDefaultsTxt"));
        set.setShortDescription(getString("ProjectDefaultsHint"));
        set.put(new ProjectLocationNodeProp(project));
        set.put(new IntNodeProp(getCompilerSet(), true, "CompilerSCollection", getString("CompilerCollectionTxt"), getString("CompilerCollectionHint"))); // NOI18N
        set.put(new BooleanNodeProp(getCRequired(), true, "cRequired", getString("CRequiredTxt"), getString("CRequiredHint"))); // NOI18N
        set.put(new BooleanNodeProp(getCppRequired(), true, "cppRequired", getString("CppRequiredTxt"), getString("CppRequiredHint"))); // NOI18N
        if (CppSettings.getDefault().isFortranEnabled()) {
            set.put(new BooleanNodeProp(getFortranRequired(), true, "fortranRequired", getString("FortranRequiredTxt"), getString("FortranRequiredHint"))); // NOI18N
        }
        set.put(new IntNodeProp(getPlatform(), true, "Platform", getString("PlatformTxt"), getString("PlatformHint"))); // NOI18N
        set.put(new IntNodeProp(getConfigurationType(), true, "ConfigurationType", getString("ConfigurationTypeTxt"), getString("ConfigurationTypeHint"))); // NOI18N
        sheet.put(set);
        
        if (isCompileConfiguration()) {
            set = Sheet.createExpertSet();
            set.put(new BooleanNodeProp(getDependencyChecking(), true, "DependencyChecking", getString("DependencyCheckingTxt"), getString("DependencyCheckingHint"))); // NOI18N
            sheet.put(set);
        }
        
        return sheet;
    }
    
    public Sheet getCompilerSetSheet() {
        Sheet sheet = new Sheet();
        
        Sheet.Set set = new Sheet.Set();
        set.setName("Compiler Collection"); // NOI18N
        set.setDisplayName(getString("CompilerCollectionTxt"));
        set.setShortDescription(getString("CompilerCollectionHint"));
        set.put(new IntNodeProp(getCompilerSet(), true, "CompilerCollection", getString("CompilerCollectionTxt"), getString("CompilerCollectionHint"))); // NOI18N
        sheet.put(set);
        
        return sheet;
    }
    
    public Sheet getRequiredProjectsSheet(Project project, MakeConfiguration conf) {
	Sheet sheet = new Sheet();
	String[] texts = new String[] {getString("ProjectsTxt1"), getString("ProjectsHint"), getString("ProjectsTxt2"), getString("AllOptionsTxt2")};

	Sheet.Set set2 = new Sheet.Set();
	set2.setName("Projects"); // NOI18N
	set2.setDisplayName(getString("ProjectsTxt1"));
	set2.setShortDescription(getString("ProjectsHint"));
	set2.put(new ProjectsNodeProp(getRequiredProjectsConfiguration(), project, conf, getBaseDir(), texts));
	sheet.put(set2);

	return sheet;
    }
    
    private class ProjectLocationNodeProp extends Node.Property {
        String projectLocation;
        
        public ProjectLocationNodeProp(Project project) {
            super(String.class);
            FileObject projectFolder = project.getProjectDirectory();
            File pf = FileUtil.toFile( projectFolder );
            projectLocation = pf.getPath();
        }
        
        public String getName() {
            return getString("ProjectLocationTxt");
        }
        
        public Object getValue() {
            return projectLocation;
        }
        
        public void setValue(Object v) {
            ;//
        }
        
        public boolean canWrite() {
            return false;
        }
        
        public boolean canRead() {
            return true;
        }
    }
    
    public boolean hasCPPFiles(MakeConfigurationDescriptor configurationDescriptor) {
        Item[] items = configurationDescriptor.getProjectItems();
        for (int x = 0; x < items.length; x++) {
            ItemConfiguration itemConfiguration = items[x].getItemConfiguration(this);//ItemConfiguration)getAuxObject(ItemConfiguration.getId(items[x].getPath()));
            if (itemConfiguration.getExcluded().getValue())
                continue;
            if (itemConfiguration.getTool() == Tool.CCCompiler) {
                return true;
            }
        }
        return false;
    }
    
    public boolean hasFortranFiles(MakeConfigurationDescriptor configurationDescriptor) {
        Item[] items = configurationDescriptor.getProjectItems();
        for (int x = 0; x < items.length; x++) {
            ItemConfiguration itemConfiguration = items[x].getItemConfiguration(this);//(ItemConfiguration)getAuxObject(ItemConfiguration.getId(items[x].getPath()));
            if (itemConfiguration.getExcluded().getValue())
                continue;
            if (itemConfiguration.getTool() == Tool.FortranCompiler) {
                return true;
            }
        }
        return false;
    }
    
    public String getVariant() {
        String ret = "";
        ret += CompilerSetManager.getDefault().getCompilerSet(getCompilerSet().getValue()).getName() + "-"; // NOI18N
        ret += Platforms.getPlatform(getPlatform().getValue()).getName();
        return ret;
    }
    
    public Set/*<Project>*/ getSubProjects() {
        Set subProjects = new HashSet();
        LibrariesConfiguration librariesConfiguration = getLinkerConfiguration().getLibrariesConfiguration();
        LibraryItem[] libraryItems = librariesConfiguration.getLibraryItemsAsArray();
        for (int j = 0; j < libraryItems.length; j++) {
            if (libraryItems[j] instanceof LibraryItem.ProjectItem) {
                LibraryItem.ProjectItem projectItem = (LibraryItem.ProjectItem)libraryItems[j];
                Project project = projectItem.getProject(getBaseDir());
                if (project != null) {
                    subProjects.add(project);
                } else {
                    ; // FIXUP ERROR
                }
            }
        }
        return subProjects;
    }
    
    public Set/*<String>*/ getSubProjectLocations() {
        Set subProjectLocations = new HashSet();
        LibrariesConfiguration librariesConfiguration = getLinkerConfiguration().getLibrariesConfiguration();
        LibraryItem[] libraryItems = librariesConfiguration.getLibraryItemsAsArray();
        for (int j = 0; j < libraryItems.length; j++) {
            if (libraryItems[j] instanceof LibraryItem.ProjectItem) {
                LibraryItem.ProjectItem projectItem = (LibraryItem.ProjectItem)libraryItems[j];
                subProjectLocations.add(projectItem.getMakeArtifact().getProjectLocation());
            }
        }
        return subProjectLocations;
    }
    
    public Set/*<String>*/ getSubProjectOutputLocations() {
        Set subProjectOutputLocations = new HashSet();
        LibrariesConfiguration librariesConfiguration = getLinkerConfiguration().getLibrariesConfiguration();
        LibraryItem[] libraryItems = librariesConfiguration.getLibraryItemsAsArray();
        for (int j = 0; j < libraryItems.length; j++) {
            if (libraryItems[j] instanceof LibraryItem.ProjectItem) {
                LibraryItem.ProjectItem projectItem = (LibraryItem.ProjectItem)libraryItems[j];
                String outputLocation = IpeUtils.getDirName(projectItem.getMakeArtifact().getOutput());
                if (IpeUtils.isPathAbsolute(outputLocation))
                    subProjectOutputLocations.add(outputLocation);
                else
                    subProjectOutputLocations.add(projectItem.getMakeArtifact().getProjectLocation() + "/" + outputLocation); // NOI18N
            }
        }
        return subProjectOutputLocations;
    }
    
    public String getAbsoluteOutputValue() {
        String output;
        if (isLinkerConfiguration()) {
            output = getLinkerConfiguration().getOutputValue();
        }
        else if (isArchiverConfiguration())
            output = getArchiverConfiguration().getOutputValue();
        else if (isMakefileConfiguration())
            output = getMakefileConfiguration().getOutput().getValue();
        else
            output = null;
        
        if (output == null || IpeUtils.isPathAbsolute(output))
            return output;
        else {
            output = getBaseDir() + "/" + output; // NOI18N
            output = FilePathAdaptor.normalize(output);
            return output;
        }
    }
    
    private String[] getCompilerSetDisplayNames() {
        ArrayList<String> names = new ArrayList();
        for (CompilerSet cs : CompilerSetManager.getDefault().getCompilerSets()) {
            names.add(cs.getDisplayName());
        }
        return names.toArray(new String[0]);
    }
    
    private String[] getCompilerSetNames() {
        ArrayList<String> names = new ArrayList();
        for (CompilerSet cs : CompilerSetManager.getDefault().getCompilerSets()) {
            names.add(cs.getName());
        }
        return names.toArray(new String[0]);
    }
    
    private int getDefaultCompilerSetIndex() {
        String name = CppSettings.getDefault().getCompilerSetName();
        int i = 0;
        for (CompilerSet cs : CompilerSetManager.getDefault().getCompilerSets()) {
            if (name.equals(cs.getName())) {
                return i;
            }
            i++;
        }
        return 0; // shouldn't happen
    }
    
    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(MakeConfiguration.class, s);
    }

}
