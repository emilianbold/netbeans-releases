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

/*
 * Dom4JNodeProxy.java
 *
 * Created on July 14, 2005, 10:29 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.uml.core.support.umlutils;

import com.sun.org.apache.xml.internal.dtm.DTMDOMException;
import com.sun.org.apache.xml.internal.dtm.ref.DTMNodeProxy;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.dom4j.*;
import org.dom4j.tree.DefaultAttribute;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 *
 * @author Trey Spiva
 */
public class Dom4JNodeProxy implements org.dom4j.Element
{
   private DTMNodeProxy mNode = null;
   
   public Dom4JNodeProxy(DTMNodeProxy n)
   {
      setProxyNode(n);
   }
   
   public final void setProxyNode(DTMNodeProxy n)
   {
      mNode = n;
   }
   
   public final DTMNodeProxy getProxyNode()
   {
      return mNode;
   }
   
   ////////////////////////////////////////////////////////////////////////////
   // DOM4J Element Implementation
   
   /** <p>Returns the <code>QName</code> of this element which represents
    * the local name, the qualified name and the <code>Namespace</code>.</p>
    *
    * @return the <code>QName</code> associated with this element
    */
   public QName getQName()
   {
      String local = getProxyNode().getLocalName();
      String uri = getProxyNode().getNamespaceURI();
      return QName.get(local, uri);
   }
   
   /** <p>Sets the <code>QName</code> of this element which represents
    * the local name, the qualified name and the <code>Namespace</code>.</p>
    *
    * @param qname is the <code>QName</code> to be associated with this element
    */
   public void setQName(QName qname)
   {
      
   }
   
   /** <p>Returns the <code>Namespace</code> of this element if one exists
    * otherwise <code>Namespace.NO_NAMESPACE</code> is returned.</p>
    *
    * @return the <code>Namespace</code> associated with this element
    */
   public Namespace getNamespace()
   {
      
      return new Namespace(getProxyNode().getPrefix(),
                           getProxyNode().getNamespaceURI());
   }
   
   
   
   /** <p>Returns the <code>QName</code> for the given qualified name, using
    * the namespace URI in scope for the given prefix of the qualified name
    * or the default namespace if the qualified name has no prefix.</p>
    *
    * @return the <code>QName</code> for the given qualified name
    */
   public QName getQName(String qualifiedName)
   {
//      String prefix = "";      
//      String localName = qualifiedName;      
//      int index = qualifiedName.indexOf(":");      
//      if (index > 0)
//      {         
//         prefix = qualifiedName.substring(0, index);         
//         localName = qualifiedName.substring(index + 1);         
//      }
//      
//      Namespace namespace = getNamespaceForPrefix(prefix);      
//      if (namespace != null)
//      {         
//         return getDocumentFactory().createQName(localName, namespace);         
//      }      
//      else
//      {         
//         return getDocumentFactory().createQName(localName);         
//      }    
      return QName.get(qualifiedName);
   }
   
   
   /** <p>Returns the <code>Namespace</code> which is mapped to the given
    * prefix or null if it could not be found.</p>
    *
    * @return the <code>Namespace</code> associated with the given prefix
    */
   public Namespace getNamespaceForPrefix(String prefix)
   {
      if (prefix == null)
      {         
         prefix = "";         
      }
      
      if (prefix.equals(getNamespacePrefix()))
      {         
         return getNamespace();         
      }
      
      else if (prefix.equals("xml"))
      {         
         return Namespace.XML_NAMESPACE;         
      }
      
//      else
//      {         
//         List list = contentList();         
//         int size = list.size();         
//         for (int i = 0; i < size; i++)
//         {            
//            Object object = list.get(i);            
//            if (object instanceof Namespace)
//            {               
//               Namespace namespace = (Namespace) object;               
//               if (prefix.equals(namespace.getPrefix()))
//               {                  
//                  return namespace;                  
//               }               
//            }            
//         }         
//      }
//      
//      Element parent = getParent();      
//      if (parent != null)
//      {         
//         Namespace answer = parent.getNamespaceForPrefix(prefix);         
//         if (answer != null)
//         {            
//            return answer;            
//         }         
//      }
//      
//      if (prefix == null || prefix.length() <= 0)
      {         
         return Namespace.NO_NAMESPACE;         
      }
   }
   
   /** <p>Returns the <code>Namespace</code> which is mapped to the given
    * URI or null if it could not be found.</p>
    *
    * @return the <code>Namespace</code> associated with the given URI
    */
   public Namespace getNamespaceForURI(String uri)
   {
      return null;
   }
   
   /** <p>Returns the namespace prefix of this element if one exists
    * otherwise an empty <code>String</code> is returned.</p>
    *
    * @return the prefix of the <code>Namespace</code> of this element
    * or an empty <code>String</code>
    */
   public String getNamespacePrefix()
   {
      return getProxyNode().getPrefix();
   }
   
   /** <p>Returns the URI mapped to the namespace of this element
    * if one exists otherwise an empty <code>String</code> is returned.</p>
    *
    * @return the URI for the <code>Namespace</code> of this element
    * or an empty <code>String</code>
    */
   public String getNamespaceURI()
   {
      return getProxyNode().getNamespaceURI();
   }
   
   /** <p>Returns the fully qualified name of this element.
    * This will be the same as the value returned from {@link #getName}
    * if this element has no namespace attached to this element or an
    * expression of the form
    * <pre>
    * getNamespacePrefix() + ":" + getName()
    * </pre>
    * will be returned.
    *
    * @return the fully qualified name of the element.
    */
   public String getQualifiedName()
   {
      return getQName().getQualifiedName();
   }
   
   
   /** <p>Returns any additional namespaces declarations for this element
    * other than namespace returned via the {@link #getNamespace()} method.
    * If no additional namespace declarations are present for this
    * element then an empty list will be returned.
    *
    * The list is backed by the element such that changes to the list will
    * be reflected in the element though the reverse is not the case.</p>
    *
    * @return a list of any additional namespace declarations.
    */
   public List additionalNamespaces()
   {
      return new ArrayList();
   }
   
