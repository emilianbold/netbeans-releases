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

package org.netbeans.core.actions;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

// TODO Make this action plain javax.swing.Action, SystemAction is unneeded overkill here.
public class LogAction extends CallableSystemAction {


    public LogAction() {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }


    public void performAction() {
        // FIXME This may not be used this way anymore.
        String userDir = System.getProperty("netbeans.user");
	if (userDir == null)
            return;
        // FIXME the same as above
	File f = new File(userDir + "/var/log/messages.log"); // TEMP
        LogViewerSupport p = new LogViewerSupport(f, NbBundle.getMessage(LogAction.class, "MSG_ShortLogTab_name"));
	try {
            p.showLogViewer();
	} catch (java.io.IOException e) {
            Logger.getLogger(LogAction.class.getName()).log(Level.INFO, "Showing IDE log action failed", e);
        }
    }

    public String getName() {
        return NbBundle.getMessage(LogAction.class, "MSG_LogTab_name"); // NOI18N
    }

    @Override public String iconResource() {
        return "org/netbeans/core/resources/log-file.gif"; // NOI18N
    }

    @Override public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override public boolean asynchronous() {
        return false ;
    }
}
