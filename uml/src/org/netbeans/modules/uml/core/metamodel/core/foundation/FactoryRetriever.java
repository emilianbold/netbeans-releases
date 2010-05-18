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

import java.lang.ref.WeakReference;
import java.util.Hashtable;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class FactoryRetriever
{
    public static final String IS_CLONED_ATTR = "isCloned";
   private FactoryRetriever()
   {

   }
   private static FactoryRetriever m_Retriever;
   private ICreationFactory m_Factory;

   // xmi.id, true for cloned
   //typedef std::map< CComBSTR, bool > ClonedIDMap;

   // xmi.id, actual COM object
   //typedef std::map< CComBSTR, IVersionableElement* > MemoryObjectMap;

   private Hashtable < String, WeakReference < IVersionableElement > > m_InMemoryObjects = new Hashtable < String, WeakReference < IVersionableElement > > ();
   private Hashtable < String, String > m_ClonedElements = new Hashtable < String, String > ();
   private Iterator m_MapIterator = null;

   protected void initialize()
   {
      ICoreProduct product = ProductRetriever.retrieveProduct();
      if (product != null)
      {
         m_Factory = product.getCreationFactory();
      }
   }

   public ICreationFactory getCreationFactory()
   {
      return m_Factory;
   }

   public static FactoryRetriever instance()
   {
      if (m_Retriever == null)
      {
         m_Retriever = new FactoryRetriever();
         m_Retriever.initialize();
      }
      return m_Retriever;
   }

   ///////////////////////////////////////////////////////////////////////////////
   //
   // HRESULT FactoryRetriever::CreateType( const xstring &typeName, IUnknown **newElement, IUnknown *outer)
   //
   // Creates the passed in meta type by passing it through to the internal
   // ICreationFactory.
   //
   // INPUT:
   //    typeName - the type to retrieve. For example, "Class" will retrieve the
   //               IUnknown of an implementation supporting the IClass interface.
   //    outer    - the controlling outer unknown. Used when aggregating. Can be 0.
   //
   // OUTPUT:
   //    result   - the IUnknown interface if all goes well.
   //
   // RETURN:
   //    HRESULTs
   //
   // CAVEAT:
   //    None.
   //
   ///////////////////////////////////////////////////////////////////////////////
   public Object createType(String name, Object outer)
   {
      Object retObj = null;
      if (m_Factory != null)
      {
         retObj = m_Factory.retrieveMetaType(name, outer);
      }
      return retObj;
   }

   ///////////////////////////////////////////////////////////////////////////////
   //
   // HRESULT FactoryRetriever::FindElementByID( IXMLDOMDocument* doc,
   //                                            const xstring& idToFind,
   //                                            IUnknown** element )
   //
   //
   //
   // INPUT:
   //    doc      -  the document to look in.
   //    idToFind -  the xmi.id value to look for
   //
   // OUTPUT:
   //    element  -  the found element if all is well.
   //
   // RETURN:
   //    HRESULTs
   //
   // CAVEAT:
   //    None.
   //
   ///////////////////////////////////////////////////////////////////////////////
   public Object findElementById(Document doc, String str)
   {
      Object retObj = null;
      Node foundNode = UMLXMLManip.findElementByID(doc, str);
      if (foundNode != null)
      {
         retObj = createTypeAndFill(foundNode.getName(), foundNode);
      }
      return retObj;
   }
   
   /**
    * Retrieves a model element by it's XMI ID.
    *
    * @param context A model element that can be used to specify the context to 
    *                search.
    * @param id The XMI ID to search.
    */
   public IElement findElementByID(IElement context, String id)
   {
       IElement retVal = null;
       
       if(context != null)
       {
            Document doc = context.getNode().getDocument();
            Object retObj = findElementById(doc, id);
            if(retObj instanceof IElement)
            {
                retVal = (IElement)retObj;
            }
       }
       
       return retVal;
   }

   /**
    *
    * Creates the appropriate COM wrapper object and sets the XML node of that
    * element, without calling PrepareNode on the COM wrapper. This has the effect
    * of simply creating the COM wrapper and setting the node on it, bypassing
    * all the initialization normally done when calling CreateType().
    *
    * @param typeName[in] the name of the type to create.
    * @param node[in] the actual XML node to place on the new COM object
    * @param element[out] The COM wrapper for the passed in node.
    *
    * @return HRESULTs
    *
    */
   //  public Object createTypeAndFill(String typeName, Node pNode)
   //  {
   //    Object retObj = null;
   //    String xmiId = UMLXMLManip.getAttributeValue(pNode, "xmi.id");
   //    if (xmiId.length() > 0)
   //    {
   //      Object obj = m_InMemoryObjects.get(xmiId);
   //      if (obj == null)
   //      {
   //        retObj = fillElement(typeName, pNode);
   //        if (retObj != null && retObj instanceof IVersionableElement)
   //        {
   //          IVersionableElement vEle = (IVersionableElement)retObj;
   //          m_InMemoryObjects.put(xmiId, vEle);
   //        }
   //      }
   //      else
   //      {
   //        if (!isCloned(pNode))
   //        {
   //          // Be sure to pack the existing COM object with the passed in node. This prevents
   //          // issues where the node was cloned, but the COM object retrieved contains
   //          // the node before it was cloned. This caused numerous problems for roundtrip
   //          boolean retry = false;
   //          try {
   //            ((IVersionableElement)obj).setNode(pNode);
   //          }catch (Exception e)
   //          {
   //            retry = true;
   //          }
   //
   //          if (retry)
   //          {
   //            m_InMemoryObjects.remove(xmiId);
   //            retObj = createTypeAndFill(typeName, pNode);
   //          }
   //        }
   //        else
   //        {
   //          // If the node has been cloned, don't use objects on the in-memory list. This
   //          // prevents nodes from being swapped in and out behind the COM objects. This caused
   //          // problems initially in RoundTrip, and then in Code Generation scripts
   //          retObj = fillElement(typeName, pNode);
   //        }
   //      }
   //    }
   //    return retObj;
   //  }

   public Object createTypeAndFill(String typeName, org.dom4j.Node pNode)
   {
      Object retObj = null;
      String xmiId = XMLManip.getAttributeValue(pNode, "xmi.id");
      if (xmiId != null && xmiId.length() > 0)
      {
         WeakReference obj = m_InMemoryObjects.get(xmiId);
         if (obj == null)
         {
            retObj = fillElement(typeName, pNode);
            if (retObj != null && retObj instanceof IVersionableElement)
            {
               IVersionableElement vEle = (IVersionableElement)retObj;
               WeakReference reference = new WeakReference(vEle);
               m_InMemoryObjects.put(xmiId, reference);
            }
         }
         else
         {
            IVersionableElement vEle = (IVersionableElement)obj.get();

//            if (isCloned(pNode))
//            {
//               ETSystem.out.println("Node: " + xmiId + " is cloned.");
//            }

            if (!isCloned(pNode))
            {
               //// Be sure to pack the existing COM object with the passed in node. This prevents
               //// issues where the node was cloned, but the COM object retrieved contains
               //// the node before it was cloned. This caused numerous problems for roundtrip
               boolean retry = false;
               try
               {
                  vEle.setDom4JNode(pNode);
                  retObj = vEle;
               }
               catch (Exception e)
               {
                  retry = true;
               }

               if (retry)
               {
                  m_InMemoryObjects.remove(xmiId);
                  retObj = createTypeAndFill(typeName, pNode);
               }
            }
            else
            {
               // If the node has been cloned, don't use objects on the in-memory list. This
               // prevents nodes from being swapped in and out behind the COM objects. This caused
               // problems initially in RoundTrip, and then in Code Generation scripts
               retObj = fillElement(typeName, pNode);
               if(retObj instanceof IVersionableElement)
               {
                  ((IVersionableElement)retObj).setIsClone(true);
               }
            }
         }
      }

      return retObj;
   }

   //	HRESULT FactoryRetriever::CreateTypeAndFill( const xstring& typeName, IXMLDOMNode* node, IUnknown** element )
   //	{
   //		HR_PARM_CHECK( node && element );
   //		HRESULT hr = S_OK;
   //
   //		if( m_Factory )
   //		{
   //			try
   //			{
   //				// Look up the ID of the node that needs to be placed in our in-memory objects map
   //
   //				XMLManip manip;
   //
   //				CComBSTR xmiID;
   //				_VH( manip.GetAttributeValue( node, _T( "xmi.id" ), &xmiID ));
   //
   //				if( xmiID.Length() )
   //				{
   //					MemoryObjectMap::iterator iter = m_InMemoryObjects.find( xmiID );
   //
   //					if( iter == m_InMemoryObjects.end() )
   //					{
   //						hr = FillElement( typeName, node, element );
   //
   //						CComQIPtr< IVersionableElement > ver( *element );
   //						if( ver )
   //						{
   //							AddObject(ver);
   //						}
   //					}
   //					else
   //					{
   //						if( iter->second )
   //						{
   //							if( !IsCloned( node ))
   //							{
   //								// Be sure to pack the existing COM object with the passed in node. This prevents
   //								// issues where the node was cloned, but the COM object retrieved contains
   //								// the node before it was cloned. This caused numerous problems for roundtrip
   //
   //								bool retry = false;
   //
   //								try
   //								{
   //									_VH( iter->second->put_Node( node ));
   //									_VH( iter->second->QueryInterface( __uuidof( IUnknown ), ( void** ) element ));
   //								}
   //								catch( ... )
   //								{
   //									// This can potentially fail when referencing objects outside
   //									// this process. Catch the error, remove the id, try again
   //						
   //									retry = true;
   //								
   //								}
   //
   //								if( retry )
   //								{
   //									RemoveObject( xmiID );
   //									hr = CreateTypeAndFill( typeName, node, element );
   //								}
   //							}
   //							else
   //							{
   //								// If the node has been cloned, don't use objects on the in-memory list. This 
   //								// prevents nodes from being swapped in and out behind the COM objects. This caused
   //								// problems initially in RoundTrip, and then in Code Generation scripts
   //
   //								hr = FillElement( typeName, node, element );
   //							}
   //						}
   //					}
   //				}
   //
   //         
   //			}
   //			catch( _com_error& err )
   //			{
   //				hr = COMErrorManager::ReportError( err );
   //         
   //			}
   //		}
   //		return hr;
   //	}

   /**
    *
    * Puts the ID of the passed in node on the cloned list,
    * thereby preventing the element from being established on
    * the in memory list. Results in the removal of the COM
    * object off the in memory list.
    *
    * @param node[in] The node that has been cloned
    *
    * @return HRESULT
    *
    */
   public void markAsCloned(Node pNode)
   {
      String xmiId = UMLXMLManip.getAttributeValue(pNode, "xmi.id");
      if (xmiId.length() > 0)
      {
         m_ClonedElements.put(xmiId, "true");
         m_InMemoryObjects.remove(xmiId);
      }
   }

   /**
    *
    * Determines whether or not the passed in node has been previously cloned
    *
    * @param node[in] The node to check
    *
    * @return HRESULT
    *
    */
   //  public boolean isCloned(Node pNode)
   //  {
   //    boolean cloned = false;
   //    String xmiId = UMLXMLManip.getAttributeValue(pNode, "xmi.id");
   //    if (xmiId.length() > 0 && xmiId.equalsIgnoreCase("true"))
   //    {
   //      cloned = true;
   //    }
   //    return cloned;
   //  }

   public boolean isCloned(org.dom4j.Node pNode)
   {
      boolean cloned = false;
      if (pNode != null)
      {
         String xmiId = XMLManip.getAttributeValue(pNode, "xmi.id");

         cloned = isCloned(xmiId);

//          // Does not correspond to C++         
//          if (xmiId != null && xmiId.length() > 0)
//          {
//             cloned = isCloned(xmiId) || isCloned(pNode.getParent());
//          }
//          else
//          {
//             cloned = isCloned(pNode.getParent());
//          }
         
         if(pNode instanceof org.dom4j.Element)
         {
             org.dom4j.Element element = (org.dom4j.Element)pNode;
             while(element != null)
             {
                 String value = element.attributeValue(IS_CLONED_ATTR);
                 if((value != null) && (value.equals("true") == true))
                 {
                     cloned = true;
                     break;
                 }
                 else
                 {
                    element = element.getParent();
                 }
             }
         }

      }
      return cloned;
   }

   /**
    *
    * Determines whether or not the passed in XMI ID refers to an object that has been previously cloned
    *
    * @param xmiID[in] The id to check
    *
    * @return HRESULT
    *
    */
   public boolean isCloned(String xmiid)
   {
      boolean cloned = false;
      if (xmiid != null && xmiid.length() > 0)
      {
         if (m_ClonedElements.get(xmiid) != null)
         {
            cloned = true;
         }
      }
      return cloned;
   }

   public boolean isCloned(IVersionableElement element)
   {
      boolean isCloned = false;
      if (element != null)
      {
         Node node = element.getNode();
         isCloned = isCloned(node);
      }
      return isCloned;
   }

   /**
    *
    * Erases the ID of the passed in element from the list of elements that
    * have been cloned
    *
    * @param element[in]   The element to remove from the cloned list
    *
    * @return HRESULT
    *
    */
   public void clearClonedStatus(IVersionableElement element)
   {
      Node node = element.getNode();
      clearClonedStatus(node);
   }

   /**
    *
    * Erases the ID of the passed in element from the list of elements that
    * have been cloned
    *
    * @param node[in]   The element to remove from the cloned list
    *
    * @return HRESULT
    *
    */
   public void clearClonedStatus(Node node)
   {
      String xmiid = XMLManip.getAttributeValue(node, "xmi.id");
      if (xmiid.length() > 0)
      {
         Object obj = m_ClonedElements.get(xmiid);
         if (obj != null)
         {
            m_ClonedElements.remove(xmiid);
         }
      }
   }

   /**
    *
    * Creates an empty COM wrapper, then sets the node on it.
    *
    * @param typeName[in] The type to create.
    * @param node[in] The node to set on the wrapper
    * @param element[out] The created element
    *
    * @return HRESULT
    * @warning This does not place the returned COM interface on the in-memory list!
    *
    */
   //  private Object fillElement(String typeName, Node pNode)
   //  {
   //    Object retObj = null;
   //    retObj = m_Factory.retrieveEmptyMetaType(typeName, null);
   //    if (retObj != null && retObj instanceof IVersionableElement)
   //    {
   //      ((IVersionableElement)retObj).setNode(pNode);
   //    }
   //    return retObj;
   //  }

   private Object fillElement(String typeName, org.dom4j.Node pNode)
   {
      Object retObj = null;
      retObj = m_Factory.retrieveEmptyMetaType(typeName, null);
      if (retObj != null && retObj instanceof IVersionableElement)
      {
         ((IVersionableElement)retObj).setDom4JNode(pNode);
      }
      return retObj;
   }

   /**
    *
    * Retrieves an element off the in memory object list by id.
    *
    * @param xmiID[in]     The id of the object to retrieve
    * @param element[out]  The found object, else 0 if not on list
    *
    * @return HRESULT
    *
    */
   public IVersionableElement retrieveObject(String xmiID)
   {
      IVersionableElement retEle = null;
      WeakReference reference = m_InMemoryObjects.get(xmiID);
      if (reference != null)
      {
         retEle = (IVersionableElement)reference.get();
      }
      return retEle;
   }

   /**
   *
   * Clones the passed in node, then creates a new wrapper COM object. 
   *
   * @param node[in] The node to clone
   * @param element[out] The new COM wrapper for the cloned element
   *
   * @return HRESULT
   * @warning The created COM object does NOT go onto the in-memory list of objects. This is because
   *          it would cause havic on the mechanism, as you would now have two completely differnt COM
   *          objects wrapping a XML element with the same XMI ID. Even though the IDs are the same, 
   *          the element are completely different ( i.e., two distinct instances ).
   */
   public Object clone(Node node)
   {
      Object retVer = null;
      Node clone = (Node)node.clone();
      if (node.getDocument() != null)
         clone.setParent(node.getParent());
      if (clone != null)
      {
         markAsCloned(node);
         if(clone instanceof org.dom4j.Element)
         {
             org.dom4j.Element cloneElement = (org.dom4j.Element)clone;
             cloneElement.addAttribute(IS_CLONED_ATTR, "true");
         }
         
         String name = XMLManip.retrieveSimpleName(clone);
         retVer = fillElement(name, clone);
      }
      return retVer;
   }

   /**
    * @param xmiid
    */
   public void removeObject(String xmiid)
   {
      m_InMemoryObjects.remove(xmiid);
   }

   /**
   *
   * Adds the passed in object to the in memory list of elemnets.
   *
   * @param obj[in] The element to add.
   *
   * @return HRESULT
   * @note If obj has been cloned, it will not be added to the 
   *       in memory list.
   */
   public void addObject(IVersionableElement element)
   {
      String id = element.getXMIID();
      if (id.length() > 0)
      {
         if (!isCloned(id))
         {
            WeakReference reference = new WeakReference(element);
            m_InMemoryObjects.put(id, reference);
         }
      }
   }

   /**
    * Starts the iteration
    *
    * Used to iterator over the in memory objects since our STL doesn't handle
    * cross dll boundaries very well.
    */
   public void startMapIteration()
   {
      m_MapIterator = m_InMemoryObjects.values().iterator();
   }

   /**
    * Iterates to the next element.
    *
    * @return true if we haven't reached the end yet
    */
   public IVersionableElement getNext(String sXMIID)
   {
      IVersionableElement retObj = null;
      Object obj = m_InMemoryObjects.get(sXMIID);
      if (obj instanceof IVersionableElement)
      {
         retObj = (IVersionableElement)obj;
      }
      return retObj;
   }
}
