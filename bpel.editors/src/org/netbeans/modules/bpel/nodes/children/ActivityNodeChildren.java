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
package org.netbeans.modules.bpel.nodes.children;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.editors.api.EditorUtil;
import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.BaseFaultHandlers;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Catch;
import org.netbeans.modules.bpel.model.api.CatchAll;
import org.netbeans.modules.bpel.model.api.CompensationHandler;
import org.netbeans.modules.bpel.model.api.CompensationHandlerHolder;
import org.netbeans.modules.bpel.model.api.CompositeActivity;
import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.bpel.model.api.Correlation;
import org.netbeans.modules.bpel.model.api.CorrelationContainer;
import org.netbeans.modules.bpel.model.api.CorrelationsHolder;
import org.netbeans.modules.bpel.model.api.ExtendableActivity;
import org.netbeans.modules.bpel.model.api.FaultHandlers;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.model.api.PatternedCorrelation;
import org.netbeans.modules.bpel.model.api.PatternedCorrelationContainer;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.properties.PropertyNodeFactory;
import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.openide.ErrorManager;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 02 May 2006
 */
public class ActivityNodeChildren extends BpelNodeChildren<BpelContainer> implements Index {

    private Index indexSupport;
    
    public ActivityNodeChildren(BpelContainer bpelEntity, Lookup contextLookup) {
        super(bpelEntity, contextLookup);
        indexSupport = new IndexSupport();
    }
    
    public Index getIndex() {
        return indexSupport;
    }
    
    public Collection getNodeKeys() {
        BpelContainer ref = getReference();
        if (ref == null) {
            return Collections.EMPTY_LIST;
        }
        List<BpelEntity> unmodifiableActivityChilds = ref.getChildren();
        if (unmodifiableActivityChilds == null || unmodifiableActivityChilds.size() == 0) {
            return Collections.EMPTY_LIST;
        }
        
        List<BpelEntity> childs = new ArrayList<BpelEntity>();
        List<BpelEntity> activityChilds = new ArrayList<BpelEntity>();
        activityChilds.addAll(unmodifiableActivityChilds);
        // so as special sort order ()
        // is required do next:
        

        // add correlationSet nodes if required
        if (ref instanceof CorrelationsHolder) {
            CorrelationContainer corrCont = ((CorrelationsHolder)ref)
                                                .getCorrelationContainer();
            if (corrCont != null) {
                activityChilds.remove(corrCont);
                Correlation[] corrs = corrCont.getCorrelations();
                if (corrs != null && corrs.length > 0) {
                    childs.addAll(Arrays.asList(corrs));
                }
            }
        }
        
        // set patterned correlationSet nodes for Invoke 
        if (ref instanceof Invoke) {
            PatternedCorrelationContainer pCorrCont = ((Invoke)ref)
                                            .getPatternedCorrelationContainer();
            if (pCorrCont != null ) {
                activityChilds.remove(pCorrCont);
                PatternedCorrelation[] pCorrs = pCorrCont
                                                .getPatternedCorrelations();
                if (pCorrs != null && pCorrs.length > 0) {
                    childs.addAll(Arrays.asList(pCorrs));
                }
            }
        }

        // add catch/ catchAll if required
        // !(ref instanceof FaultHandlers) need to avoid BaseScope and so on elements
        if (ref instanceof BaseFaultHandlers && !(ref instanceof FaultHandlers)) {
            
            // set catch nodes
            Catch[] catches = ((BaseFaultHandlers)ref).getCatches();
            if (catches != null && catches.length > 0) {
                activityChilds.removeAll(Arrays.asList(catches));
                childs.addAll(Arrays.asList(catches));
            }
            
            // set catchAll node
            CatchAll catchAll = ((BaseFaultHandlers)ref).getCatchAll();
            if (catchAll != null) {
                activityChilds.remove(catchAll);
                childs.add(catchAll);
            }
        }
        
        // CompensationHandler node if required
        if (ref instanceof CompensationHandlerHolder) {
            // set CompensationHandler node
            CompensationHandler compensationHandler = ((CompensationHandlerHolder)ref)
                        .getCompensationHandler();
            if (compensationHandler != null) {
                activityChilds.remove(compensationHandler);
                childs.add(compensationHandler);
            }
        }
        
        // activity childs didn't modified
        if (childs.size() == 0) {
            return activityChilds;
        }
        
        if (activityChilds.size() > 0) {
            childs.addAll(activityChilds);
        }
        
        return childs;
    }

//    protected Node[] createNodes(Object object) {
//        if (object != null && object instanceof BpelEntity) {
//            NavigatorNodeFactory factory = NavigatorNodeFactory.getInstance();
//            Node childNode = factory.createNode((BpelEntity)object,lookup);
//            if (childNode != null) {
//                return new Node[] {childNode};
//            }
//        } 
//        
//        return new Node[0];
//    }

