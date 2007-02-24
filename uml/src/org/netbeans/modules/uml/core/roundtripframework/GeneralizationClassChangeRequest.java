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
 * File       : GeneralizationClassChangeRequest.java
 * Created on : Oct 28, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;

/**
 * @author Aztec
 */
public class GeneralizationClassChangeRequest
    extends GeneralizationChangeRequest
    implements IGeneralizationClassChangeRequest
{
    IClassifier m_BeforeSpecializing = null;
    IClassifier m_AfterSpecializing = null;
    IClassifier m_BeforeGeneralizing = null;
    IClassifier m_AfterGeneralizing = null;
    IElement    m_BeforeConnection = null;
    IElement    m_AfterConnection = null;

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IGeneralizationClassChangeRequest#setAfterConnection(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public void setAfterConnection(IElement pElement)
    {
        m_AfterConnection = pElement;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IGeneralizationClassChangeRequest#setAfterGeneralizing(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void setAfterGeneralizing(IClassifier pClassifier)
    {
        m_AfterGeneralizing = pClassifier;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IGeneralizationClassChangeRequest#setAfterSpecializing(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void setAfterSpecializing(IClassifier pClassifier)
    {
        m_AfterSpecializing = pClassifier;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IGeneralizationClassChangeRequest#setBeforeConnection(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public void setBeforeConnection(IElement pElement)
    {
        m_BeforeConnection = pElement;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IGeneralizationClassChangeRequest#setBeforeGeneralizing(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void setBeforeGeneralizing(IClassifier pClassifier)
    {
        m_BeforeGeneralizing = pClassifier;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IGeneralizationClassChangeRequest#setBeforeSpecializing(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void setBeforeSpecializing(IClassifier pClassifier)
    {
        m_BeforeSpecializing = pClassifier;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IGeneralizationChangeRequest#getAfterConnection()
     */
    public IElement getAfterConnection()
    {
        return m_AfterConnection;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IGeneralizationChangeRequest#getAfterGeneralizing()
     */
    public IClassifier getAfterGeneralizing()
    {
        return m_AfterGeneralizing;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IGeneralizationChangeRequest#getAfterSpecializing()
     */
    public IClassifier getAfterSpecializing()
    {
        return m_AfterSpecializing;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IGeneralizationChangeRequest#getBeforeConnection()
     */
    public IElement getBeforeConnection()
    {
        return m_BeforeConnection;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IGeneralizationChangeRequest#getBeforeGeneralizing()
     */
    public IClassifier getBeforeGeneralizing()
    {
        return m_BeforeGeneralizing;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IGeneralizationChangeRequest#getBeforeSpecializing()
     */
    public IClassifier getBeforeSpecializing()
    {
        return m_BeforeSpecializing;
    }

}
