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
package org.netbeans.modules.bpel.editors.multiview;

import org.netbeans.modules.bpel.core.BPELDataEditorSupport;
import org.netbeans.modules.bpel.core.BPELDataObject;
import org.netbeans.modules.bpel.editors.api.EditorUtil;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Process;
import org.openide.cookies.OpenCookie;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class BpelDesignViewOpenAction extends NodeAction {
    private static final long serialVersionUID = 1L;

    @Override
    public String getName() {
        return NbBundle.getMessage(BpelSourceViewOpenAction.class,
                "BpelDesignViewOpenAction_Name");
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes == null || activatedNodes[0] == null) {
            return;
        }
        BPELDataObject dObj = activatedNodes[0].getLookup().lookup(
                BPELDataObject.class);
        if (dObj != null) {
            BPELDataEditorSupport editorSupport = dObj.getEditorSupport();
            BpelModel model = null;
            if ( editorSupport != null) {
                model = editorSupport.getBpelModel();
            }
            
            Process process = null;
            if (model != null && BpelModel.State.VALID.equals(model.getState())) {
                process = model.getProcess();
            }
            
            if (process != null) {
                if (editorSupport.getOpenedPanes() == null ||
                        editorSupport.getOpenedPanes().length == 0) {
                    EditorUtil.goToDesign(process);
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

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes == null 
                || activatedNodes.length < 1 
                || activatedNodes[0] == null) 
        {
            return false;
        }
        BPELDataObject dObj = activatedNodes[0].getLookup().lookup(BPELDataObject.class);
        return dObj != null;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
