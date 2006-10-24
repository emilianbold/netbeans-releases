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
package org.netbeans.modules.java.editor.semantic;

import java.awt.Color;
import java.awt.Font;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.SettingsDefaults;

/**
 *
 * @author Jan Lahoda
 */
public final class ColoringManager {

    private static Map<ColoringAttributes, Coloring> type2Coloring;

    private static final Font ITALIC = SettingsDefaults.defaultFont.deriveFont(Font.ITALIC);
    private static final Font BOLD = SettingsDefaults.defaultFont.deriveFont(Font.BOLD);
    private static final Font BOLDITALIC = SettingsDefaults.defaultFont.deriveFont(Font.BOLD | Font.ITALIC);
    
    static {
        type2Coloring = new HashMap<ColoringAttributes, Coloring>();
        
        type2Coloring.put(ColoringAttributes.UNUSED, new Coloring(null, 0, Color.GRAY, null));
        type2Coloring.put(ColoringAttributes.ABSTRACT, new Coloring(null, 0, null, null));
        type2Coloring.put(ColoringAttributes.FIELD, new Coloring(BOLD, Coloring.FONT_MODE_APPLY_STYLE, new Color(9, 134, 24), null));
        type2Coloring.put(ColoringAttributes.LOCAL_VARIABLE, new Coloring(null, 0, null, null));
        type2Coloring.put(ColoringAttributes.PARAMETER, new Coloring(null, Coloring.FONT_MODE_APPLY_STYLE, new Color(160, 96, 1), null));
        type2Coloring.put(ColoringAttributes.METHOD, new Coloring(BOLD, Coloring.FONT_MODE_APPLY_STYLE, null, null));
        type2Coloring.put(ColoringAttributes.CONSTRUCTOR, new Coloring(BOLD, Coloring.FONT_MODE_APPLY_STYLE, null, null));
        type2Coloring.put(ColoringAttributes.CLASS, new Coloring(null, 0, null, null));
        type2Coloring.put(ColoringAttributes.DEPRECATED, new Coloring(null, 0, null, null, null, new Color(64, 64, 64)));
        type2Coloring.put(ColoringAttributes.STATIC, new Coloring(ITALIC, Coloring.FONT_MODE_APPLY_STYLE, null, null));
        
        type2Coloring.put(ColoringAttributes.PRIVATE, new Coloring(null, 0, null, null));
        type2Coloring.put(ColoringAttributes.PACKAGE_PRIVATE, new Coloring(null, 0, null, null));
        type2Coloring.put(ColoringAttributes.PROTECTED, new Coloring(null, 0, null, null));
        type2Coloring.put(ColoringAttributes.PUBLIC, new Coloring(null, 0, null, null));
        
        type2Coloring.put(ColoringAttributes.TYPE_PARAMETER_DECLARATION, new Coloring(null, 0, null, Color.LIGHT_GRAY));
        type2Coloring.put(ColoringAttributes.TYPE_PARAMETER_USE, new Coloring(null, 0, null, Color.GREEN));
            
        type2Coloring.put(ColoringAttributes.UNDEFINED, new Coloring(null, 0, Color.RED, null));
        
        type2Coloring.put(ColoringAttributes.MARK_OCCURRENCES, new Coloring(null, 0, null, new Color( 236, 235, 163 )));
    }
    
    public static Coloring getColoring(Collection<ColoringAttributes> colorings) {
        colorings = EnumSet.copyOf(colorings);
//        System.err.println("getColoring(" + colorings + ")");
        if (colorings.contains(ColoringAttributes.UNUSED)) {
            colorings.removeAll(EnumSet.of(ColoringAttributes.ABSTRACT, ColoringAttributes.FIELD, ColoringAttributes.LOCAL_VARIABLE, ColoringAttributes.PARAMETER, ColoringAttributes.CLASS, ColoringAttributes.PRIVATE, ColoringAttributes.PACKAGE_PRIVATE, ColoringAttributes.PROTECTED, ColoringAttributes.PUBLIC, ColoringAttributes.UNDEFINED));
        }
        
        if (colorings.contains(ColoringAttributes.UNDEFINED)) {
            colorings.removeAll(EnumSet.of(ColoringAttributes.ABSTRACT, ColoringAttributes.FIELD, ColoringAttributes.LOCAL_VARIABLE, ColoringAttributes.PARAMETER, ColoringAttributes.CLASS, ColoringAttributes.PRIVATE, ColoringAttributes.PACKAGE_PRIVATE, ColoringAttributes.PROTECTED, ColoringAttributes.PUBLIC));
        }
        
        Coloring c = new Coloring(null, 0, null, null);
        
        for (ColoringAttributes type : ColoringAttributes.values()) {
//            System.err.println("type = " + type );
            if (colorings.contains(type)) {
//                System.err.println("type2Coloring.get(type)=" + type2Coloring.get(type));
                Coloring remote = type2Coloring.get(type);
                Coloring nue = remote.apply(c);
//                System.err.println("nue = " + nue );
//                System.err.println("remote = " + remote );
//                System.err.println("c = " + c );

                Font myFont     = c.getFont();
                Font remoteFont = remote.getFont();
                                Font nueFont = null;
                
                if (myFont == null) {
                    nueFont = remoteFont;
                } else {
                    if (remoteFont == null) {
                        nueFont = myFont;
                    } else {
                        int style = myFont.getStyle() | remoteFont.getStyle();
                        
                        nueFont = myFont.deriveFont(style);
                    }
                }
                
                c = new Coloring(nueFont, Coloring.FONT_MODE_APPLY_STYLE, nue.getForeColor(), nue.getBackColor(), nue.getUnderlineColor(), nue.getStrikeThroughColor(), nue.getWaveUnderlineColor());
                
//                System.err.println("c = " + c );
            }
        }
        
//        System.err.println("c = " + c );
        return c;
    }
    
}
