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
