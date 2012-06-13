/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.makeproject;

import java.awt.Image;
import java.beans.BeanInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.actions.Editable;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.cnd.api.project.NativeFileSearch;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectRegistry;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.spi.configurations.UserOptionsProvider;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.spi.jumpto.file.FileDescriptor;
import org.netbeans.spi.jumpto.file.FileProvider;
import org.netbeans.spi.jumpto.file.FileProviderFactory;
import org.netbeans.spi.jumpto.support.NameMatcher;
import org.netbeans.spi.jumpto.support.NameMatcherFactory;
import org.netbeans.spi.jumpto.type.SearchType;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.CharSequences;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 *
 * @author Alexander Simon
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.jumpto.file.FileProviderFactory.class, position=1000)
public class MakeProjectFileProviderFactory implements FileProviderFactory {

    private static final ConcurrentMap<Lookup.Provider, Map<Folder,List<CharSequence>>> searchBase = new ConcurrentHashMap<Lookup.Provider, Map<Folder, List<CharSequence>>>();
    private static final ConcurrentMap<Lookup.Provider, ConcurrentMap<CharSequence,List<CharSequence>>> fileNameSearchBase = new ConcurrentHashMap<Lookup.Provider, ConcurrentMap<CharSequence, List<CharSequence>>>();
    private static final Collection<? extends UserOptionsProvider> packageSearch = Lookup.getDefault().lookupAll(UserOptionsProvider.class);

    /**
     * Store/update/remove list of non cnd files for project folder
     *
     * @param project
     * @param folder
     * @param list
     */
    public static void updateSearchBase(Project project, Folder folder, List<CharSequence> list){
        Map<Folder, List<CharSequence>> projectSearchBase = searchBase.get(project);
        if (projectSearchBase == null) {
            projectSearchBase = new ConcurrentHashMap<Folder, List<CharSequence>>();
            Map<Folder, List<CharSequence>> old = searchBase.putIfAbsent(project, projectSearchBase);
            if (old != null) {
                projectSearchBase = old;
            }
        }
        synchronized (projectSearchBase) {
            if (list == null) {
                projectSearchBase.remove(folder);
            } else {
                if (list.isEmpty()) {
                    projectSearchBase.put(folder, new ArrayList<CharSequence>(0));
                } else {
                    projectSearchBase.put(folder, list);
                }
            }
        }
    }

    /**
     * Remove project files search base
     *
     * @param project
     */
    public static void removeSearchBase(Project project){
        searchBase.remove(project);
    }

    public static void removeFromSearchBase(Project project, Folder folder, CharSequence item) {
        updateSearchBaseImpl(project, folder, item, true);
    }

    public static void addToSearchBase(Project project, Folder folder, CharSequence item) {
        updateSearchBaseImpl(project, folder, item, false);
    }

    private static void updateSearchBaseImpl(Project project, Folder folder, CharSequence item, boolean remove) {
        Map<Folder, List<CharSequence>> projectSearchBase = searchBase.get(project);
        if (projectSearchBase != null) {
            synchronized (projectSearchBase) {
                List<CharSequence> folderItems = projectSearchBase.get(folder);
                if (folderItems != null) {
                    if (remove) {
                        folderItems.remove(item);
                    } else {
                        folderItems.add(item);
                    }
                }
            }
        }
        ConcurrentMap<CharSequence, List<CharSequence>> projectFileNames = fileNameSearchBase.get(project);
        if (projectFileNames != null) {
            String fileName = item.toString();
            int i = fileName.lastIndexOf('/');
            String name = fileName;
            if (i >= 0) {
                name = fileName.substring(i + 1);
            }
            Collection<CharSequence> res = projectFileNames.get(CharSequences.create(name));
            if (res != null) {
                synchronized (res) {
                    if (remove) {
                        res.remove(item);
                    } else {
                        res.add(item);
                    }
                }
            }
        }
    }
    
    @Override
    public String name() {
        return "CND FileProviderFactory"; // NOI18N
    }

    @Override
    public String getDisplayName() {
        return name();
    }

    @Override
    public FileProvider createFileProvider() {
        return new FileProviderImpl();
    }

    private final static class FileProviderImpl implements FileProvider, NativeFileSearch {
        private final AtomicBoolean cancel = new AtomicBoolean();
        private final Set<SearchContext> searchedProjects = new HashSet<SearchContext>();
        private Context lastContext = null;
        
        public FileProviderImpl() {
        }

