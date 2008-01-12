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

import java.util.HashMap;
import java.util.Map;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bpel.mapper.tree.TreeExpandedState;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTcContext;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.support.UniqueId;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.RightTree;

/**
 * Store mapper state for context entity which is the unique id of mapper
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class MapperStateManager {
    private Map<UniqueId, MapperState> myEntitiesContext 
            = new HashMap<UniqueId, MapperState>();

    private MapperTcContext myMapperTcContext;
    
    public MapperStateManager(MapperTcContext mapperTcContext) {
        myMapperTcContext = mapperTcContext;
    }
    
    public void storeOldEntityContext(BpelDesignContext oldContext) {
        if (oldContext == null) {
            return;
        }

        TreeExpandedState leftTreeState = getLeftTreeExpandedState();
        TreeExpandedState rightTreeState = getRightTreeExpandedState();
        BpelEntity entity = null;

        entity = oldContext.getContextEntity();

        UniqueId entityUID = entity != null ? entity.getUID() : null;
        if (entityUID != null) {
            MapperState mapperEntityState = new MapperState(entityUID);
            mapperEntityState.setLeftTreeExpandedState(leftTreeState);
            mapperEntityState.setRightTreeExpandedState(rightTreeState);
            myEntitiesContext.put(entityUID, mapperEntityState);
        }

    }

    public void restoreOldEntityContext(BpelDesignContext context) {
        if (context == null) {
            return;
        }

        BpelEntity entity = context.getContextEntity();
        UniqueId uid = entity != null ? entity.getUID() : null;
        if  ( uid == null) {
            return;
        }

        final MapperState state = myEntitiesContext.get(uid);
        if (state != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    TreeExpandedState leftTreeState = state.getLeftTreeExpandedState();
                    if (leftTreeState != null) {
                        leftTreeState.restore();
                    }
                    
                    TreeExpandedState rightTreeState = state.getRightTreeExpandedState();
                    if (rightTreeState != null) {
                        rightTreeState.restore();
                    }
                }
            });
            // TODO: restore tree selection, expanded graph
        }
    }    

    private TreeExpandedState getLeftTreeExpandedState() {
        TreeExpandedState leftTreeState = null;

        Mapper mapper = myMapperTcContext.getMapper();
        if (mapper != null) {
            JTree leftTree = mapper.getLeftTree();
            if (leftTree != null) {
                leftTreeState = new TreeExpandedState(leftTree);
                leftTreeState.save();
            }
        }
        return leftTreeState;
    }

    // TODO a
    private TreeExpandedState getRightTreeExpandedState() {
        TreeExpandedState rightTreeState = null;

        Mapper mapper = myMapperTcContext.getMapper();
        if (mapper != null) {
            RightTree rightTree = mapper.getRightTree();
            if (rightTree != null) {
//                rightTreeState = new TreeExpandedState(rightTree);
//                rightTreeState.save();
            }
        }
        return rightTreeState;
    }
}
