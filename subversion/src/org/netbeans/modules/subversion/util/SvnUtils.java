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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.subversion.util;

import java.awt.EventQueue;
import java.net.MalformedURLException;
import org.netbeans.modules.subversion.client.SvnClient;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.openide.util.Lookup;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.netbeans.api.project.*;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.modules.subversion.FileStatusCache;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.FileInformation;
import java.io.*;
import java.io.File;
import java.text.MessageFormat;
import java.util.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.netbeans.modules.subversion.SubversionVCS;
import org.netbeans.modules.subversion.SvnFileNode;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.WorkingCopyAttributesCache;
import org.netbeans.modules.subversion.client.PropertiesClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.options.AnnotationExpression;
import org.netbeans.modules.subversion.ui.commit.CommitOptions;
import org.netbeans.modules.subversion.ui.diff.Setup;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.versioning.util.FileSelector;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.tigris.subversion.svnclientadapter.*;
import org.tigris.subversion.svnclientadapter.utils.SVNUrlUtils;

/**
 * Subversion-specific utilities.
 *
 * @author Maros Sandor
 */
public class SvnUtils {

    public static final String SVN_ADMIN_DIR;
    public static final String SVN_ENTRIES_DIR;
    private static final Pattern metadataPattern;

    public static final HashSet<Character> autoEscapedCharacters = new HashSet<Character>(6);
    static {
        autoEscapedCharacters.add(';');
        autoEscapedCharacters.add('?');
        autoEscapedCharacters.add('#');
        autoEscapedCharacters.add('%');
        autoEscapedCharacters.add('[');
        autoEscapedCharacters.add(']');
    }

    static {
        if (Utilities.isWindows()) {
            String env = System.getenv("SVN_ASP_DOT_NET_HACK");
            if (env != null) {
                SVN_ADMIN_DIR = "_svn";
            } else {
                SVN_ADMIN_DIR = ".svn";
            }
        } else {
            SVN_ADMIN_DIR = ".svn";
        }
        SVN_ENTRIES_DIR = SVN_ADMIN_DIR + "/entries";
        metadataPattern = Pattern.compile(".*\\" + File.separatorChar + SVN_ADMIN_DIR + "(\\" + File.separatorChar + ".*|$)");
    }

    private static final FileFilter svnFileFilter = new FileFilter() {
        public boolean accept(File pathname) {
            if (isAdministrative(pathname)) return false;
            return SharabilityQuery.getSharability(pathname) != SharabilityQuery.NOT_SHARABLE;
        }
    };

    /**
     * Creates annotation format string.
     * @param format format specified by the user, e.g. [{status}]
     * @return modified format, e.g. [{0}]
     */
    public static String createAnnotationFormat(final String format) {
        String string = format;
        string = Utils.skipUnsupportedVariables(string, new String[]{"{status}", "{folder}", "{revision}", "{mime_type}"});     // NOI18N
        string = string.replaceAll("\\{revision\\}", "\\{0\\}");            // NOI18N
        string = string.replaceAll("\\{status\\}", "\\{1\\}");              // NOI18N
        string = string.replaceAll("\\{folder\\}", "\\{2\\}");              // NOI18N
        string = string.replaceAll("\\{mime_type\\}", "\\{3\\}");           // NOI18N
        return string;
    }

    /**
     * Decodes and encodes given URL (e.g. xxx[] -> xxx%5B%5D)
     * @param url url to be encoded
     * @return encoded URL
     * @throws java.net.MalformedURLException
     */
    public static SVNUrl decodeAndEncodeUrl(SVNUrl url) throws MalformedURLException {
        return encodeUrl(decode(url));
    }

