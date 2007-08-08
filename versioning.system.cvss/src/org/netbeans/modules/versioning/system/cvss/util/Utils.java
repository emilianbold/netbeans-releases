/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.versioning.system.cvss.util;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.lib.cvsclient.admin.Entry;
import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.FileStatusCache;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Provides static utility methods for CVS module.
 * 
 * @author Maros Sandor
 */
public class Utils {

    private static final Pattern metadataPattern = Pattern.compile(".*\\" + File.separatorChar + "CVS(\\" + File.separatorChar + ".*|$)");    
    
    private static final FileFilter cvsFileFilter = new FileFilter() {
        public boolean accept(File pathname) {
            if (CvsVersioningSystem.FILENAME_CVS.equals(pathname.getName())) return false;
            if (CvsVersioningSystem.FILENAME_CVSIGNORE.equals(pathname.getName())) return true;
            return SharabilityQuery.getSharability(pathname) != SharabilityQuery.NOT_SHARABLE;
        }
    };
    
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
        VCSContext ctx = VCSContext.forNodes(nodes);
        return new Context(new HashSet(ctx.computeFiles(cvsFileFilter)), new HashSet(ctx.getRootFiles()), new HashSet(ctx.getExclusions()));  
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
        FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();
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
        // if there are no exclusions, we may safely return this context because filtered files == root files
        if (context.getExclusions().size() == 0) return context;

        // in this code we remove files from filteredFiles to NOT include any files that do not have required status
        // consider a freeform project that has 'build' in filteredFiles, the Branch action would try to branch it
        // so, it is OK to have BranchAction enabled but the context must be a bit adjusted here
        Set<File> filteredFiles = new HashSet<File>(Arrays.asList(context.getFiles()));
        Set<File> rootFiles = new HashSet<File>(Arrays.asList(context.getRootFiles()));
        Set<File> rootFileExclusions = new HashSet<File>(context.getExclusions());

