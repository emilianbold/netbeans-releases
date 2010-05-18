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
import org.dom4j.Node;

/**
 * ContactManager is used to control the management of back and
 * forward pointers between types. For instance, when a caller is adding
 * a PresentationElement to a NamedElements collection of PresentationElements,
 * it is very important to also make sure that the PresentationElement
 * being added is told of the NamedElement that it is being added to.
 * The same goes from removals. When that same PresentationElement is being
 * removed from the NamedElement, its reference to that NamedElement needs
 * to be cleaned up.
 */
public class ContactManager {

	/**
	 * 
	 */
	public ContactManager() {
		super();
	}

	/**
	 *
	 * Checks to see if the passed in element is part of the collection
	 * of elements dictated by the query passed in.
	 *
	 * @param curElement[in] The DOM element to check.
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
	public static boolean isElementPresent(org.dom4j.Element ele, 
										   IVersionableElement newEle, 
										   String query, boolean checkById) 
	{
        if (newEle == null)
            return false;

		boolean flag = false;
		String xmiid = newEle.getXMIID();
		if (checkById)
		{
			String ids = UMLXMLManip.getAttributeValue(ele, query);
			if (ids != null && ids.length() > 0)
			{
				int pos = ids.indexOf(xmiid);
				if (pos >= 0)
				{
					flag = true;
				}
			}
		}
		else
		{
			Node node = newEle.getNode();
			flag = isDirectChild(ele, node);
		}
		return flag;
	}

	/**
	 * Makes sure the appropriate feature on this XML node exists, then adds
	 * the element to that feature
	 *
	* @param curElement[in] Element to check against
	 * @param featureName[in] The name of the XML feature that will be created
	 *								 if necessary and then appended to with the XML
	 *								 contents of value.
	* @param query[in] The XPath query to perform to check for the existence
	 *						 of the feature specified in featureName.
	 * @param value[in] The COM object whose XML node will be appended to the
	 *						 feature specified in featureName
	 *
	 * @return S_OK, else EFR_S_EVENT_CANCELLED if the particular event was
	*         cancelled, else other HRESULTs on unknown errors
	 */
	public static void addChild(IVersionableElement curElement, 
								String featureName, 
								String query, 
								Object value)
	{
		Node actual = null;
		IElementChangeDispatchHelper helper = new ElementChangeDispatchHelper();
		if (UMLXMLManip.fireElementPreModified(curElement, helper))
		{
			Node curNode = curElement.getNode();
			actual = UMLXMLManip.ensureElementExists(curNode, featureName, query);
			if (actual != null)
			{
				if (value instanceof IVersionableElement)
				{
					IVersionableElement verValue = (IVersionableElement)value;
					boolean isVersioned = UMLXMLManip.resolveInternalNode(curElement, verValue);
					UMLXMLManip.appendChild(actual, value);
					
					if (isVersioned)
					{
						// Now add this element to the type file of the project. We only
						// want to add the child element if the immediate parent or
						// one of its parents has been versioned

						// We no longer need to do this now that we are resolving
						// types in a more investigative way. See TypeManager for details
						//_VH( manip.AddToTypeFile( verValue ));
					}
				}
				if (helper != null)
				{
					helper.dispatchElementModified(curElement);
				}
			}
		}
	}

	/**
	 * Sets the specified attribute on this element.
	 *
	 * @param element[in] The XML element who owns the XML attribute
	 *                    that will be set.
	 * @param inElement[in]	the element who will be queried for the IVersionableElement
	 *								interface, and then whose xmi.id value will be used as
	 *								the value to place in the XML attribute dictated
	 *								by attrName.
	 * @param attrName[in]  name of the XML attribute to set the xmi.id of inElement.
	 *
	 * @return HRESULTs
	 */
	public static void setElement(IVersionableElement element, 
								  Object inElement, String attrName)
	{
		IVersionableElement ver = null;
		IElement actual = null;
		if (inElement instanceof IVersionableElement)
		{
			ver = (IVersionableElement)inElement;
		}
		
		if (element instanceof IElement)
		{
			actual = (IElement)element;
		}
		
		if (ver != null && actual != null)
		{
			// Using the UMLURILocator is necessary in that it takes
			// into account the subtlties of calculating correct URI's
			// of element within and external to the Project that
			// the element is in and where ver resides
			UMLURILocator loc = new UMLURILocator(actual);
			String elementID = loc.getVersionedURI(ver);
			UMLXMLManip.setAttributeValue(element, attrName, elementID);
		}
		else
		{
			UMLXMLManip.setAttributeValue(element, attrName, "");
		}
	}

