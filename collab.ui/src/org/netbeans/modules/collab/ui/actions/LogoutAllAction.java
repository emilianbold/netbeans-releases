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
package org.netbeans.modules.collab.ui.actions;

import java.awt.event.*;

import org.openide.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;

import com.sun.collablet.CollabManager;

/**
 *
 *
 * @author Todd Fast <todd.fast@sun.com>
 */
public class LogoutAllAction extends SystemAction {
    public boolean isEnabled() {
        CollabManager manager = CollabManager.getDefault();

        return (manager != null) && (manager.getSessions().length > 0);
    }

    public String getName() {
        return NbBundle.getMessage(LogoutAllAction.class, "LBL_LogoutAllAction_Name");
    }

    protected String iconResource() {
        return "org/openide/resources/actions/empty.gif";
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean asynchronous() {
        return true;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        // Prompt for logoff
        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(LogoutAction.class, "MSG_LogoutAllAction_ConfirmLogout"),
                NotifyDescriptor.YES_NO_OPTION
            );

        if (DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.YES_OPTION) {
            CollabManager manager = CollabManager.getDefault();
            assert manager != null : "Could not find default CollabManager";

            manager.invalidate();
        }
    }
}
