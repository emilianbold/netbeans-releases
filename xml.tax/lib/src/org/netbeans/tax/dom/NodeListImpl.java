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


package org.netbeans.tax.dom;

import java.util.Iterator;
import org.w3c.dom.*;
import org.netbeans.tax.*;

/**
 * NodeList taht filters out all unwrappable types.
 *
 *
 * @author  Petr Kuzel
 */
class NodeListImpl implements NodeList {

    public static final NodeList EMPTY = new NodeList() {
        public int getLength() { return 0; }
        public org.w3c.dom.Node item(int i) { return null; }
        public String toString() { return "NodeListImpl.EMPTY"; }
    };


    private final TreeObjectList peer;

    public NodeListImpl(TreeObjectList peer) {
        this.peer = peer;
    }
    
    /** The number of nodes in the list. The range of valid child node indices
     * is 0 to <code>length-1</code> inclusive.
     *
     */
    public int getLength() {
        int i = 0;
        Iterator it = peer.iterator();
        while (it.hasNext()) {
            TreeObject next = (TreeObject) it.next();
            if (accept(next)) i++;
        }
        return i;
    }
    
    /** Returns the <code>index</code>th item in the collection. If
     * <code>index</code> is greater than or equal to the number of nodes in
     * the list, this returns <code>null</code>.
     * @param index Index into the collection.
     * @return The node at the <code>index</code>th position in the
     *   <code>NodeList</code>, or <code>null</code> if that is not a valid
     *   index.
     *
     */
    public Node item(int index) {
        int i = 0;
        Iterator it = peer.iterator();
        while (it.hasNext()) {
            TreeObject next = (TreeObject) it.next();
            if (accept(next)) {
                if (i == index) {
                    return Wrapper.wrap((TreeObject)peer.get(index));
                } else {
                    i++;
                }
            }
        }
        return null;                
    }

    /**
     * Supported types.
     */
    private boolean accept(TreeObject o) {
        return o instanceof TreeText || o instanceof TreeElement;
    }
}