	/**
	 * Makes sure the appropriate feature on this XML node exists,
	* then adds the element to that feature before the reference.
	 *
	* @param curElement[in] Element to check against
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
	public static void insertChildBefore(IVersionableElement curElement, 
										 String featureName, 
										 String query, 
										 Object value, Object reference)
	{
		Node actual = null;
		IElementChangeDispatchHelper helper = new ElementChangeDispatchHelper();
		if (UMLXMLManip.fireElementPreModified(curElement, helper))
		{
			Node curNode = curElement.getNode();
			actual = UMLXMLManip.ensureElementExists(curNode, featureName, query);
			if (actual != null)
			{
				if (value instanceof IVersionableElement)
				{
					IVersionableElement verValue = (IVersionableElement)value;
					IVersionableElement verRef = (IVersionableElement)reference;
					boolean isVersioned = UMLXMLManip.resolveInternalNode(curElement, verValue);
					UMLXMLManip.insertBefore(actual, value, verRef);
					
					if (isVersioned)
					{
						// Now add this element to the type file of the project. We only
						// want to add the child element if the immediate parent or
						// one of its parents has been versioned

						// We no longer need to do this now that we are resolving
						// types in a more investigative way. See TypeManager for details
						//_VH( manip.AddToTypeFile( verValue ));
					}
				}
				if (helper != null)
				{
					helper.dispatchElementModified(curElement);
				}
			}
		}
	}

	/**
	 *
	 * Determines if childTestNode is a child node of curElement
	 *
	 * @param curElement[in] The parent node
	 * @param childTestNode[in] The node to see if it is a child of curElementa
	 * @param isChild[out] true if the node is a child node, else false
	 *
	 * @return HREUSLT
	 *
	 */
	public static boolean isDirectChild( org.dom4j.Element curElement, 
										 Node childTestNode)
	{
		return UMLXMLManip.isDirectChild(curElement, childTestNode);
	}

	/**
	 * 
	 * Removes the element matching the passed in ID from curElement.
	 *
	 * @param id[in] The ID of the element to remove
	 * @param curElement[in] The element to remove the element that
	 *                       has an ID that matches id
	 * @param attrName[in] Name of the XML attribute on curElement
	 *                     that will be modified as a result of this
	 *                     call.
	 * @param remove[in] The functor used to complete the circle
	 *                   between the two types.
	 */
	public static void removeElementByID( String id,
							   			IVersionableElement curElement,
							   			String attrName,
										IBackPointer remove )
	{
		if (curElement != null)
		{
			UMLXMLManip.removeElementByID(id, curElement, attrName);
			
			// Find the element by ID. Then set that on the BackPointer
			// functor
			FactoryRetriever fact = FactoryRetriever.instance();
			Node node = curElement.getNode();
			Document doc = node.getDocument();
			Object obj = fact.findElementById(doc, id);
			remove.execute(obj);
		}
	}
	
