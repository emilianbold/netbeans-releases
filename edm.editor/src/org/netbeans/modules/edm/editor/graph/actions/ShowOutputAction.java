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
import org.netbeans.modules.edm.editor.graph.components.EDMOutputTopComponent;

import org.netbeans.modules.edm.editor.utils.ImageConstants;
import org.netbeans.modules.edm.editor.utils.MashupGraphUtil;
import java.util.logging.Logger;
import org.openide.util.NbBundle;

/**
 *
 * @author karthikeyan s
 */
public class ShowOutputAction extends AbstractAction {
    
    private MashupDataObject mObj;
    private static final Logger mLogger = Logger.getLogger(ShowOutputAction.class.getName());
    
    /** Creates a new instance of EditJoinAction */
    public ShowOutputAction(MashupDataObject dObj) {
        super("", new ImageIcon(
                MashupGraphUtil.getImage(ImageConstants.OUTPUT)));
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('T', InputEvent.CTRL_DOWN_MASK+InputEvent.ALT_DOWN_MASK));
        mObj = dObj;
    }
    
    public ShowOutputAction(String name, MashupDataObject dObj) {
        super(name, new ImageIcon(
                MashupGraphUtil.getImage(ImageConstants.OUTPUT)));
        this.putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(ShowOutputAction.class, "TOOLTIP_Toggle_Output"));
        //this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('T', InputEvent.CTRL_DOWN_MASK+InputEvent.ALT_DOWN_MASK));
        mObj = dObj;
    }
    
    public void actionPerformed(ActionEvent e) {
        // close the netbeans output window.
       EDMOutputTopComponent topComp = EDMOutputTopComponent.findInstance();
       if(topComp.isOpened()) {
           topComp.close();
       } else {
           topComp.open();
           topComp.requestActive();
           topComp.setVisible(true);
       }
    }
}