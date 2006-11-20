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

package org.netbeans.modules.subversion.util;

import org.netbeans.modules.subversion.client.SvnClient;
import org.openide.*;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.openide.util.Lookup;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.loaders.DataObjectNotFoundException;
import org.netbeans.api.project.*;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.modules.subversion.FileStatusCache;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.SvnFileNode;

import java.io.*;
import java.lang.Character;
import java.util.*;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.client.ExceptionHandler;
import org.netbeans.modules.subversion.options.AnnotationExpression;
import org.netbeans.modules.versioning.util.FlatFolder;
import org.tigris.subversion.svnclientadapter.*;
import org.tigris.subversion.svnclientadapter.utils.SVNUrlUtils;

/**
 * Subversion-specific utilities.
 * TODO: PETR Move generic methods to versioncontrol module
 *
 * @author Maros Sandor
 */
public class SvnUtils {
    
    private static final Pattern metadataPattern = Pattern.compile(".*\\" + File.separatorChar + "(\\.|_)svn(\\" + File.separatorChar + ".*|$)");
    
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
        List<File> files = new ArrayList<File>(nodes.length);
        List<File> rootFiles = new ArrayList<File>(nodes.length);
        List<File> rootFileExclusions = new ArrayList<File>(5);
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            SvnFileNode svnNode = (SvnFileNode) node.getLookup().lookup(SvnFileNode.class);
            if (svnNode != null) {
                files.add(svnNode.getFile());
                rootFiles.add(svnNode.getFile());
                continue;
            }
            Project project = (Project) node.getLookup().lookup(Project.class);
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
                //if (f != null) { XXX fallback if experimntal should not work
//                    File probe = new File (f, ".svn");
//                    File probe2 = new File (f, "_svn");
//                    if (probe.isDirectory() || probe2.isDirectory()) {
//                        return true;
//                    }
//                }
                if ((cache.getStatus(f).getStatus() & FileInformation.STATUS_MANAGED) != 0) return true; // XXX experimental
            }
        }
        return false;
    }
    
    private static void addFileObjects(Node node, List<File> files, List<File> rootFiles) {
        Collection<? extends NonRecursiveFolder> folders = node.getLookup().lookup(new Lookup.Template<NonRecursiveFolder>(NonRecursiveFolder.class)).allInstances();
        List<File> nodeFiles = new ArrayList<File>();
        if (folders.size() > 0) {
            for (NonRecursiveFolder j : folders) {
                nodeFiles.add(new FlatFolder(FileUtil.toFile(j.getFolder()).getAbsolutePath()));
            }
        } else {
            Collection<? extends FileObject> fileObjects = node.getLookup().lookup(new Lookup.Template<FileObject>(FileObject.class)).allInstances();
            if (fileObjects.size() > 0) {
                nodeFiles.addAll(toFileCollection(fileObjects));
            } else {
                DataObject dataObject = node.getCookie(DataObject.class);
                if (dataObject instanceof DataShadow) {
                    dataObject = ((DataShadow) dataObject).getOriginal();
                }
                if (dataObject != null) {
                    Collection<File> doFiles = toFileCollection(dataObject.files());
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
    public static void addProjectFiles(Collection<File> filteredFiles, Collection<File> rootFiles, Collection<File> rootFilesExclusions, Project project) {
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
            Set<File> projectFiles = new HashSet<File>(rootChildren.length);
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
        List<File> filtered = new ArrayList<File>();
        List<File> roots = new ArrayList<File>();
        List<File> exclusions = new ArrayList<File>();
        for (int i = 0; i < projects.length; i++) {
            addProjectFiles(filtered, roots, exclusions, projects[i]);
        }
        return new Context(filtered, roots, exclusions);
    }
    
    private static Collection<File> toFileCollection(Collection<? extends FileObject> fileObjects) {
        Set<File> files = new HashSet<File>(fileObjects.size()*4/3+1);
        for (FileObject f: fileObjects) {
            files.add(FileUtil.toFile(f));
        }
        files.remove(null);
        return files;
    }
    
    public static File [] toFileArray(Collection<FileObject> fileObjects) {
        Set<File> files = new HashSet<File>(fileObjects.size()*4/3+1);
        for (Iterator<FileObject> i = fileObjects.iterator(); i.hasNext();) {
            files.add(FileUtil.toFile(i.next()));
        }
        files.remove(null);
        return files.toArray(new File[files.size()]);
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
        return revision == null ? null : Long.toString(Long.parseLong(revision) - 1);
    }
    
    /**
     * Compute relative path to repository root.
     * For not yet versioned files guess the URL
     * from parent context.
     *
     * <p>I/O intensive avoid calling it frnm AWT.
     *
     * @return the repository url or null for unknown
     */
    public static String getRelativePath(File file) {
        String repositoryPath = null;
        SvnClient client = Subversion.getInstance().getClient(false);
        
        List<String> path = new ArrayList<String>();
        SVNUrl repositoryURL = null;
        while (Subversion.getInstance().isManaged(file)) {
            
            ISVNInfo info = null;
            try {
                info = client.getInfoFromWorkingCopy(file);
            } catch (SVNClientException ex) {
                if (ExceptionHandler.isUnversionedResource(ex.getMessage()) == false) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
            
            if (info != null && info.getUrl() != null) {
                SVNUrl fileURL = decode(info.getUrl());
                repositoryURL = info.getRepository();
                
                if (fileURL != null && repositoryURL !=  null) {
                    String fileLink = fileURL.toString();
                    String repositoryLink = repositoryURL.toString();
                    repositoryPath = fileLink.substring(repositoryLink.length());
                    
                    Iterator it = path.iterator();
                    StringBuffer sb = new StringBuffer();
                    while (it.hasNext()) {
                        String segment = (String) it.next();
                        sb.append("/"); // NOI18N
                        sb.append(segment);
                    }
                    repositoryPath += sb.toString();
                    break;
                }
            }
            
            path.add(0, file.getName());
            file = file.getParentFile();
            
        }
        
        return repositoryPath;
    }
    
    /**
     * Compute relative path to repository root.
     * For not yet versioned files guess the URL
     * from parent context.
     *
     * <p>I/O intensive avoid calling it frnm AWT.
     *
     * @return the repository url or null for unknown
     * XXX we need this until we get a local implementation for client.getInfoFromWorkingCopy(file);
     */
    public static String getRelativePath(SVNUrl repositoryURL, File file) {
        String repositoryPath = null;
        SvnClient client = Subversion.getInstance().getClient(false);
        
        List<String> path = new ArrayList<String>();
        while (Subversion.getInstance().isManaged(file)) {
            
            ISVNStatus status = null;
            try {
                status = client.getSingleStatus(file);
            } catch (SVNClientException ex) {
                if (ExceptionHandler.isUnversionedResource(ex.getMessage()) == false) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
            
            if (status != null && status.getUrl() != null) {
                SVNUrl fileURL = status.getUrl();
                
                if (fileURL != null && repositoryURL !=  null) {
                    fileURL = decode(fileURL);
                    String fileLink = fileURL.toString();
                    String repositoryLink = repositoryURL.toString();
                    repositoryPath = fileLink.substring(repositoryLink.length());
                    
                    Iterator it = path.iterator();
                    StringBuffer sb = new StringBuffer();
                    while (it.hasNext()) {
                        String segment = (String) it.next();
                        sb.append("/"); // NOI18N
                        sb.append(segment);
                    }
                    repositoryPath += sb.toString();
                    break;
                }
            }
            
            path.add(0, file.getName());
            file = file.getParentFile();
            
        }
        
        return repositoryPath;
    }
    
    /**
     * Returns the repository root for the given file.
     * For not yet versioned files guess the URL
     * from parent context.
     *
     * <p>I/O intensive avoid calling it frnm AWT.
     *
     * @return the repository url or null for unknown
     */
    public static SVNUrl getRepositoryRootUrl(File file) {
        SvnClient client = Subversion.getInstance().getClient(false);
        
        SVNUrl repositoryURL = null;
        while (Subversion.getInstance().isManaged(file)) {
            ISVNInfo info = null;
            try {
                info = client.getInfoFromWorkingCopy(file);
            } catch (SVNClientException ex) {
                if (ExceptionHandler.isUnversionedResource(ex.getMessage()) == false) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
            
            if (info != null && info.getUrl() != null) {
                repositoryURL = info.getRepository();
                if (repositoryURL != null) {
                    break;
                }
            }
            
            // path.add(0, file.getName());
            file = file.getParentFile();
            
        }
        return repositoryURL;
    }
    
    /**
     * Returns the repository URL for the given file.
     * For not yet versioned files guess the URL
     * from parent context.
     *
     * <p>I/O intensive avoid calling it frnm AWT.
     *
     * @return the repository url or null for unknown
     */
    public static SVNUrl getRepositoryUrl(File file) {
        SvnClient client = Subversion.getInstance().getClient(false);
        
        StringBuffer path = new StringBuffer();
        SVNUrl fileURL = null;
        while (Subversion.getInstance().isManaged(file)) {
            
            try {
                // it works with 1.3 workdirs and our .svn parser
                ISVNStatus status = getSingleStatus(client, file);
                if (status != null) {
                    fileURL = decode(status.getUrl());
                    if (fileURL != null) {
                        break;
                    }
                }
            } catch (SVNClientException ex) {
                if (ExceptionHandler.isUnversionedResource(ex.getMessage()) == false) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
            
            // slower fallback
            
            ISVNInfo info = null;
            try {
                info = client.getInfoFromWorkingCopy(file);
            } catch (SVNClientException ex) {
                if (ExceptionHandler.isUnversionedResource(ex.getMessage()) == false) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
            
            if (info != null) {
                fileURL = decode(info.getUrl());
                
                if (fileURL != null ) {
                    break;
                }
            }
            
            path.insert(0, file.getName()).insert(0, "/");
            file = file.getParentFile();
            
        }
        if (path.length() > 0) fileURL = fileURL.appendPath(path.toString());
        return fileURL;
    }
    
    private static ISVNStatus getSingleStatus(SvnClient client, File file) throws SVNClientException{
        return client.getSingleStatus(file);
    }
    
    /**
     * Decodes svn URI by decoding %XX escape sequences.
     * 
     * @param url url to decode
     * @return decoded url
     */ 
    private static SVNUrl decode(SVNUrl url) {
        if (url == null) return null;
        String s = url.toString();
        StringBuffer sb = new StringBuffer(s.length());

        boolean inQuery = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '?') {
                inQuery = true;
            } else if (c == '+' && inQuery) {
                c = ' ';
            } else if (c == '%' && i + 2 < s.length() && isHexDigit(s.charAt(i + 1)) && isHexDigit(s.charAt(i + 2))) {
                c = (char) Integer.parseInt(s.substring(i + 1, i + 3), 16); 
                i += 2;
            }
            sb.append(c);
        }
        try {
            return new SVNUrl(sb.toString());
        } catch (java.net.MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isHexDigit(char c) {
        return c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a' && c <= 'f';
    }
    
    /*
     * Determines a versioned file's repository path
     *
     * @param file versioned file
     * @return file's path in repository
     */
    public static String getRepositoryPath(File file) {
        SVNUrl url = getRepositoryUrl(file);
        SVNUrl rootUrl = getRepositoryRootUrl(file);
        return SVNUrlUtils.getRelativePath(rootUrl, url, true);
    }
    
    /**
     * Checks if the file is binary.
     *
     * @param file file to check
     * @return true if the file cannot be edited in NetBeans text editor, false otherwise
     */
    public static boolean isFileContentBinary(File file) {
        FileObject fo = FileUtil.toFileObject(file);
        if (fo == null) return false;
        try {
            DataObject dao = DataObject.find(fo);
            return dao.getCookie(EditorCookie.class) == null;
        } catch (DataObjectNotFoundException e) {
            // not found, continue
        }
        return false;
    }
    
    /**
     * @return true if the buffer is almost certainly binary.
     * Note: Non-ASCII based encoding encoded text is binary,
     * newlines cannot be reliably detected.
     */
    public static boolean isBinary(byte[] buffer) {
        for (int i = 0; i<buffer.length; i++) {
            int ch = buffer[i];
            if (ch < 32 && ch != '\t' && ch != '\n' && ch != '\r') {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Compares two {@link FileInformation} objects by importance of statuses they represent.
     */
    public static class ByImportanceComparator<T> implements Comparator<FileInformation> {
        public int compare(FileInformation i1, FileInformation i2) {
            return getComparableStatus(i1.getStatus()) - getComparableStatus(i2.getStatus());
        }
    }
    
    
    /**
     * Normalize flat files, Subversion treats folder as normal file
     * so it's necessary explicitly list direct descendants to
     * get classical flat behaviour.
     *
     * <p> E.g. revert on package node means:
     * <ul>
     *   <li>revert package folder properties AND
     *   <li>revert all modified (including deleted) files in the folder
     * </ul>
     *
     * @return files with given status and direct descendants with given status.
     */
    public static File[] flatten(File[] files, int status) {
        LinkedList<File> ret = new LinkedList<File>();
        
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        for (int i = 0; i<files.length; i++) {
            File dir = files[i];
            FileInformation info = cache.getStatus(dir);
            if ((status & info.getStatus()) != 0) {
                ret.add(dir);
            }
            File[] entries = cache.listFiles(dir);  // comparing to dir.listFiles() lists already deleted too
            for (int e = 0; e<entries.length; e++) {
                File entry = entries[e];
                info = cache.getStatus(entry);
                if ((status & info.getStatus()) != 0) {
                    ret.add(entry);
                }
            }
        }
        
        return ret.toArray(new File[ret.size()]);
    }
    
    /**
     * Checks file location.
     *
     * @param file file to check
     * @return true if the file or folder is a part of subverion metadata, false otherwise
     */
    public static boolean isPartOfSubversionMetadata(File file) {
        return metadataPattern.matcher(file.getAbsolutePath()).matches();
    }
    
    /**
     * Gets integer status that can be used in comparators. The more important the status is for the user,
     * the lower value it has. Conflict is 0, unknown status is 100.
     *
     * @return status constant suitable for 'by importance' comparators
     */
    public static int getComparableStatus(int status) {
        if (0 != (status & FileInformation.STATUS_VERSIONED_CONFLICT)) {
            return 0;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MERGE)) {
            return 1;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_DELETEDLOCALLY)) {
            return 10;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) {
            return 11;
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)) {
            return 12;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
            return 13;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY)) {
            return 14;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY)) {
            return 30;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_NEWINREPOSITORY)) {
            return 31;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY)) {
            return 32;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_UPTODATE)) {
            return 50;
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_EXCLUDED)) {
            return 100;
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NOTMANAGED)) {
            return 101;
        } else if (status == FileInformation.STATUS_UNKNOWN) {
            return 102;
        } else {
            throw new IllegalArgumentException("Uncomparable status: " + status); // NOI18N
        }
    }
    
    //static final Pattern branchTagPattern = Pattern.compile(".*/(branches|tags)/(.+?)/.*"); // NOI18N
//    static final Pattern branchPattern = Pattern.compile(".*/(branches)/(.+?)/.*"); // NOI18N
//    static final Pattern tagPattern = Pattern.compile(".*/(tags)/(.+?)/.*"); // NOI18N
    
    public static String getCopy(File file) {
        return getCopy(file, SvnModuleConfig.getDefault().getAnnotationExpresions());
    }

    /**
     * Returns copy branch or tag name if lives
     * in typical location (branches, tags).
     *
     * @return name or null
     */
    public static String getCopy(File file, List<AnnotationExpression> annotationExpressions) {
        SVNUrl url = getRepositoryUrl(file);
        return getCopy(url, annotationExpressions);
    }
    
    public static String getCopy(SVNUrl url) {
        return getCopy(url, SvnModuleConfig.getDefault().getAnnotationExpresions());
    }
    
    public static String getCopy(SVNUrl url, List<AnnotationExpression> annotationExpressions) {
        if (url != null) {
            for (Iterator<AnnotationExpression> it = annotationExpressions.iterator(); it.hasNext();) {
                AnnotationExpression annotationExpression = it.next();
                
                Matcher m = annotationExpression.getUrlPatern().matcher(url.toString());
                if (m.matches()) {
                    String ae = annotationExpression.getAnnotationExp();
                    
                    StringBuffer copyName = new StringBuffer();
                    StringBuffer groupStr = new StringBuffer();                    
                    boolean inGroup = false;
                    
                    for (int i = 0; i < ae.length(); i++) {
                        char c = ae.charAt(i);
                        if(c == '\\') {
                            inGroup = true;                                                                      
                            continue;
                        } else if(inGroup) {
                            if(Character.isDigit(c)) {                                
                                groupStr.append(c);                                                                                                                                            
                            } else {
                                if(groupStr.length() > 0) {
                                    int group = Integer.valueOf(groupStr.toString()).intValue();
                                    copyName.append(m.group(group));
                                    groupStr = new StringBuffer();                    
                                } else {
                                    copyName.append('\\');
                                    copyName.append(c);
                                }                                
                                inGroup = false;
                            }                                                                
                            continue;                            
                        }
                        copyName.append(c);
                    }
                    if(groupStr.length() > 0) {
                        int group = Integer.valueOf(groupStr.toString()).intValue();
                        copyName.append(m.group(group));
                    }
                    return copyName.toString();
                }
            }           
        }
        return null;
    }
    
//    /**
//     * Returns branch name if the file is inside 'branches' folder.
//     *
//     * @return branch name or null
//     */
//    public static String getBranch(File file, List<AnnotationExpression> annotationExpressions) {
//        SVNUrl url = getRepositoryUrl(file);
//        if (url != null) {
//            Matcher m = branchPattern.matcher(url.toString());
//            if (m.matches()) {
//                return m.group(2);
//            }
//        }
//        return null;
//    }
    
//    /**
//     * Returns tag name if the file is inside 'tags' folder.
//     *
//     * @return branch name or null
//     */
//    public static String getTag(File file) {
//        SVNUrl url = getRepositoryUrl(file);
//        if (url != null) {
//            Matcher m = tagPattern.matcher(url.toString());
//            if (m.matches()) {
//                return m.group(2);
//            }
//        }
//        return null;
//    }
    
    /**
     * Refreshes statuses of this folder and all its parent folders up to filesystem root.
     *
     * @param folder folder to refresh
     */
    public static void refreshRecursively(File folder) {
        if (folder == null) return;
        refreshRecursively(folder.getParentFile());
        Subversion.getInstance().getStatusCache().refresh(folder, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
    }
    
    public static String ripUserFromHost(String host) {
        int idx = host.indexOf('@');
        if(idx < 0) {
            return host;
        } else {
            return host.substring(idx + 1);
        }
    }
    
    public static SVNRevision getSVNRevision(String revisionString) {
        try {
            // HEAD, PREV, BASE, COMMITED, ...
            return SVNRevision.getRevision(revisionString);
        } catch (ParseException ex) {
            return new SVNRevision.Number(Long.parseLong(revisionString));
        }
    }
    
    /*
     * Returns the first pattern from the list which matches with the given value.
     * The patterns are interpreted as shell paterns.
     *
     * @param patterns
     * @param value
     * @return the first pattern matching with the given value
     */
    public static List<String> getMatchinIgnoreParterns(List<String> patterns, String value, boolean onlyFirstMatch)  {
        List<String> ret = new ArrayList<String>();
        for (Iterator<String> i = patterns.iterator(); i.hasNext();) {
            try {
                // may contain shell patterns (almost identical to RegExp)
                String patternString  = i.next();
                String shellPatternString = regExpToFilePatterns(patternString);
                Pattern pattern =  Pattern.compile(shellPatternString);
                if (pattern.matcher(value).matches()) {
                    ret.add(patternString);
                    if(onlyFirstMatch) {
                        return ret;
                    }
                }
            } catch (PatternSyntaxException e) {
                // it's difference between shell and regexp
                // or user error (set invalid property), rethrow?
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return ret;
    }
    
    private static String regExpToFilePatterns(String exp) {
        exp = exp.replaceAll("\\.", "\\\\.");   // NOI18N
        exp = exp.replaceAll("\\*", ".*");      // NOI18N
        exp = exp.replaceAll("\\?", ".");       // NOI18N
        
        exp = exp.replaceAll("\\$", "\\\\\\$"); // NOI18N
        exp = exp.replaceAll("\\^", "\\\\^");   // NOI18N
        exp = exp.replaceAll("\\<", "\\\\<");   // NOI18N
        exp = exp.replaceAll("\\>", "\\\\>");   // NOI18N
        exp = exp.replaceAll("\\[", "\\\\[");   // NOI18N
        exp = exp.replaceAll("\\]", "\\\\]");   // NOI18N
        exp = exp.replaceAll("\\{", "\\\\{");   // NOI18N
        exp = exp.replaceAll("\\}", "\\\\}");   // NOI18N
        exp = exp.replaceAll("\\(", "\\\\(");   // NOI18N
        exp = exp.replaceAll("\\)", "\\\\)");   // NOI18N
        exp = exp.replaceAll("\\+", "\\\\+");   // NOI18N
        exp = exp.replaceAll("\\|", "\\\\|");   // NOI18N
        
        return exp;
    }
}
