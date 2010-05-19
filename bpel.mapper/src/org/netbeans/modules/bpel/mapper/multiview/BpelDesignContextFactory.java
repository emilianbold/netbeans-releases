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

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.bpel.model.api.ElseIf;
import org.netbeans.modules.bpel.model.api.ForEach;
import org.netbeans.modules.bpel.model.api.If;
import org.netbeans.modules.bpel.model.api.OnAlarmEvent;
import org.netbeans.modules.bpel.model.api.OnAlarmPick;
import org.netbeans.modules.bpel.model.api.RepeatUntil;
import org.netbeans.modules.bpel.model.api.Wait;
import org.netbeans.modules.bpel.model.api.While;
import org.netbeans.modules.soa.ui.nodes.InstanceRef;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 * @author Nikita Krjukov
 */
public class BpelDesignContextFactory implements DesignContextFactory {

    private static Set<Class> mMappableObjects = new HashSet<Class>();

    static {
        mMappableObjects.add(Assign.class);
        mMappableObjects.add(Copy.class);
        //
        mMappableObjects.add(Wait.class);
        mMappableObjects.add(OnAlarmPick.class);
        mMappableObjects.add(OnAlarmEvent.class);
        //
        mMappableObjects.add(If.class);
        mMappableObjects.add(ElseIf.class);
        mMappableObjects.add(While.class);
        mMappableObjects.add(RepeatUntil.class);
        //
        mMappableObjects.add(ForEach.class);
    }

    private static final BpelDesignContextFactory INSTANCE = new BpelDesignContextFactory();

    public static BpelDesignContextFactory getInstance() {
        return INSTANCE;
    }

    private BpelDesignContextFactory() {}

    //==========================================================================

    public BpelDesignContext createBpelDesignContext(
                    BpelEntity selectedEntity, Node node, Lookup lookup) {
        //
        if (selectedEntity == null) {
            return null;
        }
        //
        BpelDesignContext context = null;
        //
        // Initially set mapper's context entity to the selected entity.
        BpelEntity contextEntity = selectedEntity;
        while (contextEntity != null) {
            Class<? extends BpelEntity> entityType = contextEntity.getElementType();
            if (mMappableObjects.contains(entityType)) {
                // 
                // Special case only for the Copy entity
                if (entityType == Copy.class) {
                    BpelEntity parent = contextEntity.getParent();
                    if (parent != null && parent.getElementType() == Assign.class) {
                        contextEntity = parent;
                        context = new BpelDesignContextImpl(parent, selectedEntity,
                                node, lookup);
                    }
                } else {
                    context = new BpelDesignContextImpl(contextEntity,
                            selectedEntity, node, lookup);
                }
                break;
            }
            if (Activity.class.isAssignableFrom(entityType)) {
                // Break search because reaching another BPEL Activity
                // It means that the mapper isn't acceptable for the selected BPEL entity.
                break;
            }
            //
            // Go to parent entity
            contextEntity = contextEntity.getParent();
        }
        //
        if (context == null) {
            // Create an empty context
            context = new BpelDesignContextImpl(null, selectedEntity, node, lookup);
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

    public boolean isMappableEntity(BpelEntity entity) {
        //
        if (entity == null) {
            return false;
        }
        //
        BpelDesignContext context = null;
        //
        // Initially set mapper's context entity to the selected entity.
        BpelEntity contextEntity = entity;
        while (contextEntity != null) {
            Class<? extends BpelEntity> entityType = contextEntity.getElementType();
            if (mMappableObjects.contains(entityType)) {
                //
                return true;
            }
            if (Activity.class.isAssignableFrom(entityType)) {
                // Break search because reaching another BPEL Activity
                // It means that the mapper isn't acceptable for the selected BPEL entity.
                break;
            }
            //
            // Go to parent entity
            contextEntity = contextEntity.getParent();
        }
        //
        return context != null;
    }

}
