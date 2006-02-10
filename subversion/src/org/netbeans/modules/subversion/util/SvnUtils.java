/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.subversion.util;

import org.netbeans.modules.subversion.client.SvnClient;
import org.openide.*;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.Lookup;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.netbeans.api.project.*;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.modules.subversion.FileStatusCache;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.FileInformation;

import java.io.*;
import java.util.*;
import java.awt.Window;
import java.awt.KeyboardFocusManager;
import java.awt.Dialog;
import java.awt.Frame;
import org.tigris.subversion.svnclientadapter.*;

/**
 * Subversion-specific utilities.
 * TODO: Move generic methods to versioncontrol module 
 * 
 * @author Maros Sandor
 */
public class SvnUtils {

    private static Node []  contextNodesCached;
    private static Context  contextCached;

    /**
     * Semantics is similar to {@link org.openide.windows.TopComponent#getActivatedNodes()} except that this
     * method returns File objects instead od Nodes. Every node is examined for Files it represents. File and Folder
     * nodes represent their underlying files or folders. Project nodes are represented by their source groups. Other
     * logical nodes must provide FileObjects in their Lookup.
     *   
     * @return File [] array of activated files
     * @param nodes or null (then taken from windowsystem, it may be wrong on editor tabs #66700).
     */ 
    public static Context getCurrentContext(Node[] nodes) {
        if (nodes == null) {
            nodes = TopComponent.getRegistry().getActivatedNodes();
        }
        if (Arrays.equals(contextNodesCached, nodes)) return contextCached;
        List files = new ArrayList(nodes.length);
        List rootFiles = new ArrayList(nodes.length);
        List rootFileExclusions = new ArrayList(5);
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
/*
            CvsFileNode cvsNode = (CvsFileNode) node.getLookup().lookup(CvsFileNode.class);
            if (cvsNode != null) {
                files.add(cvsNode.getFile());
                rootFiles.add(cvsNode.getFile());
                continue;
            }
*/
            Project project =  (Project) node.getLookup().lookup(Project.class);
            if (project != null) {
                addProjectFiles(files, rootFiles, rootFileExclusions, project);
                continue;
            }
            addFileObjects(node, files, rootFiles);
        }
        
