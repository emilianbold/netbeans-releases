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
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
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
