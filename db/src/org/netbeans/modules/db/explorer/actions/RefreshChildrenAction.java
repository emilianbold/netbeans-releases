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

package org.netbeans.modules.db.explorer.actions;

import java.text.MessageFormat;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;

public class RefreshChildrenAction extends DatabaseAction {
    static final long serialVersionUID =-2858583720506557569L;

    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length == 1)
            if (activatedNodes[0].getChildren().getNodesCount() == 1 && activatedNodes[0].getChildren().getNodes()[0].getName().equals(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("WaitNode"))) //NOI18N
                return false;
            else
                return true;
        
        return false;
    }
    
    public void performAction (Node[] activatedNodes) {
        final Node node;
        if (activatedNodes != null && activatedNodes.length == 1)
            node = activatedNodes[0];
        else
            return;

        RequestProcessor.getDefault().post(new Runnable() {
            public void run () {
                try {
                    DatabaseNodeInfo nfo = (DatabaseNodeInfo) node.getCookie(DatabaseNodeInfo.class);
                    if (nfo != null)
                        nfo.refreshChildren();
                } catch(Exception exc) {
                    String message = bundle().getString("RefreshChildrenErrorPrefix") + " " + MessageFormat.format(bundle().getString("EXC_ConnectionError"), new String[] {exc.getMessage()}); // NOI18N
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                }
            }
        }, 0);
    }
}
