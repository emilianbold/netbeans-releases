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
 * File       : IPreRequest.java
 * Created on : Nov 6, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;

/**
 * @author Aztec
 */
public interface IPreRequest
{
    public boolean postEvent(IRelationProxy pRel);
    
    public boolean postEvent(IElement pElement);

    public IChangeRequest createChangeRequest(IElement pElement, /*ChangeKind*/ int type, /*RequestDetailKind*/ int detail);
    
    public void populateChangeRequest(IChangeRequest req);
    
    /**
     *
     * Called right after a ChangeRequest has been
     * fully intialized, but before it has been processed.
     * This method gives us the ability to handle event
     * specific situations that may affect the ChangeRequest.
     * The base implementation does nothing.
     *
     * @param req[in] The request object to pre process.
     *
     */
   
    public void preProcessRequest(IChangeRequest req);
   
    public IElement preElement();
    
    public IElement origElement();
    
    public IRelationProxy relation();
    
    public IEventPayload payload();
    
    public IElement getPreOwnerElement();    
    public void setPreOwnerElement(IElement element );

    public void setOrigElement(IElement val);
    public IElement getOrigElement();

    public void setDupeElement(IElement val);
    public IElement getDupeElement();

    public INamespace getModifiedNamespace();
    public void setModifiedNamespace(INamespace newVal);

    public IElement getElementWithArtifact();

    public IRequestProcessor getRequestProcessor(IRequestProcessor proc);
    
    public String getFileName();
    
    public String getLanguage();
    
    public int getDetail();
    
    public boolean inCreateState(IElement element);

}
