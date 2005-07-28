/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.ui.actions;

import com.sun.collablet.CollabManager;

import org.openide.*;
import org.openide.util.*;
import org.openide.util.actions.*;

import java.awt.event.*;

import org.netbeans.modules.collab.*;
import org.netbeans.modules.collab.ui.*;


/**
 *
 *
 * @author Todd Fast <todd.fast@sun.com>
 */
public class LoginAction extends SystemAction {
    public boolean isEnabled() {
        CollabManager manager = CollabManager.getDefault();

        return manager != null;
    }

    public String getName() {
        return NbBundle.getMessage(LoginAction.class, "LBL_LoginAction_Name");
    }

    protected String iconResource() {
        return "org/netbeans/modules/collab/core/resources/account_png.gif"; // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean asynchronous() {
        return true;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        CollabManager manager = CollabManager.getDefault();
        assert manager != null : "Could not find default CollabManager";

        manager.getUserInterface().login();
    }
}
