/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */


package org.netbeans.modules.uml.core.support.umlutils;

import com.sun.org.apache.xml.internal.dtm.DTMDOMException;
import com.sun.org.apache.xml.internal.dtm.ref.DTMNodeProxy;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.DocumentFactory;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.tree.DefaultElement;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.dom4j.dom.DOMNodeHelper;
import org.dom4j.dom.DOMDocumentFactory;
import org.dom4j.dom.DOMAttribute;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;
import org.dom4j.DocumentFactory;

/** <p><code>DOMAttribute</code> implements an XML element which
 * supports the W3C DOM API.</p>
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.2.6.1 $
 */
public class W3CNodeProxy extends Dom4JNodeProxy implements org.w3c.dom.Element
{

    /** The <code>DocumentFactory</code> instance used by default */
    private static final DocumentFactory DOCUMENT_FACTORY = DOMDocumentFactory.getInstance();
    
    
    public W3CNodeProxy(DTMNodeProxy n)
    {
        super(n);
    }
    
    // org.w3c.dom.Node interface
    //-------------------------------------------------------------------------
    
    /** Returns the code according to the type of node.
     * This makes processing nodes polymorphically much easier as the
     * switch statement can be used instead of multiple if (instanceof)
     * statements.
     *
     * @return a W3C DOM complient code for the node type such as
     * ELEMENT_NODE or ATTRIBUTE_NODE
     */
    public short getNodeType()
    {
        return Node.ELEMENT_NODE;
    }
   
    public boolean supports(String feature, String version)
    {
        //return DOMNodeHelper.supports(this, feature, version);
        return false;
    }
    
    public String getNamespaceURI()
    {
        return getQName().getNamespaceURI();
    }
    
    public String getPrefix()
    {
        return getQName().getNamespacePrefix();
    }
    
    public void setPrefix(String prefix) throws DOMException
    {
        DOMNodeHelper.setPrefix(this, prefix);
    }
    
    public String getLocalName()
    {
        return getQName().getName();
    }
    
    public String getNodeName()
    {
        return getName();
    }    
    
    
    public String getNodeValue() throws DOMException
    {
        return DOMNodeHelper.getNodeValue(this);
    }
    
    public void setNodeValue(String nodeValue) throws DOMException
    {
        DOMNodeHelper.setNodeValue(this, nodeValue);
    }
    
    
    public org.w3c.dom.Node getParentNode()
    {
        return DOMNodeHelper.getParentNode(this);
    }
    
    public NodeList getChildNodes()
    {
        return DOMNodeHelper.createNodeList( content() );
    }
    
    public org.w3c.dom.Node getFirstChild()
    {
        org.w3c.dom.Node retVal = null;
        
        if(nodeCount() > 0)
        {
            retVal = (org.w3c.dom.Node) node(0);
        }
        
        return retVal;
    }
    
    public org.w3c.dom.Node getLastChild()
    {
        org.w3c.dom.Node retVal = null;
        
        int nodeCount = nodeCount();
        if(nodeCount > 0)
        {
            retVal = (org.w3c.dom.Node)node( nodeCount - 1 ) ;
        }
        
        return retVal;
    }
    
    public org.w3c.dom.Node getPreviousSibling()
    {
////        return DOMNodeHelper.getPreviousSibling(this);
//        org.w3c.dom.Node retVal = null;
//        org.dom4j.Element parent = getParent();
//        if ( parent != null )
//        {
//            int index = parent.indexOf( this );
//            if ( index > 0 )
//            {
//                org.dom4j.Node previous = parent.node(index - 1);
//                if(previous instanceof org.w3c.dom.Node)
//                {
//                    retVal = (org.w3c.dom.Node)previous;
//                }
//            }
//        }
//        return retVal;
        org.w3c.dom.Node retVal = getProxyNode().getPreviousSibling();
        if(retVal instanceof DTMNodeProxy)
        {
            retVal = new W3CNodeProxy((DTMNodeProxy)retVal);
        }
        return retVal;
    }
    
    public org.w3c.dom.Node getNextSibling()
    {
////        return DOMNodeHelper.getNextSibling(this);
////        Element parent = node.getParent();
////        if ( parent != null ) {
////            int index = parent.indexOf( node );
////            if ( index >= 0 ) {
////                if ( ++index < parent.nodeCount() ) {
////                    Node next = parent.node(index);
////                    return asDOMNode( next );
////                }
////            }
////        }
////        return null;
//        
//        org.w3c.dom.Node retVal = null;
//        org.dom4j.Element parent = getParent();
//        if ( parent != null )
//        {
//            int index = parent.indexOf( this );
//            if ( index >= 0 )
//            {
//                index++;
//                if ( index < parent.nodeCount() )
//                {
//                    org.dom4j.Node previous = parent.node(index);
//                    if(previous instanceof org.w3c.dom.Node)
//                    {
//                        retVal = (org.w3c.dom.Node)previous;
//                    }
//                }
//            }
//        }
        
        org.w3c.dom.Node retVal = getProxyNode().getNextSibling();
        if(retVal instanceof DTMNodeProxy)
        {
            retVal = new W3CNodeProxy((DTMNodeProxy)retVal);
        }
        return retVal;
    }
    
