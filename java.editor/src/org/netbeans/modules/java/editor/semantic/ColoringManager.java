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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

/**
 *
 * @author Jan Lahoda
 */
public final class ColoringManager {

    private static final Map<ColoringAttributes, String> type2Coloring;
    private static final List<ColoringAttributes> attributesInOrder = Arrays.asList(ColoringAttributes.UNUSED,

    ColoringAttributes.ABSTRACT,

    ColoringAttributes.FIELD,
    ColoringAttributes.LOCAL_VARIABLE,
    ColoringAttributes.PARAMETER,
    ColoringAttributes.METHOD,
    ColoringAttributes.CONSTRUCTOR,
    ColoringAttributes.CLASS,
    ColoringAttributes.DEPRECATED,
    ColoringAttributes.STATIC,

    ColoringAttributes.PRIVATE,
    ColoringAttributes.PACKAGE_PRIVATE,
    ColoringAttributes.PROTECTED,
    ColoringAttributes.PUBLIC,

    ColoringAttributes.TYPE_PARAMETER_DECLARATION,
    ColoringAttributes.TYPE_PARAMETER_USE,

    ColoringAttributes.UNDEFINED,

    ColoringAttributes.MARK_OCCURRENCES);

    private static final Font ITALIC = SettingsDefaults.defaultFont.deriveFont(Font.ITALIC);
    private static final Font BOLD = SettingsDefaults.defaultFont.deriveFont(Font.BOLD);
    
    static {
        type2Coloring = new HashMap<ColoringAttributes, String>();
        
        type2Coloring.put(ColoringAttributes.UNUSED, "mod-unused");
        type2Coloring.put(ColoringAttributes.ABSTRACT, "mod-abstract");
        type2Coloring.put(ColoringAttributes.FIELD, "mod-field");
        type2Coloring.put(ColoringAttributes.LOCAL_VARIABLE, "mod-local-variable");
        type2Coloring.put(ColoringAttributes.PARAMETER, "mod-parameter");
        type2Coloring.put(ColoringAttributes.METHOD, "mod-method");
        type2Coloring.put(ColoringAttributes.CONSTRUCTOR, "mod-constructor");
        type2Coloring.put(ColoringAttributes.CLASS, "mod-class");
        type2Coloring.put(ColoringAttributes.DEPRECATED, "mod-deprecated");
        type2Coloring.put(ColoringAttributes.STATIC, "mod-static");
        
        type2Coloring.put(ColoringAttributes.PRIVATE, "mod-private");
        type2Coloring.put(ColoringAttributes.PACKAGE_PRIVATE, "mod-package-private");
        type2Coloring.put(ColoringAttributes.PROTECTED, "mod-protected");
        type2Coloring.put(ColoringAttributes.PUBLIC, "mod-public");
        
        type2Coloring.put(ColoringAttributes.TYPE_PARAMETER_DECLARATION, "mod-type-parameter-declaration");
        type2Coloring.put(ColoringAttributes.TYPE_PARAMETER_USE, "mod-type-parameter-use");
            
        type2Coloring.put(ColoringAttributes.UNDEFINED, "mod-use");
        
        type2Coloring.put(ColoringAttributes.MARK_OCCURRENCES, "mark-occurrences");
    }
    
    public static Coloring getColoring(Collection<ColoringAttributes> colorings) {
        FontColorSettings fcs = MimeLookup.getLookup(MimePath.get("text/x-java")).lookup(FontColorSettings.class);
        
        Coloring c = new Coloring(null, 0, null, null);
        
        for (ColoringAttributes type : attributesInOrder) {
//            System.err.println("type = " + type );
            if (colorings.contains(type)) {
//                System.err.println("type2Coloring.get(type)=" + type2Coloring.get(type));
                String key = type2Coloring.get(type);
                
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