    /**
     * Encodes the SVN url to an acceptable format. It encodes all non-standard characters to a '%XX' form.
     * Unescaped characters: '/', ':', '@' (peg revision).
     * @param url url to be encoded
     * @return encoded URL
     * @throws java.net.MalformedURLException encoded URL is of a bad format anyway
     */
    public static SVNUrl encodeUrl(final SVNUrl url) throws MalformedURLException {
        String sUrl = url.toString();
        StringBuilder sb = new StringBuilder(sUrl.length());
        for (int i = 0; i < sUrl.length(); ++i) {
            Character c = sUrl.charAt(i);
            if (autoEscapedCharacters.contains(c)) {
                char[] chars = Character.toChars(c);
                for (int j = 0; j < chars.length; ++j) {
                    sb.append('%');
                    sb.append(Integer.toHexString(chars[j]).toUpperCase());
                }
            } else {
                sb.append(c);
            }
        }
        return new SVNUrl(sb.toString());
    }

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
        return new Context(new ArrayList(ctx.computeFiles(svnFileFilter)), new ArrayList(ctx.getRootFiles()), new ArrayList(ctx.getExclusions()));
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
     * @param fromCache if set to <code>true</code> reads status from cache otherwise uses I/O operations.
     * @return File [] array of activated files, or an empty array if any of examined files/folders does not have given status
     */
    public static Context getCurrentContext(Node[] nodes, int includingFileStatus, int includingFolderStatus, boolean fromCache) {
        Context context = getCurrentContext(nodes);
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        File [] files = context.getRootFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            FileInformation fi = fromCache ? cache.getCachedStatus(file) : cache.getStatus(file);
            int status;
            if (fi != null) {
                // status got from the cache and is known or got through I/O
                status = fi.getStatus();
            } else {
                // tried to get the cached value but it was not present in the cache
                status = FileInformation.STATUS_VERSIONED_UPTODATE;
            }
            if (file.isDirectory()) {
                if ((status & includingFolderStatus) == 0) return Context.Empty;
            } else {
                if ((status & includingFileStatus) == 0) return Context.Empty;
            }
        }
        return context;
    }

    /**
     * Semantics is similar to {@link org.openide.windows.TopComponent#getActivatedNodes()} except that this
     * method returns File objects instead od Nodes. Every node is examined for Files it represents. File and Folder
     * nodes represent their underlying files or folders. Project nodes are represented by their source groups. Other
     * logical nodes must provide FileObjects in their Lookup.
     * Does not read statuses from cache, but through I/O, might take a long time to finish.
     *
     * @param nodes null (then taken from windowsystem, it may be wrong on editor tabs #66700).
     * @param includingFileStatus if any activated file does not have this CVS status, an empty array is returned
     * @param includingFolderStatus if any activated folder does not have this CVS status, an empty array is returned
     * @return File [] array of activated files, or an empty array if any of examined files/folders does not have given status
     */
    public static Context getCurrentContext(Node[] nodes, int includingFileStatus, int includingFolderStatus) {
        return getCurrentContext(nodes, includingFileStatus, includingFolderStatus, false);
    }

    /**
     * Validates annotation format text
     * @param format format to be validatet
     * @return <code>true</code> if the format is correct, <code>false</code> otherwise.
     */
    public static boolean isAnnotationFormatValid(final String format) {
        boolean retval = true;
        if (format != null) {
            try {
                new MessageFormat(format);
            } catch (IllegalArgumentException ex) {
                Subversion.LOG.log(Level.FINER, "Bad user input - annotation format", ex);
                retval = false;
            }
        }
        return retval;
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
                if (f != null && (cache.getStatus(f).getStatus() & FileInformation.STATUS_MANAGED) != 0) return true;
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
    public static void addProjectFiles(Collection<File> filteredFiles, Collection<File> rootFiles, Collection<File> rootFilesExclusions, Project project) {
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup [] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        for (int j = 0; j < sourceGroups.length; j++) {
            SourceGroup sourceGroup = sourceGroups[j];
            FileObject srcRootFo = sourceGroup.getRootFolder();
            File rootFile = FileUtil.toFile(srcRootFo);
            if (rootFile == null || (cache.getStatus(rootFile).getStatus() & FileInformation.STATUS_MANAGED) == 0) continue;
            rootFiles.add(rootFile);
            boolean containsSubprojects = false;
            FileObject [] rootChildren = srcRootFo.getChildren();
            Set<File> projectFiles = new HashSet<File>(rootChildren.length);
            for (int i = 0; i < rootChildren.length; i++) {
                FileObject rootChildFo = rootChildren[i];
                if (isAdministrative(rootChildFo.getNameExt())) continue;
                File child = FileUtil.toFile(rootChildFo);
                if (child == null) {
                    continue;
                }
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

    public static File [] toFileArray(Collection<FileObject> fileObjects) {
        Set<File> files = new HashSet<File>(fileObjects.size()*4/3+1);
        for (Iterator<FileObject> i = fileObjects.iterator(); i.hasNext();) {
            File f = FileUtil.toFile(i.next());
            if (f != null) {
                files.add(f);
            }
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
     * Evaluates if the given file is a svn administrative folder - [.svn|_svn]
     * @param file
     * @return true if the given file is a svn administrative folder, otherwise false
     */
    public static boolean isAdministrative(File file) {
        String name = file.getName();
        boolean administrative = isAdministrative(name);
        return ( administrative && !file.exists() ) ||
               ( administrative && file.exists() && file.isDirectory() ); // lets suppose it's administrative if file doesnt exist
    }

    /**
     * Evaluates if the given fileName is a svn administrative folder name - [.svn|_svn]
     * @param fileName
     * @return true if the given fileName is a svn administrative folder name, otherwise false
     */
    public static boolean isAdministrative(String fileName) {
        return fileName.equals(SVN_ADMIN_DIR); // NOI18N
    }

    /**
     * Tests whether a file or directory should receive the STATUS_NOTVERSIONED_NOTMANAGED status.
     * All files and folders that have a parent with either .svn/entries or _svn/entries file are
     * considered versioned.
     *
     * @param file a file or directory
     * @return false if the file should receive the STATUS_NOTVERSIONED_NOTMANAGED status, true otherwise
     */
    public static boolean isManaged(File file) {
        return VersioningSupport.getOwner(file) instanceof SubversionVCS && !isPartOfSubversionMetadata(file);
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
    public static String getRelativePath(File file) throws SVNClientException {
        String repositoryPath = null;

        List<String> path = new ArrayList<String>();
        SVNUrl repositoryURL = null;
        boolean fileIsManaged = false;
        while (isManaged(file)) {
            fileIsManaged = true;

            ISVNInfo info = null;
            try {
                SvnClient client = Subversion.getInstance().getClient(false);
                info = client.getInfoFromWorkingCopy(file);
            } catch (SVNClientException ex) {
                if (SvnClientExceptionHandler.isUnversionedResource(ex.getMessage()) == false) {
                    if (SvnClientExceptionHandler.isTooOldClientForWC(ex.getMessage())) {
                        // log this exception if needed and break the execution
                        WorkingCopyAttributesCache.getInstance().logUnsupportedWC(ex, file);
                    } else {
                        SvnClientExceptionHandler.notifyException(ex, false, false);
                    }
                }
            }

            if (info != null && info.getUrl() != null) {
                SVNUrl fileURL = decode(info.getUrl());
                repositoryURL = info.getRepository();

                if (fileURL != null && repositoryURL !=  null) {
                    String fileLink = fileURL.toString();
                    String repositoryLink = decode(repositoryURL).toString();
                    try {
                        repositoryPath = fileLink.substring(repositoryLink.length());
                    } catch (StringIndexOutOfBoundsException ex) {
                        // XXX delete try-catch after bugfix verification
                        Subversion.LOG.log(Level.INFO, "repoUrl: " + repositoryURL.toString() + "\nfileURL: " + fileLink, ex);
                        throw ex;
                    }

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
            File parent = file.getParentFile();
            if (parent == null) {
                // .svn in root folder
                break;
            } else {
                file = parent;
            }

        }
        if(repositoryURL == null && fileIsManaged) {
            Subversion.LOG.log(Level.WARNING, "no repository url found for managed file {0}", new Object[] {file});
            // The file is managed but we haven't found the repository URL in it's metadata -
            // this looks like the WC was created with a client < 1.3.0. I wouldn't mind for myself and
            // get the URL from the server, it's just that it could be quite a performance killer.
            // XXX and now i'm just currious how we will handle this if there will be some javahl or
            // pure java client suport -> without dispatching to our metadata parser
            throw new SVNClientException(NbBundle.getMessage(SvnUtils.class, "MSG_too_old_WC"));
        } else if(!fileIsManaged) {
            Subversion.LOG.log(Level.INFO, "no repository url found for not managed file {0}", new Object[] {file});
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
    public static SVNUrl getRepositoryRootUrl(File file) throws SVNClientException {
        SvnClient client;
        try {
            client = Subversion.getInstance().getClient(false);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, false, false);
            return null;
        }

        SVNUrl repositoryURL = null;
        boolean fileIsManaged = false;
        while (isManaged(file)) {
            fileIsManaged = true;
            ISVNInfo info = null;
            try {
                info = client.getInfoFromWorkingCopy(file);
            } catch (SVNClientException ex) {
                if (SvnClientExceptionHandler.isUnversionedResource(ex.getMessage()) == false) {
                    if (SvnClientExceptionHandler.isTooOldClientForWC(ex.getMessage())) {
                        // log this exception if needed and break the execution
                        WorkingCopyAttributesCache.getInstance().logUnsupportedWC(ex, file);
                    } else {
                        SvnClientExceptionHandler.notifyException(ex, false, false);
                    }
                }
            }

            if (info != null) {
                repositoryURL = decode(info.getRepository());
                if (repositoryURL != null) {
                    break;
                }
            }

            File parent = file.getParentFile();
            if (parent == null) {
                // .svn in root folder
                break;
            } else {
                file = parent;
            }

        }
        if(repositoryURL == null && fileIsManaged) {
            Subversion.LOG.log(Level.WARNING, "no repository url found for managed file {0}", new Object[] {file});
            // The file is managed but we haven't found the repository URL in it's metadata -
            // this looks like the WC was created with a client < 1.3.0. I wouldn't mind for myself and
            // get the URL from the server, it's just that it could be quite a performance killer.
            throw new SVNClientException(NbBundle.getMessage(SvnUtils.class, "MSG_too_old_WC"));
        } else if(!fileIsManaged) {
            Subversion.LOG.log(Level.INFO, "no repository url found for not managed file {0}", new Object[] {file});
            // XXX #168094 logging
            Level oldLevel = Subversion.LOG.getLevel();
            Subversion.LOG.setLevel(Level.FINE);
            Subversion.LOG.log(Level.INFO, "getRepositoryRootUrl: file {0} {1}", new Object[] {file, VersioningSupport.getOwner(file)});
            Subversion.LOG.setLevel(oldLevel);
            if (!file.exists()) {
                Subversion.LOG.log(Level.INFO, "getRepositoryRootUrl: file {0} does not exist", new Object[] {file});
            }
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
    public static SVNUrl getRepositoryUrl(File file) throws SVNClientException {

        StringBuffer path = new StringBuffer();
        SVNUrl fileURL = null;
        SvnClient client = null;
        try {
            client = Subversion.getInstance().getClient(false);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, false, false);
            return null;
        }
        boolean fileIsManaged = false;
        while (isManaged(file)) {
            fileIsManaged = true;

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
                if (SvnClientExceptionHandler.isUnversionedResource(ex.getMessage()) == false) {
                    if (SvnClientExceptionHandler.isTooOldClientForWC(ex.getMessage())) {
                        // log this exception if needed and break the execution
                        WorkingCopyAttributesCache.getInstance().logUnsupportedWC(ex, file);
                    } else {
                        SvnClientExceptionHandler.notifyException(ex, false, false);
                    }
                }
            }

            // slower fallback

            ISVNInfo info = null;
            try {
                info = client.getInfoFromWorkingCopy(file);
            } catch (SVNClientException ex) {
                if (SvnClientExceptionHandler.isUnversionedResource(ex.getMessage()) == false) {
                    SvnClientExceptionHandler.notifyException(ex, false, false);
                }
            }

            if (info != null) {
                fileURL = decode(info.getUrl());

                if (fileURL != null ) {
                    break;
                }
            }

            path.insert(0, file.getName()).insert(0, "/");
            File parent = file.getParentFile();
            if (parent == null) {
                // .svn in root folder
                break;
            } else {
                file = parent;
            }

        }
        if(fileURL == null && fileIsManaged) {
            Subversion.LOG.log(Level.WARNING, "no repository url found for managed file {0}", new Object[] {file});
            // The file is managed but we haven't found the URL in it's metadata -
            // this looks like the WC was created with a client < 1.3.0. I wouldn't mind for myself and
            // get the URL from the server, it's just that it could be quite a performance killer.
            throw new SVNClientException(NbBundle.getMessage(SvnUtils.class, "MSG_too_old_WC"));
        } else if(!fileIsManaged) {
            Subversion.LOG.log(Level.INFO, "no repository url found for not managed file {0}", new Object[] {file});
        }
        if (path.length() > 0) fileURL = fileURL.appendPath(path.toString());
        return fileURL;
    }

    /**
     * Returns repository urls for all versioned files underneath the given root
     *
     * @param root
     * @return
     * @throws SVNClientException
     */
    public static Map<File, SVNUrl> getRepositoryUrls(File root) throws SVNClientException {
        SVNUrl fileURL = null;
        SvnClient client = null;
        try {
            client = Subversion.getInstance().getClient(false);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, false, false);
            return null;
        }

        Map<File, SVNUrl> ret = new HashMap<File, SVNUrl>();
        try {
            ISVNStatus[] statuses = client.getStatus(root, true, true);
            for (ISVNStatus status : statuses) {
                if (status != null) {
                    fileURL = decode(status.getUrl());
                    if (fileURL != null) {
                        ret.put(status.getFile(), fileURL);
                    }
                }
            }
        } catch (SVNClientException ex) {
            if (SvnClientExceptionHandler.isUnversionedResource(ex.getMessage()) == false) {
                if (SvnClientExceptionHandler.isTooOldClientForWC(ex.getMessage())) {
                    // log this exception if needed and break the execution
                    WorkingCopyAttributesCache.getInstance().logUnsupportedWC(ex, root);
                } else {
                    SvnClientExceptionHandler.notifyException(ex, false, false);
                }
            }
        }

        return ret;
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
    public static SVNUrl decode(SVNUrl url) {
        if (url == null) return null;
        String s = url.toString();
        StringBuffer sb = new StringBuffer(s.length());

        boolean inQuery = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '?') {
                inQuery = true;
            } else if (c == '+' && inQuery) {
                sb.append(' ');
            } else if (isEncodedByte(c, s, i)) {
                List<Byte> byteList = new ArrayList<Byte>();
                do  {
                    byteList.add((byte) Integer.parseInt(s.substring(i + 1, i + 3), 16));
                    i += 3;
                    if (i >= s.length()) break;
                    c = s.charAt(i);
                } while(isEncodedByte(c, s, i));

                if(byteList.size() > 0) {
                    byte[] bytes = new byte[byteList.size()];
                    for(int ib = 0; ib < byteList.size(); ib++) {
                        bytes[ib] = byteList.get(ib);
                    }
                    try {
                        sb.append(new String(bytes, "UTF8"));
                    } catch (Exception e) {
                        Subversion.LOG.log(Level.INFO, null, e);  // oops
                    }
                    i--;
                }
            } else {
                sb.append(c);
            }
        }
        try {
            return new SVNUrl(sb.toString());
        } catch (java.net.MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isEncodedByte(char c, String s, int i) {
        return c == '%' && i + 2 < s.length() && isHexDigit(s.charAt(i + 1)) && isHexDigit(s.charAt(i + 2));
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
    public static String getRepositoryPath(File file) throws SVNClientException {
        SVNUrl url = getRepositoryUrl(file);
        SVNUrl rootUrl = getRepositoryRootUrl(file);
        return SVNUrlUtils.getRelativePath(rootUrl, url, true);
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
     * Removes all occurances of '\r' and replaces them with '\n'
     * @param text
     * @return
     */
    public static String fixLineEndings(String text) {
        return text.replaceAll("\r\n", "\n").replace('\r', '\n');
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
     * Utility method that returns all non-excluded modified files that are
     * under given roots (folders) and have one of specified statuses.
     *
     * @param context context to search
     * @param includeStatus bit mask of file statuses to include in result
     * @return File [] array of Files having specified status
     */
    public static File [] getModifiedFiles(Context context, int includeStatus) {
        File[] all = Subversion.getInstance().getStatusCache().listFiles(context, includeStatus);
        List<File> files = new ArrayList<File>();
        for (int i = 0; i < all.length; i++) {
            File file = all[i];
            String path = file.getAbsolutePath();
            if (SvnModuleConfig.getDefault().isExcludedFromCommit(path) == false) {
                files.add(file);
            }
        }

        // ensure that command roots (files that were explicitly selected by user) are included in Diff
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        File [] rootFiles = context.getRootFiles();
        for (int i = 0; i < rootFiles.length; i++) {
            File file = rootFiles[i];
            if (file.isFile() && (cache.getStatus(file).getStatus() & includeStatus) != 0 && !files.contains(file)) {
                files.add(file);
            }
        }
        return files.toArray(new File[files.size()]);
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

    /**
     * Returns a symbolic branch/tag name if the given file lives
     * in a location specified by an AnnotationExpression
     *
     * @param file
     * @return name or null
     */
    public static String getCopy(File file) {
        SVNUrl url;
        try {
            url = getRepositoryUrl(file);
        } catch (SVNClientException ex) {
            if (!SvnClientExceptionHandler.isTooOldClientForWC(ex.getMessage())) {
                SvnClientExceptionHandler.notifyException(ex, false, false);
            }
            return null;
        }
        return getCopy(url, SvnModuleConfig.getDefault().getAnnotationExpresions());
    }

    /**
     * Returns a symbolic branch/tag name if the given url represents
     * a location specified by an AnnotationExpression
     *
     * @param url
     * @return name or null
     */
    public static String getCopy(SVNUrl url) {
        return getCopy(url, SvnModuleConfig.getDefault().getAnnotationExpresions());
    }

    /**
     * Returns a symbolic branch/tag name if the given url represents
     * a location specified by an AnnotationExpression
     *
     * @param url
     * @param annotationExpressions
     * @return name or null
     */
    private static String getCopy(SVNUrl url, List<AnnotationExpression> annotationExpressions) {
        if (url != null) {
            String urlString = url.toString();
            for (Iterator<AnnotationExpression> it = annotationExpressions.iterator(); it.hasNext();) {
                String name = it.next().getCopyName(urlString);
                if(name != null) {
                    return name;
                }
            }
        }
        return null;
    }

    /**
     * Refreshes statuses of this folder and all its parent folders up to filesystem root.
     *
     * @param folder folder to refresh
     */
    public static void refreshParents(File folder) {
        if (folder == null) return;
        refreshParents(folder.getParentFile());
        Subversion.getInstance().getStatusCache().refresh(folder, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
    }

    /**
     * Rips an eventual username off - e.g. user@svn.host.org
     *
     * @param host - hostname with a userneame
     * @return host - hostname without the username
     */
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
        if(patterns == null) return ret;
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
                Subversion.LOG.log(Level.INFO, null, e);
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
        exp = patchRegExpClassCharacters(exp);
        exp = exp.replaceAll("\\{", "\\\\{");   // NOI18N
        exp = exp.replaceAll("\\}", "\\\\}");   // NOI18N
        exp = exp.replaceAll("\\(", "\\\\(");   // NOI18N
        exp = exp.replaceAll("\\)", "\\\\)");   // NOI18N
        exp = exp.replaceAll("\\+", "\\\\+");   // NOI18N
        exp = exp.replaceAll("\\|", "\\\\|");   // NOI18N

        return exp;
    }


    /*
     * Returns a string having characters <code>[</code> and <code>]</code> escaped if they do not represent
     * a character class definition.
     *
     * @param exp string to be escaped
     * @return string with escaped characters
     */
    private static String patchRegExpClassCharacters (String exp) {
        LinkedList<Integer> indexes = new LinkedList<Integer>();
        StringBuilder builder = new StringBuilder(exp.length());

        for (int index = 0, builderIndex = 0; index < exp.length(); ++index, ++builderIndex) {
            char ch = exp.charAt(index);
            if (ch == '\\') {       // NOI18N
                // backslash is escaped and added
                builder.append(ch);
                if (++index < exp.length()) {
                    ++builderIndex;
                    builder.append(exp.charAt(index));
                }
            } else if (ch == '[') { // NOI18N
                // openning parenthesis is added and its position is saved for possible later escaping
                builder.append(ch);
                indexes.add(builderIndex);
            } else {
                if (ch == ']') { // NOI18N
                    // closing parenthesis consumes the last opening parenthesis (if that exists), otherwise escapes itself
                    if (indexes.isEmpty()) {
                        builder.append("\\");   // NOI18N
                        ++builderIndex;
                    } else {
                        indexes.removeLast();
                    }
                }
                builder.append(ch); // append the current character
            }
        }

        for (Integer index : indexes) {
            // escapes all opening parenthesis that have no closing
            builder.insert(index, "\\");   // NOI18N
        }

        return builder.toString();
    }

    /**
     * Reads the svn:mime-type property or uses content analysis for unversioned files.
     *
     * @param file file to examine
     * @return String mime type of the file (or best guess)
     */
    public static String getMimeType(File file) {
        FileObject fo = FileUtil.toFileObject(file);
        String foMime;
        if (fo == null) {
            foMime = "content/unknown";
        } else {
            foMime = fo.getMIMEType();
        }
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        if ((cache.getStatus(file).getStatus() & FileInformation.STATUS_VERSIONED) == 0) {
            if(foMime.startsWith("text")) {
                return foMime;
            }
            return Utils.isFileContentText(file) ? "text/plain" : "application/octet-stream";
        } else {
            PropertiesClient client = new PropertiesClient(file);
            try {
                byte [] mimeProperty = client.getProperties().get("svn:mime-type");
                if (mimeProperty == null) {                    
                    return Utils.isFileContentText(file) ? "text/plain" : "application/octet-stream";
                }
                return new String(mimeProperty);
            } catch (IOException e) {
                return foMime;
            }
        }
    }

    public static <T> boolean equals(List<T> l1, List<T> l2) {

        if(l1 == null && l2 == null) {
            return true;
        }

        if( (l1 == null && l2 != null && l2.size() > 0) ||
            (l2 == null && l1 != null && l1.size() > 0) )
        {
            return false;
        }

        if(l1.size() != l2.size()) {
            return false;
        }

        for(T t : l1) {
            if(!l2.contains(t)) {
                return false;
            }
        }

        return true;
    }

    public static List<File> listRecursively(File root) {
        List<File> ret = new ArrayList<File>();
        if(root == null) {
            return ret;
        }
        ret.add(root);
        File[] files = root.listFiles();
        if(files != null) {
            for (File file : files) {
                if(!(isPartOfSubversionMetadata(file) || isAdministrative(file))) {
                    if(file.isDirectory()) {
                        ret.addAll(listRecursively(file));
                    } else {
                        ret.add(file);
                    }
                }
            }
        }
        return ret;
    }

    public static SvnFileNode [] getNodes(Context context, int includeStatus) {
        File [] files = Subversion.getInstance().getStatusCache().listFiles(context, includeStatus);
        SvnFileNode [] nodes = new SvnFileNode[files.length];
        for (int i = 0; i < files.length; i++) {
            nodes[i] = new SvnFileNode(files[i]);
        }
        return nodes;
    }

    public static SVNRevision toSvnRevision(String revision) {
        SVNRevision svnrevision;
        if (Setup.REVISION_HEAD.equals(revision) || SVNRevision.HEAD.toString().equals(revision)) {
            svnrevision = SVNRevision.HEAD;
        } else if (SVNRevision.BASE.toString().equals(revision)) {
            svnrevision = SVNRevision.BASE;
        } else {
            svnrevision = new SVNRevision.Number(Long.parseLong(revision));
        }
        return svnrevision;
    }

    // XXX JAVAHL
    public static ISVNLogMessage[] getLogMessages(ISVNClientAdapter client, SVNUrl rootUrl, String[] paths, SVNRevision fromRevision, SVNRevision toRevision, boolean stopOnCopy, boolean fetchChangePath) throws SVNClientException {
        Set<Long> alreadyHere = new HashSet<Long>();
        List<ISVNLogMessage> ret = new ArrayList<ISVNLogMessage>();
        boolean sorted = true;
        long lastRevNum = -1;
        for (String path : paths) {
            ISVNLogMessage[] logs = client.getLogMessages(rootUrl.appendPath(path), null, fromRevision, toRevision, stopOnCopy, fetchChangePath, 0);
            for (ISVNLogMessage log : logs) {
                long revNum = log.getRevision().getNumber();
                if(!alreadyHere.contains(revNum)) {
                    ret.add(log);
                    alreadyHere.add(revNum);
                    sorted &= (revNum > lastRevNum);
                    lastRevNum = revNum;
                }
            }
        }
        if (!sorted) {
            Collections.sort(ret, new Comparator<ISVNLogMessage>() {
                public int compare(ISVNLogMessage m1, ISVNLogMessage m2) {
                    long revNum1 = m1.getRevision().getNumber();
                    long revNum2 = m2.getRevision().getNumber();
                    return (revNum1 == revNum2) ? 0
                                                : (revNum1 > revNum2) ? 1 : -1;
                }
            });
        }
        return ret.toArray(new ISVNLogMessage[ret.size()]);
    }

    private static Logger TY9_LOG = null;
    public static void logT9Y(String msg) {
        if(TY9_LOG == null) TY9_LOG = Logger.getLogger("org.netbeans.modules.subversion.t9y");
        TY9_LOG.log(Level.FINEST, msg);
    }

    /**
     * Either returns all root files from a context if they belong to the same DataObject or 
     * opens a dialog to pick a file from all managed roots in the given context.
     *
     * @param ctx the given context to choose the root from
     * @return might be one of the following:<br>
     *          <ul>
     *              <li>all root files from a context
     *              <li>the context root picked by the one picked by the user
     *              <li>null if there are more than one root file not belonging
     *                  to the same DataObject and none was chosen by the user
     *          </ul>
     */
    public static File[] getActionRoots(Context ctx) {
        File[] roots = ctx.getRootFiles();
        List<File> l = new ArrayList<File>();

        // filter managed roots
        for (File file : roots) {
            if(isManaged(file)) {
                l.add(file);
            }
        }

        roots = l.toArray(new File[l.size()]);
        if(Utils.shareCommonDataObject(roots)) {
            return roots;
        }

        if(roots.length > 1) {
            // more than one managed root => need a dlg
            FileSelector fs = new FileSelector(
                    NbBundle.getMessage(SvnUtils.class, "LBL_FileSelector_Title"),
                    NbBundle.getMessage(SvnUtils.class, "FileSelector.jLabel1.text"),
                    new HelpCtx("org.netbeans.modules.subversion.FileSelector"),
                    SvnModuleConfig.getDefault().getPreferences());
            if(fs.show(roots)) {
                return new File[ ]{ fs.getSelectedFile()};
            } else {
                return null;
            }
        } else {
            return new File[] {roots[0]};
        }
    }

    /**
     * Returns the primary file from a DataObject if there is some
     * @param roots
     * @return
     */
    public static File getPrimaryFile(File file) {
        File primaryFile = null;
        FileObject fo = FileUtil.toFileObject(file);
        if(fo != null) {
            DataObject dao = null;
            try {
                dao = DataObject.find(fo);
            } catch (DataObjectNotFoundException ex) {
                Subversion.LOG.log(Level.INFO, "No DataObject found for " + file, ex);
            }
            if(dao != null) {
                primaryFile = FileUtil.toFile(dao.getPrimaryFile());
            }
        }
        if(primaryFile == null) {
            primaryFile = file; // consider it a fallback
        }
        return primaryFile;
    }

    /**
     * @return <code>true</code> if currently running Java Platform is 64-bit, <code>false</code> othrewise
     */
    public static boolean isJava64 () {
        String javaVMName = System.getProperty("java.vm.name"); // NOI18N
        if (javaVMName == null) {
            return false;
        }

        if (javaVMName.toLowerCase().contains("64-bit")) { // NOI18N
            return true;
        }
        return false;
    }

    public static CommitOptions[] createDefaultCommitOptions(SvnFileNode[] nodes, boolean excludeNew) {
        // NOI18N
        CommitOptions[] commitOptions = new CommitOptions[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            SvnFileNode node = nodes[i];
            File file = node.getFile();
            if (SvnModuleConfig.getDefault().isExcludedFromCommit(file.getAbsolutePath())) {
                commitOptions[i] = CommitOptions.EXCLUDE;
            } else {
                commitOptions[i] = getDefaultCommitOptions(node, excludeNew);
            }
        }
        return commitOptions;
    }

    /**
     * Parses the url and returns tunnel name which follows svn+ in protocol
     * @param urlString url with protocol starting with svn+
     * @return
     */
    public static String getTunnelName(String urlString) {
        assert urlString.startsWith("svn+");                            //NOI18N
        int idx = urlString.indexOf(":", 4);                            //NOI18N
        if (idx < 0) {
            idx = urlString.length();
        }
        return urlString.substring(4, idx);
    }

    public static void openInRevision(final File originalFile, final SVNUrl repoUrl, final SVNUrl fileUrl, final SVNRevision svnRevision, final SVNRevision pegRevision, boolean showAnnotations) {
        File file;
        String rev = svnRevision.toString();
        try {
            file = org.netbeans.modules.subversion.VersionsCache.getInstance().getFileRevision(repoUrl, fileUrl, rev, pegRevision.toString(), originalFile.getName());
        } catch (IOException e) {
            SvnClientExceptionHandler.notifyException(e, true, true);
            return;
        }

        final FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        EditorCookie ec = null;
        org.openide.cookies.OpenCookie oc = null;
        try {
            DataObject dobj = DataObject.find(fo);
            ec = dobj.getCookie(EditorCookie.class);
            oc = dobj.getCookie(org.openide.cookies.OpenCookie.class);
        } catch (DataObjectNotFoundException ex) {
            Subversion.LOG.log(Level.FINE, null, ex);
        }
        org.openide.text.CloneableEditorSupport ces = null;
        if (ec == null && oc != null) {
            oc.open();
        } else {
            ces = org.netbeans.modules.versioning.util.Utils.openFile(fo, rev);
        }
        if (showAnnotations) {
            if (ces == null) {
                return;
            } else {
                final org.openide.text.CloneableEditorSupport support = ces;
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        javax.swing.JEditorPane[] panes = support.getOpenedPanes();
                        if (panes != null) {
                            org.netbeans.modules.subversion.ui.blame.BlameAction.showAnnotations(panes[0], originalFile, svnRevision);
                        }
                    }
                });
            }
        }
    }

    /**
     * Similar to {@link #getDefaultCommitOptions(org.netbeans.modules.subversion.SvnFileNode, boolean) } but does not consider exclusions, so
     * the return value will always be a variation of {@link CommitOptions#COMMIT } (unless excludeNew is set to true)
     * @param node
     * @param excludeNew
     * @return
     */
    public static CommitOptions getDefaultCommitOptions (SvnFileNode node, boolean excludeNew) {
        CommitOptions commitOptions;
        switch (node.getInformation().getStatus()) {
            case FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY:
                commitOptions = excludeNew ? CommitOptions.EXCLUDE : getDefaultCommitOptions(node);
                break;
            case FileInformation.STATUS_VERSIONED_DELETEDLOCALLY:
            case FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY:
                commitOptions = CommitOptions.COMMIT_REMOVE;
                break;
            default:
                commitOptions = CommitOptions.COMMIT;
        }
        return commitOptions;
    }

    private static CommitOptions getDefaultCommitOptions(SvnFileNode node) {
        if (node.isFile()) {
            if (node.getMimeType().startsWith("text")) { //NOI18N
                return CommitOptions.ADD_TEXT;
            } else {
                return CommitOptions.ADD_BINARY;
            }
        } else {
            return CommitOptions.ADD_DIRECTORY;
        }
    }
}
