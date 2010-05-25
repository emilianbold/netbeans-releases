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

package org.netbeans.modules.encoder.coco.ui.action;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import javax.swing.text.Document;
import org.netbeans.modules.encoder.coco.ui.CocoEncodingConst;
import org.netbeans.modules.encoder.ui.basic.ApplyEncodingConfirmation;
import org.netbeans.modules.encoder.ui.basic.ApplyEncodingException;
import org.netbeans.modules.encoder.ui.basic.EncodingMark;
import org.netbeans.modules.encoder.ui.basic.ModelUtils;
import org.netbeans.modules.encoder.ui.basic.ModelVisitor;
import org.netbeans.modules.encoder.ui.basic.ModelVisitorException;
import org.netbeans.modules.encoder.ui.basic.ProgressDialog;
import org.netbeans.modules.encoder.ui.basic.Utils;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.NodeAction;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.openide.windows.WindowManager;

/**
 * Action that applies the COBOL Copybook encoding to an XSD.
 *
 * @author Jun Xu
 */
public class ApplyCocoEncodingAction extends NodeAction {

    private static final ResourceBundle _bundle =
            ResourceBundle.getBundle("org/netbeans/modules/encoder/coco/ui/action/Bundle");
    private static final String PROCESS = _bundle.getString("apply_coco_enc.lbl.process");
    private static final String CANCEL = _bundle.getString("apply_coco_enc.lbl.cancel");
    
    private boolean mIsApplied = false;
        
    public static void applyCocoEncoding(final SchemaModel model,
            final boolean applyToReferenced) throws IOException, CatalogModelException {
        
        InputOutput io = Utils.getEncodingIO();
        io.select();
        final OutputWriter console = io.getOut();
        console.println(_bundle.getString("apply_coco_enc.msg.start_apply_coco_encoding"));
        
        ModelUtils.ModelStatus modelStatus =
                ModelUtils.getModelStatus(model, applyToReferenced);

        if (modelStatus.getTotalCharSize() > 300 * 1024) {
            int estimate = (int) ((modelStatus.getTotalCharSize() / 18000.0 / 60.0) + 0.5);
            String msg = NbBundle.getMessage(ApplyCocoEncodingAction.class,
                    "apply_coco_enc.lbl.warn_big_file", estimate);  //NOI18N
            int option =
                    JOptionPane.showConfirmDialog(
                            WindowManager.getDefault().getMainWindow(),
                            msg,
                            _bundle.getString("apply_coco_enc.lbl.notify_lengthy_process"),
                            JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.NO_OPTION) {
                console.println(_bundle.getString("apply_coco_enc.msg.cancelled_by_user"));
                return;
            }
            
            final ProgressDialog progDialog =
                    ProgressDialog.newInstance(_bundle.getString("apply_coco_enc.lbl.applying_coco_encoding"));
            new Thread(new Runnable() {
                public void run() {

                    progDialog.getHandle().start();
                    progDialog.getHandle().switchToIndeterminate();
                    final ModelVisitor visitor = new ApplyCocoEncodingSync();
                    try {
                        ModelUtils.visitModel(model, visitor, applyToReferenced);        
                    } catch (CatalogModelException ex) {
                        throw new RuntimeException(ex);
                    } finally {
                        progDialog.getHandle().finish();
                        progDialog.getDialog().setVisible(false);
                    }
                    console.println(_bundle.getString("apply_coco_enc.msg.finished_apply_coco_encoding"));
                }
            }).start();
            progDialog.getDialog().setVisible(true);
        } else {
            ModelVisitor visitor = new ApplyCocoEncodingSync();
            ModelUtils.visitModel(model, visitor, applyToReferenced);        
            console.println(_bundle.getString("apply_coco_enc.msg.finished_apply_coco_encoding"));
        }
    }
    
    protected void performAction(Node[] node) {
        try {
            Utils.getEncodingIO().getOut().reset();
        } catch (IOException ex) {
            //Ignore
        }
        showDialog(node);
    }

