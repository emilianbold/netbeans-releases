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
package org.netbeans.api.visual.laf;

import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.model.ObjectState;

import java.awt.*;

/**
 * @author David Kaspar
 */
public abstract class LookFeel {

    public abstract Paint getBackground ();

    public abstract Color getForeground ();

    public abstract Border getBorder (ObjectState state);

    public abstract Border getMiniBorder (ObjectState state);

    public abstract boolean getOpaque (ObjectState state);

    public abstract Paint getBackground (ObjectState state);

    public abstract Color getForeground (ObjectState state);

    // TODO - is naming correct?
    public abstract int getMargin ();
/*
    public void updateWidget (Widget widget, WidgetState state) {
        widget.setBorder (getBorder (state));
        widget.setOpaque (getOpaque (state));
        widget.setBackground (getBackground (state));
        widget.setForeground (getForeground (state));
    }
*/
}
