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

package org.netbeans.modules.xslt.mapper.view;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import javax.swing.JTree;
import org.netbeans.modules.soa.mapper.basicmapper.util.MapperUtilities;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapperEvent;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.xpath.XPathPredicateExpression;
import org.netbeans.modules.xslt.mapper.model.FindUsedPredicateNodesVisitor;
import org.netbeans.modules.xslt.mapper.model.PredicatedAxiComponent;
import org.netbeans.modules.xslt.mapper.model.SourceTreeModel;
import org.netbeans.modules.xslt.mapper.model.nodes.Node;
import org.netbeans.modules.xslt.mapper.model.nodes.NodeFactory;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.mapper.model.targettree.PredicatedSchemaNode;
import org.netbeans.modules.xslt.mapper.model.targettree.SchemaNode;

/**
 * The class collects all predicate expressions which are in the XSLT model.
 * Each predicate is declared in the XPath location step.
 * The location step is bound to the specific schema element or attribute.
 * The predicate manager keeps the location of predicate in term of
 * schema elements' path.
 *
 * The set of predicates is populated from XSLT model. The manager can
 * be populated entirely or can track small model's changes only.
 * It produces notification messages about adding, editing or deleting of
 * the predicates set.
 *
 * The main intention of the predicate manager is to provide showing of
 * predicates in the mapper source tree.
 *
 * @author nk160297
 */
public class PredicateManager {
    
    private XsltMapper myMapper;
    
    // The cache of predicates.
    private LinkedList<CachedPredicate> myPredicates;
    
    public PredicateManager(XsltMapper mapper) {
        myMapper = mapper;
        myPredicates = new LinkedList<CachedPredicate>();
    }
    
    /**
     * Looks for the predicated tree node similar to the source node
     * with the specified predicates.
     */
    public TreeNode getPredicatedNode(TreeNode baseNode,
            XPathPredicateExpression[] predArr) {
        assert baseNode instanceof SchemaNode;
        //
        AXIComponent type = baseNode.getType();
        //
        // Look for the corresponding predicate
        CachedPredicate soughtPredicate = null;
        for (CachedPredicate cp : myPredicates) {
            if (cp.hasSameParams(type, predArr) && cp.hasSameLocation(baseNode)) {
                soughtPredicate = cp;
                break;
            }
        }
        //
        if (soughtPredicate == null) {
            return null;
        }
        //
        // Look for a sibling predicated node with the corresponding predicate
        PredicatedAxiComponent soughtComp = soughtPredicate.getPComponent();
        TreeNode parentNode = baseNode.getParent();
        if (parentNode == null) {
            // The parent node always has to be specified!
            return null;
        }
        for (TreeNode siblingNode : parentNode.getChildren()) {
            if (siblingNode instanceof PredicatedSchemaNode) {
                PredicatedAxiComponent comp =
                        ((PredicatedSchemaNode)siblingNode).getPredicatedAxiComp();
                if (comp.equals(soughtComp)) {
                    return siblingNode;
                }
            }
        }
        //
        return null;
    }
    
    /**
     * Looks for the predicated tree nodes similar to the source node.
     */
    public Collection<PredicatedSchemaNode> getPredicatedNodes(TreeNode baseNode) {
        assert baseNode instanceof SchemaNode;
        //
        TreeNode parentNode = baseNode.getParent();
        LinkedList result = new LinkedList<PredicatedSchemaNode>();
        if (parentNode != null) {
            for (TreeNode siblingNode : parentNode.getChildren()) {
                if (siblingNode instanceof PredicatedSchemaNode) {
                    result.add((PredicatedSchemaNode)siblingNode);
                }
            }
        }
        return result;
    }
    
