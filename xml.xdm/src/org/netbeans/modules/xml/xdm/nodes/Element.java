/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * Text.java
 *
 * Created on August 3, 2005, 5:51 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.xml.xdm.nodes;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.xml.spi.dom.NamedNodeMapImpl;
import org.netbeans.modules.xml.spi.dom.NodeListImpl;
import org.netbeans.modules.xml.xdm.visitor.DefaultVisitor;
import org.netbeans.modules.xml.xdm.visitor.XMLNodeVisitor;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;

/**
 * This class represents the XML Element.
 * An element is of the form <elmName[attributes]/> or <element[attributes]>[TextNode]</elmName>
 * In terms of tokens attibute can have upto 12 tokens.
 * Start of start element token, <.
 * element namespace token, optional.
 * element namespace seperator, optional.
 * element name token.
 * whitespace token, optional.
 * end of start element token, > or />.
 * start of end element token, </.
 * element namespace token, optional.
 * element namespace seperator, optional.
 * element name token.
 * whitespace token, optional.
 * end of end element token, >.
 * the end element tokens are optional.
 * @author Ajit
 */
public class Element extends NodeImpl implements Node, org.w3c.dom.Element {
    
    
    Element() {
        super();
    }
    
    Element(String tagName) {
        super();
        List<Token> tokens = getTokensForWrite();
        tokens.add(new Token("<".concat(tagName), TokenType.TOKEN_ELEMENT_START_TAG));
        tokens.add(new Token(">", TokenType.TOKEN_ELEMENT_END_TAG));
        tokens.add(new Token("</".concat(tagName), TokenType.TOKEN_ELEMENT_START_TAG));
        tokens.add(new Token(">", TokenType.TOKEN_ELEMENT_END_TAG));
    }
    
    public short getNodeType() {
        return Node.ELEMENT_NODE;
    }
    
    public String getNodeName() {
        return getTagName();
    }
    
    public void accept(XMLNodeVisitor visitor) {
        visitor.visit(this);
    }
    
    public String getTagName() {
        if(tagName == null){
            for(Token token : getTokens()) {
                if(token.getType() == TokenType.TOKEN_ELEMENT_START_TAG) {
                    tagName = token.getValue().substring(1);
                    break;
                }
            }
        }
        return tagName;
    }
    
    public void setTagName(String tagName) {
        assert tagName!= null && !"".equals(tagName);
        checkNotInTree();
        this.tagName = tagName;
        int tokenIndex = -1;
        for(Token token : getTokens()) {
            tokenIndex++;
            if(token.getType() == TokenType.TOKEN_ELEMENT_START_TAG) {
                String image;
                String oldImage = token.getValue();
                if(oldImage.startsWith("</")) {
                    image = "</".concat(tagName);
                } else {
                    image = "<".concat(tagName);
                }
                Token newToken = new Token(image,TokenType.TOKEN_ELEMENT_START_TAG);
                getTokensForWrite().set(tokenIndex,newToken);
            }
        }
    }
    
    public String getPrefix() {
        String qName = getTagName();
        if(qName != null){
            int idx = qName.indexOf(':');
            if(idx >0) return qName.substring(0,idx);
        }
        return null;
    }
    
    public void setPrefix(String prefix) {
        String localName = getLocalName();
        if(prefix == null || prefix.equals("")) {
            setTagName(localName);
        } else {
            setTagName(prefix.concat(":").concat(localName));
        }
    }
    
    public String getLocalName() {
        String qName = getTagName();
        if(qName != null){
            int idx = qName.indexOf(':')+1;
            if(idx >0) return qName.substring(idx);
        }
        return qName;
    }
    
    public void setLocalName(String localName) {
        String prefix = getPrefix();
        if(prefix == null) {
            setTagName(localName);
        } else if(localName == null || localName.equals("")) {
            setTagName(prefix);
        } else {
            setTagName(prefix.concat(":").concat(localName));
        }
    }
    
    private void validateTokens(List<Token> newTokens) {
    }
    
    void setTokens(List<Token> newTokens) {
//        validateTokens(newTokens);
        tagName = null;
        super.setTokens(newTokens);
    }
    
    /**
     * Retrieves an attribute value by name.
     * @param name The name of the attribute to retrieve.
     * @return The <code>Attr</code> value as a string, or the empty string
     *   if that attribute does not have a specified or default value.
     */
    public String getAttribute(String name) {
        Attribute attribute = getAttributeNode(name);
        return attribute!=null?attribute.getValue():null;
    }
    
