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
 * File       : ElementContextPayload.java
 * Created on : Nov 10, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;

/**
 * @author Aztec
 */
public class ElementContextPayload
    extends RoundTripEventPayload
    implements IElementContextPayload
{
    protected IProject m_Project = null;
    protected IElement m_Owner = null;

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IElementContextPayload#getOwner()
     */
    public IElement getOwner()
    {
        return m_Owner;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IElementContextPayload#getProject()
     */
    public IProject getProject()
    {
        return m_Project;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IElementContextPayload#setOwner(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public void setOwner(IElement pOwner)
    {
        m_Owner = pOwner;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IElementContextPayload#setProject(org.netbeans.modules.uml.core.metamodel.structure.IProject)
     */
    public void setProject(IProject pProject)
    {
        m_Project = pProject;
    }

}
