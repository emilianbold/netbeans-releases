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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItemSet;
import org.netbeans.modules.cnd.api.utils.CndFileVisibilityQuery;
import org.netbeans.modules.cnd.utils.FileFilterFactory;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.WeakSet;

public class Folder implements FileChangeListener, ChangeListener {

    public enum Kind {SOURCE_LOGICAL_FOLDER, SOURCE_DISK_FOLDER, IMPORTANT_FILES_FOLDER, TEST_LOGICAL_FOLDER, TEST};

    public static final String DEFAULT_FOLDER_NAME = "f"; // NOI18N
    public static final String DEFAULT_FOLDER_DISPLAY_NAME = getString("NewFolderName");
    public static final String DEFAULT_TEST_FOLDER_DISPLAY_NAME = getString("NewTestFolderName");
    private MakeConfigurationDescriptor configurationDescriptor;
    private final String name;
    private String displayName;
    private final Folder parent;
    private ArrayList<Object> items = null; // Folder or Item
    private HashMap<String,HashMap<Configuration,DeletedConfiguration>> deletedItems;
    private final Set<ChangeListener> changeListenerList = new WeakSet<ChangeListener>(1);
    private final boolean projectFiles;
    private String id = null;
    private String root;
    private final static Logger log = Logger.getLogger("makeproject.folder"); // NOI18N
    private static boolean checkedLogging = checkLogging();
    private Kind kind;

    public Folder(MakeConfigurationDescriptor configurationDescriptor, Folder parent, String name, String displayName, boolean projectFiles) {
        this.configurationDescriptor = configurationDescriptor;
        this.parent = parent;
        this.name = name;
        this.displayName = displayName;
        this.projectFiles = projectFiles;
        this.items = new ArrayList<Object>();
    }

    /**
     * For internal purpose.
     * Method reduce folder items size
     */
    public void pack() {
        items.trimToSize();
    }

    private void setKind(Kind kind) {
        this.kind = kind;
    }

