/*
 * The contents of this file are subject to the terms of the Common
 * Development and Distribution License (the License). You may not use this 
 * file except in compliance with the License.  You can obtain a copy of the
 *  License at http://www.netbeans.org/cddl.html
 *
 * When distributing Covered Code, include this CDDL Header Notice in each
 * file and include the License. If applicable, add the following below the
 * CDDL Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All Rights Reserved
 *
 */
package org.netbeans.modules.edm.editor.graph.actions;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import javax.swing.KeyStroke;
import org.netbeans.modules.edm.editor.dataobject.MashupDataObject;
import org.netbeans.modules.edm.editor.utils.ImageConstants;
import org.netbeans.modules.edm.editor.utils.MashupGraphUtil;
import org.netbeans.modules.edm.model.SQLCondition;
import org.netbeans.modules.edm.model.SQLObject;
import org.netbeans.modules.edm.model.SourceTable;
import org.netbeans.modules.edm.editor.ui.model.CollabSQLUIModel;
import org.netbeans.modules.edm.editor.ui.view.conditionbuilder.ConditionBuilderUtil;
import org.netbeans.modules.edm.editor.ui.view.conditionbuilder.ConditionBuilderView;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author karthikeyan s
 */
public class ExtractionConditionAction extends AbstractAction {

    private MashupDataObject mObj;
    private SQLObject obj;

    /** Creates a new instance of EditJoinAction */
    public ExtractionConditionAction(MashupDataObject dObj, SQLObject obj) {
        super("", new ImageIcon(
                MashupGraphUtil.getImage(ImageConstants.FILTER)));
        mObj = dObj;
        this.obj = obj;
    }

    public ExtractionConditionAction(MashupDataObject dObj, SQLObject obj, String name) {
        super(name, new ImageIcon(
                MashupGraphUtil.getImage(ImageConstants.FILTER)));
        this.putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(ExtractionConditionAction.class, "TOOLTIP_Table_Filter_Condition"));
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('F', InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_MASK));
        mObj = dObj;
        this.obj = obj;
    }

    public void actionPerformed(ActionEvent e) {
        Object[] args = new Object[2];
        args[0] = null;
        args[1] = obj;
        //mObj.getEditorView().execute(ICommand.DATA_EXTRACTION, args);
        showDataExtraction((SourceTable) obj);
        //mObj.getMashupDataEditorSupport().synchDocument();
        mObj.getModel().setDirty(true);
        mObj.setModified(true);   
    }

    private void showDataExtraction(SourceTable table) {

    ConditionBuilderView cView = ConditionBuilderUtil.getConditionBuilderView(table, mObj.getEditorView().getCollabSQLUIModel());        
        String title = NbBundle.getMessage(ExtractionConditionAction.class, "TITLE_Extraction_Condition");

        // Create a Dialog that defers decision-making on whether to close the dialog to
        // an ActionListener.
        DialogDescriptor dd = new DialogDescriptor(cView, title, true, NotifyDescriptor.OK_CANCEL_OPTION, null, null);

        // Pushes closing logic to ActionListener impl
        dd.setClosingOptions(new Object[0]);

        Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
        dlg.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ExtractionConditionAction.class, "ACSD_conditionWizard"));
        ActionListener dlgListener = new ConditionValidator.ExtractionFilter(table, cView, dlg, mObj);
        dd.setButtonListener(dlgListener);

        dlg.setModal(true);
        dlg.setVisible(true);
    }


    protected static abstract class ConditionValidator implements ActionListener {

        static final class ExtractionFilter extends ConditionValidator {

            private SourceTable mTable;

            public ExtractionFilter(SourceTable table, ConditionBuilderView view, Dialog dlg, MashupDataObject dObj) {
                super(view, dlg, dObj);
                mTable = table;
            }

            protected void setCondition(SQLCondition cond) {
                SQLCondition oldCondition = mTable.getFilterCondition();
                if (cond != null) {
                    if (!cond.equals(oldCondition)) {
                        mTable.setFilterCondition(cond);
                        mSqlModel.setDirty(true);
                    }
                }
            }
        }

        protected Dialog mDialog;
        //protected SQLBasicTableArea mTableNode;
        protected ConditionBuilderView mView;
        protected CollabSQLUIModel mSqlModel;
        protected MashupDataObject mDataObj;
        protected ConditionValidator(ConditionBuilderView view, Dialog dialog, MashupDataObject dObj) {
            //mTableNode = gNode;
            mDataObj = dObj;
            mView = view;
            mDialog = dialog;
            mSqlModel = dObj.getModel();
        }

        public void actionPerformed(ActionEvent e) {
            if (NotifyDescriptor.OK_OPTION.equals(e.getSource())) {
                if (!mView.isConditionValid()) {
                    String nbBundle1 =NbBundle.getMessage(ExtractionConditionAction.class, "LBL_Current_condition_is_invalid");
                    NotifyDescriptor confirmDlg = new NotifyDescriptor.Confirmation(nbBundle1, mDialog.getTitle(), NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.WARNING_MESSAGE);
                    DialogDisplayer.getDefault().notify(confirmDlg);
                    if (confirmDlg.getValue() != NotifyDescriptor.YES_OPTION) {
                        return;
                    }
                }

                setCondition((SQLCondition) mView.getPropertyValue());
                updateActions();
            //if (mTableNode != null) {
            //  mTableNode.setConditionIcons();
            // }
            }

            mDialog.dispose();
        }

        protected abstract void setCondition(SQLCondition cond);
        private boolean isDirty() {
            return mSqlModel.isDirty();
        }

        private void updateActions() {
            if (isDirty()) {
                try {
                    mDataObj.getMashupDataEditorSupport().synchDocument();
                } catch (Exception e) {
                    //ignore
                }
            }
        }

    }
}