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

package org.netbeans.modules.j2ee.jboss4.nodes.actions;

import java.util.Enumeration;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Michal Mocnak
 */
public class UndeployModuleAction extends NodeAction {
    
    protected void performAction(Node[] nodes) {
        if( (nodes == null) || (nodes.length < 1) )
            return;
        
        for (int i = 0; i < nodes.length; i++) {
            UndeployModuleCookie uCookie = (UndeployModuleCookie)nodes[i].getCookie(UndeployModuleCookie.class);
            if (uCookie != null) {
                final Task t = uCookie.undeploy();
                final Node node = nodes[i].getParentNode();
                
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        t.waitFinished();
                        if(node != null) {
                            Node apps = node.getParentNode();
                            if (apps != null) {
                                Enumeration appTypes = apps.getChildren().nodes();
                                while (appTypes.hasMoreElements()) {
                                    Node appType = (Node)appTypes.nextElement();
                                    RefreshModulesCookie cookie = (RefreshModulesCookie)appType.getCookie(RefreshModulesCookie.class);
                                    if (cookie != null) {
                                        cookie.refresh();
                                    }
                                }
                            }
                        }
                    }
                });
            }
        }
    }
    
    protected boolean enable(Node[] nodes) {
        UndeployModuleCookie cookie;
        for (int i=0; i<nodes.length; i++) {
            cookie = (UndeployModuleCookie)nodes[i].getCookie(UndeployModuleCookie.class);
            if (cookie == null || cookie.isRunning())
                return false;
        }
        
        return true;
    }
    
    public String getName() {
        return NbBundle.getMessage(UndeployModuleAction.class, "LBL_UndeployAction");
    }
    
    protected boolean asynchronous() { return false; }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}