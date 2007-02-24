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
 * File       : IRoundTripModeRestorer.java
 * Created on : Nov 20, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;

/**
 * @author Aztec
 */
public class RoundTripModeRestorer
{
    private int m_originalMode;

    protected void initialize()
    {
        getMode();
    }

    protected IRoundTripController getRoundTripController()
    {
        return ProductRetriever.retrieveProduct().getRoundTripController();
    }
    
    /** 
     * Creates a RoundTripModeRestorer object
     * 
     */
    public RoundTripModeRestorer()
    {
        initialize();
    }


    /** 
     * Creates a RoundTripModeRestorer object and immediately enters @a enterThisMode
     * 
     * @param enterThisMode[in] the mode you would like to enter
     */
    public RoundTripModeRestorer(int enterThisMode)
    {
        initialize();
        setMode(enterThisMode);
    }

    
    public void setMode(int mode)
    {
        IRoundTripController pController = getRoundTripController();
        pController.setMode(mode);    
    }
    
    public int getMode()
    {
        IRoundTripController pController = getRoundTripController();
        if(pController != null)
        m_originalMode = pController.getMode();
        return m_originalMode;
    }

    public void restoreOriginalMode()
    {
        setMode(m_originalMode);
    }
}
