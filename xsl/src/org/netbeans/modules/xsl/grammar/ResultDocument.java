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

package org.netbeans.modules.xsl.grammar;

import org.w3c.dom.*;

/**
 *
 * @author  asgeir@dimonsoftware.com
 */
public class ResultDocument extends ResultNode implements org.w3c.dom.Document {
    
    private Document doc;
    
    /** Creates a new instance of ResultDocument */
    public ResultDocument(Document peer, String ignorePrefix, String onlyUsePrefix) {
        super(peer, ignorePrefix, onlyUsePrefix);
        doc = peer;
    }
    
    public Attr createAttribute(String name) throws DOMException {
        return doc.createAttribute(name);
    }
    
    public Attr createAttributeNS(String namespaceURI, String qualifiedName) throws DOMException {
        return doc.createAttributeNS(namespaceURI, qualifiedName);
    }
    
    public CDATASection createCDATASection(String data) throws DOMException {
        return doc.createCDATASection(data);
    }
    
    public Comment createComment(String data) {
        return doc.createComment(data);
    }
    
    public DocumentFragment createDocumentFragment() {
         return doc.createDocumentFragment();
    }
    
    public Element createElement(String tagName) throws DOMException {
        return doc.createElement(tagName);
    }
    
    public Element createElementNS(String namespaceURI, String qualifiedName) throws DOMException {
        return doc.createElementNS(namespaceURI, qualifiedName);
    }
    
    public EntityReference createEntityReference(String name) throws DOMException {
        return doc.createEntityReference(name);
    }
    
    public ProcessingInstruction createProcessingInstruction(String target, String data) throws DOMException {
        return doc.createProcessingInstruction(target, data);
    }
    
    public Text createTextNode(String data) {
        return doc.createTextNode(data);
    }
    
    public DocumentType getDoctype() {
        return doc.getDoctype();
    }
    
    public Element getDocumentElement() {
        return doc.getDocumentElement();
    }
    
    public Element getElementById(String elementId) {
        return doc.getElementById(elementId);
    }
    
    public NodeList getElementsByTagName(String tagname) {
        return doc.getElementsByTagName(tagname);
    }
    
    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        return doc.getElementsByTagNameNS(namespaceURI, localName);
    }
    
    public DOMImplementation getImplementation() {
        return doc.getImplementation();
    }
    
    public Node importNode(Node importedNode, boolean deep) throws DOMException {
        return new ResultNode(doc.importNode(importedNode, deep), ignorePrefix, onlyUsePrefix);
    }
}
