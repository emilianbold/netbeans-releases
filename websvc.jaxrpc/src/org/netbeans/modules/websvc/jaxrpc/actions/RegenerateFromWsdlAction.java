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

package org.netbeans.modules.websvc.jaxrpc.actions;

import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.modules.websvc.jaxrpc.nodes.WebServiceNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 * @author  rico
 */
public class RegenerateFromWsdlAction extends CookieAction{
    
    /** Creates a new instance of RegenerateFromWsdlAction */
    public RegenerateFromWsdlAction() {
    }
    
    public String getName() {
        return NbBundle.getMessage(RegenerateFromWsdlAction.class, "LBL_RegenerateFromWsdlAction");
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        
    }
    
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }
    
    protected Class[] cookieClasses() {
        return new Class[] {};
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    protected boolean enable(Node[] activatedNodes) {
        if(activatedNodes.length == 1){
            Node n = activatedNodes[0];
            WebServiceNode wsNode = (WebServiceNode)n.getLookup().lookup(WebServiceNode.class);
            if(wsNode != null){
                WebServicesSupport support = wsNode.getWebServicesSupport();
                return support.isFromWSDL(wsNode.getName());
            }
        }
        return false;
    }
    
    
    protected void performAction(Node[] activatedNodes) {
        final RegenerateFromWsdlCookie cookie =
        (RegenerateFromWsdlCookie)activatedNodes[0].getCookie(RegenerateFromWsdlCookie.class);
        if(cookie != null){
            cookie.regenerate();
        }
        
    }
    
}