   /** <p>Returns all the namespaces declared by this element.
    * If no namespaces are declared for this element then
    * an empty list will be returned.
    *
    * The list is backed by the element such that changes to the list will
    * be reflected in the element though the reverse is not the case.</p>
    *
    * @return a list of namespaces declared for this element.
    */
   public List declaredNamespaces()
   {
      return new ArrayList();
   }
   
   
   
   // Builder methods
   //-------------------------------------------------------------------------
   
   /** <p>Adds the attribute value of the given local name.
    * If an attribute already exists for the given name it will be replaced.
    * Attributes with null values are silently ignored.
    * If the value of the attribute is null then this method call will
    * remove any attributes with the given name.</p>
    *
    * @param name is the name of the attribute whose value is to be added
    * or updated
    * @param value is the attribute's value
    * @return this <code>Element</code> instance.
    */
   public Element addAttribute(String name, String value)
   {
      throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
   }
   
   /** <p>Adds the attribute value of the given fully qualified name.
    * If an attribute already exists for the given name it will be replaced.
    * Attributes with null values are silently ignored.
    * If the value of the attribute is null then this method call will
    * remove any attributes with the given name.</p>
    *
    * @param qName is the fully qualified name of the attribute
    * whose value is to be added or updated
    * @param value is the attribute's value
    * @return this <code>Element</code> instance.
    */
   public Element addAttribute(QName qName, String value)
   {
      throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
   }
   
   /** Adds a new <code>Comment</code> node with the given text to this element.
    *
    * @param comment is the text for the <code>Comment</code> node.
    * @return this <code>Element</code> instance.
    */
   public Element addComment(String comment)
   {
      throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
   }
   
   
   /** Adds a new <code>CDATA</code> node with the given text to this element.
    *
    * @param cdata is the text for the <code>CDATA</code> node.
    * @return this <code>Element</code> instance.
    */
   public Element addCDATA(String cdata)
   {
      throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
   }
   
   /** Adds a new <code>Entity</code> node with the given name and text
    * to this element and returns a reference to the new node.
    *
    * @param name is the name for the <code>Entity</code> node.
    * @param text is the text for the <code>Entity</code> node.
    * @return this <code>Element</code> instance.
    */
   public Element addEntity(String name, String text)
   {
      throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
   }
   
   
   /** Adds a namespace to this element for use by its child content
    *
    * @param prefix is the prefix to use, which should not be null or blank
    * @param uri is the namespace URI
    * @return this <code>Element</code> instance.
    */
   public Element addNamespace(String prefix, String uri)
   {
      throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
   }
   
   /** Adds a processing instruction for the given target
    *
    * @param target is the target of the processing instruction
    * @param text is the textual data (key/value pairs) of the processing instruction
    * @return this <code>Element</code> instance.
    */
   public Element addProcessingInstruction(String target, String text)
   {
      throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
   }
   
   /** Adds a processing instruction for the given target
    *
    * @param target is the target of the processing instruction
    * @param data is a Map of the key / value pairs of the processing instruction
    * @return this <code>Element</code> instance.
    */
   public Element addProcessingInstruction(String target, Map data)
   {
      throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
   }
   
   /** Adds a new <code>Text</code> node with the given text to this element.
    *
    * @param text is the text for the <code>Text</code> node.
    * @return this <code>Element</code> instance.
    */
   public Element addText(String text)
   {
      throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
   }
   
   
   // Typesafe modifying methods
   //-------------------------------------------------------------------------
   
   
   /** Adds the given <code>Attribute</code> to this element.
    * If the given node already has a parent defined then an
    * <code>InvalidAddNodeException</code> will be thrown.
    * Attributes with null values are silently ignored.
    * If the value of the attribute is null then this method call will
    * remove any attributes with the QName of this attribute.</p>
    *
    * @param attribute is the attribute to be added
    */
   public void add(Attribute attribute)
   {
      throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
   }
   
   
   /** Adds the given <code>CDATA</code> to this element.
    * If the given node already has a parent defined then an
    * <code>InvalidAddNodeException</code> will be thrown.
    *
    * @param cdata is the CDATA to be added
    */
   public void add(CDATA cdata)
   {
      throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
   }
   
   /** Adds the given <code>Entity</code> to this element.
    * If the given node already has a parent defined then an
    * <code>InvalidAddNodeException</code> will be thrown.
    *
    * @param entity is the entity to be added
    */
   public void add(Entity entity)
   {
      throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
   }
   
   /** Adds the given <code>Text</code> to this element.
    * If the given node already has a parent defined then an
    * <code>InvalidAddNodeException</code> will be thrown.
    *
    * @param text is the text to be added
    */
   public void add(Text text)
   {
      throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
   }
   
   /** Adds the given <code>Namespace</code> to this element.
    * If the given node already has a parent defined then an
    * <code>InvalidAddNodeException</code> will be thrown.
    *
    * @param namespace is the namespace to be added
    */
   public void add(Namespace namespace)
   {
      throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
   }
   
   /** Removes the given <code>Attribute</code> from this element.
    *
    * @param attribute is the attribute to be removed
    * @return true if the attribute was removed
    */
   public boolean remove(Attribute attribute)
   {
      throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
   }
   
   /** Removes the given <code>CDATA</code> if the node is
    * an immediate child of this element.
    *
    * If the given node is not an immediate child of this element
    * then the {@link Node#detach()} method should be used instead.
    *
    * @param cdata is the CDATA to be removed
    * @return true if the cdata was removed
    */
   public boolean remove(CDATA cdata)
   {
      throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
   }
   
   /** Removes the given <code>Entity</code> if the node is
    * an immediate child of this element.
    *
    * If the given node is not an immediate child of this element
    * then the {@link Node#detach()} method should be used instead.
    *
    * @param entity is the entity to be removed
    * @return true if the entity was removed
    */
   public boolean remove(Entity entity)
   {
      throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
   }
   
   /** Removes the given <code>Namespace</code> if the node is
    * an immediate child of this element.
    *
    * If the given node is not an immediate child of this element
    * then the {@link Node#detach()} method should be used instead.
    *
    * @param namespace is the namespace to be removed
    * @return true if the namespace was removed
    */
   public boolean remove(Namespace namespace)
   {
      throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
   }
   
