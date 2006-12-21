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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.makeproject.configurations.ConfigurationMakefileWriter;
import org.netbeans.modules.cnd.makeproject.configurations.ConfigurationXMLWriter;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.NativeProjectProvider;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.makeproject.ui.wizards.FolderEntry;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

public class MakeConfigurationDescriptor extends ConfigurationDescriptor {
    public static final String EXTERNAL_FILES_FOLDER = "ExternalFiles";
    public static final String SOURCE_FILES_FOLDER = "SourceFiles";
    public static final String HEADER_FILES_FOLDER = "HeaderFiles";
    public static final String RESOURCE_FILES_FOLDER = "ResourceFiles";
    
    public static final String ICONBASE = "org/netbeans/modules/cnd/makeproject/ui/resources/makeProject";
    public static final String ICON = "org/netbeans/modules/cnd/makeproject/ui/resources/makeProject.gif";
    public static final Icon MAKEFILE_ICON = new ImageIcon(Utilities.loadImage(ICON)); // NOI18N
    
    private Project project = null;
    private String baseDir;
    
    private boolean modified = false;
    
    private Folder externalFileItems = null;
    private Folder rootFolder = null;
    private HashMap projectItems = null;
    private Vector projectItemsChangeListeners = null;
    private NativeProject nativeProject = null;
    public static String DEFAULT_PROJECT_MAKFILE_NAME = "Makefile"; // NOI18N
    private String projectMakefileName = DEFAULT_PROJECT_MAKFILE_NAME;
    
    public MakeConfigurationDescriptor(String baseDir) {
        super();
        this.baseDir = baseDir;
        rootFolder = new Folder(this, null, "root", "root", true);
        projectItems = new HashMap();
        projectItemsChangeListeners = new Vector();
        setModified(true);
    }
    
    public static MakeConfigurationDescriptor getMakeConfigurationDescriptor(Project project) {
        ConfigurationDescriptorProvider pdp = (ConfigurationDescriptorProvider)project.getLookup().lookup(ConfigurationDescriptorProvider.class );
        if (pdp != null) {
            MakeConfigurationDescriptor makeConfigurationDescriptor = (MakeConfigurationDescriptor)pdp.getConfigurationDescriptor();
            return makeConfigurationDescriptor;
        } else {
            return null;
        }
    }
    
    public Project getProject() {
        if (project == null) {
            String location = FilePathAdaptor.mapToLocal(getBaseDir()); // PC path
            try {
                FileObject fo = FileUtil.toFileObject(new File(location).getCanonicalFile());
                project = ProjectManager.getDefault().findProject(fo);
            } catch (Exception e) {
                // Should not happen
                System.err.println(e); // FIXUP
            }
        }
        return project;
    }
    
    public void init(Configuration def) {
        super.init(new Configuration[] {def}, 0);
        setModified(true);
    }
    
    public void initLogicalFolders(Iterator sourceFileFolders, boolean createLogicalFolders, Iterator importantItems) {
        if (createLogicalFolders) {
            rootFolder.addNewFolder(SOURCE_FILES_FOLDER, "Source Files", true);
            rootFolder.addNewFolder(HEADER_FILES_FOLDER, "Header Files", true);
            rootFolder.addNewFolder(RESOURCE_FILES_FOLDER, "Resource Files", true);
        }
        externalFileItems = rootFolder.addNewFolder(EXTERNAL_FILES_FOLDER, "Important Files", false);
//        if (sourceFileFolders != null)
//            setExternalFileItems(sourceFileFolders); // From makefile wrapper wizard
        externalFileItems.addItem(new Item(getProjectMakefileName())); // NOI18N
        if (importantItems != null) {
            while (importantItems.hasNext()) {
                externalFileItems.addItem(new Item((String)importantItems.next()));
            }
        }
        addSourceFilesFromFolders(sourceFileFolders, false);
        setModified(true);
    }
    
    public String getProjectMakefileName() {
        return projectMakefileName;
    }
    
    public void setProjectMakefileName(String projectMakefileName) {
        this.projectMakefileName = projectMakefileName;
    }
    
    /**
     * @deprecated. Use org.netbeans.modules.cnd.api.project.NativeProject interface instead.
     */
    public void addProjectItemsChangeListener(ChangeListener cl) {
        synchronized (projectItemsChangeListeners) {
            projectItemsChangeListeners.add(cl);
        }
    }
    
