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

package org.netbeans.modules.xml.xdm.nodes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.xml.spi.dom.ROException;
import org.w3c.dom.NamedNodeMap;

/**
 * Read-only implementation of NamedNodeMap delegating to a Java <code>Map</code>.
 * The underlaying map must use {@link #createKey} as its keys. Also keeps
 * fidelity of the attribute order.
 *
 * @author  Ayub Khan
 */
public final class NamedNodeMapImpl implements NamedNodeMap {
    
    private final Map peer;
    private final List<String> keys;
    
    /** Read-only empty map. */
    public static final NamedNodeMap EMPTY = 
            new NamedNodeMapImpl(new ArrayList(0));
    
    /**
     * Creates new NamedNodeMapImpl
     * @param peer a map to delegate to. It must not be modified after this contructor call!
     */
    public NamedNodeMapImpl(List<Attribute> attributes) {
        if (attributes == null) throw new NullPointerException();
        List<String> keys = new ArrayList<String>();
        Map<String,Node> attributeMap = new LinkedHashMap<String,Node>();
        for(Attribute attr: attributes) {
            String key = attr.getName();
            keys.add(key);
            attributeMap.put(key,attr);
        }
        this.keys = keys;
        this.peer = attributeMap;
    }
    
    public int getLength() {
        return peer.size();
    }
    
    public org.w3c.dom.Node removeNamedItem(String str) 
    throws org.w3c.dom.DOMException {
        throw new ROException();
    }
    
    public org.w3c.dom.Node setNamedItemNS(org.w3c.dom.Node node) 
    throws org.w3c.dom.DOMException {
        throw new ROException();
    }
    
    public org.w3c.dom.Node setNamedItem(org.w3c.dom.Node node) 
    throws org.w3c.dom.DOMException {
        throw new ROException();
    }
    
    public org.w3c.dom.Node getNamedItemNS(String uri, String local) {
        return (Node) peer.get(createKey(uri, local));
    }
    
    public org.w3c.dom.Node item(int param) {
        if(param < keys.size())
            return (org.w3c.dom.Node) peer.get(keys.get(param));
        return null;
    }
    
    public org.w3c.dom.Node getNamedItem(String str) {
        return (Node) peer.get(createKey(str));
    }
    
    public org.w3c.dom.Node removeNamedItemNS(String str, String str1) 
    throws org.w3c.dom.DOMException {
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
