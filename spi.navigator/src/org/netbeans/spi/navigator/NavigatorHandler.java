/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.spi.navigator;

import org.netbeans.modules.navigator.NavigatorTC;

/**
 * Set of methods for driving navigator behaviour. 
 *
 * @author Dafe Simonek
 */
public final class NavigatorHandler {
    
    /** No external instantiation allowed.
     */
    private NavigatorHandler () {
    }
    
    /** 
     * Activates and shows given panel in navigator view. Panel must be one of  
     * available panels at the time this method is called, which means that 
     * panel must be registered (either through mime type in xml layer or NavigatorLookupHint)
     * for currently activated node in the system.  
     * Previously activated panel is deactivated and hidden.
     * <p>
     * Typical use case is to set preferred navigator panel in a situation 
     * when multiple panels are registered for multiple data types.   
     * <p>
     * This method must be called from EventQueue thread.
     * 
     * @param panel Navigator panel to be activated
     * @throws IllegalArgumentException if given panel is not available 
     */ 
    public static void activatePanel (NavigatorPanel panel) {
        NavigatorTC.getInstance().getController().activatePanel(panel);
    }
    
}
