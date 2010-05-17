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

import javax.swing.KeyStroke;
import org.netbeans.modules.edm.editor.dataobject.MashupDataObject;
import org.netbeans.modules.edm.editor.ui.view.TablePanel;
import org.netbeans.modules.edm.editor.utils.ImageConstants;
import org.netbeans.modules.edm.editor.utils.MashupGraphUtil;
import org.netbeans.modules.edm.model.SQLObject;
import org.netbeans.modules.edm.model.SQLConstants;
import java.util.logging.Logger;
import org.openide.util.NbBundle;

/**
 *
 * @author karthikeyan s
 */
public class RuntimeInputAction extends AbstractAction {

    private MashupDataObject mObj;
    private SQLObject obj;
    private static final Logger mLogger = Logger.getLogger(RuntimeInputAction.class.getName());

    /** Creates a new instance of EditJoinAction */
    public RuntimeInputAction(MashupDataObject dObj) {
        super("", new ImageIcon(
                MashupGraphUtil.getImage(ImageConstants.RUNTIMEINPUT)));
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('U', InputEvent.CTRL_MASK + InputEvent.ALT_DOWN_MASK));
        mObj = dObj;
    }

    public RuntimeInputAction(MashupDataObject dObj, String name) {
        super(name, new ImageIcon(
                MashupGraphUtil.getImage(ImageConstants.RUNTIMEINPUT)));
        this.putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(RuntimeInputAction.class, "TOOLTIP_Add/Edit_Runtime_Inputs"));
        //this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('U', InputEvent.CTRL_MASK + InputEvent.ALT_DOWN_MASK));
        mObj = dObj;
    }

    public void actionPerformed(ActionEvent e) {
        try {
            TablePanel tPanel = new TablePanel(SQLConstants.RUNTIME_INPUT, mObj.getModel());
            tPanel.showTablePanel();
            if (mObj.getModel().isDirty()) {
                //mObj.getMashupDataEditorSupport().synchDocument();
                mObj.getModel().setDirty(true);
                mObj.setModified(true);
                mObj.getGraphManager().refreshGraph();
                mObj.getGraphManager().layoutGraph();
            }
        } catch (Exception ex) {
            mObj.getGraphManager().setLog(NbBundle.getMessage(RuntimeInputAction.class, "LOG_Failed_to_add_Runtime_arguments"));
        }
    }
}