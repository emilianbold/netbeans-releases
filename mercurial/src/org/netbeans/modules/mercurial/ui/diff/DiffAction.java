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
package org.netbeans.modules.mercurial.ui.diff;

import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * Diff action for mercurial: 
 * hg diff - diff repository (or selected files)
 * 
 * @author John Rice
 */
public class DiffAction extends AbstractAction {
    
    private final VCSContext context;

    public DiffAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }
    
    public void actionPerformed(ActionEvent e) {
        String contextName = Utils.getContextDisplayName(context);
        diff(context, Setup.DIFFTYPE_LOCAL, contextName);
    }
    
    public boolean isEnabled() {
        return HgUtils.getModifiedFiles(context, FileInformation.STATUS_LOCAL_CHANGE).length > 0;
    } 

    public static void diff(VCSContext ctx, int type, String contextName) {

        DiffMainPanel panel = new DiffMainPanel(ctx, type, contextName); // spawns background DiffPrepareTask
        DiffTopComponent tc = new DiffTopComponent(panel);
        tc.setName(NbBundle.getMessage(DiffAction.class, "CTL_DiffPanel_Title", contextName)); // NOI18N
        tc.open();
        tc.requestActive();
    }

    public static void diff(File file, String rev1, String rev2) {
        DiffMainPanel panel = new DiffMainPanel(file, rev1, rev2); // spawns background DiffPrepareTask
        DiffTopComponent tc = new DiffTopComponent(panel);
        tc.setName(NbBundle.getMessage(DiffAction.class, "CTL_DiffPanel_Title", file.getName())); // NOI18N
        tc.open();
        tc.requestActive();
    }
}
