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
package org.netbeans.api.visual.vmd;

import org.netbeans.api.visual.border.Border;
import org.netbeans.modules.visual.vmd.VMDNetBeans60ColorScheme;
import org.netbeans.modules.visual.vmd.VMDOriginalColorScheme;

import java.awt.*;

/**
 * Used as a factory class for objects defined in VMD visualization style.
 *
 * @author David Kaspar
 */
public final class VMDFactory {

    private static VMDColorScheme SCHEME_ORIGINAL = new VMDOriginalColorScheme ();
    private static VMDColorScheme SCHEME_NB60 = new VMDNetBeans60ColorScheme ();

    private VMDFactory () {
    }

    /**
     * Creates the original vmd color scheme. Used by default.
     * @return the color scheme
     * @since 2.5
     */
    public static VMDColorScheme getOriginalScheme () {
        return SCHEME_ORIGINAL;
    }

    /**
     * Creates the NetBeans 6.0 vmd color scheme.
     * @return the color scheme
     * @since 2.5
     */
    public static VMDColorScheme getNetBeans60Scheme () {
        return SCHEME_NB60;
    }

    /**
     * Creates a border used by VMD node.
     * @return the VMD node border
     */
    public static Border createVMDNodeBorder () {
        return VMDOriginalColorScheme.BORDER_NODE;
    }

    /**
     * Creates a border used by VMD node with a specific colors.
     * @return the VMD node border
     * @param borderColor the border color
     * @param borderThickness the border thickness
     * @param color1 1. color of gradient background
     * @param color2 2. color of gradient background
     * @param color3 3. color of gradient background
     * @param color4 4. color of gradient background
     * @param color5 5. color of gradient background
     * @since 2.5
     */
    public static Border createVMDNodeBorder (Color borderColor, int borderThickness, Color color1, Color color2, Color color3, Color color4, Color color5) {
        return new VMDNodeBorder (borderColor, borderThickness, color1, color2, color3, color4, color5);
    }

}
