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
package org.netbeans.modules.apisupport.project.metainf;

import java.lang.ref.WeakReference;
import javax.swing.JButton;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;

public final class AddService extends CookieAction {
    private static WeakReference<AddService> actionInstance;
    
    protected void performAction(Node[] activatedNodes) {
        // TODO implement action body
        if (activatedNodes.length > 0) {
            ServiceNodeHandler.ServiceNode node = activatedNodes[0].getLookup().lookup(org.netbeans.modules.apisupport.project.metainf.ServiceNodeHandler.ServiceNode.class);
            if ( node != null) {
                AddServiceDialog panel = new AddServiceDialog(node.getProject());
                
                NotifyDescriptor dd = new NotifyDescriptor(
                    panel, 
                    org.openide.util.NbBundle.getMessage(AddService.class, "LBL_Add_Service_Class"),
                    0,
                    NotifyDescriptor.PLAIN_MESSAGE,
                    new Object[] { panel.getOkButton(),panel.getCancelButton() },
                    null
                );
                
                Object res = DialogDisplayer.getDefault().notify(dd);
                System.out.println(res);
                if (res == panel.getOkButton()) {
                    String classServiceName = panel.getClassName();
                    if (classServiceName != null) {
                        node.addService(node.getName(),classServiceName);
                    }
                }
            }
        }
    }

    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    public String getName() {
        return org.openide.util.NbBundle.getMessage(AddService.class, "MSG_Add_New_Service");
    }

    protected Class[] cookieClasses() {
        return new Class[] {
            ServiceNodeHandler.ServiceNode.class
        };
    }
    
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean asynchronous() {
        return false;
    }
    
    public static AddService getInstance() {
        AddService action = (actionInstance != null ) ? actionInstance.get() : null;
        if (action == null) {
            action = new AddService();
            actionInstance = new WeakReference<AddService>(action);
        }
        return action;
    }

}

