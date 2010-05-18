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


