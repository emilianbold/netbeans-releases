/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.uml.core.metamodel.core.foundation;

import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * RedefinableElementImpl is the implementation of the RedefinableElement UML
 * meta type.
 *
 * A redefinable element is owned by a classifier and may be redefined when the 
 * owning classifier is specialized.
 */
public class RedefinableElement extends NamedElement implements IRedefinableElement{

	/**
	 * 
	 */
	public RedefinableElement() {
		super();
	}

	/**
	 *
	 * Retrieves the IsFinal flag on this element. If true, this redefinable 
	 * element cannot be further redefined. The default value is false.
	 *
	 * @param pVal[out] The value of the flag
	 *
	 * @return HRESULT
	 *
	 */
	public boolean getIsFinal() {
		return getBooleanAttributeValue("isFinal", false);
	}

	/**
	 *
	 * Sets the IsFinal flag on this element. If true, this redefinable 
	 * element cannot be further redefined. The default value is false.
	 *
	 * @param newVal[in] The new flag
	 *
	 * @return HRESULT
	 *
	 */
	public void setIsFinal(boolean value) {
		boolean isFinal = false;
		isFinal = getIsFinal();
		
		// No need to set if not different
		if (isFinal != value)
		{
			EventDispatchRetriever ret = EventDispatchRetriever.instance();
			IElementChangeEventDispatcher disp = (IElementChangeEventDispatcher) ret.getDispatcher(EventDispatchNameKeeper.modifiedName());
			boolean proceed = true;
			if (disp != null)
			{
				IEventPayload payload = disp.createPayload("PreFinalModified");
				proceed = disp.firePreFinalModified(this, value, payload);
			}
			
			if (proceed)
			{
				setBooleanAttributeValue("isFinal", value);
				if (disp != null)
				{
					IEventPayload payload = disp.createPayload("FinalModified");
					disp.fireFinalModified(this, payload);
				}
			}
			else
			{
				//cancel the event
			}
		}
	}

	/**
	 *
	 * Adds the redefinable element that is being redefined by this element. 
	 *
	 * @param element[in] The element being redefined
	 *
	 * @return HRESULT
	 *
	 */
	public long addRedefinedElement(IRedefinableElement element) {
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		IElementChangeEventDispatcher disp = (IElementChangeEventDispatcher) ret.getDispatcher(EventDispatchNameKeeper.modifiedName());
		boolean proceed = true;
		if (disp != null)
		{
			IEventPayload payload = disp.createPayload("OnPreRedefinedElementAdded");
			proceed = disp.firePreRedefinedElementAdded(this, element, payload);
		}
			
		if (proceed)
		{
			final IRedefinableElement elem = element;
			addChildAndConnect (
								true, "redefinedElement", "redefinedElement", elem,
								new IBackPointer<IRedefinableElement>() 
								{
									public void execute(IRedefinableElement obj) 
									{
										elem.addRedefiningElement(obj);
									}
								}
							);
			
			if (disp != null)
			{
				IEventPayload payload = disp.createPayload("OnRedefinedElementAdded");
				disp.fireRedefinedElementAdded(this, element, payload);
			}
		}
		else
		{
			//cancel the event
		}
		return 0;
	}

	/**
	 *
	 * Removes the redefinable element that is being redefined by this element. 
	 *
	 * @param element[in] The element to remove.
	 *
	 * @return HRESULT
	 *
	 */
	public long removeRedefinedElement(IRedefinableElement element) {
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		IElementChangeEventDispatcher disp = (IElementChangeEventDispatcher) ret.getDispatcher(EventDispatchNameKeeper.modifiedName());
		boolean proceed = true;
		if (disp != null)
		{
			IEventPayload payload = disp.createPayload("OnPreRedefinedElementRemoved");
			proceed = disp.firePreRedefinedElementRemoved(this, element, payload);
		}
			
		if (proceed)
		{
			final IRedefinableElement elem = element;
			new ElementConnector<IRedefinableElement>().removeByID
								   (
									this, elem, "redefinedElement",
									 new IBackPointer<IRedefinableElement>() 
									 {
										public void execute(IRedefinableElement obj) 
										{
										   elem.removeRedefiningElement(obj);
										}
									 }										
									);
			
			if (disp != null)
			{
				IEventPayload payload = disp.createPayload("OnRedefinedElementRemoved");
				disp.fireRedefinedElementRemoved(this, element, payload);
			}
		}
		else
		{
			//cancel the event
		}
		return 0;
	}