        for (Iterator<File> i = filteredFiles.iterator(); i.hasNext(); ) {
            File file = i.next();
            if (file.isDirectory()) {
                if ((cache.getStatus(file).getStatus() & includingFolderStatus) == 0) i.remove();          
            } else {
                if ((cache.getStatus(file).getStatus() & includingFileStatus) == 0) i.remove();          
            }
        }
        return new Context(filteredFiles, rootFiles, rootFileExclusions);
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
        Project project = lookup.lookup(Project.class);
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
            FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();
            Sources sources = ProjectUtils.getSources(project);
            SourceGroup [] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            for (int j = 0; j < sourceGroups.length; j++) {
                SourceGroup sourceGroup = sourceGroups[j];
                File f = FileUtil.toFile(sourceGroup.getRootFolder());
                if (f != null) {
                    if ((cache.getStatus(f).getStatus() & FileInformation.STATUS_MANAGED) != 0) return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines all files and folders that belong to a given project and adds them to the supplied Collection.
     *
     * @param filteredFiles destination collection of Files
     * @param project project to examine
     */
    public static void addProjectFiles(Collection filteredFiles, Collection rootFiles, Collection rootFilesExclusions, Project project) {
        FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup [] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        for (int j = 0; j < sourceGroups.length; j++) {
            SourceGroup sourceGroup = sourceGroups[j];
            FileObject srcRootFo = sourceGroup.getRootFolder();
            File rootFile = FileUtil.toFile(srcRootFo);
            try {
                getCVSRootFor(rootFile);
            } catch (IOException e) {
                // the folder is not under a versioned root
                continue;
            }
            rootFiles.add(rootFile);
            boolean containsSubprojects = false;
            FileObject [] rootChildren = srcRootFo.getChildren();
            Set projectFiles = new HashSet(rootChildren.length);
            for (int i = 0; i < rootChildren.length; i++) {
                FileObject rootChildFo = rootChildren[i];
                if (CvsVersioningSystem.FILENAME_CVS.equals(rootChildFo.getNameExt())) continue;
                File child = FileUtil.toFile(rootChildFo);
                // #67900 Added special treatment for .cvsignore files
                if (sourceGroup.contains(rootChildFo) || CvsVersioningSystem.FILENAME_CVSIGNORE.equals(rootChildFo.getNameExt())) {
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
        Set filtered = new HashSet(); 
        Set roots = new HashSet();
        Set exclusions = new HashSet(); 
        for (int i = 0; i < projects.length; i++) {
            addProjectFiles(filtered, roots, exclusions, projects[i]);
        }
        return new Context(filtered, roots, exclusions);
    }

    public static File [] toFileArray(Collection fileObjects) {
        Set files = new HashSet(fileObjects.size()*4/3+1);
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
            File rootFile = new File(file, "CVS/Root"); // NOI18N
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
        throw new IOException("CVS/Root not found"); // NOI18N
    }
    
    public static Window getCurrentWindow() {
        Window wnd = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        if (wnd instanceof Dialog || wnd instanceof Frame) {
            return wnd;
        } else {
            return WindowManager.getDefault().getMainWindow();
        }
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
     * Computes path of this file to repository root.
     *
     * @param file a file
     * @return String path of this file in repsitory. If this path does not describe a
     * versioned file, this method returns an empty string 
     */
    public static String getRelativePath(File file) {
        String postfix = "";
        for (file = file.getParentFile(); file != null && !file.exists(); file = file.getParentFile()) {
            postfix = "/" + file.getName() + postfix;
        }
        if (file == null) return "";
        try {
            return CvsVersioningSystem.getInstance().getAdminHandler().getRepositoryForDirectory(file.getAbsolutePath(), "").substring(1) + postfix; // NOI18N
        } catch (IOException e) {
            return ""; // NOI18N
        }
    }

    /**
     * Determines the sticky information for a given file. If the file is new then it
     * returns its parent directory's sticky info, if any.  
     * 
     * @param file file to examine
     * @return String sticky information for a file (without leading N, D or T specifier) or null 
     */ 
    public static String getSticky(File file) {
        if (file == null) return null;
        FileInformation info = CvsVersioningSystem.getInstance().getStatusCache().getStatus(file);
        if (info.getStatus() == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) {
            return getSticky(file.getParentFile());
        } else if (info.getStatus() == FileInformation.STATUS_NOTVERSIONED_EXCLUDED) {
            return null;
        }
        if (file.isDirectory()) {
            String std = CvsVersioningSystem.getInstance().getAdminHandler().getStickyTagForDirectory(file);
            if (std != null) {
                std = std.substring(1);
            }
            return std;
        }
        Entry entry = info.getEntry(file);
        if (entry != null) {
            String stickyInfo = null;
            if (entry.getTag() != null) stickyInfo = entry.getTag(); // NOI18N
            else if (entry.getDate() != null) stickyInfo = entry.getDateFormatted(); // NOI18N
            return stickyInfo;
        }
        return null;
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

    public static String createBranchRevisionNumber(String branchNumber) {
        StringBuilder sb = new StringBuilder();
        int idx = branchNumber.lastIndexOf('.'); // NOI18N
        sb.append(branchNumber.substring(0, idx));
        sb.append(".0"); // NOI18N
        sb.append(branchNumber.substring(idx));
        return sb.toString();
    }

    public static String formatBranches(LogInformation.Revision revision, boolean useNumbersIfNamesNotAvailable) {
        String branches = revision.getBranches();
        if (branches == null) return ""; // NOI18N
        
        boolean branchNamesAvailable = true;
        StringBuilder branchNames = new StringBuilder();
        StringTokenizer st = new StringTokenizer(branches, ";"); // NOI18N
        while (st.hasMoreTokens()) {
            String branchNumber = st.nextToken().trim();
            List<LogInformation.SymName> names = revision.getLogInfoHeader().getSymNamesForRevision(createBranchRevisionNumber(branchNumber));
            if (names.size() > 0) {
                branchNames.append(names.get(0).getName());
            } else {
                branchNamesAvailable = false;
                if (useNumbersIfNamesNotAvailable) {
                    branchNames.append(branchNumber);
                } else {
                    break;
                }
            }
            branchNames.append("; "); // NOI18N
        }
        if (branchNamesAvailable || useNumbersIfNamesNotAvailable) {
            branchNames.delete(branchNames.length() - 2, branchNames.length());
        } else {
            branchNames.delete(0, branchNames.length());
        }
        return branchNames.toString();
    }

    public static boolean containsMetadata(File folder) {
        File repository = new File(folder, CvsVersioningSystem.FILENAME_CVS_REPOSITORY);
        File entries = new File(folder, CvsVersioningSystem.FILENAME_CVS_ENTRIES);
        return repository.canRead() && entries.canRead();
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
    
    public static boolean isPartOfCVSMetadata(File file) {
        return metadataPattern.matcher(file.getAbsolutePath()).matches();
    }
        
    /** Like mkdirs but but using openide filesystems (firing events) */
    public static FileObject mkfolders(File file) throws IOException {
        if (file.isDirectory()) return FileUtil.toFileObject(file);

        File parent = file.getParentFile();
        
        String path = file.getName();
        while (parent.isDirectory() == false) {
            path = parent.getName() + "/" + path;  // NOI18N
            parent = parent.getParentFile();
        }

        FileObject fo = FileUtil.toFileObject(parent);
        return FileUtil.createFolder(fo, path);
    }

    /**
     * 1) A tag must start with a letter
     * 2) A tag must not contain characters: $,.:;@ SPACE TAB NEWLINE
     * 3) Reserved tag names: HEAD BASE
     * 
     * @param name
     * @return true if the name of the tag is valid, false otherwise
     */
    public static boolean isTagValid(String name) {
        if (name == null || name.length() == 0) return false;
        char c = name.charAt(0);
        if ((c < 'a' || c > 'z') && (c < 'A' || c > 'Z')) return false;
        for (int i = 1; i < name.length(); i++) {
            c = name.charAt(i);
            if ((c < 'a' || c > 'z') && (c < 'A' || c > 'Z') && (c < '0' || c > '9') && c != '-' && c != '_') return false;
        }
        if (name.equals("HEAD") || name.equals("BASE")) return false;
        return true;
    }
}
