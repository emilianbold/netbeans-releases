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
package org.netbeans.modules.bpel.nodes.refactoring;

import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.nodes.navigator.NavigatorNodeFactory;
import org.netbeans.modules.bpel.properties.PropertyNodeFactory;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELExtensibilityComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Query;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public abstract class RefactoringNodeCreatorAbstract
        implements RefactoringNodeCreator {

    public RefactoringNodeCreatorAbstract() {
    }
    
    protected Node getInternalNode(UsageNodeType nodeType, Object reference) {
        if (!isSupported(nodeType)) {
            return null;
        }
        
        PropertyNodeFactory propertyNodeFactory = PropertyNodeFactory
                .getInstance();
        Node node = null;
        NodeType bpelNodeType = null;
        if (reference instanceof BPELExtensibilityComponent) {
            bpelNodeType = getBpelExtensionElementNodeType(
                        (BPELExtensibilityComponent)reference
                    );
        } else if (reference instanceof BpelEntity) {
            bpelNodeType = 
                    org.netbeans.modules.bpel.editors.api.utils.
                        Util.getBasicNodeType((BpelEntity) reference);
            if (bpelNodeType == NodeType.VARIABLE_CONTAINER 
                    || bpelNodeType == NodeType.VARIABLE_CONTAINER 
                    || bpelNodeType == NodeType.CORRELATION_SET_CONTAINER
                    || bpelNodeType == NodeType.MESSAGE_EXCHANGE_CONTAINER ) 
            {
                reference = ((BpelEntity)reference).getParent();
            }
            
            if (bpelNodeType == null || bpelNodeType == NodeType.UNKNOWN_TYPE) {
                bpelNodeType = NodeType.DEFAULT_BPEL_ENTITY_NODE;
            }
        }
//        
//        if (! (reference instanceof BpelEntity)) {
//            if (reference instanceof BPELExtensibilityComponent) {
//                NodeType bpelExtNodeType = 
//                        getBpelExtensionElementNodeType((BPELExtensibilityComponent)reference);
//                if (bpelExtNodeType != null) {
//                    node = propertyNodeFactory.createNode(
//                            bpelExtNodeType,reference,null,
//                            Children.LEAF, Lookup.getDefault());
//                }
//            }
//            
//            return node;
//        }
//        
//        org.netbeans.modules.bpel.design.nodes.NodeType bpelNodeType =
//                NavigatorNodeFactory.getBasicNodeType((BpelEntity) reference);
        
        if (bpelNodeType != null) {
            
            node = propertyNodeFactory.createNode(
                    bpelNodeType, reference, 
                    Children.LEAF, Lookup.getDefault());
//            if (bpelNodeType == org.netbeans.modules.bpel.design.nodes
//                                     .NodeType.UNKNOWN_TYPE) 
//            {
//                node = propertyNodeFactory.createNode(
//                        bpelNodeType,reference,null,
//                        Children.LEAF, Lookup.getDefault());
//            } else {
//            node = propertyNodeFactory.createNode(
//                    bpelNodeType,reference,null,
//                    Children.LEAF, Lookup.getDefault());
//            }
        }
        return node;
    }
    
    private NodeType getBpelExtensionElementNodeType(BPELExtensibilityComponent component) {
        if (component instanceof PropertyAlias) {
            return NodeType.CORRELATION_PROPERTY_ALIAS;
        }

        if (component instanceof PartnerLinkType) {
            return NodeType.PARTNER_LINK_TYPE;
        }
        
        if (component instanceof CorrelationProperty) {
            return NodeType.CORRELATION_PROPERTY;
        }
        
        if (component instanceof Role) {
            return NodeType.PARTNER_ROLE;
        }
        
        if (component instanceof Query) {
            return NodeType.QUERY;
        }
        
        return NodeType.UNKNOWN_TYPE;
    }
}
