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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.xml.spi.dom.ROException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Read-only implementation of NamedNodeMap delegating to a Java <code>Map</code>.
 * The underlaying map must use {@link #createKey} as its keys. Also keeps
 * fidelity of the attribute order.
 *
 * @author  Ayub Khan
 */
public final class NamedNodeMapImpl implements NamedNodeMap {
    
    private List<Attribute> attributes;
    
    /** Read-only empty map. */
    public static final NamedNodeMap EMPTY = 
            new NamedNodeMapImpl(new ArrayList(0));
    
    /**
     * Creates new NamedNodeMapImpl
     * @param peer a map to delegate to. It must not be modified after this contructor call!
     */
    public NamedNodeMapImpl(List<Attribute> attributes) {
        if (attributes == null) throw new NullPointerException();
        this.attributes = new ArrayList(attributes);
    }
    
    public int getLength() {
        return attributes.size();
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
        String key = (String)createKey(uri, local);
        if(key == null) return null;
        return getNode(key);
    }
    
    public org.w3c.dom.Node item(int param) {
        if(param < attributes.size())
            return (org.w3c.dom.Node) attributes.get(param);
        return null;
    }
    
    public org.w3c.dom.Node getNamedItem(String str) {
        String key = (String)createKey(str);
        if(key == null) return null;
        return getNode(key);
    }
    
    public org.w3c.dom.Node removeNamedItemNS(String str, String str1) 
    throws org.w3c.dom.DOMException {
        throw new ROException();
    }
        
    private Node getNode(String key) {
        assert(key != null);        
        for(Attribute attr: attributes) {            
            if(key.equals(attr.getName())) {
                return attr;
            }
        }
        return null;
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