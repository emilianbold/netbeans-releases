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


package org.netbeans.modules.uml.core.metamodel.core.foundation;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import java.util.List;

import org.netbeans.modules.uml.core.support.umlsupport.URILocator;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * ElementCollector is a specialized class who focuses on the
 * collection of XML elements, the translation of those element
 * to there appropriate COM types, and then the collection of those
 * types.
 */

public class ElementCollector < Type >
{
   /**
    *
    * Retrieves the number of IDs in a particular
    * xml attribute value
    *
    * @param element[in] The element to query against
    * @param attrName[in] The attribute on that element
    * @param count[out] The number of ids
    *
    * @return HRESULT
    *
    */
   public int numIDRefs(Element elem, String attrName)
   {
      int count = 0;
      Document doc = elem.getDocument();
      if (doc != null)
      {
         String ids = elem.attributeValue(attrName);
         if (ids != null && ids.length() > 0)
         {
            // The attribute is an IDREFS, which is 
            // an XML attribute that is white-space delimited.
            String[] strs = ids.split(" ");
            if (strs != null)
            {
               count = strs.length;
            }
         }
      }
      return count;
   }

   /**
     *
     * Makes sure that any nodes returning from the query that reference
     * external nodes are resolved. This wraps the DOM selectNodes() call.
     *
     * @param node[in] the node we are querying on. Needs to support the
     *					selectNodes() method.
     * @param query[in] the query to perform
     * @param nodeList[out] the resultant list.
     *
     * @return HRESULTs
     *
     */
   public ETList< Node > selectNodes(Node node, String query)
   {
      ETList< Node > list = null;

      // Make sure the node we are performing a query on has also
      // been resolved
      list = UMLXMLManip.selectNodes(node, query);

//      boolean modified = false;
//      modified = ExternalFileManager.resolveExternalNodes(list);
//
//      if (modified)
//      {
//         list = XMLManip.selectNodeList(node, query);
//      }
      return list;
   }

   /**
     * 
     * Retrieves the element identified via xmi.id in the XML attribute
     * passed in.
     *
     * @param baseElement[in] The element to search. This is the element
     *							 whose indicated XML attribute has the value
     *							 who contains the xmi.id of the element we need.
     * @param attrName[in] The XML attribute to retrieve the xmi.id of
     *							  the element we need.
     * @foundElement[out] The found element, else 0.
     *
     * @return HRESULTs
     *
     */
   public Type retrieveSingleElementWithAttrID(BaseElement element,
                                               String attrName, Class c)
   {
      Type retEle = null;
      Node node = element.getNode();
      if (node != null && node.getNodeType() == Node.ELEMENT_NODE)
      {
         org.dom4j.Element elem = (org.dom4j.Element)node;
         retEle = retrieveSingleElementWithAttrID(elem, attrName, c);
      }
      return retEle;
   }

   /**
     * 
     * Retrieves the element identified via xmi.id in the XML attribute
     * passed in.
     *
     * @param element[in] The element to search. This is the element
     *							 whose indicated XML attribute has the value
     *							 who contains the xmi.id of the element we need.
     * @param attrName[in] The XML attribute to retrieve the xmi.id of
     *							  the element we need.
     * @foundElement[out] The found element, else 0.
     *
     * @return HRESULTs
     *
     */
   public Type retrieveSingleElementWithAttrID(Element elem,
                                               String  attrName, Class c)
   {
      Type retEle = null;
      Document doc = elem.getDocument();
      if (doc != null)
      {
         String id = elem.attributeValue(attrName);
         if (id != null && id.length() > 0)
         {
            Object obj = UMLXMLManip.findAndFill(doc, id);
			if (obj != null)
			{
			   try
			   {
			   	 if (obj != null && c.isAssignableFrom(obj.getClass()))
			   	 {
					retEle = (Type)obj;
			   	 }
			   }
			   catch(ClassCastException e)
			   {
				  retEle = null;
			   }
			}
         }
      }
      return retEle;
   }

   public Type retrieveSingleElement(BaseElement element,
                                     String      query, Class c)
   {
      Type retVal = null;
      
      if((element != null) && (element.getNode() != null))
      {
            Node node = element.getNode();
            retVal = retrieveSingleElement(node, query, c);
      }
      
      return retVal;
   }
   
   /**
    *
    * Retrieves a single child element of node.
    *
    * @param node[in] The node to search child elements from.
    * @param query[in] The query string to use. selectSingleNode()
    *						  is used internally, so use appropriately.
    * @param foundElement[out] The found element, else 0.
    *
    * @return HRESULTs
    *
    */
   public Type retrieveSingleElement(Node node, String query, Class c)
   {
      Type retEle = null;
      try
      {
         org.dom4j.Node n = UMLXMLManip.selectSingleNode(node, query);
         if (n != null)
         {
            FactoryRetriever ret = FactoryRetriever.instance();
            String name = XMLManip.retrieveSimpleName(n);
            Object obj = ret.createTypeAndFill(name, n);
            
            try
            {
            	if (obj != null && c.isAssignableFrom(obj.getClass()))
            	{
					retEle = (Type)obj;
            	}
            }
            catch(ClassCastException e)
            {
            }
         }
      }
      catch (Exception e)
      {
      }
      return retEle;
   }

