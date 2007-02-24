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

import org.dom4j.Document;
import org.dom4j.Node;

/**
 * ReferenceImpl implementation the IReference meta type.
 *
 * This relationship allows any two elements to arbitrarily be
 * coupled to one another.
 */
public class Reference extends DirectedRelationship implements IReference{

	/**
	 *
	 */
	public Reference() {
		super();
	}

	/**
	 *
	 * Retrieves the element that is the source of this relationship.
	 *
	 * @param element[out] The element
	 *
	 * @return HRESULT
	 *
	 */
	public IElement getReferencingElement() 
   {
      ElementCollector< IElement > col = new ElementCollector< IElement >();
		return col.retrieveSingleElementWithAttrID(this, "source", IElement.class);
	}

	/**
	 *
	 * Sets the source element on this relationship.
	 *
	 * @param element[in] The element
	 *
	 * @return HRESULT
	 *
	 */
	public void setReferencingElement(IElement elem) {
        PreventElementReEntrance reEnt = new PreventElementReEntrance(this);
		try {
                
            if (!reEnt.isBlocking())
            {
				RelationshipEventsHelper helper = new RelationshipEventsHelper(this);
				if (helper.firePreEndModified("source", elem, null))
				{
					final IElement element = elem;
					new ElementConnector<IReference>().setSingleElementAndConnect
									(
										this, element, 
										"source",
										 new IBackPointer<IElement>() 
										 {
											 public void execute(IElement obj) 
											 {
												obj.addReferencingReference(Reference.this);
											 }
										 },
										 new IBackPointer<IElement>() 
										 {
										 	 public void execute(IElement obj) 
											 {
											    obj.removeReferencingReference(Reference.this);
											 }
										 }										
									);
					helper.fireEndModified();
				}
				else
				{
					//cancel event
				}
            }
		}
        catch (Exception e)
		{
        }
		finally 
        {
			reEnt.releaseBlock();
		}
	}

	/**
	 *
	 * Retrieves the element that is the target of this relationship.
	 *
	 * @param element[out] The element
	 *
	 * @return HRESULT
	 *
	 */
	public IElement getReferredElement() 
   {
      ElementCollector< IElement > col = new ElementCollector< IElement >();
		return col.retrieveSingleElementWithAttrID(this, "target", IElement.class);
	}

	/**
	 *
	 * Sets the element that will be the target of this relationship.
	 *
	 * @param element[in] The element
	 *
	 * @return HRESULT
	 *
	 */
	public void setReferredElement(IElement elem) 
	{
		PreventElementReEntrance reEnt = new PreventElementReEntrance(this);
		try 
		{
			if (!reEnt.isBlocking())
			{
				RelationshipEventsHelper helper = new RelationshipEventsHelper(this);
				if (helper.firePreEndModified("target", null, elem))
				{
					final IElement element = elem;
					new ElementConnector<IReference>().setSingleElementAndConnect
									(
										this, element, 
										"target",
										 new IBackPointer<IElement>() 
										 {
											 public void execute(IElement obj) 
											 {
												obj.addReferredReference(Reference.this);
											 }
										 },
										 new IBackPointer<IElement>() 
										 {
											 public void execute(IElement obj) 
											 {
												obj.removeReferredReference(Reference.this);
											 }
										 }										
									);
					helper.fireEndModified();
				}
				else
				{
					//cancel event
				}
			}
		}catch (Exception e)
		{}
		finally {
			reEnt.releaseBlock();
		}
	}

	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node
	 *
	 * @return HRESULT
	 */
	public void establishNodePresence( Document doc, Node parent )
	{
	   buildNodePresence( "UML:Reference", doc, parent );
	}

}

