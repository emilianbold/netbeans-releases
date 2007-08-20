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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modeldiscovery.provider;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeFileItem.Language;
import org.netbeans.modules.cnd.discovery.api.Configuration;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.api.ProjectProperties;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.api.ProviderProperty;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.makeproject.api.configurations.BooleanConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public class AnalyzeModel implements DiscoveryProvider {
    private Map<String,ProviderProperty> myProperties = new HashMap<String,ProviderProperty>();
    public static final String MODEL_FOLDER_KEY = "folder"; // NOI18N
    protected boolean isStoped = false;
    
    
    public AnalyzeModel() {
        clean();
    }
    
    public void clean() {
        myProperties.clear();
        myProperties.put(MODEL_FOLDER_KEY, new ProviderProperty(){
            private String myPath;
            public String getName() {
                return i18n("Model_Files_Name"); // NOI18N
            }
            public String getDescription() {
                return i18n("Model_Files_Description"); // NOI18N
            }
            public Object getValue() {
                return myPath;
            }
            public void setValue(Object value) {
                if (value instanceof String){
                    myPath = (String)value;
                }
            }
            public ProviderProperty.PropertyKind getKind() {
                return ProviderProperty.PropertyKind.Folder;
            }
        });
    }
    
    public String getID() {
        return "model-folder"; // NOI18N
    }
    
    public String getName() {
        return i18n("Model_Provider_Name"); // NOI18N
    }
    
    public String getDescription() {
        return i18n("Model_Provider_Description"); // NOI18N
    }
    
    public List<String> getPropertyKeys() {
        return new ArrayList<String>(myProperties.keySet());
    }
    
    public ProviderProperty getProperty(String key) {
        return myProperties.get(key);
    }
    
    public void stop() {
        isStoped = true;
    }
    
    public List<Configuration> analyze(ProjectProxy project) {
        isStoped = false;
        MyConfiguration conf = new MyConfiguration(project);
        List<Configuration> confs = new ArrayList<Configuration>();
        confs.add(conf);
        return confs;
    }
    
    
    private Map<String,List<String>> search(String root){
        HashSet<String> set = new HashSet<String>();
        ArrayList<String> list = new ArrayList<String>();
        list.add(root);
        for (Iterator<String> it = list.iterator(); it.hasNext();){
            if (isStoped) {
                break;
            }
            File f = new File(it.next());
            gatherSubFolders(f, set);
        }
        HashMap<String,List<String>> map = new HashMap<String,List<String>>();
        for (Iterator it = set.iterator(); it.hasNext();){
            if (isStoped) {
                break;
            }
            File d = new File((String)it.next());
            if (d.isDirectory()){
                File[] ff = d.listFiles();
                for (int i = 0; i < ff.length; i++) {
                    if (ff[i].isFile()) {
                        List<String> l = map.get(ff[i].getName());
                        if (l==null){
                            l = new ArrayList<String>();
                            map.put(ff[i].getName(),l);
                        }
                        String path = ff[i].getAbsolutePath();
                        if (Utilities.isWindows()) {
                            path = path.replace('\\', '/');
                        }
                        l.add(path);
                    }
                }
            }
        }
        return map;
    }
    
    private void gatherSubFolders(File d, HashSet<String> set){
        if (isStoped) {
            return;
        }
        if (d.isDirectory()){
            String path = d.getAbsolutePath();
            if (Utilities.isWindows()) {
                path = path.replace('\\', '/');
            }
            if (path.endsWith("/SCCS") || path.endsWith("/CVS")) { // NOI18N
                return;
            }
            if (!set.contains(path)){
                set.add(path);
                File[] ff = d.listFiles();
                for (int i = 0; i < ff.length; i++) {
                    gatherSubFolders(ff[i], set);
                }
            }
        }
    }
    
    private static String i18n(String id) {
        return NbBundle.getMessage(AnalyzeModel.class,id);
    }
    
    public boolean isApplicable(ProjectProxy project) {
        if (project.getProject() instanceof Project){
            Project makeProject = (Project)project.getProject();
            CsmProject langProject = CsmModelAccessor.getModel().getProject(makeProject);
            if (langProject != null/* && langProject.isStable(null)*/){
                return true;
            }
        }
        return false;
    }
    
    public boolean canAnalyze(ProjectProxy project) {
        return true;
    }
    
    private class MyConfiguration implements Configuration{
        private List<SourceFileProperties> myFileProperties;
        private List<String> myIncludedFiles;
        private MakeConfigurationDescriptor makeConfigurationDescriptor;
        private CsmProject langProject;
        
        private MyConfiguration(ProjectProxy project){
            Project makeProject = (Project)project.getProject();
            langProject = CsmModelAccessor.getModel().getProject(makeProject);
            ConfigurationDescriptorProvider pdp = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
            makeConfigurationDescriptor = (MakeConfigurationDescriptor)pdp.getConfigurationDescriptor();
        }
        
        public List<ProjectProperties> getProjectConfiguration() {
            return divideByLanguage(getSourcesConfiguration());
        }
        
        protected List<ProjectProperties> divideByLanguage(List<SourceFileProperties> sources){
            ModelProject cProp = null;
            ModelProject cppProp = null;
            for (SourceFileProperties source : sources) {
                ItemProperties.LanguageKind lang = source.getLanguageKind();
                ModelProject current = null;
                if (lang == ItemProperties.LanguageKind.C){
                    if (cProp == null) {
                        cProp = new ModelProject(lang);
                    }
                    current = cProp;
                } else {
                    if (cppProp == null) {
                        cppProp = new ModelProject(lang);
                    }
                    current = cppProp;
                }
                current.update(source);
            }
            List<ProjectProperties> languages = new ArrayList<ProjectProperties>();
            if (cProp != null) {
                languages.add(cProp);
            }
            if (cppProp != null) {
                languages.add(cppProp);
            }
            return languages;
        }
        
        public List<Configuration> getDependencies() {
            return null;
        }
        
        public boolean isExcluded(Item item){
            MakeConfiguration makeConfiguration = (MakeConfiguration)item.getFolder().getConfigurationDescriptor().getConfs().getActive();
            ItemConfiguration itemConfiguration = item.getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(item.getPath()));
            if (itemConfiguration == null) {
                return true;
            }
            BooleanConfiguration excl =itemConfiguration.getExcluded();
            return excl.getValue();
        }
        
        private List<SourceFileProperties> getSourceFileProperties(String root){
            List<SourceFileProperties> res = new ArrayList<SourceFileProperties>();
            Map<String,List<String>> searchBase = search(root);
            Item[] items = makeConfigurationDescriptor.getProjectItems();
            for (int i = 0; i < items.length; i++){
                if (isStoped) {
                    break;
                }
                Item item = items[i];
                if (isExcluded(item)) {
                    continue;
                }
                Language lang = item.getLanguage();
                if (lang == Language.C || lang == Language.CPP){
                    String path = item.getFile().getAbsolutePath();
                    CsmFile langFile = langProject.findFile(path);
                    SourceFileProperties source = new ModelSource(item, langFile, searchBase);
                    res.add(source);
                }
            }
            return res;
        }
        
        public List<SourceFileProperties> getSourcesConfiguration() {
            if (myFileProperties == null){
                myFileProperties = getSourceFileProperties((String)getProperty(MODEL_FOLDER_KEY).getValue());
            }
            return myFileProperties;
        }
        
        public List<String> getIncludedFiles(){
            if (myIncludedFiles == null) {
                HashSet<String> unique = new HashSet<String>();
                Item[] items = makeConfigurationDescriptor.getProjectItems();
                for (int i = 0; i < items.length; i++){
                    if (isStoped) {
                        break;
                    }
                    Item item = items[i];
                    if (isExcluded(item)) {
                        continue;
                    }
                    String path = item.getAbsPath();
                    File file = new File(path);
                    if (file.exists()) {
                        unique.add(FileUtil.normalizeFile(file).getAbsolutePath());
                    }
                }
                HashSet<String> unUnique = new HashSet<String>();
                for(SourceFileProperties source : getSourcesConfiguration()){
                    if (source instanceof ModelSource){
                        unUnique.addAll( ((ModelSource)source).getIncludedFiles() );
                    }
                }
                for(String path : unUnique){
                    File file = new File(path);
                    if (file.exists()) {
                        unique.add(FileUtil.normalizeFile(file).getAbsolutePath());
                    }
                }
                myIncludedFiles = new ArrayList<String>(unique);
            }
            return myIncludedFiles;
        }
    }
}
