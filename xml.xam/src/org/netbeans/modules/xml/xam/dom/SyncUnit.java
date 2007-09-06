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
/** 
 *
 * @author Nam Nguyen
 */

package org.netbeans.modules.xml.xam.dom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class SyncUnit {
    private final DocumentComponent target;
    private List<ChangeInfo> changes = new ArrayList<ChangeInfo>();
    private List<DocumentComponent> toRemove = new ArrayList<DocumentComponent>();
    private List<DocumentComponent> toAdd = new ArrayList<DocumentComponent>();
    private Map<String, Attr> removedAttributes = new HashMap<String, Attr>();
    private Map<String, Attr> addedAttributes = new HashMap<String, Attr>();
    private boolean componentChanged;
    private boolean hasTextContentChanges = false;
    private Set<String> nonDomainedChanges = new HashSet<String>(); //tagname of top level non-domained element
    
    public SyncUnit(DocumentComponent syncTarget) {
        if (syncTarget == null) {
            throw new IllegalArgumentException("Null syncTarget");
        }
        target = syncTarget;
    }
    
    public void addChange(ChangeInfo change) {
        if (! target.referencesSameNode(change.getParent())) {
            throw new IllegalArgumentException("ChangeInfo does not match target id");
        }
        changes.add(change);
        if (change.getChangedNode() instanceof Attr)  {
            Attr attr = (Attr) change.getChangedNode();
            if (change.isAdded()) {
                addToAddedAttributes(attr);
            } else {
                addToRemovedAttributes(attr);
            }
        } else if (! change.isDomainElement()) {
            Node actualChanged = change.getActualChangedNode();
            if (! (actualChanged instanceof Attribute || actualChanged instanceof Element)) {
                // should be text, cdata, comment...
                if (actualChanged.getNodeType() != Node.TEXT_NODE || 
                    ((Text)actualChanged).getNodeValue().trim().length() != 0) {
                    setHasTextContentChanges(true);
                    addNonDomainedElementChange(change);
                }
            } else {
                addNonDomainedElementChange(change);
            }
        }
    }
    
    public List<ChangeInfo> getChanges() { return changes; }
    public DocumentComponent getTarget() { return target; }
    public List<DocumentComponent> getToRemoveList() { return toRemove; }
    public void addToRemoveList(DocumentComponent c) { 
        if (c == null) {
            throw new IllegalArgumentException("Null component");
        }
        toRemove.add(c); 
    }
    public List<DocumentComponent> getToAddList() { return toAdd; }
    public void addToAddList(DocumentComponent c) { 
        if (c == null) {
            throw new IllegalArgumentException("Null component");
        }
        toAdd.add(c); 
    }
    public void setComponentChanged(boolean v) { componentChanged = v; }
    public boolean isComponentChanged() { return componentChanged; }
    public void addToAddedAttributes(Attr attr) {
        addedAttributes.put(attr.getName(), attr);
    }
    public Map<String,Attr> getAddedAttributes() {
        return addedAttributes;
    }
    
    public Map<String,Attr> getRemovedAttributes() {
        return removedAttributes;
    }
    
    public void addToRemovedAttributes(Attr attr) {
        removedAttributes.put(attr.getName(), attr);
    }

    public void merge(SyncUnit su) {
        if (target != su.getTarget()) {
            throw new IllegalArgumentException("Invalid sync unit for merge");
        }
        changes.addAll(su.getChanges());
        for (String name : su.getRemovedAttributes().keySet()) {
            addToRemovedAttributes(su.getRemovedAttributes().get(name));
        }
        for (String name : su.getAddedAttributes().keySet()) {
            addToAddedAttributes(su.getAddedAttributes().get(name));
        }
        
        if (! su.getToAddList().isEmpty()) {
            HashSet<Element> addSet = new HashSet<Element>();
            for (DocumentComponent component : toAdd) {
                addSet.add(component.getPeer());
            }
            for (DocumentComponent component : su.getToAddList()) {
                if (! addSet.contains(component.getPeer())) {
                    toAdd.add(component);
                }
            }
        }

        for (DocumentComponent component : su.getToRemoveList()) {
            if (! toRemove.contains(component)) {
                toRemove.add(component);
            }
        }
    }
    
    public void updateTargetReference() {
        AbstractDocumentComponent component = (AbstractDocumentComponent) target;
        if (component != null) {
            component.updateReference(getParentToRootPath());
        }
    }
    
    public ChangeInfo getLastChange() {
        if (changes.size() > 0) {
            return changes.get(changes.size()-1);
        } else {
            return null;
        }
    }
    
    public List<Element> getParentToRootPath() {
        if (getLastChange() == null) {
            return Collections.emptyList();
        } else {
            return getLastChange().getParentToRootPath();
        }
    }
    
    public boolean hasTextContentChanges() {
        return hasTextContentChanges;
    }

    public void setHasTextContentChanges(boolean val) {
        hasTextContentChanges = val;
    }
    
    public Set<String> getNonDomainedElementChanges() {
        return nonDomainedChanges;
    }

    public void addNonDomainedElementChange(ChangeInfo change) {
        if (change.getChangedNode() instanceof Element) {
            nonDomainedChanges.add(((Element)change.getChangedNode()).getTagName());
        }
    }
    
    public boolean hasWhitespaceChangeOnly() {
        for (ChangeInfo ci : getChanges()) {
            if (ci.isDomainElement()) {
                continue;
            }
            Node n = ci.getActualChangedNode();
            if (n.getNodeType() == Node.TEXT_NODE) {
                String text = ((Text)n).getNodeValue();
                if (text != null && text.trim().length() > 0) {
                    return false;
                }
            } else if (n.getNodeType() == Node.ATTRIBUTE_NODE) {
                String name =  ((Attr) n).getName();
                Attr removed = getRemovedAttributes().get(name);
                if (removed == null) {
                    return false;
                }
                Attr added = getAddedAttributes().get(name);
                if (added == null) {
                    return false;
                }
                if (removed.getValue() == null || 
                    ! removed.getValue().equals(added.getValue())) {
                    return false;
                }
            } else {
                // node type must be either element or comment or cdata...
                return false;
            }
        }
        return true;
    }
}
