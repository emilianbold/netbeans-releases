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

package org.netbeans.modules.subversion;

import org.netbeans.modules.subversion.config.SvnConfigFiles;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.client.*;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.tigris.subversion.svnclientadapter.*;
import org.openide.util.RequestProcessor;
import java.io.*;
import java.util.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.subversion.config.ProxyDescriptor;
import org.netbeans.modules.subversion.ui.diff.Setup;
import org.netbeans.modules.subversion.ui.ignore.IgnoreAction;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.modules.versioning.spi.OriginalContent;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.util.VersioningEvent;
import org.netbeans.modules.versioning.VersioningManager;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.modules.subversion.ui.repository.RepositoryConnection;

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
    
    static final String INVALID_METADATA_MARKER = "invalid-metadata"; // NOI18N
    
    private static final int STATUS_DIFFABLE = 
            FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | 
            FileInformation.STATUS_VERSIONED_ADDEDLOCALLY | 
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
    private Annotator                           annotator;
    private HashMap<String, RequestProcessor>   processorsToUrl;

    private SvnClient noUrlClientWithoutListeners;
    private SvnClient noUrlClientWithListeners;
    
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

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
        Diagnostics.init();
        loadIniParserClassesWorkaround();
        setupSvnClientFactory();
        
        fileStatusCache = new FileStatusCache();
        annotator = new Annotator(this);
        fileStatusProvider = new FileStatusProvider();
        filesystemHandler  = new FilesystemHandler(this);
        cleanup();
    }
    
    public static void setupSvnClientFactory() {
        try {
            SvnClientFactory.getInstance().setup();
        } catch (SVNClientException ex) {
            Logger.getLogger("org.netbeans.modules.subversion").log(Level.INFO, UnsupportedSvnClientAdapter.getMessage());
            
            // ErrorManager.getDefault().annotate(ex, UnsupportedSvnClientAdapter.getMessage());
            // ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            // TODO: #78951 hotfix
//            ErrorManager.getDefault().notify(ErrorManager.USER, ex);
        }        
    }        
            
    /**
     * Ini4j uses context classloader to load classes, use this as a workaround. 
     */ 
    private void loadIniParserClassesWorkaround() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        try {
            SvnConfigFiles.getInstance();   // triggers ini4j initialization
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    private void cleanup() {
        getRequestProcessor().post(new Runnable() {
            public void run() {
                try {
                    Diagnostics.println("Cleaning up"); // NOI18N
                    fileStatusCache.cleanUp();
                    // TODO: refresh all annotations        
                } finally {
                    Diagnostics.println("END Cleaning up"); // NOI18N
                }
            }
        }, 3000);
    }
    
    public void shutdown() {
        fileStatusProvider.shutdown();
        // TODO: refresh all annotations        
    }

    public SvnFileNode [] getNodes(Context context, int includeStatus) {
        File [] files = fileStatusCache.listFiles(context, includeStatus);
        SvnFileNode [] nodes = new SvnFileNode[files.length];
        for (int i = 0; i < files.length; i++) {
            nodes[i] = new SvnFileNode(files[i]);
        }
        return nodes;
    }

    /**
     * Reads the svn:mime-type property or uses content analysis for unversioned files.
     * 
     * @param file file to examine
     * @return String mime type of the file (or best guess)
     */ 
    public String getMimeType(File file) {
        FileObject fo = FileUtil.toFileObject(file);
        String foMime;
        if (fo == null) {
            foMime = "content/unknown";
        } else {
            foMime = fo.getMIMEType();
            if ("content/unknown".equals(foMime)) {
                foMime = "text/plain";
            }
        }
        if ((fileStatusCache.getStatus(file).getStatus() & FileInformation.STATUS_VERSIONED) == 0) {
            return SvnUtils.isFileContentBinary(file) ? "application/octet-stream" : foMime;
        } else {
            PropertiesClient client = new PropertiesClient(file);
            try {
                byte [] mimeProperty = client.getProperties().get("svn:mime-type");
                if (mimeProperty == null) return foMime;
                return new String(mimeProperty);
            } catch (IOException e) {
                return foMime;
            }
        }
    }

    /**
     * Tests <tt>.svn</tt> directory itself.  
     */
    public boolean isAdministrative(File file) {
        String name = file.getName();
        return isAdministrative(name) && file.isDirectory();
    }

    public boolean isAdministrative(String fileName) {
        return fileName.equals(".svn") || fileName.equals("_svn"); // NOI18N
    }
    
    public FileStatusCache getStatusCache() {
        return fileStatusCache;
    }

    public Annotator getAnnotator() {
        return annotator;
    }

    public SvnClient getClient(SVNUrl repositoryUrl,
                               ProxyDescriptor pd, 
                               String username, 
                               String password) 
    throws SVNClientException    
    {
        return getClient(repositoryUrl, pd, username, password, SvnClientExceptionHandler.EX_DEFAULT_HANDLED_EXCEPTIONS);                    
    }
    
    public SvnClient getClient(SVNUrl repositoryUrl,
                               ProxyDescriptor pd, 
                               String username, 
                               String password,
                               int handledExceptions) throws SVNClientException {
        SvnClient client = SvnClientFactory.getInstance().createSvnClient(repositoryUrl, null, pd, username, password, handledExceptions);            
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
        for (int i = 0; i<roots.length; i++) {
             repositoryUrl = SvnUtils.getRepositoryRootUrl(roots[0]);
            if (repositoryUrl != null) {
                break;
            }
        }

        assert repositoryUrl != null : "Unable to get repository, context contains only unmanaged files!"; // NOI18N

        return getClient(repositoryUrl, support);
    }
    
    public SvnClient getClient(SVNUrl repositoryUrl) throws SVNClientException {
        return getClient(repositoryUrl, null);
    }

    public SvnClient getClient(SVNUrl repositoryUrl, SvnProgressSupport support) throws SVNClientException {  
        String username = ""; // NOI18N
        String password = ""; // NOI18N
        RepositoryConnection rc = SvnModuleConfig.getDefault().getRepositoryConnection(repositoryUrl.toString());        
        if(rc != null) {
            username = rc.getUsername();
            password = rc.getPassword();            
        }          
        SvnClient client = SvnClientFactory.getInstance().createSvnClient(repositoryUrl, support, null, username, password, SvnClientExceptionHandler.EX_DEFAULT_HANDLED_EXCEPTIONS);
        attachListeners(client);
        return client;
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
    public SvnClient getClient(boolean attachListeners) {        
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
    
    public FilesystemHandler getFileSystemHandler() {
        return filesystemHandler;
    }

    /**
     * Tests whether a file or directory should receive the STATUS_NOTVERSIONED_NOTMANAGED status. 
     * All files and folders that have a parent with CVS/Repository file are considered versioned.
     * 
     * @param file a file or directory
     * @return false if the file should receive the STATUS_NOTVERSIONED_NOTMANAGED status, true otherwise
     */ 
    public boolean isManaged(File file) {
        return VersioningManager.getInstance().getOwner(file) instanceof SubversionVCS && !SvnUtils.isPartOfSubversionMetadata(file);
    }

    public void versionedFilesChanged() {
        support.firePropertyChange(PROP_VERSIONED_FILES_CHANGED, null, null);
    }
    
    /**
     * Tests whether the file is managed by this versioning system. If it is, the method should return the topmost 
     * parent of the file that is still versioned.
     *  
     * @param file a file
     * @return File the file itself or one of its parents or null if the supplied file is NOT managed by this versioning system
     */
    File getTopmostManagedParent(File file) {
        if (SvnUtils.isPartOfSubversionMetadata(file)) {
            for (;file != null; file = file.getParentFile()) {
                if (isAdministrative(file)) {
                    file = file.getParentFile();
                    break;
                }
            }
        }
        File topmost = null;
        for (; file != null; file = file.getParentFile()) {
            if (new File(file, ".svn/entries").canRead() || new File(file, "_svn/entries").canRead()) { // NOI18N
                topmost = file;
            }
        }
        return topmost;
    }
    
    /**
     * TODO: Backdoor for SvnClientFactory
     */ 
    public void cleanupFilesystem() {
        filesystemHandler.removeInvalidMetadata();
    }

    private void attachListeners(SvnClient client) {
        client.addNotifyListener(getLogger(client.getSvnUrl())); 
        client.addNotifyListener(fileStatusCache);
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
    boolean isIgnored(final File file) {
        String name = file.getName();

        // ask SVN

        final File parent = file.getParentFile();
        if (parent != null) {
            int pstatus = fileStatusCache.getStatus(parent).getStatus();
            if ((pstatus & FileInformation.STATUS_VERSIONED) != 0) {
                try {
                    SvnClient client = getClient(false);
                    
                    List<String> patterns = client.getIgnoredPatterns(parent);
                    List<String> gignores = SvnConfigFiles.getInstance().getGlobalIgnores();
                    // merge global ignores and ignore patterns
                    for (Iterator<String> it = gignores.iterator(); it.hasNext(); patterns.add(it.next()));

                    if(SvnUtils.getMatchinIgnoreParterns(patterns,name, true).size() > 0) {
                        return true;
                    }
                    
                } catch (SVNClientException ex)  {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
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
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
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

    /**
     * Refreshes all textual annotations and badges.
     */
    public void refreshAllAnnotations() {
        support.firePropertyChange(PROP_ANNOTATIONS_CHANGED, null, null);
    }

    void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public OriginalContent getVCSOriginalContent(File file) {
        FileInformation info = fileStatusCache.getStatus(file);
        if ((info.getStatus() & STATUS_DIFFABLE) == 0) return null;
        return new SubversionOriginalContent(file);
    }
    
    
    private class SubversionOriginalContent extends OriginalContent implements VersioningListener {
        
        public SubversionOriginalContent(File working) { 
            super(working);
        }

        protected void getOriginalFiles(File destination, Set<File> files) throws Exception {
            for (File file : files) {
                File original = VersionsCache.getInstance().getFileRevision(file, Setup.REVISION_BASE);
                if (original == null) throw new IOException("Unable to get BASE revision of " + file);

                File daoFile = new File(destination, file.getName());
                org.netbeans.modules.versioning.util.Utils.copyStreamsCloseAll(new FileOutputStream(daoFile), new FileInputStream(original));
                daoFile.deleteOnExit();
            }
        }

        public void versioningEvent(VersioningEvent event) {
            if (FileStatusCache.EVENT_FILE_STATUS_CHANGED == event.getId()) {
                File eventFile = (File) event.getParams()[0];
                if (eventFile.equals(workingCopy)) {
                    support.firePropertyChange(PROP_CONTENT_CHANGED, null, null);
                }
            }
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            if (!support.hasListeners(null)) {
                fileStatusCache.addVersioningListener(this);
            }
            super.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            super.removePropertyChangeListener(listener);
            if (!support.hasListeners(null)) {
                fileStatusCache.removeVersioningListener(this);
            }
        }
    }
    
}