   /** Removes the given <code>Text</code> if the node is
    * an immediate child of this element.
    *
    * If the given node is not an immediate child of this element
    * then the {@link Node#detach()} method should be used instead.
    *
    * @param text is the text to be removed
    * @return true if the text was removed
    */
   public boolean remove(Text text)
   {
      throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
   }
   
   
   // Text methods
   //-------------------------------------------------------------------------
   
   /** Returns the text value of this element without recursing through
    * child elements.
    * This method iterates through all {@link Text}, {@link CDATA} and
    * {@link Entity} nodes that this element contains
    * and appends the text values together.
    *
    * @return the textual content of this Element. Child elements are not navigated.
    */
   public String getText()
   {
      return getProxyNode().getTextContent();
   }
   
   /** @return the trimmed text value where whitespace is trimmed and
    * normalised into single spaces
    */
   public String getTextTrim()
   {
      return getText().trim();
   }
   
   
   /** Returns the XPath string-value of this node.
    * The behaviour of this method is defined in the
    * <a href="http://www.w3.org/TR/xpath">XPath specification</a>.
    *
    * This method returns the string-value of all the contained
    * {@link Text}, {@link CDATA}, {@link Entity} and {@link Element} nodes
    * all appended together.
    *
    * @return the text from all the child Text and Element nodes appended
    * together.
    */
   public String getStringValue()
   {
      return "";
   }
   
   
   /** Accesses the data of this element which may implement data typing
    * bindings such as XML Schema or
    * Java Bean bindings or will return the same value as {@link #getText}
    */
   public Object getData()
   {
      return getText();
   }
   
   /** Sets the data value of this element if this element supports data
    * binding or calls {@link #setText} if it doesn't
    */
   public void setData(Object data)
   {
      throw new DTMDOMException(DOMException.INVALID_MODIFICATION_ERR);
   }
   
   public void setText(String data)
   {
      throw new DTMDOMException(DOMException.INVALID_MODIFICATION_ERR);
   }
   
   // Attribute methods
   //-------------------------------------------------------------------------
   
   
   /** <p>Returns the {@link Attribute} instances this element contains as
    * a backed {@link List} so that the attributes may be modified directly
    * using the {@link List} interface.
    * The <code>List</code> is backed by the <code>Element</code> so that
    * changes to the list are reflected in the element and vice versa.</p>
    *
    * @return the attributes that this element contains as a <code>List</code>
    */
   public List attributes()
   {
      NamedNodeMap map = getProxyNode().getAttributes();
     
      List retVal = new ArrayList();
      for(int index = 0; index < map.getLength(); index++)
      {
         retVal.add(map.item(index));
      }
      
      return retVal;
   }
   
   /** Sets the attributes that this element contains
    */
   public void setAttributes(List attributes)
   {
      throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
   }
   
   /** @return the number of attributes this element contains
    */
   public int attributeCount()
   {
      NamedNodeMap map = getProxyNode().getAttributes();
      return map.getLength();
   }
   
   /** @returns an iterator over the attributes of this element
    */
   public Iterator attributeIterator()
   {
      return attributes().iterator();
   }
   
   /** Returns the attribute at the specified indexGets the
    *
    * @return the attribute at the specified index where
    * index >= 0 and index < number of attributes or throws
    * an IndexOutOfBoundsException if the index is not within the
    * allowable range
    */
   public Attribute attribute(int index)
   {
      NamedNodeMap map = getProxyNode().getAttributes();
     
      Attribute retVal = null;
      if(index < map.getLength())
      {
         org.w3c.dom.Node obj = map.item(index);
         if(obj instanceof Attribute)
         {
             retVal = (Attribute)obj;
         }
         else if(obj instanceof DTMNodeProxy)
         {
             retVal = new W3CAttributeProxy((DTMNodeProxy)obj);
         }
      }
      
      return retVal;
   }
   
   /** Returns the attribute with the given name
    *
    * @return the attribute for the given local name in any namespace.
    * If there are more than one attributes with the given local name
    * in different namespaces then the first one is returned.
    */
   public Attribute attribute(String name)
   {
      NamedNodeMap map = getProxyNode().getAttributes();      
      Attr attribute = (Attr)map.getNamedItem(name);
      
      DefaultAttribute attr = new DefaultAttribute(attribute.getName(),
                                                   attribute.getValue());
      attr.setParent((Element)attribute.getParentNode());
      return attr;
   }
   
   /** @param qName is the fully qualified name
    * @return the attribute for the given fully qualified name or null if
    * it could not be found.
    */
   public Attribute attribute(QName qname)
   {
      NamedNodeMap map = getProxyNode().getAttributes();      
      Attr attribute = (Attr)map.getNamedItemNS(qname.getNamespaceURI(), 
                                                qname.getName());
      
      DefaultAttribute attr = new DefaultAttribute(qname);
      attr.setParent((Element)attribute.getParentNode());
      return attr;
   }
   
   /** <p>This returns the attribute value for the attribute with the
    * given name and any namespace or null if there is no such
    * attribute or the empty string if the attribute value is empty.</p>
    *
    * @param name is the name of the attribute value to be returnd
    * @return the value of the attribute, null if the attribute does
    * not exist or the empty string
    */
   public String attributeValue(String name)
   {
      return getProxyNode().getAttribute(name);
   }
   
   /** <p>This returns the attribute value for the attribute with the
    * given name and any namespace or the default value if there is
    * no such attribute value.</p>
    *
    * @param name is the name of the attribute value to be returnd
    * @param defaultValue is the default value to be returned if the
    *    attribute has no value defined.
    * @return the value of the attribute or the defaultValue if the
    *    attribute has no value defined.
    */
   public String attributeValue(String name, String defaultValue)
   {
      String retVal = attributeValue(name);
      if((retVal == null) || (retVal.length() <= 0))
      {
         retVal = defaultValue;
      }
      
      return retVal;
   }
   
   /** <p>This returns the attribute value for the attribute with the
    * given fully qualified name or null if there is no such
    * attribute or the empty string if the attribute value is empty.</p>
    *
    * @param qName is the fully qualified name
    * @return the value of the attribute, null if the attribute does
    * not exist or the empty string
    */
   public String attributeValue(QName qName)
   {
      return getProxyNode().getAttributeNS(qName.getNamespaceURI(),
                                           qName.getName());
   }
   
