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

package org.netbeans.jemmy.drivers;

/**
 * Implements "light" model of driver because does not require to
 * load classes for all supported operator types.
 * @see Driver
 * 
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */
public interface LightDriver {

    /**
     * Returns array of operator classes which are supported by this driver.
     * @return an array of supported operator classes' names.
     */
    public String[] getSupported();
}
