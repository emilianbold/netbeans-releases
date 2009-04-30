/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.mercurial.ui.diff;

import org.netbeans.modules.versioning.spi.VCSContext;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.DialogDisplayer;
import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import java.awt.event.ActionListener;
import java.awt.Dialog;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.versioning.util.AccessibleJFileChooser;
import org.openide.filesystems.FileUtil;

/**
 * ImportDiff action for mercurial: 
 * hg export
 * 
 * @author Padraig O'Briain
 */
public class ImportDiffAction extends ContextAction {
    
    private final VCSContext context;

    public ImportDiffAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }
    
    public void performAction(ActionEvent e) {
        importDiff(context);
    }
    
    public boolean isEnabled() {
        return HgUtils.getRootFile(context) != null;
    } 

    private static void importDiff(VCSContext ctx) {
        final File root = HgUtils.getRootFile(ctx);
        final JFileChooser fileChooser = new AccessibleJFileChooser(NbBundle.getMessage(ImportDiffAction.class, "ACSD_ImportBrowseFolder"), null);   // NO I18N
        fileChooser.setDialogTitle(NbBundle.getMessage(ImportDiffAction.class, "ImportBrowse_title"));                                            // NO I18N
        fileChooser.setMultiSelectionEnabled(false);
        FileFilter[] old = fileChooser.getChoosableFileFilters();
        for (int i = 0; i < old.length; i++) {
            FileFilter fileFilter = old[i];
            fileChooser.removeChoosableFileFilter(fileFilter);

        }
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooser.setApproveButtonMnemonic(NbBundle.getMessage(ImportDiffAction.class, "Import").charAt(0));                      // NO I18N
        fileChooser.setApproveButtonText(NbBundle.getMessage(ImportDiffAction.class, "Import"));                                        // NO I18N
        fileChooser.setCurrentDirectory(new File(HgModuleConfig.getDefault().getImportFolder()));

        DialogDescriptor dd = new DialogDescriptor(fileChooser, NbBundle.getMessage(ImportDiffAction.class, "Browse_title"));              // NO I18N
        dd.setOptions(new Object[0]);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        fileChooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String state = e.getActionCommand();
                if (state.equals(JFileChooser.APPROVE_SELECTION)) {
                    final File patchFile = fileChooser.getSelectedFile();

                    HgModuleConfig.getDefault().setImportFolder(patchFile.getParent());
                    if (patchFile != null) {
                        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
                        HgProgressSupport support = new HgProgressSupport() {
                            public void perform() {
                                OutputLogger logger = getLogger();
                                performImport(root, patchFile, logger);
                            }
                        };
                        support.start(rp, root, org.openide.util.NbBundle.getMessage(ImportDiffAction.class, "LBL_ImportDiff_Progress")); // NOI18N
                    }
                }
                dialog.dispose();
            }
        });
        dialog.setVisible(true);
    }

    private static void performImport(final File repository, File patchFile, OutputLogger logger) {
        try {
            logger.outputInRed(
                    NbBundle.getMessage(ImportDiffAction.class,
                    "MSG_IMPORT_TITLE")); // NOI18N
            logger.outputInRed(
                    NbBundle.getMessage(ImportDiffAction.class,
                    "MSG_IMPORT_TITLE_SEP")); // NOI18N

            List<String> list = HgCommand.doImport(repository, patchFile, logger);
            Mercurial.getInstance().changesetChanged(repository);
            logger.output(list); // NOI18N

        } catch (HgException ex) {
            NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
            DialogDisplayer.getDefault().notifyLater(e);
        } finally {
            logger.outputInRed(NbBundle.getMessage(ImportDiffAction.class, "MSG_IMPORT_DONE")); // NOI18N
            logger.output(""); // NOI18N
            Mercurial.getInstance().getRequestProcessor().post(new Runnable() {
                public void run() {
                    FileUtil.refreshFor(repository); // import just might have changed the file layout...
                }
            });
        }
    }
}
