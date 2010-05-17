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
import org.netbeans.modules.edm.editor.utils.ImageConstants;
import org.netbeans.modules.edm.editor.utils.MashupGraphUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author karthikeyan s
 */
public class FitToWidthAction extends AbstractAction {

    private MashupDataObject mObj;

    /** Creates a new instance of EditJoinAction */
    public FitToWidthAction(MashupDataObject dObj) {
        super("",new ImageIcon(
                MashupGraphUtil.getImage(ImageConstants.FITTOWIDTH)));
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('W', InputEvent.CTRL_DOWN_MASK+InputEvent.ALT_DOWN_MASK));
        mObj = dObj;
    }
    
    public FitToWidthAction(MashupDataObject dObj, String name) {
        super(name,new ImageIcon(
                MashupGraphUtil.getImage(ImageConstants.FITTOWIDTH)));
        this.putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(FitToWidthAction.class, "TOOLTIP_Fit_To_Width"));
        //this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('W', InputEvent.CTRL_DOWN_MASK+InputEvent.ALT_DOWN_MASK));
        mObj = dObj;
    }

    public void actionPerformed(ActionEvent e) {
        mObj.getGraphManager().fitToWidth();
//        String nbBundle1 = mLoc.t("BUND172: Graph successfully fit to width.");
//        mObj.getGraphManager().setLog(nbBundle1.substring(15));
    }
}