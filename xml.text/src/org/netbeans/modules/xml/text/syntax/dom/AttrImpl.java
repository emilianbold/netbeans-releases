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

package org.netbeans.modules.xml.text.syntax.dom;

import java.util.*;

import org.w3c.dom.*;
import org.netbeans.modules.xml.text.syntax.*;
import org.netbeans.editor.*;

/**
 * Holds attribute: name-value pairs. It is returned by <code>Tag</code>
 * syntax nodes. Note that attributes are not a part part of DOM Node
 * hiearchy, but they are rather properties of <code>Element</code>.
 * It matches well with fact taht attributes are not represented by
 * <code>SyntaxNode</code>s.
 *
 * @author Petr Kuzel
 */
public class AttrImpl extends AbstractNode implements Attr {
        
    private TokenItem first;
    
    private Element parent;
    
    private XMLSyntaxSupport syntax;  // that produced us
    
    AttrImpl(XMLSyntaxSupport syntax, TokenItem first, Element parent) {
        this.parent = parent;
        this.first = first;
        this.syntax = syntax;
    }

    /**
     * @return list of child nodes (Text or EntityReference), never <code>null</code>
     */
    public NodeList getChildNodes() {
        List list = new ArrayList(3);
        
        Node node = getFirstChild();
        while (node != null) {
            list.add(node);
            node = node.getNextSibling();
        }
        
        return new NodeListImpl(list);
    }
    
    /**
     * Get next sibling for non syntax element text.
     */
    Node getPreviousSibling(Text text) {
        return null;  //!!! todo
    }
    
    Node getPreviousSibling(EntityReferenceImpl ref) {
        return null;
    }
    
    Node getNextSibling(Text text) {
        return null;
    }
    
    Node getNextSibling(EntityReferenceImpl ref) {
        return null;
    }
    
    public Node getNextSibling() {
        return null;  //according to DOM-1spec
    }
    
    public Node getPreviousSibling() {
        return null;  //according to DOM-1 spec
    }
 
    public Node getFirstChild() {
        TokenItem next = first;
        while (next != null) {
            if (next.getTokenID().getNumericID() == XMLDefaultTokenContext.VALUE_ID) {
                next = next.getNext();
                break;  // we are after opening "'"
            }
            next = next.getNext();            
        }
        if (next == null) return null;                
        if (next.getTokenID().getNumericID() == XMLDefaultTokenContext.VALUE_ID) {
            return new TextImpl(syntax, first, this);  //!!! strip out ending "'", return standalone "'" token
        } else {
            throw new RuntimeException("Not implemented yet");
        }
    }
    
    public Node getLastChild() {
        throw new RuntimeException("Not implemented yet");
    }
    
    public String getNodeName() {
        return getName();
    }
    
    public String getName() {
        return first.getImage();
    }
    
    public boolean getSpecified() {
        return true;
    }
    
    public void setValue(String value) {
        throw new ROException();
    }
    
    /**
     * Iterate over children to get value.
     * @return a String never <code>null</code>
     */
    public String getNodeValue() {
        return getValue();
    }

    public String getValue() {
        NodeList nodes = getChildNodes();
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i<nodes.getLength(); i++) {
            buf.append(nodes.item(i).getNodeValue());  //!!! entity reference handling
        }
        return buf.toString();        
    }
    
    public short getNodeType() {
        return Node.ATTRIBUTE_NODE;
    }
    
    public Node getParentNode() {
        return null;  //accordnig to DOM-1 specs
    }

    public Element getOwnerElement() {
        return parent;
    }

    /**
     * Get owner document or <code>null</code>
     */
    public org.w3c.dom.Document getOwnerDocument() {
        Node parent = getOwnerElement();
        if (parent == null) return null;
        return parent.getOwnerDocument();
    }
    
    /**
     * Return string representation of the object for debug purposes.
     */
    public String toString() {
        return "Attr(" + getName() + "='" + getValue() + "')";
    }
        
}
