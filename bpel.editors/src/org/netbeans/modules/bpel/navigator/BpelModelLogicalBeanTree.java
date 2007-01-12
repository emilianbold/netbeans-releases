/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.bpel.navigator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;

import javax.swing.SwingUtilities;
import javax.swing.tree.TreeSelectionModel;

import org.netbeans.modules.bpel.core.BPELDataEditorSupport;
import org.netbeans.modules.bpel.core.multiview.spi.MultiviewId;
import org.netbeans.modules.bpel.design.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.events.ArrayUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.ChangeEventListener;
import org.netbeans.modules.bpel.model.api.events.EntityInsertEvent;
import org.netbeans.modules.bpel.model.api.events.EntityRemoveEvent;
import org.netbeans.modules.bpel.model.api.events.EntityUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyRemoveEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyUpdateEvent;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.nodes.BpelProcessNode;
import org.netbeans.modules.bpel.nodes.navigator.Util;
import org.netbeans.modules.bpel.nodes.refactoring.UsageFilterNode;
import org.netbeans.modules.bpel.properties.NodeUtils;
import org.netbeans.modules.xml.xam.Model;
import org.openide.ErrorManager;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 * Created on 15 December 2005
 *
 * Listen to the model state changes, in case invalid bpel document state -
 * show invalid state message.
 *
 */
