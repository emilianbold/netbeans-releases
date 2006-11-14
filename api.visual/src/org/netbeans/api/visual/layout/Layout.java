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
package org.netbeans.api.visual.layout;

import org.netbeans.api.visual.widget.Widget;

/**
 * This class is responsible for layout and justification of children widgets of a widget where the layout is assigned.
 * Built-in layouts could be created by LayoutFactory class.
 *
 * @author David Kaspar
 */
public interface Layout {

    /**
     * Resolve bounds of widget children based in their preferred locations and bounds.
     * @param widget the widget
     */
    public void layout (Widget widget);

    /**
     * Resolve whether a widget requires justification after whole scene layout.
     * @param widget the widget
     * @return true if requires justification
     */
    public boolean requiresJustification (Widget widget);

    /**
     * Justify bounds of widget children based on a widget client area.
     * @param widget the widget
     */
    public void justify (Widget widget);

}
