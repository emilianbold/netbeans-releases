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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
