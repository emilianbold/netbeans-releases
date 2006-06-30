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

package org.netbeans.modules.versioning.system.cvss.executor;

import org.netbeans.modules.versioning.system.cvss.*;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.DefaultFileInfoContainer;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.command.commit.CommitInformation;
import org.netbeans.lib.cvsclient.event.ModuleExpansionEvent;
import org.netbeans.api.progress.aggregate.ProgressContributor;
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
            File file = FileUtil.normalizeFile(info.getFile());
            cache.refreshCached(file, repositoryStatus);
            refreshedFiles.add(file);
        }
        
        // refresh all command roots
        File [] files = xcmd.getFiles();
        for (int i = 0; i < files.length; i++) {
            File file = FileUtil.normalizeFile(files[i]);
            refreshRecursively(file);
            if (file.isFile()) {
                cache.refreshCached(file.getParentFile(), FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
            }
            FileObject fo = FileUtil.toFileObject(file);
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
