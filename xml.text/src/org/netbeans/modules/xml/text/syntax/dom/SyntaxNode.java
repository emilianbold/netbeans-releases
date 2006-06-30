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

package org.netbeans.modules.xml.text.syntax.dom;

import org.w3c.dom.*;
import org.netbeans.modules.xml.text.syntax.*;
import org.netbeans.modules.xml.spi.dom.*;
import org.netbeans.editor.*;

/**
 * It represents higher level abstraction elements implementing
 * DOM Node interface.
 * <p>
 * Known differences to DOM specs:
 * <li> <code>getParentNode()</code> may return <code>null</code>
 * <li> NOT_SUPPORTED_ERR is thrown from <code>get_XXX_By_YYY()</code>
 * <li> implements <code>equals</code>ity at DOM Node level
 * <p>
 * Instances are produced by {@link XMLSyntaxSupport}.
 *
 * @author  Petr Kuzel
 *
 * @version 1.0
 */

public abstract class SyntaxNode extends SyntaxElement implements Node {

    /** Creates new SyntaxNode */
    public SyntaxNode(XMLSyntaxSupport support, TokenItem first, int to)  {
        super( support, first, to);
    }

    /**
     * Default implementation returning first previous <code>SyntaxNode</code>
     * or <code>null</code>. It is <code>StartTag</code> aware.
     */
    public Node getPreviousSibling() {
        SyntaxNode prev = findPrevious(this);
        
        // stop at start tag (it forms hiearchy)
        if (prev instanceof StartTag) {
            return null;
        } else {
            return prev;
        }
    }

    /**
     * Find previous SyntaxNode instance or <code>null</code>.
     */
    static SyntaxNode findPrevious(SyntaxElement el) {
        SyntaxElement prev = el.getPrevious();
        while ((prev instanceof SyntaxNode) == false) {
            if (prev == null) return null;            
            prev = prev.getPrevious();
        }
        return (SyntaxNode) prev;
    }

    /**
     * Find previous SyntaxNode instance or <code>null</code>.
     */    
    SyntaxNode findPrevious() {
        return findPrevious(this);
    }
    
    /**
     * Default implementation returning first next <code>SyntaxNode</code>
     * or <code>null</code>. It is <code>EndTag</code> aware.
     */    
    public Node getNextSibling() {        
        SyntaxNode next = findNext(this);
        
        // stop at end tag (it forms hiearchy)
        if (next instanceof EndTag) {
            return null;
        } else {
            return next;
        }

    }

    
    /**
     * Find previous SyntaxNode instance or <code>null</code>.
     */
    static SyntaxNode findNext(SyntaxElement el) {
        SyntaxElement next = el.getNext();
        while ((next instanceof SyntaxNode) == false) {
            if (next == null) return null;            
            next = next.getNext();
        }
        return (SyntaxNode) next;
    }

    /**
     * Find previous SyntaxNode instance or <code>null</code>.
     */
    SyntaxNode findNext() {
        return findNext(this);
    }
    
    /**
     * First previous start tag at higher level is my parent.
     * Skip all end-tag start-tag pairs at the same level.
     * @return SyntaxNode or <code>null</code>
     */
    public Node getParentNode() {
        SyntaxNode prev = findPrevious();
        
        do {

            while ( prev != null )  {
                if (prev instanceof StartTag) {
                    return (Element) prev;
                } else if (prev instanceof EndTag) {       // traverse end-start tag pairs
                    prev = ((EndTag)prev).getStartTag(); 
                    if (prev == null) break;                
                    prev = prev.findPrevious();
                } else {
                    prev = prev.findPrevious();
                }
            }
            
            if (prev == null) break;
            
        } while ( (prev instanceof SyntaxNode) == false );

        if (prev != null) {
            return (Node) prev;
        } else {
            return getOwnerDocument(); //??? return a DocumentFragment with some kids? or null
        }
    }
    
    public org.w3c.dom.Document getOwnerDocument() {
        return new DocumentImpl(this);
    }

    // default DOM Node implementation ~~~~~~~~~~~~~~~~~~~~~~~~``
    
    public String getNodeName() {
        return null;
    }

    /**
     * @return false
     */
    public boolean isSupported(String str, String str1) {
        throw new UOException();
    }
    
    public void setPrefix(String str) throws org.w3c.dom.DOMException {
        throw new ROException();
    }
    
    public String getPrefix() {
        throw new UOException();
    }
    
    /**
     * It is rather abstract to force all to reimplement.
     */
    public abstract short getNodeType();
        
    public org.w3c.dom.Node replaceChild(org.w3c.dom.Node node, org.w3c.dom.Node node1) throws org.w3c.dom.DOMException {
        throw new ROException();
    }
    
    public org.w3c.dom.Node cloneNode(boolean param) {
        return (Node) this;  //we are immutable, only problem with references may appear
    }
        
    public org.w3c.dom.Node insertBefore(org.w3c.dom.Node node, org.w3c.dom.Node node1) throws org.w3c.dom.DOMException {
        throw new ROException();
    }
    
    public String getNamespaceURI() {
        throw new UOException();
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
        throw new UOException();
    }
        
    public void setNodeValue(String str) throws org.w3c.dom.DOMException {
        throw new ROException();
    }
    
    public org.w3c.dom.Node getLastChild() {
        // if broken null
        return null;
    }
    
    public boolean hasAttributes() {
        throw new UOException();
    }
    
    public void normalize() {
        // ignore we are modmalized by default
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
    
    //
    // Implementation of DOM Level 3 methods
    //
    
    public short compareDocumentPosition (Node a) {
        throw new UOException();
    }
    
    public String getBaseURI() {
        throw new UOException();
    }
    public Object getFeature(String a, String b) {
        throw new UOException();
    }
    public String getTextContent () {
        throw new UOException();
    }
    public Object getUserData(String a) {
        throw new UOException();
    }
    public boolean isDefaultNamespace (String a)  {
        throw new UOException();
    }
    public boolean isEqualNode(Node a) {
        throw new UOException();
    }
    public boolean isSameNode(Node a) {
        throw new UOException();
    }
    public String lookupNamespaceURI(String a) {
        throw new UOException();
    }
    public String lookupPrefix(String a) {
        throw new UOException();
    }
    public void setTextContent(String a) {
        throw new UOException();
    }
    public Object setUserData(String a, Object b, UserDataHandler c) {
        throw new UOException();
    }
    // Implementation of DOM Level 3 methods for Text
    public Text replaceWholeText (String a) {
        throw new UOException ();
    }
    public String getWholeText() {
        throw new UOException ();
    }
    public boolean isElementContentWhitespace() {
        throw new UOException ();
    }
    // Implementation of DOM Level 3 methods for Element
    public TypeInfo getSchemaTypeInfo() {
        throw new UOException ();
    }
    public void setIdAttribute(String a, boolean b) {
        throw new UOException ();
    }
    public void setIdAttributeNS(String a, String b, boolean c) {
        throw new UOException ();
    }
    public void setIdAttributeNode(Attr a, boolean b) {
        throw new UOException ();
    }
    
}
