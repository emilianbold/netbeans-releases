/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.discovery.project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.discovery.api.Configuration;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.api.KnownProject;
import org.netbeans.modules.cnd.discovery.api.ProjectImpl;
import org.netbeans.modules.cnd.discovery.api.ProjectProperties;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.api.ProviderProperty;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.discovery.wizard.ConsolidationStrategyPanel;
import org.netbeans.modules.cnd.discovery.wizard.SelectConfigurationPanel;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.netbeans.modules.cnd.discovery.wizard.api.ProjectConfiguration;
import org.netbeans.modules.cnd.discovery.wizard.tree.ConfigurationFactory;
import org.netbeans.modules.cnd.discovery.wizard.tree.ProjectConfigurationImpl;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author AlexanderSimon
 */
public class OpenSolaris extends KnownProject {
    public static final String PROJECT_NAME = "OpenSolaris"; // NOI18N
    private String root;
    private String nb_root;

    private List<SourceFileProperties> sources;
    
    public OpenSolaris(){
    }

    @Override
    public boolean canCreate(Map<String, String> parameters) {
        if (!PROJECT_NAME.equals(parameters.get(KnownProject.PROJECT))){
            return false;
        }
        root = parameters.get(KnownProject.ROOT);
        if (root == null) {
            return false;
        }
        nb_root =parameters.get(KnownProject.NB_ROOT);
        if (nb_root == null) {
            return false;
        }
        if (findMakeLog(root) == null) {
            return false;
        }
        return true;
    }

    @Override
    public boolean create(Map<String, String> parameters){
        try{
            String log = findMakeLog(root);
            if (log == null) {
                return false;
            }
            sources = scan(log, root);
            if (sources == null || sources.size() == 0) {
                return false;
            }
            return createImpl();
        } finally {
            PathCache.dispose();
        }
    }
    
    private boolean createImpl(){
        String srcRoot = root+"/usr/src/"; // NOI18N
        Map<String,String> folders = new TreeMap<String,String>();
        for(SourceFileProperties s : sources){
            String path = s.getCompilePath();
            if (path.startsWith(srcRoot)) {
                path = path.substring(srcRoot.length());
                if (path.startsWith("cmd/")){ // NOI18N
                    String f = path.substring(4);
                    int i = f.indexOf('/'); // NOI18N
                    if (i > 0){
                        folders.put("cmd/"+f.substring(0,i), f.substring(0,i)); // NOI18N
                    } else if (f.length() >0){
                        folders.put("cmd/"+f, f); // NOI18N
                    }
                } else if (path.startsWith("lib/gss_mechs/")){ // NOI18N
                    String g = "lib/gss_mechs/"; // NOI18N
                    String f = path.substring(g.length());
                    int i = f.indexOf('/'); // NOI18N
                    if (i > 0){
                        folders.put(g+f.substring(0,i), f.substring(0,i));
                    } else if (f.length() >0){
                        folders.put(g+f, f);
                    }
                } else if (path.startsWith("lib/")){ // NOI18N
                    String f = path.substring(4);
                    int i = f.indexOf('/'); // NOI18N
                    if (i > 0){
                        folders.put("lib/"+f.substring(0,i), f.substring(0,i)); // NOI18N
                    } else if (f.length() >0){
                        folders.put("lib/"+f, f); // NOI18N
                    }
                } else {
                    int i = path.indexOf('/'); // NOI18N
                    if (i > 0){
                        folders.put(path.substring(0,i), path.substring(0,i));
                    } else {
                        folders.put(path, path);
                    }
                }
            }
        }
        for (Map.Entry<String,String> entry:folders.entrySet()){
            String r = srcRoot+entry.getKey();
            String n = nb_root+"/"+entry.getKey(); // NOI18N
            String name = entry.getValue();
            String display = entry.getValue();
            if (entry.getKey().startsWith("cmd/")){ // NOI18N
                display = "os.cmd."+display;
            } else if (entry.getKey().startsWith("lib/gss_mechs/")){ // NOI18N
                display = "os.lib.gss_mechs."+display; // NOI18N
            } else if (entry.getKey().startsWith("lib/")){ // NOI18N
                display = "os.lib."+display; // NOI18N
            } else {
                display = "os."+display; // NOI18N
            }
            createImpl(r, n, name, display, folders);
        }
        // and super projects
        createImpl(srcRoot+"cmd", nb_root+"/commands", "commands", "os.commands", folders); // NOI18N
        createImpl(srcRoot+"lib", nb_root+"/libraries", "libraries", "os.libraries", folders); // NOI18N
        createImpl(root+"/usr/src", nb_root+"/sources", "sources", "os.sources", folders); // NOI18N
        return true;
    }

