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
 * File       : CompoundChangeRequest.java
 * Created on : Nov 6, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class CompoundChangeRequest
    extends ChangeRequest
    implements ICompoundChangeRequest
{
    protected ETList<IChangeRequest> m_Requests = new ETArrayList<IChangeRequest>();
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.ICompoundChangeRequest#add(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest)
     */
    public void add(IChangeRequest pVal)
    {
        if(pVal != null)
            m_Requests.add(pVal);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.ICompoundChangeRequest#getCount()
     */
    public int getCount()
    {
        return m_Requests.size();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.ICompoundChangeRequest#getRequests()
     */
    public ETList<IChangeRequest> getRequests()
    {
        return m_Requests;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.ICompoundChangeRequest#item(int)
     */
    public IChangeRequest item(int index)
    {
        return (index >= 0 && index < getCount())?m_Requests.get(index) : null;
    }

}
