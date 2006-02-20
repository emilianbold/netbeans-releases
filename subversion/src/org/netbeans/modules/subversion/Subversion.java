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

import org.netbeans.modules.masterfs.providers.InterceptionListener;
import org.netbeans.modules.subversion.client.SvnClientFactory;
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

import org.netbeans.modules.subversion.client.ProxyDescriptor;
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
    private HashMap             clients;
    private RequestProcessor rp = new RequestProcessor("Subversion", 1, true); // NOI18N

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
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
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
            }
        }, 3000);
    }
    
    public void shutdown() {
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
        SvnClient client = (SvnClient) getClients().get(repositoryUrl.toString());
        if(client == null) {
            client = SvnClientFactory.getInstance().createSvnClient(repositoryUrl, pd, username, password);            
            attachListeners(client);
            getClients().put(repositoryUrl.toString(), client);            
        } else {
            // XXX - some kind of check if it's still the same configuration (proxy, psswd, user)            
        }               
        return client;
    }
    
    public SvnClient getClient(Context ctx) throws SVNClientException {
        File[] roots = ctx.getRootFiles();
        SVNUrl repositoryUrl = null;
        for (int i = 0; i<roots.length; i++) {
             repositoryUrl = SvnUtils.getRepositoryUrl(roots[0]);
            if (repositoryUrl != null) {
                break;
            }
        }

        assert repositoryUrl != null : "Unable to get repository, context contains only unmanaged files!";

        return getClient(repositoryUrl);
    }
    
    public SvnClient getClient(SVNUrl repositoryUrl) 
    throws SVNClientException 
    {        
        SvnClient client = (SvnClient) getClients().get(repositoryUrl.toString());
        if(client == null) {        
            client = SvnClientFactory.getInstance().createSvnClient(repositoryUrl);        
            attachListeners(client);
            getClients().put(repositoryUrl.toString(), client);
        }
        return client;
    }
    
    /**
     * <b>Creates</b> ClientAtapter implementation that already handles:
     * <ul>
     *    <li>prompts user for password if necessary,
     *    <li>let user specify proxy setting on network errors or
     *    <li>let user cancel operation (XXX then it throws SVN exception subclass)
     *    <li>logs command execuion into output tab
     *    <li>posts notification events in status cache
     * </ul>
     *
     * <p>It hanldes cancellability, XXX e.g. by Thread,interrupt?
     */
    public SvnClient getClient() {
        // XXX also cache ???
        SvnClient client = SvnClientFactory.getInstance().createSvnClient();
        attachListeners(client);
        return client;
    }    
    
    private HashMap getClients() {
        if(clients == null) {
            clients = new HashMap();
        }
        return clients;
    }    
    
    public ISVNStatus getLocalStatus(File file) throws SVNClientException {
        ISVNClientAdapter client = getClient();
        return client.getSingleStatus(file);        
    }
    
    public InterceptionListener getFileSystemHandler() {
        return filesystemHandler;
    }

    /**
     * Tests whether a file or directory is managed by Subversion. All files and folders that have a parent with .svn/Repository
     * file are considered managed by WC. This method accesses disk and should NOT be routinely called.
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

    private void attachListeners(SvnClient client) {
        client.addNotifyListener(new OutputLogger()); // XXX new ???
        client.addNotifyListener(fileStatusCache); 
    }

    public boolean isIgnored(File file) {
        if (file.isDirectory()) {
            File entries = new File(file, ".svn/entries");
            if (entries.canRead()) return false;
        }
        String name = file.getName();

        // backward compatability #68124
        if (".nbintdb".equals(name)) {  // NOI18N
            return true;
        }

        File parent = file.getParentFile();
        try {
            // XXX RE patterns?
            List patterns = Subversion.getInstance().getClient().getIgnoredPatterns(parent);

            for (Iterator i = patterns.iterator(); i.hasNext();) {
                try {
                    Pattern pattern =  Pattern.compile((String) i.next());
                    if (pattern.matcher(name).matches()) {
                        return true;
                    }
                } catch (PatternSyntaxException e) {
                    // XXX rethrow, assert?
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
        } catch (SVNClientException ex)  {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

        int sharability = SharabilityQuery.getSharability(file);
        if (sharability == SharabilityQuery.NOT_SHARABLE) {
            try {
                // propagate IDE opinion to Subversion svn:ignore
                List patterns = Subversion.getInstance().getClient().getIgnoredPatterns(parent);
                patterns.remove(file.getName());
                Subversion.getInstance().getClient().setIgnoredPatterns(parent, patterns);
            } catch (SVNClientException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
            return true;
        } else {
            return false;
        }

    }

    /**
     * Serializes all SVN requests (moves them out of AWT).
     */
    public RequestProcessor.Task postRequest(Runnable run) {
        return rp.post(run);
    }
}
