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

import org.w3c.dom.*;

/**
 * Neutral DOM level 1 Core Node implementation.
 * All methods return <code>null</code> or <code>false</code>
 * or throws an exception if they attempt to modify DOM tree
 * or they are defined at higher DOM level. Clone method
 * returns <code>this</code> as it probably safe for read-only DOM.
 * <p>
 * As a bonus it also implements some other DOM interfaces
 * by throwing a DOMException.
 *
 * @author  Petr Kuzel
 */
public abstract class AbstractNode implements Node {
    
    public String getNodeName() {
        return null;
    }

    /**
     * @return false
     */
    public boolean isSupported(String feature, String version) {
        return "1.0".equals(version);
    }
    
    public void setPrefix(String str) throws org.w3c.dom.DOMException {
        throw new ROException();
    }
    
    public String getPrefix() {
        return null;    // some client determines DOM1 by NoSuchMethodError
    }

    public org.w3c.dom.Node getPreviousSibling() {
        return null;
    }
    
    //!!! rather abstract to force all to reimplement
    public abstract short getNodeType();
    
    public org.w3c.dom.Document getOwnerDocument() {
        // let it be the first item
        return null;
    }
    
    public org.w3c.dom.Node replaceChild(org.w3c.dom.Node node, org.w3c.dom.Node node1) throws org.w3c.dom.DOMException {
        throw new ROException();
    }
    
    public org.w3c.dom.Node cloneNode(boolean param) {
        return (Node) this;  //we are immutable, only problem with references may appear
    }
    
    public org.w3c.dom.Node getNextSibling() {
        return null;
    }
    
    public org.w3c.dom.Node insertBefore(org.w3c.dom.Node node, org.w3c.dom.Node node1) throws org.w3c.dom.DOMException {
        throw new ROException();
    }
    
    public String getNamespaceURI() {
        return null;    // some client determines DOM1 by NoSuchMethodError
    }
    
    public org.w3c.dom.NamedNodeMap getAttributes() {
        return NamedNodeMapImpl.EMPTY;
    }
    
    public org.w3c.dom.NodeList getChildNodes() {       
        return NodeListImpl.EMPTY;
    }
    
    public String getNodeValue() throws org.w3c.dom.DOMException {
        // attribute, text, pi data
        return null;
    }
    
    public org.w3c.dom.Node appendChild(org.w3c.dom.Node node) throws org.w3c.dom.DOMException {
        throw new ROException();
    }
    
    public String getLocalName() {
        return null;    // some client determines DOM1 by NoSuchMethodError
    }
    
    public org.w3c.dom.Node getParentNode() {
        return null;
    }
    
    public void setNodeValue(String str) throws org.w3c.dom.DOMException {
        throw new ROException();
    }
    
    public org.w3c.dom.Node getLastChild() {
        return null;
    }
    
    public boolean hasAttributes() {
        throw new UOException();    
    }
    
    public void normalize() {
        // ignore
    }
    
    public org.w3c.dom.Node removeChild(org.w3c.dom.Node node) throws org.w3c.dom.DOMException {
        throw new ROException();
    }
    
    /**
     * @return false
     */
    public boolean hasChildNodes() {
        return false;
    }
    
    /**
     * @return null
     */
    public org.w3c.dom.Node getFirstChild() {
        return null;
    }
    
    
    // A bonus Element interface ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`
    
    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        throw new UOException();
    }

    public String getAttributeNS(String namespaceURI, String localName) {
        throw new UOException();        
    }

    public String getAttribute(String name) {
        throw new UOException();        
    }

    public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
        throw new UOException();
    }

    public Attr getAttributeNode(String name) {
        throw new UOException();
    }

    public boolean hasAttribute(String name) {
        throw new UOException();        
    }

    public String getTagName() {
        throw new UOException();        
    }

    public Attr getAttributeNodeNS(String namespaceURI, String localName) {
        throw new UOException();        
    }

    public void removeAttribute(String name) throws DOMException {
        throw new UOException();        
    }

    public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
        throw new UOException();        
    }

    public void setAttribute(String name, String value) throws DOMException {
        throw new UOException();        
    }

    public NodeList getElementsByTagName(String name) {
        throw new UOException();        
    }

    public boolean hasAttributeNS(String namespaceURI, String localName) {
        throw new UOException();        
    }

    public Attr setAttributeNode(Attr newAttr) throws DOMException {
        throw new UOException();        
    }

    public void removeAttributeNS(String namespaceURI, String localName) throws DOMException {
        throw new UOException();        
    }

    public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException {
        throw new UOException();        
    }

    
    // A bonus Attr implementation ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    public boolean getSpecified() {
        throw new UOException();
    }

    public String getName() {
        throw new UOException();                
    }

    public Element getOwnerElement() {
        throw new UOException();                
    }

    public void setValue(String value) throws DOMException {
        throw new UOException();                
    }

    public String getValue() {
        throw new UOException();        
    }

    // Notation ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    public String getPublicId() {
        throw new UOException();                
    }        

    public String getSystemId() {
        throw new UOException();
    }
    
    
}
