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
 * Created on Nov 13, 2003
 *
 */
package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;
import org.netbeans.modules.uml.core.metamodel.core.foundation.RelationProxy;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IImplementation;
import org.netbeans.modules.uml.core.roundtripframework.ChangeKind;
import org.netbeans.modules.uml.core.roundtripframework.IChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IImplementationChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IImplementationClassChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.ImplementationChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.ImplementationClassChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.RTElementKind;
import org.netbeans.modules.uml.core.roundtripframework.RequestDetailKind;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author aztec
 *
 */
public class JavaImplementationChangeHandler extends JavaDependencyChangeHandler
									implements IJavaImplementationChangeHandler
{
	public JavaImplementationChangeHandler()
	{
		super();
	}

	public JavaImplementationChangeHandler(IJavaChangeHandler copy)
	{
		super(copy);
	}
	
	public void handleRequest( IRequestValidator requestValidator)
	{
		if (requestValidator != null && requestValidator.getValid())		
		{
			if (m_Utilities != null)
			{
				RequestDetails details = m_Utilities.getRequestDetails(
													requestValidator.getRequest());																
				if(details != null)
				{
				   int cType = details.changeKind;	
				   int cDetail = details.requestDetailKind;
				   int eType = details.rtElementKind;
				
				
					deleted(requestValidator, cType, cDetail);
					added(requestValidator, cType, cDetail);
	
					if ( eType == RTElementKind.RCT_INTERFACE )
					{
					   nameChange(requestValidator, cType, cDetail );
					}
				}
			}			
		}
	}
	
	protected void added( IRequestValidator requestValidator, int cType, int cDetail )
	{
		if ( requestValidator != null && requestValidator.getValid() &&
			 requestValidator.getRequest() != null &&
			 cDetail != RequestDetailKind.RDT_RELATION_DELETED)
		{
			// If this element of the request is playing the subclass, 
			// Java needs to know about it ( valid ). Otherwise, Java does not
			// care (invalid).
			IChangeRequest request = requestValidator.getRequest();			
			
			IElement pReqElement = request.getAfter();
			IRelationProxy  pRel = request.getRelation();

			if ( pReqElement != null && pRel != null )
			{
			   String relationType = m_Utilities.getRelationType(pRel);

			   if ( "Implementation".equals(relationType) )
			   {
				  IElement pRelTo = pRel.getTo();         
				  IElement pRelFrom = pRel.getFrom();

				  boolean isSame = false;

				  if ( pRelTo != null )
				  {
					 isSame = pReqElement.isSame(pRelTo);
					 if (isSame)
					 {
						// The element of the change request is playing
						// the superclass of a generalization. Java does not
						// care (there is nothing that needs to be done to 
						// the source code for the superclass).
						requestValidator.setValid(false);
					 }
					// If the pRelTo is "unnamed" at this time, generate no 
					// request.  This prevents relation creates when a
					// lollipop is created.

					INamedElement pNamedRelTo = pRelTo instanceof INamedElement? (INamedElement) pRelTo : null;
					if ( m_Utilities.isElementUnnamed ( pNamedRelTo ) )
					{
					   requestValidator.setValid ( false );
					   isSame = true;
					}
				  }

				  if (!isSame)
				  {
					 if ( pRelFrom != null )
						isSame = pReqElement.isSame(pRelFrom);

					 if (isSame)
					 {
						// The element of the request is playing the 
						// subclass. We want to inject into that class
						// all abstract and virtual methods gotten from the 
						// superclass

						IOperationCollectionBehavior behavior = new OperationCollectionBehavior();
						behavior.setSilent( true );
						behavior.setInterfacesOnly(true);

						m_Utilities.applyInheritedOperations(requestValidator, pRelTo, pReqElement, behavior);

						// Add a dependency
						addDependency ( requestValidator, pReqElement, pRelTo );
					 }
				  }

				  if ( !isSame )
				  {
					 // It is still false at this point? Hey, the element may be the connection
					 // itself. 

					IImplementationChangeRequest pImpReq = request instanceof IImplementationChangeRequest? (IImplementationChangeRequest) request : null;
					 if ( pImpReq != null )
					 {
						// ArtifactIsFrom is exists SPECIFICALLY for this purpose. 
						// When the prerequest is created, the "same" prerequest is
						// created for every artifact that RTEventManager can determine
						// MIGHT be interested in receiving the final change request.
						// But, the prerequest is still indexed off of the element that 
						// is changing, which, in this case, is the connection.  So, 
						// the prerequest has the item that is changing, and the artifact
						// that exists that demands the change. BUT, we now are in a position
						// where we want to know if a particular classifier NEEDS to know
						// about the change (since we are now in Java-specific land, we know
						// that some of the requests are meaningless and need to be invalidated).
						// But all we have is the artifact! AND THAT ARTIFACT MAY IN FACT
						// BE THE SAME FOR BOTH ENDS!!!!!!. But we want to disable ONE of these
						// requests. Thus the existence of this flag. It tells us which end of
						// a connection the artifact was taken from. ArtifactIsFrom means that 
						// it is and artifact from the From end of the connection.

						boolean artifactFrom = pImpReq.getArtifactIsFrom();
						if (artifactFrom)
						{
						   IClassifier pNewSup = pImpReq.getAfterInterface();
						   IClassifier pNewSub = pImpReq.getAfterImplementing();

						   if ( pNewSub != null && pNewSup != null )
						   {
							  // Note that we set the collection behavior
							  // to be silent. This means that all interface
							  // methods are injected automatically. The user
							  // never has to select them. 
							  IOperationCollectionBehavior behavior = 
											new OperationCollectionBehavior();
							  behavior.setSilent(true);
							  behavior.setInterfacesOnly(true);
							  
							  m_Utilities.applyInheritedOperations ( requestValidator, 
														pNewSup, pNewSub, behavior );

							  // Add a dependency
							  addDependency ( requestValidator, pNewSub, pNewSup );
						   }
						}
						else
						{
						   // The element of the change request is playing
						   // the superclass of a generalization. Java does not
						   // care (there is nothing that needs to be done to 
						   // the source code for the superclass).

						   requestValidator.setValid ( false );
						}
					 }
				  }
			   }
			}
		}
	}
	
	public void added(IClassifier pInterface, IClassifier pClass)
	{
		IRequestValidator dummyVal = new RequestValidator();
		added ( dummyVal, pInterface, pClass );
	}
	
	public void  added ( IRequestValidator request, IClassifier pInterface, 
						 IClassifier pClass)
	{
		//C++ method is empty.					     
	}
	
	protected void deleted(IRequestValidator requestValidator, int cType, int cDetail )
	{
		try
		{			
			if ( requestValidator != null && requestValidator.getValid() && 
				 requestValidator.getRequest() != null)
			{
				// If this element of the request is playing the subclass, 
				// Java needs to know about it ( valid ). Otherwise, Java does not
				// care (invalid).
				IElement pReqElement = requestValidator.getRequest().getAfter();
				IRelationProxy  pRel = requestValidator.getRequest().getRelation();

				if ( pReqElement != null && pRel != null )
				{
				   String relationType = m_Utilities.getRelationType(pRel);

				   if ( "Implementation".equals(relationType) )
				   {
                       IChangeRequest creq = requestValidator.getRequest();
					   IImplementationChangeRequest pImpReq = 
                                (creq instanceof IImplementationChangeRequest)
                                    ? (IImplementationChangeRequest)creq : null;
						if ( pImpReq != null )
						{
						   // ArtifactIsFrom is exists SPECIFICALLY for this purpose. 
						   // When the prerequest is created, the "same" prerequest is
						   // created for every artifact that RTEventManager can determine
						   // MIGHT be interested in receiving the final change request.
						   // But, the prerequest is still indexed off of the element that 
						   // is changing, which, in this case, is the connection.  So, 
						   // the prerequest has the item that is changing, and the artifact
						   // that exists that demands the change. BUT, we now are in a position
						   // where we want to know if a particular classifier NEEDS to know
						   // about the change (since we are now in Java-specific land, we know
						   // that some of the requests are meaningless and need to be invalidated).
						   // But all we have is the artifact! AND THAT ARTIFACT MAY IN FACT
						   // BE THE SAME FOR BOTH ENDS!!!!!!. But we want to disable ONE of these
						   // requests. Thus the existence of this flag. It tells us which end of
						   // a connection the artifact was taken from. ArtifactIsFrom means that 
						   // it is an artifact from the From end of the connection.
	
						   boolean artifactFrom = pImpReq.getArtifactIsFrom();
						   if (artifactFrom)
						   {							
							  IClassifier pOldSub = pImpReq.getBeforeImplementing();
							  IClassifier pOldSup = pImpReq.getBeforeInterface();
	
							  if ( pOldSup != null && pOldSub != null )
							  {
								 m_Utilities.breakRedefinitions ( pOldSup, pOldSub );
								 m_Utilities.breakRedefinitionsPropagated ( pOldSup, pOldSub );
							  }
						   }
						   else
						   {
							  // The element of the change request is playing
							  // the superclass of a generalization. Java does not
							  // care (there is nothing that needs to be done to 
							  // the source code for the superclass).
	
							  requestValidator.setValid ( false );
						   }
						}			
				   }
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void deleted( IRequestValidator request, IClassifier pBaseClass, IClassifier pDerivedClass )
	{
		//C++ method is empty.
	}
	
	public void deleted( IClassifier pBaseClass, IClassifier pDerivedClass )
	{
		IRequestValidator dummyVal = new RequestValidator();
		deleted(dummyVal, pBaseClass, pDerivedClass);
	}
	
	protected void nameChange(IRequestValidator requestValidator, int cType, int cDetail )
	{
		if (requestValidator != null && requestValidator.getValid())
		{
			if (cType == ChangeKind.CT_MODIFY && cDetail == RequestDetailKind.RDT_NAME_MODIFIED)
			{
				// We are here because a class name or interface name has changed.
				// We need to generate appropriate change requests so that the listeners
				// can update extends statements.
				IClassifier pBeforeClass = m_Utilities.getClass(requestValidator.getRequest(), true);
				IClassifier pAfterClass = m_Utilities.getClass(requestValidator.getRequest(), false);

				// We need to find all of the classes that generalize from it.
				ETList<IClassifier> subClasses = null;
				subClasses = m_Utilities.getImplementingClassifiers(pAfterClass, subClasses);

				if ( subClasses != null )
				{
				   int count = subClasses.size();
				   int idx = 0;
				   while ( idx < count )
				   {
					  IClassifier pItem = (IClassifier)subClasses.get(idx++);
					  if ( pItem != null )
					  {
						IImplementationClassChangeRequest pNewRequest = 
										new ImplementationClassChangeRequest();

						 if ( pNewRequest != null )
						 {
							pNewRequest.setState(cType);
							pNewRequest.setRequestDetailType(RequestDetailKind.RDT_RELATION_END_MODIFIED);
							pNewRequest.setLanguage("Java");

							pNewRequest.setBefore(pItem);
							pNewRequest.setAfter(pItem);

							pNewRequest.setImplementationEffected(false);
							pNewRequest.setBeforeImplementing(pItem);
							pNewRequest.setAfterImplementing(pItem);
							pNewRequest.setBeforeInterface(pBeforeClass);
							pNewRequest.setAfterInterface(pAfterClass);

							// now just add the new request to the passed in request.
							requestValidator.addRequest(pNewRequest);
						 }
					  }
				   }
				}
			}
			else if (cType == ChangeKind.CT_CREATE && cDetail == RequestDetailKind.RDT_NAME_MODIFIED)
			{
				// We are here because a class name or interface name has changed.
				// We need to generate appropriate change requests so that the listeners
				// can update extends statements.

				// The difference here is only in the type of change request we want to
				// create. The above code is sufficient for all cases, but because the
				// listeners might not be expecting it in this case, we will create a 
				// relation create request instead.
				IClassifier pBeforeClass = m_Utilities.getClass(requestValidator.getRequest(), true);
				IClassifier pAfterClass = m_Utilities.getClass(requestValidator.getRequest(), false);
				
				// We need to find all of the classes that implement it.
				if (pAfterClass != null)
				{
					ETList<IDependency> deps = 
							pAfterClass.getSupplierDependenciesByType("Implementation");
					if ( deps != null )
					{
					   int count = deps.size();
					   int idx = 0;					   
					   while ( idx < count )
					   {
						  IDependency pItem = deps.get(idx++);						  
						  if ( pItem != null )
						  {
							 IImplementation pImpl = pItem instanceof IImplementation? (IImplementation) pItem : null;
							 if ( pImpl != null )
							 {
								IClassifier pImplClass = pImpl.getImplementingClassifier();
								if ( pImplClass != null )
								{
								   IImplementationChangeRequest pNewRequest = new ImplementationChangeRequest();								   

								   if ( pNewRequest != null )
								   {
									  pNewRequest.setState ( cType );
									  pNewRequest.setRequestDetailType ( RequestDetailKind.RDT_RELATION_CREATED );
									  pNewRequest.setLanguage ("Java");

									  pNewRequest.setBefore ( pItem );
									  pNewRequest.setAfter ( pItem );

									  // In this case we need to create a bogus proxy

									  IRelationProxy pProxy = new RelationProxy();									  
									  if ( pProxy != null )
									  {
										 pProxy.setFrom ( pImplClass );
										 pProxy.setTo ( pAfterClass );
										 pProxy.setConnection ( pImpl );

										 pNewRequest.setImplementationEffected ( true );

										 // now just add the new request to the passed in request.
										 requestValidator.addRequest ( pNewRequest );
									  }
								   }
								}
							 }
						  }
					   }
					}
				}
			}
		}
	}
}



