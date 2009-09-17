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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.viewmodel;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.Models.TreeFeatures;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Martin Entlicher
 */
public class TreeModelHyperNode extends TreeModelNode {

    private HyperCompoundModel model;
    
    public TreeModelHyperNode(
        final HyperCompoundModel model,
        final TreeModelRoot treeModelRoot,
        final Object object
    ) {
        super(
            model.getMain(),
            model.getColumns(),
            createChildren(model, treeModelRoot, object),
            treeModelRoot,
            object
        );
        this.model = model;
    }

    private static Children createChildren (
        HyperCompoundModel model,
        TreeModelRoot treeModelRoot,
        Object object
    ) {
        if (object == null) {
            throw new NullPointerException ();
        }
        return new HyperModelChildren (model, treeModelRoot, object);
    }

    @Override
    protected void refreshTheChildren(Models.CompoundModel model, TreeModelChildren.RefreshingInfo refreshInfo) {
        //System.err.println("HYPER node: refreshTheChildren("+model+", "+refreshInfo+")");
        //Thread.dumpStack();
        Children ch = getChildren();
        if (ch instanceof TreeModelChildren) {
            HyperModelChildren hch = (HyperModelChildren) ch;
            //hch.cleanCachedChildren(model);
            hch.refreshChildren(hch.new HyperRefreshingInfo(refreshInfo, model));
        } else {
            setChildren(new HyperModelChildren (this.model, treeModelRoot, object));
        }
    }

    private static final class HyperModelChildren extends TreeModelChildren {
        
        private HyperCompoundModel model;
        private final java.util.Map<Object, Models.CompoundModel> rootModelsByChildren = new HashMap<Object, Models.CompoundModel>();
        private final java.util.Map<Models.CompoundModel, Object[]> rootChildrenByModels = new HashMap<Models.CompoundModel, Object[]>();

        public HyperModelChildren (
            HyperCompoundModel model,
            TreeModelRoot   treeModelRoot,
            Object          object
        ) {
            super(null, model.getColumns(), treeModelRoot, object);
            this.model = model;
        }
        
        @Override
        protected Object[] getModelChildren(RefreshingInfo refreshInfo) throws UnknownTypeException {
            if (refreshInfo instanceof HyperRefreshingInfo) {
                HyperRefreshingInfo hri = (HyperRefreshingInfo) refreshInfo;
                for (Models.CompoundModel m : hri.getRefreshedModels()) {
                    cleanCachedChildren(m);
                }
            }
            Object[] ch = null;
            TreeModelFilter tf = model.getTreeFilter();
            for (Models.CompoundModel m : model.getModels()) {
                Object[] mch;
                synchronized (rootChildrenByModels) {
                    mch = rootChildrenByModels.get(m);
                }
                if (mch == null) {
                    if (tf != null) {
                        int count = tf.getChildrenCount (m, object);
                        mch = tf.getChildren (
                            m,
                            object,
                            0,
                            count
                        );
                    } else {
                        int count = m.getChildrenCount (object);
                        mch = m.getChildren (
                            object,
                            0,
                            count
                        );
                    }
                    synchronized (rootModelsByChildren) {
                        for (Object o : mch) {
                            rootModelsByChildren.put(o, m);
                        }
                    }
                    synchronized (rootChildrenByModels) {
                        rootChildrenByModels.put(m, mch);
                    }
                }
                if (ch == null) {
                    ch = mch;
                } else {
                    int chl = ch.length;
                    Object[] nch = new Object[chl + mch.length];
                    System.arraycopy(ch, 0, nch, 0, chl);
                    System.arraycopy(mch, 0, nch, chl, mch.length);
                    ch = nch;
                }
            }
            return ch;
        }

        private void cleanCachedChildren(Models.CompoundModel model) {
            Object[] children;
            synchronized (rootChildrenByModels) {
                children = rootChildrenByModels.remove(model);
            }
            if (children != null) {
                synchronized (rootModelsByChildren) {
                    for (Object ch : children) {
                        rootModelsByChildren.remove(ch);
                    }
                }
            }
        }

        @Override
        protected void expandIfSetToExpanded(Object child) {
            Models.CompoundModel model;
            synchronized (rootModelsByChildren) {
                model = rootModelsByChildren.get(child);
            }
            if (model == null) return ;
            try {
                DefaultTreeExpansionManager.get(model).setChildrenToActOn(getTreeDepth());
                if (model.isExpanded (child)) {
                    TreeFeatures treeTable = treeModelRoot.getTreeFeatures ();
                    if (treeTable != null && treeTable.isExpanded(object)) {
                        // Expand the child only if the parent is expanded
                        treeTable.expandNode (child);
                    }
                }
            } catch (UnknownTypeException ex) {
            }
        }

        @Override
        public Node[] createNodes (Object object) {
            if (object == WAIT_KEY) {
                return super.createNodes(object);
            }
            if (object instanceof Exception)
                return new Node[] {
                    new ExceptionNode ((Exception) object)
                };
            Models.CompoundModel m;
            synchronized (rootModelsByChildren) {
                m = rootModelsByChildren.get(object);
            }
            if (m == null) {
                //System.err.println("\n\n\n\n!!! NO NODE for object "+object+"!!!\n\n\n");
                return new Node[] {};
            }
            TreeModelNode tmn = new TreeModelNode (
                m,
                model.getColumns(),
                treeModelRoot,
                object
            );
            objectToNode.put (object, new WeakReference<TreeModelNode>(tmn));
            return new Node[] {tmn};
        }

        public class HyperRefreshingInfo extends RefreshingInfo {

            private final Set<Models.CompoundModel> models;

            public HyperRefreshingInfo(RefreshingInfo ri, Models.CompoundModel model) {
                super(ri.refreshSubNodes);
                this.models = new HashSet<Models.CompoundModel>();
                this.models.add(model);
            }

            @Override
            public RefreshingInfo mergeWith(RefreshingInfo rinfo) {
                if (rinfo instanceof HyperRefreshingInfo) {
                    this.models.addAll(((HyperRefreshingInfo) rinfo).models);
                }
                this.refreshSubNodes = this.refreshSubNodes || rinfo.refreshSubNodes;
                return this;
            }

            public Set<Models.CompoundModel> getRefreshedModels() {
                return models;
            }

            @Override
            public boolean isRefreshSubNodes(Object child) {
                //System.err.println("isRefreshSubNodes("+child+") = "+(models.contains(rootModelsByChildren.get(child))));
                //System.err.println("  child's model    = "+rootModelsByChildren.get(child));
                //System.err.println("  refreshing models = "+models);
                return super.isRefreshSubNodes(child) && models.contains(rootModelsByChildren.get(child));
            }

        }
    }

}
