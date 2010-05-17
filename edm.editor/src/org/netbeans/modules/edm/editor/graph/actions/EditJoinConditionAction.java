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
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import javax.swing.KeyStroke;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.DialogDescriptor;

import org.netbeans.modules.edm.editor.dataobject.MashupDataObject;
import org.netbeans.modules.edm.model.SQLCondition;
import org.netbeans.modules.edm.editor.graph.jgo.IOperatorXmlInfoModel;
import org.netbeans.modules.edm.editor.ui.view.conditionbuilder.ConditionBuilderView;
import org.netbeans.modules.edm.editor.graph.MashupGraphManager;
import org.netbeans.modules.edm.editor.utils.ImageConstants;
import org.netbeans.modules.edm.editor.utils.MashupGraphUtil;
import org.netbeans.modules.edm.model.SQLJoinOperator;
import org.openide.util.NbBundle;

/**
 *
 * @author karthikeyan s
 */
public class EditJoinConditionAction extends AbstractAction {

    private SQLJoinOperator joinOp;
    private MashupDataObject mObj;
    private MashupGraphManager manager;

    /** Creates a new instance of EditJoinAction */
    public EditJoinConditionAction(MashupDataObject dObj, SQLJoinOperator op) {
        super("", new ImageIcon(
                MashupGraphUtil.getImage(ImageConstants.JOINCONDITION)));
        mObj = dObj;
        joinOp = op;
        this.manager = dObj.getGraphManager();
    }

    public EditJoinConditionAction(MashupDataObject dObj, SQLJoinOperator op, String name) {
        super(name, new ImageIcon(
                MashupGraphUtil.getImage(ImageConstants.JOINCONDITION)));
        this.putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(EditJoinConditionAction.class, "TOOLTIP_Edit_JoinCondition"));
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('J', InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_MASK));
        mObj = dObj;
        joinOp = op;
        this.manager = dObj.getGraphManager();
    }

    public void actionPerformed(ActionEvent e) {
        if (joinOp != null && mObj.getEditorView() != null) {

            List srcTables = joinOp.getAllSourceTables();
            if (mObj.getModel().getSQLDefinition().getRuntimeDbModel() != null &&
                    mObj.getModel().getSQLDefinition().getRuntimeDbModel().getRuntimeInput() != null) {
                srcTables.add(mObj.getModel().getSQLDefinition().getRuntimeDbModel().getRuntimeInput());
            }

            ConditionBuilderView conditionView = new ConditionBuilderView(
                    mObj.getEditorView().getCollabSQLUIModel(),
                    srcTables, joinOp.getJoinCondition(), IOperatorXmlInfoModel.CATEGORY_FILTER);
            DialogDescriptor dd = new DialogDescriptor(conditionView, NbBundle.getMessage(EditJoinConditionAction.class, "TITLE_Edit_Join_Condition"),
                    true, NotifyDescriptor.OK_CANCEL_OPTION, null, null);

            if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION) {
                SQLCondition cond = (SQLCondition) conditionView.getPropertyValue();
                if (cond != null) {
                    SQLCondition oldCondition = joinOp.getJoinCondition();
                    if (joinOp != null && !cond.equals(oldCondition)) {
                        joinOp.setJoinCondition(cond);
                        joinOp.setJoinConditionType(SQLJoinOperator.USER_DEFINED_CONDITION);
                        //mObj.getMashupDataEditorSupport().synchDocument();
                        mObj.getModel().setDirty(true);
                        mObj.setModified(true);
                    }
                }
            }
        }
    }
}