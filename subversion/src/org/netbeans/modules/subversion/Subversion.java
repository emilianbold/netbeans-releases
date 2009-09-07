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

package org.netbeans.modules.subversion;

import java.net.MalformedURLException;
import org.netbeans.modules.subversion.config.SvnConfigFiles;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.client.*;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.tigris.subversion.svnclientadapter.*;
import org.openide.util.RequestProcessor;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import org.netbeans.modules.subversion.ui.ignore.IgnoreAction;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.modules.subversion.hooks.spi.SvnHook;
import org.netbeans.modules.subversion.ui.repository.RepositoryConnection;
import org.netbeans.modules.versioning.util.HyperlinkProvider;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;

/**
 * A singleton Subversion manager class, center of Subversion module. Use {@link #getInstance()} to get access
 * to Subversion module functionality.
 *
 * @author Maros Sandor
 */
public class Subversion {

    /**
     * Fired when textual annotations and badges have changed. The NEW value is Set<File> of files that changed or NULL
     * if all annotaions changed.
     */
    static final String PROP_ANNOTATIONS_CHANGED = "annotationsChanged";

    static final String PROP_VERSIONED_FILES_CHANGED = "versionedFilesChanged";

    /**
     * Results in refresh of annotations and diff sidebars
     */
    static final String PROP_BASE_FILE_CHANGED = "baseFileChanged";     //NOI18N

    static final String INVALID_METADATA_MARKER = "invalid-metadata"; // NOI18N

    private static final int STATUS_DIFFABLE =
            FileInformation.STATUS_VERSIONED_UPTODATE |
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY |
            FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY |
            FileInformation.STATUS_VERSIONED_CONFLICT |
            FileInformation.STATUS_VERSIONED_MERGE |
            FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY |
            FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY |
            FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY;

    private static Subversion instance;

    private FileStatusCache                     fileStatusCache;
    private FilesystemHandler                   filesystemHandler;
    private FileStatusProvider                  fileStatusProvider;
    private SvnClientRefreshHandler             refreshHandler;
    private Annotator                           annotator;
    private HashMap<String, RequestProcessor>   processorsToUrl;

    private SvnClient noUrlClientWithoutListeners;
    private SvnClient noUrlClientWithListeners;
    private List<ISVNNotifyListener> svnNotifyListeners;

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    public static final Logger LOG = Logger.getLogger("org.netbeans.modules.subversion");

    private Result<? extends SvnHook> hooksResult;
    private Result<? extends HyperlinkProvider> hpResult; 

    public static synchronized Subversion getInstance() {
        if (instance == null) {
            instance = new Subversion();
            instance.init();
        }
        return instance;
    }

    private Subversion() {
    }

    private void init() {
        fileStatusCache = new FileStatusCache();
        annotator = new Annotator(this);
        fileStatusProvider = new FileStatusProvider();
        filesystemHandler  = new FilesystemHandler(this);
        refreshHandler = new SvnClientRefreshHandler();
        prepareCache();
        // this should be registered in SubversionVCS but we needed to reduce number of classes loaded
        SubversionVCS svcs  = org.openide.util.Lookup.getDefault().lookup(SubversionVCS.class);
        fileStatusCache.addVersioningListener(svcs);
        addPropertyChangeListener(svcs);
    }

    private void prepareCache() {
        getRequestProcessor().post(new Runnable() {
            public void run() {
                try {
                    fileStatusCache.computeIndex();
                    LOG.fine("Cleaning up cache"); // NOI18N
                    fileStatusCache.cleanUp(); // do not call before computeIndex()
                } finally {
                    Subversion.LOG.fine("END Cleaning up cache"); // NOI18N
                }
            }
        }, 500);
    }

    public void shutdown() {
        fileStatusProvider.shutdown();
    }

    public FileStatusCache getStatusCache() {
        return fileStatusCache;
    }

    public Annotator getAnnotator() {
        return annotator;
    }

    public SvnClientRefreshHandler getRefreshHandler() {
        return refreshHandler;
    }

    public boolean checkClientAvailable() {
        if(SvnClientFactory.wasJavahlCrash()) {
            throw new RuntimeException("It appears that subversion javahl initialization caused trouble in a previous Netbeans session. Please report.");
        }
        try {
            SvnClientFactory.checkClientAvailable();
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return false;
        }
        return true;
    }

    public SvnClient getClient(SVNUrl repositoryUrl,
                               String username,
                               String password)
    throws SVNClientException
    {
        return getClient(repositoryUrl, username, password, SvnClientExceptionHandler.EX_DEFAULT_HANDLED_EXCEPTIONS);
    }

