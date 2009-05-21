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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.discovery.api.Configuration;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.api.KnownProject;
import org.netbeans.modules.cnd.discovery.api.Progress;
import org.netbeans.modules.cnd.discovery.api.ProjectImpl;
import org.netbeans.modules.cnd.discovery.api.ProjectProperties;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.api.ProviderProperty;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.discovery.project.SolarisLogReader.InstallLine;
import org.netbeans.modules.cnd.discovery.wizard.ConsolidationStrategyPanel;
import org.netbeans.modules.cnd.discovery.wizard.SelectConfigurationPanel;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.netbeans.modules.cnd.discovery.wizard.api.ProjectConfiguration;
import org.netbeans.modules.cnd.discovery.wizard.tree.ConfigurationFactory;
import org.netbeans.modules.cnd.discovery.wizard.tree.ProjectConfigurationImpl;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author AlexanderSimon
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.discovery.api.KnownProject.class)
public class OpenSolaris extends KnownProject {
    private static final boolean TRACE = true;
    /*package-local*/ static final boolean READ_MAP_FILE_VERS = false;
    public static final String PROJECT_NAME = "open-solaris"; // NOI18N
    public static final String LOG_FILE = "nightly-log"; // NOI18N
    public static final String BUILD_SCRIPT = "build-script"; // NOI18N
    private String root;
    private String nb_root;
    private String nightly_log;
    private String buildScript;

    private List<SourceFileProperties> sources;
    private List<InstallLine> copyHeader;
    TreeMap<String,Set<String>> mapFiles;
    TreeMap<String,Set<String>> libraries;
    Map<String,String> secondLevel;
    
    public OpenSolaris(){
        secondLevel = new HashMap<String,String>();
        secondLevel.put("lib/gss_mechs/", "os.lib.gss_mechs."); // NOI18N
        secondLevel.put("lib/fm/", "os.lib.fm."); // NOI18N
        secondLevel.put("lib/hal/", "os.lib.hal."); // NOI18N
        secondLevel.put("lib/lvm/", "os.lib.lvm."); // NOI18N
        secondLevel.put("lib/libkfm/", "os.lib.libkfm."); // NOI18N
        secondLevel.put("lib/openssl/", "os.lib.openssl."); // NOI18N
        secondLevel.put("lib/policykit/", "os.lib.policykit."); // NOI18N
        secondLevel.put("lib/print/", "os.lib.print."); // NOI18N
        secondLevel.put("lib/scsi/", "os.lib.scsi."); // NOI18N
        secondLevel.put("lib/smbsrv/", "os.lib.smbsrv."); // NOI18N
        secondLevel.put("lib/udapl/", "os.lib.udapl."); // NOI18N
        secondLevel.put("cmd/sgs/", "os.cmd.sgs."); // NOI18N
        secondLevel.put("cmd/perl/", "os.cmd.perl."); // NOI18N
        secondLevel.put("cmd/ssh/", "os.cmd.ssh."); // NOI18N
        secondLevel.put("cmd/xntpd/", "os.cmd.xntpd."); // NOI18N
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
        nb_root = parameters.get(KnownProject.NB_ROOT);
        if (nb_root == null) {
            return false;
        }
        nightly_log = parameters.get(OpenSolaris.LOG_FILE);
        if (nightly_log == null) {
            nightly_log = findMakeLog(root);
            if (nightly_log == null) {
                return false;
            }
        } else {
            File log = new File(nightly_log);
            if (!log.exists() || log.isDirectory()){
                return false;
            }
        }
        buildScript = parameters.get(OpenSolaris.BUILD_SCRIPT);
        if (buildScript == null) {
            buildScript = "opensolaris.sh"; // NOI18N
        }
        return true;
    }

