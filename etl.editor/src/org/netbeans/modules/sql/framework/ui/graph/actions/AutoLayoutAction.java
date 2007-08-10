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
package org.netbeans.modules.sql.framework.ui.graph.actions;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.net.URL;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.openide.util.NbBundle;


/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class AutoLayoutAction extends GraphAction {

    private static final URL autoLayoutImgUrl = AutoLayoutAction.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/layout_edm.png");

    public AutoLayoutAction() {
        //action name
        this.putValue(Action.NAME, NbBundle.getMessage(AutoLayoutAction.class, "ACTION_AUTOLAYOUT"));

        //action icon
        this.putValue(Action.SMALL_ICON, new ImageIcon(autoLayoutImgUrl));

        //action tooltip
        this.putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(AutoLayoutAction.class, "ACTION_AUTOLAYOUT_TOOLTIP"));

        // Acceleratot Cntl-Shift-L
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('L', InputEvent.CTRL_MASK + InputEvent.SHIFT_DOWN_MASK));

    }

    /**
     * called when this action is performed in the ui
     * 
     * @param ev event
     */
    public void actionPerformed(ActionEvent ev) {
        IGraphView graphView = (IGraphView) ev.getSource();
        graphView.autoLayout();
    }
}

