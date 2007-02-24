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
 * File       : ImpactedPreRequest.java
 * Created on : Nov 12, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.eventframework.IOriginalAndNewEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;

/**
 * @author Aztec
 */
public class ImpactedPreRequest
    extends PreRequest
    implements IImpactedPreRequest
{
    private IClassifier m_Classifier;
    private String      m_OldClassName;
    private String      m_NewClassName;

    public ImpactedPreRequest(IElement preElement, 
                        IElement elementWithArtifact,
                        IClassifier classifier,
                        IRequestProcessor proc, 
                        int detail,
                        IEventPayload payload)
    {
        super(preElement, 
                preElement, 
                elementWithArtifact, 
                proc, 
                detail, 
                payload, 
                null);
                
        m_Classifier = classifier;

        if (m_Classifier != null)
            m_OldClassName = m_Classifier.getQualifiedName();
        
        // The payload is a rtpayload, which should have the IOriginalAndNewEventPayload
        // on it.

        IRoundTripEventPayload pRTPayload  
                = (payload instanceof IRoundTripEventPayload)
                    ? (IRoundTripEventPayload)payload : null;
        if ( pRTPayload != null )
        {
            Object data = pRTPayload.getData();

            IOriginalAndNewEventPayload pChangePayload = 
                (data instanceof IOriginalAndNewEventPayload)
                ? (IOriginalAndNewEventPayload)data : null;
           
            if ( pChangePayload != null )
            {
                m_OldClassName = pChangePayload.getOriginalValue();
                m_NewClassName = pChangePayload.getNewValue();
            }
        }                
    }
    
    public IChangeRequest createChangeRequest(IElement pElement, 
                                    /*ChangeKind*/int type, 
                                    /*RequestDetailKind*/int detail)
    {
        if( pElement == null ) return null;

        IChangeRequest newReq = null;

        IAttribute pAttr = (pElement instanceof IAttribute)
                            ? (IAttribute)pElement : null;
        IParameter pParm = (pElement instanceof IParameter)
                            ? (IParameter)pElement : null;

        if ( pAttr != null || pParm != null )
        {
            if ( pAttr != null )
            {
                newReq = new AttributeTypeChangeRequest();
            }
            else if ( pParm != null )
            {
                newReq = new ParameterTypeChangeRequest();
            }

            populateChangeRequest ( newReq );

            newReq.setAfter(pElement);
            newReq.setState( type );

            // Now allow the PreRequest object make sure this ChangeRequest is absolutely
            // ready to go...\
            preProcessRequest( newReq );
        }
        return newReq;
    }
    
    public void populateChangeRequest ( IChangeRequest req )
    {
        if( req == null ) return;
        super.populateChangeRequest ( req );
        
        ITypeChangeRequest typeReq = (req instanceof ITypeChangeRequest)
                            ? (ITypeChangeRequest)req : null;

        if ( typeReq != null )
        {
            typeReq.setModifiedClassifier ( m_Classifier );
            typeReq.setOldTypeName ( m_OldClassName );
            typeReq.setNewTypeName ( m_NewClassName );
        }
    }
}
