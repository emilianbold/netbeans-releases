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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItemSet;
import org.netbeans.modules.cnd.loaders.CndDataObject;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

public class Folder {
    public static final String DEFAULT_FOLDER_NAME = "f"; // NOI18N
    public static final String DEFAULT_FOLDER_DISPLAY_NAME = getString("NewFolderName");
    
    private ConfigurationDescriptor configurationDescriptor;
    private final String name;
    private String displayName;
    private String sortName;
    private final Folder parent;
    private Vector items = null; // Folder or Item
    private Set<ChangeListener> changeListenerList = new HashSet<ChangeListener>();
    private final boolean projectFiles;
    private String id = null;
    
    public Folder(ConfigurationDescriptor configurationDescriptor, Folder parent, String name, String displayName, boolean projectFiles) {
        this.configurationDescriptor = configurationDescriptor;
        this.parent = parent;
        this.name = name;
        this.displayName = displayName;
        this.projectFiles = projectFiles;
        this.items = new Vector();
        this.sortName = displayName.toLowerCase();
    }
    
    public int size() {
        return items.size();
    }
    
    public Folder getParent() {
        return parent;
    }
    
    public Project getProject() {
        return ((MakeConfigurationDescriptor)getConfigurationDescriptor()).getProject();
    }
    
    public String getName() {
        return name;
    }
    
    public String getSortName() {
        return sortName;
    }
    
    public String getPath() {
//        StringBuilder builder = new StringBuilder(getName());
//        Folder parent = getParent();
//        while (parent != null) {
//            if (parent.getParent() != null) {
//                builder.insert(0, '/'); // NOI18N
//                builder.insert(0, parent.getName());
//            }
//            parent = parent.getParent();
//        };
//        return builder.toString();
        StringBuilder builder2 = new StringBuilder(32);
        reversePath(this, builder2);
        return builder2.toString();
    }
    
    private void reversePath(Folder folder, StringBuilder builder){
        Folder parent = folder.getParent();
        if (parent != null && parent.getParent() != null) {
            reversePath(parent, builder);
            builder.append('/'); // NOI18N
        }
        builder.append(folder.getName());
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        configurationDescriptor.setModified();
        sortName = displayName.toLowerCase();
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
    
    public Vector getElements() {
        return items;
    }
    
    public void reInsertElement(Object element) {
        int index = items.indexOf(element);
        if (index < 0)
            return;
        items.remove(element);
        if (element instanceof Folder)
            insertFolderElement((Folder)element);
        else if (element instanceof Item)
            insertItemElement((Item)element);
        else
            assert false;
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
            Object o = items.elementAt(indexAt);
            if (!(o instanceof Folder)) {
                indexAt--;
                continue;
            }
            if (!((Folder)o).isProjectFiles()) {
                indexAt--;
                continue;
            }
            String name2 = ((Folder)o).getSortName();
            int compareRes = name1.compareTo(name2);
            if (compareRes < 0) {
                indexAt--;
                continue;
            }
            break;
        }
        items.add(indexAt+1, element);
    }
    
    private void insertItemElement(Item element) {
        String name1 = ((Item)element).getSortName();
        int indexAt = items.size() - 1;
        while (indexAt >= 0) {
            Object o = items.elementAt(indexAt);
            if (!(o instanceof Item)) {
                //indexAt--;
                break;
            }
            String name2 = ((Item)o).getSortName();
            int compareRes = name1.compareTo(name2);
            if (compareRes < 0) {
                indexAt--;
                continue;
            }
            break;
        }
        items.add(indexAt+1, element);
    }
    
    public void addElement(Object element) { // FIXUP: shopuld be private
        // Always keep the vector sorted
        int indexAt = -1;
        if (element instanceof Item) {
            insertItemElement((Item)element);
        } else if (element instanceof Folder) {
            insertFolderElement((Folder)element);
        } else {
            assert false;
        }
        fireChangeEvent();
    }
    
    public Item addItemAction(Item item) {
        if (addItem(item) == null) {
            return null; // Nothing added
        }
        ArrayList list = new ArrayList(1);
        list.add(item);
        ((MakeConfigurationDescriptor)configurationDescriptor).fireFilesAdded(list);
        return item;
    }
    public Item addItem(Item item) {
        return addItem(item, true);
    }
    
