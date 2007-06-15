/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.diff.options;

import org.openide.util.NbBundle;

import java.awt.Color;
import java.util.*;

/**
 * Represents one color with some text description.
 *
 * copied from editor/options.
 * @author Maros Sandor
 */
class ColorValue {

    public static final ColorValue CUSTOM_COLOR =
            new ColorValue(ColorValue.loc("Custom"), null); //NOI18N

    private static Map<Color, String> colorMap = new HashMap<Color, String>();
    static {
        ColorValue.colorMap.put (Color.BLACK,      ColorValue.loc("Black"));         //NOI18N
        ColorValue.colorMap.put (Color.BLUE,       ColorValue.loc("Blue"));          //NOI18N
        ColorValue.colorMap.put (Color.CYAN,       ColorValue.loc("Cyan"));          //NOI18N
        ColorValue.colorMap.put (Color.DARK_GRAY,  ColorValue.loc("Dark_Gray"));     //NOI18N
        ColorValue.colorMap.put (Color.GRAY,       ColorValue.loc("Gray"));          //NOI18N
        ColorValue.colorMap.put (Color.GREEN,      ColorValue.loc("Green"));         //NOI18N
        ColorValue.colorMap.put (Color.LIGHT_GRAY, ColorValue.loc("Light_Gray"));    //NOI18N
        ColorValue.colorMap.put (Color.MAGENTA,    ColorValue.loc("Magenta"));       //NOI18N
        ColorValue.colorMap.put (Color.ORANGE,     ColorValue.loc("Orange"));        //NOI18N
        ColorValue.colorMap.put (Color.PINK,       ColorValue.loc("Pink"));          //NOI18N
        ColorValue.colorMap.put (Color.RED,        ColorValue.loc("Red"));           //NOI18N
        ColorValue.colorMap.put (Color.WHITE,      ColorValue.loc("White"));         //NOI18N
        ColorValue.colorMap.put (Color.YELLOW,     ColorValue.loc("Yellow"));        //NOI18N
    }
    
    String text;
    Color color;

    ColorValue(Color color) {
        this.color = color;
        text = ColorValue.colorMap.get (color);
        if (text != null) return;
        StringBuffer sb = new StringBuffer ();
        sb.append ('[').append (color.getRed ()).
            append (',').append (color.getGreen ()).
            append (',').append (color.getBlue ()).
            append (']');
        text = sb.toString ();
    }

    ColorValue(String text, Color color) {
        this.text = text;
        this.color = color;
    }
    
    private static String loc (String key) {
        return NbBundle.getMessage (ColorComboBox.class, key);
    }
}