    public SvnClient getClient(SVNUrl repositoryUrl,
                               String username,
                               String password,
                               int handledExceptions) throws SVNClientException {
        SvnClient client = SvnClientFactory.getInstance().createSvnClient(repositoryUrl, null, username, password, handledExceptions);
        attachListeners(client);
        return client;
    }

    public SvnClient getClient(SVNUrl repositoryUrl, SvnProgressSupport progressSupport) throws SVNClientException {
        String username = ""; // NOI18N
        String password = ""; // NOI18N

        SvnKenaiSupport kenaiSupport = SvnKenaiSupport.getInstance();
        if(kenaiSupport.isKenai(repositoryUrl.toString())) {
            PasswordAuthentication pa = kenaiSupport.getPasswordAuthentication(false);
            if(pa != null) {
                username = pa.getUserName();
                password = new String(pa.getPassword());
            }
        } else {
            RepositoryConnection rc = findRepositoryConnection(repositoryUrl);
            if(rc != null) {
                username = rc.getUsername();
                password = rc.getPassword();
            }
        }
        return getClient(repositoryUrl, username, password, progressSupport);
    }

    public SvnClient getClient(SVNUrl repositoryUrl,
                               String username,
                               String password,
                               SvnProgressSupport support) throws SVNClientException {
        SvnClient client = SvnClientFactory.getInstance().createSvnClient(repositoryUrl, support, /*null, */username, password, SvnClientExceptionHandler.EX_DEFAULT_HANDLED_EXCEPTIONS);
        attachListeners(client);
        return client;
    }

    public SvnClient getClient(File file) throws SVNClientException {
        return getClient(file, null);
    }

    public SvnClient getClient(File file, SvnProgressSupport support) throws SVNClientException {
        SVNUrl repositoryUrl = SvnUtils.getRepositoryRootUrl(file);
        assert repositoryUrl != null : "Unable to get repository: " + file.getAbsolutePath() + " is probably unmanaged."; // NOI18N

        return getClient(repositoryUrl, support);
    }

    public SvnClient getClient(Context ctx, SvnProgressSupport support) throws SVNClientException {
        File[] roots = ctx.getRootFiles();
        SVNUrl repositoryUrl = null;
        for (File root : roots) {
            // XXX #168094 logging
            if (!SvnUtils.isManaged(root)) {
                Subversion.LOG.warning("getClient: unmanaged file in context: " + root.getAbsoluteFile()); //NOI18N
            }
            repositoryUrl = SvnUtils.getRepositoryRootUrl(root);
            if (repositoryUrl != null) {
                break;
            } else {
                Subversion.LOG.log(Level.WARNING, "Could not retrieve repository root for context file {0}", new Object[]{root});
            }
        }

        assert repositoryUrl != null : "Unable to get repository, context contains only unmanaged files!"; // NOI18N
        if (repositoryUrl == null) {
            // XXX #168094 logging
            // preventing NPE in getClient(repositoryUrl, support)
            StringBuilder sb = new StringBuilder("Cannot determine repositoryRootUrl for selected context:"); //NOI18N
            for (File root : roots) {
                sb.append("\n").append(root.getAbsolutePath());         //NOI18N
            }
            throw new SVNClientException(sb.toString());
        }
        return getClient(repositoryUrl, support);
    }

    public SvnClient getClient(SVNUrl repositoryUrl) throws SVNClientException {
        return getClient(repositoryUrl, null);
    }

    /**
     * <b>Creates</b> ClientAtapter implementation that already handles:
     * <ul>
     *    <li>prompts user for password if necessary,
     *    <li>let user specify proxy setting on network errors or
     *    <li>let user cancel operation
     *    <li>logs command execuion into output tab
     *    <li>posts notification events in status cache
     * </ul>
     *
     * <p>It hanldes cancellability
     */
    public SvnClient getClient(boolean attachListeners) throws SVNClientException {
        cleanupFilesystem();
        if(attachListeners) {
            if(noUrlClientWithListeners == null) {
                noUrlClientWithListeners = SvnClientFactory.getInstance().createSvnClient();
                attachListeners(noUrlClientWithListeners);
            }
            return noUrlClientWithListeners;
        } else {
            if(noUrlClientWithoutListeners == null) {
                noUrlClientWithoutListeners = SvnClientFactory.getInstance().createSvnClient();
            }
            return noUrlClientWithoutListeners;
        }
    }

    public void versionedFilesChanged() {
        support.firePropertyChange(PROP_VERSIONED_FILES_CHANGED, null, null);
    }

    /**
     * Backdoor for SvnClientFactory
     */
    public void cleanupFilesystem() {
        filesystemHandler.removeInvalidMetadata();
    }

