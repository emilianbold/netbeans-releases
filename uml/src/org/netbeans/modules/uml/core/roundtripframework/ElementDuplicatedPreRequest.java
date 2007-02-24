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
 * File       : ElementDuplicatedPreRequest.java
 * Created on : Nov 12, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;

/**
 * @author Aztec
 */
public class ElementDuplicatedPreRequest
    extends PreRequest
    implements IElementDuplicatedPreRequest
{
    private IElement m_OrigElement;
    private IElement m_DupeElement;

    public ElementDuplicatedPreRequest(IElement preElement, 
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
    }
    
    public IChangeRequest createChangeRequest (IElement  pElement, 
                                                /*ChangeKind*/int type,
                                                /*RequestDetailKind*/int detail)
    {
        if( pElement == null )return null;
        
        IChangeRequest newReq = new ElementDuplicatedChangeRequest();

        populateChangeRequest ( newReq );

        newReq.setAfter( pElement );
        newReq.setState( type );

        // Now allow the PreRequest object make sure this ChangeRequest is absolutely
        // ready to go...

        preProcessRequest( newReq );
        
        return newReq;
    }
    
    public IElement getDupeElement()
    {
       return m_DupeElement;
    }
    
    public IElement getOrigElement()
    {
       return m_OrigElement;
    }
    
 
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IPreRequest#inCreateState(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public boolean inCreateState(IElement preElement)
    {
        return false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IPreRequest#populateChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest)
     */
    public void populateChangeRequest(IChangeRequest req)
    {
        super.populateChangeRequest(req);
        
        IElementDuplicatedChangeRequest pDupeReq  = 
                ( req instanceof IElementDuplicatedChangeRequest )
                ? (IElementDuplicatedChangeRequest) req : null;
        if ( pDupeReq != null )
        {
            pDupeReq.setOriginalElement( m_OrigElement);
            pDupeReq.setDuplicatedElement(m_DupeElement);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IPreRequest#postEvent(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy)
     */
    public boolean postEvent(IRelationProxy pRel)
    {
        return false;
    }
    
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IPreRequest#setDupeElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public void setDupeElement(IElement val)
    {
        m_DupeElement = val;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IPreRequest#setOrigElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public void setOrigElement(IElement val)
    {
        m_OrigElement = val;
    }

}