   /** <p>This returns the attribute value for the attribute with the
    * given fully qualified name or the default value if
    * there is no such attribute value.</p>
    *
    * @param qName is the fully qualified name
    * @param defaultValue is the default value to be returned if the
    *    attribute has no value defined.
    * @return the value of the attribute or the defaultValue if the
    *    attribute has no value defined.
    */
   public String attributeValue(QName qName, String defaultValue)
   {
      String retVal = attributeValue(qName);
      if((retVal == null) || (retVal.length() <= 0))
      {
         retVal = defaultValue;
      }
      
      return retVal;
   }
   
   
   /** <p>Sets the attribute value of the given local name.</p>
    *
    * @param name is the name of the attribute whose value is to be added
    * or updated
    * @param value is the attribute's value
    *
    * @deprecated As of version 0.5. Please use
    *    {@link #addAttribute(String,String)} instead.
    */
   public void setAttributeValue(String name, String value)
   {
      throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
   }
   
   /** <p>Sets the attribute value of the given fully qualified name.</p>
    *
    * @param qName is the fully qualified name of the attribute
    * whose value is to be added or updated
    * @param value is the attribute's value
    *
    * @deprecated As of version 0.5. Please use
    *    {@link #addAttribute(QName,String)} instead.
    */
   public void setAttributeValue(QName qName, String value)
   {
      throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
   }
   
   
   // Content methods
   //-------------------------------------------------------------------------
   
   
   /** Returns the first element for the given local name and any namespace.
    *
    * @return the first element with the given local name
    */
   public Element element(String name)
   {
      NodeList list = getProxyNode().getElementsByTagName(name);
      
      Element retVal = null;
      if(list.getLength() > 0)
      {
         retVal = (Element)list.item(0);
      }
      return retVal;
   }
   
   /** Returns the first element for the given fully qualified name.
    *
    * @param qName is the fully qualified name to search for
    * @return the first element with the given fully qualified name
    */
   public Element element(QName qname)
   {
      NodeList list = getProxyNode().getElementsByTagNameNS(qname.getNamespaceURI(),
                                                            qname.getName());
      
      Element retVal = null;
      if(list.getLength() > 0)
      {
         retVal = (Element)list.item(0);
      }
      return retVal;
   }
   
   /** <p>Returns the elements contained in this element.
    * If this element does not contain any elements then this method returns
    * an empty list.
    *
    * The list is backed by the element such that changes to the list will
    * be reflected in the element though the reverse is not the case.</p>
    *
    * @return a list of all the elements in this element.
    */
   public List elements()
   {
      NodeList children = getProxyNode().getChildNodes();
      
      List retVal = new ArrayList();      
      for(int index = 0; index < children.getLength(); index++)
      {
         Node child = (Node)children.item(index);
         if(child instanceof Element)
         {
            retVal.add(child);
         }
      }
      
      return retVal;
   }
   
   /** <p>Returns the elements contained in this element with the given
    * local name and any namespace.
    * If no elements are found then this method returns an empty list.
    *
    * The list is backed by the element such that changes to the list will
    * be reflected in the element though the reverse is not the case.</p>
    *
    * @return a list of all the elements in this element for the given
    * local name
    */
   public List elements(String name)
   {
      NodeList children = getProxyNode().getElementsByTagName(name);
   
      List retVal = new ArrayList();      
      for(int index = 0; index < children.getLength(); index++)
      {
         Node child = (Node)children.item(index);
         if(child instanceof Element)
         {
            retVal.add(child);
         }
      }
      
      return retVal;
   }
   
   /** <p>Returns the elements contained in this element with the given
    * fully qualified name.
    * If no elements are found then this method returns an empty list.
    *
    * The list is backed by the element such that changes to the list will
    * be reflected in the element though the reverse is not the case.</p>
    *
    * @param qName is the fully qualified name to search for
    * @return a list of all the elements in this element for the
    * given fully qualified name.
    */
   public List elements(QName qName)
   {
      NodeList children = getProxyNode().getElementsByTagNameNS(qName.getNamespaceURI(),
                                                                qName.getName());
   
      List retVal = new ArrayList();      
      for(int index = 0; index < children.getLength(); index++)
      {
         Node child = (Node)children.item(index);
         if(child instanceof Element)
         {
            retVal.add(child);
         }
      }
      
      return retVal;
   }
   
   /** Returns an iterator over all this elements child elements.
    *
    * @return an iterator over the contained elements
    */
   public Iterator elementIterator()
   {
      List children = elements();      
      return children.iterator();
   }
   
   /** Returns an iterator over the elements contained in this element
    * which match the given local name and any namespace.
    *
    * @return an iterator over the contained elements matching the given
    * local name
    */
   public Iterator elementIterator(String name)
   {
      List children = elements(name);      
      return children.iterator();
   }
   
   /** Returns an iterator over the elements contained in this element
    * which match the given fully qualified name.
    *
    * @param qName is the fully qualified name to search for
    * @return an iterator over the contained elements matching the given
    * fully qualified name
    */
   public Iterator elementIterator(QName qname)
   {
      List children = elements(qname);      
      return children.iterator();
   }
   
   
   
   // Helper methods
   //-------------------------------------------------------------------------
   
   /** @return true if this element is the root element of a document
    * and this element supports the parent relationship else false.
    */
   public boolean isRootElement()
   {
      return false;
   }
   
   /** <p>Returns true if this <code>Element</code> has mixed content.
    * Mixed content means that an element contains both textual data and
    * child elements.
    *
    * @return true if this element contains mixed content.
    */
   public boolean hasMixedContent()
   {
      return true;
   }
   
   /** <p>Returns true if this <code>Element</code> has text only content.
    *
    * @return true if this element is empty or only contains text content.
    */
   public boolean isTextOnly()
   {
      return false;
   }
   
   
   /** Appends the attributes of the given element to me.
    * This method behaves like the {@link Collection#addAll(java.util.Collection)}
    * method.
    *
    * @param element is the element whose attributes will be added to me.
    */
   public void appendAttributes(Element element)
   {
      throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
   }
   
