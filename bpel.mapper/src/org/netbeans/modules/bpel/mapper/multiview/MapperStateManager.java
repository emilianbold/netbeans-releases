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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bpel.mapper.tree.GraphExpandedState;
import org.netbeans.modules.bpel.mapper.tree.RightTreeExpandedState;
import org.netbeans.modules.bpel.mapper.tree.TreeExpandedState;
import org.netbeans.modules.bpel.mapper.tree.TreeExpandedStateImpl;
import org.netbeans.modules.bpel.mapper.model.MapperTcContext;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.support.UniqueId;
import org.netbeans.modules.soa.mappercore.Mapper;

/**
 * Store mapper state for context entity which is the unique id of mapper
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class MapperStateManager {
    private Map<UniqueId, MapperState> myEntitiesContext
            = new ContextCache<UniqueId, MapperState>();

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
        TreeExpandedState graphState = getGraphState();

        BpelEntity entity = null;

        entity = oldContext.getContextEntity();

        UniqueId entityUID = entity != null ? entity.getUID() : null;
        if (entityUID != null) {
            MapperState mapperEntityState = new MapperState(entityUID);
            mapperEntityState.setLeftTreeExpandedState(leftTreeState);
            mapperEntityState.setRightTreeExpandedState(rightTreeState);
            mapperEntityState.setGraphExpandedState(graphState);
            myEntitiesContext.put(entityUID, mapperEntityState);
        }

    }

    /**
     * Returns a flag which indicates if the state has been restored. 
     * @param context
     * @return
     */
    public boolean restoreOldEntityContext(BpelDesignContext context) {
        if (context == null) {
            return false;
        }

        BpelEntity entity = context.getContextEntity();
        UniqueId uid = entity != null ? entity.getUID() : null;
        if  ( uid == null) {
            return false;
        }

        final MapperState state = myEntitiesContext.get(uid);
        if (state == null) {
            return false;
        }
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
                TreeExpandedState leftTreeState = state.getLeftTreeExpandedState();
                if (leftTreeState != null) {
                    leftTreeState.restore();
                }

                TreeExpandedState rightTreeState = state.getRightTreeExpandedState();
                if (rightTreeState != null) {
                    rightTreeState.restore();
                }

                TreeExpandedState graphState = state.getGraphEXpandedState();
                if (graphState != null) {
                    graphState.restore();
                }
                //
                // TODO: restore tree selection, expanded graph
//            }
//        });
        //
        return true;
    }

    private TreeExpandedState getLeftTreeExpandedState() {
        TreeExpandedState leftTreeState = null;

        Mapper mapper = myMapperTcContext.getMapper();
        if (mapper != null) {
            leftTreeState = new TreeExpandedStateImpl(mapper);
            leftTreeState.save();
        }
        return leftTreeState;
    }

    private TreeExpandedState getGraphState() {
        TreeExpandedState graphState = null;

        Mapper mapper = myMapperTcContext.getMapper();
        if (mapper != null) {
            graphState = new GraphExpandedState(mapper);
            graphState.save();
        }
        return graphState;
    }

    // TODO a
    private TreeExpandedState getRightTreeExpandedState() {
        TreeExpandedState rightTreeState = null;

        Mapper mapper = myMapperTcContext.getMapper();
        if (mapper != null) {
            rightTreeState = new RightTreeExpandedState(mapper);
            rightTreeState.save();
        }
        return rightTreeState;
    }

    private static class ContextCache<K, V> extends LinkedHashMap<K, V> {
        private static final long serialVersionUID = 1L;
        private static final int MAX_ENTRIES = 50; // we aren't going to store more then last 50 entities

        @Override
        protected boolean removeEldestEntry(Entry<K, V> eldest) {
            return size() > MAX_ENTRIES;
        }
    }
}
