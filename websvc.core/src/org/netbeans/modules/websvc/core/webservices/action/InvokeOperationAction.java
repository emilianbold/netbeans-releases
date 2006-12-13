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
package org.netbeans.modules.websvc.core.webservices.action;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.core.InvokeOperationCookie;
import org.netbeans.modules.websvc.core.WebServiceActionProvider;
import org.netbeans.modules.websvc.core.webservices.ui.panels.ClientExplorerPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


import org.openide.util.actions.NodeAction;


/**
 *
 * @author Peter Williams
 */
public class InvokeOperationAction extends NodeAction {
    public String getName() {
        return NbBundle.getMessage(InvokeOperationAction.class, "LBL_CallWebServiceOperation"); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        // If you will provide context help then use:
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean asynchronous() {
        return false;
    }

    protected boolean enable(Node[] activatedNodes) {
        boolean result = false;
        if (activatedNodes != null && activatedNodes.length == 1 && activatedNodes[0] != null) {
            if (InvokeOperationCookie.TARGET_SOURCE_UNKNOWN != getTargetSourceType(activatedNodes[0]))
                return true;
        }
        return result;
    }

    protected void performAction(Node[] activatedNodes) {
        if(activatedNodes != null && activatedNodes[0] != null) {
            FileObject currentFO = getCurrentFileObject(activatedNodes[0]);
            if(currentFO != null) {
                // !PW I wrote this code before I knew about NodeOperation.  Anyway, this
                // behaves a bit nicer in that the root node is hidden and the tree opens
                // up expanded.  Both improve usability for this use case I think.
                ClientExplorerPanel serviceExplorer = new ClientExplorerPanel(currentFO);
                DialogDescriptor descriptor = new DialogDescriptor(serviceExplorer, 
                        NbBundle.getMessage(InvokeOperationAction.class,"TTL_SelectOperation"));
                serviceExplorer.setDescriptor(descriptor);
                // !PW FIXME put help context here when known to get a displayed help button on the panel.
//                descriptor.setHelpCtx(new HelpCtx("HelpCtx_J2eePlatformInstallRootQuery"));
                if(DialogDisplayer.getDefault().notify(descriptor).equals(NotifyDescriptor.OK_OPTION)) {
                    // !PW FIXME refactor this as a method implemented in a cookie
                    // on the method node.
                    Project project = FileOwnerQuery.getOwner(currentFO);
                    if (project!=null) {
                    InvokeOperationCookie invokeCookie = WebServiceActionProvider.getInvokeOperationAction(project);
                    if (invokeCookie!=null)
                        invokeCookie.invokeOperation(getTargetSourceType(activatedNodes[0]), activatedNodes[0], serviceExplorer.getSelectedMethod());
                    }
                }
            }
        }
    }
    
    private FileObject getCurrentFileObject(Node n) {
        FileObject result = null;
        DataObject dobj = (DataObject) n.getCookie(DataObject.class);
        if(dobj != null) {
            result = dobj.getPrimaryFile();
        }
        return result;
    }
    
    private int getTargetSourceType(Node node) {
        EditorCookie cookie = (EditorCookie)node.getCookie(EditorCookie.class);
        if (cookie!=null && "text/x-jsp".equals(cookie.getDocument().getProperty("mimeType"))) { //NOI18N
            return InvokeOperationCookie.TARGET_SOURCE_JSP;
        } else if (cookie!=null && "text/x-java".equals(cookie.getDocument().getProperty("mimeType"))) { //NOI18N
            return InvokeOperationCookie.TARGET_SOURCE_JAVA;
        }
        return InvokeOperationCookie.TARGET_SOURCE_UNKNOWN;
    }
    
}
