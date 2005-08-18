/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.executor;

import org.netbeans.modules.versioning.system.cvss.*;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.DefaultFileInfoContainer;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.command.commit.CommitInformation;
import org.netbeans.lib.cvsclient.event.ModuleExpansionEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import java.util.*;
import java.io.File;

/**
 * Executes a given 'checkout' command and refreshes file statuses.
 * 
 * @author Maros Sandor
 */
public class CheckoutExecutor extends ExecutorSupport {
    
    private Set refreshedFiles;
    private Set expandedModules = new HashSet();

    public CheckoutExecutor(CvsVersioningSystem cvs, CheckoutCommand cmd) {
        this(cvs, cmd, null);
    }
    
    public CheckoutExecutor(CvsVersioningSystem cvs, CheckoutCommand cmd, GlobalOptions options) {
        super(cvs, cmd, options);
    }

    /**
     * Return expanded module names (String)s, If it contains "."
     * it means "unknown but all".  
     */
    public Set getExpandedModules() {
        return expandedModules;
    }

    public final void moduleExpanded(ModuleExpansionEvent e) {
        String module = e.getModule();
        expandedModules.add(module);
    }

    /**
     * Refreshes statuses of relevant files after this command terminates.
     */ 
    protected void commandFinished(ClientRuntime.Result result) {
        
        CheckoutCommand xcmd = (CheckoutCommand) cmd;
        
        // files that we have information that changed
        refreshedFiles = new HashSet(toRefresh.size());
        
        for (Iterator i = toRefresh.iterator(); i.hasNext();) {
            // XXX getting DefaultFileInfoContainer still filled by types from  CommitInformation
            DefaultFileInfoContainer info = (DefaultFileInfoContainer) i.next();
            int repositoryStatus = FileStatusCache.REPOSITORY_STATUS_UNKNOWN;
            if (CommitInformation.CHANGED.equals(info.getType()) || CommitInformation.ADDED.equals(info.getType())) {
                repositoryStatus = FileStatusCache.REPOSITORY_STATUS_UPTODATE;                
            } else if (CommitInformation.REMOVED.equals(info.getType()) || CommitInformation.TO_ADD.equals(info.getType())) {
                repositoryStatus = FileStatusCache.REPOSITORY_STATUS_UNKNOWN;
            }
            cache.refreshCached(info.getFile(), repositoryStatus);
            refreshedFiles.add(info.getFile());
        }
        
        // refresh all command roots
        File [] files = xcmd.getFiles();
        for (int i = 0; i < files.length; i++) {
            refreshRecursively(files[i]);
            if (files[i].isFile()) {
                cache.refreshCached(files[i].getParentFile(), FileStatusCache.REPOSITORY_STATUS_UNKNOWN);                
            }
            FileObject fo = FileUtil.toFileObject(files[i]);
            if (fo != null) {
                fo.refresh(true);
            }
        }
    }

    private void refreshRecursively(File file) {
        if (cvs.isIgnoredFilename(file)) return;
        if (refreshedFiles.contains(file)) return;
        if (file.isDirectory()) {
            File [] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                refreshRecursively(files[i]);
            }
            cache.refreshCached(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
        } else {
            if (cache.getStatus(file.getParentFile()).getStatus() == FileInformation.STATUS_VERSIONED_UPTODATE) {
                // do nothing
            } else {
                cache.refreshCached(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);                
            }
        }
    }
}