	/**
	 *
	 * Retrieves the collection of elements that this element is redefining.
	 *
	 * @param pVal[out] The collection of elements
	 *
	 * @return HRESULT
	 *
	 */
/*	public IRedefinableElement[] getRedefinedElements() {
      IRedefinableElement dummy = null;
      ArrayList<IRedefinableElement> values = retrieveElementCollection(dummy, "redefinedElement");
	  IRedefinableElement[] elems = null;
	  if (values != null)
	  {
		elems = new IRedefinableElement[values.size()];
		values.toArray(elems);
	  }
      return elems;
	}
*/
	public ETList<IRedefinableElement> getRedefinedElements() 
	{
		IRedefinableElement dummy = null;
		return retrieveElementCollectionWithAttrIDs(dummy, "redefinedElement", IRedefinableElement.class);		  
	}
	/**
	 *
	 * Adds the element that is redefining this element.
	 *
	 * @param element[in] The element
	 *
	 * @return HRESULT
	 *
	 */
	public long addRedefiningElement(IRedefinableElement element) 
	{
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		IElementChangeEventDispatcher disp = (IElementChangeEventDispatcher) ret.getDispatcher(EventDispatchNameKeeper.modifiedName());
		boolean proceed = true;
		if (disp != null)
		{
			IEventPayload payload = disp.createPayload("OnPreRedefiningElementAdded");
			proceed = disp.firePreRedefiningElementAdded(this, element, payload);
		}
			
		if (proceed)
		{
			final IRedefinableElement elem = element;
			addChildAndConnect(true, "redefiningElement", 
							   "redefiningElement", elem,
							   new IBackPointer<IRedefinableElement>() 
							   {
								  public void execute(IRedefinableElement obj) 
								  {
									elem.addRedefinedElement(obj);
								  }
							   }										
							 );
			
			if (disp != null)
			{
				IEventPayload payload = disp.createPayload("OnRedefiningElementAdded");
				disp.fireRedefiningElementAdded(this, element, payload);
			}
		}
		else
		{
			//cancel the event
		}
		return 0;
	}

	/**
	 *
	 * Removes the element that is redefining this element.
	 *
	 * @param element[in] The element to remove
	 *
	 * @return HRESULT
	 *
	 */
	public long removeRedefiningElement(IRedefinableElement element) {
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		IElementChangeEventDispatcher disp = (IElementChangeEventDispatcher) ret.getDispatcher(EventDispatchNameKeeper.modifiedName());
		boolean proceed = true;
		if (disp != null)
		{
			IEventPayload payload = disp.createPayload("OnPreRedefiningElementRemove");
			proceed = disp.firePreRedefiningElementRemoved(this, element, payload);
		}
			
		if (proceed)
		{
			final IRedefinableElement elem = element;
			new ElementConnector<IRedefinableElement>().removeByID
								   (
									this, elem, "redefiningElement",
									 new IBackPointer<IRedefinableElement>() 
									 {
										public void execute(IRedefinableElement obj) 
										{
										   elem.removeRedefiningElement(obj);
										}
									 }										
									);
			if (disp != null)
			{
				IEventPayload payload = disp.createPayload("OnRedefiningElementRemove");
				disp.fireRedefiningElementRemoved(this, element, payload);
			}
		}
		else
		{
			//cancel the event
		}
		return 0;
	}

	/**
	 *
	 * Retrieves the collection of elements that are redefining this element.
	 *
	 * @param pVal[out] The collection
	 *
	 * @return HRESULT
	 *
	 */
	public ETList<IRedefinableElement> getRedefiningElements() 
	{
		IRedefinableElement dummy = null;
		return retrieveElementCollectionWithAttrIDs(dummy, "redefiningElement", IRedefinableElement.class);
		

//		if (elems != null)
//		{
//			int count = elems.length;
//			retElems = new IRedefinableElement[count];
//			for (int i=0; i<count; i++)
//			{
//				retElems[i] = (IRedefinableElement)elems[i];
//			}
//		}
//		return retElems;
	}

	public long getRedefinedElementCount() {
		return UMLXMLManip.queryCount(m_Node, "redefinedElement", true);
	}

	public long getRedefiningElementCount() {
		return UMLXMLManip.queryCount(m_Node, "redefiningElement", true);
	}

	/**
	 *
	 * Determines whether or not this element is being redefined by another element(s)
	 *
	 * @param pVal[out] true if this element is being redefined
	 *
	 * @return HRESULT
	 *
	 */
	public boolean getIsRedefined() {
		boolean redef = false;
		long count = getRedefinedElementCount();
		if (count > 0)
		{
			redef = true;
		}
		return redef;
	}

	/**
	 *
	 * Determines whether or not this element is redefining anothera
	 *
	 * @param pVal[out] true if it is
	 *
	 * @return HRESULT
	 *
	 */
	public boolean getIsRedefining() {
		boolean redef = false;
		long count = getRedefiningElementCount();
		if (count > 0)
		{
			redef = true;
		}
		return redef;
	}

	public IVersionableElement performDuplication()
	{
		IVersionableElement dup = super.performDuplication();
		IRedefinableElement retEle = null;
		try {
			retEle = (IRedefinableElement)dup;
			performDuplicationProcess(retEle);
		}catch (Exception e)
		{}
		return retEle;
	}

	/**
	 *
	 * Performs the part of the duplication process specific to RedefinableElement
	 *
	 * @param dupElement[in] The duplicated element
	 *
	 * @return HRESULT
	 *
	 */
	private void performDuplicationProcess(IRedefinableElement ele)
	{
		replaceIds(ele, ele);
	}

}

