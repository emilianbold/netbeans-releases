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

import java.util.Vector;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.IDiagramCleanupManager;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IDataType;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;

/**
 * @author sumitabhk
 *
 */
public class Namespace extends NamedElement implements INamespace{

	/// The NotifiableMetaTypes are a list of types that are preference driven that
	/// whose Delete() method should be called when their enclosing Namespace
	/// is being deleted
	private static Vector < String > s_MetaTypes = new Vector < String >();

	/**
	 *
	 * Adds an element to this Namespace.
	 *
	 * @param element[in] The element this Namespace will own
	 *
	 * @return HRESULT
	 *
	 */
	public boolean addOwnedElement(INamedElement elem) {
      boolean retVal = false;
      
		IElementChangeDispatchHelper helper = new ElementChangeDispatchHelper();
		INamespace curSpace = elem.getNamespace();
		
		ITransitionElement trans = null;
		boolean bIsSame = false;
		if (elem instanceof ITransitionElement)
		{
			trans = (ITransitionElement)elem;
		}
		else
		{
			// We only want to check to see if we have the 
			// same namespaces IF the element is not a Transition
			// element
			bIsSame = isSame(curSpace);
		}
		
		if (!bIsSame)
		{
			boolean proceed = true;
			proceed = helper.dispatchPreElementAddedToNamespace(this, elem);
			
			if (proceed)
			{
				boolean success = true;
            
				// The element being moved could contain diagrams.  So we need to 
				// make sure to move the diagrams if the toplevel project for the element
				// differs from this namespace
				IProject elemProj = elem.getProject();
				if (elemProj != null)
				{
					IProject curProj = getProject();
					if (curProj != null)
					{
						boolean projectIsSame = true;
						projectIsSame = curProj.isSame(elemProj);
						if (!projectIsSame)
						{
							success = performDependentElementMovement(elemProj, curProj, elem);
						}
					}
				}
				
				// PerformDependentElementMovement can return EFR_S_EVENT_CANCELLED if the
				// files could not be copied (ie ran out of disk space)
				if (success)
				{
					// Need to make sure that the current Namespace of element is properly
					// set to a dirty state
					if (curSpace != null)
					{
						curSpace.setDirty(true);
					}
					addElement(elem);
					helper.dispatchElementAddedToNamespace(this, elem);
                                        
                                        if (elem instanceof NamedElement) 
                                        {
                                            ((NamedElement)elem).moveAssociatedElements(this);
                                        }
				}
            
            retVal = true;
			}
			else
			{
				//cancel the event
			}
		}
		else // #6323878, fire event so the the item will be added to project tree
		{
			retVal = true;
			helper.dispatchElementAddedToNamespace(this, elem);
		}
      
      return retVal;
	}

	/**
	 * The element being moved could contain diagrams.  So we need to 
	 * make sure to move the diagrams if the toplevel project for the element
	 * differs from this namespace
	 *
	 * @param pFromProject [in] The old project
	 * @param pToProject [in] The project where pElementBeingMoved is being moved into
	 * @param pElementBeingMoved [in] The element being moved
	 */
	private boolean performDependentElementMovement(IProject fromProj, 
									IProject toProj, INamedElement elemMoved) 
	{
		boolean moved = false;
		try {
			ICoreProduct prod = ProductRetriever.retrieveProduct();
			if (prod != null)
			{
				IDiagramCleanupManager man = prod.getDiagramCleanupManager();
				if (man != null)
				{
					man.moveOwnedAndNestedDiagrams(fromProj, toProj, elemMoved);
				}
				moved = true;
			}
		}catch (Exception e)
		{}
		return moved;
	}

	/**
	 *
	 * This deletes the diagrams from this namespace, if there are any.
	 *
	 * @param thisElement[in] This Namespace that is being deleted.
	 *
	 * @return HRESULT
	 *
	 */
	protected void performDependentElementCleanup(IVersionableElement thisElement) 
	{
		try {
			ICoreProduct prod = ProductRetriever.retrieveProduct();
			if (prod != null)
			{
				cleanupPresentationElements();
				IDiagramCleanupManager man = prod.getDiagramCleanupManager();
				if (man != null)
				{
					man.cleanupOwnedAndNestedDiagrams(thisElement);
				}
				deleteNotifiableElements();
			}
		}catch (Exception e)
		{}
	}

