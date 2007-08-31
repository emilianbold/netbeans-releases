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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.navigation.hierarchy;

import org.netbeans.modules.cnd.navigation.classhierarchy.*;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

public final class ShowClassHierarchyAction extends CookieAction {
    
    protected void performAction(Node[] activatedNodes) {
        ContextUtils.getContextClass(activatedNodes);
        CsmClass decl = ContextUtils.getContextClass(activatedNodes);
        if (decl != null){
            HierarchyTopComponent view = HierarchyTopComponent.findInstance();
            view.setClass(decl);
            view.open();
            view.requestActive();
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length > 0){
            if (ContextUtils.USE_REFERENCE_RESOLVER) {
                CsmReference ref = ContextUtils.findReference(activatedNodes[0]);
                return ContextUtils.isSupportedReference(ref);
            }
            CsmDeclaration decl = ContextUtils.findDeclaration(activatedNodes[0]);
            if (decl != null && CsmKindUtilities.isClass(decl)){
                return true;
            }
        }
        return false;
    }
    
    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }
    
    public String getName() {
        return NbBundle.getMessage(getClass(), "CTL_ShowHierarchyAction"); // NOI18N
    }
    
    protected Class[] cookieClasses() {
        return new Class[] {
            EditorCookie.class
        };
    }
    
    @Override
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }
}