    /**
     * Construct the new predicated tree nodes similar to the base node.
     */
    public Collection<PredicatedSchemaNode> createPredicatedNodes(TreeNode baseNode) {
        assert baseNode instanceof SchemaNode;
        //
        AXIComponent type = baseNode.getType();
        //
        // Look for the corresponding predicates
        LinkedList<CachedPredicate> suitablePredicates = new LinkedList<CachedPredicate>();
        for (CachedPredicate cp : myPredicates) {
            if (cp.getType().equals(type) && cp.hasSameLocation(baseNode)) {
                suitablePredicates.add(cp);
            }
        }
        //
        // Construct new nodes
        LinkedList<PredicatedSchemaNode> result = new LinkedList<PredicatedSchemaNode>();
        for(CachedPredicate cp : suitablePredicates) {
            Node newNode = NodeFactory.createNode(cp.getPComponent(), myMapper);
            assert newNode instanceof PredicatedSchemaNode;
            result.add((PredicatedSchemaNode)newNode);
        }
        //
        return result;
    }
    
    /**
     * Creates a new predicate and cache it.
     * Then creates a new predicated node and return it.
     * This method is intended to be called, when a user creates a new predicate manually.
     */
    public PredicatedSchemaNode createPredicatedNode(TreeNode baseNode,
            XPathPredicateExpression[] predArr) {
        //
        assert baseNode instanceof SchemaNode;
        AXIComponent type = baseNode.getType();
        //
        PredicatedAxiComponent newPAxiComp = new PredicatedAxiComponent(
                type, predArr);
        //
        CachedPredicate newPredicate = new CachedPredicate(newPAxiComp, baseNode);
        newPredicate.setPersistent(true);
        if (!addPredicate(newPredicate)) {
            return null;
        }
        //
        TreeNode parentNode = baseNode.getParent();
        Node newNode = NodeFactory.createNode(newPAxiComp, myMapper);
        assert newNode instanceof PredicatedSchemaNode;
        ((PredicatedSchemaNode)newNode).setParent(parentNode);
        //
        JTree sourceTree = myMapper.getMapperViewManager().getSourceView().getTree();
        assert sourceTree != null;
        SourceTreeModel sourceTreeModel = (SourceTreeModel)sourceTree.getModel();
        parentNode.reload();
        sourceTreeModel.fireTreeChanged(TreeNode.getTreePath(parentNode));
        //
        return (PredicatedSchemaNode)newNode;
    }
    
    /**
     * This method is intended to be called after a new predicate is found
     * in the XSLT during a parsing.
     */ 
    public void addPredicate(List objLocationPath) {
        CachedPredicate newPredicate = new CachedPredicate(objLocationPath);
        newPredicate.setPersistent(false);
        if (!addPredicate(newPredicate)) {
            return;
        }
        //
        TreeNode parentNode = lookForParentTreeNode(newPredicate);
        parentNode.reload();
        //
        // Notify the tree
        JTree sourceTree = myMapper.getMapperViewManager().getSourceView().getTree();
        assert sourceTree != null;
        SourceTreeModel sourceTreeModel = (SourceTreeModel)sourceTree.getModel();
        sourceTreeModel.fireTreeChanged(TreeNode.getTreePath(parentNode));
    }
    
    private boolean addPredicate(CachedPredicate newPredicate) {
        //
        // Check is there the same predicate already in the cache
        for (CachedPredicate predicate : myPredicates) {
            if (predicate.equals(newPredicate)) {
                // Dublicate. Nothing to do
                return false;
            }
        }
        //
        myPredicates.add(newPredicate);
        return true;
    }
    
