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
 * File       : RequestPlug.java
 * Created on : Oct 29, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

/**
 * @author Aztec
 */
public class RequestPlug implements IRequestPlug
{
    private IPlugManager m_PlugManager;

    RequestPlug()
    {
       //m_Plugged = false;
    }

    RequestPlug (IPlugManager manager)
    {
       //m_Plugged = false;
       plug (manager);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IRequestPlug#plug(org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.PlugManager)
     */
    public void plug(IPlugManager pProcessor)
    {
        if(pProcessor != null && !pProcessor.isPluged())
        {
            pProcessor.plug();
            m_PlugManager = pProcessor;
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IRequestPlug#unPlug()
     */
    public void unPlug()
    {
        if(m_PlugManager != null && m_PlugManager.isPluged())
        {
            m_PlugManager.unPlug();
        }
    }

}
