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

package org.netbeans.modules.encoder.ui.basic;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import javax.swing.text.Document;
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
 * The action that removes all encoding related AppInfo from XSD(s).
 *
 * @author Jun Xu
 */
public class RemoveEncodingAction extends NodeAction {
    
    private static final ResourceBundle _bundle =
            ResourceBundle.getBundle("org/netbeans/modules/encoder/ui/basic/Bundle"); //NOI18N
    private static final String PROCESS = _bundle.getString("remove_encoding.lbl.process"); //NOI18N
    private static final String CANCEL = _bundle.getString("remove_encoding.lbl.cancel"); //NOI18N
    
    public static void removeEncoding(final SchemaModel model,
            final boolean applyToReferenced) throws IOException, CatalogModelException {
        
        InputOutput io = Utils.getEncodingIO();
        io.select();
        final OutputWriter console = io.getOut();
        console.println(_bundle.getString("remove_encoding.msg.start_remove_encoding")); //NOI18N
        
        ModelUtils.ModelStatus modelStatus =
                ModelUtils.getModelStatus(model, applyToReferenced);

        if (modelStatus.getTotalCharSize() > 300 * 1024) {
            int estimate = (int) ((modelStatus.getTotalCharSize() / 18000.0 / 60.0) + 0.5);
            String msg = NbBundle.getMessage(RemoveEncodingAction.class,
                    "remove_encoding.lbl.warn_big_size", estimate); //NOI18N
            int option =
                    JOptionPane.showConfirmDialog(
                            WindowManager.getDefault().getMainWindow(),
                            msg,
                            _bundle.getString("remove_encoding.lbl.notify_lengthy_proc"), //NOI18N
                            JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.NO_OPTION) {
                console.println(_bundle.getString("remove_encoding.msg.cancelled_by_user")); //NOI18N
                return;
            }
            
            final ProgressDialog progDialog =
                    ProgressDialog.newInstance(_bundle.getString("Removing_Encoding")); //NOI18N
            new Thread(new Runnable() {
                public void run() {

                    progDialog.getHandle().start();
                    progDialog.getHandle().switchToIndeterminate();
                    final ModelVisitor visitor = new RemoveEncodingSync();
                    try {
                        ModelUtils.visitModel(model, visitor, applyToReferenced);        
                    } catch (CatalogModelException ex) {
                        throw new RuntimeException(ex);
                    } finally {
                        progDialog.getHandle().finish();
                        progDialog.getDialog().setVisible(false);
                    }
                    console.println(_bundle.getString("Finished_removing_encoding")); //NOI18N
                }
            }).start();
            progDialog.getDialog().setVisible(true);
        } else {
            ModelVisitor visitor = new RemoveEncodingSync();
            ModelUtils.visitModel(model, visitor, applyToReferenced);        
            console.println(_bundle.getString("Finished_removing_encoding")); //NOI18N
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
        final RemoveEncodingConfirmation confirmPane = new RemoveEncodingConfirmation();
        confirmPane.setApplyToReferenced(true);
        DialogDescriptor dialogDescriptor =
                new DialogDescriptor(confirmPane, 
                    _bundle.getString("remove_encoding.lbl.title"), //NOI18N
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
                removeEncoding(model, applyToReferenced);
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
        return _bundle.getString("remove_encoding.lbl.title1"); //NOI18N
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
        return ModelUtils.hasEncodingMark(sm);
    }
    
    /**
     * Visitor class for removing encoding to the in-memory model and
     * with model always synchronized. For huge models, this process might be
     * very time consuming.
     */
    private static class RemoveEncodingSync implements ModelVisitor {
        
        private OutputWriter mConsole = Utils.getEncodingIO().getOut();
        private boolean mSkip = false;
        
        public boolean visit(SchemaModel model) {
            if (!ModelUtils.isModelWritable(model)) {
                if (mSkip) {
                    return true;
                }
                String msg = NbBundle.getMessage(RemoveEncodingAction.class,
                                "remove_encoding.lbl.warn_readonly_model", //NOI18N
                                ModelUtils.getFilePath(model));
                int option =
                    JOptionPane.showConfirmDialog(
                            WindowManager.getDefault().getMainWindow(),
                            msg,
                            _bundle.getString("remove_encoding.lbl.ro-model"), //NOI18N
                            JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.NO_OPTION) {
                    mConsole.println(
                            _bundle.getString("remove_encoding.msg.cancelled_by_user")); //NOI18N
                    return false;
                }
                mSkip = true;
                return true;
            }
            mConsole.println(
                    NbBundle.getMessage(
                            RemoveEncodingAction.class,
                            "remove_encoding.msg.rmv_encoding_from", //NOI18N
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
                ModelUtils.removeEncodingMark(model);
                RemoveEncodingVisitor visitor =
                        new RemoveEncodingVisitor(model);
                model.getSchema().accept(visitor);
                mConsole.println(
                        NbBundle.getMessage(
                                RemoveEncodingAction.class,
                                "remove_encoding.msg.num_of_element_modified", //NOI18N
                                visitor.getElementModified()));
                mConsole.println(
                        NbBundle.getMessage(
                                RemoveEncodingAction.class,
                                "remove_encoding.msg.num_of_cplxtyp_modified", //NOI18N
                                visitor.getComplexTypeModified()));
                mConsole.println(
                        NbBundle.getMessage(
                                RemoveEncodingAction.class,
                                "remove_encoding.msg.num_of_simptyp_modified", //NOI18N
                                visitor.getSimpleTypeModified()));
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