   /** <p>Creates a deep copy of this element
    * The new element is detached from its parent, and getParent() on the
    * clone will return null.</p>
    *
    * @return a new deep copy Element
    */
   public Element createCopy()
   {
      throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
   }
   
   /** <p>Creates a deep copy of this element with the given local name
    * The new element is detached from its parent, and getParent() on the
    * clone will return null.</p>
    *
    * @return a new deep copy Element
    */
   public Element createCopy(String name)
   {
      throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
   }
   
   /** <p>Creates a deep copy of this element with the given fully qualified name.
    * The new element is detached from its parent, and getParent() on the
    * clone will return null.</p>
    *
    * @return a new deep copy Element
    */
   public Element createCopy(QName qName)
   {
      throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
   }
   
   public String elementText(String name)
   {
      throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
   }
   public String elementText(QName qname)
   {
      throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
   }
   public String elementTextTrim(String name)
   {
      throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
   }
   public String elementTextTrim(QName qname)
   {
      throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
   }
   
   /** Returns a node at the given index suitable for an XPath result set.
    * This means the resulting Node will either be null or it will support
    * the parent relationship.
    *
    * @return the Node for the given index which will support the parent
    * relationship or null if there is not a node at the given index.
    */
   public Node getXPathResult(int index)
   {
      throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
   }
   
   ////////////////////////////////////////////////////////////////////////////
   // Branch Implementation
   
   /** Returns the <code>Node</code> at the specified index position.
    *
    * @param index the index of the node to return.
    * @return the <code>Node</code> at the specified position.
    *
    * @throws IndexOutOfBoundsException if the index is out of range (index
    *           &lt; 0 || index &gt;= {@link #nodeCount}).
    */
   public Node node(int index)
   {       
       Node retVal = null;
       
       NodeList list = getProxyNode().getChildNodes();
       if(index >= list.getLength())
       {
           throw new IndexOutOfBoundsException();
       }
       
       retVal = convertNode(list.item(index));
       return retVal;
   }
   
   /** Returns the index of the given node if it is a child node of this
    * branch or -1 if the given node is not a child node.
    *
    * @param node the content child node to find.
    * @return the index of the given node starting at 0 or -1 if the node
    *     is not a child node of this branch
    */
   public int indexOf(Node node)
   {
      int retVal = -1;
      
      if(node.getName().length() == 0)
      {
          retVal = 0;
      }
      else
      {
          NodeList list = getProxyNode().getChildNodes();
          for(int index = 0; index < list.getLength(); index++)
          {
              Node curNode = convertNode(list.item(index));          
              if(curNode.equals(node))
              {
                  retVal = index;
                  break;
              }
          }
      }
      
      return retVal;
   }
   
   /** Returns the number of <code>Node</code> instances that this branch
    * contains.
    *
    * @return the number of nodes this branch contains
    */
   public int nodeCount()
   {
      return getProxyNode().getChildNodes().getLength();
   }
   
   /** Returns the element of the given ID attribute value. If this tree
    * is capable of understanding which attribute value should be used for
    * the ID then it should be used, otherwise this method should return null.
    */
   public Element elementByID(String elementID)
   {
      throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
   }
   
   
   /** <p>Returns the content nodes of this branch as a backed {@link List}
    * so that the content of this branch may be modified directly using
    * the {@link List} interface.
    * The <code>List</code> is backed by the <code>Branch</code> so that
    * changes to the list are reflected in the branch and vice versa.</p>
    *
    * @return the nodes that this branch contains as a <code>List</code>
    */
   public List content()
   {
      throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
   }
   
   /** Returns an iterator through the content nodes of this branch
    *
    * @return an iterator through the content nodes of this branch
    */
   public Iterator nodeIterator()
   {
      throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
   }
   
   /** Sets the contents of this branch as a <code>List</code> of
    * <code>Node</code> instances.
    *
    * @param content is the list of nodes to use as the content for this
    *   branch.
    */
   public void setContent(List content)
   {
      throw new DTMDOMException(DOMException.INVALID_MODIFICATION_ERR);
   }
   
   /** Appends the content of the given branch to this branch instance.
    * This method behaves like the {@link Collection#addAll(java.util.Collection)}
    * method.
    *
    * @param element is the element whose content will be added to me.
    */
   public void appendContent(Branch branch)
   {
      throw new DTMDOMException(DOMException.INVALID_MODIFICATION_ERR);
   }
   
   /** Clears the content for this branch, removing any <code>Node</code>
    * instances this branch may contain.
    */
   public void clearContent()
   {
      throw new DTMDOMException(DOMException.INVALID_MODIFICATION_ERR);
   }
   
   /** <p>Returns a list of all the processing instructions in this branch.
    * The list is backed by this branch so that changes to the list will
    * be reflected in the branch but the reverse is not the case.</p>
    *
    * @return a backed list of the processing instructions
    */
   public List processingInstructions()
   {
      throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
   }
   
   /** <p>Returns a list of the processing instructions for the given target.
    * The list is backed by this branch so that changes to the list will
    * be reflected in the branch but the reverse is not the case.</p>
    *
    * @return a backed list of the processing instructions
    */
   public List processingInstructions(String target)
   {
      throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
   }
   
   
   /** @return the processing instruction for the given target
    */
   public ProcessingInstruction processingInstruction(String target)
   {
      throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
   }
   
   /** Sets all the processing instructions for this branch
    */
   public void setProcessingInstructions(List listOfPIs)
   {
      throw new DTMDOMException(DOMException.INVALID_MODIFICATION_ERR);
   }
   
   
   /** Adds a new <code>Element</code> node with the given name to this branch
    * and returns a reference to the new node.
    *
    * @param name is the name for the <code>Element</code> node.
    * @return the newly added <code>Element</code> node.
    */
   public Element addElement(String name)
   {
      throw new DTMDOMException(DOMException.INVALID_MODIFICATION_ERR);
   }
   
