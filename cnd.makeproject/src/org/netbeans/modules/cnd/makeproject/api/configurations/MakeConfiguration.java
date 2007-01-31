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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.Enumeration;
import java.util.Vector;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.MakeOptions;
import org.netbeans.modules.cnd.makeproject.api.compilers.CompilerSets;
import org.netbeans.modules.cnd.makeproject.api.compilers.Tool;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.makeproject.configurations.ui.IntNodeProp;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platforms;
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
    private IntConfiguration compilerSet;
    private IntConfiguration platform;
    private CCompilerConfiguration cCompilerConfiguration;
    private CCCompilerConfiguration ccCompilerConfiguration;
    private FortranCompilerConfiguration fortranCompilerConfiguration;
    private LinkerConfiguration linkerConfiguration;
    private ArchiverConfiguration archiverConfiguration;
    
    // Constructors
    public MakeConfiguration(MakeConfigurationDescriptor makeConfigurationDescriptor, String name, int configurationTypeValue) {
        this(makeConfigurationDescriptor.getBaseDir(), name, configurationTypeValue);
    }
    
    public MakeConfiguration(String baseDir, String name, int configurationTypeValue) {
        super(baseDir, name);
        configurationType = new IntConfiguration(null, configurationTypeValue, TYPE_NAMES, null);
        compilerSet = new IntConfiguration(null, MakeOptions.getInstance().getCompilerSet(), CompilerSets.getCompilerSetDisplayNames(), null);
        platform = new IntConfiguration(null, MakeOptions.getInstance().getPlatform(), Platforms.getPlatformDisplayNames(), null);
        makefileConfiguration = new MakefileConfiguration(this);
        cCompilerConfiguration = new CCompilerConfiguration(baseDir, null);
        ccCompilerConfiguration = new CCCompilerConfiguration(baseDir, null);
        fortranCompilerConfiguration = new FortranCompilerConfiguration(baseDir, null);
        linkerConfiguration = new LinkerConfiguration(this);
        archiverConfiguration = new ArchiverConfiguration(this);
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
    
    public IntConfiguration getCompilerSet() {
        return compilerSet;
    }
    
    public void setCompilerSet(IntConfiguration compilerSet) {
        this.compilerSet = compilerSet;
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
    
    public void assign(Configuration conf) {
        MakeConfiguration makeConf = (MakeConfiguration)conf;
        setName(makeConf.getName());
        setBaseDir(makeConf.getBaseDir());
        getConfigurationType().assign(makeConf.getConfigurationType());
        getCompilerSet().assign(makeConf.getCompilerSet());
        getPlatform().assign(makeConf.getPlatform());
        
        getMakefileConfiguration().assign(makeConf.getMakefileConfiguration());
        getCCompilerConfiguration().assign(makeConf.getCCompilerConfiguration());
        getCCCompilerConfiguration().assign(makeConf.getCCCompilerConfiguration());
        getFortranCompilerConfiguration().assign(makeConf.getFortranCompilerConfiguration());
        getLinkerConfiguration().assign(makeConf.getLinkerConfiguration());
        getArchiverConfiguration().assign(makeConf.getArchiverConfiguration());
        
        // do assign on all aux objects
        ConfigurationAuxObject[] auxs = getAuxObjects(); // from this profile
        ConfigurationAuxObject[] p_auxs = conf.getAuxObjects(); // from the 'other' profile
        for (int i = 0; i < auxs.length; i++)
            auxs[i].assign(p_auxs[i]);
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
        
        clone.setCompilerSet((IntConfiguration)getCompilerSet().clone());
        clone.setPlatform((IntConfiguration)getPlatform().clone());
        clone.setMakefileConfiguration((MakefileConfiguration)getMakefileConfiguration().clone());
        clone.setCCompilerConfiguration((CCompilerConfiguration)getCCompilerConfiguration().clone());
        clone.setCCCompilerConfiguration((CCCompilerConfiguration)getCCCompilerConfiguration().clone());
        clone.setFortranCompilerConfiguration((FortranCompilerConfiguration)getFortranCompilerConfiguration().clone());
        clone.setLinkerConfiguration((LinkerConfiguration)getLinkerConfiguration().clone());
        clone.setArchiverConfiguration((ArchiverConfiguration)getArchiverConfiguration().clone());
        
        // Clone all the aux objects
        Vector clonedAuxObjects = new Vector();
        for (Enumeration e = auxObjects.elements() ; e.hasMoreElements() ;) {
            ConfigurationAuxObject o = (ConfigurationAuxObject)e.nextElement();
            ConfigurationAuxObject clone2 = (ConfigurationAuxObject)o.clone();
            clonedAuxObjects.add(clone2);
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
        set.put(new IntNodeProp(getPlatform(), true, "Platform", getString("PlatformTxt"), getString("PlatformHint"))); // NOI18N
        set.put(new IntNodeProp(getConfigurationType(), true, "ConfigurationType", getString("ConfigurationTypeTxt"), getString("ConfigurationTypeHint"))); // NOI18N
        sheet.put(set);
        
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
            ItemConfiguration itemConfiguration = (ItemConfiguration)getAuxObject(ItemConfiguration.getId(items[x].getPath()));
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
            ItemConfiguration itemConfiguration = (ItemConfiguration)getAuxObject(ItemConfiguration.getId(items[x].getPath()));
            if (itemConfiguration.getExcluded().getValue())
                continue;
            if (itemConfiguration.getTool() == Tool.FortranCompiler) {
                return true;
            }
        }
        return false;
    }
    
    public String getVariant() {
        String ret = ""; // NOI18N
        ret += CompilerSets.getCompilerSet(getCompilerSet().getValue()).getName() + "-"; // NOI18N
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
    
    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(MakeConfiguration.class, s);
    }
}
