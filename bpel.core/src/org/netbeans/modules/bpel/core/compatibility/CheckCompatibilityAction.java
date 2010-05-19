/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bpel.core.compatibility;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.Callable;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.xml.transform.TransformerException;
import org.netbeans.modules.bpel.core.BPELDataObject;
import org.netbeans.modules.bpel.core.compatibility.CompatUtils.CompatibilityResult;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.spi.BpelModelFactory;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.soa.ui.ProgressDialog;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.soa.ui.UserNotification;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Nikita Krjukov
 */
public class CheckCompatibilityAction extends NodeAction {
    public static final String
        XSL_FILE_NAME_BPEL_CONVERSION = "bpelConversion.xsl", // NOI18N
        XSL_FILE_NAME_DELETE_OLD_EXT  = "deleteOldExt.xsl"; // NOI18N

    private static final long serialVersionUID = 1;

    public CheckCompatibilityAction() {
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return getSingleBpelModel(activatedNodes) != null;
    }

    /**
     * @return Bpel Model for this editor.
     */
    private BpelModel getSingleBpelModel(Node[] activatedNodes) {
        if (activatedNodes.length == 1) {
            Node node = activatedNodes[0];
            if (node != null) {
                BPELDataObject dataObj = node.getLookup().lookup(BPELDataObject.class);
                if (dataObj != null) {
                    ModelSource modelSource = Utilities.getModelSource(dataObj
                            .getPrimaryFile(), true);
                    if (modelSource != null) {
                        BpelModelFactory factory = Lookup.getDefault().
                                lookup(BpelModelFactory.class);
                        if (factory != null) {
                            BpelModel bpelModel = factory.getModel(modelSource);
                            return bpelModel;
                        }
                    }
                }
            }
        }
        return null;
    }

    /** Human presentable name. */
    public String getName() {
        return NbBundle.getMessage(
                CheckCompatibilityAction.class, "NAME_check_compatibility_action");
    }

    /** Provide accurate help. */
    public HelpCtx getHelpCtx () {
        return new HelpCtx(CheckCompatibilityAction.class);
    }

    /** Check all selected nodes. */
    protected void performAction (Node[] nodes) {
        final BpelModel bModel = getSingleBpelModel(nodes);
        assert bModel != null;
        Process process = bModel.getProcess();
        if (process == null) {
            String msg = NbBundle.getMessage(CheckCompatibilityAction.class,
                    "MSG_CorruptedFile"); // NOI18N
            UserNotification.showMessage(msg);
            return;
        }
        //
        CompatibilityResult cResult = 
                CompatUtils.checkCompatibility(bModel.getProcess().getPeer());
        //
        if (!cResult.containsBpelEditorExt()) {
            String okMsg = NbBundle.getMessage(CheckCompatibilityAction.class,
                    "LBL_BpelExtIsOk"); // NOI18N
            UserNotification.showMessage(okMsg);
        } else {
            CheckCompatibilityDlgPanel checkPanel = new CheckCompatibilityDlgPanel(cResult);
            DialogDescriptor dd = new DialogDescriptor(checkPanel,
                    NbBundle.getMessage(CheckCompatibilityAction.class,
                    "DLG_Resolve_Compatibility_Title")); // NOI18N
            //
            String btnText = NbBundle.getMessage(CheckCompatibilityAction.class,
                    "BTN_Convert"); // NOI18N
            JButton btnConvert = new JButton(btnText);
            //
            btnText = NbBundle.getMessage(CheckCompatibilityAction.class,
                    "BTN_Delete"); // NOI18N
            JButton btnDelete = new JButton(btnText);
            //
            dd.setOptions(new Object[] {btnConvert, btnDelete, DialogDescriptor.CANCEL_OPTION});
            dd.setClosingOptions(new Object[] {DialogDescriptor.CANCEL_OPTION});
            //
            final Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
            //
            btnConvert.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // dlg.setEnabled(false);
                    ProgressDialog progressDlg = new ProgressDialog(dlg);
                    String msg = NbBundle.getMessage(
                            CheckCompatibilityAction.class,
                            "MSG_Converting"); // NOI18N
                    progressDlg.start(msg, new Callable<String>() {
                        public String call() throws Exception {
                            try {
                                FileObject bpelFo = SoaUtil.getFileObjectByModel(bModel);
                                //
                                TransformUtil.applyXslTransform(
                                        bpelFo, XSL_FILE_NAME_BPEL_CONVERSION, "old"); // NOI18N
                                //
                                String msg = NbBundle.getMessage(
                                        CheckCompatibilityAction.class,
                                        "MSG_ConversionSuccessful"); // NOI18N
                                return msg;
                            } catch (FileNotFoundException ex) {
                                Exceptions.printStackTrace(ex);
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            } catch (TransformerException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            return "An error happened."; // NOI18N
                        }
                    } , new Runnable() {
                        public void run() {
                            dlg.setVisible(false);
                        }
                    });
                }
            });
            //
            btnDelete.addActionListener(new ActionListener() {
                private Task deleteTask;

                public void actionPerformed(ActionEvent e) {
                    if (deleteTask != null) {
                        return;
                    }
                    deleteTask = RequestProcessor.getDefault().create(new Runnable() {
                        public void run() {
                            FileObject bpelFo = SoaUtil.getFileObjectByModel(bModel);
                            try {
                                TransformUtil.applyXslTransform(
                                        bpelFo, XSL_FILE_NAME_DELETE_OLD_EXT, "old"); // NOI18N
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        String msg = NbBundle.getMessage(
                                                CheckCompatibilityAction.class,
                                                "MSG_CleaningSuccessful"); // NOI18N
                                        UserNotification.showMessage(msg);
                                        dlg.setVisible(false);
                                    }
                                });
                            } catch (FileNotFoundException ex) {
                                Exceptions.printStackTrace(ex);
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            } catch (TransformerException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    });
                    deleteTask.setPriority(Thread.MIN_PRIORITY);
                    deleteTask.schedule(0);
                }
            });
            //
            dlg.setVisible(true);

        }

    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

}
