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

/*
 * Created on Nov 5, 2003
 *
 */
package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import java.util.Iterator;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.common.generics.ETTripleT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IVisibilityKind;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.INavigableEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.roundtripframework.ChangeKind;
import org.netbeans.modules.uml.core.roundtripframework.IElementDuplicatedChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.RTElementKind;
import org.netbeans.modules.uml.core.roundtripframework.RequestDetailKind;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageIconKindEnum;

/**
 * @author aztec
 *
 */
public class JavaClassChangeHandler extends JavaChangeHandler
								    implements IJavaClassChangeHandler
{	
	public JavaClassChangeHandler()
	{		
	}	
	
    public JavaClassChangeHandler(JavaChangeHandlerUtilities utilities)
    {
    	super(utilities);
    }
    
	public JavaClassChangeHandler(JavaChangeHandler copy )
	{
		super(copy);
	}

	public void handleRequest(IRequestValidator requestValidator)
	{
		if ( requestValidator != null && requestValidator.getValid() && 
		     !requestValidator.isRelation())		
		{	
			if (m_Utilities != null)
			{
				RequestDetails details = m_Utilities.getRequestDetails(
													requestValidator.getRequest());
				if ( details != null && 
					 details.rtElementKind == RTElementKind.RCT_CLASS )
				{
				   int cType = details.changeKind;	
				   int cDetail = details.requestDetailKind;				   
					
				   transform( requestValidator, cType, cDetail );
				   created( requestValidator, cType, cDetail );
				   nameChange( requestValidator, cType, cDetail );
				   strictfpChange( requestValidator, cType, cDetail );
				   finalChange( requestValidator, cType, cDetail );
				   documentationChange( requestValidator, cType, cDetail );
				   namespaceChange( requestValidator, cType, cDetail );
				   abstractChange( requestValidator, cType, cDetail );
				   visibilityChange( requestValidator, cType, cDetail );
				   featureDuplicated( requestValidator, cType, cDetail );
				   deleted ( requestValidator, cType, cDetail );
				}
			}
		}
	}
	
	protected void transform( IRequestValidator requestValidator, int cType, int cDetail )
	{
		if ( requestValidator != null && requestValidator.getValid() && m_Utilities != null)
		{
			if ( cType != ChangeKind.CT_DELETE && cDetail == RequestDetailKind.RDT_TRANSFORM )
			{
				// If the transform is also a delete do nothing
				// and let the others handle it.
				IClassifier pClass = m_Utilities.getClass(requestValidator.getRequest());
				if (pClass != null)
				{
					transform ( requestValidator, pClass);		
				}
			}
		}
	}
	
	public void transform(IRequestValidator requestValidator, IClassifier pClass)
	{
		if (pClass != null)
		{
			if ( m_Utilities.isElementUnnamed ( pClass ) )
			{
			   plug(new RequestPlug());
			   requestValidator.setValid(false);
			}
			
			// A classifier is being transformed into a class.
			// We must create constructors and destructors, create all attributes.
			// The class and all of its operations will remain abstract,
			// since this is still valid, and has the least impact.
			IJavaMethodChangeHandler opHandler = new JavaMethodChangeHandler(this);
			opHandler.setSilent(true);
			
			IJavaAttributeChangeHandler attrhandler = new JavaAttributeChangeHandler(this);
			attrhandler.setSilent(true);
			
			// Create constructor if there is none.
			ETList<IOperation> constructors = m_Utilities.getConstructors(pClass);
			boolean create = true;
			if (constructors != null && constructors.size() > 0)
				create = false;
			
			if (create)
				m_Utilities.createConstructor(pClass);	
		
            // Create destructor if there is none.
            m_Utilities.createDestructor (pClass);

		
			// transform attributes
			ETList<IAttribute> attributes = pClass.getAttributes();
			if (attributes != null)
			{
				Iterator<IAttribute> iter = attributes.iterator();
				if (iter != null)
				{
					while (iter.hasNext())
					{
					   IAttribute attr = iter.next();
					   if (attr != null)
					   {
							attrhandler.transformToClass(attr, pClass);
					   }
					}
				}			
			}
			
			// Transform operations
			ETList<IOperation> operations = pClass.getOperations();
			if (operations != null)
			{
				Iterator<IOperation> iter = operations.iterator();
				if (iter != null)
				{
					while (iter.hasNext())
					{
					   IOperation oper = iter.next();
					   if (oper != null)
					   {
							boolean noAbstractProcessing = false; // this means "no abstract processing". Readability
							opHandler.transformToClass(requestValidator, oper, pClass, noAbstractProcessing);
					   }
					}
				}			
			}
			
			// Transform all attributes from associations
			ETList<IAssociationEnd> ends = m_Utilities.getReferencingNavEnds(pClass);
			if (ends != null)
			{
				Iterator<IAssociationEnd> iter = ends.iterator();
				if (iter != null)
				{
					while (iter.hasNext())
					{
					   IAssociationEnd end = iter.next();
					   if (end != null && end instanceof IAttribute)
					   {
							IAttribute pEnd = end instanceof IAttribute? (IAttribute) end : null;
							attrhandler.transformToClass ( pEnd, pClass );
					   }
					}
				}			
			}
			
			// Finally, set the class to non-abstract
			pClass.setIsAbstract(false);
		}
	}
	
	public void transform(IClassifier pClass)
	{
		IRequestValidator request = new RequestValidator();
		request.setValid(true);
		transform(request, pClass);
	}
	
	protected void created( IRequestValidator requestValidator, int cType, int cDetail )
	{	
		if ( requestValidator != null && requestValidator.getValid() && 
		     m_Utilities != null)
		{
			if ( cType == ChangeKind.CT_CREATE && cDetail != RequestDetailKind.RDT_TRANSFORM )
			{
				// This is a class create request.
				// We might be responsible for adding a default constructor
				// and finalize routine.  
				IClassifier pClass = m_Utilities.getClass(requestValidator.getRequest());				
				if (pClass != null)
				{
					// Make sure that there are no existing constructors. If there
					// are, we are in some weird situation, but we can fix it by
					// simply renaming the existing constructors.
										
					boolean noneFound = renameConstructor (pClass);
					if ( noneFound )
					{	
					   m_Utilities.createConstructor ( pClass );
                       m_Utilities.createDestructor( pClass );
					}
					   
				   // Now, this class might already be in some navigable associations.
				   // We want to treat those associations as if they were just 
				   // created, so that the end is named and attributes and accessors
				   // are created in the other classes.

				   // Note that JavaAttributeChangeHandler does process class name
				   // changes in a similar way, but that is only for name CHANGES
				   // (which is a CT_MODIFY, not a CT_CREATE) and it only changes
				   // the types of existing attributes and accessors. Here is where
				   // we must detect that a class is being created and used as
				   // an attribute, and create the attribute as well, since the only
				   // element we have is the class from the change request 
				   // (we don't have a relationship that was just "created"). 
				   // Fortunately, we can use existing utilities and doit functions
				   // on the attribute change handler to do most of the work.
				   ETList<INavigableEnd> navEnds = m_Utilities.getParticipatingNavEnds(pClass);
				   IJavaAttributeChangeHandler handler = new JavaAttributeChangeHandler(this);
				   if (navEnds != null)
				   {
				   	  Iterator<INavigableEnd> iter = navEnds.iterator();
				   	  if (iter != null)
				   	  {
				   	  	  while (iter.hasNext())
				   	  	  {
							 INavigableEnd end = iter.next();
							 if (end instanceof IAttribute)
							 {
								IAttribute pAttribute = end instanceof IAttribute? (IAttribute) end : null;
								handler.addNavigableEndAttribute(requestValidator, pAttribute, null, true);
							 }
				   	  	  }
				   	  }
				   }															
				}
			}
		}
	}
	
	protected void nameChange( IRequestValidator requestValidator, int cType, int cDetail )
	{	
		if ( requestValidator != null && requestValidator.getValid())
		{
			if ( cType == ChangeKind.CT_MODIFY && 
				 cDetail == RequestDetailKind.RDT_NAME_MODIFIED )
			{	
				IClassifier pClass = m_Utilities.getClass(requestValidator.getRequest());
				
				if (pClass != null)
				{					
					// Make sure that there are no existing constructors. If there
					// are, we are in some weird situation, but we can fix it by
					// simply renaming the existing constructors.
					boolean noneFound = renameConstructor(pClass);
				}
			}
		}
	}
	
	/**
	 * Renames the name of the contructor when the name of the class changes.
	 *
	 * @param pClass[in] The class that contains the constructor.
	 * @param noneFound[out]
	 */
	protected boolean renameConstructor (IClassifier pClass)
	{		
		boolean noneFound = true; 
		//Change the name of all constructors
		if (pClass != null)
		{
			String newName = pClass.getName();
			if (newName != null && newName.length() > 0)
			{
				ETList<IOperation> constructors =  m_Utilities.getConstructors(pClass);
				
				Iterator<IOperation> iter = constructors.iterator();
				if (iter != null)
				{
					while (iter.hasNext())
					{
					   IOperation oper = iter.next();	
					   if (oper != null)
					   {
						  noneFound = false;
						  oper.setName(newName);
					   }
					}
				}
			}
		}
		return noneFound;
	}
	
	protected void strictfpChange( IRequestValidator requestValidator, int cType, int cDetail )
	{
		// C++ method is empty.
	}
	
	protected void finalChange( IRequestValidator requestValidator, int cType, int cDetail )
	{
		// C++ method is empty.
	}
	
	protected void documentationChange( IRequestValidator requestValidator, int cType, int cDetail )
	{
		// C++ method is empty.
	}
	
	protected void namespaceChange( IRequestValidator requestValidator, int cType, int cDetail )
	{
		if ( requestValidator != null && requestValidator.getValid())
		{
			if ( cDetail == RequestDetailKind.RDT_ELEMENT_ADDED_TO_NAMESPACE || 
				 cDetail == RequestDetailKind.RDT_CHANGED_NAMESPACE || 
				 cDetail == RequestDetailKind.RDT_NAMESPACE_MOVED )
			{
				// We have to do the following:
				// If we are in relationships, we have to find all the "clients"
				// and tell them to add a dependency to our new package.
				// We then have to get all the suppliers (things we are dependent
				// on) and add dependencies to us.
				IClassifier pClass = m_Utilities.getClass(requestValidator.getRequest());
				if (pClass != null)
				{
				    // Fix for #5085547
				    INamespace nameSpace = pClass.getNamespace();
		    		String spaceType = nameSpace.getElementType();
		    		String elementType = pClass.getElementType();
		    		if (( spaceType!= null )&& (elementType!= null)) {
		                boolean outside = spaceType.equals("Package") || spaceType.equals("Project");
		                boolean isJavaSource = elementType.equals("Class") || elementType.equals("Interface");
		                if (outside && isJavaSource ) {		                    		                        
	                        int visibility =  pClass.getVisibility();
	                        if ( ( visibility == IVisibilityKind.VK_PRIVATE)
	                                ||(visibility == IVisibilityKind.VK_PROTECTED)){
	                            pClass.setVisibility( IVisibilityKind.VK_PACKAGE);
	                       }		                    
		                }
		    		}
		    		
					//Generalization
					ETList<IClassifier> subClasses = m_Utilities.getSpecializations(pClass);
					addDependencies ( requestValidator, pClass, subClasses, false );
					
					ETList<IClassifier> supClasses = m_Utilities.getGeneralizations(pClass);
					addDependencies ( requestValidator, pClass, supClasses, true );
					
					//Implementations
					ETList<IClassifier> impls = null;									
					impls = m_Utilities.getImplementingClassifiers(pClass, impls);
					addDependencies ( requestValidator, pClass, impls, false );
					
					ETList<IClassifier> inters = m_Utilities.getImplementedInterfaces(pClass);
					addDependencies ( requestValidator, pClass, inters, true );
					
					//Other Dependencies
					ETList<IElement> dependents = m_Utilities.getDependents(pClass);					
					ETList<IClassifier> depClasses = m_Utilities.elementsToClasses(dependents);
					addDependencies ( requestValidator, pClass, depClasses, false);
					
					ETList<IElement> independents = m_Utilities.getDependencies(pClass);					
					ETList<IClassifier> indClasses = m_Utilities.elementsToClasses(dependents);
					addDependencies ( requestValidator, pClass, indClasses, true);
					
					//Associations
					ETList<IClassifier> navClasses = m_Utilities.getNavigatingClasses(pClass);
					addDependencies ( requestValidator, pClass, navClasses, false );
					
					ETList<IClassifier> memClasses = m_Utilities.getNavigableClasses(pClass);
					addDependencies ( requestValidator, pClass, memClasses, true );					
					
				}
			}
		}
	}
	
	protected void abstractChange( IRequestValidator requestValidator, int cType, int cDetail )
	{
		// C++ method is empty.	
	}
	
	protected void visibilityChange( IRequestValidator requestValidator, int cType, int cDetail )
	{
		// C++ method is empty.
	}
	
	protected void featureDuplicated( IRequestValidator requestValidator, int cType, int cDetail )
	{
		if (requestValidator != null && requestValidator.getValid())
		{
			if (cDetail == RequestDetailKind.RDT_FEATURE_DUPLICATED)
			{
				IElementDuplicatedChangeRequest pDupe = (IElementDuplicatedChangeRequest)
														requestValidator.getRequest();
				if (pDupe != null)
				{
					IElement pEl = pDupe.getOriginalElement();
					if (pEl != null)
					{
						IAttribute pAttr = pEl instanceof IAttribute? (IAttribute) pEl : null;
						IOperation pOp = pEl instanceof IOperation? (IOperation) pEl : null;
						if (pAttr != null)
						{
							IJavaAttributeChangeHandler attr = new JavaAttributeChangeHandler(this);
							attr.handleRequest(requestValidator);							 															
						}
						else if (pOp != null)
						{
							IJavaMethodChangeHandler meth = new JavaMethodChangeHandler(this);
							meth.handleRequest(requestValidator);							 															
						}
					}
				}
			}
		}		
	}
	
	protected void deleted ( IRequestValidator requestValidator, int cType, int cDetail )
	{
		if (requestValidator != null && requestValidator.getValid())
		{
			if (cType == ChangeKind.CT_DELETE)
			{
				IClassifier pClass = m_Utilities.getClass(requestValidator.getRequest());
				if (pClass != null)
					deleted( requestValidator, pClass ); 
			}
		}
	}
	
	public void deleted(IRequestValidator requestValidator, IClassifier pClass)
	{
		if (pClass != null)
		{
			// Find out if there are any redefined methods on this class
			// Find out what the user wants to do with the redefined methods.
			// Find out if there are any redefining methods on this class.
			// If the user is not deleting redefined methods, but there are 
			// redefining methods, we need to "unhook" the redefinitions.
			ETTripleT<ETList<IOperation>, ETList<IOperation>, ETList<IOperation>> triple = 
													m_Utilities.collectRedefinedOps(pClass, true);
			if (triple != null)
			{					
				ETList<IOperation> topOps = triple.getParamOne();
				ETList<IOperation> bottomOps = triple.getParamTwo();			
				ETList<IOperation> middleOps = triple.getParamThree();
			
				int counttops = 0;
				int countMids = 0;
				if (topOps != null)																
					counttops = topOps.size();
				if (middleOps != null)																
					countMids = middleOps.size();
				
				boolean deleteDeep = false;
				IJavaMethodChangeHandler opHandler = new JavaMethodChangeHandler(this);
			
				if ( counttops > 0 || countMids > 0 )
				{
					// This is where we need to ask the user what to do.				
					String queryKey = "DELETE";
					deleteDeep = doQuery(queryKey);				
				
					HandlerQuery pQuery = (HandlerQuery) findQuery (queryKey);
				
					if ( pQuery != null )
					{
					   // Now copy the query. Note that this won't be the
					   // "right" delete query for operations, but it is 
					   // a delete query, and we don't want it to actually
					   // query. We just want it to answer that same as the
					   // query we just did.

					   IHandlerQuery pOpQuery = new HandlerQuery ( pQuery );
					   opHandler.addQuery ( pOpQuery );
					}
				
					// Let the ophandler propagate the deletes down or "unhook" the redefinitions.
					if (topOps != null)
					{
						Iterator<IOperation> iter = topOps.iterator();
						if (iter != null)
						{
							while (iter.hasNext())
							{
							   IOperation oper = iter.next();
							   if (oper != null)
							   {
									// Using the opHandler allows us to get the 
									// delete deep for free. We just have to be 
									// careful about its query mode.
									opHandler.deleted(oper);
							   }
							}
						}			
					}				

					// The mids are different. When we are deleting a method itself, we 
					// are not breaking any generalizations, so we want to "hook up" the
					// redefinition (the MethodHandler does this). But here, we are 
					// really deleting a class, so the redefinitions are no longer valid.
					// We don't necessarily want to delete deep, but we want to unhook
					// the redefinitions going up, since they are no longer valid.
					// In other words, if not deleting deep, the mids become tops.
					if (middleOps != null)
					{
						Iterator<IOperation> iter = middleOps.iterator();
						if (iter != null)
						{
							while (iter.hasNext())
							{
							   IOperation oper = iter.next();
							   if (oper != null)
							   {
								  if (deleteDeep)
								  {
									 // just let the ophandler do its normal job.
									 // Using the opHandler allows us to get the 
									 // delete deep for free. We just have to be 
									 // careful about its query mode.
									 opHandler.deleted ( oper );									
								  }
								  else
								  {
									 m_Utilities.breakRedefinitions(oper);
								  }
							   }
							}
						}			
					}				
				}
			
				// in all cases, clean the redefinitions of bottomOps.
				if (bottomOps != null)
				{
					Iterator<IOperation> iter = bottomOps.iterator();
					if (iter != null)
					{
						while (iter.hasNext())
						{
						   IOperation oper = iter.next();
						   if (oper != null)
						   {
							  if (deleteDeep)
							  {
								 // just let the ophandler do its normal job.
								 // Using the opHandler allows us to get the 
								 // delete deep for free. We just have to be 
								 // careful about its query mode.
								 opHandler.deleted ( oper );									
							  }
							  else
							  {
								 m_Utilities.breakRedefinitions(oper);
							  }
						   }
						}
					}			
				}									
			}
		}
	}
	
	public void deleted(IClassifier pClass)
	{ 
		IRequestValidator request = new RequestValidator();
		request.setValid(true);
		deleted(request, pClass);
	}
	
	protected ETPairT<String, String> formatDeleteMessage()
	{
	    return new ETPairT<String,String>( 
                RPMessages.getString("IDS_JRT_DELETE_CLASS_OPS"),
                RPMessages.getString("IDS_JRT_DELETE_CLASS_OPS_TITLE") );
	}
	
	protected IHandlerQuery buildQuery(String key)
	{
		IHandlerQuery pQuery = null;
		if (key.equals("DELETE"))
		{
			ETPairT<String, String> msgs = formatDeleteMessage();
			String messageStr = null;
			String titleStr = null; 
			if (msgs != null)
			{
				messageStr = msgs.getParamOne();
				titleStr = msgs.getParamTwo();
			}
			boolean deleteDeepDefault = true;
			pQuery = new HandlerQuery ( key,
										messageStr,
										titleStr,
										deleteDeepDefault,
									    MessageIconKindEnum.EDIK_ICONQUESTION,
										getSilent(), 
										false);	
			
		}
		return pQuery;		
	}
}



