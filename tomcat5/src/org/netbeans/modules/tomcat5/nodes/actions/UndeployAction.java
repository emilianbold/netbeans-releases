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

package org.netbeans.modules.tomcat5.nodes.actions;

import org.netbeans.modules.tomcat5.nodes.TomcatWebModuleNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author  Petr Pisl
 */
public class UndeployAction extends NodeAction {

    /** Creates a new instance of Undeploy */
    public UndeployAction() {
    }


    public String getName() {
        return NbBundle.getMessage(UndeployAction.class, "LBL_UndeployAction"); //NOI18N
    }

    protected void performAction(org.openide.nodes.Node[] nodes) {
        for (int i=0; i<nodes.length; i++) {
            TomcatWebModuleCookie cookie = (TomcatWebModuleCookie)nodes[i].getCookie(TomcatWebModuleCookie.class);
            if (cookie != null)
                cookie.undeploy();
        }
    }
    
    
    
    protected boolean asynchronous() { return false; }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return null;
    }
    
    protected boolean enable(Node[] activatedNodes) {
        return true;
    }
    
}
