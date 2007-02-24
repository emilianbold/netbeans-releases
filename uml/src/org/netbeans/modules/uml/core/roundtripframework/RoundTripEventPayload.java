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
 * File       : RoundTripEventPayload.java
 * Created on : Nov 10, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.eventframework.EventPayload;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class RoundTripEventPayload
    extends EventPayload
    implements IRoundTripEventPayload
{
    protected Object m_Data = null;
    protected ETList<IChangeRequest> m_Reqs = null;

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventPayload#getChangeRequests()
     */
    public ETList<IChangeRequest> getChangeRequests()
    {
        return m_Reqs;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventPayload#getData()
     */
    public Object getData()
    {
        return m_Data;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventPayload#setChangeRequests(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest[])
     */
    public void setChangeRequests(ETList<IChangeRequest> requests)
    {
        m_Reqs = requests;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventPayload#setData(java.lang.Object)
     */
    public void setData(Object value)
    {
        m_Data = value;
    }

}
