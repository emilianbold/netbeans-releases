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

package org.netbeans.modules.options.colors;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;

/**
 * Represents one color with some text description.
 *
 * @author Administrator
 */
class ColorValue {

    public static final ColorValue  CUSTOM_COLOR =
            new ColorValue (loc ("Custom"), null); //NOI18N

    private static Map colorMap = new HashMap ();
    static {
        colorMap.put (Color.BLACK,      loc ("Black"));         //NOI18N
        colorMap.put (Color.BLUE,       loc ("Blue"));          //NOI18N
        colorMap.put (Color.CYAN,       loc ("Cyan"));          //NOI18N
        colorMap.put (Color.DARK_GRAY,  loc ("Dark_Gray"));     //NOI18N
        colorMap.put (Color.GRAY,       loc ("Gray"));          //NOI18N
        colorMap.put (Color.GREEN,      loc ("Green"));         //NOI18N
        colorMap.put (Color.LIGHT_GRAY, loc ("Light_Gray"));    //NOI18N
        colorMap.put (Color.MAGENTA,    loc ("Magenta"));       //NOI18N
        colorMap.put (Color.ORANGE,     loc ("Orange"));        //NOI18N
        colorMap.put (Color.PINK,       loc ("Pink"));          //NOI18N
        colorMap.put (Color.RED,        loc ("Red"));           //NOI18N
        colorMap.put (Color.WHITE,      loc ("White"));         //NOI18N
        colorMap.put (Color.YELLOW,     loc ("Yellow"));        //NOI18N
    }
    
    String text;
    Color color;

    ColorValue (Color color) {
        this.color = color;
        text = (String) colorMap.get (color);
        if (text != null) return;
        StringBuffer sb = new StringBuffer ();
        sb.append ('[').append (color.getRed ()).
            append (',').append (color.getGreen ()).
            append (',').append (color.getBlue ()).
            append (']');
        text = sb.toString ();
    }

    ColorValue (String text, Color color) {
        this.text = text;
        this.color = color;
    }
    
    private static String loc (String key) {
        return NbBundle.getMessage (ColorComboBox.class, key);
    }
}
