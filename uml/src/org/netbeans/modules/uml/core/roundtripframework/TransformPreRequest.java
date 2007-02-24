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
 * File       : TransformPreRequest.java
 * Created on : Nov 12, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.INavigableEnd;

/**
 * @author Aztec
 */
public class TransformPreRequest
    extends PreRequest
    implements ITransformPreRequest
{
    private IClassifier m_Classifier;
    
    public TransformPreRequest(IElement preElement, 
                        IElement pClone,
                        IElement elementWithArtifact,
                        IRequestProcessor proc, 
                        int detail,
                        IEventPayload payload,
                        IElement clonedOwner)
    {
        super(preElement, 
                pClone, 
                elementWithArtifact, 
                proc, 
                detail, 
                payload, 
                clonedOwner);
                
        INavigableEnd pEnd  = (preElement instanceof INavigableEnd)
                                ? (INavigableEnd)preElement : null;
        if ( pEnd != null )
        {
            m_Classifier = pEnd.getReferencingClassifier();
        }                
    }
    
 
    



    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IPreRequest#createChangeRequest(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, int, int)
     */
    public IChangeRequest createChangeRequest(
        IElement pElement,
        int type,
        int detail)
    {
        if( pElement == null ) return null;
 
        IChangeRequest newReq = null;
        
        IClassifier pClass = (pElement instanceof IClassifier)
                               ? (IClassifier)pElement : null;
        IAssociationEnd pEnd = (pElement instanceof IAssociationEnd)
                               ? (IAssociationEnd)pElement : null;                       
        if ( pEnd != null )
        {
           newReq = new AssociationEndTransformChangeRequest();
        }
        else if ( pClass != null )
        {
            newReq = new ClassTransformChangeRequest();
        }
        else 
        {
            newReq = new TransformChangeRequest();
        }
        populateChangeRequest ( newReq );
        
        newReq.setAfter( pElement );
        newReq.setState( type );
        
        // Now allow the PreRequest object make sure this ChangeRequest is absolutely
        // ready to go...
        
        preProcessRequest( newReq );
        
        return newReq;
    }
    
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IPreRequest#populateChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest)
     */
    public void populateChangeRequest(IChangeRequest req)
    {
        super.populateChangeRequest(req);
        // if this is an assoc end transform, we need to save the classifier
        IAssociationEndTransformChangeRequest pTransform 
            = (req instanceof IAssociationEndTransformChangeRequest)
                ? (IAssociationEndTransformChangeRequest)req : null;
        if ( pTransform != null )
        {
            pTransform.setOldReferencingClassifier ( m_Classifier );
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IPreRequest#preProcessRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest)
     */
    public void preProcessRequest(IChangeRequest req)
    {
        // Unlike PreRequest, this routine uses the AFTER. For example,
        // if the after is a class and the before is an actor, this is a 
        // create. However, if the after is a class and the before is an
        // interface, this is not a create. We might also use this to
        // generate DELETES (classes to actors, for example). 

        if( req == null ) return;
        
        IElement preElement = req.getBefore();
        
        IElement postElement = req.getAfter();
        
        if ( preElement != null && postElement != null )
        {
            String preType = preElement.getElementType();
            String postType = postElement.getElementType();
        
        
            boolean preIsClass = false;
            boolean preIsInterface = false;
            boolean preIsEnumeration = false;
            boolean postIsClass = false;
            boolean postIsInterface = false;
            boolean postIsEnumeration = false;
            boolean preIsNavigable = false;
            boolean postIsNavigable = false;
        
            if ("Class".equals(preType))
            {
                preIsClass = true;
            }
            else if ("Interface".equals(preType))
            {
               preIsInterface = true;
            }
            else if ("Enumeration".equals(preType))
            {
               preIsEnumeration = true;
            }
            else if ("NavigableEnd".equals(preType))
            {
               preIsNavigable = true;
            }

            if ("Class".equals(postType))
            {
                postIsClass = true;
            }
            else if ("Interface".equals(postType))
            {
               postIsInterface = true;
            }
            else if ("Enumeration".equals(postType))
            {
               postIsEnumeration = true;
            }
            else if ("NavigableEnd".equals(postType))
            {
               postIsNavigable = true;
            }
        

        
            if ( ( preIsClass || preIsInterface || preIsEnumeration ) &&
                ( !postIsClass && !postIsInterface && !postIsEnumeration ) )
            {
               // This is a class delete
               req.setState( ChangeKind.CT_DELETE );
            }
        
            if ( ( !preIsClass && !preIsInterface && !preIsEnumeration) &&
                ( postIsClass || postIsInterface || postIsEnumeration) )
            {
               // this is a class create
               req.setState( ChangeKind.CT_CREATE );
            }
        
            if ( preIsNavigable && !postIsNavigable )
            {
               // This is an attribute delete
               req.setState( ChangeKind.CT_DELETE );
            } 
        
            if ( !preIsNavigable && postIsNavigable )
            {
                // This is an attribute create
                req.setState( ChangeKind.CT_CREATE );
            }
        }
    }
}

