/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IVisibilityKind;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author sumitabhk
 *
 */
public class BaseElement extends VersionableElement
{

	//ordering Kind
	public static final int OK_UNORDERED	= 0;
	public static final int OK_ORDERED	= 1;

	//IterationActivityGroupKind
	public static final int IAG_TEST_AT_BEGIN	= 0;
	public static final int IAG_TEST_AT_END	= 1;

	//MessageKind
	public static final int MK_UNKNOWN	= -1;
	public static final int MK_CREATE	= 0;
	public static final int MK_SYNCHRONOUS	= 1;
	public static final int MK_ASYNCHRONOUS	= 2;
	public static final int MK_RESULT	= 3;

	//ObjectFlowEffectKind
	public static final int OFE_CREATE	= 0;
	public static final int OFE_READ	= 1;
	public static final int OFE_UPDATE	= 2;
	public static final int OFE_DELETE	= 3;

	//ActivityGroupKind
	public static final int AGK_ITERATION	= 0;
	public static final int AGK_STRUCTURED	= AGK_ITERATION + 1;
	public static final int AGK_INTERRUPTIBLE	= AGK_STRUCTURED + 1;

	//ParameterSemanticsKind
	public static final int PK_OPTIONAL	= 0;
	public static final int PK_BYVALUE	= PK_OPTIONAL + 1;
	public static final int PK_BYREF	= PK_BYVALUE + 1;
	public static final int PK_ADDRESSOF	= PK_BYREF + 1;

	//AggregationKind
	public static final int 	AK_NONE	= 0;
	public static final int AK_AGGREGATE	= AK_NONE + 1;
	public static final int AK_COMPOSITE	= AK_AGGREGATE + 1;

	//ScopeKind
	public static final int SK_INSTANCE	= 0;
	public static final int SK_CLASSIFIER	= SK_INSTANCE + 1;

	//ParameterDirectionKind
	public static final int PDK_IN	= 0;
	public static final int PDK_INOUT	= PDK_IN + 1;
	public static final int PDK_OUT	= PDK_INOUT + 1;
	public static final int PDK_RESULT	= PDK_OUT + 1;

	//ChangeableKind
	public static final int CK_UNRESTRICTED	= 0;
	public static final int CK_RESTRICTED	= CK_UNRESTRICTED + 1;
	public static final int CK_ADDONLY	= CK_RESTRICTED + 1;
	public static final int CK_REMOVEONLY	= CK_ADDONLY + 1;

	//InstantiationKind
	public static final int IK_DIRECT	= 0;
	public static final int IK_INDIRECT	= IK_DIRECT + 1;
	public static final int IK_NONE	= IK_INDIRECT + 1;

	//PartKind
	public static final int PK_FOCUS	= 0;
	public static final int PK_AUXILIARY	= PK_FOCUS + 1;
	public static final int PK_INTERFACEIMPLEMENTATION	= PK_AUXILIARY + 1;

	//ObjectNodeOrderingKind
	public static final int OOK_UNORDERED	= 0;
	public static final int OOK_ORDERED	= OOK_UNORDERED + 1;
	public static final int OOK_LIFO	= OOK_ORDERED + 1;
	public static final int OOK_FIFO	= OOK_LIFO + 1;

	//StateMachineKind
	public static final int SMK_BEHAVIOR	= 0;
	public static final int SMK_PROTOCOL	= SMK_BEHAVIOR + 1;

	//InteractionOperator
	public static final int IO_ALT	= 0;
	public static final int IO_ELSE	= IO_ALT + 1;
	public static final int IO_OPT	= IO_ELSE + 1;
	public static final int IO_PAR	= IO_OPT + 1;
	public static final int IO_LOOP	= IO_PAR + 1;
	public static final int IO_REGION	= IO_LOOP + 1;
	public static final int IO_NEG	= IO_REGION + 1;
	public static final int IO_ASSERT	= IO_NEG + 1;
	public static final int IO_SEQ	= IO_ASSERT + 1;
	public static final int IO_STRICT	= IO_SEQ + 1;
	public static final int IO_FILTER	= IO_STRICT + 1;

	//PseudostateKind
	public static final int PK_CHOICE	= 0;
	public static final int PK_DEEPHISTORY	= PK_CHOICE + 1;
	public static final int PK_FORK	= PK_DEEPHISTORY + 1;
	public static final int PK_INITIAL	= PK_FORK + 1;
	public static final int PK_JOIN	= PK_INITIAL + 1;
	public static final int PK_JUNCTION	= PK_JOIN + 1;
	public static final int PK_SHALLOWHISTORY	= PK_JUNCTION + 1;
	public static final int PK_ENTRYPOINT	= PK_SHALLOWHISTORY + 1;
	public static final int PK_STOP	= PK_ENTRYPOINT + 1;

	//CallConcurrencyKind
	public static final int CCK_SEQUENTIAL	= 0;
    public static final int CCK_GUARDED	= CCK_SEQUENTIAL + 1;
    public static final int CCK_CONCURRENT	= CCK_GUARDED + 1;

	//ActivityKind
	public static final int AK_STRUCTURED	= 0;
	public static final int AK_FLOWCHART	= AK_STRUCTURED + 1;

	/**
	 * 
	 */
	public BaseElement() {
		super();
	}
	
	/**
	 * 
	 * Makes sure that the node with the passed in name is present
	 * under this elements node. If it isn't, one is created.
	 * 
	 * @param name[in]  name of the node to check for existence for. 
	 * @param query[in] the query string to used to check for existence
	 * @param node[out] the node representing the element
	 * @return HRESULTs
	 * 
	*/
	public Node ensureElementExists(String name, String query)
	{
		return UMLXMLManip.ensureElementExists(m_Node, name, query);
	}
	
	public boolean getBooleanAttributeValue(String str, boolean def /*=true*/)
	{
		boolean retVal = def;
		String val = getAttributeValue(str);
		if (val != null && val.length() > 0)
		{
			if (val.equalsIgnoreCase("false"))
				retVal = false;
			else 
				retVal = true;
		}
		return retVal;
	}
	
