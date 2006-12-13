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

import org.openide.cookies.EditorCookie;
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

    protected Class[] cookieClasses() {
        return new Class[] { /* Retouche: ClassMember.class */};
    }

    protected boolean asynchronous() {
        return false;
    }

    protected boolean enable(Node[] activatedNodes) {
        boolean result = false;
        if (activatedNodes != null && activatedNodes.length == 1 && activatedNodes[0] != null) {
            EditorCookie cookie = (EditorCookie)activatedNodes[0].getCookie(EditorCookie.class);
            if (cookie!=null && "text/x-jsp".equals(cookie.getDocument().getProperty("mimeType"))) { //NOI18N
                return true;
            } else if (cookie!=null && "text/x-java".equals(cookie.getDocument().getProperty("mimeType"))) {
                // result = (JMIUtils.getCallableFeatureFromNode(activatedNodes[0]) != null); // Retouche
                return true;
            }
        }
        return result;
    }

    protected void performAction(Node[] activatedNodes) {
        if(activatedNodes != null && activatedNodes[0] != null) {
        }
    }
    
}
