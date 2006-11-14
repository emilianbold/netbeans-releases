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
import org.netbeans.api.visual.layout.SceneLayout;

/**
 * @author David Kaspar
 */
public final class DevolveWidgetLayout extends SceneLayout {

    private Widget widget;
    private Layout devolveLayout;
    private boolean animate;

    public DevolveWidgetLayout (Widget widget, Layout devolveLayout, boolean animate) {
        super (widget.getScene ());
        assert devolveLayout != null;
        this.widget = widget;
        this.devolveLayout = devolveLayout;
        this.animate = animate;
    }

    protected void performLayout () {
        devolveLayout.layout (widget);
        for (Widget child : widget.getChildren ()) {
            if (animate)
                widget.getScene ().getSceneAnimator ().animatePreferredLocation (child, child.getLocation ());
            else
                child.setPreferredLocation (child.getLocation ());
            child.revalidate ();
        }
    }

}
