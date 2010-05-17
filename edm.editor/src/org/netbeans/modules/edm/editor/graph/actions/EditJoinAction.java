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

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import org.netbeans.modules.edm.editor.dataobject.MashupDataObject;
import org.netbeans.modules.edm.editor.utils.ImageConstants;
import org.netbeans.modules.edm.editor.utils.MashupGraphUtil;
import org.netbeans.modules.edm.model.SQLConstants;
import org.netbeans.modules.edm.model.SQLJoinView;
import org.netbeans.modules.edm.editor.ui.view.join.JoinMainDialog;
import org.netbeans.modules.edm.editor.ui.view.join.JoinUtility;
import org.netbeans.modules.edm.model.SQLJoinOperator;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author karthikeyan s
 */
public class EditJoinAction extends AbstractAction {

    private MashupDataObject mObj;
    private SQLJoinOperator joinOp;

    /** Creates a new instance of EditJoinAction */
    public EditJoinAction(MashupDataObject dObj) {
        super("", new ImageIcon(
                MashupGraphUtil.getImage(ImageConstants.EDITJOIN)));
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('J', InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_MASK));
        mObj = dObj;        
    }

    public EditJoinAction(MashupDataObject dObj, SQLJoinOperator joinOp) {
        super("", new ImageIcon(
                MashupGraphUtil.getImage(ImageConstants.EDITJOIN)));
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('J', InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_MASK));
        mObj = dObj;
        this.joinOp = joinOp;
    }

    public EditJoinAction(MashupDataObject dObj, String name) {
        super(name, new ImageIcon(
                MashupGraphUtil.getImage(ImageConstants.EDITJOIN)));
        this.putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(EditJoinAction.class, "TOOLTIP_Edit_Join_Condition"));
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('J', InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_MASK));
        mObj = dObj;
    }

    /** 
     * implements edit join action. 
     */
    public void actionPerformed(ActionEvent e) {
        SQLJoinView[] joinViews = (SQLJoinView[]) mObj.getModel().getSQLDefinition().getObjectsOfType(
                SQLConstants.JOIN_VIEW).toArray(new SQLJoinView[0]);
        SQLJoinView jView = null;
        if (joinViews != null && joinViews.length != 0) {
            jView = joinViews[0];
            JoinMainDialog.showJoinDialog(
                    mObj.getModel().getSQLDefinition().getJoinSources(), jView,
                    null);
            if (JoinMainDialog.getClosingButtonState() == JoinMainDialog.OK_BUTTON) {
                SQLJoinView joinView = JoinMainDialog.getSQLJoinView();
                try {
                    if (joinView != null) {
                        mObj.getModel().getSQLDefinition().removeObjects(
                                mObj.getModel().getSQLDefinition().getObjectsOfType(
                                SQLConstants.JOIN_VIEW));
                        JoinUtility.handleNewJoinCreation(joinView,
                                JoinMainDialog.getTableColumnNodes(),
                                mObj.getEditorView().getCollabSQLUIModel());
                        joinOp.setJoinType(joinOp.getJoinType());
                        mObj.getMashupDataEditorSupport().synchDocument();
                        mObj.getModel().setDirty(true);
                        mObj.setModified(true);
                    }
                } catch (Exception ex) {
                    mObj.getGraphManager().setLog(NbBundle.getMessage(EditJoinAction.class, "LOG_adding_Join_view"));
                }
            }
        } else {
            int response = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), NbBundle.getMessage(EditJoinAction.class, "LBL_no_joins_defined"), NbBundle.getMessage(EditJoinAction.class, "LBL_Error_editing_Join_view"), JOptionPane.YES_NO_OPTION);
            if (JOptionPane.OK_OPTION == response) {
                if (mObj.getModel().getSQLDefinition().getJoinSources().size() != 0) {
                    SQLJoinView[] joinViews1 = (SQLJoinView[]) mObj.getModel().getSQLDefinition().getObjectsOfType(
                            SQLConstants.JOIN_VIEW).toArray(new SQLJoinView[0]);
                    if (joinViews1 == null || joinViews1.length == 0) {
                        JoinMainDialog.showJoinDialog(
                                mObj.getModel().getSQLDefinition().getJoinSources(), null,
                                null, false);
                    } else {
                        JoinMainDialog.showJoinDialog(
                                mObj.getModel().getSQLDefinition().getJoinSources(), joinViews1[0],
                                null);
                    }
                    if (JoinMainDialog.getClosingButtonState() == JoinMainDialog.OK_BUTTON) {
                        SQLJoinView joinView = JoinMainDialog.getSQLJoinView();
                        try {
                            if (joinView != null) {
                                JoinUtility.handleNewJoinCreation(joinView,
                                        JoinMainDialog.getTableColumnNodes(),
                                        mObj.getEditorView().getCollabSQLUIModel());
                                mObj.getGraphManager().refreshGraph();
                                mObj.getGraphManager().layoutGraph();
                            //mObj.getMashupDataEditorSupport().synchDocument();                                
                            }
                        } catch (Exception exc) {
                            mObj.getGraphManager().setLog(NbBundle.getMessage(EditJoinAction.class, "LOG_adding_Join_view."));
                        }
                    }
                }
            }
        }
    }
}