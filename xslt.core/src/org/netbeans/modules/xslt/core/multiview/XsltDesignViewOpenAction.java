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
package org.netbeans.modules.xslt.core.multiview;

import org.netbeans.modules.xslt.core.XSLTDataEditorSupport;
import org.netbeans.modules.xslt.core.XSLTDataObject;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Vitaly Bychkov
 */
public class XsltDesignViewOpenAction extends NodeAction {

    private static final long serialVersionUID = 1L;

    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes == null || activatedNodes[0] == null) {
            return;
        }
        XSLTDataObject dObj = activatedNodes[0].getLookup().
                                                lookup(XSLTDataObject.class);
        if (dObj != null) {
            XSLTDataEditorSupport editorSupport = dObj.getEditorSupport();
            if ( editorSupport.getOpenedPanes()==null ||
                    editorSupport.getOpenedPanes().length==0 ) 
            {
                editorSupport.open();
                XsltMultiViewSupport support = 
                    XsltMultiViewSupport.getInstance();
                support.requestViewOpen(editorSupport);
            } else {
                editorSupport.open();
            }
            return;
        }
        // default to edit cookie
//        EditCookie ec = activatedNodes[0].getCookie(EditCookie.class);
//        if (ec != null) {
//            ec.edit();
//        }
    }

    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes == null || activatedNodes[0] == null) {
            return false;
        }
        XSLTDataObject dObj = activatedNodes[0].getLookup().
                                                lookup(XSLTDataObject.class);
        return dObj != null;
    }

    public String getName() {
        return NbBundle.getMessage(XsltDesignViewOpenAction.class,
                "XsltDesignViewOpenAction_Name");
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
	protected boolean asynchronous() {
        return false;
    }
}
