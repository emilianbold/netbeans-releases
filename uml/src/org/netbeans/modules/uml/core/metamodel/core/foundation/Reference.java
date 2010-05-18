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

