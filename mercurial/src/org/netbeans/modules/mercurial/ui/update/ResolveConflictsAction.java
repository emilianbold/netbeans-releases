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
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.mercurial.Mercurial;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.netbeans.modules.mercurial.FileInformation;
import javax.swing.AbstractAction;

/**
 * Show basic conflict resolver UI (provided by the diff module).
 *
 * @author Petr Kuzel
 */
public class ResolveConflictsAction extends AbstractAction {

    private final VCSContext context;
 
    public ResolveConflictsAction(String name, VCSContext context) {        
        this.context =  context;
        putValue(Action.NAME, name);
    }

    public void actionPerformed(ActionEvent e) {
        resolve(context);
    }

    public static void resolve(VCSContext ctx) {
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        File[] files = cache.listFiles(ctx, FileInformation.STATUS_VERSIONED_CONFLICT);

        resolveConflicts(files);

        return;
    }

    public boolean isEnabled() {
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();        
        
        if(cache.listFiles(context, FileInformation.STATUS_VERSIONED_CONFLICT).length != 0)
            return true;

        return false;
    }

    static void resolveConflicts(File[] files) {
        if (files.length == 0) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(
                    org.openide.util.NbBundle.getMessage(
                        ResolveConflictsAction.class, "MSG_NoConflictsFound")); // NOI18N
            DialogDisplayer.getDefault().notify(nd);
        } else {
            for (int i = 0; i<files.length; i++) {
                File file = files[i];
                ResolveConflictsExecutor executor = new ResolveConflictsExecutor(file);
                executor.exec();
            }
        }        
    }
    
}
