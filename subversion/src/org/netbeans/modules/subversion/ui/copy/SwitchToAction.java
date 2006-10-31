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

package org.netbeans.modules.subversion.ui.copy;

import java.io.File;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.FileStatusCache;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Petr Kuzel
 */
public class SwitchToAction extends ContextAction {
    
    /**
     * Creates a new instance of SwitchToAction
     */
    public SwitchToAction() {        
    }

    protected String getBaseName(Node[] activatedNodes) {
        return "CTL_MenuItem_Switch";    // NOI18N        
    }

    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_MANAGED 
             & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED 
             & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    }

    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED 
             & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED 
             & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    }

    protected boolean enable(Node[] nodes) {
        return nodes != null && nodes.length == 1 &&  getContext(nodes).getRoots().size() > 0;
    }        
    
    protected void performContextAction(final Node[] nodes) {
        Context ctx = getContext(nodes);        
        
        final File root = ctx.getRootFiles()[0];                        
        SVNUrl url = SvnUtils.getRepositoryRootUrl(root);
        final RepositoryFile repositoryRoot = new RepositoryFile(url, url, SVNRevision.HEAD);
        File[] files = Subversion.getInstance().getStatusCache().listFiles(ctx, FileInformation.STATUS_LOCAL_CHANGE);       
        boolean hasChanges = files.length > 0;

        final SwitchTo switchTo = new SwitchTo(repositoryRoot, root, hasChanges);
        if(switchTo.showDialog()) {
            ContextAction.ProgressSupport support = new ContextAction.ProgressSupport(this, nodes) {
                public void perform() {
                    RepositoryFile toRepositoryFile = switchTo.getRepositoryFile();
                    performSwitch(toRepositoryFile, root, this);
                }
            };
            support.start(createRequestProcessor(nodes));
        }        
    }

    static void performSwitch(RepositoryFile toRepositoryFile, File root, SvnProgressSupport support) {
        File[][] split = SvnUtils.splitFlatOthers(new File[] {root} );
        boolean recursive;
        // there can be only 1 root file
        if(split[0].length > 0) {
            recursive = false;
        } else {
            recursive = true;
        }        

        try {
            ISVNClientAdapter client;
            try {
                client = Subversion.getInstance().getClient(toRepositoryFile.getRepositoryUrl());
            } catch (SVNClientException ex) {
                ErrorManager.getDefault().notify(ex);
                return;
            }            
            // ... and switch
            client.switchToUrl(root, toRepositoryFile.getFileUrl(), toRepositoryFile.getRevision(), recursive);
            // XXX this is ugly and expensive! the client should notify (onNotify()) the cache. find out why it doesn't work...
            refreshRecursively(root); // XXX the same for another implementations like this in the code.... (see SvnUtils.refreshRecursively() )
            Subversion.getInstance().refreshAllAnnotations();
        } catch (SVNClientException ex) {
            support.annotate(ex);
        }
    }

    private static void refreshRecursively(File file) {
        Subversion.getInstance().getStatusCache().refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
        if(!file.isFile()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                refreshRecursively(files[i]);
            }
        }                
    }
}
