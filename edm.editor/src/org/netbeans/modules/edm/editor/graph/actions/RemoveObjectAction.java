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
import org.netbeans.modules.edm.model.SQLObject;
import org.netbeans.modules.edm.model.impl.SQLDefinitionImpl;
import org.netbeans.modules.edm.model.MashupCollaborationModel;
import org.netbeans.modules.edm.model.RuntimeInput;
import org.netbeans.modules.edm.model.SQLDBTable;
import org.netbeans.modules.edm.model.SourceColumn;
import org.netbeans.modules.edm.editor.utils.SQLObjectUtil;
import org.netbeans.modules.edm.editor.ui.event.SQLDataEvent;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author karthikeyan s
 */
public class RemoveObjectAction extends AbstractAction {

    private MashupDataObject mObj;
    private SQLObject obj;

    /** Creates a new instance of EditJoinAction */
    public RemoveObjectAction(MashupDataObject dObj, SQLObject obj) {
        super("", new ImageIcon(
                MashupGraphUtil.getImage(ImageConstants.REMOVE)));
        mObj = dObj;
        this.obj = obj;
    }

    public RemoveObjectAction(MashupDataObject dObj, SQLObject obj, String name) {
        super(name, new ImageIcon(
                MashupGraphUtil.getImage(ImageConstants.REMOVE)));
        this.putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(RemoveObjectAction.class, "TOOLTIP_Remove_Table"));
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('R', InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_MASK));
        mObj = dObj;
        this.obj = obj;
    }

    public void actionPerformed(ActionEvent e) {
        int response = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), NbBundle.getMessage(RemoveObjectAction.class, "MSG_delete_the_table"), NbBundle.getMessage(RemoveObjectAction.class, "LBL_Confirm_Delete"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (JOptionPane.OK_OPTION == response) {
            SQLDefinitionImpl defn = (SQLDefinitionImpl) mObj.getModel().getSQLDefinition();
            try {
                removeRuntimeInputs(mObj.getModel(), obj);
                mObj.getGraphManager().removeObject(obj);
                defn.removeObject(obj);
                mObj.getGraphManager().validateScene();
                mObj.getModel().setDirty(true);
                mObj.setModified(true);
            } catch (Exception ex) {
                mObj.getGraphManager().setLog(NbBundle.getMessage(RemoveObjectAction.class, "LOG_Failed_to_remove_object."));
            }
        }
    }

    public void removeRuntimeInputs(MashupCollaborationModel collabModel, SQLObject table) {
        try {
            SourceColumn col = SQLObjectUtil.removeRuntimeInput((SQLDBTable) table, collabModel);
            if (col != null) {
                SQLDataEvent evt = new SQLDataEvent(collabModel, (RuntimeInput) col.getParent(), col);
                collabModel.fireChildObjectDeletedEvent(evt);
            }
            mObj.getGraphManager().removeRuntimeArgs(col, obj);
            mObj.getGraphManager().validateScene();
        } catch (Exception e) {
        }
    }
}