    private boolean createImpl(String sourceRoot, String nbRoot, String name, String displayName, Map<String,String> folders){
        DiscoveryProviderImpl provider = new DiscoveryProviderImpl(sources, sourceRoot);
        DiscoveryDescriptorImpl discovery = new DiscoveryDescriptorImpl(null, provider, sourceRoot);
        ProjectCreator creator = new ProjectCreator(discovery);
        creator.init(nbRoot, sourceRoot, sourceRoot+"/Makefile"); // NOI18N
        try {
            creator.createProject(name, displayName, folders);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
        return true;
    }

    private List<SourceFileProperties> scan(String log, String root){
        SolarisLogReader reader = new SolarisLogReader(log, root);
        return reader.getResults();
    }

    private String findMakeLog(String root) {
        String latest = null;
        String logfolder = root + "/log"; // NOI18N
        File log = new File(logfolder);
        if (log.exists() && log.isDirectory()) {
            for (File when : log.listFiles()) {
                if (when.isDirectory()) {
                    for (File l : when.listFiles()) {
                        if (l.getAbsolutePath().endsWith("/nightly.log")) { // NOI18N

                            if (latest == null) {
                                latest = l.getAbsolutePath();
                            } else {
                                if (latest.compareTo(l.getAbsolutePath()) < 0) {
                                    latest = l.getAbsolutePath();
                                }
                            }
                            break;
                        }
                    }
                }
            }
            return latest;
        }
        return null;
    }

    private static class DiscoveryDescriptorImpl implements DiscoveryDescriptor{
        private Project project;
        private DiscoveryProvider provider;
        private List<Configuration> configurations;
        private List<ProjectConfiguration> projectConfigurations;
        private List<String> includedFiles;
        private String root;
        private DiscoveryDescriptorImpl(Project project, DiscoveryProvider provider, String root){
            this.project = project;
            this.provider = provider;
            this.root = root;
            this.configurations = provider.analyze(null);
        }
        public Project getProject() {
            return project;
        }

        public void setProject(Project project) {
            this.project = project;
        }

        public DiscoveryProvider getProvider() {
            return provider;
        }

        public String getProviderID() {
            return provider.getID();
        }

        public void setProvider(DiscoveryProvider provider) {
            this.provider = provider;
        }

        public String getRootFolder() {
            return root;
        }

        public void setRootFolder(String root) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public String getBuildResult() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public void setBuildResult(String binaryPath) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public String getAditionalLibraries() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public void setAditionalLibraries(String binaryPath) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public String getLevel() {
            return ConsolidationStrategyPanel.FILE_LEVEL;
        }

        public void setLevel(String level) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public List<ProjectConfiguration> getConfigurations() {
            if (projectConfigurations ==null) {
                provider.analyze(null);
                projectConfigurations = new ArrayList<ProjectConfiguration>();
                for (Iterator<Configuration> it = configurations.iterator(); it.hasNext();) {
                    Configuration conf = it.next();
                    List<ProjectProperties> langList = conf.getProjectConfiguration();
                    for (Iterator<ProjectProperties> it2 = langList.iterator(); it2.hasNext();) {
                        ProjectProperties pp = it2.next();
                        ProjectConfigurationImpl pc = ConfigurationFactory.makeRoot(pp, getRootFolder());
                        SelectConfigurationPanel.consolidateModel(pc, getLevel());
                        projectConfigurations.add(pc);
                    }
                }
            }
            return projectConfigurations;
        }

        public void setConfigurations(List<ProjectConfiguration> configuration) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public List<String> getIncludedFiles() {
            if (includedFiles == null) {
                includedFiles = new ArrayList<String>();
                for (Iterator<Configuration> it = configurations.iterator(); it.hasNext();) {
                    Configuration conf = it.next();
                    includedFiles.addAll(conf.getIncludedFiles());
                }
            }
            return includedFiles;
        }

        public void setIncludedFiles(List<String> includedFiles) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public boolean isInvokeProvider() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public void setInvokeProvider(boolean invoke) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public boolean isSimpleMode() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public void setSimpleMode(boolean simple) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public void setMessage(String message) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public void clean() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }
    }
    
    private static class DiscoveryProviderImpl implements DiscoveryProvider {
        private List<SourceFileProperties> sources;
        private String root;
        public DiscoveryProviderImpl(List<SourceFileProperties> sources, String root){
            this.sources = sources;
            this.root = root;
        }
        public String getID() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public String getName() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public String getDescription() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public List<String> getPropertyKeys() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public ProviderProperty getProperty(String key) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public void clean() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public boolean isApplicable(ProjectProxy project) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public int canAnalyze(ProjectProxy project) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public List<Configuration> analyze(ProjectProxy project) {
            List<Configuration> confs = new ArrayList<Configuration>();
            Configuration conf = new Configuration() {

                private List<SourceFileProperties> myFileProperties;
                private List<String> myIncludedFiles;

                public List<ProjectProperties> getProjectConfiguration() {
                    return ProjectImpl.divideByLanguage(getSourcesConfiguration());
                }

                private List<SourceFileProperties> getSourceFileProperties() {
                    List<SourceFileProperties> res = new ArrayList<SourceFileProperties>();
                    for (SourceFileProperties s : sources){
                        if (s.getCompilePath().startsWith(root)) {
                            res.add(s);
                        }
                    }
                    return res;
                }

                public List<Configuration> getDependencies() {
                    return null;
                }

                public List<SourceFileProperties> getSourcesConfiguration() {
                    if (myFileProperties == null) {
                        myFileProperties = getSourceFileProperties();
                    }
                    return myFileProperties;
                }

                public List<String> getIncludedFiles() {
                    if (myIncludedFiles == null) {
                        HashSet<String> set = new HashSet<String>();
                        for (SourceFileProperties source : getSourcesConfiguration()) {
                            set.add(source.getItemPath());
                        }
                        HashSet<String> unique = new HashSet<String>();
                        for (String path : set) {
                            File file = new File(path);
                            if (file.exists()) {
                                unique.add(FileUtil.normalizeFile(file).getAbsolutePath());
                            }
                        }
                        myIncludedFiles = new ArrayList<String>(unique);
                    }
                    return myIncludedFiles;
                }

            };
            confs.add(conf);
            return confs;
        }

        public void stop() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }
    }

}
