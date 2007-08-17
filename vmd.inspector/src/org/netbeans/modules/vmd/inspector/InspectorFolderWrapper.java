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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.vmd.inspector;

import org.netbeans.modules.vmd.api.inspector.InspectorFolder;
import org.netbeans.modules.vmd.api.inspector.InspectorOrderingController;
import org.netbeans.modules.vmd.api.inspector.common.DefaultOrderingController;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.common.AcceptSuggestion;
import org.openide.nodes.AbstractNode;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;


/**
 *
 * @author Karol Harezlak
 */
final class InspectorFolderWrapper {
    
    private List<InspectorFolderWrapper> children;
    private InspectorFolder folder;
    private InspectorFolderNode node;
    private WeakReference<DesignComponent> component;
    private Collection<InspectorFolder> childrenFolders;
    private Set<InspectorFolderWrapper> toRemove;
    private Map<InspectorOrderingController, List<InspectorFolder>> ocMap;
    private Map<Integer, List<InspectorFolder>> sortedLists;
    private List<InspectorFolderWrapper> tempChildren;
    private InspectorOrderingController defaultOrderingController;
    private List<AbstractNode> childrenNode;
    private WeakReference<DesignDocument> document;
    
    InspectorFolderWrapper(DesignDocument document, InspectorFolder folder) {
        this.folder = folder;
        this.document = new WeakReference<DesignDocument>(document);
    }
    
    List<InspectorFolderWrapper> getChildren() {
        return children;
    }
    
    List <AbstractNode> getChildrenNodes() {
        if (children == null)
            return null;
        
        if (childrenNode == null)
            childrenNode = new ArrayList<AbstractNode>();
        else
            childrenNode.clear();
        
        for (InspectorFolderWrapper child : children) {
            childrenNode.add(child.getNode());
        }
        
        return childrenNode;
    }
    
    boolean removeChild(InspectorFolder folder) {
        if (children == null)
            return false;
        if (toRemove == null)
            toRemove = new HashSet<InspectorFolderWrapper>();
        else
            toRemove.clear();
        for (InspectorFolderWrapper child : children) {
            if (child.getFolder().equals(folder))
                toRemove.add(child);
        }
        if (toRemove == null || toRemove.isEmpty())
            return false;
        children.removeAll(toRemove);
        
        return true;
    }
    
    InspectorFolder getFolder(){
        return folder;
    }
    
    void setChildren(List<InspectorFolderWrapper> children) {
        if (children == null || this.children == children)
            return;
        
        this.children = children;
        if (childrenFolders == null)
            childrenFolders = new ArrayList<InspectorFolder>();
        else
            childrenFolders.clear();
        for (InspectorFolderWrapper wrapper : children){
            childrenFolders.add(wrapper.getFolder());
        }
    }
    
    InspectorFolderNode getNode() {
        return node;
    }
    
    DesignComponent getComponent() {
        if (component == null)
            return null;
        return component.get();
    }
    
    void resolveFolder(DesignDocument document) {
        if (node == null) {
            DataObjectContext dc = ProjectUtils.getDataObjectContextForDocument(document);
            if (dc != null) {
                node = new InspectorFolderNode(dc);
            } else  //TODO No Lookup for root node only
                node = new InspectorFolderNode();
        }
        if (folder.getComponentID() != null)
            component = new WeakReference<DesignComponent>(document.getComponentByUID(folder.getComponentID()));
        executeOrder();
        getNode().resolveNode(this, document);
    }
    
    Collection<InspectorFolder> getChildrenFolders() {
        return childrenFolders;
    }
    
    public AcceptSuggestion createSuggestion(Transferable transferable) {
        return folder.createSuggestion(transferable);
    }
    
