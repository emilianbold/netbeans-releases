/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.utils;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.modules.git.FileInformation;
import org.netbeans.modules.git.FileInformation.Status;
import org.netbeans.modules.git.FileStatusCache;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.VersionsCache;
import org.netbeans.modules.git.ui.blame.AnnotateAction;
import org.netbeans.modules.git.ui.commit.CommitAction;
import org.netbeans.modules.git.ui.ignore.IgnoreAction;
import org.netbeans.modules.git.ui.status.GitStatusNode;
import org.netbeans.modules.git.ui.status.StatusAction;
import org.netbeans.modules.versioning.diff.DiffUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.FileSelector;
import org.netbeans.modules.versioning.util.IndexingBridge;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.Line;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
 *s
 * @author ondra
 */
public final class GitUtils {

    public static final String DOT_GIT = ".git"; //NOI18N
    public static final String INDEX_LOCK = "index.lock"; //NOI18N
    private static final Pattern METADATA_PATTERN = Pattern.compile(".*\\" + File.separatorChar + "(\\.)git(\\" + File.separatorChar + ".*|$)"); // NOI18N
    private static final String FILENAME_GITIGNORE = ".gitignore"; // NOI18N
    public static final String HEAD = "HEAD"; //NOI18N
    public static final String INDEX = "INDEX"; //NOI18N
    public static final String CURRENT = "CURRENT"; //NOI18N
    public static final String PREFIX_R_HEADS = "refs/heads/"; //NOI18N
    public static final String PREFIX_R_REMOTES = "refs/remotes/"; //NOI18N
    public static final ProgressMonitor NULL_PROGRESS_MONITOR = new NullProgressMonitor();
    public static final String MASTER = "master"; //NOI18N

    /**
     * Checks file location to see if it is part of git metadata
     *
     * @param file file to check
     * @return true if the file or folder is a part of git metadata, false otherwise
     */
    public static boolean isPartOfGitMetadata (File file) {
        return METADATA_PATTERN.matcher(file.getAbsolutePath()).matches();
    }

    /**
     * Tests <tt>.hg</tt> directory itself.
     */
    public static boolean isAdministrative (File file) {
        String name = file.getName();
        return isAdministrative(name) && file.isDirectory();
    }

    public static boolean isAdministrative (String fileName) {
        return fileName.equals(DOT_GIT); // NOI18N
    }

    public static boolean repositoryExistsFor (File file) {
        return new File(file, DOT_GIT).exists();
    }

    /**
     * Returns the administrative git folder for the given repository and normalizes the file
     * @param repositoryRoot normalized root of the repository
     * @return administrative git folder
     */
    public static File getGitFolderForRoot (File repositoryRoot) {
        return FileUtil.normalizeFile(new File(repositoryRoot, DOT_GIT));
    }

    /**
     * Adds the given file into filesUnderRoot:
     * <ul>
     * <li>if the file was already in the set, does nothing and returns true</li>
     * <li>if the file lies under a folder already present in the set, does nothing and returns true</li>
     * <li>if the file and none of it's ancestors is not in the set yet, this adds the file into the set,
     * removes all it's children and returns false</li>
     * @param repository repository root
     * @param filesUnderRoot set of repository roots
     * @param file file to add
     * @return false if the file was added or true if it was already contained
     */
    public static boolean prepareRootFiles (File repository, Collection<File> filesUnderRoot, File file) {
        boolean added = false;
        Set<File> filesToRemove = new HashSet<File>();
        for (File fileUnderRoot : filesUnderRoot) {
            if (file.equals(fileUnderRoot) || fileUnderRoot.equals(repository)) {
                // file has already been inserted or scan is planned for the whole repository root
                added = true;
                break;
            }
            if (file.equals(repository)) {
                // plan the scan for the whole repository root
                // adding the repository, there's no need to leave all other files
                filesUnderRoot.clear();
                break;
            } else {
                if (file.getAbsolutePath().length() < fileUnderRoot.getAbsolutePath().length()) {
                    if (Utils.isAncestorOrEqual(file, fileUnderRoot)) {
                        filesToRemove.add(fileUnderRoot);
                    }
                } else {
                    if (Utils.isAncestorOrEqual(fileUnderRoot, file)) {
                        added = true;
                        break;
                    }
                }
            }
        }
        filesUnderRoot.removeAll(filesToRemove);
        if (!added) {
            // not added yet
            filesUnderRoot.add(file);
        }
        return added;
    }
    
