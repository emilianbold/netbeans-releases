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

import javax.swing.*;
import java.awt.*;

/**
 * This interface provides a popup menu.
 *
 * @author William Headrick, David Kaspar
 */
public interface PopupMenuProvider {

    /**
     * Get a JPopupMenu to display in the context of the given Widget.
     * This method may return <code>null</code>.  If that is the case,
     * no popup menu will be displayed if this PopupMenuAction gets
     * a valid popup trigger on the given Widget.
     * @param widget the widget
     * @param localLocation the local location where the popup menu was invoked; if null, then popup menu is invoked by a keyboard
     * @return The JPopupMenu to display for the given Widget.
     *         May be <code>null</code>.
     */
    public JPopupMenu getPopupMenu (Widget widget, Point localLocation);

}
