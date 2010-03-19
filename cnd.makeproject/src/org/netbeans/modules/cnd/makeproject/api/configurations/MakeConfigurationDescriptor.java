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
package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.utils.CndFileVisibilityQuery;
import org.netbeans.modules.cnd.api.utils.CndVisibilityQuery;
import org.netbeans.modules.cnd.makeproject.configurations.ConfigurationMakefileWriter;
import org.netbeans.modules.cnd.makeproject.configurations.ConfigurationXMLWriter;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.makeproject.MakeProject;
import org.netbeans.modules.cnd.makeproject.MakeProjectType;
import org.netbeans.modules.cnd.makeproject.MakeSources;
import org.netbeans.modules.cnd.makeproject.NativeProjectProvider;
import org.netbeans.modules.cnd.makeproject.api.SourceFolderInfo;
import org.netbeans.modules.cnd.makeproject.configurations.CommonConfigurationXMLCodec;
import org.netbeans.modules.cnd.makeproject.ui.MakeLogicalViewProvider;
import org.netbeans.modules.cnd.api.toolchain.ui.ToolsPanelSupport;
import org.netbeans.modules.cnd.makeproject.MakeOptions;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectOptions;
import org.netbeans.modules.cnd.makeproject.configurations.CppUtils;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MakeConfigurationDescriptor extends ConfigurationDescriptor implements ChangeListener {

    public static final String EXTERNAL_FILES_FOLDER = "ExternalFiles"; // NOI18N
    public static final String TEST_FILES_FOLDER = "TestFiles"; // NOI18N
    public static final String SOURCE_FILES_FOLDER = "SourceFiles"; // NOI18N
    public static final String HEADER_FILES_FOLDER = "HeaderFiles"; // NOI18N
    public static final String RESOURCE_FILES_FOLDER = "ResourceFiles"; // NOI18N
    public static final String ICONBASE = "org/netbeans/modules/cnd/makeproject/ui/resources/makeProject"; // NOI18N
    public static final String ICON = "org/netbeans/modules/cnd/makeproject/ui/resources/makeProject.gif"; // NOI18N
    public static final Icon MAKEFILE_ICON = ImageUtilities.loadImageIcon(ICON, false); // NOI18N
    public static final String DEFAULT_IGNORE_FOLDERS_PATTERN = "^(nbproject|build|test)$"; // NOI18N
    public static final String DEFAULT_NO_IGNORE_FOLDERS_PATTERN = "^$"; // NOI18N
    private Project project = null;
    private String baseDir;
    private boolean modified = false;
    private Folder externalFileItems = null;
    private Folder testItems = null;
    private Folder rootFolder = null;
    private HashMap<String, Item> projectItems = null;
    private final List<String> sourceRoots = new ArrayList<String>();
    private final List<String> testRoots = new ArrayList<String>();
    private final Set<ChangeListener> projectItemsChangeListeners = new HashSet<ChangeListener>();
    private volatile NativeProject nativeProject = null;
    public static final String DEFAULT_PROJECT_MAKFILE_NAME = "Makefile"; // NOI18N
    private String projectMakefileName = DEFAULT_PROJECT_MAKFILE_NAME;
    private Task initTask = null;
    private CndVisibilityQuery folderVisibilityQuery = null;

    public MakeConfigurationDescriptor(String baseDir) {
        super();
        this.baseDir = baseDir;
        rootFolder = new Folder(this, null, "root", "root", true); // NOI18N
        projectItems = new HashMap<String, Item>();
        setModified();
        ToolsPanelSupport.addCompilerSetModifiedListener(MakeConfigurationDescriptor.this);
    }

    /*
     * Called when project is being closed
     */
    @Override
    public void closed() {
        ToolsPanelSupport.removeCompilerSetModifiedListener(this);
        for (Item item : getProjectItems()) {
            DataObject dao = item.getDataObject();
            if (dao != null) {
                dao.removePropertyChangeListener(item);
            }
        }
        closed(rootFolder);
    }

    private void closed(Folder folder) {
        if (folder != null) {
            for (Folder f : folder.getAllFolders(false)) {
                f.detachListener();
            }
            folder.detachListener();
        }
    }

    public void clean() {
        Configurations confs = getConfs();
        if (confs != null) {
            for (Configuration conf : confs.toArray()) {
                if (conf != null) {
                    conf.setAuxObjects(Collections.<ConfigurationAuxObject>emptyList());
                }
            }
        }
        projectItems.clear();
        rootFolder = null;
    }

    public static MakeConfigurationDescriptor getMakeConfigurationDescriptor(Project project) {
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        if (pdp != null) {
            MakeConfigurationDescriptor makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
            return makeConfigurationDescriptor;
        } else {
            return null;
        }
    }

    /** NPE-safe method for getting active configuration */
    public MakeConfiguration getActiveConfiguration() {
        Configurations confs = getConfs();
        if (confs != null) {
            MakeConfiguration conf = (MakeConfiguration) confs.getActive();
            if (conf == null) {
                if (MakeProject.TRACE_MAKE_PROJECT_CREATION) {
                    new Exception("There are no active configuration in the project descriptor MakeConfigurationDescriptor@" + System.identityHashCode(this) + " for project " + getBaseDir()).printStackTrace(); // NOI18N
                }
            }
            return conf;
        } else {
            if (MakeProject.TRACE_MAKE_PROJECT_CREATION) {
                new Exception("There are no configurations in the project descriptor MakeConfigurationDescriptor@" + System.identityHashCode(this) + " for project " + getBaseDir()).printStackTrace(); // NOI18N
            }
        }
        return null;
    }

    /*
     * One of the compiler sets have changed.
     * Mark project modified. This will trigger all makefiles to be regenerated.
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        setModified();
    }

    public Project getProject() {
        if (project == null) {
            String location = getBaseDir();
            try {
                // convert base path into file object
                // we can't use canonical path here, because descriptor created with path like
                // /set/ide/mars/... will be changed by canonization into
                // /net/endif/export/home1/deimos/dev/...
                // and using the canonical path based FileObject in the ProjectManager.getDefault().findProject(fo);
                // will cause creating new MakeProject project
                // because there are no opened /net/endif/export/home1/deimos/dev/... project in system
                // there is only /set/ide/mars/... project in system
                //
                // in fact ProjectManager should solve such problems in more general way
                // because even for java it's possible to open the same project from two different
                // locations /set/ide/mars/... and /net/endif/export/home1/deimos/dev/...
                FileObject fo = FileUtil.toFileObject(new File(location));
                project = ProjectManager.getDefault().findProject(fo);
            } catch (Exception e) {
                // Should not happen
                System.err.println("Cannot find project in '" + location + "' " + e); // FIXUP // NOI18N
            }
        }
        return project;
    }

    public void init(Configuration def) {
        super.init(new Configuration[]{def}, 0);
        setModified();
    }

    public void setInitTask(Task task) {
        initTask = task;
    }

    /*package-local*/ synchronized void waitInitTask() {
        if (initTask == null) {
            return;
        }
        initTask.waitFinished();
        initTask = null;
    }

    public void initLogicalFolders(Iterator<SourceFolderInfo> sourceFileFolders, boolean createLogicalFolders, Iterator<SourceFolderInfo> testFileFolders, Iterator<String> importantItems, String mainFilePath) {
        if (createLogicalFolders) {
            rootFolder.addNewFolder(SOURCE_FILES_FOLDER, getString("SourceFilesTxt"), true, Folder.Kind.SOURCE_LOGICAL_FOLDER);
            rootFolder.addNewFolder(HEADER_FILES_FOLDER, getString("HeaderFilesTxt"), true, Folder.Kind.SOURCE_LOGICAL_FOLDER);
            rootFolder.addNewFolder(RESOURCE_FILES_FOLDER, getString("ResourceFilesTxt"), true, Folder.Kind.SOURCE_LOGICAL_FOLDER);
            testItems = rootFolder.addNewFolder(TEST_FILES_FOLDER, getString("TestsFilesTxt"), false, Folder.Kind.TEST_LOGICAL_FOLDER);
        }
        externalFileItems = rootFolder.addNewFolder(EXTERNAL_FILES_FOLDER, getString("ImportantFilesTxt"), false, Folder.Kind.IMPORTANT_FILES_FOLDER);
//        if (sourceFileFolders != null)
//            setExternalFileItems(sourceFileFolders); // From makefile wrapper wizard
        externalFileItems.addItem(new Item(getProjectMakefileName())); // NOI18N
        if (importantItems != null) {
            while (importantItems.hasNext()) {
                externalFileItems.addItem(new Item(importantItems.next()));
            }
        }
//        addSourceFilesFromFolders(sourceFileFolders, false, false, true
        // Add main file
        if (mainFilePath != null) {
            Folder srcFolder = rootFolder.findFolderByName(MakeConfigurationDescriptor.SOURCE_FILES_FOLDER);
            if (srcFolder != null) {
                srcFolder.addItem(new Item(mainFilePath));
            }
        }
        // Handle test folders
        if (testFileFolders != null) {
            while (testFileFolders.hasNext()) {
                SourceFolderInfo sourceFolderInfo = testFileFolders.next();
                addTestRoot(sourceFolderInfo.getFolderName());
            }
        }
        // Handle source root folders
        if (sourceFileFolders != null) {
            while (sourceFileFolders.hasNext()) {
                SourceFolderInfo sourceFolderInfo = sourceFileFolders.next();
                addFilesFromRoot(getLogicalFolders(), sourceFolderInfo.getFile(), false, true, null);
            }
        }
        setModified();
    }

    public String getProjectMakefileName() {
        return projectMakefileName;
    }

    public void setProjectMakefileName(String projectMakefileName) {
        this.projectMakefileName = projectMakefileName;
    }

    /**
     * @deprecated Use org.netbeans.modules.cnd.api.project.NativeProject interface instead.
     */
    @Deprecated
    public void addProjectItemsChangeListener(ChangeListener cl) {
        synchronized (projectItemsChangeListeners) {
            projectItemsChangeListeners.add(cl);
        }
    }

    /**
     * @deprecated Use org.netbeans.modules.cnd.api.project.NativeProject interface instead.
     */
    @Deprecated
    public void removeProjectItemsChangeListener(ChangeListener cl) {
        synchronized (projectItemsChangeListeners) {
            projectItemsChangeListeners.remove(cl);
        }
    }

    public void fireProjectItemsChangeEvent(Item item, int action) {
        Iterator<ChangeListener> it;

        synchronized (projectItemsChangeListeners) {
            it = new HashSet<ChangeListener>(projectItemsChangeListeners).iterator();
        }
        ChangeEvent ev = new ProjectItemChangeEvent(this, item, action);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }

    public Set<ChangeListener> getProjectItemsChangeListeners() {
        synchronized (projectItemsChangeListeners) {
            return new HashSet<ChangeListener>(projectItemsChangeListeners);
        }
    }

    public void setProjectItemsChangeListeners(Set<ChangeListener> newChangeListeners) {
        synchronized (this.projectItemsChangeListeners) {
            this.projectItemsChangeListeners.clear();
            this.projectItemsChangeListeners.addAll(newChangeListeners);
        }
    }

    @Override
    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public HashMap<String, Item> getProjectItemsMap() {
        return projectItems;
    }

    public void setProjectItemsMap(HashMap<String, Item> projectItems) {
        this.projectItems = projectItems;
    }

    public void init(Configuration[] confs) {
        super.init(confs, 0);
    }

    @Override
    public Icon getIcon() {
        return MAKEFILE_ICON;
    }

    @Override
    public Configuration defaultConf(String name, int type) {
        MakeConfiguration c = new MakeConfiguration(this.getBaseDir(), name, type, CppUtils.getDefaultDevelopmentHost(), null);
        Item[] items = getProjectItems();
        for (int i = 0; i < items.length; i++) {
            c.addAuxObject(new ItemConfiguration(c, items[i]));
        }
        return c;
    }

    // External File Items
    public void setExternalFileItems(List<String> items) {
        externalFileItems.reset();
        for (String s : items) {
            externalFileItems.addItem(new Item(s));
        }
    }

    public void setExternalFileItems(Folder folder) {
        externalFileItems = folder;
    }

    public Folder getExternalFileItems() {
        return externalFileItems;
    }

    public Item[] getExternalFileItemsAsArray() {
        return externalFileItems.getItemsAsArray();
    }

    public Folder getExternalItemFolder() {
        return externalFileItems;
    }

    // Logical Folders
    public Folder getLogicalFolders() {
        return rootFolder;
    }

    public void setLogicalFolders(Folder logicalFolders) {
        this.rootFolder = logicalFolders;
    }

    // Project Files
    public Item[] getProjectItems() {
        Collection<Item> collection = projectItems.values();
        return collection.toArray(new Item[collection.size()]);
    }

    public Item findItemByFile(File file) {
        Collection<Item> coll = projectItems.values();
        Iterator<Item> it = coll.iterator();
        Item canonicalItem = null;
        while (it.hasNext()) {
            Item item = it.next();
            File itemFile = item.getNormalizedFile();
            if (itemFile == file || itemFile.getPath().equals(file.getPath())) {
                return item;
            }
            if (canonicalItem == null) {
                File canonicalItemFile = item.getCanonicalFile();
                if (canonicalItemFile == file || canonicalItemFile.getPath().equals(file.getPath())) {
                    canonicalItem = item;
                }
            }
        }
        return canonicalItem;
    }

    public Item findProjectItemByPath(String path) {
        // Try first as-is
        path = CndPathUtilitities.normalize(path);
        Item item = projectItems.get(path);
        if (item == null) {
            // Then try absolute if relative or relative if absolute
            String newPath;
            if (CndPathUtilitities.isPathAbsolute(path)) {
                newPath = CndPathUtilitities.toRelativePath(getBaseDir(), CndPathUtilitities.naturalize(path));
            } else {
                newPath = CndPathUtilitities.toAbsolutePath(getBaseDir(), path);
            }
            newPath = CndPathUtilitities.normalize(newPath);
            item = projectItems.get(newPath);
        }
        return item;
    }

    public Item findExternalItemByPath(String path) {
        // Try first as-is
        if (externalFileItems == null) {
            return null;
        }
        path = CndPathUtilitities.normalize(path);
        Item item = externalFileItems.findItemByPath(path);
        if (item == null) {
            // Then try absolute if relative or relative if absolute
            String newPath;
            if (CndPathUtilitities.isPathAbsolute(path)) {
                newPath = CndPathUtilitities.toRelativePath(getBaseDir(), CndPathUtilitities.naturalize(path));
            } else {
                newPath = CndPathUtilitities.toAbsolutePath(getBaseDir(), path);
            }
            newPath = CndPathUtilitities.normalize(newPath);
            item = externalFileItems.findItemByPath(newPath);
        }
        return item;
    }

    public Folder findFolderByPath(String path) {
        return getLogicalFolders().findFolderByPath(path);
    }

    public void addProjectItem(Item item) {
        projectItems.put(item.getPath(), item);
        fireProjectItemsChangeEvent(item, ProjectItemChangeEvent.ITEM_ADDED);
        //getNativeProject().fireFileAdded(item);
    }

    public void fireFilesAdded(List<NativeFileItem> fileItems) {
        if (getNativeProject() != null) { // once not null, it never becomes null
            getNativeProject().fireFilesAdded(fileItems);
        }
    }

    public void removeProjectItem(Item item) {
        projectItems.remove(item.getPath());
        fireProjectItemsChangeEvent(item, ProjectItemChangeEvent.ITEM_REMOVED);
        //getNativeProject().fireFileRemoved(item);
    }

    public void fireFilesRemoved(List<NativeFileItem> fileItems) {
        if (getNativeProject() != null) { // once not null, it never becomes null
            getNativeProject().fireFilesRemoved(fileItems);
        }
    }

    public void fireFileRenamed(String oldPath, NativeFileItem newFileItem) {
        if (getNativeProject() != null) { // once not null, it never becomes null
            getNativeProject().fireFileRenamed(oldPath, newFileItem);
        }
    }

    public void checkForChangedItems(Project project, Folder folder, Item item) {
        if (getNativeProject() != null) { // once not null, it never becomes null
            getNativeProject().checkForChangedItems(folder, item);
        }
        MakeLogicalViewProvider.checkForChangedViewItemNodes(project, folder, item);
    }

    @Override
    public void copyFromProjectDescriptor(ConfigurationDescriptor copyProjectDescriptor) {
        MakeConfigurationDescriptor copyExtProjectDescriptor = (MakeConfigurationDescriptor) copyProjectDescriptor;
        setConfs(copyExtProjectDescriptor.getConfs());
        setBaseDir(copyProjectDescriptor.getBaseDir());
        setProjectMakefileName(copyExtProjectDescriptor.getProjectMakefileName());
        setExternalFileItems(copyExtProjectDescriptor.getExternalFileItems());
        setLogicalFolders(copyExtProjectDescriptor.getLogicalFolders());
        setProjectItemsMap(((MakeConfigurationDescriptor) copyProjectDescriptor).getProjectItemsMap());
        setProjectItemsChangeListeners(((MakeConfigurationDescriptor) copyProjectDescriptor).getProjectItemsChangeListeners());
        setSourceRoots(((MakeConfigurationDescriptor) copyProjectDescriptor).getSourceRootsRaw());
    }

    @Override
    public void assign(ConfigurationDescriptor clonedConfigurationDescriptor) {
        Configuration[] clonedConfs = clonedConfigurationDescriptor.getConfs().toArray();
        Configuration[] newConfs = new Configuration[clonedConfs.length];
        setBaseDir(clonedConfigurationDescriptor.getBaseDir());

        for (int i = 0; i < clonedConfs.length; i++) {
            if (clonedConfs[i].getCloneOf() != null) {
                clonedConfs[i].getCloneOf().assign(clonedConfs[i]);
                newConfs[i] = clonedConfs[i].getCloneOf();
            } else {
                newConfs[i] = clonedConfs[i];
            }
        }
        init(newConfs, clonedConfigurationDescriptor.getConfs().getActiveAsIndex());
        setProjectMakefileName(((MakeConfigurationDescriptor) clonedConfigurationDescriptor).getProjectMakefileName());
        setExternalFileItems(((MakeConfigurationDescriptor) clonedConfigurationDescriptor).getExternalFileItems());
        setLogicalFolders(((MakeConfigurationDescriptor) clonedConfigurationDescriptor).getLogicalFolders());
        setProjectItemsMap(((MakeConfigurationDescriptor) clonedConfigurationDescriptor).getProjectItemsMap());
        setProjectItemsChangeListeners(((MakeConfigurationDescriptor) clonedConfigurationDescriptor).getProjectItemsChangeListeners());
        setSourceRoots(((MakeConfigurationDescriptor) clonedConfigurationDescriptor).getSourceRootsRaw());
        setTestRoots(((MakeConfigurationDescriptor) clonedConfigurationDescriptor).getTestRootsRaw());
        setFolderVisibilityQuery(((MakeConfigurationDescriptor) clonedConfigurationDescriptor).getFolderVisibilityQuery().getRegEx());
    }

    @Override
    public ConfigurationDescriptor cloneProjectDescriptor() {
        MakeConfigurationDescriptor clone = new MakeConfigurationDescriptor(getBaseDir());
        super.cloneProjectDescriptor(clone);
        clone.setProjectMakefileName(getProjectMakefileName());
        clone.setExternalFileItems(getExternalFileItems());
        clone.setLogicalFolders(getLogicalFolders());
        clone.setProjectItemsMap(getProjectItemsMap());
        clone.setProjectItemsChangeListeners(getProjectItemsChangeListeners());
        clone.setSourceRoots(getSourceRootsRaw());
        clone.setTestRoots(getTestRootsRaw());
        clone.setFolderVisibilityQuery(getFolderVisibilityQuery().getRegEx());
        return clone;
    }

    @Override
    public boolean getModified() {
        return modified;
    }

    @Override
    public final void setModified() {
        setModified(true);
    }

    public void setModified(boolean modified) {
        //System.out.println("setModified - " + modified);
        this.modified = modified;
        if (modified && getConfs() != null) {
            Configuration[] confs = getConfs().toArray();
            for (int i = 0; i < confs.length; i++) {
                ((MakeConfiguration) confs[i]).setRequiredLanguagesDirty(true);
            }
        }
    }

    public void refreshRequiredLanguages() {
        if (getConfs() != null) {
            Configuration[] confs = getConfs().toArray();
            for (int i = 0; i < confs.length; i++) {
                ((MakeConfiguration) confs[i]).reCountLanguages(this);
            }
        }
    }

    public void saveAndClose() {
        save();
        closed();
    }

    @Override
    public boolean save() {
        return save(NbBundle.getMessage(MakeProject.class, "ProjectNotSaved")); // FIXUP: move message into Bundle for this class after UI freeze
    }

    @Override
    public boolean save(final String extraMessage) {
        SaveRunnable saveRunnable = new SaveRunnable(extraMessage);
        RequestProcessor.Task task = RequestProcessor.getDefault().post(saveRunnable);
        task.waitFinished();
        return saveRunnable.ret;
    }

    /**
     * Check needed header extensions and store list in the NB/project properties.
     * @param needAdd list of needed extensions of header files.
     */
    public boolean addAdditionalHeaderExtensions(Collection<String> needAdd) {
        return ((MakeProject) getProject()).addAdditionalHeaderExtensions(needAdd);
    }

    public CndVisibilityQuery getFolderVisibilityQuery() {
        if (folderVisibilityQuery == null) {
            folderVisibilityQuery = new CndVisibilityQuery(DEFAULT_IGNORE_FOLDERS_PATTERN);
        }
        return folderVisibilityQuery;

    }

    public void setFolderVisibilityQuery(String regex) {
        if (folderVisibilityQuery == null) {
            folderVisibilityQuery = new CndVisibilityQuery(regex);
        } else {
            folderVisibilityQuery.setPattern(regex);
        }
    }

    private class SaveRunnable implements Runnable {

        private boolean ret = false;
        private String extraMessage;

        public SaveRunnable(String extraMessage) {
            this.extraMessage = extraMessage;
        }

        @Override
        public void run() {
            ret = saveWorker(extraMessage);
        }
    }

    private boolean saveWorker(String extraMessage) {

        // Prevent project files corruption.
        if (getState() != State.READY) {
            return false;
        }

        // First check all configurations aux objects if they have changed
        Configuration[] configurations = getConfs().toArray();
        for (int i = 0; i < configurations.length; i++) {
            Configuration conf = configurations[i];
            ConfigurationAuxObject[] auxObjects = conf.getAuxObjects();
            for (int j = 0; j < auxObjects.length; j++) {
                if (auxObjects[j].hasChanged()) {
                    setModified();
                }
                auxObjects[j].clearChanged();
            }
        }

        updateExtensionList();
        if (!getModified()) {
            // Always check for missing or out-of-date makefiles. They may not have been generated or have been removed.
            new ConfigurationMakefileWriter(this).writeMissingMakefiles();
            return true;
        }

        // Check metadata files are writable
        List<String> metadataFiles = new ArrayList<String>();
        List<String> notOkFiles = new ArrayList<String>();
        metadataFiles.add(getBaseDir() + File.separator + MakeConfiguration.NBPROJECT_FOLDER + File.separator + MakeConfiguration.PROJECT_XML); // NOI18N
        metadataFiles.add(getBaseDir() + File.separator + MakeConfiguration.NBPROJECT_FOLDER + File.separator + MakeConfiguration.CONFIGURATIONS_XML); // NOI18N
        metadataFiles.add(getBaseDir() + File.separator + MakeConfiguration.NBPROJECT_FOLDER + File.separator + MakeConfiguration.MAKEFILE_IMPL); // NOI18N
        Configuration[] confs = getConfs().toArray();
        for (int i = 0; i < confs.length; i++) {
            metadataFiles.add(getBaseDir() + File.separator + MakeConfiguration.NBPROJECT_FOLDER + File.separator + "Makefile-" + confs[i].getName() + ".mk"); // NOI18N
        } // NOI18N
        boolean allOk = true;
        for (int i = 0; i < metadataFiles.size(); i++) {
            File file = new File(metadataFiles.get(i));
            if (!file.exists()) {
                continue;
            }
            if (!file.canWrite()) {
                allOk = false;
                notOkFiles.add(metadataFiles.get(i));
            }
        }
        if (!allOk) {
            String projectName = CndPathUtilitities.getBaseName(getBaseDir());
            StringBuilder text = new StringBuilder();
            text.append(getString("CannotSaveTxt", projectName)); // NOI18N
            for (int i = 0; i < notOkFiles.size(); i++) {
                text.append("\n").append(notOkFiles.get(i)); // NOI18N
            }
            if (extraMessage != null) {
                text.append("\n\n").append(extraMessage); // NOI18N
            }
            NotifyDescriptor d = new NotifyDescriptor.Message(text, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return allOk;
        }

        // ALl OK
        FileObject fo = null;
        fo = FileUtil.toFileObject(new File(getBaseDir()));
        if (fo != null) {
            if (MakeProject.TRACE_MAKE_PROJECT_CREATION) {
                System.err.println("Start of writting project descriptor MakeConfigurationDescriptor@" + System.identityHashCode(this) + " for project " + fo.getName() + " @" + System.identityHashCode(this)); // NOI18N
            }
            new ConfigurationXMLWriter(fo, this).write();
            new ConfigurationMakefileWriter(this).write();
            ConfigurationProjectXMLWriter();
            if (MakeProject.TRACE_MAKE_PROJECT_CREATION) {
                System.err.println("End of writting project descriptor MakeConfigurationDescriptor@" + System.identityHashCode(this) + " for project " + fo.getName() + " @" + System.identityHashCode(this)); // NOI18N
            }
        }

        // Clear flag
        setModified(false);

        return allOk;
    }

    private void ConfigurationProjectXMLWriter() {
        // And save the project
        if (getProject() == null) {
            // See http://www.netbeans.org/issues/show_bug.cgi?id=167577
            // This method uses AntProjectHelper and Project but they are not created (correctly?) under junit tests so this will fail.
            // It means make dependen project and encoding is not correctly stored in project.xml when running tests.
            // Fix is to rewrite this method to not use Project and Ant Helper and use DocumentFactory.createInstance().parse instead to open the document.
            return;
        }
        try {
            AntProjectHelper helper = ((MakeProject) getProject()).getAntProjectHelper();
            Element data = helper.getPrimaryConfigurationData(true);
            Document doc = data.getOwnerDocument();

            // Remove old project dependency node
            NodeList nodeList = data.getElementsByTagName(MakeProjectType.MAKE_DEP_PROJECTS);
            if (nodeList != null && nodeList.getLength() > 0) {
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    data.removeChild(node);
                }
            }
            // Create new project dependency node
            Element element = doc.createElementNS(MakeProjectType.PROJECT_CONFIGURATION_NAMESPACE, MakeProjectType.MAKE_DEP_PROJECTS);
            Set<String> subprojectLocations = getSubprojectLocations();
            for (String loc : subprojectLocations) {
                Node n1;
                n1 = doc.createElement(MakeProjectType.MAKE_DEP_PROJECT);
                n1.appendChild(doc.createTextNode(loc));
                element.appendChild(n1);
            }
            data.appendChild(element);
            helper.putPrimaryConfigurationData(data, true);
            // Create source encoding node
            nodeList = data.getElementsByTagName(MakeProjectType.SOURCE_ENCODING_TAG);
            if (nodeList != null && nodeList.getLength() > 0) {
                // Node already there
                Node node = nodeList.item(0);
                node.setTextContent(((MakeProject) getProject()).getSourceEncoding());
            } else {
                // Create node
                Element nativeProjectType = doc.createElementNS(MakeProjectType.PROJECT_CONFIGURATION_NAMESPACE, MakeProjectType.SOURCE_ENCODING_TAG); // NOI18N
                nativeProjectType.appendChild(doc.createTextNode(((MakeProject) getProject()).getSourceEncoding()));
                data.appendChild(nativeProjectType);
            }
            helper.putPrimaryConfigurationData(data, true);

            ProjectManager.getDefault().saveProject(project);
        } catch (IOException ex) {
            Set<Entry<Thread, StackTraceElement[]>> entrySet = Thread.getAllStackTraces().entrySet();
            ex.printStackTrace();
            System.err.println("----- Start thread dump on catching IOException-----"); // NOI18N
            for (Map.Entry<Thread, StackTraceElement[]> entry : entrySet) {
                System.err.println(entry.getKey().getName());
                for (StackTraceElement element : entry.getValue()) {
                    System.err.println("\tat " + element.toString()); // NOI18N
                }
                System.err.println();
            }
            System.err.println("-----End thread dump on catching IOException-----"); // NOI18N
            //ErrorManager.getDefault().notify(ex);
        }
    }

    private void updateExtensionList() {
        Set<String> h = MakeProject.createExtensionSet();
        Set<String> c = MakeProject.createExtensionSet();
        Set<String> cpp = MakeProject.createExtensionSet();
        for (Item item : getProjectItems()) {
            String path = item.getPath();
            String ext = FileUtil.getExtension(path);
            if (ext.length() > 0) {
                if (!h.contains(ext) && !c.contains(ext) && !cpp.contains(ext)) {
                    if (MIMEExtensions.isRegistered(MIMENames.HEADER_MIME_TYPE, ext)) {
                        h.add(ext);
                    } else if (MIMEExtensions.isRegistered(MIMENames.C_MIME_TYPE, ext)) {
                        c.add(ext);
                    } else if (MIMEExtensions.isRegistered(MIMENames.CPLUSPLUS_MIME_TYPE, ext)) {
                        cpp.add(ext);
                    }
                }
            }
        }
        MakeProject makeProject = (MakeProject) getProject();
        if (makeProject != null) {
            makeProject.updateExtensions(c, cpp, h);
        }
    }

    /**
     * Returns project locations (rel or abs) or all subprojects in all configurations.
     */
    public Set<String> getSubprojectLocations() {
        Set<String> subProjects = new HashSet<String>();

        Configuration[] confs = getConfs().toArray();
        for (int i = 0; i < confs.length; i++) {
            MakeConfiguration makeConfiguration = (MakeConfiguration) confs[i];
            LibrariesConfiguration librariesConfiguration = null;

            if (((MakeConfiguration) confs[i]).isLinkerConfiguration()) {
                librariesConfiguration = makeConfiguration.getLinkerConfiguration().getLibrariesConfiguration();
                for (LibraryItem item : librariesConfiguration.getValue()) {
                    if (item instanceof LibraryItem.ProjectItem) {
                        LibraryItem.ProjectItem projectItem = (LibraryItem.ProjectItem) item;
                        subProjects.add(projectItem.getMakeArtifact().getProjectLocation());
                    }
                }
            }

            for (LibraryItem.ProjectItem item : makeConfiguration.getRequiredProjectsConfiguration().getValue()) {
                subProjects.add(item.getMakeArtifact().getProjectLocation());
            }
        }

        return subProjects;
    }

    public void addSourceRootRaw(String path) {
        synchronized (sourceRoots) {
            sourceRoots.add(path);
        }
    }

    public void addTestRootRaw(String path) {
        synchronized (testRoots) {
            testRoots.add(path);
        }
    }

    private void addTestRoot(String path) {
        String absPath = CndPathUtilitities.toAbsolutePath(getBaseDir(), path);
        String relPath = CndPathUtilitities.normalize(CndPathUtilitities.toRelativePath(getBaseDir(), path));
        boolean addPath = true;

        //if (CndPathUtilitities.isPathAbsolute(relPath) || relPath.startsWith("..") || relPath.startsWith(".")) { // NOI18N
        synchronized (testRoots) {
            if (addPath) {
                String usePath;
                if (MakeProjectOptions.getPathMode() == MakeProjectOptions.REL_OR_ABS) {
                    usePath = CndPathUtilitities.normalize(CndPathUtilitities.toAbsoluteOrRelativePath(getBaseDir(), path));
                } else if (MakeProjectOptions.getPathMode() == MakeProjectOptions.REL) {
                    usePath = relPath;
                } else {
                    usePath = absPath;
                }

                testRoots.add(usePath);
                setModified();
            }
        }
    }

    /*
     * Add a source new root.
     * Don't add if root inside project
     * Don't add if root is subdir of existing root
     */
    private void addSourceRoot(String path) {
        String absPath = CndPathUtilitities.toAbsolutePath(getBaseDir(), path);
        String canonicalPath = null;
        try {
            canonicalPath = new File(absPath).getCanonicalPath();
        } catch (IOException ioe) {
            canonicalPath = null;
        }
        String relPath = CndPathUtilitities.normalize(CndPathUtilitities.toRelativePath(getBaseDir(), path));
        boolean addPath = true;
        ArrayList<String> toBeRemoved = new ArrayList<String>();

        //if (CndPathUtilitities.isPathAbsolute(relPath) || relPath.startsWith("..") || relPath.startsWith(".")) { // NOI18N
        synchronized (sourceRoots) {
            if (canonicalPath != null) {
                int canonicalPathLength = canonicalPath.length();
                for (String sourceRoot : sourceRoots) {
                    String absSourceRoot = CndPathUtilitities.toAbsolutePath(getBaseDir(), sourceRoot);
                    String canonicalSourceRoot = null;
                    try {
                        canonicalSourceRoot = new File(absSourceRoot).getCanonicalPath();
                    } catch (IOException ioe) {
                        canonicalSourceRoot = null;
                    }
                    if (canonicalSourceRoot != null) {
                        int canonicalSourceRootLength = canonicalSourceRoot.length();
                        if (canonicalSourceRoot.equals(canonicalPath)) {
                            // Identical - don't add
                            addPath = false;
                            break;
                        }
                        if (canonicalSourceRoot.startsWith(canonicalPath) && canonicalSourceRoot.charAt(canonicalPathLength) == File.separatorChar) {
                            // Existing root sub dir of new path - remove existing path
                            toBeRemoved.add(sourceRoot);
                            continue;
                        }
                        if (canonicalPath.startsWith(canonicalSourceRoot) && canonicalPath.charAt(canonicalSourceRootLength) == File.separatorChar) {
                            // Sub dir of existing root - don't add
                            addPath = false;
                            break;
                        }
                    }
                }
            }
            if (toBeRemoved.size() > 0) {
                for (String toRemove : toBeRemoved) {
                    sourceRoots.remove(toRemove);
                }
            }
            if (addPath) {
                String usePath;
                if (MakeProjectOptions.getPathMode() == MakeProjectOptions.REL_OR_ABS) {
                    usePath = CndPathUtilitities.normalize(CndPathUtilitities.toAbsoluteOrRelativePath(getBaseDir(), path));
                } else if (MakeProjectOptions.getPathMode() == MakeProjectOptions.REL) {
                    usePath = relPath;
                } else {
                    usePath = absPath;
                }

                sourceRoots.add(usePath);
                setModified();
            }
        }
        //}
    }

    /*
     * Return real list
     */
    private List<String> getSourceRootsRaw() {
        return sourceRoots;
    }

    private List<String> getTestRootsRaw() {
        return testRoots;
    }

    public void setSourceRoots(List<String> list) {
        synchronized (sourceRoots) {
            sourceRoots.clear();
            sourceRoots.addAll(list);
        }
    }

    public void setTestRoots(List<String> list) {
        synchronized (testRoots) {
            testRoots.clear();
            testRoots.addAll(list);
        }
    }

//    public void setSourceRootsList(List<String> list) {
//        synchronized (sourceRoots) {
//            sourceRoots.clear();
//            for (String l : list) {
//                addSourceRoot(l);
//            }
//        }
//        MakeSources makeSources = getProject().getLookup().lookup(MakeSources.class);
//        if (makeSources != null) {
//            makeSources.sourceRootsChanged();
//        }
//    }
    private boolean inList(List<String> list, String s) {
        for (String l : list) {
            if (l.equals(s)) {
                return true;
            }
        }
        return false;
    }

    public void checkForChangedSourceRoots(List<String> oldList, List<String> newList) {
        synchronized (sourceRoots) {
            sourceRoots.clear();
            for (String l : newList) {
                addSourceRoot(l);
            }

            MakeConfiguration active = (MakeConfiguration) getConfs().getActive(); // FIXUP: need better check
            if (!active.isMakefileConfiguration()) {
                MakeSources makeSources = getProject().getLookup().lookup(MakeSources.class);
                if (makeSources != null) {
                    makeSources.sourceRootsChanged();
                }
                return;
            }

            List<String> toBeAdded = new ArrayList<String>();
            for (String s : sourceRoots) {
                if (!inList(oldList, s)) {
                    toBeAdded.add(s);
                }
            }
            List<String> toBeRemoved = new ArrayList<String>();
            for (String s : oldList) {
                if (!inList(sourceRoots, s)) {
                    toBeRemoved.add(s);
                }
            }

            // Add new source root folders
            if (toBeAdded.size() > 0) {
                for (String root : toBeAdded) {
                    String absSourceRoot = CndPathUtilitities.toAbsolutePath(getBaseDir(), root);
                    File absSourceRootFile = new File(absSourceRoot);
                    addFilesFromRoot(getLogicalFolders(), absSourceRootFile, true, true, null);
                }
                setModified();
            }

            // Remove old source root folders
            if (toBeRemoved.size() > 0) {
                for (String rootToBeRemoved : toBeRemoved) {
                    List<Folder> rootFolders = getLogicalFolders().getAllFolders(modified); // FIXUP: should probably alays be 'true'
                    for (Folder root : rootFolders) {
                        if (root.getRoot() != null && root.getRoot().equals(rootToBeRemoved)) {
                            getLogicalFolders().removeFolderAction(root);
                        }
                    }
                }
                setModified();
            }

            // Notify source root notifiers
            if (toBeAdded.size() > 0 || toBeRemoved.size() > 0) {
                MakeSources makeSources = getProject().getLookup().lookup(MakeSources.class);
                if (makeSources != null) {
                    makeSources.sourceRootsChanged();
                }
            }
        }
    }

    public void checkForChangedTestRoots(List<String> oldList, List<String> newList) {
        synchronized (testRoots) {
            testRoots.clear();
            for (String l : newList) {
                addTestRoot(l);
            }

            List<String> toBeAdded = new ArrayList<String>();
            for (String s : testRoots) {
                if (!inList(oldList, s)) {
                    toBeAdded.add(s);
                }
            }
            List<String> toBeRemoved = new ArrayList<String>();
            for (String s : oldList) {
                if (!inList(testRoots, s)) {
                    toBeRemoved.add(s);
                }
            }

            // Notify source root notifiers
            // FIXUP: hack to get the tree updated! Need to only refresh actual nodes.
            if (toBeAdded.size() > 0 || toBeRemoved.size() > 0) {
                List<String> sourceRootsSave = new ArrayList<String>();
                sourceRootsSave.addAll(sourceRoots);
                checkForChangedSourceRoots(sourceRootsSave, new ArrayList<String>());
                checkForChangedSourceRoots(new ArrayList<String>(), sourceRootsSave);
                setModified();
            }
        }
    }

    public void checkConfigurations(Configuration oldActive, Configuration newActive) {
        getConfs().fireChangedActiveConfiguration(oldActive, newActive);
    }

    /*
     * return copy
     */
    public List<String> getSourceRoots() {
        List<String> copy;
        synchronized (sourceRoots) {
            copy = new ArrayList<String>(sourceRoots);
        }
        return copy;
    }

    /*
     * return copy
     */
    public List<String> getTestRoots() {
        List<String> copy;
        synchronized (testRoots) {
            copy = new ArrayList<String>(testRoots);
        }
        return copy;
    }

    /*
     * return copy and convert to absolute
     */
    public List<String> getAbsoluteSourceRoots() {
        List<String> copy = new ArrayList<String>();
        synchronized (sourceRoots) {
            for (String sr : sourceRoots) {
                copy.add(CndPathUtilitities.toAbsolutePath(baseDir, sr));
            }
        }
        return copy;
    }

    /*
     * return copy and convert to absolute
     */
    public List<String> getAbsoluteTestRoots() {
        List<String> copy = new ArrayList<String>();
        synchronized (testRoots) {
            for (String s : testRoots) {
                copy.add(CndPathUtilitities.toAbsolutePath(baseDir, s));
            }
        }
        return copy;
    }

    private NativeProjectProvider getNativeProject() {
        // the cons
        if (nativeProject == null) {
            FileObject fo = FileUtil.toFileObject(new File(baseDir));
            try {
                Project aProject = ProjectManager.getDefault().findProject(fo);
                nativeProject = aProject.getLookup().lookup(NativeProject.class);
            } catch (Exception e) {
                // This may be ok. The project could have been removed ....
                System.err.println("getNativeProject " + e);
            }

        }
        return (NativeProjectProvider) nativeProject;
    }

    public static class ProjectItemChangeEvent extends ChangeEvent {

        public static final int ITEM_ADDED = 0;
        public static final int ITEM_REMOVED = 1;
        private Item item;
        private int action;

        public ProjectItemChangeEvent(Object src, Item item, int action) {
            super(src);
            this.item = item;
            this.action = action;
        }

        public Item getItem() {
            return item;
        }

        public int getAction() {
            return action;
        }
    }

    public Folder addFilesFromRoot(Folder folder, File dir, boolean attachListeners, boolean asDiskFolder, @NullAllowed FileFilter fileFilter) {
        ArrayList<NativeFileItem> filesAdded = new ArrayList<NativeFileItem>();
        Folder top;
        top = folder.findFolderByName(dir.getName());
        if (top == null) {
            top = new Folder(folder.getConfigurationDescriptor(), folder, dir.getName(), dir.getName(), true);
            folder.addFolder(top, true);
        }
        if (asDiskFolder) {
            String rootPath;
            if (MakeProjectOptions.getPathMode() == MakeProjectOptions.REL_OR_ABS) {
                rootPath = CndPathUtilitities.toAbsoluteOrRelativePath(baseDir, dir.getPath());
            } else if (MakeProjectOptions.getPathMode() == MakeProjectOptions.REL) {
                rootPath = CndPathUtilitities.toRelativePath(baseDir, dir.getPath());
            } else {
                rootPath = CndPathUtilitities.toAbsolutePath(baseDir, dir.getPath());
            }
            rootPath = CndPathUtilitities.normalize(rootPath);
            top.setRoot(rootPath);
        }
        addFiles(top, dir, null, filesAdded, true, true, fileFilter);
        if (getNativeProject() != null) { // once not null, it never becomes null
            getNativeProject().fireFilesAdded(filesAdded);
        }
        if (attachListeners) {
            top.attachListeners();
        }

        addSourceRoot(dir.getPath());
        MakeSources makeSources = getProject().getLookup().lookup(MakeSources.class);
        if (makeSources != null) {
            makeSources.sourceRootsChanged();
        }

        return top;
    }

    public Folder addFilesFromDir(Folder folder, File dir, boolean attachListeners, boolean setModified, @NullAllowed FileFilter fileFilter) {
        ArrayList<NativeFileItem> filesAdded = new ArrayList<NativeFileItem>();
        Folder top = new Folder(folder.getConfigurationDescriptor(), folder, dir.getName(), dir.getName(), true);
        folder.addFolder(top, setModified);
        addFiles(top, dir, null, filesAdded, true, setModified, fileFilter);
        if (getNativeProject() != null) { // once not null, it never becomes null
            getNativeProject().fireFilesAdded(filesAdded);
        }
        if (attachListeners) {
            top.attachListeners();
        }
        return top;
    }

    private void addFiles(Folder folder, File dir, ProgressHandle handle, ArrayList<NativeFileItem> filesAdded, boolean notify, boolean setModified, final @NullAllowed FileFilter fileFilter) {
        List<String> absTestRootsList = getAbsoluteTestRoots();
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        final boolean hideBinaryFiles = !MakeOptions.getInstance().getViewBinaryFiles();
        for (File file : files) {
            if (!VisibilityQuery.getDefault().isVisible(file)) {
                continue;
            }
            if (fileFilter != null && !fileFilter.accept(file)) {
                continue;
            }
            if (hideBinaryFiles && CndFileVisibilityQuery.getDefault().isIgnored(file)) {
                continue;
            }
            if (file.isDirectory() && getFolderVisibilityQuery().isVisible(file)) {
                continue;
            }
            if (file.isDirectory()) {
                Folder dirfolder = folder;
                dirfolder = folder.findFolderByName(file.getName());
                if (dirfolder == null) {
                    if (inList(absTestRootsList, file.getAbsolutePath()) || folder.isTestLogicalFolder()) {
                        dirfolder = folder.addNewFolder(file.getName(), file.getName(), true, Folder.Kind.TEST_LOGICAL_FOLDER);
                    } else {
                        dirfolder = folder.addNewFolder(file.getName(), file.getName(), true, Folder.Kind.SOURCE_LOGICAL_FOLDER);
                    }
                }
                addFiles(dirfolder, file, handle, filesAdded, notify, setModified, fileFilter);
            } else {
                String filePath;
                if (MakeProjectOptions.getPathMode() == MakeProjectOptions.REL_OR_ABS) {
                    filePath = CndPathUtilitities.toAbsoluteOrRelativePath(baseDir, file.getPath());
                } else if (MakeProjectOptions.getPathMode() == MakeProjectOptions.REL) {
                    filePath = CndPathUtilitities.toRelativePath(baseDir, file.getPath());
                } else {
                    filePath = CndPathUtilitities.toAbsolutePath(baseDir, file.getPath());
                }
                Item item = new Item(CndPathUtilitities.normalize(filePath));
                if (folder.addItem(item, notify, setModified) != null) {
                    filesAdded.add(item);
                }
                if (handle != null) {
                    handle.progress(filePath);
                }
            }
        }
    }

    public boolean okToChange() {
        int previousVersion = getVersion();
        int currentVersion = CommonConfigurationXMLCodec.CURRENT_VERSION;
        if (previousVersion < currentVersion) {
            String txt = getString("UPGRADE_TXT");
            NotifyDescriptor d = new NotifyDescriptor.Confirmation(txt, getString("UPGRADE_DIALOG_TITLE"), NotifyDescriptor.YES_NO_OPTION); // NOI18N
            if (DialogDisplayer.getDefault().notify(d) != NotifyDescriptor.YES_OPTION) {
                return false;
            }
            setVersion(currentVersion);
        }
        return true;
    }

    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(MakeConfigurationDescriptor.class, s);
    }

    private static String getString(String s, String a1) {
        return NbBundle.getMessage(MakeConfigurationDescriptor.class, s, a1);
    }
}
