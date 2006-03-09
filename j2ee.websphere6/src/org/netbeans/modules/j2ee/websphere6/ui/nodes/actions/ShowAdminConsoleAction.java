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
package org.netbeans.modules.j2ee.websphere6.ui.nodes.actions;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import org.netbeans.modules.j2ee.websphere6.WSDeploymentManager;
import org.netbeans.modules.j2ee.websphere6.ui.nodes.WSManagerNode;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.awt.HtmlBrowser.URLDisplayer;

/**
 *
 * @author Kirill Sorokin
 */
public class ShowAdminConsoleAction extends CookieAction {
    protected void performAction(Node[] nodes) {
        if( (nodes == null) || (nodes.length < 1)) {
            return;
        }
        
        for (int i = 0; i < nodes.length; i++) {
            Object node = nodes[i].getLookup().lookup(WSManagerNode.class);
            if (node instanceof WSManagerNode) {
                try{
                    URL url = new URL(
                            ((WSManagerNode) node).getAdminConsoleURL());
                    
                    URLDisplayer.getDefault().showURL(url);
                } catch (Exception e){
                    return;//nothing much to do
                }
            }
        }
    }
    
    public String getName() {
        return NbBundle.getMessage(ShowAdminConsoleAction.class, "LBL_ShowAdminConsole");
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
        
        boolean running = true;
        
        for (int i = 0; i < nodes.length; i++) {
            Object node = nodes[i].getLookup().lookup(WSManagerNode.class);
            if (!(node instanceof WSManagerNode)) {
                running = false;
                break;
            }
            
            WSDeploymentManager dm =
                    ((WSManagerNode) node).getDeploymentManager();
            
            // try to get an open socket to the target host/port
            try {
                new Socket(dm.getHost(), new Integer(dm.getPort()).intValue());
                
                running = true;
            } catch (UnknownHostException e) {
                ErrorManager.getDefault().notify(ErrorManager.ERROR, e);
            } catch (IOException e) {
                running = false;
            }
        }
        
        return running;
    }
    
    protected boolean asynchronous() {
        return false;
    }
}