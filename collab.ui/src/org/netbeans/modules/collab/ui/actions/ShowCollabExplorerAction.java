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

import org.openide.util.*;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.collab.ui.*;
import com.sun.collablet.CollabManager;

/**
 *
 *
 * @author Todd Fast <todd.fast@sun.com>
 */
public class ShowCollabExplorerAction extends SystemAction {
    public boolean isEnabled() {
        return true;
    }

    public String getName() {
        return NbBundle.getMessage(ShowCollabExplorerAction.class, "LBL_ShowCollabExplorerAction_Name");
    }

    protected String iconResource() {
        return "org/netbeans/modules/collab/core/resources/collab_png.gif";
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        CollabExplorerPanel panel = CollabExplorerPanel.getInstance();

        // Show the appropriate panel.  We do this here, because the circum-
        // stances under which open() will be called on the panel are not
        // always consistent.
        if ((CollabManager.getDefault() != null) && (CollabManager.getDefault().getSessions().length > 0)) {
            panel.showComponent(CollabExplorerPanel.COMPONENT_EXPLORER);
        } else {
            panel.showComponent(CollabExplorerPanel.COMPONENT_LOGIN);
        }

        panel.open(null);
        panel.requestActive();
    }
}
