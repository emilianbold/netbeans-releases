/*
 * Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 * 
 * The Original Code is the Jemmy library.
 * The Initial Developer of the Original Code is Alexandre Iline.
 * All Rights Reserved.
 * 
 * Contributor(s): Alexandre Iline.
 * 
 * $Id$ $Revision$ $Date$
 * 
 */

package org.netbeans.jemmy;

import java.awt.Component;

/**
 * 
 * Exception can be thrown as a result of attempt to produce a mouse operation
 * for a component which is not visible.
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class ComponentIsNotVisibleException extends JemmyInputException {
    
    /**
     * Constructs a ComponentIsNotVisibleException object.
     * @param	comp a Component.
     */
    public ComponentIsNotVisibleException(Component comp) {
	super("Component is not visible", comp);
    }

}
