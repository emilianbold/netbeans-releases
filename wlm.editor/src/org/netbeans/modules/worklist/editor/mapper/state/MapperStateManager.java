/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.mapper.state;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.SwingUtilities;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.worklist.editor.mapper.MapperTcContext;
import org.netbeans.modules.worklist.editor.mapper.WlmDesignContext;

/**
 * Store mapper state for context entity which is the unique id of mapper
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class MapperStateManager {

    public static class UniqueId {

        private Class<? extends WLMComponent> mWlmElementType;

        public UniqueId(Class<? extends WLMComponent> wlmElementType) {
            mWlmElementType = wlmElementType;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj instanceof UniqueId) {
                return ((UniqueId)obj).mWlmElementType == mWlmElementType;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return mWlmElementType.hashCode();
        }

    }

    private Map<UniqueId, MapperState> myEntitiesContext
            = new ContextCache<UniqueId, MapperState>();

    private MapperTcContext myMapperTcContext;

    public MapperStateManager(MapperTcContext mapperTcContext) {
        myMapperTcContext = mapperTcContext;
    }

    public void storeOldEntityContext(WlmDesignContext oldContext) {
        if (oldContext == null) {
            return;
        }

        TreeExpandedState leftTreeState = getLeftTreeExpandedState();
        TreeExpandedState rightTreeState = getRightTreeExpandedState();
        TreeExpandedState graphState = getGraphState();

        WLMComponent entity = null;

        entity = oldContext.getContextEntity();

        UniqueId entityUID = entity != null ? 
            new UniqueId(entity.getElementType()) : null;
        if (entityUID != null) {
            MapperState mapperEntityState = new MapperState(entityUID);
            mapperEntityState.setLeftTreeExpandedState(leftTreeState);
            mapperEntityState.setRightTreeExpandedState(rightTreeState);
            mapperEntityState.setGraphExpandedState(graphState);
            myEntitiesContext.put(entityUID, mapperEntityState);
        }

    }

    public void restoreOldEntityContext(WlmDesignContext context) {
        if (context == null) {
            return;
        }

        WLMComponent entity = context.getContextEntity();
        UniqueId uid = entity != null ? new UniqueId(entity.getElementType()) : null;
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

                    TreeExpandedState graphState = state.getGraphEXpandedState();
                    if (graphState != null) {
                        graphState.restore();
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
//            JTree leftTree = mapper.getLeftTree();
//            if (leftTree != null) {
                leftTreeState = new TreeExpandedStateImpl(mapper);
                leftTreeState.save();
//            }
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
//            RightTree rightTree = mapper.getRightTree();
//            if (rightTree != null) {
                rightTreeState = new RightTreeExpandedState(mapper);
                rightTreeState.save();
//            }
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
