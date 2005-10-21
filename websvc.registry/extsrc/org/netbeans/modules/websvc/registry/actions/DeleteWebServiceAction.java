/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.registry.actions;

import org.openide.util.actions.NodeAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import java.io.IOException;
import org.netbeans.modules.websvc.registry.nodes.WebServicesCookie;
import org.netbeans.modules.websvc.registry.model.WebServiceListModel;
import org.netbeans.modules.websvc.registry.model.WebServiceData;
/**
 * This action will delete a web service from the server navigator
 * @author  Lukas Jungman, Milan Kuchtiak
 */

public class DeleteWebServiceAction extends NodeAction {

    protected boolean enable(Node[] nodes) {
        if(nodes != null && nodes.length > 0)
            for (int i = 0; i < nodes.length; i++) {
                if (nodes[i].getCookie(WebServicesCookie.class) == null) {
                    return false;
                }
            }
        return true;
    }

    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new AddToFormAction.class);
    }

    protected String iconResource() {
        return "org/netbeans/modules/websvc/registry/resources/MyActionIcon.gif"; //NOI18N
    } 

    public String getName() {
        return NbBundle.getMessage(DeleteWebServiceAction.class, "DELETE");
    }

    protected void performAction(Node[] nodes) {
        if(null != nodes) {
            String msg = NbBundle.getMessage(DeleteWebServiceAction.class, "WS_DELETE", Integer.toString(nodes.length));
            NotifyDescriptor d = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.YES_NO_OPTION);
            Object response = DialogDisplayer.getDefault().notify(d);
            if(response.equals(NotifyDescriptor.YES_OPTION)) {
                for (int i = 0; i < nodes.length; i++) {
                    try {
                        nodes[i].destroy();
                    } catch(IOException ioe) {
                        ErrorManager.getDefault().notify(ioe);
                        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(DeleteWebServiceAction.class, "ERROR_DELETING"));
                    }
                }
            }
        }
    }

    protected boolean asynchronous() {
        return false;
    }
}