    /**
     * Retrieves an attribute node by name.
     * <br>To retrieve an attribute node by qualified name and namespace URI,
     * use the <code>getAttributeNodeNS</code> method.
     * @param name The name (<code>nodeName</code>) of the attribute to
     *   retrieve.
     * @return The <code>Attr</code> node with the specified name (
     *   <code>nodeName</code>) or <code>null</code> if there is no such
     *   attribute.
     */
    public Attribute getAttributeNode(String name) {
        NamedNodeMap attrMap = getAttributes();
        return (Attribute) attrMap.getNamedItem(name);
    }
    
    /**
     * Returns <code>true</code> when an attribute with a given name is
     * specified on this element or has a default value, <code>false</code>
     * otherwise.
     * @param name The name of the attribute to look for.
     * @return <code>true</code> if an attribute with the given name is
     *   specified on this element or has a default value, <code>false</code>
     *    otherwise.
     * @since DOM Level 2
     */
    public boolean hasAttribute(String name) {
        return getAttributeNode(name)!=null;
    }
    
    /**
     * Retrieves an attribute value by local name and namespace URI.
     * <br>Per [<a href='http://www.w3.org/TR/1999/REC-xml-names-19990114/'>XML Namespaces</a>]
     * , applications must use the value <code>null</code> as the
     * <code>namespaceURI</code> parameter for methods if they wish to have
     * no namespace.
     * @param namespaceURI The namespace URI of the attribute to retrieve.
     * @param localName The local name of the attribute to retrieve.
     * @return The <code>Attr</code> value as a string, or the empty string
     *   if that attribute does not have a specified or default value.
     * @exception DOMException
     *   NOT_SUPPORTED_ERR: May be raised if the implementation does not
     *   support the feature <code>"XML"</code> and the language exposed
     *   through the Document does not support XML Namespaces (such as [<a href='http://www.w3.org/TR/1999/REC-html401-19991224/'>HTML 4.01</a>]).
     * @since DOM Level 2
     */
    public String getAttributeNS(String namespaceURI, String localName) {
        Attribute attribute = getAttributeNodeNS(namespaceURI,localName);
        return attribute!=null?attribute.getValue():null;
    }
    
    /**
     * Retrieves an <code>Attr</code> node by local name and namespace URI.
     * <br>Per [<a href='http://www.w3.org/TR/1999/REC-xml-names-19990114/'>XML Namespaces</a>]
     * , applications must use the value <code>null</code> as the
     * <code>namespaceURI</code> parameter for methods if they wish to have
     * no namespace.
     * @param namespaceURI The namespace URI of the attribute to retrieve.
     * @param localName The local name of the attribute to retrieve.
     * @return The <code>Attr</code> node with the specified attribute local
     *   name and namespace URI or <code>null</code> if there is no such
     *   attribute.
     * @exception DOMException
     *   NOT_SUPPORTED_ERR: May be raised if the implementation does not
     *   support the feature <code>"XML"</code> and the language exposed
     *   through the Document does not support XML Namespaces (such as [<a href='http://www.w3.org/TR/1999/REC-html401-19991224/'>HTML 4.01</a>]).
     * @since DOM Level 2
     */
    public Attribute getAttributeNodeNS(String namespaceURI, String localName) {
        String prefix = lookupPrefix(namespaceURI);
        String qualifiedName = localName;
        if (prefix != null && !prefix.equals("")) qualifiedName = prefix+":"+localName;
        return getAttributeNode(qualifiedName);
    }
    
    /**
     * Returns <code>true</code> when an attribute with a given local name and
     * namespace URI is specified on this element or has a default value,
     * <code>false</code> otherwise.
     * <br>Per [<a href='http://www.w3.org/TR/1999/REC-xml-names-19990114/'>XML Namespaces</a>]
     * , applications must use the value <code>null</code> as the
     * <code>namespaceURI</code> parameter for methods if they wish to have
     * no namespace.
     * @param namespaceURI The namespace URI of the attribute to look for.
     * @param localName The local name of the attribute to look for.
     * @return <code>true</code> if an attribute with the given local name
     *   and namespace URI is specified or has a default value on this
     *   element, <code>false</code> otherwise.
     * @exception DOMException
     *   NOT_SUPPORTED_ERR: May be raised if the implementation does not
     *   support the feature <code>"XML"</code> and the language exposed
     *   through the Document does not support XML Namespaces (such as [<a href='http://www.w3.org/TR/1999/REC-html401-19991224/'>HTML 4.01</a>]).
     * @since DOM Level 2
     */
    public boolean hasAttributeNS(String namespaceURI, String localName) {
        return getAttributeNodeNS(namespaceURI,localName)!=null;
    }
    
