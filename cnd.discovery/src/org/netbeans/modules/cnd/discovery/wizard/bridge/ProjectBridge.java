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

package org.netbeans.modules.cnd.discovery.wizard.bridge;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetUtils;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.makeproject.api.ProjectGenerator;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.makeproject.api.configurations.BasicCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.BooleanConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.FolderConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public class ProjectBridge {
    private String baseFolder;
    private MakeConfigurationDescriptor makeConfigurationDescriptor;
    private Project project;
    private Set<Project> resultSet = new HashSet<Project>();
    private Map<String,Item> canonicalItems;
    
    public ProjectBridge(Project project) {
        this.project = project;
        baseFolder = File.separator+project.getProjectDirectory().getPath();
        resultSet.add(project);
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        if (pdp != null) {
            makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
        }
    }

    public boolean isValid(){
        return makeConfigurationDescriptor != null;
    }
    
    public ProjectBridge(String baseFolder) throws IOException{
        this.baseFolder = baseFolder;
        // TODO: create localhost based project
        MakeConfiguration extConf = new MakeConfiguration(baseFolder, "Default", MakeConfiguration.TYPE_MAKEFILE, HostInfoUtils.LOCALHOST); // NOI18N
        String workingDir = baseFolder;
        String workingDirRel = CndPathUtilitities.toRelativePath(baseFolder, CndPathUtilitities.naturalize(workingDir));
        workingDirRel = CndPathUtilitities.normalize(workingDirRel);
        extConf.getMakefileConfiguration().getBuildCommandWorkingDir().setValue(workingDirRel);
        String prjName = "DiscoveryProject"; // NOI18N
        ProjectGenerator.ProjectParameters prjParams = new ProjectGenerator.ProjectParameters(prjName, baseFolder);// NOI18N
        prjParams.setOpenFlag(true).setConfiguration(extConf);
        project = ProjectGenerator.createBlankProject(prjParams); // NOI18N
        resultSet.add(project);
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
    }
    
    public Folder createFolder(Folder parent, String name){
        return new Folder(makeConfigurationDescriptor, parent, name, name, true);
    }
    
    public void addSourceRoot(String path){
        makeConfigurationDescriptor.addSourceRootRaw(path);
    }
    
    /**
     * Create new item. Path is converted to relative.
     */
    public Item createItem(String path){
        return new Item(getRelativepath(path));
    }
    
    /**
     * Find project item by relative path.
     */
    public Item getProjectItem(String path){
        Item item = makeConfigurationDescriptor.findProjectItemByPath(path);
        if (item == null){
            if (!CndPathUtilitities.isPathAbsolute(path)) {
                path = CndPathUtilitities.toAbsolutePath(baseFolder, path);
            }
            item = findByCanonicalName(path);
        }
        return item;
    }

    /**
     * Check needed header extensions and store list in the NB/project properties.
     * @param needAdd list of needed extensions of header files.
     */
    public void checkForNewExtensions(Set<String> needAdd){
        Set<String> extensions = new HashSet<String>();
        for(String name : needAdd){
            int i = name.replace('\\', '/').lastIndexOf('/');
            if (i >= 0){
                name = name.substring(i+1);
            }
            i = name.lastIndexOf('.');
            if (i > 0){
                String extension = name.substring(i+1);
                if (extension.length()>0) {
                    extensions.add(extension);
                }
            }
        }
        Set<Item> old = new HashSet<Item>();
        for(Item item : getAllSources()) {
            old.add(item);
        }
        if (makeConfigurationDescriptor.addAdditionalHeaderExtensions(extensions)) {
            for(Item item : getAllSources()) {
                if (!old.contains(item)) {
                    ProjectBridge.setExclude(item,true);
                }
            }
        }
    }

    private Item findByCanonicalName(String path){
        if (canonicalItems == null) {
            canonicalItems = new HashMap<String,Item>();
            for(Item item : makeConfigurationDescriptor.getProjectItems()){
                canonicalItems.put(item.getCanonicalFile().getAbsolutePath(),item);
            }
        }
        return canonicalItems.get(path);
    }
    
    public Object getAuxObject(Item item){
        MakeConfiguration makeConfiguration = item.getFolder().getConfigurationDescriptor().getActiveConfiguration();
        ItemConfiguration itemConfiguration = item.getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(item.getPath()));
        return itemConfiguration;
    }
    
    public void setAuxObject(Item item, Object pao){
        if (pao instanceof ItemConfiguration) {
            ItemConfiguration conf = (ItemConfiguration)pao;
            MakeConfiguration makeConfiguration = item.getFolder().getConfigurationDescriptor().getActiveConfiguration();
            ItemConfiguration itemConfiguration = item.getItemConfiguration(makeConfiguration);
            switch(itemConfiguration.getTool()) {
                case CCCompiler:
                    itemConfiguration.setCCCompilerConfiguration(conf.getCCCompilerConfiguration());
                    break;
                case CCompiler:
                    itemConfiguration.setCCompilerConfiguration(conf.getCCompilerConfiguration());
                    break;
                case CustomTool:
                    itemConfiguration.setCustomToolConfiguration(conf.getCustomToolConfiguration());
                    break;
            }
        }
    }
    
    /**
     * Convert absolute path to relative.
     * Converter does some simplifications:
     * /some/../ => /
     * /./ => /
     */
    public String getRelativepath(String path){
        if (Utilities.isWindows()) {
            path = path.replace('/', File.separatorChar);
        }
        path = CndPathUtilitities.toRelativePath(makeConfigurationDescriptor.getBaseDir(), path);
        path = cutLocalRelative(path);
        path = CndPathUtilitities.normalize(path);
        return path;
    }
    
    private static final String PATTERN_1 = File.separator+"."+File.separator; // NOI18N
    private static final String PATTERN_2 = File.separator+"."; // NOI18N
    private static final String PATTERN_3 = File.separator+".."+File.separator; // NOI18N
    private static final String PATTERN_4 = File.separator+".."; // NOI18N
    private String cutLocalRelative(String path){
        String pattern = PATTERN_1;
        while(true) {
            int i = path.indexOf(pattern);
            if (i < 0){
                break;
            }
            path = path.substring(0,i+1)+path.substring(i+pattern.length());
        }
        pattern = PATTERN_2;
        if (path.endsWith(pattern)){
            path = path.substring(0,path.length()-pattern.length());
        }
        pattern = PATTERN_3;
        while(true) {
            int i = path.indexOf(pattern);
            if (i < 0){
                break;
            }
            int k = -1;
            for (int j = i-1; j >= 0; j-- ){
                if ( path.charAt(j)==File.separatorChar){
                    k = j;
                    break;
                }
            }
            if (k<0) {
                break;
            }
            path = path.substring(0,k+1)+path.substring(i+pattern.length());
        }
        pattern = PATTERN_4;
        if (path.endsWith(pattern)){
            int k = -1;
            for (int j = path.length()-pattern.length()-1; j >= 0; j-- ){
                if ( path.charAt(j)==File.separatorChar){
                    k = j;
                    break;
                }
            }
            if (k>0) {
                path = path.substring(0,k);
            }
        }
        return path;
    }
    
    public Item[] getAllSources(){
        return makeConfigurationDescriptor.getProjectItems();
    }
    
    public Folder getRoot(){
        Folder folder = makeConfigurationDescriptor.getLogicalFolders();
        List<Folder> sources = folder.getFolders();
        for (Folder sub : sources){
            if (sub.isProjectFiles()) {
                if (MakeConfigurationDescriptor.SOURCE_FILES_FOLDER.equals(sub.getName())) {
                    return sub;
                }
            }
        }
        return folder;
    }
    
    public void save(){
        makeConfigurationDescriptor.save();
    }
    
    public Set<Project> getResult(){
        makeConfigurationDescriptor.checkForChangedItems(project, null, null);
        return resultSet;
    }
    
    public void setupProject(List<String> includes, List<String> macros, boolean isCPP){
        Configuration c = makeConfigurationDescriptor.getActiveConfiguration();
        if (c instanceof MakeConfiguration) {
            for(int i = 0; i < includes.size(); i++) {
                includes.set(i, getString(includes.get(i)));
            }
            for(int i = 0; i < macros.size(); i++) {
                macros.set(i, getString(macros.get(i)));
            }
            MakeConfiguration extConf = (MakeConfiguration)c;
            if (isCPP) {
                extConf.getCCCompilerConfiguration().getIncludeDirectories().setValue(includes);
                extConf.getCCCompilerConfiguration().getPreprocessorConfiguration().setValue(macros);
                extConf.getCCCompilerConfiguration().getIncludeDirectories().setDirty(true);
                extConf.getCCCompilerConfiguration().getPreprocessorConfiguration().setDirty(true);
            } else {
                extConf.getCCompilerConfiguration().getIncludeDirectories().setValue(includes);
                extConf.getCCompilerConfiguration().getPreprocessorConfiguration().setValue(macros);
                extConf.getCCompilerConfiguration().getIncludeDirectories().setDirty(true);
                extConf.getCCompilerConfiguration().getPreprocessorConfiguration().setDirty(true);
            }
        }
        makeConfigurationDescriptor.setModified();
    }

    public CCCCompilerConfiguration getFolderConfiguration(boolean isCPP, Folder folder) {
        MakeConfiguration makeConfiguration = folder.getConfigurationDescriptor().getActiveConfiguration();
        //FolderConfiguration folderConfiguration = (FolderConfiguration)makeConfiguration.getAuxObject(folder.getId());
        FolderConfiguration folderConfiguration = folder.getFolderConfiguration(makeConfiguration);
        if (folderConfiguration == null) {
            return null;
        }
        if (isCPP) {
            return folderConfiguration.getCCCompilerConfiguration();
        } else {
            return folderConfiguration.getCCompilerConfiguration();
        }
    }

    public void setupFolder(List<String> includes, boolean inheriteIncludes, List<String> macros, boolean inheriteMacros, boolean isCPP, Folder folder) {
        CCCCompilerConfiguration cccc = getFolderConfiguration(isCPP, folder);
        if (cccc == null) {
            return;
        }
        for(int i = 0; i < includes.size(); i++) {
            includes.set(i, getString(includes.get(i)));
        }
        for(int i = 0; i < macros.size(); i++) {
            macros.set(i, getString(macros.get(i)));
        }
        cccc.getIncludeDirectories().setValue(includes);
        cccc.getInheritIncludes().setValue(inheriteIncludes);
        cccc.getPreprocessorConfiguration().setValue(macros);
        cccc.getInheritPreprocessor().setValue(inheriteMacros);
    }
    
    public static void setExclude(Item item, boolean exclude){
        MakeConfiguration makeConfiguration = item.getFolder().getConfigurationDescriptor().getActiveConfiguration();
        ItemConfiguration itemConfiguration = item.getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(item.getPath()));
        if (itemConfiguration == null) {
            return;
        }
        BooleanConfiguration excl =itemConfiguration.getExcluded();
        if (excl.getValue() ^ exclude){
            excl.setValue(exclude);
        }
        //itemConfiguration.setTool(Tool.CustomTool);
    }
    
    public void setHeaderTool(Item item){
        MakeConfiguration makeConfiguration = item.getFolder().getConfigurationDescriptor().getActiveConfiguration();
        ItemConfiguration itemConfiguration = item.getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(item.getPath()));
        if (itemConfiguration == null) {
            return;
        }
        if (itemConfiguration.getTool() == PredefinedToolKind.CCCompiler || itemConfiguration.getTool() == PredefinedToolKind.CCompiler) {
            itemConfiguration.setTool(PredefinedToolKind.CustomTool);
        }
    }

    public void setSourceTool(Item item, boolean isCPP){
        MakeConfiguration makeConfiguration = item.getFolder().getConfigurationDescriptor().getActiveConfiguration();
        ItemConfiguration itemConfiguration = item.getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(item.getPath()));
        if (itemConfiguration == null) {
            return;
        }
        if (isCPP) {
            if (itemConfiguration.getTool() != PredefinedToolKind.CCCompiler) {
                itemConfiguration.setTool(PredefinedToolKind.CCCompiler);
            }
        } else {
            if (itemConfiguration.getTool() != PredefinedToolKind.CCompiler) {
                itemConfiguration.setTool(PredefinedToolKind.CCompiler);
            }
        }
    }
    
    public CCCCompilerConfiguration getItemConfiguration(Item item) {
        MakeConfiguration makeConfiguration = item.getFolder().getConfigurationDescriptor().getActiveConfiguration();
        ItemConfiguration itemConfiguration = item.getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(item.getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) {
            return null;
        }
        BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
        if (compilerConfiguration instanceof CCCCompilerConfiguration) {
            return (CCCCompilerConfiguration) compilerConfiguration;
        }
        return null;
    }

    public void setupFile(String compilepath, List<String> includes, boolean inheriteIncludes, List<String> macros, boolean inheriteMacros, Item item) {
        MakeConfiguration makeConfiguration = item.getFolder().getConfigurationDescriptor().getActiveConfiguration();
        ItemConfiguration itemConfiguration = item.getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(item.getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) {
            return;
        }
        BooleanConfiguration excl =itemConfiguration.getExcluded();
        if (excl.getValue()){
            excl.setValue(false);
        }
        for(int i = 0; i < includes.size(); i++) {
            includes.set(i, getString(includes.get(i)));
        }
        for(int i = 0; i < macros.size(); i++) {
            macros.set(i, getString(macros.get(i)));
        }
        BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
        if (compilerConfiguration instanceof CCCCompilerConfiguration) {
            CCCCompilerConfiguration cccCompilerConfiguration = (CCCCompilerConfiguration)compilerConfiguration;
            cccCompilerConfiguration.getIncludeDirectories().setValue(includes);
            cccCompilerConfiguration.getInheritIncludes().setValue(inheriteIncludes);
            cccCompilerConfiguration.getPreprocessorConfiguration().setValue(macros);
            cccCompilerConfiguration.getInheritPreprocessor().setValue(inheriteMacros);
        }
    }

    public static void fixFileMacros(Map<String,String> macros, Item item) {
        MakeConfiguration makeConfiguration = item.getFolder().getConfigurationDescriptor().getActiveConfiguration();
        ItemConfiguration itemConfiguration = item.getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(item.getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) {
            return;
        }
        BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
        if (compilerConfiguration instanceof CCCCompilerConfiguration) {
            Set<String> set = new HashSet<String>(item.getUserMacroDefinitions());
            CCCCompilerConfiguration cccCompilerConfiguration = (CCCCompilerConfiguration)compilerConfiguration;
            List<String> list = new ArrayList<String>(cccCompilerConfiguration.getPreprocessorConfiguration().getValue());
            for(Map.Entry<String,String> entry : macros.entrySet()) {
                String s;
                if (entry.getValue() != null) {
                    s = entry.getKey()+"="+entry.getValue(); // NOI18N
                } else {
                    s = entry.getKey();
                }
                boolean find = set.contains(s);
                if (!find && (entry.getValue() == null || "".equals(entry.getValue()))) { // NOI18N
                    find = set.contains(s+"=1"); // NOI18N
                    if (!find) {
                        find = set.contains(s+"="); // NOI18N
                    }
                }
                if (!find && ("1".equals(entry.getValue()) || "".equals(entry.getValue()))) { // NOI18N
                    find = set.contains(entry.getKey());
                }
                if (!find) {
                    list.add(s);
                }
            }
            cccCompilerConfiguration.getPreprocessorConfiguration().setValue(list);
        }
    }

    private Map<String, String> cache = new HashMap<String, String>();
    private String getString(String s) {
        String res = cache.get(s);
        if (res == null) {
            cache.put(s, s);
            return s;
        }
        return res;
    }

    void dispose(){
        cache.clear();
    }
    
    private CompilerSet getCompilerSet(){
        MakeConfiguration makeConfiguration = makeConfigurationDescriptor.getActiveConfiguration();
        final ExecutionEnvironment env = makeConfiguration.getDevelopmentHost().getExecutionEnvironment();
        return CompilerSetManager.get(env).getCompilerSets().get(makeConfiguration.getCompilerSet().getValue());
    }

    public String getCygwinDrive(){
        String res =CompilerSetUtils.getCygwinBase();
        if (res != null && res.endsWith("/")){ // NOI18N
            res = res.substring(0,res.length()-1);
        }
        return res;
    }

    public String getCompilerFlavor(){
        return getCompilerSet().getCompilerFlavor().toString();
    }

    public String getCompilerDirectory(){
        return getCompilerSet().getDirectory();
    }
    
    private List<String> systemIncludePathsC;
    private List<String> systemIncludePathsCpp;
    public List<String> getSystemIncludePaths(boolean isCPP) {
        List<String> systemIncludePaths;
        if (isCPP) {
            systemIncludePaths = systemIncludePathsCpp;
        } else {
            systemIncludePaths = systemIncludePathsC;
        }
        if (systemIncludePaths == null) {
            systemIncludePaths = new ArrayList<String>();
            CompilerSet compilerSet = getCompilerSet();
            AbstractCompiler compiler;
            if (isCPP) {
                compiler = (AbstractCompiler)compilerSet.getTool(PredefinedToolKind.CCCompiler);
            } else {
                compiler = (AbstractCompiler)compilerSet.getTool(PredefinedToolKind.CCompiler);
            }
            for(Object o :compiler.getSystemIncludeDirectories()){
                String path = (String)o;
                systemIncludePaths.add(fixWindowsPath(path));
            }
            if (isCPP) {
                systemIncludePathsCpp = systemIncludePaths;
            } else {
                systemIncludePathsC = systemIncludePaths;
            }
        }
        return systemIncludePaths;
    }

    private static final String CYG_DRIVE_UNIX = "/cygdrive/"; // NOI18N
    private String fixWindowsPath(String path){
        if (Utilities.isWindows()) {
            // use unix style path 
            path = path.replace('\\', '/');
            // fix /cygdrive/d/gcc/bin/../lib/gcc/i686-pc-cygwin/3.4.4/include
            int i = path.indexOf(CYG_DRIVE_UNIX);
            if (i >= 0 && path.length() > i+CYG_DRIVE_UNIX.length()+1) {
                path = Character.toUpperCase(path.charAt(i+CYG_DRIVE_UNIX.length()))+":"+ // NOI18N
                        path.substring(i+CYG_DRIVE_UNIX.length()+1);
            }
        }
        return path;
    }
    
    private Map<String,String> systemMacroDefinitionsC;
    private Map<String,String> systemMacroDefinitionsCpp;
    public Map<String,String> getSystemMacroDefinitions(boolean isCPP) {
        Map<String,String> systemMacroDefinitions;
        if (isCPP) {
            systemMacroDefinitions = systemMacroDefinitionsCpp;
        } else {
            systemMacroDefinitions = systemMacroDefinitionsC;
        }
        if (systemMacroDefinitions == null) {
            systemMacroDefinitions = new HashMap<String,String>();
            CompilerSet compilerSet = getCompilerSet();
            AbstractCompiler compiler;
            if (isCPP) {
                compiler = (AbstractCompiler)compilerSet.getTool(PredefinedToolKind.CCCompiler);
            } else {
                compiler = (AbstractCompiler)compilerSet.getTool(PredefinedToolKind.CCompiler);
            }
            for(Object o :compiler.getSystemPreprocessorSymbols()){
                String macro = (String)o;
                int i = macro.indexOf('=');
                if (i>0){
                    systemMacroDefinitions.put(macro.substring(0,i), macro.substring(i+1).trim());
                } else {
                    systemMacroDefinitions.put(macro, null);
                }
            }
            if (isCPP) {
                systemMacroDefinitionsCpp = systemMacroDefinitions;
            } else {
                systemMacroDefinitionsC = systemMacroDefinitions;
            }
        }
        return systemMacroDefinitions;
    }
}
