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


package org.netbeans.modules.bpel.nodes.navigator;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.CorrelationSetContainer;
import org.netbeans.modules.bpel.model.api.If;
import org.netbeans.modules.bpel.model.api.MessageExchangeContainer;
import org.netbeans.modules.bpel.model.api.VariableContainer;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 11 April 2006 
 *
 */
public class Util {
    
    private Util() {
    }

    public static List<BaseScope> getClosestBaseScopes(List<BpelEntity> bpelEntities) {
        if (bpelEntities == null || bpelEntities.size() < 1) {
            return null;
        }   
        List<BaseScope> scopes = new ArrayList<BaseScope>();
        for (Object entity : bpelEntities) {
            if (entity instanceof BaseScope) {
                scopes.add((BaseScope)entity);
            } else {
                if (entity instanceof BpelContainer) {
                    List<BaseScope> tmpScope = getClosestBaseScopes(((BpelContainer)entity).getChildren());
                    if (tmpScope != null && tmpScope.size() > 0) {
                        scopes.addAll(tmpScope);
                    }
                }
            }
        }
        return scopes;
        
//////        if (scopes.size() > 0) {
//////            return scopes;
//////        }
//////        
//////        List<BpelEntity> childOneLevelEntities = new ArrayList<BpelEntity>();
//////        for (Object entity : bpelEntities) {
//////            if (entity instanceof BpelContainer) {
//////                List<BpelEntity> childs = ((BpelContainer)entity).getChildren();
//////                if (childs != null && childs.size() > 0) {
//////                    childOneLevelEntities.addAll(childs);
//////                }
//////            }
//////        }
//////        return getClosestBaseScopes(childOneLevelEntities);
    }
    
    public static BaseScope getUpClosestBaseScope(BpelEntity bpelEntity) {
        if (bpelEntity == null) {
            return null;
        }
        BaseScope scope = null;
        BpelContainer bpelContainer = bpelEntity.getParent();
        if (bpelContainer instanceof BaseScope) {
            scope = (BaseScope)bpelContainer;
        }
        
        if (scope != null) {
            return scope;
        }
        
        return getUpClosestBaseScope(bpelContainer);
    }
    
    public static BpelNode findBpelNode(Node parentNode, Object entity) {
        if (parentNode == null || entity == null 
                || !(parentNode instanceof BpelNode) 
                || !(entity instanceof BpelEntity)) 
        {
            return null;
        }
        return findBpelNode((BpelNode)parentNode, getEntitiesPath((BpelEntity)entity));
    }

    private static List<BpelEntity> getEntitiesPath(BpelEntity entity) {
        if (entity == null) {
            return null;
        }
        List<BpelEntity> entitiesPath = new ArrayList<BpelEntity>();
        entitiesPath.add(entity);
        BpelEntity parentEntity = null;
        while((parentEntity = entity.getParent()) != null) {
            entitiesPath.add(parentEntity);
            // TODO m
            // hack to support then subnodes selection...
            if (parentEntity instanceof If
                    && entity instanceof Activity) {
                entitiesPath.add(parentEntity);
            }
            entity = parentEntity;
        }
        
        return entitiesPath;
    }
    
    private static BpelNode findBpelNode(BpelNode parentNode, List<BpelEntity> path) {
        if (parentNode == null || path == null || path.size() < 1) {
            return null;
        }
        if (path.size() == 1 && parentNode.getReference().equals(path.get(0))) {
            return parentNode;
        }
        BpelNode resultNode = null;
        for (int i = path.size() -2 ; i >= 0; i--) {
            BpelNode tmpNode = findChildBpelNode(parentNode,path.get(i));
            if (tmpNode != null && i != 0) {
                parentNode = tmpNode;
            } else if (tmpNode != null && i == 0) {
                resultNode = tmpNode;
            }
        }

        return resultNode;
    }
    
    private static BpelNode findChildBpelNode(BpelNode parentNode
            , BpelEntity entity) 
    {
        if (parentNode == null || entity == null || parentNode.getChildren() == null) {
            return null;
        }
        Node[] childNodes = parentNode.getChildren().getNodes();
        if (childNodes == null || childNodes.length < 1) {
            return null;
        }
        for (Node childNode : childNodes) {
            
            if (childNode instanceof BpelNode) {
                BpelNode childBpelNode = (BpelNode)childNode;
                
                // VariableContainerNode associated with parentEntity
                if (entity instanceof VariableContainer
                    && childBpelNode.getNodeType().equals(NodeType.VARIABLE_CONTAINER)
                    && childBpelNode.getReference().equals(entity.getParent())) 
                {
                    return childBpelNode;
                }

                // CorrelationSetContainerNode associated with parentEntity
                if (entity instanceof CorrelationSetContainer
                    && childBpelNode.getNodeType().equals(NodeType.CORRELATION_SET_CONTAINER)
                    && childBpelNode.getReference().equals(entity.getParent())) 
                {
                    return childBpelNode;
                }

                // MessageExchangeContainerNode associated with parentEntity
                if (entity instanceof MessageExchangeContainer
                    && childBpelNode.getNodeType().equals(NodeType.MESSAGE_EXCHANGE_CONTAINER)
                    && childBpelNode.getReference().equals(entity.getParent())) 
                {
                    return childBpelNode;
                }

                Object childEntity = childBpelNode.getReference();
                
                if (childEntity != null && childEntity.equals(entity)) {
                    return childBpelNode;
                }
            }
        }
        
        return null;
    }
}
