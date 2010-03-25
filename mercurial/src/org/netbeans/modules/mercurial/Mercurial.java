/*+
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.mercurial;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.openide.util.RequestProcessor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.mercurial.ui.diff.Setup;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.util.NbBundle;
import org.netbeans.modules.versioning.hooks.HgHook;
import org.netbeans.modules.mercurial.kenai.HgKenaiAccessor;
import org.netbeans.modules.mercurial.ui.repository.HgURL;
import org.netbeans.modules.versioning.util.VCSHyperlinkProvider;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.Utilities;

/**
 * Main entry point for Mercurial functionality, use getInstance() to get the Mercurial object.
 *
 * @author Maros Sandor
 */
public class Mercurial {
    public static final int HG_FETCH_20_REVISIONS = 20;
    public static final int HG_FETCH_50_REVISIONS = 50;
    public static final int HG_FETCH_ALL_REVISIONS = -1;
    public static final int HG_NUMBER_FETCH_OPTIONS = 3;
    public static final int HG_NUMBER_TO_FETCH_DEFAULT = 7;
    public static final int HG_MAX_REVISION_COMBO_SIZE = HG_NUMBER_TO_FETCH_DEFAULT + HG_NUMBER_FETCH_OPTIONS;

    public static final String MERCURIAL_OUTPUT_TAB_TITLE = org.openide.util.NbBundle.getMessage(Mercurial.class, "CTL_Mercurial_DisplayName"); // NOI18N
    public static final String CHANGESET_STR = "changeset:"; // NOI18N

    public static final String PROP_ANNOTATIONS_CHANGED = "annotationsChanged"; // NOI18N
    static final String PROP_VERSIONED_FILES_CHANGED = "versionedFilesChanged"; // NOI18N
    public static final String PROP_CHANGESET_CHANGED = "changesetChanged"; // NOI18N

    public static final Logger LOG = Logger.getLogger("org.netbeans.modules.mercurial"); // NOI18N
    public static final Logger STATUS_LOG = Logger.getLogger("org.netbeans.modules.mercurial.status"); //NOI18N
    
    private static final int STATUS_DIFFABLE =
            FileInformation.STATUS_VERSIONED_UPTODATE |
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY |
            FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY |
            FileInformation.STATUS_VERSIONED_CONFLICT |
            FileInformation.STATUS_VERSIONED_MERGE |
            FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY |
            FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY |
            FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY;

    private static final String MERCURIAL_SUPPORTED_VERSION_093 = "0.9.3"; // NOI18N
    private static final String MERCURIAL_SUPPORTED_VERSION_094 = "0.9.4"; // NOI18N
    private static final String MERCURIAL_SUPPORTED_VERSION_095 = "0.9.5"; // NOI18N
    private static final String MERCURIAL_SUPPORTED_VERSION_100 = "1.0"; // NOI18N
    private static Mercurial instance;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final MercurialVCS mvcs;

    public static synchronized Mercurial getInstance() {
        if (instance == null) {
            instance = new Mercurial();
            instance.init();
        }
        return instance;
    }

    private MercurialAnnotator   mercurialAnnotator;
    private MercurialInterceptor mercurialInterceptor;
    private FileStatusCache     fileStatusCache;
    private HashMap<HgURL, RequestProcessor>   processorsToUrl;
    /**
     * true if hg is present and it's version is supported
     */
    private boolean goodVersion;
    private String version;
    /**
     * true if hg version command has been invoked
     */
    private boolean gotVersion;

    private Result<? extends VCSHyperlinkProvider> hpResult;
    private RequestProcessor parallelRP;

    private Mercurial() {
        mvcs = org.openide.util.Lookup.getDefault().lookup(MercurialVCS.class);
    }


    private void init() {
        setDefaultPath();
        fileStatusCache = new FileStatusCache();
        mercurialAnnotator = new MercurialAnnotator();
        mercurialInterceptor = new MercurialInterceptor();
        fileStatusCache.addPropertyChangeListener(mvcs);
        mercurialAnnotator.addPropertyChangeListener(mvcs);
        addPropertyChangeListener(mvcs);
        asyncInit(); // Does the Hg check but postpones querying user until menu is activated
    }

