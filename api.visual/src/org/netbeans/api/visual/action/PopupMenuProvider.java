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

import javax.swing.*;
import java.awt.*;

/**
 * @author David Kaspar
 */
public interface PopupMenuProvider {

    /**
     * Get a JPopupMenu to display in the context of the given Widget.
     * This method may return <code>null</code>.  If that is the case,
     * no popup menu will be displayed if this PopupMenuAction gets
     * a valid popup trigger on the given Widget.
     * @param widget the widget
     * @param localLocation the local location where the popup menu was invoked
     * @return The JPopupMenu to display for the given Widget.
     *         May be <code>null</code>.
     */
    public JPopupMenu getPopupMenu (Widget widget, Point localLocation);

}
