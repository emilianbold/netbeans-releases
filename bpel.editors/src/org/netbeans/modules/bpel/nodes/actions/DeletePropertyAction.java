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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.nodes.CorrelationPropertyNode;
import org.netbeans.modules.bpel.nodes.CorrelationSetNode;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.openide.ErrorManager;
import org.openide.nodes.Node;

/**
 *
 * @author Vitaly Bychkov
 * @version 19 April 2006
 *
 */
public class DeletePropertyAction extends DeleteAction {
    private static final long serialVersionUID = 1L;
    
    public void performAction(Node[] nodes) {
        if (!enable(nodes)) {
            return;
        }
        //
        CorrelationPropertyNode corrPropNode = ((CorrelationPropertyNode)nodes[0]);
        final CorrelationProperty corrProperty = corrPropNode.getReference();
        if (corrProperty == null) {
            return;
        }
        //
        assert corrPropNode.getParentNode() instanceof CorrelationSetNode;
        CorrelationSetNode corrSetNode = (CorrelationSetNode)nodes[0].getParentNode();
        final CorrelationSet corrSet = corrSetNode.getReference();
        //
        BpelModel model = corrSet.getBpelModel();
        try {
            model.invoke(new Callable<Object>() {
                public Object call() {
                    List<WSDLReference<CorrelationProperty>> oldCorrPropRefList
                            = corrSet.getProperties();
                    assert oldCorrPropRefList != null;
                    List<WSDLReference<CorrelationProperty>> newCorrPropRefList =
                            new ArrayList<WSDLReference<CorrelationProperty>>
                            (oldCorrPropRefList);
                    // remove just first equals element
                    for (WSDLReference<CorrelationProperty> elem : oldCorrPropRefList) {
                        if (elem != null && elem.get().equals(corrProperty)) {
                            newCorrPropRefList.remove(elem);
                            break;
                        }
                    }
                    //
                    corrSet.setProperties(newCorrPropRefList);
                    return null;
                }
            }, this);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    public boolean enable(Node[] nodes) {
        return nodes != null
                && nodes.length == 1
                && nodes[0] instanceof CorrelationPropertyNode
                && nodes[0].getParentNode() instanceof CorrelationSetNode;
    }
}
