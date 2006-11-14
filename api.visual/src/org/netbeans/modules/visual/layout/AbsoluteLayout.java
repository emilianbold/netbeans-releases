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
