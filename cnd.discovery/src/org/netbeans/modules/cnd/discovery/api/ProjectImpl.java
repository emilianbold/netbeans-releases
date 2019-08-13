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

package org.netbeans.modules.cnd.discovery.api;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.project.IncludePath;
import org.netbeans.modules.cnd.api.project.NativeFileItem.Language;
import org.netbeans.modules.cnd.discovery.wizard.api.support.ProjectBridge;
import org.netbeans.modules.cnd.makeproject.api.configurations.BooleanConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.util.Utilities;

/**
 * Default implementation of ProjectProperties.
 * Enough in most cases.
 *
 * @author Alexander Simon
 */
public final class ProjectImpl implements ProjectProperties {
    private static final boolean gatherFolders = true;

    private final ItemProperties.LanguageKind language;
    private final Set<String> userIncludes = new LinkedHashSet<>();
    private final Set<String> userFiles = new HashSet<>();
    private final Set<String> systemIncludes = new LinkedHashSet<>();
    private final Map<String,String> userMacros = new HashMap<>();
    private final Set<String> undefinedMacros = new LinkedHashSet<>();
    private final Map<String,FolderProperties> folders = new HashMap<>();

    /** Creates a new instance of DwarfProject */
    public ProjectImpl(ItemProperties.LanguageKind language) {
        this.language = language;
    }

    /**
     * Adds source file in the project
     * @param source source file
     */
    public void update(SourceFileProperties source){
        userIncludes.addAll(source.getUserInludePaths());
        for (String path : source.getUserInludePaths()) {
            userIncludes.add(DiscoveryUtils.convertRelativePathToAbsolute(source,path));
        }
        userFiles.addAll(source.getUserInludeFiles());
        userMacros.putAll(source.getUserMacros());
        undefinedMacros.addAll(source.getUndefinedMacros());
        if (gatherFolders) {
            updateFolder(source);
        }
    }

    /**
     * Helper method that divides list of source properties by language.
     * @param sources list of source files
     * @param project
     * @return list of language projects
     */
    public static List<ProjectProperties> divideByLanguage(List<SourceFileProperties> sources, ProjectProxy project) {
        ProjectImpl cProp = null;
        ProjectImpl cppProp = null;
        ProjectImpl fortranProp = null;
        if (sources.size() > 0) {
            if (project != null && project.mergeProjectProperties()) {
                sources = mergeLists(sources, project);
            }
        }
        for (SourceFileProperties source : sources) {
            ItemProperties.LanguageKind lang = source.getLanguageKind();
            ProjectImpl current;
            if (lang == ItemProperties.LanguageKind.C) {
                if (cProp == null) {
                    cProp = new ProjectImpl(lang);
                }
                current = cProp;
            } else if (lang == ItemProperties.LanguageKind.CPP) {
                if (cppProp == null) {
                    cppProp = new ProjectImpl(lang);
                }
                current = cppProp;
            } else if (lang == ItemProperties.LanguageKind.Fortran) {
                if (fortranProp == null) {
                    fortranProp = new ProjectImpl(lang);
                }
                current = fortranProp;
            } else {
                continue;
            }
            current.update(source);
        }
        List<ProjectProperties> languages = new ArrayList<>();
        if (cProp != null) {
            languages.add(cProp);
        }
        if (cppProp != null) {
            languages.add(cppProp);
        }
        if (fortranProp != null) {
            languages.add(fortranProp);
        }
        return languages;
    }

    private static List<SourceFileProperties> mergeLists(List<SourceFileProperties> discovered, ProjectProxy project) {
        ProjectBridge bridge = new ProjectBridge(project.getProject());
        Map<String, SourceFileProperties> map = new HashMap<>();
        List<SourceFileProperties> res = new ArrayList<>(discovered);
        for(SourceFileProperties source : discovered) {
            map.put(source.getItemPath(), source);
        }
        int equalsSources = 0;
        for(SourceFileProperties source : getExistingProjectItems(project)) {
            if (!map.containsKey(source.getItemPath())) {
                res.add(source);
            } else {
                if (isSourcesEquals(map.get(source.getItemPath()), source, bridge)) {
                    equalsSources++;
                }
            }
        }
        if (equalsSources == discovered.size()) {
            return Collections.<SourceFileProperties>emptyList();
        }
        return res;
    }