    public static boolean isIgnored(File file, boolean checkSharability){
        if (file == null) return false;
        String path = file.getPath();
        File topFile = Git.getInstance().getRepositoryRoot(file);
        
        // We assume that the toplevel directory should not be ignored.
        if (topFile == null || topFile.equals(file)) {
            return false;
        }

        // check cached not sharable folders and files
        if (isNotSharable(path, topFile)) {
            return true;
        }

        // If a parent of the file matches a pattern ignore the file
        File parentFile = file.getParentFile();
        if (!parentFile.equals(topFile)) {
            if (isIgnored(parentFile, false)) return true;
        }

        if (FILENAME_GITIGNORE.equals(file.getName())) return false;
        if (checkSharability) {
            int sharability = SharabilityQuery.getSharability(FileUtil.normalizeFile(file));
            if (sharability == SharabilityQuery.NOT_SHARABLE) {
                if (GitModuleConfig.getDefault().getAutoIgnoreFiles()) {
                    ignoreNotSharableAncestor(topFile, file);
                } else {
                    addNotSharable(topFile, path);
                }
                return true;
            }
        }
        return false;
    }

    // cached not sharable files and folders
    private static final Map<File, Set<String>> notSharable = Collections.synchronizedMap(new HashMap<File, Set<String>>(5));
    private static void addNotSharable (File topFile, String ignoredPath) {
        synchronized (notSharable) {
            // get cached patterns
            Set<String> ignores = notSharable.get(topFile);
            if (ignores == null) {
                ignores = new HashSet<String>();
            }
            String patternCandidate = ignoredPath;
            // test for duplicate patterns
            for (Iterator<String> it = ignores.iterator(); it.hasNext();) {
                String storedPattern = it.next();
                if (storedPattern.equals(ignoredPath) // already present
                        || ignoredPath.startsWith(storedPattern + '/')) { // path already ignored by its ancestor
                    patternCandidate = null;
                    break;
                } else if (storedPattern.startsWith(ignoredPath + '/')) { // stored pattern matches a subset of ignored path
                    // remove the stored pattern and add the ignored path
                    it.remove();
                }
            }
            if (patternCandidate != null) {
                ignores.add(patternCandidate);
            }
            notSharable.put(topFile, ignores);
        }
    }

    private static boolean isNotSharable (String path, File topFile) {
        boolean retval = false;
        Set<String> notSharablePaths = notSharable.get(topFile);
        if (notSharablePaths == null) {
            notSharablePaths = Collections.emptySet();
        }
        retval = notSharablePaths.contains(path);
        return retval;
    }

    /**
     * Permanently ignores (modifies ignore file) topmost not-sharable ancestor of a given file.
     * @param topFile
     * @param notSharableFile 
     */
    private static void ignoreNotSharableAncestor (File topFile, File notSharableFile) {
        if (topFile.equals(notSharableFile)) {
            throw new IllegalStateException("Trying to ignore " + notSharableFile + " in " + topFile); //NOI18N
        }
        File parent;
        // find the topmost 
        while (!topFile.equals(parent = notSharableFile.getParentFile()) && SharabilityQuery.getSharability(FileUtil.normalizeFile(parent)) == SharabilityQuery.NOT_SHARABLE) {
            notSharableFile = parent;
        }
        addNotSharable(topFile, notSharableFile.getAbsolutePath());
        // ignore only folders
        if (notSharableFile.isDirectory()) {
            for (File f : Git.getInstance().getCreatedFolders()) {
                if (Utils.isAncestorOrEqual(f, notSharableFile)) {
                    SystemAction.get(IgnoreAction.class).ignoreFolders(topFile, new File[] { notSharableFile });
                }
            }
        }
    }

