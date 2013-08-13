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

package org.netbeans.modules.cnd.discovery.wizard.api.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.project.NativeFileItem.LanguageFlavor;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetUtils;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.PredefinedMacro;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.projectimport.ImportProject;
import org.netbeans.modules.cnd.makeproject.api.ProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.configurations.BasicCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.BooleanConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider.SnapShot;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.FolderConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.filesystems.FileSystem;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public class ProjectBridge {
    private final String baseFolder;
    private final FileSystem baseFolderFileSystem;
    private final MakeConfigurationDescriptor makeConfigurationDescriptor;
    private boolean startedModification;
    private Project project;
    private Set<Project> resultSet = new HashSet<Project>();
    private Map<String,Item> canonicalItems;
    private SnapShot delta;
    
    public ProjectBridge(Project project) {
        this.project = project;
        resultSet.add(project);
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        if (pdp != null) {
            makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
            baseFolderFileSystem = makeConfigurationDescriptor.getBaseDirFileSystem();
        } else {
            makeConfigurationDescriptor = null;
            baseFolderFileSystem = CndFileUtils.getLocalFileSystem();
        }
        baseFolder = CndFileUtils.normalizePath(project.getProjectDirectory());
    }
    
    public boolean isValid(){
        return makeConfigurationDescriptor != null;
    }
    
    public void startModifications() {
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        if (pdp != null) {
            delta = pdp.startModifications();
            startedModification = true;
        }
    }
    
    public ProjectBridge(String baseFolder) throws IOException{
        this.baseFolder = baseFolder;
        // TODO: create localhost based project
        MakeConfiguration extConf = MakeConfiguration.createMakefileConfiguration(new FSPath(CndFileUtils.getLocalFileSystem(), baseFolder), "Default",  HostInfoUtils.LOCALHOST); // NOI18N
        String workingDir = baseFolder;
        String workingDirRel = CndPathUtilities.toRelativePath(baseFolder, CndPathUtilities.naturalizeSlashes(workingDir));
        workingDirRel = CndPathUtilities.normalizeSlashes(workingDirRel);
        extConf.getMakefileConfiguration().getBuildCommandWorkingDir().setValue(workingDirRel);
        String prjName = "DiscoveryProject"; // NOI18N
        ProjectGenerator.ProjectParameters prjParams = new ProjectGenerator.ProjectParameters(prjName, CndFileUtils.createLocalFile(baseFolder, prjName));// NOI18N
        prjParams.setOpenFlag(true).setConfiguration(extConf);
        project = ProjectGenerator.createBlankProject(prjParams); // NOI18N
        resultSet.add(project);
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
        baseFolderFileSystem = makeConfigurationDescriptor.getBaseDirFileSystem();
    }
    
    public Folder createFolder(Folder parent, String name){
        return new Folder(makeConfigurationDescriptor, parent, name, name, true, null);
    }
    
    public void addSourceRoot(String path){
        makeConfigurationDescriptor.addSourceRootRaw(path);
    }
    
    public FileSystem getBaseFolderFileSystem() {
        return baseFolderFileSystem;
    }
    
    /**
     * Create new item. Path is converted to relative.
     */
    public Item createItem(String path){
        return Item.createInFileSystem(baseFolderFileSystem, getRelativepath(path));
    }
    
    public static void excludeItemFromOtherConfigurations(Item item) {
        for(Configuration c : item.getFolder().getConfigurationDescriptor().getConfs().getConfigurations()) {
            if (!c.isDefault()) {
                MakeConfiguration makeConfiguration = (MakeConfiguration) c;
                ItemConfiguration itemConfiguration = item.getItemConfiguration(makeConfiguration);
                if (itemConfiguration != null) {
                    itemConfiguration.getExcluded().setValue(true);
                }
            }
        }
    }
    
    /**
     * Find project item by relative path.
     */
    public Item getProjectItem(String path){
        Item item = makeConfigurationDescriptor.findProjectItemByPath(path);
        if (item == null){
            if (!CndPathUtilities.isPathAbsolute(path)) {
                path = CndPathUtilities.toAbsolutePath(baseFolder, path);
            }
            item = findByCanonicalName(path);
        }
        return item;
    }

    /**
     * Check needed header extensions and store list in the NB/project properties.
     * @param needAdd list of needed extensions of header files.
     */
    public boolean checkForNewExtensions(Set<String> needAdd){
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
        Set<Item> old = new HashSet<Item>(Arrays.asList(getAllSources()));
        if (extensions.contains("xml")) { //NOI18N
            assert false : "Extension '.xml' cannot be a header extension"; //NOI18N
            extensions.remove("xml"); //NOI18N
        }
        boolean isChanged = false;
        if (makeConfigurationDescriptor.addAdditionalHeaderExtensions(extensions)) {
            for(Item item : getAllSources()) {
                if (!old.contains(item)) {
                    ProjectBridge.setExclude(item,true);
                    isChanged = true;
                }
            }
        }
        return isChanged;
    }

    private Item findByCanonicalName(String path){
        if (canonicalItems == null) {
            canonicalItems = new HashMap<String,Item>();
            for(Item item : makeConfigurationDescriptor.getProjectItems()){
                canonicalItems.put(item.getCanonicalPath(),item);
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
    
    public void convertIncludePaths(Set<String> set, List<String> paths, String compilePath, String filePath){
        List<String> ordinary = new ArrayList<>();
        List<Integer> incuded = new ArrayList<>();
        for (String path : paths){
            if ( path.startsWith("/") || (path.length()>1 && path.charAt(1)==':') ) {  // NOI18N
                String aPath = CndFileUtils.normalizeAbsolutePath(baseFolderFileSystem, path);
                ordinary.add(getRelativepath(aPath));
            } else {
                if (path.equals(".")) { // NOI18N
                    String aPath = CndFileUtils.normalizeAbsolutePath(baseFolderFileSystem, compilePath);
                    ordinary.add(getRelativepath(aPath));
                } else {
                    String aPath = compilePath + CndFileUtils.getFileSeparatorChar(baseFolderFileSystem) + path;
                    aPath = CndFileUtils.normalizeAbsolutePath(baseFolderFileSystem, aPath);
                    if (!CndFileUtils.isExistingDirectory(baseFolderFileSystem, aPath)) {
                        if (path.endsWith(".h") || path.endsWith(".hpp") || path.endsWith(".hxx") || path.endsWith(".def") || path.endsWith(".inc")) { // NOI18N
                            // it looks like -include <relative path>
                            // try to resolve include directive later
                            incuded.add(ordinary.size());
                        }
                    }
                    ordinary.add(getRelativepath(aPath));
                }
            }
        }
        if (isDifferentCompilePath(filePath, compilePath)){
            ordinary.add(getRelativepath(compilePath));
        }
        for(int i : incuded) {
            String inc = paths.get(i);
            for(String p : ordinary) {
                if ( !(p.startsWith("/") || (p.length()>1 && p.charAt(1)==':') ) ) {  // NOI18N
                    p = CndPathUtilities.toAbsolutePath(makeConfigurationDescriptor.getBaseDirFileObject(), p);
                }
                String aPath = p + CndFileUtils.getFileSeparatorChar(baseFolderFileSystem) + inc;
                aPath = CndFileUtils.normalizeAbsolutePath(baseFolderFileSystem, aPath);
                if (CndFileUtils.isExistingFile(baseFolderFileSystem, aPath)) {
                    ordinary.set(i, getRelativepath(aPath));
                    break;
                }
            }
        }
        set.addAll(ordinary);
    }

    private boolean isDifferentCompilePath(String name, String path){
        if (Utilities.isWindows()) {
            name = name.replace('\\', '/'); // NOI18N
        }
        int i = name.lastIndexOf('/'); // NOI18N
        if (i > 0) {
            name = name.substring(0,i);
            if (!name.equals(path)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Convert absolute path to relative.
     * Converter does some simplifications:
     * /some/../ => /
     * /./ => /
     */
    public String getRelativepath(String path){
        path = CndPathUtilities.toRelativePath(makeConfigurationDescriptor.getBaseDirFileObject(), path);
        if (Utilities.isWindows()) {
            path = path.replace('\\', '/');
        }
        path = cutLocalRelative(path);
        path = CndPathUtilities.normalizeSlashes(path);
        return path;
    }
    
    private static final String PATTERN_1 = "/./"; // NOI18N
    private static final String PATTERN_2 = "/."; // NOI18N
    private static final String PATTERN_3 = "/../"; // NOI18N
    private static final String PATTERN_4 = "/.."; // NOI18N
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
                if (path.charAt(j)=='/'){
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
                if (path.charAt(j)=='/'){
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

    public Map<String,Folder> prefferedFolders(){
        Map<String,Folder> folders = new HashMap<String,Folder>();
        for(Item item : getAllSources()){
            String path = item.getAbsPath();
            if (Utilities.isWindows()) {
                path = path.replace('\\', '/');
            }
            if (path.indexOf("/../")>=0 || path.indexOf("/./")>=0) { // NOI18N
                path = CndFileUtils.normalizeAbsolutePath(baseFolderFileSystem, path);
                if (Utilities.isWindows()) {
                    path = path.replace('\\', '/');
                }
            }
            int i = path.lastIndexOf('/');
            if (i >= 0){
                String folder = path.substring(0,i);
                folders.put(folder,item.getFolder());
            }
        }
        Folder root = makeConfigurationDescriptor.getLogicalFolders();
        Set<Folder> roots = new HashSet<Folder>(root.getFolders());
        roots.add(root);
        while(true) {
            Map<String,Folder> delta = new HashMap<String,Folder>();
            for(Map.Entry<String,Folder> entry : folders.entrySet()) {
                String path = entry.getKey();
                Folder folder = entry.getValue();
                Folder parent = folder.getParent();
                if (parent != null && !roots.contains(parent)) {
                    String name = folder.getName();
                    int i = path.lastIndexOf('/');
                    if (i >= 0){
                        String pathName = path.substring(i+1);
                        if (name.equals(pathName)) {
                            String needCheck = path.substring(0,i);
                            if (!folders.containsKey(needCheck)) {
                                delta.put(needCheck, parent);
                            }
                        }
                    }
                }
            }
            if (delta.isEmpty()) {
                break;
            }
            folders.putAll(delta);
        }
        return folders;
    }
    
    public void save(){
        makeConfigurationDescriptor.save();
    }
    
    public Set<Project> getResult(){
        if (startedModification) {
            ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
            pdp.endModifications(delta, startedModification, ImportProject.logger);
        } else {
            makeConfigurationDescriptor.checkForChangedItems(project, null, null);
        }
        return resultSet;
    }
        
    public void printStaticstic(Folder sourceRoot, Logger logger) {
        if (logger.isLoggable(Level.INFO)) {
            int all = 0;
            int c = 0;
            int c_excluded = 0;
            int cpp = 0;
            int cpp_excluded = 0;
            int header = 0;
            int header_excluded = 0;
            for(Item item : sourceRoot.getAllItemsAsArray()) {
                all++;
                MakeConfiguration makeConfiguration = item.getFolder().getConfigurationDescriptor().getActiveConfiguration();
                ItemConfiguration itemConfiguration = item.getItemConfiguration(makeConfiguration);
                if (itemConfiguration == null) {
                    continue;
                }
                boolean excluded = itemConfiguration.getExcluded().getValue();
                switch (itemConfiguration.getTool()) {
                    case CCCompiler:
                        if (excluded) {
                            cpp_excluded++;
                        } else {
                            cpp++;
                        }
                        break;
                    case CCompiler:
                        if (excluded) {
                            c_excluded++;
                        } else {
                            c++;
                        }
                        break;
                    case CustomTool:
                        if (excluded) {
                            header_excluded++;
                        } else {
                            header++;
                        }
                        break;
                }
            }
            logger.log(Level.INFO, "Project {0} configuration has:\n\tC files {1} + excluded {2}\n\tC++ files {3} + excluded {4}\n\tHeader files {5} + excluded {6}\n\tAll items {7}",
                    new Object[]{project, c, c_excluded, cpp, cpp_excluded, header, header_excluded, all});
        }
    }
    

    public void setupProject(List<String> includes, List<String> macros, List<String> undefs, ItemProperties.LanguageKind lang){
        MakeConfiguration extConf = makeConfigurationDescriptor.getActiveConfiguration();
        if (extConf != null) {
            for(int i = 0; i < includes.size(); i++) {
                includes.set(i, getString(includes.get(i)));
            }
            for(int i = 0; i < macros.size(); i++) {
                macros.set(i, getString(macros.get(i)));
            }
            if (lang == ItemProperties.LanguageKind.CPP) {
                extConf.getCCCompilerConfiguration().getIncludeDirectories().setValue(includes);
                extConf.getCCCompilerConfiguration().getPreprocessorConfiguration().setValue(macros);
                extConf.getCCCompilerConfiguration().getUndefinedPreprocessorConfiguration().setValue(undefs);
                extConf.getCCCompilerConfiguration().getIncludeDirectories().setDirty(true);
                extConf.getCCCompilerConfiguration().getPreprocessorConfiguration().setDirty(true);
                extConf.getCCCompilerConfiguration().getUndefinedPreprocessorConfiguration().setDirty(true);
            } else if (lang == ItemProperties.LanguageKind.C) {
                extConf.getCCompilerConfiguration().getIncludeDirectories().setValue(includes);
                extConf.getCCompilerConfiguration().getPreprocessorConfiguration().setValue(macros);
                extConf.getCCompilerConfiguration().getUndefinedPreprocessorConfiguration().setValue(undefs);
                extConf.getCCompilerConfiguration().getIncludeDirectories().setDirty(true);
                extConf.getCCompilerConfiguration().getPreprocessorConfiguration().setDirty(true);
                extConf.getCCompilerConfiguration().getUndefinedPreprocessorConfiguration().setDirty(true);
            } else if (lang == ItemProperties.LanguageKind.Fortran) {
                // not supported includes and macros
            }
        }
        makeConfigurationDescriptor.setModified();
    }

    public FolderConfiguration getFolderConfiguration(Folder folder) {
        MakeConfiguration makeConfiguration = folder.getConfigurationDescriptor().getActiveConfiguration();
        return folder.getFolderConfiguration(makeConfiguration);
    }

    public CCCCompilerConfiguration getFolderConfiguration(ItemProperties.LanguageKind lang, Folder folder) {
        FolderConfiguration folderConfiguration = getFolderConfiguration(folder);
        if (folderConfiguration == null) {
            return null;
        }
        if (lang == ItemProperties.LanguageKind.CPP) {
            return folderConfiguration.getCCCompilerConfiguration();
        } else if (lang == ItemProperties.LanguageKind.C) {
            return folderConfiguration.getCCompilerConfiguration();
        }
        return null;
    }

    public void setupFolder(List<String> includes, boolean inheriteIncludes, List<String> macros, boolean inheriteMacros, List<String> undefs, boolean inheriteUndefs, ItemProperties.LanguageKind lang, Folder folder) {
        CCCCompilerConfiguration cccc = getFolderConfiguration(lang, folder);
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
        cccc.getUndefinedPreprocessorConfiguration().setValue(undefs);
        cccc.getInheritUndefinedPreprocessor().setValue(inheriteUndefs);
    }
    
    public static void setExclude(Item item, boolean exclude){
        ItemConfiguration itemConfiguration = getOrCreateItemConfiguration(item);
        if (itemConfiguration == null) {
            return;
        }
        BooleanConfiguration excl =itemConfiguration.getExcluded();
        if (excl.getValue() ^ exclude){
            excl.setValue(exclude);
        }
        //itemConfiguration.setTool(Tool.CustomTool);
    }
    
    public static boolean setHeaderTool(Item item){
        ItemConfiguration itemConfiguration = getOrCreateItemConfiguration(item);
        if (itemConfiguration == null) {
            return false;
        }
        if (itemConfiguration.getTool() == PredefinedToolKind.CCCompiler || itemConfiguration.getTool() == PredefinedToolKind.CCompiler) {
            itemConfiguration.setTool(PredefinedToolKind.CustomTool);
            return true;
        }
        return false;
    }

    private static ItemConfiguration getOrCreateItemConfiguration(Item item) {
        MakeConfiguration makeConfiguration = item.getFolder().getConfigurationDescriptor().getActiveConfiguration();
        ItemConfiguration itemConfiguration = item.getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(item.getPath()));
        if (itemConfiguration == null) {
            String mimeType = item.getMIMEType();
            if (MIMENames.CPLUSPLUS_MIME_TYPE.equals(mimeType)) {
                itemConfiguration = new ItemConfiguration(makeConfiguration, item);
                itemConfiguration.setTool(PredefinedToolKind.CCCompiler);
                makeConfiguration.addAuxObject(itemConfiguration);
            } else if (MIMENames.C_MIME_TYPE.equals(mimeType)) {
                itemConfiguration = new ItemConfiguration(makeConfiguration, item);
                itemConfiguration.setTool(PredefinedToolKind.CCompiler);
                makeConfiguration.addAuxObject(itemConfiguration);
            } else if (MIMENames.FORTRAN_MIME_TYPE.equals(mimeType)) {
                itemConfiguration = new ItemConfiguration(makeConfiguration, item);
                itemConfiguration.setTool(PredefinedToolKind.FortranCompiler);
                makeConfiguration.addAuxObject(itemConfiguration);
            } else if (MIMENames.HEADER_MIME_TYPE.equals(mimeType)) {
                itemConfiguration = new ItemConfiguration(makeConfiguration, item);
                itemConfiguration.setTool(PredefinedToolKind.CustomTool);
                makeConfiguration.addAuxObject(itemConfiguration);
            }
        }
        return itemConfiguration;
    }
    
    public static boolean setSourceTool(Item item, ItemProperties.LanguageKind lang, ItemProperties.LanguageStandard languageStandard, boolean isIncrementalMode){
        ItemConfiguration itemConfiguration = getOrCreateItemConfiguration(item);
        if (itemConfiguration == null) {
            return false;
        }
        boolean isChanged = false;
        switch (lang) {
            case C:
                if (itemConfiguration.getTool() != PredefinedToolKind.CCompiler) {
                    itemConfiguration.setTool(PredefinedToolKind.CCompiler);
                    isChanged = true;
                }
                break;
            case CPP:
                if (itemConfiguration.getTool() != PredefinedToolKind.CCCompiler) {
                    itemConfiguration.setTool(PredefinedToolKind.CCCompiler);
                    isChanged = true;
                }
                break;
            case Fortran:
                if (itemConfiguration.getTool() != PredefinedToolKind.FortranCompiler) {
                    itemConfiguration.setTool(PredefinedToolKind.FortranCompiler);
                    isChanged = true;
                }
                break;
        }
        isChanged |= setSourceStandard(item, languageStandard, isIncrementalMode);
        return isChanged;
    }

    
    public static boolean setSourceStandard(Item item, ItemProperties.LanguageStandard languageStandard, boolean isIncrementalMode) {
        if (languageStandard == null) {
            return false;
        }
        ItemConfiguration itemConfiguration = getOrCreateItemConfiguration(item);
        if (itemConfiguration == null) {
            return false;
        }
        boolean isChanged = false;
        if (itemConfiguration.getTool() == PredefinedToolKind.CCompiler) {
            switch (languageStandard) {
                case C:
                    if (itemConfiguration.getLanguageFlavor() != LanguageFlavor.C) {
                        itemConfiguration.setLanguageFlavor(LanguageFlavor.C);
                        isChanged = true;
                    }
                    break;
                case C89:
                    if (itemConfiguration.getLanguageFlavor() != LanguageFlavor.C89) {
                        itemConfiguration.setLanguageFlavor(LanguageFlavor.C89);
                        isChanged = true;
                    }
                    break;
                case C99:
                    if (itemConfiguration.getLanguageFlavor() != LanguageFlavor.C99) {
                        itemConfiguration.setLanguageFlavor(LanguageFlavor.C99);
                        isChanged = true;
                    }
                    break;
                case Unknown:
                    if (!isIncrementalMode) {
                        if (itemConfiguration.getLanguageFlavor() != LanguageFlavor.UNKNOWN) {
                            itemConfiguration.setLanguageFlavor(LanguageFlavor.UNKNOWN);
                            isChanged = true;
                        }
                    }
                    break;
                case Default:
                    if (itemConfiguration.getLanguageFlavor() != LanguageFlavor.DEFAULT) {
                        itemConfiguration.setLanguageFlavor(LanguageFlavor.DEFAULT);
                        isChanged = true;
                    }
                    break;
            }
        } else if (itemConfiguration.getTool() == PredefinedToolKind.CCCompiler) {
            switch (languageStandard) {
                case CPP:
                    if (itemConfiguration.getLanguageFlavor() != LanguageFlavor.CPP) {
                        itemConfiguration.setLanguageFlavor(LanguageFlavor.CPP);
                        isChanged = true;
                    }
                    break;
                case CPP11:
                    if (itemConfiguration.getLanguageFlavor() != LanguageFlavor.CPP11) {
                        itemConfiguration.setLanguageFlavor(LanguageFlavor.CPP11);
                        isChanged = true;
                    }
                    break;
                case Unknown:
                    if (!isIncrementalMode) {
                        if (itemConfiguration.getLanguageFlavor() != LanguageFlavor.UNKNOWN) {
                            itemConfiguration.setLanguageFlavor(LanguageFlavor.UNKNOWN);
                            isChanged = true;
                        }
                    }
                    break;
                case Default:
                    if (itemConfiguration.getLanguageFlavor() != LanguageFlavor.DEFAULT) {
                        itemConfiguration.setLanguageFlavor(LanguageFlavor.DEFAULT);
                        isChanged = true;
                    }
                    break;
            }
        } else if (itemConfiguration.getTool() == PredefinedToolKind.FortranCompiler) {
            switch (languageStandard) {
                case F77:
                    if (itemConfiguration.getLanguageFlavor() != LanguageFlavor.F77) {
                        itemConfiguration.setLanguageFlavor(LanguageFlavor.F77);
                        isChanged = true;
                    }
                    break;
                case F90:
                    if (itemConfiguration.getLanguageFlavor() != LanguageFlavor.F90) {
                        itemConfiguration.setLanguageFlavor(LanguageFlavor.F90);
                        isChanged = true;
                    }
                    break;
                case F95:
                    if (itemConfiguration.getLanguageFlavor() != LanguageFlavor.F95) {
                        itemConfiguration.setLanguageFlavor(LanguageFlavor.F95);
                        isChanged = true;
                    }
                    break;
            }
        }
        return isChanged;
    }

    
    public CCCCompilerConfiguration getItemConfiguration(Item item) {
        ItemConfiguration itemConfiguration = getOrCreateItemConfiguration(item);
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) {
            return null;
        }
        BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
        if (compilerConfiguration instanceof CCCCompilerConfiguration) {
            return (CCCCompilerConfiguration) compilerConfiguration;
        }
        return null;
    }

    public void setupFile(String compilepath, List<String> includes, boolean inheriteIncludes, List<String> macros, boolean inheriteMacros, List<String> undefs, boolean inheriteUndefs, Item item) {
        ItemConfiguration itemConfiguration = getOrCreateItemConfiguration(item);
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) {
            return;
        }
        BooleanConfiguration excl = itemConfiguration.getExcluded();
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
            cccCompilerConfiguration.getUndefinedPreprocessorConfiguration().setValue(undefs);
            cccCompilerConfiguration.getInheritUndefinedPreprocessor().setValue(inheriteUndefs);
        }
    }

    public static boolean fixFileMacros(Map<String,String> macros, Item item) {
        ItemConfiguration itemConfiguration = getOrCreateItemConfiguration(item);
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) {
            return false;
        }
        boolean isChanged = false;
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
                    isChanged = true;
                    list.add(s);
                }
            }
            if (isChanged) {
                cccCompilerConfiguration.getPreprocessorConfiguration().setValue(list);
            }
        }
        return isChanged;
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

    public void dispose(){
        cache.clear();
    }
    
    public CompilerSet getCompilerSet(){
        MakeConfiguration makeConfiguration = makeConfigurationDescriptor.getActiveConfiguration();
        return makeConfiguration.getCompilerSet().getCompilerSet();
    }

    public String getCygwinDrive(){
        String res =CompilerSetUtils.getCygwinBase();
        if (res != null && res.endsWith("/")){ // NOI18N
            res = res.substring(0,res.length()-1);
        }
        return res;
    }

    public CompilerFlavor getCompilerFlavor(){
        final CompilerSet compilerSet = getCompilerSet();
        if (compilerSet != null) {
            return compilerSet.getCompilerFlavor();
        }
        return null;
    }

    public String getCompilerDirectory(){
        final CompilerSet compilerSet = getCompilerSet();
        if (compilerSet != null) {
            return getCompilerSet().getDirectory();
        }
        return null;
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
            if (compilerSet != null) {
                if (isCPP) {
                    compiler = (AbstractCompiler)compilerSet.getTool(PredefinedToolKind.CCCompiler);
                } else {
                    compiler = (AbstractCompiler)compilerSet.getTool(PredefinedToolKind.CCompiler);
                }
                if (compiler != null) {
                    for(String path :compiler.getSystemIncludeDirectories()){
                        systemIncludePaths.add(fixWindowsPath(path));
                    }
                }
            }
            if (isCPP) {
                systemIncludePathsCpp = systemIncludePaths;
            } else {
                systemIncludePathsC = systemIncludePaths;
            }
        }
        return systemIncludePaths;
    }

    private Map<String, List<String>> optionToMacrosC;
    private Map<String, List<String>> optionToMacrosCpp;
    public List<String> getOptionToMacros(String option, boolean isCPP) {
        Map<String, List<String>> macros;
        if (isCPP) {
            macros = optionToMacrosCpp;
        } else {
            macros = optionToMacrosC;
        }
        if (macros == null) {
            macros = new HashMap<String,List<String>>();
            CompilerSet compilerSet = getCompilerSet();
            if (compilerSet != null) {
            AbstractCompiler compiler;
                if (isCPP) {
                    compiler = (AbstractCompiler)compilerSet.getTool(PredefinedToolKind.CCCompiler);
                } else {
                    compiler = (AbstractCompiler)compilerSet.getTool(PredefinedToolKind.CCompiler);
                }
                if (compiler != null && compiler.getDescriptor() != null) {
                    final List<PredefinedMacro> predefinedMacros = compiler.getDescriptor().getPredefinedMacros();
                    if (predefinedMacros != null) {
                        for(ToolchainManager.PredefinedMacro macro : predefinedMacros){
                            if (macro.getFlags() != null) {
                                if (!macro.isHidden()) {
                                    List<String> list = macros.get(macro.getFlags());
                                    if (list == null) {
                                        list = new ArrayList<String>();
                                        macros.put(macro.getFlags(), list);
                                    }
                                    list.add(macro.getMacro());
                                }
                            }
                        }
                    }
                }
            }
            if (isCPP) {
                optionToMacrosCpp = macros;
            } else {
                optionToMacrosC = macros;
            }
        }
        return macros.get(option);
    }

    
    private Map<String, List<String>> optionToUndefinedMacrosC;
    private Map<String, List<String>> optionToUndefinedMacrosCpp;
    public List<String> getOptionToUndefinedMacros(String option, boolean isCPP) {
        Map<String, List<String>> macros;
        if (isCPP) {
            macros = optionToUndefinedMacrosCpp;
        } else {
            macros = optionToUndefinedMacrosC;
        }
        if (macros == null) {
            macros = new HashMap<String,List<String>>();
            CompilerSet compilerSet = getCompilerSet();
            if (compilerSet != null) {
                AbstractCompiler compiler;
                if (isCPP) {
                    compiler = (AbstractCompiler)compilerSet.getTool(PredefinedToolKind.CCCompiler);
                } else {
                    compiler = (AbstractCompiler)compilerSet.getTool(PredefinedToolKind.CCompiler);
                }
                if (compiler != null && compiler.getDescriptor() != null) {
                    final List<PredefinedMacro> predefinedMacros = compiler.getDescriptor().getPredefinedMacros();
                    if (predefinedMacros != null) {
                        for(ToolchainManager.PredefinedMacro macro : predefinedMacros){
                            if (macro.getFlags() != null) {
                                if (macro.isHidden()) {
                                    List<String> list = macros.get(macro.getFlags());
                                    if (list == null) {
                                        list = new ArrayList<String>();
                                        macros.put(macro.getFlags(), list);
                                    }
                                    list.add(macro.getMacro());
                                }
                            }
                        }
                    }
                }
            }
            if (isCPP) {
                optionToUndefinedMacrosCpp = macros;
            } else {
                optionToUndefinedMacrosC = macros;
            }
        }
        return macros.get(option);
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
            if (compilerSet != null) {
                AbstractCompiler compiler;
                if (isCPP) {
                    compiler = (AbstractCompiler)compilerSet.getTool(PredefinedToolKind.CCCompiler);
                } else {
                    compiler = (AbstractCompiler)compilerSet.getTool(PredefinedToolKind.CCompiler);
                }
                if (compiler != null) {
                    for(Object o :compiler.getSystemPreprocessorSymbols()){
                        String macro = (String)o;
                        int i = macro.indexOf('=');
                        if (i>0){
                            systemMacroDefinitions.put(macro.substring(0,i), macro.substring(i+1).trim());
                        } else {
                            systemMacroDefinitions.put(macro, null);
                        }
                    }
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
