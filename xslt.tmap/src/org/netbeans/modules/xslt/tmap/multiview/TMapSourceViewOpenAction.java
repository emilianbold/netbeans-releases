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
package org.netbeans.modules.xslt.tmap.multiview;

import org.netbeans.modules.xslt.tmap.TMapDataEditorSupport;
import org.netbeans.modules.xslt.tmap.TMapDataObject;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.model.api.TransformMap;
import org.netbeans.modules.xslt.tmap.util.TMapUtil;
import org.openide.actions.OpenAction;
import org.openide.cookies.OpenCookie;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class TMapSourceViewOpenAction extends OpenAction {
    private static final long serialVersionUID = 1L;

    @Override
    public String getName() {
        return NbBundle.getMessage(TMapSourceViewOpenAction.class,
                "TMapSourceViewOpenAction_Name");
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes == null || activatedNodes[0] == null) {
            return;
        }
        TMapDataObject dObj = activatedNodes[0].getLookup().lookup(
                TMapDataObject.class);
        if (dObj != null) {
            TMapDataEditorSupport editorSupport = dObj.getEditorSupport();

            TMapModel model = null;
            if ( editorSupport != null) {
                model = editorSupport.getTMapModel();
            }
            
            TransformMap transformmap = null;
            if (model != null && TMapModel.State.VALID.equals(model.getState())) {
                transformmap = model.getTransformMap();
            }
            
            if (transformmap != null) {
                if (editorSupport.getOpenedPanes() == null ||
                        editorSupport.getOpenedPanes().length == 0) {
                    TMapUtil.goToSourceView(transformmap);
                } else {
                    editorSupport.open();
                }
                return;
            }
        }
        // default to open cookie
        OpenCookie oc = activatedNodes[0].getCookie(OpenCookie.class);
        if (oc != null) {
            oc.open();
        }
    }
}