    /**
     * Determines if the given context contains at least one root file from a git repository
     *
     * @param VCSContext
     * @return true if the given context contains a root file from a git repository
     */
    public static boolean isFromGitRepository (VCSContext context){
        return getRootFile(context) != null;
    }

    /**
     * Returns path to repository root or null if not managed
     *
     * @param VCSContext
     * @return String of repository root path
     */
    public static File getRootFile (VCSContext context){
        if (context == null) return null;
        Git git = Git.getInstance();
        File [] files = context.getRootFiles().toArray(new File[context.getRootFiles().size()]);
        if (files == null || files.length == 0) return null;

        File root = git.getRepositoryRoot(files[0]);
        return root;
    }

    /**
     * Returns repository roots for all root files from context
     *
     * @param VCSContext
     * @return repository roots
     */
    public static Set<File> getRepositoryRoots(VCSContext context) {
        Set<File> rootsSet = context.getRootFiles();
        return getRepositoryRoots(rootsSet);
    }

    /**
     * Returns repository roots for all root files from context
     *
     * @param roots root files
     * @return repository roots
     */
    public static Set<File> getRepositoryRoots (Set<File> roots) {
        Set<File> ret = new HashSet<File>();

        // filter managed roots
        for (File file : roots) {
            if(Git.getInstance().isManaged(file)) {
                File repoRoot = Git.getInstance().getRepositoryRoot(file);
                if(repoRoot != null) {
                    ret.add(repoRoot);
                }
            }
        }
        return ret;
    }

    /**
     *
     * @param ctx
     * @return
     */
    public static HashMap.SimpleImmutableEntry<File, File[]> getActionRoots(VCSContext ctx) {
        Set<File> rootsSet = ctx.getRootFiles();
        Map<File, List<File>> map = new HashMap<File, List<File>>();

        // filter managed roots
        for (File file : rootsSet) {
            if(Git.getInstance().isManaged(file)) {
                File repoRoot = Git.getInstance().getRepositoryRoot(file);
                if(repoRoot != null) {
                    List<File> l = map.get(repoRoot);
                    if(l == null) {
                        l = new LinkedList<File>();
                        map.put(repoRoot, l);
                    }
                    l.add(file);
                }
            }
        }

        Set<File> repoRoots = map.keySet();
        if(map.size() > 1) {
            // more than one managed root => need a dlg
            FileSelector fs = new FileSelector(
                    NbBundle.getMessage(GitUtils.class, "LBL_FileSelector_Title"), //NOI18N
                    NbBundle.getMessage(GitUtils.class, "FileSelector.jLabel1.text"), //NOI18N
                    new HelpCtx("org.netbeans.modules.git.FileSelector"), //NOI18N
                    GitModuleConfig.getDefault().getPreferences());
            if(fs.show(repoRoots.toArray(new File[repoRoots.size()]))) {
                File selection = fs.getSelectedFile();
                List<File> l = map.get(selection);
                return new HashMap.SimpleImmutableEntry<File, File[]>(selection, l.toArray(new File[l.size()]));
            } else {
                return null;
            }
        } else if (map.isEmpty()) {
            return null;
        } else {
            File root = map.keySet().iterator().next();
            List<File> l = map.get(root);
            return new HashMap.SimpleImmutableEntry<File, File[]>(root, l.toArray(new File[l.size()]));
        }
    }

    /**
     * Returns only those root files from the given context which belong to repository
     * @param ctx
     * @param repository
     * @return
     */
    public static File[] filterForRepository(final VCSContext ctx, final File repository) {
        File[] files = null;
        if(ctx != null) {
            Set<File> s = ctx.getRootFiles();
            files = s.toArray(new File[s.size()]);
        }
        if (files != null) {
            List<File> l = new LinkedList<File>();
            for (File file : files) {
                File r = Git.getInstance().getRepositoryRoot(file);
                if (r != null && r.equals(repository)) {
                    l.add(file);
                }
            }
            files = l.toArray(new File[l.size()]);
        }
        return files;
    }