    public boolean equals(Object obj)
    {
        boolean retVal = false;
        
//        if(obj instanceof org.w3c.dom.Element)
//        {
//            org.w3c.dom.Element element = (org.w3c.dom.Element)obj;
//            String name = getLocalName();
//            if(name.equals(element.getLocalName()) == true)
//            {
//                NamedNodeMap myAttrs = getAttributes();
//                NamedNodeMap otherAttrs = element.getAttributes();
//                if(myAttrs.getLength() == otherAttrs.getLength())
//                {
//                    for(int index = 0; index < myAttrs.getLength(); index++)
//                    {
//                        Attribute attr = attribute(index);
//                        if(attr.getValue().equals(element.getAttribute(attr.getQualifiedName())) == false)
//                        {
//                            break;
//                        }
//                    }
//                    retVal = true;
//                }
//            }
//        }
//        if(obj instanceof DTMNodeProxy)
        {
            retVal = getProxyNode().isEqualNode((org.w3c.dom.Node)obj);
        }
//        else if(obj instanceof W3CNodeProxy)
//        {
//            W3CNodeProxy proxy = (W3CNodeProxy)obj;
//            
//        }
        
        return retVal;
    }
    
    public NamedNodeMap getAttributes()
    {
        return new W3CAttributeNodeMap( this );
    }
    
    public Document getOwnerDocument()
    {
        return DOMNodeHelper.getOwnerDocument(this);
    }
    