    /**
     * Adds a new attribute. If an attribute with that name is already present
     * in the element, its value is changed to be that of the value
     * parameter. This value is a simple string; it is not parsed as it is
     * being set. So any markup (such as syntax to be recognized as an
     * entity reference) is treated as literal text, and needs to be
     * appropriately escaped by the implementation when it is written out.
     * In order to assign an attribute value that contains entity
     * references, the user must create an <code>Attr</code> node plus any
     * <code>Text</code> and <code>EntityReference</code> nodes, build the
     * appropriate subtree, and use <code>setAttributeNode</code> to assign
     * it as the value of an attribute.
     * <br>To set an attribute with a qualified name and namespace URI, use
     * the <code>setAttributeNS</code> method.
     * @param name The name of the attribute to create or alter.
     * @param value Value to set in string form.
     * @exception DOMException
     *   INVALID_CHARACTER_ERR: Raised if the specified name is not an XML
     *   name according to the XML version in use specified in the
     *   <code>Document.xmlVersion</code> attribute.
     *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */
    public void setAttribute(String name, String value) {
        checkNotInTree();
        Attribute oldAttr = getAttributeNode(name);
        if (oldAttr!=null) {
            if(!oldAttr.isInTree()) {
                oldAttr.setValue(value);
            } else {
                Attribute newAttr = (Attribute)oldAttr.clone(true,false,false);
                newAttr.setValue(value);
                int index = getAttributesForRead().indexOf(oldAttr);
                getAttributesForWrite().set(index,newAttr);
            }
        } else {
            Attribute attribute = new Attribute(name, value);
            getAttributesForWrite().add(attribute);
        }
    }
    
    /**
     * Adds a new attribute node. If an attribute with that name (
     * <code>nodeName</code>) is already present in the element, it is
     * replaced by the new one. Replacing an attribute node by itself has no
     * effect.
     * <br>To add a new attribute node with a qualified name and namespace
     * URI, use the <code>setAttributeNodeNS</code> method.
     * @param newAttr The <code>Attr</code> node to add to the attribute list.
     * @return If the <code>newAttr</code> attribute replaces an existing
     *   attribute, the replaced <code>Attr</code> node is returned,
     *   otherwise <code>null</code> is returned.
     * @exception DOMException
     *   WRONG_DOCUMENT_ERR: Raised if <code>newAttr</code> was created from a
     *   different document than the one that created the element.
     *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *   <br>INUSE_ATTRIBUTE_ERR: Raised if <code>newAttr</code> is already an
     *   attribute of another <code>Element</code> object. The DOM user must
     *   explicitly clone <code>Attr</code> nodes to re-use them in other
     *   elements.
     */
    public Attribute setAttributeNode(org.w3c.dom.Attr newAttr) {
        checkNotInTree();
        if(newAttr instanceof Attribute) {
            Attribute attribute = (Attribute)newAttr;
            Attribute oldAttr = getAttributeNode(newAttr.getName());
            if (oldAttr==null) {
                getAttributesForWrite().add(attribute);
                return attribute;
            } else {
                int index = getAttributesForRead().indexOf(oldAttr);
                return getAttributesForWrite().set(index,attribute);
            }
        } else {
            throw new DOMException(DOMException.TYPE_MISMATCH_ERR,null);
        }
    }
    
    /**
     * Removes an attribute by name. If a default value for the removed
     * attribute is defined in the DTD, a new attribute immediately appears
     * with the default value as well as the corresponding namespace URI,
     * local name, and prefix when applicable. The implementation may handle
     * default values from other schemas similarly but applications should
     * use <code>Document.normalizeDocument()</code> to guarantee this
     * information is up-to-date.
     * <br>If no attribute with this name is found, this method has no effect.
     * <br>To remove an attribute by local name and namespace URI, use the
     * <code>removeAttributeNS</code> method.
     * @param name The name of the attribute to remove.
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */
    public void removeAttribute(String name) {
        Attribute attribute = getAttributeNode(name);
        removeAttributeNode(attribute);
    }
    