    /**
     * Normalize flat files, Git treats folder as normal file
     * so it's necessary explicitly list direct descendants to
     * get classical flat behaviour.
     * <strong>Does not return up-to-date files</strong>
     *
     * <p> E.g. revert on package node means:
     * <ul>
     *   <li>revert package folder properties AND
     *   <li>revert all modified (including deleted) files in the folder
     * </ul>
     *
     * @return files with given status and direct descendants with given status.
     */

    public static File[] flatten(File[] files, Set<Status> statuses) {
        LinkedList<File> ret = new LinkedList<File>();

        FileStatusCache cache = Git.getInstance().getFileStatusCache();
        for (int i = 0; i<files.length; i++) {
            File dir = files[i];
            FileInformation info = cache.getStatus(dir);
            if (info.containsStatus(statuses)) {
                ret.add(dir);
            }
            File[] entries = cache.listFiles(dir);  // comparing to dir.listFiles() lists already deleted too
            for (int e = 0; e<entries.length; e++) {
                File entry = entries[e];
                info = cache.getStatus(entry);
                if (info.containsStatus(statuses)) {
                    ret.add(entry);
                }
            }
        }

        return ret.toArray(new File[ret.size()]);
    }

    /**
     * Returns non-flat folders from the given file array plus a set of direct file descendants of flat-folders from the file array that have the given status in the cache.
     * <strong>Does not return up-to-date files</strong>
     */
    public static File[] listFiles (File[] roots, EnumSet<Status> includedStatuses) {
        File[][] split = Utils.splitFlatOthers(roots);
        List<File> fileList = new ArrayList<File>();
        FileStatusCache cache = Git.getInstance().getFileStatusCache();
        for (int c = 0; c < split.length; c++) {
            File[] splitRoots = split[c];
            if (c == 1) {
                // recursive
                fileList.addAll(Arrays.asList(cache.listFiles(splitRoots, includedStatuses)));
            } else {
                // not recursive, list only direct descendants
                fileList.addAll(Arrays.asList(GitUtils.flatten(splitRoots, includedStatuses)));
            }
        }
        return fileList.toArray(new File[fileList.size()]);
    }

    /**
     * Semantics is similar to {@link org.openide.windows.TopComponent#getActivatedNodes()} except that this
     * method returns File objects instead of Nodes. Every node is examined for Files it represents. File and Folder
     * nodes represent their underlying files or folders. Project nodes are represented by their source groups. Other
     * logical nodes must provide FileObjects in their Lookup.
     *
     * @return File [] array of activated files
     * @param nodes or null (then taken from windowsystem, it may be wrong on editor tabs #66700).
     */
    public static VCSContext getCurrentContext(Node[] nodes) {
        if (nodes == null) {
            nodes = TopComponent.getRegistry().getActivatedNodes();
        }
        return VCSContext.forNodes(nodes);
    }