   /**
    * @param constraint
    * @param string
    * @return
    */
   public ETList<Type> retrieveElementCollectionWithAttrIDs(BaseElement element,
                                                      String attrName, Class c)
   {
      ETList<Type> elems = null;
      Node node = element.getNode();
      if (node != null && node.getNodeType() == Node.ELEMENT_NODE)
      {
         org.dom4j.Element domEle = (Element)node;
         elems = retrieveElementCollectionWithAttrIDs(domEle, attrName, c);
      }
      return elems != null? elems : new ETArrayList<Type>();
   }

   /**
    *
    * Retrieves a collection of elements based on the value of that passed in
    * attribute. It is assumed that the attribute is defined as containing an
    * IDREFS value.
    *
    * @param element[in] the element whose XML attribute we are querying.
    * @param attrName[in] the name of the attribute
    * @param collClass[in] CLSID of the collection object
    * @param dummy[in] used just for type resolution.
    * @param collection[out] the actual collection of elements.
    *
    * @return HRESULTs
    *
    */
   public ETList<Type> retrieveElementCollectionWithAttrIDs(org.dom4j.Element domEle,
                                                       String attrName, Class c)
   {
      ETList<Type> elems = new ETArrayList<Type>(0);
      Document doc = domEle != null ? domEle.getDocument() : null;
      if (doc != null)
      {
         String ids = domEle.attributeValue(attrName);
         if (ids != null && ids.length() > 0)
         {
            FactoryRetriever fact = FactoryRetriever.instance();

            // The attribute is an IDREFS, which is 
            // an XML attribute that is white-space delimited.
            String[] strs = ids.split(" ");
            if (strs != null && strs.length > 0)
            {
               int count = strs.length;
               // All ids that have been added are encoded to make sure that
               // white spaces between ids are maintained, even if an id is
               // a complicated URI that contains spaces
               for (int i = 0; i < count; i++)
               {
                  String str = strs[i];
                  String decoded = URILocator.decodeURI(str);
                  Object obj = UMLXMLManip.findAndFill(doc, decoded);
                  try
                  {
                      if (obj != null && c.isAssignableFrom(obj.getClass()))
                      {
						elems.add((Type)obj);
                      }
                  }
                  catch(ClassCastException e)
                  {
                  }
               }
            }
         }
      }
      return elems;
   }

    /**
     * Retrieves all the specified nodes / children of the passed in node
     * element. Given a Class, ensures all elements in the collection are
     * instances of that class.
     * 
     * @param element[in] The BaseElement to pull the IXMLDOMNode from.
     *
     * @see RetrieveElementCollection()
     */
    public ETList<Type> retrieveElementCollection(IBaseElement element,
            String nodeName, Class c)
    {
        ETList<Type> retVal = null;
      
        if(element != null)
        {
            Node node = element.getNode();
            if(node != null)
                retVal = retrieveElementCollection(node, nodeName, c);
        }
        return retVal;
   }
   
   /**
    *
    * Retrieves all the specified nodes / children of the passed in node
    * element.
    *
    * @param node[in] the actual XML node that we are querying. It is the
    *                   children of this node that we are gathering.
    * @param nodeName[in] actual names of the elements that will make up the
    *                   collection. This should be a simple XPath expression.
    * @param collection[out] the actual collection object that will house the
    *                   individual element.s
    *
    * @return HRESULTs
    * @warning See NamedElementImpl::get_TaggedValue() for an example of how to use
    *    this method.
    *
    */
   public ETList< Type > retrieveElementCollection(Node node, 
                                                    String nodeName,
                                                    Class c)
   {
      ETList< Type > elems = null;
      if (node != null)
      {
         //List list = XMLManip.selectNodeList(node, nodeName);
         List list = selectNodes(node, nodeName);
         if (list != null)
         {
            elems = populateCollection(list, c);
         }
      }
      return elems != null? elems : new ETArrayList<Type>();
   }
   
   /**
     *
     * Populates the passed in collection object with the COM wrappers
     * of the elements found in tagList.
     *
     * @param tagList[in] the XML list of elements to wrap in COM objects and
     *                push onto the collection to return.
     * @param collClass[in] the CLSID of the collection class to create and
     *								return
     * @param pVal[out] the populated collection object.
     *
     * @return HRESULTs
     *
     */
   public ETList<Type> populateCollection(List list, Class c)
   {
      ETList<Type> elems = null;
      if (list.size() > 0)
      {
         FactoryRetriever ret = FactoryRetriever.instance();
         int count = list.size();

         elems = new ETArrayList<Type>(count);
         for (int i = 0; i < count; i++)
         {
            if(list.get(i) instanceof Node)
            {
               Node tag = (Node)list.get(i);
               String name = XMLManip.retrieveSimpleName(tag);
               Object obj = ret.createTypeAndFill(name, tag);
               if (obj != null && (c == null || c.isAssignableFrom(obj.getClass())))
               {
                   elems.add((Type)obj);
               }
            }
         }
      }
      return elems;
   }
}
