/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.versioning.system.cvss.ui.history;

import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.system.cvss.VersionsCache;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.FileStatusCache;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import javax.swing.*;
import java.io.*;
import java.awt.event.ActionEvent;
import java.awt.Dialog;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.cookies.OpenCookie;

/**
 * Allows to view a specific revision of the given file.
 * 
 * 
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
        FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();
        for (File file : ctx.getRootFiles()) {
            if (file.isDirectory()) continue;
            if ((cache.getStatus(file).getStatus() & FileInformation.STATUS_IN_REPOSITORY) != 0) return true;
        }
        return false;
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
        for (File file : ctx.getRootFiles()) {
            if (file.isDirectory()) continue;
            try {
                viewInternal(file, revision, tempFolder);
            } catch (Exception e) {
                // the file cannot be opened, ignore
            }
        }
    }

    /**
     * Open a file in the given revision in editor.
     * 
     * @param base base file
     * @param revision revision to open 
     * @param tempFolder temporary folder to use, it can be null bu this is not recommended if you will be calling this on multiple files in a row
     * @throws Exception if something goes wrong
     */
    @NbBundle.Messages("MSG_ViewRevisionAction.gettingFile=Getting File from History")
    public static void view (final File base, final String revision, final File tempFolder) {
        final ProgressHandle ph = ProgressHandleFactory.createHandle(Bundle.MSG_ViewRevisionAction_gettingFile());
        ph.start();
        CvsVersioningSystem.getInstance().getParallelRequestProcessor().post(new Runnable() {

            @Override
            public void run () {
                try {
                    viewInternal(base, revision, tempFolder);
                } catch (Exception ex) {
                    Logger.getLogger(ViewRevisionAction.class.getName()).log(Level.INFO, ex.getMessage(), ex);
                } finally {
                    ph.finish();
                }
            }
        });
    }
    
    private static void viewInternal (File base, final String revision, File tempFolder) throws Exception {
        assert !SwingUtilities.isEventDispatchThread();
        if (tempFolder == null) tempFolder = Utils.getTempFolder();
        File original = VersionsCache.getInstance().getRemoteFile(base, revision, null);
        File daoFile = new File(tempFolder, base.getName());
        daoFile.deleteOnExit();
        Utils.copyStreamsCloseAll(new FileOutputStream(daoFile), new FileInputStream(original)); 
        Utils.associateEncoding(base, daoFile);
        final FileObject fo = FileUtil.toFileObject(daoFile);
        DataObject dobj = DataObject.find(fo);
        final EditorCookie ec = dobj.getCookie(EditorCookie.class);
        final OpenCookie oc = dobj.getCookie(OpenCookie.class);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (ec != null) {
                    org.netbeans.modules.versioning.util.Utils.openFile(fo, revision);
                } else if (oc != null) {
                    oc.open();
                } else {
                    org.netbeans.modules.versioning.util.Utils.openFile(fo, revision);
                }
            }
        });
    }
}