	public void setBooleanAttributeValue(String str, boolean val)
	{
		String newVal = "false";
		if (val)
			newVal = "true";
			
		setAttributeValue(str, newVal);
	}
	
	public String getAttributeValue(String str)
	{
		String retVal = "";
		org.dom4j.Element elem = getElementNode();
		if (elem != null)
		{
		      retVal = XMLManip.getAttributeValue(elem, str);
		}
		return retVal;
	}
	
	public int getAttributeValueInt(String str)
	{
		int retVal = 0;
		org.dom4j.Element elem = getElementNode();
		if (elem != null)
		{
			String value = XMLManip.getAttributeValue(elem, str);
			if (value != null)
			{
				retVal = Integer.parseInt(value);
			}
		}
		return retVal;
	}
	
	public void setAttributeValue(String str, String val)
	{
      IElementChangeDispatchHelper helper = new ElementChangeDispatchHelper();
      if(helper.dispatchElementPreModified(this) == true)
      {
         UMLXMLManip.setAttributeValue(this, str, val);
         // comment out redundant event firing
//         helper.dispatchElementModified(this);
      }
	}
	
	/**
	 * Sets the XML attribute that has the passed in name to the passed in value.
	 *
	 * @param attrName[in] the name of the XML attribute to set.
	 * @param value[in] the actual value to set.

	 * @return HRESULTs
	 *
	*/
	public void setAttributeValue(String name, int val)
	{
		UMLXMLManip.setAttributeValue(this, name, val);
	}
	
	/**
	 *
	 * Translates the passed in string value into an VisibilityKind
	 * enum value.
	 *
	 * @param attrName[in]	The string to translate
	 * @param kind[out]		The enum value			
	 *
	 * @return 
	 */
	public int getVisibilityKindValue(String attrName)
	{
		int kind = IVisibilityKind.VK_PUBLIC;
		String value = getAttributeValue(attrName);
		if (value != null && value.length() > 0)
		{
			if( value.equals( "protected" ))
			{
			   kind = IVisibilityKind.VK_PROTECTED;
			}
			else if( value.equals("private" ))
			{
			   kind = IVisibilityKind.VK_PRIVATE;
			}
			  else if( value.equals( "package" ))
			{
			   kind = IVisibilityKind.VK_PACKAGE;
			}
		}
		return kind;
	}

	/**
	 * Sets the specified attribute on this element to the passed in enum value
	 *
	 * @param attrName[in]	The XML attribute value to set
	 * @param kind[in]		The enum value
	 *
	 * @return HRESULTs
	 */
	public void setVisibilityKindValue(String attrName, int kind)
	{
		String value = "public";
		if (kind == IVisibilityKind.VK_PACKAGE)
		{
			value = "package";
		}
		else if (kind == IVisibilityKind.VK_PRIVATE)
		{
			value = "private";
		}
		else if (kind == IVisibilityKind.VK_PROTECTED)
		{
			value = "protected";
		}
		setAttributeValue(attrName, value);
	}

	/**
	 *
	 * Adds newElement to this element, establishing the back pointer
	 * connection between the two types.
	 *
	 * @param byID[in] - true to add the new element using that
	 *                   element's id else
	 *                 - false to add the element as a child node.
	 * @param featureName[in] The location where the new element will
	 *                          be placed. This should be an XPath string
	 *                          indicating the XML element to add the
	 *                          new element to. If byID is true, this
	 *                          parameter is ignored.
	 * @param query[in] The XPath query to use to determine if
	 *                          the element to be added already exists
	 *                          on curElement. If byID is set to true,
	 *                          this will be the name of the XML attribute
	 *                          to check for ID values in.
	 * @param value[in] The new element to add
	 * @param ConnectToMember[in] The method to call on value in order to 
	 *                            establish a backpointer to this element.
	 *
	 * @return HRESULTs
	 */
   public < NewType extends IVersionableElement, Owner extends IBaseElement> void addChildAndConnect(
                                boolean byID, 
                                String featureName,
	                            String query, 
                                NewType newType,
         					    IBackPointer<Owner> backPointer)
    {
        ElementConnector<Owner> connector = new ElementConnector<Owner>();
        connector.addChildAndConnect((Owner)this, byID, featureName, query, newType, backPointer);
    }
	
   /**
    * 
    * This method should be called when the element to remove is
    * referenced by ID by the current element's XML node.
    * This should not be called if the element to remove is a fully
    * contained child.
    *
    * This method also assumes that the function to call back into
    * via the type referenced by the passed in ID is also by ID. For
    * instance, if removing an element from the current element is
    * done by passing in a BSTR for the id of the the element to remove,
    * then doing the same thing against the other element, thereby
    * managing the backpointers.
    *
    * This method will find the referenced element via id, and then
    * disconnect this element from it.
    *
    * @param id[in] The id of the element to remove
    * @param presenceQuery[in] An XPath query to perform to check
    *                          to see if elToRemove is a child
    *                          of this element.
    * @param remove[in] The functor to fire in order to remove
    *                   the current element from elToRemove.
    *
    * @return HRESULTs
    * @see RemoveElement()
    */
   //public < RemoveType, CurType > void remvoeByID(String id, 
//   public <CurType> void removeByID(
//           String id,
//           String presenceQuery,
//           CurType unused,
//           Object newVal)
//   {
//       
//   }
	
	/**
	 * This is exactly the same as RemoveByID() that takes an ID, except
	 * that the first parameter is an actual element. The element will be queried
	 * for its XMI ID, and then the BSTR version of the RemoveByID() will be 
	 * called
	 *
	 * @param elementToRemove[in] The element to remove. 
	 * @param presenceQuery[in] This is usually just the name of the attribute that
	 *                          contains the id to remove
	 * @param dummy
	 * @param RemoveMember[in] The functor to fire in order to remove
	 *                   the current element from elementToRemove.
	 *
	 * @result HRESULT
	 * @see RemoveByID()
	 */
	//public < RemoveType, CurType > void removeByID(IVersionableElement elementToRemove, 

