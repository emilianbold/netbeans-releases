/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * File       : RelationPreRequest.java
 * Created on : Nov 10, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IImplementation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;

/**
 * @author Aztec
 */
public class RelationPreRequest extends PreRequest
{
    private IRelationProxy  m_Relation = null;
    private IElement        m_OldFrom = null;
    private IElement        m_OldTo = null;
    private IElement        m_NewTo = null;
    private IElement        m_NewFrom = null;
    private boolean         m_FromIsChanging;
    private boolean         m_ToIsChanging;
    private boolean         m_IsDelete;
    private int             m_ArtifactElementID;

    public RelationPreRequest(IElement preElement, 
                        IElement pClone,
                        IElement elementWithArtifact,
                        IRelationProxy pRel,
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
                
        m_Relation = pRel;
        m_IsDelete = false;
        m_FromIsChanging = false;
        m_ToIsChanging = false;
        m_ArtifactElementID = IRelationPreRequest.EAID_UNKNOWN;

        if ( pRel != null )
        {
            IElement pCon = pRel.getConnection();

            IGeneralization pGen  = (pCon instanceof IGeneralization)
                                        ? (IGeneralization)pCon : null;
            IImplementation pImp  = (pCon instanceof IImplementation)
                                        ? (IImplementation)pCon : null;

           if ( pGen != null )
           {
                IClassifier pEnd = pGen.getGeneral();
                if ( pEnd != null )
                {
                    m_OldTo = pEnd;
                }
                pEnd = null;
                pEnd = pGen.getSpecific();
                if ( pEnd != null )
                {
                    m_OldFrom = pEnd;
                }
            }
            else if ( pImp != null )
            {
                IClassifier pInt = pImp.getContract();
                if ( pInt != null )
                {
                    m_OldTo = pInt;
                }
                INamedElement pEnd = pImp.getSupplier();
                if ( pEnd != null )
                {
                    m_OldFrom = pEnd;
                }
            }

            if ( detail == RequestDetailKind.RDT_RELATION_DELETED )
            {
                m_IsDelete = true;
            }
            else
            {
                IElement pFrom = pRel.getFrom();
                if ( pFrom != null )
                {
                    m_FromIsChanging = true;
                }

                IElement pTo = pRel.getTo();
                if ( pTo != null )
                {
                    m_ToIsChanging = true;
                }
            }

            boolean isSame = false;
            if ( m_OldFrom != null )
            {
                isSame = m_OldFrom.isSame(m_ElementWithArtifact);
                if ( isSame )
                {
                    m_ArtifactElementID = IRelationPreRequest.EAID_OLD_FROM;
                }
                else if ( m_OldTo != null )
                {
                    isSame = m_OldTo.isSame(m_ElementWithArtifact);
                    if (isSame)
                    {
                        m_ArtifactElementID = IRelationPreRequest.EAID_OLD_TO;
                    }
                }
            }
        }                
    }
    
    public IRelationProxy getRelation()
    {
        return m_Relation;
    }
    
    public IChangeRequest createChangeRequest(IElement pElement, 
                                                /*ChangeKind*/int type,
                                                /*RequestDetailKind*/int detail)
    {
        if( pElement == null ) return null;

        IChangeRequest newReq = null;
        
        if ( detail == RequestDetailKind.RDT_RELATION_END_MODIFIED ||
               detail == RequestDetailKind.RDT_RELATION_END_ADDED ||
               detail == RequestDetailKind.RDT_RELATION_END_REMOVED ||
               detail == RequestDetailKind.RDT_RELATION_DELETED ||
               detail == RequestDetailKind.RDT_RELATION_CREATED )
        {            

            String elementType = pElement.getElementType();

            if ( "Generalization".equals(elementType) )
            {
                newReq = new GeneralizationChangeRequest();
            }
            else if ( "Implementation".equals(elementType) )
            {
                newReq = new ImplementationChangeRequest();
            }
            else
            {
                // default to base for now.
                newReq = super.createChangeRequest(pElement, type, detail);
            }

            if ( newReq == null)
            {
                // default to base for now.
                newReq = super.createChangeRequest ( pElement, type, detail );
            }

            if ( newReq != null )
            {
                populateChangeRequest ( newReq );

                newReq.setAfter(pElement);
                newReq.setState(type);

                // Now allow the PreRequest object make sure this ChangeRequest is absolutely
                // ready to go...
                preProcessRequest( newReq );                
            }
        }
        else
        {
            newReq = super.createChangeRequest (pElement, type, detail);
        }
        return newReq;
    }
    