   /** Adds a new <code>Element</code> node with the given {@link QName}
    * to this branch and returns a reference to the new node.
    *
    * @param qname is the qualified name for the <code>Element</code> node.
    * @return the newly added <code>Element</code> node.
    */
   public Element addElement(QName qname)
   {
      throw new DTMDOMException(DOMException.INVALID_MODIFICATION_ERR);
   }
   
   /** Adds a new <code>Element</code> node with the given qualified name
    * and namespace URI to this branch and returns a reference to the new node.
    *
    * @param qualifiedName is the fully qualified name of the Element
    * @param namespaceURI is the URI of the namespace to use
    * @return the newly added <code>Element</code> node.
    */
   public Element addElement(String qualifiedName, String namespaceURI)
   {
      throw new DTMDOMException(DOMException.INVALID_MODIFICATION_ERR);
   }
   
   /** Removes the processing instruction for the given target if it exists
    *
    * @return true if a processing instruction was removed else false
    */
   public boolean removeProcessingInstruction(String target)
   {
      throw new DTMDOMException(DOMException.INVALID_MODIFICATION_ERR);
   }
   
   /** Adds the given <code>Node</code> or throws {@link IllegalAddException}
    * if the given node is not of a valid type. This is a polymorphic method
    * which will call the typesafe method for the node type such as
    * add(Element) or add(Comment).
    *
    * @param node is the given node to add
    */
   public void add(Node node)
   {
      throw new DTMDOMException(DOMException.INVALID_MODIFICATION_ERR);
   }
   
   /** Adds the given <code>Comment</code> to this branch.
    * If the given node already has a parent defined then an
    * <code>InvalidAddNodeException</code> will be thrown.
    *
    * @param comment is the comment to be added
    */
   public void add(Comment comment)
   {
      throw new DTMDOMException(DOMException.INVALID_MODIFICATION_ERR);
   }
   
   /** Adds the given <code>Element</code> to this branch.
    * If the given node already has a parent defined then an
    * <code>InvalidAddNodeException</code> will be thrown.
    *
    * @param element is the element to be added
    */
   public void add(Element element)
   {
      throw new DTMDOMException(DOMException.INVALID_MODIFICATION_ERR);
   }
   
   /** Adds the given <code>ProcessingInstruction</code> to this branch.
    * If the given node already has a parent defined then an
    * <code>InvalidAddNodeException</code> will be thrown.
    *
    * @param pi is the processing instruction to be added
    */
   public void add(ProcessingInstruction pi)
   {
      throw new DTMDOMException(DOMException.INVALID_MODIFICATION_ERR);
   }
   
   /** Removes the given <code>Node</code> if the node is
    * an immediate child of this branch.
    *
    * If the given node is not an immediate child of this branch
    * then the {@link Node#detach()} method should be used instead.
    *
    * This is a polymorphic method which will call the typesafe method
    * for the node type such as remove(Element) or remove(Comment).
    *
    * @param node is the given node to be removed
    * @return true if the node was removed
    */
   public boolean remove(Node node)
   {
      throw new DTMDOMException(DOMException.INVALID_MODIFICATION_ERR);
   }
   
   /** Removes the given <code>Comment</code> if the node is
    * an immediate child of this branch.
    *
    * If the given node is not an immediate child of this branch
    * then the {@link Node#detach()} method should be used instead.
    *
    * @param comment is the comment to be removed
    * @return true if the comment was removed
    */
   public boolean remove(Comment comment)
   {
      throw new DTMDOMException(DOMException.INVALID_MODIFICATION_ERR);
   }
   
   /** Removes the given <code>Element</code> if the node is
    * an immediate child of this branch.
    *
    * If the given node is not an immediate child of this branch
    * then the {@link Node#detach()} method should be used instead.
    *
    * @param element is the element to be removed
    * @return true if the element was removed
    */
   public boolean remove(Element element)
   {
      throw new DTMDOMException(DOMException.INVALID_MODIFICATION_ERR);
   }
   
   /** Removes the given <code>ProcessingInstruction</code> if the node is
    * an immediate child of this branch.
    *
    * If the given node is not an immediate child of this branch
    * then the {@link Node#detach()} method should be used instead.
    *
    * @param pi is the processing instruction to be removed
    * @return true if the processing instruction was removed
    */
   public boolean remove(ProcessingInstruction pi)
   {
      throw new DTMDOMException(DOMException.INVALID_MODIFICATION_ERR);
   }  
   
   /**
    * Puts all <code>Text</code> nodes in the full depth of the sub-tree
    * underneath this <code>Node</code>, including attribute nodes, into a
    * "normal" form where only structure (e.g., elements, comments,
    * processing instructions, CDATA sections, and entity references)
    * separates <code>Text</code> nodes, i.e., there are neither adjacent
    * <code>Text</code> nodes nor empty <code>Text</code> nodes. This can
    * be used to ensure that the DOM view of a document is the same as if
    * it were saved and re-loaded, and is useful when operations (such as
    * XPointer  lookups) that depend on a particular document tree
    * structure are to be used.In cases where the document contains
    * <code>CDATASections</code>, the normalize operation alone may not be
    * sufficient, since XPointers do not differentiate between
    * <code>Text</code> nodes and <code>CDATASection</code> nodes.
    * @version DOM Level 2
    */
   public void normalize()
   {
      throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
   }
   
   ////////////////////////////////////////////////////////////////////////////
   // Node Implementation
   
   /** <p><code>supportsParent</code> returns true if this node supports the
    * parent relationship.</p>
    *
    * <p>Some XML tree implementations are singly linked and only support
    * downward navigation through children relationships.
    * The default case is that both parent and children relationships are
    * supported though for memory and performance reasons the parent
    * relationship may not be supported.
    * </p>
    *
    * @return true if this node supports the parent relationship
    * or false it is not supported
    */
   public boolean supportsParent()
   {
      return true;
   }
   
