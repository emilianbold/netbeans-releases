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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.editor.semantic;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.SettingsDefaults;
import static org.netbeans.modules.java.editor.semantic.ColoringAttributes.*;

/**
 *
 * @author Jan Lahoda
 */
public final class ColoringManager {

    private static final Map<Set<ColoringAttributes>, String> type2Coloring;
    
    private static final Font ITALIC = SettingsDefaults.defaultFont.deriveFont(Font.ITALIC);
    private static final Font BOLD = SettingsDefaults.defaultFont.deriveFont(Font.BOLD);
    
    static {
        type2Coloring = new LinkedHashMap<Set<ColoringAttributes>, String>();
        
        put("mark-occurrences", MARK_OCCURRENCES);
        put("mod-type-parameter-use", TYPE_PARAMETER_USE);
        put("mod-type-parameter-declaration", TYPE_PARAMETER_DECLARATION);
        put("mod-enum-declaration", ENUM, DECLARATION);
        put("mod-annotation-type-declaration", ANNOTATION_TYPE, DECLARATION);
        put("mod-interface-declaration", INTERFACE, DECLARATION);
        put("mod-class-declaration", CLASS, DECLARATION);
        put("mod-constructor-declaration", CONSTRUCTOR, DECLARATION);
        put("mod-method-declaration", METHOD, DECLARATION);
        put("mod-parameter-declaration", PARAMETER, DECLARATION);
        put("mod-local-variable-declaration", LOCAL_VARIABLE, DECLARATION);
        put("mod-field-declaration", FIELD, DECLARATION);
        put("mod-enum", ENUM);
        put("mod-annotation-type", ANNOTATION_TYPE);
        put("mod-interface", INTERFACE);
        put("mod-class", CLASS);
        put("mod-constructor", CONSTRUCTOR);
        put("mod-method", METHOD);
        put("mod-parameter", PARAMETER);
        put("mod-local-variable", LOCAL_VARIABLE);
        put("mod-field", FIELD);
        put("mod-public", PUBLIC);
        put("mod-protected", PROTECTED);
        put("mod-package-private", PACKAGE_PRIVATE);
        put("mod-private", PRIVATE);
        put("mod-static", STATIC);
        put("mod-abstract", ABSTRACT);
        put("mod-deprecated", DEPRECATED);
        put("mod-undefined", UNDEFINED);
        put("mod-unused", UNUSED);
    }
    
    private static void put(String coloring, ColoringAttributes... attributes) {
        Set<ColoringAttributes> attribs = EnumSet.copyOf(Arrays.asList(attributes));
        
        type2Coloring.put(attribs, coloring);
    }
    
    public static Coloring getColoring(Collection<ColoringAttributes> colorings) {
        FontColorSettings fcs = MimeLookup.getLookup(MimePath.get("text/x-java")).lookup(FontColorSettings.class);
        
        Coloring c = new Coloring(null, 0, null, null);
        
        colorings = colorings.size() > 0 ? EnumSet.copyOf(colorings) : EnumSet.noneOf(ColoringAttributes.class);
        
        for (Entry<Set<ColoringAttributes>, String> attribs2Colorings : type2Coloring.entrySet()) {
//            System.err.println("type = " + type );
            if (colorings.containsAll(attribs2Colorings.getKey())) {
//                System.err.println("type2Coloring.get(type)=" + type2Coloring.get(type));
                String key = attribs2Colorings.getValue();
                
                colorings.removeAll(attribs2Colorings.getKey());
                
                if (key != null) {
                    AttributeSet colors = fcs.getTokenFontColors(key);
                    
                    if (colors == null) {
                        Logger.getLogger(ColoringManager.class.getName()).log(Level.SEVERE, "no colors for: {0}", key);
                        continue;
                    }
                    
                    Color foreColor = (Color) colors.getAttribute(StyleConstants.Foreground);
                    Color backColor = (Color) colors.getAttribute(StyleConstants.Background);
                    Color strikeThroughColor = (Color) colors.getAttribute(StyleConstants.StrikeThrough);
                    Color underlineColor = (Color) colors.getAttribute(StyleConstants.Underline);
                    Color waveUnderlineColor = (Color) colors.getAttribute(EditorStyleConstants.WaveUnderlineColor);
                    boolean isBold  = colors.getAttribute(StyleConstants.Bold) == Boolean .TRUE;
                    boolean isItalic = colors.getAttribute(StyleConstants.Italic) == Boolean .TRUE;
                    
                    Font font = c.getFont();
                    int  fontMode = font != null ? c.getFontMode() : 0;
                    
                    if (foreColor == null)
                        foreColor = c.getForeColor();
                    if (backColor == null)
                        backColor = c.getBackColor();
                    if (isBold) {
                        if (font != null) {
                            font = font.deriveFont(font.isItalic() ? (Font.BOLD | Font.ITALIC) : Font.BOLD);
                        } else {
                            font = BOLD;
                        }
                        fontMode |= Coloring.FONT_MODE_APPLY_STYLE;
                    }
                    if (isItalic) {
                        if (font != null) {
                            font = font.deriveFont(font.isBold() ? (Font.BOLD | Font.ITALIC) : Font.ITALIC);
                        } else {
                            font = ITALIC;
                        }
                        fontMode |= Coloring.FONT_MODE_APPLY_STYLE;
                    }
                    if (underlineColor == null)
                        underlineColor = c.getUnderlineColor();
                    if (strikeThroughColor == null)
                        strikeThroughColor = c.getStrikeThroughColor();
                    if (waveUnderlineColor == null)
                        waveUnderlineColor = c.getWaveUnderlineColor();
                    
                    c = new Coloring(font, fontMode, foreColor, backColor, underlineColor, strikeThroughColor, waveUnderlineColor);
                }
                
//                System.err.println("c = " + c );
            }
        }
        
//        System.err.println("c = " + c );
        return c;
    }
    
}