    public Item addItem(Item item, boolean notify) {
        if (item == null)
            return null;
        // Check if already in project. Refresh if it's there.
        Item existingItem;
        if (isProjectFiles() && (existingItem = ((MakeConfigurationDescriptor)configurationDescriptor).findProjectItemByPath(item.getPath())) != null) {
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
            if (dao instanceof CndDataObject) {
                CndDataObject dataObject = (CndDataObject) dao;
                MyNativeFileItemSet myNativeFileItemSet = (MyNativeFileItemSet)dataObject.getCookie(MyNativeFileItemSet.class);
                if (myNativeFileItemSet == null) {
                    myNativeFileItemSet = new MyNativeFileItemSet();
                    dataObject.addCookie(myNativeFileItemSet);
                }
                myNativeFileItemSet.add(item);
            }
        }
        
        // Add it to project Items
        if (isProjectFiles()) {
            ((MakeConfigurationDescriptor)configurationDescriptor).addProjectItem(item);
            // Add configuration to all configurations
            if (configurationDescriptor.getConfs() == null)
                return item;
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
            if (configurationDescriptor.getConfs() == null)
                return;
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
            String id = getId();
            folderConfiguration = (FolderConfiguration)configuration.getAuxObject(getId());
            if (folderConfiguration == null) {
                CCompilerConfiguration parentCCompilerConfiguration;
                CCCompilerConfiguration parentCCCompilerConfiguration;
                FolderConfiguration parentFolderConfiguration = null;
                if (getParent() != null)
                    parentFolderConfiguration = getParent().getFolderConfiguration(configuration);
                if (parentFolderConfiguration != null) {
                    parentCCompilerConfiguration = parentFolderConfiguration.getCCompilerConfiguration();
                    parentCCCompilerConfiguration = parentFolderConfiguration.getCCCompilerConfiguration();
                } else {
                    parentCCompilerConfiguration = ((MakeConfiguration)configuration).getCCompilerConfiguration();
                    parentCCCompilerConfiguration = ((MakeConfiguration)configuration).getCCCompilerConfiguration();
                }
                folderConfiguration = new FolderConfiguration(configuration, parentCCompilerConfiguration, parentCCCompilerConfiguration, this);
                configuration.addAuxObject(folderConfiguration);
            }
        }
        return folderConfiguration;
    }
    
    public Folder addNewFolder(boolean projectFiles) {
        String name;
        String displayName;
        for (int i = 1;; i++) {
            name = DEFAULT_FOLDER_NAME + i;
            displayName = DEFAULT_FOLDER_DISPLAY_NAME + " " + i; // NOI18N
            if (findFolderByName(name) == null)
                break;
        }
        return addNewFolder(name, displayName, projectFiles); // NOI18N
    }
    
    public Folder addNewFolder(String name, String displayName, boolean projectFiles) {
        Folder newFolder = new Folder(getConfigurationDescriptor(), this, name, displayName, projectFiles);
        addFolder(newFolder);
        return newFolder;
    }
    
    public boolean removeItemAction(Item item) {
        ArrayList list = new ArrayList(1);
        list.add(item);
        if (isProjectFiles())
            ((MakeConfigurationDescriptor)configurationDescriptor).fireFilesRemoved(list);
        return removeItem(item);
    }
    
    public void renameItemAction(String oldPath, Item newItem) {
        ((MakeConfigurationDescriptor)configurationDescriptor).fireFileRenamed(oldPath, newItem);
    }
    
