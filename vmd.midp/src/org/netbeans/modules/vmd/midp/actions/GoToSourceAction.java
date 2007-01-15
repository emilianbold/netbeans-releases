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

package org.netbeans.modules.vmd.midp.actions;

import java.awt.event.ActionEvent;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;


/**
 *
 * @author Karol Harezlak
 */
public final class GoToSourceAction extends SystemAction {

    public static final String DISPLAY_NAME = NbBundle.getMessage(GoToSourceAction.class, "NAME_GoToSourceAction"); //NOI18N
    
    public void actionPerformed(ActionEvent e) {
        //TODO Repalce it witch real functionality
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("Not implemented yet!")); //NOI18N
    }
    
    public String getName() {
        return DISPLAY_NAME;
    }
    
    public HelpCtx getHelpCtx() {
        return null;
    }

    //TODO Temporary disabled
    public boolean isEnabled() {
        return false;
    }
    
}
