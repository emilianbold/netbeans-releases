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

package org.netbeans.jemmy.util;

import org.netbeans.jemmy.Action;
import org.netbeans.jemmy.ComponentChooser;

import java.awt.Component;

/**
 * 
 * Supposed to be used to perform some periodical job.
 *
 * @see WindowManager
 * 
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */
public interface WindowJob extends ComponentChooser, Action {

    /**
     * Perform necessary actions.
     */
    public Object launch(Object obj);

    /**
     * Checks if window is what we want to do something with.
     */
    public boolean checkComponent(Component comp);

    /**
     * Job description.
     */
    public String getDescription();
}
