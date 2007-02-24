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
 * File       : PreRequestFactory.java
 * Created on : Nov 11, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;

/**
 * @author Aztec
 */
public class PreRequestFactory
{
    /**
     *
     * Creates the appropriate PreRequest type given the pre event
     *
     * @param preEvent[in] The name of the preevent.
     * @param preElement[in] The pre element
     * @param proc[in] The associated RequestProcessor
     * @param file[in] The file that will be modified
     *
     * @return HRESULT
     *
     */

    public static IPreRequest createPreRequest(/*RequestDetailKind*/int preEvent,
                                                     IElement preElement,
                                                     IElement pClone,
                                                     IElement pElementWithArtifact,
                                                     IRequestProcessor proc,
                                                     IEventPayload payload,
                                                     IRelationProxy proxy )
    {
        IPreRequest request = null;

        if (proxy == null)
        {
            if (preEvent == RequestDetailKind.RDT_NAME_MODIFIED)
            {
                IPackage pPackage = null;
                try
                {
                    pPackage = (IPackage)preElement;
                }
                catch(Exception e){}
                if (pPackage == null)
                {
                    request = new NameModifyPreRequest(preElement, pClone, pElementWithArtifact, proc, preEvent, payload, null);
                }
                else
                {
                    request = new NameSpaceModifyPreRequest ( preElement, pClone, pElementWithArtifact, proc, preEvent, payload, null);
                }
            }
            else if ( preEvent == RequestDetailKind.RDT_NAMESPACE_MODIFIED ||
                    preEvent == RequestDetailKind.RDT_CHANGED_NAMESPACE ||
                    preEvent == RequestDetailKind.RDT_NAMESPACE_MOVED ||
                    preEvent == RequestDetailKind.RDT_SOURCE_DIR_CHANGED )
            {
                request = new NameSpaceModifyPreRequest (preElement, pClone, pElementWithArtifact, proc, preEvent, payload,null);
            }
            else if ( preEvent == RequestDetailKind.RDT_FEATURE_DUPLICATED )
            {
                request = new ElementDuplicatedPreRequest ( preElement, 
                        pElementWithArtifact, pElementWithArtifact, proc, 
                        preEvent, payload, null);
            }
            else if ( preEvent == RequestDetailKind.RDT_TRANSFORM )
            {
                request = new TransformPreRequest ( preElement, pClone, pElementWithArtifact, proc, preEvent, payload, null );
            }
            else
            {
				if (pClone instanceof IParameter)
				{	
					IElement ownerClone = (IElement)
						(pClone.getOwner() != null && pClone.getOwner().getNode() != null?
								FactoryRetriever.instance().clone(
										pClone.getOwner().getNode()) : null);

					if (ownerClone == null && preElement.getOwner() != null && 
							preElement.getOwner().getNode() != null)
						ownerClone = (IElement) FactoryRetriever.instance().clone(
								preElement.getOwner().getNode());
            			
					request = new PreRequest( preElement, pClone, 
							pElementWithArtifact, proc, preEvent, payload,
							ownerClone);
				}
				else
                request = new PreRequest( preElement, pClone, pElementWithArtifact, proc, preEvent, payload, null );
            }
        }
        else
        {
            request = new RelationPreRequest ( preElement, pClone, pElementWithArtifact, proxy, proc, preEvent, payload, null);
        }
        return request;
    }


    /**
     *
     * 
     *
     * @param preEvent[in]
     * @param preElement[in]
     * @param proc[in]
     * @param file[in]
     * @param payload[in]
     * @param preOwner[in]
     *
     * @return 
     *
     */

    public static IPreRequest createPreRequest(/*RequestDetailKind*/int preEvent,
                                                     IElement preElement,
                                                     IElement pClone,
                                                     IElement pElementWithArtifact,
                                                     IRequestProcessor proc,
                                                     IEventPayload payload,
                                                     IElement preOwner )
    {
        IPreRequest request = null;

        if ( preEvent == RequestDetailKind.RDT_NAME_MODIFIED)
        {
            IPackage pPackage = null;
            try
            {
                pPackage = (IPackage)preElement;
            }
            catch(Exception e){}
            if (pPackage == null)
            {
                request = new NameModifyPreRequest( preElement, pClone, pElementWithArtifact, proc, preEvent, payload, preOwner );
            }
            else
            {
                request = new NameSpaceModifyPreRequest ( preElement, pClone, pElementWithArtifact, proc, preEvent, payload, preOwner );
            }
        }
        else if ( preEvent == RequestDetailKind.RDT_NAMESPACE_MODIFIED ||
                 preEvent == RequestDetailKind.RDT_CHANGED_NAMESPACE ||
                 preEvent == RequestDetailKind.RDT_NAMESPACE_MOVED )
        {
             request = new NameSpaceModifyPreRequest ( preElement, pClone, pElementWithArtifact, proc, preEvent, payload, preOwner );
        }
        else if ( preEvent == RequestDetailKind.RDT_FEATURE_DUPLICATED )
        {
            request = new ElementDuplicatedPreRequest ( preElement, null, pElementWithArtifact, proc, preEvent, payload, null );
        }
        else if ( preEvent == RequestDetailKind.RDT_TRANSFORM )
        {
            request = new TransformPreRequest ( preElement, pClone, pElementWithArtifact, proc, preEvent, payload, preOwner );
        }
        else
        {
            request = new PreRequest( preElement, pClone, pElementWithArtifact, proc, preEvent, payload, preOwner );
        }    

        return request;
    }


    /**
     *
     * 
     *
     * @param preEvent[in]
     * @param impactedElement[in]
     * @param pClassifier[in]
     * @param proc[in]
     * @param payload[in]
     *
     * @return 
     *
     */

    public static IPreRequest createImpactedPreRequest(/*RequestDetailKind*/int  preEvent,
                                            IElement impactedElement,
                                            IClassifier pClassifier,
                                            IRequestProcessor proc,
                                            IEventPayload payload )
    {
        IPreRequest request = null;

        if ( preEvent == RequestDetailKind.RDT_TYPE_MODIFIED )
        {
            request = new ImpactedPreRequest (impactedElement, impactedElement, pClassifier, proc, preEvent, payload);
        }
        return request;
    }
}
