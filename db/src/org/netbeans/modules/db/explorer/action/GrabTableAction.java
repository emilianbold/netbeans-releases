/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.explorer.action;

import java.awt.Dialog;
import java.io.File;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Enumeration;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.lib.ddl.impl.CreateTable;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DbUtilities;
import org.netbeans.modules.db.explorer.dlg.GrabTableProgressPanel;
import org.netbeans.modules.db.explorer.node.TableNode;
import org.netbeans.modules.db.metadata.model.api.Column;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.TaskListener;
import org.openide.windows.WindowManager;

/**
 *
 * @author rob
 */
public class GrabTableAction extends BaseAction {

    public String getName() {
        return bundle().getString("GrabStructure"); // NOI18N
    }

    protected boolean enable(Node[] activatedNodes) {
        boolean enabled = false;

        if (activatedNodes.length == 1) {
            enabled = activatedNodes[0].getLookup().lookup(TableNode.class) != null;
        }

        return enabled;
    }

    public void performAction (Node[] activatedNodes)
    {
        TableNode node = activatedNodes[0].getLookup().lookup(TableNode.class);

        try {
            Specification spec = node.getLookup().lookup(DatabaseConnection.class).getConnector().getDatabaseSpecification();
            String tablename = node.getName();

            // Get command

            CreateTable cmd = (CreateTable)spec.createCommandCreateTable(tablename);

            GrabTableWorker run = new GrabTableWorker(node);
            Enumeration enu = run.execute();

            // Get filename

            JFileChooser chooser = new JFileChooser();
            FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
            chooser.setDialogType(JFileChooser.SAVE_DIALOG);
            chooser.setDialogTitle(bundle().getString("GrabTableFileSaveDialogTitle")); //NOI18N
            chooser.setSelectedFile(new File(tablename+".grab")); //NOI18N
            chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                                      public boolean accept(File f) {
                                          return (f.isDirectory() || f.getName().endsWith(".grab")); //NOI18N
                                      }
                                      public String getDescription() {
                                          return bundle().getString("GrabTableFileTypeDescription"); //NOI18N
                                      }
                                  });

            java.awt.Component par = WindowManager.getDefault().getMainWindow();
            boolean noResult = true;
            File file = null;
            while(noResult) {
                if (chooser.showSaveDialog(par) == JFileChooser.APPROVE_OPTION) {
                    file = chooser.getSelectedFile();
                    if (file != null) {
                        if(file.exists()) {
                            Object yesOption = new JButton(bundle().getString("Yes"));
                            Object noOption = new JButton (bundle().getString("No"));
                            Object result = DialogDisplayer.getDefault ().notify (new NotifyDescriptor
                                            (MessageFormat.format(bundle().getString("MSG_ReplaceFileOrNot"), // NOI18N
                                                new String[] {file.getName()}), //question
                                             bundle().getString("GrabTableFileSaveDialogTitle"), // title
                                             NotifyDescriptor.YES_NO_OPTION, // optionType
                                             NotifyDescriptor.QUESTION_MESSAGE, // messageType

                                             new Object[] { yesOption, noOption }, // options
                                             yesOption // initialValue
                                            ));
                            if (result.equals(yesOption)) {
                                // the file can be replaced
                                noResult = false;
                            }
                        } else noResult = false;
                    }
                } else return;
            }

            // TODO a new helper needs to be created
            //new GrabTableHelper().execute(spec, tablename, enu, file);

        } catch(Exception exc) {
            DbUtilities.reportError(bundle().getString("ERR_UnableToGrabTable"), exc.getMessage()); // NOI18N
        }
    }

    private static final class GrabTableWorker {

        private TableNode nfo;
        private Task task;
        private Dialog dialog;
        private ProgressHandle progressHandle;
        //private boolean finished;

        private Enumeration<Column> enumeration;
        private DatabaseException exception;

        public GrabTableWorker(TableNode nfo) {
            this.nfo = nfo;
        }

        public Enumeration execute() throws DatabaseException {
            progressHandle = ProgressHandleFactory.createHandle(null);
            GrabTableProgressPanel progressPanel = new GrabTableProgressPanel();
            progressPanel.setProgressComponent(ProgressHandleFactory.createProgressComponent(progressHandle));
            String dialogTitle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("GrabTableProgressDialogTitle"); // NOI18N
            DialogDescriptor desc = new DialogDescriptor(progressPanel, dialogTitle, true, new Object[0], DialogDescriptor.NO_OPTION, DialogDescriptor.DEFAULT_ALIGN, null, null);
            dialog = DialogDisplayer.getDefault().createDialog(desc);
            dialog.setResizable(false);
            if (dialog instanceof JDialog) {
                ((JDialog)dialog).setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            }
            progressHandle.start();

            task = RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    Table table = nfo.getTable();
                    enumeration = Collections.enumeration(table.getColumns());
                }
            });

            task.addTaskListener(new TaskListener() {
                public void taskFinished(org.openide.util.Task task) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            dialog.setVisible(false);
                        }
                    });
                }
            });

            if (!task.isFinished()) {
                dialog.setVisible(true);
            }
            dialog.dispose();
            progressHandle.finish();
            if (exception != null) {
                throw exception;
            }
            return enumeration;
        }
    }
}
