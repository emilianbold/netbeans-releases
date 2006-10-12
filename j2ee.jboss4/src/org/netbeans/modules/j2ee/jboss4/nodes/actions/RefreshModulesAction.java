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

import org.netbeans.modules.j2ee.jboss4.nodes.JBItemNode;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Michal Mocnak
 */
public class RefreshModulesAction extends NodeAction {
    
    /** Creates a new instance of Undeploy */
    public RefreshModulesAction() {
    }
    
    protected boolean enable(org.openide.nodes.Node[] nodes) {
        RefreshModulesCookie cookie;
        for (int i=0; i<nodes.length; i++) {
            cookie = (RefreshModulesCookie)nodes[i].getCookie(RefreshModulesCookie.class);
            if (cookie == null)
                return false;
            
        }
        
        return true;
    }    
    
    public String getName() {
        return NbBundle.getMessage(JBItemNode.class, "LBL_RefreshModulesAction"); // NOI18N
    }
    
    protected void performAction(org.openide.nodes.Node[] nodes) {
        for (int i=0; i<nodes.length; i++) {
            RefreshModulesCookie cookie = (RefreshModulesCookie)nodes[i].getCookie(RefreshModulesCookie.class);
            if (cookie != null)
                cookie.refresh();
        }
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}