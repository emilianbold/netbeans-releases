/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.spi.dom;

import java.util.List;

import org.w3c.dom.*;

/**
 * A simple implementation of NodeList wrapping Java <code>List</code>.
 *
 * @author  Petr Kuzel
 */
public final class NodeListImpl implements NodeList {

    public static final NodeList EMPTY = new NodeList() {
        public int getLength() { return 0; }
        public org.w3c.dom.Node item(int i) { return null; }        
        public String toString() { return "NodeListImpl.EMPTY"; }
    };
    
    private final List peer;
    
    /** 
     * Creates new NodeListImpl */
    public NodeListImpl(List l) {
        peer = l;
    }

    public int getLength() {
        return peer.size();
    }
    
    public org.w3c.dom.Node item(int i) {
        return (Node) peer.get(i);
    }
      
    public String toString() {
        return peer.toString();
    }
}