    private void attachListeners(SvnClient client) {
        client.addNotifyListener(getLogger(client.getSvnUrl()));
        client.addNotifyListener(refreshHandler);

        List<ISVNNotifyListener> l = getSVNNotifyListeners();

        ISVNNotifyListener[] listeners = null;
        synchronized(l) {
            listeners = l.toArray(new ISVNNotifyListener[l.size()]);
        }
        for(ISVNNotifyListener listener : listeners) {
            client.addNotifyListener(listener);
        }
    }

    /**
     *
     * @param repositoryRoot URL of Subversion repository so that logger writes to correct output tab. Can be null
     * in which case the logger will not print anything
     * @return OutputLogger logger to write to
     */
    public OutputLogger getLogger(SVNUrl repositoryRoot) {
        return OutputLogger.getLogger(repositoryRoot);
    }

    /**
     * Non-recursive ignore check.
     *
     * <p>Side effect: if under SVN version control
     * it sets svn:ignore property
     *
     * @return true if file is listed in parent's ignore list
     * or IDE thinks it should be.
     */
    boolean isIgnored(File file) {
        String name = file.getName();
        file = FileUtil.normalizeFile(file);

        // ask SVN

        final File parent = file.getParentFile();
        if (parent != null) {
            int pstatus = fileStatusCache.getStatus(parent).getStatus();
            if ((pstatus & FileInformation.STATUS_VERSIONED) != 0) {
                try {
                    SvnClient client = getClient(false);

                    List<String> gignores = SvnConfigFiles.getInstance().getGlobalIgnores();
                    if(gignores != null && SvnUtils.getMatchinIgnoreParterns(gignores, name, true).size() > 0) {
                        // no need to read the ignored property -> its already set in ignore patterns
                        return true;
                    }
                    List<String> patterns = client.getIgnoredPatterns(parent);
                    if(patterns != null && SvnUtils.getMatchinIgnoreParterns(patterns, name, true).size() > 0) {
                        return true;
                    }

                } catch (SVNClientException ex)  {
                    if(!SvnClientExceptionHandler.isUnversionedResource(ex.getMessage()) && 
                       !SvnClientExceptionHandler.isCancelledAction(ex.getMessage()) &&
                       !SvnClientExceptionHandler.isTooOldClientForWC(ex.getMessage()))
                    {
                        SvnClientExceptionHandler.notifyException(ex, false, false);
                    }
                }
            }
        }

        if (SharabilityQuery.getSharability(file) == SharabilityQuery.NOT_SHARABLE) {
            try {
                // BEWARE: In NetBeans VISIBILTY == SHARABILITY ... and we hide Locally Removed folders => we must not Ignore them by mistake
                FileInformation info = fileStatusCache.getCachedStatus(file); // getStatus may cause stack overflow
                if (SubversionVisibilityQuery.isHiddenFolder(info, file)) {
                    return false;
                }
                // if IDE-ignore-root then propagate IDE opinion to Subversion svn:ignore
                if (SharabilityQuery.getSharability(parent) !=  SharabilityQuery.NOT_SHARABLE) {
                    if ((fileStatusCache.getStatus(parent).getStatus() & FileInformation.STATUS_VERSIONED) != 0) {
                        IgnoreAction.ignore(file);
                    }
                }
            } catch (SVNClientException ex) {
                if(!SvnClientExceptionHandler.isTooOldClientForWC(ex.getMessage())) {
                    SvnClientExceptionHandler.notifyException(ex, false, false);
                }
            }
            return true;
        } else {
            // backward compatability #68124
            if (".nbintdb".equals(name)) {  // NOI18N
                return true;
            }

            return false;
        }
    }

    /**
     * Serializes all SVN requests (moves them out of AWT).
     */
    public RequestProcessor getRequestProcessor() {
        return getRequestProcessor(null);
    }

    /**
     * Serializes all SVN requests (moves them out of AWT).
     */
    public RequestProcessor getRequestProcessor(SVNUrl url) {
        if(processorsToUrl == null) {
            processorsToUrl = new HashMap<String, RequestProcessor>();
        }

        String key;
        if(url != null) {
            key = url.toString();
        } else {
            key = "ANY_URL"; // NOI18N
        }

        RequestProcessor rp = processorsToUrl.get(key);
        if(rp == null) {
            rp = new RequestProcessor("Subversion - " + key, 1, true); // NOI18N
            processorsToUrl.put(key, rp);
        }
        return rp;
    }

    FileStatusProvider getVCSAnnotator() {
        return fileStatusProvider;
    }

    VCSInterceptor getVCSInterceptor() {
        return filesystemHandler;
    }

    private List<ISVNNotifyListener> getSVNNotifyListeners() {
        if(svnNotifyListeners == null) {
            svnNotifyListeners = new ArrayList<ISVNNotifyListener>();
        }
        return svnNotifyListeners;
    }

