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

package org.netbeans.modules.xml.xam.dom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author Nam Nguyen
 */
public class ChangeInfo {
    
        private Node changed;
        private Element parent;
        private DocumentComponent parentComponent;
        private boolean domainElement;
        private boolean added;
        private List<Element> rootToParent;
        /**
         * List of non-domain element nodes beside the changed node.
         * The order is increasing distance from root.
         */
        private List<Node> otherNonDomainElementNodes;
        
        /**
         * Creates change info.
         *
         * @param parent parent node of changed
         * @param changed added/removed domain element or first non-domain change node.
         * @param isDomainElement is the changed node a domain element.
         * @param rootToParent path from root to parent node, inclusively.
         * @param otherNodes list of other nodes that are not domain elements beside the changed nodes.
         */
        public ChangeInfo(Element parent, Node changed, boolean isDomainElement, List<Element> rootToParent, List<Node> otherNodes) {
            this.parent = parent;
            this.changed = changed;
            domainElement = isDomainElement;
            if (! domainElement) {
                otherNonDomainElementNodes = otherNodes;
            }
            this.rootToParent = rootToParent;
        }
        public Element getParent() { return parent; }
        public Node getChangedNode() { return changed; }
        public Element getChangedElement() {
            if (changed instanceof Element) {
                return (Element) changed;
            }
            return null;
        }
        public boolean isDomainElement() { return domainElement; }
        public void setDomainElement(boolean v) { domainElement = v; }
        public void setRootToParentPath(List<Element> path) {
            rootToParent = path;
        }
        public List<Element> getRootToParentPath() {
            return rootToParent;
        }
        public List<Element> getParentToRootPath() {
            ArrayList<Element> ret = new ArrayList(rootToParent);
            Collections.reverse(ret);
            return ret;
        }
        public boolean isDomainElementAdded() {
            return domainElement && added;
        }
        public void setAdded(boolean v) {
            added = v;
        }
        public boolean isAdded() {
            return added;
        }
        public void markParentAsChanged() {
            assert parent != null;
            changed = parent;

            assert rootToParent.size() > 1;
            assert parent == rootToParent.get(rootToParent.size()-1);
            rootToParent.remove(rootToParent.size()-1);
            parent = rootToParent.get(rootToParent.size()-1);
        }
        public void setParentComponent(DocumentComponent component) {
            parentComponent = component;
        }
        public DocumentComponent getParentComponent() {
            return parentComponent;
        }
        public List<Node> getOtherNonDomainElementNodes() {
            return otherNonDomainElementNodes;
        }
        public Node getActualChangedNode() {
            if (isDomainElement()) {
                return changed;
            } else {
                if (otherNonDomainElementNodes == null || otherNonDomainElementNodes.isEmpty()) {
                    return changed;
                } else {
                    return otherNonDomainElementNodes.get(otherNonDomainElementNodes.size()-1);
                }
            }
        }
        public void markNonDomainChildAsChanged() {
            assert otherNonDomainElementNodes != null && otherNonDomainElementNodes.size() > 0;
            assert(changed instanceof Element);
            rootToParent.add((Element) changed);
            parent = (Element) changed;
            changed = otherNonDomainElementNodes.remove(0);
            parentComponent = null;
        }
        
        public String toString() {
            String op = added ? "ADD: " : "REMOVE: ";
            if (changed instanceof Element) {
                return op + ((Element)changed).getTagName();
            } else if (changed instanceof Attr) {
                return op + ((Attr)changed).getNodeName()+"="+((Attr)changed).getNodeValue();
            } else {
                return op + changed.getNodeValue();
            }
        }
}