    /**
     * Uses content analysis to return the mime type for files.
     *
     * @param file file to examine
     * @return String mime type of the file (or best guess)
     */
    public static String getMimeType(File file) {
        FileObject fo = FileUtil.toFileObject(file);
        String foMime;
        boolean hasMime = false;
        if (fo == null) {
            foMime = "content/unknown"; // NOI18N
        } else {
            foMime = fo.getMIMEType();
            if ("content/unknown".equals(foMime)) { // NOI18N
                foMime = "text/plain"; // NOI18N
            } else {
                hasMime = true;
            }
        }
        if (!hasMime) {
            return isFileContentBinary(file) ? "application/octet-stream" : foMime; // NOI18N
        } else {
            return foMime;
        }
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
     * Determines if the context has been created in a git view, i.e. it consists of instances of {@link GitStatusNode}
     * @param context
     * @return true if the context contains instances of {@link GitStatusNode}
     */
    public static boolean isFromInternalView (VCSContext context) {
        return context.getElements().lookup(GitStatusNode.class) != null;
    }
    
    public static List<String> getRelativePaths(File workDir, File[] roots) {
        List<String> paths = new ArrayList<String>(roots.length);
        for (File root : roots) {
            if (workDir.equals(root)) {
                paths.clear();
                break;
            } else {
                paths.add(getRelativePath(workDir, root));
            }
        }
        return paths;
    }

    public static String getRelativePath (File repo, final File file) {
        StringBuilder relativePath = new StringBuilder(""); //NOI18N
        File parent = file;
        if (!parent.equals(repo)) {
            while (parent != null && !parent.equals(repo)) {
                relativePath.insert(0, "/").insert(0, parent.getName()); //NOI18N
                parent = parent.getParentFile();
            }
            if (parent == null) {
                throw new IllegalArgumentException(file.getAbsolutePath() + " is not under " + repo.getAbsolutePath());
            }
            relativePath.deleteCharAt(relativePath.length() - 1);
        }
        return relativePath.toString();
    }

    public static void openInVersioningView (Collection<File> files, File repository, ProgressMonitor pm) {
        List<Node> nodes = new LinkedList<Node>();
        for (File file : files) {
            Node node = new AbstractNode(Children.LEAF, Lookups.fixed(file));
            nodes.add(node);
            // this will refresh seen roots
        }
        Git.getInstance().getFileStatusCache().refreshAllRoots(Collections.<File, Collection<File>>singletonMap(repository, files), pm);
        if (!pm.isCanceled()) {
            final VCSContext context = VCSContext.forNodes(nodes.toArray(new Node[nodes.size()]));
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    SystemAction.get(StatusAction.class).performContextAction(context);
                }
            });
        }
    }

    public static void printInfo (StringBuilder sb, GitRevisionInfo info) {
        String lbrevision = NbBundle.getMessage(CommitAction.class, "MSG_CommitAction.logCommit.revision");   // NOI18N
        String lbauthor = NbBundle.getMessage(CommitAction.class, "MSG_CommitAction.logCommit.author");      // NOI18N
        String lbcommitter = NbBundle.getMessage(CommitAction.class, "MSG_CommitAction.logCommit.committer");      // NOI18N
        String lbdate = NbBundle.getMessage(CommitAction.class, "MSG_CommitAction.logCommit.date");        // NOI18N
        String lbsummary = NbBundle.getMessage(CommitAction.class, "MSG_CommitAction.logCommit.summary");     // NOI18N

        String author = info.getAuthor().toString();
        String committer = info.getCommitter().toString();
        sb.append(NbBundle.getMessage(CommitAction.class, "MSG_CommitAction.logCommit.title")).append("\n"); //NOI18N
        sb.append(lbrevision);
        sb.append(info.getRevision());
        sb.append('\n'); // NOI18N
        sb.append(lbauthor);
        sb.append(author);
        sb.append('\n'); // NOI18N
        if (!author.equals(committer)) {
            sb.append(lbcommitter);
            sb.append(committer);
            sb.append('\n'); // NOI18N
        }
        sb.append(lbdate);
        sb.append(DateFormat.getDateTimeInstance().format(new Date(info.getCommitTime())));
        sb.append('\n'); // NOI18N
        sb.append(lbsummary);
        int prefixLen = lbsummary.length();
        sb.append(formatMultiLine(prefixLen, info.getFullMessage()));
        sb.append('\n'); // NOI18N
    }
    
    private static String formatMultiLine (int prefixLen, String message) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < prefixLen; ++i) {
            sb.append(" "); //NOI18N
        }
        String prefix = sb.toString();
        String[] lines = message.split("\n"); //NOI18N
        sb = new StringBuilder(lines.length > 0 ? lines[0] : ""); //NOI18N
        for (int i = 1; i < lines.length; ++i) {
            sb.append("\n").append(prefix).append(lines[i]); //NOI18N
        }
        return sb.toString();
    }

    /**
     * Forces refresh of diff sidebars for open files belonging to the given repository
     * @param repository 
     */
    public static void headChanged (File repository) {
        Set<File> openFiles = Utils.getOpenFiles();
        for (Iterator<File> it = openFiles.iterator(); it.hasNext(); ) {
            File file = it.next();
            if (!repository.equals(Git.getInstance().getRepositoryRoot(file))) {
                it.remove();
            }
        }
        if (!openFiles.isEmpty()) {
            Git.getInstance().headChanged(openFiles);
        }
    }

    public static boolean isRepositoryLocked (File repository) {
        return new File(getGitFolderForRoot(repository), INDEX_LOCK).exists(); //NOI18N
    }

    public static void openInRevision (File originalFile, String revision1, int lineNumber,
            String revisionToOpen, boolean showAnnotations, ProgressMonitor pm) throws IOException {
        File file1 = VersionsCache.getInstance().getFileRevision(originalFile, revision1, pm);
        if (file1 == null) { // can be null if the file does not exist or is empty in the given revision
            file1 = File.createTempFile("tmp", "-" + originalFile.getName(), Utils.getTempFolder()); //NOI18N
            file1.deleteOnExit();
        }
        if (pm.isCanceled()) {
            return;
        }
        File file = VersionsCache.getInstance().getFileRevision(originalFile, revisionToOpen, pm);
        if (file == null) { // can be null if the file does not exist or is empty in the given revision
            file = File.createTempFile("tmp", "-" + originalFile.getName(), Utils.getTempFolder()); //NOI18N
            file.deleteOnExit();
        }
        if (pm.isCanceled()) {
            return;
        }
        int matchingLine = DiffUtils.getMatchingLine(file1, file, lineNumber);
        openInRevision(file, originalFile, matchingLine, revisionToOpen, showAnnotations, pm);
    }
    
    public static void openInRevision (File originalFile, int lineNumber, String revision,
            boolean showAnnotations, ProgressMonitor pm) throws IOException {
        File file = VersionsCache.getInstance().getFileRevision(originalFile, revision, pm);
        if (pm.isCanceled()) {
            return;
        }
        if (file == null) { // can be null if the file does not exist or is empty in the given revision
            file = File.createTempFile("tmp", "-" + originalFile.getName(), Utils.getTempFolder()); //NOI18N
            file.deleteOnExit();
        }
        openInRevision(file, originalFile, lineNumber, revision, showAnnotations, pm);
    }

    private static void openInRevision (final File fileToOpen, final File originalFile, final int lineNumber, final String revision, boolean showAnnotations, ProgressMonitor pm) throws IOException {
        final FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(fileToOpen));
        EditorCookie ec = null;
        org.openide.cookies.OpenCookie oc = null;
        try {
            DataObject dobj = DataObject.find(fo);
            ec = dobj.getCookie(EditorCookie.class);
            oc = dobj.getCookie(org.openide.cookies.OpenCookie.class);
        } catch (DataObjectNotFoundException ex) {
            Logger.getLogger(GitUtils.class.getName()).log(Level.FINE, null, ex);
        }
        if (ec == null && oc != null) {
            oc.open();
        } else {
            CloneableEditorSupport ces = org.netbeans.modules.versioning.util.Utils.openFile(fo, revision.substring(0, 7));
            if (showAnnotations && ces != null && !pm.isCanceled()) {
                final org.openide.text.CloneableEditorSupport support = ces;
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        javax.swing.JEditorPane[] panes = support.getOpenedPanes();
                        if (panes != null) {
                            if (lineNumber >= 0 && lineNumber < support.getLineSet().getLines().size()) {
                                support.getLineSet().getCurrent(lineNumber).show(Line.ShowOpenType.NONE, Line.ShowVisibilityType.FRONT);
                            }
                            SystemAction.get(AnnotateAction.class).showAnnotations(panes[0], originalFile, revision);
                        }
                    }
                });
            }
        }
    }

    public static Map<File, Set<File>> sortByRepository (Collection<File> files) {
        Map<File, Set<File>> sorted = new HashMap<File, Set<File>>(5);
        for (File f : files) {
            File repository = Git.getInstance().getRepositoryRoot(f);
            if (repository != null) {
                Set<File> repoFiles = sorted.get(repository);
                if (repoFiles == null) {
                    repoFiles = new HashSet<File>();
                    sorted.put(repository, repoFiles);
                }
                repoFiles.add(f);
            }
        }
        return sorted;
    }
    
    public static boolean contains (Collection<File> roots, File file) {
        for (File root : roots) {
            if (Utils.isAncestorOrEqual(root, file)) {
                return true;
            }
        }
        return false;
    }
    
    private static final String REF_SPEC_PATTERN = "+refs/heads/{0}:refs/remotes/{1}/{0}"; //NOI18N
    private static final String REF_PUSHSPEC_PATTERN = "refs/heads/{0}:refs/heads/{1}"; //NOI18N
    private static final String REF_TAG_PUSHSPEC_PATTERN = "refs/tags/{0}:refs/tags/{0}"; //NOI18N

    public static String getRefSpec(GitBranch branch, String remoteName) {
        return MessageFormat.format(REF_SPEC_PATTERN, branch.getName(), remoteName);
    }

    public static String getRefSpec (String branchName, String remoteName) {
        return MessageFormat.format(REF_SPEC_PATTERN, branchName, remoteName);
    }

    public static String getPushRefSpec (String branchName, String remoteRepositoryBranchName) {
        return MessageFormat.format(REF_PUSHSPEC_PATTERN, branchName, remoteRepositoryBranchName);
    }

    public static String getPushTagRefSpec (String tagName) {
        return MessageFormat.format(REF_TAG_PUSHSPEC_PATTERN, tagName);
    }

    public static <T> T runWithoutIndexing (Callable<T> callable, List<File> files) throws GitException {
        return runWithoutIndexing(callable, files.toArray(new File[files.size()]));
    }

    static ThreadLocal<Set<File>> indexingFiles = new ThreadLocal<Set<File>>();
    public static <T> T runWithoutIndexing (Callable<T> callable, File... files) throws GitException {
        try {
            Set<File> recursiveRoots = indexingFiles.get();
            if (recursiveRoots != null) {
                assert indexingFilesSubtree(recursiveRoots, files) 
                        : "Recursive call does not permit different roots: " 
                        + recursiveRoots + " vs. " + Arrays.asList(files);
                return callable.call();
            } else {
                try {
                    if (Git.LOG.isLoggable(Level.FINER)) {
                        Git.LOG.log(Level.FINER, "Running block in indexing bridge: on {0}", Arrays.asList(files)); //NOI18N
                    }
                    indexingFiles.set(new HashSet<File>(Arrays.asList(files)));
                    return IndexingBridge.getInstance().runWithoutIndexing(callable, files);
                } finally {
                    indexingFiles.remove();
                }
            }
        } catch (GitException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new GitException("Cannot run without indexing due to: " + ex.getMessage(), ex); //NOI18N
        }
    }

    private static boolean indexingFilesSubtree (Set<File> recursiveRoots, File[] files) {
        for (File f : files) {
            if (!recursiveRoots.contains(f)) {
                boolean contained = false;
                for (File root : recursiveRoots) {
                    if (Utils.isAncestorOrEqual(root, f)) {
                        contained = true;
                        break;
                    }
                }
                if (!contained) {
                    return false;
                }
            }
        }
        return true;
    }

    private static class NullProgressMonitor extends ProgressMonitor {

        @Override
        public boolean isCanceled () {
            return false;
        }

        @Override
        public void started (String command) {
        }

        @Override
        public void finished () {
        }

        @Override
        public void preparationsFailed (String message) {
        }

        @Override
        public void notifyError (String message) {
        }

        @Override
        public void notifyWarning (String message) {
        }

    }
    
    private GitUtils() {
    }
}