    public boolean removeItem(Item item) {
        boolean ret = false;
        if (item == null)
            return false;
        // Remove it from folder
        ret = items.removeElement(item);
        if (!ret)
            return ret;
        
        // Remove item from the dataObject's lookup
        if (isProjectFiles()) {
            DataObject dataObject = item.getDataObject();
            if (dataObject == null){
                // try to use last Data Object (getDataObject() cannot find renamed data object)
                dataObject = item.getLastDataObject();
            }
            if (dataObject instanceof CndDataObject) {
                CndDataObject cndDataObject = (CndDataObject)dataObject;
                MyNativeFileItemSet myNativeFileItemSet = (MyNativeFileItemSet)cndDataObject.getCookie(MyNativeFileItemSet.class);
                if (myNativeFileItemSet != null) {
                    myNativeFileItemSet.remove(item);
                    if (myNativeFileItemSet.isEmpty())
                        cndDataObject.removeCookie(myNativeFileItemSet);
                }
            }
        }
        
//	item.setFolder(null);
        if (isProjectFiles()) {
            // Remove it from project Items
            ((MakeConfigurationDescriptor)configurationDescriptor).removeProjectItem(item);
            // Remove it form all configurations
            Configuration[] configurations = configurationDescriptor.getConfs().getConfs();
            for (int i = 0; i < configurations.length; i++)
                configurations[i].removeAuxObject(item.getId()/*ItemConfiguration.getId(item.getPath())*/);
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
        ((MakeConfigurationDescriptor)configurationDescriptor).fireFilesRemoved(folder.getAllItemsAsList());
        return removeFolder(folder);
    }
    
    public boolean removeFolder(Folder folder) {
        boolean ret = false;
        if (folder != null) {
            folder.removeAll();
            ret = items.removeElement(folder);
            if (isProjectFiles()) {
                // Remove it form all configurations
                Configuration[] configurations = configurationDescriptor.getConfs().getConfs();
                for (int i = 0; i < configurations.length; i++)
                    configurations[i].removeAuxObject(folder.getId());
            }
        }
        if (ret)
            fireChangeEvent();
        return ret;
    }
    
    /**
     * Remove all items and folders recursively
     */
    public void removeAll() {
        Item[] itemsToRemove = getItemsAsArray();
        Folder[] foldersToRemove = getFoldersAsArray();
        for (int i = 0; i < itemsToRemove.length; i++)
            removeItem(itemsToRemove[i]);
        for (int i = 0; i < foldersToRemove.length; i++)
            removeFolder(foldersToRemove[i]);
    }
    
    public void reset() {
        items = new Vector();
        fireChangeEvent();
    }
    
    public Item findItemByPath(String path) {
        if (path == null)
            return null;
        Item[] items = getItemsAsArray();
        for (int i = 0; i < items.length; i++) {
            if (path.equals(items[i].getPath()))
                return items[i];
        }
        return null;
    }
    
    public Folder findFolderByName(String name) {
        if (name == null)
            return null;
        Folder[] folders = getFoldersAsArray();
        for (int i = 0; i < folders.length; i++) {
            if (name.equals(folders[i].getName()))
                return folders[i];
        }
        return null;
    }
    
    public Folder findFolderByDisplayName(String name) {
        if (name == null)
            return null;
        Folder[] folders = getFoldersAsArray();
        for (int i = 0; i < folders.length; i++) {
            if (name.equals(folders[i].getDisplayName()))
                return folders[i];
        }
        return null;
    }
    
    public Folder findFolderByPath(String path) {
        int i = path.indexOf('/');
        if (i >= 0) {
            String name = path.substring(0, i);
            Folder folder = findFolderByName(name);
            if (folder == null)
                return null;
            return folder.findFolderByPath(path.substring(i+1));
        } else
            return findFolderByName(path);
    }
    
    public Item[] getItemsAsArray() {
        Vector found = new Vector();
        Iterator iter = new ArrayList(getElements()).iterator();
        while (iter.hasNext()) {
            Object o = iter.next();
            if (o instanceof Item)
                found.add(o);
        }
        return (Item[])found.toArray(new Item[found.size()]);
    }
    
    public List getAllItemsAsList() {
        ArrayList found = new ArrayList();
        Iterator iter = new ArrayList(getElements()).iterator();
        while (iter.hasNext()) {
            Object o = iter.next();
            if (o instanceof Item)
                found.add(o);
            if (o instanceof Folder) {
                List items = ((Folder)o).getAllItemsAsList();
                found.addAll(items);
            }
        }
        return found;
    }
    
    
    public Item[] getAllItemsAsArray() {
        List list = getAllItemsAsList();
        return (Item[])list.toArray(new Item[list.size()]);
    }
    
    /*
     * Returns a set of all files in this logical folder as FileObjetc's
     */
    public Set/*<FileObject>*/ getItemsAsFileObjectSet() {
        Vector files = new Vector();
        Iterator iter = new ArrayList(getElements()).iterator();
        while (iter.hasNext()) {
            Item item = (Item)iter.next();
            if (item instanceof Item) {
                FileObject fo  = item.getFileObject();
                if (fo != null)
                    files.add(fo);
            }
        }
        return new LinkedHashSet(files);
    }
    
    /*
     * Returns a set of all files in this logical folder and subfolders as FileObjetc's
     */
    public Set/*<FileObject>*/ getAllItemsAsFileObjectSet(boolean projectFilesOnly) {
        Vector files = new Vector();
        
        if (!projectFilesOnly || isProjectFiles()) {
            Iterator iter = new ArrayList(getElements()).iterator();
            while (iter.hasNext()) {
                Object item = iter.next();
                if (item instanceof Item) {
                    FileObject fo  = ((Item)item).getFileObject();
                    if (fo != null)
                        files.add(fo);
                }
                if (item instanceof Folder) {
                    files.addAll(((Folder)item).getAllItemsAsFileObjectSet(projectFilesOnly));
                }
            }
        }
        
        return new LinkedHashSet(files);
    }
    
    /*
     * Returns a set of all files in this logical folder as FileObjetc's
     */
    public Set/*<DataObject>*/ getItemsAsDataObjectSet(String MIMETypeFilter) {
        Vector files = new Vector();
        Iterator iter = new ArrayList(getElements()).iterator();
        while (iter.hasNext()) {
            Item item = (Item)iter.next();
            if (item instanceof Item) {
                DataObject da  = item.getDataObject();
                if (da != null && (MIMETypeFilter == null || da.getPrimaryFile().getMIMEType().contains(MIMETypeFilter)))
                    files.add(da);
            }
        }
        return new LinkedHashSet(files);
    }
    
    /*
     * Returns a set of all files in this logical folder and subfolders as FileObjetc's
     */
    public Set/*<DataObject>*/ getAllItemsAsDataObjectSet(boolean projectFilesOnly, String MIMETypeFilter) {
        Vector files = new Vector();
        
        if (!projectFilesOnly || isProjectFiles()) {
            Iterator iter = new ArrayList(getElements()).iterator();
            while (iter.hasNext()) {
                Object item = iter.next();
                if (item instanceof Item) {
                    DataObject da  = ((Item)item).getDataObject();
                    if (da != null && (MIMETypeFilter == null || da.getPrimaryFile().getMIMEType().contains(MIMETypeFilter)))
                        files.add(da);
                }
                if (item instanceof Folder) {
                    files.addAll(((Folder)item).getAllItemsAsDataObjectSet(projectFilesOnly, MIMETypeFilter));
                }
            }
        }
        
        return new LinkedHashSet(files);
    }
    
    public String[] getItemNamesAsArray() {
        Item[] items = getItemsAsArray();
        String[] names = new String[items.length];
        for (int i = 0; i < items.length; i++)
            names[i] = items[i].getPath();
        return names;
    }
    
    /*
     * Returns a set of all logical folder in this folder as an array
     */
    public Folder[] getFoldersAsArray() {
        Vector folders = getFolders();
        return (Folder[])folders.toArray(new Folder[folders.size()]);
    }
    
    /*
     * Returns a set of all logical folder in this folder
     */
    public Vector getFolders() {
        Vector folders = new Vector();
        Iterator iter = new ArrayList(getElements()).iterator();
        while (iter.hasNext()) {
            Object item = iter.next();
            if (item instanceof Folder) {
                folders.add(item);
            }
        }
        return folders;
    }
    
    /*
     * Returns a set of all logical folder and subfolders in this folder
     */
    public Vector getAllFolders(boolean projectFilesOnly) {
        Vector folders = new Vector();
        
        if (!projectFilesOnly || isProjectFiles()) {
            Iterator iter = new ArrayList(getElements()).iterator();
            while (iter.hasNext()) {
                Object item = iter.next();
                if (item instanceof Folder) {
                    if (!projectFilesOnly || ((Folder)item).isProjectFiles()) {
                        folders.add(item);
                        folders.addAll(((Folder)item).getAllFolders(projectFilesOnly));
                    }
                }
            }
        }
        
        return folders;
    }
    
    public String[] getFolderNamesAsArray() {
        Folder[] items = getFoldersAsArray();
        String[] names = new String[items.length];
        for (int i = 0; i < items.length; i++)
            names[i] = items[i].getName();
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
            it = new HashSet(changeListenerList).iterator();
        }
        ChangeEvent ev = new ChangeEvent(source);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
        configurationDescriptor.setModified();
    }
    
    static private class MyNativeFileItemSet implements NativeFileItemSet {
        private List<NativeFileItem> items = new ArrayList<NativeFileItem>(1);
        
        public synchronized Collection<NativeFileItem> getItems() {
            return new ArrayList<NativeFileItem>(items);
        }
        public synchronized void add(NativeFileItem item){
            if (!items.contains(item)) {
                items.add(item);
            }
        }
        public synchronized void remove(NativeFileItem item){
            items.remove(item);
        }
        public boolean isEmpty() {
            return items.isEmpty();
        }
    }
    
    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(Folder.class, s);
    }
}