     public void removeByID(
             IVersionableElement elementToRemove,
             String presenceQuery,
             IBackPointer removeFunc)
     {
         ElementConnector <IBaseElement> ec = new ElementConnector <IBaseElement> ();
         ec.removeByID(this, elementToRemove.getXMIID(), presenceQuery, removeFunc);
     }
     
	/**
	 * Adds the xmi.id of inElement the the IDREFS attribute passed in.
	 *
	 * @param inElement[in]	the element whose id we are adding.
	 * @param attrName[in]	that name of the XML attribute we are adding to.
	 *
	 * @return HRESULTs
	 */
	public void addElementByID(Object elem, String attrName)
	{
		UMLXMLManip.addElementByID(this, elem, attrName);
	}
	
	/**
	 * Removes the id of the element passed in from the XML attribute value
	 * of the attribute passed in.
	 *
	 * @param elementToRemove[in]	the id of this element will be removed
	 * @param attrName[in]			name of the XML attribute that holds the
	 *										IDREFS attribute
	 *
	 * @return HRESULTs
	 */
	public void removeElementByID(IVersionableElement elem, String attrName)
	{
      // In C++ the first element is an IUnknown.  However the first parameter
      // is then cast to a IVersionableElement.  So, I have decided to just
      // require an IVersionableElement.
		UMLXMLManip.removeElementByID(this, elem, attrName);
	}

	/**
	 * 
	 * Removes the id from the XML attribute with the matching name.
	 *
	 * @param idToRemove[in] The ID to remove from the IDREFS attribute
	 *								 value.
	 * @param attrName[in] The name of the XML attribute whose value is
	 *							  going to be modified.
	 *
	 * @return HRESULTs
	 */
	public void removeElementByID(String idToRemove, String attrName)
	{
		UMLXMLManip.removeElementByID(idToRemove, this, attrName);
	}

	/**
	 * 
	 * Given an element name such as "UML:Model", "Model" will be returned.
	 * 
	 * @param node[in] the node to retrieve its name and to filter. 
	 * 
	 * @return The sliced name, else ""
	 * 
	*/
	public String retrieveSimpleName(Node node)
	{
		return XMLManip.retrieveSimpleName(node);
	}
	
	/**
	 * 
	 * This method should be called when the element to remove is
	 * fully contained as a child of the current element's XML node.
	 * This should not be called if the element to remove is simply
	 * an ID that is referenced by the current element.
	 *
	 * @param elToRemove[in] The element to remove
	 * @param presenceQuery[in] An XPath query to perform to check
	 *                          to see if elToRemove is a child
	 *                          of this element.
	 * @param remove[in] The functor to fire in order to remove
	 *                   the current element from elToRemove.
	 *
	 * @return HRESULTs
	 * @see RemoveElementByID()
	 */
	public IElement removeElement(IElement elem, String query)
	{
		new ElementConnector().removeElement(this, elem, query, null);
        return null;
	}
	
	/**
	 *
	 * The method is used to take the XMI id from newElement
	 * and place that ID value in the XML attribute value
	 * dictated by attrName. Before it does this however, it
	 * first checks to see if curElement already contains
	 * a valid value in that XML attribute. If it does, 
	 * the removeCurBack functor is fired, essentially breaking
	 * the connect between the two types. Once done, back 
	 * is fired, establishing the connection between newElement
	 * and curElement.
	 *
	 * @param newElement[in] The new element to associate with
	 *                       curElement
	 * @param attrName[in] The name of the XML attribute to check
	 *                     for existing values as well as the
	 *                     XML attribute to hold the id of 
	 *                     newElement.
	 * @param back[in] The function to fire when connecting this
	 *                 element with newElement
	 * @param removeCurBack[in] The function used to break the 
	 *                          existing connection between
	 *                          curElement and the element currently
	 *                          reference in the attrName XML value.
	 *                          If there is a valid attribute value,
	 *                          removeCurBack will be executed before
	 *                          back.
	 *
	 * @return HRESULTs
	 *
	 */
	public < NewType extends IVersionableElement, CurType extends IVersionableElement> IElement 
				setSingleElementAndConnect(NewType newEle, String attrName,
				IBackPointer<NewType> addBackPointer,
				IBackPointer<NewType> removeBackPointer)
	{
		return new ElementConnector<CurType>().
            setSingleElementAndConnect((CurType) this, newEle, attrName, 
                                        addBackPointer, removeBackPointer);
	}

	/**
	 *
	 * Checks to see if the passed in element is part of the collection
	 * of elements dictated by the query passed in.
	 *
	 * @param newElement[in] The element to match against
	 * @param query[in] This is a string to indicate how to retrieve
	 *						  the elements. If checkByID is true, this string
	 *						  will be the name of the XML attribute to query
	 *                  against. It is assumed that attribute is an IDREFS
	 *                  type. If checkByID is false, then query is an 
	 *						  XPath query string, used to gather child elements.
	 * @param checkByID[in] true is the query string is an XML attribute name,
	 *                      else false if it is an XPath query.
	 * @param flag[out] true if newElement was found in the collection,
	 *						  else false if not.
	 *
	 * @return HRESULTs
	 */
	public boolean isElementPresent(IElement newEle, String query, boolean checkById)
	{
		boolean flag = false;
		org.dom4j.Element ele = getElementNode();

		// The types, ContactManager is instantiated with
		// here, don't matter when calling IsElementPresent
		flag = ContactManager.isElementPresent(ele, newEle, query, checkById);
		
		return flag;
	}
	
	/**
	 * Retrieves all the specified nodes / children of this element.
	 *
	 * @param dummy[in] not used. Used solely to pass the type that will
	 *                   be contained in the collection.
	 * @param query[in] actual names of the elements that will make up the
	 *                   collection.
	 * @param clsid[in] the CLSID of the collection class to create and
	 *                   return
	 *
	 * @param col[out] the actual collection object that will house the
	 *                   individual element.s
	 *
	 * @return HRESULTs
	 *
	 * CAVEAT:
	 *    See ClassImpl::get_Attributes() for an example of how to use
	 *    this method.
	 *
	*/
	public < T > ETList < T > retrieveElementCollection(T dummy, String query, Class c)
	{
		Node node = getNode();
		return UMLXMLManip.retrieveElementCollection(node, dummy, query, c);
	}
	
