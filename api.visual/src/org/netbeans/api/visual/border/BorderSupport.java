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

import org.netbeans.modules.visual.border.ResizeBorder;
import org.netbeans.modules.visual.border.SwingBorder;

/**
 * This class contains support method for working with borders.
 *
 * @author David Kaspar
 */
public final class BorderSupport {

    private BorderSupport () {
    }

    /**
     * Returns whether a resize border is outer.
     * @param border the border created by
     * @return true if the border is created the createResizeBorder method as outer parameter set to true; false otherwise
     */
    public static boolean isOuterResizeBorder (Border border) {
        return border instanceof ResizeBorder  &&  ((ResizeBorder) border).isOuter ();
    }
    
    /**
     * Returns a swing border of a border created using BorderFactory.createSwingBorder or Widget.setBorder(javax.swing.border.Border).
     * @param border the widget border
     * @return Swing border if possible; otherwise null
     * @since 2.6
     */
    public static javax.swing.border.Border getSwingBorder (Border border) {
        return border instanceof SwingBorder ? ((SwingBorder) border).getSwingBorder () : null;
    }

}
