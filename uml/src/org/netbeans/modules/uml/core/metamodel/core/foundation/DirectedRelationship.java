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

import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author sumitabhk
 *
 */
public class DirectedRelationship extends Relationship implements IDirectedRelationship{
	/**
	 * Adds a target element to this relationship.
	 *
	 * @param element[in]
	 *
	 * @results S_OK, else EFR_S_EVENT_CANCELLED if the pre relationship event is
	 *          cancelled by a listener. S_FALSE will be returned if element is already
	 *          a target of this DirectedRelationship.
	 */
  public void addTarget(IElement elem)
  {
  	boolean isPresent = false;
	org.dom4j.Element domElem = getElementNode();
  	isPresent = ContactManager.isElementPresent(domElem, elem, "target", true);
  	if (!isPresent)
  	{
  		RelationshipEventsHelper help = new RelationshipEventsHelper(this);
  		if (help.firePreEndAdd(null, elem))
  		{
  			addElementByID(elem, "target");
  			help.fireEndAdded();
  		}
  	}
  }

  /**
   * Removes a target element from this relationship.
   *
   * @param element[in]
   *
   * @results HRESULT
   */
  public void removeTarget(IElement elem)
  {
	RelationshipEventsHelper help = new RelationshipEventsHelper(this);
	if (help.firePreEndRemoved(null, elem))
	{
		removeElementByID(elem, "target");
		help.fireEndRemoved();
	}
  }

//			   Retrieves the collection of target elements on this relationship.
//	   HRESULT Targets([out, retval] IElements** pVal);
  public ETList<IElement> getTargets()
  {
     return new ElementCollector< IElement >().
         retrieveElementCollectionWithAttrIDs(this, "target", IElement.class);
  }

  /**
   * Adds a source element to this relationship.
   *
   * @param element[in]
   *
   * @results HRESULT
   */
  public void addSource(IElement elem)
  {
	RelationshipEventsHelper help = new RelationshipEventsHelper(this);
	if (help.firePreEndAdd(elem, null))
	{
		addElementByID(elem, "source");
		help.fireEndAdded();
	}
  }

  /**
   * Removes a target element from this relationship.
   *
   * @param element[in]
   *
   * @results HRESULT
   */
  public void removeSource(IElement elem)
  {
	RelationshipEventsHelper help = new RelationshipEventsHelper(this);
	if (help.firePreEndRemoved(elem, null))
	{
		removeElementByID(elem, "source");
		help.fireEndRemoved();
	}
  }

//			   Retrieves the collection of source elements on this relationship.
//	   HRESULT Sources([out, retval] IElements** pVal);
  public ETList<IElement> getSources()
  {
     return new ElementCollector< IElement >().
         retrieveElementCollectionWithAttrIDs(this, "source", IElement.class);
  }

  /**
   * Description.
   *
   * @param pVal[out]
   *
   * @results HRESULT
   */
  public long getTargetCount()
  {
  	return UMLXMLManip.queryCount(m_Node, "target", true);
  }

  /**
   * Description.
   *
   * @param pVal[out]
   *
   * @results HRESULT
   */
  public long getSourceCount()
  {
	return UMLXMLManip.queryCount(m_Node, "source", true);
  }

  /**
   *
   * Called after this element has been fully deleted.
   *
   * @param ver[in] The COM object that represents this relationship.
   *
   * @return HRESULT
   *
   */
  public void fireDelete(IVersionableElement elem)
  {
	  super.fireDelete(elem);
	  if (elem instanceof IElement)
	  {
	  	IElement element = (IElement)elem;
		RelationshipEventsHelper help = new RelationshipEventsHelper(element);
		help.fireRelationDeleted();
	  }
  }

  /**
   *
   * Called when this relationship is about to be deleted
   *
   * @param ver[in] The COM object representing this relationship
   *
   * @return - true if the deletion of this element should occur, else
   *         - false if not
   *
   */
  public boolean firePreDelete(IVersionableElement elem)
  {
	  boolean proceed = super.firePreDelete(elem);
	  if (proceed && elem instanceof IElement)
	  {
		IElement element = (IElement)elem;
		RelationshipEventsHelper help = new RelationshipEventsHelper(element);
		proceed = help.firePreRelationDeleted();
	  }
	  return proceed;
  }

  /**
   *
   * Returns all the elements this relationship relates
   *
   * @param elements[out] The elements participating in this relationship
   *
   * @return HRESULT
   *
   */
  public ETList<IElement> getRelatedElements()
  {
	 return null;
  }

}


