/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.websphere6.ui.nodes.actions;

import java.io.File;
import org.netbeans.modules.j2ee.websphere6.WSDeploymentFactory;
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
                    
                    String serverName = 
                            ((WSManagerNode) node).getDeploymentManager().
                            getInstanceProperties().
                            getProperty(WSDeploymentFactory.SERVER_NAME_ATTR);
                    
                    new WSTailer(file, 
                            NbBundle.getMessage(
                            ShowServerLogAction.class, 
                            "LBL_LogWindowTitle", serverName)).start(); // NOI18N
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