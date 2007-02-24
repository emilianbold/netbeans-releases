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
 * File       : NameModifyPreRequest.java
 * Created on : Nov 6, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;

/**
 * @author Aztec
 */
public class NameModifyPreRequest
    extends PreRequest
    implements INameModifyPreRequest
{
    public NameModifyPreRequest()
    {
        super();
    }

    public NameModifyPreRequest(IElement preElement,
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
    
    /**
     *
     * Checks to see if the pre-element is named to the default name
     * ( e.g., "< un-named >" ) and then checks the name of the post element.
     * If there is a difference, then we are really in a class create
     * state.
     *
     * @param req[in] The request
     *
     * @return HRESULT
     *
     */

//    public void preProcessRequest(IChangeRequest req)
//    {
//       super.preProcessRequest(req);
//    }

    /**
     *
     * Determines whether or not a name change of an INamedElement will put us
     * into a create element state as far as RoundTrip is concerned.
     *
     * @param preElement[in] The element to check. This should be an element
     *                       in pre-modify state.
     *
     * @return true if we should go into the create state, else false
     *
     */

//    public boolean inCreateState(IElement preElement)
//    {
//       return super.inCreateState(preElement);
//    }
}
