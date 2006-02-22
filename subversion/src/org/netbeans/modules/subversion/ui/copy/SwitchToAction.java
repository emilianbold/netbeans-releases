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

package org.netbeans.modules.subversion.ui.copy;

import java.io.File;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
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
        return ~0; // XXX
    }

    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED; // XXX
    }
    
    protected void performContextAction(Node[] nodes) {
        Context ctx = getContext(nodes);        
        
        File root = ctx.getRootFiles()[0];        
                
        SVNUrl url = SvnUtils.getRepositoryUrl(root);
        RepositoryFile repositoryRoot = new RepositoryFile(url, url, SVNRevision.HEAD);
        String sourcePath = SvnUtils.getRelativePath(root);        
     
        SwitchTo switchTo = new SwitchTo(repositoryRoot, nodes[0].getName()); // XXX name or dispayname or what?                
        if(switchTo.switchTo()) {
            RepositoryFile repository = switchTo.getRepositoryFile();            
            boolean replaceModifications = switchTo.replaceModifications();            

            ISVNClientAdapter client;
            try {
                
                client = Subversion.getInstance().getClient(repositoryRoot.getRepositoryUrl());
                
                client.switchToUrl(root, repository.getRepositoryUrl(), repository.getRevision(), true);
                
                if(replaceModifications) {
                    client.revert(root, true);
                }
                
            } catch (SVNClientException ex) {
                ex.printStackTrace(); // should not hapen
                return;
            }            
        }        
    }
    
}
