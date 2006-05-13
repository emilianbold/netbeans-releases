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

package org.netbeans.modules.subversion;

import java.util.*;
import java.util.regex.Matcher;
import org.netbeans.modules.masterfs.providers.InterceptionListener;
import org.netbeans.modules.subversion.client.SvnClientFactory;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.UnsupportedSvnClientAdapter;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.ErrorManager;
import org.tigris.subversion.svnclientadapter.*;
import org.tigris.subversion.svnclientadapter.commandline.CmdLineClientAdapterFactory;
import org.openide.util.RequestProcessor;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.netbeans.modules.subversion.config.ProxyDescriptor;
import org.netbeans.api.queries.SharabilityQuery;

/**
 * A singleton Subversion manager class, center of Subversion module. Use {@link #getInstance()} to get access
 * to Subversion module functionality.
 * 
 * @author Maros Sandor
 */
public class Subversion {
    
    static final String INVALID_METADATA_MARKER = "invalid-metadata";
    
    private static Subversion instance;
    
    private FileStatusCache     fileStatusCache;
    private FilesystemHandler   filesystemHandler;
    private Annotator           annotator;
    private HashMap             processorsToUrl;

    private OutputLogger outputLogger;

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
        try {
            Diagnostics.init();
            CmdLineClientAdapterFactory.setup();
        } catch (SVNClientException ex) {
            ErrorManager.getDefault().annotate(ex, UnsupportedSvnClientAdapter.getMessage());
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            ErrorManager.getDefault().notify(ErrorManager.USER, ex);
        }

