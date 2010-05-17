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
import org.netbeans.modules.edm.editor.dataobject.MashupDataObject;
import org.netbeans.modules.edm.editor.graph.MashupGraphManager;
import org.netbeans.modules.edm.editor.utils.ImageConstants;
import org.netbeans.modules.edm.editor.utils.MashupGraphUtil;
import org.netbeans.modules.edm.model.SQLConstants;
import org.netbeans.modules.edm.model.SQLJoinView;
import org.netbeans.modules.edm.model.DBTable;
import org.netbeans.modules.edm.model.SQLObject;
import org.openide.util.NbBundle;

/**
 *
 * @author karthikeyan s
 */
public class TestRunAction extends AbstractAction {
    
    private MashupDataObject mObj;
    
    private MashupGraphManager manager;

    /** Creates a new instance of EditJoinAction */
    public TestRunAction(MashupDataObject dObj) {
        super("",new ImageIcon(
                MashupGraphUtil.getImage(ImageConstants.RUN)));
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('N', InputEvent.SHIFT_DOWN_MASK+InputEvent.ALT_MASK));
        mObj = dObj;
        this.manager = dObj.getGraphManager();
    }
    
    public TestRunAction(MashupDataObject dObj, String name) {
        super(name,new ImageIcon(
                MashupGraphUtil.getImage(ImageConstants.RUN)));
        this.putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(TestRunAction.class, "LBL_Run_Collaboration"));
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('N', InputEvent.SHIFT_DOWN_MASK+InputEvent.ALT_MASK));
        mObj = dObj;
        this.manager = dObj.getGraphManager();
    }
    
    public void actionPerformed(ActionEvent e) {
        try {
            SQLJoinView[] joinViews = (SQLJoinView[]) mObj.getModel().getSQLDefinition().getObjectsOfType(
                    SQLConstants.JOIN_VIEW).toArray(new SQLJoinView[0]);
            if (joinViews != null && joinViews.length != 0) {
                SQLJoinView joinView = joinViews[0];
                manager.showOutput(joinView, mObj.getModel().getSQLDefinition());
            } else {
                List<DBTable> tables = mObj.getModel().getSQLDefinition().getSourceTables();
                if (tables != null) {
                    for (DBTable table : tables) {
                        manager.showOutput((SQLObject) table, mObj.getModel().getSQLDefinition());
                    }
                }
            }
        } catch (Exception ex) {
            manager.setLog(NbBundle.getMessage(TestRunAction.class, "LOG_Failed_to_run_collaboration") + ex.getMessage());
        }
    }
}