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
package org.netbeans.modules.xml.catalog;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.openide.awt.StatusDisplayer;

/**
 * Action sensitive to the node selection that refreshs children.
 *
 * @author  Petr Kuzel
 */
final class RefreshAction extends CookieAction {

    /** Serial Version UID */
    private static final long serialVersionUID =4798470042774935554L;

    protected void performAction (Node[] nodes) {
        if (nodes == null) return;
        try {
            for (int i = 0; i<nodes.length; i++) {
                String msg = Util.THIS.getString("MSG_refreshing", nodes[i].getDisplayName());
                StatusDisplayer.getDefault().setStatusText(msg);
                Refreshable cake = (Refreshable) nodes[i].getCookie(Refreshable.class);
                cake.refresh();
            }
        } finally {
            String msg = Util.THIS.getString("MSG_refreshed");
            StatusDisplayer.getDefault().setStatusText(msg);
        }
    }

    public String getName () {
        return Util.THIS.getString ("LBL_Action");
    }

    protected String iconResource () {
        return null;
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx(getClass());
    }

    protected Class[] cookieClasses() {
        return new Class[] {Refreshable.class};
    }
    
    protected int mode() {
        return CookieAction.MODE_ALL;
    }
    
    protected boolean asynchronous() {
        return false;
    }

}
