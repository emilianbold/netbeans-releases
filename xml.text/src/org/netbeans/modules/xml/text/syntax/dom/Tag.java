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
 * Represents tag syntax element. It also represent DOM <code>Element</code>.
 * This duality means that one document element is represented by
 * two DOM <code>Element</code> instances - one for start tag and one for
 * end tag. This is hidden during document traversal but never relay on
 * <code>equals</code>. The <code>equals</code> is used for syntax element
 * purposes.
 */
public abstract class Tag extends SyntaxNode implements Element, XMLTokenIDs {
    
    NamedNodeMap domAttributes;
    
    String name;
    
    public Tag(XMLSyntaxSupport support, TokenItem from, int to, String name, Collection attribs) {
        super( support, from,to );
        this.name = name;
    }
    
    public final short getNodeType() {
        return Node.ELEMENT_NODE;
    }
    
    public final String getNodeName() {
        return getTagName();
    }
    
    public final String getTagName() {
        return name;
    }
    
    /**
     * Create properly bound attributes and cache results.
     * Parse attributes from first token.
     */
    public synchronized NamedNodeMap getAttributes() {
        
        // cached results not implemented
        if (domAttributes != null) return domAttributes;
        
        Map map = new HashMap(3);
        
        SCAN_LOOP:
        for (TokenItem next = first.getNext(); next != null; next = next.getNext()) {
            TokenID id = next.getTokenID();
            String name;
            String value;
            if (id == ARGUMENT) {
                TokenItem attributeStart = next;
                name = next.getImage();
                while (next.getTokenID() != VALUE) {
                    next = next.getNext();
                    if (next == null) break SCAN_LOOP;
                }

                // fuzziness to relax minor tokenization changes
                String image = next.getImage();
                char test = image.charAt(0);
                if (image.length() == 1) {
                    if (test == '"' || test == '\'') {
                        next = next.getNext();
                    }
                }

                if (next == null) break SCAN_LOOP;
                value = next.getImage();

                Object key = NamedNodeMapImpl.createKey(name);
                map.put(key, new AttrImpl(support, attributeStart, this));

                next = Util.skipAttributeValue(next, test);
                if (next == null) break SCAN_LOOP;
            } else if (id == WS) {
                // just skip
            } else {
                break; // end of element markup
            }
        }

        // domAttributes = new NamedNodeMapImpl(map);
        return new NamedNodeMapImpl(map);
    }
    
    public String getAttribute(String name) {
        Attr attribute = getAttributeNode(name);
        if (attribute == null) return null;
        return attribute.getValue();
    }
    
