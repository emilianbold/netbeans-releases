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
package org.netbeans.modules.bpel.nodes.actions;

import org.netbeans.modules.bpel.editors.api.nodes.actions.ActionType;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.nodes.BpelWSDLNode;
import org.netbeans.modules.bpel.nodes.PartnerLinkTypeNode;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELExtensibilityComponent;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class DeleteBpelExtensibiltyWsdlRefAction  extends DeleteAction {
    private static final long serialVersionUID = 1L;
    
    public ActionType getType() {
        return ActionType.DELETE_BPEL_EXT_FROM_WSDL;
    }
    
    public void performAction(Node[] nodes) {
        if (!enable(nodes)) {
            return;
        }
        
        Object ref = ((BpelNode)nodes[0]).getReference();
        assert ref instanceof BPELExtensibilityComponent;
        WSDLModel wsdlModel = ((BPELExtensibilityComponent)ref).getModel();
        if (wsdlModel == null) {
            return;
        }
        Definitions defs = wsdlModel.getDefinitions();
        if (defs == null) {
            return;
        }
        
        try {
            wsdlModel.startTransaction();
            defs.removeExtensibilityElement((BPELExtensibilityComponent)ref);
            // ?
//            wsdlModel.prepareSync();
        } finally {
            wsdlModel.endTransaction();
        }

// TODO add it later
//        NotifyDescriptor.Confirmation descriptor =
//                new NotifyDescriptor.Confirmation("Are you sure you want to delete?"
//                , "Delete Action Confirmation");
//        Object o = DialogDisplayer.getDefault().notify(descriptor);
//        
//        if (o.equals(NotifyDescriptor.OK_OPTION)) {
//            try {
//                wsdlModel.startTransaction();
//                defs.removeExtensibilityElement((BPELExtensibilityComponent)ref);
//                // ?
//                wsdlModel.prepareSync();
//            } finally {
//                wsdlModel.endTransaction();
//            }
//        }
    }
    
    public boolean enable(final Node[] nodes) {
        boolean isEnable = true;
        isEnable = nodes != null
                && nodes.length == 1
                && (nodes[0] instanceof BpelWSDLNode
                || nodes[0] instanceof PartnerLinkTypeNode);
        if (! isEnable) {
            return isEnable;
        }
        
        Object ref = ((BpelNode)nodes[0]).getReference();
        assert ref instanceof BPELExtensibilityComponent;
        WSDLModel wsdlModel = ((BPELExtensibilityComponent)ref).getModel();
        if (wsdlModel == null) {
            return false;
        }
        
//        System.out.println("wsdlModel state: "+(wsdlModel.getState()));
        isEnable = WSDLModel.State.VALID.equals(wsdlModel.getState()) 
            && XAMUtils.isWritable(wsdlModel);
        return isEnable;
    }
}