    /**
     * @deprecated. Use org.netbeans.modules.cnd.api.project.NativeProject interface instead.
     */
    public void removeProjectItemsChangeListener(ChangeListener cl) {
        synchronized (projectItemsChangeListeners) {
            projectItemsChangeListeners.remove(cl);
        }
    }
    
    public void fireProjectItemsChangeEvent(Item item, int action) {
        Iterator it;
        
        synchronized (projectItemsChangeListeners) {
            it = new HashSet(projectItemsChangeListeners).iterator();
        }
        ChangeEvent ev = new ProjectItemChangeEvent(this, item, action);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }
    
    public Vector getProjectItemsChangeListeners() {
        return projectItemsChangeListeners;
    }
    
    public void setProjectItemsChangeListeners(Vector projectItemsChangeListeners) {
        this.projectItemsChangeListeners = projectItemsChangeListeners;
    }
    
    public String getBaseDir() {
        return baseDir;
    }
    
    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }
    
    public HashMap getProjectItemsMap() {
        return projectItems;
    }
    
    public void setProjectItemsMap(HashMap projectItems) {
        this.projectItems = projectItems;
    }
    
    public void init(Configuration[] confs) {
        super.init(confs, 0);
    }
    
    public Icon getIcon() {
        return MAKEFILE_ICON;
    }
    
    public Configuration defaultConf(String name, int type) {
        MakeConfiguration c = new MakeConfiguration(this, name, type);
        Item[] items = getProjectItems();
        for (int i = 0; i < items.length; i++)
            c.addAuxObject(new ItemConfiguration(c, items[i]));
        return c;
    }
    
    // External File Items
    public void setExternalFileItems(Vector items) {
        externalFileItems.reset();
        for (Enumeration e = items.elements() ; e.hasMoreElements() ;) {
            externalFileItems.addItem(new Item((String)e.nextElement()));
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
        Collection collection = projectItems.values();
        return (Item[])collection.toArray(new Item[collection.size()]);
    }
    
    public Item findItemByFile(File file) {
        Collection coll = projectItems.values();
        Iterator it = coll.iterator();
        while (it.hasNext()) {
            Item item = (Item)it.next();
            File itemFile = item.getFile();
            if (itemFile == file || itemFile.getPath().equals(file.getPath()))
                return item;
        }
        return null;
    }
    
    public Item findProjectItemByPath(String path) {
        // Try first as-is
        Item item = (Item)projectItems.get(path);
        if (item == null) {
            // Then try absolute if relative or relative if absolute
            String newPath;
            if (IpeUtils.isPathAbsolute(path))
                newPath = IpeUtils.toRelativePath(getBaseDir(), FilePathAdaptor.naturalize(path));
            else
                newPath = IpeUtils.toAbsolutePath(getBaseDir(), path);
            item = (Item)projectItems.get(newPath);
        }
        return item;
    }
    
    public Item findExternalItemByPath(String path) {
        // Try first as-is
        Item item = (Item)externalFileItems.findItemByPath(path);
        if (item == null) {
            // Then try absolute if relative or relative if absolute
            String newPath;
            if (IpeUtils.isPathAbsolute(path))
                newPath = IpeUtils.toRelativePath(getBaseDir(), path);
            else
                newPath = IpeUtils.toAbsolutePath(getBaseDir(), path);
            item = (Item)(Item)externalFileItems.findItemByPath(newPath);
        }
        return item;
    }
    
    public void addProjectItem(Item item) {
        projectItems.put(item.getPath(), item);
        fireProjectItemsChangeEvent(item, ProjectItemChangeEvent.ITEM_ADDED);
        getNativeProject().fireFileAdded(item);
        setModified(true);
    }
    
    public void removeProjectItem(Item item) {
        projectItems.remove(item.getPath());
        fireProjectItemsChangeEvent(item, ProjectItemChangeEvent.ITEM_REMOVED);
        getNativeProject().fireFileRemoved(item);
        setModified(true);
    }
    
    public void checkForChangedItems() {
        getNativeProject().checkForChangedItems();
    }
    
    
    public void copyFromProjectDescriptor(ConfigurationDescriptor copyProjectDescriptor) {
        MakeConfigurationDescriptor copyExtProjectDescriptor = (MakeConfigurationDescriptor)copyProjectDescriptor;
        setConfs(copyExtProjectDescriptor.getConfs());
        setBaseDir(copyProjectDescriptor.getBaseDir());
        setProjectMakefileName(copyExtProjectDescriptor.getProjectMakefileName());
        setExternalFileItems(copyExtProjectDescriptor.getExternalFileItems());
        setLogicalFolders(copyExtProjectDescriptor.getLogicalFolders());
        setProjectItemsMap(((MakeConfigurationDescriptor)copyProjectDescriptor).getProjectItemsMap());
        setProjectItemsChangeListeners(((MakeConfigurationDescriptor)copyProjectDescriptor).getProjectItemsChangeListeners());
    }
    
    public void assign(ConfigurationDescriptor clonedConfigurationDescriptor) {
        Configuration[] clonedConfs = clonedConfigurationDescriptor.getConfs().getConfs();
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
        setProjectMakefileName(((MakeConfigurationDescriptor)clonedConfigurationDescriptor).getProjectMakefileName());
        setExternalFileItems(((MakeConfigurationDescriptor)clonedConfigurationDescriptor).getExternalFileItems());
        setLogicalFolders(((MakeConfigurationDescriptor)clonedConfigurationDescriptor).getLogicalFolders());
        setProjectItemsMap(((MakeConfigurationDescriptor)clonedConfigurationDescriptor).getProjectItemsMap());
        setProjectItemsChangeListeners(((MakeConfigurationDescriptor)clonedConfigurationDescriptor).getProjectItemsChangeListeners());
    }
    
    public ConfigurationDescriptor cloneProjectDescriptor() {
        MakeConfigurationDescriptor clone = new MakeConfigurationDescriptor(getBaseDir());
        super.cloneProjectDescriptor(clone);
        clone.setProjectMakefileName(getProjectMakefileName());
        clone.setExternalFileItems(getExternalFileItems());
        clone.setLogicalFolders(getLogicalFolders());
        clone.setProjectItemsMap(getProjectItemsMap());
        clone.setProjectItemsChangeListeners(getProjectItemsChangeListeners());
        return clone;
    }
    
    public boolean getModified() {
        return modified;
    }
    
    public void setModified() {
        setModified(true);
    }
    
    public void setModified(boolean modified) {
        //System.out.println("setModified - " + modified);
        this.modified = modified;
    }
    
    public boolean save() {
        return save(null);
    }
    
    public boolean save(String extraMessage) {
        // First check all configurations aux objects if they have changed
        Configuration[] configurations = getConfs().getConfs();
        for (int i = 0; i < configurations.length; i++) {
            Configuration conf = configurations[i];
            ConfigurationAuxObject[] auxObjects = conf.getAuxObjects();
            for (int j = 0; j < auxObjects.length; j++) {
                if (auxObjects[j].hasChanged()) {
                    setModified(true);
                }
                auxObjects[j].clearChanged();
            }
        }
        
        if (!getModified())
            return true;
        
        // Check metadata files are writable
        Vector metadataFiles = new Vector();
        Vector notOkFiles = new Vector();
        metadataFiles.add(getBaseDir() + File.separator + "nbproject" + File.separator + "project.xml");
        metadataFiles.add(getBaseDir() + File.separator + "nbproject" + File.separator + "configurations.xml");
        metadataFiles.add(getBaseDir() + File.separator + "nbproject" + File.separator + "Makefile-impl.mk");
        Configuration[] confs = getConfs().getConfs();
        for (int i = 0; i < confs.length; i++)
            metadataFiles.add(getBaseDir() + File.separator + "nbproject" + File.separator + "Makefile-" + confs[i].getName() + ".mk");
        boolean allOk = true;
        for (int i = 0; i < metadataFiles.size(); i++) {
            File file = new File((String)metadataFiles.elementAt(i));
            if (!file.exists())
                continue;
            if (!file.canWrite()) {
                allOk = false;
                notOkFiles.add(metadataFiles.elementAt(i));
            }
        }
        if (!allOk) {
            String projectName = IpeUtils.getBaseName(getBaseDir());
            String text = "The project '" + projectName + "' cannot be saved because the following files are write-protected:";
            for (int i = 0; i < notOkFiles.size(); i++) {
                text += "\n" + notOkFiles.elementAt(i);
            }
            if (extraMessage != null) {
                text += "\n\n" + extraMessage;
            }
            NotifyDescriptor d = new NotifyDescriptor.Message(text, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return allOk;
        }
        
        // ALl OK
        FileObject fo = null;
        fo = FileUtil.toFileObject(new File(getBaseDir()));
        if (fo != null) {
            new ConfigurationXMLWriter(fo, this).write();
            new ConfigurationMakefileWriter(this).write();
        }
        
        // Clear flag
        setModified(false);
        
        return allOk;
    }
    
    private NativeProjectProvider getNativeProject() {
        if (nativeProject == null) {
            FileObject fo = FileUtil.toFileObject(new File(baseDir));
            try {
                Project project = ProjectManager.getDefault().findProject(fo);
                nativeProject = (NativeProject)project.getLookup().lookup(NativeProject.class);
            } catch (IOException ioe) {
            }
            
        }
        return (NativeProjectProvider)nativeProject;
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
    
    public void addSourceFilesFromFolders(Iterator sourceFileFolders, boolean acrynchron) {
        addSourceFilesFromFolders(rootFolder, sourceFileFolders, acrynchron);
    }
    
    public void addSourceFilesFromFolders(Folder folder, Iterator sourceFileFoldersIterator, boolean acrynchron) {
        if (sourceFileFoldersIterator == null)
            return;
        if (acrynchron)
            new AddFilesThread(sourceFileFoldersIterator, folder).start();
        else {
            while (sourceFileFoldersIterator.hasNext()) {
                FolderEntry folderEntry = (FolderEntry)sourceFileFoldersIterator.next();
                Folder top = new Folder(folder.getConfigurationDescriptor(), folder, folderEntry.getFile().getName(), folderEntry.getFile().getName(), true);
                addFiles(top, folderEntry.getFile(), folderEntry.isAddSubfoldersSelected(), folderEntry.getFileFilter(), null);
                folder.addFolder(top);
            }
        }
    }
    
    class AddFilesThread extends Thread {
        Iterator iterator;
        Folder folder;
        private ProgressHandle handle;
        
        AddFilesThread(Iterator iterator, Folder folder) {
            this.iterator = iterator;
            this.folder = folder;
            handle = ProgressHandleFactory.createHandle("Adding files...");
        }
        public void run() {
            handle.setInitialDelay(500);
            handle.start();
            while (iterator.hasNext()) {
                FolderEntry folderEntry = (FolderEntry)iterator.next();
                Folder top = new Folder(folder.getConfigurationDescriptor(), folder, folderEntry.getFile().getName(), folderEntry.getFile().getName(), true);
                addFiles(top, folderEntry.getFile(), folderEntry.isAddSubfoldersSelected(), folderEntry.getFileFilter(), handle);
                folder.addFolder(top);
            }
            handle.finish();
        }
    }
    
    private void addFiles(Folder folder, File dir, boolean addSubFolders, FileFilter filter, ProgressHandle handle) {
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (!filter.accept(files[i]))
                continue;
            if (files[i].isDirectory()) {
                // FIXUP: is this the best way to deal with files under SCCS?
                // Unfortunately the SCCS directory contains data files with the same
                // suffixes as the the source files, and a simple file filter based on
                // a file's suffix cannot see the difference between the source file and
                // the data file. Only the source file should be added.
                if (files[i].getName().equals("SCCS")) // NOI18N
                    continue;
                Folder dirfolder = folder;
                if (addSubFolders) {
                    dirfolder = folder.addNewFolder(files[i].getName(), files[i].getName(), true);
                }
                addFiles(dirfolder, files[i], addSubFolders, filter, handle);
                if (dirfolder.size() == 0)
                    folder.removeFolder(dirfolder);
            } else {
                String filePath = IpeUtils.toRelativePath(baseDir, files[i].getPath());
                folder.addItem(new Item(FilePathAdaptor.normalize(filePath)));
                if (handle != null) {
                    handle.progress(filePath);
                }
            }
        }
    }
}
