/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.soa.xpath.mapper.lsm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.xpath.mapper.context.MapperStaticContext;
import org.netbeans.modules.soa.xpath.mapper.model.XPathMapperModel;
import org.netbeans.modules.soa.xpath.mapper.tree.DirectedList;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.soa.xpath.mapper.utils.XPathMapperUtils;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.CastSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.PredicatedSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.SchemaCompHolder;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.WrappingSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.XPathCast;
import org.netbeans.modules.xml.xpath.ext.spi.XPathPseudoComp;

/**
 *
 * This is auxiliary class for collecting LSMs. Because of the LSMs depend
 * each to another, they form a dependency tree. That is why it has a "tree" in the name.
 *
 * @author Nikita Krjukov
 */
public class MapperLsmTree {

    private List<TreeNode> mRootNodes = new ArrayList<TreeNode>();

    private MapperStaticContext mStContext;
    private boolean mInLeftTree;
    private MapperSwingTreeModel mTreeModel;

    public MapperLsmTree(MapperStaticContext stContext, boolean inLeftTree) {
        mStContext = stContext;
        mInLeftTree = inLeftTree;
    }

    public <T extends MapperLsm> void addLsms(TreeItem treeItem, Class<T> lsmClass) {

        DirectedList<Object> objPath = getTreeModel().getPathConverter().
                constructObjectLocationList(treeItem, true, false);
        DirectedList<T> lsmList = XPathMapperUtils.extractLsms(objPath, lsmClass);
        if (lsmList != null && !lsmList.isEmpty()) {
            addLsmList(lsmList);
        }
    }

    public <T extends MapperLsm> void addLsmList(DirectedList<T> lsmList) {
        MapperLsm parent = null;
        TreeNode parentNode = null;
        //
        Iterator<T> itr = lsmList.backwardIterator();
        while (itr.hasNext()) {
            T lsm = itr.next();
            //
            TreeNode newNode = null;
            //
            // Here is a support for special cases.
            // For example, if an element is casted and then a predicate
            // is applied to the casted element. In such case the mapper's tree
            // contains only a predicate LSM. But we need create full chain
            // of LSM in BPEL.
            //
            LinkedList<T> lsmSubList = new LinkedList<T>();
            lsmSubList.addFirst(lsm);
            //
            XPathSchemaContext sContext = lsm.getSchemaContext();
            while (sContext instanceof WrappingSchemaContext) {
                sContext = WrappingSchemaContext.class.cast(sContext).getBaseContext();
                //
                if (sContext instanceof PredicatedSchemaContext) {
                    PredicatedSchemaContext predCtxt =
                            PredicatedSchemaContext.class.cast(sContext);
                    PredicateManager pm = getTreeModel().getExtManagerHolder().
                            getPredicateManager();
                    MapperPredicate newPredicate = pm.createMapperPredicate(predCtxt);
                    lsmSubList.addFirst((T)newPredicate);
                } else if (sContext instanceof CastSchemaContext) {
                    XPathCast cast = CastSchemaContext.class.cast(sContext).getTypeCast();
                    CastManager cm = getTreeModel().getExtManagerHolder().
                            getCastManager();
                    MapperTypeCast newTCast = cm.createMapperTypeCast(cast);
                    lsmSubList.addFirst((T)newTCast);
                } else if (sContext != null) {
                    SchemaCompHolder sCompHolder = XPathSchemaContext.
                            Utilities.getSchemaCompHolder(sContext, false);
                    if (sCompHolder.isPseudoComp()) {
                        Object pcObj = sCompHolder.getHeldComponent();
                        assert pcObj instanceof XPathPseudoComp;
                        XPathPseudoComp pseudoComp = XPathPseudoComp.class.cast(pcObj);
                        PseudoCompManager pcm = getTreeModel().getExtManagerHolder().
                                getPseudoCompManager();
                        MapperPseudoComp newPComp =
                                pcm.createMapperPseudoComp(pseudoComp);
                        lsmSubList.addFirst((T)newPComp);
                    }
                }
            }
            //
            //
            for (MapperLsm subLsm : lsmSubList) {
                if (parent == null) {
                    newNode = new TreeNode(null, subLsm);
                    mRootNodes.add(newNode);
                } else {
                    assert parentNode != null;
                    newNode = parentNode.addChild(subLsm);
                    if (newNode == null) {
                        break;
                    }
                }
                //
                parentNode = newNode;
                parent = subLsm;
            }
        }
    }

    private MapperSwingTreeModel getTreeModel() {
        if (mTreeModel == null) {
            XPathMapperModel mm =
                    XPathMapperModel.class.cast(mStContext.getMapperModel());
            mTreeModel = mInLeftTree ?
                mm.getLeftTreeModel() : mm.getRightTreeModel();
        }
        return mTreeModel;
    }

    public List<TreeNode> getRootNodes() {
        return mRootNodes;
    } 

    public static class TreeNode {
        private TreeNode mParent;
        private MapperLsm mLsm;
        private List<TreeNode> mLsmChildren;

        public TreeNode(TreeNode parent, MapperLsm lsm) {
            mParent = parent;
            mLsm = lsm;
        }

        public TreeNode addChild(MapperLsm newLsm) {
            if (newLsm == null) {
                return null;
            }
            //
            if (mLsmChildren == null) {
                mLsmChildren = new ArrayList<TreeNode>();
            } else {
                for (TreeNode lsmNode : mLsmChildren) {
                    MapperLsm lsm = lsmNode.getLsm();
                    newLsm.equals(lsm);
                    return null; // duplicate
                }
            }
            //
            TreeNode newNode = new TreeNode(this, newLsm);
            mLsmChildren.add(newNode);
            //
            return newNode;
        }
        
        public TreeNode getParent() {
            return mParent;
        }

        public MapperLsm getLsm() {
            return mLsm;
        }

        public List<TreeNode> getChildNodes() {
            if (mLsmChildren == null) {
                return Collections.EMPTY_LIST;
            }
            return mLsmChildren;
        }
    }

}
