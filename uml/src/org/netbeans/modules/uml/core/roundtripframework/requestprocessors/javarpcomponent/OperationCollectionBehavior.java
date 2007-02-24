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
 * File       : OperationCollectionBehavior.java
 * Created on : Oct 29, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

/**
 * @author Aztec
 */
public class OperationCollectionBehavior
    implements IOperationCollectionBehavior
{
    private boolean m_AbstractOnly;
    private boolean m_InterfacesOnly;
    private boolean m_Silent;
    private boolean m_SilentSelectAll = true;
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IOperationCollectionBehavior#getAbstractOnly()
     */
    public boolean getAbstractOnly()
    {
        return m_AbstractOnly;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IOperationCollectionBehavior#getInterfacesOnly()
     */
    public boolean getInterfacesOnly()
    {
        return m_InterfacesOnly;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IOperationCollectionBehavior#getSilent()
     */
    public boolean getSilent()
    {
        return m_Silent;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IOperationCollectionBehavior#getSilentSelectAll()
     */
    public boolean getSilentSelectAll()
    {
        return m_SilentSelectAll;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IOperationCollectionBehavior#setAbstractOnly(boolean)
     */
    public void setAbstractOnly(boolean absOnly)
    {
        m_AbstractOnly = absOnly;        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IOperationCollectionBehavior#setInterfacesOnly(boolean)
     */
    public void setInterfacesOnly(boolean infOnly)
    {
        m_InterfacesOnly = infOnly;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IOperationCollectionBehavior#setSilent(boolean)
     */
    public void setSilent(boolean silent)
    {
        m_Silent = silent;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IOperationCollectionBehavior#setSilentSelectAll(boolean)
     */
    public void setSilentSelectAll(boolean silentSelectAll)
    {
        m_SilentSelectAll = silentSelectAll;
    }
}
