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

package org.netbeans.modules.xsl.grammar;

import org.w3c.dom.*;

/**
 *
 * @author  asgeir@dimonsoftware.com
 */
public class ResultElement extends ResultNode implements Element {

    private Element elem;

    /** Creates a new instance of ResultElement */
    public ResultElement(Element peer, String ignorePrefix, String onlyUsePrefix) {
        super(peer, ignorePrefix, onlyUsePrefix);
        elem = peer;
    }

    public String getAttribute(String name) {
        return elem.getAttribute(name);
    }

    public String getAttributeNS(String namespaceURI, String localName) {
        return elem.getAttributeNS(namespaceURI, localName);
    }
    
    public Attr getAttributeNode(String name) {
        return new ResultAttr(elem.getAttributeNode(name), ignorePrefix, onlyUsePrefix);
    }
    
    public Attr getAttributeNodeNS(String namespaceURI, String localName) {
        return new ResultAttr(elem.getAttributeNodeNS(namespaceURI,localName), ignorePrefix, onlyUsePrefix);
    }
    
    public NodeList getElementsByTagName(String name) {
        return new ResultNode.ResultNodeList(elem.getElementsByTagName(name));
    }
    
    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        return new ResultNode.ResultNodeList(elem.getElementsByTagNameNS(namespaceURI, localName));
    }
    
    public String getTagName() {
        return elem.getTagName();
    }
    
    public boolean hasAttribute(String name) {
        return elem.hasAttribute(name);
    }
    
    public boolean hasAttributeNS(String namespaceURI, String localName) {
        return elem.hasAttributeNS(namespaceURI, localName);
    }
    
    public void removeAttribute(String name) throws DOMException {
        elem.removeAttribute(name);
    }
    
    public void removeAttributeNS(String namespaceURI, String localName) throws DOMException {
        elem.removeAttributeNS(namespaceURI,localName);
    }
    
    public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
        return elem.removeAttributeNode(oldAttr);
    }
    
    public void setAttribute(String name, String value) throws DOMException {
        elem.setAttribute(name, value);
    }
    
    public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException {
        elem.setAttributeNS(namespaceURI, qualifiedName, value);
    }
    
    public Attr setAttributeNode(Attr newAttr) throws DOMException {
        return elem.setAttributeNode(newAttr);
    }
    
    public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
        return elem.setAttributeNode(newAttr);
    }
    
}
