/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItemSet;
import org.netbeans.modules.cnd.api.utils.AllSourceFileFilter;
import org.netbeans.modules.cnd.api.utils.CndFileVisibilityQuery;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
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

    public static final String DEFAULT_FOLDER_NAME = "f"; // NOI18N
    public static final String DEFAULT_FOLDER_DISPLAY_NAME = getString("NewFolderName");
    private ConfigurationDescriptor configurationDescriptor;
    private final String name;
    private String displayName;
    private final Folder parent;
    private ArrayList<Object> items = null; // Folder or Item
    private final Set<ChangeListener> changeListenerList = new WeakSet<ChangeListener>(1);
    private final boolean projectFiles;
    private String id = null;
    private String root;
    private final static Logger log = Logger.getLogger("makeproject.folder"); // NOI18N
    private static boolean checkedLogging = checkLogging();

    public Folder(ConfigurationDescriptor configurationDescriptor, Folder parent, String name, String displayName, boolean projectFiles) {
        this.configurationDescriptor = configurationDescriptor;
        this.parent = parent;
        this.name = name;
        this.displayName = displayName;
        this.projectFiles = projectFiles;
        this.items = new ArrayList<Object>();
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public String getRoot() {
        return root;
    }

    public Folder getThis() {
        return this;
    }

    public void refreshDiskFolder() {
        if (log.isLoggable(Level.FINER)) {
            log.finer("----------refreshDiskFolder " + getPath()); // NOI18N
        }
        String rootPath = getRootPath();
        String AbsRootPath = IpeUtils.toAbsolutePath(configurationDescriptor.getBaseDir(), rootPath);

        File folderFile = new File(AbsRootPath);

        // Folders to be removed
        if (!folderFile.exists() ||
            !folderFile.isDirectory() ||
            !VisibilityQuery.getDefault().isVisible(folderFile) ||
            ((MakeConfigurationDescriptor) getConfigurationDescriptor()).getFolderVisibilityQuery().isVisible(folderFile)) {
            // Remove it plus all subfolders and items from project
            if (log.isLoggable(Level.FINE)) {
                log.fine("------------removing folder " + getPath() + " in " + getParent().getPath()); // NOI18N
            }
            getParent().removeFolder(this);
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
                    log.fine("------------removing item " + item.getPath() + " in " + getPath()); // NOI18N
                }
                removeItem(item);
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
            if (files[i].isDirectory() && ((MakeConfigurationDescriptor) getConfigurationDescriptor()).getFolderVisibilityQuery().isVisible(files[i])) {
                continue;
            }
            fileList.add(files[i]);
        }
        for (File file : fileList) {
            if (file.isDirectory()) {
                if (findFolderByName(file.getName()) == null) {
                    if (log.isLoggable(Level.FINE)) {
                        log.fine("------------adding folder " + file.getPath() + " in " + getPath()); // NOI18N
                    }
                    ((MakeConfigurationDescriptor) getConfigurationDescriptor()).addSourceFilesFromFolder(this, file, true);

                }
            } else {
                String path = getRootPath() + '/' + file.getName();
                if (path.startsWith("./")) { // NOI18N
                    path = path.substring(2);
                }
                if (findItemByPath(path) == null) {
                    if (log.isLoggable(Level.FINE)) {
                        log.fine("------------adding item " + file.getPath() + " in " + getPath()); // NOI18N
                    }
                    addItem(new Item(path));
                }
            }
        }

        // Repeast for all sub folders
        Vector<Folder> subFolders = getFolders();
        for (Folder f : subFolders) {
            f.refreshDiskFolder();
        }
    }

    public void attachListeners() {
        String rootPath = getRootPath();
        String AbsRootPath = IpeUtils.toAbsolutePath(configurationDescriptor.getBaseDir(), rootPath);
        File folderFile = new File(AbsRootPath);

        if (!folderFile.exists() || !folderFile.isDirectory()) {
            return;
        }

        if (isDiskFolder() && getRoot() != null) {
            VisibilityQuery.getDefault().addChangeListener(this);
            CndFileVisibilityQuery.getDefault().addChangeListener(this);
            ((MakeConfigurationDescriptor) getConfigurationDescriptor()).getFolderVisibilityQuery().addChangeListener(this);
            if (log.isLoggable(Level.FINER)) {
                log.finer("-----------attachFilterListener " + getPath()); // NOI18N
            }
        }

        try {
            FileUtil.addFileChangeListener(this, folderFile);
            if (log.isLoggable(Level.FINER)) {
                log.finer("-----------attachFileChangeListener " + getPath()); // NOI18N
            }
        } catch (IllegalArgumentException iae) {
            // Can happen if trying to attach twice...
            if (log.isLoggable(Level.FINER)) {
                log.finer("-----------attachFileChangeListener duplicate error" + getPath()); // NOI18N
            }
        }

        // Repeast for all sub folders
        Vector<Folder> subFolders = getFolders();
        for (Folder f : subFolders) {
            f.attachListeners();
        }
    }

    public void detachListener() {
        if (log.isLoggable(Level.FINER)) {
           log.finer("-----------detachFileChangeListener " + getPath()); // NOI18N
        }
        FileUtil.removeFileChangeListener(this);
        if (isDiskFolder() && getRoot() != null) {
            VisibilityQuery.getDefault().removeChangeListener(this);
            CndFileVisibilityQuery.getDefault().removeChangeListener(this);
            ((MakeConfigurationDescriptor) getConfigurationDescriptor()).getFolderVisibilityQuery().removeChangeListener(this);
            if (log.isLoggable(Level.FINER)) {
                log.finer("-----------detachFilterListener " + getPath()); // NOI18N
            }
        }

    }

    public int size() {
        return items.size();
    }

    public Folder getParent() {
        return parent;
    }

    public Project getProject() {
        return ((MakeConfigurationDescriptor) getConfigurationDescriptor()).getProject();
    }

    public String getName() {
        return name;
    }

    public String getSortName() {
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

    public ConfigurationDescriptor getConfigurationDescriptor() {
        return configurationDescriptor;
    }

    public void setConfigurationDescriptor(ConfigurationDescriptor configurationDescriptor) {
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

    public ArrayList<Object> getElements() {
        return items;
    }

    public void reInsertElement(Object element) {
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

    public void addElement(Object element) { // FIXUP: shopuld be private
        // Always keep the vector sorted
        int indexAt = -1;
        if (element instanceof Item) {
            insertItemElement((Item) element);
        } else if (element instanceof Folder) {
            insertFolderElement((Folder) element);
        } else {
            assert false;
        }
        fireChangeEvent();
    }

    public Item addItemAction(Item item) {
        if (addItem(item) == null) {
            return null; // Nothing added
        }
        ArrayList<NativeFileItem> list = new ArrayList<NativeFileItem>(1);
        list.add(item);
        ((MakeConfigurationDescriptor) configurationDescriptor).fireFilesAdded(list);
        return item;
    }

    public Item addItem(Item item) {
        return addItem(item, true);
    }

    public Item addItem(Item item, boolean notify) {
        if (item == null) {
            return null;
        }
        // Check if already in project. Refresh if it's there.
        Item existingItem;
        if (isProjectFiles() && (existingItem = ((MakeConfigurationDescriptor) configurationDescriptor).findProjectItemByPath(item.getPath())) != null) {
            //System.err.println("Folder - addItem - item ignored, already added: " + item); // NOI18N  // FIXUP: correct?
            refresh(existingItem);
            return null; // Nothing added
        }
        // Add it to the folder
        item.setFolder(this);
        addElement(item);

        // Add item to the dataObject's lookup
        if (isProjectFiles() && notify) {
            DataObject dao = item.getDataObject();
            NativeFileItemSet myNativeFileItemSet = (dao == null) ? null : dao.getCookie(NativeFileItemSet.class);
            if (myNativeFileItemSet != null) {
                myNativeFileItemSet.add(item);
            } else {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("can not add folder's " + this + " item " + item + " using " + dao); // NOI18N
                }
            }
        }

        // Add it to project Items
        if (isProjectFiles()) {
            ((MakeConfigurationDescriptor) configurationDescriptor).addProjectItem(item);
            // Add configuration to all configurations
            if (configurationDescriptor.getConfs() == null) {
                return item;
            }
            Configuration[] configurations = configurationDescriptor.getConfs().getConfs();
            for (int i = 0; i < configurations.length; i++) {
                FolderConfiguration folderConfiguration = getFolderConfiguration(configurations[i]);
                configurations[i].addAuxObject(new ItemConfiguration(configurations[i], item));
            }
        }

        return item;
    }

    public void addFolder(Folder folder) {
        addElement(folder);
        if (isProjectFiles()) {
            // Add configuration to all configurations
            if (configurationDescriptor.getConfs() == null) {
                return;
            }
            Configuration[] configurations = configurationDescriptor.getConfs().getConfs();
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
        if (isProjectFiles()) {
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

    public Folder addNewFolder(boolean projectFiles) {
        String aNname;
        String aDisplayName;
        for (int i = 1;; i++) {
            aNname = DEFAULT_FOLDER_NAME + i;
            aDisplayName = DEFAULT_FOLDER_DISPLAY_NAME + " " + i; // NOI18N
            if (findFolderByName(aNname) == null) {
                break;
            }
        }
        return addNewFolder(aNname, aDisplayName, projectFiles); // NOI18N
    }

    public Folder addNewFolder(String name, String displayName, boolean projectFiles) {
        Folder newFolder = new Folder(getConfigurationDescriptor(), this, name, displayName, projectFiles);
        addFolder(newFolder);
        return newFolder;
    }

    public boolean removeItemAction(Item item) {
        ArrayList<NativeFileItem> list = new ArrayList<NativeFileItem>(1);
        list.add(item);
        if (isProjectFiles()) {
            ((MakeConfigurationDescriptor) configurationDescriptor).fireFilesRemoved(list);
        }
        return removeItem(item);
    }

    public void renameItemAction(String oldPath, Item newItem) {
        ((MakeConfigurationDescriptor) configurationDescriptor).fireFileRenamed(oldPath, newItem);
    }

    public boolean removeItem(Item item) {
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
            ((MakeConfigurationDescriptor) configurationDescriptor).removeProjectItem(item);
            // Remove it form all configurations
            Configuration[] configurations = configurationDescriptor.getConfs().getConfs();
            for (int i = 0; i < configurations.length; i++) {
                configurations[i].removeAuxObject(item.getId()/*ItemConfiguration.getId(item.getPath())*/);
            }
        }
        item.setFolder(null);
        fireChangeEvent();
        return ret;
    }

    public boolean removeItemByPath(String path) {
        boolean ret = false;
        Item item = findItemByPath(path);
        return removeItem(item);
    }

    public boolean removeFolderAction(Folder folder) {
        ((MakeConfigurationDescriptor) configurationDescriptor).fireFilesRemoved(folder.getAllItemsAsList());
        return removeFolder(folder);
    }

    public boolean removeFolder(Folder folder) {
        boolean ret = false;
        if (folder != null) {
            if (folder.isDiskFolder()) {
                folder.detachListener();
            }
            folder.removeAll();
            ret = items.remove(folder);
            if (isProjectFiles()) {
                // Remove it form all configurations
                Configuration[] configurations = configurationDescriptor.getConfs().getConfs();
                for (int i = 0; i < configurations.length; i++) {
                    configurations[i].removeAuxObject(folder.getId());
                }
            }
        }
        if (ret) {
            fireChangeEvent();
        }
        return ret;
    }

    /**
     * Remove all items and folders recursively
     */
    public void removeAll() {
        Item[] itemsToRemove = getItemsAsArray();
        Folder[] foldersToRemove = getFoldersAsArray();
        for (int i = 0; i < itemsToRemove.length; i++) {
            removeItem(itemsToRemove[i]);
        }
        for (int i = 0; i < foldersToRemove.length; i++) {
            removeFolder(foldersToRemove[i]);
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
        Iterator iter = new ArrayList<Object>(getElements()).iterator();
        while (iter.hasNext()) {
            Object o = iter.next();
            if (o instanceof Item) {
                found.add((Item) o);
            }
        }
        return found.toArray(new Item[found.size()]);
    }

    public List<NativeFileItem> getAllItemsAsList() {
        ArrayList<NativeFileItem> found = new ArrayList<NativeFileItem>();
        Iterator iter = new ArrayList<Object>(getElements()).iterator();
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
     * Returns a set of all files in this logical folder as FileObjetc's
     */
    public Set<FileObject> getItemsAsFileObjectSet() {
        ArrayList<FileObject> files = new ArrayList<FileObject>();
        Iterator iter = new ArrayList<Object>(getElements()).iterator();
        while (iter.hasNext()) {
            Item item = (Item) iter.next();
            FileObject fo = item.getFileObject();
            if (fo != null) {
                files.add(fo);
            }
        }
        return new LinkedHashSet<FileObject>(files);
    }

    /*
     * Returns a set of all files in this logical folder and subfolders as FileObjetc's
     */
    public Set<FileObject> getAllItemsAsFileObjectSet(boolean projectFilesOnly) {
        ArrayList<FileObject> files = new ArrayList<FileObject>();

        if (!projectFilesOnly || isProjectFiles()) {
            Iterator iter = new ArrayList<Object>(getElements()).iterator();
            while (iter.hasNext()) {
                Object item = iter.next();
                if (item instanceof Item) {
                    FileObject fo = ((Item) item).getFileObject();
                    if (fo != null) {
                        files.add(fo);
                    }
                }
                if (item instanceof Folder) {
                    files.addAll(((Folder) item).getAllItemsAsFileObjectSet(projectFilesOnly));
                }
            }
        }

        return new LinkedHashSet<FileObject>(files);
    }

    /*
     * Returns a set of all files in this logical folder as FileObjetc's
     */
    public Set<DataObject> getItemsAsDataObjectSet(String MIMETypeFilter) {
        ArrayList<DataObject> files = new ArrayList<DataObject>();
        Iterator iter = new ArrayList<Object>(getElements()).iterator();
        while (iter.hasNext()) {
            Item item = (Item) iter.next();
            DataObject da = item.getDataObject();
            if (da != null && (MIMETypeFilter == null || da.getPrimaryFile().getMIMEType().contains(MIMETypeFilter))) {
                files.add(da);
            }
        }
        return new LinkedHashSet<DataObject>(files);
    }

    /*
     * Returns a set of all files in this logical folder and subfolders as FileObjetc's
     */
    public Set<DataObject> getAllItemsAsDataObjectSet(boolean projectFilesOnly, String MIMETypeFilter) {
        ArrayList<DataObject> files = new ArrayList<DataObject>();

        if (!projectFilesOnly || isProjectFiles()) {
            Iterator iter = new ArrayList<Object>(getElements()).iterator();
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
        Vector<Folder> folders = getFolders();
        return folders.toArray(new Folder[folders.size()]);
    }

    /*
     * Returns a set of all logical folder in this folder
     */
    public Vector<Folder> getFolders() {
        Vector<Folder> folders = new Vector<Folder>();
        Iterator iter = new ArrayList<Object>(getElements()).iterator();
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
    public Vector<Folder> getAllFolders(boolean projectFilesOnly) {
        Vector<Folder> folders = new Vector<Folder>();

        if (!projectFilesOnly || isProjectFiles()) {
            Iterator iter = new ArrayList<Object>(getElements()).iterator();
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

    public String[] getFolderNamesAsArray() {
        Folder[] anItems = getFoldersAsArray();
        String[] names = new String[anItems.length];
        for (int i = 0; i < anItems.length; i++) {
            names[i] = anItems[i].getName();
        }
        return names;
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

    public void refresh() {
        fireChangeEvent(this);
    }

    public void refresh(Object source) {
        fireChangeEvent(source);
    }

    public void fireChangeEvent() {
        fireChangeEvent(this);
    }

    public void fireChangeEvent(Object source) {
        Iterator it;

        synchronized (changeListenerList) {
            it = new HashSet<ChangeListener>(changeListenerList).iterator();
        }
        ChangeEvent ev = new ChangeEvent(source);
        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(ev);
        }
        configurationDescriptor.setModified();
    }

    public void stateChanged(ChangeEvent e) {
        if (log.isLoggable(Level.FINER)) {
            log.fine("------------stateChanged " + getThis().getPath()); // NOI18N
        }
        // Happens when filter has changed
        if (isDiskFolder()) {
            refreshDiskFolder();
        }
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
    }

    public void fileChanged(FileEvent fe) {
    }

    public void fileDataCreated(FileEvent fe) {
        boolean currentState = getConfigurationDescriptor().getModified();
        FileObject fileObject = fe.getFile();
        File file = FileUtil.toFile(fileObject);
        if (log.isLoggable(Level.FINE)) {
            log.fine("------------fileDataCreated " + file + " in " + getThis().getPath()); // NOI18N
        }
        //if (true) return;
        if (!file.exists() || file.isDirectory()) {
            return; // FIXUP: error
        }
        if (!AllSourceFileFilter.getInstance().accept(file)) {
            return;
        }
        String itemPath = file.getPath();
        itemPath = IpeUtils.toRelativePath(getConfigurationDescriptor().getBaseDir(), itemPath);
        itemPath = FilePathAdaptor.normalize(itemPath);
        Item item = new Item(itemPath);
        addItemAction(item);
        getConfigurationDescriptor().setModified(currentState);
    }

    public void fileFolderCreated(FileEvent fe) {
        boolean currentState = getConfigurationDescriptor().getModified();
        FileObject fileObject = fe.getFile();
        File file = FileUtil.toFile(fileObject);
        if (log.isLoggable(Level.FINE)) {
            log.fine("------------fileFolderCreated " + file.getPath() + " in " + getThis().getPath()); // NOI18N
        }
        //if (true) return;
        if (!file.exists() || !file.isDirectory()) {
            assert false;
            return;
        }
        Folder top = ((MakeConfigurationDescriptor) getConfigurationDescriptor()).addSourceFilesFromFolder(getThis(), file, true);
        getConfigurationDescriptor().setModified(currentState);
    }

    public void fileDeleted(FileEvent fe) {
        boolean currentState = getConfigurationDescriptor().getModified();
        FileObject fileObject = fe.getFile();
        File file = FileUtil.toFile(fileObject);
        if (log.isLoggable(Level.FINE)) {
            log.fine("------------fileDeleted " + file.getPath() + " in " + getThis().getPath()); // NOI18N
        }
        //if (true) return;
        String path = getRootPath() + '/' + file.getName();
        if (path.startsWith("./")) { // NOI18N
            path = path.substring(2);
        }
        // Try item first
        Item item = findItemByPath(path);
        if (item != null) {
            removeItemAction(item);
            getConfigurationDescriptor().setModified(currentState);
            return;
        }
        // then folder
        Folder folder = findFolderByName(file.getName());
        if (folder != null) {
            removeFolderAction(folder);
            getConfigurationDescriptor().setModified(currentState);
            return;
        }
    }

    public void copyConfigurations(Folder src) {
        MakeConfigurationDescriptor makeConfigurationDescriptor = (MakeConfigurationDescriptor) getConfigurationDescriptor();
        if (makeConfigurationDescriptor == null) {
            return;
        }

        for (Configuration conf : makeConfigurationDescriptor.getConfs().getConfs()) {
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

    public void fileRenamed(FileRenameEvent fe) {
        boolean currentState = getConfigurationDescriptor().getModified();
        FileObject fileObject = fe.getFile();
        File file = FileUtil.toFile(fileObject);
        if (log.isLoggable(Level.FINE)) {
            log.fine("------------fileRenamed " + file.getPath() + " in " + getThis().getPath()); // NOI18N
        }
        // Try only folders. Items are taken care of in Item.propertyChange takes care of it....
        Folder folder = findFolderByName(fe.getName());
        if (folder != null && folder.isDiskFolder()) {
            // Add new Folder
            Folder top = ((MakeConfigurationDescriptor) getConfigurationDescriptor()).addSourceFilesFromFolder(getThis(), file, true);
            // Copy all configurations
            copyConfigurations(folder, top);
            // Remove old folder
            removeFolderAction(folder);
            getConfigurationDescriptor().setModified(currentState);
            return;
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
}
