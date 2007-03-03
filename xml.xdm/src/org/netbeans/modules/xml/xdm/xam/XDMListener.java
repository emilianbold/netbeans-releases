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

package org.netbeans.modules.xml.xdm.xam;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.ChangeInfo;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.dom.SyncUnit;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.netbeans.modules.xml.xdm.diff.NodeInfo;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Document;

/**
 * @author Nam Nguyen
 * @author ajit
 */
public class XDMListener implements PropertyChangeListener {
    
    private AbstractDocumentModel model;
    private boolean inSync;
    private Document oldDocument;
    
    /** Creates a new instance of XDMListener */
    public XDMListener(AbstractDocumentModel model) {
        this.model = model;
    }
    
    private XDMModel getXDMModel() {
        return ((XDMAccess) model.getAccess()).getXDMModel();
    }
    
    void startSync() {
        inSync = true;
        syncUnits.clear();
        oldDocument = getXDMModel().getCurrentDocument();
        getXDMModel().addPropertyChangeListener(this);
    }
    
    Document getOldDocument() {
        return oldDocument;
    }
    
    void endSync() {
        endSync(true);
    }
    
    void endSync(boolean processing) {
        getXDMModel().removePropertyChangeListener(this);
        try {
            if (processing) {
                for (SyncUnit unit : syncUnits.values()) {
                    model.processSyncUnit(unit);
                }
            }
        } finally {
            syncUnits.clear();
            inSync = false;
        }
    }
    
    private Map<Integer, SyncUnit> syncUnits = new HashMap<Integer, SyncUnit>();
    private static Integer getID(ChangeInfo change) {
        if (change.getParent() == null) {
            return Integer.valueOf(0);
        } else {
            Element parent = (Element) change.getParent();
            return Integer.valueOf(parent.getId());
        }
    }
    private static Integer getID(DocumentComponent c) {
        if (c == null) {
            return Integer.valueOf(0);
        }
        Element xdmElement = (Element) ((AbstractDocumentComponent)c).getPeer();
        return Integer.valueOf(xdmElement.getId());
    }
    
    protected void processChange(ChangeInfo change) {
        Integer unitID = getID(change);
        SyncUnit existing = syncUnits.get(unitID);
        SyncUnit su = model.prepareSyncUnit(change, existing);
        if (su == null) {
            return;
        }
        Integer reviewedID = getID(su.getTarget());
        existing = syncUnits.get(reviewedID);
        if (existing == null) {
            if (unitID.equals(reviewedID)) {
                // normal new sync order
                syncUnits.put(reviewedID, su);
            } else {
                existing = syncUnits.get(reviewedID);
                if (existing != null) {
                    existing.merge(su);
                } else {
                    syncUnits.put(reviewedID, su);
                }
            }
        } else {
            if (existing != su) {
                if (unitID.equals(reviewedID)) {
                    existing.merge(su);
                } else {
                    // existing sync unit replaced by reviewed one on ancestor of original target
                    syncUnits.remove(unitID);
                    existing = syncUnits.get(reviewedID);
                    if (existing != null) {
                        existing.merge(su);
                    } else {
                        syncUnits.put(reviewedID, su);
                    }
                }
            }
        }
    }

    protected void processEvent(Node eventNode, List<Node> pathToRoot, boolean isAdded) {
        if (pathToRoot.size() == 1) {
            assert pathToRoot.get(0) instanceof Document;
            if (! (eventNode instanceof Element)) {
                return;
            }
            //assert eventNode.getId() == 1;
            if (! isAdded) {
                return;
            }
            Component rootComponent = null;
            String errorMessage = null;
            try {
                rootComponent = model.createRootComponent((Element) eventNode);
            } catch(IllegalArgumentException e) {
                errorMessage = e.getMessage();
            }
            if (rootComponent == null) {
                errorMessage = errorMessage != null ? errorMessage :
                    "Unexpected root element "+AbstractDocumentComponent.getQName(eventNode);
                throw new IllegalArgumentException(new IOException(errorMessage));
            }
            model.firePropertyChangeEvent(new PropertyChangeEvent(model,
                Model.STATE_PROPERTY, Model.State.NOT_SYNCED, Model.State.VALID));
        } else {
            if (eventNode.getId() == pathToRoot.get(0).getId()) {
                throw new IllegalArgumentException("Event node has same id as parent");
            }
            pathToRoot = new ArrayList(pathToRoot);
            pathToRoot.add(0, eventNode);
            ChangeInfo change = model.prepareChangeInfo(toDomNodes(pathToRoot));
            change.setAdded(isAdded);
            processChange(change);
        }
    }
    
    public void propertyChange(PropertyChangeEvent event) {
        if (!inSync) return;
        
        NodeInfo oldInfo = (NodeInfo) event.getOldValue();
        NodeInfo newInfo = (NodeInfo) event.getNewValue();
        
        Node old = oldInfo!=null?(Node) oldInfo.getNode():null;
        Node now = newInfo!=null?(Node) newInfo.getNode():null;
        
        if (old != null) {			
            processEvent(old, oldInfo.getNewAncestors(), false);
        }
        
        if (now != null) {
            processEvent(now, newInfo.getNewAncestors(), true);
        }
    }

    static List<org.w3c.dom.Node> toDomNodes(List<Node> nodes) {
        List<org.w3c.dom.Node> domNodes = new ArrayList<org.w3c.dom.Node>();
        for (Node n : nodes) {
            domNodes.add((org.w3c.dom.Node) n);
        }
        return domNodes;
    }
}