        @Override
        public boolean computeFiles(Context context, Result result) {
            if (!MakeOptions.getInstance().isFullFileIndexer()) {
                cancel.set(false);
                Project project = context.getProject();
                SearchContext searchContext = new SearchContext(context.getText(), context.getSearchType(), project);
                if (project != null) {
                    // check if anything have changed in context, just compare context instances
                    if (context != lastContext) {
                        lastContext = context;
                        searchedProjects.clear();
                    }
                    if (searchedProjects.add(searchContext)) {
                        ConfigurationDescriptorProvider provider = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
                        if (provider != null && provider.gotDescriptor()) {
                            MakeConfigurationDescriptor descriptor = provider.getConfigurationDescriptor();
                            Sources srcs = project.getLookup().lookup(Sources.class);
                            final SourceGroup[] genericSG = srcs.getSourceGroups("generic"); // NOI18N
                            if (genericSG != null && genericSG.length > 0) {
                                for(SourceGroup group : genericSG) {
                                    if (group.getRootFolder().equals(context.getRoot())) {
                                        NameMatcher matcher = NameMatcherFactory.createNameMatcher(context.getText(), context.getSearchType());
                                        computeFiles(project, descriptor, matcher, result);
                                        break;
                                    }
                                }
                            }
                            return false;
                        }
                    } else {
                        System.err.println("MakeProjectFileProviderFactory.FileProviderImpl.computeFiles: skip already searched context " + searchContext);// NOI18N
                    }
                } else {
                    System.err.println("MakeProjectFileProviderFactory.FileProviderImpl.computeFiles: no project for source root " + context.getRoot());// NOI18N
                }
            }
            return false;
        }

        @Override
        public Collection<CharSequence> searchFile(NativeProject project, String fileName) {
            if (MakeOptions.getInstance().isFixUnresolvedInclude()) {
                Collection<CharSequence> res;
                for(NativeProject np : NativeProjectRegistry.getDefault().getOpenProjects()) {
                    if (np == project) {
                        Lookup.Provider p = np.getProject();
                        if (p instanceof Project) {
                            ConcurrentMap<CharSequence,List<CharSequence>> projectSearchBase = fileNameSearchBase.get(p);
                            if (projectSearchBase == null) {
                                projectSearchBase = computeProjectFiles(p);
                                fileNameSearchBase.put(p, projectSearchBase);
                            }
                            int i = fileName.lastIndexOf('/');
                            String name = fileName;
                            if (i >= 0) {
                                name = fileName.substring(i+1);
                            }
                            res = projectSearchBase.get(CharSequences.create(name));
                            if (res != null && res.size() > 0) {
                                return res;
                            }
                            MakeConfiguration conf = ConfigurationSupport.getProjectActiveConfiguration((Project)p);
                            ExecutionEnvironment env;
                            if (conf != null){
                                env = conf.getDevelopmentHost().getExecutionEnvironment();
                            } else {
                                env = ExecutionEnvironmentFactory.getLocal();
                            }
                            res = defaultSearch(project, fileName, env);
                            if (res != null && res.size() > 0) {
                                return res;
                            }
                            return Collections.<CharSequence>emptyList();
                        }
                    }
                }
                // Standalone project
                ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(project.getFileSystem());
                if (env == null) {
                    env = ExecutionEnvironmentFactory.getLocal();
                }
                res = defaultSearch(project, fileName, env);
                if (res != null && res.size() > 0) {
                    return res;
                }
            }
            return Collections.<CharSequence>emptyList();
        }

        private Collection<CharSequence> defaultSearch(NativeProject project, String fileName, ExecutionEnvironment env) {
            Collection<CharSequence> res = null;
            if (env == null) {
                env = ExecutionEnvironmentFactory.getLocal();
            }
            boolean isDoSearch = false;
            if (env.isLocal()) {
                isDoSearch = true;
            } else {
                if (Boolean.valueOf(System.getProperty("cnd.pkg.search.enabled", "true"))) {
                    isDoSearch = ConnectionManager.getInstance().isConnectedTo(env);
                }
            }
            if (!packageSearch.isEmpty() && isDoSearch) {
                for (UserOptionsProvider userOptionsProvider : packageSearch) {
                    NativeFileSearch search = userOptionsProvider.getPackageFileSearch(env);
                    if (search != null) {
                        res = search.searchFile(project, fileName);
                        if(res != null) {
                            break;
                        }
                    }
                }
            }
            return res;
        }
        
