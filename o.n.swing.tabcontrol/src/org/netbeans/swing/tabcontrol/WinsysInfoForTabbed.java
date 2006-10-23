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

package org.netbeans.swing.tabcontrol;

import java.awt.Component;

/**
 * Interface that provides external information (provided by window system)
 * that TabbedContainers need to know in order to work fully.<p>
 *
 * Tab control uses info to provide for example tab buttons reacting on 
 * the position of the container or on maximization state.
 *
 * @see TabbedContainer#TabbedContainer
 *
 * @author Dafe Simonek
 */
public interface WinsysInfoForTabbed {

    /** Returns global orientation of given component.
     *
     * @return Orientation of component, as defined in
     * TabDisplayer.ORIENTATION_XXX constants
     */
    public Object getOrientation (Component comp);
    
    /** Finds out in what state is window system mode containing given component.
     * 
     * @return true if given component is inside mode which is in maximized state,
     * false otherwise 
     */
    public boolean inMaximizedMode (Component comp);
    
}
