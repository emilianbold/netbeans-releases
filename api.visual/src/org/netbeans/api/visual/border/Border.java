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
package org.netbeans.api.visual.border;

import java.awt.*;

/**
 * The class is responsible for defining and rendering borders. Border size is defined by insets.
 * <p>
 * Borders can be opaque. If true, then the border has to care about painting all pixels in borders.
 * If false, then the widget background is painted under borders too.
 * <p>
 * This can be used for non-rectagular shapes of borders e.g. returning true from isOpaque and drawing a filled rounded rectangle.
 *
 * @author David Kaspar
 */
// TODO - change abstract class to assure immutable insets?
public interface Border {

    /**
     * Returns border insets. Insets has to be the same during whole life-cycle of the border.
     * @return the insets
     */
    // WARNING - must be immutable during whole lifecycle
    public Insets getInsets ();

    /**
     * Paints the border to the Graphics2D instance within specific bounds.
     * Borders are always painted immediately after the widget background and before the widget painting itself.
     * @param gr the Graphics2D instance
     * @param bounds the boundary
     */
    public void paint (Graphics2D gr, Rectangle bounds);

    /**
     * Returns whether the border is opaque. The result of the method controls whether a widget background is painted
     * under the border insets too.
     * @return true, if background is painted under the border insets.
     */
    public boolean isOpaque ();

}
