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
