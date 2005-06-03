/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.util;

import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.netbeans.modules.versioning.system.cvss.CvsFileNode;
import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.FileStatusCache;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.project.*;

import java.io.*;
import java.util.*;
import java.awt.Window;
import java.awt.KeyboardFocusManager;
import java.awt.Dialog;
import java.awt.Frame;

/**
 * @author Maros Sandor
 */
public class Utils {

    /**
     * Determines which nodes the user wants to work on. They are either activated (selected) nodes or
     * all defined versioning roots in a project.
     * 
     * @return array of files/folders to act on
     */ 
    public static File [] getActivatedFiles() {
        Node [] nodes = TopComponent.getRegistry().getActivatedNodes();
        List files = new ArrayList(nodes.length);
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            CvsFileNode cvsNode = (CvsFileNode) node.getLookup().lookup(CvsFileNode.class);
            if (cvsNode != null) {
                files.add(cvsNode.getFile());
                continue;
            }
            Project project =  (Project) node.getLookup().lookup(Project.class);
            if (project != null) {
                addProjectFiles(files, project);
                continue;
            }
            addFileObjects(node, files);
        }
        return (File[]) files.toArray(new File[files.size()]);
    }

    /**
     * @return <code>true</code> if
     * <ul>
     *  <li> the node contains a project in its lookup and
     *  <li> the project contains at least one CVS versioned source group
     * </ul>
     * otherwise <code>false</code>.
     */
    public static boolean isVersionedProject(Node node) {
        Lookup lookup = node.getLookup();
        Project project = (Project) lookup.lookup(Project.class);
        if (project != null) {
            Sources sources = ProjectUtils.getSources(project);
            SourceGroup [] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            for (int j = 0; j < sourceGroups.length; j++) {
                SourceGroup sourceGroup = sourceGroups[j];
                File f = FileUtil.toFile(sourceGroup.getRootFolder());
                if (f != null) {
                    File cvsMeta = new File(f, "CVS");  // NOI18N
                    boolean ret = cvsMeta.isDirectory();
                    if (ret) {
                        return ret;
                    }
                }
            }
        }
        return false;
    }

    private static void addFileObjects(Node node, List files) {
        Collection folders = node.getLookup().lookup(new Lookup.Template(NonRecursiveFolder.class)).allInstances();
        if (folders.size() > 0) {
            for (Iterator j = folders.iterator(); j.hasNext();) {
                NonRecursiveFolder nonRecursiveFolder = (NonRecursiveFolder) j.next();
                files.add(new FlatFolder(FileUtil.toFile(nonRecursiveFolder.getFolder()).getAbsolutePath()));
            }
        } else {
            Collection fileObjects = node.getLookup().lookup(new Lookup.Template(FileObject.class)).allInstances();
            if (fileObjects.size() > 0) {
                files.add(toFileCollection(fileObjects));
            } else {
                DataObject dataObject = (DataObject) node.getCookie(DataObject.class);
                if (dataObject instanceof DataShadow) {
                    dataObject = ((DataShadow) dataObject).getOriginal();
                }
                if (dataObject != null) {
                    FileObject fo = dataObject.getPrimaryFile();
                    File file = FileUtil.toFile(fo);
                    if (file != null) {
                        files.add(file);
                    }
                }
            }
        }
    }

    public static void addProjectFiles(Collection files, Project project) {
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup [] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        for (int j = 0; j < sourceGroups.length; j++) {
            SourceGroup sourceGroup = sourceGroups[j];
            File srcRoot = FileUtil.toFile(sourceGroup.getRootFolder());
            try {
                getCVSRootFor(srcRoot);
                files.add(srcRoot);
            } catch (IOException e) {
                // the folder is not under a versioned root
            }
        }
    }
    
    public static List getProjectsSources(Project [] projects) {
        List roots = new ArrayList();
        for (int i = 0; i < projects.length; i++) {
            addProjectFiles(roots, projects[i]);
        }
        return roots;
    }
    

    private static Collection toFileCollection(Collection fileObjects) {
        Set files = new HashSet(fileObjects.size());
        for (Iterator i = fileObjects.iterator(); i.hasNext();) {
            files.add(FileUtil.toFile((FileObject) i.next()));
        }
        files.remove(null);
        return files;
    }

    public static File [] toFileArray(Collection fileObjects) {
        Set files = new HashSet(fileObjects.size());
        for (Iterator i = fileObjects.iterator(); i.hasNext();) {
            files.add(FileUtil.toFile((FileObject) i.next()));
        }
        files.remove(null);
        return (File[]) files.toArray(new File[files.size()]);
    }

    /**
     * Determines CVS repository root for the given file. It does that by reading the CVS/Root file from 
     * its parent directory, its parent and so on until CVS/Root is found.
     * 
     * @param file the file in question
     * @return CVS root for the given file
     * @throws IOException if CVS/Root file is unreadable
     */ 
    public static String getCVSRootFor(File file) throws IOException {
        if (file.isFile()) file = file.getParentFile();
        for (; file != null; file = file.getParentFile()) {
            File rootFile = new File(file, "CVS/Root");
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(rootFile));
                return br.readLine();
            } catch (FileNotFoundException e) {
                continue;
            } finally {
                if (br != null) br.close();
            }
        }
        throw new IOException("CVS/Root not found");
    }
    
    public static Window getCurrentWindow() {
        Window wnd = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        if (wnd instanceof Dialog || wnd instanceof Frame) {
            return wnd;
        } else {
            return WindowManager.getDefault().getMainWindow();
        }
    }

    public static String getStackTrace() {
        Exception e = new Exception();
        e.fillInStackTrace();
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    /**
     * Tests parent/child relationship of files.
     * 
     * @param parent file to be parent of the second parameter
     * @param file file to be a child of the first parameter
     * @return true if the second parameter represents the same file as the first parameter OR is its descendant (child)
     */ 
    public static boolean isParentOrEqual(File parent, File file) {
        for (; file != null && !file.equals(parent); file = file.getParentFile());
        return file != null;
    }

    /**
     * Computes relative path from the nearest versioned root.
     * TODO: add second argument: File [] roots
     *
     * @param path full path
     * @return String relative path from the nearest versioned root. If this path does not describe a
     * versioned file, this method returns the full path itself. 
     */
    public static String getRelativePath(String path) {
        File file = FileUtil.normalizeFile(new File(path));
        Project p = getProject(file);
        if (p != null) {
            String rootPath = FileUtil.toFile(p.getProjectDirectory()).getAbsolutePath();
            int idx = rootPath.lastIndexOf(File.separatorChar);
            return path.substring(idx == -1 ? 0 : idx + 1);
        } else {
            FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();
            for (file = file.getParentFile(); file != null; file = file.getParentFile()) {
                if (cache.getStatus(file).getStatus() == FileInformation.STATUS_NOTVERSIONED_NOTMANAGED) {
                    int idx = file.getAbsolutePath().lastIndexOf(File.separatorChar);
                    return path.substring(idx == -1 ? 0 : idx + 1);
                }
            }
        }
        return path;
    }
    
    private static Project getProject(File file) {
        FileObject fo = FileUtil.toFileObject(file);
        if (fo == null) return null;
        return FileOwnerQuery.getOwner(fo);
    }

    /**
     * Compares two {@link FileInformation} objects by importance of statuses they represent.
     */ 
    public static class ByImportanceComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FileInformation i1 = (FileInformation) o1;
            FileInformation i2 = (FileInformation) o2;
            return getComparableStatus(i1.getStatus()) - getComparableStatus(i2.getStatus());
        }
    }
    
    /**
     * Splits files/folders into 2 groups: flat folders and other files
     * 
     * @param files array of files to split
     * @return File[][] the first array File[0] contains flat folders, File[1] contains all other files 
     */ 
    public static File[][] splitFlatOthers(File [] files) {
        Set flat = new HashSet(1);
        for (int i = 0; i < files.length; i++) {
            if (files[i] instanceof FlatFolder) {
                flat.add(files[i]);
            }
        }
        if (flat.size() == 0) {
            return new File[][] { new File[0], files };
        } else {
            Set allFiles = new HashSet(Arrays.asList(files));
            allFiles.removeAll(flat);
            return new File[][] { (File[]) flat.toArray(new File[flat.size()]), (File[]) allFiles.toArray(new File[allFiles.size()]) };
        }
    }
    
    /**
     * Gets integer status that can be used in comparators. The more important the status is for the user,
     * the lower value it has. Conflict is 0, unknown status is 100. 
     * 
     * @return status constant suitable for 'by importance' comparators
     */ 
    private static int getComparableStatus(int status) {
        switch (status) {
        case FileInformation.STATUS_VERSIONED_CONFLICT:
            return 0;
        case FileInformation.STATUS_VERSIONED_MERGE:
            return 1;
        case FileInformation.STATUS_VERSIONED_DELETEDLOCALLY:
            return 10;
        case FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY:
            return 11;
        case FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY:
            return 12;
        case FileInformation.STATUS_VERSIONED_ADDEDLOCALLY:
            return 13;
        case FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY:
            return 14;
        case FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY:
            return 30;
        case FileInformation.STATUS_VERSIONED_NEWINREPOSITORY:
            return 31;
        case FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY:
            return 32;
        case FileInformation.STATUS_VERSIONED_UPTODATE:
            return 50;
        case FileInformation.STATUS_NOTVERSIONED_EXCLUDED:
            return 100;
        case FileInformation.STATUS_NOTVERSIONED_NOTMANAGED:
            return 101;
        case FileInformation.STATUS_UNKNOWN:
            return 102;
        default:
            throw new IllegalArgumentException("Unknown status: " + status);
        }
    }
    
}
