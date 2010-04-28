/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.makeproject;

import java.awt.Image;
import java.beans.BeanInfo;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.spi.jumpto.file.FileDescriptor;
import org.netbeans.spi.jumpto.file.FileProvider;
import org.netbeans.spi.jumpto.file.FileProviderFactory;
import org.netbeans.spi.jumpto.type.SearchType;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Alexander Simon
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.jumpto.file.FileProviderFactory.class, position=1000)
public class MakeProjectFileProviderFactory implements FileProviderFactory {

    private static final ConcurrentMap<Project, Map<Folder,List<CharSequence>>> searchBase = new ConcurrentHashMap<Project, Map<Folder, List<CharSequence>>>();

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
                    projectSearchBase.put(folder, Collections.<CharSequence>emptyList());
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

    private class FileProviderImpl implements FileProvider {
        private final AtomicBoolean cancel = new AtomicBoolean();

        public FileProviderImpl() {
        }

        @Override
        public boolean computeFiles(Context context, Result result) {
            if (!MakeOptions.getInstance().isFullFileIndexer()) {
                cancel.set(false);
                Project project = context.getProject();
                ConfigurationDescriptorProvider provider = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
                if (provider != null && provider.gotDescriptor()) {
                    MakeConfigurationDescriptor descriptor = provider.getConfigurationDescriptor();
                    Sources srcs = project.getLookup().lookup(Sources.class);
                    final SourceGroup[] genericSG = srcs.getSourceGroups("generic"); // NOI18N
                    if (genericSG != null && genericSG.length > 0) {
                        if (genericSG[0].getRootFolder().equals(context.getRoot())) {
                            computeFiles(project, descriptor, context.getSearchType(), context.getText(), result);
                        }
                    }
                    return true;
                }
            }
            return false;
        }

        @Override
        public void cancel() {
            cancel.set(true);
        }