        contextCached = new Context(files, rootFiles, rootFileExclusions);
        contextNodesCached = nodes;
        return contextCached;
    }

    
    /**
     * Semantics is similar to {@link org.openide.windows.TopComponent#getActivatedNodes()} except that this
     * method returns File objects instead od Nodes. Every node is examined for Files it represents. File and Folder
     * nodes represent their underlying files or folders. Project nodes are represented by their source groups. Other
     * logical nodes must provide FileObjects in their Lookup.
     *
     * @param nodes null (then taken from windowsystem, it may be wrong on editor tabs #66700).
     * @param includingFileStatus if any activated file does not have this CVS status, an empty array is returned
     * @param includingFolderStatus if any activated folder does not have this CVS status, an empty array is returned
     * @return File [] array of activated files, or an empty array if any of examined files/folders does not have given status
     */ 
    public static Context getCurrentContext(Node[] nodes, int includingFileStatus, int includingFolderStatus) {
        Context context = getCurrentContext(nodes);
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        File [] files = context.getRootFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            FileInformation fi = cache.getStatus(file);
            if (file.isDirectory()) {
                if ((fi.getStatus() & includingFolderStatus) == 0) return Context.Empty;
            } else {
                if ((fi.getStatus() & includingFileStatus) == 0) return Context.Empty;
            }
        }
        return context;
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
        return isVersionedProject(project);
    }

    /**
     * @return <code>true</code> if
     * <ul>
     *  <li> the project != null and
     *  <li> the project contains at least one CVS versioned source group
     * </ul>
     * otherwise <code>false</code>.
     */
    public static boolean isVersionedProject(Project project) {
        if (project != null) {
            FileStatusCache cache = Subversion.getInstance().getStatusCache();
            Sources sources = ProjectUtils.getSources(project);
            SourceGroup [] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            for (int j = 0; j < sourceGroups.length; j++) {
                SourceGroup sourceGroup = sourceGroups[j];
                File f = FileUtil.toFile(sourceGroup.getRootFolder());
                if (f != null) {                                                                    
// XXX                    if ((cache.getStatus(f).getStatus() & FileInformation.STATUS_MANAGED) != 0) return true;
                    File probe = new File (f, ".svn");
                    File probe2 = new File (f, "_svn");
                    if (probe.isDirectory() || probe2.isDirectory()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void addFileObjects(Node node, List files, List rootFiles) {
        Collection folders = node.getLookup().lookup(new Lookup.Template(NonRecursiveFolder.class)).allInstances();
        List nodeFiles = new ArrayList();
        if (folders.size() > 0) {
            for (Iterator j = folders.iterator(); j.hasNext();) {
                NonRecursiveFolder nonRecursiveFolder = (NonRecursiveFolder) j.next();
                nodeFiles.add(new FlatFolder(FileUtil.toFile(nonRecursiveFolder.getFolder()).getAbsolutePath()));
            }
        } else {
            Collection fileObjects = node.getLookup().lookup(new Lookup.Template(FileObject.class)).allInstances();
            if (fileObjects.size() > 0) {
                nodeFiles.addAll(toFileCollection(fileObjects));
            } else {
                DataObject dataObject = (DataObject) node.getCookie(DataObject.class);
                if (dataObject instanceof DataShadow) {
                    dataObject = ((DataShadow) dataObject).getOriginal();
                }
                if (dataObject != null) {
                    Collection doFiles = toFileCollection(dataObject.files());
                    nodeFiles.addAll(doFiles);
                }
            }
        }
        files.addAll(nodeFiles);
        rootFiles.addAll(nodeFiles);
    }

    /**
     * Determines all files and folders that belong to a given project and adds them to the supplied Collection.
     *
     * @param filteredFiles destination collection of Files
     * @param project project to examine
     */
    public static void addProjectFiles(Collection filteredFiles, Collection rootFiles, Collection rootFilesExclusions, Project project) {
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup [] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        for (int j = 0; j < sourceGroups.length; j++) {
            SourceGroup sourceGroup = sourceGroups[j];
            FileObject srcRootFo = sourceGroup.getRootFolder();
            File rootFile = FileUtil.toFile(srcRootFo);
            if ((cache.getStatus(rootFile).getStatus() & FileInformation.STATUS_MANAGED) == 0) continue; 
            rootFiles.add(rootFile);
            boolean containsSubprojects = false;
            FileObject [] rootChildren = srcRootFo.getChildren();
            Set projectFiles = new HashSet(rootChildren.length);
            for (int i = 0; i < rootChildren.length; i++) {
                FileObject rootChildFo = rootChildren[i];
                if (Subversion.getInstance().isAdministrative(rootChildFo.getNameExt())) continue;
                File child = FileUtil.toFile(rootChildFo);
                if (sourceGroup.contains(rootChildFo)) {
                    // TODO: #60516 deep scan is required here but not performed due to performace reasons 
                    projectFiles.add(child);
                } else {
                    int status = cache.getStatus(child).getStatus();
                    if (status != FileInformation.STATUS_NOTVERSIONED_EXCLUDED) {
                        rootFilesExclusions.add(child);
                        containsSubprojects = true;
                    }
                }
            }
            if (containsSubprojects) {
                filteredFiles.addAll(projectFiles);
            } else {
                filteredFiles.add(rootFile);
            }
        }
    }

    /**
     * May take a long time for many projects, consider making the call from worker threads.
     * 
     * @param projects projects to examine
     * @return Context context that defines list of supplied projects
     */ 
    public static Context getProjectsContext(Project [] projects) {
        List filtered = new ArrayList(); 
        List roots = new ArrayList();
        List exclusions = new ArrayList(); 
        for (int i = 0; i < projects.length; i++) {
            addProjectFiles(filtered, roots, exclusions, projects[i]);
        }
        return new Context(filtered, roots, exclusions);
    }

    private static Collection toFileCollection(Collection fileObjects) {
        Set files = new HashSet(fileObjects.size()*4/3+1);
        for (Iterator i = fileObjects.iterator(); i.hasNext();) {
            files.add(FileUtil.toFile((FileObject) i.next()));
        }
        files.remove(null);
        return files;
    }

    public static File [] toFileArray(Collection fileObjects) {
        Set files = new HashSet(fileObjects.size()*4/3+1);
        for (Iterator i = fileObjects.iterator(); i.hasNext();) {
            files.add(FileUtil.toFile((FileObject) i.next()));
        }
        files.remove(null);
        return (File[]) files.toArray(new File[files.size()]);
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
        for (; file != null; file = file.getParentFile()) {
            if (file.equals(parent)) return true;
        }
        return false;
    }

    /**
     * Computes previous revision or <code>null</code>
     * for initial.
     *
     * @param revision num.dot revision or <code>null</code>
     */
    public static String previousRevision(String revision) {
        if (revision == null) return null;
        String[] nums = revision.split("\\.");  // NOI18N
        assert (nums.length % 2) == 0 : "File revisions must consist from even tokens: " + revision; // NOI18N

        // eliminate branches
        int lastIndex = nums.length -1;
        boolean cutoff = false;
        while (lastIndex>1 && "1".equals(nums[lastIndex])) { // NOI18N
            lastIndex -= 2;
            cutoff = true;
        }
        if (lastIndex <= 0) {
            return null;
        } else if (lastIndex == 1 && "1".equals(nums[lastIndex])) { // NOI18N
            return null;
        } else {
            int rev = Integer.parseInt(nums[lastIndex]);
            if (!cutoff) rev--;
            StringBuffer sb = new StringBuffer(nums[0]);
            for (int i = 1; i<lastIndex; i++) {
                sb.append('.').append(nums[i]); // NOI18N
            }
            sb.append('.').append(rev);  // NOI18N
            return sb.toString();
        }
    }

    /**
     * Determines parent project for a file.
     *
     * @param file file to examine
     * @return Project owner of the file or null if the file does not belong to a project
     */
    public static Project getProject(File file) {
        if (file == null) return null;
        FileObject fo = FileUtil.toFileObject(file);
        if (fo == null) return getProject(file.getParentFile());
        return FileOwnerQuery.getOwner(fo);
    }

    /**
     * Recursively deletes all files and directories under a given file/directory.
     *
     * @param file file/directory to delete
     */
    public static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File [] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteRecursively(files[i]);
            }
        }
        file.delete();
    }
    
    /**
     * Searches for common filesystem parent folder for given files.
     * 
     * @param a first file
     * @param b second file
     * @return File common parent for both input files with the longest filesystem path or null of these files
     * have not a common parent
     */ 
    public static File getCommonParent(File a, File b) {
        for (;;) {
            if (a.equals(b)) {
                return a;
            } else if (a.getAbsolutePath().length() > b.getAbsolutePath().length()) {
                a = a.getParentFile();
                if (a == null) return null;
            } else {
                b = b.getParentFile();
                if (b == null) return null;
            }
        }
    }

    /**
     * Translate to relative path to repository root
     *
     * @return relative repositpry path or null for unknown
     */
    public static String getRelativePath(File file) {
        String repositoryPath = null;
        try {
            SvnClient client = Subversion.getInstance().getClient();
            ISVNInfo info = client.getInfoFromWorkingCopy(file);  // XXX info does not contain repository URL
            SVNUrl fileURL = info.getUrl();
//            SVNUrl repositoryURL = client.getRepositoryRoot(fileURL);
//            String fileLink = fileURL.toString();
//            String repositoryLink = repositoryURL.toString();
//            repositoryPath = fileLink.substring(repositoryLink.length());
            repositoryPath = "";
            String[] segments = fileURL.getPathSegments();
            for (int i = 0; i<segments.length; i++) {
                repositoryPath += "/" + segments[i];
            }
        } catch (SVNClientException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
                
        return repositoryPath;
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
    public static int getComparableStatus(int status) {
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
            throw new IllegalArgumentException("Unknown status: " + status); // NOI18N
        }
    }

    /**
     * Copies the specified sourceFile to the specified targetFile.
     */
    public static void copyFile(File sourceFile, File targetFile) throws IOException {
        if (sourceFile == null || targetFile == null) {
            throw new NullPointerException("sourceFile and targetFile must not be null"); // NOI18N
        }

        // ensure existing parent directories
        File directory = targetFile.getParentFile();
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Could not create directory '" + directory + "'"); // NOI18N
        }

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(sourceFile));
            outputStream = new BufferedOutputStream(new FileOutputStream(targetFile));

            try {
                byte[] buffer = new byte[32768];
                for (int readBytes = inputStream.read(buffer);
                     readBytes > 0;
                     readBytes = inputStream.read(buffer)) {
                    outputStream.write(buffer, 0, readBytes);
                }
            }
            catch (IOException ex) {
                targetFile.delete();
                throw ex;
            }
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (IOException ex) {
                    // ignore
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                }
                catch (IOException ex) {
                    // ignore
                }
            }
        }
    }    

    public static String getConfigDir() {
        String nbHome = System.getProperty("netbeans.user");       
        return nbHome + "/config/svn/config/";
    }    
}