    private static boolean isSourcesEquals(SourceFileProperties newSource, SourceFileProperties oldSource, ProjectBridge bridge) {
        HashSet<String> set1 = new HashSet<>();
        bridge.convertIncludePaths(set1, newSource.getUserInludePaths(), newSource.getCompilePath(), newSource.getItemPath());
        HashSet<String> set2 = new HashSet<>();
        for(String s : oldSource.getUserInludePaths()) {
            set2.add(bridge.getRelativepath(s));
        }
        if (!set1.equals(set2)) {
            return false;
        }

        set1 = new HashSet<>();
        bridge.convertIncludeFiles(set1, newSource.getUserInludeFiles(), newSource.getCompilePath(), newSource.getUserInludePaths());
        set2 = new HashSet<>();
        for(String s : oldSource.getUserInludeFiles()) {
            set2.add(bridge.getRelativepath(s));
        }
        if (!set1.equals(set2)) {
            return false;
        }

        Map<String, String> m1 = newSource.getUserMacros();
        Map<String, String> m2 = oldSource.getUserMacros();
        if (m1.size() != m2.size()) {
            return false;
        }
        if (!new HashMap<>(m1).equals(m2)) {
            return false;
        }
        Set<String> u1 = new HashSet<>(newSource.getUndefinedMacros());
        Set<String> u2 = new HashSet<>(oldSource.getUndefinedMacros());
        if (u1.size() != u2.size()) {
            return false;
        }
        return u1.equals(u2);
    }

    private static List<SourceFileProperties> getExistingProjectItems(ProjectProxy project) {
        List<SourceFileProperties> res = new ArrayList<>();
        Project makeProject = project.getProject();
        ConfigurationDescriptorProvider pdp = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
        MakeConfigurationDescriptor makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
        if (makeConfigurationDescriptor != null) {
            Item[] items = makeConfigurationDescriptor.getProjectItems();
            for (int i = 0; i < items.length; i++) {
                Item item = items[i];
                if (!isExcluded(item)) {
                    final Language lang = item.getLanguage();
                    if (lang == Language.C || lang == Language.CPP) {
                        res.add(new ItemWrapper(item));
                    }
                }
            }
        }
        return res;
    }