	/**
	 *
	 * Looks for any owner of presentation element. We know we have a presentation
	 * element as the removeOnSave xml attribute will be present
	 *
	 * @return HRESULT
	 *
	 */
	private void cleanupPresentationElements() {
		Node curNode = getNode();

		if( curNode != null)
		{
		   notifyOwnedElements( ".//*[@removeOnSave]/ancestor::*[2]" );
		}
	}

	/**
	 *
	 * Calls delete on any owned elements of a particular type. The types notifiable during a delete
	 * of this Namespace is preference driven.
	 *
	 * @return HRESULT
	 *
	 */
	private void deleteNotifiableElements() {

            //kris richards - removed call to deleted method buildNotifyList. (pref issue; see UMLOptionsDialog wiki)
		String query = "UML:Element.ownedElement/";
		if (s_MetaTypes.size() > 0)
		{
			for (int i=0; i<s_MetaTypes.size(); i++)
			{
				String actualQuery = query + s_MetaTypes.elementAt(i);
				notifyOwnedElements(actualQuery);
			}
		}
		
	}

	/**
	 *
	 * Queries the current namespace for a particular element type. If that
	 * type is found, Delete() is called on it
	 *
	 * @param actualQuery[in] The query to perform
	 *
	 * @return HRESULT
	 *
	 */
	private void notifyOwnedElements(String actualQuery) {
		if (actualQuery.length() > 0)
		{
			Node curNode = getNode();
			if (curNode != null)
			{
	            ElementCollector < IElement > col = new ElementCollector < IElement >();
	            ETList<IElement> values = col.retrieveElementCollection(curNode, actualQuery, IElement.class);
				if (values != null)
				{
					int count = values.size();
					for (int i=0; i<count; i++)
					{
						IElement actualEle = values.get(i);
						actualEle.delete();
					}
				}
			}
		}
	}

	/**
	 *
	 * Makes sure that the internal static list of notifiable elements is established
	 *
	 * @return HRESULT
	 *
	 */

	/**
	 *
	 * Removes the passed-in element from this Namespace.
	 *
	 * @param element[in] The element to remove
	 *
	 * @return HRESULT
	 *
	 */
	public void removeOwnedElement(INamedElement elem) {
		removeElement(elem);
	}

	/**
	 *
	 * Retrieves all the elements this Namespace directly owns.
	 *
	 * @param namedElements[in] The owned elements
	 *
	 * @return HRESULT
	 *
	 */
	public ETList<INamedElement> getOwnedElements() {
		ETList<INamedElement> retElems = null;
		ETList<IElement> elems = getElements();
		if (elems != null)
		{
			int count = elems.size();
			retElems = new ETArrayList<INamedElement>();
			for (int i = 0; i < count; i++)
            {
                IElement el = elems.get(i);
                if (el instanceof INamedElement)
                    retElems.add((INamedElement) el);
            }
		}
		return retElems;
	}

	/**
	 *
	 * Adds a member that is visible within this Namespace. This 
	 * element can either be owned or imported into this Namespace.
	 *
	 * @param member[in] The element
	 *
	 * @return HRESULT
	 *
	 */
	public void addVisibleMember(INamedElement elem) {
		addElementByID(elem, "member");
	}

	/**
	 *
	 * Removes the passed-in element from the member list of this Namespace.
	 *
	 * @param member[in] The element to remove
	 *
	 * @return HRESULT
	 *
	 */
	public void removeVisibleMember(INamedElement elem) {
		removeElementByID(elem, "member");
	}

	/**
	 *
	 * Retrieves all the elements of the member list of this Namespace.
	 *
	 * @param members[in] The collection of elements
	 *
	 * @return HRESULT
	 *
	 */
	public ETList<INamedElement> getVisibleMembers() 
	{
		INamedElement dummy = null;
		return retrieveElementCollection(dummy, "member", INamedElement.class);	
	}

