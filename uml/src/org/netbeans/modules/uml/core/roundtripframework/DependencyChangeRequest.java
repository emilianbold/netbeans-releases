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
 * File       : DependencyChangeRequest.java
 * Created on : Nov 20, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;

/**
 * @author Aztec
 */
public class DependencyChangeRequest
    extends ChangeRequest
    implements IDependencyChangeRequest
{
    private IElement m_IndependentElement;
    private String m_IndependentElementName;


    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IDependencyChangeRequest#getIndependentElement()
     */
    public IElement getIndependentElement()
    {
        return m_IndependentElement;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IDependencyChangeRequest#getIndependentElementName()
     */
    public String getIndependentElementName()
    {
        return m_IndependentElementName;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IDependencyChangeRequest#setIndependentElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public void setIndependentElement(IElement newVal)
    {
        m_IndependentElement = newVal;

        if ( newVal != null )
        {
            INamedElement pNamed = (newVal instanceof INamedElement)
                                        ? (INamedElement)newVal : null;
            if ( pNamed != null )
            {
                String newName = pNamed.getQualifiedName2();
                setIndependentElementName(newName);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IDependencyChangeRequest#setIndependentElementName(java.lang.String)
     */
    public void setIndependentElementName(String newVal)
    {
        m_IndependentElementName = newVal;
    }

}
