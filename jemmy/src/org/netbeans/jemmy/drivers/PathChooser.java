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
 * Specifies an interface for objects defining path searching criteria.
 *
 * @author Alexandre Iline(alexandre.iline@sun.com)
 */
public interface PathChooser {

    /**
     * Checks if <code>depth</code>'th path components fits the requirements.
     * @param depth A depth of the component.
     * @param component A path component to be checked.
     * @return true if the component fits the requirements.
     */
    public boolean checkPathComponent(int depth, Object component);

    /**
     * Return requiered depth of the path.
     * @return depth.
     */
    public int getDepth();
}