	/**
	 *
	 * Retrieves all the elements contained by this Namespace by name. The name should NOT be
	 * fully qualified.
	 *
	 * @param elementName[in] The name of the element(s) to retrieve
	 * @param elements[out] The collection of found elements
	 *
	 * @return HRESULT
	 * @warning If the returned collection has more than one element in it, the model
	 *          is considered mal-formed.
	 *
	 */
	public ETList<INamedElement> getOwnedElementsByName(String name) 
	{
		return super.getOwnedElementsByName(name);
	}

	/**
	 * Description
	 *
	 * @param pVal[out]
	 *
	 * @result HRESULT
	 */
	public long getOwnedElementCount() {
		return getElementCount();
	}

	public long getVisibleMemberCount() {
		return UMLXMLManip.queryCount(m_Node, "member", true);
	}

	/**
	 *
	 * Makes sure that any Namespace created has the name set to the default.
	 *
	 * @param node[in] the node behind this COM object.
	 *
	 * @return HRESULTs
	 *
	 */
	public void establishNodeAttributes(org.dom4j.Element elem) {
		super.establishNodeAttributes(elem);
		establishDefaultName();
	}

	/** 
	 * Creates a package structure within this namespace
	 * 
	 * @param packageStructure[in] a "::" delimited package path
	 * @param ppMostNestedPackage[out] the most deeply nested package
	 */
	public IPackage createPackageStructure( String packageStructure )
	{
		return createPackageStructureInNamespace(packageStructure, this);
	}
	
	/** 
	 * Creates a package structure within a namespace
	 * 
	 * @param packageStructure[in] the "::" delimited package path to be created
	 * @param pNamespace[in] the namespace to create the package(s) in
	 * @param ppMostNestedPackage[out] the most deeply nested package.
	 */
	protected IPackage createPackageStructureInNamespace(String packageStructure, INamespace pNamespace)
	{
		IPackage retPack = null;
		String delimiter = "::";
		int pos = packageStructure.indexOf(delimiter);
		if (pos >= 0)
		{
			String packToCreate = packageStructure.substring(0, pos);
			IPackage pack = ensurePackageExists(pNamespace, packToCreate);
			if (pack != null)
			{
				// See if we need to recurse
				if (pos != packageStructure.length())
				{
					// we need to create more packages.
					String subPack = packageStructure.substring(pos + delimiter.length());

					// Recurse
					retPack = createPackageStructureInNamespace(subPack, pack);
				}
				else
				{
					retPack = pack;
				}
			}
		}
		else
		{
			retPack = ensurePackageExists(pNamespace, packageStructure);
		}
		return retPack;
	}
	
	/** 
	 * If the package named @a name already exists, it is returned.
	 * Otherwise, it is created.
	 * 
	 * @param pNamespace[in] the namespace to look in
	 * @param name[in] the name of the package to find/create
	 * @param ppPackage[out] the found/created package
	 * 
	 * @return HRESULT
	 */
	private IPackage ensurePackageExists( INamespace pNamespace, String name)
	{
		IPackage retPack = findPackage(pNamespace, name);
		if (retPack == null)
		{
			// We didn't find the package so create it.
			TypedFactoryRetriever<IPackage> ret = new TypedFactoryRetriever<IPackage>();
			retPack = ret.createType("Package");
			pNamespace.addOwnedElement(retPack);
			retPack.setName(name);
		}
		return retPack;
	}

	private IPackage findPackage(INamespace pNamespace, String name)
	{
		IPackage retPack = null;
		IElementLocator locator = new ElementLocator();
		String fullyQualifiedName = pNamespace.getFullyQualifiedName(true);
		fullyQualifiedName += "::" + name;
		ETList<IElement> pElements = locator.findScopedElements(pNamespace, fullyQualifiedName);
		if (pElements != null)
		{
			for (int i=0; i<pElements.size(); i++)
			{
				IElement pEle = pElements.get(i);
				if (pEle instanceof IPackage)
				{
					retPack = (IPackage)pEle;
					break;
				}
			}
		}
		return retPack;
	}
	
	protected boolean checkForNameCollision( String newName, IElementChangeDispatchHelper helper, INamedElement curElement )
	{
	   return firePreNameCollisionIfNeeded( helper, curElement, newName );
	}
	
	protected void checkForNameCollision( IElementChangeDispatchHelper helper, INamedElement curElement )
	{
	   fireNameCollisionIfNeeded( helper, curElement );
	}
}


