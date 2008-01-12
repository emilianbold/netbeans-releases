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

package org.netbeans.modules.bpel.mapper.multiview;

import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.bpel.model.api.Expression;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.api.To;
import org.netbeans.modules.soa.ui.nodes.InstanceRef;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 * @author Vitaly Bychkov
 */
public class BpelDesignContextFactory {

    private static final BpelDesignContextFactory INSTANCE = new BpelDesignContextFactory();
    private final ContextCreator[] contextCreators;
            
    private BpelDesignContextFactory() {
        contextCreators = new ContextCreator[] {new AssignContextCreator(), 
                                                 new DefaultContextCreator()};
    }
    
    public static BpelDesignContextFactory getInstance() {
        return INSTANCE;
    }
    
    public BpelDesignContext createBpelDesignContext(
                    BpelEntity selectedEntity, Node node, Lookup lookup) 
    {
        if (selectedEntity == null || node == null || lookup == null) {
            return null;
        }
        
        BpelDesignContext context = null;
        
        assert contextCreators != null;
        for (BpelDesignContextFactory.ContextCreator contextCreator : contextCreators) {
            if (contextCreator.accepted(selectedEntity)) {
                context = contextCreator.create(selectedEntity, node, lookup);
                break;
            }
        }

        return context;
    }
    
    public BpelDesignContext getActivatedContext(BpelModel currentBpelModel) {
        if (currentBpelModel == null) {
            return null;
        }
        
        Node[] nodes = TopComponent.getRegistry().getActivatedNodes();
        if (nodes == null || nodes.length != 1) {
            return null;
        }
        BpelEntity bpelEntity = null;
        if (nodes[0] instanceof InstanceRef) {
            Object entity = ((InstanceRef) nodes[0]).getReference();
            if (entity instanceof BpelEntity 
                    && currentBpelModel.equals(((BpelEntity)entity).getBpelModel())) 
            {
                bpelEntity = (BpelEntity)entity;
            }
        } else {
            return null;
        }
        
        Lookup lookup = nodes[0].getLookup();
        BpelDesignContext bpelContext = 
                createBpelDesignContext(bpelEntity, nodes[0], lookup);
        return bpelContext;
    }

    private interface ContextCreator {
        boolean accepted(BpelEntity selectedEntity);
        BpelDesignContext create(BpelEntity selectedEntity, Node node, Lookup lookup);
    }
    
    private class AssignContextCreator implements ContextCreator {

        /**
         * 
         * @param selectedEntity - the selected bpel entity to show mapper
         * @return true if selected Entity is Assign or Assign bpel descendant - Copy, From or To
         */
        public boolean accepted(BpelEntity selectedEntity) {
            if (selectedEntity == null) {
                return false;
            }
            
            boolean accept = false;
            Class<? extends BpelEntity> entityType = selectedEntity.getElementType();
            if (entityType == Assign.class) {
                accept = true;
            } else if (entityType == Copy.class) {
                BpelEntity parent = selectedEntity.getParent();
                accept = parent != null && parent.getElementType() == Assign.class;
            } else if (entityType == From.class || entityType ==  To.class) {
                BpelEntity parent = selectedEntity.getParent();
                accept = parent != null && parent.getElementType() == Copy.class;
                if (accept) {
                    parent = parent.getParent();
                    accept = parent != null && parent.getElementType() == Assign.class;
                }
            }
            
            return accept;
        }

        // TODO m
        public BpelDesignContext create(BpelEntity selectedEntity, Node node, Lookup lookup) {
            if (!accepted(selectedEntity)) {
                return null;
            }
            
            BpelDesignContext context =  null;
            if (selectedEntity instanceof Assign) {
                context = new AssignBpelDesignContext((Assign)selectedEntity, node, lookup);
            } else if (selectedEntity instanceof Copy) {
                context = new AssignBpelDesignContext((Copy)selectedEntity, node, lookup);
            } else if (selectedEntity instanceof Expression) {
                context = new AssignBpelDesignContext((Expression)selectedEntity, node, lookup);
            } 
            
            return context;
        }
    }
    
    private class DefaultContextCreator implements ContextCreator {

        public boolean accepted(BpelEntity selectedEntity) {
            return selectedEntity != null;
        }

        // TODO m
        public BpelDesignContext create(BpelEntity selectedEntity, Node node, Lookup lookup) {
            if (!accepted(selectedEntity)) {
                return null;
            }
            return new SimpleBpelDesignContext(selectedEntity, node, lookup);
        }
    }
    

}
