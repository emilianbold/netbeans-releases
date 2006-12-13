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

import org.openide.util.actions.CookieAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;

public class WSRegisterAction extends CookieAction {
    public String getName() {
        return NbBundle.getMessage(WSRegisterAction.class, "LBL_WSRegisterAction");
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
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
        return activatedNodes.length == 1;
    }
    
    
    protected void performAction(Node[] activatedNodes) {
       final WSRegisterCookie cookie = 
            (WSRegisterCookie)activatedNodes[0].getCookie(WSRegisterCookie.class); 
       if(cookie != null){
           cookie.registerWebService();
       }
    }
}
