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
 * This interface should be implemented to define the criteria
 * used to search for a component.
 * @see org.netbeans.jemmy.ComponentSearcher
 * @see org.netbeans.jemmy.WindowWaiter
 * 
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public interface ComponentChooser {
    /**
     * Check if the component argument meets the search criteria.
     * @param comp Component to check.
     * @return <code>true</code> when the component conforms to
     * the search criteria; <code>false</code> otherwise.
     */
    public boolean checkComponent(Component comp);

    /**
     * Returns searched component description.
     */
    public String getDescription();
}