    public void populateChangeRequest ( IChangeRequest req )
    {
        if( req == null ) return;
        super.populateChangeRequest ( req );
        req.setRelation ( m_Relation );

        IGeneralizationChangeRequest pGenReq  
                                = (req instanceof IGeneralizationChangeRequest)
                                    ? (IGeneralizationChangeRequest)req : null;
        IImplementationChangeRequest pImpReq  
                                = (req instanceof IImplementationChangeRequest)
                                    ? (IImplementationChangeRequest)req : null;
                                    
        if ( pGenReq != null || pImpReq != null )
        {
            boolean specEffected = false;
            boolean artifactIsFrom = false;

            specEffected = ( m_IsDelete || 
                                m_FromIsChanging || 
                                m_Detail == RequestDetailKind.
                                                RDT_RELATION_CREATED );
            

            if ( m_ArtifactElementID == IRelationPreRequest.EAID_OLD_FROM ||
               m_ArtifactElementID == IRelationPreRequest.EAID_NEW_FROM )
            {
                artifactIsFrom = true;
            }

            if ( pGenReq != null )
            {
                pGenReq.setArtifactIsFrom ( artifactIsFrom );
                pGenReq.setSpecializationEffected ( specEffected );
            }
            else if ( pImpReq != null )
            {
                pImpReq.setArtifactIsFrom ( artifactIsFrom );
                pImpReq.setImplementationEffected ( specEffected );
            }
        }
    }
    
    public boolean postEvent(IRelationProxy pRel)
    {
        boolean retval = false;
        if ( pRel != null && m_Relation != null )
        {
            IElement pRelConnection = pRel.getConnection();
            IElement pMyConnection = m_Relation.getConnection();

            boolean isSame = false;
            if ( pRelConnection != null )
            {
                isSame = pRelConnection.isSame(pMyConnection);
            }
            if ( isSame )
            {
                retval = true;
                IElement pCon = pRel.getConnection();

                IGeneralization pGen  = (pCon instanceof IGeneralization)
                                            ? (IGeneralization)pCon : null;
                IImplementation pImp  = (pCon instanceof IImplementation)
                                            ? (IImplementation)pCon : null;

                if ( pGen != null )
               {
                    IClassifier pEnd = pGen.getGeneral();
                    if ( pEnd != null )
                    {
                        m_NewTo = pEnd;
                    }
                    pEnd = null;
                    pEnd = pGen.getSpecific();
                    if ( pEnd != null )
                    {
                        m_NewFrom = pEnd;
                    }
                }
                else if ( pImp != null )
                {
                    IClassifier pInt = pImp.getContract();
                    if ( pInt != null )
                    {
                        m_NewTo = pInt;
                    }
                    INamedElement pEnd = pImp.getSupplier();
                    if ( pEnd != null )
                    {
                        m_NewFrom = pEnd;
                    }
                }

                if ( m_ArtifactElementID == IRelationPreRequest.EAID_UNKNOWN )
                {
                    isSame = false;
                    if ( m_NewFrom != null )
                    {
                        isSame = m_NewFrom.isSame(m_ElementWithArtifact);
                        if ( isSame )
                        {
                            m_ArtifactElementID = IRelationPreRequest.EAID_NEW_FROM;
                        }
                        else if ( m_NewTo != null )
                        {
                            if ( m_NewTo.isSame(m_ElementWithArtifact) )
                            {
                                m_ArtifactElementID = IRelationPreRequest.EAID_NEW_TO;
                            }
                        }
                    }
                }
            }
        }
        return retval;
    }
    
    public boolean postEvent(IElement pElement)
    {
       return false;
    }
    
    public void preProcessRequest ( IChangeRequest pReq )
    {
        // No valid implementation in the C++ code base.        
    }
}
