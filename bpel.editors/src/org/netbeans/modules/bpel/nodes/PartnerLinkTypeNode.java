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
package org.netbeans.modules.bpel.nodes;

import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.editors.api.nodes.actions.ActionType;
import org.netbeans.modules.bpel.nodes.synchronizer.ModelSynchronizer;
import org.netbeans.modules.bpel.nodes.synchronizer.SynchronisationListener;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

public class PartnerLinkTypeNode extends BpelNode<PartnerLinkType> 
        implements SynchronisationListener {

    private ModelSynchronizer synchronizer;

    public PartnerLinkTypeNode(PartnerLinkType reference, Children children, Lookup lookup) {
        super(reference, children, lookup);
        suscribeWSDLSynchronizer(reference);
    }

    public PartnerLinkTypeNode(PartnerLinkType reference, Lookup lookup) {
        super(reference, lookup);
        suscribeWSDLSynchronizer(reference);
    }
    
    public NodeType getNodeType() {
        return NodeType.PARTNER_LINK_TYPE;
    }

    // TODO m
    protected void suscribeWSDLSynchronizer(PartnerLinkType component) {
        if (component == null) {
            return;
        }
        
        WSDLModel model = component.getModel();
        if (model != null) {
            synchronizer = new ModelSynchronizer(this);
            synchronizer.subscribe(model);
        }
    }
    
    public void componentUpdated(org.netbeans.modules.xml.xam.Component component) {
        if (component.equals(getReference())) {
            updateName();
            updateAllProperties();
        }
    }

    public void childrenUpdated(org.netbeans.modules.xml.xam.Component component) {
        // do nothing
    }

    protected ActionType[] getActionsArray() {
        return new ActionType[] {
            ActionType.DELETE_BPEL_EXT_FROM_WSDL
        };
    }

}