    private void setDefaultPath() {
        // Set default executable location for mercurial on mac
        if (System.getProperty("os.name").equals("Mac OS X")) { // NOI18N
            String defaultPath = HgModuleConfig.getDefault().getExecutableBinaryPath ();
            if (defaultPath == null || defaultPath.length() == 0) {
                String[] pathNames  = {"/Library/Frameworks/Python.framework/Versions/Current/bin", // NOI18N
                                        "/usr/bin", "/usr/local/bin","/opt/local/bin/", "/sw/bin"}; // NOI18N
                for (int i = 0; i < pathNames.length; i++) {
                    if (HgModuleConfig.getDefault().isExecPathValid(pathNames[i])) {
                        HgModuleConfig.getDefault().setExecutableBinaryPath (pathNames[i]); // NOI18N
                        break;
                     }
                 }
            }
        }else if (Utilities.isWindows()) { // NOI18N
            String defaultPath = HgModuleConfig.getDefault().getExecutableBinaryPath ();
            if (defaultPath == null || defaultPath.length() == 0) {
                String path = HgUtils.findInUserPath(HgCommand.HG_WINDOWS_EXECUTABLES);
                if (path != null && !path.equals("")) { // NOI18N
                    HgModuleConfig.getDefault().setExecutableBinaryPath (path); // NOI18N
                }
            }
        }
    }

    public void asyncInit() {
        gotVersion = false;
        RequestProcessor rp = getRequestProcessor();
        Runnable init = new Runnable() {
            public void run() {
                HgKenaiAccessor.getInstance().registerVCSNoficationListener();
                synchronized(this) {
                    checkVersionIntern();
                }
            }

        };
        rp.post(init);
    }

    private void checkVersionIntern() {
        version = HgCommand.getHgVersion();
        LOG.log(Level.FINE, "version: {0}", version); // NOI18N
        if (version != null) {
            goodVersion = isSupportedVersion(version);
        } else {
            goodVersion = false;
        }
        gotVersion = true;
    }

    private boolean isSupportedVersion(String version) {
        if(version.startsWith(MERCURIAL_SUPPORTED_VERSION_093) ||
           version.startsWith(MERCURIAL_SUPPORTED_VERSION_094) ||
           version.startsWith(MERCURIAL_SUPPORTED_VERSION_095) ||
           version.startsWith(MERCURIAL_SUPPORTED_VERSION_100))
        {
            return true;
        }
        if(version.startsWith("0.")) {
            // seems to be older then 0.93
            return false;
        }
        return true;
    }

    public boolean isAvailable () {
        return isAvailable(false, false);
    }

    public boolean isAvailable (boolean notifyUI) {
        return isAvailable(false, notifyUI);
    }

    /**
     * Tests if hg is or is not available
     * @param forceCheck if version command has not been invoked yet and forceCheck is true, it will be, otherwise the command will be skipped
     * @param notifyUI if true and hg is not available, a dialog will be shown and a message will be printed into a logger
     * @return
     */
    public boolean isAvailable (boolean forceCheck, boolean notifyUI) {
        synchronized(this) {
            if (!gotVersion) {
                // version has not been scanned yet, run the version command
                LOG.log(Level.FINE, "Call to hg version not finished"); // NOI18N
                if(forceCheck) {
                    checkVersionIntern();
                } else {
                    return true;
                }
            }
        }
        if (version != null && !goodVersion) {
            // hg is present but it's version is unsupported
            // a warning message is printed into log, always only once per netbeans session
            OutputLogger logger = getLogger(Mercurial.MERCURIAL_OUTPUT_TAB_TITLE);
            logger.outputInRed(NbBundle.getMessage(Mercurial.class, "MSG_USING_UNRECOGNIZED_VERSION_MSG", version)); // NOI18N);
            logger.closeLog();
            LOG.log(Level.WARNING, "Using an unsupported hg version: {0}", version); //NOI18N
            goodVersion = true; // do not show the warning next time
        } else if (version == null) {
            // hg is not present at all, show a warning dialog
            if (notifyUI) {
                OutputLogger logger = getLogger(Mercurial.MERCURIAL_OUTPUT_TAB_TITLE);
                logger.outputInRed(NbBundle.getMessage(Mercurial.class, "MSG_VERSION_NONE_OUTPUT_MSG")); // NOI18N);
                HgUtils.warningDialog(Mercurial.class, "MSG_VERSION_NONE_TITLE", "MSG_VERSION_NONE_MSG"); //NOI18N
                logger.closeLog();
                LOG.warning("Hg is not available");     //NOI18N
            }
        }
        return goodVersion; // true if hg is present
    }

    public MercurialAnnotator getMercurialAnnotator() {
        return mercurialAnnotator;
    }

    MercurialInterceptor getMercurialInterceptor() {
        return mercurialInterceptor;
    }

    /**
     * Gets the File Status Cache for the mercurial repository
     *
     * @return FileStatusCache for the repository
     */
    public FileStatusCache getFileStatusCache() {
        return fileStatusCache;
    }