    public final void setAttribute(String name, String value) {
        // Holds the position in the document where the attribute name-value string should be inserted
        int insertStart = -1;
        
        // Holds the lenght of an old name-value string which should be removed before the new one will be inserted
        int removeCount = 0;
        
        // Check if an attribute with this name already exists for the element
        SCAN_LOOP:
        for (TokenItem next = first.getNext(); next != null; next = next.getNext()) {
            TokenID id = next.getTokenID();
            String curName;
            String curValue;
            if (id == ARGUMENT) {
                TokenItem attributeStart = next;
                curName = next.getImage();

                if (name.equals(curName)) {
                    // Found attribute to replace
                    insertStart = attributeStart.getOffset();
                }

                while (next.getTokenID() != VALUE) {
                    next = next.getNext();
                    if (next == null) break SCAN_LOOP;
                }

                // fuzziness to relax minor tokenization changes
                String image = next.getImage();
                char test = image.charAt(0);
                if (image.length() == 1) {
                    if (test == '"' || test == '\'') {
                        next = next.getNext();
                    }
                }

                if (next == null) break SCAN_LOOP;
                curValue = next.getImage();

                if (insertStart != -1) {
                    // Found the attribute, let's calculate removeCount
                    removeCount = next.getOffset() + curValue.length() - insertStart;
                    break SCAN_LOOP;
                }

                next = Util.skipAttributeValue(next, test);
                if (next == null) break SCAN_LOOP;
            } else if (id == WS) {
                // just skip
            } else {
                break; // end of element markup
            }
        }

        // Construct a new attribute name-value string to insert
        String quoteCharString = value.indexOf('"') == -1 ? "\"" : "'";
        String stringToInsert = name + "=" + quoteCharString + value + quoteCharString;
        
        // Get the document and lock it
        BaseDocument doc = (BaseDocument)support.getDocument();
        doc.atomicLock();

        if (insertStart == -1) {
            // An attribute with the name was not found for the element
            // Let's add it to the end
            insertStart = offset + length - 1;
            
            // Decrese if it is closed element of the form <blu/> 
            try {
                System.out.println("Text at insertStart: " + doc.getText(insertStart - 1, 1));
                if (doc.getText(insertStart - 1, 1).equals("/")) {
                    insertStart--;
                }
            } catch( BadLocationException e) {} // Will never been thrown
            stringToInsert = " " + stringToInsert;
        }

        // Update the document
        try {
            if (removeCount != 0) {
                doc.remove(insertStart, removeCount);
            }
            doc.insertString(insertStart, stringToInsert, null);
            doc.invalidateSyntaxMarks();
        } catch( BadLocationException e ) {
            throw new DOMException(DOMException.INVALID_STATE_ERR , e.getMessage());
        } finally {
            doc.atomicUnlock();
        }

        // Update this object's member variables
        try {
            length += stringToInsert.length() - removeCount;
            int endOffset = offset + length;
            if (endOffset > doc.getLength()) {
                endOffset = doc.getLength();
            }
            first = support.getTokenChain(offset, endOffset);
        } catch (BadLocationException e) {
            throw new DOMException(DOMException.INVALID_STATE_ERR , e.getMessage());
        }
    }
    
    public final void removeAttribute(String name) {
        throw new ROException();
    }
    
    public Attr getAttributeNode(String name) {
        NamedNodeMap map = getAttributes();
        Node node = map.getNamedItem(name);
        return (Attr) node;
    }
    
    public final Attr setAttributeNode(Attr attribute) {
        throw new ROException();
    }
    
    public final Attr removeAttributeNode(Attr attribute) {
        throw new ROException();
    }
    
    public NodeList getElementsByTagName(String name) {
        throw new ROException();
    }
    
    /**
     * Returns previous sibling by locating pairing start tag
     * and asking it for previous non-start tag SyntaxNode.
     */
    public Node getPreviousSibling() {
        SyntaxNode prev = getStartTag();
        if (prev == null) return null;
        prev = findPrevious(prev);
        if (prev instanceof StartTag) {
            return null;
        } else {
            return prev;
        }
    }
    
    /**
     * Returns next sibling by locating pairing end tag
     * and asking it for next non-end tag SyntaxNode.
     */
    public Node getNextSibling() {
        SyntaxNode next = getEndTag();
        if (next == null) return null;
        next = findNext(next);
        if (next instanceof EndTag) {
            return null;
        } else {
            return next;
        }
    }
    
    public Node getFirstChild() {
        return getChildNodes().item(0);
    }
    
    public Node getLastChild() {
        NodeList list = getChildNodes();
        return list.item(list.getLength());
    }
    
    protected abstract Tag getStartTag();
    
    protected abstract Tag getEndTag();
    
    //    public boolean equals(Object obj) {
    //        if ((obj instanceof Tag) == false) return false;
    //        return false;
    //    }
    
    
    // unsupported DOM level 2 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    public String getAttributeNS(String namespaceURI, String localName) {
        throw new UOException();
    }
    
    public void setAttributeNS(String namespaceURI, String qualifiedName, String value) {
        throw new UOException();
    }
    
    public void removeAttributeNS(String namespaceURI, String localName) {
        throw new UOException();
    }
    
    public Attr getAttributeNodeNS(String namespaceURI, String localName) {
        throw new UOException();
    }
    
    public Attr setAttributeNodeNS(Attr newAttr) {
        throw new UOException();
    }
    
    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        throw new UOException();
    }
    
    public boolean hasAttribute(String name) {
        throw new UOException();
    }
    
    public boolean hasAttributeNS(String namespaceURI, String localName) {
        throw new UOException();
    }
    
}