    public int indexOf(final Node node) {
        return indexSupport.indexOf(node);
    }

    public void reorder() {
        indexSupport.reorder();
    }

    public void reorder(int[] perm) {
        indexSupport.reorder(perm);
    }

    public void move(int x, int y) {
        indexSupport.move(x,y);
    }

    public void exchange(int x, int y) {
        indexSupport.exchange(x,y);
    }

    public void moveUp(int x) {
        indexSupport.moveUp(x);
    }

    public void moveDown(int x) {
        indexSupport.moveDown(x);
    }

    public void addChangeListener(final ChangeListener chl) {
        indexSupport.addChangeListener(chl);
    }

    public void removeChangeListener(final ChangeListener chl) {
        indexSupport.removeChangeListener(chl);
    }

    // TODO m | r
    public static class NoChildrenIndexSupport extends Index.Support {
        private CompositeActivity ref;
        private Lookup lookup;
        private ExtendableActivity[] activities;
        private Node[] myNodes;
        
        public NoChildrenIndexSupport(CompositeActivity ref, Lookup lookup) {
            this.ref = ref;
            this.lookup = lookup;
        }
        
        public Node[] getNodes() {
            if (ref == null) {
                return new Node[0];
            }
            List<Node> nodeList = new ArrayList<Node>();
            ExtendableActivity[] tmpActivities = ref.getActivities();
            if (activities != null && activities.equals(tmpActivities)) {
                return myNodes;
            } 
            
            if (tmpActivities == null) {
                return new Node[0];
            }
            
            for (ExtendableActivity elem : tmpActivities) {
                Node tmpNode = getNode(elem);
                if (tmpNode != null) {
                    nodeList.add(tmpNode);
                }
            }
            
            myNodes = nodeList.toArray(new Node[nodeList.size()]);
            activities = tmpActivities;
            
            return myNodes;
        }

        private Node getNode(ExtendableActivity activity) {
            if (activity == null) {
                return null;
            }
            NodeType nodeType = EditorUtil.getBasicNodeType(activity);
            NodeFactory factory = getNodeFactory();
            
            if (factory != null){
                return factory.createNode(
                        nodeType,
                        activity,
                        lookup);
            }
            return null;
        }
        
        private NodeFactory<NodeType> getNodeFactory() {
            return PropertyNodeFactory.getInstance();
        }
        
        public int getNodesCount() {
            return getNodes().length;
        }

        public void reorder(final int[] perm) {
            Node[] nodes = this.getNodes();
            if (nodes == null || nodes.length < 1) {
                return;
            }
            
            if (ref == null) {
                return;
            }
            
            final CompositeActivity parentEntity = ref;
            BpelModel model = parentEntity.getBpelModel();
            if (model == null) {
                return;
            }
            try {
                
                model.invoke(new Callable() {
                    public Object call() throws Exception {
                        reorderEntity(perm, parentEntity);

                        return null;
                    }
                }, null);
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
            // Else silently fail, as with reordering within category nodes.
            fireChangeEvent(new ChangeEvent(this));
        }
    }

    /**
     * Allows re-ordering of the child nodes.
     * TODO m
     */
    private class IndexSupport extends Index.Support {

