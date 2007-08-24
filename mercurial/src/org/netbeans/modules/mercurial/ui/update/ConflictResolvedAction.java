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

package org.netbeans.modules.mercurial.ui.update;

import java.io.File;
import java.util.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.FileInformation;
import javax.swing.AbstractAction;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.util.RequestProcessor;
import  org.openide.util.NbBundle;

/**
 * Allow files who have Conlfict Status to be manually resolved.
 *
 * @author Petr Kuzel
 */
public class ConflictResolvedAction extends AbstractAction {

    private final VCSContext context;
 
    public ConflictResolvedAction(String name, VCSContext context) {        
        this.context =  context;
        putValue(Action.NAME, name);
    }

    public void actionPerformed(ActionEvent e) {
        resolved(context);
    }

    public static void resolved(VCSContext ctx) {
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        File[] files = cache.listFiles(ctx, FileInformation.STATUS_VERSIONED_CONFLICT);
        final File root = HgUtils.getRootFile(ctx);
        if (root == null || files == null || files.length == 0) return;

        conflictResolved(root, files);

        return;
    }

    public boolean isEnabled() {
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();        
        
        if(cache.listFiles(context, FileInformation.STATUS_VERSIONED_CONFLICT).length != 0)
            return true;

        return false;
    }

    public static void conflictResolved(File repository, final File[] files) {
        if (repository == null || files == null || files.length == 0) return;
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
        HgProgressSupport support = new HgProgressSupport() {

            public void perform() {
                for (int i = 0; i < files.length; i++) {
                    if (isCanceled()) {
                        return;
                    }
                    File file = files[i];
                    ConflictResolvedAction.perform(file);
                }
            }
        };
        support.start(rp, repository.getAbsolutePath(), NbBundle.getMessage(ConflictResolvedAction.class, "MSG_ConlictResolved_Progress")); // NOI18N
    }
    
    private static void perform(File file) {
        if (file == null) return;
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        
        HgCommand.deleteConflictFile(file.getAbsolutePath());
        Mercurial.LOG.log(Level.FINE, "ConflictResolvedAction.perform(): DELETE CONFLICT File: {0}", // NOI18N
                new Object[] {file.getAbsolutePath() + HgCommand.HG_STR_CONFLICT_EXT} );
        cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
    }
    
    public static void resolved(File file) {
        perform(file);
    }

}
