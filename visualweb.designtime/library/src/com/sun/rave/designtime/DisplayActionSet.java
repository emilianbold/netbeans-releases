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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package com.sun.rave.designtime;

/**
 * <p>An extension of the DisplayAction interface that introduces a hierarchy (tree) structure.
 * This allows a DisplayAction to become an arbitrary tree of items.  If displayed in a menu,
 * the DisplayActionSet is either a popup item (if isPopup() returns true), or it is displayed as
 * a flat list of items between separators.  If displayed as a button (in an option dialog), the
 * DisplayActionSet defines a button with a popup menu on it.  Note that the 'invoke()' method will
 * never be called in a DisplayActionSet.</p>
 *
 * <P><B>IMPLEMENTED BY THE COMPONENT AUTHOR</B> - This interface is designed to be implemented by
 * the component (bean) author.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 */
public interface DisplayActionSet extends DisplayAction {

    /**
     * Returns the list of contained DisplayAction objects.  These will either be shown in a popup
     * or a flat list depending on the 'isPopup' return value.
     *
     * @return An array of DisplayAction objects
     */
    public DisplayAction[] getDisplayActions();

    /**
     * Returns <code>true</code> if this DisplayActionSet should be displayed as a pop-up, or
     * <code>false</code> if it is should be represented as a flat container (for example, between
     * separators in a context menu).
     *
     * @return <code>true</code> if this DisplayActionSet should be displayed as a pop-up, or
     *         <code>false</code> if it is should be represented as a flat container (for example,
     *         between separators in a context menu)
     */
    public boolean isPopup();
}
