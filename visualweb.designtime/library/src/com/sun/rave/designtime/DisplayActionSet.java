/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
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