    /**
     * Removes the specified attribute node. If a default value for the
     * removed <code>Attr</code> node is defined in the DTD, a new node
     * immediately appears with the default value as well as the
     * corresponding namespace URI, local name, and prefix when applicable.
     * The implementation may handle default values from other schemas
     * similarly but applications should use
     * <code>Document.normalizeDocument()</code> to guarantee this
     * information is up-to-date.
     * @param oldAttr The <code>Attr</code> node to remove from the attribute
     *   list.
     * @return The <code>Attr</code> node that was removed.
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *   <br>NOT_FOUND_ERR: Raised if <code>oldAttr</code> is not an attribute
     *   of the element.
     */
    public Attribute removeAttributeNode(org.w3c.dom.Attr oldAttr) {
        checkNotInTree();
        getAttributesForWrite().remove(oldAttr);
        return (Attribute) oldAttr;
    }
    
    /**
     * Adds a new attribute. If an attribute with that local name and that
     * namespace URI is already present in the element, it is replaced by
     * the new one. Replacing an attribute node by itself has no effect.
     * <br>Per [<a href='http://www.w3.org/TR/1999/REC-xml-names-19990114/'>XML Namespaces</a>]
     * , applications must use the value <code>null</code> as the
     * <code>namespaceURI</code> parameter for methods if they wish to have
     * no namespace.
     * @param newAttr The <code>Attr</code> node to add to the attribute list.
     * @return If the <code>newAttr</code> attribute replaces an existing
     *   attribute with the same local name and namespace URI, the replaced
     *   <code>Attr</code> node is returned, otherwise <code>null</code> is
     *   returned.
     * @exception DOMException
     *   WRONG_DOCUMENT_ERR: Raised if <code>newAttr</code> was created from a
     *   different document than the one that created the element.
     *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *   <br>INUSE_ATTRIBUTE_ERR: Raised if <code>newAttr</code> is already an
     *   attribute of another <code>Element</code> object. The DOM user must
     *   explicitly clone <code>Attr</code> nodes to re-use them in other
     *   elements.
     *   <br>NOT_SUPPORTED_ERR: May be raised if the implementation does not
     *   support the feature <code>"XML"</code> and the language exposed
     *   through the Document does not support XML Namespaces (such as [<a href='http://www.w3.org/TR/1999/REC-html401-19991224/'>HTML 4.01</a>]).
     * @since DOM Level 2
     */
    public org.w3c.dom.Attr setAttributeNodeNS(org.w3c.dom.Attr newAttr) {
        return setAttributeNode(newAttr);
    }
    
    /**
     * Adds a new attribute. If an attribute with the same local name and
     * namespace URI is already present on the element, its prefix is
     * changed to be the prefix part of the <code>qualifiedName</code>, and
     * its value is changed to be the <code>value</code> parameter. This
     * value is a simple string; it is not parsed as it is being set. So any
     * markup (such as syntax to be recognized as an entity reference) is
     * treated as literal text, and needs to be appropriately escaped by the
     * implementation when it is written out. In order to assign an
     * attribute value that contains entity references, the user must create
     * an <code>Attr</code> node plus any <code>Text</code> and
     * <code>EntityReference</code> nodes, build the appropriate subtree,
     * and use <code>setAttributeNodeNS</code> or
     * <code>setAttributeNode</code> to assign it as the value of an
     * attribute.
     * <br>Per [<a href='http://www.w3.org/TR/1999/REC-xml-names-19990114/'>XML Namespaces</a>]
     * , applications must use the value <code>null</code> as the
     * <code>namespaceURI</code> parameter for methods if they wish to have
     * no namespace.
     * @param namespaceURI The namespace URI of the attribute to create or
     *   alter.
     * @param qualifiedName The qualified name of the attribute to create or
     *   alter.
     * @param value The value to set in string form.
     * @exception DOMException
     *   INVALID_CHARACTER_ERR: Raised if the specified qualified name is not
     *   an XML name according to the XML version in use specified in the
     *   <code>Document.xmlVersion</code> attribute.
     *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *   <br>NAMESPACE_ERR: Raised if the <code>qualifiedName</code> is
     *   malformed per the Namespaces in XML specification, if the
     *   <code>qualifiedName</code> has a prefix and the
     *   <code>namespaceURI</code> is <code>null</code>, if the
     *   <code>qualifiedName</code> has a prefix that is "xml" and the
     *   <code>namespaceURI</code> is different from "<a href='http://www.w3.org/XML/1998/namespace'>
     *   http://www.w3.org/XML/1998/namespace</a>", if the <code>qualifiedName</code> or its prefix is "xmlns" and the
     *   <code>namespaceURI</code> is different from "<a href='http://www.w3.org/2000/xmlns/'>http://www.w3.org/2000/xmlns/</a>", or if the <code>namespaceURI</code> is "<a href='http://www.w3.org/2000/xmlns/'>http://www.w3.org/2000/xmlns/</a>" and neither the <code>qualifiedName</code> nor its prefix is "xmlns".
     *   <br>NOT_SUPPORTED_ERR: May be raised if the implementation does not
     *   support the feature <code>"XML"</code> and the language exposed
     *   through the Document does not support XML Namespaces (such as [<a href='http://www.w3.org/TR/1999/REC-html401-19991224/'>HTML 4.01</a>]).
     * @since DOM Level 2
     */
    public void setAttributeNS(String namespaceURI, String qualifiedName, String value) {
        //TODO Implement later
        checkNotInTree();
        Attribute oldAttr = getAttributeNodeNS(namespaceURI, qualifiedName);
        if (oldAttr!=null) {
            if(!oldAttr.isInTree()) {
                oldAttr.setValue(value);
            } else {
                Attribute newAttr = (Attribute)oldAttr.clone(true,false,false);
                newAttr.setValue(value);
                int index = getAttributesForRead().indexOf(oldAttr);
                getAttributesForWrite().set(index,newAttr);
            }
        } else {
            String prefix = lookupPrefix(namespaceURI);
            String name = qualifiedName;
            if (prefix != null && !prefix.equals("")) name = prefix + ":" + qualifiedName;
            Attribute attribute = new Attribute(name, value);
            getAttributesForWrite().add(attribute);
        }
    }
    
