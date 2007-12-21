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

package org.netbeans.modules.bpel.debugger.ui.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.netbeans.modules.bpel.debugger.ui.process.ProcessesTopComponent;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2005.12.01
 */
public class Process extends AbstractAction {

    /**{@inheritDoc}*/
    public Process() {
      putValue(NAME, NbBundle.getMessage(Process.class, "LBL_ProcessView"));//NOI18N
      putValue(
        SMALL_ICON,
        new ImageIcon (Utilities.loadImage (
        "org/netbeans/modules/bpel/debugger/ui/" + // NOI18N
        "resources/image/process.gif"))); // NOI18N
    }
    
    /**{@inheritDoc}*/
    public void actionPerformed(ActionEvent e) {
        TopComponent view = WindowManager.getDefault().findTopComponent(
            ProcessesTopComponent.VIEW_NAME);
        if (view == null) {
            throw new IllegalArgumentException(ProcessesTopComponent.VIEW_NAME);
        }
        view.open();
        view.requestActive();
    }
    
    private static final long serialVersionUID = 1L; 
}
