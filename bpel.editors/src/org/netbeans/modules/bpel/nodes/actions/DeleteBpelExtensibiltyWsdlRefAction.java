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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.bpel.nodes.actions;

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