	/**
	 * Called to remove elToRemove from curElement. elToRemove
	 * can be an element completely encapsulated as a child of
	 * curElement, as simply referenced via ID.
	 *
	 * @param byID[in] - true if elToRemove is not fully contained, but 
	 *                   rather refereced via ID, else
	 *                 - false to remove the fully contained child.
	 * @param elToRemove[in] The element to remove
	 * @param curElement[in] The element who will be affected by
	 *                       the removal of elToRemove.
	 * @param presenceQuery[in] An XPath query to perform to check
	 *                          to see if elToRemove is a child
	 *                          of curElement.
	 * @param remove[in] The functor to fire in order to remove
	 *                   curElement from elToRemove.
	 *
	 * @return HRESULTs
	 */
	public static void removeElement( boolean byID, 
						   			  IVersionableElement elToRemove,
									  IVersionableElement curElement,
						   			  String presenceQuery,
									  IBackPointer remove )
	{
		if (elToRemove != null)
		{
			String id = elToRemove.getXMIID();
			if (curElement != null)
			{
				boolean isPresent = false;
				Node node = curElement.getNode();
				if (node instanceof org.dom4j.Element)
				{
					org.dom4j.Element element = (org.dom4j.Element)node;
					isPresent = isElementPresent(element, elToRemove, 
												 presenceQuery, byID);
				}
				if (isPresent)
				{
					if (byID)
					{
						UMLXMLManip.removeElementByID(curElement, elToRemove, presenceQuery);
					}
					else
					{
						UMLXMLManip.removeChild(node, elToRemove);
					}
                    if (remove != null)
					   remove.execute(null);
                                    IElementChangeDispatchHelper helper = new ElementChangeDispatchHelper();
                                    helper.dispatchElementModified(curElement);
				}
			}
		}
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
	 * @param curElement[in] The element being manipulated. It is
	 *                       this element's XML attribute values
	 *                       that are being manipulated.
	 * @param attrName[in] The name of the XML attribute to check
	 *                     for existing values as well as the
	 *                     XML attribute to hold the id of 
	 *                     newElement.
	 * @param back[in] The functor object used to establish the
	 *                 new connection between newElement and
	 *                 curElement
	 * @param removeCurBack[in] The functor used to break the 
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
	public static <Owner extends IVersionableElement> IElement 
                setSingleElementAndConnect( 
                    Owner owner,
                    IVersionableElement element,
					String attrName,
					IBackPointer<Owner> back,
					IBackPointer<Owner> removeCurBack )
	{
		if (element != null)
		{
			Node node = element.getNode();
			org.dom4j.Element elNode = (org.dom4j.Element)node;
			if (owner != null)
			{
				String spaceID = owner.getXMIID();
				String curID = UMLXMLManip.getAttributeValue(elNode, attrName);
                if (curID != null)
				    curID = UMLXMLManip.retrieveRawID(curID);
				if (!spaceID.equals(curID))
				{
					// The current namespace does not match the one coming
					// in. So we need to remove this NamedElement from
					// the old Namespace and then set the new namespace
					if (curID != null && curID.length() > 0)
					{
						Document doc = elNode.getDocument();
						FactoryRetriever fact = FactoryRetriever.instance();
						Object obj = fact.findElementById(doc, curID);
						if (obj != null)
						{
                            if(obj instanceof IVersionableElement)
							     removeCurBack.execute((Owner)obj);
						}
					}
					setElement(element, owner, attrName);
					back.execute(owner);
				}
			}
			else
			{
				setElement(element, owner, attrName);
			}
		}
        
        return (IElement) owner;
	}
	
	/**
	 *
	 * Adds newElement to curElement, establishing the back pointer
	 * connection between the two types, dictated by the BackPointer
	 * object.
	 *
	 * @param byID[in] - true to add the new element using that
	 *                   element's id else
	 *                 - false to add the element as a child node.
	 * @param newElement[in] The new element to add
	 * @param curElement[in] The element that we are adding to
	 * @param presenceQuery[in] The XPath query to use to determine if
	 *                          the element to be added already exists
	 *                          on curElement. If byID is set to true,
	 *                          this will be the name of the XML attribute
	 *                          to check for ID values in.
	 * @param locationQuery[in] The location where the new element will
	 *                          be placed. This should be an XPath string
	 *                          indicating the XML element to add the
	 *                          new element to. If byID is true, this
	 *                          parameter is ignored.
	 * @param back[in]   The functor to be fired in order
	 *                    to connect the newElement with curElement.
	 *                    This is how the back pointer management is done.
	 *
	 * @return HRESULTs
	 */
	 public static <Owner extends IVersionableElement> void addMemberAndConnect( boolean byID, 
                                                 IVersionableElement newElement, 
                         Owner curElement,
						 String presenceQuery,
				 		 String locationQuery,
						 IBackPointer<Owner> back )
	 {
	 	boolean isPresent = false;
	 	if (curElement != null)
	 	{
	 		Node node = curElement.getNode();
	 		org.dom4j.Element element = (org.dom4j.Element)node;
	 		isPresent = isElementPresent(element, newElement, presenceQuery, byID);
	 		if (!isPresent)
	 		{
	 			if (byID)
	 			{
	 				UMLXMLManip.addElementByID(curElement, newElement, presenceQuery);
	 			}
	 			else
	 			{
	 				addChild(curElement, locationQuery, locationQuery, newElement);
	 			}

				// Make sure the Attribute knows that it is now
				// part of this qualifier
	 			back.execute(curElement);
	 		}
         
         curElement.setDirty(true);
	 	}
	 }
	 
	/**
	 *
	 * Adds newElement to curElement, establishing the back pointer
	 * connection between the two types, dictated by the BackPointer
	 * object.
	 *
	 * @param byID[in] - true to add the new element using that
	 *                   element's id else
	 *                 - false to add the element as a child node.
	 * @param newElement[in] The new element to add
	 * @param newElement[in] The element to insert before
	 * @param curElement[in] The element that we are adding to
	 * @param presenceQuery[in] The XPath query to use to determine if
	 *                          the element to be added already exists
	 *                          on curElement. If byID is set to true,
	 *                          this will be the name of the XML attribute
	 *                          to check for ID values in.
	 * @param locationQuery[in] The location where the new element will
	 *                          be placed. This should be an XPath string
	 *                          indicating the XML element to add the
	 *                          new element to. If byID is true, this
	 *                          parameter is ignored.
	 * @param back[in]   The functor to be fired in order
	 *                    to connect the newElement with curElement.
	 *                    This is how the back pointer management is done.
	 *
	 * @return HRESULTs
	 */
	public static <Owner extends IVersionableElement, 
				   Child extends IVersionableElement> 
		void insertMemberBeforeAndConnect( boolean byID,
										 Child newElement, 
										 Child refElement, 
										 Owner curElement,
										 String presenceQuery,
										 String locationQuery,
										 IBackPointer<Owner> back )
	 {
	 	boolean isPresent = false;
	 	if (curElement != null)
	 	{
	 		Node node = curElement.getNode();
	 		org.dom4j.Element element = (org.dom4j.Element)node;
	 		isPresent = isElementPresent(element, newElement, presenceQuery, byID);
	 		if (!isPresent)
	 		{
	 			if (byID)
	 			{
	 				UMLXMLManip.insertElementBeforeByID(curElement, presenceQuery, newElement, refElement);
	 			}
	 			else
	 			{
	 				insertChildBefore(curElement, locationQuery, locationQuery, newElement, refElement);
	 			}
	 			
				// Make sure the Attribute knows that it is now
				// part of this qualifier
	 			back.execute(curElement);
	 		}
	 	}
	 }
	 
}

