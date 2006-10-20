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

package org.netbeans.modules.identity.server.manager.ui.actions;

import java.net.URL;
import org.netbeans.modules.identity.server.manager.ui.ServerInstanceNode;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 * Action for bringing up the AM Admin Console via a web browser.
 * 
 * Created on June 14, 2006, 4:48 PM
 *
 * @author ptliu
 */
public class ViewAdminConsoleAction extends CookieAction {
    
    private static final String HELP_ID = "idmtools_acessing_am_console"; //NOI18N
    
    /** Creates a new instance of ViewAdminConsoleAction */
    public ViewAdminConsoleAction() {
    }
    
    protected int mode() {
        return MODE_EXACTLY_ONE;
        // return MODE_ALL;
    }
    
    protected void performAction(Node[] nodes) {
         if( (nodes == null) || (nodes.length < 1) )
             return ;
    
        //TODO: Need to use Lookup
        try{
            ServerInstanceNode instanceNode = (ServerInstanceNode) nodes[0].getLookup().lookup(ServerInstanceNode.class);
            URLDisplayer.getDefault().showURL(new URL(instanceNode.getAdminURL()));
        } catch (Exception e){
            e.printStackTrace();
            return;//nothing much to do
        }
    }
    
    protected boolean enable(Node[] activatedNodes) {
        return true;
    }
    
    public String getName() {
        return NbBundle.getMessage(ViewAdminConsoleAction.class, "LBL_ViewAdminConsole");
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(HELP_ID);
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    protected Class[] cookieClasses() {
        return new Class[] {};
    }
    
}