    public Kind getKind() {
        return kind;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public String getRoot() {
        return root;
    }

    public void refreshDiskFolder(boolean setModified) {
        if (log.isLoggable(Level.FINER)) {
            log.log(Level.FINER, "----------refreshDiskFolder {0}", getPath()); // NOI18N
        }
        String rootPath = getRootPath();
        String AbsRootPath = CndPathUtilitities.toAbsolutePath(configurationDescriptor.getBaseDir(), rootPath);

        File folderFile = new File(AbsRootPath);

        // Folders to be removed
        if (!folderFile.exists() ||
            !folderFile.isDirectory() ||
            !VisibilityQuery.getDefault().isVisible(folderFile) ||
            getConfigurationDescriptor().getFolderVisibilityQuery().isVisible(folderFile)) {
            // Remove it plus all subfolders and items from project
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "------------removing folder {0} in {1}", new Object[]{getPath(), getParent().getPath()}); // NOI18N
            }
            getParent().removeFolder(this, setModified);
            return;
        }
        // Items to be removed
        for (Item item : getItemsAsArray()) {
            File file = item.getFile();
            if (!file.exists() ||
                !file.isFile() ||
                !VisibilityQuery.getDefault().isVisible(file) ||
                !CndFileVisibilityQuery.getDefault().isVisible(file)) {
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE, "------------removing item {0} in {1}", new Object[]{item.getPath(), getPath()}); // NOI18N
                }
                removeItem(item, setModified);
            }
        }
        // files/folders to be added
        File files[] = folderFile.listFiles();
        if (files == null) {
            return;
        }
        List<File> fileList = new ArrayList<File>();
        for (int i = 0; i < files.length; i++) {
            if (!VisibilityQuery.getDefault().isVisible(files[i])) {
                continue;
            }
            if (files[i].isFile() && !CndFileVisibilityQuery.getDefault().isVisible(files[i])) {
                continue;
            }
            if (files[i].isDirectory() && getConfigurationDescriptor().getFolderVisibilityQuery().isVisible(files[i])) {
                continue;
            }
            fileList.add(files[i]);
        }
        for (File file : fileList) {
            if (file.isDirectory()) {
                if (findFolderByName(file.getName()) == null) {
                    if (log.isLoggable(Level.FINE)) {
                        log.log(Level.FINE, "------------adding folder {0} in {1}", new Object[]{file.getPath(), getPath()}); // NOI18N
                    }
                    getConfigurationDescriptor().addFilesFromDir(this, file, true, setModified, null);

                }
            } else {
                String path = getRootPath() + '/' + file.getName();
                if (path.startsWith("./")) { // NOI18N
                    path = path.substring(2);
                }
                if (findItemByPath(path) == null) {
                    if (log.isLoggable(Level.FINE)) {
                        log.log(Level.FINE, "------------adding item {0} in {1}", new Object[]{file.getPath(), getPath()}); // NOI18N
                    }
                    addItem(new Item(path), true, setModified);
                }
            }
        }

        // Repeast for all sub folders
        List<Folder> subFolders = getFolders();
        for (Folder f : subFolders) {
            f.refreshDiskFolder(setModified);
        }
    }

    public void attachListeners() {
        String rootPath = getRootPath();
        String AbsRootPath = CndPathUtilitities.toAbsolutePath(configurationDescriptor.getBaseDir(), rootPath);
        File folderFile = new File(AbsRootPath);

        if (!folderFile.exists() || !folderFile.isDirectory()) {
            return;
        }

        if (isDiskFolder() && getRoot() != null) {
            VisibilityQuery.getDefault().addChangeListener(this);
            CndFileVisibilityQuery.getDefault().addChangeListener(this);
            getConfigurationDescriptor().getFolderVisibilityQuery().addChangeListener(this);
            if (log.isLoggable(Level.FINER)) {
                log.log(Level.FINER, "-----------attachFilterListener {0}", getPath()); // NOI18N
            }
        }

        try {
            FileUtil.addFileChangeListener(this, folderFile);
            if (log.isLoggable(Level.FINER)) {
                log.log(Level.FINER, "-----------attachFileChangeListener {0}", getPath()); // NOI18N
            }
        } catch (IllegalArgumentException iae) {
            // Can happen if trying to attach twice...
            if (log.isLoggable(Level.FINER)) {
                log.log(Level.FINER, "-----------attachFileChangeListener duplicate error{0}", getPath()); // NOI18N
            }
        }

        // Repeast for all sub folders
        List<Folder> subFolders = getFolders();
        for (Folder f : subFolders) {
            f.attachListeners();
        }
    }

    public void detachListener() {
        if (log.isLoggable(Level.FINER)) {
           log.log(Level.FINER, "-----------detachFileChangeListener {0}", getPath()); // NOI18N
        }
        FileUtil.removeFileChangeListener(this);
        if (isDiskFolder() && getRoot() != null) {
            VisibilityQuery.getDefault().removeChangeListener(this);
            CndFileVisibilityQuery.getDefault().removeChangeListener(this);
            getConfigurationDescriptor().getFolderVisibilityQuery().removeChangeListener(this);
            if (log.isLoggable(Level.FINER)) {
                log.log(Level.FINER, "-----------detachFilterListener {0}", getPath()); // NOI18N
            }
        }

    }

    public Folder getParent() {
        return parent;
    }

    public Project getProject() {
        return getConfigurationDescriptor().getProject();
    }

    public String getName() {
        return name;
    }

    private String getSortName() {
        return displayName;
    }

    public String getPath() {
        StringBuilder builder2 = new StringBuilder(32);
        reversePath(this, builder2, false);
        return builder2.toString();
    }

    public String getRootPath() {
        StringBuilder builder2 = new StringBuilder(32);
        reversePath(this, builder2, true);
        String path = builder2.toString();
        return path;
    }

    private void reversePath(Folder folder, StringBuilder builder, boolean fromRoot) {
        Folder aParent = folder.getParent();
        if (aParent != null && aParent.getParent() != null) {
            reversePath(aParent, builder, fromRoot);
            builder.append('/'); // NOI18N
        }
        if (fromRoot && folder.getRoot() != null) {
            builder.append(folder.getRoot());
        } else {
            builder.append(folder.getName());
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        configurationDescriptor.setModified();
        getParent().reInsertElement(this);
    }

    public MakeConfigurationDescriptor getConfigurationDescriptor() {
        return configurationDescriptor;
    }

    public void setConfigurationDescriptor(MakeConfigurationDescriptor configurationDescriptor) {
        this.configurationDescriptor = configurationDescriptor;
    }

    public boolean isProjectFiles() {
        return projectFiles;
    }

    public boolean isDiskFolder() {
        Folder f = this;
        while (true) {
            if (f.getRoot() != null) {
                return true;
            }
            f = f.getParent();
            if (f == null) {
                break;
            }
        }
        return false;
    }

    public boolean isTestLogicalFolder() {
        return getKind() == Kind.TEST_LOGICAL_FOLDER;
    }

    public boolean isTestRootFolder() {
        return isTestLogicalFolder() && getName().equals(MakeConfigurationDescriptor.TEST_FILES_FOLDER);
    }

    public boolean isTest() {
        return getKind() == Kind.TEST;
    }

    public ArrayList<Object> getElements() {
        return items;
    }

    private void reInsertElement(Object element) {
        int index = items.indexOf(element);
        if (index < 0) {
            return;
        }
        items.remove(element);
        if (element instanceof Folder) {
            insertFolderElement((Folder) element);
        } else if (element instanceof Item) {
            insertItemElement((Item) element);
        } else {
            assert false;
        }
        fireChangeEvent();
    }

    private void insertFolderElement(Folder element) {
        if (!element.isProjectFiles()) {
            // Insert last
            items.add(element);
            return;
        }
        String name1 = element.getSortName();
        int indexAt = items.size() - 1;
        while (indexAt >= 0) {
            Object o = items.get(indexAt);
            if (!(o instanceof Folder)) {
                indexAt--;
                continue;
            }
            if (!((Folder) o).isProjectFiles()) {
                indexAt--;
                continue;
            }
            String name2 = ((Folder) o).getSortName();
            int compareRes = name1.compareToIgnoreCase(name2);
            if (compareRes < 0) {
                indexAt--;
                continue;
            }
            break;
        }
        items.add(indexAt + 1, element);
    }

    public static void insertItemElementInList(ArrayList<Object> list, Item element) {
        String name1 = (element).getSortName();
        int indexAt = list.size() - 1;
        while (indexAt >= 0) {
            Object o = list.get(indexAt);
            if (!(o instanceof Item)) {
                //indexAt--;
                break;
            }
            String name2 = ((Item) o).getSortName();
            int compareRes = name1.compareTo(name2);
            if (compareRes < 0) {
                indexAt--;
                continue;
            }
            break;
        }
        list.add(indexAt + 1, element);
    }

    private void insertItemElement(Item element) {
        insertItemElementInList(items, element);
    }

    private void addElement(Object element, boolean setModified) { // FIXUP: shopuld be private
        // Always keep the vector sorted
        if (element instanceof Item) {
            insertItemElement((Item) element);
        } else if (element instanceof Folder) {
            insertFolderElement((Folder) element);
        } else {
            assert false;
        }
        fireChangeEvent(this, setModified);
    }

    public Item addItemAction(Item item) {
        return addItemAction(item, true);
    }
    
    public Item addItemAction(Item item, boolean setModified) {
        if (addItem(item, true, setModified) == null) {
            return null; // Nothing added
        }
        ArrayList<NativeFileItem> list = new ArrayList<NativeFileItem>(1);
        list.add(item);
        configurationDescriptor.fireFilesAdded(list);
        return item;
    }

    public Item addItem(Item item) {
        return addItem(item, true);
    }

    public Item addItem(Item item, boolean notify) {
        return addItem(item, notify, true);
    }

    public Item addItem(Item item, boolean notify, boolean setModified) {
        if (item == null) {
            return null;
        }
        // Check if already in project. Refresh if it's there.
        Item existingItem;
        if (isProjectFiles() && (existingItem = configurationDescriptor.findProjectItemByPath(item.getPath())) != null) {
            //System.err.println("Folder - addItem - item ignored, already added: " + item); // NOI18N  // FIXUP: correct?
            fireChangeEvent(existingItem, setModified);
            return null; // Nothing added
        }
        // Add it to the folder
        item.setFolder(this);
        addElement(item, setModified);

        // Add item to the dataObject's lookup
        if (isProjectFiles() && notify) {
            DataObject dao = item.getDataObject();
            NativeFileItemSet myNativeFileItemSet = (dao == null) ? null : dao.getCookie(NativeFileItemSet.class);
            if (myNativeFileItemSet != null) {
                myNativeFileItemSet.add(item);
            } else {
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE, "can not add folder\'s {0} item {1} using {2}", new Object[]{this, item, dao}); // NOI18N
                }
            }
        }

        // Add it to project Items
        if (isProjectFiles()) {
            configurationDescriptor.addProjectItem(item);
            if (setModified) {
                configurationDescriptor.setModified();
            }

            // Add configuration to all configurations
            if (configurationDescriptor.getConfs() == null) {
                return item;
            }
            HashMap<Configuration, DeletedConfiguration> map = null;
            if (deletedItems != null) {
                map = deletedItems.get(item.getPath());
            }
            Configuration[] configurations = configurationDescriptor.getConfs().toArray();
            for (int i = 0; i < configurations.length; i++) {
                FolderConfiguration folderConfiguration = getFolderConfiguration(configurations[i]);
                DeletedConfiguration old = null;
                if (map != null) {
                    old = map.get(configurations[i]);
                }
                ItemConfiguration ic = new ItemConfiguration(configurations[i], item);
                if (old != null) {
                    ic.setTool(old.ic.getTool());
                    ic.assignValues(old.aux);
                } else {
                    ic = new ItemConfiguration(configurations[i], item);
                }
                configurations[i].addAuxObject(ic);
            }
            if (map != null && deletedItems != null) {
                deletedItems.remove(item.getPath());
            }
        }

        return item;
    }

    public void addFolder(Folder folder, boolean setModified) {
        addElement(folder, setModified);
        if (isProjectFiles()) {
            // Add configuration to all configurations
            if (configurationDescriptor.getConfs() == null) {
                return;
            }
            Configuration[] configurations = configurationDescriptor.getConfs().toArray();
            for (int i = 0; i < configurations.length; i++) {
                folder.getFolderConfiguration(configurations[i]);
            }
        }
    }

    /**
     * Returns an unique id (String) used to retrive this object from the
     * pool of aux objects
     */
    public String getId() {
        if (id == null) {
            id = "f-" + getPath(); // NOI18N
        }
        return id;
    }

    public FolderConfiguration getFolderConfiguration(Configuration configuration) {
        FolderConfiguration folderConfiguration = null;
        if (isProjectFiles() || isTest() || isTestLogicalFolder()) {
            folderConfiguration = (FolderConfiguration) configuration.getAuxObject(getId());
            if (folderConfiguration == null) {
                CCompilerConfiguration parentCCompilerConfiguration;
                CCCompilerConfiguration parentCCCompilerConfiguration;
                FolderConfiguration parentFolderConfiguration = null;
                if (getParent() != null) {
                    parentFolderConfiguration = getParent().getFolderConfiguration(configuration);
                }
                if (parentFolderConfiguration != null) {
                    parentCCompilerConfiguration = parentFolderConfiguration.getCCompilerConfiguration();
                    parentCCCompilerConfiguration = parentFolderConfiguration.getCCCompilerConfiguration();
                } else {
                    parentCCompilerConfiguration = ((MakeConfiguration) configuration).getCCompilerConfiguration();
                    parentCCCompilerConfiguration = ((MakeConfiguration) configuration).getCCCompilerConfiguration();
                }
                folderConfiguration = new FolderConfiguration(configuration, parentCCompilerConfiguration, parentCCCompilerConfiguration, this);
                configuration.addAuxObject(folderConfiguration);
            }
        }
        return folderConfiguration;
    }

    public String suggestedNewTestFolderName() {
        return suggestedName(DEFAULT_TEST_FOLDER_DISPLAY_NAME);
    }

    public String suggestedNewFolderName() {
        return suggestedName(DEFAULT_FOLDER_DISPLAY_NAME);
    }
    
    public String suggestedName(String template) {
        String aNname;
        String aDisplayName;
        for (int i = 1;; i++) {
            aNname = DEFAULT_FOLDER_NAME + i;
            aDisplayName = template + " " + i; // NOI18N
            if (findFolderByName(aNname) == null) {
                break;
            }
        }
        return aDisplayName;
    }

    public Folder addNewFolder(boolean projectFiles) {
        return addNewFolder(projectFiles, getKind());
    }

    public Folder addNewFolder(boolean projectFiles, Kind kind) {
        String aNname;
        String aDisplayName;
        for (int i = 1;; i++) {
            aNname = DEFAULT_FOLDER_NAME + i;
            aDisplayName = DEFAULT_FOLDER_DISPLAY_NAME + " " + i; // NOI18N
            if (findFolderByName(aNname) == null) {
                break;
            }
        }
        return addNewFolder(aNname, aDisplayName, projectFiles, kind); // NOI18N
    }

    public Folder addNewFolder(String name, String displayName, boolean projectFiles, String kindText) {
        Kind k = null;
        if (kindText != null) {
            if (kindText.equals("IMPORTANT_FILES_FOLDER")) { // NOI18N
                k = Kind.IMPORTANT_FILES_FOLDER;
            }
            else if (kindText.equals("SOURCE_DISK_FOLDER")) { // NOI18N
                k = Kind.SOURCE_DISK_FOLDER;
            }
            else if (kindText.equals("SOURCE_LOGICAL_FOLDER")) { // NOI18N
                k = Kind.SOURCE_LOGICAL_FOLDER;
            }
            else if (kindText.equals("TEST")) { // NOI18N
                k = Kind.TEST;
            }
            else if (kindText.equals("TEST_LOGICAL_FOLDER")) { // NOI18N
                k = Kind.TEST_LOGICAL_FOLDER;
            }
        }
        return addNewFolder(name, displayName, projectFiles, k);
    }

    public Folder addNewFolder(String name, String displayName, boolean projectFiles, Kind kind) {
        Folder newFolder = new Folder(getConfigurationDescriptor(), this, name, displayName, projectFiles);
        addFolder(newFolder, true);
        newFolder.setKind(kind);
        return newFolder;
    }

    public boolean removeItemAction(Item item) {
        return removeItemAction(item, true);
    }

    public boolean removeItemAction(Item item, boolean setModified) {
        ArrayList<NativeFileItem> list = new ArrayList<NativeFileItem>(1);
        list.add(item);
        if (isProjectFiles()) {
            configurationDescriptor.fireFilesRemoved(list);
        }
        return removeItem(item, setModified);
    }

    public void renameItemAction(String oldPath, Item newItem) {
        configurationDescriptor.fireFileRenamed(oldPath, newItem);
    }

    public boolean removeItem(Item item) {
        return removeItem(item, true);
    }

    private boolean removeItem(Item item, boolean setModified) {
        boolean ret = false;
        if (item == null) {
            return false;
        }
        // Remove it from folder
        ret = items.remove(item);
        if (!ret) {
            return ret;
        }

        // Remove item from the dataObject's lookup
        if (isProjectFiles()) {
            DataObject dataObject = item.getDataObject();
            if (dataObject != null) {
                NativeFileItemSet myNativeFileItemSet = dataObject.getCookie(NativeFileItemSet.class);
                if (myNativeFileItemSet != null) {
                    myNativeFileItemSet.remove(item);
                }
            }
        }

//	item.setFolder(null);
        if (isProjectFiles()) {
            // Remove it from project Items
            configurationDescriptor.removeProjectItem(item);
            if (setModified) {
                configurationDescriptor.setModified();
            }

            // Remove it form all configurations
            if (deletedItems == null) {
                deletedItems = new HashMap<String, HashMap<Configuration, DeletedConfiguration>>();
            }
            HashMap<Configuration, DeletedConfiguration> map = new HashMap<Configuration, DeletedConfiguration>();
            deletedItems.put(item.getPath(), map);
            Configuration[] configurations = configurationDescriptor.getConfs().toArray();
            for (int i = 0; i < configurations.length; i++) {
                DeletedConfiguration del = new DeletedConfiguration();
                del.ic = item.getItemConfiguration(configurations[i]);
                del.aux = configurations[i].removeAuxObject(item.getId()/*ItemConfiguration.getId(item.getPath())*/);
                map.put(configurations[i], del);
            }
        }
        item.setFolder(null);
        fireChangeEvent(this, setModified);
        return ret;
    }

    public boolean removeFolderAction(Folder folder) {
        return removeFolderAction(folder, true);
    }

    public boolean removeFolderAction(Folder folder, boolean setModified) {
        configurationDescriptor.fireFilesRemoved(folder.getAllItemsAsList());
        return removeFolder(folder, setModified);
    }

    private boolean removeFolder(Folder folder, boolean setModified) {
        boolean ret = false;
        if (folder != null) {
            if (folder.isDiskFolder()) {
                folder.detachListener();
            }
            folder.removeAll();
            ret = items.remove(folder);
            if (isProjectFiles()) {
                // Remove it form all configurations
                Configuration[] configurations = configurationDescriptor.getConfs().toArray();
                for (int i = 0; i < configurations.length; i++) {
                    configurations[i].removeAuxObject(folder.getId());
                }
            }
        }
        if (ret) {
            fireChangeEvent(this, setModified);
        }
        return ret;
    }

    /**
     * Remove all items and folders recursively
     */
    private void removeAll() {
        Item[] itemsToRemove = getItemsAsArray();
        Folder[] foldersToRemove = getFoldersAsArray();
        for (int i = 0; i < itemsToRemove.length; i++) {
            removeItem(itemsToRemove[i]);
        }
        for (int i = 0; i < foldersToRemove.length; i++) {
            removeFolder(foldersToRemove[i], true);
        }
    }

    public void reset() {
        items = new ArrayList<Object>();
        fireChangeEvent();
    }

    public Item findItemByPath(String path) {
        if (path == null) {
            return null;
        }
        Item[] anItems = getItemsAsArray();
        for (int i = 0; i < anItems.length; i++) {
            if (path.equals(anItems[i].getPath())) {
                return anItems[i];
            }
        }
        return null;
    }

    public Item findItemByName(String name) {
        if (name == null) {
            return null;
        }
        Item[] anItems = getItemsAsArray();
        for (int i = 0; i < anItems.length; i++) {
            if (name.equals(anItems[i].getName())) {
                return anItems[i];
            }
        }
        return null;
    }

    public Folder findFolderByName(String name) {
        if (name == null) {
            return null;
        }
        Folder[] folders = getFoldersAsArray();
        for (int i = 0; i < folders.length; i++) {
            if (name.equals(folders[i].getName())) {
                return folders[i];
            }
        }
        return null;
    }

    public Folder findFolderByDisplayName(String name) {
        if (name == null) {
            return null;
        }
        Folder[] folders = getFoldersAsArray();
        for (int i = 0; i < folders.length; i++) {
            if (name.equals(folders[i].getDisplayName())) {
                return folders[i];
            }
        }
        return null;
    }

    public Folder findFolderByPath(String path) {
        int i = path.indexOf('/');
        if (i >= 0) {
            String aName = path.substring(0, i);
            Folder folder = findFolderByName(aName);
            if (folder == null) {
                return null;
            }
            return folder.findFolderByPath(path.substring(i + 1));
        } else {
            return findFolderByName(path);
        }
    }

    public Item[] getItemsAsArray() {
        ArrayList<Item> found = new ArrayList<Item>();
        Iterator<?> iter = new ArrayList<Object>(getElements()).iterator();
        while (iter.hasNext()) {
            Object o = iter.next();
            if (o instanceof Item) {
                found.add((Item) o);
            }
        }
        return found.toArray(new Item[found.size()]);
    }

    private List<NativeFileItem> getAllItemsAsList() {
        ArrayList<NativeFileItem> found = new ArrayList<NativeFileItem>();
        Iterator<?> iter = new ArrayList<Object>(getElements()).iterator();
        while (iter.hasNext()) {
            Object o = iter.next();
            if (o instanceof Item) {
                found.add((Item) o);
            }
            if (o instanceof Folder) {
                List<NativeFileItem> anItems = ((Folder) o).getAllItemsAsList();
                found.addAll(anItems);
            }
        }
        return found;
    }

    public Item[] getAllItemsAsArray() {
        List<NativeFileItem> list = getAllItemsAsList();
        return list.toArray(new Item[list.size()]);
    }

    /*
     * Returns a set of all files in this logical folder and subfolders as FileObjetc's
     */
    public Set<DataObject> getAllItemsAsDataObjectSet(boolean projectFilesOnly, String MIMETypeFilter) {
        ArrayList<DataObject> files = new ArrayList<DataObject>();

        if (!projectFilesOnly || isProjectFiles()) {
            Iterator<?> iter = new ArrayList<Object>(getElements()).iterator();
            while (iter.hasNext()) {
                Object item = iter.next();
                if (item instanceof Item) {
                    DataObject da = ((Item) item).getDataObject();
                    if (da != null && (MIMETypeFilter == null || da.getPrimaryFile().getMIMEType().contains(MIMETypeFilter))) {
                        files.add(da);
                    }
                }
                if (item instanceof Folder) {
                    files.addAll(((Folder) item).getAllItemsAsDataObjectSet(projectFilesOnly, MIMETypeFilter));
                }
            }
        }

        return new LinkedHashSet<DataObject>(files);
    }

    /*
     * Returns a set of all logical folder in this folder as an array
     */
    public Folder[] getFoldersAsArray() {
        List<Folder> folders = getFolders();
        return folders.toArray(new Folder[folders.size()]);
    }

    /*
     * Returns a set of all logical folder in this folder
     */
    public List<Folder> getFolders() {
        List<Folder> folders = new ArrayList<Folder>();
        Iterator<?> iter = new ArrayList<Object>(getElements()).iterator();
        while (iter.hasNext()) {
            Object item = iter.next();
            if (item instanceof Folder) {
                folders.add((Folder) item);
            }
        }
        return folders;
    }

    /*
     * Returns a set of all logical folder and subfolders in this folder
     */
    public List<Folder> getAllFolders(boolean projectFilesOnly) {
        List<Folder> folders = new ArrayList<Folder>();

        if (!projectFilesOnly || isProjectFiles()) {
            Iterator<?> iter = new ArrayList<Object>(getElements()).iterator();
            while (iter.hasNext()) {
                Object item = iter.next();
                if (item instanceof Folder) {
                    if (!projectFilesOnly || ((Folder) item).isProjectFiles()) {
                        folders.add((Folder) item);
                        folders.addAll(((Folder) item).getAllFolders(projectFilesOnly));
                    }
                }
            }
        }

        return folders;
    }

    public List<Folder> getAllTests() {
        List<Folder> list = new ArrayList<Folder>();
        getTests(list);
        return list;
    }

    /*
     * recursive!
     */
    private void getTests(List<Folder> list) {
        Iterator<?> iter = new ArrayList<Object>(getElements()).iterator();
        while (iter.hasNext()) {
            Object o = iter.next();
            if (o instanceof Folder) {
                if (((Folder)o).isTest()) {
                    list.add((Folder)o);
                }
                ((Folder)o).getTests(list);
            }
        }
    }

    public void addChangeListener(ChangeListener cl) {
        synchronized (changeListenerList) {
            changeListenerList.add(cl);
        }
    }

    public void removeChangeListener(ChangeListener cl) {
        synchronized (changeListenerList) {
            changeListenerList.remove(cl);
        }
    }

    public void refresh(Object source) {
        fireChangeEvent(source, true);
    }

    public void fireChangeEvent() {
        fireChangeEvent(this, true);
    }

    private void fireChangeEvent(Object source, boolean setModified) {
        Iterator<ChangeListener> it;

        synchronized (changeListenerList) {
            it = new HashSet<ChangeListener>(changeListenerList).iterator();
        }
        ChangeEvent ev = new ChangeEvent(source);
        while (it.hasNext()) {
            (it.next()).stateChanged(ev);
        }
        if (setModified) {
            configurationDescriptor.setModified();
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (log.isLoggable(Level.FINER)) {
            log.log(Level.FINE, "------------stateChanged {0}", getPath()); // NOI18N
        }
        // Happens when filter has changed
        if (isDiskFolder()) {
            refreshDiskFolder(true);
        }
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
    }

    @Override
    public void fileChanged(FileEvent fe) {
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        FileObject fileObject = fe.getFile();
        File file = FileUtil.toFile(fileObject);
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "------------fileDataCreated {0} in {1}", new Object[]{file, getPath()}); // NOI18N
        }
        //if (true) return;
        if (!file.exists() || file.isDirectory()) {
            return; // FIXUP: error
        }
        if (!FileFilterFactory.getAllSourceFileFilter().accept(file)) {
            return;
        }
        String itemPath = file.getPath();
        itemPath = CndPathUtilitities.toRelativePath(getConfigurationDescriptor().getBaseDir(), itemPath);
        itemPath = CndPathUtilitities.normalize(itemPath);
        Item item = new Item(itemPath);
        addItemAction(item, false);
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        FileObject fileObject = fe.getFile();
        assert fileObject.isFolder();
        if (fileObject.isValid()) {
            File file = FileUtil.toFile(fileObject);
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "------------fileFolderCreated {0} in {1}", new Object[]{file.getPath(), getPath()}); // NOI18N
            }
            if (!file.exists() || !file.isDirectory()) {
                // It is possible that short-living temporary folder is created while building project
                return;
            }
            /*Folder top =*/ getConfigurationDescriptor().addFilesFromDir(this, file, true, false, null);
        }
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        FileObject fileObject = fe.getFile();
        File file = FileUtil.toFile(fileObject);
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "------------fileDeleted {0} in {1}", new Object[]{file.getPath(), getPath()}); // NOI18N
        }
        //if (true) return;
        String path = getRootPath() + '/' + file.getName();
        if (path.startsWith("./")) { // NOI18N
            path = path.substring(2);
        }
        // Try item first
        Item item = findItemByPath(path);
        if (item != null) {
            removeItemAction(item, false);
            return;
        }
        // then folder
        Folder folder = findFolderByName(file.getName());
        if (folder != null) {
            removeFolderAction(folder, false);
            return;
        }
    }

    private void copyConfigurations(Folder src) {
        MakeConfigurationDescriptor makeConfigurationDescriptor = getConfigurationDescriptor();
        if (makeConfigurationDescriptor == null) {
            return;
        }

        for (Configuration conf : makeConfigurationDescriptor.getConfs().toArray()) {
            FolderConfiguration srcFolderConfiguration = src.getFolderConfiguration(conf);
            FolderConfiguration dstFolderConfiguration = getFolderConfiguration(conf);
            if (srcFolderConfiguration != null && dstFolderConfiguration != null) {
                dstFolderConfiguration.assignValues(srcFolderConfiguration);
            }
        }
    }

    private static void copyConfigurations(Folder oldFolder, Folder newFolder) {
        newFolder.copyConfigurations(oldFolder);
        // Copy item configurations
        Item oldItems[] = oldFolder.getItemsAsArray();
        for (Item oldItem : oldItems) {
            Item newItem = newFolder.findItemByName(oldItem.getName());
            if (newItem != null) {
                newItem.copyConfigurations(oldItem);
            }
        }
        // copy subfolder cnfigurations
        Folder srcFolders[] = oldFolder.getFoldersAsArray();
        for (Folder srcFolder : srcFolders) {
            Folder dstFolder = newFolder.findFolderByName(srcFolder.getName());
            if (dstFolder != null) {
                dstFolder.copyConfigurations(srcFolder);
            }
        }
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        FileObject fileObject = fe.getFile();
        File file = FileUtil.toFile(fileObject);
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "------------fileRenamed {0} in {1}", new Object[]{file.getPath(), getPath()}); // NOI18N
        }
        // Try only folders. Items are taken care of in Item.propertyChange takes care of it....
        Folder folder = findFolderByName(fe.getName());
        if (folder != null && folder.isDiskFolder()) {
            // Add new Folder
            Folder top = getConfigurationDescriptor().addFilesFromDir(this, file, true, false, null);
            // Copy all configurations
            copyConfigurations(folder, top);
            // Remove old folder
            removeFolderAction(folder, false);
        }
    }

    private static boolean checkLogging() {
        if (checkedLogging) {
            return true;
        }
        String logProp = System.getProperty("makeproject.folder"); // NOI18N
        if (logProp != null) {
            if (logProp.equals("FINE")) { // NOI18N
                log.setLevel(Level.FINE);
            } else if (logProp.equals("FINER")) { // NOI18N
                log.setLevel(Level.FINER);
            } else if (logProp.equals("FINEST")) { // NOI18N
                log.setLevel(Level.FINEST);
            }
        }
        return true;
    }

    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(Folder.class, s);
    }

    @Override
    public String toString() {
        return name;
    }

    private static final class DeletedConfiguration {
        private ConfigurationAuxObject aux;
        private ItemConfiguration ic;
    }
}
