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
import javax.swing.text.BadLocationException;

import org.w3c.dom.*;
import org.netbeans.modules.xml.text.syntax.*;
import org.netbeans.modules.xml.spi.dom.*;
import org.netbeans.editor.*;

/**
 * Holds attribute: name-value pairs. It is returned by <code>Tag</code>
 * syntax nodes. Note that attributes are not a part part of DOM Node
 * hiearchy, but they are rather properties of <code>Element</code>.
 * It matches well with fact taht attributes are not represented by
 * <code>SyntaxNode</code>s.
 *
 * @author Petr Kuzel
 * @author asgeir@dimonsoftware.com
 */
public class AttrImpl extends AbstractNode implements Attr, XMLTokenIDs {
        
    private TokenItem first;
    
    private Element parent;
    
    private XMLSyntaxSupport syntax;  // that produced us
    
    AttrImpl(XMLSyntaxSupport syntax, TokenItem first, Element parent) {
        this.parent = parent;
        this.first = first;
        this.syntax = syntax;
    }
    
    public TokenItem getFirstToken() {
        return first;
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
    
    // NOTE:  This method has to be implemented, unless the getChildNodes() method
    // will only return the first child node.
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
        for (; next != null; next = next.getNext()) {
            if (next.getTokenID() == VALUE) {
                // fuzziness to relax minor tokenization changes
                String image = next.getImage();
                if (image.length() == 1) {
                    char test = image.charAt(0);
                    if (test == '"' || test == '\'') {
                        next = next.getNext();
                    }
                }
                break;  // we are after opening "'"
            }            
        }
        if (next == null) return null;                
        if (next.getTokenID() == VALUE) {
            return new TextImpl(syntax, next, this);  //!!! strip out ending "'", return standalone "'" token
        } else {
            throw new RuntimeException("Not recognized yet: " + next.getTokenID());
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
        TokenItem next = first;
        for (; next != null; next = next.getNext()) {
            if (next.getTokenID() == VALUE) {
                // fuzziness to relax minor tokenization changes
                String image = next.getImage();
                if (image.length() == 1) {
                    char test = image.charAt(0);
                    if (test == '"' || test == '\'') {
                        next = next.getNext();
                    }
                }
                break;  // we are after opening "'"
            }            
        }
        if (next == null) return;                
        BaseDocument doc = (BaseDocument)syntax.getDocument();
        if (next.getTokenID() == VALUE) {
            doc.atomicLock();
            try {
                doc.remove( next.getOffset() + 1, next.getImage().length() - 2 );
                doc.insertString( next.getOffset() + 1, value, null);
                doc.invalidateSyntaxMarks();
            } catch( BadLocationException e ) {
                throw new DOMException(DOMException.INVALID_STATE_ERR , e.getMessage());
            } finally {
                doc.atomicUnlock();
            }
        }
        
        try {
        	int endOffset = next.getOffset() + next.getImage().length();
        	if (endOffset > doc.getLength()) {
        		endOffset = doc.getLength();
        	}
            first = syntax.getTokenChain(first.getOffset(), endOffset);
        } catch (BadLocationException e) {
            throw new DOMException(DOMException.INVALID_STATE_ERR , e.getMessage());
        }
    }
    
    public void setNodeValue(String value) {
        setValue(value);
    }
    
    /**
     * Iterate over children to get value.
     * @return a String never <code>null</code>
     */
    public String getNodeValue() {
        return getValue();
    }

    // NOTE:  This method doesn't resolve entities.  If the attirbute value 
    // contains entities, the attribute value if chopped at the entity location.
    public String getValue() {
        NodeList nodes = getChildNodes();
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i<nodes.getLength(); i++) {
            buf.append(nodes.item(i).getNodeValue());  //!!! entity reference handling
        }
        
        // Remove " and ' around the attribute value, because getChildNodes returns it
        if (buf.length() > 0) {
            char firstChar = buf.charAt(0);
            if (firstChar == '"' ||  firstChar == '\'') {
                buf.deleteCharAt(0);
                if (buf.length() > 0 && buf.charAt(buf.length()-1) == firstChar) {
                    buf.deleteCharAt(buf.length()-1);
                }
            }
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
