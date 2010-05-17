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

import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Classifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;

/**
 * @author sumitabhk
 *
 */
/**
 * ElementConnector is a specialized class who focuses on the
 * connection between XML elements, both the making of that
 * connection and the breaking of that connection.
 */
public class ElementConnector <Type extends IVersionableElement>
{

	/**
	 *
	 */
	public ElementConnector() 
	{
		super();
	}

	/**
	 *
	 * Adds newElement to this element, establishing the back pointer
	 * connection between the two types.
	 *
	 * @param base[in] The BaseElement derived object whose child
	 *                 element we are removing.
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
        public <NewType extends IVersionableElement> void addChildAndConnect(
                                    Type base, 
                                    boolean byID,
				                    String featureName, 
                                    String query,
				                    NewType newType,
				                    IBackPointer<Type> backPointer)
	{
            ContactManager.addMemberAndConnect(byID, newType, base, query,featureName, backPointer);
	}

	/**
	 *
	 * Call this method when you need to remove an element
	 * in base by id, but then need to detach this 
	 * current element from the initial element to remove
	 * by value. For example, IClassifier::RemoveFromPowerTypeRange()
	 * takes an ID ( a BSTR ) as its sole parameter. That routine
	 * must remove the Generalization that has the matching ID
	 * from its collection. It simply needs to remove the IDREF from
	 * the XML attribute. The next thing it must do is detach the 
	 * IClassifier from the IGeneralization just removed. It must
	 * do that by setting the single element ( put_PowerType() )
	 * on the IGeneralization to 0.
	 *
	 * @param base[in] The BaseElement derived object we are affecting
	 * @param id[in] The id of the element to remove
	 * @param presenceQuery[in] An XPath query to perform to check
	 *                          to see if elToRemove is a child
	 *                          of this element.
	 * @param RemoveMember[in] The actual function pointer to fire on elToRemove 
	 *                   in order to remove the current element from elToRemove.
	 *
	 * @return HRESULTs
	 * @see RemoveElement()
	 */
	public void removeByID(
                IVersionableElement element,
                String xmiid, String presenceQuery,
                IBackPointer <Type> backPointer)
	{
            ContactManager contact = new ContactManager();
            contact.removeElementByID(xmiid, element, presenceQuery, backPointer);
        }

	/**
	 * @param element
	 * @param elem
	 * @param query
	 */
	public void removeElement(
                IVersionableElement base,
                IVersionableElement elemToEl,
                String query,
                IBackPointer <Type> backPointer)
        {
            // ContactManager< RemoveType, Type > contact;
            ContactManager contact = new ContactManager();
            contact.removeElement(false,elemToEl, base, query, backPointer);
        }

	/**
	 * @param element
	 * @param newEle
	 * @param attrName
	 * @return
	 */
	public <NewType extends IVersionableElement>
			 IElement setSingleElementAndConnect(
                                Type element,
							    NewType newEle, 
                                String attrName,
							    IBackPointer<NewType> addBackPointer,
							    IBackPointer<NewType> removeBackPointer) 
	{
        return ContactManager.setSingleElementAndConnect(newEle, 
                                            element, 
                                            attrName, 
                                            addBackPointer, 
                                            removeBackPointer);
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
	 * @param base[in] The BaseElement derived object we are affecting
	 * @param elToRemove[in] The element who's id will be removed
	 * @param presenceQuery[in] An XPath query to perform to check
	 *                          to see if elToRemove is a child
	 *                          of this element.
	 * @param RemoveMember[in] The function to fire in order to remove
	 *                   the current element from elToRemove.
	 *
	 * @return HRESULTs
	 * @see RemoveElement()
	 */
	public <RemoveType extends IVersionableElement> 
		void removeByID(IVersionableElement base, RemoveType elToRemove, 
								  String presenceQuery, 
								  IBackPointer<Type> backPointer)
	{
		new ContactManager().removeElement(true, elToRemove, base, presenceQuery,
											backPointer);
	}

	public <InsertType extends IVersionableElement> void
		insertChildBeforeAndConnect(
			Type parent, boolean byID, String featureName, String query,
			InsertType newChild, InsertType referenceElement,
			IBackPointer<Type> connectNewMember)
	{
		new ContactManager().insertMemberBeforeAndConnect(
				byID, newChild, referenceElement, parent, query, featureName,
				connectNewMember);
	}
}



