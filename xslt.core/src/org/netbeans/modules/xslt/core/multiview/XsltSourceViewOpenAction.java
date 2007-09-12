/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.xslt.core.multiview;

import org.netbeans.modules.xslt.core.XSLTDataEditorSupport;
import org.netbeans.modules.xslt.core.XSLTDataObject;
import org.openide.actions.OpenAction;
import org.openide.cookies.OpenCookie;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 */
public class XsltSourceViewOpenAction extends OpenAction {
    private static final long serialVersionUID = 1L;

    @Override
    public String getName() {
        return NbBundle.getMessage(XsltSourceViewOpenAction.class,
                "XsltSourceViewOpenAction_Name");
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes == null || activatedNodes[0] == null) {
            return;
        }
        XSLTDataObject dObj = activatedNodes[0].getLookup().lookup(
                XSLTDataObject.class);
        if (dObj != null) {
            XSLTDataEditorSupport editorSupport = dObj.getEditorSupport();
            if ( editorSupport.getOpenedPanes()==null ||
                    editorSupport.getOpenedPanes().length==0 ) 
            {
                editorSupport.edit();
                XsltMultiViewSupport support = 
                    XsltMultiViewSupport.getInstance();
                support.requestViewOpen(editorSupport);
            } else {
                editorSupport.edit();
            }
            return;
        }
        // default to open cookie
        OpenCookie oc = activatedNodes[0].getCookie(OpenCookie.class);
        if (oc != null) {
            oc.open();
        }
    }
}
