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
