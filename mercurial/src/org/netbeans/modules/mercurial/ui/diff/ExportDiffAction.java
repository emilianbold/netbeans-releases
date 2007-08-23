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
 * "Portions Copyrighted [year] [name of copyright owner]" // NOI18N
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.mercurial.ui.diff;

import org.netbeans.modules.versioning.spi.VCSContext;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.util.HgRepositoryContextCache;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * ExportDiff action for mercurial: 
 * hg export
 * 
 * @author Padraig O'Briain
 */
public class ExportDiffAction extends AbstractAction {
    
    private final VCSContext context;

    public ExportDiffAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }
    
    public void actionPerformed(ActionEvent e) {
        exportDiff(context);
    }
    
    public boolean isEnabled() {
        return HgRepositoryContextCache.hasHistory(context);
    } 

    private static void exportDiff(VCSContext ctx) {
        final File root = HgUtils.getRootFile(ctx);
        ExportDiff ed = new ExportDiff(root);
        if (!ed.showDialog()) {
            return;
        }
        final String revStr = ed.getSelectionRevision();
        final String outputFileName = ed.getOutputFileName();
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root.getAbsolutePath());
        HgProgressSupport support = new HgProgressSupport() {
            public void perform() {
                performExport(root, revStr, outputFileName);
            }
        };
        support.start(rp, root.getAbsolutePath(), org.openide.util.NbBundle.getMessage(ExportDiffAction.class, "LBL_ExportDiff_Progress")); // NOI18N
    }

    private static void performExport(File repository, String revStr, String outputFileName) {
    try {
        HgCommand.doExport(repository, revStr, outputFileName);
        } catch (HgException ex) {
            NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
            DialogDisplayer.getDefault().notifyLater(e);
        }
    }
}