    /**
     * Refreshes all textual annotations and badges.
     */
    public void refreshAllAnnotations() {
        support.firePropertyChange(PROP_ANNOTATIONS_CHANGED, null, null);
    }

    /**
     * Refreshes all textual annotations and badges for the given files.
     *
     * @param files files to chage the annotations for
     */
    public void refreshAnnotations(File... files) {
        Set<File> s = new HashSet<File>();
        for (File file : files) {
            s.add(file);
        }
        support.firePropertyChange(PROP_ANNOTATIONS_CHANGED, null, s);
    }

    /**
     * Refreshes all textual annotations, badges and sidebars for the given files.
     *
     * @param files files to chage the annotations and sidebars for
     */
    public void refreshAnnotationsAndSidebars (File... files) {
        Set<File> s = new HashSet<File>();
        for (File file : files) {
            s.add(file);
        }
        support.firePropertyChange(PROP_BASE_FILE_CHANGED, null, s);
    }

    void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public void addSVNNotifyListener(ISVNNotifyListener listener) {
        List<ISVNNotifyListener> listeners = getSVNNotifyListeners();
        synchronized(listeners) {
            listeners.add(listener);
        }
    }

    public void removeSVNNotifyListener(ISVNNotifyListener listener) {
        List<ISVNNotifyListener> listeners = getSVNNotifyListeners();
        synchronized(listeners) {
            listeners.remove(listener);
        }
    }

    public void getOriginalFile(File workingCopy, File originalFile) {
        FileInformation info = fileStatusCache.getStatus(workingCopy);
        if ((info.getStatus() & STATUS_DIFFABLE) == 0) {
            return;
        }

        File original = null;
        try {
            SvnClientFactory.checkClientAvailable();
            original = VersionsCache.getInstance().getBaseRevisionFile(workingCopy);
            if (original == null) {
                throw new IOException("Unable to get BASE revision of " + workingCopy);
            }
            org.netbeans.modules.versioning.util.Utils.copyStreamsCloseAll(new FileOutputStream(originalFile), new FileInputStream(original));
        } catch (IOException e) {
            LOG.log(Level.INFO, "Unable to get original file", e);
        } catch (SVNClientException ex) {
            Subversion.LOG.log(Level.INFO, "Subversion.getOriginalFile: file is managed but svn client is unavailable (file " + workingCopy.getAbsolutePath() + ")"); //NOI18N
            if (Subversion.LOG.isLoggable(Level.FINE)) {
                Subversion.LOG.log(Level.FINE, null, ex);
            }
        } finally {
            if (original != null) {
                try {
                    original.delete();
                } catch (Exception ex) {
                    if (LOG.isLoggable(Level.WARNING)) {
                        LOG.warning("Failed to delete temporary file "  //NOI18N
                                    + original.getAbsolutePath());
                    }
                    //otherwise ignore the exception - leave the file as it is
                }
            }
        }
    }
    
    public List<SvnHook> getHooks() {
        if (hooksResult == null) {
            hooksResult = (Result<? extends SvnHook>) Lookup.getDefault().lookupResult(SvnHook.class);
        }
        if(hooksResult == null) {
            return Collections.EMPTY_LIST;
        }
        List<SvnHook> ret = new ArrayList<SvnHook>();
        Collection<? extends SvnHook> hooks = hooksResult.allInstances();
        if (hooks.size() > 0) {
            for (SvnHook hook : hooks) {
                ret.add(hook);
            }
        }
        return ret;
    }

    /**
     *
     * @return registered hyperlink providers
     */
    public List<HyperlinkProvider> getHyperlinkProviders() {
        if (hpResult == null) {
            hpResult = (Result<? extends HyperlinkProvider>) Lookup.getDefault().lookupResult(HyperlinkProvider.class);
        }
        if (hpResult == null) {
            return Collections.EMPTY_LIST;
        }
        Collection<? extends HyperlinkProvider> providersCol = hpResult.allInstances();
        List<HyperlinkProvider> providersList = new ArrayList<HyperlinkProvider>(providersCol.size());
        providersList.addAll(providersCol);
        return Collections.unmodifiableList(providersList);
    }

    private RepositoryConnection findRepositoryConnection(SVNUrl repositoryUrl) {
        RepositoryConnection rc = null;
        try {
            // this will remove username from the hostname
            rc = SvnModuleConfig.getDefault().getRepositoryConnection(new RepositoryConnection(repositoryUrl.toString()).getSvnUrl().toString());
        } catch (MalformedURLException ex) {
            // not interested
        }
        if (rc == null) {
            rc = SvnModuleConfig.getDefault().getRepositoryConnection(repositoryUrl.toString());
        }
        return rc;
    }
    
}