    public org.w3c.dom.Node insertBefore(
            org.w3c.dom.Node newChild,
            org.w3c.dom.Node refChild
            ) throws DOMException
    {
        throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
    
    public org.w3c.dom.Node replaceChild(
            org.w3c.dom.Node newChild,
            org.w3c.dom.Node oldChild
            ) throws DOMException
    {
        throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
    
    public org.w3c.dom.Node removeChild(org.w3c.dom.Node oldChild) throws DOMException
    {
        return DOMNodeHelper.removeChild(this, oldChild);
    }
    
    public org.w3c.dom.Node appendChild(org.w3c.dom.Node newChild) throws DOMException
    {
        return DOMNodeHelper.appendChild(this, newChild);
    }
    
    public boolean hasChildNodes()
    {
        return nodeCount() > 0;
    }
    
    public org.w3c.dom.Node cloneNode(boolean deep)
    {
        return DOMNodeHelper.cloneNode(this, deep);
    }
    
    public boolean isSupported(String feature, String version)
    {
        return DOMNodeHelper.isSupported(this, feature, version);
    }
    
    public boolean hasAttributes()
    {
        return DOMNodeHelper.hasAttributes(this);
    }
    
    
    // org.w3c.dom.Element interface
    //-------------------------------------------------------------------------
    public String getTagName()
    {
        return getName();
    }
    
    public void setIdAttributeNode(Attr attr, boolean isID)
    {
        throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
    
    public void setIdAttributeNS(String namespaceURI,
                                 String qualifiedName,
                                 boolean isID)
    {
        throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
    
    public void setIdAttribute(String namespaceURI,
                               boolean isID)
    {
        throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
    
    public TypeInfo getSchemaTypeInfo()
    {
        
        throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
    }
    
    public Object getUserData(String key)
    {
        
        throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
    }
    
    public Object setUserData(String key,
                              Object data,
                              UserDataHandler handler)
    {
        throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
    
    public Object getFeature(String feature,
                             String version)
    {
        
        throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
    }
                  
    public String getAttribute(String name)
    {
        String answer = attributeValue(name);
        return (answer != null) ? answer : "";
    }
    
    public String lookupNamespaceURI(String prefix)
    {
        
        throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
    }
    
    public boolean isDefaultNamespace(String namespaceURI)
    {
        
        throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
    }
    
    public String lookupPrefix(String namespaceURI)
    {
        throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
    }
    
    public boolean isEqualNode(Node arg)
    {
       throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
    }

    public boolean isSameNode(Node arg)
    {
       throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
    }
    
    public void setTextContent(String textContent) throws DOMException
    {
        throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
    
    public String getTextContent() throws DOMException
    {
        
        throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
    }
    public short compareDocumentPosition(Node other) throws DOMException
    {
        
        throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
    }
    
    public String getBaseURI()
    {
        
        throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
    }
    public void setAttribute(String name, String value) throws DOMException
    {
        addAttribute(name, value);
    }
    
    public void removeAttribute(String name) throws DOMException
    {
        throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
    
    public org.w3c.dom.Attr getAttributeNode(String name)
    {
        return DOMNodeHelper.asDOMAttr( attribute( name ) );
    }
    
    public org.w3c.dom.Attr setAttributeNode(org.w3c.dom.Attr newAttr) throws DOMException
    {
        throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
    
    public org.w3c.dom.Attr removeAttributeNode(org.w3c.dom.Attr oldAttr) throws DOMException
    {
        throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
    
    public String getAttributeNS(String namespaceURI,  String localName)
    {
        Attribute attribute = attribute( namespaceURI, localName );
        if ( attribute != null )
        {
            String answer = attribute.getValue();
            if ( answer != null )
            {
                return answer;
            }
        }
        return "";
    }
    
    public void setAttributeNS(
            String namespaceURI,
            String qualifiedName,
            String value
            ) throws DOMException
    {
        throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
    
    public void removeAttributeNS(
            String namespaceURI,
            String localName
            ) throws DOMException
    {
       throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
    
    public org.w3c.dom.Attr getAttributeNodeNS(String namespaceURI,  String localName)
    {
        Attribute attribute = attribute( namespaceURI, localName );
        if ( attribute != null )
        {
            DOMNodeHelper.asDOMAttr( attribute );
        }
        return null;
    }
    
    public org.w3c.dom.Attr setAttributeNodeNS(org.w3c.dom.Attr newAttr) throws DOMException
    {
        throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
    
    public NodeList getElementsByTagName(String name)
    {
        ArrayList list = new ArrayList();
        DOMNodeHelper.appendElementsByTagName( list, this, name );
        return DOMNodeHelper.createNodeList( list );
    }
    
    public NodeList getElementsByTagNameNS(
            String namespaceURI, String localName
            )
    {
        ArrayList list = new ArrayList();
        DOMNodeHelper.appendElementsByTagNameNS(list, this, namespaceURI, localName );
        return DOMNodeHelper.createNodeList( list );
    }
    
    public boolean hasAttribute(String name)
    {
        return attribute(name) != null;
    }
    
    public boolean hasAttributeNS(String namespaceURI, String localName)
    {
        return attribute(namespaceURI, localName) != null;
    }
    
    
//    // Implementation methods
//    //-------------------------------------------------------------------------
//    protected DocumentFactory getDocumentFactory()
//    {
//        DocumentFactory factory = getQName().getDocumentFactory();
//        return ( factory != null ) ? factory : DOCUMENT_FACTORY;
//    }
//    
    protected Attribute attribute(org.w3c.dom.Attr attr)
    {
        return attribute(
                DOCUMENT_FACTORY.createQName(
                attr.getLocalName(),
                attr.getPrefix(),
                attr.getNamespaceURI()
                )
                );
    }
    
    protected Attribute attribute(String namespaceURI,  String localName)
    {
////        List attributes = attributeList();
////        int size = attributes.size();
////        for ( int i = 0; i < size; i++ )
////        {
////            Attribute attribute = (Attribute) attributes.get(i);
////            if ( localName.equals( attribute.getName() ) &&
////                    namespaceURI.equals( attribute.getNamespaceURI() ) )
////            {
////                return attribute;
////            }
////        }
////        return null;
//        
//        NamedNodeMap map = getProxyNode().getAttributes();
//        Attr attribute = (Attr)map.getNamedItem(name);
//        
//        DefaultAttribute attr = new DefaultAttribute(attribute.getName(),
//                attribute.getValue());
//        attr.setParent((Element)attribute.getParentNode());
//        return attr;
        
        NamedNodeMap map = getProxyNode().getAttributes();
        Attr attribute = (Attr)map.getNamedItemNS(namespaceURI,
                                                  localName);
        return attribute(attribute);
    }
    
    protected Attribute createAttribute( org.w3c.dom.Attr newAttr )
    {
        QName qname = null;
        String name = newAttr.getLocalName();
        String uri = newAttr.getNamespaceURI();
        if ( uri != null && uri.length() > 0 )
        {
            Namespace namespace = getNamespaceForURI( uri );
            if ( namespace != null )
            {
                qname = DOCUMENT_FACTORY.createQName( name, namespace );
            }
        }
        if ( qname == null )
        {
            qname = DOCUMENT_FACTORY.createQName( name );
        }
        return new DOMAttribute( qname, newAttr.getValue() );
    }
    
    protected QName getQName( String namespaceURI, String qualifiedName )
    {
        int index = qualifiedName.indexOf( ':' );
        String prefix = "";
        String localName = qualifiedName;
        if ( index >= 0 )
        {
            prefix = qualifiedName.substring(0, index);
            localName = qualifiedName.substring(index+1);
        }
        return DOCUMENT_FACTORY.createQName( localName, prefix, namespaceURI );
    }
}
