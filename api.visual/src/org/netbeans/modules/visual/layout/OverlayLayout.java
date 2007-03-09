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
package org.netbeans.modules.visual.layout;

import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;

/**
 * @author David Kaspar
 */
public class OverlayLayout implements Layout {

    public void layout (Widget widget) {
        Dimension total = new Dimension ();
        for (Widget child : widget.getChildren ()) {
            if (! child.isVisible ())
                continue;
            Dimension size = child.getPreferredBounds ().getSize ();
            if (size.width > total.width)
                total.width = size.width;
            if (size.height > total.height)
                total.height = size.height;
        }
        for (Widget child : widget.getChildren ()) {
            Point location = child.getPreferredBounds ().getLocation ();
            child.resolveBounds (new Point (- location.x, - location.y), new Rectangle (location, total));
        }
    }

    public boolean requiresJustification (Widget widget) {
        return true;
    }

    public void justify (Widget widget) {
        Rectangle clientArea = widget.getClientArea ();
        for (Widget child : widget.getChildren ()) {
            if (child.isVisible ()) {
                Point location = child.getPreferredBounds ().getLocation ();
                child.resolveBounds (new Point (clientArea.x - location.x, clientArea.y - location.y), new Rectangle (location, clientArea.getSize ()));
            } else {
                child.resolveBounds (clientArea.getLocation (), new Rectangle ());
            }
        }
    }

}
