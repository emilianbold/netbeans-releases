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

/**
 * 
 * Any class which contains methods requiring waiting or
 * sleeping should implement this interface.  Waiting and
 * sleeping operations have time limits that can be set or
 * returned using the methods of this interface.
 *	
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public interface Timeoutable {
    /**
     * Defines current timeouts.
     * @param t A collection of timeout assignments.
     * @see #getTimeouts
     */
    public void setTimeouts(Timeouts t);

    /**
     * Return current timeouts.
     * @return the collection of current timeout assignments.
     * @see #setTimeouts
     */
    public Timeouts getTimeouts();
}
