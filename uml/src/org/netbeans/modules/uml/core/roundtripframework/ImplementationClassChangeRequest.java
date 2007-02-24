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
 * File       : ImplementationClassChangeRequest.java
 * Created on : Oct 28, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;

/**
 * @author Aztec
 */
public class ImplementationClassChangeRequest
    extends ImplementationChangeRequest
    implements IImplementationClassChangeRequest
{
    IClassifier m_BeforeImplementing = null;
    IClassifier m_AfterImplementing = null;
    IClassifier m_BeforeInterface = null;
    IClassifier m_AfterInterface = null;
    IElement    m_BeforeConnection = null;
    IElement    m_AfterConnection = null;
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IImplementationClassChangeRequest#setAfterConnection(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public void setAfterConnection(IElement pElement)
    {
        m_AfterConnection = pElement;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IImplementationClassChangeRequest#setAfterImplementing(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void setAfterImplementing(IClassifier pClassifier)
    {
        m_AfterImplementing = pClassifier;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IImplementationClassChangeRequest#setAfterInterface(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void setAfterInterface(IClassifier pClassifier)
    {
        m_AfterInterface = pClassifier;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IImplementationClassChangeRequest#setBeforeConnection(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public void setBeforeConnection(IElement pElement)
    {
        m_BeforeConnection = pElement;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IImplementationClassChangeRequest#setBeforeImplementing(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void setBeforeImplementing(IClassifier pClassifier)
    {
        m_BeforeImplementing = pClassifier;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IImplementationClassChangeRequest#setBeforeInterface(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void setBeforeInterface(IClassifier pClassifier)
    {
        m_BeforeInterface = pClassifier;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IImplementationChangeRequest#getAfterConnection()
     */
    public IElement getAfterConnection()
    {
        return m_AfterConnection;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IImplementationChangeRequest#getAfterImplementing()
     */
    public IClassifier getAfterImplementing()
    {
        return m_AfterImplementing;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IImplementationChangeRequest#getAfterInterface()
     */
    public IClassifier getAfterInterface()
    {
        return m_AfterInterface;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IImplementationChangeRequest#getBeforeConnection()
     */
    public IElement getBeforeConnection()
    {
        return m_BeforeConnection;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IImplementationChangeRequest#getBeforeImplementing()
     */
    public IClassifier getBeforeImplementing()
    {
        return m_BeforeImplementing;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IImplementationChangeRequest#getBeforeInterface()
     */
    public IClassifier getBeforeInterface()
    {
        return m_BeforeInterface;
    }

}
