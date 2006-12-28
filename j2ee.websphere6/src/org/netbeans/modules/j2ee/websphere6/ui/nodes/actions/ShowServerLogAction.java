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
package org.netbeans.modules.j2ee.websphere6.ui.nodes.actions;

import java.io.File;
import org.netbeans.modules.j2ee.websphere6.WSDeploymentManager;
import org.netbeans.modules.j2ee.websphere6.ui.nodes.WSManagerNode;
import org.netbeans.modules.j2ee.websphere6.util.WSDebug;
import org.netbeans.modules.j2ee.websphere6.util.WSTailer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 *
 * @author Kirill Sorokin
 */
public class ShowServerLogAction extends CookieAction {
    protected void performAction(Node[] nodes) {
        if( (nodes == null) || (nodes.length < 1)) {
            return;
        }
        
        for (int i = 0; i < nodes.length; i++) {
            Object node = nodes[i].getLookup().lookup(WSManagerNode.class);
            if (node instanceof WSManagerNode) {
                try{
                    File file =
                            new File(((WSManagerNode) node).getLogFilePath());
                    
                    WSDebug.notify(file.getAbsolutePath());
                    
                    
                    WSDeploymentManager dm = ((WSManagerNode) node).
                            getDeploymentManager();
                    
                    new WSTailer(file,
                            NbBundle.getMessage(
                            ShowServerLogAction.class,
                            "LBL_LogWindowTitle",
                            dm.getServerTitleMessage())).start(); // NOI18N
                } catch (Exception e){
                    return;//nothing much to do
                }
            }
        }
    }
    
    public String getName() {
        return NbBundle.getMessage(ShowAdminConsoleAction.class, "LBL_ShowServerLog");
    }
    
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }
    
    public HelpCtx getHelpCtx() {
        return null;
    }
    
    protected Class[] cookieClasses() {
        return new Class[]{};
    }
    
    protected boolean enable(Node[] nodes) {
        if (nodes == null || nodes.length < 1) {
            return false;
        }
        
        boolean local = true;
        
        for (int i = 0; i < nodes.length; i++) {
            Object node = nodes[i].getLookup().lookup(WSManagerNode.class);
            if (!(node instanceof WSManagerNode)) {
                local = false;
                break;
            }
            
            WSDeploymentManager dm =
                    ((WSManagerNode) node).getDeploymentManager();
            
            local = dm.getIsLocal().equals("true"); // NOI18N
        }
        
        return local;
    }
    
    protected boolean asynchronous() {
        return false;
    }
}