    @Override
    public boolean create(Map<String, String> parameters){
        try{
            if (TRACE) {System.out.println(NbBundle.getMessage(OpenSolaris.class, "CREATE_PROJECT"));} //NOI18N
            if (TRACE) {System.out.println(KnownProject.PROJECT+"="+OpenSolaris.PROJECT_NAME);} //NOI18N
            if (TRACE) {System.out.println(KnownProject.ROOT+"="+root);} //NOI18N
            if (TRACE) {System.out.println(KnownProject.NB_ROOT+"="+nb_root);} //NOI18N
            if (TRACE) {System.out.println(OpenSolaris.LOG_FILE+"="+nightly_log);} //NOI18N
            if (TRACE) {System.out.println(OpenSolaris.BUILD_SCRIPT+"="+buildScript);} //NOI18N
            if (TRACE) {System.out.println(NbBundle.getMessage(OpenSolaris.class, "SCANNING_LOG"));} //NOI18N
            sources = scan(nightly_log, root);
            if (sources == null || sources.size() == 0) {
                return false;
            }
            return createImpl();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        } finally {
            PathCache.dispose();
        }
    }

    private void createFolders(Map<String, ProjectSources> folders) throws IOException {
        FileObject fo = ProjectCreator.createProjectDir(new File(nb_root));
        for (Map.Entry<String, ProjectSources> entry : folders.entrySet()) {
            StringTokenizer st = new StringTokenizer(entry.getKey(), "/"); // NOI18N
            FileObject f = fo;
            while (st.hasMoreTokens()) {
                String fn = st.nextToken();
                boolean find = false;
                for (FileObject ff : f.getChildren()) {
                    if (fn.equals(ff.getNameExt())) {
                        f = ff;
                        find = true;
                        break;
                    }
                }
                if (!find) {
                    f = f.createFolder(fn);
                }
            }
        }
    }
    
    private String getSecondlevel(String path){
        for(String s : secondLevel.keySet()){
            if (path.startsWith(s)){
                return s;
            }
        }
        return null;
    }
    
    private boolean createImpl() throws IOException{
        File proto = new File(root+"/proto"); // NOI18N
        if (!proto.exists()) {
            if (TRACE) {System.out.println(NbBundle.getMessage(OpenSolaris.class, "INSTALLING_PROTO"));} //NOI18N
            for(InstallLine il : copyHeader){
                il.install();
            }
        }
        String srcRoot = root+"/usr/src/"; // NOI18N
        Map<String,ProjectSources> folders = new TreeMap<String,ProjectSources>();
        for(SourceFileProperties s : sources){
            String path = s.getCompilePath();
            if (path.startsWith(srcRoot)) {
                path = path.substring(srcRoot.length());
                if (getSecondlevel(path) != null){
                    String g = getSecondlevel(path); // NOI18N
                    String f = path.substring(g.length());
                    int i = f.indexOf('/'); // NOI18N
                    if (i > 0){
                        addFolder(folders, g+f.substring(0,i), f.substring(0,i), s);
                    } else if (f.length() >0){
                        addFolder(folders, g+f, f, s);
                    }
                } else if (path.startsWith("cmd/")){ // NOI18N
                    String f = path.substring(4);
                    int i = f.indexOf('/'); // NOI18N
                    if (i > 0){
                        addFolder(folders, "cmd/"+f.substring(0,i), f.substring(0,i), s); // NOI18N
                    } else if (f.length() >0){
                        addFolder(folders, "cmd/"+f, f, s); // NOI18N
                    }
                } else if (path.startsWith("lib/")){ // NOI18N
                    String f = path.substring(4);
                    int i = f.indexOf('/'); // NOI18N
                    if (i > 0){
                        addFolder(folders, "lib/"+f.substring(0,i), f.substring(0,i), s); // NOI18N
                    } else if (f.length() >0){
                        addFolder(folders, "lib/"+f, f, s); // NOI18N
                    }
                } else {
                    int i = path.indexOf('/'); // NOI18N
                    if (i > 0){
                        addFolder(folders, path.substring(0,i), path.substring(0,i), s);
                    } else {
                        addFolder(folders, path, path, s);
                    }
                }
            }
        }
        createFolders(folders);
        Set<String> projectList = folders.keySet();
        for (Map.Entry<String,ProjectSources> entry:folders.entrySet()){
            String r = srcRoot+entry.getKey();
            String n = nb_root+"/"+entry.getKey(); // NOI18N
            String name = entry.getValue().name;
            String display = entry.getValue().name;
            if (getSecondlevel(entry.getKey()) != null){
                String g = getSecondlevel(entry.getKey());
                display = secondLevel.get(g)+display;
            } else if (entry.getKey().startsWith("cmd/")){ // NOI18N
                display = "os.cmd."+display; // NOI18N
            } else if (entry.getKey().startsWith("lib/")){ // NOI18N
                display = "os.lib."+display; // NOI18N
            } else {
                display = "os."+display; // NOI18N
            }
            if (TRACE) {System.out.println(NbBundle.getMessage(OpenSolaris.class, "CREATING_PROJECT", n));} //NOI18N
            createImpl(r, n, name, display, projectList, entry.getValue().myFileProperties);
        }
        // and super projects
        if (TRACE) {System.out.println(NbBundle.getMessage(OpenSolaris.class, "CREATING_COMMANDS", nb_root));} //NOI18N
        createImpl(srcRoot+"cmd", nb_root+"/commands", "commands", "os.commands", projectList, sources); // NOI18N
        if (TRACE) {System.out.println(NbBundle.getMessage(OpenSolaris.class, "CREATING_LIBRARIES", nb_root));} //NOI18N
        createImpl(srcRoot+"lib", nb_root+"/libraries", "libraries", "os.libraries", projectList, sources);// NOI18N
        if (TRACE) {System.out.println(NbBundle.getMessage(OpenSolaris.class, "CREATING_SOURCES", nb_root));} //NOI18N
        createImpl(root+"/usr/src", nb_root+"/sources", "sources", "os.sources", projectList, sources); // NOI18N
        return true;
    }

