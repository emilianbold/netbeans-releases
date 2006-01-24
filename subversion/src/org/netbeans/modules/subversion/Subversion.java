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
import org.netbeans.modules.subversion.util.Context;
import org.openide.ErrorManager;
import org.tigris.subversion.svnclientadapter.*;
import org.tigris.subversion.svnclientadapter.commandline.CmdLineClientAdapterFactory;
import org.openide.util.RequestProcessor;

import java.io.File;
import java.util.*;

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
            ErrorManager.getDefault().notify(ex);
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
    
    public ISVNClientAdapter getClient() {
        return SVNClientAdapterFactory.createSVNClient(CmdLineClientAdapterFactory.COMMANDLINE_CLIENT);
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
     * file are considered managed by CVS. This method accesses disk and should NOT be routinely called.
     * 
     * @param file a file or directory
     * @return true if the file is under CVS management, false otherwise
     */ 
    boolean isManaged(File file) {
        if (isAdministrative(file)) return false;
        if (file.isFile()) file = file.getParentFile();
        for (; file != null; file = file.getParentFile()) {
            if (new File(file, ".svn/entries").canRead()) return true;
            if (new File(file, "_svn/entries").canRead()) return true;
        }
        return false;
    }
}