    /**
     * Removes an attribute by local name and namespace URI. If a default
     * value for the removed attribute is defined in the DTD, a new
     * attribute immediately appears with the default value as well as the
     * corresponding namespace URI, local name, and prefix when applicable.
     * The implementation may handle default values from other schemas
     * similarly but applications should use
     * <code>Document.normalizeDocument()</code> to guarantee this
     * information is up-to-date.
     * <br>If no attribute with this local name and namespace URI is found,
     * this method has no effect.
     * <br>Per [<a href='http://www.w3.org/TR/1999/REC-xml-names-19990114/'>XML Namespaces</a>]
     * , applications must use the value <code>null</code> as the
     * <code>namespaceURI</code> parameter for methods if they wish to have
     * no namespace.
     * @param namespaceURI The namespace URI of the attribute to remove.
     * @param localName The local name of the attribute to remove.
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *   <br>NOT_SUPPORTED_ERR: May be raised if the implementation does not
     *   support the feature <code>"XML"</code> and the language exposed
     *   through the Document does not support XML Namespaces (such as [<a href='http://www.w3.org/TR/1999/REC-html401-19991224/'>HTML 4.01</a>]).
     * @since DOM Level 2
     */
    public void removeAttributeNS(String namespaceURI, String localName) {
        removeAttributeNode(getAttributeNodeNS(namespaceURI, localName));
    }
    
    /**
     * Returns a <code>NodeList</code> of all descendant <code>Elements</code>
     * with a given tag name, in document order.
     * @param name The name of the tag to match on. The special value "*"
     *   matches all tags.
     * @return A list of matching <code>Element</code> nodes.
     */
    public org.w3c.dom.NodeList getElementsByTagName(String name) {
        //TODO implement later
        return NodeListImpl.EMPTY;
    }
    
