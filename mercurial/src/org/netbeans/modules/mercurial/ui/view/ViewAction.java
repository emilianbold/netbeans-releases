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
package org.netbeans.modules.mercurial.ui.view;

import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import java.io.File;
import java.util.List;
import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * View action for mercurial: 
 * hg view - launch hg view to view the dependency tree for the repository
 * 
 * @author John Rice
 */
public class ViewAction extends AbstractAction {
    
    private final VCSContext context;

    public ViewAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }
    
    public void actionPerformed(ActionEvent e) {
        final File root = HgUtils.getRootFile(context);
        if (root == null) return;
        String repository = root.getAbsolutePath();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                performView(root);
            }
        });
    }

    static void performView(File root) {
        try {
            List<String> output = HgCommand.doView(root);
        } catch (HgException ex) {
            NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
            DialogDisplayer.getDefault().notifyLater(e);
        }
    }

    public boolean isEnabled() {
        File root = HgUtils.getRootFile(context);
        if (root == null)
            return false;
        else
            return true;
    } 
}