    /**
     * This method is called when the predicate is modified manually.
     */ 
    public boolean modifyPredicate(PredicatedSchemaNode node, 
            XPathPredicateExpression[] predArr) {
        PredicatedAxiComponent pac = node.getPredicatedAxiComp();
        //
        for (CachedPredicate cp : myPredicates) {
            if (cp.hasSameParams(pac) && cp.hasSameLocation(node)) {
                String oldPredicatesText = cp.getPComponent().getPredicatesText();
                //
                cp.getPComponent().setPredicates(predArr);
                cp.setPersistent(true); // because it was modified manually
                String newPredicatesText = cp.getPComponent().getPredicatesText();
                //
                TreeNode parentNode = node.getParent();
                parentNode.reload();
                //
                // Notify the tree
                JTree sourceTree = myMapper.getMapperViewManager().getSourceView().getTree();
                assert sourceTree != null;
                SourceTreeModel sourceTreeModel = (SourceTreeModel)sourceTree.getModel();
                sourceTreeModel.fireTreeChanged(TreeNode.getTreePath(parentNode));
                //
                // Post notification to update the XSLT sources
                myMapper.getMapperViewManager().postMapperEvent(
                        MapperUtilities.getMapperEvent(
                        this,
                        node.getMapperNode(),
                        IBasicMapperEvent.REQ_UPDATE_NODE,
                        "Predicate is modeified." +
                        " Location: " + cp.locationToString() + 
                        " Old: " + oldPredicatesText + 
                        " New: " + newPredicatesText));
                
                return true;
            }
        }
        //
        return false;
    }
    
    public boolean deletePredicate(PredicatedSchemaNode node) {
        PredicatedAxiComponent pac = node.getPredicatedAxiComp();
        //
        for (CachedPredicate cp : myPredicates) {
            if (cp.hasSameParams(pac) && cp.hasSameLocation(node)) {
                myPredicates.remove(cp);
                //
                TreeNode parentNode = node.getParent();
                parentNode.reload();
                //
                // Notify the tree
                JTree sourceTree = myMapper.getMapperViewManager().getSourceView().getTree();
                assert sourceTree != null;
                SourceTreeModel sourceTreeModel = (SourceTreeModel)sourceTree.getModel();
                sourceTreeModel.fireTreeChanged(TreeNode.getTreePath(parentNode));
                return true;
            }
        }
        //
        return false;
    }
    
    public void clearTemporaryPredicates() {
        TreeNode root = (TreeNode) myMapper.getMapperViewManager().
                getDestView().getTree().getModel().getRoot();
        if (root == null) {
            // impossible to understand if the predicated node is used or not.
            return;
        }
        FindUsedPredicateNodesVisitor vis = new FindUsedPredicateNodesVisitor();
        root.accept(vis);

        
        ListIterator<CachedPredicate> predItr = myPredicates.listIterator();
        
        
        while (predItr.hasNext()) {
            CachedPredicate predicate = predItr.next();
            PredicatedSchemaNode node = predicate.findNode(myMapper);
            if (node == null) {
                // the corresponding node isn't found so the cached predicate 
                // can be removed because its location path is corrupted. 
                predItr.remove();
            } else {
                if (predicate.isPersistent()) {
                    // the predicate is persistent
                    continue;
                }
                if (vis.getResultList().contains(node)) {
                    // the predicate is used
                    continue;
                }
                //
                predItr.remove();
                //
                TreeNode parentNode = node.getParent();
                parentNode.reload();
                //
                // Notify the tree
                JTree sourceTree = myMapper.getMapperViewManager().getSourceView().getTree();
                assert sourceTree != null;
                SourceTreeModel sourceTreeModel = (SourceTreeModel)sourceTree.getModel();
                sourceTreeModel.fireTreeChanged(TreeNode.getTreePath(parentNode));
            }
        }
    }
    
    //-----------------------------------------------------------
    
    public static String toString(XPathPredicateExpression[] predicatesArr) {
        if (predicatesArr != null && predicatesArr.length != 0) {
            StringBuilder sb = new StringBuilder();
            for (XPathPredicateExpression predicate : predicatesArr) {
                sb.append("[").append(predicate.getExpressionString()).append("]");
            }
            return sb.toString();
        } else {
            return "";
        }
    }
    
