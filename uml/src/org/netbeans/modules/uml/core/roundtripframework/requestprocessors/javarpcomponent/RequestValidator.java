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

package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;
import org.netbeans.modules.uml.core.roundtripframework.IChangeRequest;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class RequestValidator implements IRequestValidator
{
    IChangeRequest m_Request;
    boolean m_Valid = false;
    ETList <IRequestValidator> m_AddedRequests = new ETArrayList<IRequestValidator>();
    
    public RequestValidator()
    {        
    }
    
    public RequestValidator(IChangeRequest pRequest )
    {
        m_Request = pRequest;
        m_Valid = true;
    }

    public RequestValidator(IRequestValidator copy)
    {
        m_Request = copy.getRequest();
        m_Valid = true;
    }    

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IRequestValidator#addRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest)
     */
    public void addRequest(IChangeRequest request)
    {
        if (request != null)
        {
            m_AddedRequests.add(new RequestValidator(request));
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IRequestValidator#appendRequests()
     */
    public ETList<IChangeRequest> appendRequests(ETList <IChangeRequest> requests)
    {
        if(requests == null) return null;
        
        if(m_Request != null && getValid())
        {
            requests.add(m_Request);
        }
        int count = m_AddedRequests.size();
        for(int i = 0 ; i < count ; ++i)
        {
			IRequestValidator req = m_AddedRequests.get(i);
			if (req != null)
				req.appendRequests(requests);
        }
        return requests;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IRequestValidator#getRequest()
     */
    public IChangeRequest getRequest()
    {
        return m_Request;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IRequestValidator#getValid()
     */
    public boolean getValid()
    {
        return m_Valid;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IRequestValidator#setValid(boolean)
     */
    public void setValid(boolean valid)
    {
        m_Valid = valid;
    }
    
    public boolean isRelation()
    {
		boolean retval = false;
		if ( m_Request != null )
		{
		   IRelationProxy pRel = m_Request.getRelation();
		   if ( pRel != null )
		   {
			  retval = true;
		   }
		}

		return retval;
    }

}