        private void computeFiles(Project project, MakeConfigurationDescriptor descriptor, SearchType type, String what, Result result) {
            Pattern pattern = null;
            switch (type) {
                case CAMEL_CASE:
                    {
                        final StringBuilder patternString = new StringBuilder();
                        for (int i = 0; i < what.length(); i++) {
                            char c = what.charAt(i);
                            patternString.append(c);
                            if (i == what.length() - 1) {
                                patternString.append("\\w*"); // NOI18N
                            } else {
                                patternString.append("[\\p{Lower}\\p{Digit}]*"); // NOI18N
                            }
                        }
                        pattern = Pattern.compile(patternString.toString());
                        break;
                    }
                case CASE_INSENSITIVE_PREFIX:
                case CASE_INSENSITIVE_EXACT_NAME:
                    what = what.toLowerCase();
                    break;
                case CASE_INSENSITIVE_REGEXP:
                case REGEXP:
                    pattern = Pattern.compile(what);
                    break;
            }
            FileObject projectDirectoryFO = project.getProjectDirectory();
            // track configuration && generated files
            if (projectDirectoryFO != null) {
                FileObject nbFO = projectDirectoryFO.getFileObject(MakeConfiguration.NBPROJECT_FOLDER);
                computeFOs(nbFO, type, what, pattern, result);
            }
            for (Item item : descriptor.getExternalFileItemsAsArray()) {
                if (cancel.get()) {
                    return;
                }
                if (match(item, type, what, pattern)) {
                    result.addFileDescriptor(new ItemFD(item, project));
                }
            }
            for (Item item : descriptor.getProjectItems()) {
                if (cancel.get()) {
                    return;
                }
                if (match(item, type, what, pattern)) {
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
                            if (match(name.toString(), type, what, pattern)) {
                                result.addFileDescriptor(new OtherFD(name.toString(), project, baseDir, folder));
                            }
                        }
                    }
                }
            }
        }

        private boolean match(Item item, SearchType type, String what, Pattern pattern) {
            return match(item.getName(), type, what, pattern);
        }

        private boolean match(String name, SearchType type, String what, Pattern pattern) {
            switch (type) {
                case CAMEL_CASE:
                    return pattern.matcher(name).matches();
                case CASE_INSENSITIVE_EXACT_NAME:
                    return name.toLowerCase().equals(what);
                case CASE_INSENSITIVE_PREFIX:
                    return name.toLowerCase().startsWith(what);
                case CASE_INSENSITIVE_REGEXP:
                    return pattern.matcher(name.toLowerCase()).matches();
                case EXACT_NAME:
                    return name.equals(what);
                case PREFIX:
                    return name.startsWith(what);
                case REGEXP:
                    return pattern.matcher(name).matches();
            }
            return false;
        }

        private void computeFOs(FileObject nbFO, SearchType searchType, String type, Pattern what, Result result) {
            if (nbFO != null) {
                assert nbFO.isFolder();
                for (FileObject fileObject : nbFO.getChildren()) {
                    if (fileObject.isFolder()) {
                        computeFOs(fileObject, searchType, type, what, result);
                    } else if (match(fileObject.getNameExt(), searchType, type, what)) {
                        result.addFile(fileObject);
                    }
                }
            }
        }
    }

    private static final class ItemFD extends FileDescriptor {

        private final Item item;
        private final Project project;

        public ItemFD(Item item, Project project) {
            this.item = item;
            this.project = project;
        }


        @Override
        public String getFileName() {
            return item.getName();
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
        public Icon getIcon() {
            DataObject od = item.getDataObject();
            if (od != null) {
                Image i = od.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16);
                return new ImageIcon(i);
            }
            return null;
        }

        @Override
        public String getProjectName() {
            return ((MakeProject)project).getName();
        }

        @Override
        public Icon getProjectIcon() {
            return ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/makeproject/ui/resources/makeProject.gif", true); // NOI18N
        }

        @Override
        public void open() {
            DataObject od = item.getDataObject();
            if (od != null) {
                EditCookie ec = od.getCookie(EditCookie.class);
                if (ec != null) {
                    ec.edit();
                } else {
                    OpenCookie oc = od.getCookie(OpenCookie.class);
                    if (oc != null) {
                        oc.open();
                    }
                }
            }
        }

        @Override
        public FileObject getFileObject() {
            return item.getFileObject();
        }
    }

    private static final class OtherFD extends FileDescriptor {

        private final String name;
        private final Project project;
        private final Folder folder;
        private final String baseDir;
        public OtherFD(String name, Project project, String baseDir, Folder folder) {
            this.name = name;
            this.project = project;
            this.folder = folder;
            this.baseDir = baseDir;
        }


        @Override
        public String getFileName() {
            return name;
        }

        @Override
        public String getOwnerPath() {
            return folder.getPath();
        }

        @Override
        public Icon getIcon() {
            DataObject od = getDataObject();
            if (od != null) {
                Image i = od.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16);
                return new ImageIcon(i);
            }
            return null;
        }

        @Override
        public String getProjectName() {
            return ((MakeProject)project).getName();
        }

        @Override
        public Icon getProjectIcon() {
            return ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/makeproject/ui/resources/makeProject.gif", true); // NOI18N
        }

        @Override
        public void open() {
            DataObject od = getDataObject();
            if (od != null) {
                EditCookie ec = od.getCookie(EditCookie.class);
                if (ec != null) {
                    ec.edit();
                } else {
                    OpenCookie oc = od.getCookie(OpenCookie.class);
                    if (oc != null) {
                        oc.open();
                    }
                }
            }
        }

        private DataObject getDataObject(){
            try {
                FileObject fo = getFileObject();
                if (fo != null) {
                    return DataObject.find(fo);
                }
            } catch (DataObjectNotFoundException e) {
            }
            return null;
        }

        @Override
        public FileObject getFileObject() {
            String AbsRootPath = CndPathUtilitities.toAbsolutePath(baseDir, folder.getRootPath());
            File file = new File(AbsRootPath, name);
            return FileUtil.toFileObject(file);
        }
    }
}
