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
package org.netbeans.modules.xslt.tmap.nodes;

import org.netbeans.modules.soa.ui.nodes.ReflectionNodeFactory;
import org.netbeans.modules.xslt.tmap.model.api.Invoke;
import org.netbeans.modules.xslt.tmap.model.api.Param;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class NavigatorNodeFactory extends ReflectionNodeFactory<NodeType> {

    private static NavigatorNodeFactory INSTANCE = new NavigatorNodeFactory();
    
    public static NavigatorNodeFactory getInstance() {
        return INSTANCE;
    }
    
    private NavigatorNodeFactory() {
        super(6);
        //
        key2Class.put(NodeType.TRANSFORMMAP, TransformMapNode.class);
        key2Class.put(NodeType.SERVICE, ServiceNode.class);
        key2Class.put(NodeType.OPERATION, OperationNode.class);
        key2Class.put(NodeType.INVOKE, InvokeNode.class);
        key2Class.put(NodeType.TRANSFORM, TransformNode.class);
        key2Class.put(NodeType.PARAM, ParamNode.class);
    }

    public Node createNode(TMapComponent entity, Lookup lookup) {
        Node node = null;
        NodeType nodeType = NodeType.getNodeType(entity);
        if (nodeType != null) {
            node = createNode(nodeType, entity, lookup);
        }
        return node;
    }
    
    public Node getTransformMapNode(TMapModel model, Lookup lookup) {
        assert model != null && lookup != null;
        return createNode(NodeType.TRANSFORMMAP, model.getTransformMap(), lookup);
    }
    
    @Override
    public Node createNode(NodeType nodeType, Object ref, Lookup lookup) {
        if (nodeType == null || ref == null || lookup == null) {
            return null;
        }
        

//        assert nodeType != null && ref != null && lookup != null;
        if (NodeType.UNKNOWN_TYPE.equals(nodeType)) {
            return createDefaultNode(ref, lookup);
        }
        
        Node node = null;
        switch (nodeType) {
            case TRANSFORMMAP:
//            assert ref instanceof TransformMap
//                    : "reference should be TransformMap type to create TransformMap type Node"; // NOI18N
//                node = super.createNode(nodeType,ref,
//                        new TransformMapChildren((TransformMap)ref,lookup),lookup);
//            break;
            case SERVICE:
//            assert ref instanceof Service
//                    : "reference should be Service type to create Service type Node"; // NOI18N
//                node = super.createNode(nodeType,ref,
//                        new ServiceChildren((Service)ref,lookup),lookup);
//            break;
            case OPERATION:
            case TRANSFORM:
//            assert ref instanceof Operation
//                    : "reference should be Operation type to create Operation type Node"; // NOI18N
//                node = super.createNode(nodeType,ref,
//                        new OperationChildren((Operation)ref,lookup),lookup);
                assert ref instanceof TMapComponent 
                        : "reference should be TMapComponent type to create TMapComponent type Node"; // NOI18N
                node = super.createNode(nodeType, ref, new TMapComponentNodeChildrenImpl((TMapComponent) ref, lookup), lookup);
                break;
            case PARAM:
                assert ref instanceof Param 
                        : "reference should be Param type to create Param type Node"; // NOI18N
                node = super.createNode(nodeType, ref, Children.LEAF, lookup);
                break;
            case INVOKE:
                assert ref instanceof Invoke 
                        : "reference should be Invoke type to create Invoke type Node"; // NOI18N
                node = super.createNode(nodeType, ref, Children.LEAF, lookup);
                break;
        }
        
        return node;
    }

    @Override
    public Node createNode(NodeType nodeType, Object ref, Children children, Lookup lookup) {
        return super.createNode(nodeType, ref, children, lookup);
    }
    
    // TODO add impl for default node
    private Node createDefaultNode(Object ref, Lookup lookup) {
        return null;
    } 
    
}