    /**
     * Refreshes cached modification timestamp of the repository's hg folder
     * @param repository owner of the hg folder to refresh
     */
    public void refreshWorkingCopyTimestamp(File repository) {
        getMercurialInterceptor().refreshHgFolderTimestamp(repository);
    }

    /**
     * Returns a set of known repository roots (those visible or open in IDE)
     * @param repositoryRoot
     * @return
     */
    public Set<File> getSeenRoots (File repositoryRoot) {
        return getMercurialInterceptor().getSeenRoots(repositoryRoot);
    }

   /**
     * Tests whether a file or directory should receive the STATUS_NOTVERSIONED_NOTMANAGED status.
     * All files and folders that have a parent with CVS/Repository file are considered versioned.
     *
     * @param file a file or directory
     * @return false if the file should receive the STATUS_NOTVERSIONED_NOTMANAGED status, true otherwise
     */
    public boolean isManaged(File file) {
        return VersioningSupport.getOwner(file) instanceof MercurialVCS && !HgUtils.isPartOfMercurialMetadata(file);
    }

    private final RootsToFile rootsToFile = new RootsToFile();
    public File getRepositoryRoot(File file) {
        File oFile = file;

        rootsToFile.logStatistics();
        File root = rootsToFile.get(file, true);
        if(root != null) {
            return root;
        }

        root = mvcs.getTopmostManagedAncestor(file);
        if(root != null) {
            if(file.isFile()) file = file.getParentFile();
            List<File> folders = new ArrayList<File>();
            for (; file != null && !file.getAbsolutePath().equals(root.getAbsolutePath()) ; file = file.getParentFile()) {
                File knownRoot = rootsToFile.get(file);
                if(knownRoot != null) {
                    rootsToFile.put(folders, knownRoot);
                    rootsToFile.put(oFile, knownRoot);
                    return knownRoot;
                }
                folders.add(file);
                if(HgUtils.hgExistsFor(file)) {
                    rootsToFile.put(folders, file);
                    rootsToFile.put(oFile, file);
                    return file;
                }
            }
            folders.add(root);
            rootsToFile.put(folders, root);
            rootsToFile.put(oFile, root);
            return root;
        }
        return null;
    }

    private static class RootsToFile {
        private static Logger LOG = Logger.getLogger("org.netbeans.modules.mercurial.RootsToFile"); // NOI18N
        private LinkedList<File> order = new LinkedList<File>();
        private Map<File, File> files = new HashMap<File, File>();
        private long cachedAccesCount = 0;
        private long accesCount = 0;
        private int statisticsFrequency = 0;

        public RootsToFile() {
            String s = System.getProperty("mercurial.root.stat.frequency", "0"); // NOI18N
            statisticsFrequency = Integer.parseInt(s);
        }
        synchronized void put(Collection<File> files, File root) {
            for (File file : files) {
                put(file, root);
            }
        }
        synchronized void put(File file, File root) {
            if(order.size() > 1500) {
                for (int i = 0; i < 150; i++) {
                    files.remove(order.getFirst());
                    order.removeFirst();
                }
            }
            order.addLast(file);
            files.put(file, root);
        }
        synchronized File get(File file) {
            return get(file, false);
        }
        synchronized File get(File file, boolean statistics) {
            File root = files.get(file);
            if(statistics && LOG.isLoggable(Level.FINEST)) {
               cachedAccesCount += root != null ? 1 : 0;
               accesCount++;
            }
            return root;
        }
        synchronized int size() {
            return order.size();
        }
        synchronized void logStatistics() {
            if(!LOG.isLoggable(Level.FINEST) ||
               (statisticsFrequency > 0 && (accesCount % statisticsFrequency != 0)))
            {
                return;
            }

            LOG.finest("HG Repository roots cache statistics:\n" +                                    // NOI18N
                     "  cached roots size       = " + order.size() + "\n" +                         // NOI18N
                     "  access count            = " + accesCount + "\n" +                           // NOI18N
                     "  cached access count     = " + cachedAccesCount + "\n" +                     // NOI18N
                     "  not cached access count = " + (accesCount - cachedAccesCount) + "\n");      // NOI18N
        }

        synchronized void clear () {
            order.clear();
            files.clear();
            cachedAccesCount = 0;
            accesCount = 0;
        }
    }