    private void addFolder(Map<String,ProjectSources> folders, String folder, String name, SourceFileProperties s){
        ProjectSources l = folders.get(folder);
        if (l == null){
            l = new ProjectSources(name);
            folders.put(folder, l);
        }
        l.myFileProperties.add(s);
    }
    
    private boolean createImpl(String sourceRoot, String nbRoot, String name, String displayName,
            Set<String> folders, List<SourceFileProperties> list){
        DiscoveryProviderImpl provider = new DiscoveryProviderImpl(list, sourceRoot);
        DiscoveryDescriptorImpl discovery = new DiscoveryDescriptorImpl(null, provider, sourceRoot);
        ProjectCreator creator = new ProjectCreator(discovery);
        creator.init(nbRoot, sourceRoot, sourceRoot+"/Makefile", buildScript); // NOI18N
        try {
            creator.createProject(name, displayName, folders, getLibraries(displayName, sourceRoot, nbRoot));
            createGlobalList(sourceRoot, nbRoot, list);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
        return true;
    }

    private Set<String> getLibraries(String displayName, String sourceRoot, String nbRoot){
        Set<String> res = new HashSet<String>();
        if (displayName.indexOf(".sources")>0 || // NOI18N
            displayName.indexOf(".libraries")>0 || // NOI18N
            displayName.indexOf(".uts")>0 || // NOI18N
            displayName.indexOf(".commands")>0) { // NOI18N
            return res;
        }
        SortedMap<String,Set<String>> map = libraries.subMap(sourceRoot, sourceRoot+"/z"); // NOI18N
        for(Map.Entry<String, Set<String>> entry : map.entrySet()){
            for(String s: entry.getValue()) {
                res.add(s);
            }
        }
        if (res.size() > 0) {
            if (TRACE) {System.out.println(NbBundle.getMessage(OpenSolaris.class, "REQUIRED_PROJECTS", ""+res));} //NOI18N
        }
        return res;
    }
    
    private void createGlobalList(String sourceRoot, String nbRoot, List<SourceFileProperties> list){
        if (mapFiles != null) {
            Properties res = new Properties();
            SortedMap<String,Set<String>> map = mapFiles.subMap(sourceRoot, sourceRoot+"/z"); // NOI18N
            for(Map.Entry<String, Set<String>> entry : map.entrySet()){
                for(String s: entry.getValue()) {
                    res.put(s, "");
                }
            }
            if (res.size() == 0) {
                return;
            }
            try {
                if (TRACE) {System.out.println(NbBundle.getMessage(OpenSolaris.class, "WRITING_GLOBALS", ""+res.size()));} //NOI18N
                grepDefinitions(res, list);
                OutputStream os = new FileOutputStream(nbRoot + "/nbproject/private/globals.xml"); // NOI18N
                res.storeToXML(os, "Globals"); // NOI18N
                os.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    private void grepDefinitions(Properties res, List<SourceFileProperties> list){
        for(SourceFileProperties source : list){
            String path = source.getItemPath();
            File file = new File(path);
            if (file.exists() && file.canRead() && !file.isDirectory()){
                try {
                    BufferedReader in = new BufferedReader(new FileReader(file));
                    int lineNo = 0;
                    while(true){
                        String line = in.readLine();
                        if (line == null){
                            break;
                        }
                        lineNo++;
                        if (line.length()==0){
                            continue;
                        }
                        char c = line.charAt(0);
                        if (!(Character.isLetter(c)||c=='_')){
                            continue;
                        }
                        int i = line.indexOf('(');
                        if (i < 0) {
                            continue;
                        }
                        String id = line.substring(0,i).trim();
                        if (res.containsKey(id)){
                            res.put(id, path+":"+lineNo); // NOI18N
                        }
                    }
                    in.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private List<SourceFileProperties> scan(String log, String root){
        SolarisLogReader reader = new SolarisLogReader(log, root);
        List<SourceFileProperties> res = reader.getResults();
        copyHeader = reader.getInstalls();
        if (READ_MAP_FILE_VERS) {
            mapFiles = reader.readMapFile();
        }
        libraries = reader.getLibraries();
        return res;
    }

    private String findMakeLog(String root) {
        String latest = null;
        String logfolder = root + "/log"; // NOI18N
        File log = new File(logfolder);
        if (log.exists() && log.isDirectory() && log.canRead()) {
            for (File when : log.listFiles()) {
                if (when.exists() && when.isDirectory() && when.canRead()) {
                    for (File l : when.listFiles()) {
                        String current = l.getAbsolutePath();
                        if (current.endsWith("/nightly.log")) { // NOI18N
                            if (latest == null) {
                                latest = current;
                            } else {
                                String folder1 = latest.substring(0, latest.lastIndexOf("/nightly.log")); // NOI18N
                                String folder2 = current.substring(0, current.lastIndexOf("/nightly.log")); // NOI18N
                                if (folder1.compareTo(folder2) < 0) {
                                    latest = current;
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
            this.configurations = provider.analyze(null,null);
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

        public String getBuildLog() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public void setBuildLog(String logFile) {
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
                provider.analyze(null,null);
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

        public List<Configuration> analyze(ProjectProxy project, Progress progress) {
            List<Configuration> confs = new ArrayList<Configuration>();
            Configuration conf = new Configuration() {

                private List<SourceFileProperties> myFileProperties;
                private List<String> myIncludedFiles;

                public List<ProjectProperties> getProjectConfiguration() {
                    return ProjectImpl.divideByLanguage(getSourcesConfiguration());
                }

                private List<SourceFileProperties> getSourceFileProperties() {
                    List<SourceFileProperties> res = new ArrayList<SourceFileProperties>();
                    HashSet<String> set = new HashSet<String>();
                    for (SourceFileProperties s : sources){
                        if (s.getCompilePath().startsWith(root)) {
                            File file = new File(s.getItemPath());
                            String p = CndFileUtils.normalizeFile(file).getAbsolutePath();
                            if (!set.contains(p)) {
                                res.add(s);
                                set.add(p);
                            }
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
                            if (CndFileUtils.exists(file)) {
                                unique.add(CndFileUtils.normalizeFile(file).getAbsolutePath());
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

    public static class ProjectSources {
        private List<SourceFileProperties> myFileProperties = new ArrayList<SourceFileProperties>();
        private String name;
        private ProjectSources(String name){
            this.name = name;
        }
    }
    
}