   /** <p><code>getParent</code> returns the parent <code>Element</code>
    * if this node supports the parent relationship or null if it is
    * the root element or does not support the parent relationship.</p>
    *
    * <p>This method is an optional feature and may not be supported
    * for all <code>Node</code> implementations.</p>
    *
    * @return the parent of this node or null if it is the root of the
    * tree or the parent relationship is not supported.
    */
   public Element getParent()
   {
//      return (Element)getProxyNode().getParentNode();
       
       Element retVal = null;
       
       org.w3c.dom.Node obj = getProxyNode().getParentNode();
       
       if(obj instanceof DTMNodeProxy)
       {
           retVal = new W3CNodeProxy((DTMNodeProxy)obj);
       }
       else if(obj instanceof Element)
       {
           retVal = (Element)obj;
       }
       return retVal;
   }
   
   /** <p><code>setParent</code> sets the parent relationship of
    * this node if the parent relationship is supported or does nothing
    * if the parent relationship is not supported.</p>
    *
    * <p>This method should only be called from inside an
    * <code>Element</code> implementation method and is not intended for
    * general use.</p>
    *
    * @param parent is the new parent of this node.
    */
   public void setParent(Element parent)
   {
      throw new DTMDOMException(DOMException.INVALID_MODIFICATION_ERR);
   }
   
   
   /** <p><code>getDocument</code> returns the <code>Document</code>
    * that this <code>Node</code> is part of if this node supports
    * the parent relationship.</p>
    *
    * <p>This method is an optional feature and may not be supported
    * for all <code>Node</code> implementations.</p>
    *
    * @return the document of this node or null if this feature is not
    * supported or the node is not associated with a <code>Document</code>
    */
   public Document getDocument()
   {
      return (Document)XSLTHelper.getDocument();
   }
   
   /** <p><code>setDocument</code> sets the document of this node if the
    * parent relationship is supported or does nothing if the parent
    * relationship is not supported.</p>
    *
    * <p>This method should only be called from inside a
    * <code>Document</code> implementation method and is not intended for
    * general use.</p>
    *
    * @param document is the new document of this node.
    */
   public void setDocument(Document document)
   {
      throw new DTMDOMException(DOMException.INVALID_MODIFICATION_ERR);
   }
   
   
   /** <p><code>isReadOnly</code> returns true if this node is read only
    * and cannot be modified.
    * Any attempt to modify a read-only <code>Node</code> will result in
    * an <code>UnsupportedOperationException</code> being thrown.</p>
    *
    * @return true if this <code>Node</code> is read only
    * and cannot be modified otherwise false.
    */
   public boolean isReadOnly()
   {
      return true;
   }
   
   /** <p><code>hasContent</code> returns true if this node is a Branch
    * (either an Element or a Document) and it contains at least one
    * content node such as a child Element or Text node.</p>
    *
    * @return true if this <code>Node</code> is a Branch
    * with a nodeCount() of one or more.
    */
   public boolean hasContent()
   {
      return nodeCount() > 0;
   }
   
   
   
   /** <p><code>getName</code> returns the name of this node.
    * This is the XML local name of the element, attribute, entity or
    * processing instruction.
    * For CDATA and Text nodes this method will return null.</p>
    *
    * @return the XML name of this node
    */
   public String getName()
   {
      return getProxyNode().getNodeName();
   }
   
   
   /** <p>Sets the text data of this node or this method will
    * throw an <code>UnsupportedOperationException</code> if it is
    * read-only.</p>
    *
    * @param name is the new name of this node
    */
   public void setName(String name)
   {
      throw new DTMDOMException(DOMException.INVALID_MODIFICATION_ERR);
   }
   
   /** <p>Returns the XPath expression which will return a node set
    * containing the given node such as /a/b/@c. No indexing will
    * be used to restrict the path if multiple elements with the
    * same name occur on the path.</p>
    *
    * @return the XPath expression which will return a nodeset
    * containing at least this node.
    */
   public String getPath()
   {
      throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
   }
   
   /** <p>Returns the relative XPath expression which will return a node set
    * containing the given node such as a/b/@c. No indexing will
    * be used to restrict the path if multiple elements with the
    * same name occur on the path.
    *
    * @param context is the parent context from which the relative path should
    * start. If the context is null or the context is not an ancestor of
    * this node then the path will be absolute and start from the document and so
    * begin with the '/' character.
    *
    * @return the XPath expression relative to the given context
    * which will return a nodeset containing at least this node.
    */
   public String getPath(Element context)
   {
       throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
   }
   
   /** <p>Returns the XPath expression which will return a nodeset
    * of one node which is the current node. This method will use
    * the XPath index operator to restrict the path if
    * multiple elements with the same name occur on the path.</p>
    *
    * @return the XPath expression which will return a nodeset
    * containing just this node.
    */
   public String getUniquePath()
   {
      throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
   }
   
   /** <p>Returns the relative unique XPath expression from the given context
    * which will return a nodeset
    * of one node which is the current node.
    * This method will use the XPath index operator to restrict the
    * path if multiple elements with the same name occur on the path.
    * </p>
    *
    * @param context is the parent context from which the path should
    * start. If the context is null or the context is not an ancestor of
    * this node then the path will start from the document and so
    * begin with the '/' character.
    *
    * @return the XPath expression relative to the given context
    * which will return a nodeset containing just this node.
    */
   public String getUniquePath(Element context)
   {
      throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
   }
   
   
   /** <p><code>asXML</code> returns the textual XML representation of this
    * node.</p>
    *
    * @return the XML representation of this node
    */
   public String asXML()
   {
      throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
   }
   
   /** <p><code>write</code> writes this node as the default XML
    * notation for this node. If you wish to control the XML output
    * (such as for pretty printing, changing the indentation policy etc.)
    * then please use {@link org.dom4j.io.XMLWriter} or its derivations.
    *
    * @param writer is the <code>Writer</code> to output the XML to
    */
   public void write(Writer writer) throws IOException
   {
      throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
   }
   
   
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
   
   /** @return the name of the type of node such as "Document", "Element", "Attribute" or "Text"
    */
   public String getNodeTypeName()
   {
      return "Element";
   }
   
   
   /** <p>Removes this node from its parent if there is one.
    * If this node is the root element of a document then it is removed
    * from the document as well.</p>
    *
    * <p>This method is useful if you want to remove
    * a node from its source document and add it to another document.
    * For example</p>
    *
    * <code>
    *     Node node = ...;
    *     Element someOtherElement = ...;
    *     someOtherElement.add( node.detach() );
    * </code>
    *
    * @return the node that has been removed from its parent node if
    * any and its document if any.
    */
   public Node detach()
   {
      throw new DTMDOMException(DOMException.INVALID_MODIFICATION_ERR);
   }
   
   
   
