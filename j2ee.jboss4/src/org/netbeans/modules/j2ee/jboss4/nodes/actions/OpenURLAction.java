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

import java.net.MalformedURLException;
import java.net.URL;
import org.openide.ErrorManager;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Action which displays selected web module in browser.
 *
 * @author Michal Mocnak
 */
public class OpenURLAction extends NodeAction {
    
    protected void performAction(Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            OpenURLActionCookie oCookie = (OpenURLActionCookie)nodes[i].getCookie(OpenURLActionCookie.class);
            
            if(oCookie != null) {
                try {
                    URLDisplayer.getDefault().showURL(new URL(oCookie.getWebURL()));
                } catch (MalformedURLException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
        }
    }
    
    protected boolean enable(Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            OpenURLActionCookie oCookie = (OpenURLActionCookie)nodes[i].getCookie(OpenURLActionCookie.class);
            UndeployModuleCookie uCookie = (UndeployModuleCookie)nodes[i].getCookie(UndeployModuleCookie.class);
            UndeployModuleCookie upCookie = (UndeployModuleCookie)nodes[i].getParentNode().getCookie(UndeployModuleCookie.class);
            
            if(uCookie != null)
                if(uCookie.isRunning())
                    return false;
            
            if(upCookie != null)
                if(upCookie.isRunning())
                    return false;
            
            if (oCookie != null)
                return true;
        }
        
        return false;
    }
    
    public String getName() {
        return NbBundle.getMessage(OpenURLAction.class, "LBL_OpenInBrowserAction"); // NOI18N
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}