    private static boolean isExcluded(Item item){
        MakeConfiguration makeConfiguration = item.getFolder().getConfigurationDescriptor().getActiveConfiguration();
        ItemConfiguration itemConfiguration = item.getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(item.getPath()));
        if (itemConfiguration == null) {
            return true;
        }
        BooleanConfiguration excl =itemConfiguration.getExcluded();
        return excl.getValue();
    }

    private void updateFolder(SourceFileProperties source){
        File file = new File(source.getItemPath());
        File parent = file.getParentFile();
        if (parent != null) {
            String path = CndFileUtils.normalizeFile(parent).getAbsolutePath();
            // folders should use unix style
            if (Utilities.isWindows()) {
                path = path.replace('\\', '/');
            }
            FolderProperties folder = folders.get(path);
            if (folder == null) {
                folders.put(path,new FolderImpl(path,source));
            } else {
                ((FolderImpl)folder).update(source);
            }
        }
    }

    @Override
    public List<FolderProperties> getConfiguredFolders(){
        return new ArrayList<>(folders.values());
    }

    @Override
    public String getMakePath() {
        return null;
    }

    @Override
    public String getBinaryPath() {
        return null;
    }

    @Override
    public ProjectProperties.BinaryKind getBinaryKind() {
        return null;
    }

    @Override
    public List<String> getUserInludePaths() {
        return new ArrayList<>(userIncludes);
    }

    @Override
    public List<String> getUserInludeFiles() {
        return new ArrayList<>(userFiles);
    }

    @Override
    public List<String> getSystemInludePaths() {
        return new ArrayList<>(systemIncludes);
    }

    @Override
    public Map<String, String> getUserMacros() {
        return userMacros;
    }

    @Override
    public List<String> getUndefinedMacros() {
        return new ArrayList<>(undefinedMacros);
    }

    @Override
    public Map<String, String> getSystemMacros() {
        return null;
    }

    @Override
    public ItemProperties.LanguageKind getLanguageKind() {
        return language;
    }

    @Override
    public String getCompilerName() {
        return "";
    }

    @Override
    public LanguageStandard getLanguageStandard() {
        // now projects do not divided by language standards
        return LanguageStandard.Unknown;
    }

    private static final class ItemWrapper implements SourceFileProperties {
        private final Item item;
        private final List<String> userIncludePaths;
        private final List<String> userIncludeFiles;
        private final Map<String, String> userMacroDefinitions;
        private final List<String> userUndefinesMacros;
        private final String importantFlags;

        private ItemWrapper(Item item) {
            this.item = item;
            userIncludePaths = IncludePath.toStringList(item.getUserIncludePaths());
            userIncludeFiles = convertFSPaths(item.getIncludeFiles());
            userMacroDefinitions =  convertToMap(item.getUserMacroDefinitions());
            userUndefinesMacros =  new ArrayList<>(item.getUndefinedMacros());
            importantFlags = item.getImportantFlags();
        }
 
        private List<String> convertFSPaths(List<FSPath> list) {
            List<String> res = new ArrayList<>(list.size());
            for(FSPath p : list) {
                res.add(p.getPath());
            }
            return res;
        }
        
        private Map<String, String> convertToMap(List<String> list) {
            Map<String, String> res = new HashMap<>();
            for(String macro : list){
                int i = macro.indexOf('=');
                if (i>0){
                    res.put(macro.substring(0,i).trim(),macro.substring(i+1).trim());
                } else {
                    res.put(macro,null);
                }
            }
            return res;
        }

        @Override
        public String getCompilePath() {
            return item.getFileObject().getParent().getPath();
        }

        @Override
        public String getItemPath() {
            return item.getFileObject().getPath();
        }

        @Override
        public String getCompileLine() {
            return null;
        }

        @Override
        public String getImportantFlags() {
            return importantFlags;
        }

        @Override
        public String getItemName() {
            return item.getFileObject().getNameExt();
        }

        @Override
        public List<String> getUserInludePaths() {
            return userIncludePaths;
        }

        @Override
        public List<String> getUserInludeFiles() {
            return userIncludeFiles;
        }

        @Override
        public List<String> getSystemInludePaths() {
            return IncludePath.toStringList(item.getSystemIncludePaths());
        }

        @Override
        public Map<String, String> getUserMacros() {
            return userMacroDefinitions;
        }

        @Override
        public List<String> getUndefinedMacros() {
            return userUndefinesMacros;
        }

        @Override
        public Map<String, String> getSystemMacros() {
            return convertToMap(item.getSystemMacroDefinitions());
        }

        @Override
        public LanguageKind getLanguageKind() {
            switch (item.getLanguage()) {
                case C:
                    return LanguageKind.C;
                case CPP:
                    return LanguageKind.CPP;
                case FORTRAN:
                    return LanguageKind.Fortran;
                default:
                    return LanguageKind.Unknown;
            }
        }

        @Override
        public LanguageStandard getLanguageStandard() {
            switch (item.getLanguageFlavor()) {
                case C: return LanguageStandard.C;
                case C89: return LanguageStandard.C89;
                case C99: return LanguageStandard.C99;
                case C11: return LanguageStandard.C11;
                case CPP98: return LanguageStandard.CPP98;
                case CPP11: return LanguageStandard.CPP11;
                case CPP14: return LanguageStandard.CPP14;
                case CPP17: return LanguageStandard.CPP17;
                case F77: return LanguageStandard.F77;
                case F90: return LanguageStandard.F90;
                case F95: return LanguageStandard.F95;
                case DEFAULT: return LanguageStandard.Default;
                default: return LanguageStandard.Unknown;
            }
        }

        @Override
        public String getCompilerName() {
            switch (item.getLanguage()) {
                case C:
                    return "cc"; //NOI18N
                case CPP:
                    return "CC"; //NOI18N
                case FORTRAN:
                    return "f95"; //NOI18N
                default:
                    return ""; //NOI18N
                }
        }
    }
}