        private ConcurrentMap<CharSequence,List<CharSequence>> computeProjectFiles(Lookup.Provider project) {
            ConcurrentMap<CharSequence,List<CharSequence>> result = new ConcurrentHashMap<CharSequence,List<CharSequence>>();
            ConfigurationDescriptorProvider provider = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
            if (provider != null && provider.gotDescriptor()) {
                MakeConfigurationDescriptor descriptor = provider.getConfigurationDescriptor();
                for (Item item : descriptor.getProjectItems()) {
                    CharSequence name = CharSequences.create(item.getName());
                    List<CharSequence> list = result.get(name);
                    if (list == null) {
                        List<CharSequence> prev = result.putIfAbsent(name, list = new ArrayList<CharSequence>(1));
                        if (prev != null) {
                            list = prev;
                        }
                    }
                    list.add(CharSequences.create(item.getAbsPath()));
                }
                if (!MakeOptions.getInstance().isFullFileIndexer()) {
                    Map<Folder,List<CharSequence>> projectSearchBase = searchBase.get(project);
                    if (projectSearchBase != null) {
                        // create copy of data
                        synchronized (projectSearchBase) {
                            projectSearchBase = new HashMap<Folder, List<CharSequence>>(projectSearchBase);
                        }
                        for (List<CharSequence> files : projectSearchBase.values()) {
                            if (files != null) {
                                for(CharSequence path : files) {
                                    String absPath = path.toString();
                                    int i = absPath.lastIndexOf('/');
                                    if (i < 0) {
                                        i = absPath.lastIndexOf('\\');
                                    }
                                    if (i >= 0) {
                                        CharSequence name = CharSequences.create(absPath.substring(i+1));
                                        List<CharSequence> list = result.get(name);
                                        if (list == null) {
                                            List<CharSequence> prev = result.putIfAbsent(name, list = new ArrayList<CharSequence>(1));
                                            if (prev != null) {
                                                list = prev;
                                            }
                                        }
                                        list.add(path);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return result;
        }

        @Override
        public void cancel() {
            cancel.set(true);
        }

        private void computeFiles(Project project, MakeConfigurationDescriptor descriptor, NameMatcher matcher, Result result) {
            FileObject projectDirectoryFO = project.getProjectDirectory();
            // track configuration && generated files
            if (projectDirectoryFO != null) {
                FileObject nbFO = projectDirectoryFO.getFileObject(MakeConfiguration.NBPROJECT_FOLDER);
                computeFOs(nbFO, matcher, result);
            }
            for (Item item : descriptor.getExternalFileItemsAsArray()) {
                if (cancel.get()) {
                    return;
                }
                if (matcher.accept(item.getName())) {
                    result.addFileDescriptor(new ItemFD(item, project));
                }
            }
            for (Item item : descriptor.getProjectItems()) {
                if (cancel.get()) {
                    return;
                }
                if (matcher.accept(item.getName())) {
                    result.addFileDescriptor(new ItemFD(item, project));
                }
            }
            Map<Folder,List<CharSequence>> projectSearchBase = searchBase.get(project);
            if (projectSearchBase != null) {
                synchronized (projectSearchBase) {
                    projectSearchBase = new HashMap<Folder, List<CharSequence>>(projectSearchBase);
                }
                String baseDir = descriptor.getBaseDir();
                for (Map.Entry<Folder, List<CharSequence>> entry : projectSearchBase.entrySet()) {
                    if (cancel.get()) {
                        return;
                    }
                    Folder folder = entry.getKey();
                    List<CharSequence> files = entry.getValue();
                    if (files != null) {
                        for(CharSequence name : files) {
                            if (cancel.get()) {
                                return;
                            }
                            if (matcher.accept(name.toString())) {
                                result.addFileDescriptor(new OtherFD(name.toString(), project, baseDir, folder));
                            }
                        }
                    }
                }
            }
        }

        private void computeFOs(FileObject nbFO, NameMatcher matcher, Result result) {
            if (nbFO != null) {
                assert nbFO.isFolder();
                for (FileObject fileObject : nbFO.getChildren()) {
                    if (fileObject.isFolder()) {
                        computeFOs(fileObject, matcher, result);
                    } else if (matcher.accept(fileObject.getNameExt())) {
                        result.addFile(fileObject);
                    }
                }
            }
        }
        
        private static final class SearchContext {

            private final String searchText;
            private final SearchType searchType;
            private final Project project;

            public SearchContext(String searchText, SearchType searchType, Project project) {
                this.searchText = searchText;
                this.searchType = searchType;
                this.project = project;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                final SearchContext other = (SearchContext) obj;
                if ((this.searchText == null) ? (other.searchText != null) : !this.searchText.equals(other.searchText)) {
                    return false;
                }
                if (this.searchType != other.searchType) {
                    return false;
                }
                if (this.project != other.project && (this.project == null || !this.project.equals(other.project))) {
                    return false;
                }
                return true;
            }

            @Override
            public int hashCode() {
                int hash = 7;
                hash = 43 * hash + (this.searchText != null ? this.searchText.hashCode() : 0);
                hash = 43 * hash + (this.searchType != null ? this.searchType.hashCode() : 0);
                hash = 43 * hash + (this.project != null ? this.project.hashCode() : 0);
                return hash;
            }

            @Override
            public String toString() {
                return "SearchContext{" + "searchText=" + searchText + ", searchType=" + searchType + ", project=" + project + '}'; // NOI18N
            }
        }
    }

    private static abstract class FDImpl extends FileDescriptor {
        private final String fileName;
        private final Project project;

        public FDImpl(String fileName, Project project) {
            this.fileName = fileName;
            this.project = project;
        }

        @Override
        public final String getFileName() {
            return fileName;
        }
        
        @Override
        public final String getProjectName() {
            MakeProject.InfoInterface info = (MakeProject.InfoInterface) project.getLookup().lookup(ProjectInformation.class);
            return info.getName();
        }   
        
        @Override
        public final Icon getProjectIcon() {
            return ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/makeproject/ui/resources/makeProject.gif", true); // NOI18N
        }       
        
        @Override
        public final void open() {
            DataObject od = getDataObject();
            if (od != null) {
                // comment out trick
//                // use trick due to CR7002932
//                EditorCookie erc = od.getCookie(EditorCookie.class);
//                if (erc != null) {
//                    try {
//                        try {
//                            erc.openDocument();
//                        } catch (UserQuestionException e) {
//                            e.confirmed();
//                            erc.openDocument();
//                        }
//                    } catch (IOException ex) {
//                        Exceptions.printStackTrace(ex);
//                    }
//                    erc.open();
//                } else {
                    Editable ec = od.getLookup().lookup(Editable.class);
                    if (ec != null) {
                        ec.edit();
                    } else {
                        Openable oc = od.getLookup().lookup(Openable.class);
                        if (oc != null) {
                            oc.open();
                        }
                    }
//                }
            }
        }
        
        @Override
        public final Icon getIcon() {
            DataObject od = getDataObject();
            if (od != null) {
                Image i = od.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16);
                return new ImageIcon(i);
            }
            return null;
        }
        
        protected abstract DataObject getDataObject();
        
    }
    
    private static final class ItemFD extends FDImpl {

        private final Item item;

        public ItemFD(Item item, Project project) {
            super(item.getName(), project);
            this.item = item;
        }

        @Override
        public String getOwnerPath() {
            StringBuilder out = new StringBuilder();
            Folder parent = item.getFolder();
            while (parent != null && parent.getParent() != null) {
                if (out.length() > 0) {
                    out.insert(0, "/"); // NOI18N
                }
                out.insert(0, parent.getDisplayName());
                parent = parent.getParent();
            }
            return out.toString();
        }

        @Override
        public FileObject getFileObject() {
            return item.getFileObject();
        }

        @Override
        protected DataObject getDataObject() {
            return item.getDataObject();
        }
    }

    private static final class OtherFD extends FDImpl {

        private final String name;
        private final Folder folder;
        private final String baseDir;
        public OtherFD(String name, Project project, String baseDir, Folder folder) {
            super(name, project);
            this.name = name;
            this.folder = folder;
            this.baseDir = baseDir;
        }

        @Override
        public String getOwnerPath() {
            return folder.getPath();
        }

        @Override
        protected DataObject getDataObject(){
            try {
                FileObject fo = getFileObject();
                if (fo != null && fo.isValid()) {
                    return DataObject.find(fo);
                }
            } catch (DataObjectNotFoundException e) {
            }
            return null;
        }

        @Override
        public FileObject getFileObject() {
            FileObject fileObject = RemoteFileUtil.getFileObject(folder.getConfigurationDescriptor().getBaseDirFileObject(), folder.getRootPath()+"/"+name); //NOI18N
            if (fileObject != null && fileObject.isValid()) {
                return fileObject;
            }
            return null;
        }
    }
}
