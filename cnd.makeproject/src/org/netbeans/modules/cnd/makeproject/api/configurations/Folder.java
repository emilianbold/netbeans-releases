/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;

public class Folder {
    public static final String DEFAULT_FOLDER_NAME = "New Folder";

    private ConfigurationDescriptor configurationDescriptor;
    private String name;
    private String displayName;
    private Folder parent = null;
    private Vector items = null; // Folder or Item
    private Vector changeListenerList = null;
    private boolean projectFiles = true;

    public Folder(ConfigurationDescriptor configurationDescriptor, Folder parent, String name, String displayName, boolean projectFiles) {
	this.configurationDescriptor = configurationDescriptor;
	this.parent = parent;
	this.name = name;
	this.displayName = displayName;
	this.projectFiles = projectFiles;
	items = new Vector();
	changeListenerList = new Vector();
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

    public void setName(String name) {
	this.name = name;
	configurationDescriptor.setModified();
    }

    public String getDisplayName() {
	return displayName;
    }

    public void setDisplayName(String displayName) {
	this.displayName = displayName;
	configurationDescriptor.setModified();
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

    public void addElement(Object element) { // FIXUP: shopuld be private
	// Always keep the vector sorted
	int indexAt = -1;
	if (element instanceof Item) {
	    String name1 = ((Item)element).getSortName();
	    indexAt = 0;
	    while (indexAt < items.size()) {
		Object o = items.elementAt(indexAt);
		if (!(o instanceof Item)) {
		    indexAt++;
		    continue;
		}
		String name2 = ((Item)o).getSortName();
		int compareRes = name1.compareTo(name2);
		if (compareRes > 0) {
		    indexAt++;
		    continue;
		}
		break;
	    }
	}
	else if (element instanceof Folder) {
             if (((Folder)element).isProjectFiles()) {
                 Object lastElement = null;
                 if (items.size() > 0) {
                     lastElement = items.elementAt(items.size()-1);
                     if (lastElement instanceof Folder && !((Folder)lastElement).isProjectFiles()) {
                        indexAt = items.size() - 1;
                     }
                 }
             }
        }
	if (indexAt >= 0) {
	    items.add(indexAt, element);
        }
        else {
	    items.add(element);
        }
	fireChangeEvent();
    }

    public void addItem(Item item) {
	if (item == null)
	    return;
	// Check if already in project. Silently ignore if already there.
	if (isProjectFiles() && ((MakeConfigurationDescriptor)configurationDescriptor).findProjectItemByPath(item.getPath()) != null) {
	    System.err.println("Folder - addItem - item ignored, already added: " + item);
	    return;
	}
	// Add it to the folder
	item.setFolder(this);
	addElement(item);
	// Add it to project Items
	if (isProjectFiles()) {
	    ((MakeConfigurationDescriptor)configurationDescriptor).addProjectItem(item);
	    // Add configuration to all configurations
	    if (configurationDescriptor.getConfs() == null)
		return;
	    Configuration[] configurations = configurationDescriptor.getConfs().getConfs();
	    for (int i = 0; i < configurations.length; i++)
		configurations[i].addAuxObject(new ItemConfiguration(configurations[i], item));
	}
    }

    public void addFolder(Folder folder) {
	addElement(folder);
    }

    public Folder addNewFolder(boolean projectFiles) {
	String name = DEFAULT_FOLDER_NAME;
	for (int i = 1;; i++) {
	    name = DEFAULT_FOLDER_NAME + " " + i; // NOI18N
	    if (findFolderByName(name) == null)
		break;
	}
	return addNewFolder(name, name, projectFiles);
    }

    public Folder addNewFolder(String name, String displayName, boolean projectFiles) {
	Folder newFolder = new Folder(getConfigurationDescriptor(), this, name, displayName, projectFiles);
	addFolder(newFolder);
	return newFolder;
    }

    public boolean removeItem(Item item) {
	boolean ret = false;
	if (item == null)
	    return false;
	// Remove it from folder
	ret = items.removeElement(item);
	if (!ret)
	    return ret;
//	item.setFolder(null);
	if (isProjectFiles()) {
	    // Remove it from project Items
	    ((MakeConfigurationDescriptor)configurationDescriptor).removeProjectItem(item);
	    // Remove it form all configurations
	    Configuration[] configurations = configurationDescriptor.getConfs().getConfs();
	    for (int i = 0; i < configurations.length; i++)
		configurations[i].removeAuxObject(ItemConfiguration.getId(item.getPath()));
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

    public boolean removeFolder(Folder folder) {
	boolean ret = false;
	if (folder != null) {
	    folder.removeAll();
	    ret = items.removeElement(folder);
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
	if (getName().equals(name))
	    return this;
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
	if (getDisplayName().equals(name))
	    return this;
	Folder[] folders = getFoldersAsArray();
	for (int i = 0; i < folders.length; i++) {
	    if (name.equals(folders[i].getDisplayName()))
		return folders[i];
	}
	return null;
    }

    public Item[] getItemsAsArray() {
	int count = 0;
	Iterator iter = getElements().iterator();
        while (iter.hasNext()) {
	    if (iter.next() instanceof Item)
		count++;
        }
	Item[] items = new Item[count];
	int i = 0;
	iter = getElements().iterator();
        while (iter.hasNext()) {
	    Object o = iter.next();
	    if (o instanceof Item)
		items[i++] = (Item)o;
        }
	return items; // FIXUP
    }

    public String[] getItemNamesAsArray() {
	Item[] items = getItemsAsArray();
	String[] names = new String[items.length];
	for (int i = 0; i < items.length; i++)
	    names[i] = items[i].getPath();
	return names;
    }
    
    public Folder[] getFoldersAsArray() {
	int count = 0;
	Iterator iter = getElements().iterator();
        while (iter.hasNext()) {
	    if (iter.next() instanceof Folder)
		count++;
        }
	Folder[] items = new Folder[count];
	int i = 0;
	iter = getElements().iterator();
        while (iter.hasNext()) {
	    Object o = iter.next();
	    if (o instanceof Folder)
		items[i++] = (Folder)o;
        }
	return items; // FIXUP
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

    public void fireChangeEvent() {
        Iterator it;
       
        synchronized (changeListenerList) {
            it = new HashSet(changeListenerList).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
	configurationDescriptor.setModified();
    }
}
