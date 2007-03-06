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

package org.netbeans.modules.languages.features;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.List;
import java.util.Map;
import java.util.Map;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.Context;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.modules.editor.settings.storage.api.FontColorSettingsFactory;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.Language.TokenType;


/**
 *
 * @author Jan Jancura
 */
public class ColorsManager {

    public static final String COLOR = "COLOR";
    
    static List<AttributeSet> getColors (Language l, ASTPath path, Document doc) {
        List<AttributeSet> result = new ArrayList<AttributeSet> ();
        Context context = SyntaxContext.create (doc, path);
        List<Feature> fs = l.getFeatures (COLOR, path);
        Iterator<Feature> it = fs.iterator ();
        while (it.hasNext ()) {
            Feature f = it.next ();
            if (!f.getBoolean ("condition", context, true)) continue;
            result.add (createColoring (f));
        }
        return result;
    }
    
    public static void initColorings (Language l) {
        System.out.println("mimeType " + l.getMimeType ());
        FontColorSettingsFactory fcsf = EditorSettings.getDefault ().
            getFontColorSettings (new String[] {l.getMimeType ()});
        Map<String,AttributeSet> colorsMap = new HashMap<String,AttributeSet> ();
        Iterator<Language> it = l.getImportedLanguages ().iterator ();
        while (it.hasNext ())
            addColors (colorsMap, it.next ());
        if (l.getMimeType().equals("text/html2"))
            System.out.println("");
        addColors (colorsMap, l);
        fcsf.setAllFontColorsDefaults ("NetBeans", colorsMap.values ());
        fcsf.setAllFontColors ("NetBeans", colorsMap.values ());
    }
    
    private static void addColors (Map<String,AttributeSet> colorsMap, Language l) {
        Map<String,AttributeSet> defaultsMap = getDefaultColors ();
        List<Feature> list = l.getFeatures ("COLOR");
        Iterator<Feature> it = list.iterator ();
        while (it.hasNext ()) {
            Feature f = it.next ();
            AttributeSet as = createColoring (f);
            colorsMap.put (
                (String) as.getAttribute (StyleConstants.NameAttribute),
                as
            );
        }
        
        Iterator<TokenType> it2 = l.getTokenTypes ().iterator ();
        while (it2.hasNext ()) {
            TokenType token = it2.next ();
            String type = token.getType ();
            if (colorsMap.containsKey (type)) continue;
            SimpleAttributeSet sas = new SimpleAttributeSet ();
            sas.addAttribute (StyleConstants.NameAttribute, type);
            sas.addAttribute (EditorStyleConstants.DisplayName, type);
            String def = type;
            int i = def.lastIndexOf ('_');
            if (i > 0) def = def.substring (i + 1);
            if (defaultsMap.containsKey (def))
                sas.addAttribute (EditorStyleConstants.Default, def);
            colorsMap.put (type, sas);
        }
    }
    
    private static List<AttributeSet> getColors (Language l) {
        List<Feature> list = l.getFeatures ("COLORS");
        List<AttributeSet> result = new ArrayList<AttributeSet> ();
        Iterator<Feature> it = list.iterator ();
        while (it.hasNext ()) {
            Feature f = it.next ();
            result.add (createColoring (f));
        }
        return result;
    }
    
    private static AttributeSet createColoring (Feature f) {
        String colorName = (String) f.getValue ("color_name");
        if (colorName == null)
            colorName = f.getSelector ().getAsString ();
        return createColoring (
            colorName,
            (String) f.getValue ("default_coloring"),
            (String) f.getValue ("foreground_color"),
            (String) f.getValue ("background_color"),
            (String) f.getValue ("underline_color"),
            (String) f.getValue ("wave_underline_color"),
            (String) f.getValue ("strike_through_color"),
            (String) f.getValue ("font_name"),
            (String) f.getValue ("font_type")
        );
    }
    
    private static AttributeSet createColoring (
        String colorName, 
        String defaultColor,
        String foreground,
        String background,
        String underline,
        String waveunderline,
        String strikethrough,
        String fontName,
        String fontType
    ) {
        SimpleAttributeSet coloring = new SimpleAttributeSet ();
        coloring.addAttribute (StyleConstants.NameAttribute, colorName);
        if (defaultColor != null)
            coloring.addAttribute (EditorStyleConstants.Default, defaultColor);
        if (foreground != null)
            coloring.addAttribute (StyleConstants.Foreground, readColor (foreground));
        if (background != null)
            coloring.addAttribute (StyleConstants.Background, readColor (background));
        if (strikethrough != null)
            coloring.addAttribute (StyleConstants.StrikeThrough, readColor (strikethrough));
        if (underline != null)
            coloring.addAttribute (StyleConstants.Underline, readColor (underline));
        if (waveunderline != null)
            coloring.addAttribute (EditorStyleConstants.WaveUnderlineColor, readColor (waveunderline));
        if (fontName != null)
            coloring.addAttribute (StyleConstants.FontFamily, fontName);
        if (fontType != null) {
            if (fontType.toLowerCase ().indexOf ("bold") >= 0)
                coloring.addAttribute (StyleConstants.Bold, Boolean.TRUE);
            if (fontType.toLowerCase ().indexOf ("italic") >= 0)
                coloring.addAttribute (StyleConstants.Italic, Boolean.TRUE);
        }
        return coloring;
    }
    
