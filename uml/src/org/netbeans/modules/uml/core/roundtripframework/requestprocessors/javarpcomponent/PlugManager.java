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
 * File       : PlugManager.java
 * Created on : Oct 29, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

/**
 * @author Aztec
 */
public class PlugManager implements IPlugManager
{
    private int m_PlugCount = 0;

    public PlugManager()
    {
        m_PlugCount = 0;
    }

    public PlugManager(IPlugManager rhs)
    {
        copy(rhs);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IPlugManager#copy(org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IPlugManager)
     */
    public void copy(IPlugManager rhs)
    {
        if(this != rhs)
        {
            m_PlugCount = rhs.getPlugCount();
        }
    }
    
    public int getPlugCount()
    {
        return m_PlugCount;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IPlugManager#isPluged()
     */
    public boolean isPluged()
    {
        return m_PlugCount > 0;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IPlugManager#plug()
     */
    public void plug()
    {
        m_PlugCount++;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IPlugManager#unPlug()
     */
    public void unPlug()
    {
        m_PlugCount--;
        if(m_PlugCount < 0)
            m_PlugCount = 0;
    }

}
