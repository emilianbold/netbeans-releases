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
import org.netbeans.modules.edm.editor.graph.MashupGraphManager;
import org.netbeans.modules.edm.editor.utils.ImageConstants;
import org.netbeans.modules.edm.editor.utils.MashupGraphUtil;
import org.netbeans.modules.edm.model.SQLObject;
import org.openide.util.NbBundle;

/**
 *
 * @author karthikeyan s
 */
public class ShowDataAction extends AbstractAction {
    
    private MashupDataObject mObj;
    
    private SQLObject obj;
    private MashupGraphManager manager;
    
    /** Creates a new instance of EditJoinAction */
    public ShowDataAction(MashupDataObject dObj, SQLObject obj) {
        super("",new ImageIcon(
                MashupGraphUtil.getImage(ImageConstants.OUTPUT)));        
        mObj = dObj;
        this.manager = dObj.getGraphManager();
        this.obj = obj;
    }
    
    public ShowDataAction(MashupDataObject dObj, SQLObject obj, String name) {
        super(name,new ImageIcon(
                MashupGraphUtil.getImage(ImageConstants.OUTPUT)));
        this.putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(ShowDataAction.class, "TOOLTIP_Show_Data"));
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('D', InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_MASK));
        mObj = dObj;
        this.manager = dObj.getGraphManager();
        this.obj = obj;
    }
    
    public void actionPerformed(ActionEvent e) {
        try {
            manager.showOutput(obj, mObj.getModel().getSQLDefinition());
        } catch (Exception ex) {
            manager.setLog(NbBundle.getMessage(ShowDataAction.class, "LOG_Failed_to_generate_output") + ex.getMessage());
        }
    }
}