   /**
     * Uses content analysis to return the mime type for files.
     *
     * @param file file to examine
     * @return String mime type of the file (or best guess)
     */
    public String getMimeType(File file) {
        FileObject fo = FileUtil.toFileObject(file);
        String foMime;
        if (fo == null) {
            foMime = "content/unknown"; // NOI18N
        } else {
            foMime = fo.getMIMEType();
            if ("content/unknown".equals(foMime)) { // NOI18N
                foMime = "text/plain"; // NOI18N
            }
        }
        if ((fileStatusCache.getStatus(file).getStatus() & FileInformation.STATUS_VERSIONED) == 0) {
            return HgUtils.isFileContentBinary(file) ? "application/octet-stream" : foMime; // NOI18N
        } else {
            return foMime;
        }
    }

    public void versionedFilesChanged() {
        rootsToFile.clear();
        support.firePropertyChange(PROP_VERSIONED_FILES_CHANGED, null, null);
    }

    public void refreshAllAnnotations() {
        support.firePropertyChange(PROP_ANNOTATIONS_CHANGED, null, null);
    }

    public void changesetChanged(File repository) {
        support.firePropertyChange(PROP_CHANGESET_CHANGED, repository, null);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public void getOriginalFile(File workingCopy, File originalFile) {
        FileInformation info = fileStatusCache.getStatus(workingCopy);
        LOG.log(Level.FINE, "getOriginalFile: {0} {1}", new Object[] {workingCopy, info}); // NOI18N
        if ((info.getStatus() & STATUS_DIFFABLE) == 0) return;

        // We can get status returned as UptoDate instead of LocallyNew
        // because refreshing of status after creation has been scheduled
        // but may not have happened yet.

        try {
            File original = VersionsCache.getInstance().getFileRevision(workingCopy, Setup.REVISION_BASE);
            if (original == null) return;
            org.netbeans.modules.versioning.util.Utils.copyStreamsCloseAll(new FileOutputStream(originalFile), new FileInputStream(original));
            original.delete();
        } catch (IOException e) {
            Logger.getLogger(Mercurial.class.getName()).log(Level.INFO, "Unable to get original file", e); // NOI18N
        }
    }

    /**
     * Serializes all Hg requests (moves them out of AWT).
     */
    public RequestProcessor getRequestProcessor() {
        return getRequestProcessor((HgURL) null);
    }

    /**
     * Request processor for parallel tasks
     * @return
     */
    public synchronized RequestProcessor getParallelRequestProcessor() {
        if (parallelRP == null) {
            parallelRP = new RequestProcessor("Mercurial.ParallelRP", 5, true); //NOI18N
        }
        return parallelRP;
    }

    /**
     * Serializes all Hg requests (moves them out of AWT).
     */
    public RequestProcessor getRequestProcessor(File file) {
        return getRequestProcessor(new HgURL(file));
    }

    /**
     * @param  url  URL or {@code null}
     */
    public RequestProcessor getRequestProcessor(HgURL url) {
        if(processorsToUrl == null) {
            processorsToUrl = new HashMap<HgURL, RequestProcessor>();
        }

        RequestProcessor rp = processorsToUrl.get(url);   //'url' can be null
        if (rp == null) {
            String rpName = "Mercurial - "                              //NOI18N
                           + (url != null ? url.toString() : "ANY_KEY");//NOI18N
            rp = new RequestProcessor(rpName, 1, true);
            processorsToUrl.put(url, rp);
        }
        return rp;
    }

    public void clearRequestProcessor(HgURL url) {
        if(processorsToUrl != null & url != null) {
             processorsToUrl.remove(url);
        }
    }

    public void notifyFileChanged(File file) {
        fileStatusCache.notifyFileChanged(file);
    }

    /**
     *
     * @param repositoryRoot String of Mercurial repository so that logger writes to correct output tab. Can be null
     * in which case the logger will not print anything
     * @return OutputLogger logger to write to
     */
    public OutputLogger getLogger(String repositoryRoot) {
        return OutputLogger.getLogger(repositoryRoot);
    }

    public Boolean isRefreshScheduled(File file) {
        return mercurialInterceptor.isRefreshScheduled(file);
    }

    /**
     *
     * @return registered hyperlink providers
     */
    public List<VCSHyperlinkProvider> getHyperlinkProviders() {
        if (hpResult == null) {
            hpResult = (Result<? extends VCSHyperlinkProvider>) Lookup.getDefault().lookupResult(VCSHyperlinkProvider.class);
        }
        if (hpResult == null) {
            return Collections.EMPTY_LIST;
        }
        Collection<? extends VCSHyperlinkProvider> providersCol = hpResult.allInstances();
        List<VCSHyperlinkProvider> providersList = new ArrayList<VCSHyperlinkProvider>(providersCol.size());
        providersList.addAll(providersCol);
        return Collections.unmodifiableList(providersList);
    }

    /**
     * Returns scanned version or null if has not been scanned yet
     * @return
     */
    public String getVersion () {
        return version;
    }
}
