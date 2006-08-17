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
package org.netbeans.modules.visual.layout;

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.layout.Layout;

import java.util.Collection;

/**
 * @author David Kaspar
 */
public final class AbsoluteLayout implements Layout {

    public void layout (Widget widget) {
        Collection<Widget> children = widget.getChildren ();
        for (Widget child : children)
            child.resolveBounds (child.getPreferredLocation (), null);
    }

    public boolean requiresJustification (Widget widget) {
        return false;
    }

    public void justify (Widget widget) {
    }

}
