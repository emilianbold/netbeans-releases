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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.navigation.hierarchy;

import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.loaders.CCDataObject;
import org.netbeans.modules.cnd.loaders.HDataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 *
 * @author Alexander Simon
 */
public class ClassHierarchyPopupAction extends CookieAction {

    protected void performAction(Node[] activatedNodes) {
        CsmClass decl = ContextUtils.getContextClass(activatedNodes);
        if (decl != null){
            HierarchyDialog.show(decl);
        }
    }

    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    public String getName() {
        return NbBundle.getMessage(ClassHierarchyPopupAction.class, "CTL_ClassHierarchyPopupAction");
    }

    protected Class[] cookieClasses() {
        return new Class[]{CCDataObject.class, HDataObject.class};
    }

    @Override
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE);
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
