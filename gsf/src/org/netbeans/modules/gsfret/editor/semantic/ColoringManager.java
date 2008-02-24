/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.gsfret.editor.semantic;

import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
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
import org.netbeans.modules.gsf.api.ColoringAttributes;
import static org.netbeans.modules.gsf.api.ColoringAttributes.*;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 * The main modification to this file is to change the static methods
 * into instance methods, and change the ColoringManager from a singleton
 * (hardcoded to the Java mimetype) into a per-mimetype ColoringManager
 * stashed in each Language.
 * 
 *
 * @author Jan Lahoda
 */
public final class ColoringManager {
    private String mimeType;
    private final Map<Set<ColoringAttributes>, String> type2Coloring;
    
    private static final Font ITALIC = SettingsDefaults.defaultFont.deriveFont(Font.ITALIC);
    private static final Font BOLD = SettingsDefaults.defaultFont.deriveFont(Font.BOLD);

    
    public ColoringManager(String mimeType) {
        this.mimeType = mimeType;
        
        type2Coloring = new LinkedHashMap<Set<ColoringAttributes>, String>();
        
        put("mark-occurrences", MARK_OCCURRENCES);
        put("mod-type-parameter-use", TYPE_PARAMETER_USE);
        put("mod-type-parameter-declaration", TYPE_PARAMETER_DECLARATION);
//        put("mod-enum-declaration", ENUM, DECLARATION);
//        put("mod-annotation-type-declaration", ANNOTATION_TYPE, DECLARATION);
//        put("mod-interface-declaration", INTERFACE, DECLARATION);
//        put("mod-class-declaration", CLASS, DECLARATION);
//        put("mod-constructor-declaration", CONSTRUCTOR, DECLARATION);
//        put("mod-method-declaration", METHOD, DECLARATION);
//        put("mod-parameter-declaration", PARAMETER, DECLARATION);
//        put("mod-local-variable-declaration", LOCAL_VARIABLE, DECLARATION);
//        put("mod-field-declaration", FIELD, DECLARATION);
        put("mod-enum", ENUM);
        put("mod-annotation-type", ANNOTATION_TYPE);
        put("mod-interface", INTERFACE);
        put("mod-class", CLASS);
        put("mod-global", GLOBAL);
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

        put("mod-local-variable-use", LOCAL_VARIABLE_USE);
        put("mod-local-variable-declaration", LOCAL_VARIABLE_DECLARATION);
        put("mod-parameter-declaration", PARAMETER_DECLARATION);
        put("mod-parameter-use", PARAMETER_USE);
        put("mod-java-method-use", JAVA_METHOD_USE);
        put("mod-java-field-use", JAVA_FIELD_USE);
        put("mod-java-constructor-use", JAVA_CONSTRUCTOR_USE);
        put("mod-class-use", CLASS_USE);
        put("mod-class-declaration", CLASS_DECLARATION);
        put("mod-java-interface-use", JAVA_INTERFACE_USE);
        put("mod-attribute-declaration", ATTRIBUTE_DECLARATION);
        put("mod-function-declaration", FUNCTION_DECLARATION);
        put("mod-operation-declaration", OPERATION_DECLARATION);
        put("mod-operation-use", OPERATION_USE);
        put("mod-attribute-use", ATTRIBUTE_USE);
        put("mod-functions-use", FUNCTION_USE); 

        put("mod-static-field", STATICFIELD); 
        put("mod-static-method", STATICMETHOD); 
        put("mod-regexp", REGEXP); 
    }
    
    private void put(String coloring, ColoringAttributes... attributes) {
        Set<ColoringAttributes> attribs = EnumSet.copyOf(Arrays.asList(attributes));
        
        type2Coloring.put(attribs, coloring);
    }
    
    public Coloring getColoring(Collection<ColoringAttributes> colorings) {
        FontColorSettings fcs = MimeLookup.getLookup(MimePath.get(mimeType)).lookup(FontColorSettings.class);
        
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
