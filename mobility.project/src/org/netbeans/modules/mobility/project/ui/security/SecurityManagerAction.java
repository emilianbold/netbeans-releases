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

package org.netbeans.modules.mobility.project.ui.security;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;



public class SecurityManagerAction extends CallableSystemAction {
    
    public SecurityManagerAction() {
        super();
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    public void performAction() {
        SecurityManagerPanel.showSecurityManager(null, null);
    }
    
    public String getName() {
        return NbBundle.getMessage(SecurityManagerAction.class, "NAME_SecurityManagerAction"); // NOI18N
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(SecurityManagerAction.class);
    }
    
    protected boolean asynchronous() {
        return false;
    }
}