    private void showDialog(final Node[] node) {
        final ApplyEncodingConfirmation confirmPane = new ApplyEncodingConfirmation();
        confirmPane.setApplyToReferenced(true);
        DialogDescriptor dialogDescriptor =
                new DialogDescriptor(confirmPane, 
                    _bundle.getString("apply_coco_enc.lbl.apply_coco_encoding_title"),
                    true,
                    new Object[] {PROCESS, CANCEL},
                    PROCESS,
                    DialogDescriptor.BOTTOM_ALIGN,
                    HelpCtx.DEFAULT_HELP, 
                    this);
        dialogDescriptor.setClosingOptions(new Object[] {PROCESS, CANCEL});
        dialogDescriptor.setButtonListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String action = e.getActionCommand();
                if (action.equals(PROCESS)) {
                    try {
                        confirmPane.setCursor(Utilities.createProgressCursor(confirmPane));
                        process(node, confirmPane.isApplyToReferenced());
                    } finally {
                        confirmPane.setCursor(null);
                    }
                }
            }
        });
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.setModal(true);
        dialog.setVisible(true);
    }

    private void process(final Node[] node, boolean applyToReferenced) {
        SchemaModel model = null;
        for (int nodeIndex = 0; nodeIndex < node.length; nodeIndex++) {
            try {
                model = ModelUtils.getSchemaModelFromNode(node[nodeIndex]);
                if (model == null) {
                    continue;
                }
                applyCocoEncoding(model, applyToReferenced);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            } catch (ApplyEncodingException ex) {
                ErrorManager.getDefault().notify(ex);
            } catch (CatalogModelException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }
    
    public String getName() {
        if (mIsApplied) {
            return _bundle.getString("apply_coco_enc.lbl.reapply_coco_encoding");
        }
        return _bundle.getString("apply_coco_enc.lbl.apply_coco_encoding_menu");
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean asynchronous() {
        return false;
    }
    
    protected boolean enable(Node[] node) {
        //Check if it is writable
        if (node == null || node.length == 0) {
            return false;
        }
        SchemaModel sm = null;
        try {
	    sm = ModelUtils.getSchemaModelFromNode(node[0]);
        } catch (IOException ex) {
            ErrorManager.getDefault().log(ErrorManager.EXCEPTION,
                    ex.toString());
            return false;
        }
        boolean writable = ModelUtils.isModelWritable(sm);
        if (!writable) {
            return false;
        }
        EncodingMark existingMark = ModelUtils.getEncodingMark(sm);
        if (existingMark != null) {
            if (CocoEncodingConst.STYLE.equals(existingMark.getStyle())) {
                //Already has COBOL Copybook encoding applied
                mIsApplied = true;
                return true;
            }
            //Already has other encoding applied
            mIsApplied = false;
            return false;
        }
        //No encoding has been applied
        mIsApplied = false;
        return true;
    }
    
    /**
     * Visitor class for applying COBOL Copybook encoding to the in-memory model
     * and with model always synchronized. For huge models, this process might
     * be very time consuming.
     */
    private static class ApplyCocoEncodingSync implements ModelVisitor {
        
        private OutputWriter mConsole = Utils.getEncodingIO().getOut();
        private boolean mSkip = false;
        
        public boolean visit(SchemaModel model) {
            if (!ModelUtils.isModelWritable(model)) {
                if (mSkip) {
                    return true;
                }
                String msg = NbBundle.getMessage(ApplyCocoEncodingAction.class,
                        "apply_coco_enc.lbl.read_only_model", ModelUtils.getFilePath(model)); //NOI18N
                int option =
                        JOptionPane.showConfirmDialog(
                                WindowManager.getDefault().getMainWindow(),
                                msg,
                                _bundle.getString("apply_coco_enc.lbl.read_only_model"),
                                JOptionPane.YES_NO_OPTION);
                mConsole.println(
                        NbBundle.getMessage(
                                ApplyCocoEncodingAction.class,
                                "apply_coco_enc.msg.apply_coco_enc_to", //NOI18N
                                ModelUtils.getFilePath(model)));
                if (option == JOptionPane.NO_OPTION) {
                    mConsole.println(_bundle.getString("apply_coco_enc.msg.cancelled_by_user"));
                    return false;
                }
                mSkip = true;
                return true;
            }
            
            mConsole.println(
                    NbBundle.getMessage(
                            ApplyCocoEncodingAction.class,
                            "apply_coco_enc.msg.apply_coco_enc_to", //NOI18N
                            ModelUtils.getFilePath(model)));
            boolean transStarted = false;
            try {
                if (!model.isIntransaction()) {
                    if (!model.startTransaction()) {
                        //TODO how to handle???
                    } else {
                        transStarted = true;
                    }
                }
                try {
                    ModelUtils.applyEncodingMark(model, CocoEncodingConst.NAME,
                            CocoEncodingConst.URI, CocoEncodingConst.STYLE);
                } catch (IOException ex) {
                    throw new ModelVisitorException(ex);
                }
                ApplyCocoEncodingVisitor visitor =
                        new ApplyCocoEncodingVisitor(model);
                model.getSchema().accept(visitor);
                mConsole.println(
                        NbBundle.getMessage(
                                ApplyCocoEncodingAction.class,
                                "apply_coco_enc.msg.modified_elem_count", //NOI18N
                                visitor.getElementModified()));
            } finally {
                if (transStarted) {
                    model.endTransaction();
                }
            }
            return true;
        }

        public boolean visit(Document doc) {
            return true;
        }

        public boolean visit(FileObject fileObj) {
            return true;
        }

        public boolean visit(DataObject dataObj) {
            return true;
        }
    }
}