    /**
     * Looks for a node in the source tree by a location path of the predicate.
     * The
     */
    private TreeNode lookForParentTreeNode(CachedPredicate predicate) {
        JTree sourceTree = myMapper.getMapperViewManager().getSourceView().getTree();
        SourceTreeModel model = (SourceTreeModel)sourceTree.getModel();
        //
        LinkedList locationPath = predicate.getLocationPath();
        assert !locationPath.isEmpty();
        //
        // It is assumed that the root node is the first path element.
        TreeNode rootNode = (TreeNode)model.getRoot();
        ListIterator itr = locationPath.listIterator();
        Object dataObj = itr.next();
        if (!isCompatible(rootNode, dataObj)) {
            return null;
        }
        //
        TreeNode parentNode = rootNode;
        PredicatedAxiComponent lastItem = predicate.getPComponent();
        while (itr.hasNext() && parentNode != null) {
            dataObj = itr.next();
            //
            if (dataObj == lastItem) {
                break;
            }
            //
            TreeNode soughtNode = null;
            for (TreeNode childNode : parentNode.getChildren()) {
                if (isCompatible(childNode, dataObj)) {
                    soughtNode = childNode;
                    break;
                }
            }
            //
            if (soughtNode == null) {
                return null;
            }
            //
            parentNode = soughtNode;
        }
        //
        return parentNode;
    }
    
    /**
     * Returns true only if the tree node has compatible data object in
     * comparison with the specified one.
     */
    private boolean isCompatible(TreeNode treeNode, Object dataObject) {
        if (dataObject instanceof PredicatedAxiComponent) {
            if (treeNode instanceof PredicatedSchemaNode &&
                    ((PredicatedSchemaNode)treeNode).getPredicatedAxiComp().equals(dataObject)) {
                return true;
            }
        } else if (dataObject instanceof AXIComponent) {
            if (treeNode instanceof SchemaNode && treeNode.getType().equals(dataObject)) {
                return true;
            }
        } else {
            assert false : "Incompatible data object"; // NOI18N
        }
        return false;
    }
    
    //-----------------------------------------------------------
    
    public static class CachedPredicate {
        // It's implied that the list contains either AxiComponents or PredicatedAxiComponent
        // The last element of the list is always PredicatedAxiComponent
        private LinkedList myLocationPath;
        
        // Persistense means that the instance should not be automatically
        // deleted from the cache if it is not used.
        // The predicates which are not persistent will be removed from
        // the cache automatically.
        private boolean isPersistent;
        
        public static LinkedList buildLocationPathList(
                PredicatedAxiComponent predicateComp, TreeNode treeNode) {
            LinkedList locationPath = new LinkedList();
            //
            // Add the predicateComp first. Finally it should be at the tail of list.
            locationPath.add(predicateComp);
            //
            TreeNode parentNode = treeNode.getParent();
            while (parentNode != null) {
                Object dataObject = parentNode.getDataObject();
                assert dataObject != null;
                if (!(  (dataObject instanceof AXIComponent) || 
                        (dataObject instanceof PredicatedAxiComponent) )) {
                    // The root node of the tree can be not of a schema type.
                    break;
                }
                //
                // Add to beginning to provide natural location path order.
                locationPath.addFirst(dataObject);
                //
                parentNode = parentNode.getParent();
            }
            //
            //
            return locationPath;
        }
        
        public CachedPredicate(PredicatedAxiComponent predicateComp,
                TreeNode baseNode) {
            this(buildLocationPathList(predicateComp, baseNode));
        }
        
        public CachedPredicate(List locationPath) {
            if (locationPath instanceof LinkedList) {
                myLocationPath = (LinkedList)locationPath;
            } else {
                myLocationPath = new LinkedList(locationPath);
            }
        }
        
        public boolean isPersistent() {
            return isPersistent;
        }
        
        public void setPersistent(boolean newValue) {
            isPersistent = newValue;
        }
        
        public AXIComponent getType() {
            return getPComponent().getType();
        }
        
        public PredicatedAxiComponent getPComponent() {
            return (PredicatedAxiComponent)myLocationPath.getLast();
        }
        
