/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.visual.action;

import org.netbeans.api.visual.widget.Widget;

/**
 * This is an inteface for text-field based in-place editor.
 *
 * @author David Kaspar
 */
public interface TextFieldInplaceEditor {

    /**
     * Returns whether the in-place editing is allowed.
     * @param widget the widget where the editor will be invoked
     * @return true, if enabled; false if disabled
     */
    boolean isEnabled (Widget widget);

    /**
     * Returns an initial text of the in-place editor.
     * @param widget the edited widget
     * @return the initial text
     */
    String getText (Widget widget);

    /**
     * Sets a new text approved by an user.
     * @param widget the edited widget
     * @param text the new text entered by an user
     */
    void setText (Widget widget, String text);

}