        public void reorder(final int[] perm) {
            
            // Moving the last node of five to the second position results
            // in an array that looks like: [0, 2, 3, 4, 1]
            // This means that the first node stays first, the second
            // node is now third, and so on, while the last node is
            // now in the second position.

            // Because some nodes present the children of their only child
            // (e.g. simple type node), we need to get the node children
            // and ask the first one for its schema component. We assume
            // there is at least one child (otherwise this method would not
            // be invoked) and that all of the children have a common parent.
            Node[] nodes = getNodes();
            if (nodes == null || nodes.length < 1) {
                return;
            }
            
            Node parentNode = nodes[0].getParentNode();
            if (! (parentNode instanceof BpelNode)) {
                return;
            }
            
            Object parentRef = ((BpelNode)parentNode).getReference();
            if (! (parentRef instanceof CompositeActivity || parentRef instanceof Assign)) {
                return;
            }

            
            BpelModel model = ((Activity) parentRef).getBpelModel();
            if (model == null) {
                return;
            }
            try {
                if (parentRef instanceof CompositeActivity) {
                    final CompositeActivity parentEntity = (CompositeActivity)parentRef;

                    model.invoke(new Callable() {
                        public Object call() throws Exception {
                            reorderEntity(perm, parentEntity);
                            return null;
                        }
                    }, null);
                } else if (parentRef instanceof Assign) {
                    final Assign parentEntity = (Assign)parentRef;

                    model.invoke(new Callable() {
                        public Object call() throws Exception {
                            reorderEntity(perm, parentEntity);
                            return null;
                        }
                    }, null);
                }
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
            
            
//////            SchemaComponentNode scn = (SchemaComponentNode) nodes[0].
//////                    getCookie(SchemaComponentNode.class);
//////            SchemaComponent parent = null;
//////            if (scn != null) {
//////                parent = scn.getReference().get().getParent();
//////            } else {
//////                // Not a schema component node? May be a category node.
//////                CategoryNode cn = (CategoryNode) getNode().
//////                        getCookie(CategoryNode.class);
//////                if (cn != null) {
//////                    parent = cn.getReference().get();
//////                }
//////                // Else, it is unknown and we cannot reorder its children.
//////            }
//////            if (parent != null) {
//////                // Re-order the children in the model and let the nodes get
//////                // refreshed via the listeners.
//////                Model model = parent.getModel();
//////                try {
//////                    model.startTransaction();
//////                    List<SchemaComponent> children = parent.getChildren();
//////                    // Need to create a copy of the list since we would
//////                    // otherwise be mutating it locally and via the model.
//////                    children = new ArrayList<SchemaComponent>(children);
//////                    SchemaComponent[] arr = children.toArray(
//////                            new SchemaComponent[children.size()]);
//////                    for (int i = 0; i < arr.length; i++) {
//////                        children.set(perm[i], arr[i]);
//////                    }
//////                    // Make copies of the children. Need to make a copy,
//////                    // otherwise model says we are adding a node that is
//////                    // already a part of the tree.
//////                    List<SchemaComponent> copies = new ArrayList<SchemaComponent>();
//////                    for (SchemaComponent child : children) {
//////                        copies.add((SchemaComponent)child.copy(parent));
//////                    }
//////                    // Cannot remove children until after they are copied.
//////                    for (SchemaComponent child : children) {
//////                        model.removeChildComponent(child);
//////                    }
//////                    // Now add the copies back to the parent.
//////                    for (SchemaComponent copy : copies) {
//////                        model.addChildComponent(parent, copy, -1);
//////                    }
//////                } catch (IndexOutOfBoundsException ioobe) {
//////                    // This occurs for redefine node when user drags and drops.
//////                    // Need to silently fail, as with reordering category nodes.
//////                    return;
//////                } finally {
//////                    model.endTransaction();
//////                }
                // Notify listeners of the change.
//////                fireChangeEvent(new ChangeEvent(this));
//////            }
            // Else silently fail, as with reordering within category nodes.
            fireChangeEvent(new ChangeEvent(this));

        }

        public int getNodesCount() {
            return ActivityNodeChildren.this.getNodesCount();
        }

        public Node[] getNodes() {
            return ActivityNodeChildren.this.getNodes();
        }
    }

    private static void reorderEntity(int[] perm, CompositeActivity parent) {
        if (perm == null || parent == null) {
            return;
        }
        
        List<ExtendableActivity> childrenActivities = Arrays.asList(parent.getActivities());
        // Need to create a copy of the list since we would
        // otherwise be mutating it locally and via the model.
        childrenActivities = new ArrayList<ExtendableActivity>(childrenActivities);
        ExtendableActivity[] arr = childrenActivities.toArray(
                new ExtendableActivity[childrenActivities.size()]);
        for (int i = 0; i < arr.length; i++) {
            childrenActivities.set(perm[i], arr[i]);
        }
        
        
        // Make copies of the children. Need to make a copy,
        // otherwise model says we are adding a node that is
        // already a part of the tree.
        List<ExtendableActivity> copies = new ArrayList<ExtendableActivity>();
        for (ExtendableActivity child : childrenActivities) {
            copies.add((ExtendableActivity)child.cut());
        }
//        // Cannot remove children until after they are copied.
//        for (ExtendableActivity child : childrenActivities) {
//            parentEntity.remove(child);
//        }
        // Now add the copies back to the parent.
        for (ExtendableActivity copy : copies) {
            parent.addActivity(copy);
        }
    }

    // todo m
    private static void reorderEntity(int[] perm, Assign parent) {
        if (perm == null || parent == null) {
            return;
        }
        
        List<Copy> childrenActivities = parent.getChildren(Copy.class);
        // Need to create a copy of the list since we would
        // otherwise be mutating it locally and via the model.
        childrenActivities = new ArrayList<Copy>(childrenActivities);
        Copy[] arr = childrenActivities.toArray(
                new Copy[childrenActivities.size()]);
        for (int i = 0; i < arr.length; i++) {
            childrenActivities.set(perm[i], arr[i]);
        }
        
        
        // Make copies of the children. Need to make a copy,
        // otherwise model says we are adding a node that is
        // already a part of the tree.
        List<Copy> copies = new ArrayList<Copy>();
        for (Copy child : childrenActivities) {
            copies.add((Copy)child.cut());
        }
//        // Cannot remove children until after they are copied.
//        for (ExtendableActivity child : childrenActivities) {
//            parentEntity.remove(child);
//        }
        // Now add the copies back to the parent.
        for (Copy copy : copies) {
            parent.addAssignChild(copy);
        }
    }
}