        fileStatusCache = new FileStatusCache();
        annotator = new Annotator(this);
        filesystemHandler  = new FilesystemHandler(this);
        cleanup();
    }

    private void cleanup() {
        getRequestProcessor().post(new Runnable() {
            public void run() {
                try {
                    Diagnostics.println("Cleaning up");
                    // HACK: FileStatusProvider cannot do it itself
                    if (FileStatusProvider.getInstance() != null) {
                        // must be called BEFORE cache is cleaned up
                        fileStatusCache.addVersioningListener(FileStatusProvider.getInstance());
                        FileStatusProvider.getInstance().init();
                    }
    //                MetadataAttic.cleanUp();
                    // must be called AFTER the filestatusprovider is attached
                    fileStatusCache.cleanUp();
                    filesystemHandler.init();
                } finally {
                    Diagnostics.println("END Cleaning up");
                }
            }
        }, 3000);
    }
    
    public void shutdown() {        
        filesystemHandler.shutdown();
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
     * @return true if the buffer is almost certainly binary.
     * Note: Non-ASCII based encoding encoded text is binary,
     * newlines cannot be reliably detected.
     */
    public boolean isBinary(byte[] buffer) {
        for (int i = 0; i<buffer.length; i++) {
            int ch = buffer[i];
            if (ch < 32 && ch != '\t' && ch != '\n' && ch != '\r') {
                return true;
            }
        }
        return false;
    }


    /**
     * Tests <tt>.svn</tt> directory itself.  
     */
    public boolean isAdministrative(File file) {
        String name = file.getName();
        return isAdministrative(name) && file.isDirectory();
    }

    public boolean isAdministrative(String fileName) {
        return fileName.equals(".svn") || fileName.equals("_svn");
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
        SvnClient client = SvnClientFactory.getInstance().createSvnClient(repositoryUrl, pd, username, password);            
        attachListeners(client, false);            

        return client;
    }

    public SvnClient getClient(File file) throws SVNClientException {
        SVNUrl repositoryUrl = SvnUtils.getRepositoryRootUrl(file);
        assert repositoryUrl != null : "Unable to get repository: " + file.getAbsolutePath() + " is probably unmanaged.";

        return getClient(repositoryUrl);
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

        assert repositoryUrl != null : "Unable to get repository, context contains only unmanaged files!";

        return getClient(repositoryUrl, support);
    }

    public SvnClient getClient(SVNUrl repositoryUrl, boolean quite) throws SVNClientException {
        return getClient(repositoryUrl, null, quite);
    }
    
    public SvnClient getClient(SVNUrl repositoryUrl) throws SVNClientException {
        return getClient(repositoryUrl, null, false);
    }

    public SvnClient getClient(SVNUrl repositoryUrl, SvnProgressSupport support) throws SVNClientException {
        return getClient(repositoryUrl, support, false);
    }

    // XXX quite is a hot fix
    private SvnClient getClient(SVNUrl repositoryUrl, SvnProgressSupport support, boolean quite) throws SVNClientException {
        SvnClient client = SvnClientFactory.getInstance().createSvnClient(repositoryUrl, support);
        attachListeners(client, quite);
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
    public SvnClient getClient() {        
        SvnClient client = SvnClientFactory.getInstance().createSvnClient();
        attachListeners(client, false);
        return client;
    }            
    
    public InterceptionListener getFileSystemHandler() {
        return filesystemHandler;
    }

    /**
     * Tests whether a file or directory is managed by Subversion. All files and folders that have a parent with .svn/entriesy
     * file are considered managed by WC. This method accesses disk and should NOT be routinely called.
     *
     * <p>It works even for non-existing files where cache return UNKNOWN.
     *
     * @param file a file or directory
     * @return true if the file is under WC management, false otherwise
     */
    public boolean isManaged(File file) {
        if (isAdministrative(file)) return false;
        if (file.isFile()) file = file.getParentFile();
        for (; file != null; file = file.getParentFile()) {
            if (new File(file, ".svn/entries").canRead() || new File(file, "_svn/entries").canRead()) {
                return true;
            }
        }
        return false;
    }

    private void attachListeners(SvnClient client, boolean quite) {
        client.addNotifyListener(getLogger()); 
        if(!quite) {
            client.addNotifyListener(fileStatusCache);
        }
    }

    public OutputLogger getLogger() {
        if(outputLogger==null) {
           outputLogger = new OutputLogger();
        }
        return outputLogger;
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
    public boolean isIgnored(File file) {
        String name = file.getName();

        // ask SVN

        File parent = file.getParentFile();
        if (parent != null) {
            FileStatusCache cache = Subversion.getInstance().getStatusCache();
            int pstatus = cache.getStatus(parent).getStatus();
            if ((pstatus & FileInformation.STATUS_VERSIONED) != 0) {
                try {
                    SvnClient client = Subversion.getInstance().getClient();
                    client.removeNotifyListener(Subversion.getInstance().getLogger());

                    // XXX property can contain shell patterns (almost identical to RegExp)
                    List<String> patterns = client.getIgnoredPatterns(parent);
                    for (Iterator<String> i = patterns.iterator(); i.hasNext();) {
                        try {
                            String patternString = regExpToFilePatters(i.next());                            
                            Pattern pattern =  Pattern.compile(patternString);
                            if (pattern.matcher(name).matches()) {
                                return true;
                            }
                        } catch (PatternSyntaxException e) {
                            // XXX it's difference between shell and regexp
                            // or user error (set invalid property), rethrow?
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                        }
                    }
                } catch (SVNClientException ex)  {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
        }

        // ask projects and sync witn SVN
        
        int sharability = SharabilityQuery.getSharability(file);
        if (sharability == SharabilityQuery.NOT_SHARABLE) {
            try {
                // if IDE-ignore-root then propagate IDE opinion to Subversion svn:ignore
                if (SharabilityQuery.getSharability(parent) !=  SharabilityQuery.NOT_SHARABLE) {
                    FileStatusCache cache = Subversion.getInstance().getStatusCache();
                    if ((cache.getStatus(parent).getStatus() & FileInformation.STATUS_VERSIONED) != 0) {
                        List patterns = Subversion.getInstance().getClient().getIgnoredPatterns(parent);
                        if (patterns.contains(file.getName()) == false) {
                            patterns.add(file.getName());
                            Subversion.getInstance().getClient().setIgnoredPatterns(parent, patterns);
                        } else {
                            assert false : "Matcher failed for: " + parent.getAbsolutePath() + " file: " + file.getName();
                        }
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

    private String regExpToFilePatters(String exp) {
        exp = exp.replaceAll("\\.", "\\\\.");  
        exp = exp.replaceAll("\\*", ".*");  
        exp = exp.replaceAll("\\?", ".");   
        return exp;
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
            processorsToUrl = new HashMap();
        }

        String key;
        if(url != null) {
            key = url.toString();
        } else {
            key = "ANY_URL";
        }

        RequestProcessor rp = (RequestProcessor) processorsToUrl.get(key);
        if(rp == null) {
            rp = new RequestProcessor("Subversion - " + key, 1, true); // NOI18N
            processorsToUrl.put(key, rp);
        }
        return rp;
    }
}
