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

/*
 * Created on Nov 13, 2003
 *
 */
package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.roundtripframework.ChangeKind;
import org.netbeans.modules.uml.core.roundtripframework.GeneralizationClassChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IGeneralizationChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IGeneralizationClassChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.RTElementKind;
import org.netbeans.modules.uml.core.roundtripframework.RequestDetailKind;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author aztec
 *
 */
public class JavaGeneralizationChangeHandler extends JavaChangeHandler
implements IJavaGeneralizationChangeHandler
{
    public JavaGeneralizationChangeHandler()
    {
    }

    public JavaGeneralizationChangeHandler(IJavaChangeHandler copy)
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

                    if ( eType == RTElementKind.RCT_CLASS || 
                            eType == RTElementKind.RCT_INTERFACE )
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

                if ( "Generalization".equals(relationType) )
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
                            behavior.setSilent( getSilent() );
                            behaviorForInterfaces(behavior, pRelFrom, pRelTo);

                            m_Utilities.applyInheritedOperations(requestValidator, pRelTo, pReqElement, behavior);

                            // Add a dependency
                            addDependency ( requestValidator, pReqElement, pRelTo );
                        }
                    }

                    if ( !isSame )
                    {
                        // It is still false at this point? Hey, the element may be the connection
                        // itself. 

                        IChangeRequest creq = requestValidator.getRequest();
                        IGeneralizationChangeRequest pGenReq = 
                            (creq instanceof IGeneralizationChangeRequest)
                                ? (IGeneralizationChangeRequest)creq : null;

                        if ( pGenReq != null )
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

                            boolean artifactFrom = pGenReq.getArtifactIsFrom();
                            if (artifactFrom)
                            {
                                IClassifier pNewSup = pGenReq.getAfterGeneralizing();
                                IClassifier pNewSub = pGenReq.getAfterSpecializing();

                                if ( pNewSub != null && pNewSup != null )
                                {
                                    // Inject new methods.
                                    IOperationCollectionBehavior behavior = 
                                    new OperationCollectionBehavior();
                                    behavior.setSilent ( getSilent () );
                                    behaviorForInterfaces ( behavior, pNewSub, pNewSup );
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

    public void added(IClassifier pBaseClass, IClassifier pDerivedClass)
    {
        IRequestValidator dummyVal = new RequestValidator();
        added ( dummyVal, pBaseClass, pDerivedClass );
    }

    public void  added ( IRequestValidator request, IClassifier pBaseClass, 
            IClassifier pDerivedClass)
    {
        //C++ method is empty.					     
    }

    protected void behaviorForInterfaces(IOperationCollectionBehavior behavior, 
            IElement pFrom, IElement pTo)
    {
        try
        {
            if ( pFrom != null && pTo != null )
            {
                IInterface pBaseInter = pTo instanceof IInterface? (IInterface) pTo : null;
                IInterface pSubInter = pFrom instanceof IInterface? (IInterface) pFrom : null;

                if ( pBaseInter != null && pSubInter != null )
                {
                    // force silent and no redefinition
                    behavior.setSilent ( true );
                    behavior.setSilentSelectAll ( false );
                }
            }					 
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    protected void deleted(IRequestValidator requestValidator, int cType, int cDetail )
    {
        try
        {
            if ( requestValidator != null && requestValidator.getValid() && 
                    requestValidator.getRequest() != null)
            {
                IElement pReqElement = requestValidator.getRequest().getAfter();
                IRelationProxy  pRel = requestValidator.getRequest().getRelation();

                if ( pReqElement != null && pRel != null )
                {
                    String relationType = m_Utilities.getRelationType(pRel);

                    if ( "Generalization".equals(relationType) )
                    {
                        IGeneralizationChangeRequest pGenReq = null;
                        if (requestValidator.getRequest() instanceof IGeneralizationChangeRequest)
                            pGenReq = (IGeneralizationChangeRequest)requestValidator.getRequest();

                        if ( pGenReq != null )
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

                            boolean artifactFrom = pGenReq.getArtifactIsFrom();
                            if (artifactFrom)
                            {
                                IClassifier pOldSub = pGenReq.getBeforeSpecializing();
                                IClassifier pOldSup = pGenReq.getBeforeGeneralizing();

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
            if ( (cType == ChangeKind.CT_MODIFY || cType == ChangeKind.CT_CREATE) && cDetail == RequestDetailKind.RDT_NAME_MODIFIED)
            {
                // We are here because a class name or interface name has changed.
                // This can happen in two cases. 
                // 1. When a class is created.
                // 2. When a class is renamed.
                // We need to generate appropriate change requests so that the listeners
                // can update extends statements.
                IClassifier pBeforeClass = m_Utilities.getClass(requestValidator.getRequest(), true);
                IClassifier pAfterClass = m_Utilities.getClass(requestValidator.getRequest(), false);

                // We need to find all of the classes that generalize from it.
                ETList<IClassifier> subClasses = m_Utilities.getSpecializations(pAfterClass);

                if ( subClasses != null )
                {
                    int count = subClasses.size();
                    int idx = 0;
                    while ( idx < count )
                    {
                        IClassifier pItem = subClasses.get(idx++);
                        if ( pItem != null )
                        {
                            IGeneralizationClassChangeRequest pNewRequest = 
                            new GeneralizationClassChangeRequest();

                            if ( pNewRequest != null )
                            {
                                //Use modify even for create
                                pNewRequest.setState(ChangeKind.CT_MODIFY);
                                pNewRequest.setRequestDetailType(RequestDetailKind.RDT_RELATION_END_MODIFIED);
                                pNewRequest.setLanguage("Java");

                                pNewRequest.setBefore(pItem);
                                pNewRequest.setAfter(pItem);

                                pNewRequest.setSpecializationEffected(false);
                                pNewRequest.setBeforeSpecializing(pItem);
                                pNewRequest.setAfterSpecializing(pItem);
                                pNewRequest.setBeforeGeneralizing(pBeforeClass);
                                pNewRequest.setAfterGeneralizing(pAfterClass);

                                // now just add the new request to the passed in request.
                                requestValidator.addRequest(pNewRequest);
                            }
                        }
                    }
                }
            }
        }
    }
}