    private static Map<String,Color> colors = new HashMap<String,Color> ();
    static {
        colors.put ("black", Color.black);
        colors.put ("blue", Color.blue);
        colors.put ("cyan", Color.cyan);
        colors.put ("darkGray", Color.darkGray);
        colors.put ("gray", Color.gray);
        colors.put ("green", Color.green);
        colors.put ("lightGray", Color.lightGray);
        colors.put ("magenta", Color.magenta);
        colors.put ("orange", Color.orange);
        colors.put ("pink", Color.pink);
        colors.put ("red", Color.red);
        colors.put ("white", Color.white);
        colors.put ("yellow", Color.yellow);
    }
    
    static Color readColor (String color) {
        if (color == null) return null;
        Color result = (Color) colors.get (color);
        if (result == null)
            result = Color.decode (color);
        return result;
    }
    
//    public static Map<String,AttributeSet> getColorMap (Language l) {
//        Map defaultsMap = getDefaultColors ();
//        Map<String,AttributeSet> colorsMap = getCurrentColors (l);
//        Iterator<TokenType> it = l.getTokenTypes ().iterator ();
//        while (it.hasNext ()) {
//            TokenType token = it.next ();
//            List<SimpleAttributeSet> colors = (List<SimpleAttributeSet>) getFeature 
//                (Language.COLOR, token.getType ());
//            if (colors != null)
//                for (Iterator<SimpleAttributeSet> it2 = colors.iterator (); it2.hasNext ();) {
//                    SimpleAttributeSet as = it2.next();
//                    String id = (String) as.getAttribute ("color_name"); // NOI18N
//                    if (id == null)
//                        id = token.getType ();
//                    addColor (id, as, colorsMap, defaultsMap);
//                }
//            else
//                addColor (token.getType (), null, colorsMap, defaultsMap);
//        }
//        
//        //List<AttributeSet> colors = getColors (l);
//        Map m = (Map) features.get (Language.COLOR);
//        if (m == null)
//            return Collections.<String,AttributeSet>emptyMap ();
//        Iterator<String> it2 = m.keySet ().iterator ();
//        while (it2.hasNext ()) {
//            String type = it2.next ();
//            if (colorsMap.containsKey (type))
//                continue;
//            Object obj = m.get (type);
//            if (obj != null) {
//                for (Iterator iter = ((List)obj).iterator(); iter.hasNext(); ) {
//                    SimpleAttributeSet as = (SimpleAttributeSet) iter.next();
//                    addColor (type, as, colorsMap, defaultsMap);
//                }
//            }
//        }
//        addColor ("error", null, colorsMap, defaultsMap);
//        return colorsMap;
//    }
    
    private static void addColor (
        String tokenType, 
        SimpleAttributeSet sas,
        Map<String,AttributeSet> colorsMap, 
        Map<String,AttributeSet> defaultsMap
    ) {
        if (sas == null)
            sas = new SimpleAttributeSet ();
        else
            sas = new SimpleAttributeSet (sas);
        String colorName = (String) sas.getAttribute (StyleConstants.NameAttribute);
        if (colorName == null)
            colorName = tokenType;
        sas.addAttribute (StyleConstants.NameAttribute, colorName);
        sas.addAttribute (EditorStyleConstants.DisplayName, colorName);
        if (!sas.isDefined (EditorStyleConstants.Default)) {
            String def = colorName;
            int i = def.lastIndexOf ('_');
            if (i > 0) def = def.substring (i + 1);
            if (defaultsMap.containsKey (def))
                sas.addAttribute (EditorStyleConstants.Default, def);
        }
        colorsMap.put (colorName, sas);
    }
    
    private static Map<String,AttributeSet> getDefaultColors () {
        Collection<AttributeSet> defaults = EditorSettings.getDefault ().
            getDefaultFontColorDefaults ("NetBeans");
        Map<String,AttributeSet> defaultsMap = new HashMap<String,AttributeSet> ();
        Iterator<AttributeSet> it = defaults.iterator (); // check if IDE Defaults module is installed
        while (it.hasNext ()) {
            AttributeSet as = it.next ();
            defaultsMap.put (
                (String) as.getAttribute (StyleConstants.NameAttribute),
                as
            );
        }
        return defaultsMap;
    }
    
    private static Map getCurrentColors (Language l) {
        // current colors
        FontColorSettingsFactory fcsf = EditorSettings.getDefault ().
            getFontColorSettings (new String[] {l.getMimeType ()});
        Collection<AttributeSet> colors = fcsf.getAllFontColors ("NetBeans");
        Map<String,AttributeSet> colorsMap = new HashMap<String,AttributeSet> ();
        Iterator<AttributeSet> it = colors.iterator ();
        while (it.hasNext ()) {
            AttributeSet as = it.next ();
            colorsMap.put (
                (String) as.getAttribute (StyleConstants.NameAttribute),
                as
            );
        }
        return colorsMap;
    }
}
