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

import org.w3c.dom.*;
import java.util.*;

/**
 * Read-only implementation of NamedNodeMap delegating to a Java <code>Map</code>.
 * The underlaying map must use {@link #createKey} as its keys.
 *
 * @author  Petr Kuzel
 */
public final class NamedNodeMapImpl implements NamedNodeMap {

    private final Map peer;

    /** Read-only empty map. */
    public static final NamedNodeMap EMPTY = new NamedNodeMapImpl(new HashMap(0));

    /**
     * Creates new NamedNodeMapImpl
     * @param peer a map to delegate to. It must not be modified after this contructor call!
     */
    public NamedNodeMapImpl(Map peer) {
        if (peer == null) throw new NullPointerException();
        this.peer = peer;
    }

    public int getLength() {
        return peer.size();
    }
    
    public org.w3c.dom.Node removeNamedItem(String str) throws org.w3c.dom.DOMException {
        throw new ROException();
    }
    
    public org.w3c.dom.Node setNamedItemNS(org.w3c.dom.Node node) throws org.w3c.dom.DOMException {
        throw new ROException();
    }
    
    public org.w3c.dom.Node setNamedItem(org.w3c.dom.Node node) throws org.w3c.dom.DOMException {
        throw new ROException();
    }
    
    public org.w3c.dom.Node getNamedItemNS(String uri, String local) {
        return (Node) peer.get(createKey(uri, local));
    }
    
    public org.w3c.dom.Node item(int param) {
        int i = 0;
        Iterator it = peer.values().iterator(); 
        while (it.hasNext()) {
            Object next = it.next();
            if (param == i) return (Node) next;
            i++;
        }
        return null;
    }
    
    public org.w3c.dom.Node getNamedItem(String str) {
        return (Node) peer.get(createKey(str));
    }
    
    public org.w3c.dom.Node removeNamedItemNS(String str, String str1) throws org.w3c.dom.DOMException {
        throw new ROException();
    }

    public String toString() {
        return peer.toString();
    }

    /**
     * Create proper key for map entry
     */
    public static Object createKey(String qname) {
        return qname;
    }

    /**
     * Create proper key for map entry
     */    
    public static Object createKey(String uri, String local) {
        return uri + ":" + local;                                               // NOI18N
    }
    
}
