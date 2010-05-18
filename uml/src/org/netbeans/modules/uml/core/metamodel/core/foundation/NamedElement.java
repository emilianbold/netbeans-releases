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
import org.netbeans.modules.uml.common.Util;
import org.netbeans.modules.uml.common.generics.ETTripleT;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.eventframework.EventBlocker;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.preferenceframework.IPreferenceAccessor;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceAccessor;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.typemanagement.IPickListManager;
import org.netbeans.modules.uml.core.typemanagement.ITypeManager;
import org.openide.util.NbPreferences;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class NamedElement extends Element implements INamedElement{ 
	private Node n = null;

	public String getName() {
		return getAttributeValue("name");
	}

	public void setName(String str) 
	{
		if (str == null || str.length()==0)
		{
			return;
		}
		String newName = str;
		String simpleName = NameResolver.getSimpleName(newName);
		ETTripleT<Boolean, String, Boolean> result = doNamesDiffer(simpleName);
		boolean differ = ((Boolean)result.getParamOne()).booleanValue();
		boolean fireModEvent = ((Boolean)result.getParamThree()).booleanValue();
		if (differ)
		{
			boolean proceed = true;
			IElementChangeDispatchHelper helper = new ElementChangeDispatchHelper();
			if (fireModEvent)
			{
				proceed = firePreNameChanged(str, helper, this);
			}
			if (proceed)
			{
				setNewNameValue(simpleName, fireModEvent, helper, this);
				// Let the PickListManager know about the name change. The PickListManager
				// IS listening to name modified events, but it is important that we
				// directly tell the PickListManager about this change, 'cause if the events
				// are blocked, the PickListManager still needs to know about this change.
				IProject proj = getProject();
				if (proj != null)
				{
					ITypeManager typeMan = proj.getTypeManager();
					if (typeMan != null)
					{
						IPickListManager pickMan = typeMan.getPickListManager();
						// It is possible for this to be null. Most notably
						// when the Project itself is being created and its
						// name is being set.
						if (pickMan != null)
						{
							pickMan.addNamedType(this);
						}
					}
				}
			}
		}
		/*
			int count = 0;
			ETList<INamedElement> elems = null;
			
			if(getNamespace() !=null )
			{
				
				elems = getNamespace().getOwnedElementsByName(str);
			}
			if(elems != null)
				count = elems.getCount();
			if(count>0)
			{
				for (int i = 0; i < count; i++) 
				{
					if (elems.item(i) instanceof IClassifier && this instanceof IClassifier)
					{
						int n = 10;
						IQuestionDialog pQuestionDialog = new SwingQuestionDialogImpl();
						String title = "Element Already Exists in Namespace";
						String msg = "An element with this name already exists in the current namespace. Do you want to create a new e"+
						"lement with the same name as the existing element?\nIf you answer Yes, problems with source code generation will be encounter"+
						"ed, thus this choice is not supported with code generation features.";
						QuestionResponse dresult = pQuestionDialog.displaySimpleQuestionDialog(SimpleQuestionDialogKind.SQDK_YESNO, 
								MessageIconKindEnum.EDIK_ICONWARNING, 
								msg,
								SimpleQuestionDialogResultKind.SQDRK_RESULT_YES,
								null,
								title);
						if (dresult.getResult() == SimpleQuestionDialogResultKind.SQDRK_RESULT_YES)
						{
						
						}
						if (dresult.getResult() == SimpleQuestionDialogResultKind.SQDRK_RESULT_NO)
						{
							proceed = false;
						}
						
					}
					break;
				}
			}
			
		}
		*/
	}

	/**
	 *
	 * Sets the name value of this element
	 *
	 * @param newName[in]      The new value for the name attribute
	 * @param fireModEvent[in] true if the element modified event should be fired as 
	 *                         as result of setting the name, else false to not
	 *                         trigger the event.
	 *                         
	 * @return HRESULT
	 *
	 */
	protected void setNewNameValue(String newName, boolean fireModEvent, IElementChangeDispatchHelper helper, INamedElement element) 
   {
      setNewNameValue(newName, fireModEvent);
      if(fireModEvent == true)
      {
         helper.dispatchNameModified(element);
         checkForNameCollision(helper, element);
      }
	}

   /**
    * Sets the name value of this element
    *
    * @param newName      The new value for the name attribute
    * @param fireModEvent true if the element modified event should be fired as 
    *                     as result of setting the name, else false to not
    *                     trigger the event.
    */
   protected void setNewNameValue(String newName, boolean fireModEvent) 
   {
   	String actual = NameResolver.getSimpleName(newName);
      if (fireModEvent)
      {
         setAttributeValue("name", actual);
      }
      else
      {
         // This will not cause the lower level element modified to fire
         XMLManip.setAttributeValue(m_Node, "name", actual);
      }
   }
   
	/**
	 *
	 * Fires the PreNameChange event, returning the dispatch helper, the COM representation
	 * of this element, and whether or not the post event should be fired.
	 *
	 * @param newName[in]      The proposed new name value
	 * @param helper[out]      The dispatch helper
	 * @param curElement[out]  The COM object representing this element
	 *
	 * @return true if the post should be fired, else false
	 *
	 */
	private boolean firePreNameChanged(String str, IElementChangeDispatchHelper helper, INamedElement element) {
		boolean fire = true;
		// Only want to do all the events and setting of data
		// if the pre and post values are different...
		fire = checkForNameCollision(str, helper, element);
		if (fire)
		{
			//helper = new ElementChangeDispatchHelper();
			boolean proceed = helper.dispatchPreNameModified(element, str);
			fire = proceed ? true : false;
		}
		return fire;
	}

	/**
	 *
	 * Dispatches the PreNameCollision event IF a name collision is found
	 *
	 * @param helper[in]       The actual dispatch helper that will fire the event
	 * @param curElement[in]   The element about to be renamed
	 * @param newName[in]      The proposed name
	 *
	 * @return true if dispatching should proceed, else false if a listener cancelled.
	 *
	 */
	protected boolean firePreNameCollisionIfNeeded(IElementChangeDispatchHelper helper, 
									INamedElement curElement,
									String newName) 
	{
		boolean proceed = true;
		ETList<INamedElement> elems = Util.getCollidingElements(curElement, newName);

		// Make sure we are't just firing an empty list
		if (elems != null && elems.size() > 0)
		{
			helper.setCollidingElements(elems);
			proceed = helper.dispatchPreNameCollision(curElement, newName);
		}
		return proceed;
	}

	/**
	 *
	 * Retrieves all elements in the namespace that curElement is in that have a name that matches newName.
	 * This is also smart about only retrieving colliding elements of the same type as curElement.
	 *
	 * @param curElement[in]         The element about to be renamed
	 * @param newName[in]            The proposed name
	 * @param collidingElements[out] The collection of elements that match newName and the type of curElement
	 *
	 * @return HRESULT
	 *
	 */
//	private ETList<INamedElement> getCollidingElements(INamedElement curElement, String newName)
//	{		
//		ETList<INamedElement> collidingElements = new ETArrayList<INamedElement>();
//		if( newName != null && newName.length() > 0 )
//		{
//         ElementLocator locator = new ElementLocator();
//         ETList<IElement> foundElements = locator.findScopedElements(this, newName);
//			//ETList<INamedElement> foundElements = NameResolver.resolveFullyQualifiedNames( this, newName );
//			INamespace spaceToSearch = null;
//			if (curElement instanceof INamespace)
//			{
//				spaceToSearch = (INamespace)curElement;
//			}
//			INamespace curNamespace = getNamespace();
//			if( curNamespace != null )
//			{
//				spaceToSearch = curNamespace;
//			}
//			if( spaceToSearch != null)
//			{
//				Node spaceNode = spaceToSearch.getNode();
//				if( spaceNode != null )
//				{   
//					String query = "UML:Element.ownedElement/*[@name=\"";
//					query += NameResolver.getSimpleName( newName );
//					ETList<String> collisionNames = getCollidingNamesForElement(curElement);
//					if(( foundElements != null ) && (foundElements.size() > 0))
//					{
//						int num = foundElements.getCount();
//						for( int x = 0; x < num; x++ )
//						{
//							//INamedElement namedElement = foundElements.get(x);
//                     IElement namedElement = foundElements.get(x);
//							if( namedElement != null )
//							{
//								Node node = namedElement.getNode();
//								if( node != null )
//								{
//									String nodeName = node.getName();
//									if( nodeName != null && nodeName.length() > 0)
//									{
//										nodeName = "UML:"+nodeName;
//										int cnt = collisionNames.size();
//										for(int y = 0; y < cnt; y++ )
//										{
//											String name = collisionNames.get(y);
//											if( name != null && name.length() > 0)
//											{
//												if( name.equals(nodeName))
//												{
//													collidingElements.add( (INamedElement)namedElement );
//													break;
//												}
//											}
//										}
//									}
//								}
//							}
//						}
//					}
//					else
//					{
//						if (collisionNames != null)
//						{
//							query += "\" and (";
//							int cnt = collisionNames.size();
//							for(int y = 0; y < cnt; y++ )
//							{
//								if (y == 0)
//								{
//									query += " name(.) = \"";
//								}
//								else
//								{
//									query += "\" or name(.) = \"";
//								}
//								query += collisionNames.get(y);
//							}
//							query += "\" )]";
//							ElementCollector<INamedElement> collector = new ElementCollector<INamedElement>();
//							collidingElements = collector.retrieveElementCollection(spaceNode,query, INamedElement.class);
//						}
//					}
//				}
//			}
//		}
//		return collidingElements;
//	}
//	public ETList<String> getCollidingNamesForElement(INamedElement curElement)
//	{
//		ETList<String> retVal = new ETArrayList<String>();
//		if(curElement != null)
//		{
//			Node curNode = curElement.getNode();
//			if( curNode != null )
//			{
//				String nodeName = curNode.getName();
//				if(nodeName != null && nodeName.length() > 0)
//				{
//					String s = "UML:" + nodeName;
//					retVal.add(s);
//				}
//			}
//		}
//		return retVal;
//	}

	/**
	 *
	 * Dispatches the NameCollision event if there are colliding elements on the helper
	 * object passed in.
	 *
	 * @param helper[in]       The helper object that may or may not contain colliding elements
	 * @param curElement[in]   The element whose name now collides with existing elements
	 *
	 */
	protected void fireNameCollisionIfNeeded(IElementChangeDispatchHelper helper, 
									INamedElement curElement) 
	{
		ETList<INamedElement> elems = helper.getCollidingElements();
		if (elems != null)
		{
			helper.dispatchNameCollision(curElement);
		}
	}

	/**
	 *
	 * This method is designed to be overridden in sub classes that care when a name collision is about to
	 * occur / has occurred.
	 *
	 * @param newName[in]      The proposed name change
	 * @param helper[in]       The helper object to dispatch the events
	 * @param curElement[in]   The current element about to be named
	 *
	 * @return true if it is ok to proceed with event dispatching, else false
	 *
	 */
	protected boolean checkForNameCollision(String str, IElementChangeDispatchHelper helper, INamedElement element) {
		return true;
	}

   /**
    *
    * This method is designed to be overridden in sub classes that care when a name collision is about to
    * occur / has occurred.
    *
    * @param helper[in]       The helper object to dispatch the events
    * @param curElement[in]   The current element about to be named
    *
    * @return true if it is ok to proceed with event dispatching, else false
    *
    */
   protected void checkForNameCollision(IElementChangeDispatchHelper helper, INamedElement element) {
      
   }
   
	/**
	 *
	 * Simply determines if the value passed in matches the current name of this element.
	 *
	 * @param newName[in,out]        The new value to check against. Always use the value
	 *                               of this variable upon return from this call, as this
	 *                               method may need to change its value.
	 * @param fireChangeEvent[out]   true if the name change event should be fired, else
	 *                               false if it should not.
	 *
	 * @return true if the current name is different than newName, else
	 *         false if they are the same.
	 *
	 */
	public ETTripleT<Boolean, String, Boolean> doNamesDiffer(String str) {
		boolean differ = true;
		String name = (str != null) ? str : "";
		boolean fireEvent = true;
		String curName = getName();
		if (curName != null && curName.length() > 0 && curName.equals(str))
		{
			differ = false;
			fireEvent = false;
		}
		if (differ)
		{
			String defaultName = retrieveDefaultName();
			if( (curName == null || curName.length() == 0) && ( defaultName.equals(str )) )
			{
			   fireEvent = false;
			}
			else if( (str.length() == 0) && (defaultName.length() > 0) )
			{
			   // setting name to blank gets the default name instead
			   str = defaultName;
			   ETTripleT<Boolean, String, Boolean> result = doNamesDiffer( str );
			   differ = ((Boolean)result.getParamOne()).booleanValue();
			   name = result.getParamTwo();
			   fireEvent = ((Boolean)result.getParamThree()).booleanValue();
			}
		}
		return new ETTripleT<Boolean, String, Boolean>(Boolean.valueOf(differ), name, Boolean.valueOf(fireEvent));
	}

	/**
	 *
	 * Retrieves the default name for use for model elements that are just
	 * being created.
	 *
	 * @return the default name of the model element
	 *
	 */
	protected String retrieveDefaultName() {
		String name = "";
		IPreferenceAccessor pref = PreferenceAccessor.instance();
		name = pref.getDefaultElementName();
		return name;
	}

	/**
	 *
	 * Retrieves the visibility of the NamedElement within the owning Namespace.
	 *
	 * @param name[out] The visibility
	 *
	 * @return HRESULT
	 *
	 */
	public int getVisibility() {
		return getVisibilityKindValue( "visibility" );
	}

	/**
	 *
	 * Sets the visibility of the NamedElement within the owning Namespace.
	 *
	 * @param name[in] The visibility.
	 *
	 * @return HRESULT
	 *
	 */
	public void setVisibility(int vis) {
		int curVal = getVisibility();

		// No need to set if the values are the same
		if (curVal != vis)
		{
			IElementChangeDispatchHelper helper = new ElementChangeDispatchHelper();
			boolean proceed = true;
			proceed = helper.dispatchPreVisibilityModified(this, vis);
			if (proceed)
			{
				setVisibilityKindValue("visibility", vis);
				helper.dispatchVisibilityModified(this);
			}
		}
	}

	/**
	 *
	 * Retrieves the immediate Namespace that owns this element.
	 *
	 * @param name[out] The Namespace
	 *
	 * @return HRESULT
	 *
	 */
	public INamespace getNamespace() {
		INamespace space = null;
		IElement owner = getOwner();
		if (owner instanceof INamespace)
		{
		   // Allow this to fail (don't throw) 'cause the owner may not be a namespace
		   space = (INamespace)owner;
		}
		return space;
	}

	/**
	 *
	 * Puts this element into the owned elements list of the passed in Namespace.
	 *
	 * @param name[in] The Namespace.
	 *
	 * @return HRESULT
	 * @warning This call will result in the removal of this element from a 
	 *          previously owning Namespace.
	 *
	 */
	public void setNamespace(INamespace space) {
        if (space != null)
            space.addOwnedElement(this);
	}

        /**
         * if there some associated elements that should be 
         * moved as well. 
         */ 
	public void moveAssociatedElements(INamespace space) {
        }

	/**
	 *
	 * Adds this element to the supplier end of a Dependency relationship.
	 *
	 * @param dep[in] The Dependency
	 *
	 * @return HRESULT
	 *
	 */
	public void addSupplierDependency(IDependency dependency) 
	{
		final IDependency dep = dependency;
		super.addChildAndConnect(true, "supplierDependency", 
								 "supplierDependency", dep,
								 new IBackPointer<INamedElement>() 
								 {
									 public void execute(INamedElement obj) 
									 {
										dep.setSupplier(obj);
									 }
								 }										
								);
	}

	/**
	 *
	 * Removes this element from the supplier end of the passed in Dependency.
	 *
	 * @param dep[in] The Dependency
	 *
	 * @return HRESULT
	 *
	 */
	public void removeSupplierDependency(final IDependency dep) 
   {
      INamedElement dummy = null;      
		removeByID(dep, "supplierDependency", new IBackPointer<INamedElement>()
		{
			public void execute(INamedElement el)
			{
				dep.setSupplier(el);
			}
		});
	}

	/**
	 *
	 * Retrieves all the Dependency relationships that this element is a supplier for.
	 *
	 * @param deps[out,retval] The collection of relationships
	 *
	 * @return HRESULT
	 *
	 */
	public ETList<IDependency> getSupplierDependencies() 
    {
       IDependency dummy = null;
	   return retrieveElementCollectionWithAttrIDs(dummy, "supplierDependency", IDependency.class);
	}

	/**
	 *
	 * Retrieves all the Dependency relationships that this element is a supplier for.
	 *
	 * @param sElementType[in] The type of dependency to get
	 * @param deps[out,retval] The collection of relationships
	 *
	 * @return HRESULT
	 *
	 */
	public ETList<IDependency> getSupplierDependenciesByType(String type)	
	{
		ETList<IDependency> foundVec = new ETArrayList<IDependency>();
		ETList<IDependency> deps = getSupplierDependencies();
		if (deps != null)
		{
			int count = deps.size();			
			for (int i=0; i<count; i++)
			{
				IDependency pThisDepends = deps.get(i);
				String sThisElementType = pThisDepends.getElementType();
				if (sThisElementType.equals(type))
				{
					foundVec.add(pThisDepends);
				}
			}
		}
		return foundVec;
	}

	/**
	 *
	 * Adds this element to the client end of a Dependency relationship.
	 *
	 * @param dep[in] The Dependency
	 *
	 * @return HRESULT
	 *
	 */
	public void addClientDependency(IDependency dependency) 
	{
		final IDependency dep = dependency;
		addChildAndConnect(true, "clientDependency", 
						 "clientDependency", dep,
						 new IBackPointer<INamedElement>() 
						 {
							 public void execute(INamedElement obj) 
							 {
								dep.setSupplier(obj);
							 }
						 }										
						);
	}

	/**
	 *
	 * Removes this element from the client end of the passed in Dependency.
	 *
	 * @param dep[in] The Dependency
	 *
	 * @return HRESULT
	 *
	 */
	public void removeClientDependency(final IDependency dep) 
    {
        new ElementConnector<INamedElement>().removeByID(this, dep.getXMIID(),
            "clientDependency",
            new IBackPointer<INamedElement>()
            {
                public void execute(INamedElement el)
                {
                    dep.setClient(el);
                }
            }
        );
//		removeByID(dep, "clientDependency", dummy, null);
	}

	/**
	 *
	 * Retrieves all the Dependency relationships that this element is a client for.
	 *
	 * @param deps[in] The collection of relationships
	 *
	 * @return HRESULT
	 *
	 */
	public ETList<IDependency> getClientDependencies() 
    {
        return retrieveElementCollectionWithAttrIDs((IDependency) null, 
            "clientDependency", IDependency.class);
	}

	/**
	 *
	 * Retrieves all the Dependency relationships that this element is a Client for.
	 *
	 * @param sElementType[in] The type of dependency to get
	 * @param deps[out,retval] The collection of relationships
	 *
	 * @return HRESULT
	 *
	 */
	public ETList<IDependency> getClientDependenciesByType(String type) {
		ETList<IDependency> foundVec = new ETArrayList<IDependency>();
		ETList<IDependency> deps = getClientDependencies();
		if (deps != null)
		{
			int count = deps.size();		
			for (int i=0; i<count; i++)
			{
				IDependency pThisDepends = deps.get(i);
				String sThisElementType = pThisDepends.getElementType();
				if (sThisElementType.equals(type))
				{
					foundVec.add(pThisDepends);
				}
			}
		}
		return foundVec;
	}

	/**
	 *
	 * Retrieves the fully qualifed name for this element.
	 * This is done by concatenating the names of all enclosing namespaces.
	 * The Project name is appended based on the user preference.
	 *
	 * @param scopedName[out] The fully scoped name
	 *
	 * @return HRESULT
	 *
	 */
	public String getQualifiedName() {
		return getFullyQualifiedName( useProjectInQualifiedName() );
	}

	/**
	 *
	 * Determines whether or not to use this element's Project name when 
	 * determining this element's fully qualified name.
	 *
	 * @return true if the Project name should be used, else false if it should 
	 *         not be.
	 *
	 */
        private boolean useProjectInQualifiedName() {
            //kris richards - "ProjectNamespace" pref has been removed; set to false.
            return false;
        }

	/**
	 *
	 * Retrieves the fully qualifed name for this element.
	 * This is done by concatenating the names of all enclosing namespaces.
	 * The Project name is appended based on the input boolean:  useProjectName.
	 *
	 * @param useProjectName[in] When true the fully scoped name will start with the project name
	 * @param scopedName[out] The fully scoped name
	 *
	 * @return HRESULT
	 *
	 */
	public String getFullyQualifiedName(boolean useProjName) {
		String scopedName = getName();
		INamespace parent = getNamespace();
		boolean gather = false;
		if (parent != null)
		{
			if (parent instanceof IProject)
			{
				if (useProjName)
				{
					gather = true;
				}
			}
			else
			{
				gather = true;
			}
		}
		
		if (gather)
		{
			String parentFullName = parent.getFullyQualifiedName(useProjName);
			scopedName = parentFullName + "::" + scopedName;
		}

		return scopedName;
	}

	/**
	 *
	 * Retrieves the alias value for this element. This value can then be used
	 * as another name for this element.  This will not be used for namespace
	 * resolution however.
	 *
	 * @param curVal[out] The current value to use as the alias value.
	 *
	 * @return HRESULT
	 * @note If the Alias property has not been previously established via a 
	 *       call to put_Alias(), get_Name() is actually used to retrieve a value.
	 *       In order to find out if an element has been aliased, see get_IsAliased()
	 *
	 */
	public String getAlias()
	{
		String alias = null;
		try
		{
			alias = getAttributeValue("alias");
		}
		catch (Exception e)
		{
			alias = null;
		}
		
		if( alias == null || alias.length() == 0 )
		{
		   alias = getName();
		}
		return alias;
	}

	/**
	 *
	 * Sets the alias value for this element. This value can then be used as 
	 * another name for this element. This will not be used for namespace
	 * resolution however.
	 *
	 * @param newName[in] The new value for the alias property.
	 *
	 * @return HRESULT
	 *
	 */
	public void setAlias(String newName) {
		String curName = getAlias();
		String strNewName = "";
		if (newName != null && newName.length() > 0)
			strNewName = newName;
		
		if (!strNewName.equals(curName))
		{
			// Ok the names don't match. Let's make sure that the new name
			// is not the default Un named name before we send out events
			boolean fireModEvent = true;
			
			boolean proceed = true;
			IElementChangeDispatchHelper helper = null;
			if (fireModEvent)
			{
				// Only want to do all the events and setting of data
				// if the pre and post values are different...
				helper = new ElementChangeDispatchHelper();
				proceed = helper.dispatchPreAliasNameModified(this, strNewName);
			}
			
			if (proceed)
			{
				if (fireModEvent)
				{
					setAttributeValue("alias", strNewName);
				}
				else
				{
					// This will not cause the lower level element modified to fire
					XMLManip.setAttributeValue(m_Node, "alias", strNewName);
				}
				
				if (fireModEvent)
				{
					helper.dispatchAliasNameModified(this);
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
	 * Determines whether or not this element has an alias value.
	 *
	 * @param bIsAliased[out] - True if this element has been aliased,
	 *                          false if it has not been aliased
	 *
	 * @return HRESULT
	 *
	 */
	public boolean isAliased() {
		boolean bIsAliased = false;
		String alias = getAttributeValue("alias");
		if (alias.length() > 0)
		{
			bIsAliased = true;
		}
		return bIsAliased;
	}

	public long getSupplierDependencyCount() {
		return UMLXMLManip.queryCount( m_Node, "supplierDependency" , true );
	}

	public long getClientDependencyCount() {
		return UMLXMLManip.queryCount( m_Node, "clientDependency" , true );
	}

	/** 
	 * Returns the Fully Qualified Name without the Project name
	 * 
	 * @param name[out] fully qualified name (without the project name)
	 */
	public String getQualifiedName2() {
		return getFullyQualifiedName(false);
	}

	/**
	 *
	 * Sets the default name of this element.
	 *
	 * @return HRESULTs
	 * @note The IElementChangeEventDispatcher is completely blocked
	 *       while this function is executed, which means that name
	 *       change events will not fire. This was done to prevent
	 *       events from firing while a new element's name is
	 *       established.
	 *
	 */
	protected void establishDefaultName()
	{
		String name = retrieveDefaultName();
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		IElementChangeEventDispatcher dispatcher = ret.getDispatcher(EventDispatchNameKeeper.modifiedName());
		if (dispatcher != null)
		{
			boolean origBlock = EventBlocker.startBlocking(dispatcher);
		
			// Block the name change event. No need to fire events right now.
			try
			{
				setName(name);
			}
			finally
			{
				EventBlocker.stopBlocking(origBlock, dispatcher);
			}
		}
	}
	
	/**
	 *
	 * Attempts to find a single NamedElement in this elements's namespace or above namespaces. If
	 * more than one element is found with the same name, only the first one is used.
	 *
	 * @param typeName[in] The name to match against
	 * @param foundElement[out] The found NamedElement, else 0.
	 *
	 * @return HRESULTs
	 */
	protected INamedElement resolveSingleTypeFromString( String typeName)
	{
		return UMLXMLManip.resolveSingleTypeFromString(	(INamedElement) getAggregator(), typeName);
	}
	
	/**
	 * Attempts to find all types in this classifier's namespace or above namespaces.a
	 *
	 * @param typeName[in] The name of the type to find
	 * @param foundElements[out] A collection of elements that have the same name as typeName
	 *
	 * @return HRESULTs
	 */
	protected ETList<INamedElement> resolveTypeFromString(String typeName)
	{
      return UMLXMLManip.resolveTypeFromString(this, typeName);
	}
	
	protected ETList<INamedElement> resolveTypeFromString(IElement elem, Document doc, 
											String typeName, INamespace space)
	{
      return UMLXMLManip.resolveTypeFromString(elem, doc, typeName, space);
	}
	
	public boolean isNameSame(IBehavioralFeature feature)
	{
		if (feature == null)
		{
			return false;
		}
		
		String name = getName();
		return (name != null) ? name.equals(feature.getName()) : (feature.getName() == null) ;
	}
	
	/**
	 *
	 * Retrieves the name or alias of this element.
	 *
	 * @param name[out] The name value
	 *
	 * @return HRESULT
	 *
	 */
	public String getNameWithAlias()
	{
		String retVal = null;
		if (showAliasedNames())
		{
			retVal = getAlias();
		}
		else
		{
			retVal = getName();
		}
		return retVal;
	}

	/**
	 *
	 * Sets the name or alias of this element.
	 *
	 * @param name[in] Name
	 *
	 * @return HRESULT
	 *
	 */
	public void setNameWithAlias(String newVal)
	{
		if (showAliasedNames())
		{
			setAlias(newVal);
		}
		else
		{
			setName(newVal);
		}
	}

        /**
         * @return
         */
        protected boolean showAliasedNames() {
            //kris richards - changing to NbPrefs
            return NbPreferences.forModule(NamedElement.class).getBoolean("UML_Show_Aliases", false) ;
        }
    
    /**
     * The default behavior to this method is to return true if the names of the
     * two elements being compared are same. Subclasses should override to 
     * implement class specific <em>isSimilar</em> behavior.
     *
     * @param other The other named element to compare this named element to.
     * @return true, if the names are the same, otherwise, false.
     */
    public boolean isSimilar(INamedElement other)
    {
        return (getName().equals(other.getName()));
    }
}