	/**
	 * 
	 * Retrieves the element identified via xmi.id in the XML attribute
	 * passed in.
	 *
	 * @param attrName[in] The XML attribute to retrieve the xmi.id of
	 *							  the element we need.
	 * @foundElement[out] The found element, else 0.
	 *
	 * @return HRESULTs
	 *
	 */
	public < Type > Type retrieveSingleElementWithAttrID(String attrName, 
                                                        Type dummy, Class c) // Only used for type resolution
	{
      Type retEle = null;
		org.dom4j.Element element = getElementNode();
		if (element != null)
		{
			retEle = UMLXMLManip.retrieveSingleElementWithAttrID(element, dummy, attrName, c);
		}
		return retEle;
	}
	
   public < Type > ETList<Type> retrieveElementCollectionWithAttrIDs(Type dummy, // Only used for type resolution
                                                               String attrName, Class c)
   {   	  
      org.dom4j.Element element = getElementNode();
      assert element != null : "Unable to retieve the DOM element";
      
      return UMLXMLManip.retrieveElementCollectionWithAttrIDs(element, dummy, attrName, c);
   }
   
//   	public < T > ETList < T > retrieveElementCollection(String query)
//	{
//		Node node = getNode();
//		return UMLXMLManip.retrieveElementCollection(node, (T) null, query);
//	}
//   
	/**
	 * Retrieves the specific attribute value or feature from this element.
	 *
	 * @param elementName[in] the name of the element or attribute.
	 * @param type[out] the actual type retrieved.
	  *
	 * @return HRESULTs
	*/
	public < Type > Type getSpecificElement(String name, 
                                           Type dummy, Class c) // Only used for type resolution)
	{
      Type elem = null;
		Object obj = getElement(name);
		if (obj != null )
		{
			try
         {
         	if (obj != null && c.isAssignableFrom(obj.getClass()))
         	{
				elem = (Type)obj;
         	}
         }
         catch (ClassCastException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
		}
		else
		{
			elem = UMLXMLManip.retrieveSingleElement(m_Node, dummy, name, c);
		}
		return elem;
	}

	/**
	 * Retrieves the element dictated by the passed in elementName. The passed
	 * in name is the name of an XML attribute whose value results in an 
	 * xmi.id value, such as "S.9". This call assumes that the value is 
	 * singular, so if the value is actually multiple ids, only the first
	 * is used.
	 *
	 * @param elementName[in]	the element or attribute to retrieve.
	 * @param element[out]		the found element.
	 *
	 * @return HRESULTs
	 */
	private Object getElement(String name) {
		Object element = null;

		// First check to see if we have the xml attribute filled in. If
		// so, find the element. If not, check to see if a feature
		// element exists. If it does, get the information.
		String childIds = getAttributeValue(name);
		if (childIds.length() > 0)
		{
			element = getElementByID(childIds);
		}
		
		return element;
	}
	
	/**
	* Retrieves the element id'd by the passed in xmi.id value
	* 
	* @param idStr[in] The xmi.id value to search. If the value contains
	*                  multiple ids, only the first is used.
	* @param unk[out] The found value, else 0
	*
	* @return HRESULTs
	*
	*/
	private Object getElementByID(String idStr) {
		Object retObj = null;
		if (idStr.length() > 0)
		{
			// The attribute is filled in, so now go find the element
			// in the DOM tree. Be careful here, as the XMI 1.1 spec allows
			// for many IDs to be in this value. However, the UML 1.4 spec does
			// not allow for this, so we will only get the first ID listed.
			// If there are multiple IDs, they will be white space delimited.
			int pos = idStr.indexOf(" ");
			String id = "";
			if (pos > 0)
			{
				id = idStr.substring(0, pos);
			}
			else
			{
				id = idStr;
			}
			
			FactoryRetriever ret = FactoryRetriever.instance();
			Document doc = m_Node.getDocument();
			retObj = ret.findElementById(doc, id);
		}
		return retObj;
	}

	/**
	 * Makes sure the appropriate feature on this XML node exists, then adds
	 * the element to that feature
	 *
	 * @param featureName[in] The name of the XML feature that will be created
	 *								 if necessary and then appended to with the XML
	 *								 contents of value.
	 * @param query[in] The XPath query to perform to check for the existence
	 *						 of the feature specified in featureName.
	 * @param value[in] The COM object whose XML node will be appended to the
	 *						 feature specified in featureName
	 *
	 * @return HRESULTs
	 */
	public void addChild(String featureName, String query, Object value)
	{
		ContactManager.addChild(this, featureName, query, value);
	}
	
	/**
	 * Determines the ordering kind of the specified XML attribute.
	 *
	 * @param attrName[in] the name of the XML attribute to set.
	 * @param kind[out] the OrderingKind enum value.
	 *
	 * @return HRESULTs
	*/
	public int getOrderingKindValue(String attrName)
	{
		int retVal = OK_UNORDERED;
		String value = getAttributeValue(attrName);
		if (value != null && value.length() > 0)
		{
			if (value.equals("ordered"))
			{
				retVal = OK_ORDERED;
			}
		}
		return retVal;
	}

	/**
	 * Sets that appropriate value based on the value of the passed in
	 * OrderingKind enum
	 *
	 * @param attrName[in] the name of the XML attribute to set.
	 * @param kind[in[ the OrderingKind enum value.
	 *
	 * @return HRESULTs
	*/
	public void setOrderingKindValue(String attrName, int kind)
	{
		String value = "unordered";
		if (kind == OK_ORDERED)
		{
			value = "ordered";
		}
		setAttributeValue(attrName, value);
	}
	
	/**
	 * Determines the iteration activity group kind of the specified XML attribute.
	 *
	 * @param attrName[in] the name of the XML attribute to set.
	 * @param kind[out] the IterationActivityGroupKind enum value.
	 *
	 * @return HRESULTs
	*/
	public int getIterationActivityGroupKindValue(String attrName)
	{
		int kind = IAG_TEST_AT_BEGIN;
		String value = getAttributeValue(attrName);
		if (value.equals("testAtEnd"))
		{
			kind = IAG_TEST_AT_END;
		}
		return kind;
	}
	
	/**
	 * Sets that appropriate value based on the value of the passed in
	 *
	 * @param attrName[in] the name of the XML attribute to set.
	 * @param kind[in[ the OrderingKind enum value.
	 *
	 * @return HRESULTs
	*/
	public void setIterationActivityGroupKind(String attrName, int kind )
	{
		String value = "testAtBegin";
		if( kind == IAG_TEST_AT_END )
		{
		   value = "testAtEnd";
		}
		setAttributeValue( attrName, value );
	}

	/**
	 * Determines the ordering kind of the specified XML attribute.
	 *
	 * @param attrName[in] the name of the XML attribute to set.
	 * @param kind[out] the MessageKind enum value.
	 *
	 * @return HRESULTs
	*/
	public int getMessageKind(String attrName)
	{
		int kind = MK_SYNCHRONOUS;
		String value = getAttributeValue(attrName);
		if (value != null)
		{
			if (value.equals("create"))
			{
				kind = MK_CREATE;
			}
			else if (value.equals("asynchronous"))
			{
				kind = MK_ASYNCHRONOUS;
			}
			else if (value.equals("result"))
			{
				kind = MK_RESULT;
			}
		}
		return kind;
	}

	/**
	 * Sets that appropriate value based on the value of the passed in
	 * MessageKind enum
	 *
	 * @param attrName[in] the name of the XML attribute to set.
	 * @param kind[in] the MessageKind enum value.
	 *
	 * @return HRESULTs
	*/
	public void setMessageKind(String attrName, int kind )
	{
		String value = "synchronous";
		if (kind == MK_CREATE)
		{
		   value = "create";
		}
		else if (kind == MK_ASYNCHRONOUS)
		{
		   value = "asynchronous";
		}
		else if (kind == MK_RESULT)
		{
		   value = "result";
		}
		setAttributeValue( attrName, value );
	}

	/**
	 * Determines the object flow effect kind of the specified XML attribute.
	 *
	 * @param attrName[in] the name of the XML attribute to set.
	 * @param kind[out] the ObjectFlowEffectKind enum value.
	 *
	 * @return HRESULTs
	*/
	public int getObjectFlowEffectKind(String attrName)
	{
		int kind = OFE_CREATE;
		String value = getAttributeValue(attrName);
		if( value.equals("read" ))
		{
		   kind = OFE_READ;
		}
		else if( value.equals("update" ))
		{
		   kind = OFE_UPDATE;
		}
		if( value.equals("delete" ))
		{
		   kind = OFE_DELETE;
		}
		return kind;
	}

	/**
	 * Sets that appropriate value based on the value of the passed in
	 * ObjectFlowEffectKind enum
	 *
	 * @param attrName[in] the name of the XML attribute to set.
	 * @param kind[in] the ObjectFlowEffectKind enum value.
	 *
	 * @return HRESULTs
	*/
	public void setObjectFlowEffectKind(String attrName, int kind )
	{
		String value = "create";
		if (kind == OFE_READ)
		{
		   value = "read";
		}
		else if (kind == OFE_UPDATE)
		{
		   value = "update";
		}
		else if (kind == OFE_DELETE)
		{
		   value = "delete";
		}
		setAttributeValue( attrName, value );
	}

	/**
	 * Determines the object flow effect kind of the specified XML attribute.
	 *
	 * @param attrName[in] the name of the XML attribute to set.
	 * @param kind[out] the ActivityGroupKind enum value.
	 *
	 * @return HRESULTs
	*/
	public int getActivityGroupKind(String attrName )
	{
		int kind = AGK_ITERATION;
		String value = getAttributeValue(attrName);
		if( value.equals("structured"))
		{
		   kind = AGK_STRUCTURED;
		}
		else if( value.equals("interruptible"))
		{
		   kind = AGK_INTERRUPTIBLE;
		}
		return kind;
	}

	/**
	 * Sets that appropriate value based on the value of the passed in
	 * ActivityGroupKind enum
	 *
	 * @param attrName[in] the name of the XML attribute to set.
	 * @param kind[in] the ActivityGroupKind enum value.
	 *
	 * @return HRESULTs
	*/
	public void setActivityGroupKind(String attrName, int kind )
	{
		String value = "iteration";
		if (kind == AGK_STRUCTURED)
		{
		   value = "structured";
		}
		else if (kind == AGK_INTERRUPTIBLE)
		{
		   value = "interruptible";
		}
		setAttributeValue( attrName, value );
	}

	/**
	 * Determines the object flow effect kind of the specified XML attribute.
	 *
	 * @param attrName[in] the name of the XML attribute to set.
	 * @param kind[out] the ParameterSemanticsKind enum value.
	 *
	 * @return HRESULTs
	*/
	public int getParameterSemanticsKind(String attrName )
	{
		int kind = PK_BYVALUE;
		String value = getAttributeValue(attrName);
		if (value != null)
		{
			if( value.equals("byRef"))
			{
			   kind = PK_BYREF;
			}
			else if( value.equals("optional"))
			{
			   kind = PK_OPTIONAL;
			}
			else if( value.equals("addressOf"))
			{
			   kind = PK_ADDRESSOF;
			}
		}
		return kind;
	}

	/**
	 * Sets that appropriate value based on the value of the passed in
	 * ParameterSemanticsKind enum
	 *
	 * @param attrName[in] the name of the XML attribute to set.
	 * @param kind[in] the ParameterSemanticsKind enum value.
	 *
	 * @return HRESULTs
	*/
	public void setParameterSemanticsKind(String attrName, int kind )
	{
		String value = "byValue";
		if (kind == PK_OPTIONAL)
		{
		   value = "optional";
		}
		else if (kind == PK_BYREF)
		{
		   value = "byRef";
		}
		else if (kind == PK_ADDRESSOF)
		{
		   value = "addressOf";
		}
		setAttributeValue( attrName, value );
	}

	/**
	 * Translates the passed in string value into an AggregationKind
	 * enum value.
	 *
	 * @param attrName[in]	The string to translate
	 * @param kind[out]		The enum value			
	 *
	 * @return HRESULTs
	 */
	public int getAggregationKindValue(String attrName)
	{ 
		int kind = AK_NONE;
		String value = getAttributeValue(attrName);
		if( value.equals("aggregate"))
		{
		   kind = AK_AGGREGATE;
		}
		else if( value.equals("composite"))
		{
		   kind = AK_COMPOSITE;
		}
		return kind;
	}

	/**
	 * Sets the specified attribute on this element to the passed in enum value
	 *
	 * @param attrName[in]	The XML attribute value to set
	 * @param kind[in]		The enum value
	 *
	 * @return HRESULTs
	 */
	public void setAggregationKindValue(String attrName, int  kind )
	{
		String value = "none";
		if (kind == AK_AGGREGATE)
		{
		   value = "aggregate";
		}
		else if (kind == AK_COMPOSITE)
		{
		   value = "composite";
		}
		setAttributeValue( attrName, value );
	}

	/**
	 * Retrieves the value of the specified XML attribute, translating that
	 *	string value into the appropriate ScopeKind enum value.
	 *
	 * @param attrName[in]	The XML attribute to retrieve
	 * @param kind[out]		The translated ScopeKind value
	 *
	 * @return HRESULTs
	 */
	public int getScopeKindValue(String attrName)
	{
		int kind = SK_INSTANCE;
		String value = getAttributeValue(attrName);
		if( value.equals("classifier"))
		{
		   kind = SK_CLASSIFIER;
		}
		return kind;
	}

	/**
	 * Sets the value of the passed in attribute to the value of the
	 * ScopeKind enum value passed in.
	 *
	 * @param attrName[in]	Name of the XML attribute to set
	 * @param kind[in]		The ScopeKind enum value
	 *
	 * @return HRESULTs
	 */
	public void setScopeKindValue(String attrName, int kind)
	{
		String value = "instance";
		if (kind == SK_CLASSIFIER)
		{
		   value = "classifier";
		}
		setAttributeValue( attrName, value );
	}

	/**
	 *
	 * Retrieves the value of the specified XML attribute, translating that
	 *	string value into the appropriate ParameterDirectionKind enum value.
	 *
	 * @param attrName[in] The XML attribute name whose value is to be
	 *							  translated.
	 * @param kind[out] The translated value.
	 *
	 * @return HRESULTs
	 */
	public int getParameterDirectionKindValue(String attrName)
	{
		int kind = PDK_IN;
		String value = getAttributeValue(attrName);
		if( "out".equals(value))
		{
		   kind = PDK_OUT;
		}
		else if( "inout".equals(value))
		{
		   kind = PDK_INOUT;
		}
		else if( "result".equals(value))
		{
		   kind = PDK_RESULT;
		}
		return kind;
	}

	/**
	 * Sets the value of the passed in attribute to the value of the
	 * ParameterDirectionKind enum value passed in.
	 *
	 * @param attrName[in]	Name of the XML attribute to set
	 * @param kind[in]		The ParameterDirectionKind enum value
	 *
	 * @return HRESULTs
	 */
	public void setParameterDirectionKindValue(String attrName, int kind)
	{
		String value = "in";
		if (kind == PDK_INOUT)
		{
		   value = "inout";
		}
		else if (kind == PDK_OUT)
		{
		   value = "out";
		}
		else if (kind == PDK_RESULT)
		{
		   value = "result";
		}
		setAttributeValue( attrName, value );
	}

	/**
	 * Retrieves the Changeability value of this element.
	 *
	 * @param attrName[in]	Name of the XML attribute that holds the value
	 * @param kind[out]		The ChangeableKind enum value
	 *
	 * @return HRESULTs
	 */
	public int getChangeableKindValue(String attrName)
	{
		int kind = CK_UNRESTRICTED;
		String value = getAttributeValue(attrName);
		if (value != null)
		{
			if( value.equals("restricted"))
			{
			   kind = CK_RESTRICTED;
			}
			else if( value.equals("addOnly"))
			{
			   kind = CK_ADDONLY;
			}
			else if( value.equals("removeOnly"))
			{
			   kind = CK_REMOVEONLY;
			}
		}
		return kind;
	}

	/**
	 * Sets the changeability value for this element.
	 *
	 * @param attrName[in]	Name of the XML attribute to hold the value
	 * @param kind[in]		The actual value.
	 *
	 * @return HRESULTs
	 */
	public void setChangeableKindValue(String attrName, int kind)
	{
		String value = "unrestricted";
		if (kind == CK_RESTRICTED)
		{
		   value = "restricted";
		}
		else if (kind == CK_ADDONLY)
		{
		   value = "addOnly";
		}
		else if (kind == CK_REMOVEONLY)
		{
		   value = "removeOnly";
		}
		setAttributeValue( attrName, value );
	}

	/**
	 *
	 * Retrieves the Instatiotion value of this element
	 *
	 * @param attrName[in] Name of the XML attribute that holds the value
	 * @param kind[out] the actual value.
	 *
	 * @return HRESULT
	 *
	 */
	public int getInstantiationKindValue(String attrName)
	{
		int kind = IK_DIRECT;
		String value = getAttributeValue(attrName);
		//NL testing for null values
		//TODO We need to find out why it does not return anything for IComponent type elements
		if (value != null && value.length() > 0) {

			if (value.equals("indirect")) {
				kind = IK_INDIRECT;
			} else if (value.equals("none")) {
				kind = IK_NONE;
			}
		}
		return kind;
	}

	/**
	 * Sets the Instantiation value for this element.
	 *
	 * @param attrName[in]	Name of the XML attribute to hold the value
	 * @param kind[in]		The actual value.
	 *
	 * @return HRESULTs
	 */
	public void setInstantiationValue(String attrName, int kind )
	{
		String value = "direct";
		if (kind == IK_INDIRECT)
		{
		   value = "indirect";
		}
		else if (kind == IK_NONE)
		{
		   value = "none";
		}
		setAttributeValue( attrName, value );
	}

	/**
	 *
	 * Retrieves the value of the specified XML attribute, translating that
	 *	string value into the appropriate PartKind enum value.
	 *
	 * @param attrName[in] The XML attribute name whose value is to be
	 *							  translated.
	 * @param kind[out] The translated value.
	 *
	 * @return HRESULTs
	 */
	public int getPartKind(String attrName)
	{
		int kind = PK_FOCUS;
		String value = getAttributeValue(attrName);
		if( value.equals("auxiliary"))
		{
		   kind = PK_AUXILIARY;
		}
		else if( value.equals("interfaceImplementation"))
		{
		   kind = PK_INTERFACEIMPLEMENTATION;
		}
		return kind;
	}

	/**
	 * Sets the PartKind value for this element.
	 *
	 * @param attrName[in]	Name of the XML attribute to hold the value
	 * @param kind[in]		The actual value.
	 *
	 * @return HRESULTs
	 */
	public void setPartKind(String attrName, int kind)
	{
		String value = "focus";
		if (kind == PK_AUXILIARY)
		{
		   value = "auxiliary";
		}
		else if (kind == PK_INTERFACEIMPLEMENTATION)
		{
		   value = "interfaceImplementation";
		}
		setAttributeValue( attrName, value );
	}

	/**
	 *
	 * Retrieves the value of the specified XML attribute, translating that
	 *	string value into the appropriate ObjectNodeOrderingKind enum value.
	 *
	 * @param attrName[in] The XML attribute name whose value is to be
	 *							  translated.
	 * @param kind[out] The translated value.
	 *
	 * @return HRESULTs
	 */
	public int getObjectNodeOrderingKind(String attrName)
	{
		int kind = OOK_UNORDERED;
		String value = getAttributeValue(attrName);
		if( value.equals("ordered"))
		{
		   kind = OOK_ORDERED;
		}
		else if( value.equals("LIFO"))
		{
		   kind = OOK_LIFO;
		}
		else if( value.equals("FIFO"))
		{
		   kind = OOK_FIFO;
		}
		return kind;
	}

	/**
	 * Sets the ObjectNodeOrderingKind value for this element.
	 *
	 * @param attrName[in]	Name of the XML attribute to hold the value
	 * @param kind[in]		The actual value.
	 *
	 * @return HRESULTs
	 */
	public void setObjectNodeOrderingKind(String attrName, int kind)
	{
		String value = "unordered";
		if (kind == OOK_ORDERED)
		{
		   value = "ordered";
		}
		else if (kind == OOK_LIFO)
		{
		   value = "LIFO";
		}
		else if (kind == OOK_FIFO)
		{
		   value = "FIFO";
		}
		setAttributeValue( attrName, value );
	}

	/**
	 *
	 * Retrieves the value of the specified XML attribute, translating that
	 *	string value into the appropriate InteractionOperator enum value.
	 *
	 * @param attrName[in] The XML attribute name whose value is to be
	 *							  translated.
	 * @param kind[out] The translated value.
	 *
	 * @return HRESULTs
	 */
	public int getInteractionOperator(String attrName)
	{
		int kind = IO_ASSERT;
		String value = getAttributeValue(attrName);
		if (value != null)
		{
			if( value.equals("alt"))
			{
			   kind = IO_ALT;
			}
			else if( value.equals("else"))
			{
			   kind = IO_ELSE;
			}
			else if( value.equals("opt"))
			{
			   kind = IO_OPT;
			}
			else if( value.equals("par"))
			{
			   kind = IO_PAR;
			}
			else if( value.equals("loop"))
			{
			   kind = IO_LOOP;
			}
			else if( value.equals("region"))
			{
			   kind = IO_REGION;
			}
			else if( value.equals("neg"))
			{
			   kind = IO_NEG;
			}
			else if( value.equals("seq"))
			{
			   kind = IO_SEQ;
			}
			else if( value.equals("strict"))
			{
			   kind = IO_STRICT;
			}
			else if (value.equals("filter"))
			{
				kind = IO_FILTER;
			}
		}
		return kind;
	}

	/**
	 * Sets the InteractionOperator value for this element.
	 *
	 * @param attrName[in]	Name of the XML attribute to hold the value
	 * @param kind[in]		The actual value.
	 *
	 * @return HRESULTs
	 */
	public void setInteractionOperator(String attrName, int kind)
	{
		String value = "assert";
		if (kind == IO_ALT)
		{
		   value = "alt";
		}
		else if (kind == IO_ELSE)
		{
		   value = "else";
		}
		else if (kind == IO_OPT)
		{
		   value = "opt";
		}
		else if (kind == IO_PAR)
		{
		   value = "par";
		}
		else if (kind == IO_LOOP)
		{
		   value = "loop";
		}
		else if (kind == IO_REGION)
		{
		   value = "region";
		}
		else if (kind == IO_NEG)
		{
		   value = "neg";
		}
		else if (kind == IO_SEQ)
		{
		   value = "seq";
		}
		else if (kind == IO_STRICT)
		{
		   value = "strict";
		}
		else if (kind == IO_FILTER)
		{
			value = "filter";
		}
		setAttributeValue( attrName, value );
	}

	/**
	 *
	 * Retrieves the value of the specified XML attribute, translating that
	 *	string value into the appropriate StateMachineKind enum value.
	 *
	 * @param attrName[in] The XML attribute name whose value is to be
	 *							  translated.
	 * @param kind[out] The translated value.
	 *
	 * @return HRESULTs
	 */
	public int getStateMachineKind(String attrName)
	{
		int kind = SMK_BEHAVIOR;
		String value = getAttributeValue(attrName);
		if( value.equals("protocol"))
		{
		   kind = SMK_PROTOCOL;
		}
		return kind;
	}

	/**
	 * Sets the StateMachineKind value for this element.
	 *
	 * @param attrName[in]	Name of the XML attribute to hold the value
	 * @param kind[in]		The actual value.
	 *
	 * @return HRESULTs
	 */
	public void setStateMachineKind(String attrName, int kind)
	{
		String value = "behavior";
		if (kind == SMK_PROTOCOL)
		{
		   value = "protocol";
		}
		setAttributeValue( attrName, value );
	}

	/**
	 *
	 * Retrieves the value of the specified XML attribute, translating that
	 *	string value into the appropriate PseudostateKind enum value.
	 *
	 * @param attrName[in] The XML attribute name whose value is to be
	 *							  translated.
	 * @param kind[out] The translated value.
	 *
	 * @return HRESULTs
	 */
	public int getPseudostateKind(String attrName)
	{
		int kind = PK_INITIAL;
		String value = getAttributeValue(attrName);
		if (value != null)
		{
			if( value.equals("choice"))
			{
			   kind = PK_CHOICE;
			}
			else if( value.equals("deepHistory"))
			{
			   kind = PK_DEEPHISTORY;
			}
			else if( value.equals("fork"))
			{
			   kind = PK_FORK;
			}
			else if( value.equals("join"))
			{
			   kind = PK_JOIN;
			}
			else if( value.equals("junction"))
			{
			   kind = PK_JUNCTION;
			}
			else if( value.equals("shallowHistory"))
			{
			   kind = PK_SHALLOWHISTORY;
			}
			else if( value.equals("entryPoint"))
			{
			   kind = PK_ENTRYPOINT;
			}
		}
		return kind;
	}

	/**
	 * Sets the PseudostateKind value for this element.
	 *
	 * @param attrName[in]	Name of the XML attribute to hold the value
	 * @param kind[in]		The actual value.
	 *
	 * @return HRESULTs
	 */
	public void setPseudostateKind(String attrName, int kind)
	{
		String value = "initial";
		if (kind == PK_CHOICE)
		{
		   value = "choice";
		}
		else if (kind == PK_DEEPHISTORY)
		{
		   value = "deepHistory";
		}
		else if (kind == PK_FORK)
		{
		   value = "fork";
		}
		else if (kind == PK_JOIN)
		{
		   value = "join";
		}
		else if (kind == PK_JUNCTION)
		{
		   value = "junction";
		}
		else if (kind == PK_SHALLOWHISTORY)
		{
		   value = "shallowHistory";
		}
		else if (kind == PK_ENTRYPOINT)
		{
		   value = "entryPoint";
		}
		setAttributeValue( attrName, value );
	}

	/**
	 * Sets the specified attribute on this element.
	 *
	 * @param inElement[in]	the element who will be queried for the IVersionableElement
	 *								interface, and then whose xmi.id value will be used as
	 *								the value to place in the XML attribute dictated
	 *								by attrName.
	 * @param attrName[in]  name of the XML attribute to set the xmi.id of inElement.
	 *
	 * @return HRESULTs
	 */
	public void setElement( Object inElement, String attrName )
	{
		ContactManager.setElement(this, inElement, attrName);
	}

	/**
	 * Makes sure the appropriate feature on this XML node exists, then adds
	 * the element to that feature
	 *
	 * @param featureName[in] The name of the XML feature that will be created
	 *								 if necessary and then appended to with the XML
	 *								 contents of value.
	 * @param query[in] The XPath query to perform to check for the existence
	 *						 of the feature specified in featureName.
	 * @param value[in] The COM object whose XML node will be appended to the
	 *						 feature specified in featureName
	 * @param reference [in] The COM object whose XML node will be the reference
	 *                       for the location to insert the value
	 *
	 * @return HRESULTs
	 */
	public void insertChildBefore( String featureName,
								   String query,
								   Object value,
								   Object reference )
	{
		ContactManager.insertChildBefore(this, featureName, query, value, reference);
	}
	
	/**
	 * Translates the passed in string value into an CallConcurrencyKind
	 * enum value.
	 *
	 * @param attrName[in]	The string to translate
	 * @param kind[out]		The enum value			
	 *
	 * @return HRESULTs
	 */
	public int getCallConcurrencyKindValue(String attrName)
	{
		int kind = CCK_SEQUENTIAL;
		String value = getAttributeValue(attrName);
		if("guarded".equals(value))
		{
		   kind = CCK_GUARDED;
		}
		else if( "concurrent".equals(value))
		{
		   kind = CCK_CONCURRENT;
		}
		return kind;
	}

	/**
	 * Sets the specified attribute on this element to the passed in enum value
	 *
	 * @param attrName[in]	The XML attribute value to set
	 * @param kind[in]		The enum value
	 *
	 * @return HRESULTs
	 */
	public void setCallConcurrencyKindValue(String attrName, int kind)
	{
		String value = "sequential";
		if (kind == CCK_GUARDED)
		{
		   value = "guarded";
		}
		else if (kind == CCK_CONCURRENT)
		{
		   value = "concurrent";
		}
		setAttributeValue( attrName, value );
	}

	/**
	 * Translates the passed in string value into an ActivityKind
	 * enum value.
	 *
	 * @param attrName[in]	The string to translate
	 * @param kind[out]		The enum value			
	 *
	 * @return HRESULT
	 */
	public int getActivityKindValue( String attrName)
	{
		int kind = AK_STRUCTURED;
		String value = getAttributeValue(attrName);
		if( value.equals("flowChart"))
		{
		   kind = AK_FLOWCHART;
		}
		return kind;
	}
	
	/**
	 * Sets the specified attribute on this element to the passed in enum value
	 *
	 * @param attrName[in]	The XML attribute value to set
	 * @param kind[in]		The enum value
	 *
	 * @return HRESULTs
	 */
	public void setActivityKindValue(String attrName, int kind)
	{
		String value = "structured";
		if (kind == AK_FLOWCHART)
		{
		   value = "flowChart";
		}
		setAttributeValue( attrName, value );
	}

}



