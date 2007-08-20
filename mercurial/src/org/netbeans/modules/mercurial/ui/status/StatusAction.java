/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
package org.netbeans.modules.mercurial.ui.status;

import java.io.File;
import java.util.Map;
import java.util.Collection;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.spi.VCSContext;
import javax.swing.*;
import java.awt.event.ActionEvent;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.util.HgUtils;

/**
 * Status action for mercurial: 
 * hg status - show changed files in the working directory
 * 
 * @author John Rice
 */
public class StatusAction extends AbstractAction {
    
    private final VCSContext context;

    public StatusAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }
    
    public void actionPerformed(ActionEvent ev) {
        Mercurial hg = Mercurial.getInstance();
        File [] files = context.getRootFiles().toArray(new File[context.getRootFiles().size()]);
        
        if (files == null || files.length == 0) return;
                
        final HgVersioningTopComponent stc = HgVersioningTopComponent.findInstance();
        stc.setContentTitle(Utils.getContextDisplayName(context)); 
        stc.setContext(context);
        stc.open(); 
        stc.requestActive();
        stc.performRefreshAction();
    }
    
    public boolean isEnabled() {
        // If it's a mercurial managed repository enable action
        File root = HgUtils.getRootFile(context);
        if (root == null)
            return false;
        else
            return true;
    } 

    /**
     * Connects to repository and gets recent status.
     */
    public static void executeStatus(final VCSContext context, HgProgressSupport support) {

        if (context == null || context.getRootFiles().size() == 0) {
            return;
        }
        File repository = HgUtils.getRootFile(context);
        if (repository == null) {
            return;
        }

        try {
            FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
            cache.refreshCached(context);
            for (File root :  context.getRootFiles()) {
                if(support.isCanceled()) {
                    return;
                }
                if (root.isDirectory()) {
                    Map<File, FileInformation> interestingFiles;
                    interestingFiles = HgCommand.getInterestingStatus(repository, root);
                    if (!interestingFiles.isEmpty()){
                        Collection<File> files = interestingFiles.keySet();
                        for (File file : files) {
                             if(support.isCanceled()) {
                                 return;
                             }
                             cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN); 
                        }
                    } 
                } else {
                    cache.refresh(root, FileStatusCache.REPOSITORY_STATUS_UNKNOWN); 
                }
            }
        } catch (HgException ex) {
            support.annotate(ex);
        }
    }
}