    /**
     * Returns a <code>NodeList</code> of all the descendant
     * <code>Elements</code> with a given local name and namespace URI in
     * document order.
     * @param namespaceURI The namespace URI of the elements to match on. The
     *   special value "*" matches all namespaces.
     * @param localName The local name of the elements to match on. The
     *   special value "*" matches all local names.
     * @return A new <code>NodeList</code> object containing all the matched
     *   <code>Elements</code>.
     * @exception DOMException
     *   NOT_SUPPORTED_ERR: May be raised if the implementation does not
     *   support the feature <code>"XML"</code> and the language exposed
     *   through the Document does not support XML Namespaces (such as [<a href='http://www.w3.org/TR/1999/REC-html401-19991224/'>HTML 4.01</a>]).
     * @since DOM Level 2
     */
    public org.w3c.dom.NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        //TODO Implement later
        return null;
    }
    
    /**
     *  If the parameter <code>isId</code> is <code>true</code>, this method
     * declares the specified attribute to be a user-determined ID attribute
     * . This affects the value of <code>Attr.isId</code> and the behavior
     * of <code>Document.getElementById</code>, but does not change any
     * schema that may be in use, in particular this does not affect the
     * <code>Attr.schemaTypeInfo</code> of the specified <code>Attr</code>
     * node. Use the value <code>false</code> for the parameter
     * <code>isId</code> to undeclare an attribute for being a
     * user-determined ID attribute.
     * @param idAttr The attribute node.
     * @param isId Whether the attribute is a of type ID.
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *   <br>NOT_FOUND_ERR: Raised if the specified node is not an attribute
     *   of this element.
     * @since DOM Level 3
     */
    public void setIdAttributeNode(org.w3c.dom.Attr idAttr, boolean isId) {
        //TODO Implement later
    }
    
    /**
     *  If the parameter <code>isId</code> is <code>true</code>, this method
     * declares the specified attribute to be a user-determined ID attribute
     * . This affects the value of <code>Attr.isId</code> and the behavior
     * of <code>Document.getElementById</code>, but does not change any
     * schema that may be in use, in particular this does not affect the
     * <code>Attr.schemaTypeInfo</code> of the specified <code>Attr</code>
     * node. Use the value <code>false</code> for the parameter
     * <code>isId</code> to undeclare an attribute for being a
     * user-determined ID attribute.
     * <br> To specify an attribute by local name and namespace URI, use the
     * <code>setIdAttributeNS</code> method.
     * @param name The name of the attribute.
     * @param isId Whether the attribute is a of type ID.
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *   <br>NOT_FOUND_ERR: Raised if the specified node is not an attribute
     *   of this element.
     * @since DOM Level 3
     */
    public void setIdAttribute(String name, boolean isId) {
        //TODO Implement later
    }
    
    /**
     *  If the parameter <code>isId</code> is <code>true</code>, this method
     * declares the specified attribute to be a user-determined ID attribute
     * . This affects the value of <code>Attr.isId</code> and the behavior
     * of <code>Document.getElementById</code>, but does not change any
     * schema that may be in use, in particular this does not affect the
     * <code>Attr.schemaTypeInfo</code> of the specified <code>Attr</code>
     * node. Use the value <code>false</code> for the parameter
     * <code>isId</code> to undeclare an attribute for being a
     * user-determined ID attribute.
     * @param namespaceURI The namespace URI of the attribute.
     * @param localName The local name of the attribute.
     * @param isId Whether the attribute is a of type ID.
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *   <br>NOT_FOUND_ERR: Raised if the specified node is not an attribute
     *   of this element.
     * @since DOM Level 3
     */
    public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) {
        //TODO Implement later
    }
    
    /**
     *  The type information associated with this element.
     * @since DOM Level 3
     */
    public org.w3c.dom.TypeInfo getSchemaTypeInfo() {
        //TODO Implement later
        return null;
    }
    
    /**
     * This api replaces old attribute of this element with new attribute.
     * This api is provided for XDMModel purposes only.
     * @param newAttr The new attribute.
     * @param oldAttr The old attribute to replace.
     * @throws IllegalStateException if a newAttr has already been added to a tree.
     */
    public void replaceAttribute(Attribute newAttr, Attribute oldAttr) {
        checkNotInTree();
        List<Attribute> attributes = getAttributesForRead();
        int index = attributes.indexOf(oldAttr);
        assert newAttr != null && index>-1;
        newAttr.checkNotInTree();
        getAttributesForWrite().set(attributes.indexOf(oldAttr),newAttr);
    }
    
    /**
     * This api adds new attribute at a given index.
     * This api is provided for XDMModel purposes only.
     * @param newAttr The new attribute to be added.
     * @param index The index at which attribute to be added
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index > size()).
     * @throws IllegalStateException if a newAttr has already been added to a tree.
     */
    public void addAttribute(Attribute newAttr, int index) {
        checkNotInTree();
        assert newAttr != null;
        newAttr.checkNotInTree();
        getAttributesForWrite().add(index,newAttr);
    }
    
    /**
     * This api adds new attribute at the end.
     * This api is provided for XDMModel purposes only.
     * @param newAttr The new attribute to be added.
     * @throws IllegalStateException if a newAttr has already been added to a tree.
     */
    public void appendAttribute(Attribute newAttr) {
        checkNotInTree();
        assert newAttr != null;
        newAttr.checkNotInTree();
        getAttributesForWrite().add(newAttr);
    }
    
    private String tagName = null;
}