public class BpelModelLogicalBeanTree
        implements PropertyChangeListener, ChangeEventListener 
{
    
    private static final long serialVersionUID = 1L;
    
    private BpelModel myBpelModel;
    //context lookup
    private Lookup myContextLookup;
    
    private BeanTreeView myBeanTreeView;
    private ExplorerManager myExplorerManager;
    
    public BpelModelLogicalBeanTree(ExplorerManager explorerManager,
            BpelModel bpelModel,
            Lookup contextLookup) 
    {
        myBpelModel = bpelModel;
        myExplorerManager = explorerManager;
        myExplorerManager.addPropertyChangeListener(this);
        
        myContextLookup = contextLookup;
        
        myBeanTreeView = createBeanTreeView();
        
        //add TopComponent Active Node changes listener :
        TopComponent.getRegistry().addPropertyChangeListener(this);
        myBpelModel.addEntityChangeListener((ChangeEventListener)this);
    }
    
    public void removeListeners() {
        if (myExplorerManager != null) {
            myExplorerManager.removePropertyChangeListener(this);
        }
        TopComponent.getRegistry().removePropertyChangeListener(this);
        if (myBpelModel != null) {
            myBpelModel.removeEntityChangeListener((ChangeEventListener)this);
        }
        myBpelModel = null;
        myExplorerManager = null;
        
    }
    
    private BeanTreeView createBeanTreeView() {
        BeanTreeView beanTreeView = new BeanTreeView();
        beanTreeView.setRootVisible(true);
        beanTreeView.setEnabled(true);
        beanTreeView.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        beanTreeView.setDefaultActionAllowed(true);
        doTreeNodeSelectionByActiveNode();
        
        return beanTreeView;
    }
    
    public BeanTreeView getBeanTreeView() {
        return myBeanTreeView;
    }
    
    private void doTreeNodeSelectionByActiveNode() {
        Node[] nodes = TopComponent.getRegistry().getActivatedNodes();
        if (nodes == null || nodes.length == 0) {
            
            return;
        }
        
        for (Node elem : nodes) {
            if (elem instanceof UsageFilterNode) {
                elem = ((UsageFilterNode)elem).getOriginal();
            }
            
            if (!(elem instanceof BpelNode)
            || ((BpelNode)elem).getNodeType().equals(NodeType.SCHEMA_ELEMENT)
            || !(((BpelNode)elem).getReference() instanceof BpelEntity)) 
            {
                continue;
            }
            BpelEntity refBpelEntityObj = BpelEntity.class.cast(
                    ((BpelNode)elem).getReference());
            if (refBpelEntityObj != null) {
                doTreeNodeSelection((BpelNode)elem);
            }
            //just one node can be selected in navigator bpel logical View
            break;
        }
    }
    
    private void doTreeNodeSelection(BpelNode bpelNode) {
        
        try {
//         myBeanTreeView.expandAll();
            Node node2sel = Util.findBpelNode(myExplorerManager.getRootContext()
            ,bpelNode.getReference());
            if (node2sel == null) {
                return;
            }
            
            myExplorerManager.setSelectedNodes(new Node[] {node2sel});
        } catch (PropertyVetoException ex) {
            ErrorManager.getDefault().notify(ex);
            ex.printStackTrace();
        }
    }
    
    private void openVisualBpelEditor(BpelNode bpelNode) {
        
        if (bpelNode == null) {
            return;
        }
        
        NodeUtils.showNodeCustomEditor(bpelNode);
    }
    
    private Lookup getContextLookup() {
        return myContextLookup;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        TopComponent navigatorTopComponent 
                = BpelNavigatorController.getNavigatorTC();
        
        if (propertyName.equals(TopComponent.Registry.PROP_ACTIVATED)) {
            if (TopComponent.getRegistry().getActivated() == navigatorTopComponent) {
                addUndoManager();
            }
            else {
                removeUndoManager();
            }
        }
        else if (propertyName.equals(TopComponent.Registry.PROP_ACTIVATED_NODES)) {
           if (TopComponent.getRegistry().getActivated() != navigatorTopComponent) {
               doTreeNodeSelectionByActiveNode();
           }
           return;
            
        } else if (propertyName.equals(ExplorerManager.PROP_SELECTED_NODES)) {
            if (navigatorTopComponent == null) {
                return;
            }
            // NAVIGATOR SELECTED NODES SETTED AS ACTIVE NODES
            //navigatorTopComponent.setActivatedNodes(new Node[] {});
            navigatorTopComponent.setActivatedNodes((Node[])evt.getNewValue());
        } else if (propertyName.equals(ExplorerManager.PROP_ROOT_CONTEXT)) {
            //EVENT FOR PROPERTY PROP_ROOT_CONTEXT
            doTreeNodeSelectionByActiveNode();
        } else if (propertyName.equals(TopComponent.Registry.PROP_OPENED)) {
            // System.out.println("the set of the opened topComponent were changed");
            BpelNavigatorController.switchNavigatorPanel();
        }
    }
    
    public void notifyPropertyRemoved(PropertyRemoveEvent event) {
        
    }
    
    public void notifyEntityInserted(final EntityInsertEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                selectEntity(event.getValue());
            }
        });
    }
    
    public void notifyPropertyUpdated(final PropertyUpdateEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                String name = event.getName();
                
                if (name.equals(BpelModel.STATE)){
                    if (!event.getNewValue().equals(Model.State.VALID)) {
                        myBeanTreeView.setEnabled(false);
                        DataObject dataObject = (DataObject) myContextLookup
                                .lookup(DataObject.class);
                        if (dataObject != null) {
                            Node node = dataObject.getNodeDelegate();
                            TopComponent tc = BpelNavigatorController
                                    .getNavigatorTC();
                            
                            if ((node != null) && (tc != null)) {
                                tc.setActivatedNodes(new Node[] { node });
                            }
                        }
                    } else {
                        myBeanTreeView.setEnabled(true);
                        ExplorerManager explorerManager
                                = ExplorerManager.find(myBeanTreeView);
                        if (explorerManager != null
                            && !(explorerManager.getRootContext() instanceof BpelProcessNode))
                        {
                            ((BpelNavigatorVisualPanel)myBeanTreeView.getParent())
                                    .navigate(myContextLookup, myBpelModel);
                        }
                    }
                }
            }
        });
    }
    
    public void notifyEntityRemoved(final EntityRemoveEvent event) {
    }
    
    public void notifyEntityUpdated(final EntityUpdateEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                selectEntity(event.getNewValue());
            }
        });
    }
    
    public void notifyArrayUpdated(ArrayUpdateEvent event) {
    }
    
    private void selectEntity(BpelEntity entity){
        if (entity == null){
            return;
        }
        final BpelNode node = Util.findBpelNode(myExplorerManager.getRootContext()
        ,entity);
        if (node == null) {
            return;
        }
        try {
            myExplorerManager.setSelectedNodes(new Node[]{node});
        } catch (PropertyVetoException ex) {
            //JUST IGNORE
        }
    }
    
    /**
     * Adds the undo/redo manager to the schema model as an undoable
     * edit listener, so it receives the edits onto the queue.
     */
    private void addUndoManager() {             /*
        ((BPELDataEditorSupport) myContextLookup
                .lookup(BPELDataEditorSupport.class))
                .addUndoManager(MultiviewId.NAVIGATOR);*/
    }
    
    /**
     * Removes the undo/redo manager undoable edit listener from the
     * schema model, to stop receiving undoable edits.
     */
    private void removeUndoManager() {          /*
        ((BPELDataEditorSupport) myContextLookup
                .lookup(BPELDataEditorSupport.class))
                .removeUndoManager(MultiviewId.NAVIGATOR);
                                                  */
    }
}

