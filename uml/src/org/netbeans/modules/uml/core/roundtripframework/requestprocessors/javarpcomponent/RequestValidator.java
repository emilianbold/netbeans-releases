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
