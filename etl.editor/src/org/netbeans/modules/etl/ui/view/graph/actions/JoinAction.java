/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.etl.ui.view.graph.actions;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.net.URL;
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.netbeans.modules.etl.ui.DataObjectProvider;
import org.netbeans.modules.etl.ui.model.impl.ETLCollaborationModel;
import org.netbeans.modules.etl.ui.view.ETLCollaborationTopComponent;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.ui.graph.actions.GraphAction;
import org.netbeans.modules.sql.framework.ui.view.join.JoinMainDialog;
import org.netbeans.modules.sql.framework.ui.view.join.JoinUtility;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.utils.Logger;

/**
 * This action is to create or edit join
 * 
 * @author Ritesh Adval
 * @version $Revision$
 */
public class JoinAction extends GraphAction {

    private static final URL joinImgUrl = ValidationAction.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/join_view.png");

    private static final String LOG_CATEGORY = JoinAction.class.getName();

    public JoinAction() {
        // action name
        this.putValue(Action.NAME, NbBundle.getMessage(JoinAction.class, "ACTION_JOIN"));

        // action icon
        this.putValue(Action.SMALL_ICON, new ImageIcon(joinImgUrl));

        // action tooltip
        this.putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(JoinAction.class, "ACTION_JOIN_TOOLTIP"));

        // Acceleratot Cntl-Shift-J
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('J', InputEvent.CTRL_MASK + InputEvent.SHIFT_DOWN_MASK));
    }

    /**
     * called when this action is performed in the ui
     * 
     * @param ev event
     */
    public void actionPerformed(ActionEvent ev) {
        ETLCollaborationTopComponent etlEditor = null;
        try {
            etlEditor = DataObjectProvider.getProvider().getActiveDataObject().getETLEditorTC();
        } catch (Exception ex) {
            //ignore
        }

        // first check if user has selected some tables/join view and he wants to create
        // a join

        // if user just selects one join view then he wants to edit that
        // if there is no selection then user wants to create a new join
        ETLCollaborationModel collabModel = DataObjectProvider.getProvider()
                                                .getActiveDataObject().getModel();

        if (collabModel != null) {
            List sList = collabModel.getSQLDefinition().getJoinSources();
            JoinMainDialog.showJoinDialog(sList, null, etlEditor.getGraphView(), true);

            if (JoinMainDialog.getClosingButtonState() == JoinMainDialog.OK_BUTTON) {
                SQLJoinView joinView = JoinMainDialog.getSQLJoinView();
                try {
                    if (joinView != null) {
                        JoinUtility.handleNewJoinCreation(joinView, JoinMainDialog.getTableColumnNodes(), etlEditor.getGraphView());
                    }
                } catch (BaseException ex) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("Error adding join view.", NotifyDescriptor.INFORMATION_MESSAGE));

                    Logger.printThrowable(Logger.ERROR, LOG_CATEGORY, "JoinAction.actionPerformed", "error adding join view", ex);
                }
            }
        }
    }
}

