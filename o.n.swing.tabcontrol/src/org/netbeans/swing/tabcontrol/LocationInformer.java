/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.swing.tabcontrol;

import java.awt.Component;

/**
 * Interface that provides information about global position of tab control
 * in more complex window system, such as Netbeans has.<p>
 *
 * Tab control uses location info to provide look and feel that is changing
 * as global location of tab control changes. Icon of pin button is an example.  
 *
 * @see TabbedContainer#TabbedContainer
 *
 * @author Dafe Simonek
 */
public interface LocationInformer {
    
    /** Returns global orientation of given component.
     *
     * @return Orientation of component, as defined in
     * TabDisplayer.ORIENTATION_XXX constants
     */
    public Object getOrientation (Component comp);
    
}
