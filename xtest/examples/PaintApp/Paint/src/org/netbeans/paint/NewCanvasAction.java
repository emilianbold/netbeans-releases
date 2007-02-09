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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.paint;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

public final class NewCanvasAction extends CallableSystemAction {

    private static final int MAX_PAINT_TC_COUNT = 8;

    public void performAction() {
        if (PaintTopComponent.getPaintTCCount() < MAX_PAINT_TC_COUNT) {
            PaintTopComponent tc = new PaintTopComponent();
            tc.open();
            tc.requestActive();
        }
        else {
            DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message(
                NbBundle.getMessage(NewCanvasAction.class, "MSG_CannotCreateNewCanvas")));
        }
    }
    
    public String getName() {
        return NbBundle.getMessage(NewCanvasAction.class, "CTL_NewCanvasAction");
    }
    
    protected String iconResource() {
        return "org/netbeans/paint/new.PNG";
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
}