    private void executeOrder() {
        if (children == null || children.isEmpty())
            return;
        
        if (ocMap == null )
            ocMap = new HashMap<InspectorOrderingController, List<InspectorFolder>>();
        else
            ocMap.clear();
        
        if (sortedLists == null)
            sortedLists = new TreeMap<Integer, List<InspectorFolder>>();
        else
            sortedLists.clear();
        
        if (tempChildren == null)
            tempChildren = new ArrayList<InspectorFolderWrapper>();
        else
            tempChildren.clear();
        
        if (defaultOrderingController == null)
            defaultOrderingController = new DefaultOrderingController(Integer.MAX_VALUE, new TypeID(TypeID.Kind.COMPONENT, "Default")); //NOI18N
        
        // sorting of descriptors based on TypeID
        for (InspectorFolder fd : childrenFolders) {
            boolean isWrite = false;
            if (this.getFolder().getOrderingControllers() != null) {
                for(InspectorOrderingController orderingController :  this.getFolder().getOrderingControllers()) {
                    if (orderingController.isTypeIDSupported(document.get(), fd.getTypeID()) && ocMap.get(orderingController) == null) {
                        ocMap.put(orderingController, new ArrayList<InspectorFolder>(Arrays.asList(fd)));
                        isWrite = true;
                    } else if (orderingController.isTypeIDSupported(document.get(), fd.getTypeID()) && ocMap.get(orderingController) != null) {
                        ocMap.get(orderingController).add(fd);
                        isWrite = true;
                    }
                }
                if ( !isWrite && ocMap.get(defaultOrderingController) == null)
                    ocMap.put(defaultOrderingController, new ArrayList<InspectorFolder>(Arrays.asList(fd)));
                else if (!isWrite)
                    ocMap.get(defaultOrderingController).add(fd);
            } else {
                if (ocMap.get(defaultOrderingController) == null)
                    ocMap.put(defaultOrderingController, new ArrayList<InspectorFolder>(Arrays.asList(fd)));
                else
                    ocMap.get(defaultOrderingController).add(fd);
            }
        }
        for(InspectorOrderingController oc : ocMap.keySet()) {
            List<InspectorFolder> sortedList;
            if (component != null)
                sortedList = oc.getOrdered(component.get(), Collections.unmodifiableList(ocMap.get(oc)));
            else
                sortedList = oc.getOrdered(null, Collections.unmodifiableList(ocMap.get(oc)));
            if (sortedList == null)
                throw new IllegalArgumentException("List returned from InspectorOrderingController is null, controller:" + oc); //NOI18N
            
            //if (sortedList.size() != ocMap.get(oc).size() || (! ocMap.get(oc).containsAll(sortedList)))
            //    Debug.warning("Elements passed to sort in component: "+ component + " are diffrent from elements returned from FolderOrderingController :" + oc.getClass()); //NOI18N
            
            if (oc.getOrder() != null && sortedLists.get(oc.getOrder()) == null)
                sortedLists.put(oc.getOrder(), sortedList);
            else if (oc.getOrder() != null && sortedLists.get(oc.getOrder()) != null)
                sortedLists.get(oc.getOrder()).addAll(sortedList);
        }
        
        childrenFolders.clear();
        
        for (List<InspectorFolder> fdList : sortedLists.values()) {
            childrenFolders.addAll(fdList);
        }
        
        for (InspectorFolder fd : childrenFolders) {
            for (InspectorFolderWrapper wrapper : children ) {
                if (wrapper.getFolder() == fd && fd.getComponentID().equals(wrapper.getFolder().getComponentID()))
                    tempChildren.add(wrapper);
            }
        }
        
        children = new ArrayList<InspectorFolderWrapper>(tempChildren);
    }
    
    void terminate() {
        folder = null;
        if (node != null) {
            node.terminate();
            try {
                node.destroy();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        node = null;
        component = null;
        childrenFolders = null;
        toRemove = null;
        ocMap = null;
        sortedLists = null;
        tempChildren = null;
        defaultOrderingController = null;
        childrenNode = null;
        if (children == null)
            return;
        for (InspectorFolderWrapper wrapper : children) {
            wrapper.terminate();
        }
        children = null;
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer()
                .append("[ ")  // NOI18N
                .append(folder.getDisplayName())
                .append(" ] TYPE : ")  // NOI18N
                .append( folder.getTypeID())
                .append(", ID : ") //NOI18N
                .append(folder.getComponentID())
                .append(", Children : ")  // NOI18N
                .append(children == null ? 0 : children.size())
                .append(" " + super.toString());
        return buffer.toString();
    }
    
}
