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
package org.netbeans.modules.collab.channel.filesharing.ui.actions;

import org.openide.util.*;
import org.openide.util.actions.*;

import java.awt.event.*;

import org.netbeans.modules.collab.channel.filesharing.FilesharingCollablet;
import org.netbeans.modules.collab.channel.filesharing.FilesharingConstants;
import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.core.Debug;


/**
 * SyncAction
 *
 * @author  Ayub Khan
 * @version 1.0
 */
public class SyncAction extends SystemAction implements FilesharingConstants {
    public String getName() {
        return NbBundle.getMessage(SyncAction.class, "LBL_SyncAction_Name");
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
        Debug.log(this, "SyncAction, actionPerformed"); //NoI18n

        FilesharingContext context = FilesharingCollablet.getActivatedComponentContext();

        if ((context != null) && context.isValid()) {
            context.doSyncOperation();
        }
    }
}
