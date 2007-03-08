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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.versioning.system.cvss.ui.history;

import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.system.cvss.VersionsCache;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import javax.swing.*;
import java.io.*;
import java.awt.event.ActionEvent;
import java.awt.Dialog;

/**
 * Allows to view a specific revision of the given file.
 * 
 * @author Maros Sandor
 */
public class ViewRevisionAction extends AbstractAction implements Runnable {
    
    private final VCSContext ctx;
    private ViewRevisionPanel settings; 

    public ViewRevisionAction(VCSContext ctx) {
        this(Utils.getActionName(ViewRevisionAction.class, "CTL_MenuItem_ViewRevision", ctx), ctx);
    }

    public ViewRevisionAction(String name, VCSContext ctx) {
        super(name);
        this.ctx = ctx;
    }

    public boolean isEnabled() {
        return ctx.getRootFiles().size() > 0;
    }

    public void actionPerformed(ActionEvent e) {

        String title = NbBundle.getMessage(ViewRevisionAction.class, "CTL_ViewRevisionDialog_Title", Utils.getContextDisplayName(ctx)); // NOI18N
        
        settings = new ViewRevisionPanel(ctx); 
        
        JButton view = new JButton(NbBundle.getMessage(ViewRevisionAction.class, "CTL_ViewRevisionDialog_Action_View")); // NOI18N
        settings.putClientProperty("OKButton", view); // NOI18N
        settings.refreshComponents();
        view.setToolTipText(NbBundle.getMessage(ViewRevisionAction.class,  "TT_ViewRevisionDialog_Action_View")); // NOI18N
        DialogDescriptor descriptor = new DialogDescriptor(
                settings,
                title,
                true,
                new Object [] { view, DialogDescriptor.CANCEL_OPTION },
                view,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(ViewRevisionAction.class),
                null);
        descriptor.setClosingOptions(null);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ViewRevisionAction.class, "ACSD_ViewRevisionDialog")); // NOI18N
        dialog.setVisible(true);
        if (descriptor.getValue() != view) return;

        settings.saveSettings();
        Utils.createTask(this).schedule(0);
    }

    public void run() {
        final String revision = settings.getRevision();
        File tempFolder = Utils.getTempFolder();
        tempFolder.deleteOnExit();
        for (File file : ctx.getRootFiles()) {
            if (file.isDirectory()) continue;
            try {
                File original = VersionsCache.getInstance().getRemoteFile(file, revision, null);
                File daoFile = new File(tempFolder, file.getName());
                Utils.copyStreamsCloseAll(new FileOutputStream(daoFile), new FileInputStream(original));
                daoFile.deleteOnExit();
                final FileObject fo = FileUtil.toFileObject(daoFile);
                try {
                    DataObject dao = DataObject.find(fo);
                    final EditorCookie ec = dao.getCookie(EditorCookie.class);
                    if (ec != null) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                Utils.openFile(fo, revision);
                            }
                        });
                    }
                } catch (DataObjectNotFoundException e) {
                    // no data obejct for the file, ignore it
                }
            } catch (Exception e) {
                // the file cannot be opened, ignore
            }
        }
    }

}
