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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.subversion;

import java.io.File;
import java.util.regex.*;
import org.netbeans.modules.versioning.util.ListenersSupport;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.turbo.Turbo;
import org.netbeans.modules.turbo.CustomProviders;
import org.openide.filesystems.FileUtil;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.WeakSet;
import org.tigris.subversion.svnclientadapter.*;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNRevision.Number;

/**
 * Central part of Subversion status management, deduces and caches statuses of files under version control.
 * 
 * @author Maros Sandor
 */
public class FileStatusCache {

    /**
     * Indicates that status of a file changed and listeners SHOULD check new status 
     * values if they are interested in this file.
     * First parameter: File whose status changes
     * Second parameter: old FileInformation object, may be null
     * Third parameter: new FileInformation object
     */
    public static final Object EVENT_FILE_STATUS_CHANGED = new Object();

    /**
     * A special map saying that no file inside the folder is managed.
     */ 
    private static final Map<File, FileInformation> NOT_MANAGED_MAP = new NotManagedMap();
       
    public static final ISVNStatus REPOSITORY_STATUS_UNKNOWN  = null;

    // Constant FileInformation objects that can be safely reused
    // Files that have a revision number cannot share FileInformation objects 
    private static final FileInformation FILE_INFORMATION_EXCLUDED = new FileInformation(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, false);
    private static final FileInformation FILE_INFORMATION_EXCLUDED_DIRECTORY = new FileInformation(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, true);
    private static final FileInformation FILE_INFORMATION_UPTODATE_DIRECTORY = new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE, true);
    private static final FileInformation FILE_INFORMATION_NOTMANAGED = new FileInformation(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, false);
    private static final FileInformation FILE_INFORMATION_NOTMANAGED_DIRECTORY = new FileInformation(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, true);
    private static final FileInformation FILE_INFORMATION_UNKNOWN = new FileInformation(FileInformation.STATUS_UNKNOWN, false);

    /**
     * Auxiliary conflict file siblings
     * After update: *.r#, *.mine
     * After merge: *.working, *.merge-right.r#, *.metge-left.r#
     */
    private static final Pattern auxConflictPattern = Pattern.compile("(.*?)\\.((r\\d+)|(mine)|" + // NOI18N
        "(working)|(merge-right\\.r\\d+)|((merge-left.r\\d+)))$"); // NOI18N

    /*
     * Holds three kinds of information: what folders we have scanned, what files we have found
     * and what statuses of these files are.
     * If a directory is not found as a key in the map, we have not scanned it yet.
     * If it has been scanned, it maps to a Set of files that were found somehow out of sync with the
     * repository (have any other status then up-to-date). In case all files are up-to-date, it maps
     * to Collections.EMPTY_MAP. Entries in this map are created as directories are scanne, are never removed and
     * are updated by the refresh method.
     */

    private final Turbo     turbo;
    
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.subversion.FileStatusCache"); //NOI18N
    /**
     * Indicates if the cache index is ready 
     */
    private boolean ready = false;

    /**
     * Identifies attribute that holds information about all non STATUS_VERSIONED_UPTODATE files.
     *
     * <p>Key type: File identifying a folder
     * <p>Value type: Map&lt;File, FileInformation>
     */
    private final String FILE_STATUS_MAP = DiskMapTurboProvider.ATTR_STATUS_MAP;

    private DiskMapTurboProvider        cacheProvider;    
    private Subversion                  svn;

    private RequestProcessor rp = new RequestProcessor("Subversion - file status refresh", 1); // NOI18N    
    private final Set<File> filesToRefresh = new HashSet<File>();
    private RequestProcessor.Task refreshTask;
    private final FileLabelCache labelsCache;

    FileStatusCache() {
        this.svn = Subversion.getInstance();
        cacheProvider = new DiskMapTurboProvider();
        
        turbo = Turbo.createCustom(new CustomProviders() {
            private final Set providers = Collections.singleton(cacheProvider);
            public Iterator providers() {
                return providers.iterator();
            }
        }, 200, 5000);
    
        refreshTask = rp.create( new Runnable() {
            public void run() {
                File[] fileArray;            
                synchronized(filesToRefresh) {
                    fileArray = filesToRefresh.toArray(new File[filesToRefresh.size()]);
                    filesToRefresh.clear();
                }    
                for (File file : fileArray) {
                    refresh(file, REPOSITORY_STATUS_UNKNOWN);
                }
            }
        });
        labelsCache = new FileLabelCache(this);
    }

    // --- Public interface -------------------------------------------------

    /**
     * Evaluates if there are any files with the given status under the given roots
     *
     * @param context context to examine
     * @param includeStatus limit returned files to those having one of supplied statuses
     * @return true if there are any files with the given status otherwise false
     */
    public boolean containsFiles(Context context, int includeStatus) {
        long ts = System.currentTimeMillis();
        try {
            File[] roots = context.getRootFiles();

            // check to roots if they already apply to the given status
            if(containsFilesIntern(roots, includeStatus, false)) {
                return true;
            }
            // check all files underneath the roots
            return containsFiles(roots, includeStatus);
        } finally {
            if(LOG.isLoggable(Level.FINE)) {
                LOG.fine(" containsFiles(Context, int) took " + (System.currentTimeMillis() - ts));
            }
        }
    }

    /**
     * Evaluates if there are any files with the given status under the given roots
     *
     * @param rootFiles context to examine
     * @param includeStatus limit returned files to those having one of supplied statuses
     * @return true if there are any files with the given status otherwise false
     */
    public boolean containsFiles(Set<File> rootFiles, int includeStatus) {
        long ts = System.currentTimeMillis();
        try {
            return containsFiles(rootFiles.toArray(new File[rootFiles.size()]), includeStatus);
        } finally {
            if(LOG.isLoggable(Level.FINE)) {
                LOG.fine(" containsFiles(Set<File>, int) took " + (System.currentTimeMillis() - ts));
            }
        }
    }

    private boolean containsFiles(File[] roots, int includeStatus) {
        for (File root : roots) {
            if(containsFilesIntern(cacheProvider.getIndexValues(root, includeStatus), includeStatus, !VersioningSupport.isFlat(root))) {
                return true;
            }
        }
        return false;
    }

    private boolean containsFilesIntern(File[] indexRoots, int includeStatus, boolean recursively) {
        if(indexRoots == null || indexRoots.length == 0) {
            return false;
        }
        for (File root : indexRoots) {

            FileInformation fi = getCachedStatus(root);

            if((fi != null && (fi.getStatus() & includeStatus) != 0) &&
                !SvnModuleConfig.getDefault().isExcludedFromCommit(root.getAbsolutePath()))
            {
                return true;
            }
            File[] indexValues = cacheProvider.getIndexValues(root, includeStatus);
            if(recursively && containsFilesIntern(indexValues, includeStatus, recursively)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Lists <b>modified files</b> and all folders that are known to be inside
     * this folder. There are locally modified files present
     * plus any files that exist in the folder in the remote repository. It
     * returns all folders, including CVS folders.
     *    
     * @param dir folder to list
     * @return
     */
    public File [] listFiles(File dir) {
        Set<File> files = getScannedFiles(dir).keySet();
        return files.toArray(new File[files.size()]);
    }

    /**
     * Lists <b>interesting files</b> that are known to be inside given folders.
     * These are locally and remotely modified and ignored files.
     *
     * <p>Comapring to CVS this method returns both folders and files.
     *
     * @param roots context to examine
     * @param includeStatus limit returned files to those having one of supplied statuses
     * @return File [] array of interesting files
     */
    public File [] listFiles(File[] roots, int includeStatus) { 
        long ts = System.currentTimeMillis();
        try {
            Set<File> set = new HashSet<File>();

            // get all files with given status underneath the roots files;
            // do it recusively if root isn't a flat folder
            for (File root : roots) {
                set.addAll(listFiles(root, includeStatus, !VersioningSupport.isFlat(root)));
            }

            // check also the root files for status and add them eventualy
            set.addAll(listFilesIntern(roots, includeStatus, false));

            return set.toArray(new File[set.size()]);
        } finally {
            if(LOG.isLoggable(Level.FINE)) {
                LOG.fine(" listFiles(File[], int, boolean) took " + (System.currentTimeMillis() - ts));
            }
        }
    }

    /**
     * Lists <b>interesting files</b> that are known to be inside given folders.
     * These are locally and remotely modified and ignored files.
     *
     * <p>Comapring to CVS this method returns both folders and files.
     *
     * @param context context to examine
     * @param includeStatus limit returned files to those having one of supplied statuses
     * @return File [] array of interesting files
     */
    public File [] listFiles(Context context, int includeStatus) {
        long ts = System.currentTimeMillis();
        try {
            Set<File> set = new HashSet<File>();
            File [] roots = context.getRootFiles();

            // list all files applying to the status with
            // root being their ancestor or equal
            set.addAll(Arrays.asList(listFiles(roots, includeStatus)));

            // filter exclusions
            if (context.getExclusions().size() > 0) {
                for (File excluded : context.getExclusions()) {
                    for (Iterator i = set.iterator(); i.hasNext();) {
                        File file = (File) i.next();
                        if (SvnUtils.isParentOrEqual(excluded, file)) {
                            i.remove();
                        }
                    }
                }
            }
            return set.toArray(new File[set.size()]);
        } finally {
            if(LOG.isLoggable(Level.FINE)) {
                LOG.fine(" listFiles(Context, int) took " + (System.currentTimeMillis() - ts));
            }
        }
    }

    private Set<File> listFiles(File root, int includeStatus, boolean recursively) {
        return listFilesIntern(cacheProvider.getIndexValues(root, includeStatus), includeStatus, recursively);
    }

    private Set<File> listFilesIntern(File[] roots, int includeStatus, boolean recursively) {
        if(roots == null || roots.length == 0) {
            return Collections.EMPTY_SET;
        }
        Set<File> ret = new HashSet<File>();
        for (File root : roots) {
            if(recursively) {
                ret.addAll(listFilesIntern(cacheProvider.getIndexValues(root, includeStatus), includeStatus, recursively));
            }
            FileInformation fi = getCachedStatus(root);
            if(fi == null || (fi.getStatus() & includeStatus) == 0) {
                continue;
            }
            ret.add(root);
        }
        return ret;
    }

    /**
     * Determines the versioning status of a file. This method accesses disk and may block for a long period of time.
     * 
     * @param file file to get status for
     * @return FileInformation structure containing the file status
     * @see FileInformation
     */ 
    public FileInformation getStatus(File file) {
        if (SvnUtils.isAdministrative(file)) return FILE_INFORMATION_NOTMANAGED_DIRECTORY;
        File dir = file.getParentFile();
        if (dir == null) {
            return FILE_INFORMATION_NOTMANAGED; //default for filesystem roots 
        }
        Map files = getScannedFiles(dir);
        if (files == NOT_MANAGED_MAP) return FILE_INFORMATION_NOTMANAGED;
        FileInformation fi = (FileInformation) files.get(file);
        if (fi != null) {
            return fi;            
        }
        if (!exists(file)) return FILE_INFORMATION_UNKNOWN;
        if (file.isDirectory()) {
            return refresh(file, REPOSITORY_STATUS_UNKNOWN);
        } else {
            return new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE, false);
        }
    }

    /**
     * Looks up cached file status.
     * 
     * @param file file to check
     * @return give file's status or null if the file's status is not in cache
     */ 
    public FileInformation getCachedStatus(File file) {
        File parent = file.getParentFile();
        if (parent == null) return FILE_INFORMATION_NOTMANAGED_DIRECTORY;
        Map<File, FileInformation> files = (Map<File, FileInformation>) turbo.readEntry(parent, FILE_STATUS_MAP);
        return files != null ? files.get(file) : null;
    }

    /**
     * 
     * Refreshes the given files asynchrously
     * 
     * @param files files to be refreshed
     */
    public void refreshAsync(List<File> files) {
        refreshAsync(false, files.toArray(new File[files.size()]));
    }
    
    /**
     * 
     * Refreshes the given files asynchrously
     * 
     * @param files files to be refreshed
     */
    public void refreshAsync(File... files) {
        refreshAsync(false, files);
    }
    
    /**
     * 
     * Refreshes the given files asynchrously
     * 
     * @param files files to be refreshed
     * @param recursively if true all children are also refreshed
     */
    public void refreshAsync(final boolean recursively, final File... files) {
        if (files == null || files.length == 0) {
            return;
        }
        rp.post(new Runnable() {
            public void run() {
                synchronized (filesToRefresh) {
                    for (File file : files) {
                        if (recursively) {
                            filesToRefresh.addAll(SvnUtils.listRecursively(file));
                        } else {
                            filesToRefresh.add(file);
                        }
                    }
                }
                refreshTask.schedule(200);
            }
        });
    }

    /**
     * Refreshes the status of the file given the repository status. Repository status is filled
     * in when this method is called while processing server output. 
     *
     * @param file
     * @param repositoryStatus
     */ 
    public FileInformation refresh(File file, ISVNStatus repositoryStatus) {
        return refresh(file, repositoryStatus, false);
    }
    
    /**
     * Refreshes status of all files inside given context. Files that have some remote status, eg. REMOTELY_ADDED
     * are brought back to UPTODATE.
     * 
     * @param ctx context to refresh
     */ 
    public void refreshCached(Context ctx) {
        File [] files = listFiles(ctx, ~0);
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            refresh(file, REPOSITORY_STATUS_UNKNOWN);
        }
    }
    
    /**
     * Refreshes the status for the given file and all its children
     * 
     * @param root
     */
    public void refreshRecursively(File root) {  
        List<File> files = SvnUtils.listRecursively(root);
        for (File file : files) {
            refresh(file, REPOSITORY_STATUS_UNKNOWN);
        }        
    }    
    
    private FileInformation refresh(File file, ISVNStatus repositoryStatus, boolean forceChangeEvent) {
        
        boolean refreshDone = false;
        FileInformation current = null;
        FileInformation fi = null;
        File [] content = null; 
                
        synchronized (this) {
            File dir = file.getParentFile();
            if (dir == null) {
                return FILE_INFORMATION_NOTMANAGED; //default for filesystem roots 
            }
            Map<File, FileInformation> files = getScannedFiles(dir);
            if (files == NOT_MANAGED_MAP && repositoryStatus == REPOSITORY_STATUS_UNKNOWN) return FILE_INFORMATION_NOTMANAGED;
            current = files.get(file);

            ISVNStatus status = null;
            try {
                SvnClient client = Subversion.getInstance().getClient(false);
                status = client.getSingleStatus(file);
                if (status != null && SVNStatusKind.UNVERSIONED.equals(status.getTextStatus())) {
                    status = null;
                }
            } catch (SVNClientException e) {
                // svnClientAdapter does not return SVNStatusKind.UNVERSIONED!!!
                // unversioned resource is expected getSingleStatus()
                // does not return SVNStatusKind.UNVERSIONED but throws exception instead            
                // instead of throwing exception
                if (SvnClientExceptionHandler.isUnversionedResource(e.getMessage()) == false
                        && !SvnClientExceptionHandler.isTooOldClientForWC(e.getMessage())) {
                    // missing or damaged entries
                    // or ignored file
                    SvnClientExceptionHandler.notifyException(e, false, false);
                }
            }
            fi = createFileInformation(file, status, repositoryStatus);
            if (equivalent(fi, current)) {
                refreshDone = true;
            }
            // do not include uptodate files into cache, missing directories must be included
            if (!refreshDone && current == null && !fi.isDirectory() && fi.getStatus() == FileInformation.STATUS_VERSIONED_UPTODATE) {
                refreshDone = true;
            }

            if(!refreshDone) {               
                if (fi.getStatus() == FileInformation.STATUS_UNKNOWN && 
                      current != null && current.isDirectory() && ( current.getStatus() == FileInformation.STATUS_VERSIONED_DELETEDLOCALLY || 
                                                                    current.getStatus() == FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY )) 
                {
                    // - if the file was deleted then all it's children have to be refreshed.
                    // - we have to list the children before the turbo.writeEntry() call 
                    //   as that unfortunatelly tends to purge them from the cache 
                    content = listFiles(new File[] {file}, ~0);
                } 

                file = FileUtil.normalizeFile(file);
                dir = FileUtil.normalizeFile(dir);
                Map<File, FileInformation> newFiles = new HashMap<File, FileInformation>(files);
                if (fi.getStatus() == FileInformation.STATUS_UNKNOWN) {
                    newFiles.remove(file);
                    turbo.writeEntry(file, FILE_STATUS_MAP, null);  // remove mapping in case of directories
                }
                else if (fi.getStatus() == FileInformation.STATUS_VERSIONED_UPTODATE && file.isFile()) {
                    newFiles.remove(file);
                } else {
                    newFiles.put(file, fi);
                }
                assert newFiles.containsKey(dir) == false;
                turbo.writeEntry(dir, FILE_STATUS_MAP, newFiles.size() == 0 ? null : newFiles);
            }
        }

        if(!refreshDone) {
            if(content == null && file.isDirectory() && needRecursiveRefresh(fi, current)) {
                content = listFiles(file);
            }

            if ( content != null ) {
                for (int i = 0; i < content.length; i++) {
                    refresh(content[i], REPOSITORY_STATUS_UNKNOWN);
                }
            }
            fireFileStatusChanged(file, current, fi);    
        } else if(forceChangeEvent) {
            fireFileStatusChanged(file, current, fi);    
        }                       
        return fi;
    }    

    public void patchRevision(File[] fileArray, Number revision) {        
        for (File file : fileArray) {            
            synchronized(this) {        
                FileInformation status = getCachedStatus(file);
                ISVNStatus entry = status != null ? status.getEntry(file) : null;
                if(entry != null) {
                    Number rev = entry.getRevision();
                    if(rev == null) continue;
                    if(rev.getNumber() != revision.getNumber()) {
                        FileInformation info = createFileInformation(file, new FakeRevisionStatus(entry, revision), REPOSITORY_STATUS_UNKNOWN);
                        File dir = file.getParentFile();
                        Map<File, FileInformation> files = getScannedFiles(dir);
                        Map<File, FileInformation> newFiles = new HashMap<File, FileInformation>(files);
                        newFiles.put(file, info);
                        turbo.writeEntry(dir, FILE_STATUS_MAP, newFiles.size() == 0 ? null : newFiles);
                    }
                }
            }
        }
    }
    
    /**
     * Two FileInformation objects are equivalent if their status contants are equal AND they both reperesent a file (or
     * both represent a directory) AND Entries they cache, if they can be compared, are equal. 
     *  
     * @param other object to compare to
     * @return true if status constants of both object are equal, false otherwise
     */ 
    private static boolean equivalent(FileInformation main, FileInformation other) {
        if (other == null || main.getStatus() != other.getStatus() || main.isDirectory() != other.isDirectory()) return false;
        
        ISVNStatus e1 = main.getEntry(null);
        ISVNStatus e2 = other.getEntry(null);
        return e1 == e2 || e1 == null || e2 == null || equal(e1, e2);
    }

    /**
     * Replacement for missing Entry.equals(). It is implemented as a separate method to maintain compatibility.
     * 
     * @param e1 first entry to compare
     * @param e2 second Entry to compare
     * @return true if supplied entries contain equivalent information
     */ 
    private static boolean equal(ISVNStatus e1, ISVNStatus e2) {
        long r1 = -1;
        if (e1 != null) {
            SVNRevision r = e1.getRevision();
            r1 = r != null ? e1.getRevision().getNumber() : r1;
        }

        long r2 = -2;
        if (e2 != null) {
            SVNRevision r = e2.getRevision();
            r2 = r != null ? e2.getRevision().getNumber() : r2;
        }
        
        if ( r1 != r2 ) {
            return false;
        }
        return e1.getUrl() == e2.getUrl() || 
                e1.getUrl() != null && e1.getUrl().equals(e2.getUrl());
    }
    
    private boolean needRecursiveRefresh(FileInformation fi, FileInformation current) {
        //     looks like the same thing is done at diferent places in a different way but the same result.
        if (fi.getStatus() == FileInformation.STATUS_NOTVERSIONED_EXCLUDED || 
                current != null && current.getStatus() == FileInformation.STATUS_NOTVERSIONED_EXCLUDED) return true;
        if (fi.getStatus() == FileInformation.STATUS_NOTVERSIONED_NOTMANAGED ||
                current != null && current.getStatus() == FileInformation.STATUS_NOTVERSIONED_NOTMANAGED) return true;
        if (fi.getStatus() == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY ||
                current != null && current.getStatus() == FileInformation.STATUS_VERSIONED_ADDEDLOCALLY) return true;        
        return false;
    }
    
    // --- Package private contract ------------------------------------------
    
    boolean ready() {
        return ready;
    }

    /**
     * compute the cache index
     */
    void computeIndex() {
        try {
            cacheProvider.computeIndex();
            Subversion.getInstance().refreshAllAnnotations();
        } finally {
            ready = true;
    }
    }

    /**
     * Cleans up the cache by removing or correcting entries that are no longer valid or correct.
     * WARNING: index has to be computed first
     */
    void cleanUp() {
        File[] modifiedFiles = cacheProvider.getAllIndexValues();
        for (File file : modifiedFiles) {
            FileInformation info = getCachedStatus(file);
            if (info != null && (info.getStatus() & FileInformation.STATUS_LOCAL_CHANGE) != 0) {
                refresh(file, REPOSITORY_STATUS_UNKNOWN);
            } else if (info == null ||info.getStatus() == FileInformation.STATUS_NOTVERSIONED_EXCLUDED) {
                // remove entries that were excluded but no longer exist
                // cannot simply call refresh on excluded files because of 'excluded on server' status
                if (!exists(file)) {
                    refresh(file, REPOSITORY_STATUS_UNKNOWN);
                }
            }
        }
    }

    /**
     * Refreshes given directory and all subdirectories.
     *
     * @param dir directory to refresh
     */
    void directoryContentChanged(File dir) {
        Map originalFiles = (Map) turbo.readEntry(dir, FILE_STATUS_MAP);
        if (originalFiles != null) {
            for (Iterator i = originalFiles.keySet().iterator(); i.hasNext();) {
                File file = (File) i.next();
                refresh(file, REPOSITORY_STATUS_UNKNOWN);
            }
        }
    }
    
    // --- Private methods ---------------------------------------------------
    private final Set<File> plannedForRecursiveScan = new WeakSet(50);
    private Map<File, FileInformation> getScannedFiles(File dir) {
        Map<File, FileInformation> files;

        // there are 2nd level nested admin dirs (.svn/tmp, .svn/prop-base, ...)

        if (SvnUtils.isAdministrative(dir)) {
            return NOT_MANAGED_MAP;
        }
        File parent = dir.getParentFile();
        if (parent != null && SvnUtils.isAdministrative(parent)) {
            return NOT_MANAGED_MAP;
        }

        files = (Map<File, FileInformation>) turbo.readEntry(dir, FILE_STATUS_MAP);
        if (files != null) return files;
        if (isNotManagedByDefault(dir)) {
            return NOT_MANAGED_MAP; 
        }

        // scan and populate cache with results

        dir = FileUtil.normalizeFile(dir);
        // recursive scan for unexplored folders
        boolean recursiveScanEnabled = !"false".equals(System.getProperty("org.netbeans.modules.subversion.FileStatusCache.recursiveScan", "true")); //NOI18N - leave option to disable the recursive scan
        if (recursiveScanEnabled) {
            // do not scan dir's children twice. It can happen that the same folder gets to this point more than once due to getScannedFile recursive nature
            // 1. if dir is unknown
            // 2. scanFolder(dir) > createMissingEntry(dir's child) > getStatus(dir) > getScannedFiles(dir) again and before turno.writeEntry(dir) is called
            File f = new File(dir.getAbsolutePath()); // do not add directly dir so the set doesn't get too big (references to dir are kept outside of this method - e.g. in turbo)
            synchronized (plannedForRecursiveScan) {
                recursiveScanEnabled = !plannedForRecursiveScan.contains(f) && plannedForRecursiveScan.add(f); // this will return false if dir is already there
            }
        }
        files = scanFolder(dir);    // must not execute while holding the lock, it may take long to execute
        assert files.containsKey(dir) == false;
        turbo.writeEntry(dir, FILE_STATUS_MAP, files);
        for (Iterator i = files.keySet().iterator(); i.hasNext();) {
            File file = (File) i.next();
            FileInformation info = files.get(file);
            // recursive scan for unexplored folders: run refresh on children if necessary
            if (recursiveScanEnabled                                        // scan is allowed and dir is not yet planned
                    && (info.getStatus() & (FileInformation.STATUS_NOTVERSIONED_NOTMANAGED | FileInformation.STATUS_NOTVERSIONED_EXCLUDED)) == 0 // do not scan notmanaged or ignored files
                    && file.isDirectory()                                   // scan only folders
                    && turbo.readEntry(file, FILE_STATUS_MAP) == null) {    // scan only those which have not yet been scanned, no information is available for them
                refreshAsync(file.listFiles());
            }
            if ((info.getStatus() & (FileInformation.STATUS_LOCAL_CHANGE | FileInformation.STATUS_NOTVERSIONED_EXCLUDED)) != 0) {
                fireFileStatusChanged(file, null, info);
            }
        }
        return files;
    }

    private boolean isNotManagedByDefault(File dir) {
        return !dir.exists();
    }

    /**
     * Scans all files in the given folder, computes and stores their CVS status. 
     * 
     * @param dir directory to scan
     * @return Map map to be included in the status cache (File => FileInformation)
     */ 
    private Map<File, FileInformation> scanFolder(File dir) {
        File [] files = dir.listFiles();
        if (files == null) files = new File[0];
        Map<File, FileInformation> folderFiles = new HashMap<File, FileInformation>(files.length);

        ISVNStatus [] entries = null;
        try {
            if (SvnUtils.isManaged(dir)) {                
                SvnClient client = Subversion.getInstance().getClient(true);
                entries = client.getStatus(dir, false, true); 
            }
        } catch (SVNClientException e) {
            // no or damaged entries
            //LOG.getDefault().annotate(e, "Can not status " + dir.getAbsolutePath() + ", guessing it...");  // NOI18N
            SvnClientExceptionHandler.notifyException(e, false, false);
        }

        if (entries == null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (SvnUtils.isAdministrative(file)) continue;
                FileInformation fi = createFileInformation(file, null, REPOSITORY_STATUS_UNKNOWN);
                if (fi.isDirectory() || fi.getStatus() != FileInformation.STATUS_VERSIONED_UPTODATE) {
                    folderFiles.put(file, fi);
                }
            }
        } else {
            Set<File> localFiles = new HashSet<File>(Arrays.asList(files));
            for (int i = 0; i < entries.length; i++) {
                ISVNStatus entry = entries[i];
                File file = new File(entry.getPath());
                if (file.equals(dir)) {
                    continue;
                }
                localFiles.remove(file);
                if (SvnUtils.isAdministrative(file)) {
                    continue;
                }
                FileInformation fi = createFileInformation(file, entry, REPOSITORY_STATUS_UNKNOWN);
                if (fi.isDirectory() || fi.getStatus() != FileInformation.STATUS_VERSIONED_UPTODATE) {
                    folderFiles.put(file, fi);
                }
            }

            Iterator it = localFiles.iterator();
            while (it.hasNext()) {
                File localFile = (File) it.next();
                FileInformation fi = createFileInformation(localFile, null, REPOSITORY_STATUS_UNKNOWN);
                if (fi.isDirectory() || fi.getStatus() != FileInformation.STATUS_VERSIONED_UPTODATE) {
                    folderFiles.put(localFile, fi);
                }
            }
        }

        return folderFiles;
    }

    /**
     * Examines a file or folder and computes its status. 
     * 
     * @param status entry for this file or null if the file is unknown to subversion
     * @return FileInformation file/folder status bean
     */ 
    private FileInformation createFileInformation(File file, ISVNStatus status, ISVNStatus repositoryStatus) {
        if (status == null || status.getTextStatus().equals(SVNStatusKind.UNVERSIONED)) {
            if (!SvnUtils.isManaged(file)) {
                return file.isDirectory() ? FILE_INFORMATION_NOTMANAGED_DIRECTORY : FILE_INFORMATION_NOTMANAGED;
            }
            return createMissingEntryFileInformation(file, repositoryStatus);
        } else {
            return createVersionedFileInformation(file, status, repositoryStatus);
        }
    }

    /**
     * Examines a file or folder that has an associated CVS entry. 
     * 
     * @param file file/folder to examine
     * @param status status of the file/folder as reported by the CVS server 
     * @return FileInformation file/folder status bean
     */ 
    private FileInformation createVersionedFileInformation(File file, ISVNStatus status, ISVNStatus repositoryStatus) {

        SVNStatusKind kind = status.getTextStatus();
        SVNStatusKind pkind = status.getPropStatus();

        int remoteStatus = 0;
        if (repositoryStatus != REPOSITORY_STATUS_UNKNOWN) {
            if (repositoryStatus.getRepositoryTextStatus() == SVNStatusKind.MODIFIED
            || repositoryStatus.getRepositoryPropStatus() == SVNStatusKind.MODIFIED) {
                remoteStatus = FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY;
            } else if (repositoryStatus.getRepositoryTextStatus() == SVNStatusKind.DELETED
            /*|| repositoryStatus.getRepositoryPropStatus() == SVNStatusKind.DELETED*/) {
                remoteStatus = FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY;
            } else if (repositoryStatus.getRepositoryTextStatus() == SVNStatusKind.ADDED
            /*|| repositoryStatus.getRepositoryPropStatus() == SVNStatusKind.ADDED*/) {
                // solved in createMissingfileInformation
            } else if ( (repositoryStatus.getRepositoryTextStatus() == null &&
                         repositoryStatus.getRepositoryPropStatus() == null)
                        ||
                        (repositoryStatus.getRepositoryTextStatus() == SVNStatusKind.NONE &&
                         repositoryStatus.getRepositoryPropStatus() == SVNStatusKind.NONE))
            {
                // no remote change at all
            } else {
                // so far above were observed....
                Subversion.LOG.warning("SVN.FSC: unhandled repository status: " + file.getAbsolutePath() + "\n" +   // NOI18N
                                       "\ttext: " + repositoryStatus.getRepositoryTextStatus() + "\n" +             // NOI18N
                                       "\tprop: " + repositoryStatus.getRepositoryPropStatus());                    // NOI18N
            }
        }
        
        if (status. getLockOwner() != null) {
            remoteStatus = FileInformation.STATUS_LOCKED | remoteStatus;
        }
        
        if (SVNStatusKind.NONE.equals(pkind)) {
            // no influence
        } else if (SVNStatusKind.NORMAL.equals(pkind)) {
            // no influence
        } else if (SVNStatusKind.MODIFIED.equals(pkind)) {
            if (SVNStatusKind.NORMAL.equals(kind)) {
                return new FileInformation(FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY | remoteStatus, status);
            }
        } else if (SVNStatusKind.CONFLICTED.equals(pkind)) {
            return new FileInformation(FileInformation.STATUS_VERSIONED_CONFLICT | remoteStatus, status);
        } else {
            throw new IllegalArgumentException("Unknown prop status: " + status.getPropStatus()); // NOI18N
        }


        if (SVNStatusKind.NONE.equals(kind)) {
            return FILE_INFORMATION_UNKNOWN;
        } else if (SVNStatusKind.NORMAL.equals(kind)) {
            return new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE | remoteStatus, status);
        } else if (SVNStatusKind.MODIFIED.equals(kind)) {
            return new FileInformation(FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY | remoteStatus, status);
        } else if (SVNStatusKind.ADDED.equals(kind)) {
            return new FileInformation(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY | remoteStatus, status);
        } else if (SVNStatusKind.DELETED.equals(kind)) {                    
            return new FileInformation(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY | remoteStatus, status);
        } else if (SVNStatusKind.UNVERSIONED.equals(kind)) {            
            return new FileInformation(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | remoteStatus, status);
        } else if (SVNStatusKind.MISSING.equals(kind)) {            
            return new FileInformation(FileInformation.STATUS_VERSIONED_DELETEDLOCALLY | remoteStatus, status);
        } else if (SVNStatusKind.REPLACED.equals(kind)) {                      
            // this status or better to use this simplyfication?
            return new FileInformation(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | remoteStatus, status);
        } else if (SVNStatusKind.MERGED.equals(kind)) {            
            return new FileInformation(FileInformation.STATUS_VERSIONED_MERGE | remoteStatus, status);
        } else if (SVNStatusKind.CONFLICTED.equals(kind)) {            
            return new FileInformation(FileInformation.STATUS_VERSIONED_CONFLICT | remoteStatus, status);
        } else if (SVNStatusKind.OBSTRUCTED.equals(kind)) {            
            return new FileInformation(FileInformation.STATUS_VERSIONED_CONFLICT | remoteStatus, status);
        } else if (SVNStatusKind.IGNORED.equals(kind)) {            
            return new FileInformation(FileInformation.STATUS_NOTVERSIONED_EXCLUDED | remoteStatus, status);
        } else if (SVNStatusKind.INCOMPLETE.equals(kind)) {            
            return new FileInformation(FileInformation.STATUS_VERSIONED_CONFLICT | remoteStatus, status);
        } else if (SVNStatusKind.EXTERNAL.equals(kind)) {            
            return new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE | remoteStatus, status);
        } else {        
            throw new IllegalArgumentException("Unknown text status: " + status.getTextStatus()); // NOI18N
        }
    }

    static String statusText(ISVNStatus status) {
        return "file: " + status.getTextStatus().toString() + " copied: " + status.isCopied() + " prop: " + status.getPropStatus().toString(); // NOI18N
    }

    /**
     * Examines a file or folder that does NOT have an associated Subversion status. 
     * 
     * @param file file/folder to examine
     * @return FileInformation file/folder status bean
     */ 
    private FileInformation createMissingEntryFileInformation(File file, ISVNStatus repositoryStatus) {
        
        // ignored status applies to whole subtrees
        boolean exists = file.exists();
        boolean isDirectory = exists && file.isDirectory();
        int parentStatus = getStatus(file.getParentFile()).getStatus();
        if (parentStatus == FileInformation.STATUS_NOTVERSIONED_EXCLUDED) {
            return isDirectory ? 
                FILE_INFORMATION_EXCLUDED_DIRECTORY : FILE_INFORMATION_EXCLUDED;
        }
        /**FILE_INFORMATION_NOTMANAGED should be set only for existing files
         * Deleted files, which were originally ignored (i.e. build/classes), 
         * used to acquire status F_I_NOTMANAGED, which was stuck to them indefinitely
         */
        if (exists && parentStatus == FileInformation.STATUS_NOTVERSIONED_NOTMANAGED) {
            if (isDirectory) {
                // Working directory roots (aka managed roots). We already know that isManaged(file) is true
                return SvnUtils.isPartOfSubversionMetadata(file) ? 
                    FILE_INFORMATION_NOTMANAGED_DIRECTORY : FILE_INFORMATION_UPTODATE_DIRECTORY;
            } else {
                return FILE_INFORMATION_NOTMANAGED;
            }
        }

        // mark auxiliary after-update conflict files as ignored
        // C source.java
        // I source.java.mine
        // I source.java.r45
        // I source.java.r57
        //
        // after-merge conflicts (even svn st does not recognize as ignored)
        // C source.java
        // ? source.java.working
        // ? source.jave.merge-right.r20
        // ? source.java.merge-left.r0
        //
        String name = file.getName();
        Matcher m = auxConflictPattern.matcher(name);
        if (m.matches()) {
            File dir = file.getParentFile();
            if (dir != null) {
                String masterName = m.group(1);
                File master = new File(dir, masterName);
                if (master.isFile()) {
                    return FILE_INFORMATION_EXCLUDED;
                }
            }
        }
        
        if (exists) {
            if (Subversion.getInstance().isIgnored(file)) {
                return new FileInformation(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, file.isDirectory());
            } else {
                return new FileInformation(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, file.isDirectory());
            }
        } else {
            if (repositoryStatus != REPOSITORY_STATUS_UNKNOWN) {
                if (repositoryStatus.getRepositoryTextStatus() == SVNStatusKind.ADDED) {
                    boolean folder = repositoryStatus.getNodeKind() == SVNNodeKind.DIR;
                    return new FileInformation(FileInformation.STATUS_VERSIONED_NEWINREPOSITORY, folder);
                }
            }
            return FILE_INFORMATION_UNKNOWN;
        }
    }

    
    private boolean exists(File file) {
        if (!file.exists()) return false;
        return file.getAbsolutePath().equals(FileUtil.normalizeFile(file).getAbsolutePath());
    }

    ListenersSupport listenerSupport = new ListenersSupport(this);
    public void addVersioningListener(VersioningListener listener) {
        listenerSupport.addListener(listener);
    }

    public void removeVersioningListener(VersioningListener listener) {
        listenerSupport.removeListener(listener);
    }
    
    private void fireFileStatusChanged(File file, FileInformation oldInfo, FileInformation newInfo) {
        getLabelsCache().remove(file); // remove info from label cache, it could change
        listenerSupport.fireVersioningEvent(EVENT_FILE_STATUS_CHANGED, new Object [] { file, oldInfo, newInfo });
    }

    public void setCommand(int command) {
        // boring ISVNNotifyListener event
    }

    public void logCommandLine(String commandLine) {
        // boring ISVNNotifyListener event
    }

    public void logMessage(String message) {
        // boring ISVNNotifyListener event
    }

    public void logError(String message) {
        // boring ISVNNotifyListener event
    }

    public void logRevision(long revision, String path) {
        // boring ISVNNotifyListener event
    }

    public void logCompleted(String message) {
        // boring ISVNNotifyListener event
    }

    private static final class NotManagedMap extends AbstractMap<File, FileInformation> {
        public Set<Entry<File, FileInformation>> entrySet() {
            return Collections.emptySet();
        }
    }

    private class FakeRevisionStatus implements ISVNStatus {
        private ISVNStatus value;
        private Number revision;
        public FakeRevisionStatus(ISVNStatus value, Number revision) {
            this.value = value;
            this.revision = revision;           
        }
        public boolean isWcLocked() {
            return value.isWcLocked();
        }
        public boolean isSwitched() {
            return value.isSwitched();
        }
        public boolean isCopied() {
            return value.isCopied();
        }
        public String getUrlString() {
            return value.getUrlString();
        }
        public SVNUrl getUrlCopiedFrom() {
            return value.getUrlCopiedFrom();
        }
        public SVNUrl getUrl() {
            return value.getUrl();
        }
        public SVNStatusKind getTextStatus() {
            return value.getTextStatus();
        }
        public Number getRevision() {                        
            return revision;
        }
        public SVNStatusKind getRepositoryTextStatus() {
            return value.getRepositoryTextStatus();
        }
        public SVNStatusKind getRepositoryPropStatus() {
            return value.getRepositoryPropStatus();
        }
        public SVNStatusKind getPropStatus() {
            return value.getPropStatus();
        }
        public String getPath() {
            return value.getPath();
        }
        public SVNNodeKind getNodeKind() {
            return value.getNodeKind();
        }
        public String getLockOwner() {
            return value.getLockOwner();
        }
        public Date getLockCreationDate() {
            return value.getLockCreationDate();
        }
        public String getLockComment() {
            return value.getLockComment();
        }
        public String getLastCommitAuthor() {
            return value.getLastCommitAuthor();
        }
        public Number getLastChangedRevision() {
            return value.getLastChangedRevision();
        }
        public Date getLastChangedDate() {
            return value.getLastChangedDate();
        }
        public File getFile() {
            return value.getFile();
        }
        public File getConflictWorking() {
            return value.getConflictWorking();
        }
        public File getConflictOld() {
            return value.getConflictOld();
        }
        public File getConflictNew() {
            return value.getConflictNew();
        }

        public boolean hasTreeConflict() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public SVNConflictDescriptor getConflictDescriptor() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isFileExternal() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public FileLabelCache getLabelsCache () {
        return labelsCache;
    }

    /**
     * Cache of information needed for name annotations, caching such information prevents from running status commands in AWT
     */
    public static class FileLabelCache {
        private static final Logger LABELS_CACHE_LOG = Logger.getLogger("org.netbeans.modules.subversion.FileLabelsCache"); //NOI18N
        private final LinkedHashMap<File, FileLabelInfo> fileLabels;
        private static final long VALID_LABEL_PERIOD = 20000; // 20 seconds
        private static final FileLabelInfo FAKE_LABEL_INFO = new FileLabelInfo("", "", ""); //NOI18N
        private final Set<File> filesForLabelRefresh = new HashSet<File>();
        private final RequestProcessor.Task labelInfoRefreshTask;
        private boolean mimeTypeFlag;
        private final FileStatusCache master;

        private FileLabelCache(FileStatusCache master) {
            this.master = master;
            labelInfoRefreshTask = master.rp.create(new LabelInfoRefreshTask());
            fileLabels = new LinkedHashMap<File, FileLabelInfo>(100);
        }

        public void flushFileLabels(File... files) {
            synchronized (fileLabels) {
                for (File f : files) {
                    if (LABELS_CACHE_LOG.isLoggable(Level.FINE)) {
                        LABELS_CACHE_LOG.fine("Removing from cache: " + f.getAbsolutePath()); //NOI18N
                    }
                    fileLabels.remove(f);
                }
            }
        }

        void setMimeTypeFlag(boolean flag) {
            this.mimeTypeFlag = flag;
        }

        /**
         * Returns a not null cache item.
         * @param file
         * @param mimeTypeFlag mime label is needed?
         * @return a cache item or a fake one if the original is null or invalid
         */
        FileLabelInfo getLabelInfo(File file, boolean mimeTypeFlag) {
            FileLabelInfo labelInfo;
            boolean refreshInfo = false;
            synchronized (fileLabels) {
                labelInfo = fileLabels.get(file);
                if (labelInfo == null || !labelInfo.isValid(mimeTypeFlag, true)) {
                    if (LABELS_CACHE_LOG.isLoggable(Level.FINE)) {
                        if (labelInfo == null && LABELS_CACHE_LOG.isLoggable(Level.FINER)) {
                            LABELS_CACHE_LOG.finer("No item in cache for : " + file.getAbsolutePath()); //NOI18N
                        } else if (labelInfo != null) {
                            LABELS_CACHE_LOG.fine("Too old item in cache for : " + file.getAbsolutePath()); //NOI18N
                        }
                    }
                    fileLabels.remove(file);
                    labelInfo = FAKE_LABEL_INFO;
                    refreshInfo = true;
                }
            }
            if (refreshInfo) {
                scheduleLabelRefresh(file);
            }
            return labelInfo;
        }

        /**
         * schedules file's label info refresh
         * @param file
         */
        private void scheduleLabelRefresh(File file) {
            synchronized (filesForLabelRefresh) {
                filesForLabelRefresh.add(file);
            }
            labelInfoRefreshTask.schedule(200);
        }

        private void remove(File file) {
            synchronized (fileLabels) {
                fileLabels.remove(file);
            }
        }

        private class LabelInfoRefreshTask extends Task {

            @Override
            public void run() {
                Set<File> filesToRefresh;
                synchronized (filesForLabelRefresh) {
                    // pick up files for refresh
                    filesToRefresh = new HashSet<File>(filesForLabelRefresh);
                    filesForLabelRefresh.clear();
                }
                if (!filesToRefresh.isEmpty()) {
                    File[] files = filesToRefresh.toArray(new File[filesToRefresh.size()]);
                    try {
                        SvnClient client = Subversion.getInstance().getClient(false);
                        // get status for all files
                        ISVNStatus[] statuses = client.getStatus(files);
                        // labels are accummulated in a temporary map so their timestamp can be later set to a more accurate value
                        // initialization for many files can be time-consuming and labels initialized in first cycles can grow old even before
                        // their annotations are refreshed through refreshAnnotations()
                        HashMap<File, FileLabelInfo> labels = new HashMap<File, FileLabelInfo>(filesToRefresh.size());
                        for (ISVNStatus status : statuses) {
                            File file = status.getFile();
                            SVNRevision rev = status.getRevision();
                            String revisionString, stickyString, binaryString = null;
                            revisionString = rev != null && !"-1".equals(rev.toString()) ? rev.toString() : ""; //NOI18N
                            if (mimeTypeFlag) {
                                // call svn prop command only when really needed
                                FileInformation fi = master.getCachedStatus(file);
                                if (fi == null || (fi.getStatus() & (FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_NOTVERSIONED_EXCLUDED)) == 0) {
                                    binaryString = getMimeType(client, file);
                                } else {
                                    binaryString = "";                  //NOI18N
                                }
                            }
                            // copy name
                            if (status != null && status.getUrl() != null) {
                                stickyString = SvnUtils.getCopy(status.getUrl());
                            } else {
                                // slower
                                stickyString = SvnUtils.getCopy(file);
                            }
                            labels.put(file, new FileLabelInfo(revisionString, binaryString, stickyString));
                        }
                        synchronized (fileLabels) {
                            for (Map.Entry<File, FileLabelInfo> e : labels.entrySet()) {
                                e.getValue().updateTimestamp(); // after a possible slow initialization for many files update all timestamps, so they remain in cache longer
                                fileLabels.remove(e.getKey()); // fileLabels is a LinkedHashSet, so in order to move the item to the back in the chain, it must be removed before inserting
                                fileLabels.put(e.getKey(), e.getValue());
                            }
                        }
                    } catch (SVNClientException ex) {
                        LABELS_CACHE_LOG.log(Level.WARNING, "LabelInfoRefreshTask: failed getting status and info for " + filesToRefresh.toString());
                        LABELS_CACHE_LOG.log(Level.INFO, null, ex);
                    }
                    Subversion.getInstance().refreshAnnotations(files);
                    synchronized (fileLabels) {
                        if (fileLabels.size() > 50) {
                            if (LABELS_CACHE_LOG.isLoggable(Level.FINE)) {
                                LABELS_CACHE_LOG.fine("Cache contains : " + fileLabels.size() + " entries before a cleanup"); //NOI18N
                            }
                            for (Iterator<File> it = fileLabels.keySet().iterator(); it.hasNext();) {
                                File f = it.next();
                                if (!fileLabels.get(f).isValid(mimeTypeFlag, false)) {
                                    it.remove();
                                } else {
                                    break;
                                }
                            }
                            if (LABELS_CACHE_LOG.isLoggable(Level.FINE)) {
                                LABELS_CACHE_LOG.fine("Cache contains : " + fileLabels.size() + " entries after a cleanup"); //NOI18N
                            }
                        }
                    }
                }
            }

            private String getMimeType(SvnClient client, File file) {
                try {
                    ISVNProperty prop = client.propertyGet(file, ISVNProperty.MIME_TYPE);
                    if (prop != null) {
                        String mime = prop.getValue();
                        return mime != null ? mime : "";                //NOI18N
                    }
                } catch (SVNClientException ex) {
                    if (LABELS_CACHE_LOG.isLoggable(Level.FINE)) {
                        LABELS_CACHE_LOG.log(Level.FINE, null, ex);
                    }
                    return "";                                          //NOI18N
                }
                return "";                                              //NOI18N
            }

        }

        /**
         * File label cache item
         */
        static class FileLabelInfo {

            private final String revisionString;
            private final String binaryString;
            private final String stickyString;
            private boolean pickedUp;
            private long timestamp;

            private FileLabelInfo(String revisionString, String binaryString, String stickyString) {
                this.revisionString = revisionString;
                this.binaryString = binaryString;
                this.stickyString = stickyString;
                updateTimestamp();
            }

            private void updateTimestamp() {
                this.timestamp = System.currentTimeMillis();
            }

            /**
             *
             * @param mimeFlag if set to true, binaryString will be checked for being set
             * @param checkFirstAccess first access to this info is always valid,
             * so the oldness will be checked only when this is false or the info has already been accessed for the first time
             * @return
             */
            private boolean isValid(boolean mimeFlag, boolean checkFirstAccess) {
                long diff = System.currentTimeMillis() - timestamp;
                boolean valid = (checkFirstAccess && !pickedUp && (pickedUp = true)) || (diff <= VALID_LABEL_PERIOD);
                return valid && (!mimeFlag || binaryString != null);

            }

            /**
             * Returns a not null String with revision number, empty for unknown revisions
             * @return
             */
            String getRevisionString() {
                return revisionString != null ? revisionString : "";        //NOI18N
            }

            /*
             * Returns a not null String, empty for not binary files
             */
            String getBinaryString() {
                return binaryString != null ? binaryString : "";            //NOI18N
            }

            /**
             * returns a not null String denoting a copy name
             * @return
             */
            String getStickyString() {
                return stickyString != null ? stickyString : "";            //NOI18N
            }
        }
    }
}
