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
 * Defines char-to-key binding.  The generation of a symbol will,
 * in general, require modifier keys to be pressed prior to pressing
 * a primary key.  Classes that implement <code>CharBindingMap</code>
 * communicate what modifiers and primary key are required to generate
 * a given symbol.
 * @see org.netbeans.jemmy.DefaultCharBindingMap
 * 
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public interface CharBindingMap {

    /**
     * Returns the code of the primary key used to type a symbol.
     * @param c Symbol code.
     * @return a key code.
     * @see java.awt.event.InputEvent
     */
    public int getCharKey(char c);

    /**
     * Returns the modifiers that should be pressed to type a symbol.
     * @param c Symbol code.
     * @return a combination of InputEvent MASK fields.
     * @see java.awt.event.InputEvent
     */
    public int getCharModifiers(char c);
}