        /**
         * Returns the list of data objects of TreeNode, which form the tree path location.
         * Data objects can be either of AxiComponents or PredicatedAxiComponent type.
         * The PredicatedAxiComponent corresponding to the predicate itself is included to the list.
         * So the list can't be empty.
         */
        public LinkedList getLocationPath() {
            return myLocationPath;
        }
        
        /**
         * Check if the cached predicate has the same AXIOM component type
         * and the same predicates.
         */
        public boolean hasSameParams(AXIComponent compType,
                XPathPredicateExpression[] predArr) {
            PredicatedAxiComponent pComp = getPComponent();
            return pComp.getType().equals(compType) &&
                    pComp.hasSamePredicates(predArr);
        }
        
        public boolean hasSameParams(PredicatedAxiComponent predAxiComp) {
            PredicatedAxiComponent pComp = getPComponent();
            return pComp.equals(predAxiComp);
        }
        
        /**
         * Check if the specified tree node has the same location path
         * as the predicate.
         */
        public boolean hasSameLocation(TreeNode treeNode) {
            TreeNode parentNode = treeNode.getParent();
            //
            // Set initial position of the iterator to the last but one.
            ListIterator itr = myLocationPath.listIterator(myLocationPath.size() - 1);
            while (itr.hasPrevious()) {
                //
                if (parentNode == null) {
                    break;
                }
                //
                Object step = itr.previous();
                Object dataObject = parentNode.getDataObject();
                if (!dataObject.equals(step)) {
                    // Inconsistent step type or data object class
                    return false;
                }
                //
                // Everything Ok. Go to next step
                parentNode = parentNode.getParent();
            }
            //
            return true;
        }
        
        public String toString() {
            return locationToString();
        }
        
        private String locationToString() {
            StringBuilder sb = new StringBuilder();
            boolean isFirst = true;
            for (Object stepObj : myLocationPath) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    sb.append("/");
                }
                //
                sb.append(stepObj.toString());
            }
            return sb.toString();
        }
        
        public boolean equals(Object obj2) {
            if (!(obj2 instanceof CachedPredicate)) {
                return false;
            }
            //
            CachedPredicate pred2 = (CachedPredicate)obj2;
            LinkedList path2 = pred2.getLocationPath();
            if (path2.size() != myLocationPath.size()) {
                return false;
            }
            //
            ListIterator itr = myLocationPath.listIterator(myLocationPath.size());
            ListIterator itr2 = path2.listIterator(path2.size());
            //
            while (itr.hasPrevious()) {
                Object dataObj = itr.previous();
                Object dataObj2 = itr2.previous();
                if (!(dataObj.equals(dataObj2))) {
                    return false;
                }
            }
            //
            return true;
        }
        
        public PredicatedSchemaNode findNode(XsltMapper myMapper) {
            JTree sourceTree = myMapper.getMapperViewManager().getSourceView().getTree();
            assert sourceTree != null;
            //
            TreeNode root = (TreeNode)sourceTree.getModel().getRoot();
            if (root == null) {
                return null;
            }
            //
            TreeNode parentNode = null;
            Iterator pathItr = myLocationPath.iterator();
            if (pathItr.hasNext()) {
                Object rootPathObject = pathItr.next();
                if (root.getDataObject() == rootPathObject) {
                    parentNode = root;
                }
            }
            //
            if (parentNode == null) {
                return null;
            }
            //
            TreeNode soughtChildNode = null;
            while (pathItr.hasNext()) {
                Object nextPathObject = pathItr.next();
                //
                // Look for the child node
                soughtChildNode = null;
                for (TreeNode childNode : parentNode.getChildren()) {
                    if (childNode.getDataObject() == nextPathObject) {
                        soughtChildNode = childNode;
                        break;
                    }
                }
                //
                // Check if the sought child node is found 
                if (soughtChildNode == null) {
                    return null; 
                } else {
                    parentNode = soughtChildNode;
                }
            }
            //
            assert soughtChildNode instanceof PredicatedSchemaNode;
            return (PredicatedSchemaNode)soughtChildNode;
        }
        
    }
    
}
  