   /** <p><code>selectNodes</code> evaluates an XPath expression and returns
    * the result as a <code>List</code> of <code>Node</code> instances or
    * <code>String</code> instances depending on the XPath expression.</p>
    *
    * @param xpathExpression is the XPath expression to be evaluated
    * @return the list of <code>Node</code> or <code>String</code> instances
    * depending on the XPath expression
    */
   public List selectNodes(String xpathExpression)
   {
      throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
   }
   
   /** <p><code>selectObject</code> evaluates an XPath expression and returns
    * the result as an {@link Object}. The object returned can
    * either be a {@link List} of one or more {@link Node} instances
    * or a scalar object like a {@link String} or a {@link Number}
    * instance depending on the XPath expression.
    *
    * @param xpathExpression is the XPath expression to be evaluated
    * @return the value of the XPath expression as a
    * {@link List} of {@link Node} instances, a {@link String} or
    * a {@link Number} instance depending on the XPath expression.
    */
   public Object selectObject(String xpathExpression)
   {
      throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
   }
   
   /** <p><code>selectNodes</code> evaluates an XPath expression then
    * sorts the results using a secondary XPath expression
    * Returns a sorted <code>List</code> of <code>Node</code> instances.</p>
    *
    * @param xpathExpression is the XPath expression to be evaluated
    * @param comparisonXPathExpression is the XPath expression used
    *     to compare the results by for sorting
    * @return the list of <code>Node</code> instances
    * sorted by the comparisonXPathExpression
    */
   public List selectNodes(
           String xpathExpression,
           String comparisonXPathExpression
           )
   {
      throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
   }
   
   /** <p><code>selectNodes</code> evaluates an XPath expression then
    * sorts the results using a secondary XPath expression
    * Returns a sorted <code>List</code> of <code>Node</code> instances.</p>
    *
    * @param xpathExpression is the XPath expression to be evaluated
    * @param comparisonXPathExpression is the XPath expression used
    *     to compare the results by for sorting
    * @param removeDuplicates if this parameter is true then duplicate
    *     values (using the comparisonXPathExpression) are removed from
    *     the result List.
    * @return the list of <code>Node</code> instances
    * sorted by the comparisonXPathExpression
    */
   public List selectNodes(
           String xpathExpression,
           String comparisonXPathExpression,
           boolean removeDuplicates
           )
   {
      throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
   }
   
   /** <p><code>selectSingleNode</code> evaluates an XPath expression
    * and returns the result as a single <code>Node</code> instance.</p>
    *
    * @param xpathExpression is the XPath expression to be evaluated
    * @return the <code>Node</code> matching the XPath expression
    */
   public Node selectSingleNode(String xpathExpression)
   {
      throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
   }
   
   /** <p><code>valueOf</code> evaluates an XPath expression
    * and returns the textual representation of the results the XPath
    * string-value of this node.
    * The string-value for a given node type is defined in the
    * <a href="http://www.w3.org/TR/xpath">XPath specification</a>.
    *
    * @param xpathExpression is the XPath expression to be evaluated
    * @return the string-value representation of the results of the XPath
    * expression
    */
   public String valueOf(String xpathExpression)
   {
      throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
   }
   
   /** <p><code>numberValueOf</code> evaluates an XPath expression
    * and returns the numeric value of the XPath expression if the XPath
    * expression results in a number, or null if the result is not a number.
    *
    * @param xpathExpression is the XPath expression to be evaluated
    * @return the numeric result of the XPath expression or null
    * if the result is not a number.
    */
   public Number numberValueOf(String xpathExpression)
   {
      throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
   }
   
   
   /** <p><code>matches</code> returns true if evaluating the given
    * XPath expression on this node returns a non-empty node set containing this node.</p>
    *
    * <p>This method does not behave like the &lt;xsl:if&gt; element - if you want
    * that behaviour, to evaluate if an XPath expression matches something, then
    * you can use the following code to be equivalent...
    * </p>
    * <code>if ( node.selectSingleNode( "/some/path" ) != nulll )</code>
    *
    * @param xpathExpression is an XPath expression
    * @return true if this node is returned by the given XPath expression
    */
   public boolean matches(String xpathExpression)
   {
      throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
   }
   
   /** <p><code>createXPath</code> creates an XPath object for
    * the given xpathExpression.
    * The XPath object allows the variable context to be specified.</p>
    *
    * @param xpathExpression is the XPath expression to be evaluated
    * @return an XPath object represeting the given expression
    * @throws InvalidXPathException if the XPath expression is invalid
    */
   public XPath createXPath(String xpathExpression) throws InvalidXPathException
   {
      throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
   }
   
   /** <p><code>asXPathResult</code> returns a version of this node which is
    * capable of being an XPath result.
    * The result of an XPath expression should always support the parent
    * relationship, whether the original XML tree was singly or doubly linked.
    * If the node does not support the parent relationship then a new node
    * will be created which is linked to its parent and returned.
    *
    * @return a <code>Node</code> which supports the parent relationship
    */
   public Node asXPathResult(Element parent)
   {
      throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
   }
   
   
   /** <p><code>accept</code> is the method used in the Visitor Pattern.</p>
    *
    * @param visitor is the visitor in the Visitor Pattern
    */
   public void accept(Visitor visitor)
   {
      throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
   }
   
   
   
   /** <p><code>clone</code> will return a deep clone or if this node is
    * read-only then clone will return the same instance.
    *
    * @@return a deep clone of myself or myself if I am read only.
    */
   public Object clone()
   {
      throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
   }
   
   protected Node convertNode(org.w3c.dom.Node obj)
   {
       Node retVal = null;
       
       if(obj instanceof DTMNodeProxy)
       {
           retVal = new W3CNodeProxy((DTMNodeProxy)obj);
       }
       else if(obj instanceof Element)
       {
           retVal = (Element)obj;
       }
       
       return retVal;
   }
}
